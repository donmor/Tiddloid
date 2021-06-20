# Tiddloid

![avatar](img/Tiddloid.png)

&ensp;&ensp;&ensp;&ensp;&ensp;[<img src="https://static.coolapk.com/static/web/v8/images/header-logo.png" width="72" height="72" alt="CoolApk" />](https://www.coolapk.com/apk/top.donmor.tiddloid)&ensp;&ensp;&ensp;&ensp;<img src="img/qr.png" width="72" height="72" alt="QrCode"/>&ensp;&ensp;&ensp;&ensp;[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-zh-cn.svg" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/top.donmor.tiddloid)

Tiddloid是一款适用于本地存储的TiddlyWiki的应用程序。一旦有了一些想法，您可以立即将它们写下来以供随时查阅。

<img src="img/img01.png" width="180" height="320" alt="01"/>&emsp;<img src="img/img02.png" width="180" height="320" alt="02"/>&emsp;<img src="img/img03.png" width="180" height="320" alt="02"/>&emsp;<img src="img/img04.png" width="180" height="320" alt="02"/>

有关TiddlyWiki的更多详细信息，请参阅https://tiddlywiki.com/。

### 主要功能

* 使用最新模板创建新的Wiki
* 导入存储在可写来源的现有Wiki
* 添加一个包含基于TiddlyWiki的`index.htm(l)`的文件夹
* 接收从浏览器分享的TiddlyWiki站点URL并存为新文件或书签
* 打开一个HTML文件，如果是TiddlyWiki则添加到列表
* 通过Wiki内配置实现应用主题色到系统界面，以及其他调整选项
* 点击保存按钮保存更改
* 历史版本备份功能
* 便捷的管理功能如`创建快捷方式`, `创建副本`和`移除`等
* 导入/导出Wiki列表

### FAQ

* 找不到如何导入/导出Wiki列表。

    Wiki列表导入/导出属于极少用到的隐藏功能。

    * 导入：在第一次运行前复制`data.json`到`内部存储/Android/data/top.donmor.tiddloid/files/`。如果已经运行过了，则需要清除应用数据。
    * 导出：在Wiki内执行`window.twi.exportDB()`，比如创建一个Tiddler并输入以下内容：`<a href="javascript:window.twi.exportDB()">export</a>`然后点击生成的链接。生成的`data.json`可以在`内部存储/Android/data/top.donmor.tiddloid/files/`找到。

* 如何调整界面？

    导入随apk提供的插件并： 

    * 应用主题色：选中`Control Panel/Appearance/Tiddloid Tweaks/Apply theme color to system bars`。
    * 隐藏标题栏：选中`Control Panel/Appearance/Tiddloid Tweaks/Hide toolbar on loading complete`。

* 新建的Wiki出现Javascript错误弹框并白屏。

    * 检查Android系统版本。TiddlyWiki 5.1.23版本在Android 5.1及以下(WebView 39)存在严重bug。一个解决方案是使用已经修复了此bug的pre-release版本，或者未出现此bug的旧版本。

* Wiki列表每次的顺序都在变化。

    * 检查Android系统版本。JSON库的一个实现在Android 4.4以下有所差异，导致了Wiki列表乱序问题。推荐在Android 8.0以上系统运行以获得最佳体验。

* 从1.4升级后，之前添加的Wiki全部失效了。

    * 检查系统权限设置页面处是否禁止了Tiddloid访问本地文件。在Android 11系统下，第一次使用旧模式时会出现一个请求全部文件读写权限的页面；Android 6 ~ 10则显示一个弹窗。

* 我仍然想用旧模式添加Wiki。

    * 安装一个为旧版Android设计，仍使用`file://`URI的文件管理器，打开一个HTML文件，打开方式选择`Add to Tiddloid`。

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
    | 模板 | 首次使用时下载 | 创建新文件时下载，并缓存以备无网络时使用            |创建新文件时下载|
    | 兼容性 | 适配大多数Android版本，支持TiddlyWiKi5及Classic | 适配新Android版本，支持TiddlyWiKi5及Classic |适配新Android版本，支持TiddlyWiKi5|
    | 推荐的Android版本 | Android 4.4 ~ 9.0 | Android 4.4 及以上，8.0及以上最佳 |Android 4.4 及以上，8.0及以上最佳|


### 许可

本应用程序遵循GPLv2许可发布，其完成离不开这些开源项目的帮助：

* Json - https://json.org/ ,
* Jsoup - https://jsoup.org/ .

许可证文件随源代码提供。

### 多语言

此应用的翻译目前由Google翻译提供。如果您有更好的版本，欢迎在GitHub/Gitee上发起Pull Request。

### 关于我们

感谢您尝试我们的产品。如果您愿意支持，我们将不胜感激，并不遗余力地加以改进。

&ensp;&ensp;<a href="https://liberapay.com/donmor3000/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="30" /></a>&ensp;&ensp;<a href="https://donmor.top/#DonationQrCode"><img alt="Donate using Alipay and Wechat Pay" src="https://donmor.top/img/aliwechat.svg" height="30" /></a>

如果您感兴趣，欢迎访问[我的主页](https://donmor.top/).
