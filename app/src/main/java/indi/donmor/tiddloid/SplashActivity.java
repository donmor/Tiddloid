package indi.donmor.tiddloid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

//import net.danlew.android.joda.JodaTimeAndroid;
//
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import org.joda.time.LocalDateTime;
//import org.joda.time.format.DateTimeFormatter;
//
//import java.util.TimeZone;

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
//		JodaTimeAndroid.init(this);
//		String v = "20190413153145672";
//		DateTime e = new DateTime(Integer.parseInt(v.substring(0, 4)),
//				Integer.parseInt(v.substring(4, 6)),
//				Integer.parseInt(v.substring(6, 8)),
//				Integer.parseInt(v.substring(8, 10)),
//				Integer.parseInt(v.substring(10, 12)),
//				Integer.parseInt(v.substring(12, 14)),
//				Integer.parseInt(v.substring(14, 17)),DateTimeZone.UTC);
//		ver.setText(e.withZone(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				SplashActivity.this.finish();
			}
		}, LOAD_DISPLAY_TIME);
	}

}
