package com.hoffi.chassis.shared.strategies

import com.hoffi.chassis.chassismodel.MixedCaseString
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

fun fullStringPackage(vararg packageParts: String?) = packageParts.filter { !it.isNullOrBlank() }.joinToString(".")
fun fullStringClassName(vararg nameParts: String?) = nameParts.filter { !it.isNullOrBlank() }.joinToString("")

interface INamingStrategy

interface IClassNameStrategy : INamingStrategy {
    fun poetType(className: String, packageName: String, prefix: String = "", postfix: String = ""): TypeName
    fun asVarname(className: String, prefix: String = "", postfix: String = ""): String
}

class ClassNameStrategyCamelCase : IClassNameStrategy {
    override fun poetType(className: String, packageName: String, prefix: String, postfix: String): TypeName {
        return ClassName(packageName, MixedCaseString(fullStringClassName(prefix, className, postfix)).toUpperCamelCase())
    }

    override fun asVarname(className: String, prefix: String, postfix: String): String {
        return MixedCaseString(fullStringClassName(prefix, className, postfix)).toLowerCamelCase()
    }
}

interface ITableNameStrategy : INamingStrategy {
    fun tableName(className: String, packageName: String, prefix: String = "", postfix: String = ""): String
}


class TableNameStrategyLowerSnakeCase : ITableNameStrategy {
    override fun tableName(className: String, packageName: String, prefix: String, postfix: String): String {
        return MixedCaseString(fullStringClassName(prefix, className, postfix)).toLowerSnakeCase()
    }
}
