package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUIDTABLE_CLASSNAME
import com.hoffi.chassis.chassismodel.RuntimeDefaults.UUID_PROPNAME
import com.hoffi.chassis.chassismodel.dsl.GenCtxException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.codegen.kotlin.GenDslRefHelpers
import com.hoffi.chassis.codegen.kotlin.GenNaming
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.GenModel
import com.hoffi.chassis.shared.parsedata.Property
import com.hoffi.chassis.shared.shared.FK
import com.hoffi.chassis.shared.shared.Tag
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

context(GenCtxWrapper)
class KotlinClassModelTable(val tableModel: GenModel.TableModel)
    : AKotlinClass(tableModel)
{
    val incomingFKs: MutableSet<FK> = mutableSetOf()
    val outgoingFKs: MutableSet<FK> = mutableSetOf()
    fun addIncomingFK(fromTableRef: DslRef.table, toTableRef: DslRef.IModelSubelement, property: Property, collectiontyp: COLLECTIONTYP): FK {
        val fk = FK(fromTableRef, toTableRef, property, collectiontyp)
        incomingFKs.add(fk)
        kotlinGenCtx.addFK(fk)
        return fk
    }
    fun addOutgoingFK(fk: FK): FK {
        outgoingFKs.add(fk)
        kotlinGenCtx.addFK(fk)
        return fk
    }

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
        val isUuidTable = modelClassDataFromDsl.propsInclSuperclassPropsMap.values.filter { Tag.Companion.PRIMARY in it.tags }
        if (isUuidTable.size == 1 && isUuidTable.first().dslPropName == UUID_PROPNAME) {
            builder.superclass(UUIDTABLE_CLASSNAME)
            tableModel.isUuidPrimary = true
        } else {
            builder.superclass(DB.TableClassName)
        }
        builder.addSuperclassConstructorParameter("%S", tableModel.modelClassName.tableName)
        val extends = modelClassDataFromDsl.extends["default"]
        for (superinterface in extends?.superInterfaces ?: mutableSetOf()) {
            builder.addSuperinterface(superinterface.modelClassName.poetType)
        }

    }

    fun buildPropertys() {
        for (theProp in tableModel.allProps.values) {
            if (tableModel.isUuidPrimary && theProp.dslPropName == UUID_PROPNAME) continue
            if (Tag.TRANSIENT in theProp.tags) continue
            val kotlinProp = KotlinPropertyTable(theProp, this).whenInit()
            builder.addProperty(kotlinProp.build())
        }
    }

    fun buildAnnotations() {
        val dtoModel = try { genCtx.genModel(DslRef.dto(C.DEFAULT, tableModel.modelSubElRef.parentDslRef)) } catch(e: GenCtxException) { null }
        if (dtoModel != null) {
            builder.addAnnotation(
                AnnotationSpec.builder(RuntimeDefaults.ANNOTATION_DTO_CLASSNAME)
                    .addMember("%T::class", modelClassDataFromDsl.poetType)
                    .addMember("targetDto = %T::class", dtoModel.modelClassName.poetType)
                    .build()
            )
        }
    }

    /** build after ALL normal KotlinClassModelTable and their "normal" props are build</br>
     * because the "dependant" KotlinClassModelTable might not yet have been generated and may not yet exist */
    fun buildMany2OneFK(fk: FK) {
        val fkPropVarName = GenNaming.fkPropVarNameUUID(fk)
        val fkPropColName = GenNaming.fkPropColumnNameUUID(fk)
        val toKotlinClassmodelTable: AKotlinClass = kotlinGenCtx.kotlinGenClass(fk.toTableRef)
        val propSpecBuilder = PropertySpec.builder(fkPropVarName, DB.ColumnClassName.parameterizedBy(RuntimeDefaults.classNameUUID), fk.toProp.modifiers)
        //propSpecBuilder.initializer("uuid(%S$nullQM).uniqueIndex().references(%T.%L)$nullFunc", columnName, toTable.modelClassDataFromDsl.poetType, toProp.name)
        propSpecBuilder.initializer("uuid(%S).references(%T.%L)", fkPropColName, toKotlinClassmodelTable.modelClassDataFromDsl.poetType, "uuid") // toProp's class uuid
        propSpecBuilder.addAnnotation(
            AnnotationSpec.builder(RuntimeDefaults.ANNOTATION_FKFROM_CLASSNAME)
                .addMember("%T::class", GenDslRefHelpers.dtoClassName(toKotlinClassmodelTable.modelClassDataFromDsl))
                .build()
        )
        //fromKotlinClassModelTable.builder.addProperty(propSpecBuilder.build())
        builder.addProperty(propSpecBuilder.build())
    }
}
