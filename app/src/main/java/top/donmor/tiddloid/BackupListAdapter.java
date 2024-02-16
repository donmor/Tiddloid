/*
 * top.donmor.tiddloid.BackupListAdapter <= [P|Tiddloid]
 * Last modified: 15:46:33 2024/02/14
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.BackupListHolder> {

	private final Context context;
	/**
	 * Backup files array
	 */
	private File[] bk;
	/**
	 * Backup files array in tree mode
	 */
	private DocumentFile[] bkd;
	/**
	 * Triggered when list loaded
	 */
	private LoadListener mLoadListener;
	/**
	 * Button callback
	 */
	private BtnClickListener mBtnClickListener;
	/**
	 * Inflate layouts
	 */
	private final LayoutInflater inflater;
	/**
	 * Indicates if source wiki is tree mode
	 */
	private boolean tree;


	private static final int ROLLBACK = 1, DELETE = 2;

	BackupListAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	static class BackupListHolder extends RecyclerView.ViewHolder {
		/**
		 * Rollback button
		 */
		private final ImageButton btnRollBack,
		/**
		 * Delete backup button
		 */
		btnDelBackup;
		/**
		 * Item label
		 */
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
	public void onBindViewHolder(@NonNull final BackupListHolder holder, int position) {
		try {
			String efn = tree ? bkd[position].getName() : bk[position].getName();    // ~GenFilename
			int efp1, efp2;        // FilenameSplittingPoint
			if (efn == null) throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);
			efp1 = efn.lastIndexOf('.', (efp2 = efn.lastIndexOf('.')) - 1);
			holder.lblBackupFile.setText(SimpleDateFormat.getDateTimeInstance().format(parseUTCString(efn.substring(efp1 + 1, efp2)).getTime()));
			holder.btnRollBack.setOnClickListener(v -> mBtnClickListener.onBtnClick(holder.getBindingAdapterPosition(), ROLLBACK));
			holder.btnDelBackup.setOnClickListener(v -> mBtnClickListener.onBtnClick(holder.getBindingAdapterPosition(), DELETE));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return tree ? bkd.length : bk != null ? bk.length : 0;
	}


	/**
	 * Button click callbacks, which=1 for rollback, 2 for delete
	 */
	interface BtnClickListener {
		void onBtnClick(int pos, int which);
	}

	/**
	 * Set button click callbacks
	 */
	void setOnBtnClickListener(BtnClickListener btnClickListener) {
		this.mBtnClickListener = btnClickListener;

	}


	/**
	 * Mainly for getting backup count
	 */
	interface LoadListener {
		/**
		 * Mainly for getting backup count
		 */
		void onLoad(int count);
	}

	/**
	 * Set on load callbacks
	 */
	void setOnLoadListener(LoadListener loadListener) {
		this.mLoadListener = loadListener;

	}

	/**
	 * Get backup file
	 */
	File getBackupFile(int position) {
		return tree ? null : position < getItemCount() ? bk[position] : null;
	}

	/**
	 * Get backup file - tree mode
	 */
	DocumentFile getBackupDF(int position) {
		return position < getItemCount() ? !tree ? DocumentFile.fromFile(bk[position]) : bkd[position] : null;
	}

	/**
	 * Refresh data
	 */
	void reload(@NonNull Uri mainFile) throws IOException {
		if (MainActivity.SCH_HTTP.equals(mainFile.getScheme()) || MainActivity.SCH_HTTPS.equals(mainFile.getScheme()))
			return;
		DocumentFile df, bdf = null;
		String vfn = null;
		boolean legacy = MainActivity.SCH_FILE.equals(mainFile.getScheme());
		if (!legacy) try {
			DocumentFile mdf = DocumentFile.fromTreeUri(context, mainFile);    // 非TreeUri时抛IllegalArgumentException
			if (mdf == null || !mdf.isDirectory())
				throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
			df = MainActivity.getIndex(context, mdf);
			vfn = df != null && df.getName() != null ? df.getName() : MainActivity.KEY_FN_INDEX;
			String vfn2 = df != null && df.getName() != null ? df.getName() : MainActivity.KEY_FN_INDEX2;
			bdf = mdf.findFile(vfn + MainActivity.BACKUP_POSTFIX);    // 不一定存在，需要判断
			if (bdf == null) {
				bdf = mdf.findFile(vfn2 + MainActivity.BACKUP_POSTFIX);    // 不一定存在，需要判断
				vfn = vfn2;
			}
			tree = true;
		} catch (IllegalArgumentException ignored) {
		}
		if (tree) {
			if (bdf != null && bdf.isDirectory()) {    // 目录模式，backup子目录是否存在
				int x = 0;
				DocumentFile[] fl = bdf.listFiles(), b0 = new DocumentFile[fl.length];
				for (DocumentFile inner : fl)
					if (inner != null && inner.isFile() &&
							MainActivity.isBackupFile(vfn, inner.getName())) {
						b0[x] = inner;
						x++;
					}
				bkd = new DocumentFile[x];
				if (x > 0) System.arraycopy(b0, 0, bkd, 0, x);
			} else bkd = new DocumentFile[0];
		} else {    // 文件模式位置：External files/encoded uri/<filename>_backup/ legacy模式原地
			String mfp = mainFile.getPath();
			File mf;
			if (legacy) {
				if (mfp == null) {
					bk = new File[0];
					mLoadListener.onLoad(getItemCount());
					return;
				}
				mf = new File(mfp);
			} else {
				File pf = new File(context.getExternalFilesDir(null), Uri.encode(mainFile.getSchemeSpecificPart()));
				if ((df = DocumentFile.fromSingleUri(context, mainFile)) != null && df.getName() != null)
					mf = new File(pf, df.getName());
				else {
					String mfu = Uri.decode(mainFile.toString());
					if (mfu == null) {
						bk = new File[0];
						mLoadListener.onLoad(getItemCount());
						return;    // Usually not possible
					}
					String lp = Uri.parse(mfu).getLastPathSegment();
					if (lp == null) {
						bk = new File[0];
						mLoadListener.onLoad(getItemCount());
						return;    // Abort on error
					}
					mf = new File(pf, lp);
				}
			}
			String mfn = mf.getName();
			File mfd = new File(mf.getParentFile(), mfn + MainActivity.BACKUP_POSTFIX);
			int x;
			bk = sortFile(mfd.listFiles(pathname -> pathname.isFile() && MainActivity.isBackupFile(mf.getName(), pathname.getName())), (x = mfn.lastIndexOf('.')) < 0 ? mfn : mfn.substring(0, x));
		}
		mLoadListener.onLoad(getItemCount());
	}

	// 排序
	private File[] sortFile(File[] src, String mfn) {
		if (src == null || src.length == 0) return new File[0];    // 使 bk != null
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

	private Date parseUTCString(String v) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(MainActivity.MASK_SDF_BACKUP, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(MainActivity.KEY_TZ_UTC));
		return format.parse(v);
	}
}
