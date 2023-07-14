package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.gens.AKotlinClass
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGeneratedFiller
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.*
import org.slf4j.LoggerFactory

context(GenCtxWrapper)
abstract class AKotlinFiller(fillerData: FillerData, val modelrefenum: MODELREFENUM) {
    private val fillerDataToDslRef = fillerData.targetDslRef
    override fun toString() = "${this::class.simpleName}(${fillerDataToDslRef})"
    protected val log = LoggerFactory.getLogger(this::class.java)
    val targetGenModel: GenModel = genCtx.genModel(fillerData.targetDslRef)
    val toKotlinClass: AKotlinClass = kotlinGenCtx.kotlinGenClass(fillerData.targetDslRef)
    val toVarNamePostfix = (targetGenModel.poetType as ClassName).simpleName
    val alreadyCreated: MutableSet<IDslRef> = mutableSetOf()

    val fillerPoetTypeSimpleName = "Filler${targetGenModel.poetTypeSimpleName}"
    val poetTypeFiller = ClassName("${(targetGenModel.poetType as ClassName).packageName}.filler", fillerPoetTypeSimpleName)
    val builder = TypeSpec.objectBuilder(poetTypeFiller).apply {
        kdocGeneratedFiller(genCtx, fillerData)
        addSuperinterface(RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
    }

    init {
        kotlinGenCtx.putKotlinFillerClass(modelrefenum, fillerData, this)
    }

    abstract fun build(fillerData: FillerData)

    fun generate(out: Appendable? = null): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(poetTypeFiller)
        val typeSpec = builder.build()
        val fileSpec = fileSpecBuilder.addType(typeSpec).build()
        if (out != null) {
            fileSpec.writeTo(out)
        } else {
            try {
                fileSpec.writeTo((targetGenModel.modelClassName.basePath / targetGenModel.modelClassName.path).toNioPath())
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
        if (KModifier.ABSTRACT in targetGenModel.classModifiers) return
        val allPKs = targetGenModel.propsInclSuperclassPropsMap.values.filter { Tag.PRIMARY in it.tags }
        if (allPKs.isNotEmpty()) {
            val funNamePairs = listOf(
                "cloneShallowIgnoreModels" to "copyShallowIgnoreModelsInto",
                "cloneShallowTakeSameModels" to "copyShallowAndTakeSameModelsInto",
                "cloneWithNewModels" to "copyShallowWithNewModelsInto"
            )
            for (funNamePair in funNamePairs) {
                val funSpecBuilder = FunSpec.builder(funNamePair.first)
                    .addParameter(ParameterSpec.builder(i.sourceVarName, i.sourceGenModel.poetType).build())
                    .returns(targetGenModel.poetType)
                if (allPKs.size == 1 && allPKs[0].name == RuntimeDefaults.UUID_PROPNAME) {
                    funSpecBuilder.addStatement("val %L = %T._internal_create()", i.targetVarName, i.sourceGenModel.poetType)
                } else {
                    funSpecBuilder.addStatement("val %L = %T._internal_create()", i.targetVarName, i.sourceGenModel.poetType)
                    for (pkProp in allPKs) {
                        funSpecBuilder.addStatement("%L.%L = %L.%L", i.targetVarName, pkProp.name, i.sourceVarName, pkProp.name)
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
            buildCodeBlock { addStatement("$c%L.%L?.clear()", targetVarName, prop.name) }
        } else {
            buildCodeBlock { addStatement("$c%L.%L.clear()", targetVarName, prop.name) }
        }
    }
    protected fun addAll(targetVarName: String, prop: Property, sourceVarName: String): CodeBlock {
        val c = if (Tag.COLLECTION_IMMUTABLE in prop.tags) "// " else ""
        return when (prop.collectionType) {
            is COLLECTIONTYP.NONE -> buildCodeBlock {  }
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                if (Tag.NULLABLE in prop.tags) {
                    buildCodeBlock { addStatement("$c%L.%L?.addAll(%L.%L ?: emptyList())", targetVarName, prop.name, sourceVarName, prop.name) }
                } else {
                    buildCodeBlock { addStatement("$c%L.%L.addAll(%L.%L)", targetVarName, prop.name, sourceVarName, prop.name) }
                }
            }
            is COLLECTIONTYP.SET -> {
                if (Tag.NULLABLE in prop.tags) {
                    buildCodeBlock { addStatement("$c%L.%L?.addAll(%L.%L?.toList() ?: emptyList())", targetVarName, prop.name, sourceVarName, prop.name) }
                } else {
                    buildCodeBlock { addStatement("$c%L.%L.addAll(%L.%L.toList())", targetVarName, prop.name, sourceVarName, prop.name) }
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
                        addStatement("%L.%L?.addAll($c%L.%L?.map { %T.%L(it) } ?: emptyList())", targetVarName, prop.name, sourceVarName, prop.name, propEitherModelFillerClassName, funName)
                    }
                } else {
                    buildCodeBlock {
                        addStatement("%L.%L.addAll($c%L.%L.map { %T.%L(it) })", targetVarName, prop.name, sourceVarName, prop.name, propEitherModelFillerClassName, funName)
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
