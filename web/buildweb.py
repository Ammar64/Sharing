#!/bin/python3
import os
import xml.etree.ElementTree as ET
import shutil


BUILD_DIRECTORY = "build"



def getStringsDict(lang: str):
    strings_tree = ET.parse(os.path.join("strings/", lang, "strings.xml"))
    res_root = strings_tree.getroot()
    strings_elements = res_root.findall("string")

    strings_dict = {}
    for i in strings_elements:
        strings_dict[i.attrib["name"]] = i.text
    return strings_dict

def buildHTMLFile(lang: str):
    with open('html/index.html') as html_file:
        strings_map = getStringsDict(lang)
        html_string = html_file.read()
        translated_html = html_string.format_map(strings_map)

        with open(os.path.join(BUILD_DIRECTORY, f"index_{lang}.html"), "w") as translated_html_file:
            translated_html_file.write(translated_html)
        

if __name__ == "__main__":
    # Make sure we are in the right dir
    os.chdir(os.path.dirname(__file__))

    # Make sure it doesn't exist first
    if os.path.exists(BUILD_DIRECTORY):
        shutil.rmtree(BUILD_DIRECTORY)
    # We will generate html files here
    os.mkdir(BUILD_DIRECTORY)

    langs = os.listdir("strings")

    for lang in langs:
        buildHTMLFile(lang)
