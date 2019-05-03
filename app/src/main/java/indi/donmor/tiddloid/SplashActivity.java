package indi.donmor.tiddloid;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class SplashActivity extends Activity {
	private static final int LOAD_DISPLAY_TIME = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		setContentView(R.layout.splash);
		TextView ver = findViewById(R.id.textVersionSplash);
		ver.setText(BuildConfig.VERSION_NAME);
		startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				SplashActivity.this.finish();
			}
		}, LOAD_DISPLAY_TIME);
	}
	public static class OnClearFromRecentService extends Service {

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.i("ClearFromRecentService", "Service Started");
			return START_NOT_STICKY;
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			Log.d("ClearFromRecentService", "Service Destroyed");
		}

		@Override
		public void onTaskRemoved(Intent rootIntent) {
			Log.i("ClearFromRecentService", "END");
			//Code here
			NotificationManagerCompat.from(this).cancelAll();
			stopSelf();
		}
	}
}
