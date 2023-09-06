package com.hoffi.chassis.codegen.kotlin.gens.crud

import com.hoffi.chassis.chassismodel.PoetHelpers.nullable
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.*
import com.hoffi.chassis.codegen.kotlin.gens.KotlinClassModelTable
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.*
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

context(GenCtxWrapper)
class KotlinCrudExposed(crudData: CrudData): AKotlinCrud(crudData) {

    // TODO constructor crudData has to construct the "right" "Crud" (might be Table crud even with target being a DTO
    // also important for build() alreadyCreated
    // move all Crud relevant data into intersectPropsData
    // add target and source CrudClassName into intersectPropsData

    override fun build(crudData: CrudData) {
        currentCrudData = crudData
        currentAHasCopyBoundryData = crudData
        if (alreadyCreated(currentCrudData)) {
            if (currentCrudData !is SynthCrudData) log.warn("Already Created specific Exposed CRUD: $currentCrudData for $this")
            return
        }
        log.trace("build({})", currentCrudData)
        if (crudData.sourceDslRef.parentDslRef.simpleName != crudData.targetDslRef.parentDslRef.simpleName) {
            // CREATE from some other DTO than this CRUD's Table
            log.warn("NOT creating CRUD because source-model and target-model differ: '$crudData'")
            return
        }

        // if we didn't explicitly declared a filler in the DSL ... but we need it for the CRUDs
        // CrudData always(!) has targetDslRef Table
        // FillerData might have targetDslRef DTO <-- TABLE sourceDslRef
        when (currentCrudData.crud) {
            is CrudData.CRUD.CREATE -> genCtx.addSyntheticFillerData(SynthFillerData.create(currentCrudData.targetDslRef, currentCrudData.sourceDslRef, currentCrudData, via = "${this::class.simpleName}('$crudData')"))
            is CrudData.CRUD.READ ->   genCtx.addSyntheticFillerData(SynthFillerData.create(currentCrudData.sourceDslRef, currentCrudData.targetDslRef, currentCrudData, via = "${this::class.simpleName}('$crudData')"))
            is CrudData.CRUD.UPDATE -> genCtx.addSyntheticFillerData(SynthFillerData.create(currentCrudData.targetDslRef, currentCrudData.sourceDslRef, currentCrudData, via = "${this::class.simpleName}('$crudData')"))
            is CrudData.CRUD.DELETE -> genCtx.addSyntheticFillerData(SynthFillerData.create(currentCrudData.targetDslRef, currentCrudData.sourceDslRef, currentCrudData, via = "${this::class.simpleName}('$crudData')"))
        }

        val targetGenModel: GenModel = genCtx.genModel(crudData.targetDslRef)
        val sourceGenModel: GenModel = genCtx.genModel(crudData.sourceDslRef)

        if (sourceGenModel.isInterface || KModifier.ABSTRACT in sourceGenModel.classModifiers) {
            log.error("crudData source is Interface or Abstract: $crudData for $this")
            throw GenException("crudData source is Interface or Abstract: $crudData for $this")
        }

        val intersectPropsData = IntersectPropertys.intersectPropsOf(
            genCtx, targetGenModel, sourceGenModel,
            "", ""
        )

        intersectPropsData.sourceVarName = WhensDslRef.whenModelSubelement(
            sourceGenModel.modelSubElRef,
            isDtoRef = { "source${intersectPropsData.sourceVarNamePostfix}" },
            isTableRef = { throw GenException("CrudData.sourceDslRef not allowed to be DslRef.table") },
        )
        intersectPropsData.targetVarName = WhensDslRef.whenModelSubelement(
            targetGenModel.modelSubElRef,
            isDtoRef = { throw GenException("CrudData.targetDslRef has to be DslRef.table") },
            isTableRef = { "resultRow${intersectPropsData.targetVarNamePostfix}" },
        )

        when (val crud = currentCrudData.crud) {
            is CrudData.CRUD.CREATE -> { insertDb(intersectPropsData) }
            is CrudData.CRUD.READ ->   {
                when (crud.variant) {
                    CrudData.CRUD.READ.READVARIANT.ALLVARIANTS -> {
                        readByJoinDb(intersectPropsData)
                        readBySelect(intersectPropsData)
                    }
                    CrudData.CRUD.READ.READVARIANT.JOIN -> readByJoinDb(intersectPropsData)
                    CrudData.CRUD.READ.READVARIANT.SELECT -> readBySelect(intersectPropsData)
                }
            }
            is CrudData.CRUD.UPDATE -> { log.warn("KotlinCrudExposed for ${currentCrudData.crud} not implemented yet") ; return }
            is CrudData.CRUD.DELETE -> { log.warn("KotlinCrudExposed for ${currentCrudData.crud} not implemented yet") ; return }
        }
        super.alreadyCreated = true
    }

    private fun insertDb(i: IntersectPropertys.CommonPropData) {
        val tableClassModel: KotlinClassModelTable = kotlinGenCtx.kotlinGenClass(i.targetGenModel.modelSubElRef) as KotlinClassModelTable
        val outgoingFKs = tableClassModel.outgoingFKs
        val incomingFKs = tableClassModel.incomingFKs

        // add neede syntheticCruds (if not specified in DSL), we'll also need the Fillers for them, too
        for (propEither in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }.map { it.eitherTypModelOrClass }) {
            if (propEither is EitherTypOrModelOrPoetType.EitherModel) {
                val propSubElRef = propEither.modelSubElementRef
                genCtx.addSyntheticCrudData(SynthCrudData.create(DslRef.table(propSubElRef.simpleName, propSubElRef.parentDslRef), DslRef.dto(propSubElRef.simpleName, propSubElRef.parentDslRef), currentCrudData, "via insert"))
            }
        }

        val insertLambda = LambdaTypeName.get(i.targetPoetType, DB.InsertStatementTypeName(), returnType = UNIT)
        val batchInsertLambda = LambdaTypeName.get(DB.BatchInsertStatementClassName, GenDslRefHelpers.dtoClassName(i.sourceGenModel), returnType = UNIT)

        // ============================
        // fun insertDb
        // ============================
        var funNameInsertOrBatch = funNameExpanded("insertDb", currentCrudData)
        var funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .addParameter(i.sourceVarName, i.sourcePoetType)
            .addOutgoingFKParams(outgoingFKs, COLLECTIONTYP.COLLECTION, funNameInsertOrBatch)
            .addParameter(ParameterSpec.builder("customStatements", insertLambda).defaultValue("{}").build())
        //.returns(returnLambdaTypeName)
        funSpec
            .addComment("insert 1To1 Models")
            .insertOutgoing1To1Props(outgoingFKs, funNameInsertOrBatch, i)
            .addComment("insertShallow %L and add outgoing ManyTo1-backrefUuids and 1To1-forwardRefUuids", i.targetGenModel.poetTypeSimpleName)
            .beginControlFlow("%T.%M", i.targetPoetType, DB.insertMember)
            .addStatement("%T.%L(%L).invoke(this, it)", i.targetFillerPoetType, funNameInsertOrBatch.swapOutOriginalFunNameWith("fillShallowLambda"), i.sourceVarName)
            .addComment("outgoing FK uuid refs")
            .addOutgoingFKProps(outgoingFKs, funNameInsertOrBatch, i)
            //.addStatement("%T.%L(source).invoke(this, it)", i.targetFillerPoetType, funNameInsertOrBatch.swapOutOriginalFunNameWith("insertShallowWith1To1sLambda"))
            .addStatement("customStatements.invoke(this, it)")
            .endControlFlow()

            .addComment("insert ManyTo1 Instances")
            .insertManyTo1ModelsBody(incomingFKs, funNameInsertOrBatch, i)
        builder.addFunction(funSpec.build())

        // ============================
        // fun batchInsertDb
        // ============================
        funNameInsertOrBatch = funNameExpanded("batchInsertDb", currentCrudData)
        funSpec = FunSpec.builder(funNameInsertOrBatch.funName)
            .addParameter(i.sourceVarName + "s", ClassName("kotlin.collections", "Collection").parameterizedBy(i.sourcePoetType))
            .addOutgoingFKParams(outgoingFKs, COLLECTIONTYP.COLLECTION, funNameInsertOrBatch)
            .addParameter(ParameterSpec.builder("customStatements", batchInsertLambda).defaultValue("{}").build())
        //.returns(returnLambdaTypeName)
        funSpec
            .addComment("insert 1To1 Models")
            .insertOutgoing1To1Props(outgoingFKs,funNameInsertOrBatch, i)
            .addComment("batchInsertShallow %L and add outgoing ManyTo1-backrefUuids and 1To1-forwardRefUuids", i.targetGenModel.poetTypeSimpleName)
            .beginControlFlow("%T.%M(%L, shouldReturnGeneratedValues = false)", i.targetPoetType, DB.batchInsertMember, i.sourceVarName + "s")
            .addStatement("%T.%L().invoke(this, it)", i.targetFillerPoetType, funNameInsertOrBatch.swapOutOriginalFunNameWith("batchFillShallowLambda"))
            .addComment("outgoing FK uuid refs")
            .addOutgoingFKProps(outgoingFKs, funNameInsertOrBatch, i)
            .addStatement("customStatements(it)")
            .endControlFlow()
            .addComment("batchInsert ManyTo1 Instances")
            .insertManyTo1ModelsBody(incomingFKs, funNameInsertOrBatch, i)
        builder.addFunction(funSpec.build())
    }

    private fun FunSpec.Builder.insertManyTo1ModelsBody(incomingFKs: MutableSet<FK>, funNameInsertOrBatch: FunName, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        var none = true
        if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
            for (fk in incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }) {
                none = false
                propBoundry(fk.toProp,
                    noPropBoundry = {
                        addStatement("%T.%L(%L.flatMap { it.%L%L }, %L.flatMap { %L -> %L.%L%L.map { it.%L to %L.%L } }.toMap())",
                            GenClassNames.crudFor((fk.toProp.eitherTypModelOrClass as EitherTypOrModelOrPoetType.EitherModel).modelSubElementRef, CrudData.CRUD.CREATE),
                            funNameInsertOrBatch.swapOutOriginalFunNameWith("batchInsertDb"),
                            i.sourceVarName + "s",
                            fk.toProp.name(), if (fk.toProp.isNullable) " ?: emptyList()" else "",
                            i.sourceVarName + "s",
                            i.sourceVarName, i.sourceVarName,
                            fk.toProp.name(), if (fk.toProp.isNullable) "!!" else "",
                            RuntimeDefaults.UUID_PROPNAME, i.sourceVarName, RuntimeDefaults.UUID_PROPNAME
                        )
                    },
                    IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                    ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
                )
            }
        } else {
            for (fk in incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }) {
                none = false
                propBoundry(fk.toProp,
                    noPropBoundry = {
                        addStatement("%T.%L(%L.%L%L, %L%L.%L%L%L.associate { it.%L to %L.%L } /* , otherBackref1, otherBackref2, ... */)",
                            GenClassNames.crudFor(fk.fromTableRef, CrudData.CRUD.CREATE),
                            funNameInsertOrBatch.swapOutOriginalFunNameWith("batchInsertDb"),
                            i.sourceVarName,
                            fk.toProp.name(), if (fk.toProp.isNullable) " ?: emptyList()" else "",
                            if (fk.toProp.isNullable) "(" else "",
                            i.sourceVarName,
                            fk.toProp.name(), if (fk.toProp.isNullable) " ?: emptyList()" else "",
                            if (fk.toProp.isNullable) ")" else "",
                            RuntimeDefaults.UUID_PROPNAME,
                            i.sourceVarName,
                            RuntimeDefaults.UUID_PROPNAME
                        )
                    },
                    IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                    ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
                )
            }
        }
        if (none) addStatement("// NONE")
        return this
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
//                    addSyntheticCrud(this, this@KotlinCrudExposed.currentCrudData, via = "KotlinCrud for prop: '$prop' from currentFillerData: ${currentCrudData}")
//                },
//                isModelSet = {
//                    none = false
//                    //bodyBuilder.addStatement(
//                    //    "%T.%M(%L.%L ?: emptyList(), shouldReturnGeneratedValues = false, body = %T.%L(%L.%L))",
//                    //    genCtx.genModel(DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)).poetType,
//                    //    DB.batchInsertMember,
//                    //    funNameInsertOrBatch.sourceOrIt(i.sourceVarName),
//                    //    prop.name(),
//                    //    propFiller(modelSubElementRef, MODELREFENUM.TABLE),
//                    //    "batchInsertLambda",
//                    //    funNameInsertOrBatch.sourceOrIt(i.sourceVarName),
//                    //    RuntimeDefaults.UUID_PROPNAME
//                    //)
//                    addSyntheticCrud(this, this@KotlinCrudExposed.currentCrudData, via = "KotlinCrud for prop: '$prop' from currentFillerData: ${currentCrudData}")
//
//                    ////for (entityDtoBackref in sources) {
//                    ////    CrudSimpleSubentityTableCREATE.batchInsert(entityDtoBackref.subentitys ?: emptyList(), entityDtoBackref)
//                    ////    //CrudSimpleOtherModelTableCREATE.batchInsert(entityDtoBackref.otherModels, entityDtoBackref)
//                    ////}
//                    if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
//                        bodyBuilder.beginControlFlow("for (%L in %L)", i.sourceVarName, i.sourceVarName + "s")
//                    }
//                    bodyBuilder.addStatement(
//                        "%T.%L(%L.%L%L, %L) /* , otherBackref1, otherBackref2) */",
//                        propCrud(this.modelSubElementRef, CRUD.CREATE),
//                        "batchInsertDb",
//                        i.sourceVarName,
//                        prop.name(),
//                        " ?: emptyList()",
//                        i.sourceVarName
//                    )
//                    if (funNameInsertOrBatch.originalFunName.startsWith("batch")) {
//                        bodyBuilder.endControlFlow()
//                    }
//                },
//                isModelCollection = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} COLLECTION of %T", prop.poetType)
//
//                    addSyntheticCrud(this, this@KotlinCrudExposed.currentCrudData, via = "KotlinCrud for prop: '$prop' from currentFillerData: ${currentCrudData}")
//                },
//                isModelIterable = {
//                    none = false
//                    bodyBuilder.addStatement("// not yet implemented ${prop.name()} ITERABLE of %T", prop.poetType)
//
//                    addSyntheticCrud(this, this@KotlinCrudExposed.currentCrudData, via = "KotlinCrud for prop: '$prop' from currentFillerData: ${currentCrudData}")
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
//        var body = bodyBuilder.build()
//        return body
    }

    private fun readBySelect(i: IntersectPropertys.CommonPropData) {
        val tableClassModel: KotlinClassModelTable = kotlinGenCtx.kotlinGenClass(i.targetGenModel.modelSubElRef) as KotlinClassModelTable
        val outgoingFKs = tableClassModel.outgoingFKs
        val incomingFKs = tableClassModel.incomingFKs

        // add needed syntheticCruds (if not specified in DSL), we'll also need the Fillers for them, too
        for (propEither in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }.map { it.eitherTypModelOrClass }) {
            if (propEither is EitherTypOrModelOrPoetType.EitherModel) {
                val propSubElRef = propEither.modelSubElementRef
                genCtx.addSyntheticCrudData(SynthCrudData.create(DslRef.table(propSubElRef.simpleName, propSubElRef.parentDslRef), DslRef.dto(propSubElRef.simpleName, propSubElRef.parentDslRef), currentCrudData, "via insert"))
            }
        }

        var funName: FunName
        var funSpec: FunSpec.Builder
        // ============================
        // fun readBySelect
        // ============================
        funName = funNameExpanded("readBySelect", currentCrudData)
        @OptIn(ExperimentalKotlinPoetApi::class)
        funSpec = FunSpec.builder(funName.funName).contextReceivers(DB.TransactionClassName)
            .addParameter("selectLambda", LambdaTypeName.get(receiver = DB.SqlExpressionBuilderClassName, parameters = emptyList(), returnType = DB.OpClassName.parameterizedBy(BOOLEAN)))
            .returns(List::class.asTypeName().parameterizedBy(i.sourcePoetType))
        funSpec
            .addStatement("val query: %T = %L.%M(selectLambda)", DB.QueryClassName, i.targetPoetType, DB.selectMember)
            .addComment("execute query against DB")
            .addStatement("val resultRowList: %T = query.toList()", List::class.asTypeName().parameterizedBy(DB.ResultRowClassName))
            .addStatement("val selected%L = mutableListOf<%T>()", i.sourcePoetType.simpleName, i.sourcePoetType)
            .beginControlFlow("for (rr in resultRowList)")
            .addStatement("val %L = %T.%L(rr)", i.sourceGenModel.asVarName, GenClassNames.fillerFor(i.sourceGenModel.modelSubElRef, MODELREFENUM.TABLE), GenNaming.createFromTableFunName(currentCrudData, i.sourceGenModel.modelClassName))
            .addComment("one2One models")
            .readOne2OneModels(outgoingFKs, i, funName, tableClassModel)
            .addComment("many2One models")
            .readMany2OneModels(incomingFKs, i, funName, tableClassModel)
            .addComment("add")
            .addStatement("selected%L.add(%L)", i.sourcePoetType.simpleName, i.sourceGenModel.asVarName)
            .endControlFlow()
            .addStatement("return selected%L", i.sourcePoetType.simpleName)
        builder.addFunction(funSpec.build())
    }
    private fun FunSpec.Builder.readOne2OneModels(outgoingFKs: MutableSet<FK>, i: IntersectPropertys.CommonPropData, funName: FunName, tableClassModel: KotlinClassModelTable): FunSpec.Builder {
        var none = true
        for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    beginControlFlow("val %L = %T.%L",
                        fk.toProp.name(),
                        GenClassNames.crudFor(fk.toTableRef, CrudData.CRUD.READ),
                        funName.funName
                    )
                    val toPropKotlinClassModelTable = kotlinGenCtx.kotlinGenClass(fk.toTableRef) as KotlinClassModelTable
                    addStatement("%T.%L eq rr[%T.%L]", toPropKotlinClassModelTable.modelClassData.poetType, RuntimeDefaults.UUID_PROPNAME, tableClassModel.modelClassData.poetType, GenNaming.fkPropVarNameUUID(fk))
                    endControlFlow()
                    addStatement(".firstOrNull()")
                    addStatement("%L.%L = %L!!", i.sourceGenModel.asVarName, fk.toProp.name(), fk.toProp.name())
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) addComment("NONE")
        return this
    }
    private fun FunSpec.Builder.readMany2OneModels(incomingFKs: MutableSet<FK>, i: IntersectPropertys.CommonPropData, funName: FunName, tableClassModel: KotlinClassModelTable): FunSpec.Builder {
        var none = true
        for (fk in incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    beginControlFlow("val %L = %T.%L", fk.toProp.name(), GenClassNames.crudFor(fk.fromTableRef, CrudData.CRUD.READ), funName.funName)
                    val toPropKotlinClassModelTable = kotlinGenCtx.kotlinGenClass(fk.fromTableRef) as KotlinClassModelTable
                    addStatement("%T.%L eq rr[%T.%L]", toPropKotlinClassModelTable.modelClassData.poetType, GenNaming.fkPropVarNameUUID(fk), tableClassModel.modelClassData.poetType, RuntimeDefaults.UUID_PROPNAME)
                    endControlFlow()
                    addStatement("%L.%L%L.addAll(%L)", i.sourceGenModel.asVarName, fk.toProp.name(), if (fk.toProp.isNullable) "?" else "", fk.toProp.name())
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) addComment("NONE")
        return this
    }

    private fun readByJoinDb(i: IntersectPropertys.CommonPropData) {
        val tableClassModel: KotlinClassModelTable = kotlinGenCtx.kotlinGenClass(i.targetGenModel.modelSubElRef) as KotlinClassModelTable
        val outgoingFKs = tableClassModel.outgoingFKs
        val incomingFKs = tableClassModel.incomingFKs

        // add needed syntheticCruds (if not specified in DSL), we'll also need the Fillers for them, too
        for (propEither in i.allIntersectPropSet.filter { Tag.TRANSIENT !in it.tags }.map { it.eitherTypModelOrClass }) {
            if (propEither is EitherTypOrModelOrPoetType.EitherModel) {
                val propSubElRef = propEither.modelSubElementRef
                genCtx.addSyntheticCrudData(SynthCrudData.create(DslRef.table(propSubElRef.simpleName, propSubElRef.parentDslRef), DslRef.dto(propSubElRef.simpleName, propSubElRef.parentDslRef), currentCrudData, "via insert"))
            }
        }

        var funName: FunName
        var funSpec: FunSpec.Builder
        //if (incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }.size + outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }.size > 0) {
        val joinProps = incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE } + outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }
        val ignoredJoinProps = joinProps.flatMap { propHasBoundry(it.toProp) }.filter { it.copyType == COPYTYPE.IGNORE }
        if (joinProps.size > ignoredJoinProps.size) {
            // ============================
            // fun simpleEntityTableJoin
            // ============================
            funName = funNameExpanded(i.targetGenModel.asVarName + "Join", currentCrudData)
            funSpec = FunSpec.builder(funName.funName).addModifiers(KModifier.PRIVATE)
                .returns(DB.JoinClassName)
            funSpec
                .addStatement("val join: Join = %T", i.targetPoetType)
                .addJoinsForOne2OneModels(outgoingFKs, i)
                .addJoinsForMany2OneModels(incomingFKs, i)
                .addStatement("return join")
            builder.addFunction(funSpec.build())
            // ============================
            // fun execToDb
            // ============================
            funName = funNameExpanded("execToDb", currentCrudData)
            funSpec = FunSpec.builder(funName.funName).addModifiers(KModifier.PRIVATE)
                .addParameter("selectLambda", LambdaTypeName.get(receiver = DB.SqlExpressionBuilderClassName, parameters = emptyList(), returnType = DB.OpClassName.parameterizedBy(BOOLEAN)))
                .returns(List::class.asTypeName().parameterizedBy(DB.ResultRowClassName))
            funSpec
                .addStatement("val join: Join = %L()", funName.swapOutOriginalFunNameWith(i.targetGenModel.asVarName + "Join"))
                .addStatement("val query: %T = join.%M(selectLambda)", DB.QueryClassName, DB.selectMember)
                .addComment("execute query against DB")
                .addStatement("val resultRowList: List<%T> = query.toList()", DB.ResultRowClassName)
                .addStatement("return resultRowList")
            builder.addFunction(funSpec.build())
            // ============================
            // fun readByJoin
            // ============================
            funName = funNameExpanded("readByJoin", currentCrudData)
            @OptIn(ExperimentalKotlinPoetApi::class)
            funSpec = FunSpec.builder(funName.funName).contextReceivers(DB.TransactionClassName)
                .addParameter("selectLambda", LambdaTypeName.get(receiver = DB.SqlExpressionBuilderClassName, parameters = emptyList(), returnType = DB.OpClassName.parameterizedBy(BOOLEAN)))
                .returns(List::class.asTypeName().parameterizedBy(i.sourcePoetType))
            funSpec
                .addStatement("val resultRowList: List<%T> = %L(selectLambda)", DB.ResultRowClassName, funName.swapOutOriginalFunNameWith("execToDb"))
                .addComment("unmarshalling _within_ transaction scope")
                .addStatement("val selected%L = %L(resultRowList)", i.sourcePoetType.simpleName, funName.swapOutOriginalFunNameWith("unmarshall${i.sourcePoetType.simpleName}s"))
                .addStatement("return selected%L", i.sourcePoetType.simpleName)
            builder.addFunction(funSpec.build())
            // ============================
            // fun readByJoinNewTransaction
            // ============================
            funName = funNameExpanded("readByJoinNewTransaction", currentCrudData)
            @OptIn(ExperimentalKotlinPoetApi::class)
            funSpec = FunSpec.builder(funName.funName).contextReceivers(DB.TransactionClassName)
                .addParameter("db", DB.DatabaseClassName.nullable())
                .addParameter("selectLambda", LambdaTypeName.get(receiver = DB.SqlExpressionBuilderClassName, parameters = emptyList(), returnType = DB.OpClassName.parameterizedBy(BOOLEAN)))
                .returns(List::class.asTypeName().parameterizedBy(i.sourcePoetType))
            funSpec
                .beginControlFlow("val resultRowList: List<%T> = %M(db = db)", DB.ResultRowClassName, DB.transactionMember)
                .addStatement("%M(%T)", DB.transactionAddLoggerMember, DB.StdOutSqlLoggerClassName)
                .addStatement("execToDb(selectLambda)")
                .endControlFlow()
                .addComment("unmarshalling _outside_ transaction scope")
                .addStatement("val selected%L = %L(resultRowList)", i.sourcePoetType.simpleName, funName.swapOutOriginalFunNameWith("unmarshall${i.sourcePoetType.simpleName}s"))
                .addStatement("return selected%L", i.sourcePoetType.simpleName)
            builder.addFunction(funSpec.build())
        }


        // ============================
        // private fun unmarshall
        // ============================
        funName = funNameExpanded("unmarshall${i.sourcePoetType.simpleName}s", currentCrudData)
        funSpec = FunSpec.builder(funName.funName).addModifiers(KModifier.PRIVATE)
            .addParameter("resultRowList", List::class.asTypeName().parameterizedBy(DB.ResultRowClassName))
            .returns(List::class.asTypeName().parameterizedBy(i.sourcePoetType))
            .addStatement("val read%Ls = mutableListOf<%T>()", i.sourceGenModel.poetTypeSimpleName, i.sourceGenModel.poetType)
            .addComment("base model NULL")
            .addStatement("var current%L: %T = %T.NULL", i.sourceGenModel.poetTypeSimpleName, i.sourcePoetType, i.sourcePoetType)
            .addComment("many2One models NULL")
            .addMany2OneNulls(incomingFKs)
            // unmarshall
            .addStatement("val iter = resultRowList.iterator()")
            .addModels(incomingFKs, outgoingFKs, i)
            .addStatement("return read%Ls", i.sourceGenModel.poetTypeSimpleName)
        builder.addFunction(funSpec.build())
    }

    private fun FunSpec.Builder.addMany2OneNulls(incomingFKs: MutableSet<FK>): FunSpec.Builder {
        var none = true
        for (fk in incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    addStatement("var current%L = %T.NULL", fk.toProp.name(), fk.toProp.poetType)
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) addComment("NONE")
        return this
    }

    private fun FunSpec.Builder.addModels(incomingFKs: MutableSet<FK>, outgoingFKs: MutableSet<FK>, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        this.beginControlFlow("while (iter.hasNext())")
            .addStatement("val rr: ResultRow = iter.next()")
        // if (rr[SimpleEntityTable.uuid] != currentSimpleEntityDto.uuid) {
        beginControlFlow("if (rr[%T.%L] != current%L.%L)", i.targetPoetType, RuntimeDefaults.UUID_PROPNAME, i.sourceGenModel.poetTypeSimpleName, RuntimeDefaults.UUID_PROPNAME)
        this.addComment("base model")
        this.addStatement("current%L = %T.%L(rr)", i.sourceGenModel.poetTypeSimpleName, GenClassNames.fillerFor(i.sourceGenModel.modelSubElRef, MODELREFENUM.TABLE), GenNaming.createFromTableFunName(currentCrudData, i.sourceGenModel.modelClassName))
            .addStatement("read%Ls.add(current%L)", i.sourceGenModel.poetTypeSimpleName, i.sourceGenModel.poetTypeSimpleName)
        this.addComment("one2One models")
        var none = true
        for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    this.addStatement("current%L.%L = %T.%L(rr)", i.sourceGenModel.poetTypeSimpleName, fk.toProp.name(), GenClassNames.fillerFor(fk.toTableRef, MODELREFENUM.TABLE), GenNaming.createFromTableFunName(currentCrudData, fk.toProp.eitherTypModelOrClass.modelClassName))
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) this.addComment("NONE")
        endControlFlow()
        this.addComment("many2One models")
        none = true
        for (fk in incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    val toPropTableGenModel = genCtx.genModel(fk.fromTableRef)
                    //val toPropDtoGenModel = genCtx.genModel(DslRef.dto(C.DEFAULT, toPropTableGenModel.modelSubElRef.parentDslRef))
                    beginControlFlow("if (rr[%T.%L] != current%L.%L)", toPropTableGenModel.poetType, RuntimeDefaults.UUID_PROPNAME, fk.toProp.name(), RuntimeDefaults.UUID_PROPNAME)
                    addStatement("current%L = %T.%L(rr)", fk.toProp.name(), GenClassNames.fillerFor(fk.fromTableRef, MODELREFENUM.TABLE), GenNaming.createFromTableFunName(currentCrudData, fk.toProp.eitherTypModelOrClass.modelClassName))
                    addStatement("current%L.%L%L.add(current%L)", i.sourceGenModel.poetTypeSimpleName, fk.toProp.name(), if (fk.toProp.isNullable) "?" else "", fk.toProp.name())
                    endControlFlow()
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) this.addComment("NONE")
        endControlFlow() // of if/while
        return this
    }

    private fun FunSpec.Builder.addJoinsForOne2OneModels(outgoingFKs: MutableSet<FK>, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        this.addComment("one2One models")
        var none = true
        for (fk in outgoingFKs.filter { it.toProp.collectionType == COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    val toPropKotlinClassModelTable = kotlinGenCtx.kotlinGenClass(fk.toTableRef) as KotlinClassModelTable
                    //val toPropTableGenModel = genCtx.genModel(fk.toTableRef)
                    //val toPropDtoGenModel = genCtx.genModel(DslRef.dto(C.DEFAULT, toPropTableGenModel.modelSubElRef.parentDslRef))
                    addStatement(".join(%T, %T.LEFT, %T.%L, %T.%L)", toPropKotlinClassModelTable.modelClassData.poetType, DB.JoinTypeClassName, i.targetPoetType, GenNaming.fkPropVarNameUUID(fk), toPropKotlinClassModelTable.modelClassData.poetType, RuntimeDefaults.UUID_PROPNAME)
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) addComment("NONE")
        return this
    }
    private fun FunSpec.Builder.addJoinsForMany2OneModels(incomingFKs: MutableSet<FK>, i: IntersectPropertys.CommonPropData): FunSpec.Builder {
        this.addComment("one2One models")
        var none = true
        for (fk in incomingFKs.filter { it.toProp.collectionType != COLLECTIONTYP.NONE }) {
            none = false
            propBoundry(fk.toProp,
                noPropBoundry = {
                    val toPropKotlinClassModelTable = kotlinGenCtx.kotlinGenClass(fk.fromTableRef) as KotlinClassModelTable
                    addStatement(".join(%T, %T.LEFT, %T.%L, %T.%L)", toPropKotlinClassModelTable.modelClassData.poetType, DB.JoinTypeClassName, i.targetPoetType, RuntimeDefaults.UUID_PROPNAME, toPropKotlinClassModelTable.modelClassData.poetType, GenNaming.fkPropVarNameUUID(fk))
                },
                IGNORE = { copyBoundry -> addComment("${fk.toProp.propTypeSimpleNameCap} copyBoundry ${copyBoundry.copyType} ${copyBoundry.boundryType} ${fk.toProp.name()}") },
                ELSE = { copyBoundry -> addComment("TODO ${copyBoundry.copyType} ${fk.toProp.name()} ${fk.toProp.propTypeSimpleNameCap} of ${fk.toProp.poetType}") },
            )
        }
        if (none) addComment("NONE")
        return this
    }
}
