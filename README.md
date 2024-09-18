<div align="center">
<img src="app/src/main/res/drawable/app_tv_logo.png"/>
<h1>Picture settings for Android TV</h1>
Simple app to customize picture on some MTK-based TVs<br><br>
<img src="/screenshots/screen1.png"/><br><br><img src="/screenshots/screen2.png"/>
</div>

## Download
[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/alexrcq/picture-settings-for-android-tv/releases)

## How it works
The app grants itself the WRITE_SECURE_SETTINGS permission via  <a href="https://github.com/tananaev/adblib">adblib</a>, allowing it to modify the Global Settings. A system service in the com.android.tv.settings package monitors this database and makes calls to hardware accordingly.

## Control the dark mode (backlight + screen filter) remotely
You can map the following actions to your TV remote buttons using the <a href="https://play.google.com/store/apps/details?id=dev.vodik7.tvquickactions">tvQuickActions</a> app for Android TV, or connect and send commands via ADB through your phone or PC (for the phone, for example, you can use <a href="https://play.google.com/store/apps/details?id=com.arlosoft.macrodroid">MacroDroid</a> and <a href="https://play.google.com/store/apps/details?id=com.ADBPlugin">ADB Shell [Tasker Plugin]</a>).

### Dark mode

- `com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE`
- `com.alexrcq.tvpicturesettings.ACTION_ENABLE_DARK_MODE`
- `com.alexrcq.tvpicturesettings.ACTION_DISABLE_DARK_MODE`

**Usage**: 
```sh
adb shell am broadcast -a com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE
```

### Screen filter

-  `com.alexrcq.tvpicturesettings.ACTION_TOGGLE_FILTER`
-  `com.alexrcq.tvpicturesettings.ACTION_ENABLE_FILTER`
-  `com.alexrcq.tvpicturesettings.ACTION_DISABLE_FILTER`

**Usage**: 
```sh
adb shell am broadcast -a com.alexrcq.tvpicturesettings.ACTION_TOGGLE_FILTER
``` 

- `ACTION_CHANGE_FILTER_POWER` (version 1.0.75+)

**Usage**: 
```sh
adb shell am broadcast -a ACTION_CHANGE_FILTER_POWER --ei filter_power 60
```
*Note*: The `filter_power` value ranges from 0 to 98.

### Screen control (version 1.0.75+)

- `ACTION_TOGGLE_SCREEN_POWER`

**Usage**: 
```sh
adb shell am broadcast -a ACTION_TOGGLE_SCREEN_POWER
```
