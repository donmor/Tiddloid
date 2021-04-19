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

### Please notice (1.4 and above)

* Tiddloid now supports wiki list data importing/exporting. 
    * To import: Copy `data.json` to `INTERNAL/Android/data/top.donmor.tiddloid/files/` before running for the first time. If you have previously run the program, clear the data in `Settings/Apps` and copy the file.
    * To export: Create a tiddler with: `<a href="javascript:window.twi.exportDB()">export</a>` and click the link. A `data.json` will be generated under `INTERNAL/Android/data/top.donmor.tiddloid/files/`.
* Now supports Applying theme color to the window or hiding the toolbar on loaded depending on configurations inside wiki.
    * Apply theme color: Check `Control Panel/Appearance/Client Tweaks/Tiddloid/Apply theme color to system bars` (in future versions) or create the tiddler: `$:/config/Tiddloid/ApplyTheme` with `yes`.
    * Hide toolbar: Check `Control Panel/Appearance/Client Tweaks/Tiddloid/Hide toolbar on loading complete` (in future versions) or create the tiddler: `$:/config/Tiddloid/HideToolbar` with `yes`.
* Now supports forking TiddlyWiki Classic.
    * No backstage/save button: Go back and enter the same page again.
* TiddlyWiki 5.1.23 has dropped support for Android 5.1 and below (WebView 39). Another thing is that JSON behaves differently between 5.0+ and 4.4, causing random wiki list order bug on KitKat devices. We recommend you to use Tiddloid on Oreo (8.0) and above to enable all features.

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
| Recommended Android version | Android 19 ~ 28 | Android 21 ~ |

### About us

Thank you for trying donmor's productions. I'd really appreciate it if you kindly support us via the buttons below, and take more effect to improve our productions.

&ensp;&ensp;<a href="https://liberapay.com/donmor3000/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="30" /></a>&ensp;&ensp;<a href="https://donmor.top/#DonationQrCode"><img alt="Donate using Alipay and Wechat Pay" src="https://donmor.top/img/aliwechat.svg" height="30" /></a>

If you take an interest in us, fell free to visit [our homepage](https://donmor.top/).
