package indi.donmor.tiddloid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
//import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import indi.donmor.tiddloid.utils.BackupListAdapter;
import indi.donmor.tiddloid.utils.NoLeakHandler;
import indi.donmor.tiddloid.utils.WikiListAdapter;

import com.github.donmor3000.filedialog.lib.FileDialog;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
	private RecyclerView rvWikiList;
	private TextView noWiki;
	private WikiListAdapter wikiListAdapter;
	public static JSONObject db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.activity_main);
		File templateOnStart = new File(getFilesDir(), "template.html");
		if (!templateOnStart.exists() || templateOnStart.length() == 0) {
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getResources().getString(R.string.please_wait));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					wGet(MainActivity.this, Uri.parse(getResources().getString(R.string.template_repo)), new File(getFilesDir(), "template.html"), true, true, new TWEditorWV.OnDownloadCompleteListener() {
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
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle("Notice")
					.setMessage("Missing template. Tiddloid will now download a new template file from TiddlyWiki.com.")
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
			db = readJson(openFileInput("data.json"));
			if (db == null) throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			db = new JSONObject();
			try {
				db.put("searchEngine", R.string.default_se);
				db.put("showHidden", false);
				db.put("wiki", new JSONArray());
				db.put("lastDir", Environment.getExternalStorageDirectory().getAbsolutePath());
				writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
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
			if (db.getJSONArray("wiki").length() == 0)
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
				if (id != null) {
					if (!loadPage(id))
						Toast.makeText(MainActivity.this, "Error loading the page", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onItemLongClick(final int position) {
				try {
					final JSONObject wikiData = MainActivity.db.getJSONArray("wiki").getJSONObject(position);
					View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.wikiconfig_dialog, null);
					final Button btnWikiConfigPath = view.findViewById(R.id.btnWikiConfigPath);
					btnWikiConfigPath.setText(wikiData.getString("path"));
					final CheckBox cbBackup = view.findViewById(R.id.cbBackup);
					cbBackup.setChecked(wikiData.getBoolean("backup"));
					final LinearLayout frmBackupList = view.findViewById(R.id.frmBackupList);
					if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE);
					else frmBackupList.setVisibility(View.GONE);
					final TextView lblNoBackup = view.findViewById(R.id.lblNoBackup);
					final RecyclerView rvBackupList = view.findViewById(R.id.rvBackupList);
					rvBackupList.setLayoutManager(new LinearLayoutManager(view.getContext()));
					Button btnRemoveWiki = view.findViewById(R.id.btnRemoveWiki);

					final AlertDialog wikiConfigDialog = new AlertDialog.Builder(MainActivity.this)
							.setTitle(wikiData.getString("name"))
							.setIcon(getResources().getDrawable(R.drawable.ic_description_black_24dp))
							.setView(view)
							.setPositiveButton("Close", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									MainActivity.this.onResume();
								}
							})
							.create();
					final BackupListAdapter backupListAdapter = new BackupListAdapter(wikiConfigDialog.getContext());
					backupListAdapter.setOnBtnClickListener(new BackupListAdapter.BtnClickListener() {
						@Override
						public void onBtnClick(int position, int which) {
							final File f = backupListAdapter.getBackupFile(position);
							if (f != null && f.exists())
								switch (which) {
									case 1:
										new AlertDialog.Builder(wikiConfigDialog.getContext())
												.setTitle("Warning")
												.setMessage("Are you sure you want to replace this Wiki with the backup version? This operation cannot be reversed!")
												.setNegativeButton("No", null)
												.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														try {
															FileInputStream is = new FileInputStream(f);
															FileOutputStream os = new FileOutputStream(new File(btnWikiConfigPath.getText().toString()));
															int len = is.available();
															int length, lengthTotal = 0;
															byte[] b = new byte[4096];
															while ((length = is.read(b)) != -1) {
																os.write(b);
																lengthTotal += length;
															}
															is.close();
															os.flush();
															os.close();
															if (lengthTotal != len)
																throw new Exception();
															wikiConfigDialog.dismiss();
															Toast.makeText(MainActivity.this, "Wiki rolled back successfully", Toast.LENGTH_SHORT).show();
															loadPage(wikiData.getString("id"));
														} catch (Exception e) {
															e.printStackTrace();
															Toast.makeText(MainActivity.this, "Failed writing the file", Toast.LENGTH_SHORT).show();
														}
													}
												})
												.show();
										break;
									case 2:
										new AlertDialog.Builder(wikiConfigDialog.getContext())
												.setTitle("Warning")
												.setMessage("Are you sure you want to delete this version?")
												.setNegativeButton("No", null)
												.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														try {
															if (f.delete())
																Toast.makeText(wikiConfigDialog.getContext(), "Backup deleted", Toast.LENGTH_SHORT).show();
															else throw new Exception();
															backupListAdapter.reload(wikiConfigDialog.getContext(), new File(btnWikiConfigPath.getText().toString()));
															rvBackupList.setAdapter(backupListAdapter);
														} catch (Exception e) {
															e.printStackTrace();
															Toast.makeText(wikiConfigDialog.getContext(), "Failed to delete the file", Toast.LENGTH_SHORT).show();
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
								wikiData.put("backup", isChecked);
								writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
								if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE);
								else frmBackupList.setVisibility(View.GONE);
								backupListAdapter.reload(wikiConfigDialog.getContext(), new File(btnWikiConfigPath.getText().toString()));
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(wikiConfigDialog.getContext(), "Data error", Toast.LENGTH_SHORT).show();
							}
						}
					});
					wikiConfigDialog.setCanceledOnTouchOutside(false);
					wikiConfigDialog.show();
					btnWikiConfigPath.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							File lastDir = Environment.getExternalStorageDirectory();
							boolean showHidden =false;
							try {
								lastDir = new File(MainActivity.db.getString("lastDir"));
								showHidden = MainActivity.db.getBoolean("showHidden");
							} catch (Exception e) {
								e.printStackTrace();
							}
							FileDialog.fileOpen(wikiConfigDialog.getContext(), lastDir, new String[]{"text/html"},showHidden, new FileDialog.OnFileTouchedListener() {
								@Override
								public void onFileTouched(File[] files) {
									if (files != null && files.length > 0 && files[0] != null) {
										File file = files[0];
										try {
											boolean exist = false;
											for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
												if (db.getJSONArray("wiki").getJSONObject(i).getString("path").equals(file.getAbsolutePath())) {
													exist = true;
													break;
												}
											}
											if (exist) {
												Toast.makeText(MainActivity.this, "The Wiki is already exist", Toast.LENGTH_SHORT).show();
											} else {
												String p = file.getAbsolutePath();
												wikiData.put("path", p);
												btnWikiConfigPath.setText(p);
												writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
											}
											db.put("lastDir", file.getParentFile().getAbsolutePath());
											writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
										} catch (Exception e) {
											e.printStackTrace();
											Toast.makeText(wikiConfigDialog.getContext(), "Data error", Toast.LENGTH_SHORT).show();
										}
									} else
										Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
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
									.setTitle("Warning")
									.setMessage("Are you sure you want to remove this Wiki from Tiddloid?")
									.setView(view1)
									.setNegativeButton("Cancel", null)
									.setPositiveButton("ok", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											try {
												final File f = new File(btnWikiConfigPath.getText().toString());
												if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
													db.put("wiki", removeUnderK(db.getJSONArray("wiki"), position));
												else
													db.getJSONArray("wiki").remove(position);
												writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
												if (cbDelFile.isChecked()) {
													try {
														File[] fbx = f.getParentFile().listFiles(new FileFilter() {
															@Override
															public boolean accept(File pathname) {
																return pathname.exists() && pathname.isDirectory() && pathname.getName().equals(getResources().getString(R.string.backup_directory_path).replace("$filename$", f.getName()));
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
														Toast.makeText(MainActivity.this, "File deleted", Toast.LENGTH_SHORT).show();
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
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private Boolean loadPage(String id) {
		Intent in = new Intent();
		try {
			Bundle bu = new Bundle();
			String vid = null;
			for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
				if (db.getJSONArray("wiki").getJSONObject(i).getString("id").equals(id)) {
					vid = id;
					break;
				}
			}
			if (vid != null) {
				bu.putString("id", vid);
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
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menu != null) {
			if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
				try {
					Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
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
			final File template = new File(getFilesDir(), "template.html");
			if (template.exists() && template.length() > 0) {
				File lastDir = Environment.getExternalStorageDirectory();
				boolean showHidden =false;
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
					showHidden = MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileDialog.fileSave(MainActivity.this, lastDir, new String[]{"text/html"},showHidden, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						try {
							if (files != null && files.length > 0 && files[0] != null) {
								File file = files[0];
								FileInputStream is = new FileInputStream(template);
								FileOutputStream os = new FileOutputStream(file);
								int len = is.available();
								int length, lengthTotal = 0;
								byte[] b = new byte[4096];
								while ((length = is.read(b)) != -1) {
									os.write(b);
									lengthTotal += length;
								}
								is.close();
								os.flush();
								os.close();
								if (lengthTotal != len) throw new Exception();
								String id = genId();
								try {
									boolean exist = false;
									for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
										if (db.getJSONArray("wiki").getJSONObject(i).getString("path").equals(file.getAbsolutePath())) {
											exist = true;
											id = db.getJSONArray("wiki").getJSONObject(i).getString("id");
											break;
										}
									}
									if (exist) {
										Toast.makeText(MainActivity.this, "The Wiki is already exist", Toast.LENGTH_SHORT).show();
									} else {
										JSONObject w = new JSONObject();
										w.put("name", "TiddlyWiki");
										w.put("id", id);
										w.put("path", file.getAbsolutePath());
										w.put("backup", false);
										db.getJSONArray("wiki").put(db.getJSONArray("wiki").length(), w);
										if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db))
											throw new Exception();
									}
									db.put("lastDir", file.getParentFile().getAbsolutePath());
									writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
								}
								MainActivity.this.onResume();
								if (!loadPage(id))
									Toast.makeText(MainActivity.this, "Error loading the page", Toast.LENGTH_SHORT).show();
							} else throw new Exception();
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(MainActivity.this, "Failed creating the file", Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onCanceled() {

					}
				});
			} else {
//				AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
//						.setTitle("Notice")
//						.setMessage("TiddlyWiki template is not exist. Click 'OK' and select a TiddlyWiki HTML file as template.")
//						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								File lastDir = Environment.getExternalStorageDirectory();
//								try {
//									lastDir = new File(MainActivity.db.getString("lastDir"));
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//								FileDialog.fileOpen(MainActivity.this, lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
//									@Override
//									public void onFileTouched(File[] files) {
//										try {
//											if (files != null && files.length > 0 && files[0] != null) {
//												File file = files[0];
//												FileInputStream is = new FileInputStream(file);
//												FileOutputStream os = new FileOutputStream(template);
//												int len = is.available();
//												int length, lengthTotal = 0;
//												byte[] b = new byte[4096];
//												while ((length = is.read(b)) != -1) {
//													os.write(b);
//													lengthTotal += length;
//												}
//												is.close();
//												os.flush();
//												os.close();
//												if (lengthTotal != len) throw new Exception();
//												Toast.makeText(MainActivity.this, "Successfully imported the template", Toast.LENGTH_SHORT).show();
//												db.put("lastDir", file.getParentFile().getAbsolutePath());
//												writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
//												onOptionsItemSelected(item);
//											} else throw new Exception();
//										} catch (Exception e) {
//											e.printStackTrace();
//											Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
//										}
//									}
//
//									@Override
//									public void onCanceled() {
//
//									}
//								});
//
//							}
//						}).create();
//				alert.setCanceledOnTouchOutside(false);
//				alert.show();
				final ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(getResources().getString(R.string.please_wait));
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(false);
				progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						wGet(MainActivity.this, Uri.parse(getResources().getString(R.string.template_repo)), new File(getFilesDir(), "template.html"), true, true, new TWEditorWV.OnDownloadCompleteListener() {
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
						.setTitle("Notice")
						.setMessage("Missing template. Tiddloid will now download a new template file from TiddlyWiki.com.")
						.setPositiveButton(android.R.string.ok, null)
						.show();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
//					TWEditorWV.wGet(MainActivity.this,Uri.parse(getResources().getString(R.string.template_repo)),new File(getFilesDir(),"template.html"));
						progressDialog.show();
					}
				});
			}
		} else if (id == R.id.action_import) {
			File lastDir = Environment.getExternalStorageDirectory();
			boolean showHidden =false;
			try {
				lastDir = new File(MainActivity.db.getString("lastDir"));
				showHidden = MainActivity.db.getBoolean("showHidden");
			} catch (Exception e) {
				e.printStackTrace();
			}
			FileDialog.fileOpen(MainActivity.this, lastDir, new String[]{"text/html"},showHidden, new FileDialog.OnFileTouchedListener() {
				@Override
				public void onFileTouched(File[] files) {
					if (files != null && files.length > 0 && files[0] != null) {
						File file = files[0];
						String id = genId();
						try {
							boolean exist = false;
							for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
								if (db.getJSONArray("wiki").getJSONObject(i).getString("path").equals(file.getAbsolutePath())) {
									exist = true;
									id = db.getJSONArray("wiki").getJSONObject(i).getString("id");
									break;
								}
							}
							if (exist) {
								Toast.makeText(MainActivity.this, "The Wiki is already exist", Toast.LENGTH_SHORT).show();
							} else {
								JSONObject w = new JSONObject();
								w.put("name", "TiddlyWiki");
								w.put("id", id);
								w.put("path", file.getAbsolutePath());
								w.put("backup", false);
								db.getJSONArray("wiki").put(db.getJSONArray("wiki").length(), w);
								if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db))
									throw new Exception();
							}
							db.put("lastDir", file.getParentFile().getAbsolutePath());
							writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
						}
						MainActivity.this.onResume();
						if (!loadPage(id))
							Toast.makeText(MainActivity.this, "Error loading the page", Toast.LENGTH_SHORT).show();

					} else
						Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
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
			final NoLeakHandler handler = new NoLeakHandler(new NoLeakHandler.MessageHandledListener() {
				@Override
				public void onMessageHandled(Message msg) {
					Bundle data = msg.getData();
					String src = data.getString("src");
					String se = data.getString("se");
					Uri uri = Uri.parse(src);
					String sch = uri.getScheme();
					String[] sug = data.getStringArray("sug");
					String[] COLUMNS = {"_id", "name", "mark", "mark2", "mark3"};
					MatrixCursor cursor = new MatrixCursor(COLUMNS);
					int i = 0;
					Uri uri1 = sch == null ? Uri.parse("http://" + src) : null;
					String hos1 = uri1 != null ? uri1.getHost() : null;
					if (sch != null && sch.length() > 0 || hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1) {
						cursor.addRow(new CharSequence[]{String.valueOf(i), src, getResources().getString(R.string.mark_Go), "", getResources().getString(R.string.mark_Return)});
						i++;
					}
					cursor.addRow(new CharSequence[]{String.valueOf(i), src, getResources().getString(R.string.mark_Search), se, i > 0 ? "" : getResources().getString(R.string.mark_Return)});
					i++;
					if (sug != null)
						for (String v : sug) {
							cursor.addRow(new CharSequence[]{String.valueOf(i), v, getResources().getString(R.string.mark_Search), se, i > 0 ? "" : getResources().getString(R.string.mark_Return)});
							i++;
						}
					if (view.getSuggestionsAdapter() == null) {
						view.setSuggestionsAdapter(new SimpleCursorAdapter(view.getContext(), R.layout.suggestion_slot, cursor, new String[]{"mark", "name", "mark3", "mark2"}, new int[]{R.id.t_sug_mark, R.id.t_sug, R.id.t_sug_first, R.id.t_sug_se}));
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
					String res = c.getString(c.getColumnIndex("name"));
					boolean direct = c.getString(c.getColumnIndex("mark2")).length() == 0;
					System.out.println(res);
					System.out.println(direct);
					String vScheme = Uri.parse(res).getScheme();
					Intent in = new Intent();
					Bundle bu = new Bundle();
					if (direct && vScheme != null && vScheme.length() > 0)
						bu.putString("url", res);
					else if (direct) bu.putString("url", "http://" + res);
					else bu.putString("url", wSearch(res));
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
					Uri uri1 = sch == null ? Uri.parse("http://" + query) : null;
					String hos1 = uri1 != null ? uri1.getHost() : null;
					if (sch != null && sch.length() > 0)
						bu.putString("url", query);
					else if (hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1)
						bu.putString("url", "http://" + query);
					else bu.putString("url", wSearch(query));
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
									String se = "", res;
									Message msg = new Message();
									Bundle data = new Bundle();
									JSONArray array = null;
									try {
										switch (db.getString("searchEngine")) {
											case "Google":
												se = getResources().getString(R.string.google);
												List<String> attrs = Jsoup.connect(getResources().getString(R.string.su_google).replace("#content#", newText)).ignoreContentType(true).get().getElementsByTag("suggestion").eachAttr("data");
												String[] vGoogle = attrs.toArray(new String[0]);
												data.putStringArray("sug", vGoogle);
												break;
											case "Bing":
												se = getResources().getString(R.string.bing);
												res = Jsoup.connect(getResources().getString(R.string.su_bing).replace("#content#", newText)).ignoreContentType(true).get().body().html();
												JSONArray arrayBing = new JSONObject(res).getJSONObject("AS").getJSONArray("Results").getJSONObject(0).getJSONArray("Suggests");
												int k = arrayBing.length();
												String[] vBing = new String[k];
												for (int i = 0; i < k; i++)
													vBing[i] = arrayBing.getJSONObject(i).getString("Txt");
												data.putStringArray("sug", vBing);
												break;
											case "Baidu":
												se = getResources().getString(R.string.baidu);
												res = Jsoup.connect(getResources().getString(R.string.su_baidu).replace("#content#", newText)).get().body().html();
												array = new JSONObject(res.substring(res.indexOf('(') + 1, res.lastIndexOf(')'))).getJSONArray("s");
												break;
											case "Sogou":
												se = getResources().getString(R.string.sogou);
												res = Jsoup.connect(getResources().getString(R.string.su_sogou).replace("#content#", newText)).ignoreContentType(true).get().body().html();
												array = new JSONObject("{\"s\":[" + res.substring(res.indexOf('[') + 1, res.lastIndexOf(']')) + "]" + "}").getJSONArray("s").getJSONArray(1);
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
										data.putStringArray("sug", v);
									}
									data.putString("src", newText);
									data.putString("se", se);
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
//			final RadioGroup seGroup = view.findViewById(R.id.seGroup);
//			RadioButton seGoogle = view.findViewById(R.id.seGoogle);
//			RadioButton seBing = view.findViewById(R.id.seBing);
//			RadioButton seBaidu = view.findViewById(R.id.seBaidu);
//			RadioButton seSogou = view.findViewById(R.id.seSogou);
//			RadioButton seCustom = view.findViewById(R.id.seCustom);
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
//			final TableRow rowCSE = view.findViewById(R.id.rowCSE);
//			final TextView tpStatus = view.findViewById(R.id.tpStatus);
//			view.findViewById(R.id.btnImport).setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					File lastDir = Environment.getExternalStorageDirectory();
//					try {
//						lastDir = new File(MainActivity.db.getString("lastDir"));
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					FileDialog.fileOpen(view.getContext(), lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
//						@Override
//						public void onFileTouched(File[] files) {
//							try {
//								if (files != null && files.length > 0 && files[0] != null) {
//									File file = files[0];
//									FileInputStream is = new FileInputStream(file);
//									FileOutputStream os = openFileOutput("template.html", Context.MODE_PRIVATE);
//									int len = is.available();
//									int length, lengthTotal = 0;
//									byte[] b = new byte[4096];
//									while ((length = is.read(b)) != -1) {
//										os.write(b);
//										lengthTotal += length;
//									}
//									is.close();
//									os.flush();
//									os.close();
//									if (lengthTotal != len) throw new Exception();
//									Toast.makeText(MainActivity.this, "Successfully imported the template", Toast.LENGTH_SHORT).show();
//									tpStatus.setText(getResources().getString(R.string.exist));
//									tpStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//									db.put("lastDir", file.getParentFile().getAbsolutePath());
//									writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
//								} else throw new Exception();
//							} catch (Exception e) {
//								e.printStackTrace();
//								Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
//							}
//
//						}
//
//						@Override
//						public void onCanceled() {
//
//						}
//					});
//				}
//			});
			try {
				String seStr = db.getString("searchEngine");
				switch (seStr) {
					case "Google":
						spnSE.setSelection(0);
//						seGoogle.toggle();
						break;
					case "Bing":
//						seBing.toggle();
						spnSE.setSelection(1);
						break;
					case "Baidu":
//						seBaidu.toggle();
						spnSE.setSelection(2);
						break;
					case "Sogou":
//						seSogou.toggle();
						spnSE.setSelection(3);
						break;
					case "Custom":
//						seCustom.toggle();
						spnSE.setSelection(4);
						break;
				}

				vCSE.setEnabled(spnSE.getSelectedItemPosition() == 4);
//				vCSE.setEnabled(seCustom.isChecked());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				vCSE.setText(db.getString("customSearchEngine"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				sh.setChecked(db.getBoolean("showHidden"));
			} catch (Exception e) {
				e.printStackTrace();
			}
//			try {
//				File template = new File(getFilesDir(), "template.html");
//				if (template.exists() && template.length() > 0) {
//					tpStatus.setText(getResources().getString(R.string.exist));
//					tpStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//				} else {
//					tpStatus.setText(getResources().getString(R.string.not_exist));
//					tpStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			AlertDialog settingDialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("Settings")
					.setView(view)
					.setNegativeButton("Cancel", null)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								String vse = getResources().getString(R.string.se_google);
								switch (spnSE.getSelectedItemPosition()) {
									case 0:
										vse = getResources().getString(R.string.se_google);
										break;
									case 1:
										vse = getResources().getString(R.string.se_bing);
										break;
									case 2:
										vse = getResources().getString(R.string.se_baidu);
										break;
									case 3:
										vse = getResources().getString(R.string.se_sogou);
										break;
									case 4:
										vse = getResources().getString(R.string.se_custom);
										break;
								}
//								RadioButton checked = view.findViewById(seGroup.getCheckedRadioButtonId());
								db.put("searchEngine", vse);
//								db.put("searchEngine", checked.getText());
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								Uri uri = Uri.parse(vCSE.getText().toString());
								String sch = uri.getScheme();
								if (sch==null) uri = Uri.parse("http://"+uri.toString());
								db.put("customSearchEngine", uri.toString());
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								db.put("showHidden", sh.isChecked());
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					})
					.create();
			settingDialog.setCanceledOnTouchOutside(false);
			settingDialog.show();
//
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}}
			final Button ok = settingDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//			seGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//				@Override
//				public void onCheckedChanged(RadioGroup group, int checkedId) {
//					vCSE.setEnabled(checkedId == R.id.seCustom);
//					ok.setEnabled(checkedId != R.id.seCustom || !vCSE.getText().toString().equals(""));
//				}
//			});
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
							wGet(MainActivity.this, Uri.parse(getResources().getString(R.string.template_repo)), new File(getFilesDir(), "template.html"), true, true, new TWEditorWV.OnDownloadCompleteListener() {
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
					ok.setEnabled(!vCSE.isEnabled() || !vCSE.getText().toString().equals(""));
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

			//			if (Build.VERSION.SDK_INT >= 26) {
//			try {
////				Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher,null);
//				Bitmap bitmap = null;
//				Drawable drawable = getResources().getDrawable(R.mipmap.ic_splash,getTheme());
//				if (drawable instanceof BitmapDrawable) {
////					return ((BitmapDrawable) drawable).getBitmap();
//				} else if (drawable instanceof AdaptiveIconDrawable) {
//					Drawable backgroundDr = ((AdaptiveIconDrawable) drawable).getBackground();
//					Drawable foregroundDr = ((AdaptiveIconDrawable) drawable).getForeground();
//
//					Drawable[] drr = new Drawable[2];
//					drr[0] = backgroundDr;
//					drr[1] = foregroundDr;
//
//					LayerDrawable layerDrawable = new LayerDrawable(drr);
//
//					int width = layerDrawable.getIntrinsicWidth();
//					int height = layerDrawable.getIntrinsicHeight();
//
//					bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//
//					Canvas canvas = new Canvas(bitmap);
//
//					layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//					layerDrawable.draw(canvas);
//
////					return bitmap;
//				}
//System.out.println(bitmap.getByteCount());
//System.out.println(bitmap.getWidth());
//System.out.println(bitmap.getHeight());
//				FileOutputStream fileOutputStream = new FileOutputStream(new File("/sdcard/icon0.png"));
//				bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
//				fileOutputStream.flush();
//				fileOutputStream.close();

			return true;
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
			db = readJson(openFileInput("data.json"));
			if (db != null) {
				int i = 0;
				do {
					if (!new File(db.getJSONArray("wiki").getJSONObject(i).getString("path")).exists())
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
							db.put("wiki", removeUnderK(db.getJSONArray("wiki"), i));
						} else {
							db.getJSONArray("wiki").remove(i);
						}
					else i++;
				} while (i < db.getJSONArray("wiki").length());
				System.out.println(db.toString(2));
			}
			writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
			wikiListAdapter.reload(db);
			rvWikiList.setAdapter(wikiListAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (db.getJSONArray("wiki").length() == 0)
				noWiki.setVisibility(View.VISIBLE);
			else
				noWiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static JSONObject readJson(FileInputStream is) {
		try {
			byte[] b = new byte[is.available()];
			if (is.read(b) < 0) throw new Exception();
			is.close();
			return new JSONObject(new String(b));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean writeJson(FileOutputStream os, JSONObject vdb) {
		try {
			byte[] b = vdb.toString(2).getBytes();
			os.write(b);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

	public static JSONArray removeUnderK(JSONArray src, int index) {
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
		String ws = getResources().getString(R.string.s_google).replace(getResources().getString(R.string.s_arg), arg);
		try {
			String se = MainActivity.db.getString("searchEngine");
			switch (se) {
				case "Google":
					ws = getResources().getString(R.string.s_google).replace(getResources().getString(R.string.s_arg), arg);
					break;
				case "Bing":
					ws = getResources().getString(R.string.s_bing).replace(getResources().getString(R.string.s_arg), arg);
					break;
				case "Baidu":
					ws = getResources().getString(R.string.s_baidu).replace(getResources().getString(R.string.s_arg), arg);
					break;
				case "Sogou":
					ws = getResources().getString(R.string.s_sogou).replace(getResources().getString(R.string.s_arg), arg);
					break;
				case "Custom":
					ws = MainActivity.db.getString("customSearchEngine").replace(getResources().getString(R.string.s_arg), arg);
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ws;
	}

	static void wGet(final Context parent, Uri uri, final File dest) {
		wGet(parent, uri, dest, false, false, null);
	}


	static void wGet(final Context parent, Uri uri, final File dest, final boolean noNotification, final boolean noToast, final TWEditorWV.OnDownloadCompleteListener listener) {
		String sch = uri.getScheme();
		if (sch != null && sch.equals("blob")) uri = Uri.parse(uri.getSchemeSpecificPart());
		sch = uri.getScheme();
		if (sch == null || sch.length() == 0) uri = Uri.parse("http://" + uri.toString());
		try {
			URL url;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				url = new URL(uri.normalizeScheme().toString());
			else url = new URL(uri.toString());
			System.out.println(url.toString());
			final String id = MainActivity.genId();
			final int idt = Integer.parseInt(id.replaceAll("-", "").substring(0, 7), 16);
			final File cacheFile = new File(parent.getCacheDir(), id);
//			cacheFile.deleteOnExit();
			final URL urlX = url;
			final Uri uriX = uri;
			final NoLeakHandler handler = new NoLeakHandler(new NoLeakHandler.MessageHandledListener() {
				@Override
				public void onMessageHandled(Message msg) {
					if (msg != null) {
						Bundle data = msg.getData();
						if (data != null) {
							String toast = data.getString("toast");
							String filepath = data.getString("filepath");
							if (toast != null)
								Toast.makeText(parent, toast, Toast.LENGTH_SHORT).show();
							if (data.getBoolean("complete") && filepath!=null) listener.onDownloadComplete(new File(filepath));
							else if (data.getBoolean("failed")) listener.onDownloadFailed();
						}
					}
				}
			});
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message msg;
					Bundle bundle = new Bundle();
					try {
						final HttpURLConnection httpURLConnection;
						if (uriX.getScheme() != null && uriX.getScheme().equals("https"))
							httpURLConnection = (HttpsURLConnection) urlX.openConnection();
						else httpURLConnection = (HttpURLConnection) urlX.openConnection();
						httpURLConnection.connect();
						int len = httpURLConnection.getContentLength();
						InputStream is = httpURLConnection.getInputStream();
						FileOutputStream os = new FileOutputStream(cacheFile);
						FileOutputStream os2 = new FileOutputStream(dest);
						int length;
						int lengthTotal = 0;
						byte[] bytes = new byte[4096];
						if (!noToast) {
							bundle.putString("toast", parent.getResources().getString(R.string.downloading));
							msg = new Message();
							msg.setData(bundle);
							handler.sendMessage(msg);
						}
						Notification notification;
						System.out.println(len);
						while ((length = is.read(bytes)) != -1) {
							os.write(bytes, 0, length);
							lengthTotal += length;
							int p = Math.round((float) lengthTotal / (float) len * 100);
//							System.out.println(lengthTotal);
							if (!noNotification) {
								notification = new NotificationCompat.Builder(parent, id)
										.setSmallIcon(R.drawable.ic_download)
										.setContentTitle(parent.getResources().getString(R.string.downloading))
										.setContentText(String.valueOf(p) + '%')
										.setOngoing(true)
										.setShowWhen(true)
										.setProgress(100, p, false)
										.build();
								NotificationManagerCompat.from(parent).notify(id, idt, notification);
							}
						}
						System.out.println(lengthTotal);
						is.close();
						os.flush();
						os.close();
						if (len>0 && lengthTotal != len) throw new Exception();
						if (!noNotification) {
							notification = new NotificationCompat.Builder(parent, id)
									.setSmallIcon(R.drawable.ic_download)
									.setContentTitle(parent.getResources().getString(R.string.downloading))
									.setOngoing(true)
									.setShowWhen(true)
									.setProgress(0, 0, true)
									.build();
							NotificationManagerCompat.from(parent).notify(id, idt, notification);
						}
						FileInputStream is2 = new FileInputStream(cacheFile);
//						System.out.println(is2.available());
						byte[] b2 = new byte[4096];
						int l2, lt2 = 0;
						while ((l2 = is2.read(b2)) != -1) {
							os2.write(b2, 0, l2);
							lt2 += l2;
//							System.out.println(lt2);
						}
//						System.out.println(b2.length);
//						System.out.println(is.read(b2));
//						if (is.read(b2) < 0) throw new Exception();
						is.close();
//						os2.write(b2);
						os2.flush();
						os2.close();
						if (lt2 != lengthTotal) throw new Exception();
						if (!noNotification) NotificationManagerCompat.from(parent).cancel(id, idt);
						if (!noToast)
							bundle.putString("toast", parent.getResources().getString(R.string.download_complete));
						bundle.putBoolean("complete", true);
						bundle.putString("filepath",dest.getAbsolutePath());
						msg = new Message();
						msg.setData(bundle);
						handler.sendMessage(msg);
						cacheFile.delete();
					} catch (Exception e) {
						e.printStackTrace();
						if (!noToast) {
							bundle.putString("toast", parent.getResources().getString(R.string.download_failed));
						}
						bundle.putBoolean("failed", true);
						msg = new Message();
						msg.setData(bundle);
						handler.sendMessage(msg);
						cacheFile.delete();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(parent, parent.getResources().getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
		}
	}


}