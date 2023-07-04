package com.hoffi.chassis.shared

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
import com.hoffi.chassis.shared.parsedata.nameandwhereto.NameAndWheretoDefaults
import com.hoffi.chassis.shared.shared.Initializer
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

interface ITypOrModelOrPoetType {
    val isInterface: Boolean
    val initializer: Initializer
    fun validate(any: Any)
    //fun classname(prefix: String = "", postfix: String = "")
}

sealed class EitherTypOrModelOrPoetType(override val initializer: Initializer) : ITypOrModelOrPoetType {
    lateinit var modelClassName: ModelClassName

    class EitherTyp constructor(val typ: TYP, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer) {
        override fun toString() = "${this::class.simpleName}($typ)"
        init { modelClassName = fakePoetTypeClassName(typ.poetType) }
        override val isInterface = false
        override fun validate(any: Any) {
            modelClassName.validate(any)
        }
    }
    class EitherModel constructor(val modelSubElementRefOriginal: DslRef.IModelOrModelSubelement, override var isInterface: Boolean, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer) {
        override fun toString() = "${this::class.simpleName}($modelSubElementRef)"
        // modelClassName (with poetType) init'ed in finish of modelSubelement
        val modelSubElementRef: DslRef.IModelSubelement
            get() = modelSubElementRefExpanded ?: modelSubElementRefOriginal as DslRef.IModelSubelement
        var modelSubElementRefExpanded: DslRef.IModelSubelement? = null

        override fun validate(any: Any) {
            modelClassName.validate(any)
        }
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EitherModel) return false
            return modelSubElementRef == other.modelSubElementRef
        }
        override fun hashCode() = modelSubElementRef.hashCode()
    }
    class EitherPoetType constructor(val poetType: ClassName, override val isInterface: Boolean, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer) {
        override fun toString() = "${this::class.simpleName}($poetType)"
        init { modelClassName = fakePoetTypeClassName(poetType) }
        override fun validate(any: Any) {
            modelClassName.validate(any)
        }
    }
    class NOTHING : EitherTypOrModelOrPoetType(Initializer.EMPTY) {
        override fun toString() = "${this::class.simpleName}"
        override val isInterface = false
        override fun validate(any: Any) {}
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return (other is NOTHING)
        }
        override fun hashCode() = 42
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EitherTypOrModelOrPoetType) return false
        return modelClassName == other.modelClassName
    }
    override fun hashCode() = modelClassName.hashCode()


    protected fun fakePoetTypeClassName(thePoetType: ClassName): ModelClassName = ModelClassName(IDslRef.NULL, thePoetType).apply {
            classNameStrategy = NameAndWheretoDefaults.classNameStrategy
            tableNameStrategy = NameAndWheretoDefaults.tableNameStrategy
            basePath = NameAndWheretoDefaults.basePath
            path = NameAndWheretoDefaults.path
            basePackage = NameAndWheretoDefaults.basePackage
            packageName = NameAndWheretoDefaults.packageName
            classPrefix = NameAndWheretoDefaults.classPrefix
            classPostfix = NameAndWheretoDefaults.classPostfix

            poetType = thePoetType
            modelOrTypeNameString = thePoetType.simpleName
        }

    companion object {
        val NOTHING = NOTHING()
        fun KClass<*>.createPoetType() = EitherPoetType(this.asClassName(), this.java.isInterface, Initializer.EMPTY)
        fun expandReffedEitherToSubelementIfModel(
            either: EitherTypOrModelOrPoetType,
            callerModelSubelementRef: DslRef.ISubElementLevel,
        ) {
            when (either) {
                is EitherTypOrModelOrPoetType.EitherModel -> {
                    when (either.modelSubElementRefOriginal) {
                        is DslRef.model -> {
                            WhensDslRef.whenModelSubelement(callerModelSubelementRef,
                                isDtoRef = {   either.modelSubElementRefExpanded = DslRef.dto(C.DEFAULT, either.modelSubElementRefOriginal) },
                                isTableRef = { either.modelSubElementRefExpanded = DslRef.table(C.DEFAULT, either.modelSubElementRefOriginal) }
                            ) {
                                DslException("no known model subelement")
                            }
                        }
                        is DslRef.dto -> {}
                        is DslRef.table -> {}
                        else -> throw DslException("no (known) model or modelSubelement")
                    }
                }
                is EitherTypOrModelOrPoetType.EitherPoetType -> {}
                is EitherTypOrModelOrPoetType.EitherTyp -> {}
                is EitherTypOrModelOrPoetType.NOTHING -> {}
            }
        }
    }
}

