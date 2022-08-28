/*
 * top.donmor.tiddloid.TemplateListAdapter <= [P|Tiddloid]
 * Last modified: 14:37:54 2022/03/26
 * Copyright (c) 2022 donmor
 */

package top.donmor.tiddloid;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TemplateListAdapter extends RecyclerView.Adapter<TemplateListAdapter.TemplateListHolder> {

	private final Context context;
	private final LayoutInflater inflater;
	private JSONArray templates0;
	private JSONObject[] templates;
	private TemplateInterface templateInterface;
	private final float scale;

	// 常量
	private static final String c160 = "\u00A0", zeroB = "0\u00A0B", PAT_SIZE = "\u00A0\u00A0\u00A0\u00A0#,##0.##",
			TPL_KEY_HOMEPAGE = "homepage";
	private static final String[] units = new String[]{"B", "KB", "MB"};


	TemplateListAdapter(Context context) {
		this.context = context;
		scale = context.getResources().getDisplayMetrics().density;
		inflater = LayoutInflater.from(context);
		templates = new JSONObject[0];
	}

	static class TemplateListHolder extends RecyclerView.ViewHolder {
		private final Button btnTemplateNew;
		private final ImageButton btnTemplateSite;

		TemplateListHolder(View itemView) {
			super(itemView);
			btnTemplateNew = itemView.findViewById(R.id.template_new);
			btnTemplateSite = itemView.findViewById(R.id.template_site);
		}
	}

	@NonNull
	@NotNull
	@Override
	public TemplateListAdapter.TemplateListHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new TemplateListAdapter.TemplateListHolder(inflater.inflate(R.layout.template_slot, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull TemplateListAdapter.TemplateListHolder holder, int position) {
		JSONObject object = templates[position];
		holder.btnTemplateNew.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
		// TODO: Icons
		holder.btnTemplateNew.setOnClickListener(view -> templateInterface.newWiki(object.optString(MainActivity.DB_KEY_URI)));
		holder.btnTemplateSite.setOnClickListener(view -> templateInterface.nav(object.optString(TPL_KEY_HOMEPAGE)));
		SpannableStringBuilder builder = new SpannableStringBuilder(object.optString(MainActivity.KEY_NAME));
		builder.setSpan(new LeadingMarginSpan.Standard(Math.round(scale * 8f)), 0, builder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		try {
			builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.content_sub)), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
		builder.append('\n');
		builder.setSpan(new RelativeSizeSpan(0.5f), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
		builder.append(object.optString(MainActivity.TPL_KEY_VER)).append(c160).append(c160).append(c160).append(c160);
		builder.append(formatSize(object.optInt(MainActivity.TPL_KEY_SIZE))).append(c160).append(c160).append(c160).append(c160);
		long lu;
		if ((lu = object.optLong(MainActivity.TPL_KEY_LU)) != 0) builder.append(SimpleDateFormat.getDateInstance().format(new Date(lu)));
		String desc;
		if ((desc = object.optString(MainActivity.TPL_KEY_DESC)).length() > 0) {
			builder.append('\n');
			builder.setSpan(new RelativeSizeSpan(1.6f), builder.length(), builder.length(), Spanned.SPAN_MARK_POINT);
			builder.append(desc);
		}
		holder.btnTemplateNew.setText(builder);
		holder.itemView.setVisibility(templateInterface.isGranted(object) ? View.VISIBLE : View.GONE);
	}

	interface TemplateInterface {
		void newWiki(String uri);

		void nav(String uri);

		boolean isGranted(JSONObject o);
	}

	void setTemplateInterface(TemplateInterface templateInterface) {
		this.templateInterface = templateInterface;
	}

	@Override
	public int getItemCount() {
		return templates.length;
	}

	void init(JSONArray templates) {
		templates0 = templates;
		int l = templates.length();
		JSONObject a;
		JSONObject[] p = new JSONObject[l];
		int j = 0;
		for (int i = 0; i < l; i++) {
			if (templateInterface.isGranted(a = templates.optJSONObject(i))) {
				p[j] = a;
				j++;
			}
		}
		this.templates = new JSONObject[j];
		System.arraycopy(p, 0, this.templates, 0, j);
	}

	void reload() {
		int l = templates0.length();
		JSONObject a;
		JSONObject[] p = new JSONObject[l];
		int j = 0;
		for (int i = 0; i < l; i++) {
			if (templateInterface.isGranted(a = templates0.optJSONObject(i))) {
				p[j] = a;
				j++;
			}
		}
		this.templates = new JSONObject[j];
		System.arraycopy(p, 0, this.templates, 0, j);
	}

	// 格式化大小
	private String formatSize(long size) {
		if (size <= 0)
			return zeroB;
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat(PAT_SIZE).format(size / Math.pow(1024, digitGroups)) + c160 + units[digitGroups];
	}
}
