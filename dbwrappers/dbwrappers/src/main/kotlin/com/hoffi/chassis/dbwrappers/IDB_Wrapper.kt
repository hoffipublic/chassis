package com.hoffi.chassis.dbwrappers

import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.TYPTranslation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

interface IDB_Wrapper {
    val BatchInsertStatementClassName: ClassName
    val ColumnClassName: ClassName
    val ColumnSetClassName: ClassName
    val DatabaseClassName: ClassName
    val JoinClassName: ClassName
    val JoinTypeClassName: ClassName
    val OpClassName: ClassName
    val QueryClassName: ClassName
    val ResultRowClassName: ClassName
    val SqlExpressionBuilderClassName: ClassName
    val StdOutSqlLoggerClassName: ClassName
    val TableClassName: ClassName
    val TablePrimaryKeyClassName: ClassName
    val TransactionClassName: ClassName

    fun Column(columnKClass: KClass<*>) : TypeName
    fun Column(columnTypeName: ClassName) : TypeName
    fun InsertStatementTypeName(): TypeName
    val batchInsertMember: MemberName
    val insertMember: MemberName
    val selectMember: MemberName
    val transactionAddLoggerMember: MemberName
    val transactionMember: MemberName

    fun coreTypeTranslation(typ: TYP): TYPTranslation
}
