package io.github.xjet.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

/** ImageView adapter for the framework ImageLoader target. */
class ImageViewTarget(val imageView: ImageView) : ImageTarget {
    override fun onLoadSuccess(bitmap: Bitmap) { imageView.setImageBitmap(bitmap) }
    override fun onLoadFailed(throwable: Throwable) { /* leave placeholder as-is */ }
}

/**
 * Dependency-free default [ImageLoaderProvider]: downloads a bitmap on the IO
 * dispatcher and posts the result on the main thread. Swap in Glide later via
 * `XJetConfig.imageLoader` or `XJet.override`.
 */
class AndroidImageLoaderProvider : ImageLoaderProvider {

    override fun load(url: String, target: ImageTarget) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { download(url) }
                target.onLoadSuccess(bitmap)
            } catch (t: Throwable) {
                target.onLoadFailed(t)
            }
        }
    }

    private fun download(url: String): Bitmap {
        val conn = URL(url).openConnection()
        val stream = conn.getInputStream() as InputStream
        return stream.use { BitmapFactory.decodeStream(it) }
    }
}
