package com.manidigit.yadin.data.repository

import java.io.Writer
import java.util.ArrayDeque

/**
 * نویسنده سبک و جریانی JSON برای جلوگیری از بافر شدن کل درخت اشیاء در حافظه RAM.
 * بدون وابستگی به Android Framework تا در تست‌های JVM و محیط‌های اجرایی با حداکثر کارایی عمل کند.
 */
class JsonStreamWriter(private val writer: Writer, private val indent: String = "  ") {

    private enum class Scope {
        EMPTY_ARRAY,
        NONEMPTY_ARRAY,
        EMPTY_OBJECT,
        DANGLING_NAME,
        NONEMPTY_OBJECT
    }

    private val stack = ArrayDeque<Scope>()

    private fun newlineAndIndent() {
        if (indent.isNotEmpty()) {
            writer.write("\n")
            for (i in 0 until stack.size) {
                writer.write(indent)
            }
        }
    }

    private fun beforeValue() {
        if (stack.isEmpty()) return
        when (stack.peek()) {
            Scope.EMPTY_ARRAY -> {
                stack.pop()
                stack.push(Scope.NONEMPTY_ARRAY)
                newlineAndIndent()
            }
            Scope.NONEMPTY_ARRAY -> {
                writer.write(",")
                newlineAndIndent()
            }
            Scope.DANGLING_NAME -> {
                writer.write(if (indent.isNotEmpty()) ": " else ":")
                stack.pop()
                stack.push(Scope.NONEMPTY_OBJECT)
            }
            Scope.EMPTY_OBJECT, Scope.NONEMPTY_OBJECT -> {
                throw IllegalStateException("Nesting error: cannot write value directly into object without property name")
            }
            null -> {}
        }
    }

    fun beginObject(): JsonStreamWriter {
        beforeValue()
        stack.push(Scope.EMPTY_OBJECT)
        writer.write("{")
        return this
    }

    fun endObject(): JsonStreamWriter {
        if (stack.isEmpty()) throw IllegalStateException("Nesting error: stack is empty")
        val top = stack.pop()
        if (top != Scope.EMPTY_OBJECT && top != Scope.NONEMPTY_OBJECT) {
            throw IllegalStateException("Nesting error: not inside an object, found $top")
        }
        if (top == Scope.NONEMPTY_OBJECT) {
            newlineAndIndent()
        }
        writer.write("}")
        return this
    }

    fun beginArray(): JsonStreamWriter {
        beforeValue()
        stack.push(Scope.EMPTY_ARRAY)
        writer.write("[")
        return this
    }

    fun endArray(): JsonStreamWriter {
        if (stack.isEmpty()) throw IllegalStateException("Nesting error: stack is empty")
        val top = stack.pop()
        if (top != Scope.EMPTY_ARRAY && top != Scope.NONEMPTY_ARRAY) {
            throw IllegalStateException("Nesting error: not inside an array, found $top")
        }
        if (top == Scope.NONEMPTY_ARRAY) {
            newlineAndIndent()
        }
        writer.write("]")
        return this
    }

    fun name(name: String): JsonStreamWriter {
        if (stack.isEmpty()) throw IllegalStateException("Nesting error: stack is empty")
        val top = stack.peek()
        if (top == Scope.NONEMPTY_OBJECT) {
            writer.write(",")
        } else if (top != Scope.EMPTY_OBJECT) {
            throw IllegalStateException("Nesting error: cannot write property name in $top")
        }
        stack.pop()
        stack.push(Scope.DANGLING_NAME)
        newlineAndIndent()
        writeString(name)
        return this
    }

    fun value(value: String?): JsonStreamWriter {
        if (value == null) return nullValue()
        beforeValue()
        writeString(value)
        return this
    }

    fun value(value: Boolean?): JsonStreamWriter {
        if (value == null) return nullValue()
        beforeValue()
        writer.write(if (value) "true" else "false")
        return this
    }

    fun value(value: Long?): JsonStreamWriter {
        if (value == null) return nullValue()
        beforeValue()
        writer.write(value.toString())
        return this
    }

    fun value(value: Int?): JsonStreamWriter = value(value?.toLong())

    fun value(value: Double?): JsonStreamWriter {
        if (value == null) return nullValue()
        beforeValue()
        writer.write(value.toString())
        return this
    }

    fun value(value: Float?): JsonStreamWriter = value(value?.toDouble())

    fun nullValue(): JsonStreamWriter {
        beforeValue()
        writer.write("null")
        return this
    }

    fun flush() {
        writer.flush()
    }

    private fun writeString(s: String) {
        writer.write("\"")
        for (i in 0 until s.length) {
            val c = s[i]
            when (c) {
                '"' -> writer.write("\\\"")
                '\\' -> writer.write("\\\\")
                '\b' -> writer.write("\\b")
                '\u000C' -> writer.write("\\f")
                '\n' -> writer.write("\\n")
                '\r' -> writer.write("\\r")
                '\t' -> writer.write("\\t")
                else -> {
                    if (c.code in 0..0x1F) {
                        writer.write(String.format("\\u%04x", c.code))
                    } else {
                        writer.write(c.toString())
                    }
                }
            }
        }
        writer.write("\"")
    }
}
