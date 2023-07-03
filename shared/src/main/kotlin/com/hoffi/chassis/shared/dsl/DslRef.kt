package com.hoffi.chassis.shared.dsl

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.chassismodel.dsl.DslRefException
import com.hoffi.chassis.shared.dsl.DslRef.Companion.genericInstance
import com.hoffi.chassis.shared.dsl.DslRefString.REF
import com.hoffi.chassis.shared.whens.WhensDslRef
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

//region class IDslRef and ADslRef and value class DslDiscriminator ...
@JvmInline
value class DslDiscriminator(val dslDiscriminator: String) { companion object { val NULL = DslDiscriminator(C.NULLSTRING) }
    override fun toString() = dslDiscriminator }

fun main() {
    val theRef = REF("disc:debugDisk;modelgroup:CommonModel;model:Intfc;dto:debugDto")
    val x: DslRef = theRef.parentRef(-3)
    println("DslRef.${x::class.simpleName} -> $x")
}

interface IDslRef {
    var level: Int
    val simpleName: String
    val parentRef: IDslRef
    var disc: DslDiscriminator
    val refList: MutableList<DslRef.DslRefAtom>
    val dslBlockName: String
        get() {
            if (this == NULL) { throw DslException("trying to get IDslRef.dslBlockName on IDslRef.NULL") }
            val funcname = refList.lastOrNull()?.functionName
            if (funcname == null) {
                throw DslException("refList never should be empty on getting IDslRef.dslBlockName")
            } else {
                if (funcname == C.NULLSTRING) throw DslException("a trailing ref Atom never should have C.NULLSTRING as funcname")
                return funcname
            }
            //refList.lastOrNull()?.functionName ?: C.NULLSTRING // TODO simplify
        }
    object NULL : IDslRef {
        override var level: Int
            get() = 0
            set(@Suppress("UNUSED_PARAMETER")value) {}
        override val simpleName: String
            get() = C.NULLSTRING
        override val parentRef: IDslRef
            get() = NULL
        override var disc: DslDiscriminator
            get() = DslDiscriminator.NULL
            set(@Suppress("UNUSED_PARAMETER") value) {}
        override val refList: MutableList<DslRef.DslRefAtom>
            get() = mutableListOf(DslRef.DslRefAtom(C.NULLSTRING, C.NULLSTRING))
        override fun equals(other: Any?): Boolean = other?.equals(NULL) ?: false
        override fun hashCode(): Int = refList.hashCode()
    }
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
abstract class ADslRef(
    override var level: Int,
    override val simpleName: String,
    override val parentRef: IDslRef,
    override var disc: DslDiscriminator,
    override val refList: MutableList<DslRef.DslRefAtom> = mutableListOf()
) : IDslRef {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun toString() = "disc:${disc};${DslRef.refListJoin(refList)}"
    fun refListJoin() = DslRef.refListJoin(refList)
    fun createRefList(forLevelOrMinusOne: Int, parentDslRef: IDslRef, funcname: String, simpleName: String) {
        if (forLevelOrMinusOne != -1 && parentDslRef.refList.size > parentDslRef.level && parentDslRef.refList[0].simpleName != C.NULLSTRING) {
            log.warn("parent refList '{}' is longer than its level '{}' while creating ref for '{}'", refListJoin(), parentDslRef.level, this)
        }
        refList.addAll(parentDslRef.refList) //.take(parentDslRef.level))
        refList.add(DslRef.DslRefAtom(funcname, simpleName))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IDslRef) return false
        //if (disc != other.disc) return false
        return refList == other.refList
    }
    override fun hashCode(): Int = refList.hashCode()
}
//endregion AdslRef ...

sealed class DslRef(level: Int, simpleName: String, parentRef: IDslRef) : ADslRef(level, simpleName, parentRef, parentRef.disc) {
    //region class DslRef refs ...
    private val log = LoggerFactory.getLogger(javaClass)

    sealed interface IGroupLevel      : IDslRef, ICrosscuttingNameAndWhereto
    sealed interface IElementLevel    : IDslRef, ICrosscuttingNameAndWhereto
    sealed interface ISubElementLevel : IDslRef
    sealed interface ICrosscutting    : IDslRef  { companion object { val NULL: ICrosscutting = propertiesOf(C.NULLSTRING, IDslRef.NULL)}}

    class modelgroup(simpleName: String, disc: DslDiscriminator)         : DslRef(1, simpleName, IDslRef.NULL)
        , IGroupLevel, ICrosscuttingPropertiesOf {
            companion object { val funcname: String = modelgroup::class.simpleName!! ; val MODELGROUPPROTO = modelgroup("PROTO", DslDiscriminator.NULL) }
            init { this.disc = disc ; refList.add(DslRefAtom(funcname, simpleName)) } }
    class apigroup(  simpleName: String, disc: DslDiscriminator)         : DslRef(1, simpleName, IDslRef.NULL)
        , IGroupLevel {
            companion object { val funcname: String = apigroup::class.simpleName!! ; val APIGROUPPROTO = apigroup("PROTO", DslDiscriminator.NULL) }
            init { this.disc = disc ; refList.add(DslRefAtom(funcname, simpleName)) } }

    interface IModelgroupElement : IElementLevel, ICrosscuttingClassModifiers
    interface IModelOrModelSubelement : ICrosscuttingPropertiesOf
    interface IModel: IModelgroupElement, ICrosscuttingPropertiesOf
    class     model(    simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
            , IModel, IModelgroupElement, IModelOrModelSubelement {
                companion object { val funcname: String = model::class.simpleName!! ; val MODELPROTO = model("PROTO", NULL) }
                init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }
    interface Ifiller : IModelgroupElement
    class     filler(   simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
            , Ifiller {
                companion object { val funcname: String = filler::class.simpleName!! ; val FILLERPROTO = filler("PROTO", NULL) }
                init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }
    interface IallModels: IModelgroupElement
    class     allModels(simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
            , IallModels {
                companion object { val funcname: String = allModels::class.simpleName!! ; val ALLMODELSPROTO = allModels("PROTO", NULL) }
                init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }
    interface IModelSubelement : ISubElementLevel, IModelOrModelSubelement, ICrosscuttingNameAndWhereto, ICrosscuttingPropertiesOf, ICrosscuttingClassModifiers {
    }
    class         dto(  simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                , IModelSubelement {
                    companion object { val funcname: String = dto::class.simpleName!! ; val DTOPROTO = dto("PROTO", NULL) }
                    init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }
    class         table(simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                , IModelSubelement {
                    companion object { val funcname: String = table::class.simpleName!! ; val TABLEPROTO = table("PROTO", NULL) }
                    init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }

    interface IApigroupElement : IElementLevel
    interface Iapi: IApigroupElement
    class     api(    simpleName: String, parentDslRef: IDslRef)         : DslRef(2, simpleName, parentDslRef)
            , Iapi { companion object { val funcname: String = api::class.simpleName!! ; val APIPROTO = api("PROTO", NULL) }
                init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }
    interface IApiSubElement : ISubElementLevel, ICrosscuttingNameAndWhereto
    interface Iapifun: IApiSubElement {
    }
    class         apifun(    simpleName: String, parentDslRef: IDslRef)  : DslRef(3, simpleName, parentDslRef)
                , Iapifun {
                    companion object { val funcname: String = apifun::class.simpleName!! ; val APIFUNPROTO = apifun("PROTO", NULL) }
                    init { this.disc = parentDslRef.disc ; createRefList(level, parentDslRef, funcname, simpleName) } }

    interface ICrosscuttingProp : ICrosscutting
    interface Iprop : ICrosscuttingProp
    class prop(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
        , Iprop
    { companion object { val funcname: String = prop::class.simpleName!! ; val PROPPROTO = prop("PROTO", NULL) }
        init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }
    interface ICrosscuttingProperties : ICrosscutting
    interface Iproperties : ICrosscuttingProperties
    class properties(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
        , Iproperties
    { companion object { val funcname: String = prop::class.simpleName!! ; val PROPERTIESPROTO = properties("PROTO", NULL) }
        init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }


    interface IDslRun : ICrosscuttingNameAndWhereto
    class dslRun(simpleName: String) : DslRef(1, simpleName, IDslRef.NULL)
        , IDslRun
            { companion object { val funcname: String = dslRun::class.simpleName!! ; val DSLRUNPROTO = dslRun("PROTO") }
                init { this.disc = DslDiscriminator(dslRun::class.simpleName!!) ; refList.add(DslRefAtom(funcname, simpleName)) } }

    interface ICrosscuttingNameAndWhereto : ICrosscutting
    interface InameAndWhereto : ICrosscuttingNameAndWhereto
    class nameAndWhereto(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
        , InameAndWhereto
            { companion object { val funcname: String = nameAndWhereto::class.simpleName!! ; val NAMEANDWHERETOPROTO = nameAndWhereto("PROTO", NULL) } init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }

    interface ICrosscuttingPropertiesOf : ICrosscutting
    // unused atm, as DslGatherPropertiesImpl atm only gets its parentRef
    interface  IpropertiesOf : ICrosscuttingPropertiesOf
    class propertiesOf(simpleName: String, parentDslRef: IDslRef) :   DslRef(-1, simpleName, parentDslRef)
        , IpropertiesOf
            { companion object { val funcname: String = propertiesOf::class.simpleName!! ; val PROPERTIESOFPROTO = propertiesOf("PROTO", NULL) } init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }

    interface ICrosscuttingClassModifiers : ICrosscutting

    interface ICrosscuttingClassMods : ICrosscutting
    interface IclassMods : ICrosscuttingClassMods
    class classMods(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
        , IclassMods
            { companion object { val funcname: String = classMods::class.simpleName!! ; val CLASSMODSPROTO = classMods("PROTO", NULL)} init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }

    interface ICrosscuttingExtends : ICrosscutting
    interface Iextends : ICrosscuttingExtends
    class extends(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
        , Iextends
            { companion object { val funcname: String = extends::class.simpleName!! ; val EXTENDSPROTO = extends("PROTO", NULL)} init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }

    interface ICrosscuttingShowcase : ICrosscutting
    interface Ishowcase : ICrosscuttingShowcase
    class showcase(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
        , Ishowcase
            { companion object { val funcname: String = showcase::class.simpleName!! ; val SHOWCASEPROTO = showcase("PROTO", NULL)} init { this.disc = parentDslRef.disc ; createRefList(-1, parentDslRef, funcname, simpleName) } }
    //endregion class DslRef refs

    //region reffing ...
    // TODO refactor to inline reified functions that return the concrete DslRef.xxx DslRef
    fun <T: DslRef> groupRef(): T = parentRef(1)
    fun <T: DslRef> elementRef(): T = parentRef(2)
    fun <T: DslRef> subelementRef(): T = parentRef(3)
    fun <T: DslRef> subsubelementRef(): T = parentRef(4)
    fun <T: DslRef> parentRef(level: Int): T {
        val log = LoggerFactory.getLogger("debugLogger")
        if (level == 0) throw DslRefException("invalid argument 0")
        val absLevel = if (level < 0) this.refList.size + level else level
        if (this.refList.size <= absLevel || absLevel <= 0) throw DslRefException("parentRef($level) deeper/equal this DslRef (${this.refList.size}) $this")
        val refAtom = this.refList[absLevel-1]
        val refClass = DslRef::class.sealedSubclasses.first { it.simpleName == refAtom.functionName }
        val constr = refClass.primaryConstructor
        if (constr?.parameters?.size != 2) throw DslRefException("sealed subclass DslRef.${refAtom.functionName} does not have a 2-arg primary constructor")
        if (constr.parameters[0].type.classifier != String::class || constr.parameters[0].type.isMarkedNullable) throw DslRefException("sealed subclass DslRef.${refAtom.functionName} first constructor arg is not non-nullable String")
        if (constr.parameters[1].type.isMarkedNullable) throw DslRefException("sealed subclass DslRef.${refAtom.functionName} 2nd constructor arg must not be nullable")
        log.debug("DslRef.{}.constr({}, parentRef|DslDisc) in DslRef.xxx()", refAtom.functionName, refAtom.simpleName)
        val theObj: T = when (constr.parameters[1].type.classifier) {
            DslDiscriminator::class -> {
                @Suppress("UNCHECKED_CAST")
                constr.call(refAtom.simpleName, this.disc) as T
            }
            IDslRef::class -> {
                @Suppress("UNCHECKED_CAST")
                constr.call(refAtom.simpleName, REF(this.refList.take(absLevel-1), this.disc)) as T
            }
            else -> throw DslRefException("sealed subclass DslRef.${refAtom.functionName} not a valid constructor")
        }
        log.debug("ok.")
        return theObj
    }
    //endregion reffing

    //region DslRef companion object ...
    companion object {
        val NULL = IDslRef.NULL
        val ATOMSEP = ":"
        val REFSEP  = ";"
        val COUNTSEP = "/"
        fun refListJoin(refList: List<DslRefAtom>) = refList.joinToString(REFSEP)
        fun refAtomsListFull(refString: String, dslDiscriminator: DslDiscriminator = DslDiscriminator(C.DEFAULT)) = refString.split(REFSEP).map { DslRefAtom.from(it) }.toMutableList().also {if (it.isNotEmpty() && it.first().functionName != "disc") it.add(0, DslRefAtom("disc", dslDiscriminator.dslDiscriminator))}
        fun genericInstance(): DslRef = modelgroup(C.NULLSTRING, DslDiscriminator(C.NULLSTRING))
        fun <T : IDslRef> funcname(dslRefClass: KClass<T>): String {
            val companion = dslRefClass.companionObjectInstance ?: return "$dslRefClass has no companion object with val funcname"
            val companionProp = companion::class.memberProperties.first { it.name == "funcname" /* companion object { val funcname = "xxx" } */ }
            //companionProp.isAccessible = true
            return companionProp.getter.call(companion).toString()
        }
//        //region introspection helpers on DslRef ...
//        inline fun <reified T: IDslRef> funcnamesImplementing(dslRefIfcClass: KClass<T>): List<String> {
//            //val funcnamesList = listOf<String>()
//            val classesImplementingInterface = classesImplementingInterface(dslRefIfcClass)
//            val funcnamesList = classesImplementingInterface.map { DslRef.funcname(it.kotlin) }
//            return funcnamesList
//        }
        inline fun <reified T: IDslRef> getDslRefFuncname(dslRefIfcClass: KClass<T>, refAtomsInclDiscriminator: List<DslRefAtom>, level: Int): Pair<DslRefAtom, KClass<out T>> {
            //val funcNameOfRefString = DslRefString.refAtomsList(refString)
            val refAtomOfLevel = refAtomsInclDiscriminator[level]
            var funcClass: KClass<out T>? = null

            // making sure we only test DslRef.funcName's
            // that are (or inherited somewhere up in the hierarchy) on an implementing class of the given interface::class
            // which then should have singleton objects that implement that sealed(!) interface
            // which then are returned by this function
            val classesImplementingInterface = classesImplementingInterface(dslRefIfcClass)
            for (javaClass: Class<out T> in classesImplementingInterface) {
                when (funcname(javaClass.kotlin)) {
                    refAtomOfLevel.functionName -> { funcClass = javaClass.kotlin; break }
                }
            }
            if (funcClass == null) throw Exception("unknown ${level}. part func '$refAtomOfLevel' in DslRef '${refAtomsInclDiscriminator.joinToString(";")}'")
            return Pair(refAtomOfLevel, funcClass)
        }
        val reflections = Reflections(ConfigurationBuilder().forPackage("com.hoffi.chassis.shared.dsl"))
        inline fun <reified T : IDslRef> objectsImplementingInterface(dslRefIfcClass: KClass<T>): List<T> {
            val objList = reflections.getSubTypesOf(dslRefIfcClass.java).mapNotNull {
                it.kotlin.objectInstance
            }
            return objList
        }
        inline fun <reified T : IDslRef> classesImplementingInterface(dslRefIfcClass: KClass<T>): Set<Class<out T>> {
            val objList = reflections.getSubTypesOf(dslRefIfcClass.java)
            return objList
        }
        //endregion introspection helpers
    }
    //endregion companion

    //region DslRefAtom
    data class DslRefAtom(val functionName: String, val simpleName: String = C.DEFAULT) {
        override fun toString() = "$functionName${ if (simpleName == C.DEFAULT) "" else ":$simpleName"}"
        fun debugToString() = "$functionName:$simpleName"
        companion object {
            fun from(refString: String) = refString.split(DslRef.ATOMSEP).let { if(it.size == 2) DslRefAtom(it[0], it[1]) else DslRefAtom(it[0]) }
        }
    }
    //endregion
}

object DslRefString {
    //region DslRefString { ...
    fun groupRef(refString: String, dslDiscriminator: DslDiscriminator) : DslRef.IGroupLevel = groupRef(DslRef.refAtomsListFull(refString), dslDiscriminator)
    private fun groupRef(refAtomsList: List<DslRef.DslRefAtom>, dslDiscriminator: DslDiscriminator) : DslRef.IGroupLevel {
        if(refAtomsList.size < 2) {// first element is DslDiscriminator
            throw DslException("cannot extract groupRef from '${DslRef.refListJoin(refAtomsList)}' as it has less than 2 elements ")
        }
        // sentinel exhaustive when to get a compile error here, when DSL adds/removes on grouplevel
        WhensDslRef.whenGroup(IDslRef.NULL, isApigroup = {}, isModelgroup = {})
        val (groupLevelRefAtom, _) = DslRef.getDslRefFuncname(DslRef.IGroupLevel::class, refAtomsList, 1)
        val dslGroupRef = when (groupLevelRefAtom.functionName) {
            DslRef.modelgroup.funcname -> { DslRef.modelgroup(groupLevelRefAtom.simpleName, dslDiscriminator) }
            DslRef.apigroup.funcname ->   { DslRef.apigroup(groupLevelRefAtom.simpleName, dslDiscriminator) }
            else -> { throw DslException("Forgot to add groupLevel when case here for group '${groupLevelRefAtom.functionName}' ???") }
        }
        return dslGroupRef
    }
    fun modelgroupRef(refString: String, dslDiscriminator: DslDiscriminator) : DslRef.modelgroup {
        val groupRef = groupRef(refString, dslDiscriminator)
        if (groupRef !is DslRef.modelgroup) throw DslException("DslRef '$refString' does not point to a modelgroup")
        return groupRef
    }
    fun elementRef(refString: String, dslDiscriminator: DslDiscriminator) : DslRef.IElementLevel {
        val refAtomsList = DslRef.refAtomsListFull(refString)
        if(refAtomsList.size < 3) {// first element is DslDiscriminator
            throw DslException("cannot extract elementRef from '${DslRef.refListJoin(refAtomsList)}' as it has less than 3 elements ")
        }
        // sentinel exhaustive when to get a compile error here, when DSL adds/removes on grouplevel
        WhensDslRef.whenApigroupElement(IDslRef.NULL, isApiRef = {})
        WhensDslRef.whenModelgroupElement(IDslRef.NULL, isModelRef = {}, isFillerRef = {}, isAllmodelsRef = {})
        val groupRef = groupRef(refAtomsList, dslDiscriminator)
        val (elementLevelRefAtom, _) = DslRef.getDslRefFuncname(DslRef.IElementLevel::class, refAtomsList, 2)
        val elementRef = when (groupRef) {
            //DslRef.IGroupLevel.NULL -> { throw DslException("trying to get elementRef on NULL") }
            is DslRef.apigroup -> {
                when (elementLevelRefAtom.functionName) {
                    DslRef.api.funcname -> { DslRef.api(elementLevelRefAtom.simpleName, groupRef)}
                    else -> { throw DslException("unknown Iapigroup.funcname '${elementLevelRefAtom}'") }
                }
            }
            is DslRef.modelgroup -> {
                when (elementLevelRefAtom.functionName) {
                    DslRef.model.funcname -> { DslRef.model(elementLevelRefAtom.simpleName, groupRef) }
                    DslRef.filler.funcname -> { DslRef.filler(elementLevelRefAtom.simpleName, groupRef) }
                    DslRef.allModels.funcname -> { DslRef.allModels(elementLevelRefAtom.simpleName, groupRef) }
                    else -> { throw DslException("unknown Imodelgroup.funcname '${elementLevelRefAtom}'") }
                }
            }
        }
        return elementRef
    }
    fun modelElementRef(refString: String, dslDiscriminator: DslDiscriminator) : DslRef.model {
        val elementRef = elementRef(refString, dslDiscriminator)
        if (elementRef !is DslRef.model) throw DslException("DslRef '$refString' does not point to a model")
        return elementRef
    }

    fun REF(dslRefAtomList: List<DslRef.DslRefAtom>, dslDiscriminator: DslDiscriminator): DslRef = REF("disc${DslRef.ATOMSEP}$dslDiscriminator${DslRef.REFSEP}${DslRef.refListJoin(dslRefAtomList)}")
    fun REF(dslRef: DslRef): DslRef = REF(dslRef.toString())
    fun REF(dslRefString: String): DslRef {
        // iterate through atoms
        // get by reflection the DslRef to instantiate via funcname
        val refAtomsListFull = DslRef.refAtomsListFull(dslRefString)
        val dslDiscriminator: DslDiscriminator = DslDiscriminator(refAtomsListFull.first().simpleName)
        // sentinel for ALL DslRef's
        when (genericInstance()) {
            is DslRef.dslRun -> {}
            is DslRef.allModels -> {}
            is DslRef.api -> {}
            is DslRef.apifun -> {}
            is DslRef.apigroup -> {}
            is DslRef.classMods -> {}
            is DslRef.dto -> {}
            is DslRef.extends -> {}
            is DslRef.filler -> {}
            is DslRef.model -> {}
            is DslRef.modelgroup -> {}
            is DslRef.nameAndWhereto -> {}
            is DslRef.properties -> {}
            is DslRef.prop -> {}
            is DslRef.propertiesOf -> {}
            is DslRef.showcase -> {}
            is DslRef.table -> {}
        }
        var parentDslRef: DslRef = DslRef.modelgroup("DUMMY", DslDiscriminator(C.NULLSTRING)) // dummy
        for ((i, refAtom) in refAtomsListFull.withIndex()) {
            if (i == 0) continue // DslDiscriminator
            when (refAtom.functionName) {
                DslRef.apigroup.funcname -> { parentDslRef = DslRef.apigroup(refAtom.simpleName, dslDiscriminator)}
                DslRef.modelgroup.funcname -> { parentDslRef = DslRef.modelgroup(refAtom.simpleName, dslDiscriminator)}
                DslRef.dslRun.funcname -> DslRef.dslRun(refAtom.simpleName)
                DslRef.allModels.funcname -> { parentDslRef = DslRef.allModels(refAtom.simpleName, parentDslRef) }
                DslRef.api.funcname -> { parentDslRef = DslRef.api(refAtom.simpleName, parentDslRef)}
                DslRef.apifun.funcname -> { parentDslRef = DslRef.apifun(refAtom.simpleName, parentDslRef)}
                DslRef.classMods.funcname -> { parentDslRef = DslRef.classMods(refAtom.simpleName, parentDslRef)}
                DslRef.dto.funcname -> { parentDslRef = DslRef.dto(refAtom.simpleName, parentDslRef)}
                DslRef.extends.funcname -> { parentDslRef = DslRef.extends(refAtom.simpleName, parentDslRef)}
                DslRef.filler.funcname -> { parentDslRef = DslRef.filler(refAtom.simpleName, parentDslRef)}
                DslRef.model.funcname -> { parentDslRef = DslRef.model(refAtom.simpleName, parentDslRef)}
                DslRef.nameAndWhereto.funcname -> { parentDslRef = DslRef.nameAndWhereto(refAtom.simpleName, parentDslRef)}
                DslRef.prop.funcname -> { parentDslRef = DslRef.prop(refAtom.simpleName, parentDslRef)}
                DslRef.propertiesOf.funcname -> { parentDslRef = DslRef.propertiesOf(refAtom.simpleName, parentDslRef)}
                DslRef.showcase.funcname -> { parentDslRef = DslRef.showcase(refAtom.simpleName, parentDslRef)}
                DslRef.table.funcname -> { parentDslRef = DslRef.table(refAtom.simpleName, parentDslRef)}
            }
        }
        return parentDslRef
    }
    fun MODELREF(dslRefString: String): DslRef.IModelOrModelSubelement {
        val dslRef = REF(dslRefString)
        if ( dslRef !is DslRef.IModelOrModelSubelement) {
            throw DslException("ref: '$dslRef' is not a model or modelSubelement")
        } else {
            return dslRef
        }
    }
    //endregion DslRefString
}

//context(IDslClass, DslDiscriminatorWrapper)
//fun String.ref() = DslRefString.fromSelf(selfDslRef, this)

