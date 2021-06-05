/*
 * top.donmor.tiddloid.TWEditorWV <= [P|Tiddloid]
 * Last modified: 18:33:05 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

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
import android.view.ViewParent;
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
import java.util.Objects;

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
	private boolean isWiki, isClassic, hideAppbar = false, ready = false;
	private byte[] exData = null;
	private Menu optMenu;
	private String id;
	private HashMap<String, byte[]> hashes = null;
	private DocumentFile tree = null, treeIndex = null;
	private ActivityResultLauncher<Intent> getChooserDL, getChooserImport, getChooserClone;


	// CONSTANT
	private static final String
			JSI = "twi",
			MIME_ANY = "*/*",
			MIME_TEXT = "text/plain",
			REX_SP_CHR = "\\s",
			STR_JS_POP_PRE = "(function(){new $tw.Story().navigateTiddler(\"",
			STR_JS_POP_POST = "\");})();",
			STR_JS_SETTINGS_C = "(function(){setOption(\"chkSaveBackups\",false);saveOption(\"chkSaveBackups\");setOption(\"chkHttpReadOnly\",false);saveOption(\"chkHttpReadOnly\");for(k in config.options){config.optionsSource[k]=\"setting\";}})();",
			STR_JS_PRINT = "(function(){window.print=function(){window.twi.print();}})();",
			STR_JS_SAVE = "(function(){$tw.saverHandler.saveWiki();})();",
			STR_JS_SAVE_C = "(function(){saveChanges();})();",
			KEY_COL = ":",
			KEY_ENC = "enc",
			KEY_ALG = "MD5",
			KEY_YES = "yes",
			SCH_ABOUT = "about",
	//			SCH_BLOB = "blob",
//			SCH_HTTP = "http",
//			SCH_HTTPS = "https",
	SCH_TEL = "tel",
			SCH_MAILTO = "mailto",
	//			SCH_JS = "javascript",
//			PREF_BLOB = "$blob$",
//			PREF_DEST = "$dest$",
	URL_BLANK = "about:blank";
//	private static final int REQUEST_DL = 906;

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
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			finish();
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
		wv = findViewById(R.id.twWebView);
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
		getChooserDL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				if (exData == null) return;
				uri = result.getData().getData();
				if (uri != null)
					try (OutputStream os = getContentResolver().openOutputStream(uri); InputStream is = new ByteArrayInputStream(exData)) {
						if (os == null || exData == null) throw new FileNotFoundException();
						int len = is.available();
						int length;
						int lengthTotal = 0;
						byte[] bytes = new byte[4096];
						while ((length = is.read(bytes)) > -1) {
							os.write(bytes, 0, length);
							lengthTotal += length;
						}
						os.flush();
						if (lengthTotal != len) throw new IOException();
					} catch (Exception e) {
						e.printStackTrace();
						try {
							DocumentsContract.deleteDocument(getContentResolver(), uri);
						} catch (Exception e1) {
							e.printStackTrace();
						}
						Toast.makeText(this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
					}
			}
			exData = null;
		});
		getChooserImport = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//			if (result.getData() != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && uploadMessage != null)
				uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.getResultCode(), result.getData()));
//			}
			uploadMessage = null;
		});
		getChooserClone = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				if (exData == null) return;
				uri = result.getData().getData();
				if (uri == null) return;
//				boolean created = false;
				try (ByteArrayInputStream is = new ByteArrayInputStream(exData);
					 OutputStream os = getContentResolver().openOutputStream(uri)) {
					if (os == null) throw new FileNotFoundException();
					//						String u = uri.toString();
//						try {
					JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
					boolean exist = false;
					String id = null;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						exist = uri.toString().equals((wa = wl.getJSONObject(id = iterator.next())).optString(MainActivity.DB_KEY_URI));
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
					wa.put(MainActivity.DB_KEY_URI, uri.toString());
					wa.put(MainActivity.DB_KEY_BACKUP, false);
//							db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
					if (!MainActivity.writeJson(this, db))
						throw new JSONException((String) null);

					int len = is.available();
					int length, lengthTotal = 0;
					byte[] b = new byte[4096];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lengthTotal += length;
					}
					os.flush();
//					System.out.println(lengthTotal);
					if (lengthTotal != len || !MainActivity.isWiki(this, uri))
						throw new IOException();
//					created = true;
//					boolean exist = false;
//					Uri u = Uri.fromFile(file);
//					JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
//					Iterator<String> iterator = wl.keys();
//					while (iterator.hasNext())
//						if ((wa = wl.getJSONObject(iterator.next())).optString(MainActivity.DB_KEY_URI).equals(u.toString())) {
//							exist = true;
//							break;
//						}
//					if (exist)
//						Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
//					else {
//						wa = new JSONObject();
//						String id = MainActivity.genId();
//						wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
//						wa.put(MainActivity.DB_KEY_URI, u.toString());
//						wl.put(id, wa);
//					}
//					wa.put(MainActivity.DB_KEY_BACKUP, false);
//					wApp = wa;
//					this.uri = uri;
//								db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
					if (!MainActivity.writeJson(this, db))
						throw new IOException();
//					if (wApp != null) {
////						runOnUiThread(() -> {
//						wv.reload();
//						wv.clearHistory();
////						});
//					}
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

//			// 浏览器标题
//			@Override
//			public void onReceivedTitle(WebView view, String title) {
//				if (wApp == null) {
//					setTitle(title);
//					toolbar.setSubtitle(null);
//				}
//			}

			// 5.0+ 导入文件
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				uploadMessage = filePathCallback;
//				Intent intent = fileChooserParams.createIntent();
				getChooserImport.launch(fileChooserParams.createIntent());
//				try {
//					startActivityForResult(intent, MainActivity.REQUEST_OPEN);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				return true;
			}

//			// 5.0+ 导入文件
//			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//			@Override
//			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//				File lastDir = Environment.getExternalStorageDirectory();
//				boolean showHidden = false;
//				try {
//					lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
//					showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				final int mode = fileChooserParams.getMode();
//				uploadMessage = filePathCallback;
//				FileDialog.fileDialog(TWEditorWV.this, lastDir, null, mode, 0, fileChooserParams.getAcceptTypes(), 0, showHidden, false, new FileDialog.OnFileTouchedListener() {
//					@Override
//					public void onFileTouched(File[] files) {
//						if (uploadMessage == null) return;
//						Uri[] results = null;
//						try {
//							switch (mode) {
//								case 0:
//									File file = files[0];
//									if (file != null && file.exists()) {
//										try {
//											results = new Uri[]{Uri.fromFile(file)};
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//									} else throw new Exception();
//									break;
//								case 1:
//									for (File file1 : files) {
//										try {
//											results = new Uri[]{Uri.parse(file1.toURI().toString())};
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//
//									}
//									break;
//								case 3:
//									File file3 = files[0];
//									if (file3 != null && file3.exists()) {
//										try {
//											results = new Uri[]{Uri.parse(file3.toURI().toString())};
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//									} else throw new Exception();
//									break;
//							}
//							db.put(MainActivity.DB_KEY_LAST_DIR, files[0].getParentFile().getAbsolutePath());
//							MainActivity.writeJson(TWEditorWV.this, db);
//
//						} catch (Exception e) {
//							e.printStackTrace();
//							Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
//						}
//						uploadMessage.onReceiveValue(results);
//						uploadMessage = null;
//					}
//
//					@Override
//					public void onCanceled() {
//						if (uploadMessage == null) return;
//						uploadMessage.onReceiveValue(null);
//						uploadMessage = null;
//					}
//				});
//				return true;
//			}

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
//				Uri cu;
//				if (data != null && !isDialog) {
//					if (!overrideUrlLoading(view, cu = Uri.parse(data)))
//						view.loadUrl(cu.toString());
//					return false;
//				}
				final WebView nwv = new WebView(TWEditorWV.this);
				final AlertDialog dialog = new AlertDialog.Builder(TWEditorWV.this)
						.setView(nwv)
						.setPositiveButton(android.R.string.ok, null)
						.setOnDismissListener(dialog1 -> nwv.destroy())
						.create();
				nwv.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						Uri u1;
						String p;
						if ((u1 = Uri.parse(url)).getSchemeSpecificPart().equals(Uri.parse(wv.getUrl()).getSchemeSpecificPart()) && (p = u1.getFragment()) != null) {
//							String p = url.substring(url.indexOf('#') + 1);
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
//		final class JavaScriptCallback {
		// JS请求处理
////			@JavascriptInterface
////			public void getB64(String data, String dest) {
////				MainActivity.wGet(TWEditorWV.this, Uri.parse(MainActivity.SCHEME_BLOB_B64 + ':' + data), new File(dest));
////			}
//
//			@JavascriptInterface
//			public void onDecrypted() {
//				runOnUiThread(() -> getInfo(wv));
//			}
//
//			// 打印
//			@JavascriptInterface
//			public void print() {
//				runOnUiThread(() -> {
//					PrintManager printManager = (PrintManager) TWEditorWV.this.getSystemService(Context.PRINT_SERVICE);
//					PrintDocumentAdapter printDocumentAdapter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? wv.createPrintDocumentAdapter(getTitle().toString()) : wv.createPrintDocumentAdapter();
//					printManager.print(getTitle().toString(), printDocumentAdapter, new PrintAttributes.Builder().build());
//				});
//			}
//
//			// AndTidWiki fallback
//			@JavascriptInterface
//			public void saveFile(String pathname, String data) {
//				saveWiki(data);
//			}
//
////			@JavascriptInterface
////			public void saveFile(String pathname, String data) {
////				try {
////					if (wApp == null || !isClassic || pathname.equals(Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)).getPath()))
////						saveWiki(data);
////				} catch (Exception e) {
////					e.printStackTrace();
////				}
////			}
//
//			@JavascriptInterface
//			public void saveDownload(String data) {
//				saveDownload(data, null);
//			}
//
//			// 保存文件（指名）
//			@JavascriptInterface
//			public void saveDownload(String data, String filename) {
//				TWEditorWV.exData = data.getBytes(StandardCharsets.UTF_8);
//				Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//				intent.addCategory(Intent.CATEGORY_OPENABLE);
//				intent.setType(MIME_ANY);
//				if (filename != null) intent.putExtra(Intent.EXTRA_TITLE, filename);
//				startActivityForResult(intent, KEY_REQ_DOWN);
//			}
////			@JavascriptInterface
////			public void saveDownload(final String data, String filename) {
//////				File lastDir = Environment.getExternalStorageDirectory();
//////				boolean showHidden = false;
//////				try {
//////					lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
//////					showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
//////				} catch (Exception e) {
//////					e.printStackTrace();
//////				}
////				FileDialog.fileSave(TWEditorWV.this, lastDir, filename != null && filename.length() > 0 ? filename : null, new FileDialogFilter[]{FileDialog.ALL}, showHidden, new FileDialog.OnFileTouchedListener() {
////					@Override
////					public void onFileTouched(File[] files) {
////						File file = files[0];
////						try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)); OutputStream os = new FileOutputStream(file)) {
////							if (file != null) {
////
////								int len = is.available();
////								int length, lengthTotal = 0;
////								byte[] b = new byte[4096];
////								while ((length = is.read(b)) != -1) {
////									os.write(b, 0, length);
////									lengthTotal += length;
////								}
////								os.flush();
////								if (lengthTotal != len) throw new Exception();
////							} else throw new Exception();
////						} catch (Exception e) {
////							e.printStackTrace();
////							if (file != null) {
////								file.delete();
////							}
////							Toast.makeText(TWEditorWV.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
////						}
////					}
////
////					@Override
////					public void onCanceled() {
////						Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
////					}
////				});
////			}
//
//			@JavascriptInterface
//			public void saveWiki(final String data) {
//				if (wApp != null && !MainActivity.SCH_HTTP.equals(uri.getScheme()) && !MainActivity.SCH_HTTPS.equals(uri.getScheme())) {
//					boolean legacy = MainActivity.SCH_FILE.equals(uri.getScheme());
////					File file = new File(Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)).getPath());
////					if (!file.exists()) {
////						Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
////						return;
////					}
//					if (wApp.optBoolean(MainActivity.DB_KEY_BACKUP)) try {
//						MainActivity.backup(TWEditorWV.this, uri);
//					} catch (IOException e) {
//						e.printStackTrace();
//						Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
//					}
//					try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
//						 OutputStream os = legacy ? new FileOutputStream(uri.getPath()) : getContentResolver().openOutputStream(uri)) {
//						if (os == null) throw new FileNotFoundException();
//						int len = is.available();
//						int length, lengthTotal = 0;
//						byte[] b = new byte[4096];
//						while ((length = is.read(b)) != -1) {
//							os.write(b, 0, length);
//							lengthTotal += length;
//						}
//						os.flush();
//						if (lengthTotal != len) throw new IOException();
//						runOnUiThread(() -> getInfo(wv));
//					} catch (IOException e) {
//						e.printStackTrace();
//						Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
//					}
//				} else {
//					runOnUiThread(() -> startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(MainActivity.TYPE_HTML), MainActivity.REQUEST_CREATE));
////					File lastDir = Environment.getExternalStorageDirectory();
////					boolean showHidden = false;
////					try {
////						lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
////						showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
////					} catch (Exception e) {
////						e.printStackTrace();
////					}
//					FileDialog.fileSave(TWEditorWV.this, lastDir, MainActivity.HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
//						@Override
//						public void onFileTouched(File[] files) {
//							File file;
//							boolean created = false;
//							try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
//								 FileOutputStream os = new FileOutputStream(file = files[0])) {
//								if (files[0] == null)
//									throw new FileNotFoundException();
//								int len = is.available();
//								System.out.println(len);
//								int length, lengthTotal = 0;
//								byte[] b = new byte[4096];
//								while ((length = is.read(b)) != -1) {
//									os.write(b, 0, length);
//									lengthTotal += length;
//								}
//								os.flush();
//								System.out.println(lengthTotal);
//								if (lengthTotal != len || !MainActivity.isWiki(file))
//									throw new IOException();
//								created = true;
//								boolean exist = false;
//								Uri u = Uri.fromFile(file);
//								JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
//								Iterator<String> iterator = wl.keys();
//								while (iterator.hasNext())
//									if ((wa = wl.getJSONObject(iterator.next())).optString(MainActivity.DB_KEY_URI).equals(u.toString())) {
//										exist = true;
//										break;
//									}
//								if (exist)
//									Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
//								else {
//									wa = new JSONObject();
//									String id = MainActivity.genId();
//									wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
//									wa.put(MainActivity.DB_KEY_URI, u.toString());
//									wl.put(id, wa);
//								}
//								wa.put(MainActivity.DB_KEY_BACKUP, false);
//								wApp = wa;
////								db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//								if (!MainActivity.writeJson(TWEditorWV.this, db))
//									throw new IOException();
//								if (wApp != null) {
//									runOnUiThread(() -> {
//										wv.reload();
//										wv.clearHistory();
//									});
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//								Toast.makeText(TWEditorWV.this, created ? R.string.data_error : R.string.error_processing_file, Toast.LENGTH_SHORT).show();
//							}
//						}
//
//						@Override
//						public void onCanceled() {
//							Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
//						}
//					});	// TODO: Refactor to SAF
//				}
//			}
//
//			@JavascriptInterface
//			public void exportDB() {
//				MainActivity.exportJson(TWEditorWV.this, db);
//			}
//
//		}
		wv.addJavascriptInterface(new Object() {
			// JS请求处理
//			@JavascriptInterface
//			public void getB64(String data, String dest) {
//				MainActivity.wGet(TWEditorWV.this, Uri.parse(MainActivity.SCHEME_BLOB_B64 + ':' + data), new File(dest));
//			}

			@JavascriptInterface
			public void onDecrypted() {
				runOnUiThread(() -> getInfo(wv));
			}

			// 打印
			@JavascriptInterface
			public void print() {
				runOnUiThread(() -> {
					PrintManager printManager = (PrintManager) TWEditorWV.this.getSystemService(Context.PRINT_SERVICE);
					PrintDocumentAdapter printDocumentAdapter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? wv.createPrintDocumentAdapter(getTitle().toString()) : wv.createPrintDocumentAdapter();
					printManager.print(getTitle().toString(), printDocumentAdapter, new PrintAttributes.Builder().build());
				});
			}

			// AndTidWiki fallback
			@JavascriptInterface
			public void saveFile(String pathname, String data) {
				saveWiki(data);
			}

//			@JavascriptInterface
//			public void saveFile(String pathname, String data) {
//				try {
//					if (wApp == null || !isClassic || pathname.equals(Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)).getPath()))
//						saveWiki(data);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}

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
//				startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT)
//						.addCategory(Intent.CATEGORY_OPENABLE)
//						.setType(MIME_ANY)
//						.putExtra(Intent.EXTRA_TITLE, filename), REQUEST_DL);
			}
//			@JavascriptInterface
//			public void saveDownload(final String data, String filename) {
////				File lastDir = Environment.getExternalStorageDirectory();
////				boolean showHidden = false;
////				try {
////					lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
////					showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
////				} catch (Exception e) {
////					e.printStackTrace();
////				}
//				FileDialog.fileSave(TWEditorWV.this, lastDir, filename != null && filename.length() > 0 ? filename : null, new FileDialogFilter[]{FileDialog.ALL}, showHidden, new FileDialog.OnFileTouchedListener() {
//					@Override
//					public void onFileTouched(File[] files) {
//						File file = files[0];
//						try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)); OutputStream os = new FileOutputStream(file)) {
//							if (file != null) {
//
//								int len = is.available();
//								int length, lengthTotal = 0;
//								byte[] b = new byte[4096];
//								while ((length = is.read(b)) != -1) {
//									os.write(b, 0, length);
//									lengthTotal += length;
//								}
//								os.flush();
//								if (lengthTotal != len) throw new Exception();
//							} else throw new Exception();
//						} catch (Exception e) {
//							e.printStackTrace();
//							if (file != null) {
//								file.delete();
//							}
//							Toast.makeText(TWEditorWV.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//						}
//					}
//
//					@Override
//					public void onCanceled() {
//						Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
//					}
//				});
//			}

			@JavascriptInterface
			public void saveWiki(final String data) {
				if (wApp != null && !MainActivity.SCH_HTTP.equals(uri.getScheme()) && !MainActivity.SCH_HTTPS.equals(uri.getScheme())) {
					boolean legacy = MainActivity.SCH_FILE.equals(uri.getScheme());
//					File file = new File(Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)).getPath());
//					if (!file.exists()) {
//						Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
//						return;
//					}
					if (wApp.optBoolean(MainActivity.DB_KEY_BACKUP)) try {
						MainActivity.backup(TWEditorWV.this, Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)));
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
					}
					try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
						 OutputStream os = legacy ? new FileOutputStream(uri.getPath()) : getContentResolver().openOutputStream(uri)) {
						if (os == null) throw new FileNotFoundException();
						int len = is.available();
						int length, lengthTotal = 0;
						byte[] b = new byte[4096];
						while ((length = is.read(b)) != -1) {
							os.write(b, 0, length);
							lengthTotal += length;
						}
						os.flush();
						if (lengthTotal != len) throw new IOException();
						runOnUiThread(() -> getInfo(wv));
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
					}
				} else {
					exData = data.getBytes(StandardCharsets.UTF_8);
					getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
							.addCategory(Intent.CATEGORY_OPENABLE)
							.setType(MainActivity.TYPE_HTML));
//					startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT)
//							.addCategory(Intent.CATEGORY_OPENABLE)
//							.setType(MainActivity.TYPE_HTML), MainActivity.REQUEST_CLONE);
//					File lastDir = Environment.getExternalStorageDirectory();
//					boolean showHidden = false;
//					try {
//						lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
//						showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					FileDialog.fileSave(TWEditorWV.this, lastDir, MainActivity.HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
//						@Override
//						public void onFileTouched(File[] files) {
//							File file;
//							boolean created = false;
//							try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
//								 FileOutputStream os = new FileOutputStream(file = files[0])) {
//								if (files[0] == null)
//									throw new FileNotFoundException();
//								int len = is.available();
//								System.out.println(len);
//								int length, lengthTotal = 0;
//								byte[] b = new byte[4096];
//								while ((length = is.read(b)) != -1) {
//									os.write(b, 0, length);
//									lengthTotal += length;
//								}
//								os.flush();
//								System.out.println(lengthTotal);
//								if (lengthTotal != len || !MainActivity.isWiki(file))
//									throw new IOException();
//								created = true;
//								boolean exist = false;
//								Uri u = Uri.fromFile(file);
//								JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
//								Iterator<String> iterator = wl.keys();
//								while (iterator.hasNext())
//									if ((wa = wl.getJSONObject(iterator.next())).optString(MainActivity.DB_KEY_URI).equals(u.toString())) {
//										exist = true;
//										break;
//									}
//								if (exist)
//									Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
//								else {
//									wa = new JSONObject();
//									String id = MainActivity.genId();
//									wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
//									wa.put(MainActivity.DB_KEY_URI, u.toString());
//									wl.put(id, wa);
//								}
//								wa.put(MainActivity.DB_KEY_BACKUP, false);
//								wApp = wa;
////								db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//								if (!MainActivity.writeJson(TWEditorWV.this, db))
//									throw new IOException();
//								if (wApp != null) {
//									runOnUiThread(() -> {
//										wv.reload();
//										wv.clearHistory();
//									});
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//								Toast.makeText(TWEditorWV.this, created ? R.string.data_error : R.string.error_processing_file, Toast.LENGTH_SHORT).show();
//							}
//						}
//
//						@Override
//						public void onCanceled() {
//							Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
//						}
//					});    // TODO: Refactor to SAF
				}
			}

			@JavascriptInterface
			public void exportDB() {
				MainActivity.exportJson(TWEditorWV.this, db);
			}

		}, JSI);
//		wv.addJavascriptInterface(new JavaScriptCallback(), JSI);
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
					isWiki = Boolean.parseBoolean(value);
					if (isWiki)
						view.evaluateJavascript(getString(R.string.js_is_classic), value1 -> {
							isClassic = Boolean.parseBoolean(value1);
//							if (wApp == null) {
//								Toast.makeText(TWEditorWV.this, R.string.ready_to_fork, Toast.LENGTH_SHORT).show();
//								toolbar.setLogo(R.drawable.ic_fork);
//							}
							getInfo(view);
							view.getSettings().setBuiltInZoomControls(isClassic);
							view.getSettings().setDisplayZoomControls(isClassic);
							if (isClassic) view.evaluateJavascript(STR_JS_SETTINGS_C, null);
						});
					else
						Toast.makeText(TWEditorWV.this, "The page is not a TiddlyWiki", Toast.LENGTH_SHORT).show();    // TODO: -> R.string
				});
				view.clearHistory();
//				File[] ppp = new File(getCacheDir(), id).listFiles();
//				File[] qqq = new File(new File(getCacheDir(), id), "index.html_backup").listFiles();
//				System.out.println(wv.getUrl());
//				System.out.println(Arrays.toString(ppp));
//				System.out.println(Arrays.toString(qqq));
//				if (wApp != null) view.clearHistory();
			}
		});
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
//		// 下载服务
//		wv.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
//			File lastDir = Environment.getExternalStorageDirectory();
//			boolean showHidden = false;
//			try {
//				lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
//				showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			String filenameProbable = URLUtil.guessFileName(url, contentDisposition, mimeType);
//			FileDialog.fileDialog(TWEditorWV.this, lastDir, filenameProbable, 3, 0, new String[]{mimeType, FileDialog.MIME_ALL}, 0, showHidden, false, new FileDialog.OnFileTouchedListener() {
//				@Override
//				public void onFileTouched(File[] files) {
//					String scheme = Uri.parse(url) != null ? Uri.parse(url).getScheme() : null;
//					if (scheme != null && scheme.equals(SCH_BLOB)) {
//						wv.loadUrl(SCH_JS + ':' + getString(R.string.js_blob).replace(PREF_BLOB, url).replace(PREF_DEST, files[0].getAbsolutePath()));
//					} else
//						MainActivity.wGet(TWEditorWV.this, Uri.parse(url), files[0]);
//				}
//
//				@Override
//				public void onCanceled() {
//					Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
//				}
//			});
//		});
//		Intent intent = getIntent();
//		Bundle bu;
//		String action = intent.getAction();
//		JSONObject wl;
//		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式添加文件
//			System.out.println("VIEW");
//			Uri u = intent.getData();
//			if (u != null)addFromUri(u);
//			else finish();
//		} else if (Intent.ACTION_SEND.equals(action)) {    // 分享链接克隆站点
//			System.out.println("SEND");
////		} else if (Intent.ACTION_MAIN.equals(action)) {    // MA/Shortcut
////			System.out.println("MAIN");
////			nextWiki(intent);
//		} else{
//				if ((wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null || (bu = intent.getExtras()) == null || wl.optJSONObject(bu.getString(MainActivity.KEY_ID)) == null) {
//					Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
//					finish();
//					break;
//				}
		nextWiki(getIntent());
//		}
//		if ((bu = intent.getExtras()) != null && (fid = bu.getString(MainActivity.KEY_ID)) == null) {
//			wvs.setJavaScriptEnabled(true);
//			String vu;
//			wv.loadUrl((vu = bu.getString(MainActivity.KEY_URL)) != null ? vu : URL_BLANK);
//			return;
//		}
//		if ((wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null || (wl.optJSONObject(fid)) == null) {
//			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
//			finish();
//			return;
//		}
//		nextWiki(intent);
	}

	// 热启动
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		Bundle bu;
		String action = intent.getAction(), fid;
		JSONObject wl, wa;
		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式添加文件
			System.out.println("VIEW");
			Uri u;
			if ((u = intent.getData()) == null || !MainActivity.isWiki(this, u)) {
				Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else if (Intent.ACTION_SEND.equals(action)) {    // 分享链接克隆站点
			System.out.println("SEND");
			if (!(MainActivity.TYPE_HTML.equals(intent.getType()) && !MainActivity.isWiki(this, intent.getParcelableExtra(Intent.EXTRA_STREAM))) || !(MIME_TEXT.equals(intent.getType()) && intent.getStringExtra(Intent.EXTRA_TEXT).contains(MainActivity.SCH_HTTP))) {
				Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else {    // MA/Shortcut
			System.out.println("MAIN");
			if ((bu = intent.getExtras()) == null || (fid = bu.getString(MainActivity.KEY_ID)) == null)
				return;
			if ((wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null || (wa = wl.optJSONObject(fid)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				return;
			}
			if (wa == wApp) return;
//				if (isWiki) {
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
//				} else nextWiki(intent);
		}
	}

//	private void addFromUri(Uri u){
//		;
//	}
//	private void addFromSharedUrl(){}
//	private void addFromSharedFile(){}

	// 处理跳转App
	private boolean overrideUrlLoading(final WebView view, Uri u) {
//		String s1 = u.toString(), s2 = uri != null ? uri.toString() : MainActivity.STR_EMPTY;
		if (u.getSchemeSpecificPart().equals(Uri.parse(wv.getUrl()).getSchemeSpecificPart()))
			return false;
//		int x;
//		int q1 = (x = s1.indexOf('?')) != -1 ? x : (x = s1.indexOf('#')) != -1 ? x : s1.length(),
//				q2 = (x = s2.indexOf('?')) != -1 ? x : (x = s2.indexOf('#')) != -1 ? x : s2.length();
//		if (s1.substring(0, q1).equals(s2.substring(0, q2))) return false;
		String sch = u.getScheme();
//		boolean browse = SCH_ABOUT.equals(sch) || MainActivity.SCH_HTTP.equals(sch) || MainActivity.SCH_HTTPS.equals(sch);
		if (sch == null || sch.length() == 0)
//		if (sch == null || sch.length() == 0 || wApp == null && browse)
			return false;
		try {
			final Intent intent;
			switch (sch) {
				case SCH_TEL:
					intent = new Intent(Intent.ACTION_DIAL, u);
					view.getContext().startActivity(intent);
					break;
				case SCH_MAILTO:
					intent = new Intent(Intent.ACTION_SENDTO, u);
					view.getContext().startActivity(intent);
					break;
				case SCH_ABOUT:
				case MainActivity.SCH_FILE:
				case MainActivity.SCH_HTTP:
				case MainActivity.SCH_HTTPS:
					intent = new Intent(Intent.ACTION_VIEW, u);
					view.getContext().startActivity(intent);
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
								} catch (Exception e) {
									e.printStackTrace();
								}
							}).show();
					break;
			}
		} catch (Exception e) {
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
				if (color.length() == 7) themeColor = Color.parseColor(color);
				else themeColor = null;
				TWEditorWV.this.onConfigurationChanged(newConfig);
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
					optMenu.getItem(0).setVisible(wApp == null && uri != null);
					optMenu.getItem(1).setVisible(wApp != null || uri == null);
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

//	// 接收导入导出文件
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//		super.onActivityResult(requestCode, resultCode, resultData);
//		if (resultCode != RESULT_OK) return;
//		Uri uri;
//		switch (requestCode) {
//			case REQUEST_DL:
//				if (exData == null) break;
//				uri = resultData.getData();
//				if (uri != null)
//					try (OutputStream os = getContentResolver().openOutputStream(uri); InputStream is = new ByteArrayInputStream(exData)) {
//						if (os == null || exData == null) throw new FileNotFoundException();
//						int len = is.available();
//						int length;
//						int lengthTotal = 0;
//						byte[] bytes = new byte[4096];
//						while ((length = is.read(bytes)) > -1) {
//							os.write(bytes, 0, length);
//							lengthTotal += length;
//						}
//						os.flush();
//						if (lengthTotal != len) throw new IOException();
//
//
//					} catch (Exception e) {
//						e.printStackTrace();
//						try {
//							DocumentsContract.deleteDocument(getContentResolver(), uri);
//						} catch (Exception e1) {
//							e.printStackTrace();
//						}
//						Toast.makeText(this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//					}
//				break;
//			case MainActivity.REQUEST_OPEN:
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && uploadMessage != null)
//					uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, resultData));
//				break;
//			case MainActivity.REQUEST_CLONE:
//				if (exData == null) break;
//				uri = resultData.getData();
//				if (uri == null) break;
////				boolean created = false;
//				try (ByteArrayInputStream is = new ByteArrayInputStream(exData);
//					 OutputStream os = getContentResolver().openOutputStream(uri)) {
//					if (os == null) throw new FileNotFoundException();
//					//						String u = uri.toString();
////						try {
//					JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
//					boolean exist = false;
//					String id = null;
//					Iterator<String> iterator = wl.keys();
//					while (iterator.hasNext()) {
//						exist = uri.toString().equals((wa = wl.getJSONObject(id = iterator.next())).optString(MainActivity.DB_KEY_URI));
//						if (exist) break;
//					}
//					if (exist)
//						Toast.makeText(this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
//					else {
//						wa = new JSONObject();
//						id = MainActivity.genId();
//						wl.put(id, wa);
//					}
//					wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
//					wa.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY);
//					wa.put(MainActivity.DB_KEY_URI, uri.toString());
//					wa.put(MainActivity.DB_KEY_BACKUP, false);
////							db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//					if (!MainActivity.writeJson(this, db))
//						throw new JSONException((String) null);
//
//					int len = is.available();
//					int length, lengthTotal = 0;
//					byte[] b = new byte[4096];
//					while ((length = is.read(b)) != -1) {
//						os.write(b, 0, length);
//						lengthTotal += length;
//					}
//					os.flush();
////					System.out.println(lengthTotal);
//					if (lengthTotal != len || !MainActivity.isWiki(this, uri))
//						throw new IOException();
////					created = true;
////					boolean exist = false;
////					Uri u = Uri.fromFile(file);
////					JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
////					Iterator<String> iterator = wl.keys();
////					while (iterator.hasNext())
////						if ((wa = wl.getJSONObject(iterator.next())).optString(MainActivity.DB_KEY_URI).equals(u.toString())) {
////							exist = true;
////							break;
////						}
////					if (exist)
////						Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
////					else {
////						wa = new JSONObject();
////						String id = MainActivity.genId();
////						wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
////						wa.put(MainActivity.DB_KEY_URI, u.toString());
////						wl.put(id, wa);
////					}
////					wa.put(MainActivity.DB_KEY_BACKUP, false);
////					wApp = wa;
////					this.uri = uri;
////								db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//					if (!MainActivity.writeJson(this, db))
//						throw new IOException();
////					if (wApp != null) {
//////						runOnUiThread(() -> {
////						wv.reload();
////						wv.clearHistory();
//////						});
////					}
//					Bundle bu = new Bundle();
//					bu.putString(MainActivity.KEY_ID, id);
//					nextWiki(new Intent().putExtras(bu));
//				} catch (IOException e) {
//					e.printStackTrace();
//					Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
//				} catch (JSONException e) {
//					e.printStackTrace();
//					Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
//				}
//				break;
//		}
////		if (requestCode == MainActivity.REQUEST_OPEN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && uploadMessage != null)
////			uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, resultData));
//		exData = null;
//		uploadMessage = null;
//	}

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
		JSONObject wa;
		Bundle bu;
		final Uri u;
		String data = null;
		final String action = nextWikiIntent.getAction();
		String nextWikiId = null;
		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式，scheme -> content/file/http(s)
			if ((u = nextWikiIntent.getData()) == null || !MainActivity.isWiki(this, u)) {
				Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if (MainActivity.SCH_CONTENT.equals(u.getScheme()) || MainActivity.SCH_FILE.equals(u.getScheme())) {
				try {
					wl = db.getJSONObject(MainActivity.DB_KEY_WIKI);
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
					if (!MainActivity.writeJson(this, db))
						throw new JSONException(MainActivity.STR_EMPTY);
					if (MainActivity.SCH_CONTENT.equals(u.getScheme()))
						getContentResolver().takePersistableUriPermission(u, MainActivity.TAKE_FLAGS);
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
					wa = null;
				}
			} else wa = null;
		} else if (Intent.ACTION_SEND.equals(action)) {
			wa = null;
			if (MainActivity.TYPE_HTML.equals(nextWikiIntent.getType())) {    // 接收html文件
				Uri u1 = nextWikiIntent.getParcelableExtra(Intent.EXTRA_STREAM);

//				data = nextWikiIntent.getStringExtra(Intent.EXTRA_TEXT);
				if (!MainActivity.isWiki(this, u1)) {
					Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				try (BufferedInputStream is = new BufferedInputStream(Objects.requireNonNull(getContentResolver().openInputStream(u1)));
					 ByteArrayOutputStream os = new ByteArrayOutputStream(MainActivity.BUF_SIZE)) {   //读全部数据
					int len = is.available();
					int length, lenTotal = 0;
					byte[] b = new byte[MainActivity.BUF_SIZE];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lenTotal += length;
					}
					os.flush();
					if (lenTotal != len) throw new IOException();
					data = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(os.toByteArray())).toString();
				} catch (IOException e) {
					e.printStackTrace();
				}
				u = null;
			} else if (MIME_TEXT.equals(nextWikiIntent.getType()) && (data = nextWikiIntent.getStringExtra(Intent.EXTRA_TEXT)).contains(MainActivity.SCH_HTTP)) {    // 接收包含url的string
				Uri u1 = null;
				for (String s : data.split(REX_SP_CHR)) {
					if (s.contains(MainActivity.SCH_HTTP)) {
						u1 = Uri.parse(s.substring(s.indexOf(MainActivity.SCH_HTTP)));
						u1 = Uri.parse(u1.getScheme() + KEY_COL + u1.getSchemeSpecificPart());
						break;
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
					|| (wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null
					|| (wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
//		if (Intent.ACTION_VIEW.equals(action) &&
//				||  && nextWikiIntent.getStringExtra(Intent.EXTRA_TEXT) == null
//				|| (bu = nextWikiIntent.getExtras()) == null
//				|| (nextWikiId = bu.getString(MainActivity.KEY_ID)) == null
//				|| nextWikiId.length() == 0
//				|| (wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null
//				|| (wa = wl.optJSONObject(nextWikiId)) == null) {
//			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
//			finish();
//			return;
//		}
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
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && u != null && MainActivity.SCH_CONTENT.equals(u.getScheme()))
									revokeUriPermission(getPackageName(), u, MainActivity.TAKE_FLAGS);
							} catch (Exception e) {
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
		}
		// 解取Title/Subtitle/favicon
		wApp = wa;    // nonnull: normal/file/content/url; null: http(s)/html
		uri = u;    // nonnull: normal/file/content/http(s)/url; null: html
		id = nextWikiId;
		Uri actualUri = u;
		if (u != null && !MainActivity.SCH_FILE.equals(u.getScheme())) try {
			tree = DocumentFile.fromTreeUri(this, u);
			DocumentFile p;
			if (tree == null || !tree.isDirectory()) throw new IOException();
			treeIndex = (p = tree.findFile(MainActivity.KEY_FN_INDEX)) != null && p.isFile() ? p : (p = tree.findFile(MainActivity.KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
			if (treeIndex == null || !treeIndex.isFile()) throw new IOException();
//			DocumentFile df = (p = tree.findFile(MainActivity.KEY_FN_INDEX)) != null && p.isFile() ? p : (p = tree.findFile(MainActivity.KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
//			if (df == null || !df.isFile()) throw new IOException();
			uri = treeIndex.getUri();
			actualUri = Uri.fromFile(syncTree(tree, id, treeIndex));
		} catch (IllegalArgumentException ignored) {
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
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
			try {
				themeColor = wApp.getInt(MainActivity.DB_KEY_COLOR);
			} catch (JSONException e) {
				themeColor = null;
			}
			onConfigurationChanged(getResources().getConfiguration());
		}
		wv.getSettings().setJavaScriptEnabled(true);
		if (u != null && MainActivity.SCH_CONTENT.equals(u.getScheme()))
			getContentResolver().takePersistableUriPermission(u, MainActivity.TAKE_FLAGS);  //保持读写权限
		if (uri != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || !MainActivity.SCH_CONTENT.equals(u.getScheme())) || tree != null && treeIndex != null)
			wv.loadUrl(actualUri != null ? actualUri.toString() : MainActivity.STR_EMPTY);
		else {
			if (uri == null && data == null) {
				wv.loadUrl(MainActivity.STR_EMPTY);
				return;
			}
//			String data = null;
			if (uri != null)
				try (BufferedInputStream is = new BufferedInputStream(Objects.requireNonNull(getContentResolver().openInputStream(uri)));
					 ByteArrayOutputStream os = new ByteArrayOutputStream(MainActivity.BUF_SIZE)) {   //读全部数据
					int len = is.available();
					int length, lenTotal = 0;
					byte[] b = new byte[MainActivity.BUF_SIZE];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lenTotal += length;
					}
					os.flush();
					if (lenTotal != len) throw new IOException();
					data = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(os.toByteArray())).toString();
				} catch (Exception e) {
					e.printStackTrace();
				}
			wv.loadDataWithBaseURL(uri != null ? uri.toString() : null, data != null ? data : MainActivity.STR_EMPTY, MainActivity.TYPE_HTML, StandardCharsets.UTF_8.name(), null);
		}
	}

	@NonNull
	private File syncTree(DocumentFile dir, String id, DocumentFile index) throws IOException, SecurityException {
		File cacheRoot = new File(getCacheDir(), id);
		if (hashes == null) hashes = new HashMap<>();    // TODO: 预先计算hash
		HashSet<String> files = new HashSet<>();
		syncDir(dir, cacheRoot, files);
		clrDir(cacheRoot, files);
		if (index.getName() == null) throw new IOException();
		return new File(cacheRoot, index.getName());
	}

	private void syncDir(DocumentFile src, File pos, HashSet<String> files) throws IOException, SecurityException {
		if (src == null || !src.isDirectory()) throw new IOException();
		if (!pos.isDirectory()) pos.delete();
		if (!pos.exists()) pos.mkdir();
		for (DocumentFile inner : src.listFiles())
			if (inner.isFile()) {
				if (inner.getName() == null) break;
				File dest = new File(pos, inner.getName());
				byte[] ba, dg = null;
				MessageDigest messageDigest = null;
				try {
					messageDigest = MessageDigest.getInstance(KEY_ALG);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				try (InputStream is = getContentResolver().openInputStream(inner.getUri());
					 DigestInputStream dis = messageDigest != null ? new DigestInputStream(is, messageDigest) : null;
					 ByteArrayOutputStream bos = new ByteArrayOutputStream(MainActivity.BUF_SIZE)) {
					if (is == null) return;
					if (dis != null) dis.on(true);
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dis != null)
						while ((length = dis.read(buf)) != -1) bos.write(buf, 0, length);
					else while ((length = is.read(buf)) != -1) bos.write(buf, 0, length);
					bos.flush();
					if (dis != null && Arrays.equals(hashes.get(dest.getPath()), dg = dis.getMessageDigest().digest()))
						break;
					ba = bos.toByteArray();
				}
				try (
						ByteArrayInputStream bis = new ByteArrayInputStream(ba);
						FileOutputStream os = new FileOutputStream(dest);
						DigestOutputStream dos = messageDigest != null ? new DigestOutputStream(os, messageDigest) : null) {
					if (dos != null) dos.on(true);
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dos != null) {
						while ((length = bis.read(buf)) != -1) dos.write(buf, 0, length);
						dos.flush();
					} else {
						while ((length = bis.read(buf)) != -1) os.write(buf, 0, length);
						os.flush();
					}
					byte[] d1 = dos != null ? dos.getMessageDigest().digest() : null;
					if (messageDigest != null && !Arrays.equals(dg, d1)) throw new IOException();
					files.add(dest.getPath());
					hashes.put(dest.getPath(), dg);
				}
//				if () try (InputStream bis = ba != null ? new ByteArrayInputStream();
//						   OutputStream dos = new DigestOutputStream(new FileOutputStream(dest), messageDigest)) {
//				} catch (NoSuchAlgorithmException e) {
//					e.printStackTrace();
//				}
			} else if (inner.isDirectory()) {
				if (inner.getName() == null) break;
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
			}
			if (!MainActivity.writeJson(this, db))
				throw new JSONException(MainActivity.STR_EMPTY);
			wApp = wa;
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
//		else if (wv.canGoBack())
//			wv.goBack();
		else if (isWiki) {
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), null));
		} else {
			TWEditorWV.super.onBackPressed();
		}
	}

	// 应用主题
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		int primColor = themeColor != null ? themeColor : getResources().getColor(R.color.design_default_color_primary);    // 优先主题色 >> 自动色
		float[] l = new float[3];
		Color.colorToHSV(primColor, l);
		boolean lightBar = themeColor != null ? (l[2] > 0.75) : (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;    // 系统栏模式 根据主题色灰度/日夜模式
		try {
			int bar = 0;
			boolean landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
			toolbar.setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || hideAppbar && ready ? View.GONE : View.VISIBLE);
			Window window = getWindow();
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				window.setStatusBarColor(primColor);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
					window.setNavigationBarColor(primColor);
				bar = lightBar ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0) : View.SYSTEM_UI_FLAG_VISIBLE;
			}
			window.getDecorView().setSystemUiVisibility(bar | (landscape ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY : View.SYSTEM_UI_FLAG_VISIBLE) : View.SYSTEM_UI_FLAG_VISIBLE));
			findViewById(R.id.wv_appbar).setBackgroundColor(primColor);
			toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
			toolbar.setSubtitleTextAppearance(this, R.style.TextAppearance_AppCompat_Small);
			if (themeColor != null) {    // 有主题色则根据灰度切换字色
				toolbar.setTitleTextColor(getResources().getColor(lightBar ? R.color.content_tint_l : R.color.content_tint_d));
				toolbar.setSubtitleTextColor(getResources().getColor(lightBar ? R.color.content_sub_l : R.color.content_sub_d));
			}
			toolbar.setNavigationIcon(themeColor != null ? (lightBar ? R.drawable.ic_arrow_back_l : R.drawable.ic_arrow_back_d) : R.drawable.ic_arrow_back);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// WebView清理
	@Override
	protected void onDestroy() {
		if (wv != null) {
			ViewParent parent = wv.getParent();
			if (parent != null) ((ViewGroup) parent).removeView(wv);
			wv.stopLoading();
			wv.getSettings().setJavaScriptEnabled(false);
			wv.removeJavascriptInterface(JSI);
			wv.clearHistory();
			wv.loadUrl(URL_BLANK);
			wv.removeAllViews();
			wv.destroyDrawingCache();
			wv.destroy();
			wv = null;
		}
		super.onDestroy();
	}
}