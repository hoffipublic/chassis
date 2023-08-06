package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.squareup.kotlinpoet.ClassName

abstract class AHasCopyBoundrysData(override var businessName: String, val targetDslRef: IDslRef, val sourceDslRef: IDslRef)
    : IHasCopyBoundrys
{
    override val theCopyBoundrys: MutableMap<COPYTYPE, CopyBoundry> = this.initTheCopyBoundrys()

    fun add(copyType: COPYTYPE, vararg propName: String): AHasCopyBoundrysData { theCopyBoundrys[copyType]!!.add(*propName) ; return this }
    fun add(copyType: COPYTYPE, vararg dslRef: IDslRef): AHasCopyBoundrysData { theCopyBoundrys[copyType]!!.add(*dslRef) ; return this }
    fun add(copyType: COPYTYPE, vararg className: ClassName): AHasCopyBoundrysData { theCopyBoundrys[copyType]!!.add(*className) ; return this }
}
interface IHasCopyBoundrys {
    var businessName: String
    val theCopyBoundrys: MutableMap<COPYTYPE, CopyBoundry>
    fun initTheCopyBoundrys(): MutableMap<COPYTYPE, CopyBoundry> = mutableMapOf<COPYTYPE, CopyBoundry>().also {
        for (copyType in COPYTYPE.entries) {
            it[copyType] = CopyBoundry(copyType)
        }
    }
}

sealed class EitherPropNamePropRefModelRefOrClassName {
    data class EitherPropName(val propNames: MutableSet<String> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
    data class EitherPropRef(val propRefs: MutableSet<DslRef.prop> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
    data class EitherModelRef(val modelRefs: MutableSet<DslRef.IModelOrModelSubelement> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
    data class EitherClassName(val classNames: MutableSet<ClassName> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
}
data class CopyBoundry(val copyType: COPYTYPE) {
    override fun toString() = when (copyType) {
        COPYTYPE.IGNORE -> toStringPrivate("IG:")
        COPYTYPE.INSTANCE -> toStringPrivate("IN:")
        COPYTYPE.NEW -> toStringPrivate("NE:")
        COPYTYPE.DEEP -> toStringPrivate("DE:")
        COPYTYPE.DEEPNEW -> toStringPrivate("DN:")
    }

    private fun toStringPrivate(ctPrefix: String) : String {
        val resultString =
            if (eitherPropNames.propNames.isNotEmpty()) eitherPropNames.propNames.joinToString(",", prefix = ctPrefix, postfix = "|") else { "" } +
            if (eitherPropRefs.propRefs.isNotEmpty()) eitherPropRefs.propRefs.joinToString(",", prefix = ctPrefix, postfix = "|") {
                it.refList.takeLast(2).joinToString(DslRef.ATOMSEP)
            } else { "" } +
            if (eitherModelRefs.modelRefs.isNotEmpty()) eitherModelRefs.modelRefs.joinToString(",", prefix = ctPrefix, postfix = "|") {
                it.refList.takeLast(2).joinToString(DslRef.ATOMSEP)
            } else { "" } +
            if (eitherClassNames.classNames.isNotEmpty()) eitherClassNames.classNames.joinToString(",", prefix = ctPrefix) {
                it.simpleName
            } else { "" }
        return resultString
    }

    fun add(vararg propName: String) = eitherPropNames.propNames.addAll(propName)
    fun add(vararg dslRef: IDslRef) {
        for (ref in dslRef) {
            when (ref) {
                is DslRef.IModelOrModelSubelement -> eitherModelRefs.modelRefs.add(ref)
                is DslRef.Iprop -> eitherPropRefs.propRefs.add(ref as DslRef.prop)
                else -> throw DslException("illegal ${this::class.simpleName} DslRef '${ref::class.simpleName}")
            }
        }
    }
    fun add(vararg className: ClassName) = eitherClassNames.classNames.addAll(className)

    val eitherPropNames = EitherPropNamePropRefModelRefOrClassName.EitherPropName()
    val eitherPropRefs = EitherPropNamePropRefModelRefOrClassName.EitherPropRef()
    val eitherModelRefs = EitherPropNamePropRefModelRefOrClassName.EitherModelRef()
    val eitherClassNames = EitherPropNamePropRefModelRefOrClassName.EitherClassName()
}
