package com.hoffi.chassis.shared.dsl

import io.kotest.core.spec.style.FunSpec

class DslRefStringTest : FunSpec({
    data class D(val selfDslRef: DslRef, val refString: String)
    context("DslRefString.fromSelf()") {
//        withData<D>(
//            nameFn = { "${it.selfDslRef}" },
//            D(null, ""),
//            D(null, ""),
//        ) {
//
//        }
    }
})
