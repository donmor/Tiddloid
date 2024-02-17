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
import android.widget.Toast;

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

	private static final String METHOD_GET_VOLUME_LIST = "getVolumeList",
			METHOD_GET_PATH_FILE = "getPathFile";

	/**
	 * The call back that will be run when the dialog is closed.
	 */
	interface OnFileTouchedListener {

		/**
		 * Invoked if one or more file is to be opened/saved.
		 *
		 * @param file the file to open/save.
		 */
		void onFileTouched(File file);

	}

	/**
	 * Open a dialog to select a single file to be opened.
	 *
	 * @param parent   The parent Context.
	 * @param filter   This parameter provides a filter.
	 * @param listener The call back that will be run when the dialog is closed.
	 */
	static void fileOpen(final Context parent, FileDialogFilter filter, final OnFileTouchedListener listener) {
		final View view = LayoutInflater.from(parent).inflate(R.layout.file_dialog, null);
		final TextView lblPath = view.findViewById(R.id.lblPath);
		File startDirectory = getStorage(parent)[0];
		lblPath.setText(startDirectory.getAbsolutePath());
		final RecyclerView dir = view.findViewById(R.id.diFileList);
		dir.setLayoutManager(new LinearLayoutManager(view.getContext()));
		final FileDialogAdapter dirAdapter = new FileDialogAdapter(view.getContext(), filter, startDirectory);
		dir.setAdapter(dirAdapter);
		final Button btnBack = view.findViewById(R.id.btnBack);
		btnBack.setEnabled(dirAdapter.getDevices().length > 1 || !startDirectory.equals(Environment.getExternalStorageDirectory()));
		AlertDialog.Builder builder = new AlertDialog.Builder(parent).setView(view);
		builder.setTitle(R.string.local_legacy);
		final AlertDialog fileDialog = builder.create();
		fileDialog.setCanceledOnTouchOutside(false);
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
			} else {
				dirAdapter.setRoot();
				dir.setAdapter(dirAdapter);
				lblPath.setText(MainActivity.STR_EMPTY);
				btnBack.setEnabled(false);
				ok.setEnabled(false);
			}
		});
		btnBack.setOnLongClickListener(v -> {
			if (!dirAdapter.getShowHidden()) {
				dirAdapter.setShowHidden(true);
				Toast.makeText(parent, R.string.show_hidden_files, Toast.LENGTH_SHORT).show();
				return true;
			} else return false;
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
				fileDialog.dismiss();
			}
		});
	}


	/**
	 * Return an array contains file paths of all storage devices mounted.
	 *
	 * @param context        The parent Context.
	 * @return the file [ ]
	 */
	@NonNull
	static File[] getStorage(Context context) {
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
	}

}