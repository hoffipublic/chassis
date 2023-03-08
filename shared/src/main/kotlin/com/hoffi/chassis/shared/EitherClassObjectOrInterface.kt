package com.hoffi.chassis.shared

import com.squareup.kotlinpoet.KModifier

sealed class EitherClassObjectOrInterface(val kModifier: MutableList<KModifier>) {
    abstract fun classPrefix(): String
    abstract fun classPostfix(): String

    class EitherClass(kModifier: MutableList<KModifier> = mutableListOf()) : EitherClassObjectOrInterface(kModifier) {
        override fun classPrefix() = if (kModifier.contains(KModifier.ABSTRACT)) "A" else ""
        override fun classPostfix() = ""
    }
    class EitherObject(kModifier: MutableList<KModifier> = mutableListOf()) : EitherClassObjectOrInterface(kModifier) {
        override fun classPrefix() = if (kModifier.contains(KModifier.ABSTRACT)) "A" else ""
        override fun classPostfix() = ""
    }
    class EitherInterface(kModifier: MutableList<KModifier> = mutableListOf()) : EitherClassObjectOrInterface(kModifier) {
        override fun classPrefix() = if (kModifier.contains(KModifier.ABSTRACT)) "AI" else "I"
        override fun classPostfix() = ""
    }
}
