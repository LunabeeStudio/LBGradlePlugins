# Module lb-android-flavors

### `studio.lunabee.plugins.android.flavors`

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

This plugin simplifies the configuration of an Android flavors. Usage:

In app `build.gradle.kts` (or in Android libraries):
```
plugins {
    alias(libs.plugins.lbAndroidFlavors)
}
```

After this configuration, you should be able to:
- launch your Android flavors on any devices.
- change build variants (devInternal, prodInternal...).
