package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.codegen.kotlin.gens.FK
import com.hoffi.chassis.shared.codegen.GenCtx
import com.hoffi.chassis.shared.dsl.DslRef

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
    private val allKotlinGenClasses: MutableMap<DslRef.IModelSubelement, AKotlinClass> = mutableMapOf()
    fun kotlinGenClass(modelSubelementRef: DslRef.IModelOrModelSubelement) = kotlinGenClass(modelSubelementRef as DslRef.IModelSubelement)
    fun kotlinGenClass(modelSubelementRef: DslRef.IModelSubelement) = allKotlinGenClasses[modelSubelementRef] ?: throw GenCtxException("${this::class.simpleName} does not contain a subelement model for $modelSubelementRef")
    fun putKotlinGenClass(modelSubelementRef: DslRef.IModelSubelement, genModel: AKotlinClass) { if (! allKotlinGenClasses.containsKey(modelSubelementRef)) { allKotlinGenClasses[modelSubelementRef] = genModel } else { throw GenCtxException("${this::class.simpleName} already contains a GenModel for '${modelSubelementRef}'") } }
    fun allKotlinGenClasses() = allKotlinGenClasses.values

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
