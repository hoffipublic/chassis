package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.whens.WhensDslRef

context(GenCtxWrapper)
class KotlinCodeGen() {
    fun codeGen(dslRefProto: IDslRef) {
        println("========================================================================================")
        println("     KotlinCodeGen(${genCtx.genRun.runIdentifier}).codeGen(${dslRefProto.dslBlockName})")
        println("========================================================================================")
        WhensDslRef.whenModelOrModelSubelement(dslRefProto,
            isModelRef = {
                codeGen(DslRef.dto.DTOPROTO)
                codeGen(DslRef.table.TABLEPROTO)
            },
            isDtoRef = { codeGenDto() },
            isTableRef = { codeGenTable() }
        )
    }

    private fun codeGenDto() {
        for(model in genCtx.allGenModels.values.filterIsInstance<GenModel.DtoModel>()) {
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
