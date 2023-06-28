package com.hoffi.chassis.chassismodel.helpers

fun String.underlined(underlineChar: Char = '=', postfix: String = "") = "$this\n${underlineChar.toString().repeat(this.length)}$postfix"
fun String.boxed(underlineChar: Char = '=', postfix: String = "") = "\n${underlineChar.toString().repeat(this.length)}\n$this\n${underlineChar.toString().repeat(this.length)}$postfix"
