# Tiddloid

![avatar](img/Tiddloid.png)

&ensp;&ensp;&ensp;&ensp;&ensp;[<img src="https://static.coolapk.com/static/web/v8/images/header-logo.png" width="72" height="72" alt="CoolApk" />](https://www.coolapk.com/apk/top.donmor.tiddloid)&ensp;&ensp;&ensp;&ensp;<img src="img/qr.png" width="72" height="72" alt="QrCode"/>&ensp;&ensp;&ensp;&ensp;[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.svg" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/top.donmor.tiddloid)

Tiddloid is an app to work with locally stored TiddlyWikis. Once have some ideas, you can immediately write them down and save it in a tiddler, and sync the Wiki to your other devices so that you can access these ideas anywhere.

<img src="img/img01.png" width="180" height="320" alt="01"/>&emsp;<img src="img/img02.png" width="180" height="320" alt="02"/>&emsp;<img src="img/img03.png" width="180" height="320" alt="02"/>&emsp;<img src="img/img04.png" width="180" height="320" alt="02"/>

See http://tiddlywiki.com/ for more details of TiddlyWiki.

### Features

* Creating new TiddlyWiki files with latest template
* Importing existing TiddlyWiki files stored on a writable source
* Adding a folder containing TiddlyWiki-based `index.htm(l)` (Android 5.0 and above)
* Adding files or folders held on WebDAV servers (Android 5.0 and above)
* Receiving a TiddlyWiki site URL shared from Browser and save as a new file or a bookmark
* Opening an HTML file and adding to the list if it is a valid TiddlyWiki
* Applying theme color to the UI with some in-wiki configurations, as well as other tweaks
* Saving changes by clicking the Save button
* Save and Find option in context menu
* Draft from any selected text
* A Backup system that preserves your previous versions in case of misoperations
* Convenient operations like `Create shortcut`, `Clone` and `Delete`
* Importing or exporting wiki list data

### Frequently Asked Questions

* I can't find where to import/export wiki list data.

    The wiki list importing/exporting are hidden features since it's seldom used.

    * To import: Copy `data.json` to `INTERNAL/Android/data/top.donmor.tiddloid/files/` before running for the first time. If you have previously run the program, clear the data in `Settings/Apps` and copy the file.
    * To export: Install `Tiddloid Tweaks` plugin, then Click `Export Data` in `Control Panel/Appearance/Tiddloid Tweaks`. A `data.json` will be generated under `INTERNAL/Android/data/top.donmor.tiddloid/files/`.

* How to apply UI tweaks?

    Install `Tiddloid Tweaks` plugin and: 

    * Apply theme color: Select `Enabled ` in `Control Panel/Appearance/Tiddloid Tweaks/Apply theme color to system bars`. If `Light / Dark only` is selected, only light/dark mode is switched.
    * Hide toolbar: Select `Enabled ` in  `Control Panel/Appearance/Tiddloid Tweaks/Hide toolbar on loading complete`. If `Landscape only` is selected, the toolbar will be hidden only if in landscape mode.
    * Custom actions:  to create a config tiddler and edit it following the instructions.

* How to get `Tiddloid Tweaks` installed?
  * Enable `Tweaks plugin auto-update` in per-wiki settings. The plugin will be prepared for installation/update as soon as the page loaded.
  * Or fetch the `.tid` file from releases page and import it.
    
* The file imported from Google Drive won't sync with other devices.

    Try marking the file for "offline access" in Google Drive app. (Thanks to @tedric42's feedback)

* I encountered a Javascript error followed by a white screen in newly created files.

    Check your Android version. TiddlyWiki 5.1.23 has critical bugs on Android 5.1 and below (WebView 39). A solution is to get a new version that had this bug fixed, or a previous version without this bug.

* My wiki list change its order randomly every time.

    Check your Android version. One of the functions in the JSON library behaves differently below 4.4, causing random wiki list order bug on KitKat devices. We recommend you to use Tiddloid on Oreo(8) to enable all of its features.

* Sometimes there are random characters under the page.

    Check your Android and Tiddloid version. There could be a bug in Android 10 filesystem API causing the files not correctly closed. Has been fixed in Tiddloid 2.3.0. Put corrupted files in `[Internal]/Android/data/top.donmor.tiddloid/files` and cold start into wiki list to trigger a batch fix (May OOM when large files loaded). We recommend you to use Tiddloid on Oreo(8) to enable all of its features.

* There are lags if WebDAV Wikis added.

    Sometimes a slow connection to your server blocks the application. Sorry :(

* I upgraded from 1.4 and all of my TiddlyWikis added before no longer to work.

    Check if your system revoked local storage accessing permissions. `Local (Legacy)` mode (which directly accesses local files) disabled on new installations by default. To enable it, Open "WebDAV / Local (Legacy)" and press the button on the top-left corner.

* I can't find `Add to Tiddloid` in floating menus in other apps.

    * Android 11 put a limit on all apps, preventing them from fetching complete application list, that's why it disappeared. It's still functional in legacy apps and some apps with certain hacks.

* I still want to add `Local (Legacy)` wikis.

    * Open "WebDAV / Local (Legacy)" and press the button on the top-left corner.
    * Or Install a file manager designed for old versions of Android that opening files by passing `file://`URIs, open a HTML file, and choose `Add to Tiddloid`.
    * Or select some text containing `file://`URI, press `Share`->`Add to Tiddloid`.

* Why there's a Tiddloid Lite? What's the difference between the two versions?

    In the past 1.x days, Tiddloid use legacy `file://` ways to handle files, causing the lack of access to cloud medias. Later I made another branch that implements Android's SAF (i.e. Storage Access Framework), and finally it became a lightweight version. Now since Tiddloid 2.0 has been refactored to use SAF, there'll be no more functional updates for Tiddloid Lite, only bug fixes will be applied, and it'll remain LITE.

    Here's the difference between different versions:

    | Feature                                   | Tiddloid 1.x                                              | Tiddloid 2.0 and above                                                                | [Tiddloid Lite](https://github.com/donmor/TiddloidLite)        |
    |-------------------------------------------|-----------------------------------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------------------|
    | File API                                  | Java File API                                             | Android Storage Access Framework and also Java File API                               | Android Storage Access Framework                               |
    | Backup system                             | Yes                                                       | Yes                                                                                   | No                                                             |
    | Search-and-Fork system                    | Yes                                                       | No (Instead it receives shared URLs from browsers and show options to save the page)  | No                                                             |
    | Downloader                                | Yes                                                       | No                                                                                    | No                                                             |
    | Direct access to files in the same folder | Yes                                                       | Partial (Legacy, or TiddlyWiki folders by caching all files)                          | No                                                             |
    | Cloud Service                             | No                                                        | Yes (via SAF)                                                                         | Yes (via SAF)                                                  |
    | WebDAV client                             | No                                                        | Yes                                                                                   | No                                                             |
    | Template                                  | Download on first startup, manually updated               | Download on creating new wiki, and cached in case it is unable to access the Internet | Download on creating new wiki                                  |
    | Compatibility                             | Supports most of Android versions and most of TiddlyWikis | Supports latest Android versions best and supports most of TiddlyWikis                | Supports latest Android versions best and supports most of TW5 |
    | Recommended Android version               | Android 4.4 ~ 9.0                                         | Android 4.4 and above, best for 8.0                                                   | Android 4.4 and above, best for 8.0                            |


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

License files provided along with the source code (including licence files of dependencies).

### Localization

The localization of this app is currently provided by Google Translation. Pull requests are welcomed if you have better translations.

### Privacy

This app uses internet connections to fetch templates and show pages added as HTTP links, none of your data will be transferred. Moreover, any of the information you provided during feedbacks will be used only for troubleshooting.

Note: Any information posted on GitHub Issues will be available to everyone.

### About us

Thank you for trying donmor's productions. I'd really appreciate it if you kindly support us via the buttons below, and take more effect to improve our productions.

&ensp;&ensp;<a href="https://liberapay.com/donmor3000/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="30" /></a>&ensp;&ensp;<a href="https://donmor.top/#DonationQrCode"><img alt="Donate using Alipay and Wechat Pay" src="https://donmor.top/img/aliwechat.svg" height="30" /></a>

If you take an interest in us, fell free to visit [our homepage](https://donmor.top/).
