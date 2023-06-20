package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.strategies.ChassisStrategies
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.squareup.kotlinpoet.TypeName
import okio.Path
import okio.Path.Companion.toPath

interface IModelClassName {
    var modelName: String
    val poetType: TypeName
    val tableName: String
    val asVarName: String
}

class ModelClassName(
    val modelSubElRef: DslRef.IModelSubelement,
    var classNameStrategy: IClassNameStrategy = ChassisStrategies.classNameStrategy(C.DEFAULT),
    var tableNameStrategy: ITableNameStrategy = ChassisStrategies.tableNameStrategy(C.DEFAULT)
) : IModelClassName {
    var basePath: Path = ".".toPath()/"generated"
    var path: Path = ".".toPath()
    var basePackage = "com.chassis"
    var packag = ""

    var classPrefix = ""
    var classPostfix = ""

    fun setToDataOf(otherModelClassName: ModelClassName) {
        basePath = otherModelClassName.basePath
        path = otherModelClassName.path
        basePackage = otherModelClassName.basePackage
        packag = otherModelClassName.packag
        classPrefix = otherModelClassName.classPrefix
        classPostfix = otherModelClassName.classPostfix
    }


    override var modelName: String = if (modelSubElRef.parentRef.simpleName.isBlank()) "HEREXXX" else modelSubElRef.parentRef.simpleName
    override val poetType: TypeName = classNameStrategy.poetType(modelName, packag, classPrefix, classPostfix)
    override val tableName: String = tableNameStrategy.tableName(modelName, classPrefix, classPostfix)
    override val asVarName: String
        get() = classNameStrategy.asVarname(modelName, classPrefix, classPostfix)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelClassName) return false
        if (modelSubElRef != other.modelSubElRef) return false
        return true
    }
    override fun hashCode(): Int {
        return modelSubElRef.hashCode()
    }
}

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    val x = ModelClassName(IDslRef.NULL as DslRef.IModelSubelement, ChassisStrategies.classNameStrategy(C.DEFAULT), ChassisStrategies.tableNameStrategy(C.DEFAULT))
    when (x.modelSubElRef.E_MODEL_SUBELEMENT) {
        DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.DTO -> TODO()
        DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.TABLE -> TODO()
    }
}
