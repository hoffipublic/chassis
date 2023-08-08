package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.CrudData
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
            codeGenCruds()
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
            val kotlinGenClass = kotlinGenCtx.kotlinGenClass(fk.fromTableRef) as KotlinClassModelTable
            kotlinGenClass.buildFK(fk)
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
        val buildFillerFun: (MODELKIND, FillerData) -> Unit = { modelkind: MODELKIND, fillerData: FillerData ->
            val (aKotlinClassFiller, _) = kotlinGenCtx.getOrCreateKotlinFillerClass(modelkind, fillerData)
            aKotlinClassFiller.build(modelkind, fillerData)
        }

        for (fillerData: FillerData in genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value } ?: mutableSetOf()) {
            if (fillerData.sourceDslRef is DslRef.table || fillerData.targetDslRef is DslRef.table) {
                buildFillerFun(MODELKIND.TABLEKIND, fillerData)
            } else {
                buildFillerFun(MODELKIND.DTOKIND, fillerData)
            }
        }
        while (genCtx.syntheticFillerDatas.isNotEmpty()) {
            val syntheticFillerData = genCtx.syntheticFillerDatas.removeFirst()
            if (syntheticFillerData.sourceDslRef is DslRef.table || syntheticFillerData.targetDslRef is DslRef.table) {
                buildFillerFun(MODELKIND.TABLEKIND, syntheticFillerData)
            } else {
                //println("current: ${syntheticFillerData}\n  to build: ${genCtx.syntheticFillerDatas.joinToString(separator = "\n  to build: ")}")
                buildFillerFun(MODELKIND.DTOKIND, syntheticFillerData)
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

    context(GenCtxWrapper)
    private fun codeGenCruds() {
        println("==================================")
        println("===  generate CRUDs       ========")
        println("==================================")
println("all CRUDs (${genCtx.crudDatas[C.DEFAULT]?.flatMap { it.value }?.size ?: 0}):")
println("\"normal\" CRUDs (${genCtx.crudDatas[C.DEFAULT]?.flatMap { it.value }?.size ?: 0}):")
for (crudData in genCtx.crudDatas[C.DEFAULT]?.flatMap { it.value } ?: mutableSetOf()) {
    println("   $crudData")
}
        val buildCrudExposedFun: (CrudData) -> Unit = { crudData: CrudData ->
            val (aKotlinCrudExposed, _) = kotlinGenCtx.getOrCreateKotlinCrudExposedClass(crudData)
            aKotlinCrudExposed.build(crudData)
        }

        for (crudData in genCtx.crudDatas[C.DEFAULT]?.flatMap { it.value } ?: mutableSetOf()) {
            buildCrudExposedFun(crudData)
        }

        println("==================================")
        println("===  write CRUDs           =====")
        println("==================================")
        for(aKotlinCrudExposed in kotlinGenCtx.allKotlinCrudClasses()) {
            log.info("{}() write CRUD for {}", object{}.javaClass.enclosingMethod.name, aKotlinCrudExposed)
            aKotlinCrudExposed.generate()
        }
    }
}
