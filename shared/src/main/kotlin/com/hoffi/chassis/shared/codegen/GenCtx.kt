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
    private val allGenModels = mutableMapOf<DslRef.IModelSubelement, GenModel>()
    fun genModel(modelSubelementRef: DslRef.IModelOrModelSubelement) = genModel(modelSubelementRef as DslRef.IModelSubelement)
    fun genModel(modelSubelementRef: DslRef.IModelSubelement) = allGenModels[modelSubelementRef] ?: throw GenCtxException("GenCtx does not contain a subelement model for $modelSubelementRef")
    fun putModel(modelSubelementRef: DslRef.IModelSubelement, genModel: GenModel) { if (! allGenModels.containsKey(modelSubelementRef)) { allGenModels[modelSubelementRef] = genModel } else { throw GenCtxException("genCtx already contains a GenModel for '${modelSubelementRef}'") } }
    fun allGenModels() = allGenModels.values

    companion object {
        val NULL = GenCtx()
        fun _create(genRun: GenRun): GenCtx = GenCtx().also { it.genRun = genRun }
        fun _internal_create(): GenCtx = GenCtx()
    }
}
