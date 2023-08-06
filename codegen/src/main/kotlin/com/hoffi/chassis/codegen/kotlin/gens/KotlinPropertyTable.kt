package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.PoetHelpers.nullable
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_INT
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_VARCHAR_LENGTH
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.whens.WhensGen
import com.hoffi.chassis.dbwrappers.exposed.DB_EXPOSED.ColumnClassName
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName

context(GenCtxWrapper)
class KotlinPropertyTable(property: Property, val genModel: KotlinClassModelTable) : AKotlinProperty(property, genModel.modelClassData) {
    override val builder: PropertySpec.Builder = whenInit()

    private fun whenInit(): PropertySpec.Builder {
        lateinit var initBuilder: PropertySpec.Builder
        val initializerCodeBlockBuilder = CodeBlock.builder()
        WhensGen.whenTypeAndCollectionType(property.eitherTypModelOrClass, property.collectionType,
            preFunc = { },
            preNonCollection = {

                translateTypeForDB() // this does alter the property and does NOT create a new one!

                if (property.initializer.hasOriginalInitializer()) {
                    initializerCodeBlockBuilder.add(property.initializer.codeBlockFull())
                }
            },
            preCollection = {
            },
            isModel = {
                initBuilder = PropertySpec.builder(property.name, ColumnClassName.parameterizedBy(RuntimeDefaults.classNameUUID), property.modifiers)
                if ( ( ! property.initializer.hasOriginalInitializer()) && (Tag.NO_DEFAULT_INITIALIZER !in property.tags) ) {
                    val defaultInitializer = Initializer.of("%T.%L", this.modelClassName.poetType, "NULL")
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                }
            },
            isPoetType = {
                initBuilder = PropertySpec.builder(property.name, ColumnClassName.parameterizedBy(property.poetType), property.modifiers)
                if ( ( ! property.initializer.hasOriginalInitializer()) && (Tag.NO_DEFAULT_INITIALIZER !in property.tags) ) {
                    val defaultInitializer = Initializer.of("%T.%L", this.modelClassName.poetType, "NULL")
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                }
            },
            isTyp = {
                initBuilder = PropertySpec.builder(property.name, ColumnClassName.parameterizedBy(property.poetType), property.modifiers)
                if ( ( ! property.initializer.hasOriginalInitializer()) && (Tag.NO_DEFAULT_INITIALIZER !in property.tags) ) {
                    val defaultInitializer = this.typ.defaultInitializer
                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
                }
            },
            postNonCollection = { },
            isModelList = {
                //val reffedTable_DTO_GenModel: GenModel = genCtx.genModel(this.modelSubElementRef)
                val reffedTableDslRef = DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)
                val reffedTable: GenModel = genCtx.genModel(reffedTableDslRef)
                val fk = genModel.addFK(
                    fromTableRef = reffedTableDslRef,
                    //toTable = kotlinGenCtx.kotlinGenClass(DslRef.table(C.DEFAULT, this@KotlinPropertyTable.modelClassData.modelSubElRef.parentDslRef)),
                    toTable = this@KotlinPropertyTable.genModel,
                    this@KotlinPropertyTable.property,
                    COLLECTIONTYP.LIST // <-- differs
                )
                initBuilder = PropertySpec.builder(property.name, Any::class.asTypeName().nullable())
                initializerCodeBlockBuilder.add("mappedBy(%T::%L)", reffedTable.poetType, fk.varName) // placeholder property TODO let's see if exposed explodes on this
            },
            isModelSet = {
                //val reffedTable_DTO_GenModel: GenModel = genCtx.genModel(this.modelSubElementRef)
                val reffedTableDslRef = DslRef.table(C.DEFAULT, this.modelSubElementRef.parentDslRef)
                val reffedTable: GenModel = genCtx.genModel(reffedTableDslRef)
                val fk = genModel.addFK(
                    fromTableRef = reffedTableDslRef,
                    //toTable = kotlinGenCtx.kotlinGenClass(DslRef.table(C.DEFAULT, this@KotlinPropertyTable.modelClassData.modelSubElRef.parentDslRef)),
                    toTable = this@KotlinPropertyTable.genModel,
                    this@KotlinPropertyTable.property,
                    COLLECTIONTYP.SET // <-- differs
                )
                initBuilder = PropertySpec.builder(property.name, Any::class.asTypeName().nullable())
                initializerCodeBlockBuilder.add("mappedBy(%T::%L)", reffedTable.poetType, fk.varName) // placeholder property TODO let's see if exposed explodes on this
            },
            isModelCollection = {
                TODO()
            },
            isModelIterable = {
                TODO()
            },
            isPoetTypeList = {
                initBuilder = PropertySpec.builder(property.name, Any::class.asTypeName().nullable()) // TODO()
                    .addKdoc("not implemented yet")
                initializerCodeBlockBuilder.add("null")
            },
            isPoetTypeSet = {
                initBuilder = PropertySpec.builder(property.name, Any::class.asTypeName().nullable()) // TODO()
                    .addKdoc("not implemented yet")
                initializerCodeBlockBuilder.add("null")
            },
            isPoetTypeCollection = { },
            isPoetTypeIterable = { },
            isTypList = {
                initBuilder = PropertySpec.builder(property.name, Any::class.asTypeName().nullable()) // TODO()
                    .addKdoc("not implemented yet")
                initializerCodeBlockBuilder.add("null")
            },
            isTypSet = {
                initBuilder = PropertySpec.builder(property.name, Any::class.asTypeName().nullable()) // TODO()
                    .addKdoc("not implemented yet")
                initializerCodeBlockBuilder.add("null")
            },
            isTypCollection = {
                TODO()
            },
            isTypIterable = {
                TODO()
            },
            postCollection = { },
        )
        //if (property.initializer.hasInitializerAddendum()) {
        //    initializerCodeBlockBuilder.add(property.initializer.codeBlockAddendum())
        //}
        initBuilder.initializer(initializerCodeBlockBuilder.build())
        return initBuilder
    }

    private fun translateTypeForDB() {
        val eitherTypOrModelOrPoetType: EitherTypOrModelOrPoetType = property.eitherTypModelOrClass
        when (eitherTypOrModelOrPoetType) {
            is EitherTypOrModelOrPoetType.EitherTyp -> {
                val (dbTypeName, dbInitializer) = DB.coreTypeTranslation(eitherTypOrModelOrPoetType.typ)
                dbInitializer.originalArgs.add(property.columnName)
                if (eitherTypOrModelOrPoetType.typ == TYP.STRING) {
                    dbInitializer.originalArgs.add(if (property.length == DEFAULT_INT) DEFAULT_VARCHAR_LENGTH else property.length)
                }
                eitherTypOrModelOrPoetType.initializer.originalFormat = dbInitializer.originalFormat
                eitherTypOrModelOrPoetType.initializer.originalArgs.clear()
                eitherTypOrModelOrPoetType.initializer.originalArgs.addAll(dbInitializer.originalArgs)
            }
            is EitherTypOrModelOrPoetType.EitherPoetType -> {
                log.warn("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
                log.warn("XXXXXXX      translateTypeForDB for typ CLASS/PoetType ${property}")
                log.warn("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            }
            is EitherTypOrModelOrPoetType.EitherModel -> {
                //public var someModelObject: Column<UUID> = uuid("SimpleSubentity_uuid").uniqueIndex().references(SimpleSubentityTable.uuid)
                val (nullQM, nullFunc) = if (Tag.NULLABLE in property.tags) Pair("?", ".nullable()") else Pair("", "")
                eitherTypOrModelOrPoetType.initializer.originalFormat = "uuid(%S$nullQM).uniqueIndex().references(%T.%L)$nullFunc"
                eitherTypOrModelOrPoetType.initializer.originalArgs.clear()
                eitherTypOrModelOrPoetType.initializer.originalArgs.add(property.columnName)
                val correspondingTable = genCtx.genModel(DslRef.table(C.DEFAULT, eitherTypOrModelOrPoetType.modelSubElementRef.parentDslRef))
                eitherTypOrModelOrPoetType.initializer.originalArgs.add(correspondingTable.modelClassName.poetType)
                eitherTypOrModelOrPoetType.initializer.originalArgs.add(UUID_PROPNAME)

                eitherTypOrModelOrPoetType.modelSubElementRefExpanded = DslRef.table(C.DEFAULT, eitherTypOrModelOrPoetType.modelSubElementRef.parentDslRef)
            }

            is EitherTypOrModelOrPoetType.NOTHING -> throw GenException("$this without ${EitherTypOrModelOrPoetType::class.simpleName}")
        }
    }

}
