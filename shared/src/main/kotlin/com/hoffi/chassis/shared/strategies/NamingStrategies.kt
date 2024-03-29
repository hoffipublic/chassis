package com.hoffi.chassis.shared.strategies

import com.hoffi.chassis.chassismodel.Cap
import com.hoffi.chassis.chassismodel.MixedCaseString
import com.hoffi.chassis.chassismodel.decap
import com.hoffi.chassis.shared.helpers.joinName
import com.hoffi.chassis.shared.helpers.joinNonBlanksToStringBy
import com.hoffi.chassis.shared.parsedata.ModelClassDataFromDsl
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
    fun poetType(modelClassDataFromDsl: ModelClassDataFromDsl, className: String, packageName: String, prefix: String = "", postfix: String = ""): TypeName
    fun asVarname(className: String, prefix: String = "", postfix: String = ""): String
}

object ClassNameStrategyCamelCasePrefixed : IClassNameStrategy {
    override fun nameOf(string: String) = ClassNameStrategyCamelCase.nameOf(string)
    override fun nameOf(string: String, prefix: String, postfix: String) = ClassNameStrategyCamelCase.nameOf(string, prefix, postfix)
    override fun nameUpperFirstOf(string: String) = ClassNameStrategyCamelCase.nameUpperFirstOf(string)
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = ClassNameStrategyCamelCase.nameUpperFirstOf(string, prefix, postfix)
    override fun nameLowerFirstOf(string: String) = ClassNameStrategyCamelCase.nameLowerFirstOf(string)
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = ClassNameStrategyCamelCase.nameLowerFirstOf(string, prefix, postfix)
    override fun poetType(modelClassDataFromDsl: ModelClassDataFromDsl, className: String, packageName: String, prefix: String, postfix: String): TypeName {
        return when (modelClassDataFromDsl.kind) {
            TypeSpec.Kind.CLASS ->  if (KModifier.ABSTRACT in modelClassDataFromDsl.classModifiers) {
                ClassNameStrategyCamelCase.poetType(modelClassDataFromDsl, className, packageName, "A$prefix", postfix)
            } else {
                ClassNameStrategyCamelCase.poetType(modelClassDataFromDsl, className, packageName, prefix, postfix)
            }
            TypeSpec.Kind.OBJECT -> ClassNameStrategyCamelCase.poetType(modelClassDataFromDsl, className, packageName, prefix, postfix)
            TypeSpec.Kind.INTERFACE -> ClassNameStrategyCamelCase.poetType(modelClassDataFromDsl, className, packageName, "I$prefix", postfix)
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
    override fun poetType(modelClassDataFromDsl: ModelClassDataFromDsl, className: String, packageName: String, prefix: String, postfix: String): TypeName {
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
    override fun nameOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string, prefix, postfix).toLowerCamelCase()
    override fun nameUpperFirstOf(string: String) = MixedCaseString(string).toUpperCamelCase()
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string, prefix, postfix).toUpperCamelCase()
    override fun nameLowerFirstOf(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string, prefix, postfix).toLowerCamelCase()
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
    override fun nameOf(string: String, prefix: String, postfix: String) = MixedCaseString(joinNonBlanksToStringBy("_", prefix, string, postfix)).toLowerSnakeCase()
    override fun nameUpperFirstOf(string: String) = MixedCaseString(string).toLowerSnakeCase().Cap()
    override fun nameUpperFirstOf(string: String, prefix: String, postfix: String) = nameOf(string, prefix, postfix).Cap()
    override fun nameLowerFirstOf(string: String) = MixedCaseString(string).toLowerSnakeCase().decap()
    override fun nameLowerFirstOf(string: String, prefix: String, postfix: String) = nameOf(string, prefix, postfix).decap()
    override fun tableName(modelName: String, prefix: String, postfix: String) = MixedCaseString(joinName(prefix, modelName, postfix)).toLowerSnakeCase()
}
