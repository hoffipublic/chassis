package com.hoffi.chassis.dbwrappers.exposed

import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.RuntimeDefaults.DB_MEMBER_datetime
import com.hoffi.chassis.chassismodel.RuntimeDefaults.DB_MEMBER_timestamp
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameInstant
import com.hoffi.chassis.chassismodel.RuntimeDefaults.classNameLocalDateTime
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.TYPTranslation
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
