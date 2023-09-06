package com.hoffi.chassis.codegen.kotlin

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelDto
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.shared.codegen.GenRun
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.shared.FK
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.hoffi.chassis.shared.whens.WhensDslRef
import org.slf4j.LoggerFactory

class KotlinCodeGen constructor(val codegenRun: GenRun) {
    val log = LoggerFactory.getLogger(javaClass)
    private val genCtxWrapper = GenCtxWrapper(codegenRun.genCtx)

    fun codeGen(dslRefProto: IDslRef) {
        codeGenSpecificOrAll(dslRefProto)
        with(genCtxWrapper) {
            codeGenCruds()
            writeCruds()
            codeGenFillers()
            writeFillers()
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
                isDtoRef = { codeGenDto() ; writeDtos() },
                isTableRef = { codeGenTable() ; writeTables() }
            )
        }
    }

    context(GenCtxWrapper)
    private fun codeGenDto() {
        println("==================================")
        println("===  generate \"DTOs\"     ========")
        println("==================================")
        for(model in genCtx.allGenModels().filterIsInstance<GenModel.DtoModel>()) {
            log.info("{}() for {} {}", object{}.javaClass.enclosingMethod.name, model, model.extends.values.firstOrNull{it.simpleName == "default"} ?: "extends NOTHING")
            val kcmDto = KotlinClassModelDto(model)
            kcmDto.build()
        }
    }
    context(GenCtxWrapper)
    private fun writeDtos() {
        println("==================================")
        println("===  write \"DTO\" classes     =====")
        println("==================================")
        for(kcmDto in kotlinGenCtx.allKotlinGenClasses()) {
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
        println("=============================================================")
        println("===  generate (Many2One) FKs and set otherEnd of FKs    =====")
        println("=============================================================")
        for(fk in kotlinGenCtx.allFKs()) {
            // set "otherEnd" FKs into kotlinGenClasses that yet did not know they are reffed:
            // set outgoingFKs into XTo1 kotlinGenClasses and incomingFKs into reffed 1To1 kotlinGenClasses
            when (fk.COLLECTIONTYP) {
                is COLLECTIONTYP.NONE -> {
                    val one2OneOtherEndKotlinGenClass =  kotlinGenCtx.kotlinGenClass(fk.toTableRef) as KotlinClassModelTable
                    one2OneOtherEndKotlinGenClass.addIncomingFK(fk.fromTableRef as DslRef.table, fk.toTableRef, fk.toProp, fk.COLLECTIONTYP)
                }
                is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
                    val many2OneKotlinGenClass = kotlinGenCtx.kotlinGenClass(fk.fromTableRef) as KotlinClassModelTable
                    many2OneKotlinGenClass.addOutgoingFK(FK(fk.fromTableRef as DslRef.table, fk.toTableRef, fk.toProp, fk.COLLECTIONTYP))
                    many2OneKotlinGenClass.buildMany2OneFK(fk)
                }
            }
        }
    }
    context(GenCtxWrapper)
    private fun writeTables() {
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
        val lambdaBuildFillerFun: (MODELKIND, FillerData) -> Unit = { modelkind: MODELKIND, fillerData: FillerData ->
            val (aKotlinClassFiller, _) = kotlinGenCtx.getOrCreateKotlinFillerClass(modelkind, fillerData)
            aKotlinClassFiller.build(modelkind, fillerData)
        }
        for (fillerData: FillerData in genCtx.fillerDatas[C.DEFAULT]?.flatMap { it.value } ?: mutableSetOf()) {
            if (fillerData.sourceDslRef is DslRef.table || fillerData.targetDslRef is DslRef.table) {
                lambdaBuildFillerFun(MODELKIND.TABLEKIND, fillerData)
            } else {
                lambdaBuildFillerFun(MODELKIND.DTOKIND, fillerData)
            }
        }
        println("==================================")
        println("===  generate synthetic FILLERs  ========")
        println("==================================")
        while (genCtx.allSyntheticFillerDatas().isNotEmpty()) {
            val syntheticFillerData = genCtx.allSyntheticFillerDatas().first().also { genCtx.allSyntheticFillerDatas().remove(it) }
            if (syntheticFillerData.sourceDslRef is DslRef.table || syntheticFillerData.targetDslRef is DslRef.table) {
                lambdaBuildFillerFun(MODELKIND.TABLEKIND, syntheticFillerData)
            } else {
                //println("current: ${syntheticFillerData}\n  to build: ${genCtx.syntheticFillerDatas.joinToString(separator = "\n  to build: ")}")
                lambdaBuildFillerFun(MODELKIND.DTOKIND, syntheticFillerData)
            }
        }
    }
    context(GenCtxWrapper)
    private fun writeFillers() {
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
for (crudData in genCtx.crudDatas[C.DEFAULT]?.flatMap { it.value }?.sortedWith(compareBy<CrudData> { it.businessName }.thenBy { it.targetDslRef.toString() }) ?: mutableSetOf()) {
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
        println("===  generate synthetic CRUDs       ========")
        println("==================================")
    println("\"synthetic\" CRUDs (${genCtx.allSyntheticCrudDatas().size}")
    for (crudData in genCtx.allSyntheticCrudDatas().sortedWith(compareBy<CrudData> { it.businessName }.thenBy { it.targetDslRef.toString() })) {
        println("   $crudData")
    }
        while (genCtx.allSyntheticCrudDatas().isNotEmpty()) {
            val syntheticCrudData = genCtx.allSyntheticCrudDatas().first().also { genCtx.allSyntheticCrudDatas().remove(it) }
            buildCrudExposedFun(syntheticCrudData)
        }

//        println("==================================")
//        println("===  generate synthetic FILLERs (FROM CRUDs)  ========")
//        println("==================================")
//        val buildFillerFun: (MODELKIND, FillerData) -> Unit = { modelkind: MODELKIND, fillerData: FillerData ->
//            val (aKotlinClassFiller, alreadyExisted) = kotlinGenCtx.getOrCreateKotlinFillerClassForSyntheticCrud(fillerData)
//            aKotlinClassFiller.build(modelkind, fillerData)
//        }
//        while (genCtx.syntheticFillerDatas.isNotEmpty()) {
//            val syntheticFillerData = genCtx.syntheticFillerDatas.removeFirst()
//            buildFillerFun(MODELKIND.TABLEKIND, syntheticFillerData)
//        }
    }
    context(GenCtxWrapper)
    private fun writeCruds() {
        println("==================================")
        println("===  write CRUDs           =====")
        println("==================================")
        for(aKotlinCrudExposed in kotlinGenCtx.allKotlinCrudClasses()) {
            if ((aKotlinCrudExposed.originalAHasCopyBoundrysData as CrudData).crud in listOf(CrudData.CRUD.DELETE, CrudData.CRUD.UPDATE)) {
                log.error("{}() skip writing anything else than CREATE|READ CRUD for {}", object {}.javaClass.enclosingMethod.name, aKotlinCrudExposed)
                continue
            }
            log.info("{}() write CRUD for {}", object{}.javaClass.enclosingMethod.name, aKotlinCrudExposed)
            aKotlinCrudExposed.generate()
        }
    }
}
