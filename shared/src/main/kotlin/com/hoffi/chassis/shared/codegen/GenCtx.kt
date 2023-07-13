package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData

class GenCtx private constructor() {
    lateinit var genRun: GenRun
    private val allGenModels: MutableMap<DslRef.IModelSubelement, GenModel> = mutableMapOf()
    fun genModel(modelSubelementRef: DslRef.IModelOrModelSubelement) = genModel(modelSubelementRef as DslRef.IModelSubelement)
    fun genModel(modelSubelementRef: DslRef.IModelSubelement) = allGenModels[modelSubelementRef] ?: throw GenCtxException("GenCtx does not contain a subelement model for $modelSubelementRef")
    fun putModel(modelSubelementRef: DslRef.IModelSubelement, genModel: GenModel) { if (! allGenModels.containsKey(modelSubelementRef)) { allGenModels[modelSubelementRef] = genModel } else { throw GenCtxException("genCtx already contains a GenModel for '${modelSubelementRef}'") } }
    fun allGenModels() = allGenModels.values

    val fillerDatas: MutableMap<String, MutableSet<FillerData>> = mutableMapOf()

    companion object {
        val NULL = GenCtx()
        fun _create(genRun: GenRun): GenCtx = GenCtx().also { it.genRun = genRun }
        fun _internal_create(): GenCtx = GenCtx()
    }
}
