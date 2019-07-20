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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
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

import com.github.donmor.filedialog.lib.FileDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

public class TWEditorWV extends AppCompatActivity {

	private JSONObject db;
	private JSONObject wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private int mOriginalOrientation;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	protected FrameLayout mFullscreenContainer;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private ProgressBar wvProgress;
	private Bitmap favicon;
	private boolean isWiki;
	private boolean dirty;

	// CONSTANT
	private static final String F02D = "%02d",
			F03D = "%03d",
			F04D = "%04d",
			SCH_ABOUT = "about",
			SCH_BLOB = "blob",
			SCH_HTTP = "http",
			SCH_HTTPS = "https",
			SCH_TEL = "tel",
			SCH_MAILTO = "mailto",
			SCH_JS = "javascript",
			PREF_BLOB = "$blob$",
			PREF_DEST = "$dest$";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		setContentView(R.layout.tweditor);

		try {
			db = MainActivity.readJson(openFileInput(MainActivity.DB_FILE_NAME));
			if (db == null) throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}


		final Toolbar toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		this.setTitle(getResources().getString(R.string.app_name));
		configurationChanged(getResources().getConfiguration());
		wv = findViewById(R.id.twWebView);
		wvProgress = findViewById(R.id.progressBar);
		wvProgress.setMax(100);
		final WebSettings wvs = wv.getSettings();
		wvs.setJavaScriptEnabled(true);
		wvs.setDatabaseEnabled(true);
		String path = getCacheDir().getPath();
		wvs.setDatabasePath(path);
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
				TWEditorWV.this.setTitle(title);
				try {
					if (wApp != null) {
						wApp.put(MainActivity.KEY_NAME, title);
						MainActivity.writeJson(openFileOutput(MainActivity.DB_FILE_NAME, MODE_PRIVATE), db);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {
				if (wApp != null) {
					if (icon != null) {
						FileOutputStream os = null;
						try {
							os = new FileOutputStream(new File(getDir(MainActivity.KEY_FAVICON, MODE_PRIVATE), wApp.getString(MainActivity.KEY_ID)));
							icon.compress(Bitmap.CompressFormat.PNG, 100, os);
							os.flush();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (os != null)
								try {
									os.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
						int width = icon.getWidth(), height = icon.getHeight();
						float scale = getResources().getDisplayMetrics().density * 16f;
						Matrix matrix = new Matrix();
						matrix.postScale(scale / width, scale / height);
						favicon = Bitmap.createBitmap(icon, 0, 0, width, height, matrix, true);
						toolbar.setLogo(new BitmapDrawable(getResources(), favicon));
					}

				}
				super.onReceivedIcon(view, icon);
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
								MainActivity.writeJson(openFileOutput(MainActivity.DB_FILE_NAME, MODE_PRIVATE), db);
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
				TWEditorWV.this.onBackPressed();
			}
		});
		Bundle bu = this.getIntent().getExtras();
		String ueu = "about:blank";
		String id = bu != null ? bu.getString(MainActivity.KEY_ID) : null;
		try {
			for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
				if (db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i).getString(MainActivity.KEY_ID).equals(id)) {
					wApp = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (wApp != null) {
			try {
				ueu = "file://" + wApp.getString(MainActivity.DB_KEY_PATH);
				String wvTitle = wApp.getString(MainActivity.KEY_NAME);
				if (!wvTitle.equals("")) this.setTitle(wvTitle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String url = bu != null ? bu.getString(MainActivity.KEY_URL) : null;
			if (url != null) ueu = url;
			if (bu != null) ueu = bu.getString(MainActivity.KEY_URL);
		}

		final class JavaScriptCallback {

			private static final String CHARSET_NAME_UTF_8 = "UTF-8";

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void getVersion(String title, boolean classic) {
				if (title.equals(getResources().getString(R.string.tiddlywiki))) {
					isWiki = true;
					if (wApp == null) {
						if (!classic)
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(TWEditorWV.this, R.string.ready_to_fork, Toast.LENGTH_SHORT).show();
									toolbar.setLogo(R.drawable.ic_fork);
								}
							});
					}
					if (classic) {
						wvs.setBuiltInZoomControls(true);
						wvs.setDisplayZoomControls(true);
					} else {
						wvs.setBuiltInZoomControls(false);
						wvs.setDisplayZoomControls(false);
					}
				} else isWiki = false;
			}

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void getB64(String data, String dest) {
				MainActivity.wGet(TWEditorWV.this, Uri.parse(MainActivity.SCHEME_BLOB_B64 + ':' + data), new File(dest));
			}

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void setDirty(boolean d) {
				dirty = d;
			}

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void saveWiki(String filepath, String data) {
				final ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(Charset.forName(CHARSET_NAME_UTF_8)));
				if (wApp != null) {
					FileOutputStream os = null;
					try {
						String fp = wApp.getString(MainActivity.DB_KEY_PATH);
						if (fp.equals(filepath)) {
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
									byte[] b = new byte[512];
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
							byte[] b = new byte[512];
							while ((length = is.read(b)) != -1) {

								os.write(b, 0, length);
								lengthTotal += length;
							}
							os.flush();
							if (lengthTotal != len) throw new Exception();
						}
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
									byte[] b = new byte[512];
									while ((length = is.read(b)) != -1) {
										os.write(b, 0, length);
										lengthTotal += length;
									}
									os.flush();
									if (lengthTotal != len) throw new Exception();
									try {
										boolean exist = false;
										JSONObject w = new JSONObject();
										for (int i = 0; i < db.getJSONArray(MainActivity.DB_KEY_WIKI).length(); i++) {
											if (db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i).getString(MainActivity.DB_KEY_PATH).equals(file.getAbsolutePath())) {
												exist = true;
												w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(i);
												break;
											}
										}
										if (exist) {
											Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();

										} else {
											w.put(MainActivity.KEY_NAME, TWEditorWV.this.getTitle());
											w.put(MainActivity.KEY_ID, MainActivity.genId());
											w.put(MainActivity.DB_KEY_PATH, file.getAbsolutePath());
											w.put(MainActivity.DB_KEY_BACKUP, false);
											db.getJSONArray(MainActivity.DB_KEY_WIKI).put(db.getJSONArray(MainActivity.DB_KEY_WIKI).length(), w);
											if (!MainActivity.writeJson(openFileOutput(MainActivity.DB_FILE_NAME, Context.MODE_PRIVATE), db))
												throw new Exception();
										}
										wApp = w;
										db.put(MainActivity.DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
										if (!MainActivity.writeJson(openFileOutput(MainActivity.DB_FILE_NAME, Context.MODE_PRIVATE), db))
											throw new Exception();
									} catch (Exception e) {
										e.printStackTrace();
										Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
									}
									if (wApp != null) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												Bitmap icon = wv.getFavicon();
												if (wApp != null) {
													if (icon != null) {
														FileOutputStream os = null;
														try {
															os = new FileOutputStream(new File(getDir(MainActivity.KEY_FAVICON, MODE_PRIVATE), wApp.getString(MainActivity.KEY_ID)));
															icon.compress(Bitmap.CompressFormat.PNG, 100, os);
															os.flush();
														} catch (Exception e) {
															e.printStackTrace();
														} finally {
															if (os != null)
																try {
																	os.close();
																} catch (Exception e) {
																	e.printStackTrace();
																}
														}
														int width = icon.getWidth(), height = icon.getHeight();
														float scale = getResources().getDisplayMetrics().density * 16f;
														Matrix matrix = new Matrix();
														matrix.postScale(scale / width, scale / height);
														favicon = Bitmap.createBitmap(icon, 0, 0, width, height, matrix, true);
														toolbar.setLogo(new BitmapDrawable(getResources(), favicon));
													} else toolbar.setLogo(null);
												}
												if (icon != null) icon.recycle();
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

		wv.addJavascriptInterface(new JavaScriptCallback(), "client");
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				Uri uri = Uri.parse(url);
				String sch = uri.getScheme();
				boolean browse = sch != null && (sch.equals(SCH_ABOUT) || sch.equals(SCH_HTTP) || sch.equals(SCH_HTTPS));
				if (sch == null || sch.length() == 0 || wApp == null && browse)
					return false;
				try {
					switch (sch) {
						case SCH_TEL:
							Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
							view.getContext().startActivity(intent);
							break;
						case SCH_MAILTO:
							Intent intent2 = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
							view.getContext().startActivity(intent2);
							break;
						case SCH_ABOUT:
						case SCH_HTTP:
						case SCH_HTTPS:
							Intent intent3 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
							view.getContext().startActivity(intent3);
							break;
						default:
							new AlertDialog.Builder(TWEditorWV.this)
									.setTitle(android.R.string.dialog_alert_title)
									.setMessage(R.string.third_part_rising)
									.setNegativeButton(android.R.string.no, null)
									.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											try {
												Intent intent4 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
												view.getContext().startActivity(intent4);
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
				if (wApp == null) {
					toolbar.setLogo(R.drawable.ic_language);
				} else {
					FileInputStream is = null;
					try {
						is = new FileInputStream(new File(getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), wApp.getString(MainActivity.KEY_ID)));
						Bitmap icon = BitmapFactory.decodeStream(is);
						if (icon != null) {
							int width = icon.getWidth(), height = icon.getHeight();
							float scale = getResources().getDisplayMetrics().density * 16f;
							Matrix matrix = new Matrix();
							matrix.postScale(scale / width, scale / height);
							TWEditorWV.this.favicon = Bitmap.createBitmap(icon, 0, 0, width, height, matrix, true);
							toolbar.setLogo(new BitmapDrawable(getResources(), TWEditorWV.this.favicon));
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (is != null) try {
							is.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}

			public void onPageFinished(WebView view, String url) {
				view.loadUrl(SCH_JS + ':' + getResources().getString(R.string.js_save));
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
								wv.loadUrl(SCH_JS + ':' + getResources().getString(R.string.js_blob).replace(PREF_BLOB, url).replace(PREF_DEST, files[0].getAbsolutePath()));
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
		wv.loadUrl(ueu);
	}

	@Override
	public void onBackPressed() {
		if (mCustomView != null)
			wcc.onHideCustomView();
		else if (wv.canGoBack())
			wv.goBack();
		else {
			if (isWiki && dirty) {
				final AlertDialog.Builder isExit = new AlertDialog.Builder(this);
				isExit.setTitle(android.R.string.dialog_alert_title);
				isExit.setMessage(R.string.confirm_to_exit_wiki);
				isExit.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								TWEditorWV.super.onBackPressed();
							}
						}
				);
				isExit.setNegativeButton(android.R.string.no, null);
				AlertDialog dialog = isExit.create();
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
			} else {
				TWEditorWV.super.onBackPressed();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configurationChanged(newConfig);
	}

	private void configurationChanged(Configuration config) {
		try {
			if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				findViewById(R.id.wv_toolbar).setVisibility(View.GONE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					TWEditorWV.this.getWindow().setStatusBarColor(Color.WHITE);
					TWEditorWV.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					TWEditorWV.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			} else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
				findViewById(R.id.wv_toolbar).setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					TWEditorWV.this.getWindow().setStatusBarColor(Color.WHITE);
					TWEditorWV.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				} else
					TWEditorWV.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		if (wv != null) {
			((ViewGroup) wv.getParent()).removeView(wv);
			wv.destroy();
			wv = null;
		}
		super.onDestroy();
	}
}