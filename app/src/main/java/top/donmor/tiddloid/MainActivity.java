/*
 * top.donmor.tiddloid.MainActivity <= [P|Tiddloid]
 * Last modified: 22:52:05 2022/09/10
 * Copyright (c) 2022 donmor
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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.thegrizzlylabs.sardineandroid.impl.SardineException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import top.donmor.tiddloid.utils.TLSSocketFactory;

public class MainActivity extends AppCompatActivity {
	private TextView noWiki;
	private WikiListAdapter wikiListAdapter;
	private JSONObject db;
	private ActivityResultLauncher<Intent> getChooserClone, getChooserCreate, getChooserImport, getChooserTree, getPermissionRequest;
	private boolean acquiringStorage = false;
	private int dialogPadding;
	private static boolean firstRun = false;

	// CONSTANT
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
			DB_KEY_URI = "uri",
			DB_KEY_DAV_AUTH = "dav_username",
			DB_KEY_DAV_TOKEN = "dav_password",
			DB_KEY_SUBTITLE = "subtitle",
			DB_KEY_BACKUP = "backup",
			KEY_EX_HTML = ".html",
			KEY_EX_HTM = ".htm",
			KEY_EX_HTA = ".hta",
			KEY_FN_INDEX = "index.html",
			KEY_FN_INDEX2 = "index.htm",
			KEY_FD_R = "r",
			KEY_FD_W = "w",
			KEY_SLASH = "/",
			KEY_URI_NOTCH = "://",
			MASK_SDF_BACKUP = "yyyyMMddHHmmssSSS",
			SCH_CONTENT = "content",
			SCH_FILE = "file",
			SCH_HTTP = "http",
			SCH_HTTPS = "https",
			STR_EMPTY = "",
			TYPE_HTA = "application/hta",
			TYPE_HTML = "text/html";
	private static final String
			DB_FILE_NAME = "data.json",
			DB_KEY_PATH = "path",
			KEY_PATCH1 = "</html>\n",    // Random char workaround
			KEY_URI_RATE = "market://details?id=",
			LICENSE_FILE_NAME = "LICENSE",
			SCH_PACKAGES = "package",
			TEMPLATE_FILE_NAME = "template.html",
			CLONING_FILE_NAME = "cloning.html";
	static final boolean APIOver21 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP,
			APIOver23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M,
			APIOver24 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
			APIOver26 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
			APIOver29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
	static final String
			EXCEPTION_JSON_DATA_ERROR = "JSON data file corrupted",
			EXCEPTION_DOCUMENT_IO_ERROR = "Document IO Error",
			EXCEPTION_FILE_NOT_FOUND = "File not present",
			EXCEPTION_TREE_INDEX_NOT_FOUND = "File index.htm(l) not present",
			EXCEPTION_TREE_NOT_A_DIRECTORY = "File passed in is not a directory",
			EXCEPTION_SAF_FILE_NOT_EXISTS = "Chosen file no longer exists";
	private static final String
			EXCEPTION_JSON_ID_NOT_FOUND = "Cannot find this id in the JSON data file",
			EXCEPTION_SHORTCUT_NOT_SUPPORTED = "Invoking a function that is not supported by the current system",
			EXCEPTION_NO_INTERNET = "No Internet connection",
			EXCEPTION_INTERRUPTED = "Interrupted by user",
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
			@SuppressLint({"QueryPermissionsNeeded", "NotifyDataSetChanged"})
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
				final boolean iDav = wa.has(DB_KEY_DAV_AUTH), iNet = !iDav && (SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme())), legacy = SCH_FILE.equals(u.getScheme());
				final Sardine davClient;
				if (iDav) {
					provider = getString(R.string.webdav);
					path = u.toString();
					davClient = new OkHttpSardine();
					davClient.setCredentials(wa.optString(DB_KEY_DAV_AUTH), wa.optString(DB_KEY_DAV_TOKEN));
				} else if (iNet) {
					provider = getString(R.string.internet);
					path = u.toString();
					davClient = null;
				} else if (legacy) {
					provider = getString(R.string.local_legacy);
					path = u.getPath();
					davClient = null;
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
					davClient = null;
				}
				// 显示属性
				textWikiInfo.setText(new StringBuilder(getString(R.string.provider))
						.append(provider)
						.append('\n')
						.append(getString(R.string.pathDir))
						.append(path));
				final CheckBox cbDefault = view.findViewById(R.id.cbDefault), cbBackup = view.findViewById(R.id.cbBackup);
				try {
					cbDefault.setChecked(id.equals(db.optString(DB_KEY_DEFAULT)));
					cbBackup.setChecked(wa.getBoolean(DB_KEY_BACKUP));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				cbDefault.setOnCheckedChangeListener((compoundButton, b) -> {
					try {
						if (b) db.put(DB_KEY_DEFAULT, id);
						else db.remove(DB_KEY_DEFAULT);
						writeJson(MainActivity.this, db);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				});
				cbDefault.setEnabled(!iNet);
				cbBackup.setEnabled(!iNet);
				final ConstraintLayout frmBackupList = view.findViewById(R.id.frmBackupList);
				if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE);
				else frmBackupList.setVisibility(View.GONE);
				final TextView lblNoBackup = view.findViewById(R.id.lblNoBackup);
				RecyclerView rvBackupList = view.findViewById(R.id.rvBackupList);
				rvBackupList.setLayoutManager(new LinearLayoutManager(view.getContext()));
				// 读图标
				Drawable icon = null;
				byte[] b = Base64.decode(wa.optString(KEY_FAVICON), Base64.NO_PADDING);
				final Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
				if (APIOver21) {
					if (favicon != null) icon = new BitmapDrawable(getResources(), favicon);
					else
						icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_description);
				}
				final Uri tu;
				Uri tu1 = null;
				DocumentFile mdf = null, df = null;
				if (APIOver21 && !legacy) try {
					mdf = DocumentFile.fromTreeUri(MainActivity.this, u);
					DocumentFile p;
					if (mdf == null || !mdf.isDirectory()) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
					df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
					if (df == null || !df.isFile()) throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
					tu1 = df.getUri();
				} catch (IllegalArgumentException ignored) {
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					tu1 = Uri.parse(STR_EMPTY);
					mdf = DocumentFile.fromTreeUri(MainActivity.this, u);
					if (mdf != null && mdf.isDirectory()) {
						DocumentFile[] ep = mdf.listFiles();
						for (DocumentFile ef : ep)
							if (ef.isDirectory() && ef.getName() != null
									&& (ef.getName().endsWith(KEY_EX_HTML + BACKUP_POSTFIX) || ef.getName().endsWith(KEY_EX_HTM + BACKUP_POSTFIX) || ef.getName().endsWith(KEY_EX_HTA + BACKUP_POSTFIX))) {
								DocumentFile vf = mdf.createFile(TYPE_HTML, ef.getName().substring(0, ef.getName().length() - BACKUP_POSTFIX.length()));
								tu1 = vf != null ? vf.getUri() : Uri.parse(STR_EMPTY);
								if (vf != null) {
									vf.delete();
								}
								break;
							}
					}
				} catch (IOException e) {
					e.printStackTrace();
					tu1 = Uri.parse(STR_EMPTY);
				}
				tu = tu1;    // 非null时为目录模式
				// 构建dialog
				final AlertDialog wikiConfigDialog = new AlertDialog.Builder(MainActivity.this)
						.setTitle(name)
						.setIcon(icon)
						.setView(view)
						.setPositiveButton(R.string.remove_wiki, (dialog, which) -> {
							dialog.dismiss();
							View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.del_confirm, null);
							final CheckBox cbDelFile = view1.findViewById(R.id.cbDelFile);
							final CheckBox cbDelBackups = view1.findViewById(R.id.cbDelBackups);
							cbDelFile.setEnabled(!iNet);
							cbDelBackups.setEnabled(false);
							cbDelFile.setOnCheckedChangeListener((buttonView, isChecked) -> cbDelBackups.setEnabled(isChecked));
							@SuppressLint("NotifyDataSetChanged") AlertDialog removeWikiConfirmationDialog = new AlertDialog.Builder(MainActivity.this)
									.setTitle(android.R.string.dialog_alert_title)
									.setMessage(R.string.confirm_to_remove_wiki)
									.setView(view1)
									.setNegativeButton(android.R.string.cancel, null)
									.setPositiveButton(android.R.string.ok, (dialog1, which1) -> {
										removeWiki(id, cbDelFile.isChecked(), cbDelBackups.isChecked(), davClient);
										if (!APIOver21) wikiListAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
										else wikiListAdapter.notifyItemRemoved(pos);
									})
									.create();
							removeWikiConfirmationDialog.setOnShowListener(dialog1 -> removeWikiConfirmationDialog.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
							removeWikiConfirmationDialog.show();
						})
						.setNegativeButton(R.string.clone_wiki, (dialog, which) -> {
							dialog.dismiss();
							try {
								Uri u1 = tu != null ? tu : u;
								File dest = new File(getCacheDir(), CLONING_FILE_NAME);
								dest.createNewFile();
								if (iDav) {
									final IOException[] e0 = new IOException[1];
									Thread jt = new Thread(() -> {
										String uf = u.toString();
										try {
											if (!davClient.exists(uf)) throw new FileNotFoundException(EXCEPTION_SAF_FILE_NOT_EXISTS);
											List<DavResource> fl = davClient.list(uf);
											String index = null;
											if (fl.get(0).isDirectory()) {
												for (DavResource f : fl) if (KEY_FN_INDEX.equals(index = f.getName()) || KEY_FN_INDEX2.equals(index)) break;
												if (index == null) throw new FileNotFoundException(EXCEPTION_SAF_FILE_NOT_EXISTS);
												uf = !uf.endsWith(KEY_SLASH) ? uf + KEY_SLASH : uf;
												uf = uf + index;
											}
											try (InputStream is = davClient.get(uf);
													OutputStream os = getContentResolver().openOutputStream(Uri.fromFile(dest))) {
												int length;
												byte[] bytes = new byte[BUF_SIZE];
												while ((length = is.read(bytes)) > -1) {
													os.write(bytes, 0, length);
												}
												os.flush();
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
								} else try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(u1, KEY_FD_R));
										ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(Uri.fromFile(dest), KEY_FD_W));
										FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
										FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
										FileChannel ic = is.getChannel();
										FileChannel oc = os.getChannel()) {
									fc2fc(ic, oc);
								}
								getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
							} catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
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
								Intent in = new Intent(MainActivity.this, TWEditorWV.class).putExtras(bu).setAction(Intent.ACTION_MAIN);
								if (ShortcutManagerCompat.isRequestPinShortcutSupported(MainActivity.this)) {
									ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(MainActivity.this, id)
											.setShortLabel(name)
											.setLongLabel(name + (sub.length() > 0 ? KEY_LBL + sub : sub))
											.setIcon(favicon != null ? IconCompat.createWithBitmap(favicon) : IconCompat.createWithResource(MainActivity.this, APIOver21 ? R.drawable.ic_shortcut : R.mipmap.ic_shortcut))
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
				wikiConfigDialog.setOnShowListener(dialog -> wikiConfigDialog.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
				// 备份系统
				final BackupListAdapter backupListAdapter = new BackupListAdapter(wikiConfigDialog.getContext());
				DocumentFile finalDf = df;
				DocumentFile finalMdf = mdf;
				backupListAdapter.setOnBtnClickListener((pos1, which) -> {
					final File bf = !iDav && tu == null ? backupListAdapter.getBackupFile(pos1) : null;
					final DocumentFile bdf = !iDav && tu != null ? backupListAdapter.getBackupDF(pos1) : null;
					final String bku = iDav ? backupListAdapter.getBackupDavUri(pos1) : null;
					String bdn = bdf != null && bdf.getParentFile() != null ? bdf.getParentFile().getName() : null, rfn = bdn != null ? bdn.substring(0, bdn.length() - BACKUP_POSTFIX.length()) : null;
					if (iDav || bf != null && bf.isFile() || bdf != null && bdf.isFile())
						switch (which) {
							case 1:        // 回滚
								AlertDialog confirmRollback = new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_rollback)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which12) -> {
											try {
												backup(MainActivity.this, u, davClient);
											} catch (IOException e) {
												e.printStackTrace();
											}
											if (iDav) {
												Thread jt = new Thread(() -> {
													String sru = null, lt = null;
													try {
														if (davClient.exists(u.toString()) && davClient.list(u.toString()).get(0).isDirectory())
															sru = u + (bku.charAt(bku.length() - 1) == 'l' ? KEY_FN_INDEX : KEY_FN_INDEX2);
														else sru = u.toString();
														try (InputStream is = davClient.get(bku);
																ByteArrayOutputStream os = new ByteArrayOutputStream()) {
															int length;
															byte[] bytes = new byte[BUF_SIZE];
															while ((length = is.read(bytes)) > -1) {
																os.write(bytes, 0, length);
															}
															os.flush();
															lt = davClient.lock(sru);
															davClient.put(sru, os.toByteArray(), TYPE_HTML);
														}
														runOnUiThread(() -> {
															wikiConfigDialog.dismiss();
															Toast.makeText(MainActivity.this, R.string.wiki_rolled_back_successfully, Toast.LENGTH_SHORT).show();
															loadPage(id);
														});
													} catch (IOException e) {
														e.printStackTrace();
														runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.failed_writing_file, Toast.LENGTH_SHORT).show());
													} finally {
														try {
															if (lt != null) davClient.unlock(sru, lt);
														} catch (IOException e) {
															e.printStackTrace();
														}
													}
												});
												jt.start();
												try {
													jt.join();
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											} else {
												DocumentFile fdf = tu != null && finalDf == null && finalMdf != null && rfn != null ? finalMdf.createFile(TYPE_HTML, rfn) : finalDf;
												try (ParcelFileDescriptor ifd = Objects.requireNonNull(getContentResolver().openFileDescriptor(tu != null ? bdf.getUri() : Uri.fromFile(bf), KEY_FD_R));
														ParcelFileDescriptor ofd = Objects.requireNonNull(getContentResolver().openFileDescriptor(tu != null ? Objects.requireNonNull(fdf).getUri() : u, KEY_FD_W));
														FileInputStream is = new FileInputStream(ifd.getFileDescriptor());
														FileOutputStream os = new FileOutputStream(ofd.getFileDescriptor());
														FileChannel ic = is.getChannel();
														FileChannel oc = os.getChannel()) {    // 由于SAF限制，文件模式无法还原丢失的文件，除非在原位创建一个文件，即使该文件是空的
													fc2fc(ic, oc);
													wikiConfigDialog.dismiss();
													Toast.makeText(MainActivity.this, R.string.wiki_rolled_back_successfully, Toast.LENGTH_SHORT).show();
													loadPage(id);
												} catch (IOException | NullPointerException | NonReadableChannelException | NonWritableChannelException e) {
													e.printStackTrace();
													Toast.makeText(MainActivity.this, R.string.failed_writing_file, Toast.LENGTH_SHORT).show();
												}
											}
										})
										.create();
								confirmRollback.setOnShowListener(dialog1 -> confirmRollback.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
								confirmRollback.show();
								break;
							case 2:        // 移除备份
								@SuppressLint("NotifyDataSetChanged") AlertDialog confirmDelBackup = new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_del_backup)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which1) -> {
											try {
												if (iDav) {
													final IOException[] e0 = new IOException[1];
													Thread jt = new Thread(() -> {
														try {
															davClient.delete(bku);
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
												} else if (bf != null && bf.delete() || bdf != null && DocumentsContract.deleteDocument(getContentResolver(), bdf.getUri()))
													Toast.makeText(wikiConfigDialog.getContext(), R.string.backup_deleted, Toast.LENGTH_SHORT).show();
												else
													throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
												backupListAdapter.reload(u, davClient);
												if (!APIOver21) backupListAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
												else backupListAdapter.notifyItemRemoved(pos1);
											} catch (IOException e) {
												e.printStackTrace();
												Toast.makeText(wikiConfigDialog.getContext(), R.string.failed_deleting_file, Toast.LENGTH_SHORT).show();
											}
										}).create();
								confirmDelBackup.setOnShowListener(dialog1 -> confirmDelBackup.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
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
					backupListAdapter.reload(u, davClient);
				} catch (IOException e) {
					e.printStackTrace();
				}
				rvBackupList.setAdapter(backupListAdapter);
				rvBackupList.setItemAnimator(new DefaultItemAnimator());
				cbBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
					try {
						wa.put(DB_KEY_BACKUP, isChecked);
						writeJson(MainActivity.this, db);
						frmBackupList.setVisibility(cbBackup.isChecked() ? View.VISIBLE : View.GONE);
						if (isChecked) {
							backupListAdapter.reload(u, davClient);
							if (APIOver21) backupListAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
							else rvBackupList.setAdapter(backupListAdapter);
						}
					} catch (IOException | JSONException e) {
						e.printStackTrace();
						Toast.makeText(wikiConfigDialog.getContext(), R.string.data_error, Toast.LENGTH_SHORT).show();
					}
				});
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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())
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
					writeJson(MainActivity.this, db);
					firstRun = true;
				} catch (JSONException e1) {
					e1.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					finish();
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
			if (APIOver21) wikiListAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
			else runOnUiThread(() -> rvWikiList.setAdapter(wikiListAdapter));
			runOnUiThread(() -> noWiki.setVisibility(wikiListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE));
			while ((System.nanoTime() - time0) / 1000000 < 1000) try {
				//noinspection BusyWait
				Thread.sleep(1);
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
					firstRunReq(this);
			});
			batchFix(MainActivity.this);
		}).start();
	}

	private interface OnGetSrc {
		void run(File file);
	}

	private static InputStream getAdaptiveUriInputStream(Uri uri, final long[] lastModified) throws NetworkErrorException, InterruptedIOException {
		try {
			HttpsURLConnection httpURLConnection;
			URL url = new URL(uri.normalizeScheme().toString());
			httpURLConnection = (HttpsURLConnection) url.openConnection();
			if (!APIOver21)
				httpURLConnection.setSSLSocketFactory(new TLSSocketFactory());
			httpURLConnection.connect();
			lastModified[0] = httpURLConnection.getLastModified();
			return httpURLConnection.getInputStream();
		} catch (InterruptedIOException e) {
			throw e;
		} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
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
			}
			dest.setLastModified(lastModified[0]);
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
		final Thread thread = new Thread(() -> fetchInThread(file -> runOnUiThread(() -> cb.run(file)), progressDialog));
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
			Intent in = new Intent().putExtras(bu).setClass(this, TWEditorWV.class);
			startActivity(in);
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void removeWiki(String id, boolean del, boolean delBackup, Sardine davClient) {
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
			JSONObject xw = (JSONObject) wl.remove(id);
			writeJson(this, db);
			purgeDir(new File(getCacheDir(), id));
			if (del && xw != null) {
				String um = xw.optString(DB_KEY_URI);
				if (um.length() == 0) return;
				Uri u = Uri.parse(um);
				boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
				DocumentFile df, mdf = null;
				if (APIOver21 && !legacy && davClient == null) try {
					mdf = DocumentFile.fromTreeUri(this, u);
					tree = true;
				} catch (IllegalArgumentException ignored) {
				}
				final File f;
				if (davClient != null) {
					f = null;
					final IOException[] e0 = new IOException[1];
					String ux = u.toString();
					Uri finalU = u;
					Thread jt = new Thread(() -> {
						try {
							List<DavResource> root = davClient.exists(ux) ? davClient.list(ux) : null, bdr = null;
							String bdu = null;
							ArrayList<String> uf = new ArrayList<>();
							if (root != null && root.get(0).isDirectory()) {
								String index = null;
								root.remove(0);
								for (DavResource f0 : root)
									if (KEY_FN_INDEX.equals(f0.getName()) || KEY_FN_INDEX2.equals(f0.getName())) {
										index = f0.getName();
										if (delBackup) {
											bdu = ux + f0.getName() + BACKUP_POSTFIX + KEY_SLASH;
											if (davClient.exists(bdu) && !(bdr = davClient.list(bdu)).get(0).isDirectory()) bdr = null;
											if (bdr != null) bdr.remove(0);
										}
										break;
									}
								if (index == null && delBackup) for (DavResource f0 : root)
									if (f0.getName().equals(KEY_FN_INDEX + BACKUP_POSTFIX) || f0.getName().equals(KEY_FN_INDEX2 + BACKUP_POSTFIX) && f0.isDirectory()) {
										bdu = ux + (f0.getName().charAt(f0.getName().length() - 1) == '/' ? f0.getName() : f0.getName() + KEY_SLASH);
										bdr = davClient.list(bdu);
										bdr.remove(0);
										break;
									}
								if (delBackup && bdu != null && bdr != null)
									for (DavResource i : bdr) if (isBackupFile(index, i.getName())) uf.add(bdu + i.getName());
								if (index != null && davClient.exists(ux + index)) uf.add(ux + index);
							} else {
								if (delBackup) {
									String r0 = ux.substring(0, ux.lastIndexOf('/') + 1);
									List<DavResource> rx = davClient.exists(r0) ? davClient.list(r0) : null;
									if (rx != null) rx.remove(0);
									if (rx != null) for (DavResource f0 : rx)
										if (f0.getName().equals(finalU.getLastPathSegment() + BACKUP_POSTFIX) && f0.isDirectory()) {
											bdu = r0 + f0.getName() + KEY_SLASH;
											if (davClient.exists(bdu) && !(bdr = davClient.list(bdu)).get(0).isDirectory()) bdr = null;
											if (bdr != null) bdr.remove(0);
											break;
										}
									if (bdu != null && bdr != null)
										for (DavResource i : bdr)
											if (isBackupFile(finalU.getLastPathSegment(), i.getName()))
												uf.add(bdu + i.getName());
								}
								if (root != null) uf.add(ux);
							}
							for (String n : uf) {
								davClient.delete(n);
								if (bdu != null && davClient.exists(bdu) && davClient.list(bdu).size() == 1) {
									davClient.delete(bdu);
								}
							}
						} catch (IndexOutOfBoundsException e) {
							e.printStackTrace();
							e0[0] = new IOException(e.getMessage());
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
				} else if (tree) {
					f = null;
					DocumentFile p;
					if (mdf == null || !mdf.isDirectory())
						throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
					df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
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
				} else if (!SCH_HTTP.equals(u.getScheme()) && !SCH_HTTPS.equals(u.getScheme())) {
					f = legacy ? new File(u.getPath()) : new File(new File(getExternalFilesDir(null), Uri.encode(u.getSchemeSpecificPart())), (df = DocumentFile.fromSingleUri(this, u)) != null && df.getName() != null ? df.getName() : KEY_FN_INDEX);    // real file in the dest dir for file:// or virtual file in ext files dir for content://
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
				if (davClient == null && legacy) f.delete();
				else if (davClient == null) DocumentsContract.deleteDocument(getContentResolver(), u);
				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
			}
			wikiListAdapter.reload(db);
		} catch (IOException | JSONException | UnsupportedOperationException e) {
			e.printStackTrace();
		}
	}

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
		if (!APIOver21) {
			menu.getItem(2).setEnabled(false);
			menu.getItem(3).setTitle(R.string.local_legacy);    // API19暂不支持WebDAV
			menu.getItem(3).setIcon(R.drawable.ic_storage);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		final int idNew = R.id.action_new,
				idImport = R.id.action_file_import,
				idDir = R.id.action_add_dir,
				idDav = R.id.action_add_dav,
				idAbout = R.id.action_about;
		switch (id) {
			case idNew:
				getChooserCreate.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
				break;
			case idImport:
				getChooserImport.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML).putExtra(Intent.EXTRA_MIME_TYPES, TYPE_FILTERS));
				break;
			case idDir:
				if (APIOver21)
					getChooserTree.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
				break;
			case idDav:
				browseDav();
				break;
			case idAbout:
				SpannableString spannableString = new SpannableString(getString(R.string.about));
				Linkify.addLinks(spannableString, Linkify.ALL);
				AlertDialog aboutDialog = new AlertDialog.Builder(this)
						.setTitle(getString(R.string.about_title, BuildConfig.VERSION_NAME))
						.setMessage(spannableString)
						.setPositiveButton(android.R.string.ok, null)
						.setNeutralButton(R.string.market, (dialog, which) -> {
							try {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(KEY_URI_RATE + getPackageName())));
							} catch (ActivityNotFoundException e) {
								e.printStackTrace();
							}
						}).create();
				aboutDialog.setOnShowListener(dialog -> aboutDialog.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
				aboutDialog.show();
				((TextView) aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
				if (APIOver23)
					((TextView) aboutDialog.findViewById(android.R.id.message)).setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NotifyDataSetChanged")
	private void browseDav() {
		if (!APIOver21) {    // API19暂不支持WebDAV
			browseLocal();
			return;
		}
		@SuppressLint("InflateParams") View davView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dav_dialog, null);
		ImageButton btnDavGo = davView.findViewById(R.id.btnDavGo),
				btnDavLegacy = davView.findViewById(R.id.btnDavLegacy);
		EditText textDavAddress = davView.findViewById(R.id.editTextDavAddress),
				textDavUser = davView.findViewById(R.id.editTextDavUser),
				textDavToken = davView.findViewById(R.id.editTextDavPassword);
		textDavAddress.setOnEditorActionListener((textView, i, keyEvent) -> {
			if (i == EditorInfo.IME_ACTION_GO && btnDavGo.isEnabled()) btnDavGo.callOnClick();
			return true;
		});
		ProgressBar bar = davView.findViewById(R.id.progressDav);
		class NetCtrl {
			private Thread thread = null;
		}
		final NetCtrl netCtrl = new NetCtrl();
		StringBuffer u = new StringBuffer(), pdu = new StringBuffer();
		final Sardine davClient = new OkHttpSardine();
		AlertDialog davDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.webdav)
				.setView(davView)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, null)
				.setOnDismissListener(dialogInterface -> {
					if (netCtrl.thread != null && netCtrl.thread.isAlive()) netCtrl.thread.interrupt();
				})
				.create();
		davDialog.setOnShowListener(dialog -> davDialog.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
		RecyclerView davDir = davView.findViewById(R.id.rvDavDir);
		davDir.setLayoutManager(new LinearLayoutManager(this));
		DavDirAdapter davDirAdapter = new DavDirAdapter(MainActivity.this);
		davDirAdapter.setOnItemClickListener(new DavDirAdapter.ItemClickListener() {
			@Override
			public void onItemClick(String dir) {
				textDavAddress.setText(dir);
				btnDavGo.callOnClick();
			}

			@Override
			public void onBackClick() {
				textDavAddress.setText(pdu.toString());
				btnDavGo.callOnClick();
			}
		});
		DavDirAdapter.PathMon mPathMon = h -> {
			Editable e;
			if (h.strPath == null || (e = textDavAddress.getText()) == null) {
				h.btnDavItem.setPressed(false);
				return;
			}
			h.btnDavItem.setPressed(h.strPath.equals(e.toString()));
		};
		davDirAdapter.setPathMon(mPathMon);
		textDavAddress.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				String http = SCH_HTTP + KEY_URI_NOTCH, https = SCH_HTTPS + KEY_URI_NOTCH, u = editable.toString();
				boolean valid = u.startsWith(http) && !u.equals(http) || u.startsWith(https) && !u.equals(https);
				davDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(valid);
				btnDavGo.setEnabled(valid);
				DavDirAdapter adapter = (DavDirAdapter) davDir.getAdapter();
				if (adapter == null) return;
				for (int i = 0; i < adapter.getItemCount(); i++) {
					DavDirAdapter.DavDirHolder holder = (DavDirAdapter.DavDirHolder) davDir.findViewHolderForAdapterPosition(i);
					if (holder != null)
						mPathMon.checkPath(holder);
				}
			}
		});
		davDir.setAdapter(davDirAdapter);
		davDir.setItemAnimator(new DefaultItemAnimator());
		btnDavGo.setOnClickListener(view -> {
			u.replace(0, u.length(), textDavAddress.getText().toString());
			pdu.replace(0, pdu.length(), u.substring(0, u.lastIndexOf(KEY_SLASH, u.length() - 2) + 1));
			davClient.setCredentials(textDavUser.getText().toString(), textDavToken.getText().toString());
			if (netCtrl.thread != null && netCtrl.thread.isAlive()) netCtrl.thread.interrupt();
			netCtrl.thread = new Thread(() -> {
				try {
					runOnUiThread(() -> {
						bar.setVisibility(View.VISIBLE);
						davDirAdapter.reload(null, null, false);
						davDirAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
					});
					String ux = u.toString();
					if (davClient.exists(ux)) {
						List<DavResource> cd = davClient.list(ux);
						cd.remove(0);    // 移除cwd
						boolean canCdP = false;
						try {
							canCdP = davClient.exists(pdu.toString());
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						boolean finalCanCdP = canCdP;
						runOnUiThread(() -> {
							URI uri = URI.create(ux);
							davDirAdapter.reload(cd, uri.getScheme() + KEY_URI_NOTCH + uri.getAuthority(), finalCanCdP);
							davDirAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
							bar.setVisibility(View.GONE);
						});
					} else throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
				} catch (InterruptedIOException ignored) {
				} catch (SardineException e0) {
					e0.printStackTrace();
					runOnUiThread(() -> {
						Toast.makeText(MainActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
						davDirAdapter.reload(null, null, false);
						davDirAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
						bar.setVisibility(View.GONE);
					});
				} catch (IOException | IllegalArgumentException e) {
					runOnUiThread(() -> {
						Toast.makeText(this, R.string.no_file, Toast.LENGTH_SHORT).show();
						davDirAdapter.reload(null, null, false);
						davDirAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
						bar.setVisibility(View.GONE);
					});
				}
			});
			netCtrl.thread.start();
		});
		davDialog.show();
		davDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener((dialogInterface) -> {
			u.replace(0, u.length(), textDavAddress.getText().toString());
			pdu.replace(0, pdu.length(), u.substring(0, u.lastIndexOf(KEY_SLASH, u.length() - 2) + 1));
			davClient.setCredentials(textDavUser.getText().toString(), textDavToken.getText().toString());
			if (netCtrl.thread != null && netCtrl.thread.isAlive()) netCtrl.thread.interrupt();
			netCtrl.thread = new Thread(() -> {
				runOnUiThread(() -> bar.setVisibility(View.VISIBLE));
				String id = null, lt = null, ux = u.toString(), un = textDavUser.getText().toString(), tok = textDavToken.getText().toString(), uf = null;
				try {
					DavResource davFile = null;
					if (davClient.exists(ux) && !(davFile = davClient.list(ux, 0).get(0)).isDirectory() && !TYPE_HTML.equals(davFile.getContentType()))
						throw new IOException();
					if (davFile != null && davFile.isDirectory() && !ux.endsWith(KEY_SLASH)) ux = ux + KEY_SLASH;
					else if (davFile != null) {
						ux = ux.endsWith(KEY_SLASH) ? ux.substring(0, ux.length() - 1) : ux;
						ux = ux.endsWith(KEY_EX_HTML) || ux.endsWith(KEY_EX_HTM) || ux.endsWith(KEY_EX_HTA) ? ux : ux + KEY_EX_HTML;
					}
					JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa = null;
					boolean exist = false;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						exist = ux.equals((wa = wl.getJSONObject(id = iterator.next())).optString(DB_KEY_URI)) && un.equals(wa.optString(DB_KEY_DAV_AUTH));
						if (exist) break;
					}
					if (exist) runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show());
					else {
						wa = new JSONObject();
						id = genId();
						wa.put(KEY_NAME, KEY_TW);
						wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
						wa.put(DB_KEY_URI, ux);
						wa.put(DB_KEY_BACKUP, false);
						wa.put(DB_KEY_DAV_AUTH, un);
						wl.put(id, wa);
					}
					wa.put(DB_KEY_DAV_TOKEN, tok);
					if (!davClient.exists(ux)) {
						final File[] f0 = new File[1];
						fetchInThread(file -> f0[0] = file, null);
						if (f0[0].exists()) {
							lt = davClient.lock(uf = ux);
							davClient.put(uf, f0[0], TYPE_HTML);
						} else {
							Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
							throw new IOException();
						}
					} else if (davFile != null && davFile.isDirectory()) {
						boolean haveIndex = false;
						for (DavResource f : davClient.list(ux)) {
							if (KEY_FN_INDEX.equals(f.getName()) || KEY_FN_INDEX2.equals(f.getName())) {
								haveIndex = true;
								break;
							}
						}
						if (!haveIndex) {
							final File[] f0 = new File[1];
							fetchInThread(file -> f0[0] = file, null);
							if (f0[0].exists()) {
								StringBuilder buffer = new StringBuilder(ux);
								if (buffer.charAt(buffer.length() - 1) != '/') buffer.append('/');
								buffer.append(KEY_FN_INDEX);
								lt = davClient.lock(uf = buffer.toString());
								davClient.put(uf, f0[0], TYPE_HTML);
							} else {
								Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
								throw new IOException();
							}
						}
					}
					writeJson(MainActivity.this, db);
					runOnUiThread(davDialog::dismiss);
					if (!loadPage(id)) runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show());
				} catch (InterruptedIOException ignored) {
				} catch (SardineException e) {
					e.printStackTrace();
					runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show());
				} catch (IOException | IndexOutOfBoundsException e) {
					e.printStackTrace();
					runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.no_file, Toast.LENGTH_SHORT).show());
				} catch (JSONException e) {
					e.printStackTrace();
					runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show());
				} finally {
					try {
						if (lt != null) davClient.unlock(uf, lt);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			netCtrl.thread.start();
		});
		davDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		btnDavGo.setEnabled(false);
		btnDavLegacy.setOnClickListener(view -> {
			if (checkPermission(MainActivity.this)) {
				davDialog.getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();
				browseLocal();
			}
		});
	}

	@SuppressLint("NotifyDataSetChanged")
	private void browseLocal() {
		@SuppressLint("InflateParams") View localView = LayoutInflater.from(MainActivity.this).inflate(R.layout.local_dialog, null);
		ImageButton btnLocalGo = localView.findViewById(R.id.btnLocalGo),
				btnLocalDav = localView.findViewById(R.id.btnLocalDav);
		EditText textLocalAddress = localView.findViewById(R.id.editTextLocalAddress);
		textLocalAddress.setOnEditorActionListener((textView, i, keyEvent) -> {
			if (i == EditorInfo.IME_ACTION_GO && btnLocalGo.isEnabled()) btnLocalGo.callOnClick();
			return true;
		});
		class FileData {
			File f = null, pd = null;
		}
		FileData fd = new FileData();
		AlertDialog localDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.local_legacy)
				.setView(localView)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, null)
				.create();
		localDialog.setOnShowListener(dialog -> localDialog.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
		RecyclerView localDir = localView.findViewById(R.id.rvLocalDir);
		localDir.setLayoutManager(new LinearLayoutManager(this));
		LocalDirAdapter localDirAdapter = new LocalDirAdapter(MainActivity.this);
		localDirAdapter.setOnItemClickListener(new LocalDirAdapter.ItemClickListener() {
			@Override
			public void onItemClick(File dir) {
				textLocalAddress.setText(dir.getPath());
				if (dir.getPath().equals(fd.f.getPath()))
					localDialog.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick();
				else btnLocalGo.callOnClick();
			}

			@Override
			public void onBackClick() {
				if (fd.pd != null) textLocalAddress.setText(fd.pd.getPath());
				btnLocalGo.callOnClick();
			}
		});
		LocalDirAdapter.PathMon mPathMon = h -> {
			Editable e;
			if (h.strPath == null || (e = textLocalAddress.getText()) == null) {
				h.btnLocalItem.setPressed(false);
				return;
			}
			h.btnLocalItem.setPressed(h.strPath.getPath().equals(e.toString()));
		};
		localDirAdapter.setPathMon(mPathMon);
		textLocalAddress.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				String u = editable.toString();
				boolean valid = new File(u).exists();
				localDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(valid);
				btnLocalGo.setEnabled(valid);
				LocalDirAdapter adapter = (LocalDirAdapter) localDir.getAdapter();
				if (adapter == null) return;
				for (int i = 0; i < adapter.getItemCount(); i++) {
					LocalDirAdapter.LocalDirHolder holder = (LocalDirAdapter.LocalDirHolder) localDir.findViewHolderForAdapterPosition(i);
					if (holder != null)
						mPathMon.checkPath(holder);
				}
			}
		});
		localDir.setAdapter(localDirAdapter);
		localDir.setItemAnimator(new DefaultItemAnimator());
		btnLocalGo.setOnClickListener(view -> {
			File f0 = fd.f;
			fd.f = new File(textLocalAddress.getText().toString());
			fd.pd = fd.f.getParentFile();
			try {
				if (fd.f.exists()) {
					boolean d = fd.f.isDirectory();
					File[] cd = d ? fd.f.listFiles() : fd.pd.listFiles();
					if (d) fd.pd = fd.f.getParentFile();
					localDirAdapter.reload(cd != null ? Arrays.asList(cd) : null, fd.pd != null && fd.pd.isDirectory() && fd.pd.canRead());
					localDirAdapter.notifyDataSetChanged();    // Poor performance but needed by API119
				} else throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
			} catch (IOException | IllegalArgumentException | SecurityException e) {
				runOnUiThread(() -> {
					Toast.makeText(this, R.string.no_file, Toast.LENGTH_SHORT).show();
					fd.f = f0;
					fd.pd = fd.f != null ? fd.f.getParentFile() : null;
				});
			}
		});
		localDirAdapter.reload(null, false);
		localDialog.show();
		localDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener((dialogInterface) -> {
			if (fd.f.exists() && fd.f.isFile() && fd.f.getPath().equals(textLocalAddress.getText().toString())) {
				try {
					String id = null, ux = Uri.fromFile(fd.f).toString();
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
						wa.put(KEY_NAME, KEY_TW);
						wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
						wa.put(DB_KEY_URI, ux);
						wa.put(DB_KEY_BACKUP, false);
						wl.put(id, wa);
					}
					writeJson(MainActivity.this, db);
					localDialog.dismiss();
					if (!loadPage(id)) runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show());
				} catch (JSONException e) {
					e.printStackTrace();
					runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show());
				}
			} else btnLocalGo.callOnClick();
		});
		localDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		btnLocalGo.setEnabled(false);
		if (APIOver21)    // API19暂不支持WebDAV
			btnLocalDav.setOnClickListener(view -> {
				localDialog.getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();
				browseDav();
			});
		else btnLocalDav.setVisibility(View.GONE);
		textLocalAddress.setText(Environment.getExternalStorageDirectory().getPath());
		btnLocalGo.callOnClick();
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
						if ((wa = wl.getJSONObject(id = iterator.next())).has(DB_KEY_DAV_AUTH)) continue;
						exist = uri.toString().equals(wa.optString(DB_KEY_URI));
						if (exist) break;
					}
					if (exist) {
						Toast.makeText(MainActivity.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
						if (wa.optBoolean(DB_KEY_BACKUP)) backup(MainActivity.this, uri, null);
					} else {
						wa = new JSONObject()
								.put(DB_KEY_URI, uri.toString())
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
				if ((wa = wl.getJSONObject(id = iterator.next())).has(DB_KEY_DAV_AUTH)) continue;
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
		DocumentFile p, index = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
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
				if ((wa = wl.getJSONObject(id = iterator.next())).has(DB_KEY_DAV_AUTH)) continue;
				exist = uri.toString().equals(wa.optString(DB_KEY_URI));
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				wa = new JSONObject();
				id = genId();
				wa.put(KEY_NAME, KEY_TW);
				wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
				wa.put(DB_KEY_URI, uri.toString());
				wa.put(DB_KEY_BACKUP, false);
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
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
				ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
			else havePerms = true;
		} else {
			if (!Environment.isExternalStorageManager()) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
						.setData(Uri.parse(SCH_PACKAGES + ':' + context.getPackageName()));
				MainActivity activity = (MainActivity) context;
				if (!activity.acquiringStorage) {
					activity.acquiringStorage = true;
					activity.getPermissionRequest.launch(intent);
				}
			} else havePerms = true;
		}
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

	static void backup(Context context, Uri u, Sardine davClient) throws IOException {
		if (SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme())) {    // WebDAV
			if (davClient == null) return;    // 忽略普通http(s)
			final IOException[] e0 = {null};
			Thread jt = new Thread(() -> {
				String sru = null, bdu = null, bku = null, lt = null, lt2 = null;
				try {
					List<DavResource> root = davClient.list(u.toString());
					if (root.get(0).isDirectory()) {
						boolean haveIndex = false;
						for (DavResource f : root) {
							if (MainActivity.KEY_FN_INDEX.equals(f.getName()) || MainActivity.KEY_FN_INDEX2.equals(f.getName())) {
								sru = u.getScheme() + MainActivity.KEY_URI_NOTCH + u.getAuthority() + f.getHref();
								bdu = sru + MainActivity.BACKUP_POSTFIX;
								bku = bdu + KEY_SLASH + new StringBuilder(f.getName()).insert(f.getName().lastIndexOf('.'), formatBackup(f.getModified().getTime()));
								haveIndex = true;
								break;
							}
						}
						if (!haveIndex) throw new FileNotFoundException(MainActivity.EXCEPTION_SAF_FILE_NOT_EXISTS);
					} else {
						DavResource f = root.get(0);
						sru = u.toString();
						bdu = sru + MainActivity.BACKUP_POSTFIX;
						bku = bdu + KEY_SLASH + new StringBuilder(f.getName()).insert(f.getName().lastIndexOf('.'), formatBackup(f.getModified().getTime()));
					}
					if (davClient.exists(bdu) && !davClient.list(bdu).get(0).isDirectory()) {
						davClient.delete(bdu);
					}
					if (!davClient.exists(bdu)) {
						lt2 = davClient.lock(bdu);
						davClient.createDirectory(bdu);
						davClient.unlock(bku, lt2);
					}
					try (InputStream is = davClient.get(sru);
							ByteArrayOutputStream os = new ByteArrayOutputStream()) {
						int length;
						byte[] bytes = new byte[BUF_SIZE];
						while ((length = is.read(bytes)) > -1) {
							os.write(bytes, 0, length);
						}
						os.flush();
						lt = davClient.lock(bku);
						davClient.put(bku, os.toByteArray(), TYPE_HTML);
					}
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					e0[0] = new IOException(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					e0[0] = e;
				} finally {
					try {
						if (lt != null) davClient.unlock(bku, lt);
						if (lt2 != null) davClient.unlock(bku, lt2);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			jt.start();
			try {
				jt.join();
			} catch (InterruptedException e) {
				throw new IOException(e.getMessage());
			}
			if (e0[0] != null) throw e0[0];
			return;
		}
		boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
		DocumentFile df = null, mdf = null;
		if (APIOver21 && !legacy) try {    // tree模式
			DocumentFile p;
			String fn;
			mdf = DocumentFile.fromTreeUri(context, u);    // 根目录
			if (mdf == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
			df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;    // index.htm(l)
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

	static byte[] fc2ba(@NonNull FileChannel ic) throws IOException, NonReadableChannelException {
		if (ic.size() > Integer.MAX_VALUE) throw new IOException();
		ByteBuffer buffer = ByteBuffer.allocate((int) ic.size());
		ic.read(buffer);
		return buffer.array();
	}

	static void ba2fc(byte[] bytes, @NonNull FileChannel oc) throws IOException, NonWritableChannelException {
		oc.write(ByteBuffer.wrap(bytes));
		oc.truncate(bytes.length);
		oc.force(true);
	}

	static void fc2fc(@NonNull FileChannel ic, @NonNull FileChannel oc) throws IOException, NonReadableChannelException, NonWritableChannelException {
		long len = ic.size();
		ic.transferTo(0, len, oc);
		oc.truncate(len);
		ic.force(true);
		oc.force(true);
	}

	static void trimDB140(Context context, JSONObject db) {
		try {
			JSONArray wl = db.optJSONArray(DB_KEY_WIKI);
			if (wl == null) return;
			JSONObject wl2 = new JSONObject();
			for (int i = 0; i < wl.length(); i++) {
				JSONObject wiki = new JSONObject(), w0 = wl.optJSONObject(i);
				if (w0 == null) continue;
				wiki.put(KEY_NAME, w0.optString(KEY_NAME, KEY_TW));
				wiki.put(DB_KEY_SUBTITLE, w0.optString(DB_KEY_SUBTITLE));
				wiki.put(DB_KEY_URI, Uri.fromFile(new File(w0.optString(DB_KEY_PATH))).toString());
				wl2.put(w0.optString(KEY_ID, genId()), wiki);
			}
			db.remove(DB_KEY_WIKI);
			db.put(DB_KEY_WIKI, wl2);
			writeJson(context, db);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	static void firstRunReq(Activity context) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		int dialogPadding2 = (int) (context.getResources().getDisplayMetrics().density * 12),
				dialogPadding3 = (int) (context.getResources().getDisplayMetrics().density * 24);
		layout.setPaddingRelative(dialogPadding3, dialogPadding2, dialogPadding3, 0);
		TextView lbl1 = new TextView(context);
		lbl1.setText(R.string.agreements_desc1);
		lbl1.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		layout.addView(lbl1);
		LinearLayout.LayoutParams agl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
		ag1.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		ag1.setTypeface(Typeface.MONOSPACE);
		ag1.setTextSize(12);
		ag1.setBackgroundColor(context.getResources().getColor(R.color.content_back_dec));
		HorizontalScrollView agh1 = new HorizontalScrollView(context);
		agh1.setHorizontalScrollBarEnabled(false);
		agh1.addView(ag1);
		ScrollView agc1 = new ScrollView(context);
		LinearLayout.LayoutParams agl1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (context.getResources().getDisplayMetrics().density * 80));
		agc1.setLayoutParams(agl1);
		agc1.addView(agh1);
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
		ag2.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		ag2.setTypeface(Typeface.MONOSPACE);
		ag2.setTextSize(12);
		ag2.setBackgroundColor(context.getResources().getColor(R.color.content_back_dec));
		HorizontalScrollView agh2 = new HorizontalScrollView(context);
		agh2.setHorizontalScrollBarEnabled(false);
		agh2.addView(ag2);
		ScrollView agc2 = new ScrollView(context);
		LinearLayout.LayoutParams agl2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (context.getResources().getDisplayMetrics().density * 40));
		agc2.setLayoutParams(agl2);
		agc2.addView(agh2);
		layout.addView(agc2);
		TextView lbl3 = new TextView(context);
		lbl3.setText(R.string.agreements_desc3);
		lbl3.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		layout.addView(lbl3);
		AlertDialog firstRunDialog = new AlertDialog.Builder(context)
				.setTitle(R.string.agreements_title)
				.setView(layout)
				.setPositiveButton(R.string.agreements_accept, null)
				.setNegativeButton(R.string.agreements_decline, (dialog, which) -> {
					File dir = context.getFilesDir(), file = new File(dir, DB_FILE_NAME);
					file.delete();
					context.finishAffinity();
					System.exit(0);
				})
				.create();
		firstRunDialog.setCanceledOnTouchOutside(false);
		firstRunDialog.show();
	}
}