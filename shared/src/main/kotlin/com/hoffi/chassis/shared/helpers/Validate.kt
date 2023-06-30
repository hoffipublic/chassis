package com.hoffi.chassis.shared.helpers

object Validate {
    val ILLEGAL_CHARACTERS_TO_ESCAPE = setOf('.', ';', '[', ']', '/', '<', '>', ':', '\\')

    fun String.failIfIdentifierInvalid(any: Any? = null) {
        require(!any { it in ILLEGAL_CHARACTERS_TO_ESCAPE }) {
            "Can't escape identifier $this because it contains illegal characters: " +
                    ILLEGAL_CHARACTERS_TO_ESCAPE.intersect(this.toSet()).joinToString("','", "'", "' ") +
                    (any?.toString() ?: "")
        }
    }
}
