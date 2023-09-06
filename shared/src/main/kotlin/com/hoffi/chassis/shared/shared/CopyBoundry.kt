package com.hoffi.chassis.shared.shared

import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.squareup.kotlinpoet.ClassName

data class CopyBoundry(val copyType: COPYTYPE, val shallowFalseDeepTrue: Boolean, val boundryType: BOUNDRYTYPE) {
    enum class BOUNDRYTYPE { propName, propRef, modelRef, className }
}

abstract class AHasCopyBoundrysData(var businessName: String, val targetDslRef: IDslRef, val sourceDslRef: IDslRef) {
    val propNameCopyBoundrys = mutableMapOf<String, CopyBoundry>()
    val propRefCopyBoundrys = mutableMapOf<DslRef.prop, CopyBoundry>()
    val modelRefCopyBoundrys = mutableMapOf<DslRef.IModelOrModelSubelement, CopyBoundry>()
    val classNameCopyBoundrys = mutableMapOf<ClassName, CopyBoundry>()

    fun addAllFrom(aHasCopyBoundrysData: AHasCopyBoundrysData) {
        propNameCopyBoundrys.putAll(aHasCopyBoundrysData.propNameCopyBoundrys)
        propRefCopyBoundrys.putAll(aHasCopyBoundrysData.propRefCopyBoundrys)
        modelRefCopyBoundrys.putAll(aHasCopyBoundrysData.modelRefCopyBoundrys)
        classNameCopyBoundrys.putAll(aHasCopyBoundrysData.classNameCopyBoundrys)
    }

    fun addPropNameCopyBoundry(copyType: COPYTYPE, shallowFalseDeepTrue: Boolean, vararg propName: String): AHasCopyBoundrysData {
        for (it in propName) {
            val newCopyBoundry = CopyBoundry(copyType, shallowFalseDeepTrue, CopyBoundry.BOUNDRYTYPE.propName)
            val copyBoundryExisted = propNameCopyBoundrys.putIfAbsent(it, newCopyBoundry)
            if (copyBoundryExisted != null && copyBoundryExisted != newCopyBoundry) {
                // it already had a different CopyBoundry
                throw DslException("ambiguous propName CopyBoundry for '$it' in $this: old->'$copyBoundryExisted' <-> '$newCopyBoundry'<-new")
            }
        }
        return this
    }
    fun addPropRefCopyBoundry(copyType: COPYTYPE, shallowFalseDeepTrue: Boolean, vararg propRef: DslRef.prop): AHasCopyBoundrysData {
        for (it in propRef) {
            val newCopyBoundry = CopyBoundry(copyType, shallowFalseDeepTrue, CopyBoundry.BOUNDRYTYPE.propRef)
            val copyBoundryExisted = propRefCopyBoundrys.putIfAbsent(it, newCopyBoundry)
            if (copyBoundryExisted != null && copyBoundryExisted != newCopyBoundry) {
                // it already had a different CopyBoundry
                throw DslException("ambiguous propRef CopyBoundry for '$it': old->'$copyBoundryExisted' <-> '$newCopyBoundry'<-new in $this")
            }
        }
        return this
    }
    fun addModelRefCopyBoundry(copyType: COPYTYPE, shallowFalseDeepTrue: Boolean, vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData {
        for (it in modelRef) {
            val newCopyBoundry = CopyBoundry(copyType, shallowFalseDeepTrue, CopyBoundry.BOUNDRYTYPE.modelRef)
            val copyBoundryExisted = modelRefCopyBoundrys.putIfAbsent(it, newCopyBoundry)
            if (copyBoundryExisted != null && copyBoundryExisted != newCopyBoundry) {
                // it already had a different CopyBoundry
                throw DslException("ambiguous modelRef CopyBoundry for '$it': old->$copyBoundryExisted <-> $newCopyBoundry<-new in $this")
            }
        }
        return this
    }
    fun addClassNameCopyBoundry(copyType: COPYTYPE, shallowFalseDeepTrue: Boolean, vararg className: ClassName): AHasCopyBoundrysData {
        for (it in className) {
            val newCopyBoundry = CopyBoundry(copyType, shallowFalseDeepTrue, CopyBoundry.BOUNDRYTYPE.className)
            val copyBoundryExisted = classNameCopyBoundrys.putIfAbsent(it, newCopyBoundry)
            if (copyBoundryExisted != null && copyBoundryExisted != newCopyBoundry) {
                // it already had a different CopyBoundry
                throw DslException("ambiguous modelRef CopyBoundry for '$it': old->$copyBoundryExisted <-> $newCopyBoundry<-new in $this")
            }
        }
        return this
    }

    override fun toString(): String {
        val sbIGNORE = StringBuilder("IG:")
        val sbINSTANCE = StringBuilder("IN:")
        val sbNEW = StringBuilder("NE:")
        val sbDEEP = StringBuilder("DE:")
        val sbDEEPNEW = StringBuilder("DN:")
        val IG = mutableListOf<String>()
        val IN = mutableListOf<String>()
        val NE = mutableListOf<String>()
        val DE = mutableListOf<String>()
        val DN = mutableListOf<String>()

        for (e in propNameCopyBoundrys) {
            when (e.value.copyType) {
                COPYTYPE.IGNORE ->   IG.add(if (e.value.shallowFalseDeepTrue) "d'${e.key}'" else "s'${e.key}'")
                COPYTYPE.INSTANCE -> IN.add(if (e.value.shallowFalseDeepTrue) "d'${e.key}'" else "s'${e.key}'")
                COPYTYPE.NEW ->      NE.add(if (e.value.shallowFalseDeepTrue) "d'${e.key}'" else "s'${e.key}'")
                COPYTYPE.DEEP ->     DE.add(if (e.value.shallowFalseDeepTrue) "d'${e.key}'" else "s'${e.key}'")
                COPYTYPE.DEEPNEW ->  DN.add(if (e.value.shallowFalseDeepTrue) "d'${e.key}'" else "s'${e.key}'")
            }
        }
        var none = true
        val result: StringBuilder =  StringBuilder()
        if (IG.isNotEmpty()) { none = false ; result.append("IG:${IG.joinToString(",", postfix = "|")}") }
        if (IN.isNotEmpty()) { none = false ; result.append("IN:${IN.joinToString(",", postfix = "|")}") }
        if (NE.isNotEmpty()) { none = false ; result.append("NE:${NE.joinToString(",", postfix = "|")}") }
        if (DE.isNotEmpty()) { none = false ; result.append("DE:${DE.joinToString(",", postfix = "|")}") }
        if (DN.isNotEmpty()) { none = false ; result.append("DN:${DN.joinToString(",", postfix = "|")}") }
        return if (result.isBlank()) "NONE" else if (result.endsWith('|')) result.substring(0, result.length-1) else result.toString()
    }
}
