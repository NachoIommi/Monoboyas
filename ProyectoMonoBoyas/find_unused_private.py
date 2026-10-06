import os
import re

def main():
    src_dir = '/Users/juanmanuelcristobo/Desktop/ADS/ProyectoMonoBoyas/src'
    java_files = []
    
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
                
    # Regex to find private fields
    # private [modifiers] Type name [= ...];
    private_field_regex = re.compile(r'private\s+(?:static\s+)?(?:final\s+)?(?:[\w<>,\[\]\?]+\s+)+(\w+)\s*[=;]')
    
    # Regex to find private methods
    # private [modifiers] Type name(...)
    private_method_regex = re.compile(r'private\s+(?:static\s+)?(?:final\s+)?(?:[\w<>,\[\]\?]+\s+)+(\w+)\s*\(')

    print("Unused Private Fields and Methods:")
    
    for jf in java_files:
        try:
            with open(jf, 'r', encoding='utf-8') as f:
                content = f.read()
                
            fields = []
            for m in private_field_regex.finditer(content):
                name = m.group(1)
                # Ignore constants
                if name.isupper(): continue
                fields.append(name)
                
            methods = []
            for m in private_method_regex.finditer(content):
                name = m.group(1)
                if name.isupper() and name[0].isupper(): continue
                methods.append(name)
                
            for field in set(fields):
                # Count occurrences of field in this file
                # If count is 1, it's unused (only the definition)
                count = len(re.findall(r'\b' + re.escape(field) + r'\b', content))
                if count == 1:
                    print(f"File: {os.path.basename(jf)} -> Unused Private Field: '{field}'")
                    
            for method in set(methods):
                count = len(re.findall(r'\b' + re.escape(method) + r'\b', content))
                if count == 1:
                    print(f"File: {os.path.basename(jf)} -> Unused Private Method: '{method}'")
                    
        except Exception as e:
            pass

if __name__ == '__main__':
    main()
