package com.hoffi.chassis.dbwrappers.exposed

import com.hoffi.chassis.dbwrappers.IDB_Wrapper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

object DB_EXPOSED : IDB_Wrapper {
    override val TableClassName = ClassName("org.jetbrains.exposed.sql","Table")
    override val TablePrimaryKeyClassName = ClassName("org.jetbrains.exposed.sql","Table", "PrimaryKey")
    override val ColumnClassName = ClassName("org.jetbrains.exposed.sql", "Column")

    override fun Column(columnKClass: KClass<*>) : TypeName {
        return Column(columnKClass.asClassName())
    }
    override fun Column(columnTypeName: ClassName): TypeName {
        return ColumnClassName.parameterizedBy(columnTypeName)
    }
}
