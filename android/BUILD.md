# Building APK from Command Line

If Android Studio's build menu is grayed out, you can build from the command line:

## Prerequisites
- Android SDK installed
- Java/JDK installed

## Build Debug APK

```bash
cd android
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Build Release APK

```bash
cd android
./gradlew assembleRelease
```

**Note:** Release builds require signing. You'll need to set up signing in `app/build.gradle` first.

## On Windows

Use `gradlew.bat` instead:
```bash
gradlew.bat assembleDebug
```

