var enableSaving = function(doc,areBackupsEnabledFn,loadFileTextFn) {
    var messageBox = doc.createElement("div");
	messageBox.id = "tiddlyfox-message-box";
	doc.body.appendChild(messageBox) ;
//	var isClassic = isTiddlyWikiClassic(doc) ;
//	if(isClassic) { injectClassicOverrides(doc,loadFileTextFn) ;
//	}
	messageBox.addEventListener("tiddlyfox-save-file",function(event) {
	    var message = event.target, filepath = message.getAttribute("data-tiddlyfox-path"), content = message.getAttribute("data-tiddlyfox-content");
//		if(process.platform !== "win32" || isClassic) {
		filepath = (new Buffer(filepath,"binary")).toString("utf8");
//		}
		if(areBackupsEnabledFn() && !isClassic) {
		    backupFile(filepath);
		}
		saveFile(filepath,content);
		message.parentNode.removeChild(message);
		var event = doc.createEvent("Events");
		event.initEvent("tiddlyfox-have-saved-file",true,false);
		event.savedFilePath = filepath;
		message.dispatchEvent(event);
		return false;
	},false);
};

//var isTiddlyWikiClassic = function(doc) { var versionArea = doc.getElementById("versionArea") ;
//return doc.getElementById("storeArea") && (versionArea && /TiddlyWiki/.test(versionArea.text)) ;
//} ;

//var injectClassicOverrides = function(doc,loadFileTextFn) { var fs = require("fs"), path = require("path"), text = fs.readFileSync(path.resolve(path.dirname(module.filename),"classic-inject.js")) ;
//text += "\n\nwindow.tiddlywikiSourceText=\"" + stringify(loadFileTextFn()) + "\";" var script = doc.createElement("script") ;
//script.appendChild(doc.createTextNode(text)) ;
//doc.getElementsByTagName("head")[0].appendChild(script) ;
//} ;

//var pad = function(value,length) { length = length || 2 ;
//var s = value.toString() ;
//if(s.length < length) { s = "000000000000000000000000000".substr(0,length - s.length) + s ;
//} return s ;
//} ;
//
//var escape = function(ch) { var charCode = ch.charCodeAt(0) ;
//if(charCode <= 0xFF) { return '\\x' + pad(charCode.toString(16).toUpperCase()) ;
//} else { return '\\u' + pad(charCode.toString(16).toUpperCase(),4) ;
//} } ;

var stringify = function(s) { return (s || "") .replace(/\\/g, '\\\\') .replace(/"/g, '\\"') .replace(/'/g, "\\'") .replace(/\r/g, '\\r') .replace(/\n/g, '\\n') .replace(/[\x00-\x1f\x80-\uFFFF]/g, escape) ;
} ;

var saveFile = function(filepath,content) { var fs = require("fs") ;
fs.writeFileSync(filepath,content) ;
}
var backupFile = function(filepath) { var fs = require("fs"), path = require("path") ;
if(fs.existsSync(filepath)) { var timestamp = $tw.utils.stringifyDate(fs.statSync(filepath).mtime || (new Date())), backupSubPath = backupPathByPath(filepath) ;
var count = 0, backupPath, uniquifier, ext = path.extname(filepath) ;
do { uniquifier = count ? " " + count : "" ;
backupPath = path.resolve( backupSubPath, path.basename(filepath,ext) + "." + timestamp + uniquifier + ext ) ;
count = count + 1 ;
} while(fs.existsSync(backupPath)) ;
$tw.utils.createDirectory(path.dirname(backupPath)) ;
fs.writeFileSync(backupPath,fs.readFileSync(filepath)) ;
} }

backupPathByPath = function(pathname) { var path = require("path"), backupPath = $tw.wiki.getTiddlerText("$:/TiddlyDesktop/BackupPath","") ;
backupPath = backupPath.replace(/\$filename\$/mgi,path.basename(pathname)) .replace(/\$filepath\$/mgi,pathname) ;
backupPath = path.resolve(path.dirname(pathname),backupPath) return backupPath ;
};
enableSaving(document,)