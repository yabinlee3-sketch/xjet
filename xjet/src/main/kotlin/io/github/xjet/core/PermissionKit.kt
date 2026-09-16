package io.github.xjet.core

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.Activity

/**
 * Runtime permission helpers (AndroidX compatible). Together with
 * [Kits] this covers the permission gap XDroid solved with RxPermission.
 */
object PermissionKit {

    fun areGranted(context: Context, vararg permissions: String): Boolean =
        permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    fun missing(context: Context, vararg permissions: String): List<String> =
        permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }

    fun launch(activity: Activity, requestCode: Int, vararg permissions: String) {
        val missing = missing(activity, *permissions)
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toTypedArray(), requestCode)
        }
    }

    fun shouldShowRationale(activity: Activity, permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}
