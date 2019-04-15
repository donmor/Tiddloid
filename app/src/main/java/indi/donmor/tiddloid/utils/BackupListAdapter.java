package indi.donmor.tiddloid.utils;

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

import indi.donmor.tiddloid.MainActivity;
import indi.donmor.tiddloid.R;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.BackupListHolder> {

	//	private Context context;
	private File mf;
	private File[] bk;
	private long[] dt;
	private int count;
	private LayoutInflater inflater;

	public BackupListAdapter(Context context) {
//		this.context = context;
//		this.mf = mainFile;
		count = 0;
		inflater = LayoutInflater.from(context);
	}

	class BackupListHolder extends RecyclerView.ViewHolder {
		private ImageButton btnRollBack, btnDelBackup;
		private TextView lblBackupFile;
//		private CardView cvWiki;
//		private String id, path;

		BackupListHolder(View itemView) {
			super(itemView);
			btnRollBack = itemView.findViewById(R.id.btnRollBack);
			btnDelBackup = itemView.findViewById(R.id.btnDelBackup);
			lblBackupFile = itemView.findViewById(R.id.lblBackupFile);
//			btnWiki = (Button) itemView.findViewById(R.id.btnWiki);
//			cvWiki = (CardView) itemView.findViewById(R.id.cvWiki);
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
//			String bfn = bk[position].getName();
//			String bfn1 = bfn.substring(0, bfn.lastIndexOf("."));
//			String ts = bfn1.substring(bfn1.lastIndexOf("."));
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
//			sdf.parse(ts);

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

//			holder.btnWiki.setText(db.getJSONArray("wiki").getJSONObject(position).getString("name"));
//			holder.id = db.getJSONArray("wiki").getJSONObject(position).getString("id");
//			holder.btnWiki.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					mItemClickListener.onItemClick(position);
//				}
//			});
//			holder.btnWiki.setOnLongClickListener(new View.OnLongClickListener() {
//				@Override
//				public boolean onLongClick(View v) {
////                    mItemClickListener.onItemClick(position);
////					vibrator.vibrate(20);
//					Toast.makeText(context, "e", Toast.LENGTH_SHORT).show();
//					return true;
//				}
//			});
//			holder.path = db.getJSONArray("wiki").getJSONObject(position).getString("path");
//			File f = new File(holder.path);
//			System.out.println(f.getAbsolutePath());
//			if (f.exists()) holder.cvWiki.setVisibility(View.VISIBLE);
//			else holder.cvWiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return count;
	}

	private BtnClickListener mBtnClickListener;

	public interface BtnClickListener {
		void onBtnClick(int position, int which);
	}

	public void setOnBtnClickListener(BtnClickListener btnClickListener) {
		this.mBtnClickListener = btnClickListener;

	}

	private LoadListener mLoadListener;

	public interface LoadListener {
		void onLoad(int count);
	}

	public void setOnLoadListener(LoadListener loadListener) {
		this.mLoadListener = loadListener;

	}

	public File getBackupFile(int position) {
		if (position < count) return bk[position];
		else return null;
	}

	public void reload(Context context, File mainFile) {
//		this.context = context;
		this.mf = mainFile;
		count = 0;
		try {
			String mfp = mf.getParentFile().getAbsolutePath(), mfn = mf.getName();
			File mfd = new File(mfp + context.getResources().getString(R.string.backup_directory_path).replace("$filename$", mfn));
			if (mfd.isDirectory())
				bk = sortFile(mfd.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return MainActivity.isBackupFile(mf, pathname);
					}
				}), mfn.substring(0, mfn.indexOf('.')));
			count = bk.length;
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


//	private boolean isBackupFile(File main, File chk) {
//		String mfn = main.getName();
//		String mfn1 = mfn.substring(0, mfn.lastIndexOf('.'));
//		String mfn2 = mfn.substring(mfn.lastIndexOf('.') + 1);
//		String efn = chk.getName();
//		int p = mfn1.length();
//		boolean k1 = efn.substring(0, p).equals(mfn1);
//		boolean k2 = efn.charAt(p) == '.';
//		p++;
//		boolean k3 = true;
//		for (int pp = p; pp < p + 17; pp++)
//			if (efn.charAt(pp) < 48 || efn.charAt(pp) > 57) {
//				k3 = false;
//				break;
//			}
//		p += 17;
//		boolean k4 = efn.charAt(p) == '.';
//		p++;
//		boolean k5 = efn.substring(p).equals(mfn2);
//		return k1 && k2 && k3 && k4 && k5;
//	}

	private DateTime parseUTCString(String v) {
		return new DateTime(Integer.parseInt(v.substring(0, 4)),
				Integer.parseInt(v.substring(4, 6)),
				Integer.parseInt(v.substring(6, 8)),
				Integer.parseInt(v.substring(8, 10)),
				Integer.parseInt(v.substring(10, 12)),
				Integer.parseInt(v.substring(12, 14)),
				Integer.parseInt(v.substring(14, 17)), DateTimeZone.UTC).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));
	}
//	public String getId(int position) {
//		String id = null;
//		try {
////			id = db.getJSONArray("wiki").getJSONObject(position).getString("id");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return id;
//	}
}
