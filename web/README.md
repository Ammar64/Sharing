What is the purpose of this folder ?
-------

This is the web side of the app.<br>
The purpose of this directory is to automatically translate each html files to multiple languages so when I update the html code I don't have to update it for every language.

`translate-html.py` is a script that translate all html files in the following structure `src/pages/<html_file_name>/<html_file_name>.html` using a `strings.xml` file similar to how android does it.

when it finds a directory named `foo` under `pages` it expects a `foo.html` under that `foo` directory
the `strings.xml` directory contains subdirs like `ar`, `en`, `zh`, `du` each subdir contains a `strings.xml` file.

if the `strings.xml` file contains a string like this
```xml
<string name="text">This is some text</string>
```
You can put the string name inside a bracket like this
```html
<body>
    <p>{text}</p>
</body>
```
`{text}` will be replaced with `This is some text`


if a string contains an html tag like `<a>` use `\&lt;` and `\&gt;` so xml treat it as a string instead of a new xml tag


when building the web the html files will be generated this way
```
build
|__ web_app
    |__ pages
        |__ example_page_name1
        |   |__ example_page_name1-ar.html
        |   |__ example_page_name1-en.html
        |   |__ example_page_name1-zh.html
        |   |__ example_page_name1-du.html
        |   |__ scripts
        |   |   |__ ...
        |   |__ style
        |       |__ ...
        |__ example_page_name2
            |__ example_page_name2-ar.html
            |__ example_page_name2-en.html
            |__ example_page_name2-zh.html
            |__ example_page_name2-du.html
            |__ style
                |__ ...
```

typescript will output js files and place them in `build/web_app/` while keeping the `src/` structure.