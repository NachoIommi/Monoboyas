import os
import re

def main():
    src_dir = '/Users/juanmanuelcristobo/Desktop/ADS/ProyectoMonoBoyas/src'
    java_files = []
    
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
                
    # Read all files
    file_contents = {}
    for jf in java_files:
        try:
            with open(jf, 'r', encoding='utf-8') as f:
                file_contents[jf] = f.read()
        except Exception as e:
            pass
            
    # Simple regex to find method definitions
    method_regex = re.compile(r'(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?(?:[\w<>,\[\]\?]+\s+)+(\w+)\s*\(')
    attr_regex = re.compile(r'(?:private|protected)\s+(?:static\s+)?(?:final\s+)?(?:[\w<>,\[\]\?]+\s+)+(\w+)\s*[=;]')

    ignore_methods = {'main', 'toString', 'hashCode', 'equals'}
    
    symbols = {} # name -> { 'type': 'method'|'attr', 'file': ..., 'count': 0, 'defined_in': [] }

    for jf, content in file_contents.items():
        # Methods
        for match in method_regex.finditer(content):
            name = match.group(1)
            # ignore constructors (capital letter)
            if name and name[0].isupper():
                continue
            if name not in ignore_methods and not name.startswith('get') and not name.startswith('set'):
                if name not in symbols:
                    symbols[name] = {'type': 'method', 'file': jf, 'count': 0, 'defined_in': [jf]}
                else:
                    if jf not in symbols[name]['defined_in']:
                        symbols[name]['defined_in'].append(jf)
                        
        # Attributes
        for match in attr_regex.finditer(content):
            name = match.group(1)
            if name.isupper():
                continue
            if name not in symbols:
                symbols[name] = {'type': 'attribute', 'file': jf, 'count': 0, 'defined_in': [jf]}
            else:
                if jf not in symbols[name]['defined_in']:
                    symbols[name]['defined_in'].append(jf)

    # Count
    for name, data in symbols.items():
        name_regex = re.compile(r'\b' + re.escape(name) + r'\b')
        count = 0
        for jf, content in file_contents.items():
            count += len(name_regex.findall(content))
        data['count'] = count
        
    # Print results
    print("Potential Unused Methods and Attributes (heuristic based):")
    for name, data in symbols.items():
        if data['count'] <= len(data['defined_in']):
            print(f"- {data['type']} '{name}' in {os.path.basename(data['file'])}")

if __name__ == '__main__':
    main()
