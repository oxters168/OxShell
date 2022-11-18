# About
OxShell is a launcher for android. It is an attempt to recreate the feel of the Sony PSP in more modern devices.

At the moment the feature list includes:
- Controller support
- Live xmb-like background
- Built in file explorer
- Launch roms directly into emulators that support it
- Install/uninstall apps

There are many more features planned to be added, but I am just one developer so it'll likely take time for them to reach.
In its current state there is no apk or release available yet as I believe it is still early in development, but if you'd like
to try this launcher anyways please follow the build instructions.

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
