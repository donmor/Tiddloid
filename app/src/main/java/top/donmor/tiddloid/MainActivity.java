/*
 * top.donmor.tiddloid.MainActivity <= [P|Tiddloid]
 * Last modified: 18:18:25 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.os.Handler;
import android.provider.DocumentsContract;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
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
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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
	private ActivityResultLauncher<Intent> getChooserClone, getChooserCreate,getChooserImport, getChooserTree;

	// CONSTANT
//	static final FileDialogFilter[] HTML_FILTERS = {new FileDialogFilter(".html;.htm", new String[]{".html", ".htm"})};
	static final int TAKE_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
			BUF_SIZE = 4096;
	static final String
			KEY_TW = "TiddlyWiki",
	//			SCHEME_BLOB_B64 = "blob-b64",
	BACKUP_POSTFIX = "_backup",
			KEY_NAME = "name",
			KEY_LBL = " — ",
			KEY_FAVICON = "favicon",
			KEY_ID = "id",
	//			KEY_URL = "url",
	KEY_TZ_UTC = "UTC",
	//			DB_KEY_SHOW_HIDDEN = "showHidden",
//			DB_KEY_LAST_DIR = "lastDir",
	DB_KEY_WIKI = "wiki",
			DB_KEY_COLOR = "color",
			DB_KEY_URI = "uri",
			KEY_SHORTCUT = "shortcut",
			DB_KEY_SUBTITLE = "subtitle",
			DB_KEY_BACKUP = "backup",
			KEY_FN_INDEX = "index.html",
			KEY_FN_INDEX2 = "index.htm",
	//			KEY_FN_INDEX_D = "index",
	MASK_SDF_BACKUP = "yyyyMMddHHmmssSSS",
			SCH_CONTENT = "content",
			SCH_FILE = "file",
			SCH_HTTP = "http",
			SCH_HTTPS = "https",
			STR_EMPTY = "",
			TYPE_HTML = "text/html";
	private static final String
			DB_FILE_NAME = "data.json",
	//			DB_KEY_CSE = "customSearchEngine",
//			DB_KEY_SEARCH_ENGINE = "searchEngine",
	DB_KEY_PATH = "path",
			KEY_APPLICATION_NAME = "application-name",
			KEY_VERSION_AREA = "versionArea",
	//			KEY_STORE_AREA = "storeArea",
	KEY_CONTENT = "content",
			KEY_URI_RATE = "market://details?id=",
	//			SE_GOOGLE = "Google",
//			SE_BING = "Bing",
//			SE_BAIDU = "Baidu",
//			SE_SOGOU = "Sogou",
//			SE_CUSTOM = "Custom",
//			PREF_S = "%s",
//			PREF_SU = "#content#",
//			SCH_EX_HTTP = "http://",
	TEMPLATE_FILE_NAME = "template.html",
			CLONING_FILE_NAME = "cloning.html",
			CLASS_MENU_BUILDER = "MenuBuilder",
			METHOD_SET_OPTIONAL_ICONS_VISIBLE = "setOptionalIconsVisible";
//	static final int REQUEST_CLONE = 41, REQUEST_OPEN = 42, REQUEST_CREATE = 43, REQUEST_DIR = 44;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.activity_main);
//		File templateOnStart = new File(getFilesDir(), TEMPLATE_FILE_NAME);
//		if (!templateOnStart.exists() || !isWiki(templateOnStart)) {
//			getTemplate(false);
//		}

		try {
			db = readJson(this);
			if (!db.has(DB_KEY_WIKI)) throw new JSONException(getString(R.string.data_error));
		} catch (Exception e) {
			e.printStackTrace();
			db = initJson(this);
			try {
				writeJson(this, db);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		trimDB140(this, db);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkPermission();	// TODO: file sch specific
		// 加载UI
		onConfigurationChanged(getResources().getConfiguration());
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		noWiki = findViewById(R.id.t_noWiki);
		final SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh);
		refreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
			MainActivity.this.onResume();
			refreshLayout.setRefreshing(false);
		}, 500));
		RecyclerView rvWikiList = findViewById(R.id.rvWikiList);
		rvWikiList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
		rvWikiList.setItemAnimator(new DefaultItemAnimator());
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
		try {
			wikiListAdapter = new WikiListAdapter(this, db);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		rvWikiList.setAdapter(wikiListAdapter);
		wikiListAdapter.setReloadListener(count -> noWiki.setVisibility(count > 0 ? View.GONE : View.VISIBLE));
		wikiListAdapter.setOnItemClickListener(new WikiListAdapter.ItemClickListener() {
			// 点击打开
			@Override
			public void onItemClick(final int pos, final String id) {
				if (pos == -1) return;
				try {
					JSONObject wa = db.getJSONObject(DB_KEY_WIKI).getJSONObject(id);
					Uri uri = Uri.parse(wa.optString(DB_KEY_URI)), u1;
					try {
						DocumentFile mdf = DocumentFile.fromTreeUri(MainActivity.this, uri), p, df;
						if (mdf == null || !mdf.isDirectory()) throw new IOException();
						df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;    // index.htm(l)
						if (df == null || !df.isFile()) throw new IOException();
						u1 = df.getUri();
					} catch (IllegalArgumentException ignored) {
						u1 = uri;
					}
					if (isWiki(MainActivity.this, u1)) {
						if (SCH_CONTENT.equals(uri.getScheme()))
							getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
						else if (SCH_FILE.equals(uri.getScheme())) checkPermission(MainActivity.this);
						if (!loadPage(id))
							Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
					} else {
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(android.R.string.dialog_alert_title)
								.setMessage(R.string.confirm_to_auto_remove_wiki)
								.setNegativeButton(android.R.string.no, null)
								.setPositiveButton(android.R.string.yes, (dialog, which) -> {
									removeWiki(id, false, false);
									if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
										wikiListAdapter.notifyDataSetChanged();
									else wikiListAdapter.notifyItemRemoved(pos);
								}).show();
					}
				} catch (Exception e) {
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
//				final File file;
				try {
					wl = db.getJSONObject(DB_KEY_WIKI);
					wa = wl.getJSONObject(id);
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
					return;
				}
				u = Uri.parse(wa.optString(DB_KEY_URI));
//					file = new File(u.getPath());
				name = wa.optString(KEY_NAME, KEY_TW);
				String path, provider = getString(R.string.unknown);
				final boolean iNet = SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme()), legacy = SCH_FILE.equals(u.getScheme());
				if (iNet) {
					provider = getString(R.string.internet);
					path = u.toString();
				} else if (legacy) {
					checkPermission(MainActivity.this);
					provider = getString(R.string.local_legacy);
					path = u.getPath();
				} else try {
					// 获取来源名
					PackageManager pm = getPackageManager();
					for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
						if (Objects.requireNonNull(u.getAuthority()).startsWith(info.packageName)) {
							provider = pm.getApplicationLabel(info).toString();
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					path = Uri.decode(u.getLastPathSegment());
				}
				// 显示属性
//				CharSequence s = getString(R.string.provider)
//						+ provider
//						+ '\n'
//						+ getString(R.string.pathDir)
//						+ Uri.decode(uri.getLastPathSegment());
//						+ '\n'
//						+ getString(R.string.filename)
//						+ (fn != null && fn.length() > 0 ? fn : getString(R.string.unknown));
				textWikiInfo.setText(new StringBuilder(getString(R.string.provider))
						.append(provider)
						.append('\n')
						.append(getString(R.string.pathDir))
						.append(path));
//				textWikiInfo.setText(file.getPath());
				final CheckBox cbBackup = view.findViewById(R.id.cbBackup);
				try {
					cbBackup.setChecked(wa.getBoolean(DB_KEY_BACKUP));
				} catch (Exception e) {
					e.printStackTrace();
				}
				cbBackup.setEnabled(!iNet);
				final ConstraintLayout frmBackupList = view.findViewById(R.id.frmBackupList);
				if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE);
				else frmBackupList.setVisibility(View.GONE);
				final TextView lblNoBackup = view.findViewById(R.id.lblNoBackup);
				RecyclerView rvBackupList = view.findViewById(R.id.rvBackupList);
				rvBackupList.setLayoutManager(new LinearLayoutManager(view.getContext()));
//				Button btnCreateShortcut = view.findViewById(R.id.btnCreateShortcut),
//						btnCloneWiki = view.findViewById(R.id.btnCloneWiki),
//						btnRemoveWiki = view.findViewById(R.id.btnRemoveWiki);
				// 读图标
				Drawable icon = null;
				byte[] b = Base64.decode(wa.optString(KEY_FAVICON), Base64.NO_PADDING);
				final Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_description);
					if (favicon != null) try {
						icon = new BitmapDrawable(getResources(), favicon);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				final Uri tu;
				Uri tu1 = null;
				DocumentFile mdf, df;
				if (!legacy) try {
					mdf = DocumentFile.fromTreeUri(MainActivity.this, u);
					DocumentFile p;
					if (mdf == null || !mdf.isDirectory()) throw new IOException();
					df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
					if (df == null || !df.isFile()) throw new IOException();
					tu1 = df.getUri();
				} catch (IllegalArgumentException ignored) {
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
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
										try {
											removeWiki(id, cbDelFile.isChecked(), cbDelBackups.isChecked());
											if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
												wikiListAdapter.notifyDataSetChanged();
											else
												wikiListAdapter.notifyItemRemoved(pos);
										} catch (Exception e) {
											e.printStackTrace();
										}
									})
									.create();
							removeWikiConfirmationDialog.show();
						})
						.setNegativeButton("Clone", (dialog, which) -> {
							dialog.dismiss();
//							if (SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme()))
//								newWikiFromUri(u);
//							else
							try {
								Uri u1 = tu != null ? tu : u;
								if (!isWiki(MainActivity.this, u1)) throw new IOException();
								File dest = new File(getCacheDir(), CLONING_FILE_NAME);
								dest.createNewFile();
								try (InputStream is = legacy ? new FileInputStream(u1.getPath()) : getContentResolver().openInputStream(u1);
									 OutputStream os = new FileOutputStream(dest)) {
									if (is == null) throw new FileNotFoundException();
									int len = is.available(), length, lenTotal = 0;
									byte[] bytes = new byte[BUF_SIZE];
									while ((length = is.read(bytes)) > -1) {
										os.write(bytes, 0, length);
										lenTotal += length;
									}
									os.flush();
									if (lenTotal != len) throw new IOException();
//									startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), REQUEST_CLONE);	// TODO: Refactor to registerForActivityResult
									getChooserClone.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
								}
							} catch (IOException e) {
								e.printStackTrace();
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
											.setIcon(favicon != null ? IconCompat.createWithBitmap(favicon) : IconCompat.createWithResource(MainActivity.this, Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ? R.drawable.ic_shortcut : R.mipmap.ic_shortcut))
											.setIntent(in)
											.build();
									if (ShortcutManagerCompat.requestPinShortcut(MainActivity.this, shortcut, null))
										Toast.makeText(MainActivity.this, R.string.shortcut_created, Toast.LENGTH_SHORT).show();
									else throw new Exception();
								}
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, R.string.shortcut_failed, Toast.LENGTH_SHORT).show();
							}
						}))
						.create();
				// 备份系统	TODO: Refactor
				final BackupListAdapter backupListAdapter = new BackupListAdapter(wikiConfigDialog.getContext());
//				DocumentFile finalMdf = mdf;
//				DocumentFile finalDf = df;
				backupListAdapter.setOnBtnClickListener((pos1, which) -> {
					final File bf = tu == null ? backupListAdapter.getBackupFile(pos1) : null;
					final DocumentFile bdf = tu != null ? backupListAdapter.getBackupDF(pos1) : null;
					if (bf != null && bf.isFile() || bdf != null && bdf.isFile())
						switch (which) {
							case 1:        // 回滚
								new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_rollback)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which12) -> {
											try {
												backup(MainActivity.this, u);
//												String fn;
//												if (tu != null && (fn = finalDf.getName()) != null)
//													try {
////														finalMdf.findFile(fn).;
//														;
//													} catch (Exception ignored){}
												try (InputStream is = tu != null ? getContentResolver().openInputStream(bdf.getUri()) : new FileInputStream(bf);
													 OutputStream os = legacy ? new FileOutputStream(u.getPath()) : getContentResolver().openOutputStream(tu != null ? tu : u)) {
//													ic.transferTo(0, ic.size(), oc);
//													ic.force(true);
													if (is == null || os == null)
														throw new IOException();
													int len = is.available(), length, lenTotal = 0;
													byte[] bytes = new byte[BUF_SIZE];
													while ((length = is.read(bytes)) > -1) {
														os.write(bytes, 0, length);
														lenTotal += length;
													}
													os.flush();
													if (lenTotal != len) throw new IOException();
													wikiConfigDialog.dismiss();
													Toast.makeText(MainActivity.this, R.string.wiki_rolled_back_successfully, Toast.LENGTH_SHORT).show();
													loadPage(id);
												} catch (IOException e) {
													e.printStackTrace();
													Toast.makeText(MainActivity.this, R.string.failed_writing_file, Toast.LENGTH_SHORT).show();
												}
											} catch (IOException e) {
												e.printStackTrace();
											}
										})
										.show();
								break;
							case 2:        // 移除备份
								new AlertDialog.Builder(wikiConfigDialog.getContext())
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.confirm_to_del_backup)
										.setNegativeButton(android.R.string.no, null)
										.setPositiveButton(android.R.string.yes, (dialog, which1) -> {
											try {
												if (bf != null && bf.delete() || bdf != null && DocumentsContract.deleteDocument(getContentResolver(), bdf.getUri()))
													Toast.makeText(wikiConfigDialog.getContext(), R.string.backup_deleted, Toast.LENGTH_SHORT).show();
												else throw new IOException();
												backupListAdapter.reload(u);
												if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
													backupListAdapter.notifyDataSetChanged();
												backupListAdapter.notifyItemRemoved(pos1);
											} catch (IOException e) {
												e.printStackTrace();
												Toast.makeText(wikiConfigDialog.getContext(), R.string.failed_deleting_file, Toast.LENGTH_SHORT).show();
											}
										})
										.show();
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
//				wikiConfigDialog.setCanceledOnTouchOutside(false);
				wikiConfigDialog.show();
				wikiConfigDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!iNet);
//				btnCreateShortcut.setOnClickListener(v -> {
//					wikiConfigDialog.dismiss();
//					try {
//						String sub = wa.optString(DB_KEY_SUBTITLE);
//						Bundle bu = new Bundle();
//						bu.putString(KEY_ID, id);
//						bu.putBoolean(KEY_SHORTCUT, true);
//						Intent in = new Intent(MainActivity.this, TWEditorWV.class).putExtras(bu).setAction(Intent.ACTION_MAIN);
//						if (ShortcutManagerCompat.isRequestPinShortcutSupported(MainActivity.this)) {
//							ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(MainActivity.this, id)
//									.setShortLabel(name)
//									.setLongLabel(name + (sub.length() > 0 ? KEY_LBL + sub : sub))
//									.setIcon(favicon != null ? IconCompat.createWithBitmap(favicon) : IconCompat.createWithResource(MainActivity.this, Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ? R.drawable.ic_shortcut : R.mipmap.ic_shortcut))
//									.setIntent(in)
//									.build();
//							if (ShortcutManagerCompat.requestPinShortcut(MainActivity.this, shortcut, null))
//								Toast.makeText(MainActivity.this, R.string.shortcut_created, Toast.LENGTH_SHORT).show();
//							else throw new Exception();
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//						Toast.makeText(MainActivity.this, R.string.shortcut_failed, Toast.LENGTH_SHORT).show();
//					}
//				});
//				btnCloneWiki.setOnClickListener(v -> {    // TODO: Clone
//					wikiConfigDialog.dismiss();
//					if (SCH_HTTP.equals(u.getScheme()) || SCH_HTTPS.equals(u.getScheme()))
//						newWikiFromUri(u);
//					else try {
//						if (!isWiki(MainActivity.this, u)) throw new IOException();
//						File dest = new File(getCacheDir(), CLONING_FILE_NAME);
//						dest.createNewFile();
//						try (InputStream is = SCH_FILE.equals(u.getScheme()) ? new FileInputStream(u.getPath()) : getContentResolver().openInputStream(u);
//							 OutputStream os = new FileOutputStream(dest)) {
//							if (is == null) throw new FileNotFoundException();
//							int length;
//							byte[] bytes = new byte[BUF_SIZE];
//							while ((length = is.read(bytes)) > -1) os.write(bytes, 0, length);
//							os.flush();
//							startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), REQUEST_CREATE);
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				});
//				btnRemoveWiki.setOnClickListener(v -> {
//					wikiConfigDialog.dismiss();
//					View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.del_confirm, null);
//					final CheckBox cbDelFile = view1.findViewById(R.id.cbDelFile);
//					final CheckBox cbDelBackups = view1.findViewById(R.id.cbDelBackups);
//					cbDelBackups.setEnabled(false);
//					cbDelFile.setOnCheckedChangeListener((buttonView, isChecked) -> cbDelBackups.setEnabled(isChecked));
//					AlertDialog removeWikiConfirmationDialog = new AlertDialog.Builder(MainActivity.this)
//							.setTitle(android.R.string.dialog_alert_title)
//							.setMessage(R.string.confirm_to_remove_wiki)
//							.setView(view1)
//							.setNegativeButton(android.R.string.cancel, null)
//							.setPositiveButton(android.R.string.ok, (dialog, which) -> {
//								try {
//									removeWiki(id, cbDelFile.isChecked(), cbDelBackups.isChecked());
//									if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
//										wikiListAdapter.notifyDataSetChanged();
//									else
//										wikiListAdapter.notifyItemRemoved(pos);
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							})
//							.create();
//					removeWikiConfirmationDialog.show();
//				});
			}
		});
		noWiki.setVisibility(wikiListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
	}

//	private void getTemplate(@Nullable String source) {
//		final ProgressDialog progressDialog = new ProgressDialog(this);
//		progressDialog.setMessage(getString(R.string.please_wait));
//		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		progressDialog.setCancelable(false);
//		progressDialog.setOnShowListener(dialog -> wGet(MainActivity.this, Uri.parse(source == null ? getString(R.string.template_repo) : source), new File(getFilesDir(), TEMPLATE_FILE_NAME), true, true, file -> !isWiki(file), new OnDownloadCompleteListener() {
//			@Override
//			public void onDownloadComplete(File file) {
//				if (file.exists()) {
//					Toast.makeText(MainActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
//					newWiki();
//				} else
//					Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
//				progressDialog.dismiss();
//			}
//
//			@Override
//			public void onDownloadFailed() {
//				Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
//				progressDialog.dismiss();
//			}
//		}));
//		AlertDialog dialog = new AlertDialog.Builder(this)
//				.setTitle(android.R.string.dialog_alert_title)
//				.setMessage(R.string.missing_template)
//				.setPositiveButton(android.R.string.ok, null)
//				.show();
//		dialog.setOnDismissListener(dialog1 -> progressDialog.show());
//	}
//	private void getTemplate(final boolean n) {
//		final ProgressDialog progressDialog = new ProgressDialog(this);
//		progressDialog.setMessage(getString(R.string.please_wait));
//		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		progressDialog.setCancelable(false);
//		progressDialog.setOnShowListener(dialog -> wGet(MainActivity.this, Uri.parse(getString(R.string.template_repo)), new File(getFilesDir(), TEMPLATE_FILE_NAME), true, true, file -> !isWiki(file), new OnDownloadCompleteListener() {
//			@Override
//			public void onDownloadComplete(File file) {
//				if (file.exists()) {
//					Toast.makeText(MainActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
//					if (n) newWiki();
//				} else
//					Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
//				progressDialog.dismiss();
//			}
//
//			@Override
//			public void onDownloadFailed() {
//				Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
//				progressDialog.dismiss();
//			}
//		}));
//		AlertDialog dialog = new AlertDialog.Builder(this)
//				.setTitle(android.R.string.dialog_alert_title)
//				.setMessage(R.string.missing_template)
//				.setPositiveButton(android.R.string.ok, null)
//				.show();
//		dialog.setOnDismissListener(dialog1 -> progressDialog.show());
//	}

	private Boolean loadPage(String id) {
		Intent in = new Intent();
		try {
			if (!db.getJSONObject(DB_KEY_WIKI).has(id)) throw new IOException();
			Bundle bu = new Bundle();
			bu.putString(KEY_ID, id);
			in.putExtras(bu).setClass(MainActivity.this, TWEditorWV.class);
			startActivity(in);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void removeWiki(String id, boolean del, boolean delBackup) {
		try {
			JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
			JSONObject xw = (JSONObject) wl.remove(id);
			writeJson(MainActivity.this, db);
			if (del && xw != null) {
				Uri u = Uri.parse(xw.optString(DB_KEY_URI));
				boolean legacy = SCH_FILE.equals(u.getScheme()), tree = false;
				DocumentFile df, mdf = null;
				if (!legacy) try {
					mdf = DocumentFile.fromTreeUri(this, u);
					tree = true;
				} catch (IllegalArgumentException ignored) {
				}
				final File f;
				if (tree) {
					if (mdf == null || !mdf.isDirectory()) throw new IOException();
					f = null;
					DocumentFile p;
					df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
					if (df == null || !df.isFile()) throw new IOException();
					String bdn = df.getName();
					if (bdn == null) throw new IOException();
					u = df.getUri();
					if (delBackup) try {
						DocumentFile bdf = mdf.findFile(bdn);
						if (bdf != null && bdf.isDirectory()) {
							for (DocumentFile inner : bdf.listFiles())
								if (inner != null && inner.isFile() && isBackupFile(df.getName(), inner.getName()))
									DocumentsContract.deleteDocument(getContentResolver(), inner.getUri());
							if (bdf.listFiles().length == 0)
								bdf.delete();
						}
					} catch (Exception e) {
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
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (legacy) f.delete();
				else DocumentsContract.deleteDocument(getContentResolver(), u);
				Toast.makeText(MainActivity.this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
			}
			wikiListAdapter.reload(db);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

//	@Override
//	public boolean onMenuOpened(int featureId, Menu menu) {
//		return super.onMenuOpened(featureId, menu);
//	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			if (CLASS_MENU_BUILDER.equals(menu.getClass().getSimpleName())) {
				try {
					Method method = menu.getClass().getDeclaredMethod(METHOD_SET_OPTIONAL_ICONS_VISIBLE, Boolean.TYPE);
					method.setAccessible(true);
					method.invoke(menu, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				menu.getItem(2).setEnabled(false);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		final int idNew = R.id.action_new,
				idImport = R.id.action_file_import,
				idDir = R.id.action_add_dir,
//				idFork = R.id.action_fork,
//				idSettings = R.id.action_settings,
				idAbout = R.id.action_about;
		switch (id) {
			case idNew:
				getChooserCreate.launch(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
//				startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), REQUEST_CREATE);
//				newWikiFromUri();
//				createWiki();
				break;
			case idImport:
//				importWiki();
				getChooserImport.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML));
//				startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), REQUEST_OPEN);
				break;
			case idDir:
//				importWiki();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getChooserTree.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
//					startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION), REQUEST_DIR);
				}
				break;
//			case idFork:
//				iNavigate();
//				break;
//			case idSettings: {
//				openCfg();
//				break;
//			}
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
							} catch (Exception e) {
								e.printStackTrace();
							}
						})
						.show();
				((TextView) aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					((TextView) aboutDialog.findViewById(android.R.id.message)).setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

//	private void newWikiFromUri() {
//		getSrcFromUri(file -> startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), REQUEST_CREATE));
//	}
//	private void newWikiFromUri(Uri uri) {
//		getSrcFromUri(uri, file -> startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), uri != null ? REQUEST_CLONE : REQUEST_CREATE));
//	}

	//	private void newWikiFromUri(Uri uri) {
//		// 对话框等待
//		final ProgressDialog progressDialog = new ProgressDialog(this);
//		progressDialog.setMessage(getString(R.string.please_wait));
//		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		progressDialog.setCanceledOnTouchOutside(false);
//		final Thread thread = new Thread(() -> {
//			boolean interrupted = false;
////			final boolean[] iNet = new boolean[3];
//			class AdaptiveUriInputStream {
//				private final InputStream is;
//
//				private AdaptiveUriInputStream(Uri uri1) throws NoSuchAlgorithmException, KeyManagementException, IOException {
//					String scheme = uri1.getScheme();
//					if (scheme == null || !SCH_HTTP.equals(scheme) && !SCH_HTTPS.equals(scheme))
//						throw new IOException();
//					HttpURLConnection httpURLConnection;
//					URL url = new URL(uri1.normalizeScheme().toString());
//					if (SCH_HTTPS.equals(scheme)) {
//						httpURLConnection = (HttpsURLConnection) url.openConnection();
//						if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
//							((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new TLSSocketFactory());
//					} else httpURLConnection = (HttpURLConnection) url.openConnection();
//					httpURLConnection.connect();
//					is = httpURLConnection.getInputStream();
////					iNet[0] = true;
//				}
//
//				private InputStream get() {
//					return is;
//				}
//			}
//			File cache = new File(getCacheDir(), genId());
//			try (InputStream isw = new AdaptiveUriInputStream(uri == null ? Uri.parse(getString(R.string.template_repo)) : uri).get();
//				 FileOutputStream osw = new FileOutputStream(cache);
//				 FileInputStream is = new FileInputStream(cache);
//				 FileOutputStream os = new FileOutputStream(new File(getCacheDir(), uri != null ? CLONING_FILE_NAME : TEMPLATE_FILE_NAME));
//				 FileChannel ic = is.getChannel();
//				 FileChannel oc = os.getChannel()) {
//				// 下载到缓存
//				int length;
//				byte[] bytes = new byte[BUF_SIZE];
//				while ((length = isw.read(bytes)) > -1) {
//					osw.write(bytes, 0, length);
//					if (Thread.currentThread().isInterrupted()) {
//						interrupted = true;
//						break;
//					}
//				}
//				osw.flush();
//				if (interrupted) throw new InterruptedException();
//				if (!isWiki(cache)) throw new IOException();
//				progressDialog.dismiss();
//				ic.transferTo(0, ic.size(), oc);
//				ic.force(true);
//			} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
//				e.printStackTrace();
//				progressDialog.dismiss();
////				try {
////					DocumentsContract.deleteDocument(getContentResolver(), uri);
////				} catch (FileNotFoundException e1) {
////					e.printStackTrace();
////				}
////				final int fid = iNet[0] ? R.string.download_failed : R.string.no_internet;
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//				cache.delete();
//				runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show());
//			} finally {
//				startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(TYPE_HTML), uri != null ? REQUEST_CLONE : REQUEST_CREATE);
//			}
//		});
//
//		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.cancel), (dialogInterface, i) -> progressDialog.cancel());
//		progressDialog.setOnShowListener(dialog -> thread.start());
//		progressDialog.setOnCancelListener(dialogInterface -> thread.interrupt());
//		progressDialog.show();
//	}
	private interface OnGetSrc {
		void run(File file);
	}

	private void getSrcFromUri(OnGetSrc cb) {
		// 对话框等待
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.please_wait));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCanceledOnTouchOutside(false);
		final Thread thread = new Thread(() -> {
			boolean interrupted = false;
//			final boolean[] iNet = new boolean[3];
			class AdaptiveUriInputStream {
				private final InputStream is;

				private AdaptiveUriInputStream(Uri uri1) throws NoSuchAlgorithmException, KeyManagementException, IOException {
					String scheme = uri1.getScheme();
					if (scheme == null || !SCH_HTTP.equals(scheme) && !SCH_HTTPS.equals(scheme))
						throw new IOException();
					HttpURLConnection httpURLConnection;
					URL url = new URL(uri1.normalizeScheme().toString());
					if (SCH_HTTPS.equals(scheme)) {
						httpURLConnection = (HttpsURLConnection) url.openConnection();
						if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
							((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new TLSSocketFactory());
					} else httpURLConnection = (HttpURLConnection) url.openConnection();
					httpURLConnection.connect();
					is = httpURLConnection.getInputStream();
//					iNet[0] = true;
				}

				private InputStream get() {
					return is;
				}
			}
			File cache = new File(getCacheDir(), genId()), dest = null;
			try (InputStream isw = new AdaptiveUriInputStream(Uri.parse(getString(R.string.template_repo))).get();
				 FileOutputStream osw = new FileOutputStream(cache);
				 FileInputStream is = new FileInputStream(cache);
				 FileOutputStream os = new FileOutputStream(dest = new File(getCacheDir(), TEMPLATE_FILE_NAME));
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
				if (interrupted) throw new InterruptedException();
				if (!isWiki(cache)) throw new IOException();
				progressDialog.dismiss();
				ic.transferTo(0, ic.size(), oc);
				ic.force(true);
			} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
				e.printStackTrace();
				progressDialog.dismiss();
//				try {
//					DocumentsContract.deleteDocument(getContentResolver(), uri);
//				} catch (FileNotFoundException e1) {
//					e.printStackTrace();
//				}
//				final int fid = iNet[0] ? R.string.download_failed : R.string.no_internet;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				cache.delete();
				runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show());
			} finally {
				cb.run(dest);
			}
		});

		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.cancel), (dialogInterface, i) -> progressDialog.cancel());
		progressDialog.setOnShowListener(dialog -> thread.start());
		progressDialog.setOnCancelListener(dialogInterface -> thread.interrupt());
		progressDialog.show();
	}
//	private void getSrcFromUri(Uri uri, OnGetSrc cb) {
//		// 对话框等待
//		final ProgressDialog progressDialog = new ProgressDialog(this);
//		progressDialog.setMessage(getString(R.string.please_wait));
//		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		progressDialog.setCanceledOnTouchOutside(false);
//		final Thread thread = new Thread(() -> {
//			boolean interrupted = false;
////			final boolean[] iNet = new boolean[3];
//			class AdaptiveUriInputStream {
//				private final InputStream is;
//
//				private AdaptiveUriInputStream(Uri uri1) throws NoSuchAlgorithmException, KeyManagementException, IOException {
//					String scheme = uri1.getScheme();
//					if (scheme == null || !SCH_HTTP.equals(scheme) && !SCH_HTTPS.equals(scheme))
//						throw new IOException();
//					HttpURLConnection httpURLConnection;
//					URL url = new URL(uri1.normalizeScheme().toString());
//					if (SCH_HTTPS.equals(scheme)) {
//						httpURLConnection = (HttpsURLConnection) url.openConnection();
//						if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
//							((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new TLSSocketFactory());
//					} else httpURLConnection = (HttpURLConnection) url.openConnection();
//					httpURLConnection.connect();
//					is = httpURLConnection.getInputStream();
////					iNet[0] = true;
//				}
//
//				private InputStream get() {
//					return is;
//				}
//			}
//			File cache = new File(getCacheDir(), genId()), dest = null;
//			try (InputStream isw = new AdaptiveUriInputStream(uri == null ? Uri.parse(getString(R.string.template_repo)) : uri).get();
//				 FileOutputStream osw = new FileOutputStream(cache);
//				 FileInputStream is = new FileInputStream(cache);
//				 FileOutputStream os = new FileOutputStream(dest = new File(getCacheDir(), uri != null ? CLONING_FILE_NAME : TEMPLATE_FILE_NAME));
//				 FileChannel ic = is.getChannel();
//				 FileChannel oc = os.getChannel()) {
//				// 下载到缓存
//				int length;
//				byte[] bytes = new byte[BUF_SIZE];
//				while ((length = isw.read(bytes)) > -1) {
//					osw.write(bytes, 0, length);
//					if (Thread.currentThread().isInterrupted()) {
//						interrupted = true;
//						break;
//					}
//				}
//				osw.flush();
//				if (interrupted) throw new InterruptedException();
//				if (!isWiki(cache)) throw new IOException();
//				progressDialog.dismiss();
//				ic.transferTo(0, ic.size(), oc);
//				ic.force(true);
//			} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
//				e.printStackTrace();
//				progressDialog.dismiss();
////				try {
////					DocumentsContract.deleteDocument(getContentResolver(), uri);
////				} catch (FileNotFoundException e1) {
////					e.printStackTrace();
////				}
////				final int fid = iNet[0] ? R.string.download_failed : R.string.no_internet;
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//				cache.delete();
//				runOnUiThread(() -> Toast.makeText(MainActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show());
//			} finally {
//				cb.run(dest);
//			}
//		});
//
//		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.cancel), (dialogInterface, i) -> progressDialog.cancel());
//		progressDialog.setOnShowListener(dialog -> thread.start());
//		progressDialog.setOnCancelListener(dialogInterface -> thread.interrupt());
//		progressDialog.show();
//	}

//	// SAF处理
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//		super.onActivityResult(requestCode, resultCode, resultData);
//		File cloning = new File(getCacheDir(), CLONING_FILE_NAME);
//		if (resultCode == Activity.RESULT_OK && resultData != null) {
//			Uri uri = resultData.getData();
//			if (uri != null)
//				switch (requestCode) {
//					case REQUEST_CLONE:
//						cloneWiki(uri);
//						break;
//					case REQUEST_CREATE:    // 新建
//						createWiki(uri);
//						break;
//					case REQUEST_OPEN:    // 导入
//						importWiki(uri);
//						break;
//					case REQUEST_DIR:    // 导入
//						addDir(uri);
//						break;
//				}
//			else if (cloning.exists()) cloning.delete();
//		} else if (cloning.exists()) cloning.delete();
//	}

	private void cloneWiki(Uri uri) {
		createWiki(uri, true);
	}

	private void createWiki(Uri uri) {
		createWiki(uri, false);
	}

	private void createWiki(Uri uri, boolean clone) {
		OnGetSrc cb = file -> {
//				final File template = clone ? new File(getCacheDir(), CLONING_FILE_NAME) : new File(getCacheDir(), TEMPLATE_FILE_NAME);
			if (file.exists() && isWiki(file)) {
//			File lastDir = Environment.getExternalStorageDirectory();
//			boolean showHidden = false;
//			try {
//				lastDir = new File(db.getString(DB_KEY_LAST_DIR));
//				showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			FileDialog.fileSave(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
//				@Override
//				public void onFileTouched(File[] files) {
//					if (files == null || files.length <= 0 || files[0] == null) {
//						Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//						return;
//					}
//					File file = files[0];
				try (FileInputStream is = new FileInputStream(file);
					 OutputStream os = getContentResolver().openOutputStream(uri)) {
//						ic.transferTo(0, ic.size(), oc);
//						ic.force(true);
					if (os == null) throw new FileNotFoundException();
					// 查重
					String id = null;
//						String u = uri.toString();
//						try {
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
//							db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
					if (!MainActivity.writeJson(MainActivity.this, db))
						throw new JSONException((String) null);
					int len = is.available(), length, lenTotal = 0;
					byte[] bytes = new byte[BUF_SIZE];
					while ((length = is.read(bytes)) > -1) {
						os.write(bytes, 0, length);
						lenTotal += length;
					}
					os.flush();
					if (lenTotal != len) throw new IOException();
					getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
//						} catch (Exception e) {
//							e.printStackTrace();
//							Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
//						}
					if (!loadPage(id))
						Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
//				}
//
//				@Override
//				public void onCanceled() {
//
//				}
//			});
			} else {
				Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
//			getTemplate(null);
			}
			if (clone) file.delete();
		};
		if (clone) cb.run(new File(getCacheDir(), CLONING_FILE_NAME));
		else getSrcFromUri(cb);
//		else cb.run(new File(getCacheDir(), TEMPLATE_FILE_NAME));
	}
//	private void createWiki(Uri uri, boolean clone) {
//		final File template = clone ? new File(getCacheDir(), CLONING_FILE_NAME) : new File(getCacheDir(), TEMPLATE_FILE_NAME);
//		if (template.exists() && isWiki(template)) {
////			File lastDir = Environment.getExternalStorageDirectory();
////			boolean showHidden = false;
////			try {
////				lastDir = new File(db.getString(DB_KEY_LAST_DIR));
////				showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
////			} catch (Exception e) {
////				e.printStackTrace();
////			}
////			FileDialog.fileSave(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
////				@Override
////				public void onFileTouched(File[] files) {
////					if (files == null || files.length <= 0 || files[0] == null) {
////						Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
////						return;
////					}
////					File file = files[0];
//			try (FileInputStream is = new FileInputStream(template);
//				 OutputStream os = getContentResolver().openOutputStream(uri)) {
////						ic.transferTo(0, ic.size(), oc);
////						ic.force(true);
//				if (os == null) throw new FileNotFoundException();
//				// 查重
//				String id = null;
////						String u = uri.toString();
////						try {
//				JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa = null;
//				boolean exist = false;
//				Iterator<String> iterator = wl.keys();
//				while (iterator.hasNext()) {
//					exist = uri.toString().equals((wa = wl.getJSONObject((id = iterator.next()))).optString(DB_KEY_URI));
//					if (exist) break;
//				}
//				if (exist)
//					Toast.makeText(MainActivity.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
//				else {
//					wa = new JSONObject();
//					wa.put(DB_KEY_URI, uri.toString());
//					id = genId();
//					wl.put(id, wa);
//				}
//				wa.put(KEY_NAME, KEY_TW);
//				wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
//				wa.put(DB_KEY_BACKUP, false);
////							db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//				if (!MainActivity.writeJson(MainActivity.this, db))
//					throw new JSONException((String) null);
//				int len = is.available(), length, lenTotal = 0;
//				byte[] bytes = new byte[BUF_SIZE];
//				while ((length = is.read(bytes)) > -1) {
//					os.write(bytes, 0, length);
//					lenTotal += length;
//				}
//				os.flush();
//				if (lenTotal != len) throw new IOException();
//				getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
////						} catch (Exception e) {
////							e.printStackTrace();
////							Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
////						}
//				if (!loadPage(id))
//					Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
//			} catch (IOException e) {
//				e.printStackTrace();
//				Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//			} catch (JSONException e) {
//				e.printStackTrace();
//				Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
//			}
////				}
////
////				@Override
////				public void onCanceled() {
////
////				}
////			});
//		} else {
//			Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
////			getTemplate(null);
//		}
//		if (clone) template.delete();
//	}
//	private void newWiki() {    // TODO: Refactor to SAF
//		final File cloning;
//		;
//		final File template = (cloning = new File(getFilesDir(), CLONING_FILE_NAME)).exists() && isWiki(cloning) ? cloning : new File(getFilesDir(), TEMPLATE_FILE_NAME);
//		if (template.exists() && isWiki(template)) {
////			File lastDir = Environment.getExternalStorageDirectory();
////			boolean showHidden = false;
////			try {
////				lastDir = new File(db.getString(DB_KEY_LAST_DIR));
////				showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
////			} catch (Exception e) {
////				e.printStackTrace();
////			}
//			FileDialog.fileSave(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
//				@Override
//				public void onFileTouched(File[] files) {
//					if (files == null || files.length <= 0 || files[0] == null) {
//						Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//						return;
//					}
//					File file = files[0];
//					try (FileInputStream is = new FileInputStream(template);
//						 FileOutputStream os = new FileOutputStream(file);
//						 FileChannel ic = is.getChannel();
//						 FileChannel oc = os.getChannel()) {
//						ic.transferTo(0, ic.size(), oc);
//						ic.force(true);
//						String id = null;
//						String u = Uri.fromFile(file).toString();
//						try {
//							boolean exist = false;
//							JSONObject wl = db.getJSONObject(DB_KEY_WIKI), wa = null;
//							Iterator<String> iterator = wl.keys();
//							while (iterator.hasNext())
//								if ((wa = wl.getJSONObject((id = iterator.next()))).optString(DB_KEY_URI).equals(u)) {
//									exist = true;
//									break;
//								}
//							if (exist)
//								Toast.makeText(MainActivity.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
//							else {
//								wa = new JSONObject();
//								id = genId();
//								wl.put(id, wa);
//							}
//							wa.put(KEY_NAME, KEY_TW);
//							wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
//							wa.put(DB_KEY_URI, u);
//							wa.put(DB_KEY_BACKUP, false);
//							db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//							if (!MainActivity.writeJson(MainActivity.this, db))
//								throw new Exception();
//						} catch (Exception e) {
//							e.printStackTrace();
//							Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
//						}
//						if (!loadPage(id))
//							Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
//					} catch (Exception e) {
//						e.printStackTrace();
//						Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
//					}
//				}
//
//				@Override
//				public void onCanceled() {
//
//				}
//			});
//		} else {
////			getTemplate(null);
//		}
//	}

	private void importWiki(Uri uri) {
//		File lastDir = Environment.getExternalStorageDirectory();
//		boolean showHidden = false;
//		try {
//			lastDir = new File(db.getString(DB_KEY_LAST_DIR));
//			showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		FileDialog.fileOpen(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
//			@Override
//			public void onFileTouched(File[] files) {
//				if (files == null || files.length <= 0 || files[0] == null) {
//					Toast.makeText(MainActivity.this, R.string.failed_opening_file, Toast.LENGTH_SHORT).show();
//					return;
//				}
//				File file = files[0];
		String id = null;
//				String u = Uri.fromFile(file).toString();
		if (!isWiki(this, uri)) {
			Toast.makeText(MainActivity.this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				JSONObject wa = new JSONObject();
				id = genId();
				wa.put(KEY_NAME, KEY_TW);
				wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
				wa.put(DB_KEY_URI, uri.toString());
				wa.put(DB_KEY_BACKUP, false);
				wl.put(id, wa);
			}
//					db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
			if (!MainActivity.writeJson(MainActivity.this, db))
				throw new Exception();
			getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
		}
		if (!loadPage(id))
			Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();

//			}
//
//			@Override
//			public void onCanceled() {
//
//			}
//		});
	}

//	private void importWiki() {    // TODO: Refactor to SAF
////		File lastDir = Environment.getExternalStorageDirectory();
////		boolean showHidden = false;
////		try {
////			lastDir = new File(db.getString(DB_KEY_LAST_DIR));
////			showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////		FileDialog.fileOpen(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
////			@Override
////			public void onFileTouched(File[] files) {
////				if (files == null || files.length <= 0 || files[0] == null) {
////					Toast.makeText(MainActivity.this, R.string.failed_opening_file, Toast.LENGTH_SHORT).show();
////					return;
////				}
////				File file = files[0];
//				String id = null;
//				String u = Uri.fromFile(file).toString();
//				if (!isWiki(file)) {
//					Toast.makeText(MainActivity.this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
//					return;
//				}
//				try {
//					boolean exist = false;
//					JSONObject wl = db.getJSONObject(DB_KEY_WIKI);
//					Iterator<String> iterator = wl.keys();
//					while (iterator.hasNext())
//						if (wl.getJSONObject((id = iterator.next())).optString(DB_KEY_URI).equals(u)) {
//							exist = true;
//							break;
//						}
//					if (exist) {
//						Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
//					} else {
//						JSONObject wa = new JSONObject();
//						id = genId();
//						wa.put(KEY_NAME, KEY_TW);
//						wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
//						wa.put(DB_KEY_URI, u);
//						wa.put(DB_KEY_BACKUP, false);
//						wl.put(id, wa);
//					}
//					db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
//					if (!MainActivity.writeJson(MainActivity.this, db))
//						throw new Exception();
//				} catch (Exception e) {
//					e.printStackTrace();
//					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
//				}
//				if (!loadPage(id))
//					Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
//
////			}
////
////			@Override
////			public void onCanceled() {
////
////			}
////		});
//	}

	private void addDir(Uri uri) {    // TODO: dev
//		try {
		DocumentFile mdf = DocumentFile.fromTreeUri(this, uri);
		if (mdf == null || !mdf.isDirectory()) {
			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			return;
		}
		DocumentFile p, index = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
//			DocumentFile p, index = (p = mdf.findFile(KEY_FN_INDEX))!=null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2))!=null && p.isFile() ? p : mdf.createFile(TYPE_HTML, KEY_FN_INDEX_D);
		if (index == null) {
			DocumentFile nf = mdf.createFile(TYPE_HTML, KEY_FN_INDEX);
			if (nf == null) {
				Toast.makeText(this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
				return;
			}
			getSrcFromUri(file -> {
				try (FileInputStream is = new FileInputStream(file);
					 OutputStream os = getContentResolver().openOutputStream(nf.getUri())) {
					if (os == null) throw new IOException();
					int len = is.available(), length, lenTotal = 0;
					byte[] bytes = new byte[BUF_SIZE];
					while ((length = is.read(bytes)) > -1) {
						os.write(bytes, 0, length);
						lenTotal += length;
					}
					os.flush();
					if (lenTotal != len) throw new IOException();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
				}
				addDir(uri);
			});
//			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			return;
		} else if (!isWiki(this, index.getUri())) {
			Toast.makeText(this, R.string.wiki_not_exist, Toast.LENGTH_SHORT).show();
			return;
		}
//		System.out.println(index.exists());
//		System.out.println(index.getName());
//		System.out.println(index.canRead());
//		System.out.println(index.canWrite());
//		System.out.println(index.getType());
//		System.out.println(index.getUri());
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
				Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
			} else {
				JSONObject wa = new JSONObject();
				id = genId();
				wa.put(KEY_NAME, KEY_TW);
				wa.put(DB_KEY_SUBTITLE, STR_EMPTY);
				wa.put(DB_KEY_URI, uri.toString());
				wa.put(DB_KEY_BACKUP, false);
				wl.put(id, wa);
			}
//					db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
			if (!MainActivity.writeJson(MainActivity.this, db))
				throw new Exception();
			getContentResolver().takePersistableUriPermission(uri, TAKE_FLAGS);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
		}
		if (!loadPage(id))
			Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();

//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

//	private void iNavigate() {
//		final SearchView view = new SearchView(this);
//		view.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
//		view.setImeOptions(EditorInfo.IME_ACTION_GO);
//		view.setQueryHint(getString(R.string.url));
//		view.onActionViewExpanded();
//		view.setSubmitButtonEnabled(true);
//		final String STR_EMPTY = "",
//				KEY_S = "s",
//				KEY_AS = "AS",
//				KEY_RESULTS = "Results",
//				KEY_SUGGESTS = "Suggests",
//				KEY_SUGGESTION = "suggestion",
//				KEY_TXT = "Txt",
//				KEY_SRC = "src",
//				KEY_SE = "se",
//				KEY_SUG = "sug",
//				KEY_DIRECT = "mark2",
//				KEY_DATA = "data";
//		final String[] SUG_COLUMNS = {"_id", "name", "mark", "mark2", "mark3"},
//				SUG_ADAPTER_COLUMNS = {"mark", MainActivity.KEY_NAME, "mark3", "mark2"};
//		final NoLeakHandler handler = new NoLeakHandler(msg -> {
//			Bundle data = msg.getData();
//			String src = data.getString(KEY_SRC);
//			String se = data.getString(KEY_SE);
//			Uri uri = Uri.parse(src);
//			String sch = uri.getScheme();
//			String[] sug = data.getStringArray(KEY_SUG);
//			MatrixCursor cursor = new MatrixCursor(SUG_COLUMNS);
//			int i = 0;
//			Uri uri1 = sch == null ? Uri.parse(SCH_EX_HTTP + src) : null;
//			String hos1 = uri1 != null ? uri1.getHost() : null;
//			if (sch != null && sch.length() > 0 || hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1) {
//				cursor.addRow(new CharSequence[]{String.valueOf(i), src, getString(R.string.mark_Go), STR_EMPTY, getString(R.string.mark_Return)});
//				i++;
//			}
//			cursor.addRow(new CharSequence[]{String.valueOf(i), src, getString(R.string.mark_Search), se != null ? se : STR_EMPTY, i > 0 ? STR_EMPTY : getString(R.string.mark_Return)});
//			i++;
//			if (sug != null)
//				for (String v : sug) {
//					cursor.addRow(new CharSequence[]{String.valueOf(i), v, getString(R.string.mark_Search), se, i > 0 ? STR_EMPTY : getString(R.string.mark_Return)});
//					i++;
//				}
//			if (view.getSuggestionsAdapter() == null) {
//				SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(view.getContext(), R.layout.suggestion_slot, cursor, SUG_ADAPTER_COLUMNS, new int[]{R.id.t_sug_mark, R.id.t_sug, R.id.t_sug_first, R.id.t_sug_se}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
//				view.setSuggestionsAdapter(simpleCursorAdapter);
//			} else {
//				view.getSuggestionsAdapter().changeCursor(cursor);
//			}
//		});
//		final AlertDialog URLDialog = new AlertDialog.Builder(MainActivity.this)
//				.setView(view)
//				.show();
//
//		view.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
//			@Override
//			public boolean onSuggestionSelect(int position) {
//				return false;
//			}
//
//			@Override
//			public boolean onSuggestionClick(int position) {
//				MatrixCursor c = (MatrixCursor) view.getSuggestionsAdapter().getItem(position);
//				String res = c.getString(c.getColumnIndex(MainActivity.KEY_NAME));
//				boolean direct = c.getString(c.getColumnIndex(KEY_DIRECT)).length() == 0;
//				String vScheme = Uri.parse(res).getScheme();
//				Intent in = new Intent();
//				Bundle bu = new Bundle();
//				if (direct && vScheme != null && vScheme.length() > 0)
//					bu.putString(KEY_URL, res);
//				else if (direct) bu.putString(KEY_URL, SCH_EX_HTTP + res);
//				else bu.putString(KEY_URL, wSearch(res));
//				in.putExtras(bu).setClass(MainActivity.this, TWEditorWV.class);
//				startActivity(in);
//				URLDialog.dismiss();
//				return true;
//			}
//		});
//
//		view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//			@Override
//			public boolean onQueryTextSubmit(String query) {
//				Intent in = new Intent();
//				Bundle bu = new Bundle();
//				Uri uri = Uri.parse(query);
//				String sch = uri.getScheme();
//				Uri uri1 = sch == null ? Uri.parse(SCH_EX_HTTP + query) : null;
//				String hos1 = uri1 != null ? uri1.getHost() : null;
//				if (sch != null && sch.length() > 0)
//					bu.putString(KEY_URL, query);
//				else if (hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1)
//					bu.putString(KEY_URL, SCH_EX_HTTP + query);
//				else bu.putString(KEY_URL, wSearch(query));
//				in.putExtras(bu).setClass(MainActivity.this, TWEditorWV.class);
//				startActivity(in);
//				URLDialog.dismiss();
//				return true;
//			}
//
//			@Override
//			public boolean onQueryTextChange(final String newText) {
//				if (newText.length() > 0)
//					new Thread() {
//						public void run() {
//							try {
//								String se = db.getString(DB_KEY_SEARCH_ENGINE);
//								String res;
//								Message msg = new Message();
//								Bundle data = new Bundle();
//								JSONArray array = null;
//								String ses = null;
//								try {
//									switch (se) {
//										case SE_GOOGLE:
//											List<String> attrs = Jsoup.connect(getString(R.string.su_google).replace(PREF_SU, newText)).ignoreContentType(true).get().getElementsByTag(KEY_SUGGESTION).eachAttr(KEY_DATA);
//											String[] vGoogle = attrs.toArray(new String[0]);
//											data.putStringArray(KEY_SUG, vGoogle);
//											ses = getString(R.string.google);
//											break;
//										case SE_BING:
//											res = Jsoup.connect(getString(R.string.su_bing).replace(PREF_SU, newText)).ignoreContentType(true).get().body().html();
//											JSONArray arrayBing = new JSONObject(res).getJSONObject(KEY_AS).getJSONArray(KEY_RESULTS).getJSONObject(0).getJSONArray(KEY_SUGGESTS);
//											int k = arrayBing.length();
//											String[] vBing = new String[k];
//											for (int i = 0; i < k; i++)
//												vBing[i] = arrayBing.getJSONObject(i).getString(KEY_TXT);
//											data.putStringArray(KEY_SUG, vBing);
//											ses = getString(R.string.bing);
//											break;
//										case SE_BAIDU:
//											res = Jsoup.connect(getString(R.string.su_baidu).replace(PREF_SU, newText)).get().body().html();
//											array = new JSONObject(res.substring(res.indexOf('(') + 1, res.lastIndexOf(')'))).getJSONArray(KEY_S);
//											ses = getString(R.string.baidu);
//											break;
//										case SE_SOGOU:
//											res = Jsoup.connect(getString(R.string.su_sogou).replace(PREF_SU, newText)).ignoreContentType(true).get().body().html();
//											array = new JSONObject(STR_EMPTY + '{' + '"' + 's' + '"' + ':' + res.substring(res.indexOf('['), res.lastIndexOf(']') + 1) + '}').getJSONArray(KEY_S).getJSONArray(1);
//											ses = getString(R.string.sogou);
//											break;
//									}
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//								if (array != null) {
//									int k = array.length();
//									String[] v = new String[k];
//									for (int i = 0; i < k; i++)
//										v[i] = array.getString(i);
//									data.putStringArray(KEY_SUG, v);
//								}
//								data.putString(KEY_SRC, newText);
//								data.putString(KEY_SE, ses);
//								msg.setData(data);
//								handler.sendMessage(msg);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}.start();
//				return true;
//			}
//		});
//	}

//	private void openCfg() {
//		View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.settings_dialog, null);
//		final Spinner spnSE = view.findViewById(R.id.spnSE);
//		spnSE.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.ext_slot, new String[]{
//				getText(R.string.google).toString(),
//				getText(R.string.bing).toString(),
//				getText(R.string.baidu).toString(),
//				getText(R.string.sogou).toString(),
//				getText(R.string.custom).toString()
//		}));
//		final EditText vCSE = view.findViewById(R.id.customSE);
//		final CheckBox sh = view.findViewById(R.id.cbHidden);
//		try {
//			String seStr = db.getString(DB_KEY_SEARCH_ENGINE);
//			switch (seStr) {
//				case SE_GOOGLE:
//					spnSE.setSelection(0);
//					break;
//				case SE_BING:
//					spnSE.setSelection(1);
//					break;
//				case SE_BAIDU:
//					spnSE.setSelection(2);
//					break;
//				case SE_SOGOU:
//					spnSE.setSelection(3);
//					break;
//				case SE_CUSTOM:
//					spnSE.setSelection(4);
//					break;
//			}
//			vCSE.setEnabled(spnSE.getSelectedItemPosition() == 4);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		try {
//			vCSE.setText(db.getString(DB_KEY_CSE));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		try {
//			sh.setChecked(db.getBoolean(DB_KEY_SHOW_HIDDEN));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		AlertDialog settingDialog = new AlertDialog.Builder(MainActivity.this)
//				.setTitle(R.string.action_settings)
//				.setView(view)
//				.setNegativeButton(android.R.string.cancel, null)
//				.setPositiveButton(android.R.string.ok, (dialog, which) -> {
//					try {
//						String vse = SE_GOOGLE;
//						switch (spnSE.getSelectedItemPosition()) {
//							case 0:
//								vse = SE_GOOGLE;
//								break;
//							case 1:
//								vse = SE_BING;
//								break;
//							case 2:
//								vse = SE_BAIDU;
//								break;
//							case 3:
//								vse = SE_SOGOU;
//								break;
//							case 4:
//								vse = SE_CUSTOM;
//								break;
//						}
//						db.put(DB_KEY_SEARCH_ENGINE, vse);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					try {
//						String cse = vCSE.getText().toString();
//						if (cse.length() > 0) {
//							Uri uri = Uri.parse(vCSE.getText().toString());
//							String sch = uri.getScheme();
//							if (sch == null)
//								uri = Uri.parse(SCH_EX_HTTP + uri.toString());
//							db.put(DB_KEY_CSE, uri.toString());
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					try {
//						db.put(DB_KEY_SHOW_HIDDEN, sh.isChecked());
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					try {
//						writeJson(MainActivity.this, db);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				})
//				.create();
//		settingDialog.setCanceledOnTouchOutside(false);
//		settingDialog.show();
//		final Button ok = settingDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//		view.findViewById(R.id.btnUpdateTemplate).setOnClickListener(v -> getTemplate(false));
//		spnSE.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				if (position == 4) vCSE.setVisibility(View.VISIBLE);
//				else vCSE.setVisibility(View.GONE);
//				vCSE.setEnabled(position == 4);
//				ok.setEnabled(!vCSE.isEnabled() || vCSE.getText().toString().length() > 0);
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//
//			}
//		});
//		vCSE.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				ok.setEnabled(!vCSE.isEnabled() || vCSE.getText().toString().length() != 0);
//			}
//		});
//	}

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Window w = getWindow();
			int color = getColor(R.color.design_default_color_primary);
			w.setStatusBarColor(color);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				w.setNavigationBarColor(color);
			w.getDecorView().setSystemUiVisibility((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : View.SYSTEM_UI_FLAG_VISIBLE) : View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}

	private static JSONObject initJson(Context context) {
		File ext = context.getExternalFilesDir(null), file = null;
		if (ext != null)
			try (InputStream is = new FileInputStream(file = new File(ext, DB_FILE_NAME))) {
				byte[] b = new byte[is.available()];
				if (is.read(b) < 0) throw new IOException();
				JSONObject jsonObject = new JSONObject(new String(b));
				if (!jsonObject.has(DB_KEY_WIKI)) jsonObject.put(DB_KEY_WIKI, new JSONObject());
//				if (!jsonObject.has(DB_KEY_SEARCH_ENGINE))
//					jsonObject.put(DB_KEY_SEARCH_ENGINE, R.string.default_se);
//				if (!jsonObject.has(DB_KEY_SHOW_HIDDEN)) jsonObject.put(DB_KEY_SHOW_HIDDEN, false);
//				if (!jsonObject.has(DB_KEY_LAST_DIR))
//					jsonObject.put(DB_KEY_LAST_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
				return jsonObject;
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			} finally {
				if (file != null) {
					file.delete();
				}
			}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(DB_KEY_WIKI, new JSONObject());
//			jsonObject.put(DB_KEY_SEARCH_ENGINE, R.string.default_se);
//			jsonObject.put(DB_KEY_SHOW_HIDDEN, false);
//			jsonObject.put(DB_KEY_LAST_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}


	static JSONObject readJson(Context context) throws Exception {
		try (InputStream is = context.openFileInput(DB_FILE_NAME)) {
			byte[] b = new byte[is.available()];
			if (is.read(b) < 0) throw new IOException();
			return new JSONObject(new String(b));
		}
	}

	static boolean writeJson(Context context, JSONObject vdb) {
		try (FileOutputStream os = context.openFileOutput(DB_FILE_NAME, MODE_PRIVATE)) {
			byte[] b = vdb.toString(2).getBytes();
			os.write(b);
			os.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static void exportJson(Context context, JSONObject vdb) {
		File ext = context.getExternalFilesDir(null);
		if (ext == null) return;
		try (OutputStream os = new FileOutputStream(new File(ext, DB_FILE_NAME))) {
			byte[] b = vdb.toString(2).getBytes();
			os.write(b);
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TargetApi(23)
	static void checkPermission(Context context) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}
	}

	static String genId() {
		return UUID.randomUUID().toString();
	}

//	static boolean isWiki(String data) {
//		try {
//			if (data == null) throw new IOException();
//			return isWiki(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), Uri.parse(null));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	static boolean isWiki(Context context, Uri uri) {
		try {
			return SCH_HTTP.equals(uri.getScheme()) || SCH_HTTPS.equals(uri.getScheme()) || isWiki(SCH_FILE.equals(uri.getScheme()) ? new FileInputStream(uri.getPath()) : context.getContentResolver().openInputStream(uri), uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	static boolean isWiki(File file) {
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
//		if (!chk.isFile()) return false;
//		if (!main.isFile() || !chk.isFile()) return false;
		if (main == null || chk == null) return false;
		String mfn1 = main.substring(0, main.lastIndexOf('.')),
				mfn2 = main.substring(main.lastIndexOf('.') + 1);
		int efp1, efp2;
//		String efn = chk.getName();
//		if (efn == null) return false;
		String efn1 = chk.substring(0, (efp1 = chk.lastIndexOf('.', (efp2 = chk.lastIndexOf('.')) - 1))),
				efn2 = chk.substring(efp1 + 1, efp2),
				efn3 = chk.substring(efp2 + 1);
//		String efn1 = efn.substring(0, (efp1 = efn.lastIndexOf('.', (efp2 = efn.lastIndexOf('.')) - 1))),
//				efn2 = efn.substring(efp1 + 1, efp2),
//				efn3 = efn.substring(efp2 + 1);
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
		if (!legacy) try {    // tree模式
			mdf = DocumentFile.fromTreeUri(context, u);    // 根目录
			if (mdf == null) throw new IOException();
			DocumentFile p;
			df = (p = mdf.findFile(KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(KEY_FN_INDEX2)) != null && p.isFile() ? p : null;    // index.htm(l)
			String fn;
			if ((df == null) || !df.isFile() || (fn = df.getName()) == null)
				throw new IOException();
			if ((p = mdf.findFile(fn = fn + BACKUP_POSTFIX)) == null) {
				mdf = mdf.createDirectory(fn);
			} else mdf = p;
			if (mdf == null) throw new IOException();
			tree = true;
		} catch (IllegalArgumentException ignored) {
		}
		DocumentFile bdf = null;
		String mfn;
		String bfn;
		File file = null, mfd = null;
		if (!tree) {
			file = legacy ? new File(u.getPath()) : new File(new File(context.getExternalFilesDir(null), Uri.encode(u.getSchemeSpecificPart())), (df = DocumentFile.fromSingleUri(context, u)) != null && df.getName() != null ? df.getName() : KEY_FN_INDEX);    // real file in the dest dir for file:// or virtual file in ext files dir for content://
			df = legacy ? DocumentFile.fromFile(file) : df;
			mfn = file.getName();
//		System.out.println(file.getAbsolutePath());
			bfn = new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), formatBackup(df != null ? df.lastModified() : 0)).toString();
			mfd = new File(file.getParentFile(), mfn + BACKUP_POSTFIX);
			if (!mfd.exists()) mfd.mkdirs();
			if (!mfd.isDirectory()) throw new IOException();
		} else {
			mfn = df.getName();
			bfn = new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), formatBackup(df.lastModified())).toString();
			if ((bdf = mdf.createFile(TYPE_HTML, bfn.substring(0, bfn.lastIndexOf('.')))) == null)
				throw new IOException();
		}
		try (InputStream is = legacy ? new FileInputStream(file) : context.getContentResolver().openInputStream(tree ? df.getUri() : u);
			 OutputStream os = tree ? context.getContentResolver().openOutputStream(bdf.getUri()) : new FileOutputStream(new File(mfd, bfn))) {
			if (is == null || os == null) throw new IOException();
			int len = is.available(), length, lenTotal = 0;
			byte[] bytes = new byte[BUF_SIZE];
			while ((length = is.read(bytes)) > -1) {
				os.write(bytes, 0, length);
				lenTotal += length;
			}
			os.flush();
			if (lenTotal != len) throw new IOException();
//			ic.transferTo(0, ic.size(), oc);
//			ic.force(true);
		}
	}
//	static void backup(File file) throws IOException {
//		String mfn = file.getName();
//		File mfd = new File(file.getParentFile(), mfn + MainActivity.BACKUP_DIRECTORY_PATH_PREFIX);
//		if (!mfd.exists()) mfd.mkdir();
//		if (!mfd.isDirectory()) throw new IOException();
//		try (FileInputStream is = new FileInputStream(file);
//			 FileOutputStream os = new FileOutputStream(new File(mfd, new StringBuilder(mfn).insert(mfn.lastIndexOf('.'), MainActivity.formatBackup(file.lastModified())).toString()));
//			 FileChannel ic = is.getChannel();
//			 FileChannel oc = os.getChannel()) {
//			ic.transferTo(0, ic.size(), oc);
//			ic.force(true);
//		}
//	}

//	private String wSearch(String arg) {
//		String ws = getString(R.string.s_google).replace(PREF_S, arg);
//		try {
//			String se = db.getString(DB_KEY_SEARCH_ENGINE);
//			switch (se) {
//				case SE_GOOGLE:
//					ws = getString(R.string.s_google).replace(PREF_S, arg);
//					break;
//				case SE_BING:
//					ws = getString(R.string.s_bing).replace(PREF_S, arg);
//					break;
//				case SE_BAIDU:
//					ws = getString(R.string.s_baidu).replace(PREF_S, arg);
//					break;
//				case SE_SOGOU:
//					ws = getString(R.string.s_sogou).replace(PREF_S, arg);
//					break;
//				case SE_CUSTOM:
//					ws = db.getString(DB_KEY_CSE).replace(PREF_S, arg);
//					break;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return ws;
//	}

//	interface OnDownloadCompleteListener {
//		void onDownloadComplete(File file);
//
//		void onDownloadFailed();
//	}
//
//	interface DownloadChecker {
//		boolean checkNg(File file);
//	}


//	static void wGet(Context parent, Uri uri, File dest) {
//		wGet(parent, uri, dest, false, false, null, null);
//	}
//
//	private static void wGet(final Context parent, Uri uri, final File dest, final boolean noNotification, final boolean noToast, final DownloadChecker checker, final OnDownloadCompleteListener listener) {
//		final String KEY_TOAST = "toast",
//				KEY_COMPLETE = "complete",
//				KEY_FAILED = "failed",
//				KEY_FILEPATH = "filepath";
//		String sch = uri.getScheme();
//		if (sch == null || sch.length() == 0)
//			uri = Uri.parse(SCH_EX_HTTP + uri.toString());
//		try {
//			final String id = MainActivity.genId().substring(0, 7);
//			final int idt = Integer.parseInt(id, 16);
//			final File cacheFile = new File(parent.getCacheDir(), id);
//			final Uri uriX = uri;
//			final NoLeakHandler handler = new NoLeakHandler(msg -> {
//				if (msg != null) {
//					Bundle data = msg.getData();
//					if (data != null) {
//						int toast = data.getInt(KEY_TOAST, -1);
//						String filepath = data.getString(KEY_FILEPATH);
//						if (toast != -1)
//							Toast.makeText(parent, toast, Toast.LENGTH_SHORT).show();
//						if (data.getBoolean(KEY_COMPLETE) && filepath != null && listener != null)
//							listener.onDownloadComplete(new File(filepath));
//						else if (data.getBoolean(KEY_FAILED) && listener != null)
//							listener.onDownloadFailed();
//					}
//				}
//			});
//			new Thread(() -> {
//				Message msg;
//				Bundle bundle = new Bundle();
//				final int[] len = new int[1];
//				class AdaptiveUriInputStream {
//					private final InputStream is;
//
//					private AdaptiveUriInputStream(Uri uri1) throws IOException, NoSuchAlgorithmException, KeyManagementException {
//						if (uri1.getScheme() != null && uri1.getScheme().equals(SCHEME_BLOB_B64)) {
//							String b64 = uri1.getSchemeSpecificPart();
//							byte[] bytes = Base64.decode(b64, Base64.NO_PADDING);
//							is = new ByteArrayInputStream(bytes);
//							len[0] = bytes.length;
//						} else {
//							HttpURLConnection httpURLConnection;
//							URL url = new URL(uri1.normalizeScheme().toString());
//							if (uri1.getScheme() != null && uri1.getScheme().equals("https")) {
//								httpURLConnection = (HttpsURLConnection) url.openConnection();
//								if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
//									((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new TLSSocketFactory());
//							} else httpURLConnection = (HttpURLConnection) url.openConnection();
//							httpURLConnection.connect();
//							len[0] = httpURLConnection.getContentLength();
//							is = httpURLConnection.getInputStream();
//						}
//					}
//
//					private InputStream get() {
//						return is;
//					}
//				}
//				try (InputStream isw = new AdaptiveUriInputStream(uriX).get();
//					 FileOutputStream osw = new FileOutputStream(cacheFile);
//					 FileInputStream is = new FileInputStream(cacheFile);
//					 FileOutputStream os = new FileOutputStream(dest);
//					 FileChannel ic = is.getChannel();
//					 FileChannel oc = os.getChannel()) {
//					int length;
//					int lengthTotal = 0;
//					byte[] bytes = new byte[4096];
//					if (!noToast) {
//						bundle.putInt(KEY_TOAST, R.string.downloading);
//						msg = new Message();
//						msg.setData(bundle);
//						handler.sendMessage(msg);
//					}
//					Notification notification;
//					NotificationManager notificationManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//						notificationManager.createNotificationChannel(new NotificationChannel(id, id, NotificationManager.IMPORTANCE_LOW));
//					while ((length = isw.read(bytes)) != -1) {
//						osw.write(bytes, 0, length);
//						lengthTotal += length;
//						int p = Math.round((float) lengthTotal / (float) len[0] * 100);
//						if (!noNotification) {
//							notification = new NotificationCompat.Builder(parent, id)
//									.setSmallIcon(R.drawable.ic_download)
//									.setContentTitle(parent.getString(R.string.downloading))
//									.setContentText(String.valueOf(p) + '%')
//									.setOngoing(true)
//									.setShowWhen(true)
//									.setProgress(100, p, false)
//									.build();
//							notificationManager.notify(id, idt, notification);
//						}
//
//					}
//					osw.flush();
//					if (len[0] > 0 && lengthTotal < len[0] || checker != null && checker.checkNg(cacheFile))
//						throw new Exception();
//					if (!noNotification) {
//						notification = new NotificationCompat.Builder(parent, id)
//								.setSmallIcon(R.drawable.ic_download)
//								.setContentTitle(parent.getString(R.string.downloading))
//								.setOngoing(true)
//								.setShowWhen(true)
//								.setProgress(0, 0, true)
//								.build();
//						notificationManager.notify(id, idt, notification);
//					}
//					ic.transferTo(0, ic.size(), oc);
//					ic.force(true);
//					if (!noNotification) notificationManager.cancel(id, idt);
//					if (!noToast)
//						bundle.putInt(KEY_TOAST, R.string.download_complete);
//					bundle.putBoolean(KEY_COMPLETE, true);
//					bundle.putString(KEY_FILEPATH, dest.getAbsolutePath());
//					msg = new Message();
//					msg.setData(bundle);
//					handler.sendMessage(msg);
//					cacheFile.delete();
//				} catch (Exception e) {
//					e.printStackTrace();
//					if (!noToast) {
//						bundle.putInt(KEY_TOAST, R.string.download_failed);
//					}
//					bundle.putBoolean(KEY_FAILED, true);
//					msg = new Message();
//					msg.setData(bundle);
//					handler.sendMessage(msg);
//					cacheFile.delete();
//				}
//			}).start();
//		} catch (Exception e) {
//			e.printStackTrace();
//			Toast.makeText(parent, R.string.download_failed, Toast.LENGTH_SHORT).show();
//		}
//	}

	private static String formatBackup(long time) {
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