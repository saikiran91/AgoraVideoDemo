package io.agora.agoravideodemo;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import io.agora.agoravideodemo.utils.NotificationHelper;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class RtcService extends Service {
    private static final String TAG = "RtcService";
    private NotificationHelper mNotificationHelper;


    enum IntentAction {FIRST_REMOTE_VIDEO_DECODED, USER_OFFLINE, USER_MUTE_VIDEO, RTC_ERROR}

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private RtcEngine mRtcEngine;


    public RtcService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, RtcService.class));
        initializeAgoraEngine();

        mNotificationHelper = new NotificationHelper(this);
    }

    public void stopRtcService() {
        RtcEngine.destroy();
        mRtcEngine = null;
        stopForeground(true);
    }

    public void joinChannelRequested() {
        Notification notification = mNotificationHelper.getNotification1("OnGoing Call", "Tap to return").build();
        mNotificationHelper.notify(100, notification);
        this.startForeground(100, notification);
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, "Need to check rtc sdk init fatal error", e);
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            saveLastRemoteUserID(uid, RtcService.this);
            Intent intent = new Intent(IntentAction.FIRST_REMOTE_VIDEO_DECODED.name());
            intent.putExtra("uid", uid);
            sendRtcEngineEvenMessage(intent);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Intent intent = new Intent(IntentAction.USER_OFFLINE.name());
            intent.putExtra("uid", uid);
            sendRtcEngineEvenMessage(intent);
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            Intent intent = new Intent(IntentAction.USER_MUTE_VIDEO.name());
            intent.putExtra("uid", uid);
            intent.putExtra("muted", muted);
            sendRtcEngineEvenMessage(intent);
        }

        @Override
        public void onError(int err) {
            Intent intent = new Intent(IntentAction.RTC_ERROR.name());
            super.onError(err);
            intent.putExtra("err", err);
            sendRtcEngineEvenMessage(intent);
        }
    };

    private void sendRtcEngineEvenMessage(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private Boolean isCallOngoing() {
        return true;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    @Override
    public void onDestroy() {
        stopRtcService();
        super.onDestroy();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        RtcService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RtcService.this;
        }
    }


    public static void saveLastRemoteUserID(int lastUserId, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("lastUserId", lastUserId).apply();
    }


    public static int getLastUserID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("lastUserId", 0);
    }
}
