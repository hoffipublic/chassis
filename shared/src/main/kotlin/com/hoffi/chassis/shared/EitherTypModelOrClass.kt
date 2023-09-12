package com.hoffi.chassis.shared

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.ReplaceAppendOrModify
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.parsedata.nameandwhereto.ModelClassName
import com.hoffi.chassis.shared.parsedata.nameandwhereto.NameAndWheretoDefaults
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

interface ITypOrModelOrPoetType {
    val isInterface: Boolean
    val initializer: Initializer
    fun validate(any: Any)
    fun copyDeep() : EitherTypOrModelOrPoetType
    fun copyDeepForDsl() : EitherTypOrModelOrPoetType
    //fun classname(prefix: String = "", postfix: String = "")
}

sealed class EitherTypOrModelOrPoetType(override val initializer: Initializer) : ITypOrModelOrPoetType {
    /** for EitherModel's modelClassName is not "valid" to use for DSL Logic,
     * it is only valid to use in CodeGen or things further down the stream
     * a valid modelClassName (with poetType and interface information) will be init'ed in finish of modelSubelement */
    lateinit var modelClassName: ModelClassName
    abstract fun finish(replaceAppendOrModify: ReplaceAppendOrModify, formatAddendum: String, argsAddendum: MutableList<Any>): EitherTypOrModelOrPoetType

    class EitherTyp(val typ: TYP, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer) {
        override fun toString() = "${this::class.simpleName!!.removePrefix("Either")}($typ)"
        init { modelClassName = fakePoetTypeClassName(typ.poetType) }
        override val isInterface = typ.isInterface
        override fun finish(replaceAppendOrModify: ReplaceAppendOrModify, formatAddendum: String, argsAddendum: MutableList<Any>): EitherTypOrModelOrPoetType {
            return EitherTyp(typ, Initializer.of(initializer.originalFormat, initializer.originalArgs, replaceAppendOrModify, formatAddendum, argsAddendum))
        }
        override fun validate(any: Any) {
            modelClassName.validate(any)
        }
        override fun copyDeep() : EitherTypOrModelOrPoetType = copyDeepForDsl().also { it.modelClassName = modelClassName }
        override fun copyDeepForDsl() : EitherTypOrModelOrPoetType {
            return EitherTyp(typ, initializer.copy())
        }
    }
    class EitherModel(val modelSubElementRefOriginal: DslRef.IModelOrModelSubelement, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer) {
        override fun toString() = "${this::class.simpleName!!.removePrefix("Either")}($modelSubElementRef)"
        // modelClassName (with poetType) init'ed in finish of modelSubelement
        /** invalid to use before modelsubelement finish'ed */
        override val isInterface: Boolean by lazy { modelClassName.modelClassDataFromDsl.isInterface }
        override fun finish(replaceAppendOrModify: ReplaceAppendOrModify, formatAddendum: String, argsAddendum: MutableList<Any>): EitherTypOrModelOrPoetType {
            return EitherModel(modelSubElementRefOriginal, Initializer.of(initializer.originalFormat, initializer.originalArgs, replaceAppendOrModify, formatAddendum, argsAddendum))
        }
        val modelSubElementRef: DslRef.IModelSubelement
            get() = modelSubElementRefExpanded ?: modelSubElementRefOriginal as DslRef.IModelSubelement
        var modelSubElementRefExpanded: DslRef.IModelSubelement? = null

        override fun validate(any: Any) {
            modelClassName.validate(any)
        }
        override fun copyDeep() : EitherTypOrModelOrPoetType = copyDeepForDsl().also { it.modelClassName = modelClassName ; (it as EitherModel).modelSubElementRefExpanded = modelSubElementRefExpanded}
        override fun copyDeepForDsl() : EitherTypOrModelOrPoetType {
            return EitherModel(modelSubElementRefOriginal, initializer.copy())
        }
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EitherModel) return false
            return modelSubElementRefOriginal == other.modelSubElementRefOriginal
        }
        override fun hashCode() = modelSubElementRefOriginal.hashCode()
    }
    class EitherPoetType(val poetType: ClassName, override var isInterface: Boolean, initializer: Initializer) : EitherTypOrModelOrPoetType(initializer) {
        override fun toString() = "${this::class.simpleName!!.removePrefix("Either")}($poetType)"
        init { modelClassName = fakePoetTypeClassName(poetType) }
        override fun finish(replaceAppendOrModify: ReplaceAppendOrModify, formatAddendum: String, argsAddendum: MutableList<Any>): EitherTypOrModelOrPoetType {
            return EitherPoetType(poetType, isInterface, Initializer.of(initializer.originalFormat, initializer.originalArgs, replaceAppendOrModify, formatAddendum, argsAddendum))
        }
        override fun validate(any: Any) {
            modelClassName.validate(any)
        }
        override fun copyDeep() : EitherTypOrModelOrPoetType = copyDeepForDsl().also { it.modelClassName = modelClassName }
        override fun copyDeepForDsl() : EitherTypOrModelOrPoetType {
            return EitherPoetType(poetType, isInterface, initializer.copy())
        }
    }
    class NOTHING : EitherTypOrModelOrPoetType(Initializer.EMPTY) {
        override fun toString() = "${this::class.simpleName}"
        override val isInterface = false
        override fun finish(
            replaceAppendOrModify: ReplaceAppendOrModify,
            formatAddendum: String,
            argsAddendum: MutableList<Any>
        ): EitherTypOrModelOrPoetType = NOTHING
        override fun validate(any: Any) {}
        override fun copyDeep() = this
        override fun copyDeepForDsl() = this
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
                                isDcoRef = {   either.modelSubElementRefExpanded = DslRef.dco(C.DEFAULT, either.modelSubElementRefOriginal) },
                                isTableRef = {
                                    // DSL caller has to change this the the Dsl proper Subelement tableFor of abstract class AProperModelSubelement
                                    either.modelSubElementRefExpanded = DslRef.table(C.DEFAULT, either.modelSubElementRefOriginal)
                                }
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

