package com.hoffi.chassis.shared.codegen

import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.SynthCrudData
import com.hoffi.chassis.shared.shared.SynthFillerData

class GenCtx private constructor() {
    lateinit var genRun: GenRun
    private val allGenModels: MutableMap<IDslRef, GenModel> = mutableMapOf()
    fun genModel(modelSubelementRef: IDslRef) = allGenModels[modelSubelementRef] ?: throw GenCtxException("GenCtx does not contain a subelement model for $modelSubelementRef")
    fun putModel(modelSubelementRef: IDslRef, genModel: GenModel) { if (! allGenModels.containsKey(modelSubelementRef)) { allGenModels[modelSubelementRef] = genModel } else { throw GenCtxException("genCtx already contains a GenModel for '${modelSubelementRef}'") } }
    fun allGenModels() = allGenModels.values

    val fillerDatas: MutableMap<String, MutableMap<DslRef.model, MutableSet<FillerData>>> = mutableMapOf()
    private val syntheticFillerDatas: MutableSet<SynthFillerData> = mutableSetOf()
    fun addSyntheticFillerData(synthFillerData: SynthFillerData) { syntheticFillerDatas.add(synthFillerData) }
    fun allSyntheticFillerDatas() = syntheticFillerDatas
    val crudDatas: MutableMap<String, MutableMap<DslRef.table, MutableSet<CrudData>>> = mutableMapOf()
    private val syntheticCrudDatas: MutableSet<SynthCrudData> = mutableSetOf()
    fun addSyntheticCrudData(synthCrudData: SynthCrudData) { syntheticCrudDatas.add(synthCrudData) }
    fun allSyntheticCrudDatas() = syntheticCrudDatas

    companion object {
        val NULL = GenCtx()
        fun _create(genRun: GenRun): GenCtx = GenCtx().also { it.genRun = genRun }
        fun _internal_create(): GenCtx = GenCtx()
    }
}
