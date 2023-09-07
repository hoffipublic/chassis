package com.hoffi.chassis.shared.parsedata.nameandwhereto

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.helpers.joinPackage
import com.hoffi.chassis.shared.helpers.pathSepRE
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.strategies.ClassNameStrategy
import com.hoffi.chassis.shared.strategies.IClassNameStrategy
import com.hoffi.chassis.shared.strategies.ITableNameStrategy
import com.hoffi.chassis.shared.strategies.TableNameStrategy
import com.hoffi.chassis.shared.whens.WhensDslRef
import okio.Path

/** all nameAndWhereto { } information from viewpoint of one(!) Element (e.g. model or api) */
class SharedGatheredNameAndWheretos(val dslRef: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRef, $dslRunIdentifier)"
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
    val allFromGroup: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()

    val allFromDslRunConfigureForSubelement: MutableMap<MODELREFENUM, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromGroupForSubelement: MutableMap<MODELREFENUM, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromElementForSubelement: MutableMap<MODELREFENUM, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()

    val allFromElement: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
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
    fun createForSubelement(subElementRef: DslRef.ISubElementLevel, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromSubelements.getOrPut(subElementRef) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("${subElementRef} has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
    fun createFromElementForSubelement(modelelement: MODELREFENUM, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromElementForSubelement.getOrPut(modelelement) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("$modelelement has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
    fun createFromGroupForSubelement(modelelement: MODELREFENUM, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromGroupForSubelement.getOrPut(modelelement) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("$modelelement has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
        }
        subelementsNameAndWheretosMap[sharedNameAndWhereto.simpleName] = sharedNameAndWhereto
    }
    fun createFromDslRunForSubelement(modelelement: MODELREFENUM, sharedNameAndWhereto: SharedNameAndWhereto) {
        val subelementsNameAndWheretosMap = allFromDslRunConfigureForSubelement.getOrPut(modelelement) { mutableMapOf() }
        if (subelementsNameAndWheretosMap.containsKey(sharedNameAndWhereto.simpleName)) {
            throw DslException("$modelelement has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
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
    override fun toString() = "SharedNameAndWhereto(clPre=${classPrefixAddendum},clPost=${classPostfixAddendum},pack=${packageNameAddendum},path=+${pathAddendum} for $dslRef)"
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
    enum class STRATEGY { SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS }

    fun resolve(strategy: STRATEGY, dslRef: DslRef.ISubElementLevel, gatheredNameAndWheretos: SharedGatheredNameAndWheretos): ModelClassName {
        return when (strategy) {
            STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS -> specialWins(dslRef, gatheredNameAndWheretos)
        }
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, gatheredNameAndWheretos: SharedGatheredNameAndWheretos): ModelClassName {
        val modelelement = WhensDslRef.whenModelSubelement(dslRef,
            isDtoRef = { MODELREFENUM.DTO },
            isTableRef = { MODELREFENUM.TABLE }
        )
        val eventualModelClassName = ModelClassName(dslRef, null)
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
        if (sharedNameAndWheretoSE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoSE, eventualSharedNameAndWhereto)
        }
        val sharedNameAndWheretoESE = g.allFromElementForSubelement[modelelement]?.get(C.DEFAULT) ?: g.allFromElementForSubelement[modelelement]?.values?.firstOrNull()
        if (sharedNameAndWheretoESE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoESE, eventualSharedNameAndWhereto)
        }
        val sharedNameAndWheretoE = g.allFromElement[C.DEFAULT] ?: g.allFromElement.values.firstOrNull()
        if (sharedNameAndWheretoE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoE, eventualSharedNameAndWhereto)
        }
        val sharedNameAndWheretoGSE = g.allFromGroupForSubelement[modelelement]?.get(C.DEFAULT) ?: g.allFromGroupForSubelement[modelelement]?.values?.firstOrNull()
        if (sharedNameAndWheretoGSE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoGSE, eventualSharedNameAndWhereto)
        }
        val sharedNameAndWheretoG = g.allFromGroup[C.DEFAULT] ?: g.allFromGroup.values.firstOrNull()
        if (sharedNameAndWheretoG != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoG, eventualSharedNameAndWhereto)
        }
        val sharedNameAndWheretoRSE = g.allFromDslRunConfigureForSubelement[modelelement]?.get(C.DEFAULT) ?: g.allFromDslRunConfigureForSubelement[modelelement]?.values?.firstOrNull()
        if (sharedNameAndWheretoRSE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoRSE, eventualSharedNameAndWhereto)
        }
        val sharedNameAndWheretoR = g.allFromDslRunConfigure[C.DEFAULT] ?: g.allFromDslRunConfigure.values.firstOrNull()
        if (sharedNameAndWheretoR != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoR, eventualSharedNameAndWhereto)
        }
        eventualModelClassName.classNameStrategy = ClassNameStrategy.get(eventualSharedNameAndWhereto.strategyClassName)
        eventualModelClassName.tableNameStrategy = TableNameStrategy.get(eventualSharedNameAndWhereto.strategyTableName)
        eventualModelClassName.basePath = eventualSharedNameAndWhereto.baseDirAbsolute / eventualSharedNameAndWhereto.baseDirAddendum
        eventualModelClassName.path = eventualSharedNameAndWhereto.pathAbsolute / eventualSharedNameAndWhereto.pathAddendum
        eventualModelClassName.basePackage = joinPackage(eventualSharedNameAndWhereto.basePackageAbsolute, eventualSharedNameAndWhereto.basePackageAddendum)
        eventualModelClassName.packageName = joinPackage(eventualSharedNameAndWhereto.packageNameAbsolute, eventualSharedNameAndWhereto.packageNameAddendum)
        eventualModelClassName.classPrefix = eventualSharedNameAndWhereto.classPrefixAbsolute + eventualSharedNameAndWhereto.classPrefixAddendum
        eventualModelClassName.classPostfix = eventualSharedNameAndWhereto.classPostfixAbsolute + eventualSharedNameAndWhereto.classPostfixAddendum

        return eventualModelClassName
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
            (sharedNameAndWhereto.baseDirAbsolute != NameAndWheretoDefaults.basePath) && (eventualSharedNameAndWhereto.baseDirAbsolute == NameAndWheretoDefaults.basePath) ) {
            eventualSharedNameAndWhereto.baseDirAbsolute = sharedNameAndWhereto.baseDirAbsolute
            absolute = true
        }
        //if ( (!eventualSharedNameAndWhereto.baseDirAbsoluteBool) &&
        //    (sharedNameAndWhereto.baseDirAddendum != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.baseDirAddendum == NameAndWheretoDefaults.path) ) {
        //    eventualSharedNameAndWhereto.baseDirAddendum /= sharedNameAndWhereto.baseDirAddendum
        //}
        if ( (sharedNameAndWhereto.baseDirAddendum != NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.baseDirAddendum /= sharedNameAndWhereto.baseDirAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.baseDirAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.pathAbsoluteBool)  &&
            (sharedNameAndWhereto.pathAbsolute != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.pathAbsolute == NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.pathAbsolute = sharedNameAndWhereto.pathAbsolute
            absolute = true
        }
        //if ( (!eventualSharedNameAndWhereto.pathAbsoluteBool)  &&
        //    (sharedNameAndWhereto.pathAddendum != NameAndWheretoDefaults.path) && (eventualSharedNameAndWhereto.pathAddendum == NameAndWheretoDefaults.path) ) {
        //    eventualSharedNameAndWhereto.pathAddendum /= sharedNameAndWhereto.pathAddendum
        //}
        if ( (sharedNameAndWhereto.pathAddendum != NameAndWheretoDefaults.path) ) {
            eventualSharedNameAndWhereto.pathAddendum /= sharedNameAndWhereto.pathAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.pathAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.classPrefixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPrefixAbsolute != NameAndWheretoDefaults.classPrefix) && (eventualSharedNameAndWhereto.classPrefixAbsolute == NameAndWheretoDefaults.classPrefix) ) {
            eventualSharedNameAndWhereto.classPrefixAbsolute = sharedNameAndWhereto.classPrefixAbsolute
            absolute = true
        }
        //if ( (!eventualSharedNameAndWhereto.classPrefixAbsoluteBool)  &&
        //    (sharedNameAndWhereto.classPrefixAddendum != NameAndWheretoDefaults.classPrefix) && (eventualSharedNameAndWhereto.classPrefixAddendum == NameAndWheretoDefaults.classPrefix) ) {
        //    eventualSharedNameAndWhereto.classPrefixAddendum += sharedNameAndWhereto.classPrefixAddendum
        //}
        if ( (sharedNameAndWhereto.classPrefixAddendum != NameAndWheretoDefaults.classPrefix) ) {
            eventualSharedNameAndWhereto.classPrefixAddendum += sharedNameAndWhereto.classPrefixAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.classPrefixAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.classPostfixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPostfixAbsolute != NameAndWheretoDefaults.classPostfix) && (eventualSharedNameAndWhereto.classPostfixAbsolute == NameAndWheretoDefaults.classPostfix) ) {
            eventualSharedNameAndWhereto.classPostfixAbsolute = sharedNameAndWhereto.classPostfixAbsolute
            absolute = true
        }
        //if ( (!eventualSharedNameAndWhereto.classPostfixAbsoluteBool)  &&
        //    (sharedNameAndWhereto.classPostfixAddendum != NameAndWheretoDefaults.classPostfix) && (eventualSharedNameAndWhereto.classPostfixAddendum == NameAndWheretoDefaults.classPostfix) ) {
        //    eventualSharedNameAndWhereto.classPostfixAddendum = eventualSharedNameAndWhereto.classPostfixAddendum + sharedNameAndWhereto.classPostfixAddendum
        //}
        if ( (sharedNameAndWhereto.classPostfixAddendum != NameAndWheretoDefaults.classPostfix) ) {
            eventualSharedNameAndWhereto.classPostfixAddendum = eventualSharedNameAndWhereto.classPostfixAddendum + sharedNameAndWhereto.classPostfixAddendum
        }
        if (absolute) eventualSharedNameAndWhereto.classPostfixAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.basePackageAbsoluteBool)  &&
            (sharedNameAndWhereto.basePackageAbsolute != NameAndWheretoDefaults.basePackage) && (eventualSharedNameAndWhereto.basePackageAbsolute == NameAndWheretoDefaults.basePackage) ) {
            eventualSharedNameAndWhereto.basePackageAbsolute = sharedNameAndWhereto.basePackageAbsolute
            absolute = true
        }
        //if ( (!eventualSharedNameAndWhereto.basePackageAbsoluteBool)  &&
        //    (sharedNameAndWhereto.basePackageAddendum != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.basePackageAddendum == NameAndWheretoDefaults.packageName) ) {
        //    eventualSharedNameAndWhereto.basePackageAddendum = eventualSharedNameAndWhereto.basePackageAddendum.ifNotBlank { "${eventualSharedNameAndWhereto.basePackageAddendum}." } + sharedNameAndWhereto.basePackageAddendum.replace(pathSepRE, ".")
        //}
        if ( (sharedNameAndWhereto.basePackageAddendum != NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.basePackageAddendum = joinPackage(eventualSharedNameAndWhereto.basePackageAddendum, sharedNameAndWhereto.basePackageAddendum.replace(pathSepRE, "."))
        }
        if (absolute) eventualSharedNameAndWhereto.basePackageAbsoluteBool = true
        absolute = false
        if ( (!eventualSharedNameAndWhereto.packageNameAbsoluteBool)  &&
            (sharedNameAndWhereto.packageNameAbsolute != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.packageNameAbsolute == NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.packageNameAbsolute = sharedNameAndWhereto.packageNameAbsolute
            absolute = true
        }
        //if ( (!eventualSharedNameAndWhereto.packageNameAbsoluteBool)  &&
        //    (sharedNameAndWhereto.packageNameAddendum != NameAndWheretoDefaults.packageName) && (eventualSharedNameAndWhereto.packageNameAddendum == NameAndWheretoDefaults.packageName) ) {
        //    eventualSharedNameAndWhereto.packageNameAddendum = eventualSharedNameAndWhereto.packageNameAddendum.ifNotBlank { "${eventualSharedNameAndWhereto.packageNameAddendum}." } + sharedNameAndWhereto.packageNameAddendum.replace(pathSepRE, ".")
        //}
        if ( (sharedNameAndWhereto.packageNameAddendum != NameAndWheretoDefaults.packageName) ) {
            eventualSharedNameAndWhereto.packageNameAddendum = joinPackage(eventualSharedNameAndWhereto.packageNameAddendum, sharedNameAndWhereto.packageNameAddendum.replace(pathSepRE, "."))
        }
    }
}
