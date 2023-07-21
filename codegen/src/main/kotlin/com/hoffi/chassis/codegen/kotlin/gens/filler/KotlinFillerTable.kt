package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.GenDslRefHelpers
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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.UNIT

context(GenCtxWrapper)
class KotlinFillerTable constructor(fillerData: FillerData): AKotlinFiller(fillerData, MODELKIND.TABLEKIND) {

    // TODO constructor fillerData has to construct the "right" "Filler" (might be Table filler even with target being a DTO
    // also important for build() alreadyCreated
    // move all Filler relevant data into intersectPropsData
    // add target and source FillerClassName into intersectPropsData

    override fun build(modelkind: MODELKIND, fillerData: FillerData) {
        currentBuildFillerData = fillerData
        if (alreadyCreated.contains(fillerData.sourceDslRef))
            return
        else
            alreadyCreated.add(fillerData.sourceDslRef)
        val targetGenModel: GenModel = genCtx.genModel(fillerData.targetDslRef)
        val sourceGenModel: GenModel = genCtx.genModel(fillerData.sourceDslRef)

        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            modelkind,
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

        if (currentBuildFillerData.targetDslRef !is DslRef.table) {
            createFromTable(intersectPropsData)
        } else {
            insertLambdas(intersectPropsData)
        }

    }

    private fun insertLambdas(i: IntersectPropertys.CommonPropData) {
        var returnLambdaTypeName = LambdaTypeName.get(i.targetPoetType, DB.InsertStatementTypeName(), returnType = UNIT)
        var funSpec = FunSpec.builder("insertLambda")
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnLambdaTypeName)
        var body = insertBody(i, funSpec, "insertLambda")
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())

        returnLambdaTypeName = LambdaTypeName.get(DB.BatchInsertStatement, GenDslRefHelpers.dtoClassName(i.sourceGenModel, genCtx), returnType = UNIT)
        funSpec = FunSpec.builder("batchInsertLambda")
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .returns(returnLambdaTypeName)
        body = insertBody(i, funSpec, "batchInsertLambda")
        funSpec.addCode(body)
        builder.addFunction(funSpec.build())
    }

    private fun insertBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, insertLambdaName: String): CodeBlock {
        var bodyBuilder = CodeBlock.builder()
            .beginControlFlow("return {") // This will take care of the {} and indentations
        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
        for (prop in i.targetGenModel.allProps.values.filter { Tag.TRANSIENT !in it.tags }) {
            WhensGen.whenTypeAndCollectionType(
                prop.eitherTypModelOrClass, prop.collectionType,
                preFunc = { },
                preNonCollection = { },
                preCollection = { },
                isModel = {
                    // TODO one2One check if dependant model Table Entry already exists!
                    if (insertLambdaName == "insertLambda") {
                        // SimpleSubentityTable.insert(SimpleSubentityTableFiller.insertFunction(sourceSimpleEntityDto.someModelObject))
                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
                        bodyBuilder.addStatement(
                            "%T.%M(%T.%L(%L.%L))",
                            genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
                            DB.insertMember,
                            propFiller(modelSubElementRef, MODELREFENUM.TABLE),
                            "insertLambda", i.sourceVarName, prop.name
                        )
                    } else {
                        //  SimpleSubentityTableFiller.batchInsertFunction(sourceSimpleEntityDto.someModelObject).invoke(this, sourceSimpleEntityDto.someModelObject)
                        val fillerTableOfReffedModel = genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).fillerPoetType
                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
                        bodyBuilder.addStatement(
                            "%T.%L(%L.%L).%L(this, %L.%L)",
                            fillerTableOfReffedModel,
                            //MemberName(fillerTableOfReffedModel, "batchInsertLambda"),
                            "batchInsertLambda",
                            i.sourceVarName, prop.name,
                            "invoke",
                            i.sourceVarName, prop.name
                        )
                    }
                    bodyBuilder.addStatement(
                        "%L[%T.%L] = %L.%L.%L",
                        if (insertLambdaName == "insertLambda") "it" else "this",
                        i.targetPoetType,
                        prop.name,
                        i.sourceVarName, prop.name, "uuid"
                    )
                    val originalRef = this.modelClassName.modelSubElRef
                    genCtx.syntheticFillerDatas.add(SynthFillerData(this.modelSubElementRef, originalRef, via = "TableFiller for contained prop $prop"))
                    genCtx.syntheticFillerDatas.add(SynthFillerData(originalRef, this.modelSubElementRef, via = "TableFiller for contained prop $prop"))
                },
                isPoetType = { },
                isTyp = {
                    bodyBuilder.addStatement(
                        "%L[%T.%L] = %L.%L",
                        if (insertLambdaName == "insertLambda") "it" else "this",
                        i.targetPoetType,
                        prop.name,
                        i.sourceVarName,
                        prop.name
                    )
                },
                postNonCollection = { },
                isModelList = { },
                isModelSet = { },
                isModelCollection = {

                    val originalRef = this.modelClassName.modelSubElRef
                    genCtx.syntheticFillerDatas.add(
                        SynthFillerData(
                            this.modelSubElementRef,
                            originalRef,
                            via = "TableFiller for contained prop $prop"
                        )
                    )
                    genCtx.syntheticFillerDatas.add(
                        SynthFillerData(
                            originalRef,
                            this.modelSubElementRef,
                            via = "TableFiller for contained prop $prop"
                        )
                    )
                },
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
        var body = bodyBuilder.endControlFlow().build()
        return body
    }

    private fun createFromTable(i: IntersectPropertys.CommonPropData) {
        val funSpec = FunSpec.builder(i.targetGenModel.asVarName)
            .addParameter(i.sourceVarName, DB.ResultRowClassName)
            .returns(i.targetPoetType)
        //val targetSimpleEntityDto = SimpleEntityDto._internal_create()
        funSpec.addStatement("val %L = %T._internal_create()", i.targetVarName, i.targetPoetType)
        for (prop in i.targetGenModel.allProps.values.filter { Tag.TRANSIENT !in it.tags }) {
            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
                preFunc = { },
                preNonCollection = { },
                preCollection = { },
                isModel = {
                    funSpec.addStatement("%L.%L = %T.%L(%L)", i.targetVarName, prop.name, propFiller(modelSubElementRef, MODELREFENUM.TABLE), prop.eitherTypModelOrClass.modelClassName.asVarName, i.sourceVarName)

                    val originalRef = this.modelClassName.modelSubElRef
                    genCtx.syntheticFillerDatas.add(SynthFillerData(this.modelSubElementRef, originalRef, via = "TableFiller for contained prop $prop"))
                    genCtx.syntheticFillerDatas.add(SynthFillerData(originalRef, this.modelSubElementRef, via = "TableFiller for contained prop $prop"))
                },
                isPoetType = { },
                isTyp = { funSpec.addStatement("%L.%L = %L[%T.%L]", i.targetVarName, prop.name, i.sourceVarName, i.sourcePoetType, prop.name) },
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
        funSpec.addStatement("return %L", i.targetVarName)
        builder.addFunction(funSpec.build())
    }
}
