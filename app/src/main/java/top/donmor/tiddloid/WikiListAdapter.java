/*
 * top.donmor.tiddloid.WikiListAdapter <= [P|Tiddloid]
 * Last modified: 05:03:14 2019/05/07
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class WikiListAdapter extends RecyclerView.Adapter<WikiListAdapter.WikiListHolder> {

	private final Context context;
	private JSONObject wl;
	private ArrayList<String> ids;
	private ItemClickListener mItemClickListener;
	private ReloadListener mReloadListener;
	private final LayoutInflater inflater;
	private final Vibrator vibrator;
	private final float scale;

	// 常量
	private static final String c160 = "\u00A0", zeroB = "0\u00A0B", PAT_SIZE = "\u00A0\u00A0\u00A0\u00A0#,##0.##";
	private static final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

	WikiListAdapter(Context context, JSONObject db) throws JSONException {
		this.context = context;
		scale = context.getResources().getDisplayMetrics().density;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		reload(db);
		inflater = LayoutInflater.from(context);
	}

	static class WikiListHolder extends RecyclerView.ViewHolder {
		private final Button btnWiki;

		WikiListHolder(View itemView) {
			super(itemView);
			btnWiki = itemView.findViewById(R.id.btnWiki);
			btnWiki.setVisibility(View.GONE);
		}
	}

	@Override
	@NonNull
	public WikiListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new WikiListHolder(inflater.inflate(R.layout.wiki_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final WikiListHolder holder, int position) {
		try {
			final String id = ids.get(position);
			JSONObject wa = wl.getJSONObject(id);
			String n = wa.optString(MainActivity.KEY_NAME, MainActivity.KEY_TW), s = wa.optString(MainActivity.DB_KEY_SUBTITLE), fib64 = wa.optString(MainActivity.KEY_FAVICON);
			if (MainActivity.SCH_FILE.equals(Uri.parse(wa.optString(MainActivity.DB_KEY_URI)).getScheme()))
				MainActivity.checkPermission(context);
//			holder.btnWiki.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
			if (fib64.length() > 0) {
				byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
				Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
				if (favicon != null) {
					int width = favicon.getWidth(), height = favicon.getHeight();
					Matrix matrix = new Matrix();
					matrix.postScale(scale * 24f / width, scale * 24f / height);
					holder.btnWiki.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(), Bitmap.createBitmap(favicon, 0, 0, width, height, matrix, true)), null, null, null);
				}
			}
			holder.btnWiki.setOnClickListener(v -> mItemClickListener.onItemClick(holder.getBindingAdapterPosition(), id));
			holder.btnWiki.setOnLongClickListener(v -> {
				vibrator.vibrate(new long[]{0, 1}, -1);
				mItemClickListener.onItemLongClick(holder.getBindingAdapterPosition(), id);
				return true;
			});
			// 条目显示
			try {
				SpannableStringBuilder builder = new SpannableStringBuilder(n);
				int p = builder.length();
				LeadingMarginSpan.Standard lms = new LeadingMarginSpan.Standard(Math.round(scale * 8f));
				builder.setSpan(lms, 0, p, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				builder.append(s.length() > 0 ? MainActivity.KEY_LBL + s : s);
				builder.append('\n');
				ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(R.color.content_sub));
				builder.setSpan(fcs, p, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				Uri u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI));
				boolean legacy = MainActivity.SCH_FILE.equals(u.getScheme());
				DocumentFile df = null;
				if (MainActivity.APIOver21 && !legacy) try {
					DocumentFile mdf = DocumentFile.fromTreeUri(context, u), p0;
					if (mdf == null || !mdf.isDirectory()) {
						Toast.makeText(context, R.string.data_error, Toast.LENGTH_SHORT).show();
						return;
					}
					df = (p0 = mdf.findFile(MainActivity.KEY_FN_INDEX)) != null && p0.isFile() ? p0 : (p0 = mdf.findFile(MainActivity.KEY_FN_INDEX2)) != null && p0.isFile() ? p0 : null;
					if (df == null || !df.isFile()) {
						Toast.makeText(context, R.string.data_error, Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (IllegalArgumentException ignored) {
				}
				DocumentFile f;
				if (MainActivity.SCH_HTTP.equals(u.getScheme()) || MainActivity.SCH_HTTPS.equals(u.getScheme())) {
					p = builder.length();
					builder.append(u.toString());
					RelativeSizeSpan rss = new RelativeSizeSpan(0.8f);
					builder.setSpan(rss, p, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				} else if ((f = legacy ? DocumentFile.fromFile(new File(u.getPath())) : df != null ? df : DocumentFile.fromSingleUri(context, u)) != null && f.exists()) {
					p = builder.length();
					builder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(f.lastModified()))).append(formatSize(f.length()));
					RelativeSizeSpan rss = new RelativeSizeSpan(0.8f);
					builder.setSpan(rss, p, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				holder.btnWiki.setText(builder);
			} catch (Exception e) {
				e.printStackTrace();
			}
			holder.btnWiki.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return ids.size();
	}


	interface ItemClickListener {
		void onItemClick(int pos, String id);

		void onItemLongClick(int pos, String id);
	}

	void setOnItemClickListener(ItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;
	}


	interface ReloadListener {
		void onReloaded(int count);
	}

	void setReloadListener(ReloadListener reloadListener) {
		this.mReloadListener = reloadListener;
	}

	void reload(JSONObject db) throws JSONException {
		this.wl = db.getJSONObject(MainActivity.DB_KEY_WIKI);
		Iterator<String> iterator = wl.keys();
		ids = new ArrayList<>();
		while (iterator.hasNext())
			ids.add(iterator.next());
		if (mReloadListener != null) mReloadListener.onReloaded(this.getItemCount());
	}

	// 格式化大小
	private String formatSize(long size) {
		if (size <= 0)
			return zeroB;
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat(PAT_SIZE).format(size / Math.pow(1024, digitGroups)) + c160 + units[digitGroups];
	}

}
