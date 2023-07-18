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
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM

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

    private val allKotlinFillerClasses = mutableMapOf<MODELREFENUM, MutableMap<IDslRef, AKotlinFiller>>().also {
        for (modelref in MODELREFENUM.entries) {
            it[modelref] = mutableMapOf()
        }
    }
    context(GenCtxWrapper)
    fun kotlinFillerClass(modelrefenum: MODELREFENUM, fillerData: FillerData): Pair<AKotlinFiller, Boolean> {
        val theDslRef = if (modelrefenum == MODELREFENUM.TABLE && fillerData.targetDslRef !is DslRef.table) {
            fillerData.sourceDslRef
        } else {
            fillerData.targetDslRef
        }
        val exists = allKotlinFillerClasses[modelrefenum]!![theDslRef]
        return if (exists == null) {
            when (modelrefenum) {
                MODELREFENUM.MODEL -> throw GenCtxException("not allowed")
                MODELREFENUM.DTO   -> Pair(KotlinFillerDto(fillerData).also {allKotlinFillerClasses[modelrefenum]!![theDslRef] = it}, true)
                MODELREFENUM.TABLE -> Pair(KotlinFillerTable(fillerData).also {allKotlinFillerClasses[modelrefenum]!![theDslRef] = it}, true)
            }
        } else {
            Pair(exists, false)
        }
    }
    fun putKotlinFillerClass(modelrefenum: MODELREFENUM, fillerData: FillerData, fillerClass: AKotlinFiller) { if (! allKotlinFillerClasses[modelrefenum]!!.containsKey(fillerData.targetDslRef)) { allKotlinFillerClasses[modelrefenum]!![fillerData.targetDslRef] = fillerClass } else { throw GenCtxException("${this::class.simpleName} already contains a filler for '${fillerData}'") } }
    fun allKotlinFillerClasses(modelrefenum: MODELREFENUM) = allKotlinFillerClasses[modelrefenum]!!.values
    context(GenCtxWrapper)
    fun buildFiller(modelrefenum: MODELREFENUM, fillerData: FillerData) {
        val (aKotlinClassFiller, _) = kotlinFillerClass(modelrefenum, fillerData)
        aKotlinClassFiller.build(fillerData)
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
