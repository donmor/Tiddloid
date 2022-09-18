/*
 * top.donmor.tiddloid.BackupListAdapter <= [P|Tiddloid]
 * Last modified: 15:22:20 2022/02/05
 * Copyright (c) 2022 donmor
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

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BackupListAdapter extends RecyclerView.Adapter<BackupListAdapter.BackupListHolder> {

	private final Context context;
	private File[] bk;
	private DocumentFile[] bkd;
	private List<DavResource> bkv;
	private Sardine davClient;
	private String bkp;
	private LoadListener mLoadListener;
	private BtnClickListener mBtnClickListener;
	private final LayoutInflater inflater;
	private boolean tree;


	private static final int ROLLBACK = 1, DELETE = 2;

	BackupListAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	static class BackupListHolder extends RecyclerView.ViewHolder {
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
	public void onBindViewHolder(@NonNull final BackupListHolder holder, int position) {
		try {
			String efn = davClient != null ? bkv.get(position).getName() : tree ? bkd[position].getName() : bk[position].getName();
			int efp1, efp2;
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
		return bkv != null ? bkv.size() : tree ? bkd.length : bk != null ? bk.length : 0;
	}


	interface BtnClickListener {
		void onBtnClick(int pos, int which);
	}

	void setOnBtnClickListener(BtnClickListener btnClickListener) {
		this.mBtnClickListener = btnClickListener;

	}


	interface LoadListener {
		void onLoad(int count);
	}

	void setOnLoadListener(LoadListener loadListener) {
		this.mLoadListener = loadListener;

	}

	File getBackupFile(int position) {
		return tree ? null : position < getItemCount() ? bk[position] : null;
	}

	DocumentFile getBackupDF(int position) {
		return position < getItemCount() ? !tree ? DocumentFile.fromFile(bk[position]) : bkd[position] : null;
	}

	String getBackupDavUri(int position) {
		return davClient != null && position < getItemCount() ? bkp + MainActivity.KEY_SLASH + bkv.get(position).getName() : null;
	}

	void reload(Uri mainFile, Sardine davClient) throws IOException {
		if (MainActivity.SCH_HTTP.equals(mainFile.getScheme()) || MainActivity.SCH_HTTPS.equals(mainFile.getScheme())) {    // WebDAV
			if (davClient == null) return;
			this.davClient = davClient;
			Thread jt = new Thread(() -> {
				try {
					List<DavResource> root;
					if (davClient.exists(mainFile.toString()) && (root = davClient.list(mainFile.toString())).get(0).isDirectory()) {
						boolean haveIndex = false;
						for (DavResource f : root) {
							if (MainActivity.KEY_FN_INDEX.equals(f.getName()) || MainActivity.KEY_FN_INDEX2.equals(f.getName())) {
								bkv = davClient.list(bkp = mainFile.getScheme() + MainActivity.KEY_URI_NOTCH + mainFile.getAuthority() + f.getHref() + MainActivity.BACKUP_POSTFIX);
								bkv.remove(0);
								haveIndex = true;
								break;
							}
						}
						if (!haveIndex) for (DavResource f : root) {
							if ((f.getName().equals(MainActivity.KEY_FN_INDEX + MainActivity.BACKUP_POSTFIX) || f.getName().equals(MainActivity.KEY_FN_INDEX2 + MainActivity.BACKUP_POSTFIX)) && f.isDirectory()) {
								bkv = davClient.list(bkp = mainFile.getScheme() + MainActivity.KEY_URI_NOTCH + mainFile.getAuthority() + f.getHref());
								bkv.remove(0);
								haveIndex = true;
								break;
							}
						}
						if (!haveIndex) throw new FileNotFoundException(MainActivity.EXCEPTION_SAF_FILE_NOT_EXISTS);
					} else {
						bkv = davClient.list(bkp = mainFile + MainActivity.BACKUP_POSTFIX);
						bkv.remove(0);
					}
				} catch (IOException | IndexOutOfBoundsException e) {
					e.printStackTrace();
					bkv = null;
				}
			});
			jt.start();
			try {
				jt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mLoadListener.onLoad(getItemCount());
			return;
		}
		DocumentFile df, bdf = null;
		String vfn = null;
		boolean legacy = MainActivity.SCH_FILE.equals(mainFile.getScheme());
		if (MainActivity.APIOver21 && !legacy) try {
			DocumentFile mdf = DocumentFile.fromTreeUri(context, mainFile), p;    // 非TreeUri时抛IllegalArgumentException
			if (mdf == null || !mdf.isDirectory())
				throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
			df = (p = mdf.findFile(MainActivity.KEY_FN_INDEX)) != null && p.isFile() ? p : (p = mdf.findFile(MainActivity.KEY_FN_INDEX2)) != null && p.isFile() ? p : null;
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
			File mf = legacy ? new File(mainFile.getPath()) : new File(new File(context.getExternalFilesDir(null), Uri.encode(mainFile.getSchemeSpecificPart())), (df = DocumentFile.fromSingleUri(context, mainFile)) != null && df.getName() != null ? df.getName() : Uri.parse(Uri.decode(mainFile.toString())).getLastPathSegment());
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
