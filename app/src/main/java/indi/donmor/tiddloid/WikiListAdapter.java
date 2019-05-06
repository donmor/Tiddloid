package indi.donmor.tiddloid;

import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.NonNull;
//import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WikiListAdapter extends RecyclerView.Adapter<WikiListAdapter.WikiListHolder> {

	private JSONObject db;
	private int count;
	private ItemClickListener mItemClickListener;
	private ReloadListener mReloadListener;
	private final LayoutInflater inflater;
	private final Vibrator vibrator;

	// CONSTANT
	private static final String HTML_ATTR_PART_1 = "<br><font color=\"grey\">",
			HTML_ATTR_PART_2 = "</font>";
	WikiListAdapter(Context context, JSONObject db) {
		this.db = db;
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
		}
	}

	@Override
	@NonNull
	public WikiListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new WikiListHolder(inflater.inflate(R.layout.wiki_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull WikiListHolder holder, int position) {
		try {
			final int pos = position;
			holder.btnWiki.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mItemClickListener.onItemClick(pos);
				}
			});
			holder.btnWiki.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					vibrator.vibrate(new long[]{0, 1}, -1);
					mItemClickListener.onItemLongClick(pos);
					return true;
				}
			});
			holder.path = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(pos).getString(MainActivity.DB_KEY_PATH);
			File f = new File(holder.path);
			System.out.println(f.getAbsolutePath());
			if (f.exists()) {
				holder.btnWiki.setVisibility(View.VISIBLE);
				holder.btnWiki.setText(Html.fromHtml(db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(pos).getString(MainActivity.KEY_NAME) + HTML_ATTR_PART_1 + SimpleDateFormat.getDateTimeInstance().format(new Date(f.lastModified())) + HTML_ATTR_PART_2));
			} else holder.btnWiki.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return count;
	}


	interface ItemClickListener {
		void onItemClick(int position);

		void onItemLongClick(int position);
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
			System.out.println(count);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mReloadListener.onReloaded(this.getItemCount());
	}

	String getId(int position) {
		String id = null;
		try {
			id = db.getJSONArray(MainActivity.DB_KEY_WIKI).getJSONObject(position).getString(MainActivity.KEY_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
}
