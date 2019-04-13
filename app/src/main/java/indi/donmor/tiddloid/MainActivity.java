package indi.donmor.tiddloid;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

import indi.donmor.tiddloid.utils.FileDialogAdapter;
import indi.donmor.tiddloid.utils.MimeTypeUtil;
import indi.donmor.tiddloid.utils.NoLeakHandler;
import indi.donmor.tiddloid.utils.OnFileTouchedListener;
import indi.donmor.tiddloid.utils.WikiListAdapter;

public class MainActivity extends AppCompatActivity {
	FloatingActionsMenu f_menu;
	RecyclerView rvWikiList;
	TextView noWiki;
	WikiListAdapter wikiListAdapter;
	Handler vHandler;
	public static JSONObject db;

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
				if (Locale.getDefault().toString().equals("zh_CN")) {
					db.put("searchEngine", "Baidu");
				} else {
					db.put("searchEngine", "Google");
				}
				db.put("showHidden", false);
				db.put("wiki", new JSONArray());
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
//				String id = wikiListAdapter.getId(position);
//				if (id != null) {
				try {
//						int pos = 0;
//						for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
//							if (MainActivity.db.getJSONArray("wiki").getJSONObject(i).getString("id").equals(id)) {
//								pos = i;
//								break;
//							}
//						}
					final JSONObject wikiData = MainActivity.db.getJSONArray("wiki").getJSONObject(position);
					View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.wikiconfig_dialog, null);
//								ImageButton btnChangeFile = view.findViewById(R.id.btnChangeFile);
//								TextView lblWikiConfigTitle = view.findViewById(R.id.lblWikiConfigTitle);
//								lblWikiConfigTitle.setText(wikiData.getString("name"));
//								final TextView lblWikiConfigPath = view.findViewById(R.id.lblWikiConfigPath);
//								lblWikiConfigPath.setText(wikiData.getString("path"));
					final Button btnWikiConfigPath = view.findViewById(R.id.btnWikiConfigPath);
					btnWikiConfigPath.setText(wikiData.getString("path"));
					final CheckBox cbBackup = view.findViewById(R.id.cbBackup);
					cbBackup.setChecked(wikiData.getBoolean("backup"));
					final LinearLayout frmBackupList = view.findViewById(R.id.frmBackupList);
					if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE); else frmBackupList.setVisibility(View.GONE);
					cbBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if (cbBackup.isChecked()) frmBackupList.setVisibility(View.VISIBLE); else frmBackupList.setVisibility(View.GONE);
							;//TODO
						}
					});
					final TextView lblNoBackup = view.findViewById(R.id.lblNoBackup);
					final RecyclerView rvBackupList = view.findViewById(R.id.rvBackupList);
					Button btnRemoveWiki = view.findViewById(R.id.btnRemoveWiki);

					final AlertDialog wikiConfigDialog = new AlertDialog.Builder(MainActivity.this)
							.setTitle(wikiData.getString("name"))
							.setIcon(getResources().getDrawable(R.drawable.ic_description_black_24dp))
							.setView(view)
							.setPositiveButton("Close", null)
							.create();
					wikiConfigDialog.setCanceledOnTouchOutside(false);
					wikiConfigDialog.show();
					btnWikiConfigPath.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							fileOpen(wikiConfigDialog.getContext(), "text/html", new OnFileTouchedListener() {
								@Override
								public void onFileTouched(DialogInterface dialog, File file) {
									if (file != null) {
										try {
											boolean exist = false;
											for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
												if (db.getJSONArray("wiki").getJSONObject(i).getString("path").equals(file.getAbsolutePath())) {
													exist = true;
//																id = db.getJSONArray("wiki").getJSONObject(i).getString("id");
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
										} catch (Exception e) {
											e.printStackTrace();
											Toast.makeText(wikiConfigDialog.getContext(), "Data error", Toast.LENGTH_SHORT).show();
										}
									} else
										Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
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
												File f = new File(wikiData.getString("path"));
												db.getJSONArray("wiki").remove(position);
												writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
												if (cbDelFile.isChecked()) {
													try {
														File fb = f.getParentFile().listFiles(new FileFilter() {
															@Override
															public boolean accept(File pathname) {
																;//TODO
																return false;
															}
														})[0];
														if (cbDelBackups.isChecked() && fb.isDirectory()) {
															File[] b = fb.listFiles();
															for (File f1 : b)
																Log.i("FileIO", "DEL " + f1.getName() + ": " + String.valueOf(f1.delete()));
															Log.i("FileIO", "DEL " + fb.getName() + ": " + String.valueOf(fb.delete()));
														}
													} catch (Exception e) {
														e.printStackTrace();
													}
													Log.i("FileIO", "DEL " + f.getName() + ": " + String.valueOf(f.delete()));
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
				;
//				} else {
//					Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
//				}
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
				;
			}
		});

		final ImageView dim = findViewById(R.id.dim);
		dim.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				f_menu.collapse();
			}
		});
		vHandler = new NoLeakHandler(this, new NoLeakHandler.MessageHandledListener() {
			@Override
			public void onMessageHandled(Message msg) {
				if (msg.what == 0 || msg.what == 1) {
					float a = Float.parseFloat((String) msg.obj);
					dim.setAlpha(a);
					if (dim.getAlpha() > 0) {
						dim.setVisibility(View.VISIBLE);
					} else {
						dim.setVisibility(View.GONE);
					}
					switch (msg.what) {
						case 0:
							dim.setClickable(false);
							break;
						case 1:
							dim.setClickable(true);
							break;
					}
				}
			}
		});
		f_menu = findViewById(R.id.fab);
		f_menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
			@Override
			public void onMenuExpanded() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i <= 25; i++) {
							try {
								Thread.sleep(4);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Message msg = vHandler.obtainMessage();
							msg.what = 1;
							msg.obj = String.valueOf(Math.pow(i * 0.04, 2) * 0.5);
							vHandler.sendMessage(msg);
						}
					}
				}).start();
			}

			@Override
			public void onMenuCollapsed() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 1; i <= 25; i++) {
							try {
								Thread.sleep(4);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Message msg = vHandler.obtainMessage();
							msg.what = 0;
							msg.obj = String.valueOf((1 - Math.pow(i * 0.04, 2)) * 0.5);
							vHandler.sendMessage(msg);
						}
					}
				}).start();
			}
		});
		FloatingActionButton f_fork = findViewById(R.id.fab_fork);
		FloatingActionButton f_import = findViewById(R.id.fab_import);
		FloatingActionButton f_new5 = findViewById(R.id.fab_new5);
		f_fork.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				f_menu.collapse();
				View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.url_dialog, null);
				final EditText vUrl = (EditText) view.findViewById(R.id.t_url);
				AlertDialog URLDialog = new AlertDialog.Builder(MainActivity.this)
						.setTitle("Input URL or search:")
						.setView(view)
						.setNegativeButton("Cancel", null)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String u = vUrl.getText().toString();
								Intent in = new Intent();
								Bundle bu = new Bundle();
								bu.putString("url", u);
								in.putExtras(bu)
										.setClass(MainActivity.this, TWEditorWV.class);
								startActivity(in);
							}
						})
						.create();
				URLDialog.setCanceledOnTouchOutside(false);
				URLDialog.show();
				final Button ok = URLDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				ok.setEnabled(false);
				vUrl.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						if (vUrl.getText().toString().length() > 0) ok.setEnabled(true);
						else ok.setEnabled(false);
					}
				});
				vUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						if (ok.isEnabled()) ok.callOnClick();
						return true;
					}
				});
			}
		});
		f_import.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				f_menu.collapse();
				fileOpen(MainActivity.this, "text/html", new OnFileTouchedListener() {
					@Override
					public void onFileTouched(DialogInterface dialog, File file) {
						if (file != null) {
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
									w.put("name", "");
									w.put("id", id);
									w.put("path", file.getAbsolutePath());
									w.put("backup", false);
									db.getJSONArray("wiki").put(db.getJSONArray("wiki").length(), w);
									if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db))
										throw new Exception();
								}
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

				});
			}
		});

		f_new5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				f_menu.collapse();
				boolean validTemplate = false;
				try {
					FileInputStream tmpIs = openFileInput("template.html");
					validTemplate = tmpIs.available() >= 0;
					tmpIs.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (validTemplate) {
					fileSave(MainActivity.this, "text/html", new OnFileTouchedListener() {
						@Override
						public void onFileTouched(DialogInterface dialog, File file) {
//							boolean wroteFile = false;
							try {
								FileInputStream is = openFileInput("template.html");
								byte[] b = new byte[is.available()];
								int x = is.read(b);
								is.close();
								if (x == -1) throw new Exception();
								FileOutputStream os = new FileOutputStream(file);
								os.write(b);
								os.flush();
								os.close();
//								wroteFile = true;
//								boolean jsonWrote = false;
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
//										jsonWrote = false;
									} else {
										JSONObject w = new JSONObject();
										w.put("name", "");
										w.put("id", id);
										w.put("path", file.getAbsolutePath());
										w.put("backup", false);
										db.getJSONArray("wiki").put(db.getJSONArray("wiki").length(), w);
										if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db))
											throw new Exception();
//										jsonWrote = true;
									}
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
								}
								MainActivity.this.onResume();
								if (!loadPage(id))
									Toast.makeText(MainActivity.this, "Error loading the page", Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, "Failed creating the file", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
							.setTitle("Notice")
							.setMessage("TiddlyWiki template is not exist. Click 'OK' and select a TiddlyWiki HTML file as template.")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									fileOpen(MainActivity.this, "text/html", new OnFileTouchedListener() {
										@Override
										public void onFileTouched(DialogInterface dialog, File file) {
											try {
												FileInputStream is = new FileInputStream(file);
												byte[] b = new byte[is.available()];
												if (is.read(b) < 0) throw new Exception();
												is.close();
												FileOutputStream os = openFileOutput("template.html", Context.MODE_PRIVATE);
												os.write(b);
												os.flush();
												os.close();
												Toast.makeText(MainActivity.this, "Successfully imported the template", Toast.LENGTH_SHORT).show();
											} catch (Exception e) {
												e.printStackTrace();
												Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
											}
										}
									});

								}
							}).create();
					alert.setCanceledOnTouchOutside(false);
					alert.show();
				}
			}
		});
	}

	Boolean loadPage(String id) {
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
		vHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}


	@Override
	public void onBackPressed() {
		if (f_menu.isExpanded()) {
			f_menu.collapse();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
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
					fileOpen(view.getContext(), "text/html", new OnFileTouchedListener() {
						@Override
						public void onFileTouched(DialogInterface dialog, File file) {
							try {
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
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
							}

						}
					});
				}
			});
//			byte[] tp = null;
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
//				if (seStr.equals("Google")) seGoogle.toggle();
//				else if (seStr.equals("Bing")) seBing.toggle();
//				else if (seStr.equals("Baidu")) seBaidu.toggle();
//				else if (seStr.equals("Sogou")) seSogou.toggle();
//				else if (seStr.equals("Custom")) seCustom.toggle();
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
				for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
//					System.out.println(new File(db.getJSONArray("wiki").getJSONObject(i).getString("path")).exists());
					if (!new File(db.getJSONArray("wiki").getJSONObject(i).getString("path")).exists())
						db.getJSONArray("wiki").remove(i);
				}
			}
			System.out.println(db.toString(2));
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


	public static JSONObject readJson(FileInputStream is) {
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
	public void checkPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}
	}

	public static void fileOpen(Context parent, String mimeTypes, final OnFileTouchedListener listener) {
		View view = LayoutInflater.from(parent).inflate(R.layout.file_dialog, null);
		view.findViewById(R.id.saveFnFrame).setVisibility(View.GONE);
		final Button btnBack = view.findViewById(R.id.btnBack);
		view.findViewById(R.id.btnNewFolder).setVisibility(View.GONE);
		final TextView lblPath = view.findViewById(R.id.lblPath);
		lblPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
		final RecyclerView dir = view.findViewById(R.id.diFileList);
		dir.setLayoutManager(new LinearLayoutManager(view.getContext()));
		final AlertDialog fileDialog = new AlertDialog.Builder(parent)
				.setTitle("Open")
				.setView(view)
				.create();
		fileDialog.setCanceledOnTouchOutside(false);
		fileDialog.show();
		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), mimeTypes, Environment.getExternalStorageDirectory());
		dirAdapter.setOnItemClickListener(new FileDialogAdapter.ItemClickListener() {
			@Override
			public void onItemClick(int position) {
				File f = dirAdapter.getFile(position);
				if (f != null && f.exists()) {
					if (f.isDirectory()) {
						dirAdapter.setDir(f);
						dir.setAdapter(dirAdapter);
						lblPath.setText(f.getAbsolutePath());
						btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
					} else if (f.isFile()) {
						listener.onFileTouched(fileDialog, f);
						fileDialog.dismiss();
					}
				} else {
					dirAdapter.setRoot();
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
				}
			}
		});
		dir.setAdapter(dirAdapter);
		btnBack.setEnabled(dirAdapter.getDevices().length > 1);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File f = dirAdapter.getParentDir();
				if (f != null && f.exists() && f.isDirectory()) {
					dirAdapter.setDir(f);
					dir.setAdapter(dirAdapter);
					lblPath.setText(f.getAbsolutePath());
					btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
				} else {
					dirAdapter.setRoot();
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
					btnBack.setEnabled(false);
				}
			}
		});
	}

	public static void fileSave(final Context parent, final String mimeTypes, final OnFileTouchedListener listener) {
		View view = LayoutInflater.from(parent).inflate(R.layout.file_dialog, null);
		view.findViewById(R.id.saveFnFrame).setVisibility(View.VISIBLE);
		final EditText fName = view.findViewById(R.id.save_f_name);
		final Button btnBack = view.findViewById(R.id.btnBack);
		ImageButton btnNewFolder = view.findViewById(R.id.btnNewFolder);
		final TextView lblPath = view.findViewById(R.id.lblPath);
		lblPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
		final RecyclerView dir = view.findViewById(R.id.diFileList);
		dir.setLayoutManager(new LinearLayoutManager(view.getContext()));
		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), mimeTypes, Environment.getExternalStorageDirectory());

		final AlertDialog fileDialog = new AlertDialog.Builder(parent)
				.setTitle("Save as")
				.setView(view)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Ok", null)
				.create();
		fileDialog.setCanceledOnTouchOutside(false);
		fileDialog.show();
		btnBack.setEnabled(dirAdapter.getDevices().length > 1);
		btnNewFolder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View view1 = LayoutInflater.from(fileDialog.getContext()).inflate(R.layout.fn_slot, null);
				final EditText editText = view1.findViewById(R.id.eFn);
//				final AlertDialog newFolderDialog = new AlertDialog.Builder(parent)
				final AlertDialog newFolderDialog = new AlertDialog.Builder(fileDialog.getContext())
						.setTitle("New Folder")
						.setView(view1)
						.setNegativeButton("Cancel", null)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								File f = new File(dirAdapter.getCurrentDir().getAbsolutePath() + "/" + editText.getText().toString());
								if (f.exists())
									Toast.makeText(fileDialog.getContext(), "The folder is already exist", Toast.LENGTH_SHORT).show();
								else {
									boolean d = f.mkdir();
									if (d && f.exists()) {
										dirAdapter.setDir(f);
										dir.setAdapter(dirAdapter);
										lblPath.setText(f.getAbsolutePath());
										btnBack.setEnabled(true);
									}
								}
							}
						})
						.show();
				final Button okx = newFolderDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				okx.setEnabled(false);
				editText.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						if (editText.getText().toString().startsWith(".")) {
							try {
								if (!db.getBoolean("showHidden")) {
									okx.setEnabled(false);
									Toast.makeText(newFolderDialog.getContext(), "Hidden files could not be created under current settings", Toast.LENGTH_SHORT).show();
								} else if (editText.getText().toString().substring(1).length() == 0)
									okx.setEnabled(false);
								else okx.setEnabled(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (editText.getText().toString().startsWith("+") || editText.getText().toString().startsWith("-")) {
							okx.setEnabled(false);
							Toast.makeText(newFolderDialog.getContext(), "These characters  are not allowed at the beginning of a filename:\n+ -", Toast.LENGTH_SHORT).show();
						} else if (illegalFilename(editText.getText().toString())) {
							okx.setEnabled(false);
							Toast.makeText(newFolderDialog.getContext(), "These characters  are not allowed in a filename:\n\" * / : < > ? \\ |", Toast.LENGTH_SHORT).show();
						} else if (editText.getText().toString().length() > 0) okx.setEnabled(true);
						else okx.setEnabled(false);
					}
				});
				editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						if (okx.isEnabled()) okx.callOnClick();
						return true;
					}
				});
			}
		});
		final Button ok = fileDialog.getButton(AlertDialog.BUTTON_POSITIVE);
		ok.setEnabled(false);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String fn = fName.getText().toString();
				try {
					if (!(MimeTypeUtil.meetsMimeTypes(fn, mimeTypes)))
						fn = fn + MimeTypeUtil.getExtensions(mimeTypes)[0];
				} catch (Exception e) {
					e.printStackTrace();
				}
				String fPath = dirAdapter.getCurrentDir().getAbsolutePath() + "/" + fn;
				final File of = new File(fPath);
				if (of.exists()) {
					new AlertDialog.Builder(fileDialog.getContext())
							.setTitle("Warning")
							.setMessage("The file already exists. Do you want to overwrite?")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									listener.onFileTouched(fileDialog, of);
									fileDialog.dismiss();
								}
							})
							.setNegativeButton("Cancel", null)
							.show();
				} else {
					listener.onFileTouched(fileDialog, of);
					fileDialog.dismiss();
				}

			}
		});
		dirAdapter.setOnItemClickListener(new FileDialogAdapter.ItemClickListener() {
			@Override
			public void onItemClick(int position) {
				File f = dirAdapter.getFile(position);
				if (f != null && f.exists()) {
					if (f.isDirectory()) {
						dirAdapter.setDir(f);
						dir.setAdapter(dirAdapter);
						lblPath.setText(f.getAbsolutePath());
						btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
					} else if (f.isFile()) {
						String fn = f.getName();
						if (fn.equals(fName.getText().toString())) ok.callOnClick();
						else fName.setText(fn);
					}
				} else {
					dirAdapter.setRoot();
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
				}
			}
		});
		dir.setAdapter(dirAdapter);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File f = dirAdapter.getParentDir();
				if (f != null && f.exists() && f.isDirectory()) {
					dirAdapter.setDir(f);
					dir.setAdapter(dirAdapter);
					lblPath.setText(f.getAbsolutePath());
					btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
				} else {
					dirAdapter.setRoot();
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
					btnBack.setEnabled(false);
				}
			}
		});
		fName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (fName.getText().toString().startsWith(".")) {
					try {
						if (!db.getBoolean("showHidden")) {
							ok.setEnabled(false);
							Toast.makeText(fileDialog.getContext(), "Hidden files could not be created under current settings", Toast.LENGTH_SHORT).show();
						} else if (fName.getText().toString().substring(1).length() == 0)
							ok.setEnabled(false);
						else ok.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (fName.getText().toString().startsWith("+") || fName.getText().toString().startsWith("-")) {
					ok.setEnabled(false);
					Toast.makeText(fileDialog.getContext(), "These characters  are not allowed at the beginning of a filename:\n+ -", Toast.LENGTH_SHORT).show();
				} else if (illegalFilename(fName.getText().toString())) {
					ok.setEnabled(false);
					Toast.makeText(fileDialog.getContext(), "These characters  are not allowed in a filename:\n\" * / : < > ? \\ |", Toast.LENGTH_SHORT).show();
				} else if (fName.getText().toString().length() > 0) ok.setEnabled(true);
				else ok.setEnabled(false);
			}
		});
		fName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (ok.isEnabled()) ok.callOnClick();
				return true;
			}
		});
	}

	public static File[] getStorage(Context context) {
		StorageManager storageManager = (StorageManager) context.getSystemService(STORAGE_SERVICE);
		try {
			Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
			method.setAccessible(true);
			Object result = method.invoke(storageManager);
			if (result instanceof String[]) {
				String[] pathArray = (String[]) result;
				StatFs statFs;
				File[] files = new File[pathArray.length];
				int i = 0;
				for (String path : pathArray) {
					File file = new File(path);
					System.out.println(file.getAbsolutePath());
					if (!TextUtils.isEmpty(path) && file.exists()) {
						statFs = new StatFs(path);
						if (statFs.getBlockCountLong() * statFs.getBlockSizeLong() != 0
								&& file.canWrite()
						) {
							System.out.println(file.canWrite());
							files[i] = file;
							i++;
						}
					}
				}
				File[] files1 = new File[i];
				System.arraycopy(files, 0, files1, 0, i);
				return files1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			File externalFolder = Environment.getExternalStorageDirectory();
			if (externalFolder != null) {
				File[] d = new File[1];
				d[0] = externalFolder;
				return d;
			}
		}
		return null;
	}

	public static String genId() {
		return UUID.randomUUID().toString();
	}

	public static boolean illegalFilename(CharSequence e) {
		String v = e.toString();
		return v.indexOf(0) >= 0
				|| v.indexOf(1) >= 0
				|| v.indexOf(2) >= 0
				|| v.indexOf(3) >= 0
				|| v.indexOf(4) >= 0
				|| v.indexOf(5) >= 0
				|| v.indexOf(6) >= 0
				|| v.indexOf(7) >= 0
				|| v.indexOf(8) >= 0
				|| v.indexOf(9) >= 0
				|| v.indexOf(10) >= 0
				|| v.indexOf(11) >= 0
				|| v.indexOf(12) >= 0
				|| v.indexOf(13) >= 0
				|| v.indexOf(14) >= 0
				|| v.indexOf(15) >= 0
				|| v.indexOf(16) >= 0
				|| v.indexOf(17) >= 0
				|| v.indexOf(18) >= 0
				|| v.indexOf(19) >= 0
				|| v.indexOf(20) >= 0
				|| v.indexOf(21) >= 0
				|| v.indexOf(22) >= 0
				|| v.indexOf(23) >= 0
				|| v.indexOf(24) >= 0
				|| v.indexOf(25) >= 0
				|| v.indexOf(26) >= 0
				|| v.indexOf(27) >= 0
				|| v.indexOf(28) >= 0
				|| v.indexOf(29) >= 0
				|| v.indexOf(30) >= 0
				|| v.indexOf(31) >= 0
				|| v.indexOf(34) >= 0
				|| v.indexOf(42) >= 0
				|| v.indexOf(47) >= 0
				|| v.indexOf(58) >= 0
				|| v.indexOf(60) >= 0
				|| v.indexOf(62) >= 0
				|| v.indexOf(63) >= 0
				|| v.indexOf(92) >= 0
				|| v.indexOf(124) >= 0
				|| v.indexOf(127) >= 0;
	}
}