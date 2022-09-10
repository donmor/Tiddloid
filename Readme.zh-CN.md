# Tiddloid

![avatar](img/Tiddloid.png)

&ensp;&ensp;&ensp;&ensp;&ensp;[<img src="https://static.coolapk.com/static/web/v8/images/header-logo.png" width="72" height="72" alt="CoolApk" />](https://www.coolapk.com/apk/top.donmor.tiddloid)&ensp;&ensp;&ensp;&ensp;<img src="img/qr.png" width="72" height="72" alt="QrCode"/>&ensp;&ensp;&ensp;&ensp;[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-zh-cn.svg" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/top.donmor.tiddloid)

Tiddloid是一款适用于本地存储的TiddlyWiki的应用程序。一旦有了一些想法，您可以立即将它们写下来以供随时查阅。

<img src="img/img01.png" width="180" height="320" alt="01"/>&emsp;<img src="img/img02.png" width="180" height="320" alt="02"/>&emsp;<img src="img/img03.png" width="180" height="320" alt="02"/>&emsp;<img src="img/img04.png" width="180" height="320" alt="02"/>

有关TiddlyWiki的更多详细信息，请参阅https://tiddlywiki.com/。

### 主要功能

* 使用最新模板创建新的Wiki
* 导入存储在可写来源的现有Wiki
* 添加一个包含基于TiddlyWiki的`index.htm(l)`的文件夹（Android 5.0+）
* 添加托管在WebDAV服务器上的文件或文件夹（Android 5.0+）
* 接收从浏览器分享的TiddlyWiki站点URL并存为新文件或书签
* 打开一个HTML文件，如果是TiddlyWiki则添加到列表
* 通过Wiki内配置实现应用主题色到系统界面，以及其他调整选项
* 点击保存按钮保存更改
* 上下文菜单中的保存和查找选项
* 随选随记功能
* 历史版本备份功能
* 便捷的管理功能如`创建快捷方式`, `创建副本`和`移除`等
* 导入/导出Wiki列表

### FAQ

* 找不到如何导入/导出Wiki列表。

    Wiki列表导入/导出属于极少用到的隐藏功能。

    * 导入：在第一次运行前复制`data.json`到`内部存储/Android/data/top.donmor.tiddloid/files/`。如果已经运行过了，则需要清除应用数据。
    * 导出：导入随apk提供的插件,然后在`Control Panel/Appearance/Tiddloid Tweaks`中点击`Export Data`。生成的`data.json`可以在`内部存储/Android/data/top.donmor.tiddloid/files/`找到。

* 如何调整界面？

    导入随apk提供的插件并： 

    * 应用主题色：选中`Control Panel/Appearance/Tiddloid Tweaks/Apply theme color to system bars`。
    * 隐藏标题栏：选中`Control Panel/Appearance/Tiddloid Tweaks/Hide toolbar on loading complete`。
    * 自定义菜单项：在`Control Panel/Appearance/Tiddloid Tweaks`中点击`Custom Actions`创建配置Tiddler，并按提示编辑。
    
* 从Google Drive导入的文件无法同步。

    尝试在Google Drive应用中将文件标记为“可离线使用”。（感谢@tedric42的反馈）

* 新建的Wiki出现Javascript错误弹框并白屏。

    检查Android系统版本。TiddlyWiki 5.1.23版本在Android 5.1及以下(WebView 39)存在严重bug。一个解决方案是使用已经修复了此bug的新版本，或者未出现此bug的旧版本。

* Wiki列表每次的顺序都在变化。

    检查Android系统版本。JSON库的一个实现在Android 4.4以下有所差异，导致了Wiki列表乱序问题。推荐在Android 8运行以获得最佳体验。

* 页面下方有时出现乱码。

    检查Android系统及Tiddloid版本。Android 10及以上系统中文件API可能存在问题，导致文件无法正常结尾。Tiddloid 2.3.0中应用了临时修复措施，在Android 9以上中会自动消除显示页面中的乱码（仅在Android 9中保存可以消除源文件中的乱码）。推荐在Android 8运行以获得最佳体验。

* 添加WebDAV项目后发生卡顿。

    慢速的服务器有时会阻塞程序。抱歉 :(

* 从1.4升级后，之前添加的Wiki全部失效了。

    检查系统权限设置页面处是否禁止了Tiddloid访问本地文件。旧模式（直接访问本地文件）默认不启用。要启用它，请打开“WebDAV / 本地（旧版）”并点击左上角的按钮。

* 我无法在其他程序中的文字选择浮动菜单中找到`添加到Tiddloid`。

    * Android 11 限制了应用对所有应用的扫描行为，导致此功能失效。旧应用以及少部分特别进行了处理的新应用仍能使用此功能。

* 我仍然想用旧模式添加Wiki。

    * 打开“WebDAV / 本地（旧版）”并点击左上角的按钮。
    * 或者安装一个为旧版Android设计，仍使用`file://`URI的文件管理器，打开一个HTML文件，打开方式选择`Add to Tiddloid`。
    * 或者选中一段包含`file://`URI的文本，选择`分享`->`Add to Tiddloid`。

* 为什么还有一个Tiddloid Lite？这两个版本有什么区别？

    在之前的1.x版本中，Tiddloid使用旧的`file://`协议打开文件，此方式不支持访问云存储。之后我制作了基于SAF（即Storage Access Framework）的Tiddloid Lite，最终成为一个轻量版。现在Tiddloid已经换用SAF，Tiddloid Lite将不再有功能性更新，但会作为一个轻量版继续进行维护。

    以下是不同版本之间的差异：

    | 功能                               | Tiddloid 1.x  | Tiddloid 2.0 及以上                                    | [Tiddloid Lite](https://gitee.com/donmor/TiddloidLite) |
    | ----------------------------------------- | ------------- | ------------------------------------------------------------ | -------------------------------- |
    | 文件API                                | Java文件API | Android SAF以及 Java文件API  | Android SAF |
    | 备份系统                        | 有          | 有                                                         | 无                              |
    | 搜索-克隆系统        | 有         | 无（改为接收从浏览器分享的TiddlyWiki站点URL并保存） | 无                              |
    | 下载服务                           | 有          | 无                                                          | 无                              |
    | 直接访问同目录下的文件 | 支持         | 部分支持（旧版模式，文件夹模式通过缓存所有文件） | 不支持                            |
    | 云存储                          | 不支持         | 支持（通过SAF）                                          | 支持（通过SAF）           |
    | WebDAV                          | 不支持         | 支持                                                     | 不支持                    |
    | 模板 | 首次使用时下载，可手动更新 | 创建新文件时下载，并缓存以备无网络时使用            |创建新文件时下载|
    | 兼容性 | 适配大多数Android版本，支持TiddlyWiKi5及Classic | 适配新Android版本，支持TiddlyWiKi5及Classic |适配新Android版本，支持TiddlyWiKi5|
    | 推荐的Android版本 | Android 4.4 ~ 9.0 | Android 4.4 及以上，8.0及以上最佳 |Android 4.4 及以上，8.0及以上最佳|


### 许可

本应用程序遵循GPLv2许可发布。WebDAV功能使用了`sardine-android` (https://github.com/thegrizzlylabs/sardine-android)，许可证文件随源代码提供。

### 多语言

此应用的翻译目前由Google翻译提供。如果您有更好的版本，欢迎在GitHub/Gitee上发起Pull Request。

### 隐私

本应用程序使用互联网连接获取模板数据和显示HTTP方式添加的页面，在此过程中不会传输您的数据。另外，您在问题反馈过程中提供的任何信息将被妥善用于解决问题且仅用于此用途。

注意：任何发布在GitHub/Gitee Issues的信息将对所有人可见。

### 关于我们

感谢您尝试我们的产品。如果您愿意支持，我们将不胜感激，并不遗余力地加以改进。

&ensp;&ensp;<a href="https://liberapay.com/donmor3000/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="30" /></a>&ensp;&ensp;<a href="https://donmor.top/#DonationQrCode"><img alt="Donate using Alipay and Wechat Pay" src="https://donmor.top/img/aliwechat.svg" height="30" /></a>

如果您感兴趣，欢迎访问[我的主页](https://donmor.top/)。

您还可以访问[太记的项目页面](https://github.com/tiddly-gittly/TidGi-Desktop)，了解有关中文版桌面应用程序的更多信息。
