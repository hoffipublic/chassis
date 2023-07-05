package com.hoffi.chassis.dsl.internal

import com.hoffi.chassis.shared.dsl.IDslRef

//data class DslDiscriminatorWrapper(val dslDiscriminator: DslDiscriminator)

interface IDslParticipator

// https://youtrack.jetbrains.com/issue/KT-51881/Consider-proper-support-for-context-receivers-in-interfaces
// java.lang.ClassFormatError: Illegal field modifiers in class com/hoffi/chassis/dsl/internal/IDslClass: 0x1012
// context(DslCtxWrapper)
interface IDslClass : IDslParticipator {
    val selfDslRef: IDslRef
    val parentDslRef: IDslRef
        get() = selfDslRef.parentDslRef
    //val parent: IDslClass
    //    get() = dslCtx.ctxObjOrCreate(parentDslRef)

    //companion object {
    ////    context(DslCtxWrapper)
    //    object NULL : ADslClass() {
    //        override val parent = NULL
    //        override val selfDslRef = IDslRef.NULL
    //    }
    //}
    //// making sure DslClasses (finally) extend ADslClass and (normally) do NOT implement IDslClass directly
    //override fun equals(other: Any?): Boolean
    //override fun hashCode(): Int
    //override fun toString(): String
}
context(DslCtxWrapper)
abstract class ADslClass() : IDslClass {
    override fun toString() = "${this::class.simpleName}($selfDslRef)"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ADslClass) return false
        return selfDslRef == other.selfDslRef

    }
    override fun hashCode(): Int {
        // careful(!!!), same as its DslRef (but should be ok, if you only use them as e.g. Map/Set alone(!)
        // and not e.g. with a common supertype or compare a Pair<DslRef, DslClass> with a Pair<DslClass, DslRef>
        return selfDslRef.hashCode()
    }
}
context(DslCtxWrapper)
abstract class ADslDelegateClass(
    val simpleNameOfDelegator: String,
    val delegatorRef: IDslRef,
) : ADslClass() {
}
