package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.EitherModel

context(GenCtxWrapper)
class KotlinCodeGen() {
    fun codeGen(modelelement: DslRef.model.MODELELEMENT) {
        println("========================================================================================")
        println("     KotlinCodeGen(${genCtx.genRun.runIdentifier}).codeGen(${modelelement})")
        println("========================================================================================")
        when (modelelement) {
            DslRef.model.MODELELEMENT.MODEL -> {
                codeGen(DslRef.model.MODELELEMENT.DTO)
                codeGen(DslRef.model.MODELELEMENT.TABLE)
            }
            DslRef.model.MODELELEMENT.DTO -> codeGenDto()
            DslRef.model.MODELELEMENT.TABLE -> codeGenTable()
        }
    }

    private fun codeGenDto() {
        for(model in genCtx.genModels.values.filterIsInstance<EitherModel.DtoModel>()) {
            println("${this::class.simpleName}.${object{}.javaClass.enclosingMethod.name}() for $model ${model.extends.values.firstOrNull{it.simpleName == "default"} ?: "extends NOTHING"} ") //-> ${model.modelSubElRef}")
            val kcmDto = KotlinClassModelDto(model)
            kcmDto.build()
            kcmDto.generate()
        }
    }

    private fun codeGenTable() {
        TODO("Not yet implemented")
    }
}
