package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.Validate.failIfIdentifierInvalid
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import okio.Path
import org.slf4j.LoggerFactory

interface IModelClassName {
    var modelOrTypeNameString: String
    var poetType: TypeName
    val poetTypeSimpleName: String
    val tableName: String
    val asVarName: String
    val fillerPoetType: ClassName
}

class ModelClassName constructor(
    val modelSubElRef: IDslRef,
    var poetTypeDirect: TypeName?
) : IModelClassName {
    val log = LoggerFactory.getLogger(javaClass)
    override fun toString() = poetType.toString()

    lateinit var modelClassData: ModelClassData
    var classNameStrategy = ClassNameStrategy.get(IClassNameStrategy.STRATEGY.DEFAULT)
    var tableNameStrategy = TableNameStrategy.get(ITableNameStrategy.STRATEGY.DEFAULT)
    var basePath: Path = NameAndWheretoDefaults.basePath
    var path: Path = NameAndWheretoDefaults.path
    var basePackage = "com.chassis"
    var packageName = "generated"

    var classPrefix = NameAndWheretoDefaults.classPrefix
    var classPostfix = NameAndWheretoDefaults.classPostfix

    override var modelOrTypeNameString: String = modelSubElRef.parentDslRef.simpleName.ifBlank { log.warn("empty simpleName of model '${modelSubElRef.parentDslRef}'") ; "" }

    // we need to wait until all properties are set on the instance before we can pre-calculate the "derived" properties via strategies:
    override var poetType: TypeName
        get() = poetTypeDirect ?: poetTypeDslModel
        set(value) { poetTypeDirect = value }
    override val poetTypeSimpleName: String
        get() = (poetType as ClassName).simpleName
    private val poetTypeDslModel: TypeName by lazy { classNameStrategy.poetType(modelClassData, modelOrTypeNameString, "${basePackage.ifNotBlank{"$basePackage."}}$packageName", classPrefix, classPostfix) as ClassName }
    override val asVarName: String by lazy { classNameStrategy.asVarname(modelOrTypeNameString, classPrefix, classPostfix) }
    override val tableName: String by lazy { tableNameStrategy.tableName(modelOrTypeNameString) /*, classPrefix, classPostfix)*/ }
    /** only makes sense if the GenModel containing this ModelClassName is a "EitherExtendsModelOrClass.EitherModel" */
    override val fillerPoetType: ClassName
        get() = if (poetTypeDirect != null) {
            ClassName("${(poetTypeDirect as ClassName).packageName}.filler", "Filler${(poetTypeDirect as ClassName).simpleName}")
        } else {
            ClassName("${(poetTypeDslModel as ClassName).packageName}.filler", "Filler${(poetTypeDslModel as ClassName).simpleName}")
        }

    fun validate(any: Any) {
        (poetType as? ClassName)?.simpleName?.failIfIdentifierInvalid("$any->$this")
        asVarName.failIfIdentifierInvalid("$any->$this")
        tableName.failIfIdentifierInvalid("$any->$this")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelClassName) return false
        return poetType == other.poetType
    }
    override fun hashCode(): Int {
        return 31 * poetType.hashCode() + poetType.hashCode()
    }
}
