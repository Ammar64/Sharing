import os.path
from pathlib import Path

def list_files_pathlib(path=Path("./dist/web_app")):
    paths_list = []
    base_path = "./dist/web_app"
    for entry in path.iterdir():
        if entry.is_file():
            paths_list.append(os.path.relpath(entry.absolute(), base_path))
        elif entry.is_dir():
            paths_list.extend(list_files_pathlib(entry))
    return paths_list

def remove_map_files():
    path = Path("./dist/web_app")
    for entry in path.iterdir():
        if entry.is_file() and entry.name.endswith(".map"):
            os.remove( entry )

def main():
    remove_map_files()
    web_app_files_list_file = open("./dist/web_app_files_list.txt", "w")
    web_app_files_list_file.writelines( line + '\n' for line in list_files_pathlib() )

main()