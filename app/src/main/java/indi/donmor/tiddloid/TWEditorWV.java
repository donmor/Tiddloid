package indi.donmor.tiddloid;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.Locale;

import indi.donmor.tiddloid.utils.TWapp;

public class TWEditorWV extends AppCompatActivity {

    JSONObject wApp;

    public WebView wv;
//    WebViewClient wvc;
    WebChromeClient wcc;
    ProgressBar wvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweditor);

        Toolbar toolbar = findViewById(R.id.wv_toolbar);
        setSupportActionBar(toolbar);

        this.setTitle("Tiddloid");

        configurationChanged(getResources().getConfiguration());

        wv = findViewById(R.id.twwv);
        wvProgress = findViewById(R.id.progressBar);
        wvProgress.setMax(100);
        WebSettings wvset = wv.getSettings();
        wvset.setJavaScriptEnabled(true);
        wvset.setUseWideViewPort(true);
        wvset.setLoadWithOverviewMode(true);
//        wvc = new WebViewClient() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                ;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                ;
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (wApp == null) {
//                    view.loadUrl(url);
//                    return true;
//                }
//                boolean v = super.shouldOverrideUrlLoading(view, url);
//                Toast.makeText(TWEditorWV.this, String.valueOf(v), Toast.LENGTH_SHORT).show();
//                return v;
//            }
//
////            @Override
////            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
////                handler.proceed();
////                Log.e("sslError", "sslError:" + error.toString());
////            }
//        };
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
                    wApp.put("name", title);
                    MainActivity.writeJson(openFileOutput("data.json",MODE_PRIVATE),MainActivity.db);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
//        wv.setWebViewClient(wvc);
//        wv.setWebViewClient(new WebViewClient());
        wv.setWebChromeClient(wcc);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                aboutToBack();
                TWEditorWV.this.onBackPressed();
            }
        });
        Bundle bu = this.getIntent().getExtras();
        String ueu = "about:blank";
//        toolbar.setLogo(R.drawable.ic_internet_black_24dp);
        try {
            String id = (String) bu.getString("id");
            for (int i = 0; i < MainActivity.db.getJSONArray("wiki").length(); i++) {
                if (MainActivity.db.getJSONArray("wiki").getJSONObject(i).getString("id").equals(id)) {
                    wApp = MainActivity.db.getJSONArray("wiki").getJSONObject(i);
                    break;
                }
            }
            if (wApp != null) {
                ueu = "file://" + wApp.getString("path");
                String wvTitle = wApp.getString("name");
                if (wvTitle != "") this.setTitle(wvTitle);
            } else {
                toolbar.setLogo(R.drawable.ic_internet_black_24dp);
                wv.setWebViewClient(new WebViewClient());
                ueu = bu.getString("url");
    //            if (!(ueu.length() > 8 && ueu.substring(0, 7) == "http://" || ueu.length() > 9 && ueu.substring(0, 8) == "https://")) {
//                if (ueu.indexOf("://") > 0) {
//                    ;
//                } else
                if (ueu.indexOf(":") > 0) {
                    ;
                } else if (ueu.indexOf(".") > 0) {
                    ueu = "http://" + ueu;
                } else {
                    ueu = wSearch(ueu);
                }
//                if (ueu.length() > 2) {
//                    Boolean url = false;
//                    for (int i = 2; i < ueu.length(); i++) {
//                        if (i > 2 && ueu.substring(i - 1, i + 2).equals("://") && ueu.length() > 5) {
//                            url = true;
//                            break;
//                        } else if (ueu.substring(i - 1, i).equals(".")) {
//                            url = true;
//                            ueu = "http://" + ueu;
//                            break;
//                        } else if (ueu.substring(i - 1, i).equals(":")) {
//                            url = true;
//                            break;
//                        }
//                    }
//                    if (!url) ueu = wSearch(ueu);
//                } else {
//                    ueu = wSearch(ueu);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        System.out.println(ueu);
        wv.loadUrl(ueu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) aboutToBack();
//        return false;
//
//    }

    @Override
    public void onBackPressed() {
//        if (f_menu.isExpanded()) {
//            f_menu.collapse();
//        } else {
//            super.onBackPressed();
//        }
        if (wv.canGoBack()) {
            wv.goBack();
        } else {
            AlertDialog.Builder isExit = new AlertDialog.Builder(this);
            isExit.setTitle("Notice");
            isExit.setMessage("Are you sure you want to quit?");
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

//    void aboutToBack() {
//
//    }

//    DialogInterface.OnClickListener quitlistener = new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) {
//            switch (which) {
//                case AlertDialog.BUTTON_POSITIVE:
//                    finish();
//                    break;
//                case AlertDialog.BUTTON_NEGATIVE:
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configurationChanged(newConfig);
    }

    void configurationChanged(Configuration config) {
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

    public static String wSearch(String arg) {
        String ws = "https://google.com/search?q=" + arg;
        try {
            String se = MainActivity.db.getString("searchEngine");
            if (se.equals("Google"))
                ws = "https://google.com/search?q=" + arg;
            else if (se.equals("Bing"))
                ws = "https://bing.com/search?q=" + arg;
            else if (se.equals("Baidu"))
                ws = "https://baidu.com/s?wd=" + arg;
            else if (se.equals("Sogou"))
                ws = "https://sogou.com/s?q=" + arg;
            else if (se.equals("Custom"))
                ws = MainActivity.db.getString("customSearchEngine").replace("%s", arg);
        } catch (Exception e) {

        }
        return ws;
    }
}
