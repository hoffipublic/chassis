package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType

data class Extends(
    val simpleName: String,
    var replaceSuperclass: Boolean = false,
    var replaceSuperInterfaces: Boolean  = false,
    var typeClassOrDslRef: EitherTypOrModelOrPoetType = EitherTypOrModelOrPoetType.NOTHING,
    val superInterfaces: MutableSet<EitherTypOrModelOrPoetType> = mutableSetOf(),
    // to determine if extends.typeClassOrDslRef is the default NOTHING (never has been touched in the DSL) or explicitly set to NOTHING
    var superclassHasBeenSet: Boolean = false
) {
    override fun toString() = "${Extends::class.simpleName}(${typeClassOrDslRef}, interfaces: '${superInterfaces.joinToString()}')"
}
