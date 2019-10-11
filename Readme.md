# Tiddloid

![avatar](img/Tiddloid.png)

&ensp;&ensp;&ensp;&ensp;&ensp;[![CoolApk](https://www.coolapk.com/static/img/icon.png)](https://www.coolapk.com/apk/top.donmor.tiddloid)&ensp;&ensp;&ensp;&ensp;<img src="img/qr.png" width="72" height="72" alt="QrCode"/>

Tiddloid is an app to work with locally stored TiddlyWikis. Once have some ideas, you can immediately write them down and save it in a tiddler, and sync the Wiki to your other devices so that you can access these ideas anywhere.

<img src="img/img01.png" width="360" height="640" alt="01"/>&emsp;&emsp;<img src="img/img02.png" width="360" height="640" alt="02"/>

See http://tiddlywiki.com/ for more details of TiddlyWiki.

### Features

* Create new Wikis with latest template
* Import existing Wikis stored on a writable device
* Browse on the Internet and fork Wiki sites interested (Supports TW5 only)
* TiddlyWiki detection
* Saving TiddlyWikis to local files by clicking the Save button
* Backup system that compatible with TiddlyDesktop, the desktop TiddlyWiki saver
* Creating shortcuts to existing wiki on desktop

### License

This app is under GPL v2 license,

1. Commercial use
2. Modification
3. Distribution
4. Private use

is permitted UNDER THESE CONDITIONS:

1. Provide this license and copyright notice
2. State changes
3. Disclose source under
4. Same license.

This app is made possible by these open source projects:

* FileDialog - https://github.com/donmor/FileDialog ,
* Json - https://json.org/ ,
* Jsoup - https://jsoup.org/ ,
* Joda-Time - https://www.joda.org/joda-time/ .

License files provided in /lic.

### Localization

The localization of this app is currently provided by Google Translation. Pull requests are welcomed if you have better localization.

### Using Tiddloid on Android Q and above

Since Google introduced scoped storage in Android Q, Tiddloid can no longer access the external storage(/sdcard). To continue using Tiddloid on Q and above, connect your device to adb and run this command:

```
adb shell cmd appops set top.donmor.tiddloid android:legacy_storage allow
```

And then force stop the app.

Another choice is to change to [Tiddloid Lite](https://github.com/donmor/TiddloidLite). It is a lightweight version of Tiddloid, which has better compatibility to latest Android builds.
Here's the difference between Tiddloid and Tiddloid Lite:

| Feature | Tiddloid | Tiddloid Lite |
| - | - | - |
| File API | Java File API | Android Storage Access Framework |
| Backup system | Yes | No |
| Search-and-Fork system | Yes | No |
| Downloader | Yes | No |
| Local file direct access | Yes | No |
| Cloud Service | No | Yes(via SAF) |
| Template | Download on first startup | Download on creating new wiki
| Compatibility | Supports most of Android versions and most of TiddlyWikis | Supports latest Android version best and supports most of TW5 |
| Recommended Android version | Android 16 ~ 28 | Android 21 ~ |
