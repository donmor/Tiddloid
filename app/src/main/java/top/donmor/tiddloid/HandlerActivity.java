/*
 * top.donmor.tiddloid.HandlerActivity <= [P|Tiddloid]
 * Last modified: 17:00:38 2022/03/13
 * Copyright (c) 2022 donmor
 */

package top.donmor.tiddloid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HandlerActivity extends AppCompatActivity {
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i;
		Uri u;
		String a;
		if ((i = getIntent()) == null || (u = i.getData()) == null || (a = i.getAction()) == null) {
			finish();
			return;
		}
		startActivity(new Intent(this, TWEditorWV.class).setData(u).setAction(a));
		finish();
	}
}
