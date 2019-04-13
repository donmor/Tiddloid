package indi.donmor.tiddloid;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONObject;

@SuppressLint("SetJavaScriptEnabled")
public class TWEditorWV extends AppCompatActivity {

	private JSONObject wApp;

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
		wvs.setUseWideViewPort(true);
		wvs.setLoadWithOverviewMode(true);
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
					if (wApp!=null){
					wApp.put("name", title);
					MainActivity.writeJson(openFileOutput("data.json", MODE_PRIVATE), MainActivity.db);}
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			if (bu!=null) id=bu.getString("id");
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
				wv.setWebViewClient(new WebViewClient());
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
		System.out.println(ueu);
		wv.loadUrl(ueu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
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
					ws = "https://google.com/search?q=" + arg;
					break;
				case "Bing":
					ws = "https://bing.com/search?q=" + arg;
					break;
				case "Baidu":
					ws = "https://baidu.com/s?wd=" + arg;
					break;
				case "Sogou":
					ws = "https://sogou.com/s?q=" + arg;
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