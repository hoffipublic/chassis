package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.GenDslRefHelpers
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.codegen.kotlin.whens.WhensGen
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.FK
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
        if (alreadyCreated(fillerData)) {
            log.trace("{} alreadyCreated for {} {}", this, modelkind, fillerData)
            return
        }
        log.trace("build({}, {})", modelkind, currentFillerData)

        val targetGenModel: GenModel = genCtx.genModel(fillerData.targetDslRef)
        val sourceGenModel: GenModel = genCtx.genModel(fillerData.sourceDslRef)

        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            targetGenModel,
            sourceGenModel,
            //kotlinGenCtx.kotlinGenClass(fillerData.sourceDslRef),
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
            createFromTable(intersectPropsData)
        } else {
            insertLambdas(intersectPropsData)
        }
    }

    private fun <T> FunSpec.Builder.addFunCall(prefixCodeBlock: CodeBlock, iterable: Iterable<T>, postfixCodeBlock: CodeBlock, preBlockCode: String = "", iterableMapBlock: CodeBlock.Builder.(T) -> CodeBlock.Builder): FunSpec.Builder {
        this.addCode(buildCodeBlock { this.addFunCall(prefixCodeBlock, iterable, postfixCodeBlock, preBlockCode, iterableMapBlock)})
        return this
    }
    private fun <T> CodeBlock.Builder.addFunCall(prefixCodeBlock: CodeBlock, iterable: Iterable<T>, postfixCodeBlock: CodeBlock, preBlockCode: String = "", iterableMapBlock: CodeBlock.Builder.(T) -> CodeBlock.Builder): CodeBlock.Builder {
        add(prefixCodeBlock)
        add(preBlockCode)
        for (el in iterable) {
            iterableMapBlock.invoke(this, el)
        }
        add(postfixCodeBlock)
        add("\n")
        return this
    }

    private fun insertLambdas(i: IntersectPropertys.CommonPropData) {
        val kotlinGenClassTable: KotlinClassModelTable = kotlinGenCtx.kotlinGenClass(i.targetGenModel.modelSubElRef) as KotlinClassModelTable
        val outgoingFKs = kotlinGenClassTable.outgoingFKs

        log.trace("insertLambdas: -> from {}", currentFillerData)

        var returnTypeInsertLambda = LambdaTypeName.get(i.targetPoetType, DB.InsertStatementTypeName(), returnType = UNIT)
        var funNameInsertOrBatch = funNameFiller("insertLambda", currentFillerData)
        var funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType).addOutgoingFKParams(outgoingFKs, kotlinGenClassTable)
            .returns(returnTypeInsertLambda)
        funSpec.beginControlFlow("return {")
        funSpec.addStatement("insertShallowLambda(%L).invoke(this, it)", i.sourceVarName)
        funSpec.addFunCall(
            buildCodeBlock { add("insertOutgoingFKsLambda(%L", i.sourceVarName) },
            outgoingFKs,
            buildCodeBlock { add(").invoke(this, it)") },
        ) {
            add(", %L", kotlinGenClassTable.fkPropVarNames(it).first)
        }
        funSpec.addStatement("insert1to1ModelsLambda(%L).invoke(this, it)", i.sourceVarName)
        funSpec.addStatement("insertNon1to1ModelsLambda(%L).invoke(this, it)", i.sourceVarName)
        funSpec.endControlFlow()
        builder.addFunction(funSpec.build())

        var funNameInsert = funNameFiller("insertOutgoingFKsLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType).addOutgoingFKParams(outgoingFKs, kotlinGenClassTable)
            .returns(returnTypeInsertLambda)
        funSpec.beginControlFlow("return {")
        // funSpec.addStatement("%L(%L).invoke(this, it)", funNameInsert.funName, i.sourceVarName)
        for (fk in outgoingFKs) {
            funSpec.addStatement(
                "%L[%T.%L] = %L",
                "it",
                i.targetPoetType,
                kotlinGenClassTable.fkPropVarNames(fk).first,
                kotlinGenClassTable.fkPropVarNames(fk).first,
            )
        }
        if (outgoingFKs.isEmpty()) funSpec.addStatement("// NONE")
        funSpec.endControlFlow()
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("insert1to1ModelsLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnTypeInsertLambda)
        var body = insert1to1ModelsBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("insertNon1to1ModelsLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnTypeInsertLambda)
        body = insertNon1to1ModelsBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("insertShallowLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnTypeInsertLambda)
        body = insertShallowBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())

        // ============================================

        returnTypeInsertLambda = LambdaTypeName.get(DB.BatchInsertStatement, GenDslRefHelpers.dtoClassName(i.sourceGenModel, genCtx), returnType = UNIT)
        funNameInsertOrBatch = funNameFiller("batchInsertLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .addOutgoingFKParams(outgoingFKs, kotlinGenClassTable)
            .returns(returnTypeInsertLambda)
        funSpec.beginControlFlow("return {")
        funSpec.addStatement("batchInsertShallowLambda().invoke(this, it)")
        funSpec.addFunCall(
            buildCodeBlock { add("batchInsertOutgoingFKsLambda(") },
            outgoingFKs,
            buildCodeBlock { add(").invoke(this, it)") },
        ) {
            add("%L", kotlinGenClassTable.fkPropVarNames(it).first)
        }
        funSpec.addStatement("batchInsert1to1ModelsLambda().invoke(this, it)")
        funSpec.addStatement("batchInsertNon1to1ModelsLambda().invoke(this, it)")
        funSpec.endControlFlow()
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("batchInsertOutgoingFKsLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .addOutgoingFKParams(outgoingFKs, kotlinGenClassTable)
            .returns(returnTypeInsertLambda)
        funSpec.beginControlFlow("return {")
        // funSpec.addStatement("%L(%L).invoke(this, it)", funNameInsert.funName, i.sourceVarName)
        for (fk in outgoingFKs) {
            funSpec.addStatement(
                "%L[%T.%L] = %L",
                "this",
                i.targetPoetType,
                kotlinGenClassTable.fkPropVarNames(fk).first,
                kotlinGenClassTable.fkPropVarNames(fk).first,
            )
        }
        if (outgoingFKs.isEmpty()) funSpec.addStatement("// NONE")
        funSpec.endControlFlow()
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("batchInsert1to1ModelsLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .returns(returnTypeInsertLambda)
        body = insert1to1ModelsBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("batchInsertNon1to1ModelsLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .returns(returnTypeInsertLambda)
        body = insertNon1to1ModelsBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())

        funNameInsert = funNameFiller("batchInsertShallowLambda", currentFillerData)
        funSpec = FunSpec.builder(funNameInsert.funName)
            .returns(returnTypeInsertLambda)
        body = insertShallowBody(i, funSpec, funNameInsertOrBatch)
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())
    }

    private fun FunSpec.Builder.addOutgoingFKParams(outgoingFKs: MutableSet<FK>, kotlinGenClassTable: KotlinClassModelTable): FunSpec.Builder {
        for (fk in outgoingFKs) {
            val fkParamBuilder = ParameterSpec.builder(kotlinGenClassTable.fkPropVarNames(fk).first, RuntimeDefaults.classNameUUID, fk.toProp.modifiers)
            this.addParameter(fkParamBuilder.build())
        }
        return this
    }

    private fun insertShallowBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funNameInsertOrBatch: FunName): CodeBlock {
        var bodyBuilder = CodeBlock.builder()
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
                    bodyBuilder.addStatement(
                        "%L[%T.%L] = %L.%L",
                        if (funNameInsertOrBatch.originalFunName == "insertLambda") "it" else "this",
                        i.targetPoetType,
                        prop.name(),
                        if (funNameInsertOrBatch.originalFunName == "insertLambda") i.sourceVarName else "it",
                        prop.name()
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
        if (none) bodyBuilder.addStatement("// NONE")
        var body = bodyBuilder.endControlFlow().build()
        return body
    }
    private fun insert1to1ModelsBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funNameInsertOrBatch: FunName): CodeBlock {
        var bodyBuilder = CodeBlock.builder()
            .beginControlFlow("return {") // This will take care of the {} and indentations
        var none = true
        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
                preFunc = { },
                preNonCollection = { },
                preCollection = { },
                isModel = {
                    none = false
                    // TODO one2One check if dependant model Table Entry already exists!
                    if (funNameInsertOrBatch.originalFunName == "insertLambda") {
                        // SimpleSubentityTable.insert(SimpleSubentityTableFiller.insertFunction(sourceSimpleEntityDto.someModelObject))
                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
                        //bodyBuilder.addStatement(
                        //    "%T.%M(%T.%L(%L.%L, %L.%L))",
                        //    genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
                        //    DB.insertMember,
                        //    propFiller(modelSubElementRef, MODELREFENUM.TABLE),
                        //    funNameInsertOrBatch.funName,
                        //    i.sourceVarName, prop.name(),
                        //    i.sourceVarName, RuntimeDefaults.UUID_PROPNAME
                        //)
                        bodyBuilder.addStatement(
                            "%T.%M(%T.%L(%L.%L))",
                            genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
                            DB.insertMember,
                            propFiller(modelSubElementRef, MODELREFENUM.TABLE),
                            funNameInsertOrBatch.funName, i.sourceVarName, prop.name()
                        )
                    } else {
                        //  SimpleSubentityTableFiller.batchInsertFunction(sourceSimpleEntityDto.someModelObject).invoke(this, sourceSimpleEntityDto.someModelObject)
                        val fillerTableOfReffedModel = genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).fillerPoetType
                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
                        bodyBuilder.addStatement(
                            "%T.%L().%L(this, %L.%L)",
                            fillerTableOfReffedModel,
                            //MemberName(fillerTableOfReffedModel, "batchInsertLambda"),
                            funNameInsertOrBatch.funName,
                            "invoke",
                            "it", prop.name()
                        )
                    }
                    bodyBuilder.addStatement(
                        "%L[%T.%L] = %L.%L.%L",
                        if (funNameInsertOrBatch.originalFunName == "insertLambda") "it" else "this",
                        i.targetPoetType,
                        prop.name(postfix = RuntimeDefaults.UUID_PROPNAME),
                        if (funNameInsertOrBatch.originalFunName == "insertLambda") i.sourceVarName else "it", prop.name(), "uuid"
                    )
                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
                },
                isPoetType = { },
                isTyp = { },
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
    private fun insertNon1to1ModelsBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funNameInsertOrBatch: FunName): CodeBlock {
        var bodyBuilder = CodeBlock.builder()
            .beginControlFlow("return {") // This will take care of the {} and indentations
        var none = true
        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
                preFunc = { },
                preNonCollection = { },
                preCollection = { },
                isModel = { },
                isPoetType = { },
                isTyp = { },
                postNonCollection = { },
                isModelList = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)

                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
                },
                isModelSet = {
                    none = false
                    //SimpleSubentityTable.batchInsert(source.subentitys ?: emptySet(), shouldReturnGeneratedValues = false,
                    //    body = FillerSimpleSubentityTable.batchInsertLambda(source.uuid)
                    //)
                    bodyBuilder.addStatement(
                        "%T.%M(%L.%L ?: emptyList(), shouldReturnGeneratedValues = false, body = %T.%L(%L.%L))",
                        genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
                        DB.batchInsertMember,
                        if (funNameInsertOrBatch.originalFunName == "insertLambda") i.sourceVarName else "it",
                        prop.name(),
                        propFiller(modelSubElementRef, MODELREFENUM.TABLE),
                        "batchInsertLambda",
                        if (funNameInsertOrBatch.originalFunName == "insertLambda") i.sourceVarName else "it",
                        RuntimeDefaults.UUID_PROPNAME
                    )
                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
                },
                isModelCollection = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)

                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
                },
                isModelIterable = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)

                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
                },
                isPoetTypeList = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isPoetTypeSet = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isPoetTypeCollection = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isPoetTypeIterable = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                isTypList = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} LIST of %T", prop.poetType)
                },
                isTypSet = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} SET of %T", prop.poetType)
                },
                isTypCollection = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
                },
                isTypIterable = {
                    none = false
                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
                },
                postCollection = { },
            )
        }
        if (none) bodyBuilder.addStatement("// NONE")
        var body = bodyBuilder.endControlFlow().build()
        return body
    }

    private fun addSyntheticFiller(propEitherModel: EitherTypOrModelOrPoetType.EitherModel, currentFillerData: FillerData, via: String) {
        val propModelRefSimpleName = propEitherModel.modelSubElementRef.simpleName
        val propModelParentRef = propEitherModel.modelSubElementRef.parentDslRef
        val propDtoModelDslRef = DslRef.dto(propModelRefSimpleName, propModelParentRef)
        val propTableModelDslRef = DslRef.table(propModelRefSimpleName, propModelParentRef)
        genCtx.syntheticFillerDatas.add(SynthFillerData(currentFillerData.businessName, propTableModelDslRef, propDtoModelDslRef, via = via))
        genCtx.syntheticFillerDatas.add(SynthFillerData(currentFillerData.businessName, propDtoModelDslRef, propTableModelDslRef, via = via))
    }

    private fun createFromTable(i: IntersectPropertys.CommonPropData) {
        val funName = funNameFiller(i.targetGenModel.asVarName, currentFillerData)
        log.trace("create(Dto)FromTable: -> {} from {}", funName.funName, currentFillerData)
        val funSpec = FunSpec.builder(funName.funName)
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

                    addSyntheticFiller(this, this@KotlinFillerTable.currentFillerData, via = "TableFiller for prop: '$prop' from currentFillerData: ${currentFillerData}")
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
