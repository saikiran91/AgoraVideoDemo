package io.agora.agoravideodemo.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import io.agora.agoravideodemo.RtcService;
import io.agora.rtc.RtcEngine;

/**
 * Created by saiki on 18-05-2018.
 **/
abstract public class BaseRtcActivity extends AppCompatActivity {
    RtcService mService;
    public boolean mBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, RtcService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            RtcService.LocalBinder binder = (RtcService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            onRtcServiceConnected(mService.getRtcEngine());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // This registers mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, getIntentFilter());
    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        for (RtcService.IntentAction action : RtcService.IntentAction.values())
            intentFilter.addAction(action.name());
        return intentFilter;
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action = intent.getAction();
            if (action == null) return;
            int uid;
            switch (RtcService.IntentAction.valueOf(action)) {
                case FIRST_REMOTE_VIDEO_DECODED:
                    uid = intent.getIntExtra("uid", 0);
                    setupRemoteVideo(uid);
                    break;
                case USER_OFFLINE:
                    onRemoteUserLeft();
                    break;
                case USER_MUTE_VIDEO:
                    uid = intent.getIntExtra("uid", 0);
                    Boolean muted = intent.getBooleanExtra("muted", false);
                    onRemoteUserVideoMuted(uid, muted);
                    break;
                case RTC_ERROR:
                    int error = intent.getIntExtra("err", 0);
                    onRtcError("Rtc Event error " + error + " Please try again.");
                    break;

                case CALL_ENDED:
                    onCallEnded();
                    break;
            }
        }
    };

    public RtcEngine getRtcEngine() {
        return mService.getRtcEngine();
    }

    abstract public void onRtcServiceConnected(RtcEngine rtcEngine);

    public void onCallEnded() {
        //Implement in client if required
    }

    public void onRemoteUserVideoMuted(int uid, boolean muted) {
        //Implement in client if required
    }

    public void onRemoteUserLeft() {
        //Implement in client if required
    }

    public void setupRemoteVideo(int uid) {
        //Implement in client if required
    }

    public void onRtcError(String error) {
        //Implement in client if required
    }

    final public void joinChannelRequested() {
        if (mBound) mService.joinChannelRequested();
    }

    final public void stopRtcService() {
        if (mBound) mService.stopRtcService();
    }
}
