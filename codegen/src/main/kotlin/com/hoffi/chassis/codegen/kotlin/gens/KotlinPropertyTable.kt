package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.CollectionTypWrapper
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_INT
import com.hoffi.chassis.chassismodel.typ.TYP.Companion.DEFAULT_VARCHAR_LENGTH
import com.hoffi.chassis.chassismodel.typ.immutable
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.codegen.kotlin.whens.WhensGen
import com.hoffi.chassis.dbwrappers.exposed.DB_EXPOSED.ColumnClassName
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec

context(GenCtxWrapper)
class KotlinPropertyTable(property: Property, genModel: ModelClassData) : AKotlinProperty(property, genModel) {
    override val builder: PropertySpec.Builder = whenInit()

//    init {
//        if (property.initializer.format.count { it == '%' } != property.initializer.args.size) {
//            throw Exception("$property imbalanced number of initializer format variables and given args: ${property.initializer} in $property")
//        }
//        val initializerCodeBlockBuilder = CodeBlock.builder()
//        when (property.collectionType) {
//            is COLLECTIONTYP.NONE -> {
//                translateTypeForDB()
//                if (property.eitherTypModelOrClass is EitherTypOrModelOrPoetType.EitherModel) {
//                    builder = PropertySpec.builder(property.name, ColumnClassName.parameterizedBy(RuntimeDefaults.classNameUUID), property.modifiers)
//                } else {
//                    builder = PropertySpec.builder(property.name, ColumnClassName.parameterizedBy(property.poetType), property.modifiers)
//                }
//                if (property.initializer.hasOriginalInitializer()) {
//                    initializerCodeBlockBuilder.add(property.initializer.codeBlockFull())
//                    builder.initializer(initializerCodeBlockBuilder.build())
//                } else if (Tag.NO_DEFAULT_INITIALIZER !in property.tags) {
//                    val eitherTypOfProp = property.eitherTypModelOrClass
//                    val defaultInitializer = when (eitherTypOfProp) {
//                        is EitherTypOrModelOrPoetType.EitherModel -> Initializer.of("%T.%L", eitherTypOfProp.modelClassName.poetType, "NULL")
//                        is EitherTypOrModelOrPoetType.EitherPoetType -> Initializer.of("%T.%L", eitherTypOfProp.modelClassName.poetType, "NULL") //Initializer.of("%T()", eitherTypOfProp.modelClassName.poetType)
//                        is EitherTypOrModelOrPoetType.EitherTyp -> eitherTypOfProp.typ.defaultInitializer
//                        is EitherTypOrModelOrPoetType.NOTHING -> { throw GenException("should not be NOTHING, something went terribly wrong!") }
//                    }
//                    initializerCodeBlockBuilder.add(defaultInitializer.codeBlockFull())
//                    if (property.initializer.hasInitializerAddendum()) {
//                        initializerCodeBlockBuilder.add(property.initializer.codeBlockAddendum())
//                    }
//                    builder.initializer(initializerCodeBlockBuilder.build())
//                }
//            }
//            is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE, is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET -> {
//                val collMutable = if (Tag.COLLECTION_IMMUTABLE in property.tags) immutable else mutable
//                val collCollectionTypWrapper = CollectionTypWrapper.of(property.collectionType, collMutable, property.isNullable, property.poetType)
//                builder = PropertySpec.builder(property.name, collCollectionTypWrapper.typeName, property.modifiers)
//                if (Tag.NO_DEFAULT_INITIALIZER !in property.tags) {
//                    initializerCodeBlockBuilder.add(Initializer.of(collCollectionTypWrapper.initializer.format, collCollectionTypWrapper.initializer.args).codeBlockFull())
//                    if (property.initializer.hasInitializerAddendum()) {
//                        initializerCodeBlockBuilder.add(property.initializer.codeBlockAddendum())
//                    }
//                    builder.initializer(initializerCodeBlockBuilder.build())
//                }
//            }
//        }
//        if (property.mutable.bool) builder.mutable() // val or var
//        //if (Tag.NULLABLE in property.tags) builder = builder.cop decide if either the generic type is nullable or the collection itself
//    }

    fun whenInit(): PropertySpec.Builder {
        lateinit var initBuilder: PropertySpec.Builder
        val initializerCodeBlockBuilder = CodeBlock.builder()
        WhensGen.whenTypeAndCollectionType(property.eitherTypModelOrClass, property.collectionType,
            preFunc = { },
            preNonCollection = {

                translateTypeForDB()

                if (property.initializer.hasOriginalInitializer()) {
                    initializerCodeBlockBuilder.add(property.initializer.codeBlockFull())
                }
            },
            preCollection = {
                val collMutable = if (Tag.COLLECTION_IMMUTABLE in property.tags) immutable else mutable
                val collCollectionTypWrapper = CollectionTypWrapper.of(property.collectionType, collMutable, property.isNullable, property.poetType)
                initBuilder = PropertySpec.builder(property.name, collCollectionTypWrapper.typeName, property.modifiers)
                if (Tag.NO_DEFAULT_INITIALIZER !in property.tags) {
                    initializerCodeBlockBuilder.add(Initializer.of(collCollectionTypWrapper.initializer.format, collCollectionTypWrapper.initializer.args).codeBlockFull())
                }
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
        //if (property.initializer.hasInitializerAddendum()) {
        //    initializerCodeBlockBuilder.add(property.initializer.codeBlockAddendum())
        //}
        initBuilder.initializer(initializerCodeBlockBuilder.build())
        return initBuilder
    }

    fun translateTypeForDB() {
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
            }

            is EitherTypOrModelOrPoetType.NOTHING -> throw GenException("$this without ${EitherTypOrModelOrPoetType::class.simpleName}")
        }
    }

}
