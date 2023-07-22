package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.squareup.kotlinpoet.ClassName

open class FillerData constructor(var fillerName: String, val targetDslRef: IDslRef, val sourceDslRef: IDslRef, funcNamePostfix: String = "") {
    override fun toString() = "${this::class.simpleName}('$fillerName', target: '${targetDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', source: '${sourceDslRef.refList.takeLast(2).joinToString(DslRef.ATOMSEP)}', " +
        theCopyBoundrys.values.map { it.toString() }.joinToString("").ifBlank { "NONE" } + ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FillerData) return false
        if (fillerName != other.fillerName) return false
        if (targetDslRef != other.targetDslRef) return false
        return sourceDslRef == other.sourceDslRef
    }
    override fun hashCode(): Int {
        var result = fillerName.hashCode()
        result = 31 * result + targetDslRef.hashCode()
        result = 31 * result + sourceDslRef.hashCode()
        return result
    }

    enum class COPYTYPE {IGNORE, INSTANCE, NEW, DEEP, DEEPNEW }

    //val uuid = UUID.randomUUID()
    val theCopyBoundrys: MutableMap<COPYTYPE, CopyBoundry> = mutableMapOf<COPYTYPE, CopyBoundry>().also {
        for (copyType in COPYTYPE.entries) {
            it[copyType] = CopyBoundry(copyType)
        }
    }

}
class SynthFillerData(fillerName: String, targetDslRef: IDslRef, sourceDslRef: IDslRef, funcNamePostfix: String = "", val via: String)
    : FillerData(fillerName, targetDslRef, sourceDslRef) {
    override fun toString() = "${super.toString()}->\"$via\""
}

sealed class EitherPropNamePropRefModelRefOrClassName {
    data class EitherPropName(val propNames: MutableSet<String> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
    data class EitherPropRef(val propRefs: MutableSet<DslRef.prop> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
    data class EitherModelRef(val modelRefs: MutableSet<DslRef.IModelOrModelSubelement> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
    data class EitherClassName(val classNames: MutableSet<ClassName> = mutableSetOf()) : EitherPropNamePropRefModelRefOrClassName()
}
data class CopyBoundry(val copyType: FillerData.COPYTYPE) {
    override fun toString() = when (copyType) {
        FillerData.COPYTYPE.IGNORE -> toStringPrivate("IG:")
        FillerData.COPYTYPE.INSTANCE -> toStringPrivate("IN:")
        FillerData.COPYTYPE.NEW -> toStringPrivate("NE:")
        FillerData.COPYTYPE.DEEP -> toStringPrivate("DE:")
        FillerData.COPYTYPE.DEEPNEW -> toStringPrivate("DN:")
    }

    private fun toStringPrivate(ctPrefix: String) : String {
        val resultString =
            if (eitherPropNames.propNames.isNotEmpty()) eitherPropNames.propNames.joinToString(",", prefix = ctPrefix, postfix = "|") else "" +
            if (eitherPropRefs.propRefs.isNotEmpty()) eitherPropRefs.propRefs.map { it.refList.takeLast(2).joinToString(DslRef.ATOMSEP) }.joinToString(",", prefix = ctPrefix, postfix = "|")  else "" +
            if (eitherModelRefs.modelRefs.isNotEmpty()) eitherModelRefs.modelRefs.map { it.refList.takeLast(2).joinToString(DslRef.ATOMSEP) }.joinToString(",", prefix = ctPrefix, postfix = "|") else "" +
            if (eitherClassNames.classNames.isNotEmpty()) eitherClassNames.classNames.map { it.simpleName }.joinToString(",", prefix = ctPrefix) else ""
        return resultString
    }

    val eitherPropNames = EitherPropNamePropRefModelRefOrClassName.EitherPropName()
    val eitherPropRefs = EitherPropNamePropRefModelRefOrClassName.EitherPropRef()
    val eitherModelRefs = EitherPropNamePropRefModelRefOrClassName.EitherModelRef()
    val eitherClassNames = EitherPropNamePropRefModelRefOrClassName.EitherClassName()
}
