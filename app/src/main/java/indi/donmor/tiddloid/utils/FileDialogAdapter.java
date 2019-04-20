//package indi.donmor.tiddloid.utils;
//
//import android.content.Context;
//import android.os.Environment;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
////import android.text.Html;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import java.io.File;
//import java.io.FileFilter;
//
//import indi.donmor.tiddloid.*;
//
//public class FileDialogAdapter extends RecyclerView.Adapter<FileDialogAdapter.FileViewHolder> {
//
//	private File currentDir, rootDir;
//	private File[] files;
//	private File[] dirs;
//	private File[] devices;
//	private boolean enRoot;
//	private int mimeIndex;
//	private Context context;
//	private String[] mimeTypes;
////	private MimeTypeUtil.MimeX mimeX;
//	private LayoutInflater inflater;
//
////	public FileDialogAdapter(Context context, MimeTypeUtil.MimeX mimeX, File dir) {
//	public FileDialogAdapter(Context context, String[] mimeTypes, File dir) {
//		this.context = context;
//		this.mimeTypes = mimeTypes;
////		this.mimeX = mimeX;
//		inflater = LayoutInflater.from(context);
//		try {
//			currentDir = dir;
//			if (!currentDir.isDirectory()) throw new Exception();
//		} catch (Exception e) {
//			currentDir = Environment.getExternalStorageDirectory();
//		}
//		rootDir = Environment.getExternalStorageDirectory();
//		enRoot = false;
//		dirs = sortFile(getDirs());
//		files = sortFile(getFiles());
//		devices = MainActivity.getStorage(context.getApplicationContext());
//	}
//
//	class FileViewHolder extends RecyclerView.ViewHolder {
//		private Button cbD, cbF,
////				cbP,
//				cbR;
//
//		FileViewHolder(View itemView) {
//			super(itemView);
//			cbD = itemView.findViewById(R.id.c_buttonD);
//			cbF = itemView.findViewById(R.id.c_buttonF);
////			cbP = itemView.findViewById(R.id.c_buttonP);
//			cbR = itemView.findViewById(R.id.c_buttonR);
//		}
//	}
//
//	@Override
//	@NonNull
//	public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//		return new FileViewHolder(inflater.inflate(R.layout.file_slot, parent, false));
//	}
//
//	@Override
//	public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
//		final int pos = position;
////		final int pos = position - 1;
//		if (enRoot) {
////			if (pos == -1) {
////				holder.cbP.setVisibility(View.VISIBLE);
////				holder.cbP.setEnabled(false);
////			} else {
//				holder.cbR.setText(devices[pos].getAbsolutePath());
//				holder.cbR.setVisibility(View.VISIBLE);
////			}
//		} else {
////			if (pos == -1) {
////				holder.cbP.setVisibility(View.VISIBLE);
////				holder.cbP.setText(Html.fromHtml("<b>..</b><br><font color=\"grey\">" + currentDir.getAbsolutePath() + "</font>"));
////				System.out.println(devices.length);
////				holder.cbP.setEnabled(devices.length > 1 || !rootDir.getAbsolutePath().equals(currentDir.getAbsolutePath()));
////			} else
//				if (pos < dirs.length) {
//				{
//					holder.cbD.setText(dirs[pos].getName());
//					holder.cbD.setVisibility(View.VISIBLE);
//				}
//			} else if (pos < dirs.length + files.length) {
//				holder.cbF.setText(files[pos - dirs.length].getName());
//				holder.cbF.setVisibility(View.VISIBLE);
//			}
//		}
//		View.OnClickListener ocl = new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mItemClickListener.onItemClick(pos);
//			}
//		};
//		holder.cbD.setOnClickListener(ocl);
//		holder.cbF.setOnClickListener(ocl);
//		holder.cbR.setOnClickListener(ocl);
////		holder.cbD.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos);
////			}
////		});
////		holder.cbF.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos);
////			}
////		});
//////		holder.cbP.setOnClickListener(new View.OnClickListener() {
//////			@Override
//////			public void onClick(View v) {
//////				mItemClickListener.onItemClick(pos + 1);
//////			}
//////		});
////		holder.cbR.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos);
////			}
////		});
////		holder.cbD.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos + 1);
////			}
////		});
////		holder.cbF.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos + 1);
////			}
////		});
////		holder.cbP.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos + 1);
////			}
////		});
////		holder.cbR.setOnClickListener(new View.OnClickListener() {
////			@Override
////			public void onClick(View v) {
////				mItemClickListener.onItemClick(pos + 1);
////			}
////		});
//	}
//
//	@Override
//	public int getItemCount() {
//		if (enRoot) return devices.length;
//		else return dirs.length + files.length;
////		if (enRoot) return devices.length + 1;
////		else return dirs.length + files.length + 1;
//	}
//
//	@Override
//	public int getItemViewType(int position) {
//		return position;
//	}
//
//	private ItemClickListener mItemClickListener;
//
//	public interface ItemClickListener {
//		void onItemClick(int position);
//	}
//
//	public void setOnItemClickListener(ItemClickListener itemClickListener) {
//		this.mItemClickListener = itemClickListener;
//
//	}
//
//	private File[] getDirs() {
//		return currentDir.listFiles(new FileFilter() {
//			@Override
//			public boolean accept(File pathname) {
//				boolean showHidden = false;
//				try {
//					showHidden = !MainActivity.db.getBoolean("showHidden");
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return !(pathname.isHidden() && showHidden) && pathname.isDirectory();
////				return !((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && showHidden) && pathname.isDirectory();
//			}
//		});
//	}
//
//	private File[] getFiles() {
//		return currentDir.listFiles(new FileFilter() {
//			@Override
//			public boolean accept(File pathname) {
//				boolean showHidden = false;
//				try {
//					showHidden = !MainActivity.db.getBoolean("showHidden");
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
////				return !((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && showHidden) && !pathname.isDirectory() && MimeTypeUtil.meetsMimeTypes(pathname.getName(), mimeTypes);
////				return !(pathname.isHidden() && showHidden) && pathname.isFile() && mimeX.meets(pathname.getName(),mimeIndex);
//				return !(pathname.isHidden() && showHidden) && pathname.isFile() && MimeTypeUtil.meetsMimeTypes(pathname.getName(), mimeTypes[mimeIndex]);
//			}
//		});
//	}
//
//	private File[] sortFile(File[] src) {
//		if (src.length == 0) return src;
//		for (int i = 0; i < src.length; i++) {
//			for (int j = 0; j < src.length - i - 1; j++) {
//				if (chrSort(src[j + 1].getName(), src[j].getName())) {
//					File temp = src[j + 1];
//					src[j + 1] = src[j];
//					src[j] = temp;
//				}
//			}
//		}
//		return src;
//	}
//
//	private Boolean chrSort(String s1, String s2) {
//		char[] sa1 = s1.toCharArray();
//		char[] sa2 = s2.toCharArray();
//		for (int i = 0; i < Math.min(sa1.length, sa2.length); i++) {
//			if (sa1[i] < sa2[i]) return true;
//			else if (sa1[i] > sa2[i]) return false;
//		}
//		return false;
//	}
//
//	public void setMimeIndex(int index){
//		mimeIndex = index;
//	}
//	public int getMimeIndex(){
//		return mimeIndex;
//	}
//
//	public File getFile(int position) {
////		final int pos = position;
////		final int pos = position - 1;
//		if (enRoot)
//			return devices[position];
//		else {
//			if (position < dirs.length) {
////				if (pos == -1) {
////					for (File dev : devices) {
////						if (dev.equals(currentDir)) {
////							return null;
////						}
////					}
////					return currentDir.getParentFile();
////				} else
//					return dirs[position];
//			} else
////				if (position < dirs.length + files.length)
//				{
//				return files[position - dirs.length];
//			}
//		}
////		return null;
//	}
//
//	public File[] getDevices() {
//		return devices;
//	}
//
//	public File getCurrentDir() {
//		return currentDir;
//	}
//
//	public File getRootDir() {
//		return rootDir;
//	}
//
//	public File getParentDir() {
//		for (File dev : devices) {
//			if (dev.equals(currentDir)) {
//				return null;
//			}
//		}
//		return currentDir.getParentFile();
//	}
//
//	public void setDir(File dir) {
//		try {
//			currentDir = dir;
//			if (!currentDir.isDirectory()) throw new Exception();
//		} catch (Exception e) {
//			currentDir = rootDir;
//		}
//		dirs = sortFile(getDirs());
//		files = sortFile(getFiles());
//		enRoot = false;
//	}
//
//	public void setRoot() {
//		devices = MainActivity.getStorage(context.getApplicationContext());
//		enRoot = true;
//	}
//	public void reload(){
//		if (!enRoot) {
//			dirs = sortFile(getDirs());
//			files = sortFile(getFiles());
//		}
//	}
//}
