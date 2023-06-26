package com.hoffi.chassis.shared

import com.hoffi.chassis.shared.dsl.IDslRef
import com.squareup.kotlinpoet.TypeName

sealed class EitherTypeOrDslRef {
    abstract val isInterface: Boolean
    data class EitherKClass(val typeName: TypeName, override val isInterface: Boolean): EitherTypeOrDslRef() {
        override fun toString() = "${this::class.simpleName} $typeName"
    }
    data class EitherDslRef(val dslRef: IDslRef): EitherTypeOrDslRef() {
        override fun toString() = "${this::class.simpleName} $dslRef"
        override val isInterface: Boolean
            get() {
                // TODO implement me!
                //val theModel = ctx[modelGenRef]
                return false
            }
    }
    class ExtendsNothing: EitherTypeOrDslRef() {
        override fun toString() = "${this::class.simpleName}"
        override val isInterface = false
    }
    companion object {
        val NOTHING = ExtendsNothing()
    }
}
