/*
 * top.donmor.tiddloid.TWEditorWV <= [P|Tiddloid]
 * Last modified: 18:33:05 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.DocumentsContract;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TWEditorWV extends AppCompatActivity {
	private JSONObject db;
	private JSONObject wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private int mOriginalOrientation;
	private Integer themeColor = null;
	private float scale;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private Toolbar toolbar;
	private ProgressBar wvProgress;
	private Uri uri = null;
	private boolean isClassic, hideAppbar = false, ready = false;
	private byte[] exData = null;
	private Menu optMenu;
	private String id;
	private HashMap<String, byte[]> hashes = null;
	private DocumentFile tree = null, treeIndex = null;
	private ActivityResultLauncher<Intent> getChooserDL, getChooserImport, getChooserClone;
	ActivityResultLauncher<Intent> getPermissionRequest;
	boolean acquiringStorage = false;


	// CONSTANT
	private static final String
			JSI = "twi",
			MIME_ANY = "*/*",
			MIME_TEXT = "text/plain",
			REX_SP_CHR = "\\s",
			STR_JS_POP_PRE = "(function(){new $tw.Story().navigateTiddler(\"",
			STR_JS_POP_POST = "\");})();",
			STR_JS_SETTINGS_C = "(function(){setOption(\"chkSaveBackups\",false);})();",
			STR_JS_SETTINGS_C2 = "(function(){readOnly=false;backstage.init();refreshAll();story.refreshAllTiddlers();})();",
			STR_JS_PRINT = "(function(){window.print=function(){window.twi.print();}})();",
			STR_JS_SAVE = "(function(){$tw.saverHandler.saveWiki();})();",
			STR_JS_SAVE_C = "(function(){saveChanges();})();",
			KEY_COL = ":",
			KEY_ENC = "enc",
			KEY_ALG = "MD5",
			KEY_YES = "yes",
			SCH_ABOUT = "about",
			SCH_TEL = "tel",
			SCH_MAILTO = "mailto",
			URL_BLANK = "about:blank";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.tweditor);
		// 初始化db
		try {
			db = MainActivity.readJson(this);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		MainActivity.trimDB140(this, db);
		// 初始化顶栏
		toolbar = findViewById(R.id.wv_toolbar);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
		setSupportActionBar(toolbar);
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
		// 注册SAF回调
		getChooserDL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (exData == null) return;
			if (result.getData() != null) {
				uri = result.getData().getData();
				if (uri == null) return;
				try (InputStream is = new ByteArrayInputStream(exData);
						OutputStream os = getContentResolver().openOutputStream(uri)) {
					if (os == null)
						throw new FileNotFoundException(MainActivity.EXCEPTION_SAF_FILE_NOT_EXISTS);
					int len = is.available();
					int length;
					int lengthTotal = 0;
					byte[] bytes = new byte[4096];
					while ((length = is.read(bytes)) > -1) {
						os.write(bytes, 0, length);
						lengthTotal += length;
					}
					os.flush();
					if (lengthTotal != len)
						throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
				} catch (IOException e) {
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
				if (u != null) try (ByteArrayInputStream is = new ByteArrayInputStream(exData);
						OutputStream os = getContentResolver().openOutputStream(u)) {
					if (os == null)
						throw new FileNotFoundException(MainActivity.EXCEPTION_SAF_FILE_NOT_EXISTS);
					JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
					boolean exist = false;
					String id = null;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						exist = u.toString().equals((wa = wl.getJSONObject(id = iterator.next())).optString(MainActivity.DB_KEY_URI));
						if (exist) break;
					}
					if (exist)
						Toast.makeText(this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
					else {
						wa = new JSONObject();
						id = MainActivity.genId();
						wl.put(id, wa);
					}
					wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
					wa.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY);
					wa.put(MainActivity.DB_KEY_URI, u.toString());
					wa.put(MainActivity.DB_KEY_BACKUP, false);
					MainActivity.writeJson(this, db);
					int len = is.available();
					int length, lengthTotal = 0;
					byte[] b = new byte[4096];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lengthTotal += length;
					}
					os.flush();
					if (lengthTotal != len || !MainActivity.isWiki(this, u))
						throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
					Bundle bu = new Bundle();
					bu.putString(MainActivity.KEY_ID, id);
					nextWiki(new Intent().putExtras(bu));
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			}
			exData = null;
		});
		getPermissionRequest = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())
				Toast.makeText(this, "No file permissions", Toast.LENGTH_SHORT).show();
			acquiringStorage = false;
		});
		wcc = new WebChromeClient() {
			// 进度条
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				ready = newProgress == 100;
				toolbar.setVisibility(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE || hideAppbar && ready ? View.GONE : View.VISIBLE);
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
							nwv.loadDataWithBaseURL(null, null, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
							nwv.clearHistory();
							((ViewGroup) nwv.getParent()).removeView(nwv);
							nwv.removeAllViews();
							nwv.destroy();
						})
						.create();
				nwv.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						Uri u1;
						String p;
						if ((u1 = Uri.parse(url)).getSchemeSpecificPart().equals(Uri.parse(wv.getUrl()).getSchemeSpecificPart()) && (p = u1.getFragment()) != null) {
							wv.evaluateJavascript(STR_JS_POP_PRE + Uri.decode(p) + STR_JS_POP_POST, null);
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
				TWEditorWV.this.exData = data.getBytes(StandardCharsets.UTF_8);
				getChooserDL.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
						.addCategory(Intent.CATEGORY_OPENABLE)
						.setType(MIME_ANY)
						.putExtra(Intent.EXTRA_TITLE, filename));
			}

			@JavascriptInterface
			public void saveWiki(final String data) {
				if (wApp == null || MainActivity.SCH_HTTP.equals(uri.getScheme()) || MainActivity.SCH_HTTPS.equals(uri.getScheme())) {
					exData = data.getBytes(StandardCharsets.UTF_8);
					getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
							.addCategory(Intent.CATEGORY_OPENABLE)
							.setType(MainActivity.TYPE_HTML));
					return;
				}
				boolean legacy = MainActivity.SCH_FILE.equals(uri.getScheme());
				if (wApp.optBoolean(MainActivity.DB_KEY_BACKUP)) try {
					MainActivity.backup(TWEditorWV.this, Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)));
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
				}
				try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
						OutputStream os = getContentResolver().openOutputStream(uri)) {
//						OutputStream os = legacy ? new FileOutputStream(uri.getPath()) : getContentResolver().openOutputStream(uri)) {
					if (os == null)
						throw new FileNotFoundException(MainActivity.EXCEPTION_SAF_FILE_NOT_EXISTS);
					int len = is.available();
					int length, lengthTotal = 0;
					byte[] b = new byte[4096];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lengthTotal += length;
					}
					os.flush();
					if (lengthTotal != len)
						throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
					runOnUiThread(() -> getInfo(wv));
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
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

			// 加载开始
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (optMenu != null) {
					optMenu.getItem(0).setVisible(false);
					optMenu.getItem(1).setVisible(false);
				}
			}

			// 加载完成回调
			@Override
			public void onPageFinished(final WebView view, String url) {
				view.evaluateJavascript(STR_JS_PRINT, null);
				view.evaluateJavascript(getString(R.string.js_is_wiki), value -> {
					if (Boolean.parseBoolean(value))
						view.evaluateJavascript(getString(R.string.js_is_classic), value1 -> {
							isClassic = Boolean.parseBoolean(value1);
							getInfo(view);
							view.getSettings().setBuiltInZoomControls(isClassic);
							view.getSettings().setDisplayZoomControls(isClassic);
							if (isClassic) {
								view.evaluateJavascript(STR_JS_SETTINGS_C, null);
								if (MainActivity.SCH_CONTENT.equals(Uri.parse(view.getUrl()).getScheme()))
									view.evaluateJavascript(STR_JS_SETTINGS_C2, null);
							}
						});
					else if (!URL_BLANK.equals(url)) {
						Toast.makeText(TWEditorWV.this, "The page is not a TiddlyWiki", Toast.LENGTH_SHORT).show();    // TODO: -> R.string
//						finish();
					}
				});
				view.clearHistory();
			}
		});
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
		nextWiki(getIntent());
	}

	// 热启动
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		Bundle bu;
		String action = intent.getAction(), fid;
		JSONObject wl, wa;
		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式添加文件
//			System.out.println("VIEW");
			Uri u;
			if ((u = intent.getData()) == null || !MainActivity.isWiki(this, u)) {
				Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else if (Intent.ACTION_SEND.equals(action)) {    // 分享链接克隆站点
//			System.out.println("SEND");
			if (!(MainActivity.TYPE_HTML.equals(intent.getType()) && !MainActivity.isWiki(this, intent.getParcelableExtra(Intent.EXTRA_STREAM))) || !(MIME_TEXT.equals(intent.getType()) && intent.getStringExtra(Intent.EXTRA_TEXT).contains(MainActivity.SCH_HTTP))) {
				Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else {    // MA/Shortcut
//			System.out.println("MAIN");
			if ((bu = intent.getExtras()) == null || (fid = bu.getString(MainActivity.KEY_ID)) == null)
				return;
			if ((wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null || (wa = wl.optJSONObject(fid)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				return;
			}
			if (wa == wApp) return;
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		}
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
					new AlertDialog.Builder(TWEditorWV.this)
							.setTitle(android.R.string.dialog_alert_title)
							.setMessage(R.string.third_part_rising)
							.setNegativeButton(android.R.string.no, null)
							.setPositiveButton(android.R.string.yes, (dialog, which) -> {
								try {
									view.getContext().startActivity(intent);
								} catch (RuntimeException e) {
									e.printStackTrace();
								}
							}).show();
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
				hideAppbar = KEY_YES.equals(array.getString(2));
				Configuration newConfig = getResources().getConfiguration();
				toolbar.setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || hideAppbar && ready ? View.GONE : View.VISIBLE);
				// 解取主题色
				String color = array.getString(3);
				float[] l = new float[3];
				if (color.length() == 7) {
					themeColor = Color.parseColor(color);
					Color.colorToHSV(themeColor, l);
				} else themeColor = null;
				getDelegate().setLocalNightMode(themeColor == null ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : l[2] > 0.75 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);   // 系统栏模式 根据主题色灰度/日夜模式
				onConfigurationChanged(newConfig);
				// 解取favicon
				String fib64 = array.getString(4);
				byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
				Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
				toolbar.setLogo(favicon != null ? cIcon(favicon) : null);
				if (wApp != null) {
					// 写Json
					wApp.put(MainActivity.KEY_NAME, title).put(MainActivity.DB_KEY_SUBTITLE, subtitle).put(MainActivity.DB_KEY_COLOR, themeColor).put(MainActivity.KEY_FAVICON, fib64.length() > 0 ? fib64 : null);
					MainActivity.writeJson(TWEditorWV.this, db);
					if (tree != null && treeIndex != null) try {
						syncTree(tree, id, treeIndex);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (optMenu != null) {
					optMenu.findItem(R.id.action_save_c).setVisible(wApp == null && uri != null);
					optMenu.findItem(R.id.action_save).setVisible(wApp != null || uri == null);
//					if (MainActivity.APIOver24) {
//						optMenu.findItem(R.id.action_save_c).setIcon(R.drawable.ic_save);
//						optMenu.findItem(R.id.action_save_file).setIcon(R.drawable.ic_description);
//						optMenu.findItem(R.id.action_save_link).setIcon(R.drawable.ic_language);
//						optMenu.findItem(R.id.action_save).setIcon(R.drawable.ic_save);
//					} else {
//						optMenu.findItem(R.id.action_save_c).setIcon(R.drawable.ic_save);
//						optMenu.findItem(R.id.action_save_file).setIcon(R.drawable.ic_description);
//						optMenu.findItem(R.id.action_save_link).setIcon(R.drawable.ic_language);
//						optMenu.findItem(R.id.action_save).setIcon(R.drawable.ic_save);
//					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
	}

	//初始化菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_twi, menu);
		optMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		final int idSaveFile = R.id.action_save_file,
				idSaveLink = R.id.action_save_link,
				idSave = R.id.action_save;
		switch (id) {
			case idSaveLink:
				addLink(uri);
				break;
			case idSaveFile:
			case idSave:
				wv.evaluateJavascript(isClassic ? STR_JS_SAVE_C : STR_JS_SAVE, null);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 保存提醒
	private void confirmAndExit(boolean dirty, final Intent nextWikiIntent) {
		if (dirty) {
			AlertDialog confirmExit = new AlertDialog.Builder(TWEditorWV.this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.confirm_to_exit_wiki)
					.setPositiveButton(android.R.string.yes, (dialog, which) -> {
								if (nextWikiIntent == null)
									TWEditorWV.super.onBackPressed();
								else
									nextWiki(nextWikiIntent);
								dialog.dismiss();
							}
					)
					.setNegativeButton(android.R.string.no, null)
					.show();
			confirmExit.setCanceledOnTouchOutside(false);
		} else {
			if (nextWikiIntent == null)
				TWEditorWV.super.onBackPressed();
			else
				nextWiki(nextWikiIntent);
		}
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
		Bundle bu;
		final Uri u;
//		String data = null;
		String nextWikiId = null;
		final String action = nextWikiIntent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式，scheme -> content/file/http(s)
			if ((u = nextWikiIntent.getData()) == null || !MainActivity.isWiki(this, u)) {
				Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if (MainActivity.SCH_CONTENT.equals(u.getScheme()) || MainActivity.SCH_FILE.equals(u.getScheme())) {
				try {
//					wl = db.getJSONObject(MainActivity.DB_KEY_WIKI);
					wa = null;
					boolean exist = false;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						exist = u.toString().equals((wa = wl.optJSONObject(iterator.next())) != null ? wa.optString(MainActivity.DB_KEY_URI) : null);
						if (exist) break;
					}
					if (exist) {
						Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
					} else {
						wa = new JSONObject();
						String id = MainActivity.genId();
						wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
						wa.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY);
						wa.put(MainActivity.DB_KEY_URI, u.toString());
						wa.put(MainActivity.DB_KEY_BACKUP, false);
						wl.put(id, wa);
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
		} else if (Intent.ACTION_SEND.equals(action)) {
			wa = null;
			String data;
			if (MainActivity.TYPE_HTML.equals(nextWikiIntent.getType())) {    // 接收html文件
//				Uri u1 = nextWikiIntent.getParcelableExtra(Intent.EXTRA_STREAM);
//				if (!MainActivity.isWiki(this, u1)) {
//					Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
//					finish();
//					return;
//				}
//				try (BufferedInputStream is = new BufferedInputStream(getContentResolver().openInputStream(u1));
//						ByteArrayOutputStream os = new ByteArrayOutputStream(MainActivity.BUF_SIZE)) {   //读全部数据
//					int len = is.available();
//					int length, lenTotal = 0;
//					byte[] b = new byte[MainActivity.BUF_SIZE];
//					while ((length = is.read(b)) != -1) {
//						os.write(b, 0, length);
//						lenTotal += length;
//					}
//					os.flush();
//					if (lenTotal != len) throw new IOException();
//					data = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(os.toByteArray())).toString();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				u = null;
			} else if (MIME_TEXT.equals(nextWikiIntent.getType()) && (data = nextWikiIntent.getStringExtra(Intent.EXTRA_TEXT)) != null && data.contains(MainActivity.SCH_HTTP)) {    // 接收包含url的string
				Uri u1 = null;
				for (String s : data.split(REX_SP_CHR)) {
					if (s.contains(MainActivity.SCH_HTTP)) {
						u1 = Uri.parse(s.substring(s.indexOf(MainActivity.SCH_HTTP)));
						u1 = Uri.parse(u1.getScheme() + KEY_COL + u1.getSchemeSpecificPart());
					}
				}
				u = u1;
				if (u == null) {
					finish();
					return;
				}
			} else {
				finish();
				return;
			}
		} else {
			if ((bu = nextWikiIntent.getExtras()) == null
					|| (nextWikiId = bu.getString(MainActivity.KEY_ID)) == null
					|| nextWikiId.length() == 0
//					|| (wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null
					|| (wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null || bu.getBoolean(MainActivity.KEY_SHORTCUT) && !MainActivity.isWiki(this, u)) {
				String finalNextWikiId = nextWikiId;
				new AlertDialog.Builder(this)
						.setTitle(android.R.string.dialog_alert_title)
						.setMessage(R.string.confirm_to_auto_remove_wiki)
						.setNegativeButton(android.R.string.no, null)
						.setPositiveButton(android.R.string.yes, (dialog, which) -> {
							try {
								wl.remove(finalNextWikiId);
								MainActivity.writeJson(TWEditorWV.this, db);
								if (MainActivity.APIOver26)
									if (u != null && MainActivity.SCH_CONTENT.equals(u.getScheme()))
										revokeUriPermission(getPackageName(), u, MainActivity.TAKE_FLAGS);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}).setOnDismissListener(dialogInterface -> TWEditorWV.this.finish()).show();
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
			wv.loadUrl(URL_BLANK);
			getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		}
		// 解取Title/Subtitle/favicon
		wApp = wa;    // nonnull: normal/file/content/url; null: http(s)/html
		uri = u;    // nonnull: normal/file/content/http(s)/url; null: html
		id = nextWikiId;
		Uri actualUri = u;
		if (u != null && (MainActivity.SCH_HTTP.equals(u.getScheme()) || MainActivity.SCH_HTTPS.equals(u.getScheme()))) {
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				if (Uri.parse(u.getScheme() + KEY_COL + u.getSchemeSpecificPart()).toString().equals((wa = wl.optJSONObject(iterator.next())) != null ? wa.optString(MainActivity.DB_KEY_URI) : null)) {
					wApp = wa;
					Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
					break;
				}
			}
		} else if (MainActivity.APIOver21 && u != null && !MainActivity.SCH_FILE.equals(u.getScheme()))
			try {
				tree = DocumentFile.fromTreeUri(this, u);
				DocumentFile p;
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
				Toast.makeText(this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		if (nextWikiIntent != getIntent()) setIntent(nextWikiIntent);
		if (wApp != null) {
			String wvTitle = wApp.optString(MainActivity.KEY_NAME, MainActivity.KEY_TW);
			String wvSubTitle = wApp.optString(MainActivity.DB_KEY_SUBTITLE);
			String fib64 = wApp.optString(MainActivity.KEY_FAVICON);
			this.setTitle(wvTitle);
			toolbar.setSubtitle(wvSubTitle.length() > 0 ? wvSubTitle : null);
			if (fib64.length() > 0) {
				byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
				Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
				toolbar.setLogo(favicon != null ? cIcon(favicon) : null);
			}
			float[] l = new float[3];
			try {
				themeColor = wApp.getInt(MainActivity.DB_KEY_COLOR);
				Color.colorToHSV(themeColor, l);
			} catch (JSONException e) {
				themeColor = null;
			}
			getDelegate().setLocalNightMode(themeColor == null ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : l[2] > 0.75 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);   // 系统栏模式 根据主题色灰度/日夜模式
			onConfigurationChanged(getResources().getConfiguration());
		}
		wv.getSettings().setJavaScriptEnabled(true);
		if (u != null && MainActivity.SCH_CONTENT.equals(u.getScheme())) try {
			getContentResolver().takePersistableUriPermission(u, MainActivity.TAKE_FLAGS);  //保持读写权限
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		if (u == null || !MainActivity.APIOver21 && MainActivity.SCH_CONTENT.equals(u.getScheme()) && actualUri == uri) {
			Uri u1 = null, ux;
			if (u == null)
				if (!MainActivity.isWiki(this, u1 = nextWikiIntent.getParcelableExtra(Intent.EXTRA_STREAM))) {
					Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
			ux = u != null ? u : u1;
			try (InputStream is0 = getContentResolver().openInputStream(ux);
					BufferedInputStream is = is0 != null ? new BufferedInputStream(is0) : null;
					ByteArrayOutputStream os = new ByteArrayOutputStream(MainActivity.BUF_SIZE)) {   //读全部数据
				if (is == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
				int len = is.available();
				int length, lenTotal = 0;
				byte[] b = new byte[MainActivity.BUF_SIZE];
				while ((length = is.read(b)) != -1) {
					os.write(b, 0, length);
					lenTotal += length;
				}
				os.flush();
				if (lenTotal != len)
					throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
				String data = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(os.toByteArray())).toString();
				wv.loadDataWithBaseURL(ux.toString(), data, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			wv.loadUrl(actualUri != null ? actualUri.toString() : URL_BLANK);
		}
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
				System.out.println(messageDigest);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			if (messageDigest != null)
				for (File child : fl) {
					if (child.isDirectory()) hashDir(child, map);
					else
						try (DigestInputStream dis = new DigestInputStream(new FileInputStream(child), messageDigest)) {
							dis.on(true);
							byte[] buf = new byte[MainActivity.BUF_SIZE];
							//noinspection StatementWithEmptyBody
							while (dis.read(buf, 0, MainActivity.BUF_SIZE) != -1) ;
							map.put(child.getPath(), dis.getMessageDigest().digest());
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void syncDir(DocumentFile src, File pos, HashSet<String> files) throws IOException, SecurityException {
		if (src == null || !src.isDirectory())
			throw new IOException(MainActivity.EXCEPTION_TREE_NOT_A_DIRECTORY);
		if (!pos.isDirectory()) pos.delete();
		if (!pos.exists()) pos.mkdir();
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
				byte[] ba, dg = null;
				try (InputStream is = getContentResolver().openInputStream(inner.getUri());
						DigestInputStream dis = messageDigest != null ? new DigestInputStream(is, messageDigest) : null;
						ByteArrayOutputStream bos = new ByteArrayOutputStream(MainActivity.BUF_SIZE)) {
					if (is == null) continue;
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dis != null) {
						dis.on(true);
						while ((length = dis.read(buf)) != -1) bos.write(buf, 0, length);
						if (Arrays.equals(hashes.get(dest.getPath()), dg = dis.getMessageDigest().digest())) {
							files.add(dest.getPath());
							continue;
						}
					} else while ((length = is.read(buf)) != -1) bos.write(buf, 0, length);
					bos.flush();
					ba = bos.toByteArray();
				}
				try (ByteArrayInputStream bis = new ByteArrayInputStream(ba);
						FileOutputStream os = new FileOutputStream(dest);
						DigestOutputStream dos = messageDigest != null ? new DigestOutputStream(os, messageDigest) : null) {
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dos != null) {
						dos.on(true);
						while ((length = bis.read(buf)) != -1) dos.write(buf, 0, length);
						dos.flush();
						byte[] d1 = dos.getMessageDigest().digest();
						if (!Arrays.equals(dg, d1))
							throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
					} else {
						while ((length = bis.read(buf)) != -1) os.write(buf, 0, length);
						os.flush();
					}
					files.add(dest.getPath());
					hashes.put(dest.getPath(), dg);
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
				exist = u.toString().equals((wa = wl.optJSONObject(iterator.next())) != null ? wa.optString(MainActivity.DB_KEY_URI) : null);
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				wa = new JSONObject();
				String id = MainActivity.genId();
				wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
				wa.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY);
				wa.put(MainActivity.DB_KEY_URI, u.toString());
				wa.put(MainActivity.DB_KEY_BACKUP, false);
				wl.put(id, wa);
				MainActivity.writeJson(this, db);
			}
			wApp = wa;
			Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show();    // TODO: Res
			getInfo(wv);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
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

	// 关闭/返回
	@Override
	public void onBackPressed() {
		if (mCustomView != null)
			wcc.onHideCustomView();
		else
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), null));
	}

	// 应用主题
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		int primColor = themeColor != null ? themeColor : getResources().getColor(R.color.design_default_color_primary);    // 优先主题色 >> 自动色
		float[] l = new float[3];
		if (themeColor != null) Color.colorToHSV(themeColor, l);
		boolean lightBar = themeColor != null ? l[2] > 0.75 : (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;    // 系统栏模式 根据主题色灰度/日夜模式
		int bar = 0;
		boolean landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
		toolbar.setVisibility(wApp != null && (landscape || hideAppbar && ready) ? View.GONE : View.VISIBLE);
		Window window = getWindow();
		if (MainActivity.APIOver23) {
			window.setStatusBarColor(primColor);
			if (MainActivity.APIOver26)
				window.setNavigationBarColor(primColor);
			bar = lightBar ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | (MainActivity.APIOver26 ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0) : View.SYSTEM_UI_FLAG_VISIBLE;
		}
		window.getDecorView().setSystemUiVisibility(bar | (landscape ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY : View.SYSTEM_UI_FLAG_VISIBLE));
		findViewById(R.id.wv_appbar).setBackgroundColor(primColor);
		toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);// 刷新字色
		toolbar.setSubtitleTextAppearance(this, R.style.TextAppearance_AppCompat_Small);
		toolbar.setNavigationIcon(MainActivity.APIOver24 || lightBar ? R.drawable.ic_arrow_back : R.drawable.ic_arrow_back_d);
		if (optMenu != null) {
			optMenu.findItem(R.id.action_save_c).setIcon(MainActivity.APIOver24 ? R.drawable.ic_save : lightBar ? R.drawable.ic_save_l : R.drawable.ic_save_d);
			optMenu.findItem(R.id.action_save_file).setIcon(MainActivity.APIOver24 ? R.drawable.ic_description : lightBar ? R.drawable.ic_description_l : R.drawable.ic_description_d);
			optMenu.findItem(R.id.action_save_link).setIcon(MainActivity.APIOver24 ? R.drawable.ic_language : lightBar ? R.drawable.ic_language_l : R.drawable.ic_language_d);
			optMenu.findItem(R.id.action_save).setIcon(optMenu.findItem(R.id.action_save_c).getIcon());
//			if (MainActivity.APIOver24) {
//				optMenu.findItem(R.id.action_save_c).setIcon(R.drawable.ic_save);
//				optMenu.findItem(R.id.action_save_file).setIcon(R.drawable.ic_description);
//				optMenu.findItem(R.id.action_save_link).setIcon(R.drawable.ic_language);
//				optMenu.findItem(R.id.action_save).setIcon(R.drawable.ic_save);
//			} else if (lightBar) {
//				optMenu.findItem(R.id.action_save_c).setIcon(R.drawable.ic_save_l);
//				optMenu.findItem(R.id.action_save_file).setIcon(R.drawable.ic_description_l);
//				optMenu.findItem(R.id.action_save_link).setIcon(R.drawable.ic_language_l);
//				optMenu.findItem(R.id.action_save).setIcon(R.drawable.ic_save_l);
//			} else {
//				optMenu.findItem(R.id.action_save_c).setIcon(R.drawable.ic_save_d);
//				optMenu.findItem(R.id.action_save_file).setIcon(R.drawable.ic_description_d);
//				optMenu.findItem(R.id.action_save_link).setIcon(R.drawable.ic_language_d);
//				optMenu.findItem(R.id.action_save).setIcon(R.drawable.ic_save_d);
//			}
		}
	}

	// WebView清理
	@Override
	protected void onDestroy() {
		if (wv != null) {
			wv.removeJavascriptInterface(JSI);
			wv.loadDataWithBaseURL(null, null, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
			wv.clearHistory();
			((ViewGroup) wv.getParent()).removeView(wv);
			wv.removeAllViews();
			wv.destroy();
			wv = null;
		}
		super.onDestroy();
	}
}