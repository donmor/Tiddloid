/*
 * top.donmor.tiddloid.DavDirAdapter <= [P|Tiddloid]
 * Last modified: 05:03:14 2019/05/07
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.util.List;

public class DavDirAdapter extends RecyclerView.Adapter<DavDirAdapter.DavDirHolder> {

	//	private final Context context;
	//	private JSONObject wl;
	private List<DavResource> davItems = null;
	//	private ArrayList<String> ids;
	private ItemClickListener mItemClickListener;
	//	private ReloadListener mReloadListener;
	private final LayoutInflater inflater;
	private String host;
	private boolean canBack;
//	private final Vibrator vibrator;
//	private final float scale;

	// 常量
	private static final String PD = "..";
//	private static final String c160 = "\u00A0", zeroB = "0\u00A0B", PAT_SIZE = "\u00A0\u00A0\u00A0\u00A0#,##0.##";
//	private static final String[] units = new String[]{"B", "KB", "MB"};

	DavDirAdapter(Context context) {
//		this.context = context;
//		scale = context.getResources().getDisplayMetrics().density;
//		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//		reload(db);
		inflater = LayoutInflater.from(context);
	}

	static class DavDirHolder extends RecyclerView.ViewHolder {
		private final Button btnDavItem;

		DavDirHolder(View itemView) {
			super(itemView);
			btnDavItem = itemView.findViewById(R.id.btnDavDirItem);
			btnDavItem.setVisibility(View.GONE);
		}
	}

	@Override
	@NonNull
	public DavDirHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new DavDirHolder(inflater.inflate(R.layout.dav_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final DavDirHolder holder, int position) {
//		try {
		int pos = canBack ? position - 1 : position;
		if (canBack && position == 0) {
			holder.btnDavItem.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
			holder.btnDavItem.setText(PD);
			holder.btnDavItem.setEnabled(true);
			holder.btnDavItem.setOnClickListener(v -> mItemClickListener.onBackClick());
			holder.btnDavItem.setVisibility(View.VISIBLE);
			return;
		}
		if ((davItems == null || davItems.size() == 0)) {
			if (pos == 0) {
				holder.btnDavItem.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
				holder.btnDavItem.setText(R.string.no_wiki);
				holder.btnDavItem.setEnabled(false);
				holder.btnDavItem.setVisibility(View.VISIBLE);
			}
			return;
		}
		DavResource res = davItems.get(pos);
//			final String id = ids.get(position);
//			JSONObject wa = wl.getJSONObject(id);
//			String n = wa.optString(MainActivity.KEY_NAME, MainActivity.KEY_TW), s = wa.optString(MainActivity.DB_KEY_SUBTITLE), fib64 = wa.optString(MainActivity.KEY_FAVICON);
		holder.btnDavItem.setCompoundDrawablesRelativeWithIntrinsicBounds(res.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_description, 0, 0, 0);
//			if (fib64.length() > 0) {
//				byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
//				Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
//				if (favicon != null) {
//					int width = favicon.getWidth(), height = favicon.getHeight();
//					Matrix matrix = new Matrix();
//					matrix.postScale(scale * 24f / width, scale * 24f / height);
//					holder.btnDavItem.setCompoundDrawablesRelativeWithIntrinsicBounds(new BitmapDrawable(context.getResources(), Bitmap.createBitmap(favicon, 0, 0, width, height, matrix, true)), null, null, null);
//				}
//			}

		holder.btnDavItem.setOnClickListener(v -> mItemClickListener.onItemClick(host + res.getHref()));
		holder.btnDavItem.setText(res.getName());
		holder.btnDavItem.setEnabled(res.isDirectory() || MainActivity.TYPE_HTML.equals(res.getContentType()));
//		holder.btnDavItem.setEnabled(res.isDirectory());
//			holder.btnDavItem.setOnLongClickListener(v -> {
//				vibrator.vibrate(new long[]{0, 1}, -1);
//				mItemClickListener.onItemLongClick(holder.getBindingAdapterPosition(), id);
//				return true;
//			});
		// 条目显示
//			SpannableStringBuilder builder = new SpannableStringBuilder(n);
//			builder.setSpan(new LeadingMarginSpan.Standard(Math.round(scale * 8f)), 0, builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//			try {
//				builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.content_sub)), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
//			} catch (Resources.NotFoundException e) {
//				e.printStackTrace();
//			}
//			builder.append(s.length() > 0 ? MainActivity.KEY_LBL + s : s);
//			builder.setSpan(new RelativeSizeSpan(0.8f), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
//			Uri u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI));
//			boolean legacy = MainActivity.SCH_FILE.equals(u.getScheme());
//			DocumentFile df = null;
//			try {
//				if (MainActivity.APIOver21 && !legacy) try {
//					DocumentFile mdf = DocumentFile.fromTreeUri(context, u), p0;
//					if (mdf == null || !mdf.isDirectory())
//						throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
//					df = (p0 = mdf.findFile(MainActivity.KEY_FN_INDEX)) != null && p0.isFile() ? p0 : (p0 = mdf.findFile(MainActivity.KEY_FN_INDEX2)) != null && p0.isFile() ? p0 : null;
//					if (df == null || !df.isFile())
//						throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
//				} catch (IllegalArgumentException ignored) {
//				}
//				DocumentFile f;
//				if (MainActivity.SCH_HTTP.equals(u.getScheme()) || MainActivity.SCH_HTTPS.equals(u.getScheme())) {
//					builder.append('\n');
//					builder.append(u.toString());
//				} else if ((f = legacy ? DocumentFile.fromFile(new File(u.getPath())) : df != null ? df : DocumentFile.fromSingleUri(context, u)) != null && f.exists()) {
//					builder.append('\n');
//					builder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(f.lastModified()))).append(formatSize(f.length()));
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			holder.btnDavItem.setText(builder);
		holder.btnDavItem.setVisibility(View.VISIBLE);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public int getItemCount() {
		return (davItems != null && davItems.size() > 0 ? davItems.size() : 1) + (canBack ? 1 : 0);
	}


	interface ItemClickListener {
		void onItemClick(String dir);

		void onBackClick();
	}

	void setOnItemClickListener(ItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;
	}


//	interface ReloadListener {
//		void onReloaded(int count);
//	}

//	void setReloadListener(ReloadListener reloadListener) {
//		this.mReloadListener = reloadListener;
//	}

	void reload(List<DavResource> dir, String host, boolean canBack) {
		davItems = dir;
		this.host = host;
		this.canBack = canBack;
//		this.wl = db.getJSONObject(MainActivity.DB_KEY_WIKI);
//		Iterator<String> iterator = wl.keys();
//		ids = new ArrayList<>();
//		while (iterator.hasNext())
//			ids.add(iterator.next());
//		if (mReloadListener != null) mReloadListener.onReloaded(this.getItemCount());
	}

//	// 格式化大小
//	private String formatSize(long size) {
//		if (size <= 0)
//			return zeroB;
//		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
//		return new DecimalFormat(PAT_SIZE).format(size / Math.pow(1024, digitGroups)) + c160 + units[digitGroups];
//	}

}
