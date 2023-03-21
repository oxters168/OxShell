# About Ox Shell

Ox Shell is a launcher for android. It is an attempt to recreate the feel of a classic video game system.


![xmb3](https://user-images.githubusercontent.com/15348986/221724810-ca4237f8-e889-46b8-9282-a8684b9a2c44.gif)



At the moment the feature list includes:

- Controller support
- Live wallpaper service
- Built in file explorer
- Launch roms directly into emulators that support it
- Create custom associations for roms and other files
- Install and uninstall apps
- Customizable home menu

There are many more features planned to be added, but I am just one developer so it'll likely take time for them to reach.
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
