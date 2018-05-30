package io.agora.agoravideodemo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.agora.agoravideodemo.R
import io.agora.agoravideodemo.base.BaseRtcActivity
import io.agora.agoravideodemo.model.ContactModel
import io.agora.agoravideodemo.syncadapter.SyncAdapterManager
import io.agora.agoravideodemo.ui.VideoChatViewActivity.CHAT_ROOM_KEY
import io.agora.agoravideodemo.utils.ContactsHelper
import io.agora.rtc.RtcEngine
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.ref.WeakReference


class MainActivity : BaseRtcActivity(), ContactsPullTask.ContactsPullTaskInteractionListener {

    override fun onCallEnded() {
        on_call_tv.visibility = if (isCallOnGoing) View.VISIBLE else View.GONE
    }

    override fun onRtcServiceConnected(rtcEngine: RtcEngine?) {
        on_call_tv.setOnClickListener { launchVideoChatActivity() }
        on_call_tv.visibility = if (isCallOnGoing) View.VISIBLE else View.GONE
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
        ContactsPullTask(WeakReference(this)).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, getLocalCountryCode())
    }

    override fun onContactPulled(list: MutableList<ContactModel>) {
        print(list.distinctBy { it.mobileNumber })
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
            R.id.action_settings -> {
                SyncAdapterManager.forceRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //https://stackoverflow.com/a/17266260/2102794
    private fun getLocalCountryCode(): String {
        val manager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (manager.simCountryIso != null) manager.simCountryIso.toUpperCase().trim() else "ZZ"
    }
}

class ContactsPullTask(private val weakActivity: WeakReference<Activity>) : AsyncTask<String, Void, MutableList<ContactModel>>() {
    override fun onPreExecute() {
        super.onPreExecute()
        if (weakActivity.get() !is ContactsPullTaskInteractionListener)
            throw RuntimeException("Activity must implement ContactsPullTaskInteractionListener")
    }

    public override fun doInBackground(vararg params: String): MutableList<ContactModel> {
        //IMPORTANT to pass applicationContext
        return ContactsHelper.getAllLocalContacts2(weakActivity.get()?.applicationContext, params[0])
    }

    public override fun onPostExecute(result: MutableList<ContactModel>) {
        val activity = weakActivity.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            // activity is no longer valid, don't do anything!
            Log.d("ContactsPullTask", "onPostExecute error  activity is not available")
            return
        }
        // The activity is still valid, do main-thread stuff here
        if (result.isNotEmpty()) (weakActivity.get() as ContactsPullTaskInteractionListener).onContactPulled(result)
        else Log.d("ContactsPullTask", "onPostExecute error result is empty")
    }

    interface ContactsPullTaskInteractionListener {
        fun onContactPulled(list: MutableList<ContactModel>)
    }
}


