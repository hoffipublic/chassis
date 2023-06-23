package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
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
    val modelSubElRef: IDslRef
) : IModelClassName {
    var classNameStrategy = ClassNameStrategy.get(IClassNameStrategy.STRATEGY.DEFAULT)
    var tableNameStrategy = TableNameStrategy.get(ITableNameStrategy.STRATEGY.DEFAULT)
    var basePath: Path = "./generated".toPath()
    var path: Path = NameAndWheretoDefaults.path
    var basePackage = "com.chassis"
    var packageName = "generated"

    var classPrefix = NameAndWheretoDefaults.classPrefix
    var classPostfix = NameAndWheretoDefaults.classPostfix

    override var modelName: String = modelSubElRef.parentRef.simpleName.ifBlank { "HEREXXX" }

    // we need to wait until all properties are set on the instance before we can pre-calculate the "derived" properties:

    override val poetType: TypeName by lazy { classNameStrategy.poetType(modelName, "${basePackage.ifNotBlank{"$basePackage."}}$packageName", classPrefix, classPostfix) }
    override val asVarName: String by lazy { classNameStrategy.asVarname(modelName, classPrefix, classPostfix) }
    override val tableName: String by lazy { tableNameStrategy.tableName(modelName) /*, classPrefix, classPostfix)*/ }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelClassName) return false
        return modelSubElRef == other.modelSubElRef
    }
    override fun hashCode(): Int {
        return modelSubElRef.hashCode()
    }

}
