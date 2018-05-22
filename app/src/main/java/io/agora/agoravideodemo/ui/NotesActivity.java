package io.agora.agoravideodemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.agora.agoravideodemo.R;
import io.agora.agoravideodemo.base.BaseRtcActivity;
import io.agora.rtc.RtcEngine;

public class NotesActivity extends BaseRtcActivity {

    private TextView mOnCallTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        mOnCallTv = findViewById(R.id.on_call_tv);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.notes_toolbar_title));
    }

    @Override
    public void onRtcServiceConnected(RtcEngine rtcEngine) {
        mOnCallTv.setVisibility(isCallOnGoing() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCallEnded() {
        mOnCallTv.setVisibility(isCallOnGoing() ? View.VISIBLE : View.GONE);
    }

    public void tapToReturnClicked(View view) {
        finishIfCallIsOnging();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                String notes = ((EditText) findViewById(R.id.notes_et)).getText().toString();
                //TODO save the notes before finish this activity
                finishIfCallIsOnging();
                break;
            case android.R.id.home:
                finishIfCallIsOnging();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void finishIfCallIsOnging() {
        if (isCallOnGoing()) {
            finish();
            startActivity(new Intent(this, VideoChatViewActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

}
