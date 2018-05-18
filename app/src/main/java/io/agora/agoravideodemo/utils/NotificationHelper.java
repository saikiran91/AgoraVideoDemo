package io.agora.agoravideodemo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import io.agora.agoravideodemo.R;
import io.agora.agoravideodemo.RtcService;


/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;
    public static final String CALL_STATUS_CHANNEL = "CALL_STATUS_CHANNEL";

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx) {
        super(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getManager().getNotificationChannel(CALL_STATUS_CHANNEL) == null) {
            NotificationChannel chan1 = new NotificationChannel(CALL_STATUS_CHANNEL,
                    getString(R.string.noti_channel_call), NotificationManager.IMPORTANCE_LOW);
            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(chan1);
        }
    }

    /**
     * Get a notification of type 1
     * <p>
     * Provide the builder rather than the notification it's self as useful for making notification
     * changes.
     *
     * @param title the title of the notification
     * @param body  the body text for the notification
     * @return the builder as it keeps a reference to the notification (since API 24)
     */
    public NotificationCompat.Builder getNotification1(String title, String body) {

        Intent endCallIntent = new Intent(this, RtcService.class);
        endCallIntent.setAction(RtcService.ACTION_END_CALL);
        PendingIntent endCallPendingIntent =
                PendingIntent.getService(this, 0, endCallIntent, 0);

        return new NotificationCompat.Builder(getApplicationContext(), CALL_STATUS_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .addAction(0, "End Call", endCallPendingIntent)
                .setAutoCancel(true);
    }

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return android.R.drawable.stat_notify_chat;
    }

    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}