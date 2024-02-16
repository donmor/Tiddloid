/*
 * top.donmor.tiddloid.WikiListAdapter <= [P|Tiddloid]
 * Last modified: 18:20:51 2024/02/16
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SvgParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class WikiListAdapter extends RecyclerView.Adapter<WikiListAdapter.WikiListHolder> {

	private final Context context;
	private JSONObject db, wl;
	private ArrayList<String> ids;
	private ItemClickListener mItemClickListener;
	private ItemFilter mItemFilter;
	private ReloadListener mReloadListener;
	private final LayoutInflater inflater;
	private final Vibrator vibrator;
	private final float scale;

	// 常量
	private static final String c160 = "\u00A0", zeroB = "\u00A0\u00A0\u00A0\u00A00\u00A0B", PAT_SIZE = "\u00A0\u00A0\u00A0\u00A0#,##0.##";
	private static final String[] units = new String[]{"B", "KB", "MB"};

	WikiListAdapter(Context context) {
		this.context = context;
		scale = context.getResources().getDisplayMetrics().density;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		inflater = LayoutInflater.from(context);
	}

	static class WikiListHolder extends RecyclerView.ViewHolder {
		private final Button btnWiki;

		WikiListHolder(View itemView) {
			super(itemView);
			btnWiki = itemView.findViewById(R.id.btnWiki);
			setVisibility(View.GONE);
		}

		private void setVisibility(int visibility) {
			itemView.setVisibility(visibility);
			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
			if (visibility == View.VISIBLE) {
				params.width = RecyclerView.LayoutParams.MATCH_PARENT;
				params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
			} else {
				params.width = 0;
				params.height = 0;
			}
			itemView.setLayoutParams(params);
		}
	}

	@Override
	@NonNull
	public WikiListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new WikiListHolder(inflater.inflate(R.layout.wiki_slot, parent, false));
	}

	@SuppressLint("QueryPermissionsNeeded")
	@Override
	public void onBindViewHolder(@NonNull final WikiListHolder holder, int position) {
		try {
			final String id = ids.get(position);
			JSONObject wa = wl.getJSONObject(id);
			String n = wa.optString(MainActivity.KEY_NAME, MainActivity.KEY_TW), s = wa.optString(MainActivity.DB_KEY_SUBTITLE), fib64 = wa.optString(MainActivity.KEY_FAVICON);
			holder.btnWiki.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
			if (fib64.length() > 0) {
				try {
					if (fib64.matches(MainActivity.REX_B64)) {    // Base64
						byte[] b = Base64.decode(fib64, Base64.NO_PADDING);
						Bitmap favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
						if (favicon != null) {
							int width = favicon.getWidth(), height = favicon.getHeight();
							Matrix matrix = new Matrix();
							matrix.postScale(scale * 24f / width, scale * 24f / height);
							holder.btnWiki.setCompoundDrawablesRelativeWithIntrinsicBounds(new BitmapDrawable(context.getResources(), Bitmap.createBitmap(favicon, 0, 0, width, height, matrix, true)), null, null, null);
						}
					} else {    // SVG
						Sharp svg = Sharp.loadString(fib64);
						Drawable drawable = svg.getSharpPicture().getDrawable();
						int bound = Math.round(scale * 24);
						holder.btnWiki.setCompoundDrawablesRelativeWithIntrinsicBounds(MainActivity.svg2bmp(drawable, bound, context.getResources()), null, null, null);
					}
				} catch (IllegalArgumentException | SvgParseException e) {
					e.printStackTrace();
				}
			}
			// 调用接口
			holder.btnWiki.setOnClickListener(v -> mItemClickListener.onItemClick(holder.getBindingAdapterPosition(), id));
			holder.btnWiki.setOnLongClickListener(v -> {
				vibrator.vibrate(new long[]{0, 1}, -1);
				mItemClickListener.onItemLongClick(holder.getBindingAdapterPosition(), id);
				return true;
			});
			// 条目显示
			boolean fTextA = mItemFilter.fTextActive(), fText = mItemFilter.fText(n, s);
			if (fTextA && !fText) {
				holder.setVisibility(View.GONE);
				return;
			}
			String fKeyword = mItemFilter.fKeyword();
			SpannableStringBuilder builder = new SpannableStringBuilder(n);
			builder.setSpan(new LeadingMarginSpan.Standard(Math.round(scale * 8f)), 0, builder.length(), Spanned.SPAN_MARK_POINT);
			try {
				builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.content_sub)), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
				if (fTextA) {
					int pkw = n.indexOf(fKeyword);
					while (pkw >= 0 && pkw < builder.length()) {
						builder.setSpan(new BackgroundColorSpan(context.getResources().getColor(R.color.content_back_fil)), pkw, pkw + fKeyword.length(), Spanned.SPAN_POINT_MARK);
						pkw = n.indexOf(fKeyword, pkw + 1);
					}
				}
			} catch (Resources.NotFoundException e) {
				e.printStackTrace();
			}
			if (s.length() > 0) {
				builder.append(MainActivity.KEY_LBL);
				int ps = builder.length();
				builder.append(s);
				if (fTextA) {
					int pkw = s.indexOf(fKeyword);
					while (pkw >= 0 && pkw < builder.length()) {
						builder.setSpan(new BackgroundColorSpan(context.getResources().getColor(R.color.content_back_fil)), ps + pkw, ps + pkw + fKeyword.length(), Spanned.SPAN_POINT_MARK);
						pkw = n.indexOf(fKeyword, pkw + 1);
					}
				}
			}
			builder.setSpan(new RelativeSizeSpan(0.8f), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
			Uri u = Uri.parse(wa.optString(MainActivity.DB_KEY_URI));
			boolean legacy = MainActivity.SCH_FILE.equals(u.getScheme());
			DocumentFile df = null;
			int pTimeSubBgn = 0, pTimeSubEnd = 0;
			Date mt = null;
			try {
				if (!legacy) try {
					DocumentFile mdf = DocumentFile.fromTreeUri(context, u);
					if (mdf == null || !mdf.isDirectory())
						throw new IOException(MainActivity.EXCEPTION_DOCUMENT_IO_ERROR);    // Fatal 根目录不可访问
					df = MainActivity.getIndex(context, mdf);
					if (df == null || !df.isFile())
						throw new FileNotFoundException(MainActivity.EXCEPTION_TREE_INDEX_NOT_FOUND);    // Fatal index不存在
				} catch (IllegalArgumentException ignored) {
				}
				DocumentFile f;
				if (MainActivity.SCH_HTTP.equals(u.getScheme()) || MainActivity.SCH_HTTPS.equals(u.getScheme())) {
					builder.append('\n');
					builder.append(u.toString());
					builder.append(c160).append(c160).append(c160).append(c160).append(context.getString(R.string.internet));
				} else if ((!legacy || u.getPath() != null)
						&& (f = legacy ? DocumentFile.fromFile(new File(u.getPath())) : df != null ? df : DocumentFile.fromSingleUri(context, u)) != null
						&& f.exists()) {
					builder.append('\n');
					pTimeSubBgn = builder.length();
					mt = new Date(f.lastModified());
					builder.append(SimpleDateFormat.getDateTimeInstance().format(mt));
					pTimeSubEnd = builder.length();
					builder.append(formatSize(f.length()));
					if (legacy) builder.append(c160).append(c160).append(c160).append(c160).append(context.getString(R.string.local_legacy));
					else {
						// 获取来源名
						PackageManager pm = context.getPackageManager();
						String v;
						for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA))
							if ((v = u.getAuthority()) != null && v.startsWith(info.packageName)) {
								builder.append(c160).append(c160).append(c160).append(c160).append(pm.getApplicationLabel(info).toString());
								break;
							}
					}
				}
				if (id.equals(db.optString(MainActivity.DB_KEY_DEFAULT)))
					builder.append(c160).append(c160).append(c160).append(c160).append(context.getString(R.string.default_wiki));
			} catch (IOException e) {
				e.printStackTrace();
			}

			boolean fTimeA = mItemFilter.fTimeActive(), fTime = mItemFilter.fTime(mt);
			if (fTimeA && !fTime) {
				holder.setVisibility(View.GONE);
				return;
			}
			if (fTimeA && pTimeSubBgn > 0 && pTimeSubEnd > 0)
				builder.setSpan(new BackgroundColorSpan(context.getResources().getColor(R.color.content_back_fil)),
						pTimeSubBgn, pTimeSubEnd, Spanned.SPAN_POINT_MARK);

			holder.btnWiki.setText(builder);
			holder.setVisibility(View.VISIBLE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return ids != null ? ids.size() : 0;
	}

	interface ItemClickListener {
		void onItemClick(int pos, String id);

		void onItemLongClick(int pos, String id);
	}

	interface ItemFilter {
		boolean fTextActive();

		boolean fText(String title, String sub);

		String fKeyword();

		boolean fTimeActive();

		boolean fTime(Date time);
	}

	void setOnItemClickListener(ItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;
	}

	void setItemFilter(ItemFilter itemFilter) {
		this.mItemFilter = itemFilter;
	}

	interface ReloadListener {
		void onReloaded(int count);
	}

	void setReloadListener(ReloadListener reloadListener) {
		this.mReloadListener = reloadListener;
	}

	void reload(JSONObject db) throws JSONException {
		this.db = db;
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
