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
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WikiListAdapter extends RecyclerView.Adapter<WikiListAdapter.WikiListHolder> {

	private final Context context;
	private JSONObject db;
	private int count;
	private ItemClickListener mItemClickListener;
	private ReloadListener mReloadListener;
	private final LayoutInflater inflater;
	private final Vibrator vibrator;
	private final float scale;

	WikiListAdapter(Context context, JSONObject db) {
		this.context = context;
		this.db = db;
		scale = context.getResources().getDisplayMetrics().density;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		try {
			count = db.getJSONArray(MainActivity.DB_KEY_WIKI).length();
		} catch (Exception e) {
			e.printStackTrace();
		}
		inflater = LayoutInflater.from(context);
	}

	class WikiListHolder extends RecyclerView.ViewHolder {
		private final Button btnWiki;
		private String path;

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
	public void onBindViewHolder(@NonNull WikiListHolder holder, final int position) {
		try {
			final JSONObject w = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(position);
			holder.path = w.getString(MainActivity.DB_KEY_PATH);
			String vs = null;
			try {
				vs = w.getString(MainActivity.DB_KEY_SUBTITLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			final String n = w.getString(MainActivity.KEY_NAME), s = vs, id = w.getString(MainActivity.KEY_ID);
			FileInputStream is = null;
			Bitmap icon = null;
			try {
				File iconFile = new File(context.getDir(MainActivity.KEY_FAVICON, Context.MODE_PRIVATE), id);
				if (iconFile.exists() && iconFile.length() > 0) {
					is = new FileInputStream(iconFile);
					icon = BitmapFactory.decodeStream(is);
					if (icon != null) {
						Matrix matrix = new Matrix();
						int width = icon.getWidth(), height = icon.getHeight();
						matrix.postScale(scale * 24f / width, scale * 24f / height);
						Bitmap favicon = Bitmap.createBitmap(icon, 0, 0, width, height, matrix, true);
						holder.btnWiki.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(), favicon), null, null, null);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			final Bitmap fi = icon;
			final File f = new File(holder.path);
			holder.btnWiki.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mItemClickListener.onItemClick(position, id, f);
				}
			});
			holder.btnWiki.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					vibrator.vibrate(new long[]{0, 1}, -1);
					mItemClickListener.onItemLongClick(position, w, id, f, n, s, fi);
					return true;
				}
			});
			try {
				SpannableStringBuilder builder = new SpannableStringBuilder(w.getString(MainActivity.KEY_NAME));
				int p = builder.length();
				LeadingMarginSpan.Standard lms = new LeadingMarginSpan.Standard(Math.round(scale * 8f));
				builder.setSpan(lms, 0, p, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				builder.append(s != null && s.length() > 0 ? MainActivity.KEY_LBL + s : MainActivity.STR_EMPTY);
				builder.append('\n');
				ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(R.color.content_sub));
				builder.setSpan(fcs, p, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				p = builder.length();
				builder.append(f.exists() ? SimpleDateFormat.getDateTimeInstance().format(new Date(f.lastModified())) : MainActivity.STR_EMPTY);
				RelativeSizeSpan rss = new RelativeSizeSpan(0.8f);
				builder.setSpan(rss, p, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
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
		return count;
	}


	interface ItemClickListener {
		void onItemClick(int position, String id, File file);

		void onItemLongClick(int position, JSONObject w, String id, File file, String name, String sub, Bitmap favicon);
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

	void reload(JSONObject db) {
		this.db = db;
		try {
			count = this.db.getJSONArray(MainActivity.DB_KEY_WIKI).length();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mReloadListener.onReloaded(this.getItemCount());
	}
}
