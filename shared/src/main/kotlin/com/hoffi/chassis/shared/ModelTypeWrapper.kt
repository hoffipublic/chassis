package com.hoffi.chassis.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.squareup.kotlinpoet.KModifier

/** created and filled by dsl after having done all PASS'es and evaluated/transformed all necessary data to provide it bite-sized to codegen via this `ModelTypeWrapper` */
sealed class ModelTypeWrapper private constructor(private val dsRef: DslRef, kModifier: MutableList<KModifier>) {
    protected val kind: EitherClassObjectOrInterface = EitherClassObjectOrInterface.EitherClass(kModifier)
    protected val packageName: MutableList<String> = mutableListOf()
    protected val simpleName: String = ""
    protected val prefixClassname: MutableList<String> = mutableListOf()
    protected val postfixClassname: MutableList<String> = mutableListOf()
    protected val prefixPackages: MutableList<String> = mutableListOf()
    protected val postfixPackages: MutableList<String> = mutableListOf()

    fun modelName() = simpleName
    abstract fun className(): String
    abstract fun asVarName(prefix: String, postfix: String)
    abstract fun asTableName(prefix: String, postfix: String)
    abstract fun asFillerName(prefix: String, postfix: String)

//    TODO ModelTypeWrapper for DtoType|TableType|Plain|...
//    class DtoTypeWrapper(): ModelTypeWrapper() {
//        fun className() = joinName()
//    }
//    class TableTypeWrapper(): ModelTypeWrapper()
//    class PlainTypeWrapper(): ModelTypeWrapper()
}
