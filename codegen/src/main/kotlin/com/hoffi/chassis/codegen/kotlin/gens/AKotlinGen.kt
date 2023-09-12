package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl

abstract class AKotlinGen(val modelClassDataFromDsl: ModelClassDataFromDsl) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AKotlinGen) return false
        if (other::class != this::class) return false
        if (modelClassDataFromDsl != other.modelClassDataFromDsl) return false
        return true
    }
    override fun hashCode(): Int {
        return 31 * this::class.hashCode() + modelClassDataFromDsl.hashCode()
    }
}
