package indi.donmor.tiddloid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
//import android.app.Notification;
//import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
//import android.os.Message;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
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

import org.json.JSONObject;

import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;

//import javax.net.ssl.HttpsURLConnection;

//import indi.donmor.tiddloid.utils.NoLeakHandler;

@SuppressLint("SetJavaScriptEnabled")
public class TWEditorWV extends AppCompatActivity {

	private JSONObject wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	//	private int mOriginalSystemUiVisibility;
	private int mOriginalOrientation;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	protected FrameLayout mFullscreenContainer;
	private ValueCallback<Uri[]> uploadMessage;
	private ValueCallback<Uri> uploadMessageDep;
	private WebView wv;
	private ProgressBar wvProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		setContentView(R.layout.tweditor);
		Toolbar toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		this.setTitle("Tiddloid");
		configurationChanged(getResources().getConfiguration());
		wv = findViewById(R.id.twwv);
		wvProgress = findViewById(R.id.progressBar);
		wvProgress.setMax(100);
		WebSettings wvs = wv.getSettings();
		wvs.setJavaScriptEnabled(true);
//		wvs.setSupportZoom(true);
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

			@SuppressWarnings({"unused", "unchecked"})

			public void openFileChooser(ValueCallback valueCallback, String acceptType) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
					showHidden = MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				uploadMessageDep = valueCallback;
				FileDialog.fileOpen(TWEditorWV.this, lastDir, new String[]{acceptType}, showHidden, new FileDialog.OnFileTouchedListener() {
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

			//For Android  >= 4.1
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
				FileDialog.fileOpen(TWEditorWV.this, lastDir, new String[]{acceptType}, showHidden, new FileDialog.OnFileTouchedListener() {
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
				FileDialog.fileDialog(TWEditorWV.this, lastDir, null, mode, fileChooserParams.getAcceptTypes(), 1,1, showHidden, false, new FileDialog.OnFileTouchedListener() {
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
//											System.out.println(file.getAbsolutePath());
//											System.out.println(file.toURI());
											System.out.println(Uri.fromFile(file));
//											if (Build.VERSION.SDK_INT >= 24) {
//												System.out.println(FileProvider.getUriForFile(TWEditorWV.this,"indi.donmor.tiddloid.fileprovider", file).toString());
//											}

											try {
												results = new Uri[]{Uri.fromFile(file)};
//												results = new Uri[]{FileProvider.getUriForFile(TWEditorWV.this,"indi.donmor.tiddloid.fileprovider", file)};
//												results = new Uri[]{Uri.parse(file.toURI().toString())};
											} catch (Exception e) {
												e.printStackTrace();
											}
//											System.out.println(Uri.parse("file://" + file.getAbsolutePath()));
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
				mCustomViewCallback.onCustomViewHidden();
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
				toolbar.setLogo(R.drawable.ic_internet_black_24dp);
				String url = bu != null ? bu.getString("url") : null;
				ueu = url != null ? url : "about:blank";
				if (bu != null) ueu = bu.getString("url");
//				if (ueu != null) {
////					if ((!ueu.contains(":")) && ueu.contains(".")) {
//					if (ueu.indexOf('.') > 0 && ueu.indexOf(':') < ueu.indexOf('.') + 1) {
//						ueu = "http://" + ueu;
//					} else if (ueu.indexOf(':') < 1) {
//						ueu = wSearch(ueu);
//					}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
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
		});
		wv.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
					showHidden = MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("X:" + url);
				System.out.println("X:" + userAgent);
				System.out.println("X:" + contentDisposition);
				System.out.println("X:" + mimeType);
				System.out.println("X:" + contentLength);
				String filenameProbable = URLUtil.guessFileName(url, contentDisposition, mimeType);
				FileDialog.fileSave(TWEditorWV.this, lastDir, filenameProbable, new String[]{mimeType, "*/*"}, 1, showHidden, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						if (files != null && files.length > 0)
							MainActivity.wGet(TWEditorWV.this, Uri.parse(url), files[0]);
					}

					@Override
					public void onCanceled() {
						Toast.makeText(TWEditorWV.this, "Cancelled", Toast.LENGTH_SHORT).show();
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
		else if (wv.canGoBack()) {
			wv.goBack();
		} else {
			AlertDialog.Builder isExit = new AlertDialog.Builder(this);
			isExit.setTitle("Notice");
			if (wApp != null)
				isExit.setMessage("Are you sure you want to quit? Please make sure all your modifications have been saved.");
			else isExit.setMessage("Are you sure you want to quit?");
			isExit.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
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

	interface OnDownloadCompleteListener {
		void onDownloadComplete(File file);
		void onDownloadFailed();
	}

//	private static String wSearch(String arg) {
//		String ws = "https://google.com/search?q=" + arg;
//		try {
//			String se = MainActivity.db.getString("searchEngine");
//			switch (se) {
//				case "Google":
//					ws = "https://www.google.com/search?q=" + arg;
//					break;
//				case "Bing":
//					ws = "https://www.bing.com/search?q=" + arg;
//					break;
//				case "Baidu":
//					ws = "https://www.baidu.com/s?wd=" + arg;
//					break;
//				case "Sogou":
//					ws = "https://www.sogou.com/web?query=" + arg;
//					break;
//				case "Custom":
//					ws = MainActivity.db.getString("customSearchEngine").replace("%s", arg);
//					break;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return ws;
//	}
}