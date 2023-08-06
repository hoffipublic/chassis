package com.hoffi.chassis.shared.strategies

import com.hoffi.chassis.chassismodel.MixedCaseString
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.helpers.joinName
import com.hoffi.chassis.shared.parsedata.ModelClassData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

interface INamingStrategy {
    // TODO add vararg'y like list of prefixes and postfixes
    fun nameOf(string: String): String
    fun nameOf(string: String, prefix: String = "", postfix: String = ""): String
    fun nameUpperFirstOf(string: String): String
    fun nameUpperFirstOf(string: String, prefix: String = "", postfix: String = ""): String
    fun nameLowerFirstOf(string: String): String
    fun nameLowerFirstOf(string: String, prefix: String = "", postfix: String = ""): String
}


object ClassNameStrategy {
    fun get(strategy: IClassNameStrategy.STRATEGY) =
        when (strategy) {
            IClassNameStrategy.STRATEGY.DEFAULT, IClassNameStrategy.STRATEGY.CAMELCASE_PREFIXED -> ClassNameStrategyCamelCasePrefixed
            IClassNameStrategy.STRATEGY.CAMELCASE -> ClassNameStrategyCamelCase
        }
}

interface IClassNameStrategy : INamingStrategy {
    enum class STRATEGY { DEFAULT, CAMELCASE, CAMELCASE_PREFIXED }
    fun poetType(modelClassData: ModelClassData, className: String, packageName: String, prefix: String = "", postfix: String = ""): TypeName
    fun asVarname(className: String, prefix: String = "", postfix: String = ""): String
}

object ClassNameStrategyCamelCasePrefixed : IClassNameStrategy {
    override fun nameOf(string: String) = ClassNameStrategyCamelCase.nameOf(string)
    override fun nameOf(string: String, prefix: String, postfix: String) = ClassNameStrategyCamelCase.nameOf(string, prefix, postfix)
    override fun nameUpperFirstOf(string: String) = ClassNameStrategyCamelCase.nameUpperFirstOf(string)
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = ClassNameStrategyCamelCase.nameUpperFirstOf(string, prefix, postfix)
    override fun nameLowerFirstOf(string: String) = ClassNameStrategyCamelCase.nameLowerFirstOf(string)
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = ClassNameStrategyCamelCase.nameLowerFirstOf(string, prefix, postfix)
    override fun poetType(modelClassData: ModelClassData, className: String, packageName: String, prefix: String, postfix: String): TypeName {
        return when (modelClassData.kind) {
            TypeSpec.Kind.CLASS ->  if (KModifier.ABSTRACT in modelClassData.classModifiers) {
                ClassNameStrategyCamelCase.poetType(modelClassData, className, packageName, "A$prefix", postfix)
            } else {
                ClassNameStrategyCamelCase.poetType(modelClassData, className, packageName, prefix, postfix)
            }
            TypeSpec.Kind.OBJECT -> ClassNameStrategyCamelCase.poetType(modelClassData, className, packageName, prefix, postfix)
            TypeSpec.Kind.INTERFACE -> ClassNameStrategyCamelCase.poetType(modelClassData, className, packageName, "I$prefix", postfix)
        }
    }
    override fun asVarname(className: String, prefix: String, postfix: String): String {
        return MixedCaseString(joinName(prefix, className, postfix)).toLowerCamelCase()
    }
}

object ClassNameStrategyCamelCase : IClassNameStrategy {
    override fun nameOf(string: String) = MixedCaseString(string).toCamelCase()
    override fun nameOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string, prefix, postfix).toCamelCase()
    override fun nameUpperFirstOf(string: String) = MixedCaseString(string).toUpperCamelCase()
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string, prefix, postfix).toUpperCamelCase()
    override fun nameLowerFirstOf(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string, prefix, postfix).toLowerCamelCase()
    override fun poetType(modelClassData: ModelClassData, className: String, packageName: String, prefix: String, postfix: String): TypeName {
        return ClassName(packageName, MixedCaseString(joinName(prefix, className, postfix)).toUpperCamelCase())
    }
    override fun asVarname(className: String, prefix: String, postfix: String): String {
        return MixedCaseString(joinName(prefix, className, postfix)).toLowerCamelCase()
    }
}

object VarNameStrategy {
    fun get(strategy: IVarNameStrategy.STRATEGY) =
        when (strategy) {
            IVarNameStrategy.STRATEGY.DEFAULT, IVarNameStrategy.STRATEGY.LOWERCAMELCASE -> VarNameStrategyLowerCamelCase
            IVarNameStrategy.STRATEGY.LOWERSNAKECASE -> TableNameStrategyLowerSnakeCase
        }
}

interface IVarNameStrategy : INamingStrategy {
    enum class STRATEGY { DEFAULT, LOWERCAMELCASE, LOWERSNAKECASE }
}

object VarNameStrategyLowerCamelCase : IVarNameStrategy {
    override fun nameOf(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun nameOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toLowerCamelCase()
    override fun nameUpperFirstOf(string: String) = MixedCaseString(string).toUpperCamelCase()
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toUpperCamelCase()
    override fun nameLowerFirstOf(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toLowerCamelCase()
}

object TableNameStrategy {
    fun get(strategy: ITableNameStrategy.STRATEGY) =
        when (strategy) {
            ITableNameStrategy.STRATEGY.DEFAULT, ITableNameStrategy.STRATEGY.LOWERSNAKECASE -> TableNameStrategyLowerSnakeCase
        }
}
interface ITableNameStrategy : INamingStrategy {
    enum class STRATEGY { DEFAULT, LOWERSNAKECASE }
    fun tableName(modelName: String, prefix: String = "", postfix: String = ""): String
}

object TableNameStrategyLowerSnakeCase : ITableNameStrategy {
    override fun nameOf(string: String) = MixedCaseString(string).toLowerSnakeCase()
    override fun nameOf(string: String, prefix: String, postfix: String) = MixedCaseString("${prefix.ifNotBlank { "${prefix}_" }}$string${postfix.ifNotBlank { "_$postfix" }}").toLowerSnakeCase()
    override fun nameUpperFirstOf(string: String) = MixedCaseString(string).toLowerSnakeCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString("${prefix.ifNotBlank { "${prefix}_" }}$string${postfix.ifNotBlank { "_$postfix" }}").toLowerSnakeCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    override fun nameLowerFirstOf(string: String) = MixedCaseString(string).toLowerSnakeCase().replaceFirstChar { it.lowercase() }
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString("${prefix.ifNotBlank { "${prefix}_" }}$string${postfix.ifNotBlank { "_$postfix" }}").toLowerSnakeCase().replaceFirstChar { it.lowercase() }
    override fun tableName(modelName: String, prefix: String, postfix: String): String {
        return MixedCaseString(joinName(prefix, modelName, postfix)).toLowerSnakeCase()
    }
}
