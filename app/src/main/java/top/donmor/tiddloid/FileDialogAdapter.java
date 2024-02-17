/*
 * top.donmor.tiddloid.FileDialogAdapter <= [P|Tiddloid]
 * Last modified: 22:11:19 2024/02/15
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

class FileDialogAdapter extends RecyclerView.Adapter<FileDialogAdapter.FileViewHolder> {

	private File currentDir;
	private final File rootDir;
	private File[] files, dirs, devices;
	private boolean enRoot;
	private boolean showHidden;
	private final Context context;
	private final FileDialogFilter filter;
	private final LayoutInflater inflater;

	FileDialogAdapter(Context context, FileDialogFilter filter, File dir) {
		this.context = context;
		this.showHidden = false;
		this.filter = filter;
		inflater = LayoutInflater.from(context);
		try {
			currentDir = dir;
			if (!currentDir.isDirectory()) throw new Exception();
		} catch (Exception e) {
			currentDir = Environment.getExternalStorageDirectory();
		}
		rootDir = Environment.getExternalStorageDirectory();
		enRoot = false;
		dirs = sortFile(getDirs());
		files = sortFile(getFiles());
		devices = FileDialogOpen.getStorage(context.getApplicationContext());
	}

	static class FileViewHolder extends RecyclerView.ViewHolder {
		private final Button cbD, cbF, cbR;

		FileViewHolder(View itemView) {
			super(itemView);
			cbD = itemView.findViewById(R.id.c_buttonD);
			cbF = itemView.findViewById(R.id.c_buttonF);
			cbR = itemView.findViewById(R.id.c_buttonR);
		}
	}

	@Override
	@NonNull
	public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FileViewHolder(inflater.inflate(R.layout.file_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
		final int pos = position;
		if (enRoot) {
			holder.cbR.setText(devices[pos].getAbsolutePath());
			holder.cbR.setVisibility(View.VISIBLE);
		} else {
			if (pos < dirs.length) {
				{
					holder.cbD.setText(dirs[pos].getName());
					holder.cbD.setVisibility(View.VISIBLE);
				}
			} else if (pos < dirs.length + files.length) {
				holder.cbF.setText(files[pos - dirs.length].getName());
				holder.cbF.setVisibility(View.VISIBLE);
			}
		}
		View.OnClickListener ocl = v -> mItemClickListener.onItemClick(pos);
		holder.cbD.setOnClickListener(ocl);
		holder.cbF.setOnClickListener(ocl);
		holder.cbR.setOnClickListener(ocl);
	}

	@Override
	public int getItemCount() {
		if (enRoot) return devices.length;
		else {
			int d = 0, f = 0;
			if (dirs != null) d = dirs.length;
			if (files != null) f = files.length;
			return d + f;
		}
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	private ItemClickListener mItemClickListener;

	interface ItemClickListener {
		void onItemClick(int position);
	}

	void setOnItemClickListener(ItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;

	}

	/**
	 * @noinspection SameParameterValue
	 */
	void setShowHidden(boolean val) {
		this.showHidden = val;
		setDir(currentDir);
	}

	boolean getShowHidden() {
		return this.showHidden;
	}

	private File[] getDirs() {
		return currentDir.listFiles(pathname -> (!pathname.isHidden() || showHidden) && pathname.isDirectory());
	}

	private File[] getFiles() {
		return currentDir.listFiles(pathname -> (!pathname.isHidden() || showHidden) && pathname.isFile() && filter.meetExtensions(pathname.getName()));
	}

	private File[] sortFile(File[] src) {
		if (src == null || src.length == 0) return src;
		for (int i = 0; i < src.length; i++) {
			for (int j = 0; j < src.length - i - 1; j++) {
				if (chrSort(src[j + 1].getName(), src[j].getName())) {
					File temp = src[j + 1];
					src[j + 1] = src[j];
					src[j] = temp;
				}
			}
		}
		return src;
	}

	private boolean chrSort(String s1, String s2) {
		char[] sa1 = s1.toLowerCase().toCharArray();
		char[] sa2 = s2.toLowerCase().toCharArray();
		for (int i = 0; i < Math.min(sa1.length, sa2.length); i++) {
			if (sa1[i] < sa2[i]) return true;
			else if (sa1[i] > sa2[i]) return false;
		}
		return false;
	}

	File getFile(int position) {
		if (enRoot) return devices[position];
		else {
			if (position < dirs.length) {
				return dirs[position];
			} else {
				return files[position - dirs.length];
			}
		}
	}

	File[] getDevices() {
		return devices;
	}

	File getRootDir() {
		return rootDir;
	}

	File getParentDir() {
		for (File dev : devices) {
			if (dev.equals(currentDir)) {
				return null;
			}
		}
		return currentDir.getParentFile();
	}

	void setDir(File dir) {
		try {
			currentDir = dir;
			if (!currentDir.isDirectory()) throw new Exception();
		} catch (Exception e) {
			currentDir = rootDir;
		}
		dirs = sortFile(getDirs());
		files = sortFile(getFiles());
		enRoot = false;
	}

	void setRoot() {
		devices = FileDialogOpen.getStorage(context.getApplicationContext());
		enRoot = true;
	}

}
