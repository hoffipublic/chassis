package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.EitherTypeOrDslRef

data class Extends(
    val simpleName: String,
    var replaceSuperclass: Boolean = false,
    var replaceSuperInterfaces: Boolean  = false,
    var typeClassOrDslRef: EitherTypeOrDslRef = EitherTypeOrDslRef.NOTHING,
    val superInterfaces: MutableSet<EitherTypeOrDslRef> = mutableSetOf(),
    // to determine if extends.typeClassOrDslRef is the default NOTHING (never has been touched in the DSL) or explicitly set to NOTHING
    var superclassHasBeenSet: Boolean = false
) {
    override fun toString() = "${Extends::class.simpleName}(${typeClassOrDslRef}, interfaces: '${superInterfaces.joinToString()}')"
}
