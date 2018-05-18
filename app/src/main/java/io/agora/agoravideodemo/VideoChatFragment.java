package io.agora.agoravideodemo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import io.agora.agoravideodemo.utils.OnDragTouchListener;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class VideoChatFragment extends BaseRtcActivity {

    private static final String LOG_TAG = VideoChatFragment.class.getSimpleName();
    Timer mTimerFinal;
    Boolean one_time = false;

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private String chat_room_id;

    private RtcEngine mRtcEngine;// Tutorial Step 1

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            setupRemoteVideo(uid);
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            onRemoteUserVideoMuted(uid, muted);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.local_video_view_container).setOnTouchListener(new OnDragTouchListener(findViewById(R.id.local_video_view_container)));

        Intent intent = getIntent();
        String roomId = intent.getStringExtra("chat_room_id");
        chat_room_id = roomId != null ? roomId : "demo_1";

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        mTimerFinal = new java.util.Timer();
        mTimerFinal.scheduleAtFixedRate(new TimerTask() {
            private Handler updateUI = new Handler() {
                @Override
                public void dispatchMessage(Message msg) {
                    super.dispatchMessage(msg);
                    if (one_time) {
                        final Dialog dialog = new Dialog(VideoChatFragment.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_end_call);
                        dialog.show();
                        onRemoteTimeUp();
                        TextView textView = dialog.findViewById(R.id.text3);
                        Button btn_ok = dialog.findViewById(R.id.btn_ok);
                        textView.setText("You consult has gone beyond the allotted time. Please schedule another consult with your doctor on AfriDOKTA.");
                        btn_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    } else {
                        one_time = true;
                    }

                }
            };

            public void run() {
                try {
                    updateUI.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 15 * 60 * 1000);

    }

    private void initAgoraEngineAndJoinChannel() {
        //Make sure this activity is connected to service.
        if (mBound) {
            initializeAgoraEngine();     // Tutorial Step 1
            setupVideoProfile();         // Tutorial Step 2
            setupLocalVideo();           // Tutorial Step 3
            joinChannel();               // Tutorial Step 4
        } else {
            Log.e(LOG_TAG, "Not connected to service");
        }
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    // Tutorial Step 10
    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalVideoStream(iv.isSelected());

        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        surfaceView.setZOrderMediaOverlay(!iv.isSelected());
        surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
    }

    // Tutorial Step 9
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 8
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    // Tutorial Step 6
    public void onEncCallClicked(View view) {
        finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        mRtcEngine = getRtcEngine();
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_480P_6, false);
    }

    // Tutorial Step 3
    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    // Tutorial Step 4
    private void joinChannel() {
        mRtcEngine.joinChannel(null, "demo_1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 5
    @Override
    public void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);

        if (container.getChildCount() >= 1) {
            return;
        }

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));

        surfaceView.setTag(uid);

//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.GONE);
    }

    // Tutorial Step 6
    private void leaveChannel() {
        if (mRtcEngine != null)
            mRtcEngine.leaveChannel();
    }


    // Tutorial Step 7
    @Override
    public void onRemoteUserLeft() {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
        Toast.makeText(this, "Call End!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onRemoteTimeUp() {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
    }

    @Override
    public void onRtcServiceConnected(RtcEngine rtcEngine) {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel();
        }

    }

    // Tutorial Step 10
    @Override
    public void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);

        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);

        if (surfaceView != null) {
            Object tag = surfaceView.getTag();
            if (tag != null && (Integer) tag == uid) {
                surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
            }
        }
    }

}
