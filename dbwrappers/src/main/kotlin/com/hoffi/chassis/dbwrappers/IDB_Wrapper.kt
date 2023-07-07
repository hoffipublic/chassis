package com.hoffi.chassis.dbwrappers

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

interface IDB_Wrapper {
    val TableClassName: ClassName
    val TablePrimaryKeyClassName: ClassName
    val ColumnClassName: ClassName

    fun Column(columnKClass: KClass<*>) : TypeName
    fun Column(columnTypeName: ClassName) : TypeName
}
