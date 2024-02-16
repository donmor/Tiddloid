/*
 * top.donmor.tiddloid.TWEditorWV <= [P|Tiddloid]
 * Last modified: 21:43:19 2022/09/10
 * Copyright (c) 2022 donmor
 */

package top.donmor.tiddloid;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static top.donmor.tiddloid.MainActivity.APIOver21;
import static top.donmor.tiddloid.MainActivity.APIOver33;
import static top.donmor.tiddloid.MainActivity.KEY_FD_R;
import static top.donmor.tiddloid.MainActivity.KEY_FD_W;
import static top.donmor.tiddloid.MainActivity.KEY_ID;
import static top.donmor.tiddloid.MainActivity.SCH_HTTPS;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.documentfile.provider.DocumentFile;

import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SvgParseException;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import top.donmor.tiddloid.utils.TLSSocketFactory;

public class TWEditorWV extends AppCompatActivity {
	private JSONObject db, wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private int mOriginalOrientation, dialogPadding, hideAppbar = 0;
	private Integer themeColor = null;
	private float scale;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private Toolbar toolbar;
	private ProgressBar wvProgress;
	private Uri uri = null, cachedUri;
	private boolean isWiki, isClassic, noTint = false, ready = false, failed = false, acc_notification = true;
	private static boolean firstRun;
	private Charset overrodeCharset = null;
	private byte[] exData = null;
	private Menu optMenu;
	private String id;
	private CharSequence extraContent;
	private HashMap<String, byte[]> hashes = null;
	private DocumentFile tree = null, treeIndex = null;
	private ActivityResultLauncher<Intent> getChooserCreate, getChooserDL, getChooserImport, getChooserClone;
	private JSONArray customActions, extraContent2 = null;
	private final HashMap<Integer, String> customActionsMap = new HashMap<>();
	private Thread wt;

	// CONSTANT
	private static final String
			JSI = "twi",
			EXT_HTA = ".hta",
			MIME_ANY = "*/*",
			MIME_TEXT = "text/plain",
			MIME_TID = "application/x-tiddler",
			REX_SP_CHR = "\\s",
			KEY_ACTION = "action",
			KEY_ALG = "MD5",
			KEY_COL = ":",
			KEY_ENC = "enc",
			KEY_ENCODING = "encoding",
			KEY_EXTENSION = "extension",
			KEY_FIND_IND = "%1$d/%2$d",
			KEY_HD_METHOD = "GET",
			KEY_HD_AUTH = "Authorization",
			KEY_HD_AUTH_LD = "Basic ",
			KEY_HD_HOST = "Host",
			KEY_HD_UA = "User-Agent",
			KEY_HD_ACC = "Accept",
			KEY_HD_TYPE = "Content-Type",
			KEY_HD_LEN = "Content-Length",
			KEY_ICON = "icon",
			KEY_YES = "yes",
			KEY_LAND = "land",
			KEY_TEXT = "text",
			KEY_TITLE = "title",
			KEY_TYPE = "type",
			KEY_PACKAGE = "package",
			KEY_APP_PACKAGE = "app_package",
			KEY_APP_UID = "app_uid",
			PLUGIN_FILE_NAME = "tiddloid-tweaks-plugin.tid",
			SCH_ABOUT = "about",
			SCH_TEL = "tel",
			SCH_MAILTO = "mailto",
			SCH_PACKAGE = "package",
			URL_BLANK = "about:blank";
	static final String ID_DEFAULT = "<default>";
	private static final int CA_GRP_ID = 999;
	static final byte[]
			HEADER_U16BE_BOM = "\n<!doctype html".getBytes(StandardCharsets.UTF_16),
			HEADER_U16LE_BOM = new byte[]{(byte) 0xFF, (byte) 0xFE, '\n', 0, '<', 0, '!', 0, 'd', 0, 'o', 0, 'c', 0, 't', 0, 'y', 0, 'p', 0, 'e', 0, ' ', 0, 'h', 0, 't', 0, 'm', 0, 'l', 0},
			HEADER_U16BE = "\n<!doctype html>".getBytes(StandardCharsets.UTF_16BE),
			HEADER_U16LE = "\n<!doctype html>".getBytes(StandardCharsets.UTF_16LE);

	private enum TW_CONTENT_ENCODING {
		UTF8, UTF16LE, BASE64
	}

	private static final HashMap<String, TW_CONTENT_ENCODING> TW_TYPE_MAP = new HashMap<String, TW_CONTENT_ENCODING>() {
		private static final long serialVersionUID = 5978131546666849058L;

		{
			put("utf8", TW_CONTENT_ENCODING.UTF8);
			put("utf16le", TW_CONTENT_ENCODING.UTF16LE);
			put("base64", TW_CONTENT_ENCODING.BASE64);
		}
	};
	private static final HashMap<String, TW_CONTENT_ENCODING> TW_TYPES = new HashMap<String, TW_CONTENT_ENCODING>() {
		private static final long serialVersionUID = 5978131546666849058L;

		{
			put("application/hta", TW_CONTENT_ENCODING.UTF16LE);
			put("application/javascript", TW_CONTENT_ENCODING.UTF8);
			put("application/json", TW_CONTENT_ENCODING.UTF8);
			put("application/octet-stream", TW_CONTENT_ENCODING.BASE64);
			put("application/pdf", TW_CONTENT_ENCODING.BASE64);
			put("application/x-tiddler", TW_CONTENT_ENCODING.UTF8);
			put("application/x-tiddler-html-div", TW_CONTENT_ENCODING.UTF8);
			put("application/x-tiddlers", TW_CONTENT_ENCODING.UTF8);
			put("application/zip", TW_CONTENT_ENCODING.BASE64);
			put("audio/mp3", TW_CONTENT_ENCODING.BASE64);
			put("audio/mp4", TW_CONTENT_ENCODING.BASE64);
			put("audio/ogg", TW_CONTENT_ENCODING.BASE64);
			put("image/gif", TW_CONTENT_ENCODING.BASE64);
			put("image/jpeg", TW_CONTENT_ENCODING.BASE64);
			put("image/png", TW_CONTENT_ENCODING.BASE64);
			put("image/svg+xml", TW_CONTENT_ENCODING.UTF8);
			put("image/webp", TW_CONTENT_ENCODING.BASE64);
			put("image/x-icon", TW_CONTENT_ENCODING.BASE64);
			put("text/css", TW_CONTENT_ENCODING.UTF8);
			put("text/html", TW_CONTENT_ENCODING.UTF8);
			put("text/markdown", TW_CONTENT_ENCODING.UTF8);
			put("text/plain", TW_CONTENT_ENCODING.UTF8);
			put("text/vnd.tiddlywiki", TW_CONTENT_ENCODING.UTF8);
			put("text/x-markdown", TW_CONTENT_ENCODING.UTF8);
			put("video/mp4", TW_CONTENT_ENCODING.BASE64);
			put("video/ogg", TW_CONTENT_ENCODING.BASE64);
			put("video/webm", TW_CONTENT_ENCODING.BASE64);
		}
	};
	private static final HashMap<String, String> TW_TYPE_EXT = new HashMap<String, String>() {
		private static final long serialVersionUID = 5978131546666849058L;

		{
			put(".hta", "application/hta");
			put(".js", "application/javascript");
			put(".json", "application/json");
			put(".octet-stream", "application/octet-stream");
			put(".pdf", "application/pdf");
			put(".tid", "application/x-tiddler");
			put(".tiddler", "application/x-tiddler-html-div");
			put(".multids", "application/x-tiddlers");
			put(".zip", "application/zip");
			put(".mp3", "audio/mp3");
			put(".gif", "image/gif");
			put(".jpg", "image/jpeg");
			put(".png", "image/png");
			put(".svg", "image/svg+xml");
			put(".webp", "image/webp");
			put(".ico", "image/x-icon");
			put(".css", "text/css");
			put(".html", "text/html");
			put(".md", "text/markdown");
			put(".txt", "text/plain");
			put(".mp4", "video/mp4");
			put(".ogg", "video/ogg");
			put(".webm", "video/webm");
		}
	};
	private static final Map<String, String> TW_TYPE_EXT_D = new HashMap<String, String>() {
		private static final long serialVersionUID = 5978131546666849058L;

		{
			put(".js", "application/javascript");
			put(".json", "application/json");
			put(".tid", "application/x-tiddler");
			put(".tiddler", "application/x-tiddler-html-div");
			put(".multids", "application/x-tiddlers");
			put(".css", "text/css");
			put(".html", "text/html");
			put(".txt", "text/plain");
		}
	};
	private PermissionRequest mPermissionRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.tweditor);
		dialogPadding = (int) (getResources().getDisplayMetrics().density * 20);
		// 初始化顶栏
		toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
		this.setTitle(R.string.app_name);
		onConfigurationChanged(getResources().getConfiguration());
		wvProgress = findViewById(R.id.progressBar);
		wvProgress.setMax(100);
		// 初始化WebView
		LinearLayout wrapper = findViewById(R.id.wv_wrapper);
		wv = new WebView(this);
		wv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
		wrapper.addView(wv);
		WebSettings wvs = wv.getSettings();
		wvs.setDatabaseEnabled(true);
		wvs.setDomStorageEnabled(true);
		wvs.setBuiltInZoomControls(false);
		wvs.setDisplayZoomControls(false);
		wvs.setUseWideViewPort(true);
		wvs.setLoadWithOverviewMode(true);
		wvs.setAllowFileAccess(true);
		wvs.setAllowContentAccess(true);
		wvs.setAllowFileAccessFromFileURLs(true);
		wvs.setAllowUniversalAccessFromFileURLs(true);
		wvs.setSupportMultipleWindows(true);
		wvs.setMediaPlaybackRequiresUserGesture(false);
		scale = getResources().getDisplayMetrics().density;
		if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) WebView.setWebContentsDebuggingEnabled(true);    // 在debug环境启用调试
		// 注册SAF回调
		getChooserCreate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				if (wv.getUrl() != null) {
					toolbar.setLogo(null);
					wv.getSettings().setBuiltInZoomControls(false);
					wv.getSettings().setDisplayZoomControls(false);
					setTitle(R.string.app_name);
					toolbar.setSubtitle(null);
					wv.getSettings().setJavaScriptEnabled(false);
					customActionsMap.clear();
					customActions = null;
					wv.loadUrl(URL_BLANK);
					themeColor = null;
					noTint = false;
					hideAppbar = 0;
					onConfigurationChanged(getResources().getConfiguration());
					overrodeCharset = null;
					hashes = null;
					tree = null;
					treeIndex = null;
					stopBackgroundService();
				}
				createWiki(result.getData().getData());
			} else finish();
		});
		getChooserDL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (exData == null) return;
			if (result.getData() != null) {
				uri = result.getData().getData();
				if (uri == null) return;
				try (ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(uri, KEY_FD_W));
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel oc = os.getChannel()) {
					MainActivity.ba2fc(exData, oc);
				} catch (NullPointerException | IOException | NonWritableChannelException e) {
					e.printStackTrace();
					try {
						DocumentsContract.deleteDocument(getContentResolver(), uri);
					} catch (FileNotFoundException e1) {
						e.printStackTrace();
					}
					Toast.makeText(this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				}
			}
			exData = null;
		});
		getChooserImport = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (MainActivity.APIOver21 && uploadMessage != null)
				uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.getResultCode(), result.getData()));
			uploadMessage = null;
		});
		getChooserClone = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (exData == null) return;
			if (result.getData() != null) {
				Uri u = result.getData().getData();
				if (u != null) try (ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(u, KEY_FD_W));
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel oc = os.getChannel()) {
					JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
					boolean exist = false;
					String id = null;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						if ((wa = wl.getJSONObject(id = iterator.next())).has(MainActivity.DB_KEY_DAV_AUTH)) continue;
						exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
						if (exist) break;
					}
					if (exist) {
						Toast.makeText(this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
						if (wa.optBoolean(MainActivity.DB_KEY_BACKUP)) MainActivity.backup(this, u, null);
					} else {
						wa = new JSONObject();
						id = MainActivity.genId();
						wl.put(id, wa);
						wa.put(MainActivity.DB_KEY_BACKUP, false);
					}
					wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW)
							.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY)
							.put(MainActivity.DB_KEY_URI, u.toString());
					MainActivity.writeJson(this, db);
					MainActivity.ba2fc(exData, oc);
					Bundle bu = new Bundle();
					bu.putString(MainActivity.KEY_ID, id);
					failed = false;
					nextWiki(new Intent().putExtras(bu));
				} catch (NullPointerException | IOException | NonWritableChannelException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
					failed = true;
					dumpOnFail(exData, u);
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					failed = true;
					dumpOnFail(exData, u);
				}
			}
			exData = null;
		});
		wcc = new WebChromeClient() {
			// 进度条
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				ready = newProgress == 100;
				toolbar.setVisibility(wApp != null && (hideAppbar == 1 || hideAppbar == 2 && Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) && ready ? View.GONE : View.VISIBLE);
				wvProgress.setVisibility(ready ? View.GONE : View.VISIBLE);
				wvProgress.setProgress(newProgress);
				super.onProgressChanged(view, newProgress);
			}

			// 5.0+ 导入文件
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				uploadMessage = filePathCallback;
				getChooserImport.launch(fileChooserParams.createIntent());
				return true;
			}

			// 全屏
			@Override
			public void onShowCustomView(View view, CustomViewCallback callback) {
				if (mCustomView != null) {
					onHideCustomView();
					return;
				}
				mCustomView = view;
				mOriginalOrientation = getRequestedOrientation();
				mCustomViewCallback = callback;
				FrameLayout decor = (FrameLayout) getWindow().getDecorView();
				decor.addView(mCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}

			// 退出全屏
			@Override
			public void onHideCustomView() {
				FrameLayout decor = (FrameLayout) getWindow().getDecorView();
				decor.removeView(mCustomView);
				mCustomView = null;
				setRequestedOrientation(mOriginalOrientation);
				if (mCustomViewCallback != null) mCustomViewCallback.onCustomViewHidden();
				mCustomViewCallback = null;
			}

			// 小窗打开
			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				WebView.HitTestResult result = view.getHitTestResult();
				String data = result.getExtra();
				if (data != null && !isDialog) return overrideUrlLoading(view, Uri.parse(data));
				final WebView nwv = new WebView(TWEditorWV.this);
				final AlertDialog dialog = new AlertDialog.Builder(TWEditorWV.this)
						.setView(nwv)
						.setPositiveButton(android.R.string.ok, null)
						.setOnDismissListener(dialog1 -> {
							nwv.loadDataWithBaseURL(null, MainActivity.STR_EMPTY, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
							nwv.clearHistory();
							((ViewGroup) nwv.getParent()).removeView(nwv);
							nwv.removeAllViews();
							nwv.destroy();
						})
						.create();
				if (!noTint && themeColor != null && dialog.getWindow() != null)
					dialog.getWindow().getDecorView().setBackgroundColor(themeColor);
				if (MainActivity.APIOver29) nwv.getSettings().setForceDark(wv.getSettings().getForceDark());
				nwv.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						Uri u1;
						String p;
						if ((u1 = Uri.parse(url)).getSchemeSpecificPart().equals(Uri.parse(wv.getUrl()).getSchemeSpecificPart()) && (p = u1.getFragment()) != null) {
							wv.evaluateJavascript(getString(R.string.js_pop, Uri.decode(p)), null);
							dialog.dismiss();
						}
						super.onPageFinished(view, url);
					}

					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						dialog.dismiss();
						return TWEditorWV.this.overrideUrlLoading(view, Uri.parse(url));
					}

					@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
						dialog.dismiss();
						return TWEditorWV.this.overrideUrlLoading(view, request.getUrl());
					}
				});
				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
				transport.setWebView(nwv);
				resultMsg.sendToTarget();
				dialog.show();
				return true;
			}

			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onPermissionRequest(PermissionRequest request) {
				for (String r : request.getResources())
					if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r))
						openCamera(TWEditorWV.this, request);
			}
		};
		wv.setWebChromeClient(wcc);
		wv.addJavascriptInterface(new Object() {
			// JS请求处理
			@JavascriptInterface
			public void onDecrypted() {
				runOnUiThread(() -> getInfo(wv));
			}

			// 打印
			@JavascriptInterface
			public void print() {
				runOnUiThread(() -> {
					PrintManager printManager = (PrintManager) TWEditorWV.this.getSystemService(Context.PRINT_SERVICE);
					PrintDocumentAdapter printDocumentAdapter = MainActivity.APIOver21 ? wv.createPrintDocumentAdapter(getTitle().toString()) : wv.createPrintDocumentAdapter();
					printManager.print(getTitle().toString(), printDocumentAdapter, new PrintAttributes.Builder().build());
				});
			}

			// AndTidWiki fallback
			@JavascriptInterface
			public void saveFile(String pathname, String data) {
				saveWiki(data);
			}

			@JavascriptInterface
			public void saveDownload(String data) {
				saveDownload(data, null);
			}

			// 保存文件（指名）
			@JavascriptInterface
			public void saveDownload(String data, String filename) {
				exData = data.getBytes(StandardCharsets.UTF_8);
				getChooserDL.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
						.addCategory(Intent.CATEGORY_OPENABLE)
						.setType(MIME_ANY)
						.putExtra(Intent.EXTRA_TITLE, filename));
			}

			@JavascriptInterface
			public void saveWiki(final String data) {
				if (wApp == null || (MainActivity.SCH_HTTP.equals(uri.getScheme()) || SCH_HTTPS.equals(uri.getScheme())) && cachedUri == null) {
					exData = data.getBytes(StandardCharsets.UTF_8);
					getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
							.addCategory(Intent.CATEGORY_OPENABLE)
							.setType(MainActivity.TYPE_HTML));
					return;
				}
				// TODO: test PUT========
				new Thread(() -> {
					try {
						URL url = new URL(uri.toString());
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						if (!APIOver21 && SCH_HTTPS.equals(url.getProtocol()) && connection instanceof HttpsURLConnection)
							((HttpsURLConnection) connection).setSSLSocketFactory(new TLSSocketFactory());
						connection.setDoOutput(true);
						connection.setDoInput(true);
						connection.setRequestMethod(KEY_HD_METHOD);
						String authU, authP;
						if ((authU = wApp.optString(MainActivity.DB_KEY_DAV_AUTH)).length() > 0 && (authP = wApp.optString(MainActivity.DB_KEY_DAV_TOKEN)).length() > 0)
							connection.setRequestProperty(KEY_HD_AUTH,
									KEY_HD_AUTH_LD + Base64.encodeToString((authU + ':' + authP).getBytes(StandardCharsets.UTF_8), Base64.DEFAULT));
						connection.setRequestProperty(KEY_HD_HOST, url.getHost());
						connection.setRequestProperty(KEY_HD_UA, getString(R.string.app_name) + '/' + MainActivity.getVersion(TWEditorWV.this));
						connection.setRequestProperty(KEY_HD_ACC, MIME_ANY);
						connection.setRequestProperty(KEY_HD_TYPE, MIME_PUT);
						ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(overrodeCharset != null ? overrodeCharset : StandardCharsets.UTF_8));
						connection.setRequestProperty(KEY_HD_LEN, String.valueOf(is.available()));
						connection.setConnectTimeout(60000);
						connection.setReadTimeout(60000);
						connection.connect();
						OutputStream os = connection.getOutputStream();
						int length;
						byte[] bytes = new byte[MainActivity.BUF_SIZE];
						while ((length = is.read(bytes)) > -1) {
							os.write(bytes, 0, length);
						}
						os.flush();
						is.close();
						os.close();
						int rc = connection.getResponseCode();
						System.out.println(rc);
						connection.disconnect();
						if (rc != 200 && rc != 204) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
					} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
						e.printStackTrace();
					}
				}).start();
				// TODO: test put========
				Sardine davClient = null;
				String auth = wApp.optString(MainActivity.DB_KEY_DAV_AUTH), tok = wApp.optString(MainActivity.DB_KEY_DAV_TOKEN);
				if (cachedUri != null) {//	DAV
					davClient = new OkHttpSardine();
					davClient.setCredentials(auth, tok);
				}
				if (wApp.optBoolean(MainActivity.DB_KEY_BACKUP)) try {
					MainActivity.backup(TWEditorWV.this, Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)), davClient);
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
				}
				Uri ux = cachedUri != null ? cachedUri : uri;
				String lt = null;
				try (ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(ux, KEY_FD_W));
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel oc = os.getChannel()) {
					MainActivity.ba2fc(data.getBytes(overrodeCharset != null ? overrodeCharset : StandardCharsets.UTF_8), oc);
					if (cachedUri != null) {    //	DAV
						davClient = new OkHttpSardine();
						davClient.setCredentials(auth, tok);
						lt = davClient.lock(uri.toString());
						davClient.put(uri.toString(), new File(cachedUri.getPath()), MainActivity.TYPE_HTML);
					}
					failed = false;
					runOnUiThread(() -> {
						try {
							if (cachedUri != null && !wApp.optString(MainActivity.DB_KEY_URI).equals(uri.toString()))
								syncTree(wApp.optString(MainActivity.DB_KEY_URI), auth, tok);
							else if (tree != null && treeIndex != null)
								syncTree(tree, id, treeIndex);
						} catch (IOException e) {
							e.printStackTrace();
						}
						getInfo(wv);
					});
				} catch (NullPointerException | IOException | NonWritableChannelException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
					failed = true;
					dumpOnFail(data.getBytes(StandardCharsets.UTF_8), uri);
				} finally {
					if (davClient != null && lt != null) {
						try {
							davClient.unlock(uri.toString(), lt);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			@JavascriptInterface
			public void exportDB() {
				MainActivity.exportJson(TWEditorWV.this, db);
			}

		}, JSI);
		wv.setWebViewClient(new WebViewClient() {
			// KitKat fallback
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url == null) return false;
				return overrideUrlLoading(view, Uri.parse(url));
			}

			// 跳转处理
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return overrideUrlLoading(view, request.getUrl());
			}

			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {    // TODO: Saved creds
				LinearLayout layout = new LinearLayout(TWEditorWV.this);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.setMarginStart(dialogPadding);
				params.setMarginEnd(dialogPadding);
				layout.setOrientation(LinearLayout.VERTICAL);
				EditText username = new EditText(TWEditorWV.this), password = new EditText(TWEditorWV.this);
				username.setHint(R.string.hint_username);
				username.setSingleLine();
				username.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				password.setHint(R.string.hint_password);
				password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				password.setSingleLine();
				layout.addView(username, params);
				layout.addView(password, params);
				final AlertDialog dialog = new AlertDialog.Builder(TWEditorWV.this)
						.setTitle(getString(R.string.hint_login, host))
						.setMessage(realm)
						.setView(layout)
						.setPositiveButton(android.R.string.ok, (dialog12, which) -> handler.proceed(username.getText().toString(), password.getText().toString()))
						.setNegativeButton(android.R.string.cancel, null)
						.create();
				dialog.setOnShowListener(dialog13 -> username.requestFocus());
				password.setOnEditorActionListener((v, actionId, event) -> (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_NULL) && dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick());
				dialog.show();
			}

			// 加载开始
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (optMenu != null) {
					optMenu.getItem(0).setVisible(false);
					optMenu.getItem(1).setVisible(false);
					optMenu.removeGroup(CA_GRP_ID);
				}
			}

			// 加载完成回调
			@Override
			public void onPageFinished(final WebView view, String url) {
				view.evaluateJavascript(getString(R.string.js_print), null);
				view.evaluateJavascript(getString(R.string.js_is_wiki), value -> {
					if (isWiki = Boolean.parseBoolean(value))
						view.evaluateJavascript(getString(R.string.js_is_classic), value1 -> {
							isClassic = Boolean.parseBoolean(value1);
							getInfo(view);
							view.getSettings().setBuiltInZoomControls(isClassic);
							view.getSettings().setDisplayZoomControls(isClassic);
							if (isClassic) {
								view.evaluateJavascript(getString(R.string.js_settings_c), null);
								if (MainActivity.SCH_CONTENT.equals(Uri.parse(view.getUrl()).getScheme()))
									view.evaluateJavascript(getString(R.string.js_settings_c2), null);
								if (extraContent != null) {
									JSONObject p = new JSONObject();
									try {
										p.put(KEY_TEXT, extraContent.toString());
										wv.evaluateJavascript(getString(R.string.js_new_tiddler_c, p.toString()), null);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							} else if (extraContent != null) {
								JSONObject p = new JSONObject();
								try {
									p.put(KEY_TEXT, extraContent.toString());
									wv.evaluateJavascript(getString(R.string.js_new_tiddler, p.toString()), null);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else if (extraContent2 != null)
								view.evaluateJavascript(getString(R.string.js_import, extraContent2.toString()), null);
							extraContent = null;
							extraContent2 = null;
						});
					else if (!URL_BLANK.equals(url)) {
						if (wApp == null) notWikiConfirm();
						else {
							try {
								autoRemoveConfirm(db.getJSONObject(MainActivity.DB_KEY_WIKI), id, Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
				if (wApp != null && wApp.optBoolean(MainActivity.DB_KEY_KEEP_ALIVE)) {
					startBackgroundService();
				}
				view.clearHistory();
			}
		});
		// 关闭/返回
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (mCustomView != null)
					wcc.onHideCustomView();
				else
					wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), null));
			}
		});
		// 初始化db
		try {
			db = MainActivity.readJson(this);
			if (!db.has(MainActivity.DB_KEY_WIKI)) throw new JSONException(MainActivity.EXCEPTION_JSON_DATA_ERROR);
		} catch (JSONException e) {
			e.printStackTrace();
			try {
				db = MainActivity.initJson(TWEditorWV.this);    // 初始化JSON数据，如果加载失败
				firstRun = true;
			} catch (JSONException e1) {
				e1.printStackTrace();
				Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		}
		MainActivity.trimDB140(this, db);    // db格式转换
		// 初始化页内查找
		((EditText) findViewById(R.id.find_edit_find)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				wv.findAllAsync(editable.toString());
			}
		});
		ImageButton btnUp = findViewById(R.id.find_up), btnDn = findViewById(R.id.find_down);
		btnUp.setOnClickListener(view -> wv.findNext(false));
		btnDn.setOnClickListener(view -> wv.findNext(true));
		btnUp.setEnabled(false);
		btnDn.setEnabled(false);
		findViewById(R.id.find_close).setOnClickListener(view -> {
			findViewById(R.id.find_bar).setVisibility(View.GONE);
			wv.clearMatches();
		});
		TextView indicator = findViewById(R.id.find_indicator);
		indicator.setText(String.format(Locale.getDefault(), KEY_FIND_IND, 0, 0));
		wv.setFindListener((i, i1, b) -> {
			indicator.setText(String.format(Locale.getDefault(), KEY_FIND_IND, Math.min(i + 1, i1), i1));
			btnUp.setEnabled(i1 != 0);
			btnDn.setEnabled(i1 != 0);
		});
		// 加载数据
		nextWiki(getIntent());
		if (firstRun)
			MainActivity.firstRunReq(this, new MainActivity.OnFirstRun() {
				@Override
				public void onAgreed() {
					try {
						MainActivity.writeJson(TWEditorWV.this, db);
					} catch (JSONException e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onDeclined() {

				}
			});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (wApp != null && wApp.optBoolean(MainActivity.DB_KEY_KEEP_ALIVE) && wv != null && wv.getProgress() == 100) {
			startBackgroundService();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (wApp != null && wApp.optBoolean(MainActivity.DB_KEY_KEEP_ALIVE) && wv != null && wv.getProgress() == 100) {
			startBackgroundService();
		}
	}

	private void checkNotificationPerms() {
		if (MainActivity.APIOver33) {
			if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
				// 如果上次申请权限被用户选择了禁止
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, POST_NOTIFICATIONS)) {
					enableNotifications();
				} else {
					ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 100);
				}
			}
		} else {
			boolean enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
			if (!enabled) {
				enableNotifications();
			}
		}
		acc_notification = false;
	}

	private void enableNotifications() {
		new AlertDialog.Builder(this)
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(R.string.acquire_permission_notification)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, (dialog, which) -> startActivity(MainActivity.APIOver33
						// Android 13的Settings有专门针对Notification的页面
						? new Intent()
						.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
						.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
						.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid)
						.putExtra(KEY_APP_PACKAGE, getPackageName())
						.putExtra(KEY_APP_UID, getApplicationInfo().uid)
						// Android 13之前只要打开Settings
						: new Intent()
						.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
						.setData(Uri.fromParts(KEY_PACKAGE, getPackageName(), null))))
				.show();
	}

	// 挂入通知区域
	@SuppressLint("BatteryLife")
	private void startBackgroundService() {
		if (acc_notification)
			checkNotificationPerms();
		// Create an intent to start the foreground service
		Intent BGServiceIntent = new Intent(this, StayBackgroundService.class);
		if (wv != null)
			BGServiceIntent.putExtra(MainActivity.KEY_NAME, wv.getTitle());
		if (MainActivity.APIOver26) {
			startForegroundService(BGServiceIntent);
		} else {
			startService(BGServiceIntent);
		}
		if (MainActivity.APIOver23) {
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			if (!pm.isIgnoringBatteryOptimizations(getPackageName())) try {
				startActivity(new Intent()
						.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
						.setData(Uri.parse(SCH_PACKAGE + ':' + getPackageName())));
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	// 清除通知
	private void stopBackgroundService() {
		Intent BGServiceIntent = new Intent(this, StayBackgroundService.class);
		stopService(BGServiceIntent);
	}

	private JSONArray getExDataSingle(final Intent intent) {
		Uri uri;
		try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(uri = intent.getParcelableExtra(Intent.EXTRA_STREAM), KEY_FD_R));
				FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
				FileChannel ic = is.getChannel()) {
			String path = Uri.decode(uri.toString()), type = intent.getType();
			int esp = path.lastIndexOf('.');
			if (esp > 0 && TW_TYPE_EXT.containsKey(path.substring(esp))) {
				type = TW_TYPE_EXT_D.get(path.substring(esp));
				if (type == null) type = TW_TYPE_EXT.get(path.substring(esp));
			}
			if (!TW_TYPES.containsKey(type)) type = MIME_TEXT;
			int seg = Math.max(path.lastIndexOf(':'), path.lastIndexOf('/'));
			TW_CONTENT_ENCODING enc = TW_TYPES.get(type);
			return new JSONArray().put(new JSONObject().put(KEY_TITLE, path.substring(seg + 1))
					.put(KEY_TYPE, type)
					.put(KEY_TEXT, enc == TW_CONTENT_ENCODING.BASE64
							? Base64.encodeToString(MainActivity.fc2ba(ic), Base64.NO_WRAP)
							: enc == TW_CONTENT_ENCODING.UTF16LE
							? new String(MainActivity.fc2ba(ic), StandardCharsets.UTF_16)
							: new String(MainActivity.fc2ba(ic))));
		} catch (NullPointerException | IOException | JSONException | NonReadableChannelException e) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONArray getExDataMultiple(final Intent intent) {
		JSONArray array = new JSONArray();
		ArrayList<Uri> files = APIOver33
				? intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri.class)
				: intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (files == null) return array;
		for (Uri uri : files)
			try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(uri, KEY_FD_R));
					FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
					FileChannel ic = is.getChannel()) {
				String path = Uri.decode(uri.toString()), type = getContentResolver().getType(uri);
				if (type == null) type = MIME_TEXT;
				int esp = path.lastIndexOf('.');
				if (esp > 0 && TW_TYPE_EXT.containsKey(path.substring(esp))) {
					type = TW_TYPE_EXT_D.get(path.substring(esp));
					if (type == null) type = TW_TYPE_EXT.get(path.substring(esp));
				}
				if (!TW_TYPES.containsKey(type)) type = MIME_TEXT;
				int seg = Math.max(path.lastIndexOf(':'), path.lastIndexOf('/'));
				TW_CONTENT_ENCODING enc = TW_TYPES.get(type);
				array.put(new JSONObject().put(KEY_TITLE, path.substring(seg + 1))
						.put(KEY_TYPE, type)
						.put(KEY_TEXT, enc == TW_CONTENT_ENCODING.BASE64
								? Base64.encodeToString(MainActivity.fc2ba(ic), Base64.NO_WRAP)
								: enc == TW_CONTENT_ENCODING.UTF16LE
								? new String(MainActivity.fc2ba(ic), StandardCharsets.UTF_16)
								: new String(MainActivity.fc2ba(ic))));
			} catch (NullPointerException | IOException | JSONException | NonReadableChannelException e) {
				e.printStackTrace();
			}
		return array;
	}

	private void createWiki(Uri uri) {
		wt = new Thread(() -> {
			final ProgressBar[] pBar = new ProgressBar[1];
			runOnUiThread(() -> {
				pBar[0] = findViewById(R.id.progressBar2);
				pBar[0].setVisibility(View.VISIBLE);
			});
			boolean interrupted = false;
			File cache = new File(getCacheDir(), MainActivity.genId()), dest = new File(getCacheDir(), MainActivity.TEMPLATE_FILE_NAME);
			long pModified = dest.lastModified();
			final long[] lastModified = new long[]{0L};
			try (InputStream isw = MainActivity.getAdaptiveUriInputStream(Uri.parse(getString(R.string.template_repo)), lastModified);
					OutputStream osw = Objects.requireNonNull(getContentResolver().openOutputStream(Uri.fromFile(cache)));
					ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(cache), KEY_FD_R));
					ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(dest), KEY_FD_W));
					FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
					FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
					FileChannel ic = is.getChannel();
					FileChannel oc = os.getChannel()) {
				// 下载到缓存
				if (lastModified[0] != pModified) {
					int length;
					byte[] bytes = new byte[MainActivity.BUF_SIZE];
					while ((length = isw.read(bytes)) > -1) {
						osw.write(bytes, 0, length);
						if (Thread.currentThread().isInterrupted()) {
							interrupted = true;
							break;
						}
					}
					osw.flush();
					if (interrupted) throw new InterruptedException(MainActivity.EXCEPTION_INTERRUPTED);
					MainActivity.fc2fc(ic, oc);
					dest.setLastModified(lastModified[0]);
				}
			} catch (InterruptedException | InterruptedIOException ignored) {
				runOnUiThread(() -> Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show());
				finish();
			} catch (NetworkErrorException e) {
				e.printStackTrace();
				runOnUiThread(() -> Toast.makeText(this, R.string.server_error, Toast.LENGTH_SHORT).show());
				finish();
			} catch (IOException | SecurityException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
				e.printStackTrace();
				runOnUiThread(() -> Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show());
				finish();
			} finally {
				cache.delete();
			}
			if (!dest.exists()) {
				Toast.makeText(this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
				finish();
			}
			try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(dest), KEY_FD_R));
					ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(uri, KEY_FD_W));
					FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
					FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
					FileChannel ic = is.getChannel();
					FileChannel oc = os.getChannel()) {
				// 查重
				String id = null;
				JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
				boolean exist = false;
				Iterator<String> iterator = wl.keys();
				while (iterator.hasNext()) {
					if ((wa = wl.getJSONObject(id = iterator.next())).has(MainActivity.DB_KEY_DAV_AUTH)) continue;
					exist = uri.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
					if (exist) break;
				}
				if (exist) {
					Toast.makeText(this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
					if (wa.optBoolean(MainActivity.DB_KEY_BACKUP)) MainActivity.backup(this, uri, null);
				} else {
					wa = new JSONObject()
							.put(MainActivity.DB_KEY_URI, uri.toString())
							.put(MainActivity.DB_KEY_PLUGIN_AUTO_UPDATE, false)
							.put(MainActivity.DB_KEY_BACKUP, false);
					id = MainActivity.genId();
					wl.put(id, wa);
				}
				wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW)
						.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY);
				MainActivity.writeJson(this, db);
				MainActivity.fc2fc(ic, oc);
				try {
					getContentResolver().takePersistableUriPermission(uri, MainActivity.TAKE_FLAGS);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				Bundle bu = new Bundle();
				bu.putString(KEY_ID, id);
				runOnUiThread(() -> {
					pBar[0].setVisibility(View.GONE);
					nextWiki(new Intent(Intent.ACTION_MAIN).putExtras(bu));
				});
			} catch (IOException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
				e.printStackTrace();
				Toast.makeText(this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				finish();
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
				finish();
			}
			wt = null;
		});
		wt.start();
	}

	// 热启动
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		Bundle bu;
		String action = intent.getAction(), fid;
		JSONObject wl, wa;
		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式添加文件
			Uri u = intent.getData();
			if (u == null) {
				Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else if (Intent.ACTION_SEND.equals(action)) {    // 分享
			String cs = null;
			boolean bin = !MIME_TEXT.equals(intent.getType()) || (cs = intent.getStringExtra(Intent.EXTRA_TEXT)) == null;
			if (isClassic && bin) {
				Toast.makeText(this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
				return;
			}
			if (bin) {
				JSONArray array = getExDataSingle(intent);
				cs = array != null ? array.toString() : null;
			} else {
				JSONObject p = new JSONObject();
				try {
					p.put(KEY_TEXT, cs);
					cs = p.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (cs == null) {
				Toast.makeText(this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : bin ? R.string.js_import : R.string.js_new_tiddler, cs), null);
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {    // 分享链接克隆站点
			String cs;
			if (isClassic) {
				Toast.makeText(this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
				return;
			}
			cs = getExDataMultiple(intent).toString();
			wv.evaluateJavascript(getString(R.string.js_import, cs), null);
		} else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {    // 摘录文本
			CharSequence cs;
			if (!MainActivity.APIOver23 || !isWiki || (cs = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)) == null || cs.length() == 0) return;
			JSONObject p = new JSONObject();
			try {
				p.put(KEY_TEXT, cs.toString());
				wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, p.toString()), null);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (Intent.ACTION_CREATE_DOCUMENT.equals(action)) {
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else {    // MA/Shortcut
			if ((bu = intent.getExtras()) == null || (fid = bu.getString(MainActivity.KEY_ID)) == null)
				return;
			if ((wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				return;
			}
			if (ID_DEFAULT.equals(fid)) {
				if ((fid = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(fid)) == null) {
					Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				bu.putString(MainActivity.KEY_ID, fid);
				intent.putExtras(bu);
			} else if ((wa = wl.optJSONObject(fid)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				return;
			}
			if (wa == wApp) return;
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		}
	}

	@TargetApi(23)
	private void openCamera(Context context, PermissionRequest request) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			mPermissionRequest = request;
			ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 1);
		} else
			request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && mPermissionRequest != null) {
			mPermissionRequest.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
		}
		mPermissionRequest = null;
	}

	// 处理跳转App
	private boolean overrideUrlLoading(final WebView view, Uri u) {
		if (u.getSchemeSpecificPart().equals(Uri.parse(wv.getUrl()).getSchemeSpecificPart()))
			return false;
		String sch = u.getScheme();
		if (sch == null || sch.length() == 0)
			return false;
		try {
			final Intent intent;
			switch (sch) {
				case SCH_TEL:
					intent = new Intent(Intent.ACTION_DIAL, u);
					try {
						view.getContext().startActivity(intent);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					break;
				case SCH_MAILTO:
					intent = new Intent(Intent.ACTION_SENDTO, u);
					try {
						view.getContext().startActivity(intent);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					break;
				case SCH_ABOUT:
				case MainActivity.SCH_FILE:
				case MainActivity.SCH_HTTP:
				case MainActivity.SCH_HTTPS:
					intent = new Intent(Intent.ACTION_VIEW, u);
					try {
						view.getContext().startActivity(intent);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					break;
				default:
					intent = new Intent(Intent.ACTION_VIEW, u);
					AlertDialog confirmInvoke = new AlertDialog.Builder(this)
							.setTitle(android.R.string.dialog_alert_title)
							.setMessage(R.string.third_part_rising)
							.setNegativeButton(android.R.string.no, null)
							.setPositiveButton(android.R.string.yes, (dialog, which) -> {
								try {
									view.getContext().startActivity(intent);
								} catch (RuntimeException e) {
									e.printStackTrace();
								}
							}).create();
					confirmInvoke.setOnShowListener(dialog1 -> confirmInvoke.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
					confirmInvoke.show();
					break;
			}
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}

	// 读配置 favicon 主题等
	private void getInfo(WebView view) {
		view.evaluateJavascript(getString(isClassic ? R.string.js_info_c : R.string.js_info), value -> {
			try {
				JSONArray array = new JSONArray(value);
				if (KEY_ENC.equals(array.getString(2)) || KEY_ENC.equals(array.getString(3)) || KEY_ENC.equals(array.getString(4)))
					return;
				// 解取标题
				String title = array.getString(0), subtitle = array.getString(1);
				TWEditorWV.this.setTitle(title);
				toolbar.setSubtitle(subtitle);
				// appbar隐藏
				hideAppbar = KEY_YES.equals(array.getString(2)) ? 1 : KEY_LAND.equals(array.getString(2)) ? 2 : 0;
				Configuration newConfig = getResources().getConfiguration();
				// 解取主题色
				String color = array.getString(3);
				float[] l = new float[3];
				if (color.length() == 7) {
					if (color.charAt(0) == '$') {
						themeColor = Color.parseColor(color.replace('$', '#'));
						Color.colorToHSV(themeColor, l);
						noTint = true;
					} else if (color.charAt(0) == '#') {
						themeColor = Color.parseColor(color);
						Color.colorToHSV(themeColor, l);
						noTint = false;
					} else themeColor = null;
				} else themeColor = null;
				getDelegate().setLocalNightMode(themeColor == null ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : l[2] > 0.75 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);   // 系统栏模式 根据主题色灰度/日夜模式
				// 解取favicon
				String fib64 = array.getString(4);
				try {
					if (fib64.matches(MainActivity.REX_B64)) {    // Base64
						byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
						Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
						toolbar.setLogo(favicon != null ? cIcon(favicon) : null);
					} else {    // SVG
						Sharp svg = Sharp.loadString(fib64);
						Drawable drawable = svg.getSharpPicture().getDrawable();
						int bound = Math.round(scale * 24);
						toolbar.setLogo(MainActivity.svg2bmp(drawable, bound, getResources()));
					}
				} catch (IllegalArgumentException | SvgParseException e) {
					e.printStackTrace();
				}
				if (wApp != null) {
					// 写Json
					wApp.put(MainActivity.KEY_NAME, title)
							.put(MainActivity.DB_KEY_SUBTITLE, subtitle)
							.put(MainActivity.DB_KEY_COLOR, themeColor)
							.put(MainActivity.DB_KEY_NO_TINT, noTint)
							.put(MainActivity.KEY_FAVICON, fib64.length() > 0 ? fib64 : null);
					MainActivity.writeJson(TWEditorWV.this, db);
				}
				if (optMenu != null) {
					optMenu.findItem(R.id.action_save_c).setVisible(wApp == null && uri != null);
					optMenu.findItem(R.id.action_save).setVisible(wApp != null || uri == null);
				}
				String ca = array.optString(5);
				customActions = ca.length() > 8 ? new JSONArray(ca) : null;
				JSONObject mt = array.optJSONObject(6);
				if (mt != null) {
					final Iterator<String> keys = mt.keys();
					while (keys.hasNext()) {
						String i = keys.next();
						JSONObject object = mt.getJSONObject(i);
						String enc = object.optString(KEY_ENCODING), ext = object.optString(KEY_EXTENSION);
						if (enc.length() == 0 || ext.length() == 0) continue;
						TW_TYPES.put(i, TW_TYPE_MAP.get(enc));
						TW_TYPE_EXT.put(ext, i);
					}
					JSONArray dt = array.optJSONArray(7);
					if (dt != null) {
						for (int i = 0; i < dt.length(); i++) {
							JSONObject o;
							String e = dt.optString(i);
							if ((o = mt.optJSONObject(e)) != null)
								TW_TYPE_EXT_D.put(o.getString(KEY_EXTENSION), e);
						}
					}
				}
				String v = array.optString(8);
				if (wApp != null && wApp.optBoolean(MainActivity.DB_KEY_PLUGIN_AUTO_UPDATE) && !v.equals(getString(R.string.plugin_version))) {
					if (isClassic) {
						return;
					}
					JSONArray arr;
					try (InputStream is = getAssets().open(PLUGIN_FILE_NAME)) {
						int len = is.available();
						byte[] buf = new byte[len];
						is.read(buf, 0, len);
						is.close();
						arr = new JSONArray().put(new JSONObject().put(KEY_TITLE, PLUGIN_FILE_NAME)
								.put(KEY_TYPE, MIME_TID)
								.put(KEY_TEXT, new String(buf)));
						wv.evaluateJavascript(getString(R.string.js_import, arr.toString()), null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				onConfigurationChanged(newConfig);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
	}

	//初始化菜单
	@Override
	public boolean onCreateOptionsMenu(@NotNull Menu menu) {
		getMenuInflater().inflate(R.menu.menu_twi, menu);
		optMenu = menu;
		return true;
	}

	@SuppressLint("RestrictedApi")
	@Override
	public boolean onPrepareOptionsMenu(@NotNull Menu menu) {
		if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		final int idSaveFile = R.id.action_save_file,
				idSaveLink = R.id.action_save_link,
				idSave = R.id.action_save;
		if (id == idSaveLink) {
			addLink(uri);
		} else if (id == idSaveFile || id == idSave) {
			wv.evaluateJavascript(getString(isClassic ? R.string.js_save_c : R.string.js_save), null);
		} else {
			if (item.getGroupId() == CA_GRP_ID) {
				String vjs = customActionsMap.get(item.getItemId());
				if (vjs != null) wv.evaluateJavascript(vjs, null);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		Menu menu = mode.getMenu();
		menu.add(R.string.context_save).setOnMenuItemClickListener(menuItem -> {
			wv.evaluateJavascript(getString(isClassic ? R.string.js_save_c : R.string.js_save), null);
			mode.finish();
			return false;
		});
		menu.add(R.string.context_find).setOnMenuItemClickListener(menuItem -> {
			findViewById(R.id.find_bar).setVisibility(View.VISIBLE);
			wv.evaluateJavascript(getString(R.string.js_get_selected), s -> {
				String s1 = s.substring(1, s.length() - 1);
				EditText txtFind = findViewById(R.id.find_edit_find);
				if (s1.length() > 0) txtFind.setText(s1);
				else wv.findAllAsync(txtFind.getText().toString());
			});
			mode.finish();
			return false;
		});
		MenuItem vi = null;
		Intent vp;
		ComponentName vc;
		for (int i = 0; i < menu.size(); i++) {
			if ((vi = menu.getItem(i)) != null && (vp = vi.getIntent()) != null && (vc = vp.getComponent()) != null && getPackageName().equals(vc.getPackageName())) {
				vi.setTitle(R.string.context_new_tiddler);
				break;
			} else vi = null;
		}
		if (vi == null) menu.add(R.string.context_new_tiddler).setOnMenuItemClickListener(menuItem -> {
			wv.evaluateJavascript(getString(R.string.js_get_selected), s -> {
				JSONObject p = new JSONObject();
				try {
					p.put(KEY_TEXT, s.subSequence(1, s.length() - 1).toString());
					wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, p.toString()), null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			});
			mode.finish();
			return false;
		});
		super.onActionModeStarted(mode);
	}

	// 保存提醒
	private void confirmAndExit(boolean dirty, final Intent nextWikiIntent) {
		if (failed || dirty) {
			AlertDialog confirmExit = new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.confirm_to_exit_wiki)
					.setPositiveButton(android.R.string.yes, (dialog, which) -> {
								dialog.dismiss();
								if (nextWikiIntent == null)
									TWEditorWV.super.finish();
								else
									nextWiki(nextWikiIntent);
							}
					)
					.setNegativeButton(android.R.string.no, null)
					.create();
			confirmExit.setOnShowListener(dialog1 -> confirmExit.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
			confirmExit.show();
			confirmExit.setCanceledOnTouchOutside(false);
		} else {
			if (nextWikiIntent == null)
				finish();
			else
				nextWiki(nextWikiIntent);
		}
	}

	private void notWikiConfirm() {
		AlertDialog closeRemoveConfirm = new AlertDialog.Builder(TWEditorWV.this)
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(R.string.not_a_wiki_page)
				.setPositiveButton(android.R.string.ok, null)
				.setOnDismissListener(dialog -> TWEditorWV.this.finish())
				.create();
		closeRemoveConfirm.setOnShowListener(dialog -> closeRemoveConfirm.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
		closeRemoveConfirm.show();
	}

	private void autoRemoveConfirm(JSONObject wl, String id, Uri u) {
		AlertDialog confirmAutoRemove = new AlertDialog.Builder(this)
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(R.string.confirm_to_auto_remove_wiki)
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, (dialog, which) -> {
					try {
						wl.remove(id);
						MainActivity.writeJson(TWEditorWV.this, db);
						if (MainActivity.APIOver26) {
							if (u != null && MainActivity.SCH_CONTENT.equals(u.getScheme()))
								revokeUriPermission(getPackageName(), u, MainActivity.TAKE_FLAGS);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}).setOnDismissListener(dialogInterface -> TWEditorWV.this.finish())
				.create();
		confirmAutoRemove.setOnShowListener(dialog1 -> {
			Window w;
			if ((w = confirmAutoRemove.getWindow()) != null)
				w.getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()));
		});
		confirmAutoRemove.show();
	}

	// 加载内容
	private void nextWiki(Intent nextWikiIntent) {
		// 读取数据
		final JSONObject wl;
		try {
			wl = db.getJSONObject(MainActivity.DB_KEY_WIKI);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		JSONObject wa;
		final Uri u;
		String nextWikiId = null;
		final String action = nextWikiIntent.getAction();
		if (Intent.ACTION_CREATE_DOCUMENT.equals(action)) {
			// 重置
//			if (wv.getUrl() != null) {
//				toolbar.setLogo(null);
//				wv.getSettings().setBuiltInZoomControls(false);
//				wv.getSettings().setDisplayZoomControls(false);
//				setTitle(R.string.app_name);
//				toolbar.setSubtitle(null);
//				wv.getSettings().setJavaScriptEnabled(false);
//				customActionsMap.clear();
//				customActions = null;
//				wv.loadUrl(URL_BLANK);
//				themeColor = null;
//				noTint = false;
//				hideAppbar = 0;
//				onConfigurationChanged(getResources().getConfiguration());
//				overrodeCharset = null;
//				hashes = null;
//				tree = null;
//				treeIndex = null;
//				stopBackgroundService();
//			}
			getChooserCreate.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(MainActivity.TYPE_HTML));
//			Bundle bu = new Bundle();
//			bu.putString(KEY_ID, newId);
//			nextWiki(new Intent(Intent.ACTION_MAIN).putExtras(bu));
			return;
		} else if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式，scheme -> content/file/http(s)
			if ((u = nextWikiIntent.getData()) == null) {
				notWikiConfirm();
				return;
			}
			if (MainActivity.SCH_CONTENT.equals(u.getScheme()) || MainActivity.SCH_FILE.equals(u.getScheme())) {
				try {
					wa = null;
					boolean exist = false;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						if ((wa = wl.optJSONObject(id = iterator.next())) == null || wa.has(MainActivity.DB_KEY_DAV_AUTH)) continue;
						exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
						if (exist) break;
					}
					if (exist) {    // 列表中已存在
						Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
					} else {    // 添加到列表
						wa = new JSONObject();
						nextWikiId = MainActivity.genId();
						wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW)
								.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY)
								.put(MainActivity.DB_KEY_URI, u.toString())
								.put(MainActivity.DB_KEY_BACKUP, false);
						wl.put(nextWikiId, wa);
					}
					MainActivity.writeJson(this, db);
					if (MainActivity.SCH_CONTENT.equals(u.getScheme())) try {
						getContentResolver().takePersistableUriPermission(u, MainActivity.TAKE_FLAGS);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
					wa = null;
				}
			} else wa = null;
		} else if (Intent.ACTION_SEND.equals(action)) {    // 接收分享文件
			wa = null;
			String data;
			if (MainActivity.TYPE_HTML.equals(nextWikiIntent.getType())) {    // 接收html文件
				u = null;
			} else if (MIME_TEXT.equals(nextWikiIntent.getType()) && (data = nextWikiIntent.getStringExtra(Intent.EXTRA_TEXT)) != null) {    // 接收纯文本
				if (data.contains(MainActivity.SCH_HTTP) && data.indexOf('\n') < 0) {    // 接收包含url的单行string
					Uri u1 = null;
					for (String s : data.split(REX_SP_CHR)) {
						if (s.contains(MainActivity.SCH_HTTP)) {
							u1 = Uri.parse(s.substring(s.indexOf(MainActivity.SCH_HTTP)));
							u1 = Uri.parse(u1.getScheme() + KEY_COL + u1.getSchemeSpecificPart());
						}
					}
					if (u1 == null) {
						if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
							Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
							finish();
							return;
						}
						if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
							autoRemoveConfirm(wl, nextWikiId, null);
							return;
						}
						extraContent = data;
					} else
						u = u1;
				} else if (data.contains(MainActivity.SCH_FILE)) {    // 接收包含url的string
					Uri u1 = null;
					for (String s : data.split(REX_SP_CHR)) {
						if (s.contains(MainActivity.SCH_FILE)) {
							u1 = Uri.parse(s.substring(s.indexOf(MainActivity.SCH_FILE)));
							u1 = Uri.parse(u1.getScheme() + KEY_COL + u1.getSchemeSpecificPart());
						}
					}
					if (u1 == null) {
						if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
							Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
							finish();
							return;
						}
						if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
							autoRemoveConfirm(wl, nextWikiId, null);
							return;
						}
						extraContent = data;
					} else {
						u = u1;
						try {
							boolean exist = false;
							Iterator<String> iterator = wl.keys();
							while (iterator.hasNext()) {
								if ((wa = wl.optJSONObject(id = iterator.next())) == null || wa.has(MainActivity.DB_KEY_DAV_AUTH)) continue;
								exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
								if (exist) break;
							}
							if (exist) {    // 列表中已存在
								Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
							} else {    // 添加到列表
								wa = new JSONObject();
								nextWikiId = MainActivity.genId();
								wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW)
										.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY)
										.put(MainActivity.DB_KEY_URI, u.toString())
										.put(MainActivity.DB_KEY_BACKUP, false);
								wl.put(nextWikiId, wa);
							}
							MainActivity.writeJson(this, db);
						} catch (JSONException e) {
							e.printStackTrace();
							Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
							wa = null;
						}
					}
				} else {
					if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
						Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
						finish();
						return;
					}
					if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
						autoRemoveConfirm(wl, nextWikiId, null);
						return;
					}
					extraContent = data;
				}
			} else {
				if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
					Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
					autoRemoveConfirm(wl, nextWikiId, null);
					return;
				}
				extraContent2 = getExDataSingle(nextWikiIntent);
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {    // 分享链接克隆站点
			if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
				autoRemoveConfirm(wl, nextWikiId, null);
				return;
			}
			extraContent2 = getExDataMultiple(nextWikiIntent);
		} else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {    // 接收摘录
			if (!MainActivity.APIOver23) {
				if (!isWiki) finish();
				return;
			}
			CharSequence cs = nextWikiIntent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
			if (isWiki) {
				JSONObject p = new JSONObject();
				try {
					p.put(KEY_TEXT, cs.toString());
					wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, p.toString()), null);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return;
			}
			if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
				autoRemoveConfirm(wl, nextWikiId, null);
				return;
			}
			extraContent = cs;
		} else {    // 从列表中打开
			Bundle bu;
			if ((bu = nextWikiIntent.getExtras()) == null
					|| (nextWikiId = bu.getString(MainActivity.KEY_ID)) == null
					|| nextWikiId.length() == 0) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if (ID_DEFAULT.equals(nextWikiId)) {
				if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
					Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
			} else if ((wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
				autoRemoveConfirm(wl, nextWikiId, null);
				return;
			}
		}
		// 重置
		if (wv.getUrl() != null) {
			toolbar.setLogo(null);
			wv.getSettings().setBuiltInZoomControls(false);
			wv.getSettings().setDisplayZoomControls(false);
			setTitle(R.string.app_name);
			toolbar.setSubtitle(null);
			wv.getSettings().setJavaScriptEnabled(false);
			customActionsMap.clear();
			customActions = null;
			wv.loadUrl(URL_BLANK);
			themeColor = null;
			noTint = false;
			hideAppbar = 0;
			onConfigurationChanged(getResources().getConfiguration());
			overrodeCharset = null;
			hashes = null;
			tree = null;
			treeIndex = null;
			stopBackgroundService();
		}
		// 解取Title/Subtitle/favicon
		wApp = wa;    // nonnull: normal/file/content/url; null: http(s)/html
		uri = u;    // nonnull: normal/file/content/http(s)/url; null: html
		id = nextWikiId;
		Uri actualUri = u;
		if (wa != null && wa.has(MainActivity.DB_KEY_DAV_AUTH)) {    // DAV
			try {
				cachedUri = actualUri = Uri.fromFile(syncTree(u.toString(), wa.optString(MainActivity.DB_KEY_DAV_AUTH), wa.optString(MainActivity.DB_KEY_DAV_TOKEN)));
			} catch (IOException e) {
				e.printStackTrace();
				autoRemoveConfirm(wl, id, u);
				return;
			}
		} else if (u != null && (MainActivity.SCH_HTTP.equals(u.getScheme()) || MainActivity.SCH_HTTPS.equals(u.getScheme()))) {
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				if (Uri.parse(u.getScheme() + KEY_COL + u.getSchemeSpecificPart()).toString().equals((wa = wl.optJSONObject(iterator.next())) != null ? wa.optString(MainActivity.DB_KEY_URI) : null)) {
					wApp = wa;
					break;
				}
			}
		} else if (MainActivity.APIOver21 && u != null && !MainActivity.SCH_FILE.equals(u.getScheme()))
			try {
				DocumentFile p;
				tree = DocumentFile.fromTreeUri(this, u);
				if (tree == null || !tree.isDirectory())
					throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
				treeIndex = (p = tree.findFile(MainActivity.KEY_FN_INDEX)) != null && p.isFile() ? p : (p = tree.findFile(MainActivity.KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
				if (treeIndex == null || !treeIndex.isFile())
					throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
				uri = treeIndex.getUri();
				actualUri = Uri.fromFile(syncTree(tree, id, treeIndex));
			} catch (IllegalArgumentException ignored) {
			} catch (IOException | SecurityException e) {
				e.printStackTrace();
				autoRemoveConfirm(wl, id, u);
				return;
			}
		if (nextWikiIntent != getIntent()) setIntent(nextWikiIntent);
		if (wApp != null) {
			String wvTitle = wApp.optString(MainActivity.KEY_NAME, MainActivity.KEY_TW);
			String wvSubTitle = wApp.optString(MainActivity.DB_KEY_SUBTITLE);
			String fib64 = wApp.optString(MainActivity.KEY_FAVICON);
			this.setTitle(wvTitle);
			toolbar.setSubtitle(wvSubTitle.length() > 0 ? wvSubTitle : null);
			if (fib64.length() > 0) try {
				if (fib64.matches(MainActivity.REX_B64)) {    // Base64
					byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
					Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
					toolbar.setLogo(favicon != null ? cIcon(favicon) : null);
				} else {    // SVG
					Sharp svg = Sharp.loadString(fib64);
					Drawable drawable = svg.getSharpPicture().getDrawable();
					int bound = Math.round(scale * 24);
					toolbar.setLogo(MainActivity.svg2bmp(drawable, bound, getResources()));
				}
			} catch (IllegalArgumentException | SvgParseException e) {
				e.printStackTrace();
			}
			float[] l = new float[3];
			try {
				themeColor = wApp.getInt(MainActivity.DB_KEY_COLOR);
				Color.colorToHSV(themeColor, l);
			} catch (JSONException e) {
				themeColor = null;
			}
			getDelegate().setLocalNightMode(themeColor == null ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : l[2] > 0.75 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);   // 系统栏模式 根据主题色灰度/日夜模式
			noTint = wApp.optBoolean(MainActivity.DB_KEY_NO_TINT);
			onConfigurationChanged(getResources().getConfiguration());
		}
		wv.getSettings().setJavaScriptEnabled(true);
		if (u != null && MainActivity.SCH_CONTENT.equals(u.getScheme())) try {
			getContentResolver().takePersistableUriPermission(u, MainActivity.TAKE_FLAGS);  //保持读写权限
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		String ufn;
		if (uri == null
				|| !MainActivity.APIOver21 && MainActivity.SCH_CONTENT.equals(uri.getScheme()) && actualUri == uri
				|| actualUri != null && (MainActivity.TYPE_HTA.equals(getContentResolver().getType(actualUri))
				|| (ufn = actualUri.getLastPathSegment()) != null && ufn.endsWith(EXT_HTA))) {
			Uri u1 = null, ux;
			if (uri == null)
				if ((u1 = nextWikiIntent.getParcelableExtra(Intent.EXTRA_STREAM)) == null) {
					notWikiConfirm();
					return;
				}
			ux = actualUri != null ? actualUri : u1;
			try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(ux, KEY_FD_R));
					FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
					FileChannel ic = is.getChannel()) {   //读全部数据
				String data = null;
				byte[] bytes = MainActivity.fc2ba(ic), hdr = new byte[32];
				System.arraycopy(bytes, 0, hdr, 0, 32);
				if (Arrays.equals(hdr, HEADER_U16BE_BOM) || Arrays.equals(hdr, HEADER_U16LE_BOM)) {
					data = new String(bytes, StandardCharsets.UTF_16);    // UTF-16 + BOM
					overrodeCharset = StandardCharsets.UTF_16;
				}
				if (data == null && Arrays.equals(hdr, HEADER_U16BE)) {
					data = new String(bytes, StandardCharsets.UTF_16BE);    // UTF-16BE
					overrodeCharset = StandardCharsets.UTF_16BE;
				}
				if (data == null && Arrays.equals(hdr, HEADER_U16LE)) {
					data = new String(bytes, StandardCharsets.UTF_16LE);    // UTF-16LE
					overrodeCharset = StandardCharsets.UTF_16LE;
				}
				if (data == null)
					data = new String(bytes, StandardCharsets.UTF_8);    // UTF-8 / Fallback
				wv.loadDataWithBaseURL(ux.toString(), data, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
			} catch (NullPointerException | IOException | SecurityException | NonReadableChannelException e) {
				e.printStackTrace();
				Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
			}
		} else wv.loadUrl(actualUri != null ? actualUri.toString() : URL_BLANK);
	}

	@NonNull
	private File syncTree(String davSrc, String auth, String token) throws IOException, SecurityException {
		File cacheRoot = new File(getCacheDir(), id);
		if (hashes == null) {
			hashes = new HashMap<>();
			hashDir(cacheRoot, hashes);
		}
		Sardine davClient = new OkHttpSardine();
		davClient.setCredentials(auth, token);
		final IOException[] e0 = new IOException[1];
		final File[] index = new File[1];
		Thread jt = new Thread(() -> {
			try {
				if (!davClient.exists(davSrc)) throw new FileNotFoundException(MainActivity.EXCEPTION_FILE_NOT_FOUND);
				List<DavResource> root = davClient.list(davSrc);
				DavResource ivf;
				if (!(ivf = root.remove(0)).isDirectory()) {
					if (cacheRoot.exists() && !cacheRoot.isDirectory()) cacheRoot.delete();
					if (!cacheRoot.exists()) cacheRoot.mkdir();
					try (InputStream is = davClient.get(davSrc);
							OutputStream os = getContentResolver().openOutputStream(Uri.fromFile(index[0] = new File(cacheRoot, ivf.getName())))) {
						int length;
						byte[] bytes = new byte[MainActivity.BUF_SIZE];
						while ((length = is.read(bytes)) > -1) os.write(bytes, 0, length);
						os.flush();
					}
				} else {
					HashSet<String> files = new HashSet<>();
					syncDir(davClient, davSrc, root, cacheRoot, files);
					clrDir(cacheRoot, files);
					File[] fl;
					if ((fl = cacheRoot.listFiles(file -> MainActivity.KEY_FN_INDEX.equals(file.getName()))) != null && fl.length == 1) {
						uri = Uri.parse(davSrc + MainActivity.KEY_FN_INDEX);
						index[0] = new File(cacheRoot, MainActivity.KEY_FN_INDEX);
					} else if ((fl = cacheRoot.listFiles(file -> MainActivity.KEY_FN_INDEX2.equals(file.getName()))) != null && fl.length == 1) {
						uri = Uri.parse(davSrc + MainActivity.KEY_FN_INDEX2);
						index[0] = new File(cacheRoot, MainActivity.KEY_FN_INDEX2);
					} else throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);
				}
			} catch (IOException e) {
				e.printStackTrace();
				e0[0] = e;
			}
		});
		jt.start();
		try {
			jt.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		if (e0[0] != null) throw e0[0];
		if (index[0] == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
		return index[0];
	}

	@NonNull
	private File syncTree(DocumentFile dir, String id, DocumentFile index) throws IOException, SecurityException {
		File cacheRoot = new File(getCacheDir(), id);
		if (hashes == null) {
			hashes = new HashMap<>();
			hashDir(cacheRoot, hashes);
		}
		HashSet<String> files = new HashSet<>();
		syncDir(dir, cacheRoot, files);
		clrDir(cacheRoot, files);
		if (index.getName() == null)
			throw new IOException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);
		return new File(cacheRoot, index.getName());
	}

	private void hashDir(File root, HashMap<String, byte[]> map) {
		File[] fl;
		if (root == null || !root.isDirectory() || (fl = root.listFiles()) == null) return;
		try {
			MessageDigest messageDigest = null;
			try {
				messageDigest = MessageDigest.getInstance(KEY_ALG);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			if (messageDigest != null)
				for (File child : fl) {
					if (child.isDirectory()) hashDir(child, map);
					else
						try (DigestInputStream dis = new DigestInputStream(getContentResolver().openInputStream(Uri.fromFile(child)), messageDigest)) {
							dis.on(true);
							byte[] buf = new byte[MainActivity.BUF_SIZE];
							int length;
							do length = dis.read(buf, 0, MainActivity.BUF_SIZE); while (length != -1);
							map.put(child.getPath(), dis.getMessageDigest().digest());
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void syncDir(Sardine davClient, String uri, List<DavResource> src, File pos, HashSet<String> files) throws IOException, SecurityException {
		if (!pos.isDirectory()) pos.delete();
		if (!pos.exists()) {
			pos.mkdir();
		}
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(KEY_ALG);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		for (DavResource inner : src) {
			String iu = (uri.charAt(uri.length() - 1) == '/' ? uri : uri + MainActivity.KEY_SLASH) + inner.getName();
			if (!inner.isDirectory()) {
				File dest = new File(pos, inner.getName());
				byte[] dg = null;
				try (InputStream is = davClient.get(iu);
						DigestInputStream dis = messageDigest != null ? new DigestInputStream(is, messageDigest) : null
				) {
					if (is == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dis != null) {
						dis.on(true);
						do length = dis.read(buf); while (length != -1);
						if (Arrays.equals(hashes.get(dest.getPath()), dg = dis.getMessageDigest().digest())) {
							files.add(dest.getPath());
							continue;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				try (InputStream is = davClient.get(iu);
						OutputStream os = getContentResolver().openOutputStream(Uri.fromFile(dest))) {
					int length;
					byte[] bytes = new byte[MainActivity.BUF_SIZE];
					while ((length = is.read(bytes)) > -1) os.write(bytes, 0, length);
					os.flush();
					files.add(dest.getPath());
					hashes.put(dest.getPath(), dg);
				}
			} else if (inner.isDirectory()) {
				if (inner.getName() == null) continue;
				List<DavResource> innerSrc = davClient.list(iu);
				innerSrc.remove(0);
				syncDir(davClient, iu, innerSrc, new File(pos, inner.getName()), files);
			}
		}
		files.add(pos.getPath());
	}

	private void syncDir(DocumentFile src, File pos, HashSet<String> files) throws IOException, SecurityException {
		if (src == null || !src.isDirectory())
			throw new IOException(MainActivity.EXCEPTION_TREE_NOT_A_DIRECTORY);
		if (!pos.isDirectory()) pos.delete();
		if (!pos.exists()) {
			pos.mkdir();
		}
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(KEY_ALG);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		for (DocumentFile inner : src.listFiles())
			if (inner.isFile()) {
				if (inner.getName() == null) continue;
				File dest = new File(pos, inner.getName());
				byte[] dg = null;
				try (InputStream is = getContentResolver().openInputStream(inner.getUri());
						DigestInputStream dis = messageDigest != null ? new DigestInputStream(is, messageDigest) : null
				) {
					if (is == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dis != null) {
						dis.on(true);
						do length = dis.read(buf); while (length != -1);
						if (Arrays.equals(hashes.get(dest.getPath()), dg = dis.getMessageDigest().digest())) {
							files.add(dest.getPath());
							continue;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(inner.getUri(), KEY_FD_R));
						ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(dest), KEY_FD_W));
						FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel ic = is.getChannel();
						FileChannel oc = os.getChannel()
				) {
					MainActivity.fc2fc(ic, oc);
					files.add(dest.getPath());
					hashes.put(dest.getPath(), dg);
				} catch (NullPointerException | IOException | NonReadableChannelException | NonWritableChannelException e) {
					e.printStackTrace();
				}
			} else if (inner.isDirectory()) {
				if (inner.getName() == null) continue;
				syncDir(inner, new File(pos, inner.getName()), files);
			}
		files.add(pos.getPath());
	}

	private void clrDir(File root, HashSet<String> map) throws SecurityException {
		File[] fl;
		if (root == null || !root.isDirectory() || (fl = root.listFiles()) == null) return;
		try {
			for (File child : fl) {
				if (child.isDirectory()) clrDir(child, map);
				if (!map.contains(child.getPath())) child.delete();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	// 添加链接
	private void addLink(Uri u) {
		if (!MainActivity.SCH_HTTP.equals(u.getScheme()) && !MainActivity.SCH_HTTPS.equals(u.getScheme()))
			return;
		u = Uri.parse(u.getScheme() + KEY_COL + u.getSchemeSpecificPart());
		try {
			JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
			boolean exist = false;
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				if ((wa = wl.optJSONObject(id = iterator.next())) == null || wa.has(MainActivity.DB_KEY_DAV_AUTH)) continue;
				exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				wa = new JSONObject();
				String id = MainActivity.genId();
				wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW)
						.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY)
						.put(MainActivity.DB_KEY_URI, u.toString())
						.put(MainActivity.DB_KEY_BACKUP, false);
				wl.put(id, wa);
				MainActivity.writeJson(this, db);
			}
			wApp = wa;
			Toast.makeText(this, R.string.wiki_link_added, Toast.LENGTH_SHORT).show();
			getInfo(wv);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
		}

	}

	private void dumpOnFail(byte[] data, Uri u) {
		String mfn = Uri.parse(Uri.decode(u.toString())).getLastPathSegment();
		File dumpDir = new File(new File(getExternalFilesDir(null), Uri.encode(u.getSchemeSpecificPart())), mfn + MainActivity.BACKUP_POSTFIX);
		dumpDir.mkdirs();
		try (ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(new File(dumpDir,
				new StringBuffer(mfn).insert(mfn.lastIndexOf('.'), MainActivity.formatBackup(System.currentTimeMillis())).toString())), KEY_FD_W));
				FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
				FileChannel oc = os.getChannel()) {
			MainActivity.ba2fc(data, oc);
		} catch (NullPointerException | IOException | SecurityException | NonWritableChannelException e) {
			e.printStackTrace();
		}
	}

	// 生成icon
	private BitmapDrawable cIcon(Bitmap icon) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale * 32f / icon.getWidth(), scale * 32f / icon.getHeight());
		Bitmap icons = Bitmap.createBitmap(Math.round(scale * 40f), Math.round(scale * 32f), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(icons);
		c.drawBitmap(icon, matrix, null);
		c.save();
		c.restore();
		return new BitmapDrawable(getResources(), icons);
	}

	// 应用主题
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		int primColor = themeColor != null && !noTint ? themeColor : getResources().getColor(R.color.design_default_color_primary);    // 优先主题色 >> 自动色
		float[] l = new float[3];
		if (themeColor != null) Color.colorToHSV(themeColor, l);
		boolean lightBar = themeColor != null ? l[2] > 0.75 : (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;    // 系统栏模式 根据主题色灰度/日夜模式
		toolbar.setVisibility(wApp != null && (hideAppbar == 1 || hideAppbar == 2 && Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation) && ready ? View.GONE : View.VISIBLE);
		Window window = getWindow();
		WindowInsetsControllerCompat wic = WindowCompat.getInsetsController(window, window.getDecorView());
		if (MainActivity.APIOver23)
			window.setStatusBarColor(primColor);
		if (MainActivity.APIOver26)
			window.setNavigationBarColor(primColor);
		wic.setAppearanceLightNavigationBars(lightBar);
		wic.setAppearanceLightStatusBars(lightBar);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			wic.hide(WindowInsetsCompat.Type.systemBars());
			wic.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
			WindowCompat.setDecorFitsSystemWindows(window, false);
		} else {
			wic.show(WindowInsetsCompat.Type.systemBars());
			WindowCompat.setDecorFitsSystemWindows(window, true);
		}
		findViewById(R.id.wv_appbar).setBackgroundColor(primColor);
		toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);// 刷新字色
		toolbar.setSubtitleTextAppearance(this, R.style.TextAppearance_AppCompat_Small);
		toolbar.setNavigationIcon(MainActivity.APIOver24 || lightBar ? R.drawable.ic_arrow_back : R.drawable.ic_arrow_back_d);
		EditText txtFind = findViewById(R.id.find_edit_find);
		txtFind.setTextColor(getResources().getColor(R.color.content));
		txtFind.setHintTextColor(getResources().getColor(R.color.content_sub));
		((TextView) findViewById(R.id.find_indicator)).setTextColor(getResources().getColor(R.color.content_sub));
		((ImageButton) findViewById(R.id.find_up)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), MainActivity.APIOver21 || lightBar ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_up_d, null));
		((ImageButton) findViewById(R.id.find_down)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), MainActivity.APIOver21 || lightBar ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_down_d, null));
		((ImageButton) findViewById(R.id.find_close)).setImageDrawable(ResourcesCompat.getDrawable(getResources(), MainActivity.APIOver21 || lightBar ? R.drawable.ic_close : R.drawable.ic_close_d, null));
		if (optMenu != null) {
			try {
				optMenu.removeGroup(CA_GRP_ID);
				customActionsMap.clear();
				if (customActions != null) {
					for (int i = 0; i < customActions.length(); i++) {
						JSONObject item = customActions.getJSONObject(i);
						if (item == null) continue;
						String vn = item.optString(MainActivity.KEY_NAME), vc = item.optString(KEY_ACTION);
						if (vn.length() == 0 || vc.length() == 0) continue;
						int id = CA_GRP_ID + i;
						MenuItem si = optMenu.add(CA_GRP_ID, id, 0, vn);
						si.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
						String fib64 = item.optString(KEY_ICON);
						if (fib64.length() > 0) try {
							if (fib64.matches(MainActivity.REX_B64)) {    // Base64
								byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
								Bitmap icon = BitmapFactory.decodeByteArray(b, 0, b.length);
								si.setIcon(cIcon(icon));
							} else {    // SVG
								Sharp svg = Sharp.loadString(fib64);
								Drawable drawable = svg.getSharpPicture().getDrawable();
								int bound = Math.round(scale * 24);
								si.setIcon(MainActivity.svg2bmp(drawable, bound, getResources()));
							}
						} catch (IllegalArgumentException | SvgParseException e) {
							e.printStackTrace();
						}
						else si.setIcon(MainActivity.APIOver24 ? R.drawable.ic_menu : lightBar ? R.drawable.ic_menu_l : R.drawable.ic_menu_d);
						customActionsMap.put(id, vc);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			optMenu.findItem(R.id.action_save_c).setIcon(MainActivity.APIOver24 ? R.drawable.ic_save : lightBar ? R.drawable.ic_save_l : R.drawable.ic_save_d);
			optMenu.findItem(R.id.action_save_file).setIcon(MainActivity.APIOver24 ? R.drawable.ic_description : lightBar ? R.drawable.ic_description_l : R.drawable.ic_description_d);
			optMenu.findItem(R.id.action_save_link).setIcon(MainActivity.APIOver24 ? R.drawable.ic_language : lightBar ? R.drawable.ic_language_l : R.drawable.ic_language_d);
			optMenu.findItem(R.id.action_save).setIcon(optMenu.findItem(R.id.action_save_c).getIcon());
			for (int i = 0; i < optMenu.size(); i++) {
				MenuItem item = optMenu.getItem(i);
				SpannableString s = new SpannableString(item.getTitle());
				s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.content_tint)), 0, s.length(), 0);
				item.setTitle(s);
			}
		}
		if (MainActivity.APIOver29 && wv != null) wv.getSettings().setForceDark(lightBar ? WebSettings.FORCE_DARK_OFF : WebSettings.FORCE_DARK_ON);
	}

	// WebView清理
	@Override
	protected void onDestroy() {
		stopBackgroundService();
		if (wv != null) {
			ViewParent parent = wv.getParent();
			if (parent != null) ((ViewGroup) parent).removeView(wv);
			wv.stopLoading();
			wv.getSettings().setJavaScriptEnabled(false);
			wv.removeJavascriptInterface(JSI);
			wv.loadDataWithBaseURL(null, MainActivity.STR_EMPTY, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
			wv.clearHistory();
			wv.removeAllViews();
			wv.destroyDrawingCache();
			wv.destroy();
			wv = null;
		}
		super.onDestroy();
	}
}