package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.values.DslPath
import okio.Path.Companion.toPath

/** all nameAndWhereto { } information from viewpoint of one(!) Element (e.g. model or api) */
class SharedGatheredNameAndWheretos(val  dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    enum class THINGSWITHNAMEANDWHERETOS { DslRunConfigure, Modelgroup, model }
    var fromDslRunConfigure: SharedNameAndWhereto
        get() = allFromDslRunConfigure[C.DEFAULT] ?: throw DslException("no '${C.DEFAULT}' nameAndWhereto in DslRun.configure { }")
        private set(value) { allFromDslRunConfigure[C.DEFAULT] = value }
    var fromGroup: SharedNameAndWhereto
        get() = allFromGroup[C.DEFAULT] ?: throw DslException("no '${C.DEFAULT}' nameAndWhereto in group '$dslRef'")
        private set(value) { allFromGroup[C.DEFAULT] = value }
    var fromElement: SharedNameAndWhereto
        get() = allFromElement[C.DEFAULT] ?: throw DslException("no '${C.DEFAULT}' nameAndWhereto in element '$dslRef'")
        private set(value) { allFromElement[C.DEFAULT] = value }
    val allFromDslRunConfigure: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val fromDslRunConfigureForSubelement: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromGroup: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val fromGroupForSubelement: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromElement: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val fromElementForSubelement: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val fromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()

    fun createFor(thing: THINGSWITHNAMEANDWHERETOS, sharedNameAndWhereto: SharedNameAndWhereto) {
        val existingSharedNameAndWhereto = when (thing) {
            THINGSWITHNAMEANDWHERETOS.DslRunConfigure -> allFromDslRunConfigure[sharedNameAndWhereto.simpleName]
            THINGSWITHNAMEANDWHERETOS.Modelgroup -> allFromGroup[sharedNameAndWhereto.simpleName]
            THINGSWITHNAMEANDWHERETOS.model -> allFromElement[sharedNameAndWhereto.simpleName]
        }
        if (existingSharedNameAndWhereto != null) {
            throw DslException("$thing of $dslRef has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        when (thing) {
            THINGSWITHNAMEANDWHERETOS.DslRunConfigure -> allFromDslRunConfigure[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
            THINGSWITHNAMEANDWHERETOS.Modelgroup -> allFromGroup[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
            THINGSWITHNAMEANDWHERETOS.model -> allFromElement[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
        }
    }
    fun createFor(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = fromSubelements.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef} has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName]
    }
    fun createFromElementForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = fromElementForSubelement.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef.parentRef} has more than one nameAndWhereto for '${subElementRef.refList.last().functionName}' with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName]
    }
    fun createFromGroupForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = fromGroupForSubelement.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef.parentRef} has more than one nameAndWhereto for '${subElementRef.refList.last().functionName}' with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName]
    }
    fun createFromDslRunForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = fromDslRunConfigureForSubelement.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef.parentRef} has more than one nameAndWhereto for '${subElementRef.refList.last().functionName}' with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName]
    }
}

interface ISharedNameAndWhereto {
    var baseDir: String
        get() = baseDirPath.toString()
        set(value) { baseDirPath = DslPath(value.toPath()) }
    var baseDirPath: DslPath
    var path: String
        get() = pathPath.toString()
        set(value) { pathPath = DslPath(value.toPath()) }
    var pathPath: DslPath
    var classPrefix: String
    var classPostfix: String
    var packageName: String
}

class SharedNameAndWhereto(
    val simpleName: String,
    val dslRef: DslRef,
    override var baseDirPath: DslPath,
    override var pathPath: DslPath,
    override var classPrefix: String,
    override var classPostfix: String,
    override var packageName: String
) : ISharedNameAndWhereto {
}

