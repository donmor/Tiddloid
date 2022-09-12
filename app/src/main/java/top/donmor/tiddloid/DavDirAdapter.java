/*
 * top.donmor.tiddloid.DavDirAdapter <= [P|Tiddloid]
 * Last modified: 19:30:25 2022/08/28
 * Copyright (c) 2022 donmor
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

	private List<DavResource> davItems = null;
	private ItemClickListener mItemClickListener;
	private final LayoutInflater inflater;
	private String host;
	private boolean canBack;

	// 常量
	private static final String PD = "..";

	DavDirAdapter(Context context) {
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
		holder.btnDavItem.setCompoundDrawablesRelativeWithIntrinsicBounds(res.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_description, 0, 0, 0);
		holder.btnDavItem.setOnClickListener(v -> mItemClickListener.onItemClick(host + res.getHref()));
		holder.btnDavItem.setText(res.getName());
		holder.btnDavItem.setEnabled(res.isDirectory() || MainActivity.TYPE_HTML.equals(res.getContentType()) || MainActivity.TYPE_HTA.equals(res.getContentType()));
		holder.btnDavItem.setVisibility(View.VISIBLE);
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

	void reload(List<DavResource> dir, String host, boolean canBack) {
		davItems = dir;
		this.host = host;
		this.canBack = canBack;
	}
}
