import sys, os
import pickle
from sklearn.ensemble import RandomForestClassifier
import m2cgen as m2c

map_functions = {
    'c':    m2c.export_to_c,
    'java': m2c.export_to_java
}

def wrong_usage():
    print(f"usage: python port.py {'|'.join(map_functions.keys())} [-o filename] [-i path]")
    exit()

# find path to file name from start_dir
def find_path(file_name, start_dir):
   for root, dirs, files in os.walk(start_dir):
        for file in files:
            if file == file_name:
                return root
   return None

if __name__ == "__main__":
    if len(sys.argv) <= 1 or sys.argv[1] not in map_functions.keys():
        wrong_usage()

    mode = sys.argv[1]
    file_name = 'Model.' + mode
    model_path = 'model.pkl'

    if '-o' in sys.argv:
        fflag_idx = sys.argv.index('-o')
        if len(sys.argv) <= fflag_idx + 1:
            wrong_usage()
        else:
            file_name = sys.argv[fflag_idx + 1]
            if file_name[file_name.index('.')+1:] != mode:
                print(f'filename "{file_name}" does not math with target language "{mode}"')
                wrong_usage()

    if '-i' in sys.argv:
        fflag_idx = sys.argv.index('-i')
        if len(sys.argv) <= fflag_idx + 1:
            wrong_usage()
        else:
            model_path = sys.argv[fflag_idx + 1]

    package_name = ""
    main_activity = "MainActivity.kt"
    app_root = "../app"
    print(f"Finding {main_activity} in {app_root}")
    path_to_main = find_path(main_activity, app_root)

    # make package name with path to MainActivity.kt
    if path_to_main:
        package_name = '.'.join(path_to_main[path_to_main.index('com'):].split('/'))
        print(f"Found {main_activity}, package: {package_name}")
    else:
        print(f"Failed to find {main_activity}, cont'd...")

    with open(model_path, 'rb') as pkl:
        print("Loading pickle file...", end="")
        rf: RandomForestClassifier = pickle.load(pkl)  # 미리 학습된 테스트용 객체
        print(" Done.")

        print("Generating code...", end="")
        code = map_functions[mode](rf)
        print(" Done.")

        if path_to_main and mode == 'java':
            code = code.replace('Model', file_name[:file_name.index('.')])
            path_to_model = f"{app_root}/{path_to_main}/{file_name}"
            print(f"Writing to file {path_to_main}/{file_name}", end=" ")
            with open(path_to_model, 'w') as ofile:
                ofile.write(f"package {package_name};\n\n" + code)
            print(" Done.")

        print(f"Writing to file ./{file_name}", end=" ")
        with open(file_name, 'w') as ofile:
            ofile.write(code)
        print(" Done.")
