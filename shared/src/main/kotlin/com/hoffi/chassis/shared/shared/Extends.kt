package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.EitherTypeOrDslRef

data class Extends(
    val simpleName: String,
    var replaceSuperclass: Boolean = false,
    var replaceSuperInterfaces: Boolean  = false,
    var typeClassOrDslRef: EitherTypeOrDslRef = EitherTypeOrDslRef.NOTHING,
    val superInterfaces: MutableSet<EitherTypeOrDslRef> = mutableSetOf()
) { override fun toString() = "${Extends::class.simpleName}(${typeClassOrDslRef}, interfaces: '${superInterfaces.joinToString()}')" }
