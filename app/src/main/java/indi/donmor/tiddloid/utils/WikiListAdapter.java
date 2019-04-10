package indi.donmor.tiddloid.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;

import indi.donmor.tiddloid.MainActivity;
import indi.donmor.tiddloid.R;

public class WikiListAdapter extends RecyclerView.Adapter<WikiListAdapter.WikiListHolder> {

    Context context;
    JSONObject db;
    int count;
    private LayoutInflater inflater;

    Vibrator vibrator;

    public WikiListAdapter(Context context, JSONObject db) {
        this.context = context;
        this.db = db;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        try {
            count = db.getJSONArray("wiki").length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        inflater = LayoutInflater.from(context);
    }

    public class WikiListHolder extends RecyclerView.ViewHolder {
        private Button btnWiki;
        private CardView cvWiki;
        private String id,path;

        public WikiListHolder(View itemView) {
            super(itemView);
            btnWiki = (Button) itemView.findViewById(R.id.btnWiki);
            cvWiki = (CardView) itemView.findViewById(R.id.cvWiki);
        }
    }

    @Override
    public WikiListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WikiListHolder holder = new WikiListHolder(inflater.inflate(R.layout.wiki_slot, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WikiListHolder holder, final int position) {

        try {
            holder.btnWiki.setText(db.getJSONArray("wiki").getJSONObject(position).getString("name"));
            holder.id = db.getJSONArray("wiki").getJSONObject(position).getString("id");
            holder.btnWiki.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(position);
                }
            });
            holder.btnWiki.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    mItemClickListener.onItemClick(position);
                    vibrator.vibrate(20);
                    Toast.makeText(context, "e", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            holder.path = db.getJSONArray("wiki").getJSONObject(position).getString("path");
            File f = new File(holder.path);
            System.out.println(f.getAbsolutePath());
            if (f.exists()) holder.cvWiki.setVisibility(View.VISIBLE); else holder.cvWiki.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        public void onItemClick(int position);
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;

    }
    public void reload(JSONObject db) {
        this.db = db;
        try {
            count = this.db.getJSONArray("wiki").length();
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public String getId(int position) {
        String id = null;
        try {
            id = db.getJSONArray("wiki").getJSONObject(position).getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }
}
