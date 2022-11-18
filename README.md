# About
OxShell is a launcher for android. It is an attempt to recreate the feel of the Sony PSP in more modern devices.

At the moment the feature list includes:
- Live xmb-like background
- Built in file explorer
- Launch roms directly into emulators that support it
- Install/Uninstall apps

There are many more features planned to be added, but I am just one developer so it'll likely take time for them to reach.
In its current state there is no apk or release available yet as I believe it is still early in development, but if you'd like
to try this launcher anyways please follow the build instructions.

# Build for Yourself
Clone the project or download into a zip using the green code button above. Open Android Studio and browse to the directory you cloned/downloaded
to and select it. For a debug build all you need to do is run it. If you'd like to create a release variant then you'll need to have a keystore file
in the `app/keystores` folder and create a `keystore.properties` file in the same directory with the following contents:
```
storeFile keystores/[keystore-filename]
storePassword [store-password]
keyAlias [key-alias]
keyPassword [key-password]
```
Once done you can set your build variant to release and then run.
