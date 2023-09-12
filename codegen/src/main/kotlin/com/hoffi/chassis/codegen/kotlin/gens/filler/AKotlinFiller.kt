package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.IntersectPropertys
import com.hoffi.chassis.codegen.kotlin.gens.ABaseForCrudAndFiller
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGeneratedFiller
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.AHasCopyBoundrysData
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.squareup.kotlinpoet.*
import okio.Path

context(GenCtxWrapper)
abstract class AKotlinFiller(fillerData: FillerData, modelkind: MODELKIND): ABaseForCrudAndFiller(fillerData, modelkind) {
    private val fillerDataTargetDslRef = fillerData.targetDslRef
    var currentFillerData: FillerData = fillerData // changes for every build() call, also helpfull for debugging
    override fun toString() = "${this::class.simpleName}(current${currentFillerData})"
    /** fillerName -> source(!) DslRef (as each targetDslRef has its own KotlinFiller Instance */
    val alreadyCreatedCruds: MutableSet<AHasCopyBoundrysData> = mutableSetOf()
    fun alreadyCreated(fillerData: FillerData) = ! alreadyCreatedCruds.add(fillerData)

    var fillerBasePath: Path
    var fillerPath: Path
    val fillerPoetType: ClassName

    init {
        when (modelkind) {
            MODELKIND.DTOKIND -> {
                val targetGenModel = genCtx.genModelFromDsl(fillerData.targetDslRef)
                fillerBasePath = targetGenModel.modelClassName.basePath
                fillerPath =     targetGenModel.modelClassName.path
                fillerPoetType = targetGenModel.fillerPoetType
            }
            MODELKIND.TABLEKIND -> {
                if (fillerData.targetDslRef !is DslRef.table) {
                    val sourceGenModel = genCtx.genModelFromDsl(fillerData.sourceDslRef)
                    fillerBasePath = sourceGenModel.modelClassName.basePath
                    fillerPath =     sourceGenModel.modelClassName.path
                    fillerPoetType = sourceGenModel.fillerPoetType
                } else {
                    val targetGenModel = genCtx.genModelFromDsl(fillerData.targetDslRef)
                    fillerBasePath = targetGenModel.modelClassName.basePath
                    fillerPath =     targetGenModel.modelClassName.path
                    fillerPoetType = targetGenModel.fillerPoetType
                }
            }
        }
    }

    val builder = TypeSpec.objectBuilder(fillerPoetType).apply {
        kdocGeneratedFiller(genCtx, fillerData)
        addSuperinterface(RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
    }

    init {
        log.info("created $this")
        kotlinGenCtx.putKotlinFillerClass(modelkind, fillerData, this)
    }

    abstract fun build(modelkind: MODELKIND, fillerData: FillerData)


    fun generate(out: Appendable? = null): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(fillerPoetType)
        val typeSpec = builder.build()
        val fileSpec = fileSpecBuilder.addType(typeSpec).build()
        if (out != null) {
            fileSpec.writeTo(out)
        } else {
            try {
                val targetPathWithoutPackageAndFile = (fillerBasePath/fillerPath).toNioPath()
                log.info("writing: $targetPathWithoutPackageAndFile/${fillerPoetType.toString().replace('.', '/')}.kt")
                fileSpec.writeTo(targetPathWithoutPackageAndFile)
            } catch (e: Exception) {
                throw GenException(e.message ?: "unknown error", e)
            }
        }
        return typeSpec
    }

    fun nullSentinel(funBuilder: FunSpec.Builder, targetVarName: String, targetModel: GenModel) {
        if (KModifier.ABSTRACT !in targetModel.classModifiers) {
            funBuilder.addStatement("if (%L === %T.NULL) throw Exception(%S)", targetVarName, targetModel.poetType, "cannot clone/copy into companion.NULL")
        }
    }

    protected fun cloneFillerFunctions(i: IntersectPropertys.CommonPropData) {
        if (KModifier.ABSTRACT in i.targetGenModelFromDsl.classModifiers) return
        val allPKs = i.targetGenModelFromDsl.propsInclSuperclassPropsMap.values.filter { Tag.PRIMARY in it.tags }
        if (allPKs.isNotEmpty()) {
            val funNamePairs = listOf(
                "cloneShallowIgnoreModels" to "copyShallowIgnoreModelsInto",
                "cloneShallowTakeSameModels" to "copyShallowAndTakeSameModelsInto",
                "cloneWithNewModels" to "copyShallowWithNewModelsInto"
            )
            for (funNamePair in funNamePairs) {
                val funSpecBuilder = FunSpec.builder(funNamePair.first)
                    .addParameter(ParameterSpec.builder(i.sourceVarName, i.sourceGenModelFromDsl.poetType).build())
                    .returns(i.targetGenModelFromDsl.poetType)
                if (allPKs.size == 1 && allPKs[0].dslPropName == RuntimeDefaults.UUID_PROPNAME) {
                    funSpecBuilder.addStatement("val %L = %T._internal_create()", i.targetVarName, i.sourceGenModelFromDsl.poetType)
                } else {
                    funSpecBuilder.addStatement("val %L = %T._internal_create()", i.targetVarName, i.sourceGenModelFromDsl.poetType)
                    for (pkProp in allPKs) {
                        funSpecBuilder.addStatement("%L.%L = %L.%L", i.targetVarName, pkProp.name(), i.sourceVarName, pkProp.name())
                    }
                }
                funSpecBuilder.addStatement("%L(%L, %L)", funNamePair.second, i.targetVarName, i.sourceVarName)
                funSpecBuilder.addStatement("return %L", i.targetVarName)
                builder.addFunction(funSpecBuilder.build())
            }
        }
    }

    protected fun clearCollection(targetVarName: String, prop: Property): CodeBlock {
        val c = if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else ""
        return if (Tag.NULLABLE in prop.tags) {
            buildCodeBlock { addStatement("$c%L.%L?.clear()", targetVarName, prop.name()) }
        } else {
            buildCodeBlock { addStatement("$c%L.%L.clear()", targetVarName, prop.name()) }
        }
    }
    protected fun addAll(targetVarName: String, prop: Property, sourceVarName: String): CodeBlock {
        val c = if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else ""
        return when (prop.collectionType) {
            is COLLECTIONTYP.NONE -> buildCodeBlock {  }
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                if (Tag.NULLABLE in prop.tags) {
                    buildCodeBlock { addStatement("$c%L.%L?.addAll(%L.%L ?: emptyList())", targetVarName, prop.name(), sourceVarName, prop.name()) }
                } else {
                    buildCodeBlock { addStatement("$c%L.%L.addAll(%L.%L)", targetVarName, prop.name(), sourceVarName, prop.name()) }
                }
            }
            is COLLECTIONTYP.SET -> {
                if (Tag.NULLABLE in prop.tags) {
                    buildCodeBlock { addStatement("$c%L.%L?.addAll(%L.%L?.toList() ?: emptyList())", targetVarName, prop.name(), sourceVarName, prop.name()) }
                } else {
                    buildCodeBlock { addStatement("$c%L.%L.addAll(%L.%L.toList())", targetVarName, prop.name(), sourceVarName, prop.name()) }
                }
            }
        }
    }
    protected fun addAllMapped(targetVarName: String, prop: Property, sourceVarName: String, propEitherModelFillerClassName: ClassName, funName: String): CodeBlock {
        val c = if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else ""
        return when (prop.collectionType) {
            is COLLECTIONTYP.NONE -> buildCodeBlock {  }
            is COLLECTIONTYP.SET, is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                if (Tag.NULLABLE in prop.tags) {
                    buildCodeBlock {
                        addStatement("%L.%L?.addAll($c%L.%L?.map { %T.%L(it) } ?: emptyList())", targetVarName, prop.name(), sourceVarName, prop.name(), propEitherModelFillerClassName, funName)
                    }
                } else {
                    buildCodeBlock {
                        addStatement("%L.%L.addAll($c%L.%L.map { %T.%L(it) })", targetVarName, prop.name(), sourceVarName, prop.name(), propEitherModelFillerClassName, funName)
                    }
                }
            }
            //is COLLECTIONTYP.SET -> {
            //    if (Tag.NULLABLE in prop.tags) {
            //        buildCodeBlock {
            //            addStatement("$c%L.%L?.addAll(%L.%L?.map { %T.%L(it) }) ?: emptyList())", targetVarName, prop.name, sourceVarName, prop.name, propEitherModelFillerClassName, funName)
            //        }
            //    } else {
            //        buildCodeBlock {
            //            addStatement("$c%L.%L.addAll(%L.%L.map { %T.%L(it) })", targetVarName, prop.name, sourceVarName, prop.name, propEitherModelFillerClassName, funName)
            //        }
            //    }
            //}
        }
    }
}
