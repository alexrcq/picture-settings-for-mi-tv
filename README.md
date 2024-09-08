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
The app grants itself the WRITE_SECURE_SETTINGS permission via  <a href="https://github.com/tananaev/adblib">adblib</a>, allowing it to modify global settings. A system service in the com.android.tv.settings package monitors this database and makes calls to hardware accordingly.
