package com.hoffi.chassis.shared.strategies

import com.hoffi.chassis.chassismodel.MixedCaseString
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.helpers.joinName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

interface INamingStrategy {
    fun name(string: String): String
    fun name(string: String, prefix: String = "", postfix: String = ""): String
    fun nameUpperFirst(string: String): String
    fun nameUpperFirst(string: String, prefix: String = "", postfix: String = ""): String
    fun nameLowerFirst(string: String): String
    fun nameLowerFirst(string: String, prefix: String = "", postfix: String = ""): String
}


object ClassNameStrategy {
    fun get(strategy: IClassNameStrategy.STRATEGY) =
        when (strategy) {
            IClassNameStrategy.STRATEGY.DEFAULT, IClassNameStrategy.STRATEGY.CAMELCASE -> ClassNameStrategyCamelCase
        }
}

interface IClassNameStrategy : INamingStrategy {
    enum class STRATEGY { DEFAULT, CAMELCASE }
    fun poetType(className: String, packageName: String, prefix: String = "", postfix: String = ""): TypeName
    fun asVarname(className: String, prefix: String = "", postfix: String = ""): String
}

object ClassNameStrategyCamelCase : IClassNameStrategy {
    override fun name(string: String) = MixedCaseString(string).toCamelCase()
    override fun name(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toCamelCase()
    override fun nameUpperFirst(string: String) = MixedCaseString(string).toUpperCamelCase()
    override fun nameUpperFirst(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toUpperCamelCase()
    override fun nameLowerFirst(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun nameLowerFirst(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toLowerCamelCase()
    override fun poetType(className: String, packageName: String, prefix: String, postfix: String): TypeName {
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
        }
}

interface IVarNameStrategy : INamingStrategy {
    enum class STRATEGY { DEFAULT, LOWERCAMELCASE }

}

object VarNameStrategyLowerCamelCase : IVarNameStrategy {
    override fun name(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun name(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toLowerCamelCase()
    override fun nameUpperFirst(string: String) = MixedCaseString(string).toUpperCamelCase()
    override fun nameUpperFirst(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toUpperCamelCase()
    override fun nameLowerFirst(string: String) = MixedCaseString(string).toLowerCamelCase()
    override fun nameLowerFirst(string: String, prefix: String, postfix: String) = MixedCaseString.concatCapitalized(string).toLowerCamelCase()
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
    override fun name(string: String) = MixedCaseString(string).toLowerSnakeCase()
    override fun name(string: String, prefix: String, postfix: String) = MixedCaseString("${prefix.ifNotBlank { "${prefix}_" }}$string${postfix.ifNotBlank { "_$postfix" }}").toLowerSnakeCase()
    override fun nameUpperFirst(string: String) = MixedCaseString(string).toLowerSnakeCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    override fun nameUpperFirst(string: String, prefix: String, postfix: String) = MixedCaseString("${prefix.ifNotBlank { "${prefix}_" }}$string${postfix.ifNotBlank { "_$postfix" }}").toLowerSnakeCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    override fun nameLowerFirst(string: String) = MixedCaseString(string).toLowerSnakeCase().replaceFirstChar { it.lowercase() }
    override fun nameLowerFirst(string: String, prefix: String, postfix: String) = MixedCaseString("${prefix.ifNotBlank { "${prefix}_" }}$string${postfix.ifNotBlank { "_$postfix" }}").toLowerSnakeCase().replaceFirstChar { it.lowercase() }
    override fun tableName(modelName: String, prefix: String, postfix: String): String {
        return MixedCaseString(joinName(prefix, modelName, postfix)).toLowerSnakeCase()
    }
}
