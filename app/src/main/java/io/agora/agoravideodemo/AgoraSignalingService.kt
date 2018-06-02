package io.agora.agoravideodemo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.GsonBuilder
import io.agora.AgoraAPIOnlySignal
import io.agora.IAgoraAPI
import io.agora.agoravideodemo.model.SignalMessage
import io.agora.agoravideodemo.model.SignalMessageAction
import io.agora.agoravideodemo.model.SignalSenderInfo
import io.agora.agoravideodemo.utils.BaseICallBack
import io.agora.agoravideodemo.utils.decodeFromBase64
import io.agora.agoravideodemo.utils.toBase64Encode
import timber.log.Timber

/**
 * Created by saiki on 02-06-2018.
 **/
class AgoraSignalingService : Service() {

    private val mSignal: AgoraAPIOnlySignal by lazy { initSignal() }
    private var mOnGoingCallUserID: String? = null
    private val mGson = GsonBuilder().setPrettyPrinting().create()


    override fun onCreate() {
        super.onCreate()
        val appID = getString(R.string.agora_app_id)
        mSignal.callbackSet(mCustomICallBack)
        mSignal.login2(appID, io.agora.agoravideodemo.model.UserInfo.userId, "_no_need_token", 0, "", 5, 2)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            when (SignalMessageAction.valueOf(intent.action)) {
                SignalMessageAction.MAKE_CALL -> {
                    mOnGoingCallUserID = intent.extras.getString(ON_GOING_USER_ID_KEY)
                    makeCall(mOnGoingCallUserID!!)
                }
                SignalMessageAction.END_CALL -> {
                    endCall(mOnGoingCallUserID!!)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun endCall(receiverUserId: String) {
        val message = SignalMessage(action = SignalMessageAction.END_CALL, senderInfo = SignalSenderInfo(), message = "")
        mSignal.messageInstantSend(receiverUserId, 0, mGson.toBase64Encode(message), "")
    }

    private fun makeCall(receiverUserId: String) {
        val message = SignalMessage(action = SignalMessageAction.MAKE_CALL, senderInfo = SignalSenderInfo(), message = "")
        mSignal.messageInstantSend(receiverUserId, 0, mGson.toBase64Encode(message), "")
    }

    private fun initSignal(): AgoraAPIOnlySignal {
        val appID = getString(R.string.agora_app_id)
        try {
            return AgoraAPIOnlySignal.getInstance(this, appID)
        } catch (e: Exception) {
            Timber.e(Log.getStackTraceString(e))
            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ON_GOING_USER_ID_KEY = "ON_GOING_USER_ID_KEY"
    }

    private val mCustomICallBack: IAgoraAPI.ICallBack = object : BaseICallBack {

        override fun onMessageSendError(messageID: String?, ecode: Int) {
            Timber.d("onMessageSendError messageID = %s and ecode = %s", messageID, ecode)
        }

        override fun onLoginSuccess(uid: Int, fd: Int) {
            Timber.d("onLoginSuccess uid = %s and fd = %s", uid, fd)
        }

        override fun onMessageSendSuccess(messageID: String?) {
            Timber.d("onMessageSendSuccess messageID = %s", messageID)

        }

        override fun onLoginFailed(ecode: Int) {
            Timber.d("onLoginFailed ecode = %s", ecode)
        }

        override fun onLogout(ecode: Int) {
            Timber.d("onLogout ecode = %s", ecode)
        }

        override fun onMessageInstantReceive(account: String?, uid: Int, msg: String?) {
            super.onMessageInstantReceive(account, uid, msg)
            Timber.d("onMessageInstantReceive account = %s , uid = %s , msg = %s", account, uid,
                    mGson.fromJson(msg!!.decodeFromBase64(), SignalMessage::class.java))
        }
    }
}