import os
import re

def main():
    src_dir = '/Users/juanmanuelcristobo/Desktop/ADS/ProyectoMonoBoyas/src'
    java_files = []
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))

    contents = {}
    for jf in java_files:
        with open(jf, 'r', encoding='utf-8') as f:
            contents[jf] = f.read()

    method_regex = re.compile(r'(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?(?:[\w<>,\[\]\?]+\s+)+(\w+)\s*\(')
    
    symbols = set()
    for jf, content in contents.items():
        for m in method_regex.finditer(content):
            name = m.group(1)
            # Skip Constructors
            if name[0].isupper(): continue
            symbols.add(name)

    ignore = {'main', 'equals', 'hashCode', 'toString', 'valueOf', 'values', 'run'}
    for s in ignore:
        if s in symbols:
            symbols.remove(s)

    results = []
    for sym in symbols:
        # Ignore getters/setters heuristically if they are simple
        if sym.startswith('get') or sym.startswith('set'):
            pass # Actually let's include them to see
            
        count = 0
        for jf, content in contents.items():
            count += len(re.findall(r'\b' + re.escape(sym) + r'\b', content))
        results.append((count, sym))

    results.sort()
    print("Methods with exactly 1 occurrence (only the definition):")
    for count, sym in results:
        if count == 1:
            print(f"- {sym}")

if __name__ == '__main__':
    main()
