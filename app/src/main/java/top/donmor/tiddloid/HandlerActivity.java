/*
 * top.donmor.tiddloid.HandlerActivity <= [P|Tiddloid]
 * Last modified: 21:23:48 2024/02/14
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HandlerActivity extends AppCompatActivity {
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i;
		Bundle b;
		String a;
		if ((i = getIntent()) == null || (b = i.getExtras()) == null || b.getString(MainActivity.KEY_ID) == null || (a = i.getAction()) == null) {
			finish();
			return;
		}
		startActivity(new Intent(this, TWEditorWV.class).putExtras(b).setAction(a));
		finish();
	}
}
