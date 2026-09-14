package com.example.telecom

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveCallInfo(
    val number: String,
    val callerName: String?,
    val state: Int, // e.g. Call.STATE_DIALING, Call.STATE_RINGING, Call.STATE_ACTIVE, Call.STATE_HOLDING, Call.STATE_DISCONNECTED
    val isIncoming: Boolean,
    val isMuted: Boolean,
    val isSpeakerOn: Boolean,
    val isHolding: Boolean,
    val elapsedSeconds: Long
)

object OngoingCallManager {
    private var currentCall: Call? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _callState = MutableStateFlow<ActiveCallInfo?>(null)
    val callState: StateFlow<ActiveCallInfo?> = _callState.asStateFlow()

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            updateCallState(call)
            if (state == Call.STATE_DISCONNECTED) {
                stopTimer()
                scope.launch {
                    delay(1500)
                    if (currentCall == call) {
                        currentCall = null
                        _callState.value = null
                    }
                }
            } else if (state == Call.STATE_ACTIVE) {
                startTimer()
            }
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            updateCallState(call)
        }
    }

    fun onCallAdded(call: Call) {
        currentCall?.unregisterCallback(callCallback)
        currentCall = call
        call.registerCallback(callCallback)
        updateCallState(call)
        if (call.state == Call.STATE_ACTIVE) {
            startTimer()
        }
    }

    fun onCallRemoved(call: Call) {
        if (currentCall == call) {
            stopTimer()
            currentCall?.unregisterCallback(callCallback)
            currentCall = null
            _callState.value = null
        }
    }

    fun answer() {
        currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun decline() {
        if (currentCall?.state == Call.STATE_RINGING) {
            currentCall?.reject(false, null)
        } else {
            currentCall?.disconnect()
        }
    }

    fun endCall() {
        currentCall?.disconnect()
    }

    fun toggleMute(service: InCallService?) {
        val call = currentCall ?: return
        val currentMute = service?.callAudioState?.isMuted ?: false
        service?.setMuted(!currentMute)
        updateCallState(call)
    }

    fun toggleSpeaker(service: InCallService?) {
        val call = currentCall ?: return
        val currentRoute = service?.callAudioState?.route ?: CallAudioState.ROUTE_EARPIECE
        val newRoute = if (currentRoute == CallAudioState.ROUTE_SPEAKER) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        service?.setAudioRoute(newRoute)
        updateCallState(call)
    }

    fun toggleHold() {
        val call = currentCall ?: return
        if (call.state == Call.STATE_HOLDING) {
            call.unhold()
        } else if (call.state == Call.STATE_ACTIVE) {
            call.hold()
        }
    }

    fun sendDtmf(c: Char) {
        currentCall?.playDtmfTone(c)
        scope.launch {
            delay(200)
            currentCall?.stopDtmfTone()
        }
    }

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = scope.launch {
            var elapsed = 0L
            while (true) {
                delay(1000)
                elapsed++
                _callState.value = _callState.value?.copy(elapsedSeconds = elapsed)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateCallState(call: Call) {
        val handle = call.details?.handle?.schemeSpecificPart ?: ""
        val callerName = call.details?.callerDisplayName
        val isIncoming = call.state == Call.STATE_RINGING
        val isMuted = call.details?.let { false } ?: false
        val isHolding = call.state == Call.STATE_HOLDING

        _callState.value = ActiveCallInfo(
            number = handle,
            callerName = callerName,
            state = call.state,
            isIncoming = isIncoming,
            isMuted = isMuted,
            isSpeakerOn = false,
            isHolding = isHolding,
            elapsedSeconds = _callState.value?.elapsedSeconds ?: 0L
        )
    }
}
