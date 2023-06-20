package com.hoffi.chassis.shared.helpers

fun joinName(vararg parts: String?)    = parts.filter { !it.isNullOrBlank() }.joinToString("")
fun joinPackage(vararg parts: String?) = parts.filter { !it.isNullOrBlank() }.joinToString(".")

public inline fun <C : CharSequence> C.ifNotBlank(defaultValue: () -> C): C = if (!isBlank()) defaultValue() else this
