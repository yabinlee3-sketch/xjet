package io.github.xjet.core

import android.util.Log

/**
 * Lightweight structured logger mirroring the ergonomics XDroid users expect
 * (json / xml / throwable + leveled messages). Initialized automatically from
 * XJetConfig.debug and logTag during [XJet.init].
 */
object XJetLog {

    @Volatile var debug: Boolean = true
        private set
    @Volatile var tag: String = "XJet"
        private set

    fun init(debug: Boolean, tag: String) {
        this.debug = debug
        this.tag = tag
    }

    fun v(msg: String, vararg args: Any?) { if (debug) Log.v(tag, format(msg, args)) }
    fun d(msg: String, vararg args: Any?) { if (debug) Log.d(tag, format(msg, args)) }
    fun i(msg: String, vararg args: Any?) { if (debug) Log.i(tag, format(msg, args)) }
    fun w(msg: String, vararg args: Any?) { if (debug) Log.w(tag, format(msg, args)) }
    fun e(msg: String, vararg args: Any?) { if (debug) Log.e(tag, format(msg, args)) }

    fun e(throwable: Throwable) { if (debug) Log.e(tag, throwable.message ?: throwable.javaClass.simpleName, throwable) }

    fun json(text: String?) {
        if (!debug || text.isNullOrBlank()) return
        Log.d(tag, indentJson(text))
    }

    fun xml(text: String?) {
        if (!debug || text.isNullOrBlank()) return
        Log.d(tag, indentXml(text))
    }

    private fun format(msg: String, args: Array<out Any?>): String =
        if (args.isEmpty()) msg else msg.format(args)

    internal fun indentJson(json: String): String {
        val sb = StringBuilder()
        var depth = 0
        var inString = false
        var i = 0
        while (i < json.length) {
            val ch = json[i]
            if (ch == '"') {
                inString = !inString
                sb.append(ch)
            } else if (inString) {
                sb.append(ch)
            } else when (ch) {
                '{', '[' -> { newline(sb, depth++); sb.append(ch) }
                '}', ']' -> { newline(sb, --depth); sb.append(ch) }
                ',' -> sb.append(",\n").append(indent(depth))
                ':' -> sb.append(": ")
                else -> sb.append(ch)
            }
            i++
        }
        return sb.toString()
    }

    internal fun indentXml(xml: String): String {
        val sb = StringBuilder()
        var depth = 0
        var i = 0
        while (i < xml.length) {
            val ch = xml[i]
            sb.append(ch)
            if (ch == '<') {
                if (i + 1 < xml.length && xml[i + 1] != '/') {
                    val end = xml.indexOf('>', i)
                    if (end > i && !xml.substring(i + 1, end).endsWith("/")) depth++
                } else if (i + 1 < xml.length && xml[i + 1] == '/') {
                    depth--
                }
                if (ch == '<' && i + 1 < xml.length && xml[i + 1] != '/') sb.append("\n").append(indent(depth))
                else if (xml[i] == '<' && i + 1 < xml.length && xml[i + 1] == '/') sb.append("\n").append(indent(depth))
            } else if (ch == '>') {
                sb.append("\n").append(indent(depth))
            }
            i++
        }
        return sb.toString().trim()
    }

    private fun newline(sb: StringBuilder, after: Int) {
        sb.append("\n").append(indent(after))
    }
    private fun indent(depth: Int): String = "    ".repeat(depth.coerceAtLeast(0))
}

