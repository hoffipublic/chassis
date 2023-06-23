package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.ifNotBlank
import com.hoffi.chassis.shared.helpers.pathSepRE
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
import okio.Path

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
interface IDslApiSharedNameAndWheretoProps {
    var strategyClassName: IClassNameStrategy.STRATEGY
    var strategyTableName: ITableNameStrategy.STRATEGY
}
interface ISharedNameAndWheretoProps : IDslApiSharedNameAndWheretoProps {
    var baseDirAbsolute : Path
    var baseDirAddendum : Path
    var pathAbsolute : Path
    var pathAddendum : Path
    var classPrefixAbsolute : String
    var classPrefixAddendum : String
    var classPostfixAbsolute : String
    var classPostfixAddendum : String
    var basePackageAbsolute : String
    var basePackageAddendum : String
    var packageNameAbsolute : String
    var packageNameAddendum : String
}

open class SharedNameAndWhereto(
    val simpleName: String,
    val dslRef: IDslRef,
    override var strategyClassName: IClassNameStrategy.STRATEGY, override var strategyTableName: ITableNameStrategy.STRATEGY,
    override var baseDirAbsolute : Path, override var baseDirAddendum : Path,
    override var pathAbsolute : Path, override var pathAddendum : Path,
    override var classPrefixAbsolute : String, override var classPrefixAddendum : String,
    override var classPostfixAbsolute : String, override var classPostfixAddendum : String,
    override var basePackageAbsolute : String, override var basePackageAddendum : String,
    override var packageNameAbsolute : String, override var packageNameAddendum : String,
) : ISharedNameAndWheretoProps {
    override fun toString() = "SharedNameAndWhereto(dslRef=$dslRef)"
}
class EventualSharedNameAndWhereto(
    simpleName: String,
    dslRef: IDslRef,
    strategyClassName: IClassNameStrategy.STRATEGY, strategyTableName: ITableNameStrategy.STRATEGY,
    baseDirAbsolute: Path, baseDirAddendum: Path,
    pathAbsolute: Path, pathAddendum: Path,
    classPrefixAbsolute: String, classPrefixAddendum: String,
    classPostfixAbsolute: String, classPostfixAddendum: String,
    basePackageAbsolute: String, basePackageAddendum: String,
    packageNameAbsolute: String, packageNameAddendum: String
) : SharedNameAndWhereto(
    simpleName,
    dslRef,
    strategyClassName, strategyTableName,
    baseDirAbsolute, baseDirAddendum,
    pathAbsolute, pathAddendum,
    classPrefixAbsolute, classPrefixAddendum,
    classPostfixAbsolute, classPostfixAddendum,
    basePackageAbsolute, basePackageAddendum,
    packageNameAbsolute, packageNameAddendum,
) {
    var baseDirAbsoluteBool = false
    var pathAbsoluteBool = false
    var classPrefixAbsoluteBool = false
    var classPostfixAbsoluteBool = false
    var basePackageAbsoluteBool = false
    var packageNameAbsoluteBool = false
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
            val eventualSharedNameAndWhereto = EventualSharedNameAndWhereto(
                dslRef.simpleName,
                dslRef,
                IClassNameStrategy.STRATEGY.DEFAULT,
                ITableNameStrategy.STRATEGY.DEFAULT,
                baseDirAbsolute = NameAndWheretoDefaults.basePath,
                baseDirAddendum = NameAndWheretoDefaults.path,
                pathAbsolute = NameAndWheretoDefaults.path,
                pathAddendum = NameAndWheretoDefaults.path,
                classPrefixAbsolute = NameAndWheretoDefaults.classPrefix,
                classPrefixAddendum = NameAndWheretoDefaults.classPrefix,
                classPostfixAbsolute = NameAndWheretoDefaults.classPostfix,
                classPostfixAddendum = NameAndWheretoDefaults.classPostfix,
                basePackageAbsolute = NameAndWheretoDefaults.basePackage,
                basePackageAddendum = NameAndWheretoDefaults.packageName,
                packageNameAbsolute = NameAndWheretoDefaults.packageName,
                packageNameAddendum = NameAndWheretoDefaults.packageName,
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
            basePath = eventualSharedNameAndWhereto.baseDirAbsolute / eventualSharedNameAndWhereto.baseDirAddendum
            path = eventualSharedNameAndWhereto.pathAbsolute / eventualSharedNameAndWhereto.pathAddendum
            basePackage = eventualSharedNameAndWhereto.basePackageAbsolute +
                    if (eventualSharedNameAndWhereto.basePackageAbsolute.isNotBlank()) "." else "" +
                    eventualSharedNameAndWhereto.basePackageAddendum
            packageName = eventualSharedNameAndWhereto.packageNameAbsolute +
                    if (eventualSharedNameAndWhereto.packageNameAbsolute.isNotBlank()) "." else "" +
                            eventualSharedNameAndWhereto.packageNameAddendum
            classPrefix = eventualSharedNameAndWhereto.classPrefixAbsolute + eventualSharedNameAndWhereto.classPrefixAddendum
            classPostfix = eventualSharedNameAndWhereto.classPostfixAbsolute + eventualSharedNameAndWhereto.classPostfixAddendum
        }
        return modelClassName
    }

    private fun takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWhereto: SharedNameAndWhereto, eventualSharedNameAndWhereto: EventualSharedNameAndWhereto) {
        if ( (sharedNameAndWhereto.strategyClassName != IClassNameStrategy.STRATEGY.DEFAULT) && (eventualSharedNameAndWhereto.strategyClassName == IClassNameStrategy.STRATEGY.DEFAULT) ) {
            eventualSharedNameAndWhereto.strategyClassName = sharedNameAndWhereto.strategyClassName
        }
        if ( (sharedNameAndWhereto.strategyTableName != ITableNameStrategy.STRATEGY.DEFAULT) && (eventualSharedNameAndWhereto.strategyTableName == ITableNameStrategy.STRATEGY.DEFAULT) ) {
            eventualSharedNameAndWhereto.strategyTableName = sharedNameAndWhereto.strategyTableName
        }
        var absolute = false
        if ( (!eventualSharedNameAndWhereto.baseDirAbsoluteBool) &&
            (sharedNameAndWhereto.baseDirAbsolute != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.baseDirAbsolute == NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.baseDirAbsolute = sharedNameAndWhereto.baseDirAbsolute
            absolute = true
        }
        if ( (!eventualSharedNameAndWhereto.baseDirAbsoluteBool) &&
            (sharedNameAndWhereto.baseDirAddendum != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.baseDirAddendum == NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.baseDirAddendum /= sharedNameAndWhereto.baseDirAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.baseDirAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.pathAbsoluteBool)  &&
            (sharedNameAndWhereto.pathAbsolute != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.pathAbsolute == NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.pathAbsolute = sharedNameAndWhereto.pathAbsolute
            absolute = true
        }
        if ( (!eventualSharedNameAndWhereto.pathAbsoluteBool)  &&
            (sharedNameAndWhereto.pathAddendum != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.pathAddendum == NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.pathAddendum /= sharedNameAndWhereto.pathAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.pathAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.classPrefixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPrefixAbsolute != NameAndWheretoDefaults.classPrefix) && (eventualSharedNameAndWhereto.classPrefixAbsolute == NameAndWheretoDefaults.classPrefix) ) {
            eventualSharedNameAndWhereto.classPrefixAbsolute = sharedNameAndWhereto.classPrefixAbsolute
            absolute = true
        }
        if ( (!eventualSharedNameAndWhereto.classPrefixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPrefixAddendum != NameAndWheretoDefaults.classPrefix) && (eventualSharedNameAndWhereto.classPrefixAddendum == NameAndWheretoDefaults.classPrefix) ) {
            eventualSharedNameAndWhereto.classPrefixAddendum += sharedNameAndWhereto.classPrefixAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.classPrefixAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.classPostfixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPostfixAbsolute != NameAndWheretoDefaults.classPostfix) && (eventualSharedNameAndWhereto.classPostfixAbsolute == NameAndWheretoDefaults.classPostfix) ) {
            eventualSharedNameAndWhereto.classPostfixAbsolute = sharedNameAndWhereto.classPostfixAbsolute
            absolute = true
        }
        if ( (!eventualSharedNameAndWhereto.classPostfixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPostfixAddendum != NameAndWheretoDefaults.classPostfix) && (eventualSharedNameAndWhereto.classPostfixAddendum == NameAndWheretoDefaults.classPostfix) ) {
            eventualSharedNameAndWhereto.classPostfixAddendum = eventualSharedNameAndWhereto.classPostfixAddendum + sharedNameAndWhereto.classPostfixAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.classPostfixAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.basePackageAbsoluteBool)  &&
            (sharedNameAndWhereto.basePackageAbsolute != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.basePackageAbsolute == NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.basePackageAbsolute = sharedNameAndWhereto.basePackageAbsolute
            absolute = true
        }
        if ( (!eventualSharedNameAndWhereto.basePackageAbsoluteBool)  &&
            (sharedNameAndWhereto.basePackageAddendum != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.basePackageAddendum == NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.basePackageAddendum = eventualSharedNameAndWhereto.basePackageAddendum.ifNotBlank { "${eventualSharedNameAndWhereto.basePackageAddendum}." } + sharedNameAndWhereto.basePackageAddendum.replace(pathSepRE, ".")
        }
        if (absolute) eventualSharedNameAndWhereto.basePackageAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.packageNameAbsoluteBool)  &&
            (sharedNameAndWhereto.packageNameAbsolute != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.packageNameAbsolute == NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.packageNameAbsolute = sharedNameAndWhereto.packageNameAbsolute
            absolute = true
        }
        if ( (!eventualSharedNameAndWhereto.packageNameAbsoluteBool)  &&
            (sharedNameAndWhereto.packageNameAddendum != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.packageNameAddendum == NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.packageNameAddendum = eventualSharedNameAndWhereto.packageNameAddendum.ifNotBlank { "${eventualSharedNameAndWhereto.packageNameAddendum}." } + sharedNameAndWhereto.packageNameAddendum.replace(pathSepRE, ".")
        }
    }
}
