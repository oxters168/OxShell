# About Ox Shell

Ox Shell is a launcher for android. It is an attempt to recreate the feel of a classic video game system.


![xmb3](https://user-images.githubusercontent.com/15348986/221724810-ca4237f8-e889-46b8-9282-a8684b9a2c44.gif)



## Features

- [x] Controller support
- [x] Live wallpaper service
- [x] Built in file explorer
- [x] Launch roms directly into emulators that support it
- [x] Create custom associations for roms and other files
- [x] Install and uninstall apps
- [x] Customizable home menu
- [x] Customizable controls
- [x] Use home/recent apps/etc through controller from anywhere
- [ ] Add compression/extraction options to file explorer
- [x] Show prompt before deleting files in file explorer
- [ ] Show which files are in the copy/cut clipboard
- [ ] Show any file operation errors
- [ ] Show progress of file operations
- [ ] Add ftp support to file explorer
- [ ] Add start menu to file explorer that lists locations and is customizable
- [ ] Add more default associations
- [x] Make default generic associations (image/video/audio)
- [ ] Make a chooser in the create association menu to pick a template from the defaults
- [ ] Add more default shaders
- [x] Add UI scale option
- [x] Add text scale option
- [ ] Add setting to choose typeface
- [x] Optimize home menu performance
- [ ] Remember where user was in the home menu when coming back
- [ ] Make it possible to choose a default association for an extension if more than one exists
- [ ] Make it possible to pick which association to launch a file with when multiple exist for it
- [ ] Add gyro/accelerometer support to shaders
- [ ] Support ability to have multiple shader files
- [ ] Make it possible to set values of variables when setting shader as background if the shader has any to be set
- [ ] Add ability to write a shader directly in app with the ability to preview
- [x] Add a video player
- [x] Add a music player
- [ ] Add a photo viewer
- [ ] Make it possible to set the rect of the background image chosen when user setting wallpaper to image
- [ ] Add a web browser

It'll likely take time for these features to reach, as I am just one developer.
Please consider supporting this project by purchasing it on the store.

<a href='https://play.google.com/store/apps/details?id=com.OxGames.OxShell'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height=60px/></a>

# Build for Yourself
- Clone the project or download the zip using the green code button above.
- Open Android Studio and browse to the directory you cloned/downloaded to and select it.

<details><summary>Creating a debug variant</summary>

* Make sure the build variant selected is debug
* Run it!
</details>
<details><summary>Creating a release variant</summary>

* Add your keystore file in the `app/keystores` folder
* Create a `keystore.properties` file in the `app/keystores` folder with the following contents:
```
storeFile keystores/[keystore-filename]
storePassword [store-password]
keyAlias [key-alias]
keyPassword [key-password]
```
* Set your build variant to release
* Run it!
</details>
