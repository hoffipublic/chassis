package com.hoffi.chassis.shared.parsedata

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
    val modelSubElRef: DslRef.IModelSubElement,
    var classNameStrategy: IClassNameStrategy = ChassisStrategies.classNameStrategy(C.DEFAULT),
    var tableNameStrategy: ITableNameStrategy = ChassisStrategies.tableNameStrategy(C.DEFAULT)
) : IModelClassName {
    var basePath: Path = ".".toPath()
    var path: Path = basePath
    var basePackage = "com.chassis"
    var packag = basePackage

    var classPrefix = ""
    var classPostfix = ""

    fun setToDataOf(otherModelClassName: ModelClassName) {
        basePath = otherModelClassName.basePath
        basePackage = otherModelClassName.basePackage
        classPrefix = otherModelClassName.classPrefix
        classPostfix = otherModelClassName.classPostfix
    }


    override var modelName: String = modelSubElRef.parentRef.simpleName
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
    val x = ModelClassName(IDslRef.NULL as DslRef.IModelSubElement, ChassisStrategies.classNameStrategy(C.DEFAULT), ChassisStrategies.tableNameStrategy(C.DEFAULT))
    when (x.modelSubElRef.E_MODEL_SUBELEMENT) {
        DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.DTO -> TODO()
        DslRef.MODELGROUP_MODEL_SUBELEMENTLEVEL.TABLE -> TODO()
    }
}
