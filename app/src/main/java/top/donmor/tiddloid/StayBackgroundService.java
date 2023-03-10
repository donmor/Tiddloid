/*
 * top.donmor.tiddloid.StayBackgroundService <= [P|Tiddloid]
 * Last modified: 01:12:49 2023/03/01
 * Copyright (c) 2023 donmor
 */

package top.donmor.tiddloid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class StayBackgroundService extends Service {
	public StayBackgroundService() {
	}

	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "top.donmor.tiddloid";
	private PendingIntent pendingIntent;

	@Override
	public void onCreate() {
		super.onCreate();

		Intent notificationIntent = new Intent(this, TWEditorWV.class);

		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		// 创建通知对象
		if (MainActivity.APIOver26) {
			NotificationChannel channel = new NotificationChannel(
					NOTIFICATION_CHANNEL_ID,
					getString(R.string.stay_in_background),
					NotificationManager.IMPORTANCE_MIN
			);
			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(channel);
		}
		pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				MainActivity.APIOver23 ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_NO_CREATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(intent != null ? intent.getStringExtra(MainActivity.KEY_NAME) : null)
				.setSmallIcon(R.drawable.ic_notification_tiddly_wiki)
				.setContentIntent(pendingIntent) // Add the intent to the notification
				.setPriority(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
				.setCategory(NotificationCompat.CATEGORY_SERVICE)
				.setOngoing(true)
				.build();
		startForeground(NOTIFICATION_ID, notification);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}