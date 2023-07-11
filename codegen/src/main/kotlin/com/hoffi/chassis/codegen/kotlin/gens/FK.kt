package com.hoffi.chassis.codegen.kotlin.gens

import com.hoffi.chassis.chassismodel.RuntimeDefaults
import com.hoffi.chassis.chassismodel.dsl.GenException
import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.codegen.kotlin.GenCtxWrapper
import com.hoffi.chassis.shared.db.DB
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.parsedata.Property
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec

context(GenCtxWrapper)
class FK(
    val fromTableRef: DslRef.IModelSubelement,
    val toTable: AKotlinClass,
    var toProp: Property,
    val COLLECTIONTYP: COLLECTIONTYP,
) : Comparable<FK> {
    val varName: String
        get() = when (COLLECTIONTYP) {
            is COLLECTIONTYP.NONE -> throw GenException("1:1 FK should be handled in normal 'KotlinPropertyTable' class")
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                "${toTable.modelClassData.tableName}Uuid_${toProp.name}"
            }

        }
    val columnName: String
        get() = when (COLLECTIONTYP) {
            is COLLECTIONTYP.NONE -> throw GenException("1:1 FK should be handled in normal 'KotlinPropertyTable' class")
            is COLLECTIONTYP.LIST, is COLLECTIONTYP.SET, is COLLECTIONTYP.COLLECTION, is COLLECTIONTYP.ITERABLE -> {
                "${toTable.modelClassData.tableName}_uuid_${toProp.columnName}"
            }
        }
    override fun compareTo(other: FK): Int = toProp.dslPropName.compareTo(other.toProp.dslPropName)

    /** build after ALL normal KotlinClassModelTable and their "normal" props are build</br>
     * because the "dependant" KotlinClassModelTable might not yet have been generated and doesn't yet exist */
    fun buildFK() {
        //public var someModelObject: Column<UUID> = uuid("SimpleSubentity_uuid").uniqueIndex().references(SimpleSubentityTable.uuid)
        //val (nullQM, nullFunc) = if (Tag.NULLABLE in toProp.tags) Pair("?", ".nullable()") else Pair("", "")
        val fromKotlinClassModelTable: AKotlinClass = kotlinGenCtx.kotlinGenClass(fromTableRef)
        val propSpecBuilder = PropertySpec.Companion.builder(varName, DB.ColumnClassName.parameterizedBy(RuntimeDefaults.classNameUUID), toProp.modifiers)
        //propSpecBuilder.initializer("uuid(%S$nullQM).uniqueIndex().references(%T.%L)$nullFunc", columnName, toTable.modelClassData.poetType, toProp.name)
        propSpecBuilder.initializer("uuid(%S).uniqueIndex().references(%T.%L)", columnName, toTable.modelClassData.poetType, "uuid") // toProp's class uuid
        //eitherTypOrModelOrPoetType.initializer.originalFormat = "uuid(%S$nullQM).uniqueIndex().references(%T.%L)$nullFunc"
        //eitherTypOrModelOrPoetType.initializer.originalArgs.clear()
        //eitherTypOrModelOrPoetType.initializer.originalArgs.add(property.columnName)
        //val correspondingTable = genCtx.genModel(DslRef.table(C.DEFAULT, eitherTypOrModelOrPoetType.modelSubElementRef.parentDslRef))
        //eitherTypOrModelOrPoetType.initializer.originalArgs.add(correspondingTable.modelClassName.poetType)
        //eitherTypOrModelOrPoetType.initializer.originalArgs.add(RuntimeDefaults.UUID_PROPNAME)

        fromKotlinClassModelTable.builder.addProperty(propSpecBuilder.build())
    }
}
