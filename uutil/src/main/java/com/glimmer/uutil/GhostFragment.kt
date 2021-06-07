package com.glimmer.uutil

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

class GhostFragment : Fragment() {
    private var requestCode = -1
    private var intent: Intent? = null
    private var callback: ((result: Intent?) -> Unit)? = null

    fun init(requestCode: Int, intent: Intent, callback: ((result: Intent?) -> Unit)) {
        this.requestCode = requestCode
        this.intent = intent
        this.callback = callback
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //附加到Activity之后马上startActivityForResult
        intent?.let { startActivityForResult(it, requestCode) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //检查requestCode
        if (requestCode == this.requestCode) {
            //检查resultCode，如果不OK的话，那就直接回传个null
            val result = if (resultCode == Activity.RESULT_OK && data != null) data else null
            //执行回调
            callback?.let { it(result) }
        }
    }

    override fun onDetach() {
        super.onDetach()
        intent = null
        callback = null
    }
}