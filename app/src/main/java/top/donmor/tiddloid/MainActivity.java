/*
 * top.donmor.tiddloid.MainActivity <= [P|Tiddloid]
 * Last modified: 18:18:25 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.Manifest;
import android.accounts.NetworkErrorException;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
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
			DB_KEY_WIKI = "wiki",
			DB_KEY_COLOR = "color",
			DB_KEY_URI = "uri",
			KEY_SHORTCUT = "shortcut",
			DB_KEY_SUBTITLE = "subtitle",
			DB_KEY_BACKUP = "backup",
			KEY_FN_INDEX = "index.html",
			KEY_FN_INDEX2 = "index.htm",
			MASK_SDF_BACKUP = "yyyyMMddHHmmssSSS",
			SCH_CONTENT = "content",
			SCH_FILE = "file",
			SCH_HTTP = "http",
			SCH_HTTPS = "https",
			STR_EMPTY = "",
			TYPE_HTML = "text/html";
	private static final String
			DB_FILE_NAME = "data.json",
			DB_KEY_PATH = "path",
			KEY_APPLICATION_NAME = "application-name",
			KEY_VERSION_AREA = "versionArea",
			KEY_EX_HTML = ".html",
			KEY_EX_HTM = ".htm",
			KEY_CONTENT = "content",
			KEY_URI_RATE = "market://details?id=",
			SCH_PACKAGES = "package",
			TEMPLATE_FILE_NAME = "template.html",
			CLONING_FILE_NAME = "cloning.html",
			CLASS_MENU_BUILDER = "MenuBuilder",
			METHOD_SET_OPTIONAL_ICONS_VISIBLE = "setOptionalIconsVisible";
	static final boolean APIOver21 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP,
			APIOver23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M,
			APIOver24 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
			APIOver26 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
	static final String
			EXCEPTION_DOCUMENT_IO_ERROR = "Document IO Error",
			EXCEPTION_TREE_INDEX_NOT_FOUND = "File index.htm(l) not present",
			EXCEPTION_TREE_NOT_A_DIRECTORY = "File passed in is not a directory",
			EXCEPTION_SAF_FILE_NOT_EXISTS = "Chosen file no longer exists",
			EXCEPTION_TRANSFER_CORRUPTED = "Transfer dest file corrupted: hash or size not match";
	private static final String
			EXCEPTION_JSON_DATA_ERROR = "JSON data file corrupted",
			EXCEPTION_JSON_ID_NOT_FOUND = "Cannot find this id in the JSON data file",
			EXCEPTION_SHORTCUT_NOT_SUPPORTED = "Invoking a function that is not supported by the current system",
			EXCEPTION_WIKI_NOT_AVAILABLE = "The wiki is no longer available",
			EXCEPTION_NO_INTERNET = "No Internet connection",
			EXCEPTION_INTERRUPTED = "Interrupted by user";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.activity_main);
		dialogPadding = (int) (getResources().getDisplayMetrics().density * 30);
		try {    // 加载JSON数据
			db = readJson(this);
			if (!db.has(DB_KEY_WIKI)) throw new JSONException(EXCEPTION_JSON_DATA_ERROR);
		} catch (JSONException e) {
			e.printStackTrace();
			try {
				db = initJson(this);    // 初始化JSON数据，如果加载失败
				writeJson(this, db);
			} catch (JSONException e1) {
				e1.printStackTrace();
				Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		}
		trimDB140(this, db);    // db格式转换
		// 加载UI
		onConfigurationChanged(getResources().getConfiguration());
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
		try {
			wikiListAdapter = new WikiListAdapter(this, db);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.data_error, Toast.LENGTH_SHORT).show();
			return;
		}
		rvWikiList.setAdapter(wikiListAdapter);
		wikiListAdapter.setReloadListener(count -> noWiki.setVisibility(count > 0 ? View.GONE : View.VISIBLE));
		wikiListAdapter.setOnItemClickListener(new WikiListAdapter.ItemClickListener() {
			// 点击打开
			@Override
			public void onItemClick(final int pos, final String id) {
				if (pos == -1) return;
				boolean invalidDir = false;
				try {
					JSONObject wa = db.getJSONObject(DB_KEY_WIKI).getJSONObject(id);
					Uri uri = Uri.parse(wa.optString(DB_KEY_URI)), u1;
					boolean legacy = SCH_FILE.equals(uri.getScheme());
					if (APIOver21 && !legacy) try {
						DocumentFile mdf = DocumentFile.fromTreeUri(MainActivity.this, uri), p, df;
						if (mdf == null || !mdf.isDirectory())
							throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
						df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;    // index.htm(l)
						if (df == null || !df.isFile())
							throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
						u1 = df.getUri();
					} catch (IllegalArgumentException ignored) {
						u1 = uri;
					} catch (IOException e) {
						e.printStackTrace();
						u1 = null;
						invalidDir = true;
					}
					else u1 = uri;
					if (SCH_CONTENT.equals(uri.getScheme())) try {
						getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					if (invalidDir || !isWiki(MainActivity.this, u1)) {
						AlertDialog autoRemoveConfirm = new AlertDialog.Builder(MainActivity.this)
								.setTitle(android.R.string.dialog_alert_title)
								.setMessage(R.string.confirm_to_auto_remove_wiki)
								.setNegativeButton(android.R.string.no, null)
								.setPositiveButton(android.R.string.yes, (dialog, which) -> {
									removeWiki(id, false, false);
									if (!APIOver21)
										wikiListAdapter.notifyDataSetChanged();
									else wikiListAdapter.notifyItemRemoved(pos);
								}).create();
						autoRemoveConfirm.setOnShowListener(dialog -> autoRemoveConfirm.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
						autoRemoveConfirm.show();
					} else if (!loadPage(id))
						Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			}

			// 长按属性
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
				final boolean iNet = SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme()), legacy = SCH_FILE.equals(u.getScheme());
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
				final CheckBox cbBackup = view.findViewById(R.id.cbBackup);
				try {
					cbBackup.setChecked(wa.getBoolean(DB_KEY_BACKUP));
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
				DocumentFile mdf, df;
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
							if (ef.isDirectory() && (ef.getName() != null && ef.getName().endsWith(KEY_EX_HTML + BACKUP_POSTFIX) || ef.getName() != null && ef.getName().endsWith(KEY_EX_HTM + BACKUP_POSTFIX))) {
								DocumentFile vf = mdf.createFile(TYPE_HTML, ef.getName().substring(0, ef.getName().length() - 7));
								tu1 = vf != null ? vf.getUri() : Uri.parse(STR_EMPTY);
								break;
							}
					}
				} catch (IOException e) {
					e.printStackTrace();
					tu1 = Uri.parse(STR_EMPTY);
				}
				tu = tu1;
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
							AlertDialog removeWikiConfirmationDialog = new AlertDialog.Builder(MainActivity.this)
									.setTitle(android.R.string.dialog_alert_title)
									.setMessage(R.string.confirm_to_remove_wiki)
									.setView(view1)
									.setNegativeButton(android.R.string.cancel, null)
									.setPositiveButton(android.R.string.ok, (dialog1, which1) -> {
										removeWiki(id, cbDelFile.isChecked(), cbDelBackups.isChecked());
										if (!APIOver21) wikiListAdapter.notifyDataSetChanged();
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
								if (!isWiki(MainActivity.this, u1)) throw new IOException(EXCEPTION_WIKI_NOT_AVAILABLE);
								File dest = new File(getCacheDir(), CLONING_FILE_NAME);
								dest.createNewFile();
								try (InputStream is = getContentResolver().openInputStream(u1);
										OutputStream os = new FileOutputStream(dest)) {
									if (is == null) throw new FileNotFoundException(EXCEPTION_DOCUMENT_IO_ERROR);
									int len = is.available(), length, lenTotal = 0;
									byte[] bytes = new byte[BUF_SIZE];
									while ((length = is.read(bytes)) > -1) {
										os.write(bytes, 0, length);
										lenTotal += length;
									}
									os.flush();
									if (lenTotal != len) throw new IOException(EXCEPTION_TRANSFER_CORRUPTED);
									getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
								}
							} catch (IOException | IllegalArgumentException e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
							}
						})
						.setNeutralButton(R.string.create_shortcut, ((dialog, which) -> {
							dialog.dismiss();
							try {
								String sub = wa.optString(DB_KEY_SUBTITLE);
								Bundle bu = new Bundle();
								bu.putString(KEY_ID, id);
								bu.putBoolean(KEY_SHORTCUT, true);
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
				backupListAdapter.setOnBtnClickListener((pos1, which) -> {
					final File bf = tu == null ? backupListAdapter.getBackupFile(pos1) : null;
					final DocumentFile bdf = tu != null ? backupListAdapter.getBackupDF(pos1) : null;
					if (bf != null && bf.isFile() || bdf != null && bdf.isFile())
						switch (which) {
							case 1:        // 回滚
								AlertDialog confirmRollback = new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_rollback)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which12) -> {
											try {
												backup(MainActivity.this, u);
											} catch (IOException e) {
												e.printStackTrace();
											}
											try (InputStream is = getContentResolver().openInputStream(tu != null ? bdf.getUri() : Uri.fromFile(bf));
													OutputStream os = getContentResolver().openOutputStream(tu != null ? tu : u)) {    // 由于SAF限制，文件模式无法还原丢失的文件，除非在原位创建一个文件，即使该文件是空的
												if (is == null || os == null)
													throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
												int len = is.available(), length, lenTotal = 0;
												byte[] bytes = new byte[BUF_SIZE];
												while ((length = is.read(bytes)) > -1) {
													os.write(bytes, 0, length);
													lenTotal += length;
												}
												os.flush();
												if (lenTotal != len)
													throw new IOException(EXCEPTION_TRANSFER_CORRUPTED);
												wikiConfigDialog.dismiss();
												Toast.makeText(MainActivity.this, R.string.wiki_rolled_back_successfully, Toast.LENGTH_SHORT).show();
												loadPage(id);
											} catch (IOException e) {
												e.printStackTrace();
												Toast.makeText(MainActivity.this, R.string.failed_writing_file, Toast.LENGTH_SHORT).show();
											}
										})
										.create();
								confirmRollback.setOnShowListener(dialog1 -> confirmRollback.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
								confirmRollback.show();
								break;
							case 2:        // 移除备份
								AlertDialog confirmDelBackup = new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_del_backup)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which1) -> {
											try {
												if (bf != null && bf.delete() || bdf != null && DocumentsContract.deleteDocument(getContentResolver(), bdf.getUri()))
													Toast.makeText(wikiConfigDialog.getContext(), R.string.backup_deleted, Toast.LENGTH_SHORT).show();
												else
													throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
												backupListAdapter.reload(u);
												if (!APIOver21)
													backupListAdapter.notifyDataSetChanged();
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
					backupListAdapter.reload(u);
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
							backupListAdapter.reload(u);
							backupListAdapter.notifyDataSetChanged();
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
		noWiki.setVisibility(wikiListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
	}

	private interface OnGetSrc {
		void run(File file);
	}

	private void getSrcFromUri(OnGetSrc cb) {
		// 对话框等待
		LinearLayout layout = new LinearLayout(this);
		layout.setPaddingRelative(dialogPadding, dialogPadding, dialogPadding, 0);
		layout.setGravity(Gravity.CENTER_VERTICAL);
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setIndeterminate(true);
		progressBar.setPaddingRelative(0, 0,dialogPadding,0);
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
		final Thread thread = new Thread(() -> {
			boolean interrupted = false;
			class AdaptiveUriInputStream {
				private final InputStream is;

				private AdaptiveUriInputStream(Uri uri) throws NetworkErrorException, InterruptedIOException {
					try {
						HttpsURLConnection httpURLConnection;
						URL url = new URL(uri.normalizeScheme().toString());
						httpURLConnection = (HttpsURLConnection) url.openConnection();
						if (!APIOver21)
							httpURLConnection.setSSLSocketFactory(new TLSSocketFactory());
						httpURLConnection.connect();
						is = httpURLConnection.getInputStream();
					} catch (InterruptedIOException e) {
						throw e;
					} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
						e.printStackTrace();
						throw new NetworkErrorException(EXCEPTION_NO_INTERNET);
					}
				}

				private InputStream get() {
					return is;
				}
			}
			File cache = new File(getCacheDir(), genId()), dest = new File(getCacheDir(), TEMPLATE_FILE_NAME);
			try (InputStream isw = new AdaptiveUriInputStream(Uri.parse(getString(R.string.template_repo))).get();
					FileOutputStream osw = new FileOutputStream(cache);
					FileInputStream is = new FileInputStream(cache);
					FileOutputStream os = new FileOutputStream(dest);
					FileChannel ic = is.getChannel();
					FileChannel oc = os.getChannel()) {
				// 下载到缓存
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
				if (!isWiki(cache)) throw new IOException(EXCEPTION_TRANSFER_CORRUPTED);
				ic.transferTo(0, ic.size(), oc);
				ic.force(true);
				progressDialog.dismiss();
			} catch (InterruptedException | InterruptedIOException ignored) {
				interrupted = true;
				runOnUiThread(() -> Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show());
			} catch (NetworkErrorException e) {
				e.printStackTrace();
				runOnUiThread(() -> Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show());
				progressDialog.dismiss();
			} catch (IOException e) {
				e.printStackTrace();
				runOnUiThread(() -> Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show());
				progressDialog.dismiss();
			} finally {
				cache.delete();
				if (!interrupted) runOnUiThread(() -> cb.run(dest));
			}
		});
		progressDialog.setOnShowListener(dialog -> thread.start());
		progressDialog.setOnCancelListener(dialogInterface -> thread.interrupt());
		progressDialog.show();
	}

	private Boolean loadPage(String id) {
		Intent in = new Intent();
		try {
			if (!db.getJSONObject(DB_KEY_WIKI).has(id))
				throw new JSONException(EXCEPTION_JSON_ID_NOT_FOUND);
			Bundle bu = new Bundle();
			bu.putString(KEY_ID, id);
			in.putExtras(bu).setClass(this, TWEditorWV.class);
			startActivity(in);
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void removeWiki(String id, boolean del, boolean delBackup) {
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
			JSONObject xw = (JSONObject) wl.remove(id);
			writeJson(this, db);
			if (del && xw != null) {
				Uri u = Uri.parse(xw.optString(DB_KEY_URI));
				boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
				DocumentFile df, mdf = null;
				if (APIOver21 && !legacy) try {
					mdf = DocumentFile.fromTreeUri(this, u);
					tree = true;
				} catch (IllegalArgumentException ignored) {
				}
				final File f;
				if (tree) {
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
				} else {
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
				}
				if (legacy) f.delete();
				else DocumentsContract.deleteDocument(getContentResolver(), u);
				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
			}
			wikiListAdapter.reload(db);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			if (CLASS_MENU_BUILDER.equals(menu.getClass().getSimpleName())) try {
				Method method = menu.getClass().getDeclaredMethod(METHOD_SET_OPTIONAL_ICONS_VISIBLE, Boolean.TYPE);
				method.setAccessible(true);
				method.invoke(menu, true);
			} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
				e.printStackTrace();
			}
			if (!APIOver21) menu.getItem(2).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		final int idNew = R.id.action_new,
				idImport = R.id.action_file_import,
				idDir = R.id.action_add_dir,
				idAbout = R.id.action_about;
		switch (id) {
			case idNew:
				getChooserCreate.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
				break;
			case idImport:
				getChooserImport.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
				break;
			case idDir:
				if (APIOver21)
					getChooserTree.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
				break;
			case idAbout:
				SpannableString spannableString = new SpannableString(getString(R.string.about));
				Linkify.addLinks(spannableString, Linkify.ALL);
				AlertDialog aboutDialog = new AlertDialog.Builder(this)
						.setTitle(R.string.action_about)
						.setMessage(spannableString)
						.setPositiveButton(android.R.string.ok, null)
						.setNeutralButton(R.string.market, (dialog, which) -> {
							try {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(KEY_URI_RATE + getPackageName())));
							} catch (ActivityNotFoundException e) {
								e.printStackTrace();
							}
						}).create();
				aboutDialog.setOnShowListener(dialog1 -> aboutDialog.getWindow().getDecorView().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())));
				aboutDialog.show();
				((TextView) aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
				if (APIOver23)
					((TextView) aboutDialog.findViewById(android.R.id.message)).setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void cloneWiki(Uri uri) {
		createWiki(uri, true);
	}

	private void createWiki(Uri uri) {
		createWiki(uri, false);
	}

	private void createWiki(Uri uri, boolean clone) {
		OnGetSrc cb = file -> {
			if (file.exists() && isWiki(file)) {
				try (FileInputStream is = new FileInputStream(file);
						OutputStream os = getContentResolver().openOutputStream(uri)) {
					if (os == null) throw new FileNotFoundException(EXCEPTION_DOCUMENT_IO_ERROR);
					// 查重
					String id = null;
					JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa = null;
					boolean exist = false;
					Iterator<String> iterator = wl.keys();
					while (iterator.hasNext()) {
						exist = uri.toString().equals((wa = wl.getJSONObject((id = iterator.next()))).optString(DB_KEY_URI));
						if (exist) break;
					}
					if (exist)
						Toast.makeText(MainActivity.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
					else {
						wa = new JSONObject();
						wa.put(DB_KEY_URI, uri.toString());
						id = genId();
						wl.put(id, wa);
					}
					wa.put(KEY_NAME, KEY_TW);
					wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
					wa.put(DB_KEY_BACKUP, false);
					MainActivity.writeJson(MainActivity.this, db);
					int len = is.available(), length, lenTotal = 0;
					byte[] bytes = new byte[BUF_SIZE];
					while ((length = is.read(bytes)) > -1) {
						os.write(bytes, 0, length);
						lenTotal += length;
					}
					os.flush();
					if (lenTotal != len) throw new IOException(EXCEPTION_TRANSFER_CORRUPTED);
					try {
						getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
					if (!loadPage(id))
						Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			}
			if (clone) file.delete();
		};
		if (clone) cb.run(new File(getCacheDir(), CLONING_FILE_NAME));
		else getSrcFromUri(cb);
	}

	private void importWiki(Uri uri) {
		String id = null;
		if (!isWiki(this, uri)) {
			Toast.makeText(this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
			boolean exist = false;
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				exist = uri.toString().equals(wl.getJSONObject(id = iterator.next()).optString(DB_KEY_URI));
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				JSONObject wa = new JSONObject();
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
				try (FileInputStream is = new FileInputStream(file);
						OutputStream os = getContentResolver().openOutputStream(nf.getUri())) {
					if (os == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
					int len = is.available(), length, lenTotal = 0;
					byte[] bytes = new byte[BUF_SIZE];
					while ((length = is.read(bytes)) > -1) {
						os.write(bytes, 0, length);
						lenTotal += length;
					}
					os.flush();
					if (lenTotal != len) throw new IOException(EXCEPTION_TRANSFER_CORRUPTED);
					addDir(uri);
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
				}
			});
			return;
		} else if (!isWiki(this, index.getUri())) {
			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			return;
		}
		String id = null;
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
			boolean exist = false;
			Iterator<String> iterator = wl.keys();
			while (iterator.hasNext()) {
				exist = uri.toString().equals(wl.getJSONObject(id = iterator.next()).optString(DB_KEY_URI));
				if (exist) break;
			}
			if (exist) {
				Toast.makeText(this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				JSONObject wa = new JSONObject();
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

	@Override
	public void onResume() {
		super.onResume();
		try {
			db = readJson(this);
			wikiListAdapter.reload(db);
			wikiListAdapter.notifyDataSetChanged();
			noWiki.setVisibility(wikiListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (APIOver23) {
			Window w = getWindow();
			int color = getColor(R.color.design_default_color_primary);
			w.setStatusBarColor(color);
			if (APIOver26)
				w.setNavigationBarColor(color);
			w.getDecorView().setSystemUiVisibility((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | (APIOver26 ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : View.SYSTEM_UI_FLAG_VISIBLE) : View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}

	private static JSONObject initJson(Context context) throws JSONException {
		File ext = context.getExternalFilesDir(null), file = new File(ext, DB_FILE_NAME);
		if (ext != null && file.isFile())
			try (InputStream is = new FileInputStream(file)) {
				byte[] b = new byte[is.available()];
				if (is.read(b) < 0) throw new IOException(EXCEPTION_JSON_DATA_ERROR);
				JSONObject jsonObject = new JSONObject(new String(b));
				if (!jsonObject.has(DB_KEY_WIKI)) jsonObject.put(DB_KEY_WIKI, new JSONObject());
				return jsonObject;
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			} finally {
				file.delete();
			}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(DB_KEY_WIKI, new JSONObject());
		return jsonObject;
	}


	static JSONObject readJson(Context context) throws JSONException {
		try (InputStream is = context.openFileInput(DB_FILE_NAME)) {
			byte[] b = new byte[is.available()];
			if (is.read(b) < 0) throw new IOException(EXCEPTION_JSON_DATA_ERROR);
			return new JSONObject(new String(b));
		} catch (IOException e) {
			throw new JSONException(e.getMessage());
		}
	}

	static void writeJson(Context context, JSONObject vdb) throws JSONException {
		try (FileOutputStream os = context.openFileOutput(DB_FILE_NAME, MODE_PRIVATE)) {
			byte[] b = vdb.toString(2).getBytes();
			os.write(b);
			os.flush();
		} catch (IOException e) {
			throw new JSONException(e.getMessage());
		}
	}

	static void exportJson(Context context, JSONObject vdb) {
		File ext = context.getExternalFilesDir(null);
		if (ext == null) return;
		try (OutputStream os = new FileOutputStream(new File(ext, DB_FILE_NAME))) {
			byte[] b = vdb.toString(2).getBytes();
			os.write(b);
			os.flush();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	@TargetApi(23)
	private static void checkPermission(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
				ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		} else {
			if (!Environment.isExternalStorageManager()) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
						.setData(Uri.parse(SCH_PACKAGES + ':' + context.getPackageName()));
				if (context instanceof MainActivity) {
					MainActivity activity = (MainActivity) context;
					if (!activity.acquiringStorage) {
						activity.acquiringStorage = true;
						activity.getPermissionRequest.launch(intent);
					}
				} else if (context instanceof TWEditorWV) {
					TWEditorWV activity = (TWEditorWV) context;
					if (!activity.acquiringStorage) {
						activity.acquiringStorage = true;
						activity.getPermissionRequest.launch(intent);
					}
				}
			}
		}
	}

	static String genId() {
		return UUID.randomUUID().toString();
	}

	static boolean isWiki(Context context, Uri uri) {
		try {
			if (SCH_FILE.equals(uri.getScheme())) checkPermission(context);
			return SCH_HTTP.equals(uri.getScheme()) || SCH_HTTPS.equals(uri.getScheme()) || isWiki(context.getContentResolver().openInputStream(uri), uri);
		} catch (IOException | SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean isWiki(File file) {
		try {
			return isWiki(new FileInputStream(file), Uri.fromFile(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean isWiki(InputStream vis, Uri u) throws IOException {
		try (InputStream is = vis) {
			Document doc = Jsoup.parse(is, StandardCharsets.UTF_8.name(), u.toString());
			Element an = doc.selectFirst(new Evaluator.AttributeKeyPair(KEY_NAME, KEY_APPLICATION_NAME) {
				@Override
				public boolean matches(Element root, Element element) {
					return KEY_VERSION_AREA.equals(element.id()) || KEY_APPLICATION_NAME.equals(element.attr(KEY_NAME)) && KEY_TW.equals(element.attr(KEY_CONTENT));
				}
			});
			return an != null;
		}
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
			return;    // No backup for http links
		boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
		DocumentFile df = null, mdf = null;
		if (APIOver21 && !legacy) try {    // tree模式
			DocumentFile p;
			String fn;
			mdf = DocumentFile.fromTreeUri(context, u);    // 根目录
			if (mdf == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
			df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;    // index.htm(l)
			if ((df == null) || !df.isFile() || (fn = df.getName()) == null)
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
			bfn = new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), formatBackup(df != null ? df.lastModified() : 0)).toString();
			mfd = new File(file.getParentFile(), mfn + BACKUP_POSTFIX);
			if (!mfd.exists()) mfd.mkdirs();
			if (!mfd.isDirectory()) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
		} else {
			mfn = df.getName();
			bfn = new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), formatBackup(df.lastModified())).toString();
			if ((bdf = mdf.createFile(TYPE_HTML, bfn)) == null)
				throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
		}
		try (InputStream is = context.getContentResolver().openInputStream(tree ? df.getUri() : u);
				OutputStream os = context.getContentResolver().openOutputStream(tree ? bdf.getUri() : Uri.fromFile(new File(mfd, bfn)))) {
			if (is == null || os == null) throw new IOException(EXCEPTION_DOCUMENT_IO_ERROR);
			int len = is.available(), length, lenTotal = 0;
			byte[] bytes = new byte[BUF_SIZE];
			while ((length = is.read(bytes)) > -1) {
				os.write(bytes, 0, length);
				lenTotal += length;
			}
			os.flush();
			if (lenTotal != len) throw new IOException(EXCEPTION_TRANSFER_CORRUPTED);
		}
	}

	static String formatBackup(long time) {
		SimpleDateFormat format = new SimpleDateFormat(MASK_SDF_BACKUP, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(KEY_TZ_UTC));
		return '.' + format.format(new Date(time));
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
}