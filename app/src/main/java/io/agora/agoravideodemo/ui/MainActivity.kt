package io.agora.agoravideodemo.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.agora.agoravideodemo.R
import io.agora.agoravideodemo.base.BaseRtcActivity
import io.agora.agoravideodemo.ui.VideoChatViewActivity.CHAT_ROOM_KEY
import io.agora.rtc.RtcEngine
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseRtcActivity() {

    override fun onRtcServiceConnected(rtcEngine: RtcEngine?) {
        on_call_tv.setOnClickListener { launchVideoChatActivity() }
        if (rtcEngine != null && rtcEngine.callId != null) {
            on_call_tv.visibility = View.VISIBLE
        } else {
            on_call_tv.visibility = View.GONE
        }
    }

    private fun launchVideoChatActivity() {
        startActivity(Intent(this, VideoChatViewActivity::class.java)
                .apply { putExtra(CHAT_ROOM_KEY, "demo_1") })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        join_call.setOnClickListener { launchVideoChatActivity() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
