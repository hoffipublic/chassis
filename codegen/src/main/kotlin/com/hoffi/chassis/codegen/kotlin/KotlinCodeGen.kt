package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.whens.WhensDslRef

class KotlinCodeGen constructor(val codegenRun: GenRun) {
    fun codeGen(dslRefProto: IDslRef) {
        with(GenCtxWrapper(codegenRun.genCtx)) {
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
    }

    context(GenCtxWrapper)
    private fun codeGenDto() {
        for(model in genCtx.allGenModels().filterIsInstance<GenModel.DtoModel>()) {
            println("${this::class.simpleName}.${object{}.javaClass.enclosingMethod.name}() for $model ${model.extends.values.firstOrNull{it.simpleName == "default"} ?: "extends NOTHING"} ") //-> ${model.modelSubElRef}")
            val kcmDto = KotlinClassModelDto(model)
            kcmDto.build()
            kcmDto.generate()
        }
    }

    context(GenCtxWrapper)
    private fun codeGenTable() {
        for(model in genCtx.allGenModels().filterIsInstance<GenModel.TableModel>()) {
            println("${this::class.simpleName}.${object{}.javaClass.enclosingMethod.name}() for $model ${model.extends.values.firstOrNull{it.simpleName == "default"} ?: "extends NOTHING"} ") //-> ${model.modelSubElRef}")
            val kcmTable = KotlinClassModelTable(model)
            kcmTable.build()
        }
        println("===========================")
        println("===  generate FKs     =====")
        println("===========================")
        for(fk in kotlinGenCtx.allFKs()) {
            fk.buildFK()
        }

        println("==================================")
        println("===  write Table classes     =====")
        println("==================================")
        for(aKotlinClass in kotlinGenCtx.allKotlinGenClasses().filterIsInstance<KotlinClassModelTable>()) {
            println("${this::class.simpleName}.${object{}.javaClass.enclosingMethod.name}() write Table class for ${aKotlinClass} ") //-> ${model.modelSubElRef}")
            aKotlinClass.generate()
        }
    }
}
