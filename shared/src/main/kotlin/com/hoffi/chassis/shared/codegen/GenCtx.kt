package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel

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
    val allGenModels = mutableMapOf<DslRef.IModelSubelement, GenModel>()
    fun genModel(modelSubelementRef: DslRef.IModelSubelement) = allGenModels[modelSubelementRef] ?: throw GenCtxException("GenCtx does not contain a subelement model for $modelSubelementRef")

    companion object {
        val NULL = GenCtx()
        fun _create(genRun: GenRun): GenCtx = GenCtx().also { it.genRun = genRun }
        fun _internal_create(): GenCtx = GenCtx()
    }
}
