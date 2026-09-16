package io.github.xjet.core

import java.security.MessageDigest

/** Lightweight digests (MD5 / SHA-*) with hex output. */
object Codec {

    fun md5(data: ByteArray): String = digest("MD5", data)
    fun sha1(data: ByteArray): String = digest("SHA-1", data)
    fun sha256(data: ByteArray): String = digest("SHA-256", data)

    fun digest(algorithm: String, data: ByteArray): String {
        val bytes = MessageDigest.getInstance(algorithm).digest(data)
        return bytes.joinToString("") { String.format("%02x", it) }
    }
}
