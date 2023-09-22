import sys
import pickle
from sklearn.ensemble import RandomForestClassifier
import m2cgen as m2c

map_functions = {
    'c':    m2c.export_to_c,
    'java': m2c.export_to_java
}

def wrong_usage():
    print(f"usage: python port.py {'|'.join(map_functions.keys())} [-o filename]")
    exit()

if __name__ == "__main__":
    if len(sys.argv) <= 1 or sys.argv[1] not in map_functions.keys():
        wrong_usage()

    mode = sys.argv[1]
    filename = 'output.' + mode

    if '-o' in sys.argv:
        fflag_idx = sys.argv.index('-o')
        if len(sys.argv) <= fflag_idx + 1:
            wrong_usage()
        else:
            filename = sys.argv[fflag_idx + 1]

    with open('test_model.pkl', 'rb') as pkl:
        print("Loading pickle file...", end="")
        rf: RandomForestClassifier = pickle.load(pkl)  # 미리 학습된 테스트용 객체
        print(" Done.")

        print("Generating code...", end="")
        code = map_functions[mode](rf)
        print(" Done.")

        print("Writing to file...", end="")
        with open(filename, 'w') as ofile:
            ofile.write(code)
        print(" Done.")
