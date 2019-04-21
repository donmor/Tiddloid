package indi.donmor.tiddloid;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import indi.donmor.tiddloid.utils.BackupListAdapter;
import indi.donmor.tiddloid.utils.NoLeakHandler;
import indi.donmor.tiddloid.utils.WikiListAdapter;

import com.github.donmor3000.filedialog.lib.FileDialog;

public class MainActivity extends AppCompatActivity {
	private RecyclerView rvWikiList;
	private TextView noWiki;
	private WikiListAdapter wikiListAdapter;
	public static JSONObject db;
//	private NoLeakHandler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.activity_main);
		try {
			db = readJson(openFileInput("data.json"));
			if (db == null) throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			db = new JSONObject();
			try {
//				if (Locale.getDefault().toString().equals("zh_CN")) {
//					db.put("searchEngine", "Baidu");
//				} else {
//					db.put("searchEngine", "Google");
//				}
				db.put("searchEngine", R.string.default_se);
				db.put("showHidden", false);
				db.put("wiki", new JSONArray());
				db.put("lastDir", Environment.getExternalStorageDirectory().getAbsolutePath());
				writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

//		System.out.println(Uri.parse("a.b").normalizeScheme().toString());

//		try {
//			final NoLeakHandler handler = new NoLeakHandler(this, new NoLeakHandler.MessageHandledListener() {
//				@Override
//				public void onMessageHandled(Message msg) {
//					String[] data = msg.getData().getStringArray("sug");
//					if (data != null) for (String vv : data) System.out.println(vv);
//				}
//			});
//			new Thread() {
//				public void run() {
//					try {
////					Document wert = ;
////						String res = Jsoup.connect("https://api.bing.com/qsonhs.aspx?q=123").ignoreContentType(true).get().body().html();
////						Toast.makeText(MainActivity.this, "X", Toast.LENGTH_SHORT).show();
//						String sd = Jsoup.connect("http://suggestqueries.google.com/complete/search?output=toolbar&hl=en&q=123").get().toString();
//						List<String> attrs = Jsoup.connect("https://suggestqueries.google.com/complete/search?output=toolbar&hl=en&q=123").ignoreContentType(true).get().getElementsByTag("suggestion").eachAttr("data");
//						String[] v = attrs.toArray(new String[attrs.size()]);
////						Toast.makeText(MainActivity.this, Jsoup.connect("http://suggestqueries.google.com/complete/search?output=toolbar&hl=en&q=123").get().toString(), Toast.LENGTH_SHORT).show();
////						Toast.makeText(MainActivity.this, "D", Toast.LENGTH_SHORT).show();
////						JSONArray array =new JSONObject(res).getJSONObject("AS").getJSONArray("Results").getJSONObject(0).getJSONArray("Suggests");
////						System.out.println(new JSONObject(res).getJSONObject("AS").getJSONArray("Results").getJSONObject(0).getJSONArray("Suggests").toString(2));
////						JSONArray array4 = new JSONObject("{\"s\":[" + res4.substring(res4.indexOf('[') + 1, res4.lastIndexOf(']')) +"]"+ "}").getJSONArray("s").getJSONArray(1);
////System.out.println("{\"k\":[" + res4.substring(res4.indexOf('[') + 1, res4.lastIndexOf(']')) +"]"+ "}");
////System.out.println(new JSONObject("{\"s\":[" + res4.substring(res4.indexOf('[') + 1, res4.lastIndexOf(']')) +"]"+ "}").getJSONArray("s").getJSONArray(1).toString(2));
////						System.out.println(new JSONObject("{s=").toString(2));
////						System.out.println(new JSONObject( "{"+"["+ res4.substring(res4.indexOf('[') + 1, res4.lastIndexOf(']')) +"]"+ "}").toString(2));
////						JSONArray array4 = new JSONObject( "{"+"["+ res4.substring(res4.indexOf('[') + 1, res4.lastIndexOf(']')) +"]"+ "}").getJSONArray("s");
////					System.out.println(res);
////					JSONObject xxx = ;
////					System.out.println(xxx.toString(2));
////					JSONArray array = new JSONObject(res.substring(res.indexOf('(') + 1, res.lastIndexOf(')'))).getJSONArray("s");
//
////						int k = array.length();
////						String[] v = new String[k];
////						for (int i = 0; i < k; i++)
////							v[i] = array.getString(i);
////					for (String vv:v) System.out.println(vv);
////						int k = array.length();
////						String[] v = new String[k];
////						for (int i = 0; i < k; i++)
////							v[i] = array.getJSONObject(i).getString("Txt");
////					for (String vv:v) System.out.println(vv);
//						String ee = "";
//					for (String vv:v) {System.out.println(vv);ee+=vv;}
////						Toast.makeText(MainActivity.this, ee, Toast.LENGTH_SHORT).show();
//						Message msg = new Message();
//						Bundle data = new Bundle();
////						data.putStringArray("sug", v);
//						msg.setData(data);
//						handler.sendMessage(msg);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}


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
															byte[] b = new byte[is.available()];
															int rd = is.read(b);
															is.close();
															if (rd == -1) throw new Exception();
															FileOutputStream os = new FileOutputStream(new File(btnWikiConfigPath.getText().toString()));
															os.write(b);
															os.flush();
															os.close();
															wikiConfigDialog.dismiss();
															Toast.makeText(MainActivity.this, "Wiki rolled back successfully", Toast.LENGTH_SHORT).show();
															loadPage(wikiData.getString("id"));
														} catch (Exception e) {
															e.printStackTrace();
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
							try {
								lastDir = new File(MainActivity.db.getString("lastDir"));
							} catch (Exception e) {
								e.printStackTrace();
							}
							FileDialog.fileOpen(wikiConfigDialog.getContext(), lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
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
																return pathname.exists() && pathname.isDirectory() && pathname.getName().equals(getResources().getString(R.string.backup_directory_path).replace("$filename$", f.getName()).substring(1));
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
																	Log.i("FileIO", "DEL " + f1.getName() + ": " + String.valueOf(f1.delete()));
																Log.i("FileIO", "DEL " + fb.getName() + ": " + String.valueOf(fb.delete()));
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
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_new) {
			boolean validTemplate = false;
			try {
				FileInputStream tmpIs = openFileInput("template.html");
				validTemplate = tmpIs.available() >= 0;
				tmpIs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (validTemplate) {
				File lastDir = Environment.getExternalStorageDirectory();
				try {
					lastDir = new File(MainActivity.db.getString("lastDir"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileDialog.fileSave(MainActivity.this, lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
					@Override
					public void onFileTouched(File[] files) {
						try {
							if (files != null && files.length > 0 && files[0] != null) {
								File file = files[0];
								FileInputStream is = openFileInput("template.html");
								byte[] b = new byte[is.available()];
								int x = is.read(b);
								is.close();
								if (x == -1) throw new Exception();
								FileOutputStream os = new FileOutputStream(file);
								os.write(b);
								os.flush();
								os.close();
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
				AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
						.setTitle("Notice")
						.setMessage("TiddlyWiki template is not exist. Click 'OK' and select a TiddlyWiki HTML file as template.")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								File lastDir = Environment.getExternalStorageDirectory();
								try {
									lastDir = new File(MainActivity.db.getString("lastDir"));
								} catch (Exception e) {
									e.printStackTrace();
								}
								FileDialog.fileOpen(MainActivity.this, lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
									@Override
									public void onFileTouched(File[] files) {
										try {
											if (files != null && files.length > 0 && files[0] != null) {
												File file = files[0];
												FileInputStream is = new FileInputStream(file);
												byte[] b = new byte[is.available()];
												if (is.read(b) < 0) throw new Exception();
												is.close();
												FileOutputStream os = openFileOutput("template.html", Context.MODE_PRIVATE);
												os.write(b);
												os.flush();
												os.close();
												Toast.makeText(MainActivity.this, "Successfully imported the template", Toast.LENGTH_SHORT).show();
												db.put("lastDir", file.getParentFile().getAbsolutePath());
												writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
											} else throw new Exception();
										} catch (Exception e) {
											e.printStackTrace();
											Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
										}
									}

									@Override
									public void onCanceled() {

									}
								});

							}
						}).create();
				alert.setCanceledOnTouchOutside(false);
				alert.show();
			}
		} else if (id == R.id.action_import) {
			File lastDir = Environment.getExternalStorageDirectory();
			try {
				lastDir = new File(MainActivity.db.getString("lastDir"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			FileDialog.fileOpen(MainActivity.this, lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
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
			final NoLeakHandler handler = new NoLeakHandler(MainActivity.this, new NoLeakHandler.MessageHandledListener() {
				@Override
				public void onMessageHandled(Message msg) {
					Bundle data = msg.getData();
					String src = data.getString("src");
					String se = data.getString("se");
//					String se = "";
//					try {
//						switch (db.getString("searchEngine")) {
//							case "Google":
//								se = getResources().getString(R.string.google);
//								break;
//							case "Bing":
//								se = getResources().getString(R.string.bing);
//								break;
//							case "Baidu":
//								se = getResources().getString(R.string.baidu);
//								break;
//							case "Sogou":
//								se = getResources().getString(R.string.sogou);
//								break;
//							case "Custom":
//								se = getResources().getString(R.string.search);
//								break;
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
					Uri uri = Uri.parse(src);
					String sch = uri.getScheme();
					String scs = uri.getSchemeSpecificPart();
					String aut = uri.getAuthority();
					String fra = uri.getFragment();
					String hos = uri.getHost();
					String[] pts = uri.getPathSegments().toArray(new String[uri.getPathSegments().size()]);
					String ptx = "";
					for (String ee : pts) ptx += "<" + ee + ">";
					String lps = uri.getLastPathSegment();
					String pth = uri.getPath();
					String que = uri.getQuery();
					String usr = uri.getUserInfo();
					int prt = uri.getPort();
					System.out.println(sch);
					System.out.println(scs);
					System.out.println(aut);
					System.out.println(fra);
					System.out.println(hos);
					System.out.println(ptx);
					System.out.println(lps);
					System.out.println(pth);
					System.out.println(que);
					System.out.println(usr);
					System.out.println(prt);
					System.out.println("=================");
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
//					if (sch != null && sch.length() > 0 || hos1 != null && hos1.indexOf('.') > 0 && hos1.length() > hos1.indexOf('.') + 1)
					cursor.addRow(new CharSequence[]{String.valueOf(i), src, getResources().getString(R.string.mark_Search), se, i > 0 ? "" : getResources().getString(R.string.mark_Return)});
					i++;
//					if (sug != null && !(sch != null && sch.length() > 0 && hos != null && hos.length() > 0))
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
			view.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
				@Override
				public boolean onSuggestionSelect(int position) {
					MatrixCursor c = (MatrixCursor) view.getSuggestionsAdapter().getItem(position);
					view.setQuery(c.getString(c.getColumnIndex("name")), false);
					return true;
				}

				@Override
				public boolean onSuggestionClick(int position) {
					MatrixCursor c = (MatrixCursor) view.getSuggestionsAdapter().getItem(position);
					view.setQuery(c.getString(c.getColumnIndex("name")), true);
					return true;
				}
			});
			final AlertDialog URLDialog = new AlertDialog.Builder(MainActivity.this)
					.setView(view)
					.show();

			view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					Intent in = new Intent();
					Bundle bu = new Bundle();
					bu.putString("url", query);
					in.putExtras(bu).setClass(MainActivity.this, TWEditorWV.class);
					startActivity(in);
					URLDialog.dismiss();
					return false;
				}

				@Override
				public boolean onQueryTextChange(final String newText) {
					if (newText.length() > 0)
						new Thread() {
							public void run() {
								try {
									String se = "", res = "";
									Message msg = new Message();
									Bundle data = new Bundle();
									JSONArray array = null;
									try {
										switch (db.getString("searchEngine")) {
											case "Google":
												se = getResources().getString(R.string.google);
												List<String> attrs = Jsoup.connect(getResources().getString(R.string.su_google).replace("#content#",newText)).ignoreContentType(true).get().getElementsByTag("suggestion").eachAttr("data");
												String[] vGoogle = attrs.toArray(new String[0]);
												data.putStringArray("sug", vGoogle);
												break;
											case "Bing":
												se = getResources().getString(R.string.bing);
												res = Jsoup.connect(getResources().getString(R.string.su_bing).replace("#content#",newText)).ignoreContentType(true).get().body().html();
												JSONArray arrayBing =new JSONObject(res).getJSONObject("AS").getJSONArray("Results").getJSONObject(0).getJSONArray("Suggests");
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
												array = new JSONObject("{\"s\":[" + res.substring(res.indexOf('[') + 1, res.lastIndexOf(']')) +"]"+ "}").getJSONArray("s").getJSONArray(1);
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
					return false;
				}
			});
		} else if (id == R.id.action_settings) {
			final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.settings_dialog, null);
			final RadioGroup seGroup = view.findViewById(R.id.seGroup);
			RadioButton seGoogle = view.findViewById(R.id.seGoogle);
			RadioButton seBing = view.findViewById(R.id.seBing);
			RadioButton seBaidu = view.findViewById(R.id.seBaidu);
			RadioButton seSogou = view.findViewById(R.id.seSogou);
			RadioButton seCustom = view.findViewById(R.id.seCustom);
			final EditText vCSE = view.findViewById(R.id.customSE);
			final CheckBox sh = view.findViewById(R.id.cbHidden);
			final TextView tpStatus = view.findViewById(R.id.tpStatus);
			view.findViewById(R.id.btnImport).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					File lastDir = Environment.getExternalStorageDirectory();
					try {
						lastDir = new File(MainActivity.db.getString("lastDir"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					FileDialog.fileOpen(view.getContext(), lastDir, new String[]{"text/html"}, new FileDialog.OnFileTouchedListener() {
						@Override
						public void onFileTouched(File[] files) {
							try {
								if (files != null && files.length > 0 && files[0] != null) {
									File file = files[0];
									FileInputStream is = new FileInputStream(file);
									byte[] b = new byte[is.available()];
									if (is.read(b) < 0) throw new Exception();
									is.close();
									FileOutputStream os = openFileOutput("template.html", Context.MODE_PRIVATE);
									os.write(b);
									os.flush();
									os.close();
									Toast.makeText(MainActivity.this, "Successfully imported the template", Toast.LENGTH_SHORT).show();
									tpStatus.setText(getResources().getString(R.string.exist));
									tpStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
									db.put("lastDir", file.getParentFile().getAbsolutePath());
									writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
								} else throw new Exception();
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
							}

						}

						@Override
						public void onCanceled() {

						}
					});
				}
			});
			try {
				String seStr = db.getString("searchEngine");
				switch (seStr) {
					case "Google":
						seGoogle.toggle();
						break;
					case "Bing":
						seBing.toggle();
						break;
					case "Baidu":
						seBaidu.toggle();
						break;
					case "Sogou":
						seSogou.toggle();
						break;
					case "Custom":
						seCustom.toggle();
						break;
				}
				vCSE.setEnabled(seCustom.isChecked());
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
			try {
				FileInputStream tmpIs = openFileInput("template.html");
				boolean validTemplate = tmpIs.available() >= 0;
				tmpIs.close();
				if (validTemplate) {
					tpStatus.setText(getResources().getString(R.string.exist));
					tpStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
				} else {
					tpStatus.setText(getResources().getString(R.string.not_exist));
					tpStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			AlertDialog settingDialog = new AlertDialog.Builder(MainActivity.this)
					.setTitle("Settings")
					.setView(view)
					.setNegativeButton("Cancel", null)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								RadioButton checked = view.findViewById(seGroup.getCheckedRadioButtonId());
								db.put("searchEngine", checked.getText());
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								db.put("customSearchEngine", vCSE.getText().toString());
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
//
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}}
			final Button ok = settingDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			seGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					vCSE.setEnabled(checkedId == R.id.seCustom);
					ok.setEnabled(checkedId != R.id.seCustom || !vCSE.getText().toString().equals(""));
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

	private static String genId() {
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