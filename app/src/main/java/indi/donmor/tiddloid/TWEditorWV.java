package indi.donmor.tiddloid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.donmor3000.filedialog.lib.FileDialog;

import org.json.JSONObject;

import java.io.File;

@SuppressLint("SetJavaScriptEnabled")
public class TWEditorWV extends AppCompatActivity {

	private JSONObject wApp;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private ProgressBar wvProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
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
		wvs.setSupportZoom(true);
		wvs.setBuiltInZoomControls(true);
		wvs.setDisplayZoomControls(false);
		wvs.setUseWideViewPort(true);
		wvs.setLoadWithOverviewMode(true);
		wvs.setAllowFileAccess(true);
		wvs.setAllowContentAccess(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) wvs.setAllowUniversalAccessFromFileURLs(true);
		wv.setWebChromeClient(new WebChromeClient() {
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

			@TargetApi(Build.VERSION_CODES.LOLLIPOP)
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//				fileChooserParams.getAcceptTypes();
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
				FileDialog.fileDialog(TWEditorWV.this, lastDir, mode, fileChooserParams.getAcceptTypes(), 1, showHidden, false, new FileDialog.OnFileTouchedListener() {
					//				MainActivity.fileOpen(TWEditorWV.this, new String[]{"*/*"}, new MainActivity.OnFileTouchedListener() {
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
											//							results = new Uri[]{Uri.fromFile(file)};
											//							results = new Uri[]{Uri.parse(file.getAbsolutePath())};
											//							results = new Uri[]{Uri.parse("file:///storage/emulated/0/DCIM/Camera/IMG_20190415_062536.jpg")};
											System.out.println(file.getAbsolutePath());
											System.out.println(file.toURI());
											try {
												results = new Uri[]{Uri.parse(file.toURI().toString())};
												//							results = new Uri[]{Uri.parse("file://"+file.getAbsolutePath())};
												//							results = new Uri[]{Uri.fromFile(file)};
												//								Uri localUri = Uri.fromFile(file);
												////								System.out.println();
												//								Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
												//								sendBroadcast(localIntent);
												//								Uri vUri = Uri.fromFile(file);
												//								results = new Uri[]{vUri};
											} catch (Exception e) {
												e.printStackTrace();
											}
											System.out.println(Uri.parse("file://" + file.getAbsolutePath()));
//											System.out.println(results);
										} else throw new Exception();
										break;
									case 1:
//										int v = files.length;
//										for (int i=0;i<files.length;i++) {
										for (File file1 : files) {
											try {
												results = new Uri[]{Uri.parse(file1.toURI().toString())};
//												results = new Uri[]{Uri.parse(files[i].toURI().toString())};
												//							results = new Uri[]{Uri.parse("file://"+file.getAbsolutePath())};
												//							results = new Uri[]{Uri.fromFile(file)};
												//								Uri localUri = Uri.fromFile(file);
												////								System.out.println();
												//								Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
												//								sendBroadcast(localIntent);
												//								Uri vUri = Uri.fromFile(file);
												//								results = new Uri[]{vUri};
											} catch (Exception e) {
												e.printStackTrace();
											}

										}
										break;
									case 3:
										File file3 = files[0];
										if (file3 != null && file3.exists()) {
											//							results = new Uri[]{Uri.fromFile(file)};
											//							results = new Uri[]{Uri.parse(file.getAbsolutePath())};
											//							results = new Uri[]{Uri.parse("file:///storage/emulated/0/DCIM/Camera/IMG_20190415_062536.jpg")};
											System.out.println(file3.getAbsolutePath());
											System.out.println(file3.toURI());
											try {
												results = new Uri[]{Uri.parse(file3.toURI().toString())};
												//							results = new Uri[]{Uri.parse("file://"+file.getAbsolutePath())};
												//							results = new Uri[]{Uri.fromFile(file)};
												//								Uri localUri = Uri.fromFile(file);
												////								System.out.println();
												//								Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
												//								sendBroadcast(localIntent);
												//								Uri vUri = Uri.fromFile(file);
												//								results = new Uri[]{vUri};
											} catch (Exception e) {
												e.printStackTrace();
											}
											System.out.println(Uri.parse("file://" + file3.getAbsolutePath()));
//											System.out.println(results);
										} else throw new Exception();
										break;
								}
								MainActivity.db.put("lastDir", files[0].getParentFile().getAbsolutePath());
							} else throw new Exception();

						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(TWEditorWV.this, "Error processing the file", Toast.LENGTH_SHORT).show();
						}
//								results = new Uri[]{localUri};
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
		});
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
				if (bu != null) ueu = bu.getString("url");
				if (ueu != null) {
					if ((!ueu.contains(":")) && ueu.contains(".")) {
						ueu = "http://" + ueu;
					} else if (!ueu.contains(":")) {
						ueu = wSearch(ueu);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				int v = 0;
				String host = Uri.parse(url).getHost();
				if (host != null) v = host.length();
				if (v == 0) {
					return false;
				}

				if (url != null && url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
					view.getContext().startActivity(intent);
					return true;
				}

				if (url != null && url.startsWith("mailto:")) {
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
		System.out.println(ueu);
		wv.loadUrl(ueu);
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public void onBackPressed() {
//		Toast.makeText(this, WebSettings.getDefaultUserAgent(this), Toast.LENGTH_LONG).show();
		if (wv.canGoBack()) {
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
					TWEditorWV.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				}
			} else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
				findViewById(R.id.wv_toolbar).setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					TWEditorWV.this.getWindow().setStatusBarColor(Color.WHITE);
					TWEditorWV.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				}
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

	private static String wSearch(String arg) {
		String ws = "https://google.com/search?q=" + arg;
		try {
			String se = MainActivity.db.getString("searchEngine");
			switch (se) {
				case "Google":
					ws = "https://www.google.com/search?q=" + arg;
					break;
				case "Bing":
					ws = "https://www.bing.com/search?q=" + arg;
					break;
				case "Baidu":
					ws = "https://www.baidu.com/s?wd=" + arg;
					break;
				case "Sogou":
					ws = "https://www.sogou.com/web?query=" + arg;
					break;
				case "Custom":
					ws = MainActivity.db.getString("customSearchEngine").replace("%s", arg);
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ws;
	}
}