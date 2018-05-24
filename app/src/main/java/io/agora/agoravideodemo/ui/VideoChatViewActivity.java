package io.agora.agoravideodemo.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import io.agora.agoravideodemo.R;
import io.agora.agoravideodemo.RtcService;
import io.agora.agoravideodemo.adapter.VideoViewAdapter;
import io.agora.agoravideodemo.base.BaseRtcActivity;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class VideoChatViewActivity extends BaseRtcActivity {

    private static final String LOG_TAG = VideoChatViewActivity.class.getSimpleName();
    public static final String CHAT_ROOM_KEY = "CHAT_ROOM_KEY";

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private RtcEngine mRtcEngine;// Tutorial Step 1
    private String mRoomId;

    private VideoViewAdapter mVideoViewAdapter = new VideoViewAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);

        String roomId = getIntent().getStringExtra(CHAT_ROOM_KEY);
        if (roomId != null) mRoomId = roomId;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupDragAndViewSwitching();

        RecyclerView mListView = findViewById(R.id.video_list);
        mListView.setAdapter(mVideoViewAdapter);

    }

    private void setupDragAndViewSwitching() {

      /*  View localVideoContainer = findViewById(R.id.local_video_view_container);
        localVideoContainer.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                switchVideoContainers();
            }
        });

        findViewById(R.id.container_tip).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
                findViewById(R.id.container_tip).setVisibility(View.GONE);
                return false;
            }
        });

        findViewById(R.id.container_tip).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (findViewById(R.id.container_tip) != null)
                    findViewById(R.id.container_tip).setVisibility(View.GONE);
            }
        }, 5000);*/
    }

    private void switchVideoContainers() {
       /* FrameLayout localContainer = findViewById(R.id.local_video_view_container);
        FrameLayout remoteContainer = findViewById(R.id.remote_video_view_container);

        SurfaceView surfaceView1 = (SurfaceView) localContainer.getChildAt(0);
        SurfaceView surfaceView2 = (SurfaceView) remoteContainer.getChildAt(0);

        localContainer.removeView(surfaceView1);
        if (surfaceView1 != null) {
            surfaceView1.setZOrderMediaOverlay(false);
            remoteContainer.addView(surfaceView1);
        }

        remoteContainer.removeView(surfaceView2);
        if (surfaceView2 != null) {
            surfaceView2.setZOrderMediaOverlay(true);
            localContainer.addView(surfaceView2);
        }
*/

    }

    private void initAgoraEngineAndJoinChannel() {
        //Make sure this activity is connected to service.
        if (mBound) {
            initializeAgoraEngine();     // Tutorial Step 1
            setupVideoProfile();         // Tutorial Step 2
            setupLocalVideo();           // Tutorial Step 3
            joinChannel();               // Tutorial Step 4

            findViewById(R.id.mute).performClick();
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    // Tutorial Step 10
    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalVideoStream(iv.isSelected());

        FrameLayout container = findViewById(R.id.large_video_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
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
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 8
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    // Tutorial Step 6
    public void onEncCallClicked(View view) {
        leaveChannel();
        finish();
    }

    public void onNotesClicked(View view) {
        finish();
        startActivity(new Intent(this, NotesActivity.class));
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        mRtcEngine = getRtcEngine();
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }

    // Tutorial Step 3
    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.large_video_container);
        SurfaceView localSurfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(localSurfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    // Tutorial Step 4
    private void joinChannel() {
        Log.d(LOG_TAG, "joinChannel mRtcEngine.getCallId() = " + mRtcEngine.getCallId());
        if (mRtcEngine.getCallId() == null) {
            mRtcEngine.joinChannel(null, mRoomId, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
        } else {
            int lastRemoteUserID = RtcService.getLastUserID(this);
            //lastRemoteUserID != -1 is to make sure it is not connected to non existing user
            if (lastRemoteUserID != -1) setupRemoteVideo(lastRemoteUserID);
        }
        joinChannelRequested();
    }

    // Tutorial Step 5
    @Override
    public void setupRemoteVideo(int uid) {
        SurfaceView remoteSurfaceView = RtcEngine.CreateRendererView(getBaseContext());
        remoteSurfaceView.setTag(uid); // for mark purpose
        boolean result = mVideoViewAdapter.addView(remoteSurfaceView, uid);
        if (result) {
            mRtcEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
            View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
            tipMsg.setVisibility(View.GONE);
        }
    }



    // Tutorial Step 6
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
        stopRtcService();
    }

    // Tutorial Step 7
    @Override
    public void onRemoteUserLeft(int uid) {
        boolean result = mVideoViewAdapter.removeView(uid);
        if (result && mVideoViewAdapter.getItemCount() == 0) {
            View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
            tipMsg.setVisibility(View.VISIBLE);
        }
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
        mVideoViewAdapter.onUserVideoMuted(uid, muted);
    }

    @Override
    public void onRtcError(String error) {
        super.onRtcError(error);
        Snackbar.make(findViewById(R.id.activity_video_chat_view), error, Snackbar.LENGTH_LONG)
                .setAction("OK", null).show();
    }

    @Override
    public void onCallEnded() {
        finish();
    }
}
