package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.codegen.kotlin.gens.crud.AKotlinCrud
import com.hoffi.chassis.codegen.kotlin.gens.crud.KotlinCrudExposed
import com.hoffi.chassis.codegen.kotlin.gens.filler.AKotlinFiller
import com.hoffi.chassis.codegen.kotlin.gens.filler.KotlinFillerDto
import com.hoffi.chassis.codegen.kotlin.gens.filler.KotlinFillerTable
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.shared.FK
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.squareup.kotlinpoet.ClassName

/** wrapping the GenCtx (finished DSL-parsing result, bite-sized but _immutable for codegen_!!!)</br>
 * plus the codegen context(s), e.g. KotlinGenCtx</br>
 * wrapped to be conveniently accessible without having to qualify with "this@GenCtx.xyz" or "this@KotlinGenCtx.xyz"
 * in context(GenCtxWrapper) classes and functions */
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
/** specific to project codegen (kotlin) stuff (codegen subproject only write into this, not into GenCtx)</br>
 * -> just READ the "immutable" GenCtx (which is the finished DSL parsing result bite-sized for codegen) */
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
    private val allKotlinFillerClassNames: MutableSet<ClassName> = mutableSetOf()
    context(GenCtxWrapper)
    fun getOrCreateKotlinFillerClass(modelkind: MODELKIND, fillerData: FillerData): Pair<AKotlinFiller, Boolean> {
        val theDslRef = if (modelkind == MODELKIND.TABLEKIND && fillerData.targetDslRef !is DslRef.table) {
            fillerData.sourceDslRef // special case e.g. DTO <-- TABLE
        } else {
            fillerData.targetDslRef
        }
        val exists = allKotlinFillerClasses[modelkind]!![theDslRef]
        return if (exists == null) {
            when (modelkind) {
                MODELKIND.DTOKIND   -> {
                    val kfd = KotlinFillerDto(fillerData)
                    allKotlinFillerClasses[modelkind]!![theDslRef] = kfd
                    // double check
                    if ( ! allKotlinCrudClassNames.add(kfd.fillerPoetType) ) { throw GenCtxException("already did a ${kfd.fillerPoetType} for $fillerData") }
                    Pair(kfd, false)
                }
                MODELKIND.TABLEKIND -> {
                    val kft = KotlinFillerTable(fillerData)
                    allKotlinFillerClasses[modelkind]!![theDslRef] = kft
                    // double check
                    if ( ! allKotlinCrudClassNames.add(kft.fillerPoetType) ) { throw GenCtxException("already did a ${kft.fillerPoetType} for $fillerData") }
                    Pair(kft, false)
                }
            }
        } else {
            Pair(exists, true)
        }
    }
//    context(GenCtxWrapper)
//    fun getOrCreateKotlinFillerClassForSyntheticCrud(fillerData: FillerData): Pair<AKotlinFiller, Boolean> { // TODO remove?
//        val theDslRef = if (fillerData.targetDslRef is DslRef.table) {
//            fillerData.targetDslRef
//        } else {
//            fillerData.sourceDslRef // special case e.g. CRUD.READ
//        }
//        val exists = allKotlinFillerClasses[MODELKIND.TABLEKIND]!![theDslRef]
//        return if (exists == null) {
//                Pair(KotlinFillerTable(fillerData).also {allKotlinFillerClasses[MODELKIND.TABLEKIND]!![theDslRef] = it}, false)
//        } else {
//            Pair(exists, true)
//        }
//    }
    fun putKotlinFillerClass(modelkind: MODELKIND, fillerData: FillerData, fillerClass: AKotlinFiller) { if (! allKotlinFillerClasses[modelkind]!!.containsKey(fillerData.targetDslRef)) { allKotlinFillerClasses[modelkind]!![fillerData.targetDslRef] = fillerClass } else { throw GenCtxException("${this::class.simpleName} already contains a filler for '${fillerData}'") } }
    fun allKotlinFillerClasses(modelkind: MODELKIND) = allKotlinFillerClasses[modelkind]!!.values

    private val allKotlinCrudClasses: MutableMap<Pair<CrudData.CRUD, IDslRef>, AKotlinCrud> = mutableMapOf()
    private val allKotlinCrudClassNames: MutableSet<ClassName> = mutableSetOf()
    context(GenCtxWrapper)
    fun getOrCreateKotlinCrudExposedClass(crudData: CrudData): Pair<AKotlinCrud, Boolean> {
        //val theDslRef = if (modelkind == MODELKIND.TABLEKIND && crudData.targetDslRef !is DslRef.table) {
        //    crudData.sourceDslRef // special case e.g. DTO <-- TABLE
        //} else {
        //    crudData.targetDslRef
        //}
        val toGetOrCreate = Pair(crudData.crud, crudData.targetDslRef)
        val exists = allKotlinCrudClasses[toGetOrCreate]
        return if (exists == null) {
            val kce = KotlinCrudExposed(crudData)
            allKotlinCrudClasses[toGetOrCreate] = kce
            // double check
            if ( ! allKotlinCrudClassNames.add(kce.crudPoetType) ) {
                throw GenCtxException("already did a ${kce.crudPoetType} for $crudData")
            }
            Pair(kce, true)
        } else {
            Pair(exists, false)
        }
    }
    fun putKotlinCrudClass(crudData: CrudData, crudClass: AKotlinCrud) { if (! allKotlinCrudClasses.containsKey(Pair(crudData.crud, crudData.targetDslRef))) { allKotlinCrudClasses[Pair(crudData.crud, crudData.targetDslRef)] = crudClass } else { throw GenCtxException("${this::class.simpleName} already contains a crudClass for '${crudData}'") } }
    fun allKotlinCrudClasses() = allKotlinCrudClasses.values

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
