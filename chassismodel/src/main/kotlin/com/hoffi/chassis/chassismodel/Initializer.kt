package com.hoffi.chassis.chassismodel

import com.squareup.kotlinpoet.CodeBlock

enum class ReplaceAppendOrModify { REPLACE, APPEND, MODIFY }

class Initializer private constructor(var originalFormat: String, val originalArgs: MutableList<Any> = mutableListOf()) {
    override fun toString() = if (hasNone()) "EMPTY" else "\"$originalFormat\"[\"$formatAddendum\"]->(${originalArgs.joinToString()})[${argsAddendum.joinToString()}] $replaceAppendOrModify"

    var replaceAppendOrModify: ReplaceAppendOrModify = ReplaceAppendOrModify.APPEND
    var formatAddendum: String = ""
    var argsAddendum: MutableList<Any> = mutableListOf()

    val format: String
        //get() = originalFormat + formatAddendum
        get() {
            return when (replaceAppendOrModify) {
                ReplaceAppendOrModify.REPLACE -> formatAddendum
                ReplaceAppendOrModify.APPEND -> originalFormat + formatAddendum
                ReplaceAppendOrModify.MODIFY -> originalFormat + formatAddendum
            }
        }
    val args: List<Any>
        //get() = (originalArgs + argsAddendum).toList()
        get() {
            return when (replaceAppendOrModify) {
                ReplaceAppendOrModify.REPLACE -> argsAddendum
                ReplaceAppendOrModify.APPEND -> (originalArgs + argsAddendum).toMutableList()
                ReplaceAppendOrModify.MODIFY -> (originalArgs + argsAddendum).toMutableList()
            }
        }

    fun hasOriginalInitializer() = if (replaceAppendOrModify == ReplaceAppendOrModify.REPLACE) formatAddendum.isNotBlank() else originalFormat.isNotBlank()
    fun hasInitializerAddendum() = formatAddendum.isNotBlank()
    fun hasNone() = originalFormat.isBlank() && formatAddendum.isBlank()
    fun codeBlockFull() = CodeBlock.of(format, *args.toTypedArray())
    fun codeBlockOriginal() = CodeBlock.of(originalFormat, *originalArgs.toTypedArray())
    fun codeBlockAddendum() = CodeBlock.of(formatAddendum, *argsAddendum.toTypedArray())

    fun copy(): Initializer {
        val initializer = Initializer(originalFormat, originalArgs)
        initializer.replaceAppendOrModify = replaceAppendOrModify
        initializer.formatAddendum = formatAddendum
        initializer.argsAddendum = argsAddendum
        return initializer
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Initializer) return false

        if (originalFormat != other.originalFormat) return false
        if (originalArgs != other.originalArgs) return false
        if (formatAddendum != other.formatAddendum) return false
        return argsAddendum == other.argsAddendum
    }

    override fun hashCode(): Int {
        var result = originalFormat.hashCode()
        result = 31 * result + originalArgs.hashCode()
        result = 31 * result + formatAddendum.hashCode()
        result = 31 * result + argsAddendum.hashCode()
        return result
    }

    companion object {
        val EMPTY: Initializer
            get() = Initializer("")
        val REFFED: Initializer
            get() = Initializer("")
        fun of(format: String, vararg args: Any) = Initializer(format, mutableListOf(*args))
        fun of(format: String, args: MutableList<Any>, replaceAppendOrModify: ReplaceAppendOrModify = ReplaceAppendOrModify.APPEND, formatAddendum: String = "", argsAddendum: MutableList<Any> = mutableListOf()): Initializer {
            return Initializer(format, args).also { it.replaceAppendOrModify = replaceAppendOrModify ; it.formatAddendum = formatAddendum ; it.argsAddendum = argsAddendum }
        }
    }
}
