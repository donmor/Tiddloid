package indi.donmor.tiddloid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.BackupListHolder> {

	private File mf;
	private File[] bk;
	private long[] dt;
	private int count;
	private final LayoutInflater inflater;

	BackupListAdapter(Context context) {
		count = 0;
		inflater = LayoutInflater.from(context);
	}

	class BackupListHolder extends RecyclerView.ViewHolder {
		private final ImageButton btnRollBack, btnDelBackup;
		private final TextView lblBackupFile;

		BackupListHolder(View itemView) {
			super(itemView);
			btnRollBack = itemView.findViewById(R.id.btnRollBack);
			btnDelBackup = itemView.findViewById(R.id.btnDelBackup);
			lblBackupFile = itemView.findViewById(R.id.lblBackupFile);
		}
	}

	@Override
	@NonNull
	public BackupListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new BackupListHolder(inflater.inflate(R.layout.backup_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull BackupListHolder holder, int position) {
		final int pos = position;

		try {
			holder.lblBackupFile.setText(SimpleDateFormat.getDateTimeInstance().format(dt[position]));
			holder.btnRollBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mBtnClickListener.onBtnClick(pos, 1);
				}
			});
			holder.btnDelBackup.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mBtnClickListener.onBtnClick(pos, 2);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return count;
	}

	private BtnClickListener mBtnClickListener;

	interface BtnClickListener {
		void onBtnClick(int position, int which);
	}

	void setOnBtnClickListener(BtnClickListener btnClickListener) {
		this.mBtnClickListener = btnClickListener;

	}

	private LoadListener mLoadListener;

	interface LoadListener {
		void onLoad(int count);
	}

	void setOnLoadListener(LoadListener loadListener) {
		this.mLoadListener = loadListener;

	}

	File getBackupFile(int position) {
		if (position < count) return bk[position];
		else return null;
	}

	void reload(Context context, File mainFile) {
		this.mf = mainFile;
		count = 0;
		try {
			String mfp = mf.getParentFile().getAbsolutePath(), mfn = mf.getName();
			File mfd = new File(mfp +'/'+mfn+ MainActivity.BACKUP_DIRECTORY_PATH_PREFIX);
//			File mfd = new File(mfp +'/'+ context.getResources().getString(R.string.backup_directory_path).replace("$filename$", mfn));
			if (mfd.isDirectory())
				bk = sortFile(mfd.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return MainActivity.isBackupFile(mf, pathname);
					}
				}), mfn.substring(0, mfn.indexOf('.')));
			if (bk!=null)count = bk.length;
			JodaTimeAndroid.init(context);
			dt = new long[count];
			for (int i = 0; i < count; i++) {
				dt[i] = parseUTCString(bk[i].getName().substring(mfn.indexOf('.') + 1, mfn.indexOf('.') + 18)).getMillis();
			}
			mLoadListener.onLoad(count);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private File[] sortFile(File[] src, String mfn) {
		if (src.length == 0) return src;
		int p = mfn.length() + 1;
		for (int i = 0; i < src.length; i++) {
			for (int j = 0; j < src.length - i - 1; j++) {
				if (Long.parseLong(src[j + 1].getName().substring(p, p + 17)) < Long.parseLong(src[j].getName().substring(p, p + 17))) {
					File temp = src[j + 1];
					src[j + 1] = src[j];
					src[j] = temp;
				}
			}
		}
		return src;
	}

	private DateTime parseUTCString(String v) {
		return new DateTime(Integer.parseInt(v.substring(0, 4)),
				Integer.parseInt(v.substring(4, 6)),
				Integer.parseInt(v.substring(6, 8)),
				Integer.parseInt(v.substring(8, 10)),
				Integer.parseInt(v.substring(10, 12)),
				Integer.parseInt(v.substring(12, 14)),
				Integer.parseInt(v.substring(14, 17)), DateTimeZone.UTC).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));
	}
}
