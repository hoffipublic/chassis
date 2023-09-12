package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.PoetHelpers.addComment
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.GenDslRefHelpers
import com.hoffi.chassis.codegen.kotlin.GenNaming
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
import com.hoffi.chassis.codegen.kotlin.whens.WhensGen
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.SynthFillerData
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.*

context(GenCtxWrapper)
class KotlinFillerTable(fillerData: FillerData): AKotlinFiller(fillerData, MODELKIND.TABLEKIND) {

    override fun build(modelkind: MODELKIND, fillerData: FillerData) {
        currentFillerData = fillerData
        currentAHasCopyBoundryData = fillerData
        if (alreadyCreated(currentFillerData)) {
            if (currentFillerData !is SynthFillerData) log.warn("Already Created specific $modelkind, FILLER: $currentFillerData for $this")
            return
        }
        log.trace("build({}, {})", modelkind, currentFillerData)

        val targetGenModelFromDsl: GenModel = genCtx.genModelFromDsl(fillerData.targetDslRef)
        val sourceGenModelFromDsl: GenModel = genCtx.genModelFromDsl(fillerData.sourceDslRef)
        if (fillerData.targetDslRef is DslRef.table) {
            if (sourceGenModelFromDsl.isInterface || KModifier.ABSTRACT in sourceGenModelFromDsl.classModifiers) {
                throw GenException("something went wrong, trying to generate a TABLE filler from an abstract or interface DTOTYPE class for $currentFillerData")
            }
        } else if (targetGenModelFromDsl.isInterface || KModifier.ABSTRACT in targetGenModelFromDsl.classModifiers) {
                throw GenException("something went wrong, trying to generate a TABLE filler from an abstract or interface DTOTYPE class for $currentFillerData")
        }


        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            genCtx, targetGenModelFromDsl, sourceGenModelFromDsl,
            "", ""
        )

        intersectPropsData.sourceVarName = WhensDslRef.whenModelSubelement(sourceGenModelFromDsl.modelSubElRef,
            isDtoRef = { "source${intersectPropsData.sourceVarNamePostfix}" },
            isDcoRef = { "source${intersectPropsData.sourceVarNamePostfix}" },
            isTableRef = { "resultRow${intersectPropsData.sourceVarNamePostfix}" },
        )
        intersectPropsData.targetVarName = WhensDslRef.whenModelSubelement(targetGenModelFromDsl.modelSubElRef,
            isDtoRef = { "target${intersectPropsData.targetVarNamePostfix}" },
            isDcoRef = { "target${intersectPropsData.targetVarNamePostfix}" },
            isTableRef = { "resultRow${intersectPropsData.targetVarNamePostfix}" },
        )

        if (currentFillerData.targetDslRef !is DslRef.table) {
            // special case e.g. DTO <-- TABLE
            createFromTable(intersectPropsData)
        } else {
            // so for all these, targetGenModelFromDsl is a DslRef.table
            fillLambdas(intersectPropsData)
        }
        super.alreadyCreated = true
    }

    private fun fillLambdas(i: IntersectPropertys.CommonPropData) {
        //val kotlinGenClassTable: KotlinGenExposedTable = kotlinGenCtx.kotlinGenClass(i.targetGenModelFromDsl.modelSubElRef) as KotlinGenExposedTable
        //val outgoingFKs = kotlinGenClassTable.outgoingFKs

        log.trace("fillLambdas: -> from {}", currentFillerData)

        val returnInsertLambda = LambdaTypeName.get(i.targetPoetType, DB.InsertStatementTypeName(), returnType = UNIT)
        val returnBatchInsertLambda = LambdaTypeName.get(DB.BatchInsertStatementClassName, GenDslRefHelpers.nonpersistentClassName(i.targetGenModelFromDsl), returnType = UNIT)

        var funNameInsertOrBatch = funNameExpanded("fillShallowLambda", currentFillerData)
        var funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnInsertLambda)
        var body = insertShallowBody(i, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())
        /**
         * fun batchInsertShallowLambda(): BatchInsertStatement.(Dto) -> Unit
         */
        funNameInsertOrBatch = funNameExpanded("batchFillShallowLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .returns(returnBatchInsertLambda)
        body = insertShallowBody(i, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())
    }

    private fun insertShallowBody(i: IntersectPropertys.CommonPropData, funNameInsertOrBatch: FunName): CodeBlock {
        val bodyBuilder = CodeBlock.builder()
            .beginControlFlow("return {") // This will take care of the {} and indentations
        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
        var none = true
        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
                preFunc = { },
                preNonCollection = { },
                preCollection = { },
                isModel = { },
                isPoetType = {
                    none = false
                    propBoundry(prop,
                        noPropBoundry = {
                            bodyBuilder.addComment("TODO %L ${prop.propTypeSimpleNameCap} of %T", prop.name(), prop.poetType)
                        },
                        IGNORE = { copyBoundry -> bodyBuilder.addComment("${prop.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                        ELSE = { copyBoundry -> bodyBuilder.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                    )
                },
                isTyp = {
                    none = false
                    propBoundry(prop,
                        noPropBoundry = {
                            KotlinFillerTablePoetStatements.fillTablePropTypOrPoetType(bodyBuilder, funNameInsertOrBatch, i.targetPoetType, i.sourceVarName, prop)
                        },
                        IGNORE = { copyBoundry -> bodyBuilder.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                        ELSE = { copyBoundry -> bodyBuilder.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                    )
                },
                postNonCollection = { },
                isModelList = { },
                isModelSet = { },
                isModelCollection = { },
                isModelIterable = { },
                isPoetTypeList = { },
                isPoetTypeSet = { },
                isPoetTypeCollection = { },
                isPoetTypeIterable = { },
                isTypList = { },
                isTypSet = { },
                isTypCollection = { },
                isTypIterable = { },
                postCollection = { },
            )
        }
        if (none) bodyBuilder.addComment("NONE")
        var body = bodyBuilder.endControlFlow().build()
        return body
    }

    /** special case e.g. DTO <-- TABLE */
    private fun createFromTable(i: IntersectPropertys.CommonPropData) {
        val funName = GenNaming.createFromTableFunName(currentFillerData, i.targetGenModelFromDsl.modelClassName)
        log.trace("create(Dto)FromTable: -> {} from {}", funName, currentFillerData)
        val funSpec = FunSpec.builder(funName)
            .addParameter(i.sourceVarName, DB.ResultRowClassName)
            .returns(i.targetPoetType)
        //val targetSimpleEntityDto = SimpleEntityDto._internal_create()
        funSpec.addStatement("val %L = %T._internal_create()", i.targetVarName, i.targetPoetType)
        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
                preFunc = { },
                preNonCollection = { },
                preCollection = { },
                isModel = {
                    funSpec.addComment("%L.%L dealt with in sql.CRUD...", i.targetVarName, prop.name())
                    addSyntheticFillersForTableModelProp(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: $currentFillerData")
                },
                isPoetType = {
                    funSpec.addComment("TODO ${prop.name()} ${prop.propTypeSimpleNameCap} of %T", prop.poetType)
                },
                isTyp = {
                    propBoundry(prop,
                        noPropBoundry = {
                            funSpec.addStatement("%L.%L = %L[%T.%L]", i.targetVarName, prop.name(), i.sourceVarName, i.sourcePoetType, prop.name())
                        },
                        IGNORE = { copyBoundry -> funSpec.addComment("copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${prop.name()}") },
                        ELSE = { copyBoundry -> funSpec.addComment("TODO ${copyBoundry.copyType} ${prop.name()} ${prop.propTypeSimpleNameCap} of ${prop.poetType}") },
                    )
                },
                postNonCollection = { },
                isModelList = {
                    funSpec.addComment("%L.%L LIST of %T dealt with in sql.CRUD...", i.targetVarName, prop.name(), prop.poetType)
                },
                isModelSet = {
                    funSpec.addComment("%L.%L SET of %T dealt with in sql.CRUD...", i.targetVarName, prop.name(), prop.poetType)
                },
                isModelCollection = {
                    funSpec.addComment("%L.%L COLLECTION of %T dealt with in sql.CRUD...", i.targetVarName, prop.name(), prop.poetType)
                },
                isModelIterable = {
                    funSpec.addComment("%L.%L ITERABLE of %T dealt with in sql.CRUD...", i.targetVarName, prop.name(), prop.poetType)
                },
                isPoetTypeList = {
                    funSpec.addComment("not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isPoetTypeSet = {
                    funSpec.addComment("not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isPoetTypeCollection = {
                    funSpec.addComment("not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isPoetTypeIterable = {
                    funSpec.addComment("not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                isTypList = {
                    funSpec.addComment("not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isTypSet = {
                    funSpec.addComment("not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isTypCollection = {
                    funSpec.addComment("not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isTypIterable = {
                    funSpec.addComment("not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                postCollection = { },
            )
        }
        if (i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }.isEmpty()) funSpec.addComment("NONE")
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }
}
