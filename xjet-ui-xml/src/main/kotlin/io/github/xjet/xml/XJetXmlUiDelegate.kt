package io.github.xjet.xml

import android.app.Activity
import android.widget.Toast
import io.github.xjet.core.UiDelegate

/** Default XML UiDelegate with Toast + simple progress. */
class XJetXmlUiDelegate(private val activity: Activity) : UiDelegate {

    override fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        // Apps commonly swap in their own progress indicator via UiDelegate.
    }

    override fun hideLoading() {
    }
}
