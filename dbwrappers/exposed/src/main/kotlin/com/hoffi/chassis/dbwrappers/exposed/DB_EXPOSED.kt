package com.hoffi.chassis.dbwrappers.exposed

import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.RuntimeDefaults.DB_MEMBER_datetime
import com.hoffi.chassis.chassismodel.RuntimeDefaults.DB_MEMBER_timestamp
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameInstant
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameLocalDateTime
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.TYPTranslation
import com.hoffi.chassis.dbwrappers.IDB_Wrapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.exposed.sql.statements.InsertStatement
import kotlin.reflect.KClass

object DB_EXPOSED : IDB_Wrapper {
    override val BatchInsertStatementClassName = ClassName("org.jetbrains.exposed.sql.statements", "BatchInsertStatement")
    override val ColumnClassName = ClassName("org.jetbrains.exposed.sql", "Column")
    override val ColumnSetClassName = ClassName("org.jetbrains.exposed.sql", "ColumnSet")
    override val DatabaseClassName = ClassName("org.jetbrains.exposed.sql","Database")
    override val JoinClassName = ClassName("org.jetbrains.exposed.sql", "Join")
    override val JoinTypeClassName = ClassName("org.jetbrains.exposed.sql", "JoinType")
    override val OpClassName = ClassName("org.jetbrains.exposed.sql", "Op")
    override val QueryClassName = ClassName("org.jetbrains.exposed.sql", "Query")
    override val ResultRowClassName = ClassName("org.jetbrains.exposed.sql", "ResultRow")
    override val SqlExpressionBuilderClassName = ClassName("org.jetbrains.exposed.sql", "SqlExpressionBuilder")
    override val ISqlExpressionBuilderClassName = ClassName("org.jetbrains.exposed.sql", "ISqlExpressionBuilder")
    override val StdOutSqlLoggerClassName = ClassName("org.jetbrains.exposed.sql", "StdOutSqlLogger")
    override val TableClassName = ClassName("org.jetbrains.exposed.sql","Table")
    override val TablePrimaryKeyClassName = ClassName("org.jetbrains.exposed.sql","Table", "PrimaryKey")
    override val TransactionClassName = ClassName("org.jetbrains.exposed.sql", "Transaction")


    override fun Column(columnKClass: KClass<*>) : TypeName {
        return Column(columnKClass.asClassName())
    }
    override fun Column(columnTypeName: ClassName): TypeName {
        return ColumnClassName.parameterizedBy(columnTypeName)
    }

    override fun InsertStatementTypeName(): TypeName = InsertStatement::class.asTypeName().parameterizedBy(Number::class.asTypeName())
    override val eqMember = MemberName(ISqlExpressionBuilderClassName, "eq")
    override val insertMember = MemberName("org.jetbrains.exposed.sql", "insert")
    override val batchInsertMember = MemberName("org.jetbrains.exposed.sql", "batchInsert")
    override val selectMember = MemberName("org.jetbrains.exposed.sql", "select")
    override val transactionAddLoggerMember = MemberName("org.jetbrains.exposed.sql", "addLogger", isExtension = true)
    override val transactionMember = MemberName("org.jetbrains.exposed.sql.transactions", "transaction")

    override fun coreTypeTranslation(typ: TYP): TYPTranslation {
        return when (typ) {
            is TYP.BOOL ->          TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(ClassName("kotlin", "Boolean")), Initializer.of("%L(%S)",     "bool"))
            is TYP.INT ->           TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(ClassName("kotlin", "Int")),     Initializer.of("%L(%S)",     "integer"))
            is TYP.LONG ->          TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(ClassName("kotlin", "Long")),    Initializer.of("%L(%S)",     "long"))
            is TYP.STRING ->        TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(ClassName("kotlin", "String")),  Initializer.of("%L(%S, %L)", "varchar"))
            is TYP.UUID ->          TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(ClassName("java.util", "UUID")), Initializer.of("%L(%S)",     "uuid"))
            is TYP.INSTANT ->       TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(classNameInstant),                                        Initializer.of("%M(%S)",     DB_MEMBER_timestamp))
            is TYP.LOCALDATETIME -> TYPTranslation(ClassName("org.jetbrains.exposed.sql", "Column").parameterizedBy(classNameLocalDateTime),                                  Initializer.of("%M(%S)",     DB_MEMBER_datetime))
        }
    }
}
