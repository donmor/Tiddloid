/*
 * top.donmor.tiddloid.SplashActivity <= [P|Tiddloid]
 * Last modified: 03:43:55 2019/05/07
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;

public class SplashActivity extends Activity {
	private static final int LOAD_DISPLAY_TIME = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		setContentView(R.layout.splash);
		TextView ver = findViewById(R.id.textVersionSplash);
		ver.setText(BuildConfig.VERSION_NAME);
		startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
		new Handler().postDelayed(() -> {
			Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
			SplashActivity.this.startActivity(mainIntent);
			SplashActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			SplashActivity.this.finish();
		}, LOAD_DISPLAY_TIME);
	}

	public static class OnClearFromRecentService extends Service {

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			return START_NOT_STICKY;
		}

		@Override
		public void onTaskRemoved(Intent rootIntent) {
			NotificationManagerCompat.from(this).cancelAll();
			stopSelf();
		}
	}
}
