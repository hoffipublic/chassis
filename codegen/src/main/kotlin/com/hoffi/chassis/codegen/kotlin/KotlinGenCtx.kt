package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.codegen.kotlin.gens.FK
import com.hoffi.chassis.codegen.kotlin.gens.filler.AKotlinFiller
import com.hoffi.chassis.codegen.kotlin.gens.filler.KotlinFillerDto
import com.hoffi.chassis.codegen.kotlin.gens.filler.KotlinFillerTable
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELKIND

class GenCtxWrapper(val genCtx: GenCtx) {
    override fun toString() = "${this::class.simpleName}(genCtx=$genCtx)"
    val kotlinGenCtx = KotlinGenCtx._create()

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
class KotlinGenCtx private constructor() {
    private val allKotlinGenClasses: MutableMap<IDslRef, AKotlinClass> = mutableMapOf()
    fun kotlinGenClass(modelSubelementRef: IDslRef) = allKotlinGenClasses[modelSubelementRef] ?: throw GenCtxException("${this::class.simpleName} does not contain a subelement model for $modelSubelementRef")
    fun putKotlinGenClass(modelSubelementRef: IDslRef, genModel: AKotlinClass) { if (! allKotlinGenClasses.containsKey(modelSubelementRef)) { allKotlinGenClasses[modelSubelementRef] = genModel } else { throw GenCtxException("${this::class.simpleName} already contains a GenModel for '${modelSubelementRef}'") } }
    fun allKotlinGenClasses() = allKotlinGenClasses.values

    private val allKotlinFillerClasses = mutableMapOf<MODELKIND, MutableMap<IDslRef, AKotlinFiller>>().also {
        for (modelref in MODELKIND.entries) {
            it[modelref] = mutableMapOf()
        }
    }
    context(GenCtxWrapper)
    fun kotlinFillerClass(modelkind: MODELKIND, fillerData: FillerData): Pair<AKotlinFiller, Boolean> {
        val theDslRef = if (modelkind == MODELKIND.TABLEKIND && fillerData.targetDslRef !is DslRef.table) {
            fillerData.sourceDslRef // special case e.g. DTO <-- TABLE
        } else {
            fillerData.targetDslRef
        }
        val exists = allKotlinFillerClasses[modelkind]!![theDslRef]
        return if (exists == null) {
            when (modelkind) {
                MODELKIND.DTOKIND   -> Pair(KotlinFillerDto(fillerData).also {allKotlinFillerClasses[modelkind]!![theDslRef] = it}, true)
                MODELKIND.TABLEKIND -> Pair(KotlinFillerTable(fillerData).also {allKotlinFillerClasses[modelkind]!![theDslRef] = it}, true)
            }
        } else {
            Pair(exists, false)
        }
    }
    fun putKotlinFillerClass(modelkind: MODELKIND, fillerData: FillerData, fillerClass: AKotlinFiller) { if (! allKotlinFillerClasses[modelkind]!!.containsKey(fillerData.targetDslRef)) { allKotlinFillerClasses[modelkind]!![fillerData.targetDslRef] = fillerClass } else { throw GenCtxException("${this::class.simpleName} already contains a filler for '${fillerData}'") } }
    fun allKotlinFillerClasses(modelkind: MODELKIND) = allKotlinFillerClasses[modelkind]!!.values
    context(GenCtxWrapper)
    fun buildFiller(modelkind: MODELKIND, fillerData: FillerData) {
        val (aKotlinClassFiller, _) = kotlinFillerClass(modelkind, fillerData)
        aKotlinClassFiller.build(modelkind, fillerData)
    }

    private val fks: MutableSet<FK> = mutableSetOf()
    fun allFKs() = fks
    fun addFK(fk: FK) {
        fks.add(fk)
    }

    companion object {
        val NULL = KotlinGenCtx()
        fun _create(): KotlinGenCtx = KotlinGenCtx().also { }
        fun _internal_create(): KotlinGenCtx = KotlinGenCtx()
    }

}
