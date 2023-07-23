package com.hoffi.chassis.codegen.kotlin.gens.filler

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGeneratedFiller
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.FillerData
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELKIND
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.squareup.kotlinpoet.*
import okio.Path
import org.slf4j.LoggerFactory

context(GenCtxWrapper)
abstract class AKotlinFiller constructor(fillerData: FillerData, val modelkind: MODELKIND) {
    private val fillerDataTargetDslRef = fillerData.targetDslRef
    var currentFillerData: FillerData = fillerData // changes for every build() call, also helpfull for debugging
    override fun toString() = "${this::class.simpleName}(current${currentFillerData})"
    protected val log = LoggerFactory.getLogger(this::class.java)
    /** fillerName -> source(!) DslRef (as each targetDslRef has its own KotlinFiller Instance */
    val alreadyCreated: MutableSet<Pair<String, IDslRef>> = mutableSetOf()

    lateinit var fillerBasePath: Path
    lateinit var fillerPath: Path
    val fillerPoetType = when (modelkind) {
        MODELKIND.DTOKIND -> {
            val targetGenModel = genCtx.genModel(fillerData.targetDslRef)
            fillerBasePath = targetGenModel.modelClassName.basePath
            fillerPath =     targetGenModel.modelClassName.path
            targetGenModel.fillerPoetType
        }
        MODELKIND.TABLEKIND -> {
            if (fillerData.targetDslRef !is DslRef.table) {
                val sourceGenModel = genCtx.genModel(fillerData.sourceDslRef)
                fillerBasePath = sourceGenModel.modelClassName.basePath
                fillerPath =     sourceGenModel.modelClassName.path
                sourceGenModel.fillerPoetType
            } else {
                val targetGenModel = genCtx.genModel(fillerData.targetDslRef)
                fillerBasePath = targetGenModel.modelClassName.basePath
                fillerPath =     targetGenModel.modelClassName.path
                targetGenModel.fillerPoetType
            }
        }
    }
    fun propFiller(targetDslRef: IDslRef, modelrefenum: MODELREFENUM): ClassName {
        val swappedDslRef = when (modelrefenum) {
            MODELREFENUM.MODEL -> throw GenException("MODELs do not have fillers themselves")
            MODELREFENUM.DTO -> DslRef.dto(C.DEFAULT, targetDslRef.parentDslRef)
            MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, targetDslRef.parentDslRef)
        }
        val swappedGenModel = genCtx.genModel(swappedDslRef)
        return ClassName((swappedGenModel.poetType as ClassName).packageName + ".filler", "Filler" + (swappedGenModel.poetType as ClassName).simpleName)
    }

    val builder = TypeSpec.objectBuilder(fillerPoetType).apply {
        kdocGeneratedFiller(genCtx, fillerData)
        addSuperinterface(RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
    }

    init {
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
                fileSpec.writeTo((fillerBasePath/fillerPath).toNioPath())
            } catch (e: Exception) {
                throw GenException(e.message ?: "unknown error", e)
            }
        }
        return typeSpec
    }

    data class FunName(val funName: String, val originalFunName:String, val prefix: String = "", val postfix: String = "") {
        fun of(otherFunName: String): String = if (prefix.isNotBlank()) {
            prefix + otherFunName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(C.LOCALE) else it.toString() } + postfix
        } else {
            otherFunName + postfix
        }
    }
    fun funName(origFunName: String, fillerData: FillerData): FunName {
        val funName = if (fillerData.fillerName == C.DEFAULT) {
            if (fillerData.sourceDslRef == fillerData.targetDslRef) {
                FunName(origFunName, origFunName, "", "")
            } else {
                val targetGenModel: GenModel = genCtx.genModel(fillerData.targetDslRef)
                val sourceGenModel: GenModel = genCtx.genModel(fillerData.sourceDslRef)
                val prefix = targetGenModel.modelClassName.classNameStrategy.nameLowerFirst(sourceGenModel.poetTypeSimpleName)
                FunName(prefix + targetGenModel.modelClassName.classNameStrategy.nameUpperFirst(origFunName),
                    origFunName, prefix, "")
            }
        } else {
            val targetGenModel: GenModel = genCtx.genModel(fillerData.targetDslRef)
            val prefix = targetGenModel.modelClassName.classNameStrategy.nameLowerFirst(fillerData.fillerName)
            FunName(prefix + targetGenModel.modelClassName.classNameStrategy.nameUpperFirst(origFunName),
                origFunName, prefix, "")
        }
        return funName
    }

    fun nullSentinel(funBuilder: FunSpec.Builder, targetVarName: String, targetModel: GenModel) {
        if (KModifier.ABSTRACT !in targetModel.classModifiers) {
            funBuilder.addStatement("if (%L === %T.NULL) throw Exception(%S)", targetVarName, targetModel.poetType, "cannot clone/copy into companion.NULL")
        }
    }

    protected fun cloneFillerFunctions(i: IntersectPropertys.CommonPropData) {
        if (KModifier.ABSTRACT in i.targetGenModel.classModifiers) return
        val allPKs = i.targetGenModel.propsInclSuperclassPropsMap.values.filter { Tag.PRIMARY in it.tags }
        if (allPKs.isNotEmpty()) {
            val funNamePairs = listOf(
                "cloneShallowIgnoreModels" to "copyShallowIgnoreModelsInto",
                "cloneShallowTakeSameModels" to "copyShallowAndTakeSameModelsInto",
                "cloneWithNewModels" to "copyShallowWithNewModelsInto"
            )
            for (funNamePair in funNamePairs) {
                val funSpecBuilder = FunSpec.builder(funNamePair.first)
                    .addParameter(ParameterSpec.builder(i.sourceVarName, i.sourceGenModel.poetType).build())
                    .returns(i.targetGenModel.poetType)
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
