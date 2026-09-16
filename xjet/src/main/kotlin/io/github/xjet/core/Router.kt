package io.github.xjet.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Chainable intent builder mirroring XDroid's Router ergonomics, but exposed
 * through the single XJet entry point: `XJet.open(Target::class.java) { ... }`.
 */
class RouterBuilder {

    private val extras = Bundle()
    private var flags = 0
    private var requestCode = -1
    private var enterAnim = 0
    private var exitAnim = 0

    fun putString(key: String, value: String) = apply { extras.putString(key, value) }
    fun putInt(key: String, value: Int) = apply { extras.putInt(key, value) }
    fun putLong(key: String, value: Long) = apply { extras.putLong(key, value) }
    fun putBoolean(key: String, value: Boolean) = apply { extras.putBoolean(key, value) }
    fun putParcelable(key: String, value: Parcelable?) = apply { extras.putParcelable(key, value) }
    fun putSerializable(key: String, value: Serializable?) = apply { extras.putSerializable(key, value) }
    fun putExtras(bundle: Bundle) = apply { extras.putAll(bundle) }
    fun addFlags(flag: Int) = apply { flags = flags or flag }
    fun requestCode(code: Int) = apply { requestCode = code }
    fun anim(enter: Int, exit: Int) = apply { enterAnim = enter; exitAnim = exit }

    fun intent(context: Context, target: Class<out Activity>): Intent =
        Intent(context, target).apply {
            putExtras(this@RouterBuilder.extras)
            addFlags(flags)
        }

    fun launch(context: Context, target: Class<out Activity>) {
        val intent = intent(context, target)
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (requestCode >= 0 && context is Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            context.startActivity(intent)
        }
        if (enterAnim != 0 && exitAnim != 0 && context is Activity) {
            context.overridePendingTransition(enterAnim, exitAnim)
        }
    }
}

/** Chainable router through the single entry point. */
fun XJet.open(
    target: Class<out Activity>,
    context: Context = appContext ?: throw IllegalStateException("XJet.init(...) must be called first."),
    block: RouterBuilder.() -> Unit = {},
) {
    RouterBuilder().apply(block).launch(context, target)
}

