package com.hoffi.chassis.shared

import com.hoffi.chassis.shared.dsl.IDslRef
import com.squareup.kotlinpoet.TypeName

sealed class EitherTypeOrDslRef {
    abstract val isInterface: Boolean
    data class EitherKClass(val typeName: TypeName, override val isInterface: Boolean): EitherTypeOrDslRef()
    data class EitherDslRef(val dslRef: IDslRef): EitherTypeOrDslRef() {
        override val isInterface: Boolean
            get() {
                // TODO implement me!
                //val theModel = ctx[modelGenRef]
                return false
            }
    }
    class ExtendsNothing: EitherTypeOrDslRef() {
        override val isInterface = false
    }
    companion object {
        val NOTHING = ExtendsNothing()
    }
}
