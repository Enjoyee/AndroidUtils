package com.glimmer.uutil

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.io.Serializable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.URLDecoder
import java.net.URLEncoder

/*========================Toast========================*/
private fun checkToastLooper(showBlock: () -> Unit) {
    if (Looper.getMainLooper() != Looper.myLooper()) {
        Looper.prepare()
    }
    showBlock.invoke()
    Looper.loop()
}

private fun Context.toast(msg: CharSequence?, duration: Int) {
    if (!msg.isNullOrEmpty()) {
        checkToastLooper {
            Toast.makeText(this.applicationContext, null, duration).apply {
                setText(msg)
                show()
            }
        }
    }
}

private fun Context.toast(resId: Int, duration: Int) {
    checkToastLooper {
        Toast.makeText(this.applicationContext, null, duration).apply {
            setText(resId)
            show()
        }
    }
}

fun Context.toastShort(msg: CharSequence?) {
    toast(msg, Toast.LENGTH_SHORT)
}

fun Context.toastShort(resId: Int) {
    toast(resId, Toast.LENGTH_SHORT)
}

fun Context.toastLong(msg: CharSequence?) {
    toast(msg, Toast.LENGTH_LONG)
}

fun Context.toastLong(resId: Int) {
    toast(resId, Toast.LENGTH_LONG)
}

fun Context.isDebug(): Boolean {
    return applicationContext.applicationInfo != null &&
            applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}

/**
 * 关闭输入键盘
 */
fun Context.closeInputKeyboard() {
    (this as? Activity)?.currentFocus?.let { focusView ->
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(focusView.windowToken, 0)
        currentFocus?.clearFocus()
    }
}

/**
 * 弹出输入法
 */
fun Context.showInputKeyBord(remindView: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    remindView.requestFocus()
    imm.showSoftInput(remindView, 0)
}

/*========================fragment========================*/
/**
 * 切换内容页
 */
private var mCurrentFragment: Fragment? = null
fun FragmentActivity.switchFragmentView(@IdRes flContainer: Int, switchFragment: Fragment) {
    val fragmentTransaction = supportFragmentManager.beginTransaction()
    if (mCurrentFragment != null) {
        if (switchFragment.isAdded) {
            fragmentTransaction.hide(mCurrentFragment!!).show(switchFragment)
        } else {
            fragmentTransaction.hide(mCurrentFragment!!).add(flContainer, switchFragment)
        }
        fragmentTransaction.commitAllowingStateLoss()
    } else {
        fragmentTransaction.replace(flContainer, switchFragment).commitAllowingStateLoss()
    }
    mCurrentFragment = switchFragment
}

/*========================startActivity========================*/
inline fun <reified target : Activity> AppCompatActivity.launchActivity(vararg params: Pair<String, Any>) {
    startActivity(Intent(this, target::class.java).putExtras(*params))
}

private var sRequestCode = 0
    set(value) {
        field = if (value >= Integer.MAX_VALUE) 1 else value
    }

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <reified target : Activity> AppCompatActivity.launchActivityForResult(
    starter: Activity,
    vararg params: Pair<String, Any>,
    crossinline callback: ((result: Intent?) -> Unit)
) {
    //初始化intent
    val intent = Intent(this, target::class.java).putExtras(*params)
    //无界面的Fragment
    val fragment = GhostFragment()
    fragment.init(++sRequestCode/*先让requestCode自增*/, intent) { result ->
        //包装一层：在回调执行完成之后把对应的Fragment移除掉
        callback(result)
        supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }
    //把Fragment添加进去
    supportFragmentManager.beginTransaction().add(fragment, GhostFragment::class.java.simpleName).commitAllowingStateLoss()
}

fun AppCompatActivity.finishOkResult(vararg params: Pair<String, Any>) = run {
    setResult(Activity.RESULT_OK, Intent().putExtras(*params))
    finish()
}

/*========================Intent========================*/
@Suppress("UNCHECKED_CAST")
fun Intent.putExtras(vararg params: Pair<String, Any>): Intent {
    if (params.isEmpty()) return this
    params.forEach { (key, value) ->
        when (value) {
            is Int -> putExtra(key, value)
            is Byte -> putExtra(key, value)
            is Char -> putExtra(key, value)
            is Long -> putExtra(key, value)
            is Float -> putExtra(key, value)
            is Short -> putExtra(key, value)
            is Double -> putExtra(key, value)
            is Boolean -> putExtra(key, value)
            is Bundle -> putExtra(key, value)
            is String -> putExtra(key, value)
            is IntArray -> putExtra(key, value)
            is ByteArray -> putExtra(key, value)
            is CharArray -> putExtra(key, value)
            is LongArray -> putExtra(key, value)
            is FloatArray -> putExtra(key, value)
            is Parcelable -> putExtra(key, value)
            is ShortArray -> putExtra(key, value)
            is DoubleArray -> putExtra(key, value)
            is BooleanArray -> putExtra(key, value)
            is CharSequence -> putExtra(key, value)
            is Array<*> -> {
                when {
                    value.isArrayOf<String>() ->
                        putExtra(key, value as Array<String?>)
                    value.isArrayOf<Parcelable>() ->
                        putExtra(key, value as Array<Parcelable?>)
                    value.isArrayOf<CharSequence>() ->
                        putExtra(key, value as Array<CharSequence?>)
                    else -> putExtra(key, value)
                }
            }
            is Serializable -> putExtra(key, value)
        }
    }
    return this
}

@SuppressLint("DiscouragedPrivateApi")
internal object IntentFieldMethod {
    lateinit var mExtras: Field
    lateinit var mMap: Field
    lateinit var unparcel: Method

    init {
        try {
            mExtras = Intent::class.java.getDeclaredField("mExtras")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMap = BaseBundle::class.java.getDeclaredField("mMap")
                unparcel = BaseBundle::class.java.getDeclaredMethod("unparcel")
            } else {
                mMap = Bundle::class.java.getDeclaredField("mMap")
                unparcel = Bundle::class.java.getDeclaredMethod("unparcel")
            }
            mExtras.isAccessible = true
            mMap.isAccessible = true
            unparcel.isAccessible = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <O> Intent.get(key: String): O? {
    try {
        //获取Intent.mExtras实例
        val extras = IntentFieldMethod.mExtras.get(this) as Bundle
        //调用unparcel方法来初始化mMap
        IntentFieldMethod.unparcel.invoke(extras)
        //获取Bundle.mMap实例
        val map = IntentFieldMethod.mMap.get(extras) as Map<*, *>
        //取出对应的key
        return map[key] as O
    } catch (e: Exception) {
        //Ignore
    }
    return null
}

/*========================资源========================*/
fun Context.getColorById(@ColorRes id: Int) = ContextCompat.getColor(this, id)

fun Context.getDrawableById(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

val Context.density: Float
    get() = resources.displayMetrics.density

/*========================encode/decode========================*/
fun String.encode(enc: String = "UTF-8"): String = URLEncoder.encode(this, enc)

fun String.decode(enc: String = "UTF-8"): String = URLDecoder.decode(this, enc)

/*========================TextView========================*/
fun TextView.str() = this.text.toString()

/*=======================================================*/
inline fun <T, R> T.doWithTry(block: (T) -> R) {
    try {
        block(this)
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

/*=======================================================*/
fun Any.logV(tag: String? = null) {
    KLog.v(message = this.toString(), tag = tag)
}

fun Any.logD(tag: String? = null) {
    KLog.d(message = this.toString(), tag = tag)
}

fun Any.logI(tag: String? = null) {
    KLog.i(message = this.toString(), tag = tag)
}

fun Any.logW(tag: String? = null) {
    KLog.w(message = this.toString(), tag = tag)
}

fun Any.logE(tag: String? = null) {
    KLog.e(message = this.toString(), tag = tag)
}

