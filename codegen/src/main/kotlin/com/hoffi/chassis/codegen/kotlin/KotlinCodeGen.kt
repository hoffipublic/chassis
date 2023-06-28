package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.EitherModel

class KotlinCodeGen(val genRun: GenRun) {
    fun codeGen(modelelement: DslRef.model.MODELELEMENT) {
        println("========================================================================================")
        println("     KotlinCodeGen(${genRun.runIdentifier}).codeGen(${modelelement})")
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
        for(model in genRun.genCtx.genModels.values.filterIsInstance<EitherModel.DtoModel>()) {
            println("$model -> ${model.modelSubElRef}")
        }
    }

    private fun codeGenTable() {
        TODO("Not yet implemented")
    }
}
