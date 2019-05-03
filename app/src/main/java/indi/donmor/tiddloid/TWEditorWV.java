package indi.donmor.tiddloid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.donmor3000.filedialog.lib.FileDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;


@SuppressLint("SetJavaScriptEnabled")
public class TWEditorWV extends AppCompatActivity {

	private JSONObject wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private int mOriginalOrientation;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	protected FrameLayout mFullscreenContainer;
	private ValueCallback<Uri[]> uploadMessage;
	private ValueCallback<Uri> uploadMessageDep;
	private WebView wv;
	private ProgressBar wvProgress;

	@SuppressLint("AddJavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		setContentView(R.layout.tweditor);
		final Toolbar toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		this.setTitle("Tiddloid");
		configurationChanged(getResources().getConfiguration());
		wv = findViewById(R.id.twwv);
		wvProgress = findViewById(R.id.progressBar);
		wvProgress.setMax(100);
		final WebSettings wvs = wv.getSettings();
		wvs.setJavaScriptEnabled(true);
		wvs.setDomStorageEnabled(true);
		wvs.setBuiltInZoomControls(false);
		wvs.setDisplayZoomControls(false);
		wvs.setUseWideViewPort(true);
		wvs.setLoadWithOverviewMode(true);
		wvs.setAllowFileAccess(true);
		wvs.setAllowContentAccess(true);
		Toast.makeText(this, wvs.getUserAgentString(), Toast.LENGTH_SHORT).show();
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
						wApp.put("name", title);
						MainActivity.writeJson(openFileOutput("data.json", MODE_PRIVATE), MainActivity.db);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
					showHidden = MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				uploadMessageDep = valueCallback;
				FileDialog.fileOpen(TWEditorWV.this, lastDir, MainActivity.HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						if (uploadMessage == null) return;
						Uri result = null;
						try {
							if (files != null && files.length > 0) {
								File file = files[0];
								if (file != null && file.exists())
									try {
										result = Uri.parse(file.toURI().toString());
									} catch (Exception e) {
										e.printStackTrace();
									}
								else throw new Exception();
								MainActivity.db.put("lastDir", files[0].getParentFile().getAbsolutePath());
								MainActivity.writeJson(openFileOutput("data.json", MODE_PRIVATE), MainActivity.db);
							} else throw new Exception();
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(TWEditorWV.this, "Error processing the file", Toast.LENGTH_SHORT).show();
						}
						uploadMessageDep.onReceiveValue(result);
						uploadMessageDep = null;
					}

					@Override
					public void onCanceled() {
						if (uploadMessageDep == null) return;
						uploadMessageDep.onReceiveValue(null);
						uploadMessageDep = null;
					}
				});
			}

			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
					showHidden = MainActivity.db.getBoolean("showHidden");
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
											System.out.println(Uri.fromFile(file));

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
											System.out.println(file3.getAbsolutePath());
											System.out.println(file3.toURI());
											try {
												results = new Uri[]{Uri.parse(file3.toURI().toString())};
											} catch (Exception e) {
												e.printStackTrace();
											}
											System.out.println(Uri.parse("file://" + file3.getAbsolutePath()));
										} else throw new Exception();
										break;
								}
								MainActivity.db.put("lastDir", files[0].getParentFile().getAbsolutePath());
								MainActivity.writeJson(openFileOutput("data.json", MODE_PRIVATE), MainActivity.db);
							} else throw new Exception();

						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(TWEditorWV.this, "Error processing the file", Toast.LENGTH_SHORT).show();
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
		try {
			String id = "";
			if (bu != null) id = bu.getString("id");
			for (int i = 0; i < MainActivity.db.getJSONArray("wiki").length(); i++) {
				if (MainActivity.db.getJSONArray("wiki").getJSONObject(i).getString("id").equals(id)) {
					wApp = MainActivity.db.getJSONArray("wiki").getJSONObject(i);
					break;
				}
			}
			if (wApp != null) {
				ueu = "file://" + wApp.getString("path");
				String wvTitle = wApp.getString("name");
				if (!wvTitle.equals("")) this.setTitle(wvTitle);
			} else {
				String url = bu != null ? bu.getString("url") : null;
				ueu = url != null ? url : "about:blank";
				if (bu != null) ueu = bu.getString("url");
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		final class JavaScriptCallback {
			@SuppressWarnings("unused")
			@JavascriptInterface
			public void getApplicationName(String data) {
				System.out.println(data);
				if (data.equals("TiddlyWiki")) {
					if (wApp == null) {
						Toast.makeText(TWEditorWV.this, R.string.ready_to_fork, Toast.LENGTH_SHORT).show();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								toolbar.setLogo(R.drawable.ic_fork);
							}
						});
					} else
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								toolbar.setLogo(null);
							}
						});
				}
			}

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void getIsClassic(String data) {
				System.out.println(data);
				if (!data.equals("undefined"))
					wvs.setBuiltInZoomControls(true);
				else wvs.setBuiltInZoomControls(false);
			}

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void getB64(String data, String dest) {
				MainActivity.wGet(TWEditorWV.this,Uri.parse("blob-b64:"+data),new File(dest));
			}

			@SuppressWarnings("unused")
			@JavascriptInterface
			public void saveWiki(String filepath, String data) {
				final ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
				if (wApp != null) {
					try {
						String fp = wApp.getString("path");
						if (fp.equals(filepath)) {
							File file = new File(fp);
							if (wApp.getBoolean("backup")) {
								try {
									String mfn = file.getName();
									File mfd = new File(file.getParentFile().getAbsolutePath() + '/' + getResources().getString(R.string.backup_directory_path).replace("$filename$", mfn));
									if (!mfd.exists()) mfd.mkdir();
									else if (!mfd.isDirectory()) throw new Exception();
									DateTime dateTime = new DateTime(file.lastModified(), DateTimeZone.UTC);
									String prefix = '.'
											+ String.format(Locale.US, "%04d", dateTime.year().get())
											+ String.format(Locale.US, "%02d", dateTime.monthOfYear().get())
											+ String.format(Locale.US, "%02d", dateTime.dayOfMonth().get())
											+ String.format(Locale.US, "%02d", dateTime.hourOfDay().get())
											+ String.format(Locale.US, "%02d", dateTime.minuteOfHour().get())
											+ String.format(Locale.US, "%02d", dateTime.secondOfMinute().get())
											+ String.format(Locale.US, "%03d", dateTime.millisOfSecond().get());
									FileInputStream isb = new FileInputStream(file);
									FileOutputStream osb = new FileOutputStream(new File(mfd.getAbsolutePath() + '/' + new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), prefix).toString()));
									int len = isb.available();
									int length, lengthTotal = 0;
									byte[] b = new byte[512];
									while ((length = isb.read(b)) != -1) {
										osb.write(b, 0, length);
										lengthTotal += length;
									}
									isb.close();
									osb.flush();
									osb.close();
									if (lengthTotal != len) throw new Exception();
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
								}
							}
//							FileOutputStream os = new FileOutputStream(new File("/sdcard/qwe.txt"));
							FileOutputStream os = new FileOutputStream(file);
							int len = is.available();
							int length, lengthTotal = 0;
							byte[] b = new byte[512];
							while ((length = is.read(b)) != -1) {

								os.write(b, 0, length);
								lengthTotal += length;
							}
							is.close();
							os.flush();
							os.close();
							if (lengthTotal != len) throw new Exception();
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
					}
				} else {
					File lastDir = Environment.getExternalStorageDirectory();
					boolean showHidden = false;
					try {
						lastDir = new File(MainActivity.db.getString("lastDir"));
						showHidden = MainActivity.db.getBoolean("showHidden");
					} catch (Exception e) {
						e.printStackTrace();
					}
					FileDialog.fileSave(TWEditorWV.this, lastDir, MainActivity.HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
						@Override
						public void onFileTouched(File[] files) {
							try {
								if (files != null && files.length > 0 && files[0] != null) {
									File file = files[0];
									FileOutputStream os = new FileOutputStream(file);
									int len = is.available();
									int length, lengthTotal = 0;
									byte[] b = new byte[512];
									while ((length = is.read(b)) != -1) {
										os.write(b, 0, length);
										lengthTotal += length;
									}
									is.close();
									os.flush();
									os.close();
									if (lengthTotal != len) throw new Exception();
									try {
										boolean exist = false;
										JSONObject w = new JSONObject();
										for (int i = 0; i < MainActivity.db.getJSONArray("wiki").length(); i++) {
											if (MainActivity.db.getJSONArray("wiki").getJSONObject(i).getString("path").equals(file.getAbsolutePath())) {
												exist = true;
												w = MainActivity.db.getJSONArray("wiki").getJSONObject(i);
												break;
											}
										}
										if (exist) {
											Toast.makeText(TWEditorWV.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();

										} else {
											w.put("name", TWEditorWV.this.getTitle());
											w.put("id", MainActivity.genId());
											w.put("path", file.getAbsolutePath());
											w.put("backup", false);
											MainActivity.db.getJSONArray("wiki").put(MainActivity.db.getJSONArray("wiki").length(), w);
											if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), MainActivity.db))
												throw new Exception();
										}
										wApp = w;
										MainActivity.db.put("lastDir", file.getParentFile().getAbsolutePath());
										if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), MainActivity.db))
											throw new Exception();
									} catch (Exception e) {
										e.printStackTrace();
										Toast.makeText(TWEditorWV.this, "Data error", Toast.LENGTH_SHORT).show();
									}
									System.out.println("==");
									System.out.println(wApp);
									if (wApp != null) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												toolbar.setLogo(null);
											}
										});
									}

								} else throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
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
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Uri uri = Uri.parse(url);
				String host = uri.getHost();
				String sch = uri.getScheme();
				if (host == null || host.length() == 0 || sch == null || sch.length() == 0)
					return false;

				if (sch.equals("tel")) {
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
					view.getContext().startActivity(intent);
					return true;
				}

				if (sch.equals("mailto")) {
					Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
					view.getContext().startActivity(intent);
					return true;
				}
				if (wApp != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					view.getContext().startActivity(intent);
					return true;
				}
				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (wApp != null) {
					toolbar.setLogo(null);
				} else {
					toolbar.setLogo(R.drawable.ic_language);
				}
			}

			public void onPageFinished(WebView view, String url) {
				view.getFavicon();
				view.loadUrl("javascript:try{window.client.getApplicationName(version.title);}catch(error){}");
				view.loadUrl("javascript:try{window.client.getIsClassic(version.major);}catch(error){}");
				view.loadUrl("javascript:" + getResources().getString(R.string.js_save));
			}
		});
		wv.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
//				String scheme = Uri.parse(url)!=null?Uri.parse(url).getScheme():null;
//				if (scheme!=null && scheme.equals("blob")){wv.loadUrl("javascript:"+getResources().getString(R.string.blob));return;};
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
					showHidden = MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				String filenameProbable = URLUtil.guessFileName(url, contentDisposition, mimeType);
				FileDialog.fileDialog(TWEditorWV.this, lastDir, filenameProbable, 3, 0, new String[]{mimeType, "*/*"}, 0, showHidden, false, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						if (files != null && files.length > 0) {
							String scheme = Uri.parse(url) != null ? Uri.parse(url).getScheme() : null;
							if (scheme != null && scheme.equals("blob")) {
								wv.loadUrl("javascript:" + getResources().getString(R.string.js_blob).replace(getResources().getString(R.string.blob_str),url).replace(getResources().getString(R.string.dest_str),files[0].getAbsolutePath()));
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
		System.out.println(ueu);
		wv.loadUrl(ueu);
	}

	@Override
	public void onBackPressed() {
		if (mCustomView != null)
			wcc.onHideCustomView();
		else if (wApp == null && wv.canGoBack()) {
			wv.goBack();
		} else {
			final AlertDialog.Builder isExit = new AlertDialog.Builder(this);
			isExit.setTitle("Notice");
			if (wApp != null)
				isExit.setMessage("Are you sure you want to quit? Please make sure all your modifications have been saved.");
			else isExit.setMessage("Are you sure you want to quit?");
			isExit.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							TWEditorWV.super.onBackPressed();
						}
					}
			);
			isExit.setNegativeButton("No", null);
			AlertDialog dialog = isExit.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
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
			wv.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
			wv.clearHistory();

			((ViewGroup) wv.getParent()).removeView(wv);
			wv.destroy();
			wv = null;
		}
		super.onDestroy();
	}
}