/*
 * top.donmor.tiddloid.HandlerActivity <= [P|Tiddloid]
 * Last modified: 21:23:48 2024/02/14
 * Copyright (c) 2024 donmor
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
		String a, m;
		if ((i = getIntent()) == null || (a = i.getAction()) == null || ((u = i.getData()) == null && i.getExtras() == null)) {
			finishAfterTransition();
			return;
		}
		m = i.getType();
		startActivity(new Intent(this, TWEditorWV.class).setAction(a).setDataAndType(u, m).putExtras(i));
		finishAfterTransition();
	}
}
