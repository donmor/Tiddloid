/*
 * top.donmor.tiddloid.KeepAliveService <= [P|Tiddloid]
 * Last modified: 01:12:49 2023/03/01
 * Copyright (c) 2023 donmor
 */

package top.donmor.tiddloid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class KeepAliveService extends Service {
    public KeepAliveService() {
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "top.donmor.tiddloid";

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建通知对象
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Keep Alive Channel",
                    NotificationManager.IMPORTANCE_MIN
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Tiddloid")
                .setContentText("Wiki await in background...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                // The notification's priority is set to PRIORITY_HIGH to satisfy the system's requirements for foreground service notifications.
                .setPriority(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        Log.d("TAG", "onCreate: startForeground");
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // keep the service running even if the app is killed
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}