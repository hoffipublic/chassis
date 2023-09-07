package com.hoffi.chassis.shared.helpers

fun joinNonBlanksToStringBy(separator: CharSequence, vararg parts: Any?) = parts.joinNonBlanksToString(separator)
fun <T> Iterable<T>.joinNonBlanksToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    return filter{it.toString().isNotBlank()}.joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}
fun <T> Array<out T>.joinNonBlanksToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    return filter{it.toString().isNotBlank()}.joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

fun joinName(vararg parts: String?)    = parts.joinNonBlanksToString("")
fun joinPackage(vararg parts: String?) = parts.joinNonBlanksToString(".")

public inline fun <C : CharSequence> C.ifNotBlank(defaultValue: () -> C): C = if (isNotBlank()) defaultValue() else this

fun <T> MutableSet<T>.getOrAdd(element: T): T {
    return if (this.contains(element)) {
        this.find { it == element }!!
    } else {
        this.add(element)
        element
    }
}
