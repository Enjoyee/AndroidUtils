package com.glimmer.uutil

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

class KHandler(private val lifecycleOwner: LifecycleOwner, callback: Callback? = null, looper: Looper = Looper.getMainLooper()) : Handler(looper, callback), LifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        removeCallbacksAndMessages(null)
        lifecycleOwner.lifecycle.removeObserver(this)
    }

}