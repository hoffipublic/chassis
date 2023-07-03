package com.hoffi.chassis.shared.shared

import com.squareup.kotlinpoet.CodeBlock

data class Format private constructor(val format: String, val args: MutableList<Any> = mutableListOf()) {
    override fun toString() = "\"$format\"->${args.joinToString()}"
    constructor(format: String, arg: Any, vararg args: Any) : this(format, mutableListOf(arg, *args))
    fun isEmpty() = format.isBlank()
    companion object {
        val EMPTY = Format("")
        fun of(format: String, vararg args: Any) = Format(format, args)
    }

    //fun copy(format: String = this.format, args: MutableList<Any> = this.args) = Format(format, args)
}
data class Initializer private constructor(var format: String, val args: MutableList<Any> = mutableListOf()) {
    override fun toString() = "\"$format\"->(${args.joinToString()})"
    constructor(format: String, arg: Any, vararg args: Any) : this(format, mutableListOf(arg, *args))
    fun isEmpty() = format.isBlank()
    fun isNotEmpty() = format.isNotBlank()
    fun codeBlock() = CodeBlock.of(format, *args.toTypedArray())
    companion object {
        val EMPTY = Initializer("")
        val REFFED = Initializer("")
        fun of(format: String, vararg args: Any) = Initializer(format, mutableListOf(*args))
        fun of(format: String, args: List<Any>) = Initializer(format, args.toTypedArray())
    }
    //fun copy(format: String = this.format, args: MutableList<Any> = this.args) = Initializer(format, args)
}
