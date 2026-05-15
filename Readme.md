# Tiddloid

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.svg" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/top.donmor.tiddloid)

Tiddloid is an app to work with locally stored TiddlyWikis. Once have some ideas, you can immediately write them down and save it in a tiddler, and sync the Wiki to your other devices so that you can access these ideas anywhere.

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="180" height="320" alt="01"/>&emsp;<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="180" height="320" alt="02"/>&emsp;<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="180" height="320" alt="03"/>&emsp;<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="180" height="320" alt="04"/>

See http://tiddlywiki.com/ for more details of TiddlyWiki.

### Features

* Creating new TiddlyWiki files with latest template
* Importing existing TiddlyWiki files stored on a writable source
* Adding a folder containing TiddlyWiki-based `index.htm(l)` (Android 5.0 and above)
* Receiving a TiddlyWiki site URL shared from Browser and save as a new file or a bookmark
* Opening an HTML file and adding to the list if it is a valid TiddlyWiki
* Applying theme color to the UI with some in-wiki configurations, as well as other tweaks
* Saving changes by clicking the Save button
* Save and Find option in context menu
* Draft from selected text
* A Backup system that preserves your previous versions in case of misoperations

### Frequently Asked Questions

* Can I import/export wiki list data?

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

* I encountered a JavaScript error followed by a white screen in newly created files.

    Check your Android version. TiddlyWiki 5.1.23 has critical bugs on Android 5.1 and below (WebView 39). A solution is to get a new version that had this bug fixed, or a previous version without this bug.

* My wiki list change its order randomly every time.

    Check your Android and TiddlyWiki version. One of the functions in the JSON library behaves differently below 4.4, causing random wiki list order bug on KitKat devices. We recommend you to use Tiddloid on Oreo(8)+ to enable all of its features.

* Sometimes there are random characters under the page.

    Check your Android and Tiddloid version. There could be a bug in Android 10 filesystem API causing the files not correctly closed. Has been fixed in Tiddloid 2.3.0. Put corrupted files in `[Internal]/Android/data/top.donmor.tiddloid/files` and cold start into wiki list to trigger a batch fix (May OOM when large files loaded). We recommend you to use Tiddloid on Oreo(8) to enable all of its features.

* I upgraded from 1.4 and all of my TiddlyWikis added before no longer to work.

    Check if your system revoked local storage accessing permissions. `Local (Legacy)` mode (which directly accesses local files) disabled on new installations by default. To enable it, Open "WebDAV / Local (Legacy)" and press the button in the top-left corner.

* I can't find `Add to Tiddloid` or other options in floating menus.

    * Android 11 and above put a limit on accessing floating menus. It's still functional in legacy apps and some apps with certain hacks.

* I still want to add `Local (Legacy)` wikis.

    * Use "Local (Legacy)" option.
    * Or Install a file manager designed for old versions of Android that opening files by passing `file://`URIs, open an HTML file, and choose `Add to Tiddloid`.
    * Or select `file://`URI text and press `Share`->`Add to Tiddloid`.

* Why there's a Tiddloid Lite? What's the difference between the two versions?

    In the past 1.x days, Tiddloid use legacy `file://` ways to handle files, causing the lack of access to cloud medias. Later I made another branch that implements Android's SAF (i.e. Storage Access Framework), and finally it became a lightweight version. Now since Tiddloid 2.0 has been refactored to use SAF, there'll be no more functional updates for Tiddloid Lite, only bug fixes will be applied, and it'll remain LITE.

    Here's the difference between different versions:

    | Feature                                   | Tiddloid 1.x                                             | Tiddloid 2.0 and above                                                                | [Tiddloid Lite](https://github.com/donmor/TiddloidLite)        |
    |-------------------------------------------|----------------------------------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------------------|
    | File API                                  | Java File API                                            | Android Storage Access Framework and also Java File API                               | Android Storage Access Framework                               |
    | Backup system                             | Yes                                                      | Yes                                                                                   | No                                                             |
    | Search-and-Fork system                    | Yes                                                      | No (Instead it receives shared URLs from browsers and show options to save the page)  | No                                                             |
    | Downloader                                | Yes                                                      | No                                                                                    | No                                                             |
    | Direct access to files in the same folder | Yes                                                      | Partial (Legacy, or TiddlyWiki folders by caching all files)                          | No                                                             |
    | Cloud Service                             | No                                                       | Yes (via SAF)                                                                         | Yes (via SAF)                                                  |
    | Template                                  | Download on first startup, manually updated              | Download on creating new wiki, and cached in case it is unable to access the Internet | Download on creating new wiki                                  |
    | Compatibility                             | Supports legacy Android versions and most of TiddlyWikis | Supports latest Android versions best and supports most of TiddlyWikis                | Supports latest Android versions best and supports most of TW5 |
    | Recommended Android version               | Android 4.4 ~ 9.0                                        | Android 5.0 and above, best for 8.0                                                   | Android 5.0 and above, best for 8.0                            |


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

License files provided along with the source code (including license files of dependencies).

### Localization

The localization of this app is mostly provided by Google Translation. Pull requests are welcomed if you have better translations.

### Privacy

This app uses internet connections to fetch templates and show pages added as HTTP links, none of your data will be collected. Moreover, any of the information you provided during feedbacks will be used only for troubleshooting.

Note: Any information posted on Issues will be available to everyone.

### About us

Thank you for trying our productions. I'd really appreciate it if you kindly support us via the buttons below, and take more effect to improve our productions.

&ensp;&ensp;<a href="https://liberapay.com/donmor3000/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="30" /></a>&ensp;&ensp;<a href="https://afdian.com/a/donmor"><img alt="Donate using Afdian" src="https://static.afdiancdn.com/static/img/logo/logo.png" height="30" /></a>

Fell free to visit [my homepage](https://donmor.top/).
