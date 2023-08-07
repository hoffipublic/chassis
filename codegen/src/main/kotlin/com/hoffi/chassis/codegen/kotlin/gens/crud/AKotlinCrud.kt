package com.hoffi.chassis.codegen.kotlin.gens.crud

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.helpers.PoetHelpers.kdocGeneratedCrud
import com.hoffi.chassis.shared.helpers.PoetHelpers.plus
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.CrudData
import com.squareup.kotlinpoet.*
import okio.Path
import org.slf4j.LoggerFactory

context(GenCtxWrapper)
abstract class AKotlinCrud(crudData: CrudData) {
    var currentCrudData: CrudData = crudData // changes for every build() call, also helpfull for debugging
    override fun toString() = "${this::class.simpleName}(current${currentCrudData})"
    protected val log = LoggerFactory.getLogger(this::class.java)
    val alreadyCreated: MutableSet<CrudData> = mutableSetOf()
    fun alreadyCreated(crudData: CrudData) = ! alreadyCreated.add(crudData)

    var crudBasePath: Path
    var crudPath: Path
    val crudPoetType: ClassName = if (crudData.targetDslRef !is DslRef.table) {
            throw GenException("CRUD gen Target always should be DslRef.table (for Exposed SQL)")
        } else {
            val targetGenModel = genCtx.genModel(crudData.targetDslRef)
            crudBasePath = targetGenModel.modelClassName.basePath
            crudPath =     targetGenModel.modelClassName.path
            targetGenModel.crudPoetType + "${currentCrudData.crud}"
        }

    //fun propFiller(targetDslRef: IDslRef, modelrefenum: MODELREFENUM): ClassName {
    //    val swappedDslRef = when (modelrefenum) {
    //        MODELREFENUM.MODEL -> throw GenException("MODELs do not have fillers themselves")
    //        MODELREFENUM.DTO -> DslRef.dto(C.DEFAULT, targetDslRef.parentDslRef)
    //        MODELREFENUM.TABLE -> DslRef.table(C.DEFAULT, targetDslRef.parentDslRef)
    //    }
    //    val swappedGenModel = genCtx.genModel(swappedDslRef)
    //    return ClassName((swappedGenModel.poetType as ClassName).packageName + ".filler", "Filler" + (swappedGenModel.poetType as ClassName).simpleName)
    //}

    val builder = TypeSpec.objectBuilder(crudPoetType).apply {
        kdocGeneratedCrud(genCtx, crudData)
        addSuperinterface(RuntimeDefaults.WAS_GENERATED_INTERFACE_ClassName)
    }

    init {
        kotlinGenCtx.putKotlinCrudClass(crudData, this)
    }

    abstract fun build(crudData: CrudData)


    fun generate(out: Appendable? = null): TypeSpec {
        val fileSpecBuilder = FileSpec.builder(crudPoetType)
        val typeSpec = builder.build()
        val fileSpec = fileSpecBuilder.addType(typeSpec).build()
        if (out != null) {
            fileSpec.writeTo(out)
        } else {
            try {
                fileSpec.writeTo((crudBasePath/crudPath).toNioPath())
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

}
