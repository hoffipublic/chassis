package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
import okio.Path
import okio.Path.Companion.toPath

/** all nameAndWhereto { } information from viewpoint of one(!) Element (e.g. model or api) */
class SharedGatheredNameAndWheretos(val  dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${SharedGatheredNameAndWheretos::class.simpleName}($dslRef, $dslRunIdentifier)"
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
    val allFromDslRunConfigureForSubelement: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromGroup: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val allFromGroupForSubelement: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()

    val allFromElement: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val allFromElementForSubelement: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()

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
        val subelementsNameAndWheretosMap = allFromSubelements.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef} has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
    fun createFromElementForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromElementForSubelement.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef.parentRef} has more than one nameAndWhereto for '${subElementRef.refList.last().functionName}' with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
    fun createFromGroupForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromGroupForSubelement.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef.parentRef} has more than one nameAndWhereto for '${subElementRef.refList.last().functionName}' with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
    fun createFromDslRunForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromDslRunConfigureForSubelement.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef.parentRef} has more than one nameAndWhereto for '${subElementRef.refList.last().functionName}' with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
}

interface ISharedNameAndWhereto {
    var strategyClassName: IClassNameStrategy.STRATEGY
    var strategyTableName: ITableNameStrategy.STRATEGY
    var baseDir: String
        get() = baseDirPath.toString()
        set(value) { baseDirPath = value.toPath() }
    var baseDirPath: Path
    var path: String
        get() = pathPath.toString()
        set(value) { pathPath = value.toPath() }
    var pathPath: Path
    var classPrefix: String
    var classPostfix: String
    var basePackage: String
    var packageName: String
}

class SharedNameAndWhereto(
    val simpleName: String,
    val dslRef: IDslRef,
    override var baseDirPath: Path,
    override var pathPath: Path,
    override var classPrefix: String,
    override var classPostfix: String,
    override var basePackage: String,
    override var packageName: String,
    override var strategyClassName: IClassNameStrategy.STRATEGY, // = IClassNameStrategy.STRATEGY.DEFAULT,
    override var strategyTableName: ITableNameStrategy.STRATEGY, // = ITableNameStrategy.STRATEGY.DEFAULT,
) : ISharedNameAndWhereto {
    override fun toString() = "SharedNameAndWhereto(dslRef=$dslRef)"
}

object StrategyNameAndWhereto {
    enum class STRATEGY { SPECIAL_WINS }

    fun resolve(strategy: STRATEGY, dslRef: DslRef.ISubElementLevel, gatheredNameAndWheretos: SharedGatheredNameAndWheretos): ModelClassName {
        return when (strategy) {
            STRATEGY.SPECIAL_WINS -> specialWins(dslRef, gatheredNameAndWheretos)
        }
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, gatheredNameAndWheretos: SharedGatheredNameAndWheretos): ModelClassName {
        val modelClassName = ModelClassName(dslRef)
        modelClassName.apply {
            val g = gatheredNameAndWheretos
            val eventualSharedNameAndWhereto = SharedNameAndWhereto(
                dslRef.simpleName,
                dslRef,
                NameAndWheretoDefaults.basePath,
                NameAndWheretoDefaults.path,
                NameAndWheretoDefaults.classPrefix,
                NameAndWheretoDefaults.classPostfix,
                NameAndWheretoDefaults.basePackage,
                NameAndWheretoDefaults.packageName,
                IClassNameStrategy.STRATEGY.DEFAULT,
                ITableNameStrategy.STRATEGY.DEFAULT,
            )
            val sharedNameAndWheretoSE = g.allFromSubelements[dslRef]?.get(C.DEFAULT) ?: g.allFromSubelements[dslRef]?.values?.firstOrNull()
            if (sharedNameAndWheretoSE != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoSE, eventualSharedNameAndWhereto)
            val sharedNameAndWheretoESE = g.allFromElementForSubelement[dslRef]?.get(C.DEFAULT) ?: g.allFromElementForSubelement[dslRef]?.values?.firstOrNull()
            if (sharedNameAndWheretoESE != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoESE, eventualSharedNameAndWhereto)
            val sharedNameAndWheretoE = g.allFromElement[C.DEFAULT] ?: g.allFromElement.values.firstOrNull()
            if (sharedNameAndWheretoE != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoE, eventualSharedNameAndWhereto)
            val sharedNameAndWheretoGSE = g.allFromGroupForSubelement[dslRef]?.get(C.DEFAULT) ?: g.allFromGroupForSubelement[dslRef]?.values?.firstOrNull()
            if (sharedNameAndWheretoGSE != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoGSE, eventualSharedNameAndWhereto)
            val sharedNameAndWheretoG = g.allFromGroup[C.DEFAULT] ?: g.allFromGroup.values.firstOrNull()
            if (sharedNameAndWheretoG != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoG, eventualSharedNameAndWhereto)
            val sharedNameAndWheretoRSE = g.allFromDslRunConfigureForSubelement[dslRef]?.get(C.DEFAULT) ?: g.allFromDslRunConfigureForSubelement[dslRef]?.values?.firstOrNull()
            if (sharedNameAndWheretoRSE != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoRSE, eventualSharedNameAndWhereto)
            val sharedNameAndWheretoR = g.allFromDslRunConfigure[C.DEFAULT] ?: g.allFromDslRunConfigure.values.firstOrNull()
            if (sharedNameAndWheretoR != null) takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoR, eventualSharedNameAndWhereto)
            classNameStrategy = ClassNameStrategy.get(eventualSharedNameAndWhereto.strategyClassName)
            tableNameStrategy = TableNameStrategy.get(eventualSharedNameAndWhereto.strategyTableName)
            basePath = eventualSharedNameAndWhereto.baseDirPath
            path = eventualSharedNameAndWhereto.pathPath
            basePackage = eventualSharedNameAndWhereto.basePackage
            packageName = eventualSharedNameAndWhereto.packageName
            classPrefix = eventualSharedNameAndWhereto.classPrefix
            classPostfix = eventualSharedNameAndWhereto.classPostfix
        }
        return modelClassName
    }

    private fun takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWhereto: SharedNameAndWhereto, eventualSharedNameAndWhereto: SharedNameAndWhereto) {
        if ( (sharedNameAndWhereto.baseDirPath != NameAndWheretoDefaults.basePath) && (eventualSharedNameAndWhereto.baseDirPath == NameAndWheretoDefaults.basePath) ) {
            eventualSharedNameAndWhereto.baseDirPath = sharedNameAndWhereto.baseDirPath
        }
        if ( (sharedNameAndWhereto.pathPath != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.pathPath == NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.pathPath = sharedNameAndWhereto.pathPath
        }
        if ( (sharedNameAndWhereto.classPrefix != NameAndWheretoDefaults.classPrefix) && (eventualSharedNameAndWhereto.classPrefix == NameAndWheretoDefaults.classPrefix) ) {
            eventualSharedNameAndWhereto.classPrefix = sharedNameAndWhereto.classPrefix
        }
        if ( (sharedNameAndWhereto.classPostfix != NameAndWheretoDefaults.classPostfix) && (eventualSharedNameAndWhereto.classPostfix == NameAndWheretoDefaults.classPostfix) ) {
            eventualSharedNameAndWhereto.classPostfix = sharedNameAndWhereto.classPostfix
        }
        if ( (sharedNameAndWhereto.basePackage != NameAndWheretoDefaults.basePackage) && (eventualSharedNameAndWhereto.basePackage == NameAndWheretoDefaults.basePackage) ) {
            eventualSharedNameAndWhereto.basePackage = sharedNameAndWhereto.basePackage
        }
        if ( (sharedNameAndWhereto.packageName != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.packageName == NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.packageName = sharedNameAndWhereto.packageName
        }
        if ( (sharedNameAndWhereto.strategyClassName != IClassNameStrategy.STRATEGY.DEFAULT) && (eventualSharedNameAndWhereto.strategyClassName == IClassNameStrategy.STRATEGY.DEFAULT) ) {
            eventualSharedNameAndWhereto.strategyClassName = sharedNameAndWhereto.strategyClassName
        }
        if ( (sharedNameAndWhereto.strategyTableName != ITableNameStrategy.STRATEGY.DEFAULT) && (eventualSharedNameAndWhereto.strategyTableName == ITableNameStrategy.STRATEGY.DEFAULT) ) {
            eventualSharedNameAndWhereto.strategyTableName = sharedNameAndWhereto.strategyTableName
        }
    }
}
