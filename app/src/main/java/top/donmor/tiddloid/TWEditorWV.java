/*
 * top.donmor.tiddloid.TWEditorWV <= [P|Tiddloid]
 * Last modified: 18:33:05 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.annotation.TargetApi;
import android.content.DialogInterface;
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
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.github.donmor.filedialog.lib.FileDialog;
import com.github.donmor.filedialog.lib.FileDialogFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;

public class TWEditorWV extends AppCompatActivity {

	private JSONObject db;
	private JSONObject wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private int mOriginalOrientation;
	//	private int nextWikiSerial = -1;
	private Integer themeColor = null;
	private float scale;
	//	private Intent nextWikiIntent;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private Toolbar toolbar;
	private ProgressBar wvProgress;
	private Uri uri = null;
	private boolean isWiki, isClassic, hideAppbar = false, ready = false;
//	private String id;

	// CONSTANT
	private static final String
//			F02D = "%02d",
//			F03D = "%03d",
//			F04D = "%04d",
			JSI = "twi",
			KEY_HASH_1 = "(function(){new $tw.Story().navigateTiddler(\"",
			KEY_HASH_2 = "\");})();",
			KEY_YES = "yes",
			SCH_ABOUT = "about",
			SCH_BLOB = "blob",
			SCH_HTTP = "http",
			SCH_HTTPS = "https",
			SCH_TEL = "tel",
			SCH_MAILTO = "mailto",
			SCH_JS = "javascript",
			PREF_BLOB = "$blob$",
			PREF_DEST = "$dest$",
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
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			finish();
		}
		MainActivity.trimDB140(this, db);
		// 初始化顶栏
		toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		this.setTitle(R.string.app_name);
		onConfigurationChanged(getResources().getConfiguration());
		// 初始化WebView
		wv = findViewById(R.id.twWebView);
		wvProgress = findViewById(R.id.progressBar);
		wvProgress.setMax(100);
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
		scale = getResources().getDisplayMetrics().density;
		wcc = new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				ready = newProgress == 100;
				toolbar.setVisibility(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE || hideAppbar && ready ? View.GONE : View.VISIBLE);
				wvProgress.setVisibility(ready ? View.GONE : View.VISIBLE);
				wvProgress.setProgress(newProgress);
				super.onProgressChanged(view, newProgress);
			}

			// 浏览器标题
			@Override
			public void onReceivedTitle(WebView view, String title) {
				if (wApp == null) {
					setTitle(title);
					toolbar.setSubtitle(null);
				}
			}

			// 5.0+ 导入文件
			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
					showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				final int mode = fileChooserParams.getMode();
				uploadMessage = filePathCallback;
				FileDialog.fileDialog(TWEditorWV.this, lastDir, null, mode, 0, fileChooserParams.getAcceptTypes(), 0, showHidden, false, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						if (uploadMessage == null) return;
						Uri[] results = null;
						try {
							switch (mode) {
								case 0:
									File file = files[0];
									if (file != null && file.exists()) {
										try {
											results = new Uri[]{Uri.fromFile(file)};
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else throw new Exception();
									break;
								case 1:
									for (File file1 : files) {
										try {
											results = new Uri[]{Uri.parse(file1.toURI().toString())};
										} catch (Exception e) {
											e.printStackTrace();
										}

									}
									break;
								case 3:
									File file3 = files[0];
									if (file3 != null && file3.exists()) {
										try {
											results = new Uri[]{Uri.parse(file3.toURI().toString())};
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else throw new Exception();
									break;
							}
							db.put(MainActivity.DB_KEY_LAST_DIR, files[0].getParentFile().getAbsolutePath());
							MainActivity.writeJson(TWEditorWV.this, db);

						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
						}
						uploadMessage.onReceiveValue(results);
						uploadMessage = null;
					}

					@Override
					public void onCanceled() {
						if (uploadMessage == null) return;
						uploadMessage.onReceiveValue(null);
						uploadMessage = null;
					}
				});
				return true;
			}

			// 全屏
			@Override
			public void onShowCustomView(View view,
										 WebChromeClient.CustomViewCallback callback) {
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
				if (data != null) return overrideUrlLoading(view, Uri.parse(data));
				final WebView nwv = new WebView(TWEditorWV.this);
				final AlertDialog dialog = new AlertDialog.Builder(TWEditorWV.this).setView(nwv).setPositiveButton(android.R.string.ok, null).setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						nwv.destroy();
					}
				}).create();
				nwv.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						if (url.startsWith(TWEditorWV.this.uri.toString())) {
							String p = url.substring(url.indexOf('#') + 1);
							wv.evaluateJavascript(KEY_HASH_1 + Uri.decode(p) + KEY_HASH_2, null);
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
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});


		//TODO: Refactor
//		Bundle bu = getIntent().getExtras();
//		String ueu = URL_BLANK;
//		CharSequence wvTitle = null, wvSubTitle = null;
//		id = bu != null ? bu.getString(MainActivity.KEY_ID) : null;
//		boolean shortcut = bu != null && bu.getBoolean(MainActivity.KEY_SHORTCUT);
//		boolean fin = false;
//		try {
//			if (id != null && id.length() > 0)
//				for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
//					JSONObject w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
//					if (w.getString(MainActivity.KEY_ID).equals(id)) {
//						wApp = w;
//						final int p = i;
//						if (shortcut && !new MainActivity.TWInfo(this, new File(wApp.getString(MainActivity.DB_KEY_PATH))).isWiki) {
//							fin = true;
//							new AlertDialog.Builder(this)
//									.setTitle(android.R.string.dialog_alert_title)
//									.setMessage(R.string.confirm_to_auto_remove_wiki)
//									.setNegativeButton(android.R.string.no, null)
//									.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog, int which) {
//											try {
//												db.getJSONArray(MainActivity.DB_KEY_WIKI).remove(p);
//												MainActivity.writeJson(TWEditorWV.this, db);
//											} catch (Exception e) {
//												e.printStackTrace();
//											}
//										}
//									}).setOnDismissListener(new DialogInterface.OnDismissListener() {
//								@Override
//								public void onDismiss(DialogInterface dialogInterface) {
//									finish();
//								}
//							}).show();
//						}
//						wvTitle = wApp.getString(MainActivity.KEY_NAME);
//						try {
//							wvSubTitle = wApp.getString(MainActivity.DB_KEY_SUBTITLE);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						break;
//					} else if (i == db.getJSONArray(MainActivity.DB_KEY_WIKI).length() - 1)
//						throw new Exception();
//				}
//		} catch (Exception e) {
//			e.printStackTrace();
//			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
//			finish();
//		}
//
//		if (wApp == null) {
//			String url = bu != null ? bu.getString(MainActivity.KEY_URL) : null;
//			if (url != null) ueu = url;
//		} else {
//			try {
//				ueu = Uri.fromFile(new File(wApp.getString(MainActivity.DB_KEY_URI))).getPath();
//				if (wvTitle != null && wvTitle.length() > 0) this.setTitle(wvTitle);
//				if (wvSubTitle != null && wvSubTitle.length() > 0) toolbar.setSubtitle(wvSubTitle);
//				InputStream is = null;
//				try {
//					is = new FileInputStream(new File(getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), id));
//					Bitmap icon = BitmapFactory.decodeStream(is);
//					if (icon != null) toolbar.setLogo(cIcon(icon));
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					if (is != null) try {
//						is.close();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		//TODO


		// JS请求处理
		final class JavaScriptCallback {

//			@JavascriptInterface
//			public void getVersion(boolean wiki, boolean classic) {
//				if (isWiki = wiki) {
//					isClassic = classic;
//					if (wApp == null && !classic)
//						runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								Toast.makeText(TWEditorWV.this, R.string.ready_to_fork, Toast.LENGTH_SHORT).show();
//								toolbar.setLogo(R.drawable.ic_fork);
//							}
//						});
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							getInfo(wv);
//						}
//					});
//					wv.getSettings().setBuiltInZoomControls(classic);
//					wv.getSettings().setDisplayZoomControls(classic);
//				}
//			}

			@JavascriptInterface
			public void getB64(String data, String dest) {
				MainActivity.wGet(TWEditorWV.this, Uri.parse(MainActivity.SCHEME_BLOB_B64 + ':' + data), new File(dest));
			}

//			@JavascriptInterface
//			public void isDirtyOnQuit(final boolean dirty) {
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						confirmAndExit(dirty, null);
//					}
//				});
//			}

			@JavascriptInterface
			public void saveFile(String pathname, String data) {
				try {
					if (wApp == null || !isClassic || pathname.equals(Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)).getPath()))
//					if (wApp == null || !isClassic || pathname.equals(wApp.getString(MainActivity.DB_KEY_PATH)))
						saveWiki(data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@JavascriptInterface
			public void saveDownload(String data) {
				saveDownload(data, null);
			}

			@JavascriptInterface
			public void saveDownload(final String data, String filename) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
					showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileDialog.fileSave(TWEditorWV.this, lastDir, filename != null && filename.length() > 0 ? filename : null, new FileDialogFilter[]{FileDialog.ALL}, showHidden, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						File file = files[0];
						try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)); OutputStream os = new FileOutputStream(file)) {
							if (file != null) {

								int len = is.available();
								int length, lengthTotal = 0;
								byte[] b = new byte[4096];
								while ((length = is.read(b)) != -1) {
									os.write(b, 0, length);
									lengthTotal += length;
								}
								os.flush();
								if (lengthTotal != len) throw new Exception();
							} else throw new Exception();
						} catch (Exception e) {
							e.printStackTrace();
							if (file != null) {
								file.delete();
							}
							Toast.makeText(TWEditorWV.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//						} finally {
//							try {
//								is.close();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//							if (os != null)
//								try {
//									os.close();
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
						}
					}

					@Override
					public void onCanceled() {
						Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
					}
				});
			}

			@JavascriptInterface
			public void saveWiki(final String data) {
				if (wApp != null) {
					File file = new File(Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)).getPath());
					if (!file.exists()) {
						Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
						return;
					}
					if (wApp.optBoolean(MainActivity.DB_KEY_BACKUP)) try {
						MainActivity.backup(file);
//							FileInputStream isb = null;
//							FileOutputStream osb = null;
//							try {
//								String mfn = file.getName();
//								File mfd = new File(file.getParentFile(), mfn + MainActivity.BACKUP_DIRECTORY_PATH_PREFIX);
//								if (!mfd.exists()) mfd.mkdir();
//								else if (!mfd.isDirectory()) throw new Exception();
////								DateTime dateTime = new DateTime(file.lastModified(), DateTimeZone.UTC);
////								String prefix = '.'
////										+ String.format(Locale.US, F04D, dateTime.year().get())
////										+ String.format(Locale.US, F02D, dateTime.monthOfYear().get())
////										+ String.format(Locale.US, F02D, dateTime.dayOfMonth().get())
////										+ String.format(Locale.US, F02D, dateTime.hourOfDay().get())
////										+ String.format(Locale.US, F02D, dateTime.minuteOfHour().get())
////										+ String.format(Locale.US, F02D, dateTime.secondOfMinute().get())
////										+ String.format(Locale.US, F03D, dateTime.millisOfSecond().get());
//								isb = new FileInputStream(file);
//								osb = new FileOutputStream(new File(mfd.getAbsolutePath(), new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), MainActivity.formatBackup(file.lastModified())).toString()));
////								osb = new FileOutputStream(new File(mfd.getAbsolutePath(), new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), prefix).toString()));
//								int len = isb.available();
//								int length, lengthTotal = 0;
//								byte[] b = new byte[4096];
//								while ((length = isb.read(b)) != -1) {
//									osb.write(b, 0, length);
//									lengthTotal += length;
//								}
//								osb.flush();
//								if (lengthTotal != len) throw new Exception();
//							} catch (Exception e) {
//								e.printStackTrace();
//								Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
//							} finally {
//								if (isb != null)
//									try {
//										isb.close();
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//								if (osb != null)
//									try {
//										osb.close();
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//							}
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
					}
					try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
						 FileOutputStream os = new FileOutputStream(file)) {
						int len = is.available();
						int length, lengthTotal = 0;
						byte[] b = new byte[4096];
						while ((length = is.read(b)) != -1) {
							os.write(b, 0, length);
							lengthTotal += length;
						}
						os.flush();
						if (lengthTotal != len) throw new IOException();
//						final MainActivity.TWInfo info = new MainActivity.TWInfo(TWEditorWV.this, file);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								getInfo(wv);
//								if (info.favicon != null) {
//									toolbar.setLogo(cIcon(info.favicon));
//								} else {
//									toolbar.setLogo(null);
//								}
//								MainActivity.updateIcon(TWEditorWV.this, info.favicon, id);
//								TWEditorWV.this.setTitle(info.title);
//								toolbar.setSubtitle(info.subtitle);
							}
						});
//						wApp.put(MainActivity.KEY_NAME, (info.title != null && info.title.length() > 0) ? info.title : getString(R.string.tiddlywiki));
//						wApp.put(MainActivity.DB_KEY_SUBTITLE, (info.subtitle != null && info.subtitle.length() > 0) ? info.subtitle : MainActivity.STR_EMPTY);
//						MainActivity.writeJson(TWEditorWV.this, db);
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
//					} finally {
//						try {
//							is.close();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						if (os != null)
//							try {
//								os.close();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
					}
				} else {
					File lastDir = Environment.getExternalStorageDirectory();
					boolean showHidden = false;
					try {
						lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
						showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
					} catch (Exception e) {
						e.printStackTrace();
					}
					FileDialog.fileSave(TWEditorWV.this, lastDir, MainActivity.HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
						@Override
						public void onFileTouched(File[] files) {
//							FileOutputStream os = null;
							File file;
							boolean created = false;
							try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
								 FileOutputStream os = new FileOutputStream(file = files[0])) {
								if (files[0] == null)
									throw new FileNotFoundException();
//									File file = files[0];
//									os = new FileOutputStream(file);
								int len = is.available();
								System.out.println(len);
								int length, lengthTotal = 0;
								byte[] b = new byte[4096];
								while ((length = is.read(b)) != -1) {
									os.write(b, 0, length);
									lengthTotal += length;
								}
								os.flush();
								System.out.println(lengthTotal);
								if (lengthTotal != len || !MainActivity.isWiki(file))
									throw new IOException();
								created = true;
//								final MainActivity.TWInfo info = new MainActivity.TWInfo(TWEditorWV.this, file);
//									if (info.isWiki)
//									try {
								boolean exist = false;
								Uri u = Uri.fromFile(file);
								JSONObject wl = db.getJSONObject(MainActivity.DB_KEY_WIKI), wa = null;
								Iterator<String> iterator = wl.keys();
								while (iterator.hasNext())
									if ((wa = wl.getJSONObject(iterator.next())).optString(MainActivity.DB_KEY_URI).equals(u.toString())) {
										exist = true;
										break;
									}
//										boolean exist = false;
//										JSONObject wa = null;
//										id = MainActivity.genId();
//										for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
//											wa = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
//											if (wa.getString(MainActivity.DB_KEY_PATH).equals(file.getAbsolutePath())) {
//												exist = true;
//												id = wa.getString(MainActivity.KEY_ID);
//												break;
//											}
//										}
								if (exist)
									Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
								else {
									wa = new JSONObject();
									String id = MainActivity.genId();
//											wa.put(MainActivity.KEY_ID, id);
									wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
									wa.put(MainActivity.DB_KEY_URI, u.toString());
									wl.put(id, wa);
//											db.getJSONArray(MainActivity.DB_KEY_WIKI).put(db.getJSONArray(MainActivity.DB_KEY_WIKI).length(), wa);
								}
//										wa.put(MainActivity.KEY_NAME, info.title);
//										wa.put(MainActivity.DB_KEY_SUBTITLE, (info.subtitle != null && info.subtitle.length() > 0) ? info.subtitle : MainActivity.STR_EMPTY);
//										wa.put(MainActivity.DB_KEY_PATH, file.getAbsolutePath());
								wa.put(MainActivity.DB_KEY_BACKUP, false);
//										MainActivity.updateIcon(TWEditorWV.this, info.favicon, id);
//										if (!MainActivity.writeJson(TWEditorWV.this, db))
//											throw new Exception();
								wApp = wa;
//								TWEditorWV.this.id = id;
								db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
								if (!MainActivity.writeJson(TWEditorWV.this, db))
									throw new IOException();
//									} catch (Exception e) {
//										e.printStackTrace();
//								Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
//									}
//									else
//										Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
								if (wApp != null) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
//												if (wApp != null) {
//													if (info.favicon != null)
//														toolbar.setLogo(cIcon(info.favicon));
//													else toolbar.setLogo(null);
//												}
//												TWEditorWV.this.setTitle(info.title);
//												toolbar.setSubtitle(info.subtitle);
											getInfo(wv);
											wv.clearHistory();
										}
									});
								}
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(TWEditorWV.this, created ? R.string.data_error : R.string.error_processing_file, Toast.LENGTH_SHORT).show();
//							} finally {
//								try {
//									is.close();
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//								if (os != null)
//									try {
//										os.close();
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
							}
						}

						@Override
						public void onCanceled() {
							Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}

			@JavascriptInterface
			public void exportDB() {
				MainActivity.exportJson(TWEditorWV.this, db);
			}

		}
		wv.addJavascriptInterface(new JavaScriptCallback(), JSI);
		wv.setWebViewClient(new WebViewClient() {
			// KitKat fallback
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url == null) return false;
				return overrideUrlLoading(view, Uri.parse(url));
//				Uri u = Uri.parse(url);
//				String sch = u.getScheme();
//				boolean browse = sch != null && (sch.equals(SCH_ABOUT) || sch.equals(SCH_HTTP) || sch.equals(SCH_HTTPS));
//				if (sch == null || sch.length() == 0 || wApp == null && browse)
//					return false;
//				try {
//					final Intent intent;
//					switch (sch) {
//						case SCH_TEL:
//							intent = new Intent(Intent.ACTION_DIAL, u);
//							view.getContext().startActivity(intent);
//							break;
//						case SCH_MAILTO:
//							intent = new Intent(Intent.ACTION_SENDTO, u);
//							view.getContext().startActivity(intent);
//							break;
//						case SCH_ABOUT:
//						case SCH_HTTP:
//						case SCH_HTTPS:
//							intent = new Intent(Intent.ACTION_VIEW, u);
//							view.getContext().startActivity(intent);
//							break;
//						default:
//							intent = new Intent(Intent.ACTION_VIEW, u);
//							new AlertDialog.Builder(TWEditorWV.this)
//									.setTitle(android.R.string.dialog_alert_title)
//									.setMessage(R.string.third_part_rising)
//									.setNegativeButton(android.R.string.no, null)
//									.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog, int which) {
//											try {
//												view.getContext().startActivity(intent);
//											} catch (Exception e) {
//												e.printStackTrace();
//											}
//										}
//									}).show();
//							break;
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return true;
			}

			// 跳转处理
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return TWEditorWV.this.overrideUrlLoading(view, request.getUrl());
			}

			// 浏览器图标
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (wApp == null) toolbar.setLogo(R.drawable.ic_language);
			}

			// 加载完成回调
			public void onPageFinished(final WebView view, String url) {
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				view.evaluateJavascript(getString(R.string.js_is_wiki), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						isWiki = Boolean.parseBoolean(value);
						if (isWiki)
							view.evaluateJavascript(getString(R.string.js_is_classic), new ValueCallback<String>() {
								@Override
								public void onReceiveValue(String value) {
									isClassic = Boolean.parseBoolean(value);
									if (wApp == null && !isClassic) {
										Toast.makeText(TWEditorWV.this, R.string.ready_to_fork, Toast.LENGTH_SHORT).show();
										toolbar.setLogo(R.drawable.ic_fork);
									}
									getInfo(view);
									view.getSettings().setBuiltInZoomControls(isClassic);
									view.getSettings().setDisplayZoomControls(isClassic);
								}
							});
					}
				});
//				else
//					view.loadUrl(SCH_JS + ':' + getString(R.string.js_version));
				if (wApp != null) view.clearHistory();
			}
		});
		// 下载服务
		wv.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(db.getString(MainActivity.DB_KEY_LAST_DIR));
					showHidden = db.getBoolean(MainActivity.DB_KEY_SHOW_HIDDEN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String filenameProbable = URLUtil.guessFileName(url, contentDisposition, mimeType);
				FileDialog.fileDialog(TWEditorWV.this, lastDir, filenameProbable, 3, 0, new String[]{mimeType, FileDialog.MIME_ALL}, 0, showHidden, false, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						String scheme = Uri.parse(url) != null ? Uri.parse(url).getScheme() : null;
						if (scheme != null && scheme.equals(SCH_BLOB)) {
							wv.loadUrl(SCH_JS + ':' + getString(R.string.js_blob).replace(PREF_BLOB, url).replace(PREF_DEST, files[0].getAbsolutePath()));
						} else
							MainActivity.wGet(TWEditorWV.this, Uri.parse(url), files[0]);
					}

					@Override
					public void onCanceled() {
						Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		Intent intent;
		Bundle bu;
		if ((bu = (intent = getIntent()).getExtras()) != null && bu.getString(MainActivity.KEY_ID) == null) {
			wvs.setJavaScriptEnabled(true);
			String vu;
			wv.loadUrl((vu = bu.getString(MainActivity.KEY_URL)) != null ? vu : URL_BLANK);
			return;
		}
		nextWiki(intent);
//		wv.loadUrl(!fin ? ueu : null);
	}

	// 热启动
	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		Bundle bu;
		String fid;
		JSONObject wl;
//		if ((bu = intent.getExtras()) == null || (fid = bu.getString(MainActivity.KEY_ID)) == null || (wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null || (wa = wl.optJSONObject(fid)) == null) {
//			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
//			return;
//		}
//		if (wa == wApp) return;
//		if (isWiki)wv.evaluateJavascript(getString(R.string.js_exit), new ValueCallback<String>() {
//			@Override
//			public void onReceiveValue(String value) {
//				confirmAndExit(Boolean.parseBoolean(value), intent);
//			}
//		});else nextWiki(intent);
		try {
			if ((bu = intent.getExtras()) == null || (fid = bu.getString(MainActivity.KEY_ID)) == null || fid.equals(wApp.getString(MainActivity.KEY_ID)))
				return;
			wl = db.getJSONObject(MainActivity.DB_KEY_WIKI);
//			int ser = -1;
//			JSONObject w = null;
//			try {
//				for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
//					w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
//					if (w.getString(MainActivity.KEY_ID).equals(fid)) {
//						ser = i;
//						break;
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			if (!wl.has(fid)) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				return;
			}
			//		if (ser == -1) Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
//			if (new MainActivity.TWInfo(this, new File(wa.getString(MainActivity.DB_KEY_PATH))).isWiki) {
//					nextWikiIntent = intent;
//					nextWikiSerial = ser;
			if (isWiki) {
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						confirmAndExit(Boolean.parseBoolean(value), intent);
					}
				});
//				else
//					wv.loadUrl(SCH_JS + ':' + getString(isClassic ? R.string.js_quit_c : R.string.js_quit));
			} else nextWiki(intent);
//			} else {
////					final int p = ser;
//				new AlertDialog.Builder(this)
//						.setTitle(android.R.string.dialog_alert_title)
//						.setMessage(R.string.confirm_to_auto_remove_wiki)
//						.setNegativeButton(android.R.string.no, null)
//						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								try {
//									wl.remove(fid);
////										db.getJSONArray(MainActivity.DB_KEY_WIKI).remove(p);
//									MainActivity.writeJson(TWEditorWV.this, db);
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//						}).show();
//			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	// 处理跳转App
	private boolean overrideUrlLoading(final WebView view, Uri u) {
		String sch = u.getScheme();
		boolean browse = sch != null && (sch.equals(SCH_ABOUT) || sch.equals(SCH_HTTP) || sch.equals(SCH_HTTPS));
		if (sch == null || sch.length() == 0 || wApp == null && browse)
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
				case SCH_HTTP:
				case SCH_HTTPS:
					intent = new Intent(Intent.ACTION_VIEW, u);
					view.getContext().startActivity(intent);
					break;
				default:
					intent = new Intent(Intent.ACTION_VIEW, u);
					new AlertDialog.Builder(TWEditorWV.this)
							.setTitle(android.R.string.dialog_alert_title)
							.setMessage(R.string.third_part_rising)
							.setNegativeButton(android.R.string.no, null)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									try {
										view.getContext().startActivity(intent);
									} catch (Exception e) {
										e.printStackTrace();
									}
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
		view.evaluateJavascript(getString(isClassic ? R.string.js_info_c : R.string.js_info), new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				try {
					JSONArray array = new JSONArray(value);
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
					if (wApp != null) {
						// 解取favicon
						String fib64 = array.getString(4);
						byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
						Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
						toolbar.setLogo(favicon != null ? cIcon(favicon) : null);
						// 写Json
						wApp.put(MainActivity.KEY_NAME, title).put(MainActivity.DB_KEY_SUBTITLE, subtitle).put(MainActivity.DB_KEY_COLOR, themeColor).put(MainActivity.KEY_FAVICON, fib64.length() > 0 ? fib64 : null);
						MainActivity.writeJson(TWEditorWV.this, db);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	// 保存提醒
	private void confirmAndExit(boolean dirty, final Intent nextWikiIntent) {
		if (dirty) {
			AlertDialog isExit = new AlertDialog.Builder(TWEditorWV.this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.confirm_to_exit_wiki)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if (nextWikiIntent == null)
										TWEditorWV.super.onBackPressed();
									else
										nextWiki(nextWikiIntent);
									dialog.dismiss();
								}
							}
					)
					.setNegativeButton(android.R.string.no, null)
					.show();
			isExit.setCanceledOnTouchOutside(false);
		} else {
			if (nextWikiIntent == null)
				TWEditorWV.super.onBackPressed();
			else
				nextWiki(nextWikiIntent);
		}
	}

//	// 保存提醒
//	private void confirmAndExit(boolean dirty) {
//		if (dirty) {
//			AlertDialog isExit = new AlertDialog.Builder(TWEditorWV.this)
//					.setTitle(android.R.string.dialog_alert_title)
//					.setMessage(R.string.confirm_to_exit_wiki)
//					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog, int which) {
//									if (nextWikiIntent == null)
//										TWEditorWV.super.onBackPressed();
//									else
//										nextWiki();
//									dialog.dismiss();
//								}
//							}
//					)
//					.setNegativeButton(android.R.string.no, null)
//					.setOnCancelListener(new DialogInterface.OnCancelListener() {
//						@Override
//						public void onCancel(DialogInterface dialog) {
//							nextWikiIntent = null;
//							nextWikiSerial = -1;
//						}
//					})
//					.show();
//			isExit.setCanceledOnTouchOutside(false);
//		} else {
//			if (nextWikiIntent == null)
//				TWEditorWV.super.onBackPressed();
//			else
//				nextWiki();
//		}
//	}

	// 加载内容
	private void nextWiki(Intent nextWikiIntent) {
		// 读取数据
		final JSONObject wl, wa;
		Bundle bu;
		final String nextWikiId;
		Uri u;
		if ((bu = nextWikiIntent.getExtras()) == null
				|| (nextWikiId = bu.getString(MainActivity.KEY_ID)) == null
				|| nextWikiId.length() == 0
				|| (wl = db.optJSONObject(MainActivity.DB_KEY_WIKI)) == null
				|| (wa = wl.optJSONObject(nextWikiId)) == null) {
			Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null || bu.getBoolean(MainActivity.KEY_SHORTCUT) && !MainActivity.isWiki(u)) {
			new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.confirm_to_auto_remove_wiki)
					.setNegativeButton(android.R.string.no, null)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								wl.remove(nextWikiId);
								MainActivity.writeJson(TWEditorWV.this, db);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					TWEditorWV.this.finish();
				}
			}).show();
			return;
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
		wApp = wa;
		uri = u;
		if (nextWikiIntent != getIntent()) setIntent(nextWikiIntent);
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
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(uri != null ? uri.toString() : MainActivity.STR_EMPTY);
	}

//	private void nextWiki() {
//		String ueu = null;
//		toolbar.setLogo(null);
//		wv.getSettings().setBuiltInZoomControls(false);
//		wv.getSettings().setDisplayZoomControls(false);
//		wApp = null;
//		wv.getSettings().setJavaScriptEnabled(false);
//		wv.loadUrl(URL_BLANK);
//		setIntent(nextWikiIntent);
//		String wvTitle = null, wvSubTitle = null;
//		try {
//			wApp = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(nextWikiSerial);
//			if (wApp == null) throw new Exception();
//			id = wApp.getString(MainActivity.KEY_ID);
//			ueu = MainActivity.SCH_EX_FILE + wApp.getString(MainActivity.DB_KEY_PATH);
//			wvTitle = wApp.getString(MainActivity.KEY_NAME);
//			try {
//				wvSubTitle = wApp.getString(MainActivity.DB_KEY_SUBTITLE);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
//			finish();
//		}
//		try {
//			if (wvTitle != null && wvTitle.length() > 0) this.setTitle(wvTitle);
//			if (wvSubTitle != null && wvSubTitle.length() > 0) toolbar.setSubtitle(wvSubTitle);
//			InputStream is = null;
//			try {
//				is = new FileInputStream(new File(getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), id));
//				Bitmap icon = BitmapFactory.decodeStream(is);
//				if (icon != null) toolbar.setLogo(cIcon(icon));
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				if (is != null) try {
//					is.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		wv.getSettings().setJavaScriptEnabled(true);
//		wv.loadUrl(ueu);
//		nextWikiIntent = null;
//		nextWikiSerial = -1;
//	}

	//生成icon
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
		else if (wv.canGoBack())
			wv.goBack();
		else if (isWiki) {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), new ValueCallback<String>() {
				@Override
				public void onReceiveValue(String value) {
					confirmAndExit(Boolean.parseBoolean(value), null);
				}
			});
//			else
//				wv.loadUrl(SCH_JS + ':' + getString(isClassic ? R.string.js_quit_c : R.string.js_quit));
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
			findViewById(R.id.wv_ll).setBackgroundColor(primColor);
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