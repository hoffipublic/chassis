package com.hoffi.chassis.shared.parsedata.nameandwhereto

import arrow.core.right
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import okio.Path
import okio.Path.Companion.toPath
import org.slf4j.LoggerFactory

interface IModelClassName {
    var modelName: String
    var poetType: ClassName
    val tableName: String
    val asVarName: String
}

class ModelClassName constructor(
    val modelSubElRef: IDslRef,
    private var poetTypeDirect: ClassName? = null
) : IModelClassName {
    val log = LoggerFactory.getLogger(javaClass)
    override fun toString() = poetType.toString()
    var classNameStrategy = ClassNameStrategy.get(IClassNameStrategy.STRATEGY.DEFAULT)
    var tableNameStrategy = TableNameStrategy.get(ITableNameStrategy.STRATEGY.DEFAULT)
    var basePath: Path = "./generated".toPath()
    var path: Path = NameAndWheretoDefaults.path
    var basePackage = "com.chassis"
    var packageName = "generated"

    var classPrefix = NameAndWheretoDefaults.classPrefix
    var classPostfix = NameAndWheretoDefaults.classPostfix

    override var modelName: String = modelSubElRef.parentRef.simpleName.ifBlank { log.warn("empty simpleName of model '${modelSubElRef.parentRef}'") ; "" }

    // we need to wait until all properties are set on the instance before we can pre-calculate the "derived" properties via strategies:
    override var poetType: ClassName
        get() = poetTypeDirect ?: poetTypeInternal
        set(value) { poetTypeDirect = value }
    private val poetTypeInternal by lazy { classNameStrategy.poetType(modelName, "${basePackage.ifNotBlank{"$basePackage."}}$packageName", classPrefix, classPostfix) as ClassName }
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
