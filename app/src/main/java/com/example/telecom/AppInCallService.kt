package com.example.telecom

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.example.MainActivity

class AppInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        instance = this
        OngoingCallManager.onCallAdded(call)

        // Launch MainActivity so the calling screen is visible
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        OngoingCallManager.onCallRemoved(call)
        if (instance == this) {
            instance = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: AppInCallService? = null
    }
}
