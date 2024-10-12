Sharing
-------
[![Reproducible builds](https://github.com/Ammar64/Sharing/actions/workflows/rb-verify.yml/badge.svg)](https://github.com/Ammar64/Sharing/actions/workflows/rb-verify.yml/)

Share files and apps over HTTP.

You need the other device to be connected to the same network. just toggle on the server and scan the QR Code on other device and you're good to go.
Files sent from browser to the app can be found in Sharing/ folder in your internal storage.
You can always disable uploads in the app settings. 

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
alt="Get it on IzzyOnDroid"
height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.ammar.sharing)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.ammar.sharing/)

Or download the latest APK from the [Releases Section](https://github.com/Ammar64/Sharing/releases/latest).

Important issue
-----
We need a new icon design for this app.  
A new stable update is almost ready but will not be released without the new icon and I'm not a good designer 😁.  
See this [issue](https://github.com/Ammar64/Sharing/issues/19) for more details 

TODO
-----------------
- [ ] Add the ability to share files and/or apps app-to-app
- [ ] Add the ability to stream video and/or audio from camera, screen and/or microphone to the browser and the otherway around.
- [ ] Support sending files from SD Card.
- [ ] Redesign the Add Files Activity so it can detect real time file system updates and make the UI responsive for larger devices
- [ ] Add the ability to send and receive text using websocket
- [x] Make a script that will help translate html.


Screen shots
-----------------

<p align="center" class="scroll" >
     <img width="200px" src="https://github.com/Ammar64/Sharing/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/0.jpg" alt="App screen shot">
     &nbsp;&nbsp;&nbsp;
     <img width="200px" src="https://github.com/Ammar64/Sharing/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" alt="App screen shot">
     &nbsp;&nbsp;&nbsp;
     <img width="200px" src="https://github.com/Ammar64/Sharing/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" alt="App screen shot">
     &nbsp;&nbsp;&nbsp;
     <img width="200px" src="https://github.com/Ammar64/Sharing/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" alt="App screen shot">
     &nbsp;&nbsp;&nbsp;
     <img width="200px" src="https://github.com/Ammar64/Sharing/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" alt="App screen shot">
     &nbsp;&nbsp;&nbsp;
     <img width="200px" src="https://github.com/Ammar64/Sharing/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" alt="App screen shot">
     &nbsp;&nbsp;&nbsp;
</p>

Translation
----------------
When translating [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml) consider also translating [web/strings/**/strings.xml](web/strings/en/strings.xml) to translate the webpage that shows on the other device.
[Learn more](web/README.md)

Note
----------------
Tutorial is available at https://ammar64.github.io/Sharing/Tutorial <br>
Clicking the tutorial button in the app will open the previous link in a web browser in version 1.5 and up<br>
Feel free to contribute to it in the [tutorial](https://github.com/Ammar64/Sharing/tree/tutorial) branch<br>

License
-------------
    Copyright (C) 2024 Sharing authors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

