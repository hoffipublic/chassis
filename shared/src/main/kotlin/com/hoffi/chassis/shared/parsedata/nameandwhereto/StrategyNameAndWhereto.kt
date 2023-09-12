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
class CollectedNameAndWheretos(val dslRefOfBasemodelWithoutAllFromSubelements: DslRef.IElementLevel, val dslRunIdentifier: String) {
    override fun toString() = "${this::class.simpleName}($dslRefOfBasemodelWithoutAllFromSubelements, $dslRunIdentifier)"
    enum class THINGSWITHNAMEANDWHERETOS { DslRunConfigure, Modelgroup, model }
    var fromDslRunConfigure: SharedNameAndWhereto
        get() = allFromDslRunConfigure[C.DEFAULT] ?: throw DslException("no '${C.DEFAULT}' nameAndWhereto in DslRun.configure { }")
        private set(value) { allFromDslRunConfigure[C.DEFAULT] = value }
    var fromGroup: SharedNameAndWhereto
        get() = allFromGroup[C.DEFAULT] ?: throw DslException("no '${C.DEFAULT}' nameAndWhereto in group '$dslRefOfBasemodelWithoutAllFromSubelements'")
        private set(value) { allFromGroup[C.DEFAULT] = value }
    var fromElement: SharedNameAndWhereto
        get() = allFromElement[C.DEFAULT] ?: throw DslException("no '${C.DEFAULT}' nameAndWhereto in element '$dslRefOfBasemodelWithoutAllFromSubelements'")
        private set(value) { allFromElement[C.DEFAULT] = value }

    val allFromDslRunConfigure: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val allFromGroup: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()

    val allFromDslRunConfigureForSubelement: MutableMap<MODELREFENUM, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromGroupForSubelement: MutableMap<MODELREFENUM, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()
    val allFromElementForSubelement: MutableMap<MODELREFENUM, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()

    val allFromElement: MutableMap<String, SharedNameAndWhereto> = mutableMapOf()
    val allFromSubelements: MutableMap<DslRef.ISubElementLevel, MutableMap<String, SharedNameAndWhereto>> = mutableMapOf()

    companion object {
        fun FAKE(someHelpingIdentifierThatWillBeTakenAsDslRunIdentifier: String) = CollectedNameAndWheretos(DslRef.model("<Fake>", DslRef.NULL), someHelpingIdentifierThatWillBeTakenAsDslRunIdentifier)
    }

    fun copyForNewSubelement() = CollectedNameAndWheretos(dslRefOfBasemodelWithoutAllFromSubelements, dslRunIdentifier).also {
        it.allFromDslRunConfigure.putAll(allFromDslRunConfigure.map { el -> el.key to el.value.copy() })
        it.allFromGroup.putAll(allFromGroup.map { el -> el.key to el.value.copy() })

        it.allFromDslRunConfigureForSubelement.putAll(allFromDslRunConfigureForSubelement.map { outer -> outer.key to outer.value.map { inner -> inner.key to inner.value.copy() }.toMap().toMutableMap() })
        it.allFromGroupForSubelement.putAll(allFromGroupForSubelement.map { outer -> outer.key to outer.value.map { inner -> inner.key to inner.value.copy() }.toMap().toMutableMap() })
        it.allFromElementForSubelement.putAll(allFromElementForSubelement.map { outer -> outer.key to outer.value.map { inner -> inner.key to inner.value.copy() }.toMap().toMutableMap() })

        it.allFromElement.putAll(allFromElement.map { el -> el.key to el.value.copy() })
        it.allFromSubelements.putAll(allFromSubelements.map { outer -> outer.key to outer.value.map { el -> el.key to el.value.copy() }.toMap().toMutableMap() }) // should be empty here
    }

    fun createFor(thing: THINGSWITHNAMEANDWHERETOS, sharedNameAndWhereto: SharedNameAndWhereto) {
        val existingSharedNameAndWhereto = when (thing) {
            THINGSWITHNAMEANDWHERETOS.DslRunConfigure -> allFromDslRunConfigure[sharedNameAndWhereto.simpleName]
            THINGSWITHNAMEANDWHERETOS.Modelgroup -> allFromGroup[sharedNameAndWhereto.simpleName]
            THINGSWITHNAMEANDWHERETOS.model -> allFromElement[sharedNameAndWhereto.simpleName]
        }
        if (existingSharedNameAndWhereto != null) {
            throw DslException("$thing of $dslRefOfBasemodelWithoutAllFromSubelements has more than one nameAndWhereto with simpleName: '${sharedNameAndWhereto.simpleName}'")
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
    override var packageNameAbsolute : String, override var packageNameAddendum : String
) : ISharedNameAndWheretoProps {
    override fun toString() = "SharedNameAndWhereto(clPre=${classPrefixAddendum},clPost=${classPostfixAddendum},pack=${packageNameAddendum},path=+${pathAddendum} for $dslRef)"
    open fun copy() = SharedNameAndWhereto(
        simpleName,
        dslRef,
        strategyClassName, strategyTableName,
        baseDirAbsolute, baseDirAddendum,
        pathAbsolute, pathAddendum,
        classPrefixAbsolute, classPrefixAddendum,
        classPostfixAbsolute, classPostfixAddendum,
        basePackageAbsolute, basePackageAddendum,
        packageNameAbsolute, packageNameAddendum
    )
}
class EventualNameAndWhereto(
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
    override fun copy() = super.copy()
}

object StrategyNameAndWhereto {
    enum class STRATEGY { SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS }

    fun resolve(strategy: STRATEGY, dslRef: DslRef.ISubElementLevel, gatheredNameAndWheretos: CollectedNameAndWheretos): ModelClassName {
        return when (strategy) {
            STRATEGY.SPECIAL_WINS_ON_ABSOLUTE_CONCAT_ADDENDUMS -> specialWins(dslRef, gatheredNameAndWheretos)
        }
    }

    private fun specialWins(dslRef: DslRef.ISubElementLevel, gatheredNameAndWheretos: CollectedNameAndWheretos): ModelClassName {
        val modelelement = WhensDslRef.whenModelSubelement(dslRef,
            isDtoRef = { MODELREFENUM.DTO },
            isDcoRef = { MODELREFENUM.DCO },
            isTableRef = { MODELREFENUM.TABLE }
        )
        val eventualModelClassName = ModelClassName(dslRef, null)
        val g = gatheredNameAndWheretos
        val eventualNameAndWhereto = EventualNameAndWhereto(
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
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoSE, eventualNameAndWhereto)
        }
        val sharedNameAndWheretoESE = g.allFromElementForSubelement[modelelement]?.get(C.DEFAULT) ?: g.allFromElementForSubelement[modelelement]?.values?.firstOrNull()
        if (sharedNameAndWheretoESE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoESE, eventualNameAndWhereto)
        }
        val sharedNameAndWheretoE = g.allFromElement[C.DEFAULT] ?: g.allFromElement.values.firstOrNull()
        if (sharedNameAndWheretoE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoE, eventualNameAndWhereto)
        }
        val sharedNameAndWheretoGSE = g.allFromGroupForSubelement[modelelement]?.get(C.DEFAULT) ?: g.allFromGroupForSubelement[modelelement]?.values?.firstOrNull()
        if (sharedNameAndWheretoGSE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoGSE, eventualNameAndWhereto)
        }
        val sharedNameAndWheretoG = g.allFromGroup[C.DEFAULT] ?: g.allFromGroup.values.firstOrNull()
        if (sharedNameAndWheretoG != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoG, eventualNameAndWhereto)
        }
        val sharedNameAndWheretoRSE = g.allFromDslRunConfigureForSubelement[modelelement]?.get(C.DEFAULT) ?: g.allFromDslRunConfigureForSubelement[modelelement]?.values?.firstOrNull()
        if (sharedNameAndWheretoRSE != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoRSE, eventualNameAndWhereto)
        }
        val sharedNameAndWheretoR = g.allFromDslRunConfigure[C.DEFAULT] ?: g.allFromDslRunConfigure.values.firstOrNull()
        if (sharedNameAndWheretoR != null) {
            takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWheretoR, eventualNameAndWhereto)
        }
        eventualModelClassName.classNameStrategy = ClassNameStrategy.get(eventualNameAndWhereto.strategyClassName)
        eventualModelClassName.tableNameStrategy = TableNameStrategy.get(eventualNameAndWhereto.strategyTableName)
        eventualModelClassName.basePath = eventualNameAndWhereto.baseDirAbsolute / eventualNameAndWhereto.baseDirAddendum
        eventualModelClassName.path = eventualNameAndWhereto.pathAbsolute / eventualNameAndWhereto.pathAddendum
        eventualModelClassName.basePackage = joinPackage(eventualNameAndWhereto.basePackageAbsolute, eventualNameAndWhereto.basePackageAddendum)
        eventualModelClassName.packageName = joinPackage(eventualNameAndWhereto.packageNameAbsolute, eventualNameAndWhereto.packageNameAddendum)
        eventualModelClassName.classPrefix = eventualNameAndWhereto.classPrefixAbsolute + eventualNameAndWhereto.classPrefixAddendum
        eventualModelClassName.classPostfix = eventualNameAndWhereto.classPostfixAbsolute + eventualNameAndWhereto.classPostfixAddendum

        return eventualModelClassName
    }

    private fun takeNonDefaultsIfEventualStillIsDefault(sharedNameAndWhereto: SharedNameAndWhereto, eventualNameAndWhereto: EventualNameAndWhereto) {
        if ( (sharedNameAndWhereto.strategyClassName != IClassNameStrategy.STRATEGY.DEFAULT) && (eventualNameAndWhereto.strategyClassName == IClassNameStrategy.STRATEGY.DEFAULT) ) {
            eventualNameAndWhereto.strategyClassName = sharedNameAndWhereto.strategyClassName
        }
        if ( (sharedNameAndWhereto.strategyTableName != ITableNameStrategy.STRATEGY.DEFAULT) && (eventualNameAndWhereto.strategyTableName == ITableNameStrategy.STRATEGY.DEFAULT) ) {
            eventualNameAndWhereto.strategyTableName = sharedNameAndWhereto.strategyTableName
        }
        var absolute = false
        if ( (!eventualNameAndWhereto.baseDirAbsoluteBool) &&
            (sharedNameAndWhereto.baseDirAbsolute != NameAndWheretoDefaults.basePath) && (eventualNameAndWhereto.baseDirAbsolute == NameAndWheretoDefaults.basePath) ) {
            eventualNameAndWhereto.baseDirAbsolute = sharedNameAndWhereto.baseDirAbsolute
            absolute = true
        }
        //if ( (!eventualNameAndWhereto.baseDirAbsoluteBool) &&
        //    (sharedNameAndWhereto.baseDirAddendum != NameAndWheretoDefaults.path) && (eventualNameAndWhereto.baseDirAddendum == NameAndWheretoDefaults.path) ) {
        //    eventualNameAndWhereto.baseDirAddendum /= sharedNameAndWhereto.baseDirAddendum
        //}
        if ( (sharedNameAndWhereto.baseDirAddendum != NameAndWheretoDefaults.path) ) {
            eventualNameAndWhereto.baseDirAddendum /= sharedNameAndWhereto.baseDirAddendum
        }
        if (absolute) eventualNameAndWhereto.baseDirAbsoluteBool = true
        absolute = false
        if ( (!eventualNameAndWhereto.pathAbsoluteBool)  &&
            (sharedNameAndWhereto.pathAbsolute != NameAndWheretoDefaults.path) && (eventualNameAndWhereto.pathAbsolute == NameAndWheretoDefaults.path) ) {
            eventualNameAndWhereto.pathAbsolute = sharedNameAndWhereto.pathAbsolute
            absolute = true
        }
        //if ( (!eventualNameAndWhereto.pathAbsoluteBool)  &&
        //    (sharedNameAndWhereto.pathAddendum != NameAndWheretoDefaults.path) && (eventualNameAndWhereto.pathAddendum == NameAndWheretoDefaults.path) ) {
        //    eventualNameAndWhereto.pathAddendum /= sharedNameAndWhereto.pathAddendum
        //}
        if ( (sharedNameAndWhereto.pathAddendum != NameAndWheretoDefaults.path) ) {
            eventualNameAndWhereto.pathAddendum /= sharedNameAndWhereto.pathAddendum
        }
        if (absolute) eventualNameAndWhereto.pathAbsoluteBool = true
        absolute = false
        if ( (!eventualNameAndWhereto.classPrefixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPrefixAbsolute != NameAndWheretoDefaults.classPrefix) && (eventualNameAndWhereto.classPrefixAbsolute == NameAndWheretoDefaults.classPrefix) ) {
            eventualNameAndWhereto.classPrefixAbsolute = sharedNameAndWhereto.classPrefixAbsolute
            absolute = true
        }
        //if ( (!eventualNameAndWhereto.classPrefixAbsoluteBool)  &&
        //    (sharedNameAndWhereto.classPrefixAddendum != NameAndWheretoDefaults.classPrefix) && (eventualNameAndWhereto.classPrefixAddendum == NameAndWheretoDefaults.classPrefix) ) {
        //    eventualNameAndWhereto.classPrefixAddendum += sharedNameAndWhereto.classPrefixAddendum
        //}
        if ( (sharedNameAndWhereto.classPrefixAddendum != NameAndWheretoDefaults.classPrefix) ) {
            eventualNameAndWhereto.classPrefixAddendum += sharedNameAndWhereto.classPrefixAddendum
        }
        if (absolute) eventualNameAndWhereto.classPrefixAbsoluteBool = true
        absolute = false
        if ( (!eventualNameAndWhereto.classPostfixAbsoluteBool)  &&
            (sharedNameAndWhereto.classPostfixAbsolute != NameAndWheretoDefaults.classPostfix) && (eventualNameAndWhereto.classPostfixAbsolute == NameAndWheretoDefaults.classPostfix) ) {
            eventualNameAndWhereto.classPostfixAbsolute = sharedNameAndWhereto.classPostfixAbsolute
            absolute = true
        }
        //if ( (!eventualNameAndWhereto.classPostfixAbsoluteBool)  &&
        //    (sharedNameAndWhereto.classPostfixAddendum != NameAndWheretoDefaults.classPostfix) && (eventualNameAndWhereto.classPostfixAddendum == NameAndWheretoDefaults.classPostfix) ) {
        //    eventualNameAndWhereto.classPostfixAddendum = eventualNameAndWhereto.classPostfixAddendum + sharedNameAndWhereto.classPostfixAddendum
        //}
        if ( (sharedNameAndWhereto.classPostfixAddendum != NameAndWheretoDefaults.classPostfix) ) {
            eventualNameAndWhereto.classPostfixAddendum = eventualNameAndWhereto.classPostfixAddendum + sharedNameAndWhereto.classPostfixAddendum
        }
        if (absolute) eventualNameAndWhereto.classPostfixAbsoluteBool = true
        absolute = false
        if ( (!eventualNameAndWhereto.basePackageAbsoluteBool)  &&
            (sharedNameAndWhereto.basePackageAbsolute != NameAndWheretoDefaults.basePackage) && (eventualNameAndWhereto.basePackageAbsolute == NameAndWheretoDefaults.basePackage) ) {
            eventualNameAndWhereto.basePackageAbsolute = sharedNameAndWhereto.basePackageAbsolute
            absolute = true
        }
        //if ( (!eventualNameAndWhereto.basePackageAbsoluteBool)  &&
        //    (sharedNameAndWhereto.basePackageAddendum != NameAndWheretoDefaults.packageName) && (eventualNameAndWhereto.basePackageAddendum == NameAndWheretoDefaults.packageName) ) {
        //    eventualNameAndWhereto.basePackageAddendum = eventualNameAndWhereto.basePackageAddendum.ifNotBlank { "${eventualNameAndWhereto.basePackageAddendum}." } + sharedNameAndWhereto.basePackageAddendum.replace(pathSepRE, ".")
        //}
        if ( (sharedNameAndWhereto.basePackageAddendum != NameAndWheretoDefaults.packageName) ) {
            eventualNameAndWhereto.basePackageAddendum = joinPackage(eventualNameAndWhereto.basePackageAddendum, sharedNameAndWhereto.basePackageAddendum.replace(pathSepRE, "."))
        }
        if (absolute) eventualNameAndWhereto.basePackageAbsoluteBool = true
        absolute = false
        if ( (!eventualNameAndWhereto.packageNameAbsoluteBool)  &&
            (sharedNameAndWhereto.packageNameAbsolute != NameAndWheretoDefaults.packageName) && (eventualNameAndWhereto.packageNameAbsolute == NameAndWheretoDefaults.packageName) ) {
            eventualNameAndWhereto.packageNameAbsolute = sharedNameAndWhereto.packageNameAbsolute
            absolute = true
        }
        //if ( (!eventualNameAndWhereto.packageNameAbsoluteBool)  &&
        //    (sharedNameAndWhereto.packageNameAddendum != NameAndWheretoDefaults.packageName) && (eventualNameAndWhereto.packageNameAddendum == NameAndWheretoDefaults.packageName) ) {
        //    eventualNameAndWhereto.packageNameAddendum = eventualNameAndWhereto.packageNameAddendum.ifNotBlank { "${eventualNameAndWhereto.packageNameAddendum}." } + sharedNameAndWhereto.packageNameAddendum.replace(pathSepRE, ".")
        //}
        if ( (sharedNameAndWhereto.packageNameAddendum != NameAndWheretoDefaults.packageName) ) {
            eventualNameAndWhereto.packageNameAddendum = joinPackage(eventualNameAndWhereto.packageNameAddendum, sharedNameAndWhereto.packageNameAddendum.replace(pathSepRE, "."))
        }
    }
}
