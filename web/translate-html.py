#!/bin/python3
import os
import xml.etree.ElementTree as ET
import shutil


BUILD_DIR = "build"
SRC_DIR = "src"

# directory where we put other html files
SRC_PAGES_DIR = os.path.join(SRC_DIR, "pages")
BUILD_PAGES_DIR = os.path.join(BUILD_DIR, "pages")


def getStringsDict(lang: str):
    strings_tree = ET.parse(os.path.join("strings/", lang, "strings.xml"))
    res_root = strings_tree.getroot()
    strings_elements = res_root.findall("string")

    strings_dict = {}
    for i in strings_elements:
        strings_dict[i.attrib["name"]] = i.text
    return strings_dict


def buildHTMLFiles(lang: str):
    strings_map = getStringsDict(lang)
    
    # Translate any other HTML file in pages directory
    pages = os.listdir(SRC_PAGES_DIR)
    for page in pages:
        with open(os.path.join(SRC_PAGES_DIR, page, f'{page}.html'), 'r') as html_page_file:
            html_string = html_page_file.read()
            translated_html = html_string.format_map(strings_map)
            
            PAGE_BUILD_PATH = os.path.join(BUILD_PAGES_DIR, page)
            with open(os.path.join(PAGE_BUILD_PATH, f'{page}-{lang}.html'), 'w') as translated_html_file:
                translated_html_file.write(translated_html)


if __name__ == "__main__":
    # Make sure we are in the right dir
    os.chdir(os.path.dirname(__file__))

    # Make sure it doesn't exist first
    if os.path.exists(BUILD_DIR):
        shutil.rmtree(BUILD_DIR)
    # We will generate html files here
    os.mkdir(BUILD_DIR)

    langs = os.listdir("strings")

    # if we have pages make a pages dir in the build dir and make its subdirs
    if  os.path.exists(SRC_PAGES_DIR):
        os.mkdir(BUILD_PAGES_DIR)
        pages = os.listdir(SRC_PAGES_DIR)
        for i in pages:
            os.mkdir(os.path.join(BUILD_PAGES_DIR, i))


    for lang in langs:
        buildHTMLFiles(lang)
