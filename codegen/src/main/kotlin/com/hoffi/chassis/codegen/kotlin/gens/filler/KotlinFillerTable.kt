package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Cap
import com.hoffi.chassis.chassismodel.decap
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.GenDslRefHelpers
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.codegen.kotlin.whens.WhensGen
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.SynthFillerData
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.*

context(GenCtxWrapper)
class KotlinFillerTable(fillerData: FillerData): AKotlinFiller(fillerData, MODELKIND.TABLEKIND) {

    // TODO constructor fillerData has to construct the "right" "Filler" (might be Table filler even with target being a DTO
    // also important for build() alreadyCreated
    // move all Filler relevant data into intersectPropsData
    // add target and source FillerClassName into intersectPropsData

    override fun build(modelkind: MODELKIND, fillerData: FillerData) {
        currentFillerData = fillerData
        currentAHasCopyBoundryData = fillerData
        if (alreadyCreated(currentFillerData)) {
            if (currentFillerData !is SynthFillerData) log.warn("Already Created specific $modelkind, FILLER: $currentFillerData for $this")
            return
        }
        log.trace("build({}, {})", modelkind, currentFillerData)

        val targetGenModel: GenModel = genCtx.genModel(fillerData.targetDslRef)
        val sourceGenModel: GenModel = genCtx.genModel(fillerData.sourceDslRef)
        if (fillerData.targetDslRef is DslRef.table) {
            if (sourceGenModel.isInterface || KModifier.ABSTRACT in sourceGenModel.classModifiers) {
                throw GenException("something went wrong, trying to generate a TABLE filler from an abstract or interface DTOTYPE class for $currentFillerData")
            }
        } else if (targetGenModel.isInterface || KModifier.ABSTRACT in targetGenModel.classModifiers) {
                throw GenException("something went wrong, trying to generate a TABLE filler from an abstract or interface DTOTYPE class for $currentFillerData")
        }


        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            genCtx, targetGenModel, sourceGenModel,
            "", ""
        )

        intersectPropsData.sourceVarName = WhensDslRef.whenModelSubelement(sourceGenModel.modelSubElRef,
            isDtoRef = { "source${intersectPropsData.sourceVarNamePostfix}" },
            isTableRef = { "resultRow${intersectPropsData.sourceVarNamePostfix}" },
        )
        intersectPropsData.targetVarName = WhensDslRef.whenModelSubelement(targetGenModel.modelSubElRef,
            isDtoRef = { "target${intersectPropsData.targetVarNamePostfix}" },
            isTableRef = { "resultRow${intersectPropsData.targetVarNamePostfix}" },
        )

        if (currentFillerData.targetDslRef !is DslRef.table) {
            // special case e.g. DTO <-- TABLE
            createFromTable(intersectPropsData)
        } else {
            // so for all these, targetGenModel is a DslRef.table
            fillLambdas(intersectPropsData)
        }
        super.alreadyCreated = true
    }

    private fun fillLambdas(i: IntersectPropertys.CommonPropData) {
        val kotlinGenClassTable: KotlinClassModelTable = kotlinGenCtx.kotlinGenClass(i.targetGenModel.modelSubElRef) as KotlinClassModelTable
        val outgoingFKs = kotlinGenClassTable.outgoingFKs

        log.trace("fillLambdas: -> from {}", currentFillerData)

        val returnInsertLambda = LambdaTypeName.get(i.targetPoetType, DB.InsertStatementTypeName(), returnType = UNIT)
        val returnBatchInsertLambda = LambdaTypeName.get(DB.BatchInsertStatement, GenDslRefHelpers.dtoClassName(i.sourceGenModel, genCtx), returnType = UNIT)

        var funNameInsertOrBatch = funNameExpanded("fillShallowLambda", currentFillerData)
        var funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnInsertLambda)
        var body = insertShallowBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())
        /**
         * fun batchInsertShallowLambda(): BatchInsertStatement.(Dto) -> Unit
         */
        funNameInsertOrBatch = funNameExpanded("batchFillShallowLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .returns(returnBatchInsertLambda)
        body = insertShallowBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())
    }

    private fun insertShallowBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funNameInsertOrBatch: FunName): CodeBlock {
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
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} PoetType of %T", prop.poetType)
                },
                isTyp = {
                    none = false
                    KotlinFillerTablePoetStatements.fillTablePropTypOrPoetType(bodyBuilder, funNameInsertOrBatch, i.targetPoetType, i.sourceVarName, prop)
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
        if (none) bodyBuilder.addStatement("// NONE")
        var body = bodyBuilder.endControlFlow().build()
        return body
    }

//    private fun insert1to1ModelsBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funNameInsertOrBatch: FunName): CodeBlock {
//        val bodyBuilder = CodeBlock.builder()
//            .beginControlFlow("return {") // This will take care of the {} and indentations
//        var none = true
//        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
//        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
//            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
//                preFunc = { },
//                preNonCollection = { },
//                preCollection = { },
//                isModel = {
//                    none = false
//                    // TODO one2One check if dependant model Table Entry already exists!
//                    if (funNameInsertOrBatch.originalFunName.startsWith("insert")) {
//                        // SimpleSubentityTable.insert(SimpleSubentityTableFiller.insertFunction(sourceSimpleEntityDto.someModelObject))
//                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
//                        val genModelTable = genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef))
//                        bodyBuilder.addStatement(
//                            "%T.insert(%L.%L) /* TODO , backRef1: SomeDto, backRef2: SomeOtherDto */", // TODO also insert hardcoded instead swapOutOriginalFunNameWith
//                            genModelTable.modelClassName.crudBasePoetTypeForAllCruds + CrudData.CRUD.CREATE.toString(),
//                            // genModelTable.poetType,
//                            i.sourceVarName, prop.name(),
//                        )
//                    } else {
//                        //  SimpleSubentityTableFiller.batchInsertFunction(sourceSimpleEntityDto.someModelObject).invoke(this, sourceSimpleEntityDto.someModelObject)
//                        val fillerTableOfReffedModel = genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).fillerPoetType
//                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
//                        bodyBuilder.addStatement(
//                            "%T.%L().%L(this, %L.%L)",
//                            fillerTableOfReffedModel,
//                            //MemberName(fillerTableOfReffedModel, "batchInsertLambda"),
//                            funNameInsertOrBatch.swapOutOriginalFunNameWith("batchInsertShallowWith1To1sLambda"),
//                            "invoke",
//                            "it", prop.name()
//                        )
//                    }
//                    KotlinFillerTablePoetStatements.fillTablePropOne2OneModelUuid(bodyBuilder, funNameInsertOrBatch, i.targetPoetType, i.sourceVarName, prop)
//                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
//                },
//                isPoetType = { },
//                isTyp = { },
//                postNonCollection = { },
//                isModelList = { },
//                isModelSet = { },
//                isModelCollection = { },
//                isModelIterable = { },
//                isPoetTypeList = { },
//                isPoetTypeSet = { },
//                isPoetTypeCollection = { },
//                isPoetTypeIterable = { },
//                isTypList = { },
//                isTypSet = { },
//                isTypCollection = { },
//                isTypIterable = { },
//                postCollection = { },
//            )
//        }
//        if (none) bodyBuilder.addStatement("// NONE")
//        var body = bodyBuilder.endControlFlow().build()
//        return body
//    }
//
//    private fun insertNon1to1ModelsBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funNameInsertOrBatch: FunName): CodeBlock {
//        val bodyBuilder = CodeBlock.builder()
//            .beginControlFlow("return {") // This will take care of the {} and indentations
//        var none = true
//        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
//        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
//            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
//                preFunc = { },
//                preNonCollection = { },
//                preCollection = { },
//                isModel = { },
//                isPoetType = { },
//                isTyp = { },
//                postNonCollection = { },
//                isModelList = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
//
//                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
//                },
//                isModelSet = {
//                    none = false
//                    //SimpleSubentityTable.batchInsert(source.subentitys ?: emptySet(), shouldReturnGeneratedValues = false,
//                    //    body = FillerSimpleSubentityTable.batchInsertLambda(source.uuid)
//                    //)
//                    bodyBuilder.addStatement(
//                        "%T.%M(%L.%L ?: emptyList(), shouldReturnGeneratedValues = false, body = %T.%L(%L.%L))",
//                        genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
//                        DB.batchInsertMember,
//                        funNameInsertOrBatch.sourceOrIt(i.sourceVarName),
//                        prop.name(),
//                        propFiller(modelSubElementRef, MODELREFENUM.TABLE),
//                        "batchInsertLambda",
//                        funNameInsertOrBatch.sourceOrIt(i.sourceVarName),
//                        RuntimeDefaults.UUID_PROPNAME
//                    )
//                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
//                },
//                isModelCollection = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
//
//                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
//                },
//                isModelIterable = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
//
//                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
//                },
//                isPoetTypeList = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
//                },
//                isPoetTypeSet = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
//                },
//                isPoetTypeCollection = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
//                },
//                isPoetTypeIterable = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
//                },
//                isTypList = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
//                },
//                isTypSet = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
//                },
//                isTypCollection = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
//                },
//                isTypIterable = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
//                },
//                postCollection = { },
//            )
//        }
//        if (none) bodyBuilder.addStatement("// NONE")
//        var body = bodyBuilder.endControlFlow().build()
//        return body
//    }

    /** special case e.g. DTO <-- TABLE */
    private fun createFromTable(i: IntersectPropertys.CommonPropData) {
        val funName = if (currentFillerData.businessName == C.DEFAULT)
            i.targetGenModel.asVarName
        else
            currentFillerData.businessName.decap() + i.targetGenModel.asVarName.Cap()
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
                    funSpec.addStatement("%L.%L = %T.%L(%L)", i.targetVarName, prop.name(), propFiller(modelSubElementRef, MODELREFENUM.TABLE), prop.eitherTypModelOrClass.modelClassName.asVarName, i.sourceVarName)

                    addSyntheticFillersForTableModelProp(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
                },
                isPoetType = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} PoetType of %T", prop.poetType)
                },
                isTyp = { funSpec.addStatement("%L.%L = %L[%T.%L]", i.targetVarName, prop.name(), i.sourceVarName, i.sourcePoetType, prop.name()) },
                postNonCollection = { },
                isModelList = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isModelSet = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isModelCollection = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isModelIterable = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                isPoetTypeList = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isPoetTypeSet = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isPoetTypeCollection = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isPoetTypeIterable = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                isTypList = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isTypSet = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isTypCollection = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isTypIterable = {
                    funSpec.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                postCollection = { },
            )
        }
        if (i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }.isEmpty()) funSpec.addStatement("// NONE")
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }
}
