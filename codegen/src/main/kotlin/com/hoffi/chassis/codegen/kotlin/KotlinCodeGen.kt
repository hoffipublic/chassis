package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
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
            log.info("{}() for {} {}", object{}.javaClass.enclosingMethod.name, model, model.extends.values.firstOrNull{it.simpleName == "default"} ?: "extends NOTHING")
            val kcmDto = KotlinClassModelDto(model)
            kcmDto.build()
            kcmDto.generate()
        }
    }

    context(GenCtxWrapper)
    private fun codeGenTable() {
        for(model in genCtx.allGenModels().filterIsInstance<GenModel.TableModel>()) {
            log.info("{}() for {} {} ", object{}.javaClass.enclosingMethod.name, model, model.extends.values.firstOrNull{it.simpleName == "default"} ?: "extends NOTHING")
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
            log.info("{}() write Table class for {}", object{}.javaClass.enclosingMethod.name, aKotlinClass)
            aKotlinClass.generate()
        }
    }

    context(GenCtxWrapper)
    private fun codeGenFillers() {
        println("==================================")
        println("===  generate Fillers     ========")
        println("==================================")
println("all Fillers (${genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value }?.size ?: 0}):")
println("\"normal\" fillers (${genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value }?.filter { it.targetDslRef !is DslRef.table && it.sourceDslRef !is DslRef.table }?.size ?: 0}):")
for (fillerData in genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value }?.filter { it.targetDslRef !is DslRef.table && it.sourceDslRef !is DslRef.table } ?: mutableSetOf()) {
    println("   $fillerData")
}
println("TABLE fillers (${genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value }?.filter { it.targetDslRef is DslRef.table || it.sourceDslRef is DslRef.table }?.size ?: 0}):")
for (fillerData in genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value }?.filter { it.targetDslRef is DslRef.table || it.sourceDslRef is DslRef.table } ?: mutableSetOf()) {
    println("   $fillerData")
}
        for (fillerData: FillerData in genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value } ?: mutableSetOf()) {
            if (fillerData.sourceDslRef is DslRef.table || fillerData.targetDslRef is DslRef.table) {
                kotlinGenCtx.buildFiller(MODELKIND.TABLEKIND, fillerData)
            } else {
                kotlinGenCtx.buildFiller(MODELKIND.DTOKIND, fillerData)
            }
        }
        while (genCtx.syntheticFillerDatas.isNotEmpty()) {
            val syntheticFillerData = genCtx.syntheticFillerDatas.removeFirst()
            if (syntheticFillerData.sourceDslRef is DslRef.table || syntheticFillerData.targetDslRef is DslRef.table) {
                kotlinGenCtx.buildFiller(MODELKIND.TABLEKIND, syntheticFillerData)
            } else {
                //println("current: ${syntheticFillerData}\n  to build: ${genCtx.syntheticFillerDatas.joinToString(separator = "\n  to build: ")}")
                kotlinGenCtx.buildFiller(MODELKIND.DTOKIND, syntheticFillerData)
            }
        }

        println("==================================")
        println("===  write Fillers           =====")
        println("==================================")
        for(aKotlinFiller in kotlinGenCtx.allKotlinFillerClasses(MODELKIND.DTOKIND)) {
            log.info("{}() write Filler for {}", object{}.javaClass.enclosingMethod.name, aKotlinFiller)
            aKotlinFiller.generate()
        }
        for(aKotlinFiller in kotlinGenCtx.allKotlinFillerClasses(MODELKIND.TABLEKIND)) {
            log.info("{}() write Filler for {}", object{}.javaClass.enclosingMethod.name, aKotlinFiller) //-> ${model.modelSubElRef}")
            aKotlinFiller.generate()
        }
    }
}
