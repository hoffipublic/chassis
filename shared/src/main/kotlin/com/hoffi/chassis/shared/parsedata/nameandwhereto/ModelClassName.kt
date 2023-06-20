package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
import com.squareup.kotlinpoet.TypeName
import okio.Path

interface IModelClassName {
    var modelName: String
    val poetType: TypeName
    val tableName: String
    val asVarName: String
}

class ModelClassName(
    val modelSubElRef: IDslRef
) : IModelClassName {
    var classNameStrategy = ClassNameStrategy.get(IClassNameStrategy.STRATEGY.DEFAULT)
    var tableNameStrategy = TableNameStrategy.get(ITableNameStrategy.STRATEGY.DEFAULT)
    var basePath: Path = NameAndWheretoDefaults.basePath
    var path: Path = NameAndWheretoDefaults.path
    var basePackage = NameAndWheretoDefaults.basePackage
    var packageName = NameAndWheretoDefaults.packageName

    var classPrefix = NameAndWheretoDefaults.classPrefix
    var classPostfix = NameAndWheretoDefaults.classPostfix

    override var modelName: String = modelSubElRef.parentRef.simpleName.ifBlank { "HEREXXX" }
    override val poetType: TypeName = classNameStrategy.poetType(modelName, packageName, classPrefix, classPostfix)
    override val tableName: String = tableNameStrategy.tableName(modelName, classPrefix, classPostfix)
    override val asVarName: String = classNameStrategy.asVarname(modelName, classPrefix, classPostfix)

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
