package indi.donmor.tiddloid.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

import indi.donmor.tiddloid.*;
//import indi.donmor.tiddloid.utils.MimeTypeUtil;

public class FileDialogAdapter extends RecyclerView.Adapter<FileDialogAdapter.FileViewHolder> {

	File currentDir, rootDir;
	File[] files;
	File[] dirs;
	File[] devs;
	boolean enRoot;
//	int extMode;
	Context context;
	String mimeTypes;
	private LayoutInflater inflater;

	public FileDialogAdapter(Context context, String mimeTypes, int extMode, File dir) {
		this.context = context;
		this.mimeTypes = mimeTypes;
//		this.extMode = extMode;
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
		devs = MainActivity.getStorages(context.getApplicationContext());
	}

	public class FileViewHolder extends RecyclerView.ViewHolder {
		//        private TextView fN;
//        private ImageView fT;
//        private LinearLayout fB;
		private Button cbD, cbF, cbP, cbR;

		public FileViewHolder(View itemView) {
			super(itemView);
//            fN = (TextView) itemView.findViewById(R.id.c_filename);
//            fT = (ImageView) itemView.findViewById(R.id.c_icon);
//            fB = (LinearLayout) itemView.findViewById(R.id.c_button);
			cbD = (Button) itemView.findViewById(R.id.c_buttonD);
			cbF = (Button) itemView.findViewById(R.id.c_buttonF);
			cbP = (Button) itemView.findViewById(R.id.c_buttonP);
			cbR = (Button) itemView.findViewById(R.id.c_buttonR);
		}
	}

	@Override
	public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		FileViewHolder holder = new FileViewHolder(inflater.inflate(R.layout.file_slot, parent, false));
		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull FileViewHolder holder, final int position) {
		final int pos = position - 1;
		if (enRoot) {
			if (pos == -1) {
				holder.cbP.setVisibility(View.VISIBLE);
				holder.cbP.setEnabled(false);
			} else {
				holder.cbR.setText(devs[pos].getAbsolutePath());
				holder.cbR.setVisibility(View.VISIBLE);
			}
		} else {
			if (pos == -1) {
				holder.cbP.setVisibility(View.VISIBLE);
//				holder.cbP.setText("..      \n" + currentDir.getAbsolutePath());
				holder.cbP.setText(Html.fromHtml("<b>..</b><br><font color=\"grey\">" + currentDir.getAbsolutePath() + "</font>"));
//				System.out.println(extMode);
//				System.out.println(currentDir.getAbsolutePath());
//				System.out.println(defaultDir.getAbsolutePath());
//				holder.cbP.setEnabled(extMode > 0 || extMode == 0 && !rootDir.getAbsolutePath().equals(currentDir.getAbsolutePath()));
				System.out.println(devs.length);
				holder.cbP.setEnabled(devs.length > 1 || !rootDir.getAbsolutePath().equals(currentDir.getAbsolutePath()));
			} else if (pos < dirs.length) {
				{
					holder.cbD.setText(dirs[pos].getName());
					holder.cbD.setVisibility(View.VISIBLE);
				}
			} else if (pos >= dirs.length && pos < dirs.length + files.length) {
				holder.cbF.setText(files[pos - dirs.length].getName());
				holder.cbF.setVisibility(View.VISIBLE);
			}
		}
//        if (pos < dirs.length) {
//            if (pos == -1) holder.fN.setText("..");
//            else holder.fN.setText(dirs[pos].getName());
//            holder.fT.setImageResource(R.drawable.ic_folder_black_24dp);
//        } else if (pos >= dirs.length && pos < dirs.length + files.length) {
//            holder.fN.setText(files[pos - dirs.length].getName());
//            holder.fT.setImageResource(R.drawable.ic_description_black_24dp);
//        }
//        if (mItemClickListener != null){
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // 这里利用回调来给RecyclerView设置点击事件
//                    mItemClickListener.onItemClick(pos + 1);
//                }
//            });
//        }


		// 给RecyclerView中item中的单独控件设置点击事件 可以直接在adapter中使用setOnClickListener即可
		holder.cbD.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//                System.out.println(v.toString());
				mItemClickListener.onItemClick(position);
			}
		});
		holder.cbF.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//                System.out.println(v.toString());
				mItemClickListener.onItemClick(position);
			}
		});
		holder.cbP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//                System.out.println(v.toString());
				mItemClickListener.onItemClick(position);
			}
		});
		holder.cbR.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//                System.out.println(v.toString());
				mItemClickListener.onItemClick(position);
			}
		});
	}

	@Override
	public int getItemCount() {
		if (enRoot) return devs.length + 1;
		else return dirs.length + files.length + 1;
	}

//    @Override
//    public long getItemId(int position) {
//        return position;
//    }

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	private ItemClickListener mItemClickListener;

	public interface ItemClickListener {
		public void onItemClick(int position);
	}

	public void setOnItemClickListener(ItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;

	}

	File[] getDirs() {
		return currentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				boolean showHidden = false;
				try {
					showHidden = !MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return !((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && showHidden) && pathname.isDirectory();
			}
		});
	}

	File[] getFiles() {
		return currentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String[] ex = MimeTypeUtil.getExtensions(mimeTypes);
//                for (String e : ex)
//                    if (!((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && !MainActivity.hiddenFiles) && !pathname.isDirectory() && pathname.getName().endsWith(e))
//                        return true;

//                System.out.println(MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeTypes));
//                if (!((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && !MainActivity.hiddenFiles) && !pathname.isDirectory() && pathname.getName().endsWith(".htm"))
//                    return true;
//                if (!((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && !MainActivity.hiddenFiles) && !pathname.isDirectory() && pathname.getName().endsWith(".html"))
//                    return true;
				boolean showHidden = false;
				try {
					showHidden = !MainActivity.db.getBoolean("showHidden");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return !((pathname.getName().startsWith(".") || pathname.getName().equals("LOST.DIR")) && showHidden) && !pathname.isDirectory() && MimeTypeUtil.meetsMimeTypes(pathname.getName(), mimeTypes);
			}
		});
	}

	File[] sortFile(File[] src) {
		if (src.length == 0) return src;
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

	Boolean chrSort(String s1, String s2) {
//        System.out.println("============================");
//        System.out.println("============================");
//        System.out.println(s1);
//        System.out.println(s2);
//        System.out.println("============================");
//        System.out.println("============================");
		char[] sa1 = s1.toCharArray();
		char[] sa2 = s2.toCharArray();
		for (int i = 0; i < Math.min(sa1.length, sa2.length); i++) {
//            System.out.println(sa1[i]);
//            System.out.println(sa2[i]);
//            System.out.println(String.valueOf(sa1[i] < sa2[i]));
			if (sa1[i] < sa2[i]) return true;
			else if (sa1[i] > sa2[i]) return false;
		}
		return false;
	}

	public File getFile(int position) {
		final int pos = position - 1;
		if (enRoot)
			return devs[pos];
		else {
			if (pos < dirs.length) {
				if (pos == -1) {
					for (File dev : devs) {
						if (dev.equals(currentDir)) {
							return null;
						}
					}
					return currentDir.getParentFile();
				} else return dirs[pos];
			} else if (pos >= dirs.length && pos < dirs.length + files.length) {
				return files[pos - dirs.length];
			}
		}
		return null;
	}

	public File[] getDevs() {
		return devs;
	}
	public File getCurrentDir() {		return currentDir;	}
	public File getRootDir() {		return rootDir;	}
	public File getParentDir() {
		for (File dev : devs) {
			if (dev.equals(currentDir)) {
				return null;
			}
		}
		return currentDir.getParentFile();
	}

	public void setDir(File dir) {
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

	public void setRoot() {
//        try {
//            currentDir = dir;
//            if (!currentDir.isDirectory()) throw new Exception();
//        } catch (Exception e) {
//            currentDir = Environment.getExternalStorageDirectory();
//        }
//        dirs = sortFile(getDirs());
//        files = sortFile(getFiles());
		devs = MainActivity.getStorages(context.getApplicationContext());
		enRoot = true;
	}
}
