/*
 * top.donmor.tiddloid.MainActivity <= [P|Tiddloid]
 * Last modified: 18:18:25 2019/05/10
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.javascript.Scriptable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import top.donmor.tiddloid.utils.NoLeakHandler;
import top.donmor.tiddloid.utils.TLSSocketFactory;

import com.github.donmor.filedialog.lib.FileDialog;
import com.github.donmor.filedialog.lib.FileDialogFilter;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
	private RecyclerView rvWikiList;
	private TextView noWiki;
	private WikiListAdapter wikiListAdapter;
	private JSONObject db;

	// CONSTANT
	static final FileDialogFilter[] HTML_FILTERS = {new FileDialogFilter(".html;.htm", new String[]{".html", ".htm"})};
	static final String SCHEME_BLOB_B64 = "blob-b64",
			BACKUP_DIRECTORY_PATH_PREFIX = "_backup",
			KEY_NAME = "name",
			KEY_FAVICON = "favicon",
			KEY_ID = "id",
			KEY_URL = "url",
			DB_FILE_NAME = "data.json",
			DB_KEY_SHOW_HIDDEN = "showHidden",
			DB_KEY_LAST_DIR = "lastDir",
			DB_KEY_WIKI = "wiki",
			DB_KEY_PATH = "path",
			DB_KEY_BACKUP = "backup";
	private static final String DB_KEY_CSE = "customSearchEngine",
			DB_KEY_SEARCH_ENGINE = "searchEngine",
			KEY_APPLICATION_NAME = "application-name",
			KEY_LBL = " — ",
			KEY_CONTENT = "content",
			KEY_VERSION = "version",
			KEY_VERSION_AREA = "versionArea",
			KEY_TITLE = "title",
			SE_GOOGLE = "Google",
			SE_BING = "Bing",
			SE_BAIDU = "Baidu",
			SE_SOGOU = "Sogou",
			SE_CUSTOM = "Custom",
			PREF_VER_1 = "var version",
			PREF_VER_2 = "};",
			PREF_S = "%s",
			PREF_SU = "#content#",
			SCH_EX_HTTP = "http://",
			TEMPLATE_FILE_NAME = "template.html",
			CHARSET_DEFAULT = "UTF-8",
			CLASS_MENU_BUILDER = "MenuBuilder",
			METHOD_SET_OPTIONAL_ICONS_VISIBLE = "setOptionalIconsVisible";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.activity_main);
		File templateOnStart = new File(getFilesDir(), TEMPLATE_FILE_NAME);
		if (!templateOnStart.exists() || !(new TWInfo(MainActivity.this, templateOnStart).isWiki)) {
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getResources().getString(R.string.please_wait));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					wGet(MainActivity.this, Uri.parse(getResources().getString(R.string.template_repo)), new File(getFilesDir(), TEMPLATE_FILE_NAME), true, true, new DownloadChecker() {
						@Override
						public boolean checkNg(File file) {
							return !(new TWInfo(MainActivity.this, file).isWiki);
						}
					}, new OnDownloadCompleteListener() {
						@Override
						public void onDownloadComplete(File file) {
							if (file.exists())
								Toast.makeText(MainActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
							else
								Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
							progressDialog.dismiss();
						}

						@Override
						public void onDownloadFailed() {
							Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
							progressDialog.dismiss();
						}
					});
				}
			});
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage(R.string.missing_template)
					.setPositiveButton(android.R.string.ok, null)
					.show();
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					progressDialog.show();
				}
			});
		}

		try {
			db = readJson(openFileInput(DB_FILE_NAME));
			if (db == null) throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			db = new JSONObject();
			try {
				db.put(DB_KEY_SEARCH_ENGINE, R.string.default_se);
				db.put(DB_KEY_SHOW_HIDDEN, false);
				db.put(DB_KEY_WIKI, new JSONArray());
				db.put(DB_KEY_LAST_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
				writeJson(openFileOutput(DB_FILE_NAME, MODE_PRIVATE), db);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			MainActivity.this.getWindow().setStatusBarColor(Color.WHITE);
			MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			checkPermission();
		}
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		noWiki = findViewById(R.id.t_noWiki);
		try {
			if (db.getJSONArray(DB_KEY_WIKI).length() == 0)
				noWiki.setVisibility(View.VISIBLE);
			else
				noWiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					public void run() {
						MainActivity.this.onResume();
						refreshLayout.setRefreshing(false);
					}
				}, 1000);
			}
		});
		rvWikiList = findViewById(R.id.rvWikiList);
		rvWikiList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
		wikiListAdapter = new WikiListAdapter(this, db);
		wikiListAdapter.setReloadListener(new WikiListAdapter.ReloadListener() {
			@Override
			public void onReloaded(int count) {
				if (count > 0) noWiki.setVisibility(View.GONE);
				else noWiki.setVisibility(View.VISIBLE);
			}
		});
		wikiListAdapter.setOnItemClickListener(new WikiListAdapter.ItemClickListener() {
			@Override
			public void onItemClick(int position) {
				String id = wikiListAdapter.getId(position);
				String vp = null;
				int mp = 0, ep = 0;
				try {
					ep = db.getJSONArray(DB_KEY_WIKI).length();
					for (int i = 0; i < ep; i++) {
						if (db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(KEY_ID).equals(id)) {
							vp = db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(DB_KEY_PATH);
							break;
						}
						mp++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (id != null && vp != null && mp < ep) {
					File f = new File(vp);
					if (new TWInfo(MainActivity.this, f).isWiki) {
						if (!loadPage(id))
							Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
					} else {
						final int p = mp;
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(android.R.string.dialog_alert_title)
								.setMessage(R.string.confirm_to_auto_remove_wiki)
								.setNegativeButton(android.R.string.no, null)
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										try {
											if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
												db.put(DB_KEY_WIKI, removeUnderK(db.getJSONArray(DB_KEY_WIKI), p));
											else
												db.getJSONArray(DB_KEY_WIKI).remove(p);
											writeJson(openFileOutput(DB_FILE_NAME, MODE_PRIVATE), db);
										} catch (Exception e) {
											e.printStackTrace();
										}
										MainActivity.this.onResume();
									}
								}).show();
					}
				} else {
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onItemLongClick(final int position) {
				try {
					final JSONObject wikiData = db.getJSONArray(DB_KEY_WIKI).getJSONObject(position);
					View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.wikiconfig_dialog, null);
					final Button btnWikiConfigPath = view.findViewById(R.id.btnWikiConfigPath);
					btnWikiConfigPath.setText(wikiData.getString(DB_KEY_PATH));
					final CheckBox cbBackup = view.findViewById(R.id.cbBackup);
					cbBackup.setChecked(wikiData.getBoolean(DB_KEY_BACKUP));
					final LinearLayout frmBackupList = view.findViewById(R.id.frmBackupList);
					if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE);
					else frmBackupList.setVisibility(View.GONE);
					final TextView lblNoBackup = view.findViewById(R.id.lblNoBackup);
					final RecyclerView rvBackupList = view.findViewById(R.id.rvBackupList);
					rvBackupList.setLayoutManager(new LinearLayoutManager(view.getContext()));
					Button btnCreateShortcut = view.findViewById(R.id.btnCreateShortcut);
					Button btnRemoveWiki = view.findViewById(R.id.btnRemoveWiki);


					final AlertDialog wikiConfigDialog = new AlertDialog.Builder(MainActivity.this)
							.setTitle(wikiData.getString(MainActivity.KEY_NAME))
							.setIcon(getResources().getDrawable(R.drawable.ic_description))
							.setView(view)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									MainActivity.this.onResume();
								}
							})
							.create();
					FileInputStream is = null;
					Bitmap iconX = null;
					try {
						is = new FileInputStream(new File(getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), wikiListAdapter.getId(position)));
						iconX = BitmapFactory.decodeStream(is);
						if (iconX != null)
							wikiConfigDialog.setIcon(new BitmapDrawable(getResources(), iconX));
						else throw new Exception();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (is != null) try {
							is.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					final Bitmap icon = iconX;
					final BackupListAdapter backupListAdapter = new BackupListAdapter(wikiConfigDialog.getContext());
					backupListAdapter.setOnBtnClickListener(new BackupListAdapter.BtnClickListener() {
						@Override
						public void onBtnClick(int position, int which) {
							final File f = backupListAdapter.getBackupFile(position);
							if (f != null && f.exists())
								switch (which) {
									case 1:
										new AlertDialog.Builder(wikiConfigDialog.getContext())
												.setTitle(android.R.string.dialog_alert_title)
												.setMessage(R.string.confirm_to_rollback)
												.setNegativeButton(android.R.string.no, null)
												.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														FileInputStream is = null;
														FileOutputStream os = null;
														try {
															is = new FileInputStream(f);
															os = new FileOutputStream(new File(btnWikiConfigPath.getText().toString()));
															int len = is.available();
															int length, lengthTotal = 0;
															byte[] b = new byte[4096];
															while ((length = is.read(b)) != -1) {
																os.write(b, 0, length);
																lengthTotal += length;
															}
															os.flush();
															if (lengthTotal != len)
																throw new Exception();
															wikiConfigDialog.dismiss();
															Toast.makeText(MainActivity.this, R.string.wiki_rolled_back_successfully, Toast.LENGTH_SHORT).show();
															loadPage(wikiData.getString(KEY_ID));
														} catch (Exception e) {
															e.printStackTrace();
															Toast.makeText(MainActivity.this, R.string.failed_writing_file, Toast.LENGTH_SHORT).show();
														} finally {
															if (is != null)
																try {
																	is.close();
																} catch (Exception e) {
																	e.printStackTrace();
																}
															if (os != null)
																try {
																	os.close();
																} catch (Exception e) {
																	e.printStackTrace();
																}
														}
													}
												})
												.show();
										break;
									case 2:
										new AlertDialog.Builder(wikiConfigDialog.getContext())
												.setTitle(android.R.string.dialog_alert_title)
												.setMessage(R.string.confirm_to_del_backup)
												.setNegativeButton(android.R.string.no, null)
												.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														try {
															if (f.delete())
																Toast.makeText(wikiConfigDialog.getContext(), R.string.backup_deleted, Toast.LENGTH_SHORT).show();
															else throw new Exception();
															backupListAdapter.reload(wikiConfigDialog.getContext(), new File(btnWikiConfigPath.getText().toString()));
															rvBackupList.setAdapter(backupListAdapter);
														} catch (Exception e) {
															e.printStackTrace();
															Toast.makeText(wikiConfigDialog.getContext(), R.string.failed_deleting_file, Toast.LENGTH_SHORT).show();
														}
													}
												})
												.show();
										break;
								}
						}
					});
					backupListAdapter.setOnLoadListener(new BackupListAdapter.LoadListener() {
						@Override
						public void onLoad(int count) {
							if (count > 0)
								lblNoBackup.setVisibility(View.GONE);
							else
								lblNoBackup.setVisibility(View.VISIBLE);
						}
					});
					backupListAdapter.reload(wikiConfigDialog.getContext(), new File(btnWikiConfigPath.getText().toString()));
					rvBackupList.setAdapter(backupListAdapter);
					cbBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							try {
								wikiData.put(DB_KEY_BACKUP, isChecked);
								writeJson(openFileOutput(DB_FILE_NAME, MODE_PRIVATE), db);
								if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE);
								else frmBackupList.setVisibility(View.GONE);
								backupListAdapter.reload(wikiConfigDialog.getContext(), new File(btnWikiConfigPath.getText().toString()));
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(wikiConfigDialog.getContext(), R.string.data_error, Toast.LENGTH_SHORT).show();
							}
						}
					});
					wikiConfigDialog.setCanceledOnTouchOutside(false);
					wikiConfigDialog.show();
					btnWikiConfigPath.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							File lastDir = Environment.getExternalStorageDirectory();
							boolean showHidden = false;
							try {
								lastDir = new File(db.getString(DB_KEY_LAST_DIR));
								showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
							} catch (Exception e) {
								e.printStackTrace();
							}
							FileDialog.fileOpen(wikiConfigDialog.getContext(), lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
								@Override
								public void onFileTouched(File[] files) {
									if (files != null && files.length > 0 && files[0] != null) {
										File file = files[0];
										TWInfo info = new TWInfo(MainActivity.this, file);
										if (info.isWiki) {
											try {
												boolean exist = false;
												for (int i = 0; i < db.getJSONArray(DB_KEY_WIKI).length(); i++) {
													if (db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(DB_KEY_PATH).equals(file.getAbsolutePath())) {
														exist = true;
														break;
													}
												}
												if (exist) {
													Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
												} else {
													String p = file.getAbsolutePath(), t = (info.title != null && info.title.length() > 0) ? info.title : getResources().getString(R.string.tiddlywiki);
													wikiData.put(KEY_NAME, t);
													wikiData.put(DB_KEY_PATH, p);
													btnWikiConfigPath.setText(p);
													wikiConfigDialog.setTitle(t);
													writeJson(openFileOutput(DB_FILE_NAME, MODE_PRIVATE), db);
												}
												db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
												writeJson(openFileOutput(DB_FILE_NAME, MODE_PRIVATE), db);
											} catch (Exception e) {
												e.printStackTrace();
												Toast.makeText(wikiConfigDialog.getContext(), R.string.data_error, Toast.LENGTH_SHORT).show();
											}
										} else
											Toast.makeText(MainActivity.this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
									} else
										Toast.makeText(MainActivity.this, R.string.failed_opening_file, Toast.LENGTH_SHORT).show();
								}

								@Override
								public void onCanceled() {

								}
							});
						}
					});
					btnRemoveWiki.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							View view1 = LayoutInflater.from(wikiConfigDialog.getContext()).inflate(R.layout.del_confirm, null);
							final CheckBox cbDelFile = view1.findViewById(R.id.cbDelFile);
							final CheckBox cbDelBackups = view1.findViewById(R.id.cbDelBackups);
							cbDelBackups.setEnabled(false);
							cbDelFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									cbDelBackups.setEnabled(isChecked);
								}
							});
							AlertDialog removeWikiConfirmationDialog = new AlertDialog.Builder(wikiConfigDialog.getContext())
									.setTitle(android.R.string.dialog_alert_title)
									.setMessage(R.string.confirm_to_remove_wiki)
									.setView(view1)
									.setNegativeButton(android.R.string.cancel, null)
									.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											try {
												final File f = new File(btnWikiConfigPath.getText().toString());
												if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
													db.put(DB_KEY_WIKI, removeUnderK(db.getJSONArray(DB_KEY_WIKI), position));
												else
													db.getJSONArray(DB_KEY_WIKI).remove(position);
												writeJson(openFileOutput(DB_FILE_NAME, MODE_PRIVATE), db);
												if (cbDelFile.isChecked()) {
													try {
														File[] fbx = f.getParentFile().listFiles(new FileFilter() {
															@Override
															public boolean accept(File pathname) {
																return pathname.exists() && pathname.isDirectory() && pathname.getName().equals(f.getName() + BACKUP_DIRECTORY_PATH_PREFIX);
															}
														});
														for (File fb : fbx)
															if (cbDelBackups.isChecked() && fb.isDirectory()) {
																File[] b = fb.listFiles(new FileFilter() {
																	@Override
																	public boolean accept(File pathname) {
																		return isBackupFile(f, pathname);
																	}
																});
																for (File f1 : b)
																	f1.delete();
																fb.delete();
															}
													} catch (Exception e) {
														e.printStackTrace();
													}
													if (f.delete())
														Toast.makeText(MainActivity.this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
											wikiConfigDialog.dismiss();
											MainActivity.this.onResume();
										}
									})
									.create();
							removeWikiConfirmationDialog.show();
						}
					});
					btnCreateShortcut.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								String id = wikiData.getString(KEY_ID);
								Bundle bu = new Bundle();
								bu.putString(KEY_ID, id);
								Intent in = new Intent(MainActivity.this, TWEditorWV.class).putExtras(bu).setAction(Intent.ACTION_MAIN);
								IconCompat iconCompat;
								if (icon != null) iconCompat = IconCompat.createWithBitmap(icon);
								else
									iconCompat = IconCompat.createWithResource(MainActivity.this, R.drawable.ic_shortcut);
								String lbl = wikiData.getString(MainActivity.KEY_NAME);
								if (ShortcutManagerCompat.isRequestPinShortcutSupported(MainActivity.this)) {
									ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(MainActivity.this, id)
											.setShortLabel(lbl.substring(0, lbl.indexOf(KEY_LBL)))
											.setLongLabel(lbl)
											.setIcon(iconCompat)
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
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private Boolean loadPage(String id) {
		Intent in = new Intent();
		try {
			Bundle bu = new Bundle();
			String vid = null;
			for (int i = 0; i < db.getJSONArray(DB_KEY_WIKI).length(); i++) {
				if (db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(KEY_ID).equals(id)) {
					vid = id;
					break;
				}
			}
			if (vid != null) {
				bu.putString(KEY_ID, vid);
				in.putExtras(bu)
						.setClass(MainActivity.this, TWEditorWV.class);
				startActivity(in);
			} else throw new Exception();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menu != null) {
			if (menu.getClass().getSimpleName().equalsIgnoreCase(CLASS_MENU_BUILDER)) {
				try {
					Method method = menu.getClass().getDeclaredMethod(METHOD_SET_OPTIONAL_ICONS_VISIBLE, Boolean.TYPE);
					method.setAccessible(true);
					method.invoke(menu, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_new) {
			final File template = new File(getFilesDir(), TEMPLATE_FILE_NAME);
			if (template.exists() && new TWInfo(MainActivity.this, template).isWiki) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden = false;
				try {
					lastDir = new File(db.getString(DB_KEY_LAST_DIR));
					showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileDialog.fileSave(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						FileInputStream is = null;
						FileOutputStream os = null;
						try {
							if (files != null && files.length > 0 && files[0] != null) {
								File file = files[0];
								is = new FileInputStream(template);
								os = new FileOutputStream(file);
								int len = is.available();
								int length, lengthTotal = 0;
								byte[] b = new byte[4096];
								while ((length = is.read(b)) != -1) {
									os.write(b, 0, length);
									lengthTotal += length;
								}
								os.flush();
								if (lengthTotal != len) throw new Exception();
								String id = genId();
								try {
									boolean exist = false;
									for (int i = 0; i < db.getJSONArray(DB_KEY_WIKI).length(); i++) {
										if (db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(DB_KEY_PATH).equals(file.getAbsolutePath())) {
											exist = true;
											id = db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(KEY_ID);
											break;
										}
									}
									if (exist) {
										Toast.makeText(MainActivity.this, R.string.wiki_replaced, Toast.LENGTH_SHORT).show();
									} else {
										TWInfo info = new TWInfo(MainActivity.this, file);
										JSONObject w = new JSONObject();
										w.put(KEY_NAME, (info.title != null && info.title.length() > 0) ? info.title : getResources().getString(R.string.tiddlywiki));
										w.put(KEY_ID, id);
										w.put(DB_KEY_PATH, file.getAbsolutePath());
										w.put(DB_KEY_BACKUP, false);
										db.getJSONArray(DB_KEY_WIKI).put(db.getJSONArray(DB_KEY_WIKI).length(), w);
									}
									db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
									if (!MainActivity.writeJson(openFileOutput(DB_FILE_NAME, Context.MODE_PRIVATE), db))
										throw new Exception();
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
								}
								MainActivity.this.onResume();
								if (!loadPage(id))
									Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
							} else throw new Exception();
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(MainActivity.this, R.string.failed_creating_file, Toast.LENGTH_SHORT).show();
						} finally {
							if (is != null)
								try {
									is.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							if (os != null)
								try {
									os.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
						}
					}

					@Override
					public void onCanceled() {

					}
				});
			} else {
				final ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(getResources().getString(R.string.please_wait));
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(false);
				progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						wGet(MainActivity.this, Uri.parse(getResources().getString(R.string.template_repo)), new File(getFilesDir(), TEMPLATE_FILE_NAME), true, true, new DownloadChecker() {
							@Override
							public boolean checkNg(File file) {
								return !(new TWInfo(MainActivity.this, file).isWiki);
							}
						}, new OnDownloadCompleteListener() {
							@Override
							public void onDownloadComplete(File file) {
								Toast.makeText(MainActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
								progressDialog.dismiss();
								onOptionsItemSelected(item);
							}

							@Override
							public void onDownloadFailed() {
								Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
								progressDialog.dismiss();
							}
						});
					}
				});
				AlertDialog dialog = new AlertDialog.Builder(this)
						.setTitle(android.R.string.dialog_alert_title)
						.setMessage(R.string.missing_template)
						.setPositiveButton(android.R.string.ok, null)
						.show();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						progressDialog.show();
					}
				});
			}
		} else if (id == R.id.action_import) {
			File lastDir = Environment.getExternalStorageDirectory();
			boolean showHidden = false;
			try {
				lastDir = new File(db.getString(DB_KEY_LAST_DIR));
				showHidden = db.getBoolean(DB_KEY_SHOW_HIDDEN);
			} catch (Exception e) {
				e.printStackTrace();
			}
			FileDialog.fileOpen(MainActivity.this, lastDir, HTML_FILTERS, showHidden, new FileDialog.OnFileTouchedListener() {
				@Override
				public void onFileTouched(File[] files) {
					if (files != null && files.length > 0 && files[0] != null) {
						File file = files[0];
						String id = genId();
						TWInfo info = new TWInfo(MainActivity.this, file);
						if (info.isWiki) {
							try {
								boolean exist = false;
								for (int i = 0; i < db.getJSONArray(DB_KEY_WIKI).length(); i++) {
									if (db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(DB_KEY_PATH).equals(file.getAbsolutePath())) {
										exist = true;
										id = db.getJSONArray(DB_KEY_WIKI).getJSONObject(i).getString(KEY_ID);
										break;
									}
								}
								if (exist) {
									Toast.makeText(MainActivity.this, R.string.wiki_already_exists, Toast.LENGTH_SHORT).show();
								} else {
									JSONObject w = new JSONObject();
									w.put(KEY_NAME, (info.title != null && info.title.length() > 0) ? info.title : getResources().getString(R.string.tiddlywiki));
									w.put(KEY_ID, id);
									w.put(DB_KEY_PATH, file.getAbsolutePath());
									w.put(DB_KEY_BACKUP, false);
									db.getJSONArray(DB_KEY_WIKI).put(db.getJSONArray(DB_KEY_WIKI).length(), w);
								}
								db.put(DB_KEY_LAST_DIR, file.getParentFile().getAbsolutePath());
								if (!MainActivity.writeJson(openFileOutput(DB_FILE_NAME, Context.MODE_PRIVATE), db))
									throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, R.string.data_error, Toast.LENGTH_SHORT).show();
							}
							MainActivity.this.onResume();
							if (!loadPage(id))
								Toast.makeText(MainActivity.this, R.string.error_loading_page, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(MainActivity.this, R.string.not_a_wiki, Toast.LENGTH_SHORT).show();
						}

					} else
						Toast.makeText(MainActivity.this, R.string.failed_opening_file, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onCanceled() {

				}
			});

		} else if (id == R.id.action_fork) {
			final SearchView view = new SearchView(this);
			view.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			view.setImeOptions(EditorInfo.IME_ACTION_GO);
			view.setQueryHint(getResources().getString(R.string.url));
			view.onActionViewExpanded();
			view.setSubmitButtonEnabled(true);
			final String STR_EMPTY = "",
					KEY_S = "s",
					KEY_AS = "AS",
					KEY_RESULTS = "Results",
					KEY_SUGGESTS = "Suggests",
					KEY_SUGGESTION = "suggestion",
					KEY_TXT = "Txt",
					KEY_SRC = "src",
					KEY_SE = "se",
					KEY_SUG = "sug",
					KEY_DIRECT = "mark2",
					KEY_DATA = "data";
			final String[] SUG_COLUMNS = {"_id", "name", "mark", "mark2", "mark3"},
					SUG_ADAPTER_COLUMNS = {"mark", MainActivity.KEY_NAME, "mark3", "mark2"};
			final NoLeakHandler handler = new NoLeakHandler(new NoLeakHandler.MessageHandledListener() {
				@Override
				public void onMessageHandled(Message msg) {
					Bundle data = msg.getData();
					String src = data.getString(KEY_SRC);
					String se = data.getString(KEY_SE);
					Uri uri = Uri.parse(src);
					String sch = uri.getScheme();
					String[] sug = data.getStringArray(KEY_SUG);
					MatrixCursor cursor = new MatrixCursor(SUG_COLUMNS);
					int i = 0;
					Uri uri1 = sch == null ? Uri.parse(SCH_EX_HTTP + src) : null;
					String hos1 = uri1 != null ? uri1.getHost() : null;
					if (sch != null && sch.length() > 0 || hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1) {
						cursor.addRow(new CharSequence[]{String.valueOf(i), src, getResources().getString(R.string.mark_Go), STR_EMPTY, getResources().getString(R.string.mark_Return)});
						i++;
					}
					cursor.addRow(new CharSequence[]{String.valueOf(i), src, getResources().getString(R.string.mark_Search), se != null ? se : STR_EMPTY, i > 0 ? STR_EMPTY : getResources().getString(R.string.mark_Return)});
					i++;
					if (sug != null)
						for (String v : sug) {
							cursor.addRow(new CharSequence[]{String.valueOf(i), v, getResources().getString(R.string.mark_Search), se, i > 0 ? STR_EMPTY : getResources().getString(R.string.mark_Return)});
							i++;
						}
					if (view.getSuggestionsAdapter() == null) {
						SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(view.getContext(), R.layout.suggestion_slot, cursor, SUG_ADAPTER_COLUMNS, new int[]{R.id.t_sug_mark, R.id.t_sug, R.id.t_sug_first, R.id.t_sug_se}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
						view.setSuggestionsAdapter(simpleCursorAdapter);
					} else {
						view.getSuggestionsAdapter().changeCursor(cursor);
					}
				}
			});
			final AlertDialog URLDialog = new AlertDialog.Builder(MainActivity.this)
					.setView(view)
					.show();

			view.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
				@Override
				public boolean onSuggestionSelect(int position) {
					return false;
				}

				@Override
				public boolean onSuggestionClick(int position) {
					MatrixCursor c = (MatrixCursor) view.getSuggestionsAdapter().getItem(position);
					String res = c.getString(c.getColumnIndex(MainActivity.KEY_NAME));
					boolean direct = c.getString(c.getColumnIndex(KEY_DIRECT)).length() == 0;
					String vScheme = Uri.parse(res).getScheme();
					Intent in = new Intent();
					Bundle bu = new Bundle();
					if (direct && vScheme != null && vScheme.length() > 0)
						bu.putString(KEY_URL, res);
					else if (direct) bu.putString(KEY_URL, SCH_EX_HTTP + res);
					else bu.putString(KEY_URL, wSearch(res));
					in.putExtras(bu).setClass(MainActivity.this, TWEditorWV.class);
					startActivity(in);
					URLDialog.dismiss();
					return true;
				}
			});

			view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					Intent in = new Intent();
					Bundle bu = new Bundle();
					Uri uri = Uri.parse(query);
					String sch = uri.getScheme();
					Uri uri1 = sch == null ? Uri.parse(SCH_EX_HTTP + query) : null;
					String hos1 = uri1 != null ? uri1.getHost() : null;
					if (sch != null && sch.length() > 0)
						bu.putString(KEY_URL, query);
					else if (hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1)
						bu.putString(KEY_URL, SCH_EX_HTTP + query);
					else bu.putString(KEY_URL, wSearch(query));
					in.putExtras(bu).setClass(MainActivity.this, TWEditorWV.class);
					startActivity(in);
					URLDialog.dismiss();
					return true;
				}

				@Override
				public boolean onQueryTextChange(final String newText) {
					if (newText.length() > 0)
						new Thread() {
							public void run() {
								try {
									String se = db.getString(DB_KEY_SEARCH_ENGINE), res;
									Message msg = new Message();
									Bundle data = new Bundle();
									JSONArray array = null;
									String ses = null;
									try {
										switch (se) {
											case SE_GOOGLE:
												List<String> attrs = Jsoup.connect(getResources().getString(R.string.su_google).replace(PREF_SU, newText)).ignoreContentType(true).get().getElementsByTag(KEY_SUGGESTION).eachAttr(KEY_DATA);
												String[] vGoogle = attrs.toArray(new String[0]);
												data.putStringArray(KEY_SUG, vGoogle);
												ses = getResources().getString(R.string.google);
												break;
											case SE_BING:
												res = Jsoup.connect(getResources().getString(R.string.su_bing).replace(PREF_SU, newText)).ignoreContentType(true).get().body().html();
												JSONArray arrayBing = new JSONObject(res).getJSONObject(KEY_AS).getJSONArray(KEY_RESULTS).getJSONObject(0).getJSONArray(KEY_SUGGESTS);
												int k = arrayBing.length();
												String[] vBing = new String[k];
												for (int i = 0; i < k; i++)
													vBing[i] = arrayBing.getJSONObject(i).getString(KEY_TXT);
												data.putStringArray(KEY_SUG, vBing);
												ses = getResources().getString(R.string.bing);
												break;
											case SE_BAIDU:
												res = Jsoup.connect(getResources().getString(R.string.su_baidu).replace(PREF_SU, newText)).get().body().html();
												array = new JSONObject(res.substring(res.indexOf('(') + 1, res.lastIndexOf(')'))).getJSONArray(KEY_S);
												ses = getResources().getString(R.string.baidu);
												break;
											case SE_SOGOU:
												res = Jsoup.connect(getResources().getString(R.string.su_sogou).replace(PREF_SU, newText)).ignoreContentType(true).get().body().html();
												array = new JSONObject(STR_EMPTY + '{' + '"' + 's' + '"' + ':' + res.substring(res.indexOf('['), res.lastIndexOf(']') + 1) + '}').getJSONArray(KEY_S).getJSONArray(1);
												ses = getResources().getString(R.string.sogou);
												break;
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									if (array != null) {
										int k = array.length();
										String[] v = new String[k];
										for (int i = 0; i < k; i++)
											v[i] = array.getString(i);
										data.putStringArray(KEY_SUG, v);
									}
									data.putString(KEY_SRC, newText);
									data.putString(KEY_SE, ses);
									msg.setData(data);
									handler.sendMessage(msg);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();
					return true;
				}
			});
		} else if (id == R.id.action_settings) {
			final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.settings_dialog, null);
			final Spinner spnSE = view.findViewById(R.id.spnSE);
			spnSE.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.ext_slot, new String[]{
					getResources().getText(R.string.google).toString(),
					getResources().getText(R.string.bing).toString(),
					getResources().getText(R.string.baidu).toString(),
					getResources().getText(R.string.sogou).toString(),
					getResources().getText(R.string.custom).toString()
			}));
			final EditText vCSE = view.findViewById(R.id.customSE);
			final CheckBox sh = view.findViewById(R.id.cbHidden);
			try {
				String seStr = db.getString(DB_KEY_SEARCH_ENGINE);
				switch (seStr) {
					case SE_GOOGLE:
						spnSE.setSelection(0);
						break;
					case SE_BING:
						spnSE.setSelection(1);
						break;
					case SE_BAIDU:
						spnSE.setSelection(2);
						break;
					case SE_SOGOU:
						spnSE.setSelection(3);
						break;
					case SE_CUSTOM:
						spnSE.setSelection(4);
						break;
				}
				vCSE.setEnabled(spnSE.getSelectedItemPosition() == 4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				vCSE.setText(db.getString(DB_KEY_CSE));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				sh.setChecked(db.getBoolean(DB_KEY_SHOW_HIDDEN));
			} catch (Exception e) {
				e.printStackTrace();
			}
			AlertDialog settingDialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.action_settings)
					.setView(view)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								String vse = SE_GOOGLE;
								switch (spnSE.getSelectedItemPosition()) {
									case 0:
										vse = SE_GOOGLE;
										break;
									case 1:
										vse = SE_BING;
										break;
									case 2:
										vse = SE_BAIDU;
										break;
									case 3:
										vse = SE_SOGOU;
										break;
									case 4:
										vse = SE_CUSTOM;
										break;
								}
								db.put(DB_KEY_SEARCH_ENGINE, vse);
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								String cse = vCSE.getText().toString();
								if (cse.length() > 0) {
									Uri uri = Uri.parse(vCSE.getText().toString());
									String sch = uri.getScheme();
									if (sch == null)
										uri = Uri.parse(SCH_EX_HTTP + uri.toString());
									db.put(DB_KEY_CSE, uri.toString());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								db.put(DB_KEY_SHOW_HIDDEN, sh.isChecked());
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								writeJson(openFileOutput(DB_FILE_NAME, Context.MODE_PRIVATE), db);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					})
					.create();
			settingDialog.setCanceledOnTouchOutside(false);
			settingDialog.show();
			final Button ok = settingDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			view.findViewById(R.id.btnUpdateTemplate).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final ProgressDialog progressDialog = new ProgressDialog(view.getContext());
					progressDialog.setMessage(getResources().getString(R.string.please_wait));
					progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progressDialog.setCancelable(false);
					progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
						@Override
						public void onShow(DialogInterface dialog) {
							wGet(MainActivity.this, Uri.parse(getResources().getString(R.string.template_repo)), new File(getFilesDir(), TEMPLATE_FILE_NAME), true, true, new DownloadChecker() {
								@Override
								public boolean checkNg(File file) {
									return !(new TWInfo(MainActivity.this, file).isWiki);
								}
							}, new OnDownloadCompleteListener() {
								@Override
								public void onDownloadComplete(File file) {
									Toast.makeText(MainActivity.this, R.string.download_complete, Toast.LENGTH_SHORT).show();
									progressDialog.dismiss();
								}

								@Override
								public void onDownloadFailed() {
									Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
									progressDialog.dismiss();
								}
							});
						}
					});
					progressDialog.show();
				}
			});
			spnSE.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (position == 4) vCSE.setVisibility(View.VISIBLE);
					else vCSE.setVisibility(View.GONE);
					vCSE.setEnabled(position == 4);
					ok.setEnabled(!vCSE.isEnabled() || vCSE.getText().toString().length() > 0);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
			vCSE.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if (vCSE.isEnabled() && vCSE.getText().toString().length() == 0)
						ok.setEnabled(false);
					else ok.setEnabled(true);
				}
			});
			return true;
		} else if (id == R.id.action_about) {
			final SpannableString spannableString = new SpannableString(getResources().getString(R.string.about));
			Linkify.addLinks(spannableString, Linkify.ALL);
			final AlertDialog aboutDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.action_about)
					.setMessage(spannableString)
					.show();
			((TextView) aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				((TextView) aboutDialog.findViewById(android.R.id.message)).setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			db = readJson(openFileInput(DB_FILE_NAME));
			wikiListAdapter.reload(db);
			rvWikiList.setAdapter(wikiListAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (db.getJSONArray(DB_KEY_WIKI).length() == 0)
				noWiki.setVisibility(View.VISIBLE);
			else
				noWiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static JSONObject readJson(FileInputStream is) {
		byte[] b;
		JSONObject jsonObject = null;
		try {
			b = new byte[is.available()];
			if (is.read(b) < 0) throw new Exception();
			jsonObject = new JSONObject(new String(b));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return jsonObject;
	}

	static boolean writeJson(FileOutputStream os, JSONObject vdb) {
		boolean v;
		try {
			byte[] b = vdb.toString(2).getBytes();
			os.write(b);
			os.flush();
			v = true;
		} catch (Exception e) {
			e.printStackTrace();
			v = false;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return v;
	}

	@TargetApi(23)
	private void checkPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}
	}

	static String genId() {
		return UUID.randomUUID().toString();
	}

	private class TWInfo {
		private boolean isWiki;
		private String title;

		private TWInfo(Context context, File file) {
			try {
				Document doc = Jsoup.parse(file, CHARSET_DEFAULT);
				Element ti = doc.getElementsByTag(KEY_TITLE).first();
				title = ti != null ? ti.html() : null;
				Element an = doc.getElementsByAttributeValue(KEY_NAME, KEY_APPLICATION_NAME).first();
				isWiki = an != null && an.attr(KEY_CONTENT).equals(context.getResources().getString(R.string.tiddlywiki));
				if (isWiki) return;
				Element ele = doc.getElementsByAttributeValue(KEY_ID, KEY_VERSION_AREA).first();
				String js = ele != null ? ele.html().substring(ele.html().indexOf(PREF_VER_1), ele.html().indexOf(PREF_VER_2) + 2) : null;
				if (js != null) {
					org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();
					rhino.setOptimizationLevel(-1);
					try {
						Scriptable scope = rhino.initStandardObjects();
						rhino.evaluateString(scope, js, context.getResources().getString(R.string.app_name), 1, null);
						String c = (String) ((Scriptable) scope.get(KEY_VERSION, scope)).get(KEY_TITLE, scope);
						isWiki = c != null && c.equals(context.getResources().getString(R.string.tiddlywiki));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						org.mozilla.javascript.Context.exit();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isBackupFile(File main, File chk) {
		String mfn = main.getName();
		String mfn1 = mfn.substring(0, mfn.lastIndexOf('.'));
		String mfn2 = mfn.substring(mfn.lastIndexOf('.') + 1);
		String efn = chk.getName();
		int p = mfn1.length();
		boolean k1 = efn.substring(0, p).equals(mfn1);
		boolean k2 = efn.charAt(p) == '.';
		p++;
		boolean k3 = true;
		for (int pp = p; pp < p + 17; pp++)
			if (efn.charAt(pp) < 48 || efn.charAt(pp) > 57) {
				k3 = false;
				break;
			}
		p += 17;
		boolean k4 = efn.charAt(p) == '.';
		p++;
		boolean k5 = efn.substring(p).equals(mfn2);
		return k1 && k2 && k3 && k4 && k5;
	}

	private static JSONArray removeUnderK(JSONArray src, int index) {
		if (src == null) return null;
		if (src.length() <= index) return src;
		JSONArray des = new JSONArray();
		for (int i = 0; i < src.length(); i++)
			try {
				if (i != index) des.put(src.getJSONObject(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		return des;
	}

	private String wSearch(String arg) {
		String ws = getResources().getString(R.string.s_google).replace(PREF_S, arg);
		try {
			String se = db.getString(DB_KEY_SEARCH_ENGINE);
			switch (se) {
				case SE_GOOGLE:
					ws = getResources().getString(R.string.s_google).replace(PREF_S, arg);
					break;
				case SE_BING:
					ws = getResources().getString(R.string.s_bing).replace(PREF_S, arg);
					break;
				case SE_BAIDU:
					ws = getResources().getString(R.string.s_baidu).replace(PREF_S, arg);
					break;
				case SE_SOGOU:
					ws = getResources().getString(R.string.s_sogou).replace(PREF_S, arg);
					break;
				case SE_CUSTOM:
					ws = db.getString(DB_KEY_CSE).replace(PREF_S, arg);
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ws;
	}

	interface OnDownloadCompleteListener {
		void onDownloadComplete(File file);

		void onDownloadFailed();
	}

	interface DownloadChecker {
		boolean checkNg(File file);
	}


	static void wGet(final Context parent, Uri uri, final File dest) {
		wGet(parent, uri, dest, false, false, null, null);
	}

	private static void wGet(final Context parent, Uri uri, final File dest, final boolean noNotification, final boolean noToast, final DownloadChecker checker, final OnDownloadCompleteListener listener) {
		final String KEY_TOAST = "toast",
				KEY_COMPLETE = "complete",
				KEY_FAILED = "failed",
				KEY_FILEPATH = "filepath";
		String sch = uri.getScheme();
		if (sch == null || sch.length() == 0)
			uri = Uri.parse(SCH_EX_HTTP + uri.toString());
		try {
			final String id = MainActivity.genId().substring(0, 7);
			final int idt = Integer.parseInt(id, 16);
			final File cacheFile = new File(parent.getCacheDir(), id);
			final Uri uriX = uri;
			final NoLeakHandler handler = new NoLeakHandler(new NoLeakHandler.MessageHandledListener() {
				@Override
				public void onMessageHandled(Message msg) {
					if (msg != null) {
						Bundle data = msg.getData();
						if (data != null) {
							int toast = data.getInt(KEY_TOAST, -1);
							String filepath = data.getString(KEY_FILEPATH);
							if (toast != -1)
								Toast.makeText(parent, toast, Toast.LENGTH_SHORT).show();
							if (data.getBoolean(KEY_COMPLETE) && filepath != null && listener != null)
								listener.onDownloadComplete(new File(filepath));
							else if (data.getBoolean(KEY_FAILED) && listener != null)
								listener.onDownloadFailed();
						}
					}
				}
			});
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message msg;
					Bundle bundle = new Bundle();
					InputStream is = null, is2 = null;
					OutputStream os = null, os2 = null;
					try {
						final HttpURLConnection httpURLConnection;
						int len;
						if (uriX.getScheme() != null && uriX.getScheme().equals(SCHEME_BLOB_B64)) {
							String b64 = uriX.getSchemeSpecificPart();
							byte[] bytes = Base64.decode(b64, Base64.NO_PADDING);
							is = new ByteArrayInputStream(bytes);
							len = bytes.length;
						} else {
							URL url;
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
								url = new URL(uriX.normalizeScheme().toString());
							else url = new URL(uriX.toString());
							if (uriX.getScheme() != null && uriX.getScheme().equals("https")) {
								httpURLConnection = (HttpsURLConnection) url.openConnection();
								if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
									((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new TLSSocketFactory());
							} else httpURLConnection = (HttpURLConnection) url.openConnection();
							httpURLConnection.connect();
							len = httpURLConnection.getContentLength();
							is = httpURLConnection.getInputStream();
						}
						os = new FileOutputStream(cacheFile);
						os2 = new FileOutputStream(dest);
						int length;
						int lengthTotal = 0;
						byte[] bytes = new byte[4096];
						if (!noToast) {
							bundle.putInt(KEY_TOAST, R.string.downloading);
							msg = new Message();
							msg.setData(bundle);
							handler.sendMessage(msg);
						}
						Notification notification;
						NotificationManager notificationManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
							notificationManager.createNotificationChannel(new NotificationChannel(id, id, NotificationManager.IMPORTANCE_LOW));
						while ((length = is.read(bytes)) != -1) {
							os.write(bytes, 0, length);
							lengthTotal += length;
							int p = Math.round((float) lengthTotal / (float) len * 100);
							if (!noNotification) {
								notification = new NotificationCompat.Builder(parent, id)
										.setSmallIcon(R.drawable.ic_download)
										.setContentTitle(parent.getResources().getString(R.string.downloading))
										.setContentText(String.valueOf(p) + '%')
										.setOngoing(true)
										.setShowWhen(true)
										.setProgress(100, p, false)
										.build();
								notificationManager.notify(id, idt, notification);
							}

						}
						os.flush();
						if (len > 0 && lengthTotal < len || checker != null && checker.checkNg(cacheFile))
							throw new Exception();
						if (!noNotification) {
							notification = new NotificationCompat.Builder(parent, id)
									.setSmallIcon(R.drawable.ic_download)
									.setContentTitle(parent.getResources().getString(R.string.downloading))
									.setOngoing(true)
									.setShowWhen(true)
									.setProgress(0, 0, true)
									.build();
							notificationManager.notify(id, idt, notification);
						}
						is2 = new FileInputStream(cacheFile);
						byte[] b2 = new byte[4096];
						int l2, lt2 = 0;
						while ((l2 = is2.read(b2)) != -1) {
							os2.write(b2, 0, l2);
							lt2 += l2;
						}
						os2.flush();
						if (lt2 != lengthTotal) throw new Exception();
						if (!noNotification) notificationManager.cancel(id, idt);
						if (!noToast)
							bundle.putInt(KEY_TOAST, R.string.download_complete);
						bundle.putBoolean(KEY_COMPLETE, true);
						bundle.putString(KEY_FILEPATH, dest.getAbsolutePath());
						msg = new Message();
						msg.setData(bundle);
						handler.sendMessage(msg);
						cacheFile.delete();
					} catch (Exception e) {
						e.printStackTrace();
						if (!noToast) {
							bundle.putInt(KEY_TOAST, R.string.download_failed);
						}
						bundle.putBoolean(KEY_FAILED, true);
						msg = new Message();
						msg.setData(bundle);
						handler.sendMessage(msg);
						cacheFile.delete();
					} finally {
						if (is != null)
							try {
								is.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						if (os != null)
							try {
								os.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						if (is2 != null)
							try {
								is2.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						if (os2 != null)
							try {
								os2.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
				}


			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(parent, R.string.download_failed, Toast.LENGTH_SHORT).show();
		}
	}
}