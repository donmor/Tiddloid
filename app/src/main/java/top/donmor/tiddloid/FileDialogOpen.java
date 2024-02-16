/*
 * top.donmor.tiddloid.FileDialogOpen <= [P|Tiddloid]
 * Last modified: 22:10:05 2024/02/15
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The class FileDialogOpen is from FileDialog project, provides a set of methods to open dialogs for file-operating. Only fileOpen is used here.
 */
abstract class FileDialogOpen {

//	/**
//	 * The constant MIME_ALL(&#42;/&#42;).
//	 */
//	public static final String MIME_ALL = "*/*";

	//	private static final String METHOD_GET_VOLUME_PATHS = "getVolumePaths";
	private static final String METHOD_GET_VOLUME_LIST = "getVolumeList";
	private static final String METHOD_GET_PATH_FILE = "getPathFile";
//	private static final String METHOD_GET_VOLUME_PATHS = "getVolumePaths", STR_EMPTY = "";

	/**
	 * The call back that will be run when the dialog is closed.
	 */
	public interface OnFileTouchedListener {

		/**
		 * Invoked if one or more file is to be opened/saved.
		 *
		 * @param file the file to open/save.
		 */
		void onFileTouched(File file);
//		void onFileTouched(File[] files);

		/**
		 * Invoked if the dialog is cancelled.
		 */
		void onCanceled();

	}

//	/**
//	 * Open a dialog to select a single file to be opened.
//	 *
//	 * @param parent   The parent Context.
//	 * @param filter   This parameter provides a filter.
//	 * @param listener The call back that will be run when the dialog is closed.
//	 */
//	static void fileOpen(final Context parent, FileDialogFilter filter, final OnFileTouchedListener listener) {
////	private static void fileDialog(final Context parent, FileDialogFilter filter, final OnFileTouchedListener listener) {
//		fileDialog(parent, filter, listener);
////		fileDialog(parent, Environment.getExternalStorageDirectory(), filter, listener);
//	}
//	static void fileOpen(final Context parent, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), filters, listener);
//	}

//	/**
//	 * Open a dialog to select multiple files to be opened.
//	 *
//	 * @param parent   The parent Context.
//	 * @param filters  This parameter provides a set of filters.
//	 * @param listener The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpenMultiple(final Context parent, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 1, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a directory.
//	 *
//	 * @param parent   The parent Context.
//	 * @param listener The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSelectDirectory(final Context parent, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 2, null, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent   The parent Context.
//	 * @param filters  This parameter provides a set of filters.
//	 * @param listener The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 3, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent   The parent Context.
//	 * @param filename The default filename.
//	 * @param filters  This parameter provides a set of filters.
//	 * @param listener The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, String filename, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), filename, 3, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a single file to be opened.
//	 *
//	 * @param parent     The parent Context.
//	 * @param filters    This parameter provides a set of filters.
//	 * @param showHidden This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener   The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpen(final Context parent, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 0, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select multiple files to be opened.
//	 *
//	 * @param parent     The parent Context.
//	 * @param filters    This parameter provides a set of filters.
//	 * @param showHidden This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener   The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpenMultiple(final Context parent, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 1, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a directory.
//	 *
//	 * @param parent     The parent Context.
//	 * @param showHidden This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener   The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSelectDirectory(final Context parent, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 2, null, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent     The parent Context.
//	 * @param filters    This parameter provides a set of filters.
//	 * @param showHidden This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener   The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), null, 3, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent     The parent Context.
//	 * @param filename   The default filename.
//	 * @param filters    This parameter provides a set of filters.
//	 * @param showHidden This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener   The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, String filename, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, Environment.getExternalStorageDirectory(), filename, 3, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a single file to be opened.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpen(final Context parent, File startDirectory, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 0, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select multiple files to be opened.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpenMultiple(final Context parent, File startDirectory, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 1, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a directory.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSelectDirectory(final Context parent, File startDirectory, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 2, null, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, File startDirectory, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 3, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filename       The default filename.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, File startDirectory, String filename, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, filename, 3, filters, 0, null, 0, false, false, listener);
//	}

//	/**
//	 * Open a dialog to select a single file to be opened.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param showHidden     This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpen(final Context parent, File startDirectory, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 0, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select multiple files to be opened.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param showHidden     This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileOpenMultiple(final Context parent, File startDirectory, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 1, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a directory.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param showHidden     This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSelectDirectory(final Context parent, File startDirectory, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 2, null, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param showHidden     This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, File startDirectory, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, null, 3, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select a file path to be saved.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. if invalid, the dialog will start with the default SD card directory.
//	 * @param filename       The default filename.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param showHidden     This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileSave(final Context parent, File startDirectory, String filename, FileDialogFilter[] filters, boolean showHidden, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, filename, 3, filters, 0, null, 0, showHidden, false, listener);
//	}

//	/**
//	 * Open a dialog to select folder or files. All parameters are required using this method.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. If invalid, the dialog will start with the default SD card directory.
//	 * @param filename       The default filename for fileSave mode. It will be ignored in other modes.
//	 * @param mode           This parameter decides the type of the dialog. 0 for fileOpen, 1 for fileOpenMultiple, 2 for fileSelectDirectory, and 3 for fileSave.
//	 * @param filterIndex    This parameter provides the default position of filter spinner. Usually its value is 0, and it will be reset if an invalid value was passed.
//	 * @param filters        This parameter provides a set of filters.
//	 * @param showHidden     This parameter decides whether hidden(starts with '.') files could be shown or be created.
//	 * @param ignoreReadOnly This parameter decides whether read-only status of a device will be ignored, for example, most of systems prevents third part apps from writing to external SD cards.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileDialog(final Context parent, File startDirectory, String filename, final int mode, int filterIndex, FileDialogFilter[] filters, final boolean showHidden, boolean ignoreReadOnly, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, filename, mode, filters, filterIndex, null, 0, showHidden, ignoreReadOnly, listener);
//	}

//	/**
//	 * Open a dialog to select folder or files. All parameters are required using this method.
//	 *
//	 * @param parent         The parent Context.
//	 * @param startDirectory The directory the dialog will start with. If invalid, the dialog will start with the default SD card directory.
//	 * @param listener       The call back that will be run when the dialog is closed.
//	 */
//	public static void fileDialog(final Context parent, File startDirectory, String filename, final int mode, int filterIndex, String[] mimes, int det, final boolean showHidden, boolean ignoreReadOnly, final OnFileTouchedListener listener) {
//		fileDialog(parent, startDirectory, filename, mode, null, filterIndex, mimes, det, showHidden, ignoreReadOnly, listener);
//	}

	/**
	 * Open a dialog to select a single file to be opened.
	 *
	 * @param parent   The parent Context.
	 * @param filter   This parameter provides a filter.
	 * @param listener The call back that will be run when the dialog is closed.
	 */
	static void fileOpen(final Context parent, FileDialogFilter filter, final OnFileTouchedListener listener) {
//	private static void fileDialog(final Context parent, File startDirectory, FileDialogFilter[] filters, final OnFileTouchedListener listener) {
		final View view = LayoutInflater.from(parent).inflate(R.layout.file_dialog, null);
//		String[] mimeTypes = null;
//		final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//		final EditText fName = view.findViewById(R.id.save_f_name);
//		fName.setVisibility(View.GONE);
		final TextView lblPath = view.findViewById(R.id.lblPath);
		File startDirectory = getStorage(parent, false)[0];
//		try {
//			if (startDirectory == null || !startDirectory.isDirectory() || !startDirectory.canWrite()) throw new Exception();
//		} catch (Exception e) {
//			startDirectory = Environment.getExternalStorageDirectory();
//		}
//		try {
//			if (startDirectory == null || !startDirectory.isDirectory() || !startDirectory.canWrite()) throw new Exception();
//		} catch (Exception e) {
//			startDirectory = Environment.getExternalStorageDirectory();
//		}
		lblPath.setText(startDirectory.getAbsolutePath());
//		Spinner spnExt = view.findViewById(R.id.spnExt);
//		ArrayAdapter<String> spinnerAdapter;
//		if (filters != null) {
//			String[] fil = new String[filters.length];
//			for (int i = 0; i < filters.length; i++)
//				fil[i] = filters[i].name;
//			spinnerAdapter = new ArrayAdapter<>(view.getContext(), R.layout.ext_slot, fil);
//			spinnerAdapter.setDropDownViewResource(R.layout.ext_slot);
//			spnExt.setAdapter(spinnerAdapter);
//			if (filters.length < 2) spnExt.setEnabled(false);
//		}
//		if (filters != null) {
//			String[] fil = new String[filters.length];
//			for (int i = 0; i < filters.length; i++)
//				fil[i] = filters[i].name;
////			spinnerAdapter = new ArrayAdapter<>(view.getContext(), R.layout.ext_slot, fil);
////			spinnerAdapter.setDropDownViewResource(R.layout.ext_slot);
////			spnExt.setAdapter(spinnerAdapter);
////			if (filters.length < 2) spnExt.setEnabled(false);
//		}
		final RecyclerView dir = view.findViewById(R.id.diFileList);
		dir.setLayoutManager(new LinearLayoutManager(view.getContext()));
		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), filter, startDirectory, false, false, false);
//		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), filter, mimeTypes, mimeTypeMap, startDirectory, false, false, false, false);
//		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), filters, spnExt.getSelectedItemPosition(), mimeTypes, mimeTypeMap, startDirectory, false, false, false, false);
		dir.setAdapter(dirAdapter);
		final Button btnBack = view.findViewById(R.id.btnBack);
		btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !startDirectory.equals(Environment.getExternalStorageDirectory()));
//		ImageButton btnNewFolder = view.findViewById(R.id.btnNewFolder);
//		btnNewFolder.setVisibility(View.GONE);
//		spnExt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				dirAdapter.setFilterIndex(position);
//				dirAdapter.reload();
//				dir.setAdapter(dirAdapter);
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//
//			}
//		});
		AlertDialog.Builder builder = new AlertDialog.Builder(parent).setView(view);
		builder.setTitle(R.string.local_legacy);
		final AlertDialog fileDialog = builder.create();
		fileDialog.setCanceledOnTouchOutside(false);
		fileDialog.setOnCancelListener(dialog -> listener.onCanceled());
		fileDialog.show();
		final Button ok = fileDialog.getButton(AlertDialog.BUTTON_POSITIVE);
		ok.setOnClickListener(v -> {
		});
		btnBack.setOnClickListener(v -> {
			File f = dirAdapter.getParentDir();
			if (f != null && f.exists() && f.isDirectory()) {
				dirAdapter.setDir(f);
				dir.setAdapter(dirAdapter);
				lblPath.setText(f.getAbsolutePath());
				btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
				//					if (fName.getText().toString().length() > 0 && !illegalFilename(fName.getText().toString()))
//						ok.setEnabled(true);
			} else {
				dirAdapter.setRoot();
				dir.setAdapter(dirAdapter);
				lblPath.setText(MainActivity.STR_EMPTY);
				btnBack.setEnabled(false);
				ok.setEnabled(false);
			}
		});
		dirAdapter.setOnItemClickListener(position -> {
			File f = dirAdapter.getFile(position);
			if (f.isDirectory()) {
				dirAdapter.setDir(f);
				dir.setAdapter(dirAdapter);
				lblPath.setText(f.getAbsolutePath());
				btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !dirAdapter.getRootDir().getAbsolutePath().equals(f.getAbsolutePath()));
			} else if (f.isFile()) {
				listener.onFileTouched(f);
//				listener.onFileTouched(new File[]{f});
				fileDialog.dismiss();
			}
		});
//		fName.addTextChangedListener(new TextWatcher() {
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
//				if (fName.getText().toString().indexOf('.') == 0) {
//					ok.setEnabled(false);
////						Toast.makeText(view.getContext(), R.string.cannot_create_hidden_files, Toast.LENGTH_SHORT).show();
//				} else if (fName.getText().toString().indexOf('+') == 0 || fName.getText().toString().indexOf('-') == 0) {
//					ok.setEnabled(false);
////					Toast.makeText(view.getContext(), R.string.filename_cannot_begin_with, Toast.LENGTH_SHORT).show();
////				} else if (illegalFilename(fName.getText().toString())) {
////					ok.setEnabled(false);
////					Toast.makeText(view.getContext(), R.string.filename_cannot_contains, Toast.LENGTH_SHORT).show();
//				} else ok.setEnabled(fName.getText().toString().length() > 0);
//			}
//		});
//		fName.setOnEditorActionListener((v, actionId, event) -> {
//			if (ok.isEnabled())
//				ok.callOnClick();
//			return true;
//		});
	}


	/**
	 * Return an array contains file paths of all storage devices mounted.
	 *
	 * @param context        The parent Context.
	 * @param ignoreReadOnly This parameter decides whether read-only status of a device will be ignored, for example, most of systems prevents third part apps from writing to external SD cards.
	 * @return the file [ ]
	 */
	@NonNull
	static File[] getStorage(Context context, boolean ignoreReadOnly) {
		StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		if (storageManager != null) {
			StorageVolume[] sl = null;
			if (MainActivity.APIOver24)
				sl = storageManager.getStorageVolumes().toArray(new StorageVolume[0]);
			else try {
				Method getVolumeList = storageManager.getClass().getMethod(METHOD_GET_VOLUME_LIST);
				getVolumeList.setAccessible(true);
				sl = (StorageVolume[]) getVolumeList.invoke(storageManager);
			} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
				e.printStackTrace();
			}
			if (sl != null) {
				List<File> dir = new ArrayList<>();
				for (StorageVolume storageVolume : sl) {
					System.out.println(storageVolume.toString());
					if (MainActivity.APIOver30) {
						dir.add(storageVolume.getDirectory());
					} else try {
						//noinspection JavaReflectionMemberAccess
						Method getPathFile = storageVolume.getClass().getMethod(METHOD_GET_PATH_FILE);
						getPathFile.setAccessible(true);
						dir.add((File) getPathFile.invoke(storageVolume));
					} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
				if (!dir.isEmpty())
					return dir.toArray(new File[0]);
			}
		}


		return new File[]{Environment.getExternalStorageDirectory()};
//		File externalFolder = Environment.getExternalStorageDirectory();
////		if (externalFolder != null) {
//			return new File[]{externalFolder};
////		}
//		return null;
	}

//	/**
//	 * This FileDialogFilter accepts all kinds of files.
//	 */
//	public static final FileDialogFilter ALL = new FileDialogFilter("*", new String[]{"*"});

//	private static boolean illegalFilename(CharSequence e) {
//		String v = e.toString();
//		for (int i = 0; i < 32; i++) if (v.indexOf(i) >= 0) return true;
//		return v.indexOf('"') >= 0
//				|| v.indexOf('*') >= 0
//				|| v.indexOf('/') >= 0
//				|| v.indexOf(':') >= 0
//				|| v.indexOf('<') >= 0
//				|| v.indexOf('>') >= 0
//				|| v.indexOf('?') >= 0
//				|| v.indexOf('\\') >= 0
//				|| v.indexOf('|') >= 0
//				|| v.indexOf(127) >= 0;
//	}
}