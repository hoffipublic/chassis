package com.hoffi.chassis.shared.strategies

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException

object ChassisStrategies {
//    val illegalClassCharsRegex = "[.:;/\\-]".toRegex()
//    val illegalVarnameRegex = illegalClassCharsRegex
//    val illegalPackageCharsRegex = "[:;/\\-]".toRegex()
//
//    fun toModifiedClassname(name: String) = name.replace(illegalClassCharsRegex, "").replaceFirstChar { if (it.isLowerCase()) it.titlecase(
//        C.LOCALE) else it.toString() }
//    fun toModifiedVarname(name: String) = name.replace(illegalClassCharsRegex, "").replaceFirstChar { it.lowercase(C.LOCALE) }
//    fun toModifiedPackagename(name: String) = name.replace(illegalPackageCharsRegex, "").replaceFirstChar { it.lowercase(C.LOCALE) }
//    fun toModifiedDbTableName(name: String) = name.toSnakeCase().lowercase()
//    fun toModifiedDbColName(name: String) = name.toSnakeCase().lowercase()

    private val classNameStrategies = mutableMapOf<String, IClassNameStrategy>(
        C.DEFAULT to ClassNameStrategyCamelCase()
    )
    fun classNameStrategy(strategyName: String = C.DEFAULT): IClassNameStrategy {
        return classNameStrategies[strategyName] ?: throw DslException("unknown classNameStrategy: '$strategyName'")
    }

    private val tableNameStrategies = mutableMapOf<String, ITableNameStrategy>(
        C.DEFAULT to TableNameStrategyLowerSnakeCase()
    )
    fun tableNameStrategy(strategyName: String = C.DEFAULT): ITableNameStrategy {
        return tableNameStrategies[strategyName] ?: throw DslException("unknown tableNameStrategy: '$strategyName'")
    }
}
