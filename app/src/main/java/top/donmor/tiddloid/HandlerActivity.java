/*
 * top.donmor.tiddloid.HandlerActivity <= [P|Tiddloid]
 * Last modified: 16:17:21 2022/03/13
 * Copyright (c) 2022 donmor
 */

package top.donmor.tiddloid;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HandlerActivity extends AppCompatActivity {
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(getIntent().setClass(this, TWEditorWV.class));
		finish();
	}
}
