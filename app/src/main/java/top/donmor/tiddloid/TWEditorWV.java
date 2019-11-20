/*
 * top.donmor.tiddloid.TWEditorWV <= [P|Tiddloid]
 * Last modified: 18:33:05 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.github.donmor.filedialog.lib.FileDialog;
import com.github.donmor.filedialog.lib.FileDialogFilter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

public class TWEditorWV extends AppCompatActivity {

	private JSONObject db;
	private JSONObject wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private int mOriginalOrientation, nextWikiSerial = -1;
	private float scale;
	private Intent nextWikiIntent;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private Toolbar toolbar;
	private ProgressBar wvProgress;
	private boolean isWiki, isClassic;
	private String id;

	// CONSTANT
	private static final String
			CHARSET_NAME_UTF_8 = "UTF-8",
			F02D = "%02d",
			F03D = "%03d",
			F04D = "%04d",
			JSI = "twi",
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
		try {
			db = MainActivity.readJson(this);
			if (db == null) throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			finish();
		}
		toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		this.setTitle(R.string.app_name);
		onConfigurationChanged(getResources().getConfiguration());
		wv = findViewById(R.id.twWebView);
		wvProgress = findViewById(R.id.progressBar);
		wvProgress.setMax(100);
		WebSettings wvs = wv.getSettings();
		wvs.setJavaScriptEnabled(true);
		wvs.setDatabaseEnabled(true);
		wvs.setDatabasePath(getCacheDir().getPath());
		wvs.setDomStorageEnabled(true);
		wvs.setBuiltInZoomControls(false);
		wvs.setDisplayZoomControls(false);
		wvs.setUseWideViewPort(true);
		wvs.setLoadWithOverviewMode(true);
		wvs.setAllowFileAccess(true);
		wvs.setAllowContentAccess(true);
		wvs.setAllowFileAccessFromFileURLs(true);
		wvs.setAllowUniversalAccessFromFileURLs(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			wvs.setAllowFileAccessFromFileURLs(true);
			wvs.setAllowUniversalAccessFromFileURLs(true);
		}
		scale = getResources().getDisplayMetrics().density;
		wcc = new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) {
					wvProgress.setVisibility(View.GONE);
				} else {
					wvProgress.setVisibility(View.VISIBLE);
					wvProgress.setProgress(newProgress);
				}
				super.onProgressChanged(view, newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				if (wApp == null) {
					setTitle(title);
					toolbar.setSubtitle(null);
				}
			}

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
							if (files != null && files.length > 0) {
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
							} else throw new Exception();

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

			@Override
			public void onHideCustomView() {
				FrameLayout decor = (FrameLayout) getWindow().getDecorView();
				decor.removeView(mCustomView);
				mCustomView = null;
				setRequestedOrientation(mOriginalOrientation);
				if (mCustomViewCallback != null) mCustomViewCallback.onCustomViewHidden();
				mCustomViewCallback = null;
			}
		};
		wv.setWebChromeClient(wcc);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		Bundle bu = getIntent().getExtras();
		String ueu = URL_BLANK;
		CharSequence wvTitle = null, wvSubTitle = null;
		id = bu != null ? bu.getString(MainActivity.KEY_ID) : null;
		boolean shortcut = bu != null && bu.getBoolean(MainActivity.KEY_SHORTCUT);
		boolean fin = false;
		try {
			if (id != null && id.length() > 0)
				for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
					JSONObject w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
					if (w.getString(MainActivity.KEY_ID).equals(id)) {
						wApp = w;
						final int p = i;
						if (shortcut && !new MainActivity.TWInfo(this, new File(wApp.getString(MainActivity.DB_KEY_PATH))).isWiki) {
							fin = true;
							new AlertDialog.Builder(this)
									.setTitle(android.R.string.dialog_alert_title)
									.setMessage(R.string.confirm_to_auto_remove_wiki)
									.setNegativeButton(android.R.string.no, null)
									.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											try {
												if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
													db.put(MainActivity.DB_KEY_WIKI, MainActivity.removeUnderK(db.getJSONArray(MainActivity.DB_KEY_WIKI), p));
												else
													db.getJSONArray(MainActivity.DB_KEY_WIKI).remove(p);
												MainActivity.writeJson(TWEditorWV.this, db);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}).setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialogInterface) {
									finish();
								}
							}).show();
						}
						wvTitle = wApp.getString(MainActivity.KEY_NAME);
						try {
							wvSubTitle = wApp.getString(MainActivity.DB_KEY_SUBTITLE);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					} else if (i == db.getJSONArray(MainActivity.DB_KEY_WIKI).length() - 1)
						throw new Exception();
				}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			finish();
		}

		if (wApp != null) try {
			ueu = MainActivity.SCH_EX_FILE + wApp.getString(MainActivity.DB_KEY_PATH);
			if (wvTitle != null && wvTitle.length() > 0) this.setTitle(wvTitle);
			if (wvSubTitle != null && wvSubTitle.length() > 0) toolbar.setSubtitle(wvSubTitle);
			InputStream is = null;
			try {
				is = new FileInputStream(new File(getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), id));
				Bitmap icon = BitmapFactory.decodeStream(is);
				if (icon != null) toolbar.setLogo(cIcon(icon));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		else {
			String url = bu != null ? bu.getString(MainActivity.KEY_URL) : null;
			if (url != null) ueu = url;
		}

		final class JavaScriptCallback {

			@JavascriptInterface
			public void getVersion(boolean wiki, boolean classic) {
				if (wiki) {
					isWiki = true;
					if (wApp == null && !classic)
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(TWEditorWV.this, R.string.ready_to_fork, Toast.LENGTH_SHORT).show();
								toolbar.setLogo(R.drawable.ic_fork);
							}
						});

					wv.getSettings().setBuiltInZoomControls(classic);
					wv.getSettings().setDisplayZoomControls(classic);
					isClassic = classic;
				} else isWiki = false;
			}

			@JavascriptInterface
			public void getB64(String data, String dest) {
				MainActivity.wGet(TWEditorWV.this, Uri.parse(MainActivity.SCHEME_BLOB_B64 + ':' + data), new File(dest));
			}

			@JavascriptInterface
			public void isDirtyOnQuit(final boolean dirty) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						confirmAndExit(dirty);
					}
				});
			}

			@JavascriptInterface
			public void saveFile(String pathname, String data) {
				try {
					if (wApp == null || !isClassic || pathname.equals(wApp.getString(MainActivity.DB_KEY_PATH)))
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
			public void saveDownload(String data, String filename) {
				final InputStream is = new ByteArrayInputStream(data.getBytes(Charset.forName(CHARSET_NAME_UTF_8)));
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
						OutputStream os = null;
						File file = null;
						try {
							if (files != null && files.length > 0 && files[0] != null) {
								file = files[0];
								os = new FileOutputStream(file);
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
						} finally {
							try {
								is.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (os != null)
								try {
									os.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
					}

					@Override
					public void onCanceled() {
						Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
					}
				});
			}

			@JavascriptInterface
			public void saveWiki(String data) {
				final ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(Charset.forName(CHARSET_NAME_UTF_8)));
				if (wApp != null) {
					FileOutputStream os = null;
					try {
						String fp = wApp.getString(MainActivity.DB_KEY_PATH);
						File file = new File(fp);
						if (wApp.getBoolean(MainActivity.DB_KEY_BACKUP)) {
							FileInputStream isb = null;
							FileOutputStream osb = null;
							try {
								String mfn = file.getName();
								File mfd = new File(file.getParentFile().getAbsolutePath() + '/' + mfn + MainActivity.BACKUP_DIRECTORY_PATH_PREFIX);
								if (!mfd.exists()) mfd.mkdir();
								else if (!mfd.isDirectory()) throw new Exception();
								DateTime dateTime = new DateTime(file.lastModified(), DateTimeZone.UTC);
								String prefix = '.'
										+ String.format(Locale.US, F04D, dateTime.year().get())
										+ String.format(Locale.US, F02D, dateTime.monthOfYear().get())
										+ String.format(Locale.US, F02D, dateTime.dayOfMonth().get())
										+ String.format(Locale.US, F02D, dateTime.hourOfDay().get())
										+ String.format(Locale.US, F02D, dateTime.minuteOfHour().get())
										+ String.format(Locale.US, F02D, dateTime.secondOfMinute().get())
										+ String.format(Locale.US, F03D, dateTime.millisOfSecond().get());
								isb = new FileInputStream(file);
								osb = new FileOutputStream(new File(mfd.getAbsolutePath() + '/' + new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), prefix).toString()));
								int len = isb.available();
								int length, lengthTotal = 0;
								byte[] b = new byte[4096];
								while ((length = isb.read(b)) != -1) {
									osb.write(b, 0, length);
									lengthTotal += length;
								}
								osb.flush();
								if (lengthTotal != len) throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
							} finally {
								if (isb != null)
									try {
										isb.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
								if (osb != null)
									try {
										osb.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
							}
						}
						os = new FileOutputStream(file);
						int len = is.available();
						int length, lengthTotal = 0;
						byte[] b = new byte[4096];
						while ((length = is.read(b)) != -1) {
							os.write(b, 0, length);
							lengthTotal += length;
						}
						os.flush();
						os.close();
						os = null;
						if (lengthTotal != len) throw new Exception();
						final MainActivity.TWInfo info = new MainActivity.TWInfo(TWEditorWV.this, file);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (info.favicon != null) {
									toolbar.setLogo(cIcon(info.favicon));
								} else {
									toolbar.setLogo(null);
								}
								MainActivity.updateIcon(TWEditorWV.this, info.favicon, id);
								TWEditorWV.this.setTitle(info.title);
								toolbar.setSubtitle(info.subtitle);
							}
						});
						wApp.put(MainActivity.KEY_NAME, (info.title != null && info.title.length() > 0) ? info.title : getString(R.string.tiddlywiki));
						wApp.put(MainActivity.DB_KEY_SUBTITLE, (info.subtitle != null && info.subtitle.length() > 0) ? info.subtitle : MainActivity.STR_EMPTY);
						MainActivity.writeJson(TWEditorWV.this, db);
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
					} finally {
						try {
							is.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (os != null)
							try {
								os.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
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
							FileOutputStream os = null;
							try {
								if (files != null && files.length > 0 && files[0] != null) {
									File file = files[0];
									os = new FileOutputStream(file);
									int len = is.available();
									int length, lengthTotal = 0;
									byte[] b = new byte[4096];
									while ((length = is.read(b)) != -1) {
										os.write(b, 0, length);
										lengthTotal += length;
									}
									os.flush();
									os.close();
									os = null;
									if (lengthTotal != len) throw new Exception();
									final MainActivity.TWInfo info = new MainActivity.TWInfo(TWEditorWV.this, file);
									if (info.isWiki) try {
										boolean exist = false;
										JSONObject w = null;
										id = MainActivity.genId();
										for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
											w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
											if (w.getString(MainActivity.DB_KEY_PATH).equals(file.getAbsolutePath())) {
												exist = true;
												id = w.getString(MainActivity.KEY_ID);
												break;
											}
										}
										if (exist)
											Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
										else {
											w = new JSONObject();
											w.put(MainActivity.KEY_ID, id);
											db.getJSONArray(MainActivity.DB_KEY_WIKI).put(db.getJSONArray(MainActivity.DB_KEY_WIKI).length(), w);
										}
										w.put(MainActivity.KEY_NAME, info.title);
										w.put(MainActivity.DB_KEY_SUBTITLE, (info.subtitle != null && info.subtitle.length() > 0) ? info.subtitle : MainActivity.STR_EMPTY);
										w.put(MainActivity.DB_KEY_PATH, file.getAbsolutePath());
										w.put(MainActivity.DB_KEY_BACKUP, false);
										MainActivity.updateIcon(TWEditorWV.this, info.favicon, id);
										if (!MainActivity.writeJson(TWEditorWV.this, db))
											throw new Exception();
										wApp = w;
										db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
										if (!MainActivity.writeJson(TWEditorWV.this, db))
											throw new Exception();
									} catch (Exception e) {
										e.printStackTrace();
										Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
									}
									else
										Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
									if (wApp != null) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (wApp != null) {
													if (info.favicon != null)
														toolbar.setLogo(cIcon(info.favicon));
													else toolbar.setLogo(null);
												}
												TWEditorWV.this.setTitle(info.title);
												toolbar.setSubtitle(info.subtitle);
												wv.clearHistory();
											}
										});
									}
								} else throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								try {
									is.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
								if (os != null)
									try {
										os.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
							}
						}

						@Override
						public void onCanceled() {
							Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}

		wv.addJavascriptInterface(new JavaScriptCallback(), JSI);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, String url) {
				Uri u = Uri.parse(url);
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

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (wApp == null) toolbar.setLogo(R.drawable.ic_language);
			}

			public void onPageFinished(final WebView view, String url) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
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
										view.getSettings().setBuiltInZoomControls(isClassic);
										view.getSettings().setDisplayZoomControls(isClassic);
									}
								});
						}
					});
				else
					view.loadUrl(SCH_JS + ':' + getString(R.string.js_version));
				if (wApp != null) view.clearHistory();
			}
		});
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
						if (files != null && files.length > 0) {
							String scheme = Uri.parse(url) != null ? Uri.parse(url).getScheme() : null;
							if (scheme != null && scheme.equals(SCH_BLOB)) {
								wv.loadUrl(SCH_JS + ':' + getString(R.string.js_blob).replace(PREF_BLOB, url).replace(PREF_DEST, files[0].getAbsolutePath()));
							} else
								MainActivity.wGet(TWEditorWV.this, Uri.parse(url), files[0]);
						}
					}

					@Override
					public void onCanceled() {
						Toast.makeText(TWEditorWV.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		wv.loadUrl(!fin ? ueu : null);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Bundle bu = intent.getExtras();
		String fid = bu != null ? bu.getString(MainActivity.KEY_ID) : null;
		if (fid != null) {
			int ser = -1;
			JSONObject w = null;
			try {
				for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
					w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
					if (w.getString(MainActivity.KEY_ID).equals(fid)) {
						ser = i;
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (ser == -1) Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			else if (!fid.equals(id)) {
				try {
					if (new MainActivity.TWInfo(this, new File(w.getString(MainActivity.DB_KEY_PATH))).isWiki) {
						nextWikiIntent = intent;
						nextWikiSerial = ser;
						if (isWiki) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
								wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), new ValueCallback<String>() {
									@Override
									public void onReceiveValue(String value) {
										confirmAndExit(Boolean.parseBoolean(value));
									}
								});
							else
								wv.loadUrl(SCH_JS + ':' + getString(isClassic ? R.string.js_quit_c : R.string.js_quit));
						} else nextWiki();
					} else {
						final int p = ser;
						new AlertDialog.Builder(this)
								.setTitle(android.R.string.dialog_alert_title)
								.setMessage(R.string.confirm_to_auto_remove_wiki)
								.setNegativeButton(android.R.string.no, null)
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										try {
											if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
												db.put(MainActivity.DB_KEY_WIKI, MainActivity.removeUnderK(db.getJSONArray(MainActivity.DB_KEY_WIKI), p));
											else
												db.getJSONArray(MainActivity.DB_KEY_WIKI).remove(p);
											MainActivity.writeJson(TWEditorWV.this, db);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void confirmAndExit(boolean dirty) {
		if (dirty) {
			AlertDialog isExit = new AlertDialog.Builder(TWEditorWV.this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.confirm_to_exit_wiki)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if (nextWikiIntent == null)
										TWEditorWV.super.onBackPressed();
									else
										nextWiki();
									dialog.dismiss();
								}
							}
					)
					.setNegativeButton(android.R.string.no, null)
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							nextWikiIntent = null;
							nextWikiSerial = -1;
						}
					})
					.show();
			isExit.setCanceledOnTouchOutside(false);
		} else {
			if (nextWikiIntent == null)
				TWEditorWV.super.onBackPressed();
			else
				nextWiki();
		}
	}

	private void nextWiki() {
		String ueu = null;
		toolbar.setLogo(null);
		wv.getSettings().setBuiltInZoomControls(false);
		wv.getSettings().setDisplayZoomControls(false);
		wApp = null;
		wv.getSettings().setJavaScriptEnabled(false);
		wv.loadUrl(URL_BLANK);
		setIntent(nextWikiIntent);
		String wvTitle = null, wvSubTitle = null;
		try {
			wApp = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(nextWikiSerial);
			if (wApp == null) throw new Exception();
			id = wApp.getString(MainActivity.KEY_ID);
			ueu = MainActivity.SCH_EX_FILE + wApp.getString(MainActivity.DB_KEY_PATH);
			wvTitle = wApp.getString(MainActivity.KEY_NAME);
			try {
				wvSubTitle = wApp.getString(MainActivity.DB_KEY_SUBTITLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
			finish();
		}
		try {
			if (wvTitle != null && wvTitle.length() > 0) this.setTitle(wvTitle);
			if (wvSubTitle != null && wvSubTitle.length() > 0) toolbar.setSubtitle(wvSubTitle);
			InputStream is = null;
			try {
				is = new FileInputStream(new File(getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), id));
				Bitmap icon = BitmapFactory.decodeStream(is);
				if (icon != null) toolbar.setLogo(cIcon(icon));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(ueu);
		nextWikiIntent = null;
		nextWikiSerial = -1;
	}

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

	@Override
	public void onBackPressed() {
		if (mCustomView != null)
			wcc.onHideCustomView();
		else if (wv.canGoBack())
			wv.goBack();
		else if (isWiki) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						confirmAndExit(Boolean.parseBoolean(value));
					}
				});
			else
				wv.loadUrl(SCH_JS + ':' + getString(isClassic ? R.string.js_quit_c : R.string.js_quit));
		} else {
			TWEditorWV.super.onBackPressed();
		}
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		try {
			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				findViewById(R.id.wv_toolbar).setVisibility(View.GONE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					getWindow().setStatusBarColor(getColor(R.color.design_default_color_primary));
					getWindow().getDecorView().setSystemUiVisibility(((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : View.SYSTEM_UI_FLAG_VISIBLE) | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
				findViewById(R.id.wv_toolbar).setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					getWindow().setStatusBarColor(getColor(R.color.design_default_color_primary));
					getWindow().getDecorView().setSystemUiVisibility((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : View.SYSTEM_UI_FLAG_VISIBLE);
				} else
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
			toolbar.setBackgroundColor(getResources().getColor(R.color.design_default_color_primary));
			toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);
			toolbar.setSubtitleTextAppearance(this, R.style.TextAppearance_AppCompat_Small);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
			wvProgress.setBackgroundColor(getResources().getColor(R.color.design_default_color_primary));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


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
