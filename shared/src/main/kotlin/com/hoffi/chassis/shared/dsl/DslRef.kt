package com.hoffi.chassis.shared.dsl

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.chassismodel.dsl.DslRefException
import com.hoffi.chassis.shared.dsl.DslRefString.REF
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
    val theRef = REF("disc:debugDisk|modelgroup:CommonModel|model:Intfc|dto:debugDto")
    val x: DslRef = theRef.parentRef(-3)
    println("DslRef.${x::class.simpleName} -> $x")
}

interface IDslRef {
    var level: Int
    val simpleName: String
    val parentDslRef: IDslRef
    var disc: DslDiscriminator
    val refList: MutableList<DslRef.DslRefAtom>
    object NULL : IDslRef {
        override var level: Int
            get() = 0
            set(@Suppress("UNUSED_PARAMETER")value) {}
        override val simpleName: String
            get() = C.NULLSTRING
        override val parentDslRef: IDslRef
            get() = NULL
        override var disc: DslDiscriminator
            get() = DslDiscriminator.NULL
            set(@Suppress("UNUSED_PARAMETER") value) {}
        override val refList: MutableList<DslRef.DslRefAtom>
            get() = mutableListOf(DslRef.DslRefAtom(C.NULLSTRING, C.NULLSTRING))

        override fun toString(last: Int) = C.NULLSTRING
        override fun equals(other: Any?): Boolean = other === NULL
        override fun hashCode(): Int = refList.hashCode()
    }
    fun toString(last: Int): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
abstract class ADslRef(
    override var level: Int,
    override val simpleName: String,
    override val parentDslRef: IDslRef,
    override var disc: DslDiscriminator,
    override val refList: MutableList<DslRef.DslRefAtom> = mutableListOf()
) : IDslRef {
    override fun toString() = "disc:${disc}${DslRef.REFSEP}${DslRef.refListJoinToString(refList)}"
    protected val log = LoggerFactory.getLogger(javaClass)

    override fun toString(last: Int) = refList.takeLast(last).joinToString(DslRef.ATOMSEP)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IDslRef) return false
        //if (disc != other.disc) return false
        return refList == other.refList
    }
    override fun hashCode(): Int = refList.hashCode()

    protected fun refListCloneAndAdd(levelOrMinusOne: Int, parentDslRef: IDslRef, dslRefName: String, simpleName: String) {
        if (levelOrMinusOne != -1 && parentDslRef.refList.size > parentDslRef.level && parentDslRef.refList[0].simpleName != C.NULLSTRING) {
            log.error("parent refList '{}' is longer than its level '{}' while creating ref for '{}'", refList.joinToString(DslRef.REFSEP), parentDslRef.level, this)
        }
        refList.addAll(parentDslRef.refList) //.take(parentDslRef.level))
        refList.add(DslRef.DslRefAtom(dslRefName, simpleName))
    }

    //region Dsl Reffing ...
    // TODO refactor to inline reified functions that return the concrete DslRef.xxx DslRef
    fun <T: DslRef> groupRef(): T = parentRef(1)
    fun <T: DslRef> elementRef(): T = parentRef(2)
    fun <T: DslRef> subelementRef(): T = parentRef(3)
    fun <T: DslRef> subsubelementRef(): T = parentRef(4)
    fun <T: DslRef> parentRef(level: Int): T {
        if (level == 0) throw DslRefException("invalid argument 0")
        val absLevel = if (level < 0) this.refList.size + level else level // negative level means relative from the end
        if (this.refList.size <= absLevel || absLevel <= 0) throw DslRefException("parentRef($level) deeper/equal this DslRef (${this.refList.size}) $this")
        val refAtom = this.refList[absLevel-1]
        val refClass = DslRef::class.sealedSubclasses.first { it.simpleName == refAtom.dslRefName }
        val constr = refClass.primaryConstructor
        if (constr?.parameters?.size != 2) throw DslRefException("sealed subclass DslRef.${refAtom.dslRefName} does not have a 2-arg primary constructor")
        if (constr.parameters[0].type.classifier != String::class || constr.parameters[0].type.isMarkedNullable) throw DslRefException("sealed subclass DslRef.${refAtom.dslRefName} first constructor arg is not non-nullable String")
        if (constr.parameters[1].type.isMarkedNullable) throw DslRefException("sealed subclass DslRef.${refAtom.dslRefName} 2nd constructor arg must not be nullable")
        val theObj: T = when (constr.parameters[1].type.classifier) {
            DslDiscriminator::class -> {
                @Suppress("UNCHECKED_CAST")
                constr.call(refAtom.simpleName, this.disc) as T
            }
            IDslRef::class -> {
                @Suppress("UNCHECKED_CAST")
                constr.call(refAtom.simpleName, REF(this.refList.take(absLevel-1), this.disc)) as T
            }
            else -> throw DslRefException("sealed subclass DslRef.${refAtom.dslRefName} not a valid constructor")
        }
        log.debug("ok.")
        return theObj
    }
    //endregion reffing
}
//endregion AdslRef ...

sealed class DslRef(level: Int, simpleName: String, parentRef: IDslRef) : ADslRef(level, simpleName, parentRef, parentRef.disc) {
    abstract class ADslRefCompanion { val dslRefName: String = this::class.java.declaringClass.simpleName }
    //region class DslRef refs ...
    sealed interface IGroupLevel      : IDslRef, ICrosscuttingNameAndWhereto
    sealed interface IElementLevel    : IDslRef, ICrosscuttingNameAndWhereto
    sealed interface ISubElementLevel : IDslRef
    sealed interface ICrosscutting    : IDslRef  { companion object { val NULL: ICrosscutting = propertiesOf(C.NULLSTRING, IDslRef.NULL)}}

    class modelgroup(simpleName: String, disc: DslDiscriminator)         : DslRef(1, simpleName, IDslRef.NULL)
            , IGroupLevel, ICrosscuttingPropertiesOf {
                companion object companion : ADslRefCompanion()
                init { this.disc = disc ; refList.add(DslRefAtom(dslRefName, simpleName)) } }
    class apigroup(  simpleName: String, disc: DslDiscriminator)         : DslRef(1, simpleName, IDslRef.NULL)
            , IGroupLevel {
                companion object companion : ADslRefCompanion()
                init { this.disc = disc ; refList.add(DslRefAtom(dslRefName, simpleName)) } }

    interface IModelgroupElement : IElementLevel, ICrosscuttingClassModifiers
    interface IModelOrModelSubelement : ICrosscuttingPropertiesOf
    interface IModel: IModelgroupElement, ICrosscuttingPropertiesOf
    class     model(    simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
                , IModel, IModelgroupElement, IModelOrModelSubelement { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    interface Ifiller : IModelgroupElement
    class     filler(   simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
                , Ifiller { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    interface IallModels: IModelgroupElement
    class     allModels(simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
                , IallModels { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface IModelSubelement : ISubElementLevel, IModelOrModelSubelement, ICrosscuttingNameAndWhereto, ICrosscuttingPropertiesOf, ICrosscuttingClassModifiers
    class         dto(  simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                    , IModelSubelement { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    class         table(simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                    , IModelSubelement { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    class         dco(simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                    , IModelSubelement { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    class           crud(simpleName: String, parentDslRef: IDslRef)     : DslRef(4, simpleName, parentDslRef) {
                        companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    class               crudBlock(simpleName: String, parentDslRef: IDslRef)     : DslRef(5, simpleName, parentDslRef) {
                            companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    class                   crudSubBlock(simpleName: String, parentDslRef: IDslRef)     : DslRef(6, simpleName, parentDslRef) {
                                companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    class copyBoundry(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
            , ICrosscutting { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface IApigroupElement : IElementLevel
    interface Iapi: IApigroupElement
    class     api(    simpleName: String, parentDslRef: IDslRef)         : DslRef(2, simpleName, parentDslRef)
                , Iapi { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    interface IApiSubElement : ISubElementLevel, ICrosscuttingNameAndWhereto
    interface Iapifun: IApiSubElement
    class         apifun(    simpleName: String, parentDslRef: IDslRef)  : DslRef(3, simpleName, parentDslRef)
                    , Iapifun { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface ICrosscuttingProp : ICrosscutting
    interface Iprop : ICrosscuttingProp
    class prop(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
            , Iprop { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    interface ICrosscuttingProperties : ICrosscutting
    interface Iproperties : ICrosscuttingProperties
    class properties(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
            , Iproperties { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }


    interface IDslRun : ICrosscuttingNameAndWhereto
    class dslRun(simpleName: String) : DslRef(1, simpleName, IDslRef.NULL)
            , IDslRun {
                companion object companion : ADslRefCompanion()
                init { this.disc = DslDiscriminator(dslRun::class.simpleName!!) ; refList.add(DslRefAtom(dslRefName, simpleName)) } }

    interface ICrosscuttingNameAndWhereto : ICrosscutting
    interface InameAndWhereto : ICrosscuttingNameAndWhereto
    class nameAndWhereto(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
            , InameAndWhereto { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface ICrosscuttingPropertiesOf : ICrosscutting
    // unused atm, as DslGatherPropertiesImpl atm only gets its parentRef
    interface  IpropertiesOf : ICrosscuttingPropertiesOf
    class propertiesOf(simpleName: String, parentDslRef: IDslRef) :   DslRef(-1, simpleName, parentDslRef)
            , IpropertiesOf { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface ICrosscuttingClassModifiers : ICrosscutting
    interface ICrosscuttingClassMods : ICrosscutting
    interface IclassMods : ICrosscuttingClassMods
    class classMods(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
            , IclassMods { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface ICrosscuttingExtends : ICrosscutting
    interface Iextends : ICrosscuttingExtends
    class extends(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
            , Iextends { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }

    interface ICrosscuttingShowcase : ICrosscutting
    interface Ishowcase : ICrosscuttingShowcase
    class showcase(simpleName: String, parentDslRef: IDslRef) : DslRef(-1, simpleName, parentDslRef)
            , Ishowcase { companion object companion : ADslRefCompanion() init { this.disc = parentDslRef.disc ; refListCloneAndAdd(level, parentDslRef, dslRefName, simpleName) } }
    //endregion class DslRef refs

    //region DslRef companion object ...
    companion object {
        val NULL = IDslRef.NULL
        val ATOMSEP = ":"
        val REFSEP  = "|"
        val COUNTSEP = "/"
        fun refListJoinToString(dslRefAtomList: List<DslRefAtom>) = dslRefAtomList.joinToString(REFSEP)
        fun refAtomsListFull(refString: String, dslDiscriminator: DslDiscriminator = DslDiscriminator(C.DEFAULT)) = refString.split(REFSEP).map { DslRefAtom.from(it) }.toMutableList().also {if (it.isNotEmpty() && it.first().dslRefName != "disc") it.add(0, DslRefAtom("disc", dslDiscriminator.dslDiscriminator))}
        fun groupAndElementAndSubelementLevelDslRef(dslRef: IDslRef): Triple<IDslRef, IDslRef, IDslRef?> {
            if (dslRef.refList.size < 2) throw DslRefException("DslRef not at least ElementLevel depth $dslRef")
            if (dslRef.refList.size == 3) return Triple(dslRef.parentDslRef.parentDslRef, dslRef.parentDslRef, dslRef)
            if (dslRef.refList.size == 2) return Triple(dslRef.parentDslRef, dslRef, null)
            var subelDslRef = dslRef
            while (subelDslRef.refList.size > 3) { subelDslRef = subelDslRef.parentDslRef }
            return Triple(subelDslRef.parentDslRef.parentDslRef, subelDslRef.parentDslRef, subelDslRef)
        }
        fun groupRefFrom(dslRef: IDslRef) = groupAndElementAndSubelementLevelDslRef(dslRef).first
        fun modelRefFrom(dslRef: IDslRef, swappedModelSimpleName: String = C.NULLSTRING): model {
            val (groupRef, elementRef, _) = groupAndElementAndSubelementLevelDslRef(dslRef)
            return model(if (swappedModelSimpleName == C.NULLSTRING) elementRef.simpleName else swappedModelSimpleName, groupRef)
        }
        fun dtoRefFrom(dslRef: IDslRef, simpleName: String = C.DEFAULT, swappedModelSimpleName: String = C.NULLSTRING): dto =
            dto(simpleName, modelRefFrom(dslRef, swappedModelSimpleName))
        fun dcoRefFrom(dslRef: IDslRef, simpleName: String = C.DEFAULT, swappedModelSimpleName: String = C.NULLSTRING): dco =
            dco(simpleName, modelRefFrom(dslRef, swappedModelSimpleName))
        fun tableRefFrom(dslRef: IDslRef, simpleName: String = C.DEFAULT, swappedModelSimpleName: String = C.NULLSTRING): table =
            table(simpleName, modelRefFrom(dslRef, swappedModelSimpleName))

        fun <T : IDslRef> dslRefName(dslRefClass: KClass<T>): String {
            val companion = dslRefClass.companionObjectInstance ?: return "$dslRefClass has no companion object with val dslRefName"
            val companionProp = companion::class.memberProperties.first { it.name == "dslRefName" /* companion object { val dslRefName = "xxx" } */ }
            //companionProp.isAccessible = true
            return companionProp.getter.call(companion).toString()
        }
        //region introspection helpers on DslRef ...
//        inline fun <reified T: IDslRef> funcnamesImplementing(dslRefIfcClass: KClass<T>): List<String> {
//            //val funcnamesList = listOf<String>()
//            val classesImplementingInterface = classesImplementingInterface(dslRefIfcClass)
//            val funcnamesList = classesImplementingInterface.map { DslRef.funcname(it.kotlin) }
//            return funcnamesList
//        }
        inline fun <reified T: IDslRef> getDslRefName(dslRefIfcClass: KClass<T>, refAtomsInclDiscriminator: List<DslRefAtom>, level: Int): Pair<DslRefAtom, KClass<out T>> {
            //val dslRefNameOfRefString = DslRefString.refAtomsList(refString)
            val refAtomOfLevel = refAtomsInclDiscriminator[level]
            var funcClass: KClass<out T>? = null

            // making sure we only test DslRef.dslRefName's
            // that are (or inherited somewhere up in the hierarchy) on an implementing class of the given interface::class
            // which then should have singleton objects that implement that sealed(!) interface
            // which then are returned by this function
            val classesImplementingInterface = classesImplementingInterface(dslRefIfcClass)
            for (javaClass: Class<out T> in classesImplementingInterface) {
                when (dslRefName(javaClass.kotlin)) {
                    refAtomOfLevel.dslRefName -> { funcClass = javaClass.kotlin; break }
                }
            }
            if (funcClass == null) throw Exception("unknown ${level}. part func '$refAtomOfLevel' in DslRef '${refAtomsInclDiscriminator.joinToString(REFSEP)}'")
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
    data class DslRefAtom(val dslRefName: String, val simpleName: String = C.DEFAULT) {
        override fun toString() = "$dslRefName${ if (simpleName == C.DEFAULT) "" else ":$simpleName"}"
        fun debugToString() = "$dslRefName:$simpleName"
        companion object {
            fun from(refString: String) = refString.split(DslRef.ATOMSEP).let { if(it.size == 2) DslRefAtom(it[0], it[1]) else DslRefAtom(it[0]) }
        }
    }
    //endregion
}

object DslRefString {
    fun genericInstance(): DslRef = DslRef.modelgroup(C.NULLSTRING, DslDiscriminator(C.NULLSTRING))
    fun REF(dslRefAtomList: List<DslRef.DslRefAtom>, dslDiscriminator: DslDiscriminator): DslRef = REF("disc${DslRef.ATOMSEP}$dslDiscriminator${DslRef.REFSEP}${dslRefAtomList.joinToString(DslRef.REFSEP)}")
    fun REF(dslRef: DslRef): DslRef = REF(dslRef.toString())
    fun REF(dslRefString: String): DslRef {
        // iterate through atoms
        // get by reflection the DslRef to instantiate via dslRefName
        val refAtomsListFull = DslRef.refAtomsListFull(dslRefString)
        val dslDiscriminator: DslDiscriminator = DslDiscriminator(refAtomsListFull.first().simpleName)

        // sentinel for ALL DslRef's
        // IF ONE IS MISSING, YOU ALSO HAVE TO ADD IT BELOW
        when (genericInstance()) {
            is DslRef.dslRun, is DslRef.apigroup, is DslRef.modelgroup, is DslRef.allModels, is DslRef.api, is DslRef.apifun, is DslRef.classMods, is DslRef.dto, is DslRef.extends, is DslRef.filler, is DslRef.model, is DslRef.nameAndWhereto, is DslRef.properties, is DslRef.prop, is DslRef.propertiesOf, is DslRef.showcase, is DslRef.table, is DslRef.crud, is DslRef.crudBlock, is DslRef.crudSubBlock, is DslRef.copyBoundry, is DslRef.dco -> {}
        }
        var currentDslRef: DslRef = DslRef.modelgroup("DUMMY", DslDiscriminator(C.NULLSTRING)) // dummy
        for ((i, refAtom) in refAtomsListFull.withIndex()) {
            if (i == 0) continue // DslDiscriminator
            when (refAtom.dslRefName) {
                DslRef.dslRun.dslRefName -> DslRef.dslRun(refAtom.simpleName)
                DslRef.apigroup.dslRefName -> { currentDslRef = DslRef.apigroup(refAtom.simpleName, dslDiscriminator)}
                DslRef.modelgroup.dslRefName -> { currentDslRef = DslRef.modelgroup(refAtom.simpleName, dslDiscriminator)}
                DslRef.allModels.dslRefName -> { currentDslRef = DslRef.allModels(refAtom.simpleName, currentDslRef) }
                DslRef.api.dslRefName -> { currentDslRef = DslRef.api(refAtom.simpleName, currentDslRef)}
                DslRef.apifun.dslRefName -> { currentDslRef = DslRef.apifun(refAtom.simpleName, currentDslRef)}
                DslRef.classMods.dslRefName -> { currentDslRef = DslRef.classMods(refAtom.simpleName, currentDslRef)}
                DslRef.dto.dslRefName -> { currentDslRef = DslRef.dto(refAtom.simpleName, currentDslRef)}
                DslRef.extends.dslRefName -> { currentDslRef = DslRef.extends(refAtom.simpleName, currentDslRef)}
                DslRef.filler.dslRefName -> { currentDslRef = DslRef.filler(refAtom.simpleName, currentDslRef)}
                DslRef.model.dslRefName -> { currentDslRef = DslRef.model(refAtom.simpleName, currentDslRef)}
                DslRef.nameAndWhereto.dslRefName -> { currentDslRef = DslRef.nameAndWhereto(refAtom.simpleName, currentDslRef)}
                DslRef.properties.dslRefName -> { currentDslRef = DslRef.properties(refAtom.simpleName, currentDslRef)}
                DslRef.prop.dslRefName -> { currentDslRef = DslRef.prop(refAtom.simpleName, currentDslRef)}
                DslRef.propertiesOf.dslRefName -> { currentDslRef = DslRef.propertiesOf(refAtom.simpleName, currentDslRef)}
                DslRef.showcase.dslRefName -> { currentDslRef = DslRef.showcase(refAtom.simpleName, currentDslRef)}
                DslRef.table.dslRefName -> { currentDslRef = DslRef.table(refAtom.simpleName, currentDslRef)}
                DslRef.crud.dslRefName -> { currentDslRef = DslRef.crud(refAtom.simpleName, currentDslRef)}
                DslRef.crudBlock.dslRefName -> { currentDslRef = DslRef.crudBlock(refAtom.simpleName, currentDslRef)}
                DslRef.crudSubBlock.dslRefName -> { currentDslRef = DslRef.crudSubBlock(refAtom.simpleName, currentDslRef)}
                DslRef.copyBoundry.dslRefName -> { currentDslRef = DslRef.copyBoundry(refAtom.simpleName, currentDslRef)}
                DslRef.dco.dslRefName -> { currentDslRef = DslRef.copyBoundry(refAtom.simpleName, currentDslRef)}
            }
        }
        return currentDslRef
    }
    fun REFmodelOrModelSubelement(dslRefString: String): DslRef.IModelOrModelSubelement {
        val dslRef = REF(dslRefString)
        if ( dslRef !is DslRef.IModelOrModelSubelement) {
            throw DslException("ref: '$dslRef' is not a model or modelSubelement")
        } else {
            return dslRef
        }
    }
}

