package com.hoffi.chassis.codegen.kotlin.gens.crud

import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.gens.filler.IntersectPropertys
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.CrudData
import com.hoffi.chassis.shared.whens.WhensDslRef

context(GenCtxWrapper)
class KotlinCrudExposed(crudData: CrudData): AKotlinCrud(crudData) {

    // TODO constructor crudData has to construct the "right" "Crud" (might be Table crud even with target being a DTO
    // also important for build() alreadyCreated
    // move all Crud relevant data into intersectPropsData
    // add target and source CrudClassName into intersectPropsData

    override fun build(crudData: CrudData) {
        currentCrudData = crudData
        if (alreadyCreated(crudData)) return
        log.trace("build({}, {})", currentCrudData)

        val targetGenModel: GenModel = genCtx.genModel(crudData.targetDslRef)
        val sourceGenModel: GenModel = genCtx.genModel(crudData.sourceDslRef)

        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            targetGenModel,
            sourceGenModel,
            //kotlinGenCtx.kotlinGenClass(crudData.sourceDslRef),
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

//println(crudPoetType)
//        val crudBuilder = TypeSpec.objectBuilder(crudPoetType)
//        builder.addType(crudBuilder.build())

        if (currentCrudData.targetDslRef !is DslRef.table) {
            //createFromTable(intersectPropsData)
        } else {
            //insertLambdas(intersectPropsData)
        }
    }

//    private fun insertLambdas(i: IntersectPropertys.CommonPropData) {
//        var returnLambdaTypeName = LambdaTypeName.get(i.targetPoetType, DB.InsertStatementTypeName(), returnType = UNIT)
//        var funName = funNameCrud("insertLambda", currentCrudData)
//        var funSpec = FunSpec.builder(funName.funName)
//            .addParameter(i.sourceVarName, i.sourcePoetType)
//            .returns(returnLambdaTypeName)
//        var body = insertBody(i, funSpec, funName)
//        funSpec.addCode(body)
//        builder.addFunction(funSpec.build())
//
//        returnLambdaTypeName = LambdaTypeName.get(DB.BatchInsertStatement, GenDslRefHelpers.dtoClassName(i.sourceGenModel, genCtx), returnType = UNIT)
//        funName = funNameCrud("batchInsertLambda", currentCrudData)
//        funSpec = FunSpec.builder(funName.funName)
//            .addParameter(i.sourceVarName, i.sourcePoetType)
//            .returns(returnLambdaTypeName)
//        body = insertBody(i, funSpec, funName)
//        funSpec.addCode(body)
//        builder.addFunction(funSpec.build())
//    }
//
//    private fun insertBody(i: IntersectPropertys.CommonPropData, funSpec: FunSpec.Builder, funName: FunName): CodeBlock {
//        var bodyBuilder = CodeBlock.builder()
//            .beginControlFlow("return {") // This will take care of the {} and indentations
//        // allProps as a) Table's always gatherProps from superclasses and b) alle table columns have to be filled
//        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
//            WhensGen.whenTypeAndCollectionType(
//                prop.eitherTypModelOrClass, prop.collectionType,
//                preFunc = { },
//                preNonCollection = { },
//                preCollection = { },
//                isModel = {
//                    // TODO one2One check if dependant model Table Entry already exists!
//                    if (funName.originalFunName == "insertLambda") {
//                        // SimpleSubentityTable.insert(SimpleSubentityTableCrud.insertFunction(sourceSimpleEntityDto.someModelObject))
//                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
//                        bodyBuilder.addStatement(
//                            "%T.%M(%T.%L(%L.%L))",
//                            genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
//                            DB.insertMember,
//                            propCrud(modelSubElementRef, MODELREFENUM.TABLE),
//                            "insertLambda", i.sourceVarName, prop.name
//                        )
//                    } else {
//                        //  SimpleSubentityTableCrud.batchInsertFunction(sourceSimpleEntityDto.someModelObject).invoke(this, sourceSimpleEntityDto.someModelObject)
//                        val crudTableOfReffedModel = genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).crudPoetType
//                        bodyBuilder.addStatement("// TODO one2One check if dependant model Table Entry already exists!")
//                        bodyBuilder.addStatement(
//                            "%T.%L(%L.%L).%L(this, %L.%L)",
//                            crudTableOfReffedModel,
//                            //MemberName(crudTableOfReffedModel, "batchInsertLambda"),
//                            "batchInsertLambda",
//                            i.sourceVarName, prop.name,
//                            "invoke",
//                            i.sourceVarName, prop.name
//                        )
//                    }
//                    bodyBuilder.addStatement(
//                        "%L[%T.%L] = %L.%L.%L",
//                        if (funName.originalFunName == "insertLambda") "it" else "this",
//                        i.targetPoetType,
//                        prop.name,
//                        i.sourceVarName, prop.name, "uuid"
//                    )
//                    val originalRef = this.modelClassName.modelSubElRef
//                    genCtx.syntheticCrudDatas.add(SynthCrudData(currentCrudData.businessName, this.modelSubElementRef, originalRef, via = "TableCrud for contained prop $prop"))
//                    genCtx.syntheticCrudDatas.add(SynthCrudData(currentCrudData.businessName, originalRef, this.modelSubElementRef, via = "TableCrud for contained prop $prop"))
//                },
//                isPoetType = { },
//                isTyp = {
//                    bodyBuilder.addStatement(
//                        "%L[%T.%L] = %L.%L",
//                        if (funName.originalFunName == "insertLambda") "it" else "this",
//                        i.targetPoetType,
//                        prop.name,
//                        i.sourceVarName,
//                        prop.name
//                    )
//                },
//                postNonCollection = { },
//                isModelList = { },
//                isModelSet = { },
//                isModelCollection = {
//
//                    val originalRef = this.modelClassName.modelSubElRef
//                    genCtx.syntheticCrudDatas.add(SynthCrudData(currentCrudData.businessName, this.modelSubElementRef, originalRef, via = "TableCrud for contained prop $prop"))
//                    genCtx.syntheticCrudDatas.add(SynthCrudData(currentCrudData.businessName, originalRef, this.modelSubElementRef, via = "TableCrud for contained prop $prop"))
//                },
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
//        var body = bodyBuilder.endControlFlow().build()
//        return body
//    }
//
//    private fun createFromTable(i: IntersectPropertys.CommonPropData) {
//        val funName = funNameCrud(i.targetGenModel.asVarName, currentCrudData)
//        log.trace("currentCrudData: -> {}\n{}", funName, currentCrudData)
//        val funSpec = FunSpec.builder(funName.funName)
//            .addParameter(i.sourceVarName, DB.ResultRowClassName)
//            .returns(i.targetPoetType)
//        //val targetSimpleEntityDto = SimpleEntityDto._internal_create()
//        funSpec.addStatement("val %L = %T._internal_create()", i.targetVarName, i.targetPoetType)
//        for (prop in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }) {
//            WhensGen.whenTypeAndCollectionType(prop.eitherTypModelOrClass, prop.collectionType,
//                preFunc = { },
//                preNonCollection = { },
//                preCollection = { },
//                isModel = {
//                    funSpec.addStatement("%L.%L = %T.%L(%L)", i.targetVarName, prop.name, propCrud(modelSubElementRef, MODELREFENUM.TABLE), prop.eitherTypModelOrClass.modelClassName.asVarName, i.sourceVarName)
//
//                    val originalRef = this.modelClassName.modelSubElRef
//                    genCtx.syntheticCrudDatas.add(SynthCrudData(currentCrudData.businessName, this.modelSubElementRef, originalRef, via = "TableCrud for contained prop $prop"))
//                    genCtx.syntheticCrudDatas.add(SynthCrudData(currentCrudData.businessName, originalRef, this.modelSubElementRef, via = "TableCrud for contained prop $prop"))
//                },
//                isPoetType = { },
//                isTyp = { funSpec.addStatement("%L.%L = %L[%T.%L]", i.targetVarName, prop.name, i.sourceVarName, i.sourcePoetType, prop.name) },
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
//        funSpec.addStatement("return %L", i.targetVarName)
//        builder.addFunction(funSpec.build())
//    }
}
