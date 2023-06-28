package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.EitherModel

class GenCtxWrapper(val genCtx: GenCtx) {
    override fun toString() = "${this::class.simpleName}(genCtx=$genCtx)"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is GenCtxWrapper -> genCtx == other.genCtx
            is GenCtx -> genCtx == other
            else -> false
        }
    }
    override fun hashCode() = genCtx.hashCode()
}
class GenCtx private constructor() {
    lateinit var genRun: GenRun
    val genModels = mutableMapOf<DslRef.IModelSubelement, EitherModel>()

    companion object {
        val NULL = GenCtx()
        fun _create(genRun: GenRun): GenCtx = GenCtx().also { it.genRun = genRun }
        fun _internal_create(): GenCtx = GenCtx()
    }
}
