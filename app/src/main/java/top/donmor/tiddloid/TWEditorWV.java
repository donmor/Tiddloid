/*
 * top.donmor.tiddloid.TWEditorWV <= [P|Tiddloid]
 * Last modified: 18:33:05 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
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
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.documentfile.provider.DocumentFile;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TWEditorWV extends AppCompatActivity {
	private JSONObject db, wApp;
	private WebChromeClient wcc;
	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private int mOriginalOrientation, dialogPadding;
	private Integer themeColor = null;
	private float scale;
	private ValueCallback<Uri[]> uploadMessage;
	private WebView wv;
	private Toolbar toolbar;
	private ProgressBar wvProgress;
	private Uri uri = null, cachedUri;
	private boolean isWiki, isClassic, hideAppbar = false, ready = false, failed = false;
	private byte[] exData = null;
	private Menu optMenu;
	private String id;
	private CharSequence extraContent;
	private HashMap<String, byte[]> hashes = null;
	private DocumentFile tree = null, treeIndex = null;
	private ActivityResultLauncher<Intent> getChooserDL, getChooserImport, getChooserClone;
	private JSONArray customActions = null;
	private final HashMap<Integer, String> customActionsMap = new HashMap<>();


	// CONSTANT
	private static final String
			JSI = "twi",
			EXT_HTA = ".hta",
			MIME_ANY = "*/*",
			MIME_TEXT = "text/plain",
			REX_SP_CHR = "\\s",
			KEY_ACTION = "action",
			KEY_ALG = "MD5",
			KEY_COL = ":",
			KEY_ENC = "enc",
			KEY_FIND_IND = "%1$d/%2$d",
			KEY_ICON = "icon",
			KEY_YES = "yes",
			SCH_ABOUT = "about",
			SCH_TEL = "tel",
			SCH_MAILTO = "mailto",
			URL_BLANK = "about:blank";
	private static final int CA_GRP_ID = 999;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.tweditor);
		dialogPadding = (int) (getResources().getDisplayMetrics().density * 20);
		// 初始化顶栏
		toolbar = findViewById(R.id.wv_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(v -> TWEditorWV.this.onBackPressed());
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
					byte[] bytes = new byte[MainActivity.BUF_SIZE];
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
						if ((wa = wl.getJSONObject(id = iterator.next())).has(MainActivity.DB_KEY_DAV_AUTH)) continue;
						exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
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
					byte[] b = new byte[MainActivity.BUF_SIZE];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lengthTotal += length;
					}
					os.flush();
					if (lengthTotal != len)
						throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
					Bundle bu = new Bundle();
					bu.putString(MainActivity.KEY_ID, id);
					failed = false;
					nextWiki(new Intent().putExtras(bu));
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
					failed = true;
					dumpOnFail(exData, u);
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					failed = true;
					dumpOnFail(exData, u);
				}
			}
			exData = null;
		});
		wcc = new WebChromeClient() {
			// 进度条
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				ready = newProgress == 100;
				toolbar.setVisibility(hideAppbar && ready ? View.GONE : View.VISIBLE);
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
				if (themeColor != null && dialog.getWindow() != null)
					dialog.getWindow().getDecorView().setBackgroundColor(themeColor);
				nwv.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						Uri u1;
						String p;
						if ((u1 = Uri.parse(url)).getSchemeSpecificPart().equals(Uri.parse(wv.getUrl()).getSchemeSpecificPart()) && (p = u1.getFragment()) != null) {
							wv.evaluateJavascript(getString(R.string.js_pop, Uri.decode(p)), null);
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
				if (wApp == null) {
					exData = data.getBytes(StandardCharsets.UTF_8);
					getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT)
							.addCategory(Intent.CATEGORY_OPENABLE)
							.setType(MainActivity.TYPE_HTML));
					return;
				}
				Sardine davClient = null;
				String auth = wApp.optString(MainActivity.DB_KEY_DAV_AUTH), tok = wApp.optString(MainActivity.DB_KEY_DAV_TOKEN);
				if (cachedUri != null) {//	DAV
					davClient = new OkHttpSardine();
					davClient.setCredentials(auth, tok);
				}
				if (wApp.optBoolean(MainActivity.DB_KEY_BACKUP)) try {
					MainActivity.backup(TWEditorWV.this, Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)), davClient);
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.backup_failed, Toast.LENGTH_SHORT).show();
				}
				Uri ux = cachedUri != null ? cachedUri : uri;
				String lt = null;
				try (ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
						OutputStream os = getContentResolver().openOutputStream(ux)) {
					if (os == null)
						throw new FileNotFoundException(MainActivity.EXCEPTION_SAF_FILE_NOT_EXISTS);
					int len = is.available();
					int length, lengthTotal = 0;
					byte[] b = new byte[MainActivity.BUF_SIZE];
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
						lengthTotal += length;
					}
					os.flush();
					if (lengthTotal != len)
						throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
					if (cachedUri != null) {    //	DAV
						davClient = new OkHttpSardine();
						davClient.setCredentials(auth, tok);
						lt = davClient.lock(uri.toString());
						davClient.put(uri.toString(), new File(cachedUri.getPath()), MainActivity.TYPE_HTML);
					}
					failed = false;
					runOnUiThread(() -> {
						try {
							if (cachedUri != null && !wApp.optString(MainActivity.DB_KEY_URI).equals(uri.toString()))
								syncTree(wApp.optString(MainActivity.DB_KEY_URI), auth, tok);
							else if (tree != null && treeIndex != null)
								syncTree(tree, id, treeIndex);
						} catch (IOException e) {
							e.printStackTrace();
						}
						getInfo(wv);
					});
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(TWEditorWV.this, R.string.failed, Toast.LENGTH_SHORT).show();
					failed = true;
					dumpOnFail(data.getBytes(StandardCharsets.UTF_8), uri);
				} finally {
					if (davClient != null && lt != null) {
						try {
							davClient.unlock(uri.toString(), lt);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
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

			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
				LinearLayout layout = new LinearLayout(TWEditorWV.this);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.setMarginStart(dialogPadding);
				params.setMarginEnd(dialogPadding);
				layout.setOrientation(LinearLayout.VERTICAL);
				EditText username = new EditText(TWEditorWV.this), password = new EditText(TWEditorWV.this);
				username.setHint(R.string.hint_username);
				username.setSingleLine();
				username.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				password.setHint(R.string.hint_password);
				password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				password.setSingleLine();
				layout.addView(username, params);
				layout.addView(password, params);
				final AlertDialog dialog = new AlertDialog.Builder(TWEditorWV.this)
						.setTitle(getString(R.string.hint_login, host))
						.setMessage(realm)
						.setView(layout)
						.setPositiveButton(android.R.string.ok, (dialog12, which) -> handler.proceed(username.getText().toString(), password.getText().toString()))
						.setNegativeButton(android.R.string.cancel, null)
						.create();
				dialog.setOnShowListener(dialog13 -> username.requestFocus());
				password.setOnEditorActionListener((v, actionId, event) -> (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_NULL) && dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick());
				dialog.show();
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
				view.evaluateJavascript(getString(R.string.js_print), null);
				view.evaluateJavascript(getString(R.string.js_is_wiki), value -> {
					if (isWiki = Boolean.parseBoolean(value))
						view.evaluateJavascript(getString(R.string.js_is_classic), value1 -> {
							isClassic = Boolean.parseBoolean(value1);
							getInfo(view);
							view.getSettings().setBuiltInZoomControls(isClassic);
							view.getSettings().setDisplayZoomControls(isClassic);
							if (isClassic) {
								view.evaluateJavascript(getString(R.string.js_settings_c), null);
								if (MainActivity.SCH_CONTENT.equals(Uri.parse(view.getUrl()).getScheme()))
									view.evaluateJavascript(getString(R.string.js_settings_c2), null);
								if (extraContent != null) view.evaluateJavascript(getString(R.string.js_new_tiddler_c, extraContent), null);
							} else if (extraContent != null) view.evaluateJavascript(getString(R.string.js_new_tiddler, extraContent), null);
							extraContent = null;
						});
					else if (!URL_BLANK.equals(url)) {
						if (wApp == null) notWikiConfirm();
						else {
							try {
								autoRemoveConfirm(db.getJSONObject(MainActivity.DB_KEY_WIKI), id, Uri.parse(wApp.optString(MainActivity.DB_KEY_URI)));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
				view.clearHistory();
			}
		});
		// 初始化db
		try {
			db = MainActivity.readJson(this);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		// 初始化页内查找
		((EditText) findViewById(R.id.find_edit_find)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				wv.findAllAsync(editable.toString());
			}
		});
		ImageButton btnUp = findViewById(R.id.find_up), btnDn = findViewById(R.id.find_down);
		btnUp.setOnClickListener(view -> wv.findNext(false));
		btnDn.setOnClickListener(view -> wv.findNext(true));
		btnUp.setEnabled(false);
		btnDn.setEnabled(false);
		findViewById(R.id.find_close).setOnClickListener(view -> {
			findViewById(R.id.find_bar).setVisibility(View.GONE);
			wv.clearMatches();
		});
		TextView indicator = findViewById(R.id.find_indicator);
		indicator.setText(String.format(Locale.getDefault(), KEY_FIND_IND, 0, 0));
		wv.setFindListener((i, i1, b) -> {
			indicator.setText(String.format(Locale.getDefault(), KEY_FIND_IND, Math.min(i + 1, i1), i1));
			btnUp.setEnabled(i1 != 0);
			btnDn.setEnabled(i1 != 0);
		});
		// 加载数据
		MainActivity.trimDB140(this, db);
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
			Uri u = intent.getData();
			if (u == null) {
				Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else if (Intent.ACTION_SEND.equals(action)) {    // 分享链接克隆站点
			if (!(MainActivity.TYPE_HTML.equals(intent.getType())) || !(MIME_TEXT.equals(intent.getType()) && intent.getStringExtra(Intent.EXTRA_TEXT).contains(MainActivity.SCH_HTTP))) {
				Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				return;
			}
			wv.evaluateJavascript(getString(isClassic ? R.string.js_exit_c : R.string.js_exit), value -> confirmAndExit(Boolean.parseBoolean(value), intent));
		} else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {    // 摘录文本
			CharSequence cs;
			if (!MainActivity.APIOver23 || !isWiki || (cs = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)) == null || cs.length() == 0) return;
			wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, cs), null);
		} else {    // MA/Shortcut
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
					AlertDialog confirmInvoke = new AlertDialog.Builder(this)
							.setTitle(android.R.string.dialog_alert_title)
							.setMessage(R.string.third_part_rising)
							.setNegativeButton(android.R.string.no, null)
							.setPositiveButton(android.R.string.yes, (dialog, which) -> {
								try {
									view.getContext().startActivity(intent);
								} catch (RuntimeException e) {
									e.printStackTrace();
								}
							}).create();
					confirmInvoke.setOnShowListener(dialog1 -> confirmInvoke.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
					confirmInvoke.show();
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
				toolbar.setVisibility(hideAppbar && ready ? View.GONE : View.VISIBLE);
				// 解取主题色
				String color = array.getString(3);
				float[] l = new float[3];
				if (color.length() == 7) {
					themeColor = Color.parseColor(color);
					Color.colorToHSV(themeColor, l);
				} else themeColor = null;
				getDelegate().setLocalNightMode(themeColor == null ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : l[2] > 0.75 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);   // 系统栏模式 根据主题色灰度/日夜模式
				// 解取favicon
				String fib64 = array.getString(4);
				byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
				Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
				toolbar.setLogo(favicon != null ? cIcon(favicon) : null);
				if (wApp != null) {
					// 写Json
					wApp.put(MainActivity.KEY_NAME, title).put(MainActivity.DB_KEY_SUBTITLE, subtitle).put(MainActivity.DB_KEY_COLOR, themeColor).put(MainActivity.KEY_FAVICON, fib64.length() > 0 ? fib64 : null);
					MainActivity.writeJson(TWEditorWV.this, db);
				}
				if (optMenu != null) {
					optMenu.findItem(R.id.action_save_c).setVisible(wApp == null && uri != null);
					optMenu.findItem(R.id.action_save).setVisible(wApp != null || uri == null);
				}
				String v = array.optString(5);
				customActions = v.length() > 8 ? new JSONArray(v) : null;
				onConfigurationChanged(newConfig);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			if (MainActivity.CLASS_MENU_BUILDER.equals(menu.getClass().getSimpleName())) try {
				Method method = menu.getClass().getDeclaredMethod(MainActivity.METHOD_SET_OPTIONAL_ICONS_VISIBLE, Boolean.TYPE);
				method.setAccessible(true);
				method.invoke(menu, true);
			} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return super.onPrepareOptionsMenu(menu);
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
				wv.evaluateJavascript(getString(isClassic ? R.string.js_save_c : R.string.js_save), null);
				break;
			default:
				if (item.getGroupId() == CA_GRP_ID) {
					wv.evaluateJavascript(customActionsMap.get(item.getItemId()), null);
				}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActionModeStarted(ActionMode mode) {
		Menu menu = mode.getMenu();
		menu.add(R.string.context_save).setOnMenuItemClickListener(menuItem -> {
			wv.evaluateJavascript(getString(isClassic ? R.string.js_save_c : R.string.js_save), null);
			mode.finish();
			return false;
		});
		menu.add(R.string.context_find).setOnMenuItemClickListener(menuItem -> {
			findViewById(R.id.find_bar).setVisibility(View.VISIBLE);
			wv.evaluateJavascript(getString(R.string.js_get_selected), s -> {
				String s1 = s.substring(1, s.length() - 1);
				EditText txtFind = findViewById(R.id.find_edit_find);
				if (s1.length() > 0) txtFind.setText(s1);
				else wv.findAllAsync(txtFind.getText().toString());
			});
			mode.finish();
			return false;
		});
		MenuItem vi = null;
		Intent vp;
		ComponentName vc;
		for (int i = 0; i < menu.size(); i++) {
			if ((vi = menu.getItem(i)) != null && (vp = vi.getIntent()) != null && (vc = vp.getComponent()) != null && getPackageName().equals(vc.getPackageName())) {
				vi.setTitle(R.string.context_new_tiddler);
				vi.setOnMenuItemClickListener(menuItem -> {
					wv.evaluateJavascript(getString(R.string.js_get_selected), s -> wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, s.subSequence(1, s.length() - 1)), null));
					mode.finish();
					return false;
				});
				break;
			} else vi = null;
		}
		if (vi == null) menu.add(R.string.context_new_tiddler).setOnMenuItemClickListener(menuItem -> {
			wv.evaluateJavascript(getString(R.string.js_get_selected), s -> wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, s.subSequence(1, s.length() - 1)), null));
			mode.finish();
			return false;
		});
		super.onActionModeStarted(mode);
	}

	// 保存提醒
	private void confirmAndExit(boolean dirty, final Intent nextWikiIntent) {
		if (failed || dirty) {
			AlertDialog confirmExit = new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.confirm_to_exit_wiki)
					.setPositiveButton(android.R.string.yes, (dialog, which) -> {
								dialog.dismiss();
								if (nextWikiIntent == null)
//									new Handler().postDelayed(() -> {
//										runOnUiThread(() -> {
									TWEditorWV.super.finish();
//										});
//									},100);
								else
									nextWiki(nextWikiIntent);
							}
					)
					.setNegativeButton(android.R.string.no, null)
					.create();
			confirmExit.setOnShowListener(dialog1 -> confirmExit.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
			confirmExit.show();
			confirmExit.setCanceledOnTouchOutside(false);
		} else {
			if (nextWikiIntent == null)
				finish();
			else
				nextWiki(nextWikiIntent);
		}
	}

	private void notWikiConfirm() {
		AlertDialog closeRemoveConfirm = new AlertDialog.Builder(TWEditorWV.this)
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(R.string.not_a_wiki_page)
				.setPositiveButton(android.R.string.ok, null)
				.setOnDismissListener(dialog -> TWEditorWV.this.finish())
				.create();
		closeRemoveConfirm.setOnShowListener(dialog -> closeRemoveConfirm.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
		closeRemoveConfirm.show();
	}

	private void autoRemoveConfirm(JSONObject wl, String id, Uri u) {
		AlertDialog confirmAutoRemove = new AlertDialog.Builder(this)
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(R.string.confirm_to_auto_remove_wiki)
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, (dialog, which) -> {
					try {
						wl.remove(id);
						MainActivity.writeJson(TWEditorWV.this, db);
						if (MainActivity.APIOver26) {
							if (u != null && MainActivity.SCH_CONTENT.equals(u.getScheme()))
								revokeUriPermission(getPackageName(), u, MainActivity.TAKE_FLAGS);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}).setOnDismissListener(dialogInterface -> TWEditorWV.this.finish())
				.create();
		confirmAutoRemove.setOnShowListener(dialog1 -> confirmAutoRemove.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
		confirmAutoRemove.show();
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
		final Uri u;
		String nextWikiId = null;
		final String action = nextWikiIntent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {    // 打开方式，scheme -> content/file/http(s)
			if ((u = nextWikiIntent.getData()) == null) {
				notWikiConfirm();
				return;
			}
			if (MainActivity.SCH_CONTENT.equals(u.getScheme()) || MainActivity.SCH_FILE.equals(u.getScheme())) {
				try {
					wa = null;
					boolean exist = false;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						if ((wa = wl.optJSONObject(id = iterator.next())) == null || wa.has(MainActivity.DB_KEY_DAV_AUTH)) continue;
						exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
						if (exist) break;
					}
					if (exist) {    // 列表中已存在
						Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
					} else {    // 添加到列表
						wa = new JSONObject();
						nextWikiId = MainActivity.genId();
						wa.put(MainActivity.KEY_NAME, MainActivity.KEY_TW);
						wa.put(MainActivity.DB_KEY_SUBTITLE, MainActivity.STR_EMPTY);
						wa.put(MainActivity.DB_KEY_URI, u.toString());
						wa.put(MainActivity.DB_KEY_BACKUP, false);
						wl.put(nextWikiId, wa);
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
		} else if (Intent.ACTION_SEND.equals(action)) {    // 接收分享文件
			wa = null;
			String data;
			if (MainActivity.TYPE_HTML.equals(nextWikiIntent.getType())) {    // 接收html文件
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
					notWikiConfirm();
					return;
				}
			} else {
				notWikiConfirm();
				return;
			}
		} else if (Intent.ACTION_PROCESS_TEXT.equals(action)) {    // 接收摘录
			if (!MainActivity.APIOver23) {
				if (!isWiki) finish();
				return;
			}
			CharSequence cs = nextWikiIntent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
			if (isWiki) {
				wv.evaluateJavascript(getString(isClassic ? R.string.js_new_tiddler_c : R.string.js_new_tiddler, cs), null);
				return;
			}
			if ((nextWikiId = db.optString(MainActivity.DB_KEY_DEFAULT)).length() == 0 || (wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.default_wiki_needed, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
				autoRemoveConfirm(wl, nextWikiId, null);
				return;
			}
			extraContent = cs;
		} else {    // 从列表中打开
			Bundle bu;
			if ((bu = nextWikiIntent.getExtras()) == null
					|| (nextWikiId = bu.getString(MainActivity.KEY_ID)) == null
					|| nextWikiId.length() == 0
					|| (wa = wl.optJSONObject(nextWikiId)) == null) {
				Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if ((u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI))) == null) {
				autoRemoveConfirm(wl, nextWikiId, null);
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
		if (wa != null && wa.has(MainActivity.DB_KEY_DAV_AUTH)) {    // DAV
			try {
				cachedUri = actualUri = Uri.fromFile(syncTree(u.toString(), wa.optString(MainActivity.DB_KEY_DAV_AUTH), wa.optString(MainActivity.DB_KEY_DAV_TOKEN)));
			} catch (IOException e) {
				e.printStackTrace();
				autoRemoveConfirm(wl, id, u);
				return;
			}
		} else if (u != null && (MainActivity.SCH_HTTP.equals(u.getScheme()) || MainActivity.SCH_HTTPS.equals(u.getScheme()))) {
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				if (Uri.parse(u.getScheme() + KEY_COL + u.getSchemeSpecificPart()).toString().equals((wa = wl.optJSONObject(iterator.next())) != null ? wa.optString(MainActivity.DB_KEY_URI) : null)) {
					wApp = wa;
					break;
				}
			}
		} else if (MainActivity.APIOver21 && u != null && !MainActivity.SCH_FILE.equals(u.getScheme()))
			try {
				DocumentFile p;
				tree = DocumentFile.fromTreeUri(this, u);
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
				autoRemoveConfirm(wl, id, u);
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
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		if (u == null || !MainActivity.APIOver21 && MainActivity.SCH_CONTENT.equals(u.getScheme()) && actualUri == uri || actualUri != null && (MainActivity.TYPE_HTA.equals(getContentResolver().getType(actualUri)) || actualUri.getLastPathSegment().endsWith(EXT_HTA))) {
			Uri u1 = null, ux;
			if (u == null)
				if ((u1 = nextWikiIntent.getParcelableExtra(Intent.EXTRA_STREAM)) == null) {
					notWikiConfirm();
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
			} catch (IOException | SecurityException e) {
				e.printStackTrace();
				Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
			}
		} else wv.loadUrl(actualUri != null ? actualUri.toString() : URL_BLANK);
	}

	@NonNull
	private File syncTree(String davSrc, String auth, String token) throws IOException, SecurityException {
		File cacheRoot = new File(getCacheDir(), id);
		if (hashes == null) {
			hashes = new HashMap<>();
			hashDir(cacheRoot, hashes);
		}
		Sardine davClient = new OkHttpSardine();
		davClient.setCredentials(auth, token);
		final IOException[] e0 = new IOException[1];
		final File[] index = new File[1];
		Thread jt = new Thread(() -> {
			try {
				if (!davClient.exists(davSrc)) throw new FileNotFoundException(MainActivity.EXCEPTION_FILE_NOT_FOUND);
				List<DavResource> root = davClient.list(davSrc);
				DavResource ivf;
				if (!(ivf = root.remove(0)).isDirectory()) {
					if (cacheRoot.exists() && !cacheRoot.isDirectory()) cacheRoot.delete();
					if (!cacheRoot.exists()) cacheRoot.mkdir();
					try (InputStream is = davClient.get(davSrc);
							FileOutputStream os = new FileOutputStream(index[0] = new File(cacheRoot, ivf.getName()))) {
						int length;
						byte[] bytes = new byte[MainActivity.BUF_SIZE];
						while ((length = is.read(bytes)) > -1) os.write(bytes, 0, length);
						os.flush();
					}
				} else {
					HashSet<String> files = new HashSet<>();
					syncDir(davClient, davSrc, root, cacheRoot, files);
					clrDir(cacheRoot, files);
					File[] fl;
					if ((fl = cacheRoot.listFiles(file -> MainActivity.KEY_FN_INDEX.equals(file.getName()))) != null && fl.length == 1) {
						uri = Uri.parse(davSrc + MainActivity.KEY_FN_INDEX);
						index[0] = new File(cacheRoot, MainActivity.KEY_FN_INDEX);
					} else if ((fl = cacheRoot.listFiles(file -> MainActivity.KEY_FN_INDEX2.equals(file.getName()))) != null && fl.length == 1) {
						uri = Uri.parse(davSrc + MainActivity.KEY_FN_INDEX2);
						index[0] = new File(cacheRoot, MainActivity.KEY_FN_INDEX2);
					} else throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);
				}
			} catch (IOException e) {
				e.printStackTrace();
				e0[0] = e;
			}
		});
		jt.start();
		try {
			jt.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		if (e0[0] != null) throw e0[0];
		if (index[0] == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
		return index[0];
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
							int length;
							do length = dis.read(buf, 0, MainActivity.BUF_SIZE); while (length != -1);
							map.put(child.getPath(), dis.getMessageDigest().digest());
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void syncDir(Sardine davClient, String uri, List<DavResource> src, File pos, HashSet<String> files) throws IOException, SecurityException {
		if (!pos.isDirectory()) pos.delete();
		if (!pos.exists()) {
			pos.mkdir();
		}
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(KEY_ALG);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		for (DavResource inner : src) {
			String iu = (uri.charAt(uri.length() - 1) == '/' ? uri : uri + MainActivity.KEY_SLASH) + inner.getName();
			if (!inner.isDirectory()) {
				File dest = new File(pos, inner.getName());
				byte[] dg = null;
				try (InputStream is = davClient.get(iu);
						DigestInputStream dis = messageDigest != null ? new DigestInputStream(is, messageDigest) : null
				) {
					if (is == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dis != null) {
						dis.on(true);
						do length = dis.read(buf); while (length != -1);
						if (Arrays.equals(hashes.get(dest.getPath()), dg = dis.getMessageDigest().digest())) {
							files.add(dest.getPath());
							continue;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				try (InputStream is = davClient.get(iu);
						FileOutputStream os = new FileOutputStream(dest)) {
					int length;
					byte[] bytes = new byte[MainActivity.BUF_SIZE];
					while ((length = is.read(bytes)) > -1) os.write(bytes, 0, length);
					os.flush();
					files.add(dest.getPath());
					hashes.put(dest.getPath(), dg);
				}
			} else if (inner.isDirectory()) {
				if (inner.getName() == null) continue;
				List<DavResource> innerSrc = davClient.list(iu);
				innerSrc.remove(0);
				syncDir(davClient, iu, innerSrc, new File(pos, inner.getName()), files);
			}
		}
		files.add(pos.getPath());
	}

	private void syncDir(DocumentFile src, File pos, HashSet<String> files) throws IOException, SecurityException {
		if (src == null || !src.isDirectory())
			throw new IOException(MainActivity.EXCEPTION_TREE_NOT_A_DIRECTORY);
		if (!pos.isDirectory()) pos.delete();
		if (!pos.exists()) {
			pos.mkdir();
		}
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
				byte[] dg = null;
				try (InputStream is = getContentResolver().openInputStream(inner.getUri());
						DigestInputStream dis = messageDigest != null ? new DigestInputStream(is, messageDigest) : null
				) {
					if (is == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
					byte[] buf = new byte[MainActivity.BUF_SIZE];
					int length;
					if (dis != null) {
						dis.on(true);
						do length = dis.read(buf); while (length != -1);
						if (Arrays.equals(hashes.get(dest.getPath()), dg = dis.getMessageDigest().digest())) {
							files.add(dest.getPath());
							continue;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				try (ParcelFileDescriptor ifd = getContentResolver().openFileDescriptor(inner.getUri(), MainActivity.KEY_FD_R);
						FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
						FileOutputStream os = new FileOutputStream(dest);
						FileChannel ic = is.getChannel();
						FileChannel oc = os.getChannel()
				) {
					ic.transferTo(0, ic.size(), oc);
					ic.force(true);
					files.add(dest.getPath());
					hashes.put(dest.getPath(), dg);
				} catch (IOException e) {
					e.printStackTrace();
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
				if ((wa = wl.optJSONObject(id = iterator.next())) == null || wa.has(MainActivity.DB_KEY_DAV_AUTH)) continue;
				exist = u.toString().equals(wa.optString(MainActivity.DB_KEY_URI));
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
			Toast.makeText(this, R.string.wiki_link_added, Toast.LENGTH_SHORT).show();
			getInfo(wv);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
		}

	}

	private void dumpOnFail(byte[] data, Uri u) {
		String mfn = Uri.parse(Uri.decode(u.toString())).getLastPathSegment();
		File dumpDir = new File(new File(getExternalFilesDir(null), Uri.encode(u.getSchemeSpecificPart())), mfn + MainActivity.BACKUP_POSTFIX);
		dumpDir.mkdirs();
		try (ByteArrayInputStream is = new ByteArrayInputStream(data);
				FileOutputStream os = new FileOutputStream(new File(dumpDir, new StringBuffer(mfn).insert(mfn.lastIndexOf('.'), MainActivity.formatBackup(System.currentTimeMillis())).toString()))) {
			int len = is.available(), length, lenTotal = 0;
			byte[] bytes = new byte[MainActivity.BUF_SIZE];
			while ((length = is.read(bytes)) > -1) {
				os.write(bytes, 0, length);
				lenTotal += length;
			}
			os.flush();
			if (lenTotal != len) throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
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
		toolbar.setVisibility(wApp != null && (hideAppbar && ready) ? View.GONE : View.VISIBLE);
		Window window = getWindow();
		if (MainActivity.APIOver23)
			window.setStatusBarColor(primColor);
		if (MainActivity.APIOver26)
			window.setNavigationBarColor(primColor);
		if (MainActivity.APIOver30) {
			WindowInsetsControllerCompat wic = WindowInsetsControllerCompat.toWindowInsetsControllerCompat(window.getInsetsController());
			wic.setAppearanceLightNavigationBars(lightBar);
			wic.setAppearanceLightStatusBars(lightBar);
			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				wic.hide(WindowInsetsCompat.Type.systemBars());
				wic.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
				window.setDecorFitsSystemWindows(false);
			} else {
				wic.show(WindowInsetsCompat.Type.systemBars());
				window.setDecorFitsSystemWindows(true);
			}
		} else
			window.getDecorView().setSystemUiVisibility((lightBar ? (MainActivity.APIOver23 ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : View.SYSTEM_UI_FLAG_VISIBLE) | (MainActivity.APIOver26 ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : View.SYSTEM_UI_FLAG_VISIBLE) : View.SYSTEM_UI_FLAG_VISIBLE) |
					(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY : View.SYSTEM_UI_FLAG_VISIBLE));
		findViewById(R.id.wv_appbar).setBackgroundColor(primColor);
		toolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);// 刷新字色
		toolbar.setSubtitleTextAppearance(this, R.style.TextAppearance_AppCompat_Small);
		EditText txtFind = findViewById(R.id.find_edit_find);
		txtFind.setTextColor(getResources().getColor(R.color.content));
		txtFind.setHintTextColor(getResources().getColor(R.color.content_sub));
		((TextView) findViewById(R.id.find_indicator)).setTextColor(getResources().getColor(R.color.content_sub));
		toolbar.setNavigationIcon(MainActivity.APIOver24 || lightBar ? R.drawable.ic_arrow_back : R.drawable.ic_arrow_back_d);
		((ImageButton) findViewById(R.id.find_up)).setImageDrawable(getResources().getDrawable(MainActivity.APIOver21 || lightBar ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_up_d));
		((ImageButton) findViewById(R.id.find_down)).setImageDrawable(getResources().getDrawable(MainActivity.APIOver21 || lightBar ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_down_d));
		((ImageButton) findViewById(R.id.find_close)).setImageDrawable(getResources().getDrawable(MainActivity.APIOver21 || lightBar ? R.drawable.ic_close : R.drawable.ic_close_d));
//		((ImageButton) findViewById(R.id.find_down)).setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
//		((ImageButton) findViewById(R.id.find_close)).setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
		if (optMenu != null) {
			try {
				optMenu.removeGroup(CA_GRP_ID);
				customActionsMap.clear();
				if (customActions != null) {
					for (int i = 0; i < customActions.length(); i++) {
						JSONObject item = customActions.getJSONObject(i);
						if (item == null) continue;
						String vn = item.optString(MainActivity.KEY_NAME), vc = item.optString(KEY_ACTION);
						if (vn.length() == 0 || vc.length() == 0) continue;
						int id = CA_GRP_ID + i;
						MenuItem si = optMenu.add(CA_GRP_ID, id, 0, vn);
						si.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
						String fib64 = item.optString(KEY_ICON);
						if (fib64.length() > 0) {
							byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
							Bitmap icon = BitmapFactory.decodeByteArray(b, 0, b.length);
							si.setIcon(cIcon(icon));
						} else si.setIcon(MainActivity.APIOver24 ? R.drawable.ic_menu : lightBar ? R.drawable.ic_menu_l : R.drawable.ic_menu_d);
						customActionsMap.put(id, vc);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			optMenu.findItem(R.id.action_save_c).setIcon(MainActivity.APIOver24 ? R.drawable.ic_save : lightBar ? R.drawable.ic_save_l : R.drawable.ic_save_d);
			optMenu.findItem(R.id.action_save_file).setIcon(MainActivity.APIOver24 ? R.drawable.ic_description : lightBar ? R.drawable.ic_description_l : R.drawable.ic_description_d);
			optMenu.findItem(R.id.action_save_link).setIcon(MainActivity.APIOver24 ? R.drawable.ic_language : lightBar ? R.drawable.ic_language_l : R.drawable.ic_language_d);
			optMenu.findItem(R.id.action_save).setIcon(optMenu.findItem(R.id.action_save_c).getIcon());
			for (int i = 0; i < optMenu.size(); i++) {
				MenuItem item = optMenu.getItem(i);
				SpannableString s = new SpannableString(item.getTitle());
				s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.content_tint)), 0, s.length(), 0);
				item.setTitle(s);
			}
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