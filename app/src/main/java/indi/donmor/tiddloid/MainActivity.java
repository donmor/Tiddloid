package indi.donmor.tiddloid;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import indi.donmor.tiddloid.utils.FileDialogAdapter;
import indi.donmor.tiddloid.utils.MimeTypeUtil;
import indi.donmor.tiddloid.utils.NoLeakHandler;
import indi.donmor.tiddloid.utils.OnFileTouchedListener;
import indi.donmor.tiddloid.utils.WikiListAdapter;

public class MainActivity extends AppCompatActivity {

	FloatingActionsMenu f_menu;

	RecyclerView rv_wikilist;
	TextView nowiki;

	WikiListAdapter wikiListAdapter;

	Handler vHandler;

//    public static boolean hiddenFiles;

	public static JSONObject db;

//    String se, cse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//        try {
//            JSONObject a = new JSONObject();
//            a.put("e","v3");
//            JSONArray m = new JSONArray();
//            JSONObject i1 = new JSONObject();
//            i1.put("i","1");
//            i1.put("j","q");
//            m.put(i1);
//            JSONObject i2 = new JSONObject();
//            i2.put("i","2");
//            i2.put("j","w");
//            m.put(i2);
//            JSONObject i3 = new JSONObject();
//            i3.put("i","3");
//            i3.put("j","e");
//            m.put(i3);
//            JSONObject i4 = new JSONObject();
//            i4.put("i","4");
//            i4.put("j","r");
//            m.put(i4);
//            JSONObject i5 = new JSONObject();
//            i5.put("i","5");
//            i5.put("j","t");
//            m.put(i5);
//            a.put("a", m);
//            FileOutputStream os = openFileOutput("t1.json",MODE_PRIVATE);
//            byte[] b = a.toString(2).getBytes();
//            os.write(b);
//            os.flush();
//            os.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            FileInputStream is = openFileInput("t1.json");
//            byte[] b = new byte[is.available()];
//            is.read();
//            is.close();
//            String jsonStr = new String(b);
////            if(jsonStr.startsWith("\ufeff")){
////                jsonStr= jsonStr.substring(1);
////            }
//            JSONObject db = new JSONObject(jsonStr);
//            for (int i = 0; i < db.getJSONArray("a").length(); i++) {
//                System.out.println(db.getString("e"));
//                System.out.println(db.getJSONArray("a").getJSONObject(i).getString("i"));
//                System.out.println(db.getJSONArray("a").getJSONObject(i).getString("j"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//		Toast.makeText(this, ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Toast.LENGTH_SHORT).show();
//		System.out.println(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
//		System.out.println(PackageManager.PERMISSION_GRANTED);
//		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//		}

//		System.out.println(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));


		try {
			db = readJson(openFileInput("data.json"));
			if (db == null) throw new Exception();
//            Toast.makeText(this, db.toString(2), Toast.LENGTH_SHORT).show();
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
//				db.put("extpath", "");
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
//			try {
//				if (!(db.getString("extpath").length() > 0))
//					startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 201);
//			} catch (Exception e) {
//				e.printStackTrace();
//				startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 201);
//			}
		}

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		nowiki = findViewById(R.id.t_nowiki);
		try {
			if (db.getJSONArray("wiki").length() == 0)
				nowiki.setVisibility(View.VISIBLE);
			else
				nowiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		View view = LayoutInflater.from(this).inflate(R.layout.file_dialog, null);
		final SwipeRefreshLayout refreshLayout = findViewById(R.id.swiperereshlayout);
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
		rv_wikilist = findViewById(R.id.rv_wikilist);
		rv_wikilist.setLayoutManager(new LinearLayoutManager(view.getContext()));

		wikiListAdapter = new WikiListAdapter(this, db);
		wikiListAdapter.setReloadListener(new WikiListAdapter.ReloadListener() {
			@Override
			public void onReloaded(int count) {
				if (count > 0) nowiki.setVisibility(View.GONE);
				else nowiki.setVisibility(View.VISIBLE);
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
			public void onItemLongClick(int position) {
				String id = wikiListAdapter.getId(position);
				final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.settings_dialog, null);
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

//        final LinearLayout w_body = findViewById(R.id.body);
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
//                    System.out.println(a);
					dim.setAlpha(a);
//                    System.out.println(dim.getAlpha());
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
//                switch (msg.what) {
//                    case 0:
//                        float a = Float.parseFloat((String) msg.obj);
//                        System.out.println(a);
//                        dim.setAlpha(a);
//                        System.out.println(dim.getAlpha());
//                        if (dim.getAlpha() > 0) {
//                            dim.setVisibility(View.VISIBLE);
//                        } else {
//                            dim.setVisibility(View.GONE);
//                        }
//                        dim.setClickable(true);
//                        break;
//                    case 1:
//                        a = Float.parseFloat((String) msg.obj);
//                        System.out.println(a);
//                        dim.setAlpha(a);
//                        System.out.println(dim.getAlpha());
//                        if (dim.getAlpha() > 0) {
//                            dim.setVisibility(View.VISIBLE);
//                        } else {
//                            dim.setVisibility(View.GONE);
//                        }
//                        dim.setClickable(true);
//                        break;
//                }
			}
		});
//        w_body.setForegroundTintList(new ColorStateList());
//        w_body.tint;
		f_menu = findViewById(R.id.fab);
		f_menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
			@Override
			public void onMenuExpanded() {
//                w_body.setEnabled(false);
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
				;
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
				;

//                w_body.setEnabled(true);
//                dim.setVisibility(View.GONE);
//                for (int i = 50; i >= 50; i--) {
//                    dim.setAlpha(i/100);
//                }
				;
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
//                FileDialog fopenDialog = new OpenFileDialog();
//                fopenDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
//                fopenDialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());
//                Intent intent = new Intent()
//                        .setType("text/html")
//                        .setAction(Intent.ACTION_GET_CONTENT);
//
//                startActivityForResult(Intent.createChooser(intent, "Select a file"), 102);
//                try {
//                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "Failed opening the file.", Toast.LENGTH_SHORT).show();
//                    }
				fileOpen(MainActivity.this, "text/html", new OnFileTouchedListener() {
					@Override
					public void onFileTouched(DialogInterface dialog, File file) {
						if (file != null) {
							boolean jsonWrote = false;
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
									jsonWrote = false;
								} else {
									JSONObject w = new JSONObject();
									w.put("name", "");
									w.put("id", id);
									w.put("path", file.getAbsolutePath());
									w.put("backup", false);
									db.getJSONArray("wiki").put(db.getJSONArray("wiki").length(), w);
									if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db))
										throw new Exception();
									jsonWrote = true;
								}
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
							}
//							if (jsonWrote) {
//								wikiListAdapter.reload(db);
//								rv_wikilist.setAdapter(wikiListAdapter);
//							}
							MainActivity.this.onResume();
//                                Intent in = new Intent();
//                                Bundle bu = new Bundle();
//                                //                        bu.putString("url", filepath);
//                                System.out.println(file.getAbsolutePath());
//                                bu.putSerializable("wapp", new TWapp(file.getAbsolutePath(), null));
//                                in.putExtras(bu)
//                                        .setClass(MainActivity.this, TWEditorWV.class);
//                                startActivity(in);
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
				Boolean validTemplate = false;
				byte[] templateTW = null;
				try {
					FileInputStream tmis = openFileInput("template.html");
					templateTW = new byte[tmis.available()];
					tmis.read(templateTW);
					tmis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (templateTW != null) {
					fileSave(MainActivity.this, "text/html", new OnFileTouchedListener() {
						@Override
						public void onFileTouched(DialogInterface dialog, File file) {
//                            System.out.println(file.getAbsolutePath());
							boolean wroteFile = false;
							try {
								//                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
								//                        Toast.makeText(this, filepath, Toast.LENGTH_SHORT).show();
								FileInputStream is = openFileInput("template.html");
								byte[] b = new byte[is.available()];
								is.read(b);
								is.close();
//								file.createNewFile();
//								OutputStream os = null;
////								DocumentFile e = DocumentsUtils.getDocumentFile(file,false,MainActivity.this);
//								if (file.canWrite()) os = new FileOutputStream(file);
//								else {
//									DocumentFile f = DocumentFile.fromFile(file);
////									os = new FileOutputStream(f);
////									os = MainActivity.this.getContentResolver().openOutputStream(f.getUri());
////									os = DocumentsUtils.getOutputStream(MainActivity.this, file);
//								}
								FileOutputStream os = new FileOutputStream(file);
								os.write(b);
								os.flush();
								os.close();
								wroteFile = true;
								boolean jsonWrote = false;
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
										jsonWrote = false;
									} else {
										JSONObject w = new JSONObject();
										w.put("name", "");
										w.put("id", id);
										w.put("path", file.getAbsolutePath());
										w.put("backup", false);
										db.getJSONArray("wiki").put(db.getJSONArray("wiki").length(), w);
										if (!MainActivity.writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db))
											throw new Exception();
										jsonWrote = true;
									}
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(MainActivity.this, "Data error", Toast.LENGTH_SHORT).show();
								}
//								if (jsonWrote) {
//									wikiListAdapter.reload(db);
//									rv_wikilist.setAdapter(wikiListAdapter);
//                                    Intent in = new Intent();
//                                    Bundle bu = new Bundle();
//                                    //                        bu.putString("url", filepath);
//                                    System.out.println(file.getAbsolutePath());
//                                    bu.putSerializable("wapp", new TWapp(file.getAbsolutePath(), null));
//                                    in.putExtras(bu)
//                                            .setClass(MainActivity.this, TWEditorWV.class);
//                                    startActivity(in);
								;
//								}
								MainActivity.this.onResume();
								if (!loadPage(id))
									Toast.makeText(MainActivity.this, "Error loading the page", Toast.LENGTH_SHORT).show();


							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, "Failed creating the file", Toast.LENGTH_SHORT).show();
							}
						}
					});
//                    Intent intent = new Intent()
//                            .setType("text/html")
//                            .setAction(Intent.ACTION_CREATE_DOCUMENT);
//                    startActivityForResult(Intent.createChooser(intent, "Save As"), 101);
				} else {
					AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
							.setTitle("Notice")
							.setMessage("TiddlyWiki template is not exist. Click 'OK' and select a TiddlyWiki HTML file as template.")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent()
//                                            .setType("text/html")
//                                            .setAction(Intent.ACTION_GET_CONTENT);
//                                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 103);
									fileOpen(MainActivity.this, "text/html", new OnFileTouchedListener() {
										@Override
										public void onFileTouched(DialogInterface dialog, File file) {
											try {
//                                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
												FileInputStream is = new FileInputStream(file);
												byte[] b = new byte[is.available()];
												is.read(b);
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

	//    @Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (resultCode != this.RESULT_OK) {
//			return;
//		} else {
//			if (requestCode == 201) {
//				Uri treeUri = data.getData();
//				DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
//				grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//				getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//				Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
//						DocumentsContract.getTreeDocumentId(treeUri));
//				String path = docUri.getPath();
//				try {
////					db.put("extpath", treeUri.getPath());
//					System.out.println(new File(path).getAbsolutePath());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//            String filepath = null;
//            switch (requestCode) {
//                case 101:
////                    try {
////                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
////                        Toast.makeText(this, filepath, Toast.LENGTH_SHORT).show();
////                        FileInputStream is = openFileInput("template.html");
////                        byte[] b = new byte[is.available()];
////                        is.read(b);
////                        is.close();
////                        FileOutputStream os = new FileOutputStream(filepath);
////                        os.write(b);
////                        os.flush();
////                        os.close();
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                        Toast.makeText(this, "Failed creating the file.", Toast.LENGTH_SHORT).show();
////                    }
//                    break;
//                case 102:
////                    try {
////                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                        Toast.makeText(this, "Failed opening the file.", Toast.LENGTH_SHORT).show();
////                    }
////                    if (filepath != null) {
////                        Intent in = new Intent();
////                        Bundle bu = new Bundle();
//////                        bu.putString("url", filepath);
////                        bu.putSerializable("wapp", new TWapp(filepath, null));
////                        in.putExtras(bu)
////                                .setClass(MainActivity.this, TWEditorWV.class);
////                        startActivity(in);
////                    } else
////                        Toast.makeText(this, "Failed opening the file.", Toast.LENGTH_SHORT).show();
//                    break;
//                case 103:
////                    try {
////                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
////                        FileInputStream is = new FileInputStream(filepath);
////                        byte[] b = new byte[is.available()];
////                        is.read(b);
////                        is.close();
////                        FileOutputStream os = openFileOutput("template.html", Context.MODE_PRIVATE);
////                        os.write(b);
////                        os.flush();
////                        os.close();
////                        Toast.makeText(this, "Successfully imported the template.", Toast.LENGTH_SHORT).show();
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                        Toast.makeText(this, "Failed opening the file.", Toast.LENGTH_SHORT).show();
////                    }
//                    break;
//            }
//		}
//	}

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
				//                        bu.putString("url", filepath);
//                System.out.println(id);
				bu.putString("id", vid);
				in.putExtras(bu)
						.setClass(MainActivity.this, TWEditorWV.class);
				startActivity(in);
			} else throw new Exception();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
//            Toast.makeText(this, "Error loading page", Toast.LENGTH_SHORT).show();
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

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        if (keyCode == KeyEvent.KEYCODE_BACK && f_menu.isExpanded()) {
//            f_menu.collapse();
//            return false;
//        } else
//            return super.onKeyDown(keyCode, event);
//    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
//            try {
//                writeJson(openFileOutput("data.json", Context.MODE_PRIVATE), db);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            System.out.println(MainActivity.this);
			final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.settings_dialog, null);
			final RadioGroup seGroup = (RadioGroup) view.findViewById(R.id.seGroup);
			RadioButton seGoogle = (RadioButton) view.findViewById(R.id.seGoogle);
			RadioButton seBing = (RadioButton) view.findViewById(R.id.seBing);
			RadioButton seBaidu = (RadioButton) view.findViewById(R.id.seBaidu);
			RadioButton seSogou = (RadioButton) view.findViewById(R.id.seSogou);
			RadioButton seCustom = (RadioButton) view.findViewById(R.id.seCustom);
			final EditText vCSE = (EditText) view.findViewById(R.id.customSE);
			final CheckBox sh = (CheckBox) view.findViewById(R.id.cbHidden);
			final TextView tpStatus = (TextView) view.findViewById(R.id.tpStatus);
			view.findViewById(R.id.btnImport).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
//                    Toast.makeText(MainActivity.this, "1111111111111111111", Toast.LENGTH_SHORT).show();
//                    System.out.println(v.getParent().getParent().toString());
					fileOpen(view.getContext(), "text/html", new OnFileTouchedListener() {
						@Override
						public void onFileTouched(DialogInterface dialog, File file) {
//                            System.out.println(dialog.toString());
//                            System.out.println(file.getAbsolutePath());
							try {
//                                        filepath = URI2Path(Uri.parse(URLDecoder.decode(data.getData().toString(), "UTF-8")), this);
								FileInputStream is = new FileInputStream(file);
								byte[] b = new byte[is.available()];
								is.read(b);
								is.close();
								FileOutputStream os = openFileOutput("template.html", Context.MODE_PRIVATE);
								os.write(b);
								os.flush();
								os.close();
								Toast.makeText(MainActivity.this, "Successfully imported the template", Toast.LENGTH_SHORT).show();
								tpStatus.setText("Exist");
								tpStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(MainActivity.this, "Failed opening the file", Toast.LENGTH_SHORT).show();
							}

						}
					});
				}
			});
			byte[] tp = null;
			try {
				String seStr = db.getString("searchEngine");
				if (seStr.equals("Google")) seGoogle.toggle();
				else if (seStr.equals("Bing")) seBing.toggle();
				else if (seStr.equals("Baidu")) seBaidu.toggle();
				else if (seStr.equals("Sogou")) seSogou.toggle();
				else if (seStr.equals("Custom")) seCustom.toggle();
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
				FileInputStream tmls = openFileInput("template.html");
				tp = new byte[tmls.available()];
				tmls.read(tp);
				tmls.close();
				if (tp != null) {
					tpStatus.setText("Exist");
					tpStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
				} else {
					tpStatus.setText("Not Exist");
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
								RadioButton checked = (RadioButton) view.findViewById(seGroup.getCheckedRadioButtonId());
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
							}
						}
					})
					.create();
			settingDialog.setCanceledOnTouchOutside(false);
			settingDialog.show();
			final Button ok = settingDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			seGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					vCSE.setEnabled(checkedId == R.id.seCustom);
					ok.setEnabled(checkedId != R.id.seCustom || checkedId == R.id.seCustom && !vCSE.getText().toString().equals(""));
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
//            final Button ok = URLDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            ok.setEnabled(false);
//            vUrl.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    if (vUrl.getText().toString().length() > 0) ok.setEnabled(true);
//                    else ok.setEnabled(false);
//                }
//            });
//            vUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                @Override
//                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                    if (ok.isEnabled()) ok.callOnClick();
//                    return true;
//                }
//            });
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
//        Toast.makeText(this, "P", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResume() {
		super.onResume();
//        Toast.makeText(this, "R", Toast.LENGTH_SHORT).show();
		try {
			db = readJson(openFileInput("data.json"));
			for (int i = 0; i < db.getJSONArray("wiki").length(); i++) {
				System.out.println(new File(db.getJSONArray("wiki").getJSONObject(i).getString("path")).exists());
				if (!new File(db.getJSONArray("wiki").getJSONObject(i).getString("path")).exists())
					db.getJSONArray("wiki").remove(i);
			}
			System.out.println(db.toString(2));
			writeJson(openFileOutput("data.json", MODE_PRIVATE), db);
//            Toast.makeText(this, db.toString(2), Toast.LENGTH_SHORT).show();
			wikiListAdapter.reload(db);
			rv_wikilist.setAdapter(wikiListAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (db.getJSONArray("wiki").length() == 0)
				nowiki.setVisibility(View.VISIBLE);
			else
				nowiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static JSONObject readJson(FileInputStream is) {
		try {
			byte[] b = new byte[is.available()];
			is.read(b);
			is.close();
//            Toast.makeText(this, new String(b), Toast.LENGTH_SHORT).show();
//            String m = new String(b);
//            System.out.println(m);
			return new JSONObject(new String(b));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
//        if (firstRun) {
//            db = new JSONObject();
//            try {
//                if (Locale.getDefault().toString().equals("zh_CN")) {
//                    db.put("searchEngine", "Baidu");
//                } else {
//                    db.put("searchEngine", "Google");
//                }
//                db.put("wiki", new JSONArray());
////                db.getJSONArray("wiki").put(new JSONObject().put("name","123"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
	}

	//    public static void readJson() {
//        Boolean firstRun = false;
//        try {
//            FileInputStream is = openFileInput("data.json");
//            byte[] b = new byte[is.available()];
//            is.read(b);
//            is.close();
////            Toast.makeText(this, new String(b), Toast.LENGTH_SHORT).show();
//            db = new JSONObject(new String(b));
//        } catch (Exception e) {
//            e.printStackTrace();
//            firstRun = true;
//        }
//        if (firstRun) {
//            db = new JSONObject();
//            try {
//                if (Locale.getDefault().toString().equals("zh_CN")) {
//                    db.put("searchEngine", "Baidu");
//                } else {
//                    db.put("searchEngine", "Google");
//                }
//                db.put("wiki", new JSONArray());
////                db.getJSONArray("wiki").put(new JSONObject().put("name","123"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
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

	//    public static void writeJson() {
//        try {
//            FileOutputStream os = openFileOutput("data.json", Context.MODE_PRIVATE);
//            byte[] b = db.toString(2).getBytes();
//            os.write(b);
//            os.flush();
//            os.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
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
		view.findViewById(R.id.btnNewfldr).setVisibility(View.GONE);
		final TextView lblPath = (TextView) view.findViewById(R.id.lblPath);
		lblPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
		final RecyclerView dir = (RecyclerView) view.findViewById(R.id.diFileList);
		dir.setLayoutManager(new LinearLayoutManager(view.getContext()));
//        System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
		final AlertDialog fileDialog = new AlertDialog.Builder(parent)
				.setTitle("Open")
				.setView(view)
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        ;
//                    }
//                })
//                .setPositiveButton("Ok", listener)
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        ;
//                    }
//                })
				.create();
//        System.out.println(fileDialog.toString());
		fileDialog.setCanceledOnTouchOutside(false);
		fileDialog.show();
		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), mimeTypes, 1, Environment.getExternalStorageDirectory());
		dirAdapter.setOnItemClickListener(new FileDialogAdapter.ItemClickListener() {
			@Override
			public void onItemClick(int position) {
				File f = dirAdapter.getFile(position);
				if (f != null && f.exists()) {
					if (f.isDirectory()) {
						dirAdapter.setDir(f);
//                        dirAdapter.setHasStableIds(true);
						dir.setAdapter(dirAdapter);
						lblPath.setText(f.getAbsolutePath());
						btnBack.setEnabled(dirAdapter.getDevs().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
					} else if (f.isFile()) {
						listener.onFileTouched(fileDialog, f);
						fileDialog.dismiss();
					}
				} else {
					dirAdapter.setRoot();
//                    dirAdapter.setHasStableIds(true);
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
				}
			}
		});
//        dirAdapter.setHasStableIds(true);
		dir.setAdapter(dirAdapter);
		btnBack.setEnabled(dirAdapter.getDevs().length > 1);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File f = dirAdapter.getParentDir();
				if (f != null && f.exists() && f.isDirectory()) {
					dirAdapter.setDir(f);
					dir.setAdapter(dirAdapter);
					lblPath.setText(f.getAbsolutePath());
					btnBack.setEnabled(dirAdapter.getDevs().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
				} else {
					dirAdapter.setRoot();
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
					btnBack.setEnabled(false);
				}
			}
		});

	}

	public static void fileSave(Context parent, final String mimeTypes, final OnFileTouchedListener listener) {
		View view = LayoutInflater.from(parent).inflate(R.layout.file_dialog, null);
//		view.findViewById(R.id.saveFnFrame).setVisibility(View.GONE);
		view.findViewById(R.id.saveFnFrame).setVisibility(View.VISIBLE);
		final EditText fName = (EditText) view.findViewById(R.id.save_f_name);
		final Button btnBack = view.findViewById(R.id.btnBack);
		ImageButton btnNewfldr = view.findViewById(R.id.btnNewfldr);
		final TextView lblPath = (TextView) view.findViewById(R.id.lblPath);
		lblPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
		final RecyclerView dir = (RecyclerView) view.findViewById(R.id.diFileList);
		dir.setLayoutManager(new LinearLayoutManager(view.getContext()));
//        System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), mimeTypes, 1, Environment.getExternalStorageDirectory());

		final AlertDialog fileDialog = new AlertDialog.Builder(parent)
				.setTitle("Save as")
				.setView(view)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Ok", null)
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String fn = fName.getText().toString();
//                        if (!(fn.endsWith(".htm") || fn.endsWith(".html"))) fn = fn + ".html";
//                        String fPath = dirAdapter.getCurrentDir().getAbsolutePath() + "/" + fn;
//                        File of = new File(fPath);
//                        if (of.exists()) {
//                            ;
//                        } else {
//                            ;
//                        }
//                    }
//                })
				.create();
//        System.out.println(fileDialog.toString());
		fileDialog.setCanceledOnTouchOutside(false);
		fileDialog.show();
		btnBack.setEnabled(dirAdapter.getDevs().length > 1);
		btnNewfldr.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View view1 = LayoutInflater.from(fileDialog.getContext()).inflate(R.layout.fn_slot, null);
				final EditText editText = view1.findViewById(R.id.eFn);
				final AlertDialog newFldrDialog = new AlertDialog.Builder(fileDialog.getContext())
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
									f.mkdir();
									if (f.exists()) {
										dirAdapter.setDir(f);
										dir.setAdapter(dirAdapter);
										lblPath.setText(f.getAbsolutePath());
										btnBack.setEnabled(true);
									}
								}
							}
						})
						.show();
				final Button okx = newFldrDialog.getButton(AlertDialog.BUTTON_POSITIVE);
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
						if (editText.getText().toString().startsWith("+") || editText.getText().toString().startsWith("-")) {
							okx.setEnabled(false);
							Toast.makeText(newFldrDialog.getContext(), "These characters  are not allowed at the beginning of a filename:\n+ -", Toast.LENGTH_SHORT).show();
						} else if (illegalFilename(editText.getText().toString())) {
							okx.setEnabled(false);
							Toast.makeText(newFldrDialog.getContext(), "These characters  are not allowed in a filename:\n\" * / : < > ? \\ |", Toast.LENGTH_SHORT).show();
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
					AlertDialog override = new AlertDialog.Builder(fileDialog.getContext())
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
//                        dirAdapter.setHasStableIds(true);
						dir.setAdapter(dirAdapter);
						lblPath.setText(f.getAbsolutePath());
						btnBack.setEnabled(dirAdapter.getDevs().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
					} else if (f.isFile()) {
						String fn = f.getName();
						if (fn.equals(fName.getText().toString())) ok.callOnClick();
						else fName.setText(fn);
//                        fName.setText(fn.substring(0, fn.lastIndexOf(".")));
//                        listener.onFileTouched(fileDialog, f);
//                        fileDialog.dismiss();
					}
				} else {
					dirAdapter.setRoot();
//                    dirAdapter.setHasStableIds(true);
					dir.setAdapter(dirAdapter);
					lblPath.setText("");
				}
			}
		});
//        dirAdapter.setHasStableIds(true);
		dir.setAdapter(dirAdapter);

		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File f = dirAdapter.getParentDir();
				if (f != null && f.exists() && f.isDirectory()) {
					dirAdapter.setDir(f);
					dir.setAdapter(dirAdapter);
					lblPath.setText(f.getAbsolutePath());
					btnBack.setEnabled(dirAdapter.getDevs().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
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
				if (fName.getText().toString().length() > 0) ok.setEnabled(true);
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

	public static File[] getStorages(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            File[] d = new File[1];
//            d[1] = Environment.getExternalStorageDirectory();
//            return d;
//        } else {
//		if (mode == 0) {
		StorageManager storageManager = (StorageManager) context.getSystemService(STORAGE_SERVICE);
		try {
			Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
			method.setAccessible(true);
			Object result = method.invoke(storageManager);
			if (result != null && result instanceof String[]) {
				String[] pathes = (String[]) result;
				StatFs statFs;
				File[] files = new File[pathes.length];
				int i = 0;
				for (String path : pathes) {
					File file = new File(path);
					if (!TextUtils.isEmpty(path) && file.exists()) {
						statFs = new StatFs(path);
						if (statFs.getBlockCountLong() * statFs.getBlockSizeLong() != 0 && file.canWrite()) {
//                            pathsList.add(path);
//                            System.out.println(path);
							System.out.println(file.canWrite());
							files[i] = file;
							i++;
						}
					}
				}
//				if (i > 0) {
				File[] files1 = new File[i];
				for (int j = 0; j < i; j++)
					files1[j] = files[j];
				return files1;
//				} else {
//					return null;
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			File externalFolder = Environment.getExternalStorageDirectory();
			if (externalFolder != null) {
				File[] d = new File[1];
				d[0] = externalFolder;
				return d;

//                pathsList.add(externalFolder.getAbsolutePath());
			}
		}
//            List storageVolumeList = mStorageManager.getStorageVolumes();
//            if (storageVolumeList!=null) {
//                for (Object volume:storageVolumeList) {
//                    String path=volume.toString();
//                    System.out.println(path);
//                    volume.
////                    if (path.contains("USB")) {
////                        dirUsb=path;
////                        return dirUsb;
////                    }else if (path.contains("SD")) {
////                        dirScard=path;
////                        return dirScard
////                    }
//                }
//            }

//        };
//		} else if (mode == 1) {
//			String v = null;
//			try {
//				v = db.getString("extpath");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return new File[]{Environment.getExternalStorageDirectory(), new File(v)};
//		}
		return null;
	}

	public static String genId() {
		return Long.toHexString(Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())));
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
				|| v.indexOf(127) >= 0
				;
	}
//    public String URI2Path(Uri uri, Context context) {
////        System.out.println(uri.toString());
//        String scheme = uri.getScheme();
//        String data = null;
//        if (scheme == null || ContentResolver.SCHEME_FILE.equals(scheme)) data = uri.getPath();
//        else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
//            try {
//                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
//                if (cursor != null) {
//                    if (cursor.moveToFirst()) {
//                        int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
//                        if (index > -1) {
//                            data = cursor.getString(index);
//                        }
//                    }
//                    cursor.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (data != null) System.out.println(data);
//        return data;
//
//    }
//    private static class NoLeakHandler extends Handler {
//        //持有弱引用MainActivity,GC回收时会被回收掉.
//        private WeakReference<MainActivity> mActivity;
//        public NoLeakHandler(MainActivity activity) {
//            mActivity = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    }

}
