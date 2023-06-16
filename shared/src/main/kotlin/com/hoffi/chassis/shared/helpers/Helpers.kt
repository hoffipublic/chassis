package com.hoffi.chassis.shared.helpers

fun joinName(vararg parts: String?)    = parts.filter { !it.isNullOrBlank() }.joinToString("")
fun joinPackage(vararg parts: String?) = parts.filter { !it.isNullOrBlank() }.joinToString(".")
