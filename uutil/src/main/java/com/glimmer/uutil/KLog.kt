package com.glimmer.uutil

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.LogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object KLog {
    private var loggable: Boolean = true
    private var logTag = "Glimmer"

    init {
        buildLog()
    }

    fun logTag(tag: String): KLog {
        logTag = tag
        return this
    }

    fun loggable(enable: Boolean): KLog {
        loggable = enable
        return this
    }

    fun buildLog() {
        Logger.clearLogAdapters()
        Logger.addLogAdapter(
            object : AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(false)
                    .methodCount(0)
                    .tag(logTag)
                    .build()
            ) {
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return loggable
                }
            }
        )
    }

    /*===================================================*/
    fun addLogAdapter(@NonNull adapter: LogAdapter) {
        checkLoggable {
            Logger.addLogAdapter(adapter)
        }
    }

    fun log(priority: Int, @Nullable tag: String?, @Nullable message: String?, @Nullable throwable: Throwable?) {
        checkLoggable {
            Logger.log(priority, tag, message, throwable)
        }
    }

    fun d(@NonNull message: String, @Nullable vararg args: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).d(message, *args)
        }
    }

    fun d(@Nullable `object`: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).d(`object`)
        }
    }

    fun e(@Nullable throwable: Throwable? = null, @NonNull message: String, @Nullable vararg args: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).e(throwable, message, *args)
        }
    }

    fun i(@NonNull message: String, @Nullable vararg args: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).i(message, *args)
        }
    }

    fun v(@NonNull message: String, @Nullable vararg args: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).v(message, *args)
        }
    }

    fun w(@NonNull message: String, @Nullable vararg args: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).w(message, *args)
        }
    }

    fun wtf(@NonNull message: String, @Nullable vararg args: Any?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).wtf(message, *args)
        }
    }

    fun json(@Nullable json: String?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).json(json)
        }
    }

    fun xml(@Nullable xml: String?, tag: String? = null) {
        checkLoggable {
            Logger.t(tag).xml(xml)
        }
    }

    private fun checkLoggable(block: () -> Unit) {
        if (loggable) {
            block.invoke()
        }
    }
}