package io.github.xjet.core

import android.content.Context
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Everyday utilities matching what XDroid's Kits provided: dates, files,
 * random values and package info.
 */
object Kits {

    private val random = SecureRandom()

    fun randomString(length: Int = 8): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length).map { chars[random.nextInt(chars.size)] }.joinToString("")
    }

    fun randomInt(min: Int = 0, max: Int = Int.MAX_VALUE): Int =
        if (max <= min) min else min + random.nextInt(max - min + 1)

    fun isToday(millis: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val now = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }

    fun formatDate(millis: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    fun makeDirs(path: String): Boolean = File(path).mkdirs()
    fun isFileExist(path: String): Boolean = File(path).isFile
    fun isFolderExist(path: String): Boolean = File(path).isDirectory
    fun fileExists(path: String): Boolean = File(path).exists()
    fun fileSize(path: String): Long = File(path).takeIf { it.exists() }?.length() ?: -1L

    fun deleteRecursively(file: File?): Boolean = file?.deleteRecursively() ?: false

    fun packageName(context: Context): String = context.packageName
    fun versionName(context: Context): String? =
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName } catch (_: Exception) { null }
    fun versionCode(context: Context): Long =
        try { context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode } catch (_: Exception) { -1L }
}
