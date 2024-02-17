/*
 * top.donmor.tiddloid.MainActivity <= [P|Tiddloid]
 * Last modified: 18:20:51 2024/02/16
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SvgParseException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
	private TextView noWiki;
	private WikiListAdapter wikiListAdapter;
	private JSONObject db;
	private ActivityResultLauncher<Intent> getChooserClone, getChooserCreate, getChooserImport, getChooserTree, getPermissionRequest;
	private boolean acquiringStorage = false;
	private int dialogPadding;
	private static boolean firstRun = false, isDebug = true;
	private static String version = null, latestVersion = null;
	private LinearLayout filterBar, filterBar2;
	private AppCompatCheckBox btnFilterDT;
	private Button btnFilterDTBgn, btnFilterDTEnd;
	private EditText txtFilter;
	private Date dtFilterBgn = null, dtFilterEnd = null;

	// CONSTANT
	private static final FileDialogFilter HTML_FILTER = new FileDialogFilter(new String[]{".html", ".htm", ".hta"});
	static final int TAKE_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION, BUF_SIZE = 4096;
	static final String
			KEY_TW = "TiddlyWiki",
			BACKUP_POSTFIX = "_backup",
			KEY_NAME = "name",
			KEY_LBL = " — ",
			KEY_FAVICON = "favicon",
			KEY_ID = "id",
			KEY_TZ_UTC = "UTC",
			DB_KEY_DEFAULT = "default",
			DB_KEY_WIKI = "wiki",
			DB_KEY_COLOR = "color",
			DB_KEY_NO_TINT = "no_tint",
			DB_KEY_URI = "uri",
			DB_KEY_HTTP_AUTH = "dav_username",
			DB_KEY_HTTP_TOKEN = "dav_password",
			DB_KEY_SUBTITLE = "subtitle",
			DB_KEY_BACKUP = "backup",
			DB_KEY_PLUGIN_AUTO_UPDATE = "plugin_auto_update",
			DB_KEY_KEEP_ALIVE = "keep_alive",
			KEY_FN_INDEX = "index.html",
			KEY_FN_INDEX2 = "index.htm",
			KEY_FD_R = "r",
			KEY_FD_W = "w",
			REX_B64 = "^[a-zA-Z0-9+/=]*$",
			MASK_SDF_BACKUP = "yyyyMMddHHmmssSSS",
			TEMPLATE_FILE_NAME = "template.html",
			SCH_CONTENT = "content",
			SCH_FILE = "file",
			SCH_HTTP = "http",
			SCH_HTTPS = "https",
			STR_EMPTY = "",
			TYPE_HTA = "application/hta",
			TYPE_HTML = "text/html";
	private static final String
			DB_FILE_NAME = "data.json",
			KEY_FN_HTACCESS = ".htaccess",
			KEY_DIRECTORY_INDEX = "DirectoryIndex ",
			KEY_DATE_SHORT_PF = "%tF",
			KEY_DS_NEW = "new",
			KEY_DS_DEFAULT = "default",
			DB_KEY_PATH = "path",
			KEY_EX_HTML = ".html",
			KEY_EX_HTM = ".htm",
			KEY_EX_HTA = ".hta",
			KEY_SPACE = " ",
			KEY_HDR_LOC = "Location",
			KEY_PATCH1 = "</html>\n",    // Random char workaround
			KEY_URI_RATE = "market://details?id=",
			LICENSE_FILE_NAME = "LICENSE",
			SCH_PACKAGES = "package",
			CLONING_FILE_NAME = "cloning.html";
	@SuppressWarnings("WeakerAccess")
	static final boolean APIOver23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M,
			APIOver24 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
			APIOver25 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1,
			APIOver26 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
			APIOver29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
			APIOver30 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
			APIOver33 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
	static final String
			EXCEPTION_JSON_DATA_ERROR = "JSON data file corrupted",
			EXCEPTION_DOCUMENT_IO_ERROR = "Document IO Error",
			EXCEPTION_TREE_INDEX_NOT_FOUND = "File index.htm(l) not present",
			EXCEPTION_TREE_NOT_A_DIRECTORY = "File passed in is not a directory",
			EXCEPTION_INTERRUPTED = "Interrupted by user";
	private static final String
			EXCEPTION_JSON_ID_NOT_FOUND = "Cannot find this id in the JSON data file",
			EXCEPTION_SHORTCUT_NOT_SUPPORTED = "Invoking a function that is not supported by the current system",
			EXCEPTION_NO_INTERNET = "No Internet connection",
			EXCEPTION_TRANSFER_CORRUPTED = "Transfer dest file corrupted: hash or size not match";
	private static final String[] TYPE_FILTERS = {TYPE_HTA, TYPE_HTML};

	@SuppressLint("NotifyDataSetChanged")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		long time0 = System.nanoTime();
		super.onCreate(savedInstanceState);
		Window w = getWindow();
		w.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		w.setFormat(PixelFormat.RGBA_8888);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.activity_main);
		onConfigurationChanged(getResources().getConfiguration());
		dialogPadding = (int) (getResources().getDisplayMetrics().density * 30);
		// 加载UI
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		filterBar = findViewById(R.id.filter_bar);
		filterBar2 = findViewById(R.id.filter_bar2);
		txtFilter = findViewById(R.id.filter_text);
		txtFilter.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (filterBar.getVisibility() == View.VISIBLE)
					wikiListAdapter.notifyDataSetChanged();
			}
		});
		ImageButton btnFilterClose = findViewById(R.id.filter_close);
		btnFilterClose.setOnClickListener(v -> {
			filterBar.setVisibility(View.GONE);
			filterBar2.setVisibility(View.GONE);
			btnFilterDT.setChecked(false);
			txtFilter.getEditableText().clear();
			dtFilterBgn = null;
			dtFilterEnd = null;
			btnFilterDTBgn.setText(R.string.filter_time_init);
			btnFilterDTEnd.setText(R.string.filter_time_init);
			wikiListAdapter.notifyDataSetChanged();
		});
		btnFilterDT = findViewById(R.id.filter_date);
		btnFilterDT.setOnCheckedChangeListener((buttonView, isChecked) -> {
			filterBar2.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			if (!isChecked) {
				dtFilterBgn = null;
				dtFilterEnd = null;
				btnFilterDTBgn.setText(R.string.filter_time_init);
				btnFilterDTEnd.setText(R.string.filter_time_init);
				wikiListAdapter.notifyDataSetChanged();
			}

		});
		btnFilterDTBgn = findViewById(R.id.filter_date_bgn);
		btnFilterDTBgn.setOnClickListener(v -> {
			DatePicker dp = new DatePicker(MainActivity.this);
			if (dtFilterBgn != null) {
				Calendar ref = Calendar.getInstance();
				ref.setTime(dtFilterBgn);
				dp.updateDate(ref.get(Calendar.YEAR), ref.get(Calendar.MONTH), ref.get(Calendar.DATE));
			}
			new AlertDialog.Builder(MainActivity.this)
					.setView(dp)
					.setPositiveButton(android.R.string.ok, (dialog, which) -> {
						Calendar calendar = Calendar.getInstance();
						calendar.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 0, 0, 0);
						dtFilterBgn = calendar.getTime();
						btnFilterDTBgn.setText(String.format(KEY_DATE_SHORT_PF, dtFilterBgn));
						if (dtFilterEnd != null && dtFilterEnd.before(dtFilterBgn)) {
							Calendar calendar1 = Calendar.getInstance();
							calendar1.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 23, 59, 59);
							dtFilterEnd = calendar1.getTime();
							btnFilterDTEnd.setText(String.format(KEY_DATE_SHORT_PF, dtFilterEnd));
						}
						wikiListAdapter.notifyDataSetChanged();
					})
					.setNegativeButton(android.R.string.cancel, null)
					.setNeutralButton(android.R.string.cancel, (dialog, which) -> {
						dtFilterBgn = null;
						btnFilterDTBgn.setText(R.string.filter_time_init);
					}).show();
		});
		btnFilterDTEnd = findViewById(R.id.filter_date_end);
		btnFilterDTEnd.setOnClickListener(v -> {
			DatePicker dp = new DatePicker(MainActivity.this);
			if (dtFilterEnd != null) {
				Calendar ref = Calendar.getInstance();
				ref.setTime(dtFilterEnd);
				dp.updateDate(ref.get(Calendar.YEAR), ref.get(Calendar.MONTH), ref.get(Calendar.DATE));
			}
			new AlertDialog.Builder(MainActivity.this)
					.setView(dp)
					.setPositiveButton(android.R.string.ok, (dialog, which) -> {
						Calendar calendar = Calendar.getInstance();
						calendar.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 23, 59, 59);
						dtFilterEnd = calendar.getTime();
						btnFilterDTEnd.setText(String.format(KEY_DATE_SHORT_PF, dtFilterEnd));
						if (dtFilterBgn != null && dtFilterBgn.after(dtFilterEnd)) {
							Calendar calendar1 = Calendar.getInstance();
							calendar1.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 0, 0, 0);
							dtFilterBgn = calendar1.getTime();
							btnFilterDTBgn.setText(String.format(KEY_DATE_SHORT_PF, dtFilterBgn));
						}
						wikiListAdapter.notifyDataSetChanged();
					})
					.setNegativeButton(android.R.string.cancel, null)
					.setNeutralButton(android.R.string.cancel, (dialog, which) -> {
						dtFilterEnd = null;
						btnFilterDTEnd.setText(R.string.filter_time_init);
					}).show();
		});
		noWiki = findViewById(R.id.t_noWiki);
		final SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh);
		refreshLayout.setOnRefreshListener(() -> {
			new Handler().postDelayed(() -> refreshLayout.setRefreshing(false), 500);
			MainActivity.this.onResume();
		});
		RecyclerView rvWikiList = findViewById(R.id.rvWikiList);
		rvWikiList.setLayoutManager(new LinearLayoutManager(this));
		rvWikiList.setItemAnimator(new DefaultItemAnimator());
		wikiListAdapter = new WikiListAdapter(this);
		wikiListAdapter.setReloadListener(count -> runOnUiThread(() -> noWiki.setVisibility(count > 0 ? View.GONE : View.VISIBLE)));
		wikiListAdapter.setItemFilter(new WikiListAdapter.ItemFilter() {
			@Override
			public boolean fTextActive() {
				return filterBar.getVisibility() == View.VISIBLE && txtFilter.getEditableText().length() > 0;
			}

			@Override
			public boolean fText(String title, String sub) {
				return fTextActive() && (title.contains(txtFilter.getEditableText().toString()) || sub.contains(txtFilter.getEditableText().toString()));
			}

			@Override
			public String fKeyword() {
				return fTextActive() ? txtFilter.getEditableText().toString() : STR_EMPTY;
			}

			@Override
			public boolean fTimeActive() {
				return filterBar.getVisibility() == View.VISIBLE && btnFilterDT.isChecked() && (dtFilterBgn != null || dtFilterEnd != null);
			}

			@Override
			public boolean fTime(Date time) {
				return fTimeActive() && !(dtFilterBgn != null && time.before(new Date(dtFilterBgn.getTime())) || dtFilterEnd != null && time.after(dtFilterEnd));
			}
		});
		wikiListAdapter.setOnItemClickListener(new WikiListAdapter.ItemClickListener() {
			// 点击打开
			@Override
			public void onItemClick(final int pos, final String id) {
				if (pos == -1) return;
				try {
					JSONObject wa = db.getJSONObject(DB_KEY_WIKI).getJSONObject(id);
					Uri uri = Uri.parse(wa.optString(DB_KEY_URI));
					if (SCH_CONTENT.equals(uri.getScheme())) try {
						getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					if (!loadPage(id))
						Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			}

			// 长按属性
			@SuppressLint("QueryPermissionsNeeded")
			@Override
			public void onItemLongClick(final int pos, final String id) {
				if (pos == -1) return;
				View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.wikiconfig_dialog, null);
				TextView textWikiInfo = view.findViewById(R.id.textWikiConfigPath);
				// 初始化
				final JSONObject wl, wa;
				Uri u;
				final String name;
				try {
					wl = db.getJSONObject(DB_KEY_WIKI);
					wa = wl.getJSONObject(id);
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					return;
				}
				u = Uri.parse(wa.optString(DB_KEY_URI));
				name = wa.optString(KEY_NAME, KEY_TW);
				String path, provider = getString(R.string.unknown);
				final boolean iNet = SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme()),
						legacy = SCH_FILE.equals(u.getScheme());
				if (iNet) {
					provider = getString(R.string.internet);
					path = u.toString();
				} else if (legacy) {
					provider = getString(R.string.local_legacy);
					path = u.getPath();
				} else {
					// 获取来源名
					PackageManager pm = getPackageManager();
					String v;
					for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA))
						if ((v = u.getAuthority()) != null && v.startsWith(info.packageName)) {
							provider = pm.getApplicationLabel(info).toString();
							break;
						}
					path = Uri.decode(u.getLastPathSegment());
				}
				// 显示属性
				textWikiInfo.setText(new StringBuilder(getString(R.string.provider))
						.append(provider)
						.append('\n')
						.append(getString(R.string.pathDir))
						.append(path));
				final CheckBox cbDefault = view.findViewById(R.id.cbDefault),
						cbStayBackground = view.findViewById(R.id.cbStayBackground),
						cbPluginAutoUpdate = view.findViewById(R.id.cbPluginAutoUpdate),
						cbBackup = view.findViewById(R.id.cbBackup);
				final LinearLayout rowCredential = view.findViewById(R.id.rowCredential);
				final Button btnDeleteCredential = view.findViewById(R.id.delete_credential);
				try {
					cbDefault.setChecked(id.equals(db.optString(DB_KEY_DEFAULT)));
					cbStayBackground.setChecked(wa.optBoolean(DB_KEY_KEEP_ALIVE));
					cbPluginAutoUpdate.setChecked(wa.optBoolean(DB_KEY_PLUGIN_AUTO_UPDATE));
					cbBackup.setChecked(wa.getBoolean(DB_KEY_BACKUP));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// 隐藏不可用的设置
				cbDefault.setVisibility(!iNet ? View.VISIBLE : View.GONE);
				cbStayBackground.setVisibility(!iNet ? View.VISIBLE : View.GONE);
				cbPluginAutoUpdate.setVisibility(!iNet ? View.VISIBLE : View.GONE);
				rowCredential.setVisibility(iNet && wa.optString(DB_KEY_HTTP_AUTH).length() > 0 && wa.optString(DB_KEY_HTTP_TOKEN).length() > 0 ? View.VISIBLE : View.GONE);
				cbBackup.setVisibility(!iNet ? View.VISIBLE : View.GONE);
				final ConstraintLayout frmBackupList = view.findViewById(R.id.frmBackupList);
				frmBackupList.setVisibility(cbBackup.isChecked() ? View.VISIBLE : View.GONE);
				final TextView lblNoBackup = view.findViewById(R.id.lblNoBackup);
				RecyclerView rvBackupList = view.findViewById(R.id.rvBackupList);
				rvBackupList.setLayoutManager(new LinearLayoutManager(view.getContext()));
				// 读图标
				Drawable icon = null;
				try {
					String fib64 = wa.optString(KEY_FAVICON);
					if (fib64.matches(REX_B64)) {    // Base64
						byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
						Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
						if (favicon != null) icon = new BitmapDrawable(getResources(), favicon);
					} else {    // SVG
						Sharp svg = Sharp.loadString(fib64);
						icon = svg.getSharpPicture().getDrawable();
					}
				} catch (IllegalArgumentException | SvgParseException e) {
					e.printStackTrace();
				}
				// 初始化Uri
				final Uri tu;
				Uri tu1 = null;
				DocumentFile mdf = null, df = null;
				if (!legacy) try {
					mdf = DocumentFile.fromTreeUri(MainActivity.this, u);    // ~MainDirFile
					if (mdf == null || !mdf.isDirectory()) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // 根目录不可访问, 回落SAF file
					df = getIndex(MainActivity.this, mdf);    // ~DocFile
					if (df == null || !df.isFile()) throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);    // index不存在, 预备重建
					tu1 = df.getUri();    // ~TreeUriMainFile
				} catch (IllegalArgumentException ignored) {
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					tu1 = Uri.parse(STR_EMPTY);
					mdf = DocumentFile.fromTreeUri(MainActivity.this, u);    // ~MainDirFile
					if (mdf != null && mdf.isDirectory()) {
						DocumentFile[] ep = mdf.listFiles();    // ls
						for (DocumentFile ef : ep)    // ~EachFile
							if (ef.isDirectory() && ef.getName() != null    // 存在备份
									&& (ef.getName().endsWith(KEY_EX_HTML + BACKUP_POSTFIX)
									|| ef.getName().endsWith(KEY_EX_HTM + BACKUP_POSTFIX)
									|| ef.getName().endsWith(KEY_EX_HTA + BACKUP_POSTFIX))) {
								DocumentFile vf = mdf.createFile(
										TYPE_HTML,
										ef.getName().substring(0, ef.getName().length() - BACKUP_POSTFIX.length())
								);    // ~VirtualFile; 还原文件名
								tu1 = vf != null ? vf.getUri() : Uri.parse(STR_EMPTY);    // ~TreeUriMainFile, virtual
								if (vf != null) {
									vf.delete();    // Clean up
								}
								break;
							}
					}
				} catch (IOException e) {
					e.printStackTrace();
					tu1 = Uri.parse(STR_EMPTY);    // Is SAF doc file
				}
				tu = tu1;    // ~TreeUri; 非null时为目录模式; Either real or virtual
				// 构建dialog
				Drawable finalIcon = icon;
				final AlertDialog wikiConfigDialog = new AlertDialog.Builder(MainActivity.this)
						.setTitle(name)
						.setIcon(icon != null ? icon : ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_description))
						.setView(view)
						.setPositiveButton(R.string.remove_wiki, (dialog, which) -> {
							dialog.dismiss();
							View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.del_confirm, null);
							final CheckBox cbDelFile = view1.findViewById(R.id.cbDelFile);
							final CheckBox cbDelBackups = view1.findViewById(R.id.cbDelBackups);
							cbDelFile.setEnabled(!iNet);
							cbDelBackups.setEnabled(false);
							cbDelFile.setOnCheckedChangeListener((buttonView, isChecked) -> cbDelBackups.setEnabled(isChecked));
							AlertDialog removeWikiConfirmationDialog = new AlertDialog.Builder(MainActivity.this)
									.setTitle(android.R.string.dialog_alert_title)
									.setMessage(R.string.confirm_to_remove_wiki)
									.setView(view1)
									.setNegativeButton(android.R.string.cancel, null)
									.setPositiveButton(android.R.string.ok, (dialog1, which1) -> {
										removeWiki(id, cbDelFile.isChecked(), cbDelBackups.isChecked());
										wikiListAdapter.notifyItemRemoved(pos);
									})
									.create();
							removeWikiConfirmationDialog.setOnShowListener(dialog1 -> {
								Window w = removeWikiConfirmationDialog.getWindow();
								if (w != null) w.getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()));
							});
							removeWikiConfirmationDialog.show();
						})
						.setNegativeButton(R.string.clone_wiki, (dialog, which) -> {
							dialog.dismiss();
							try {
								Uri u1 = tu != null ? tu : u;    // 从实际文件到文件模式DocFile
								File dest = new File(getCacheDir(), CLONING_FILE_NAME);
								dest.createNewFile();
								try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(u1, KEY_FD_R));
										ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(dest), KEY_FD_W));
										FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
										FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
										FileChannel ic = is.getChannel();
										FileChannel oc = os.getChannel()) {
									fc2fc(ic, oc);
								}
								getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
							} catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException |
									 NonReadableChannelException | NonWritableChannelException e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
							}
						})
						.setNeutralButton(R.string.create_shortcut, ((dialog, which) -> {
							dialog.dismiss();
							try {
								String sub = wa.optString(DB_KEY_SUBTITLE);
								Bundle bu = new Bundle();
								bu.putString(KEY_ID, id);
								Intent in = new Intent(MainActivity.this, HandlerActivity.class).putExtras(bu).setAction(Intent.ACTION_MAIN);
								if (ShortcutManagerCompat.isRequestPinShortcutSupported(MainActivity.this)) {
									ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(MainActivity.this, id)
											.setShortLabel(name)
											.setLongLabel(name + (sub.length() > 0 ? KEY_LBL + sub : sub))
											.setIcon(finalIcon != null ? IconCompat.createWithBitmap(
													finalIcon instanceof BitmapDrawable ? ((BitmapDrawable) finalIcon).getBitmap() : drawable2bitmap(finalIcon)
											) : IconCompat.createWithResource(MainActivity.this, R.drawable.ic_shortcut))
											.setIntent(in)
											.build();
									if (ShortcutManagerCompat.requestPinShortcut(MainActivity.this, shortcut, null))
										Toast.makeText(MainActivity.this, R.string.shortcut_created, Toast.LENGTH_SHORT).show();
									else throw new IllegalArgumentException(EXCEPTION_SHORTCUT_NOT_SUPPORTED);
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, R.string.shortcut_failed, Toast.LENGTH_SHORT).show();
							}
						}))
						.create();
				wikiConfigDialog.setOnShowListener(dialog -> {
					Window w = wikiConfigDialog.getWindow();
					if (w != null) w.getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()));
				});
				// 备份系统
				final BackupListAdapter backupListAdapter = new BackupListAdapter(wikiConfigDialog.getContext());
				DocumentFile finalDf = df;        // DocFile; real / virtual (null)
				DocumentFile finalMdf = mdf;    // MainDirFile; doc file / dir tree
				backupListAdapter.setOnBtnClickListener((pos1, which) -> {
					final File bf = tu == null ? backupListAdapter.getBackupFile(pos1) : null;    // DocFile模式:~BackupFile-FileObject
					final DocumentFile bdf = tu != null ? backupListAdapter.getBackupDF(pos1) : null;    // DirTree模式:~DirModeBackupFile-FileObjectD
					String bdn = bdf != null && bdf.getParentFile() != null ? bdf.getParentFile().getName() : null,        // DirTree模式:~BackupDirName-Filename
							rfn = bdn != null ? bdn.substring(0, bdn.length() - BACKUP_POSTFIX.length()) : null;    // DirTree模式:~RegularFileName-Filename
					if (bf != null && bf.isFile() || bdf != null && bdf.isFile())    // BackupFile:real / DirModeBackupFile:real
						switch (which) {    // 1 for ROLLBACK; 2 for DELETE
							case 1:        // 回滚
								AlertDialog confirmRollback = new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_rollback)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which12) -> {
											try {
												backup(MainActivity.this, u);    // 保留当前文件副本
											} catch (IOException e) {
												e.printStackTrace();
											}
											DocumentFile fdf = tu != null && finalDf == null && finalMdf != null && rfn != null
													? finalMdf.createFile(TYPE_HTML, rfn)    // 目录模式利用备份文件名重建文件并建立FileObject
													: finalDf;    // DocFile模式 / 目录模式有残留文件
											try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(tu != null ? bdf.getUri() : Uri.fromFile(bf), KEY_FD_R));
													ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(tu != null ? Objects.requireNonNull(fdf).getUri() : u, KEY_FD_W));
													FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
													FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
													FileChannel ic = is.getChannel();        // 由于SAF限制，文件模式无法还原丢失的文件，除非在原位创建一个文件，
													FileChannel oc = os.getChannel()) {    // 即使该文件是空的; 损坏的文件可以直接还原
												fc2fc(ic, oc);    // 对拷
												wikiConfigDialog.dismiss();
												Toast.makeText(MainActivity.this, R.string.wiki_rolled_back_successfully, Toast.LENGTH_SHORT).show();
												loadPage(id);    // 立即验证
											} catch (IOException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
												e.printStackTrace();
												Toast.makeText(MainActivity.this, R.string.failed_writing_file, Toast.LENGTH_SHORT).show();
											}

										})
										.create();
								confirmRollback.setOnShowListener(dialog1 -> {
									Window w = confirmRollback.getWindow();
									if (w != null) w.getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()));
								});
								confirmRollback.show();
								break;
							case 2:        // 移除备份
								AlertDialog confirmDelBackup = new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_del_backup)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which1) -> {
											try {
												if (bf != null && bf.delete()    // 文件模式
														|| bdf != null && DocumentsContract.deleteDocument(getContentResolver(), bdf.getUri()))        // 目录模式
													Toast.makeText(wikiConfigDialog.getContext(), R.string.backup_deleted, Toast.LENGTH_SHORT).show();
												else
													throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
												backupListAdapter.reload(u);
												backupListAdapter.notifyItemRemoved(pos1);
											} catch (IOException e) {
												e.printStackTrace();
												Toast.makeText(wikiConfigDialog.getContext(), R.string.failed_deleting_file, Toast.LENGTH_SHORT).show();
											}
										}).create();
								confirmDelBackup.setOnShowListener(dialog1 -> {
									Window w = confirmDelBackup.getWindow();
									if (w != null) w.getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()));
								});
								confirmDelBackup.show();
								break;
						}
				});
				backupListAdapter.setOnLoadListener(count -> {
					if (count > 0)
						lblNoBackup.setVisibility(View.GONE);
					else
						lblNoBackup.setVisibility(View.VISIBLE);
				});
				if (cbBackup.isChecked()) try {
					backupListAdapter.reload(u);
				} catch (IOException e) {
					e.printStackTrace();
				}
				rvBackupList.setAdapter(backupListAdapter);
				rvBackupList.setItemAnimator(new DefaultItemAnimator());
				cbDefault.setOnCheckedChangeListener((compoundButton, b) -> {
					try {
						if (b) db.put(DB_KEY_DEFAULT, id);
						else if (id.equals(db.optString(DB_KEY_DEFAULT))) db.remove(DB_KEY_DEFAULT);
						writeJson(MainActivity.this, db);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				});
				cbStayBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
					try {
						wa.put(DB_KEY_KEEP_ALIVE, isChecked);
						writeJson(MainActivity.this, db);
					} catch (JSONException e) {
						e.printStackTrace();
						Toast.makeText(wikiConfigDialog.getContext(), R.string.data_error, Toast.LENGTH_SHORT).show();
					}
				});
				cbPluginAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
					try {
						wa.put(DB_KEY_PLUGIN_AUTO_UPDATE, isChecked);
						writeJson(MainActivity.this, db);
					} catch (JSONException e) {
						e.printStackTrace();
						Toast.makeText(wikiConfigDialog.getContext(), R.string.data_error, Toast.LENGTH_SHORT).show();
					}
				});
				btnDeleteCredential.setOnClickListener(v -> {
					wa.remove(DB_KEY_HTTP_AUTH);
					wa.remove(DB_KEY_HTTP_TOKEN);
					try {
						writeJson(MainActivity.this, db);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					CookieManager cookieManager = CookieManager.getInstance();
					cookieManager.removeSessionCookies(null);
					cookieManager.removeAllCookies(null);
					cookieManager.flush();
					if (!(wa.optString(DB_KEY_HTTP_AUTH).length() > 0 && wa.optString(DB_KEY_HTTP_TOKEN).length() > 0)) rowCredential.setVisibility(View.GONE);
				});
				cbBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
					try {
						wa.put(DB_KEY_BACKUP, isChecked);
						writeJson(MainActivity.this, db);
						frmBackupList.setVisibility(cbBackup.isChecked() ? View.VISIBLE : View.GONE);
						if (isChecked) {
							backupListAdapter.reload(u);
							rvBackupList.setAdapter(backupListAdapter);
						}
					} catch (IOException | JSONException e) {
						e.printStackTrace();
						Toast.makeText(wikiConfigDialog.getContext(), R.string.data_error, Toast.LENGTH_SHORT).show();
					}
				});
				wikiConfigDialog.setOnDismissListener(dialog -> MainActivity.this.onResume());
				wikiConfigDialog.show();
				wikiConfigDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!iNet);
			}
		});
		rvWikiList.setAdapter(wikiListAdapter);
		// 注册SAF回调
		getChooserClone = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				cloneWiki(result.getData().getData());
			}
		});
		getChooserCreate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				createWiki(result.getData().getData());
			}
		});
		getChooserImport = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				importWiki(result.getData().getData());
			}
		});
		getChooserTree = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getData() != null) {
				addDir(result.getData().getData());
			}
		});
		getPermissionRequest = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (APIOver30 && !Environment.isExternalStorageManager())
				Toast.makeText(this, R.string.no_permission, Toast.LENGTH_SHORT).show();
			acquiringStorage = false;
		});
		new Thread(() -> {
			// 加载JSON数据
			try {
				db = readJson(MainActivity.this);
				if (!db.has(DB_KEY_WIKI)) throw new JSONException(EXCEPTION_JSON_DATA_ERROR);
			} catch (JSONException e) {
				e.printStackTrace();
				try {
					db = initJson(MainActivity.this);    // 初始化JSON数据，如果加载失败
					firstRun = true;
				} catch (JSONException e1) {
					e1.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					finishAfterTransition();
					return;
				}
			}
			trimDB140(MainActivity.this, db);    // db格式转换
			try {
				wikiListAdapter.reload(db);
			} catch (JSONException e) {
				e.printStackTrace();
				runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show());
				return;
			}
			runOnUiThread(() -> {
				rvWikiList.setAdapter(wikiListAdapter);
				noWiki.setVisibility(wikiListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
			});
			while ((System.nanoTime() - time0) / 1000000 < 1000) try {
				//noinspection BusyWait
				Thread.sleep(1);    // Minimum splash time
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runOnUiThread(() -> {
				View splash = findViewById(R.id.splash_layout);
				ViewParent parent;
				if (splash != null && (parent = splash.getParent()) instanceof ViewGroup)
					((ViewGroup) parent).removeView(splash);
				w.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
				if (firstRun)
					firstRunReq(this, new OnFirstRun() {
						@Override
						public void onAgreed() {
							try {
								writeJson(MainActivity.this, db);
								runOnUiThread(() -> refreshDynamicShortcuts());
							} catch (JSONException e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
							}
						}

						@Override
						public void onDeclined() {
							finishAffinity();
							System.exit(0);
						}
					});
				else runOnUiThread(this::refreshDynamicShortcuts);
			});
			batchFix(MainActivity.this);
		}).start();
		// 检查更新
		if (!isDebug(this))
			new Thread(this::checkUpdate).start();
	}

	private interface OnGetSrc {
		void run(File file);
	}

	static InputStream getAdaptiveUriInputStream(Uri uri, final long[] lastModified) throws NetworkErrorException, InterruptedIOException {
		try {
			HttpsURLConnection httpURLConnection;
			URL url = new URL(uri.normalizeScheme().toString());
			httpURLConnection = (HttpsURLConnection) url.openConnection();
			httpURLConnection.connect();
			lastModified[0] = httpURLConnection.getLastModified();
			return httpURLConnection.getInputStream();
		} catch (InterruptedIOException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkErrorException(EXCEPTION_NO_INTERNET);
		}
	}

	private void fetchInThread(OnGetSrc cb, AlertDialog progressDialog) {
		boolean interrupted = false;
		File cache = new File(getCacheDir(), genId()), dest = new File(getCacheDir(), TEMPLATE_FILE_NAME);
		long pModified = dest.lastModified();
		final long[] lastModified = new long[]{0L};
		try (InputStream isw = getAdaptiveUriInputStream(Uri.parse(getString(R.string.template_repo)), lastModified);
				OutputStream osw = Objects.requireNonNull(getContentResolver().openOutputStream(Uri.fromFile(cache)));
				ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(cache), KEY_FD_R));
				ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(dest), KEY_FD_W));
				FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
				FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
				FileChannel ic = is.getChannel();
				FileChannel oc = os.getChannel()) {
			// 下载到缓存
			if (lastModified[0] != pModified) {
				int length;
				byte[] bytes = new byte[BUF_SIZE];
				while ((length = isw.read(bytes)) > -1) {
					osw.write(bytes, 0, length);
					if (Thread.currentThread().isInterrupted()) {
						interrupted = true;
						break;
					}
				}
				osw.flush();
				if (interrupted) throw new InterruptedException(EXCEPTION_INTERRUPTED);
				fc2fc(ic, oc);
				dest.setLastModified(lastModified[0]);
			}
			if (progressDialog != null) progressDialog.dismiss();
		} catch (InterruptedException | InterruptedIOException ignored) {
			interrupted = true;
			runOnUiThread(() -> Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show());
		} catch (NetworkErrorException e) {
			e.printStackTrace();
			runOnUiThread(() -> Toast.makeText(this, R.string.server_error, Toast.LENGTH_SHORT).show());
			if (progressDialog != null) progressDialog.dismiss();
		} catch (IOException | SecurityException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
			e.printStackTrace();
			runOnUiThread(() -> Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show());
			if (progressDialog != null) progressDialog.dismiss();
		} finally {
			cache.delete();
			if (!interrupted) cb.run(dest);
		}
	}

	private void getSrcFromUri(OnGetSrc cb) {
		// 对话框等待
		LinearLayout layout = new LinearLayout(this);
		layout.setPaddingRelative(dialogPadding, dialogPadding, dialogPadding, 0);
		layout.setGravity(Gravity.CENTER_VERTICAL);
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setIndeterminate(true);
		progressBar.setPaddingRelative(0, 0, dialogPadding, 0);
		layout.addView(progressBar);
		TextView lblWait = new TextView(this);
		lblWait.setText(R.string.please_wait);
		lblWait.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Small);
		layout.addView(lblWait);
		final AlertDialog progressDialog = new AlertDialog.Builder(this)
				.setView(layout)
				.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
				.create();
		progressDialog.setCanceledOnTouchOutside(false);
		final Thread thread = new Thread(() -> fetchInThread(file -> MainActivity.this.runOnUiThread(() -> cb.run(file)), progressDialog));
		progressDialog.setOnShowListener(dialog -> thread.start());
		progressDialog.setOnCancelListener(dialogInterface -> thread.interrupt());
		progressDialog.show();
	}

	private Boolean loadPage(String id) {
		try {
			if (!db.getJSONObject(DB_KEY_WIKI).has(id))
				throw new JSONException(EXCEPTION_JSON_ID_NOT_FOUND);
			Bundle bu = new Bundle();
			bu.putString(KEY_ID, id);
			startActivity(new Intent(this, TWEditorWV.class).putExtras(bu).setAction(Intent.ACTION_MAIN));
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void removeWiki(String id, boolean del, boolean delBackup) {
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
			JSONObject xw = (JSONObject) wl.remove(id);        // Detached JSONObject
			writeJson(this, db);    // Commit now
			purgeDir(new File(getCacheDir(), id));    // Remove related cache
			if (del && xw != null) {
				String um = xw.optString(DB_KEY_URI);
				if (um.length() == 0) return;
				Uri u = Uri.parse(um);
				boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
				DocumentFile df, mdf = null;
				if (!legacy) try {
					mdf = DocumentFile.fromTreeUri(this, u);
					tree = true;
				} catch (IllegalArgumentException ignored) {
				}
				final File f;
				String path;
				if (tree) {
					f = null;
					if (mdf == null || !mdf.isDirectory())
						throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
					df = getIndex(this, mdf);
					if (df == null || !df.isFile())
						throw new FileNotFoundException(EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
					String mdn = df.getName();
					if (mdn == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
					u = df.getUri();
					if (delBackup) try {
						DocumentFile bdf = mdf.findFile(mdn + BACKUP_POSTFIX);
						if (bdf != null && bdf.isDirectory()) {
							for (DocumentFile inner : bdf.listFiles())
								if (inner != null && inner.isFile() && isBackupFile(df.getName(), inner.getName()))
									DocumentsContract.deleteDocument(getContentResolver(), inner.getUri());
							if (bdf.listFiles().length == 0)
								bdf.delete();
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				} else if (!SCH_HTTP.equals(u.getScheme()) && !SCH_HTTPS.equals(u.getScheme()) && (path = u.getPath()) != null) {
					f = legacy
							? new File(path)
							: new File(new File(getExternalFilesDir(null), Uri.encode(u.getSchemeSpecificPart())),
							(df = DocumentFile.fromSingleUri(this, u)) != null && df.getName() != null
									? df.getName()
									: KEY_FN_INDEX);    // real file in the dest dir for file:// or virtual file in ext files dir for content://
					if (delBackup) try {
						File fb = new File(f.getParentFile(), f.getName() + BACKUP_POSTFIX);
						if (fb.isDirectory()) {
							File[] fl;
							if ((fl = fb.listFiles(pathname -> pathname.isFile() && isBackupFile(f.getName(), pathname.getName()))) != null)
								for (File inner : fl)
									inner.delete();
							fb.delete();
							File fv;
							if (!legacy && (fv = f.getParentFile()) != null && fv.exists())
								fv.delete();
						}
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				} else return;
				if (legacy) f.delete();
				else DocumentsContract.deleteDocument(getContentResolver(), u);
				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
			}
			wikiListAdapter.reload(db);
		} catch (IOException | JSONException | UnsupportedOperationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recursive delete
	 */
	private void purgeDir(File dir) {
		if (dir == null || !dir.exists()) return;
		if (dir.isDirectory()) {
			File[] fl = dir.listFiles();
			if (fl != null) for (File f : fl) {
				if (f.isDirectory()) purgeDir(f);
				else f.delete();
			}
		}
		dir.delete();
	}

	@Override
	public boolean onCreateOptionsMenu(@NotNull Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@SuppressLint("RestrictedApi")
	@Override
	public boolean onPrepareOptionsMenu(@NotNull Menu menu) {
		if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);
		if (!isDebug(this) && latestVersion != null && !latestVersion.equals(getVersion(this))) {
			MenuItem item = menu.findItem(R.id.action_update);
			item.setTitle(getString(R.string.action_update, latestVersion));
			item.setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		final int idNew = R.id.action_new,
				idImport = R.id.action_file_import,
				idDir = R.id.action_add_dir,
				idLocal = R.id.action_add_legacy,
				idFilter = R.id.action_filter,
				idAbout = R.id.action_about,
				idUpdate = R.id.action_update;
		if (id == idNew) {
			getChooserCreate.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
		} else if (id == idImport) {
			getChooserImport.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML).putExtra(Intent.EXTRA_MIME_TYPES, TYPE_FILTERS));
		} else if (id == idDir) {
			getChooserTree.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
		} else if (id == idLocal) {
			browseLocal();
		} else if (id == idFilter) {
			filterBar.setVisibility(View.VISIBLE);
		} else if (id == idAbout) {
			SpannableStringBuilder spannableString = new SpannableStringBuilder(getString(R.string.about));
			Linkify.addLinks(spannableString, Linkify.ALL);
			if (Locale.CHINA.equals(getResources().getConfiguration().locale)) {
				spannableString.append('\n').append('\n');
				spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.content_sub)),
						spannableString.length(), spannableString.length(), Spanned.SPAN_MARK_POINT);
				spannableString.setSpan(new RelativeSizeSpan(0.75f), spannableString.length(), spannableString.length(), Spanned.SPAN_MARK_POINT);
				spannableString.setSpan((AlignmentSpan) () -> Layout.Alignment.ALIGN_CENTER,
						spannableString.length(), spannableString.length(), Spanned.SPAN_MARK_POINT);
				spannableString.append(getString(R.string.ICP));
			}
			AlertDialog aboutDialog = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.about_title, getVersion(this)))
					.setMessage(spannableString)
					.setPositiveButton(android.R.string.ok, null)
					.setNeutralButton(R.string.market, (dialog, which) -> {
						try {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(KEY_URI_RATE + getPackageName())));
						} catch (ActivityNotFoundException e) {
							e.printStackTrace();
						}
					}).create();
			aboutDialog.setOnShowListener(dialog -> {
				Window w = aboutDialog.getWindow();
				if (w != null) w.getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()));
			});
			aboutDialog.show();
			((TextView) aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			if (APIOver23)
				((TextView) aboutDialog.findViewById(android.R.id.message)).setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		} else if (id == idUpdate) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.update_url)));
			try {
				startActivity(intent);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("ReportShortcutUsage")
	private void refreshDynamicShortcuts() {
		if (APIOver25) {
			ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
			Intent newWikiIntent = new Intent(this, TWEditorWV.class).setAction(Intent.ACTION_CREATE_DOCUMENT);
			ShortcutInfo newWikiShortcut = new ShortcutInfo.Builder(this, KEY_DS_NEW)
					.setShortLabel(getString(R.string.action_sc_s_new))
					.setLongLabel(getString(R.string.action_new))
					.setIcon(Icon.createWithResource(this, R.drawable.ic_description))
					.setIntent(newWikiIntent).build();
			Bundle bundle = new Bundle();
			bundle.putString(KEY_ID, TWEditorWV.ID_DEFAULT);
			Intent defaultWikiIntent = new Intent(this, TWEditorWV.class).setAction(Intent.ACTION_MAIN).putExtras(bundle);
			ShortcutInfo defaultWikiShortcut = new ShortcutInfo.Builder(this, KEY_DS_DEFAULT)
					.setShortLabel(getString(R.string.action_sc_s_default))
					.setLongLabel(getString(R.string.default_wiki))
					.setIcon(Icon.createWithResource(this, R.drawable.ic_description))
					.setIntent(defaultWikiIntent).build();
			shortcutManager.setDynamicShortcuts(Arrays.asList(newWikiShortcut, defaultWikiShortcut));
		}
	}

	private void checkUpdate() {
		try {
			URL url = new URL(getString(R.string.update_url));
			HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
			httpURLConnection.setReadTimeout(10000);
			httpURLConnection.setInstanceFollowRedirects(false);
			httpURLConnection.connect();
			int status = httpURLConnection.getResponseCode();
			if (status != HttpsURLConnection.HTTP_MOVED_TEMP
					&& status != HttpsURLConnection.HTTP_MOVED_PERM
					&& status != HttpsURLConnection.HTTP_SEE_OTHER)
				return;
			latestVersion = Uri.parse(httpURLConnection.getHeaderField(KEY_HDR_LOC)).getLastPathSegment();
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void browseLocal() {
		if (!checkPermission(MainActivity.this)) return;
		FileDialogOpen.fileOpen(this, HTML_FILTER, file -> {
			try {
				String id = null, ux = Uri.fromFile(file).toString();
				JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa;
				boolean exist = false;
				Iterator<String> iterator = wl.keys();
				while (iterator.hasNext()) {
					exist = ux.equals(wl.getJSONObject(id = iterator.next()).optString(DB_KEY_URI));
					if (exist) break;
				}
				if (exist) Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
				else {
					wa = new JSONObject();
					id = genId();
					wa.put(KEY_NAME, KEY_TW)
							.put(DB_KEY_SUBTITLE, STR_EMPTY)
							.put(DB_KEY_URI, ux)
							.put(DB_KEY_PLUGIN_AUTO_UPDATE, false)
							.put(DB_KEY_BACKUP, false);
					wl.put(id, wa);
				}
				writeJson(MainActivity.this, db);
				if (!loadPage(id)) runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show());
			} catch (JSONException e) {
				e.printStackTrace();
				runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show());
			}
		});
	}

	private void cloneWiki(Uri uri) {
		createWiki(uri, true);
	}

	private void createWiki(Uri uri) {
		createWiki(uri, false);
	}

	private void createWiki(Uri uri, boolean clone) {
		OnGetSrc cb = file -> {
			if (file.exists()) {
				try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(file), KEY_FD_R));
						ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(uri, KEY_FD_W));
						FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel ic = is.getChannel();
						FileChannel oc = os.getChannel()) {
					// 查重
					String id = null;
					JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa = null;
					boolean exist = false;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						if ((wa = wl.getJSONObject(id = iterator.next())).has(DB_KEY_HTTP_AUTH)) continue;
						exist = uri.toString().equals(wa.optString(DB_KEY_URI));
						if (exist) break;
					}
					if (exist) {
						Toast.makeText(MainActivity.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
						if (wa.optBoolean(DB_KEY_BACKUP)) backup(MainActivity.this, uri);
					} else {
						wa = new JSONObject()
								.put(DB_KEY_URI, uri.toString())
								.put(DB_KEY_PLUGIN_AUTO_UPDATE, false)
								.put(DB_KEY_BACKUP, false);
						id = genId();
						wl.put(id, wa);
					}
					wa.put(KEY_NAME, KEY_TW)
							.put(DB_KEY_SUBTITLE, STR_EMPTY);
					writeJson(MainActivity.this, db);
					fc2fc(ic, oc);
					try {
						getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					if (!loadPage(id))
						Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				} catch (IOException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			} else Toast.makeText(MainActivity.this, R.string.error_processing_file, Toast.LENGTH_SHORT).show();
			if (clone) file.delete();
		};
		if (clone) cb.run(new File(getCacheDir(), CLONING_FILE_NAME));
		else getSrcFromUri(cb);
	}

	private void importWiki(Uri uri) {
		String id = null;
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa;
			boolean exist = false;
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				if ((wa = wl.getJSONObject(id = iterator.next())).has(DB_KEY_HTTP_AUTH)) continue;
				exist = uri.toString().equals(wa.optString(DB_KEY_URI));
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				id = genId();
				wa = new JSONObject()
						.put(KEY_NAME, KEY_TW)
						.put(DB_KEY_SUBTITLE, STR_EMPTY)
						.put(DB_KEY_URI, uri.toString())
						.put(DB_KEY_PLUGIN_AUTO_UPDATE, false)
						.put(DB_KEY_BACKUP, false);
				wl.put(id, wa);
			}
			writeJson(this, db);
			try {
				getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
		}
		if (!loadPage(id))
			Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
	}

	private void addDir(Uri uri) {
		DocumentFile mdf = DocumentFile.fromTreeUri(this, uri);
		if (mdf == null || !mdf.isDirectory()) {
			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			return;
		}
		DocumentFile index = getIndex(this, mdf);
		if (index == null) {
			DocumentFile nf = mdf.createFile(TYPE_HTML, KEY_FN_INDEX);
			if (nf == null) {
				Toast.makeText(this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				return;
			}
			getSrcFromUri(file -> {
				try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(file), KEY_FD_R));
						ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(nf.getUri(), KEY_FD_W));
						FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel ic = is.getChannel();
						FileChannel oc = os.getChannel()) {
					fc2fc(ic, oc);
					addDir(uri);
				} catch (IOException | NullPointerException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
				}
			});
			return;
		}
		String id = null;
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa;
			boolean exist = false;
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				if ((wa = wl.getJSONObject(id = iterator.next())).has(DB_KEY_HTTP_AUTH)) continue;
				exist = uri.toString().equals(wa.optString(DB_KEY_URI));
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				wa = new JSONObject();
				id = genId();
				wa.put(KEY_NAME, KEY_TW)
						.put(DB_KEY_SUBTITLE, STR_EMPTY)
						.put(DB_KEY_URI, uri.toString())
						.put(DB_KEY_PLUGIN_AUTO_UPDATE, false)
						.put(DB_KEY_BACKUP, false);
				wl.put(id, wa);
			}
			writeJson(this, db);
			try {
				getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
		}
		if (!loadPage(id))
			Toast.makeText(this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@SuppressLint("NotifyDataSetChanged")
	@Override
	public void onResume() {
		super.onResume();
		try {
			db = readJson(this);
			wikiListAdapter.reload(db);
			wikiListAdapter.notifyDataSetChanged();    // Poor performance but no other way's here
			noWiki.setVisibility(wikiListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		onConfigurationChanged(getResources().getConfiguration());    // 刷新界面主题色
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Window w = getWindow();
		int color = getResources().getColor(R.color.design_default_color_primary);
		WindowInsetsControllerCompat wic = WindowCompat.getInsetsController(w, w.getDecorView());
		if (APIOver23) w.setStatusBarColor(color);
		if (APIOver26) w.setNavigationBarColor(color);
		wic.show(WindowInsetsCompat.Type.systemBars());
		boolean lightBar = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
		wic.setAppearanceLightNavigationBars(lightBar);
		wic.setAppearanceLightStatusBars(lightBar);
		WindowCompat.setDecorFitsSystemWindows(w, true);
	}

	/**
	 * Get index from .htaccess / fallback presets
	 */
	static DocumentFile getIndex(Context context, @NonNull DocumentFile parent) {
		if (!parent.isDirectory()) return null;
		DocumentFile p, ht = parent.findFile(KEY_FN_HTACCESS);
		if (ht != null)
			try (InputStream is = context.getContentResolver().openInputStream(ht.getUri())) {
				if (is == null) throw new IOException();
				byte[] b = new byte[is.available()];
				is.read(b);
				StringBuilder w = new StringBuilder(new String(b, StandardCharsets.UTF_8));
				if (!w.toString().contains(KEY_DIRECTORY_INDEX)) throw new IOException();
				w.delete(0, w.indexOf(KEY_DIRECTORY_INDEX) + KEY_DIRECTORY_INDEX.length());
				String x;
				if ((x = w.toString()).indexOf('<') > 0) w.setLength(x.indexOf('<'));
				if ((x = w.toString()).indexOf('\r') > 0) w.setLength(x.indexOf('\r'));
				if ((x = w.toString()).indexOf('\n') > 0) w.setLength(x.indexOf('\n'));
				String[] col = w.toString().split(KEY_SPACE);
				for (String i : col)
					if ((p = parent.findFile(i)) != null && p.isFile() && p.getName() != null
							&& (p.getName().endsWith(KEY_EX_HTML) || p.getName().endsWith(KEY_EX_HTM)))
						return p;
			} catch (IOException ignored) {
			}
		return (p = parent.findFile(KEY_FN_INDEX)) != null && p.isFile()    // Fallback
				? p
				: (p = parent.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
	}

	private static void batchFix(Context context) {
		File ext = context.getExternalFilesDir(null);
		Charset mCharset = StandardCharsets.UTF_8;
		if (ext != null && ext.isDirectory()) {
			File[] files = ext.listFiles(pathname -> pathname.isFile() && pathname.canRead() && pathname.canWrite() && (
					pathname.getName().endsWith(KEY_EX_HTML)
							|| pathname.getName().endsWith(KEY_EX_HTM)
							|| pathname.getName().endsWith(KEY_EX_HTA)));
			if (files != null) for (File f : files) {
				try (ParcelFileDescriptor ifd = Objects.requireNonNull(context.getContentResolver().openFileDescriptor(Uri.fromFile(f), KEY_FD_R));
						ParcelFileDescriptor ofd = Objects.requireNonNull(context.getContentResolver().openFileDescriptor(Uri.fromFile(f), KEY_FD_W));
						FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
						FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
						FileChannel ic = is.getChannel();
						FileChannel oc = os.getChannel()) {   //读全部数据
					long len = ic.size();
					ByteBuffer sc = ByteBuffer.allocate((int) len);
					ic.read(sc);
					ic.force(true);
					if (len < 32) throw new IOException(MainActivity.EXCEPTION_TRANSFER_CORRUPTED);
					String data = null;
					byte[] bytes = sc.array(), hdr = new byte[32];
					System.arraycopy(bytes, 0, hdr, 0, 32);
					if (Arrays.equals(hdr, TWEditorWV.HEADER_U16BE_BOM) || Arrays.equals(hdr, TWEditorWV.HEADER_U16LE_BOM)) {
						data = new String(bytes, StandardCharsets.UTF_16);    // UTF-16 + BOM
						mCharset = StandardCharsets.UTF_16;
					}
					if (data == null && Arrays.equals(hdr, TWEditorWV.HEADER_U16BE)) {
						data = new String(bytes, StandardCharsets.UTF_16BE);    // UTF-16BE
						mCharset = StandardCharsets.UTF_16BE;
					}
					if (data == null && Arrays.equals(hdr, TWEditorWV.HEADER_U16LE)) {
						data = new String(bytes, StandardCharsets.UTF_16LE);    // UTF-16LE
						mCharset = StandardCharsets.UTF_16LE;
					}
					if (data == null)
						data = new String(bytes, StandardCharsets.UTF_8);    // UTF-8 / Fallback
					int sk;
					if ((sk = data.indexOf(KEY_PATCH1)) > 0 && sk + KEY_PATCH1.length() < data.length())
						data = data.substring(0, sk + KEY_PATCH1.length());
					ByteBuffer byteBuffer = ByteBuffer.wrap(data.getBytes(mCharset));
					oc.write(byteBuffer);
					oc.truncate(byteBuffer.array().length);
					oc.force(true);
				} catch (NullPointerException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static JSONObject initJson(Context context) throws JSONException {
		File ext = context.getExternalFilesDir(null), file = new File(ext, DB_FILE_NAME);
		if (ext != null && file.isFile())
			try (ParcelFileDescriptor ifd = Objects.requireNonNull(context.getContentResolver().openFileDescriptor(Uri.fromFile(file), KEY_FD_R));
					FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
					FileChannel ic = is.getChannel()) {
				JSONObject jsonObject = new JSONObject(new String(fc2ba(ic)));
				if (!jsonObject.has(DB_KEY_WIKI)) jsonObject.put(DB_KEY_WIKI, new JSONObject());
				return jsonObject;
			} catch (IOException | JSONException | NonReadableChannelException e) {
				e.printStackTrace();
			} finally {
				file.delete();
			}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(DB_KEY_WIKI, new JSONObject());
		return jsonObject;
	}


	static JSONObject readJson(Context context) throws JSONException {
		try (FileInputStream is = context.openFileInput(DB_FILE_NAME);
				FileChannel ic = is.getChannel()) {
			return new JSONObject(new String(fc2ba(ic)));
		} catch (IOException | NonReadableChannelException e) {
			throw new JSONException(e.getMessage());
		}
	}

	static void writeJson(Context context, JSONObject vdb) throws JSONException {
		try (FileOutputStream os = context.openFileOutput(DB_FILE_NAME, MODE_PRIVATE);
				FileChannel oc = os.getChannel()) {
			ba2fc(vdb.toString(2).getBytes(), oc);
		} catch (IOException | NonWritableChannelException e) {
			throw new JSONException(e.getMessage());
		}
	}

	static void exportJson(Context context, JSONObject vdb) {
		File ext = context.getExternalFilesDir(null);
		if (ext == null) return;
		try (ParcelFileDescriptor ofd = Objects.requireNonNull(context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(ext, DB_FILE_NAME)), KEY_FD_W));
				FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
				FileChannel oc = os.getChannel()) {
			ba2fc(vdb.toString(2).getBytes(), oc);
		} catch (IOException | JSONException | NonWritableChannelException e) {
			e.printStackTrace();
		}
	}

	@TargetApi(23)
	private static boolean checkPermission(Context context) {
		boolean havePerms = false;
		if (APIOver30) {
			if (!Environment.isExternalStorageManager()) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
						.setData(Uri.parse(SCH_PACKAGES + ':' + context.getPackageName()));
				MainActivity activity = (MainActivity) context;
				if (!activity.acquiringStorage) {
					activity.acquiringStorage = true;
					activity.getPermissionRequest.launch(intent);
				}
			} else havePerms = true;
		} else if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		else havePerms = true;
		return havePerms;
	}

	static String genId() {
		return UUID.randomUUID().toString();
	}

	static boolean isBackupFile(String main, String chk) {
		if (main == null || chk == null) return false;
		String mfn1 = main.substring(0, main.lastIndexOf('.')),
				mfn2 = main.substring(main.lastIndexOf('.') + 1);
		int efp1, efp2;
		String efn1 = chk.substring(0, (efp1 = chk.lastIndexOf('.', (efp2 = chk.lastIndexOf('.')) - 1))),
				efn2 = chk.substring(efp1 + 1, efp2),
				efn3 = chk.substring(efp2 + 1);
		if (efn2.length() != 17 || !mfn1.equals(efn1) || !mfn2.equals(efn3)) return false;
		for (char p : efn2.toCharArray()) {
			if (p < 48 || p > 57) return false;
		}
		return true;
	}

	static void backup(Context context, Uri u) throws IOException {
		if (SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme()))
			return;        // No backup for http links
		boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
		DocumentFile df = null, mdf = null;
		if (!legacy) try {    // tree模式
			DocumentFile p;
			String fn;
			mdf = DocumentFile.fromTreeUri(context, u);    // 根目录
			if (mdf == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
			df = getIndex(context, mdf);    // index.htm(l)
			if ((df == null) || !df.isFile() || (fn = df.getName()) == null || df.length() == 0)
				throw new FileNotFoundException(EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
			if ((p = mdf.findFile(fn = fn + BACKUP_POSTFIX)) == null) {
				mdf = mdf.createDirectory(fn);
			} else mdf = p;
			if (mdf == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
			tree = true;
		} catch (IllegalArgumentException ignored) {
		}
		DocumentFile bdf = null;
		String mfn;
		String bfn;
		File file, mfd = null;
		if (!tree) {
			if (u.getPath() == null) return;    // Usually impossible
			file = legacy ? new File(u.getPath()) : new File(new File(context.getExternalFilesDir(null), Uri.encode(u.getSchemeSpecificPart())), (df = DocumentFile.fromSingleUri(context, u)) != null && df.getName() != null ? df.getName() : KEY_FN_INDEX);    // real file in the dest dir for file:// or virtual file in ext files dir for content://
			df = legacy ? DocumentFile.fromFile(file) : df;
			mfn = file.getName();
			bfn = new StringBuffer(mfn).insert(mfn.lastIndexOf('.'), formatBackup(df != null ? df.lastModified() : 0)).toString();
			mfd = new File(file.getParentFile(), mfn + BACKUP_POSTFIX);
			if (!mfd.exists()) mfd.mkdirs();
			if (!mfd.isDirectory()) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
		} else {
			mfn = df.getName();
			bfn = new StringBuffer(mfn).insert(mfn.lastIndexOf('.'), formatBackup(df.lastModified())).toString();
			if ((bdf = mdf.createFile(TYPE_HTML, bfn)) == null)
				throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
		}
		try (ParcelFileDescriptor ifd = Objects.requireNonNull(context.getContentResolver().openFileDescriptor(tree ? df.getUri() : u, KEY_FD_R));
				ParcelFileDescriptor ofd = Objects.requireNonNull(context.getContentResolver().openFileDescriptor(tree ? bdf.getUri() : Uri.fromFile(new File(mfd, bfn)), KEY_FD_W));
				FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
				FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
				FileChannel ic = is.getChannel();
				FileChannel oc = os.getChannel()) {
			fc2fc(ic, oc);
		} catch (IOException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
			throw new IOException(e.getMessage());
		}
	}

	static String formatBackup(long time) {
		SimpleDateFormat format = new SimpleDateFormat(MASK_SDF_BACKUP, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(KEY_TZ_UTC));
		return '.' + format.format(new Date(time));
	}

	private static Bitmap drawable2bitmap(Drawable drawable) {
		int w = drawable.getIntrinsicWidth(), h = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(new Canvas(bitmap));
		return bitmap;
	}

	static byte[] fc2ba(@NonNull FileChannel ic) throws IOException, NonReadableChannelException {
		if (ic.size() > Integer.MAX_VALUE) throw new IOException();
		ByteBuffer buffer = ByteBuffer.allocate((int) ic.size());
		ic.read(buffer);
		return buffer.array();
	}

	static void ba2fc(byte[] bytes, @NonNull FileChannel oc) throws IOException, NonWritableChannelException {
		oc.write(ByteBuffer.wrap(bytes));
		try {
			oc.truncate(bytes.length);
			oc.force(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void fc2fc(@NonNull FileChannel ic, @NonNull FileChannel oc) throws IOException, NonReadableChannelException, NonWritableChannelException {
		long len = ic.size();
		ic.transferTo(0, len, oc);
		try {
			oc.truncate(len);
			ic.force(true);
			oc.force(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static Drawable svg2bmp(@NonNull Drawable d, int b, Resources cRes) {
		Bitmap bitmap = Bitmap.createBitmap(b, b, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		d.draw(canvas);
		return new BitmapDrawable(cRes, bitmap);
	}

	static void trimDB140(Context context, JSONObject db) {
		try {
			JSONArray wl = db.optJSONArray(DB_KEY_WIKI);
			if (wl == null) return;
			JSONObject wl2 = new JSONObject();
			for (int i = 0; i < wl.length(); i++) {
				JSONObject wiki = new JSONObject(), w0 = wl.optJSONObject(i);
				if (w0 == null) continue;
				wiki.put(KEY_NAME, w0.optString(KEY_NAME, KEY_TW))
						.put(DB_KEY_SUBTITLE, w0.optString(DB_KEY_SUBTITLE))
						.put(DB_KEY_URI, Uri.fromFile(new File(w0.optString(DB_KEY_PATH))).toString());
				wl2.put(w0.optString(KEY_ID, genId()), wiki);
			}
			db.remove(DB_KEY_WIKI);
			db.put(DB_KEY_WIKI, wl2);
			writeJson(context, db);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@NonNull
	private static String getVersion(Context context) {
		if (version != null) return version;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return version = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return STR_EMPTY;
	}

	static boolean isDebug(Context context) {
		if (!isDebug) return false;
		try {
			ApplicationInfo info = context.getApplicationInfo();
			return (isDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
		} catch (Exception e) {
			return false;
		}
	}

	interface OnFirstRun {
		void onAgreed();

		void onDeclined();
	}

	static void firstRunReq(Activity context, OnFirstRun cb) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		int dialogPadding2 = (int) (context.getResources().getDisplayMetrics().density * 12),
				dialogPadding3 = (int) (context.getResources().getDisplayMetrics().density * 24);
		layout.setPaddingRelative(dialogPadding3, dialogPadding2, dialogPadding3, 0);
		TextView lbl1 = new TextView(context);
		lbl1.setText(R.string.agreements_desc1);
		lbl1.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		layout.addView(lbl1);
		LinearLayout.LayoutParams agl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		TextView ag1 = new TextView(context);
		ag1.setLayoutParams(agl);
		ag1.setPadding(4, 0, 4, 0);
		StringBuffer sb = new StringBuffer();
		try (InputStream is = context.getAssets().open(LICENSE_FILE_NAME);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) {
			sb.append(br.readLine());
			String line;
			while ((line = br.readLine()) != null) {
				sb.append("\n").append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		ag1.setText(sb);
		ag1.setHorizontallyScrolling(true);
		ag1.setMovementMethod(ScrollingMovementMethod.getInstance());
		ag1.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		ag1.setTypeface(Typeface.MONOSPACE);
		ag1.setTextSize(12);
		ag1.setBackgroundColor(context.getResources().getColor(R.color.content_back_dec));
		ScrollView agc1 = new ScrollView(context);
		LinearLayout.LayoutParams agl1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (context.getResources().getDisplayMetrics().density * 80));
		agc1.setLayoutParams(agl1);
		agc1.addView(ag1);
		layout.addView(agc1);
		TextView lbl2 = new TextView(context);
		lbl2.setText(R.string.agreements_desc2);
		lbl2.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		layout.addView(lbl2);
		TextView ag2 = new TextView(context);
		ag2.setLayoutParams(agl);
		ag2.setPadding(4, 0, 4, 0);
		ag2.setText(R.string.agreements_privacy);
		ag2.setHorizontallyScrolling(true);
		ag2.setMovementMethod(ScrollingMovementMethod.getInstance());
		ag2.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		ag2.setTypeface(Typeface.MONOSPACE);
		ag2.setTextSize(12);
		ag2.setBackgroundColor(context.getResources().getColor(R.color.content_back_dec));
		ScrollView agc2 = new ScrollView(context);
		LinearLayout.LayoutParams agl2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (context.getResources().getDisplayMetrics().density * 40));
		agc2.setLayoutParams(agl2);
		agc2.addView(ag2);
		layout.addView(agc2);
		TextView lbl3 = new TextView(context);
		lbl3.setText(R.string.agreements_desc3);
		lbl3.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		layout.addView(lbl3);
		AlertDialog firstRunDialog = new AlertDialog.Builder(context)
				.setTitle(R.string.agreements_title)
				.setView(layout)
				.setPositiveButton(R.string.agreements_accept, (dialog, which) -> cb.onAgreed())
				.setNegativeButton(R.string.agreements_decline, (dialog, which) -> cb.onDeclined())
				.create();
		firstRunDialog.setCanceledOnTouchOutside(false);
		firstRunDialog.show();
	}
}