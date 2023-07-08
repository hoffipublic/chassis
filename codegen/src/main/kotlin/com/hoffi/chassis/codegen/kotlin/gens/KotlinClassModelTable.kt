package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.shared.codegen.GenCtxWrapper
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.fix.RuntimeDefaults
import com.hoffi.chassis.shared.fix.RuntimeDefaults.UUIDTABLE_CLASSNAME
import com.hoffi.chassis.shared.fix.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeSpec

context(GenCtxWrapper)
class KotlinClassModelTable(val tableModel: GenModel.TableModel)
    : AKotlinClass(tableModel)
{
    fun build(): TypeSpec.Builder {
        builder.addModifiers(tableModel.classModifiers)
        buildExtends()
        buildPropertys()
        //buildFeatures()
        //buildFunctions()
        //buildAuxiliaryFunctions()
        buildAnnotations()
        return builder
    }

    private fun buildExtends() {
        val isUuidTable = propsInclSuperclassPropsMap.values.filter { Tag.Companion.PRIMARY in it.tags }
        if (isUuidTable.size == 1) {
            builder.superclass(UUIDTABLE_CLASSNAME)
            tableModel.isUuidPrimary = true
        } else {
            builder.superclass(DB.TableClassName)
        }
        builder.addSuperclassConstructorParameter("%S", tableModel.modelClassName.tableName)
        val extends = modelClassData.extends["default"]
        for (superinterface in extends?.superInterfaces ?: mutableSetOf()) {
            builder.addSuperinterface(superinterface.modelClassName.poetType)
        }

    }

    fun buildPropertys() {
        for (theProp in tableModel.allProps.values) {
            if (tableModel.isUuidPrimary && theProp.name == UUID_PROPNAME) continue
            val kotlinProp = KotlinPropertyTable(theProp, this.modelClassData)
            builder.addProperty(kotlinProp.build())
        }
    }

    fun buildAnnotations() {
        val dtoModel = try { genCtx.genModel(DslRef.dto(C.DEFAULT, tableModel.modelSubElRef.parentDslRef)) } catch(e: GenCtxException) { null }
        if (dtoModel != null) {
            builder.addAnnotation(
                AnnotationSpec.builder(RuntimeDefaults.ANNOTATION_TABLE_CLASSNAME)
                    .addMember("%T::class", dtoModel.modelClassName.poetType)
                    .build()
            )
        }
    }
}
