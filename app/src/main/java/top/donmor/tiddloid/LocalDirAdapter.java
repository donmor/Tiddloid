/*
 * top.donmor.tiddloid.LocalDirAdapter <= [P|Tiddloid]
 * Last modified: 20:51:59 2022/08/28
 * Copyright (c) 2022 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class LocalDirAdapter extends RecyclerView.Adapter<LocalDirAdapter.LocalDirHolder> {

	private List<File> localItems = null;
	private ItemClickListener mItemClickListener;
	private PathMon mPathMon;
	private final LayoutInflater inflater;
	private boolean canBack;

	// 常量
	private static final String PD = "..";

	LocalDirAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	static class LocalDirHolder extends RecyclerView.ViewHolder {
		final Button btnLocalItem;
		File strPath;

		LocalDirHolder(View itemView) {
			super(itemView);
			btnLocalItem = itemView.findViewById(R.id.btnDavDirItem);
			btnLocalItem.setVisibility(View.GONE);
		}
	}

	@Override
	@NonNull
	public LocalDirHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new LocalDirHolder(inflater.inflate(R.layout.dav_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final LocalDirHolder holder, int position) {
		int pos = canBack ? position - 1 : position;
		if (canBack && position == 0) {
			holder.btnLocalItem.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
			holder.btnLocalItem.setText(PD);
			holder.btnLocalItem.setEnabled(true);
			holder.btnLocalItem.setOnClickListener(v -> mItemClickListener.onBackClick());
			holder.btnLocalItem.setVisibility(View.VISIBLE);
			return;
		}
		if ((localItems == null || localItems.size() == 0)) {
			if (pos == 0) {
				holder.btnLocalItem.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
				holder.btnLocalItem.setText(R.string.no_wiki);
				holder.btnLocalItem.setEnabled(false);
				holder.btnLocalItem.setVisibility(View.VISIBLE);
			}
			return;
		}
		File res = localItems.get(pos);
		holder.btnLocalItem.setCompoundDrawablesRelativeWithIntrinsicBounds(res.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_description, 0, 0, 0);
		holder.strPath = res;
		mPathMon.checkPath(holder);
		holder.btnLocalItem.setOnClickListener(v -> {
			mItemClickListener.onItemClick(res);
			mPathMon.checkPath(holder);
		});
		holder.btnLocalItem.setText(res.getName());
		holder.btnLocalItem.setEnabled(res.isDirectory() || res.getName().endsWith(MainActivity.KEY_EX_HTML) || res.getName().endsWith(MainActivity.KEY_EX_HTM) || res.getName().endsWith(MainActivity.KEY_EX_HTA));
		holder.btnLocalItem.setVisibility(View.VISIBLE);
	}

	@Override
	public int getItemCount() {
		return (localItems != null && localItems.size() > 0 ? localItems.size() : 1) + (canBack ? 1 : 0);
	}

	interface ItemClickListener {
		void onItemClick(File dir);

		void onBackClick();
	}

	void setOnItemClickListener(ItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;
	}

	interface PathMon {
		void checkPath(LocalDirHolder h);
	}

	void setPathMon(PathMon m) {
		this.mPathMon = m;
	}

	void reload(List<File> dir, boolean canBack) {
		localItems = dir;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localItems != null) {
			localItems.sort((file, t1) -> {
				if (t1.isDirectory() && file.isFile()) return 1;
				return file.getName().compareToIgnoreCase(t1.getName());
			});
		}
		this.canBack = canBack;
	}
}
