package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import org.slf4j.LoggerFactory

class KotlinCodeGen constructor(val codegenRun: GenRun) {
    val log = LoggerFactory.getLogger("modelgroup")
    private val genCtxWrapper = GenCtxWrapper(codegenRun.genCtx)

    fun codeGen(dslRefProto: IDslRef) {
        codeGenSpecificOrAll(dslRefProto)
        with(genCtxWrapper) {
            codeGenFillers()
        }
    }
    fun codeGenSpecificOrAll(dslRefProto: IDslRef) {
        with(genCtxWrapper) {
            println("========================================================================================")
            println("     KotlinCodeGen(${genCtx.genRun.runIdentifier}).codeGen(${dslRefProto.dslBlockName})")
            println("========================================================================================")
            WhensDslRef.whenModelOrModelSubelement(dslRefProto,
                isModelRef = {
                    codeGenSpecificOrAll(DslRef.dto.DTOPROTO)
                    codeGenSpecificOrAll(DslRef.table.TABLEPROTO)
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

    context(GenCtxWrapper)
    private fun codeGenFillers() {
        println("==================================")
        println("===  generate Fillers     ========")
        println("==================================")
        for (fillerData: FillerData in genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value } ?: mutableSetOf()) {
            if (fillerData.sourceDslRef is DslRef.table || fillerData.targetDslRef is DslRef.table) {
                kotlinGenCtx.buildFiller(MODELREFENUM.TABLE, fillerData)
            } else {
                kotlinGenCtx.buildFiller(MODELREFENUM.DTO, fillerData)
            }
        }
        while (genCtx.syntheticFillerDatas.isNotEmpty()) {
            val syntheticFillerData = genCtx.syntheticFillerDatas.removeFirst()
            if (syntheticFillerData.sourceDslRef is DslRef.table || syntheticFillerData.targetDslRef is DslRef.table) {
                kotlinGenCtx.buildFiller(MODELREFENUM.TABLE, syntheticFillerData)
            } else {
                println("current: ${syntheticFillerData}\n  to build: ${genCtx.syntheticFillerDatas.joinToString(separator = "\n  to build: ")}")
                kotlinGenCtx.buildFiller(MODELREFENUM.DTO, syntheticFillerData)
            }
        }

        println("==================================")
        println("===  write Fillers           =====")
        println("==================================")
        for(aKotlinFiller in kotlinGenCtx.allKotlinFillerClasses(MODELREFENUM.DTO)) {
            println("${this::class.simpleName}.${object{}.javaClass.enclosingMethod.name}() write Filler for $aKotlinFiller ") //-> ${model.modelSubElRef}")
            aKotlinFiller.generate()
        }
        for(aKotlinFiller in kotlinGenCtx.allKotlinFillerClasses(MODELREFENUM.TABLE)) {
            println("${this::class.simpleName}.${object{}.javaClass.enclosingMethod.name}() write Filler for $aKotlinFiller ") //-> ${model.modelSubElRef}")
            aKotlinFiller.generate()
        }
    }
}
