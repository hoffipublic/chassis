package com.hoffi.chassis.shared.dsl

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties

interface IDslRef { var level: Int ; val simpleName: String ; val parentRef: IDslRef ; var disc: DslDiscriminator ; val refList: MutableList<DslRef.DslRefAtom>
    companion object { val NULL: DslRef = DslRef.NULL} }
sealed class DslRef(override var level: Int, override val simpleName: String, override val parentRef: IDslRef) : IDslRef {
    //region class DslRef refs
    //override fun toString() = "DslRef(${funcname(this::class)})"
    override fun toString(): String = "disc:$disc;${refListJoin()}"
    private val log = LoggerFactory.getLogger(javaClass)
    override var disc: DslDiscriminator = DslDiscriminator(C.DEFAULT)

    sealed interface IGroupLevel      : IDslRef, ICrosscuttingNameAndWhereto { companion object { val NULL: IGroupLevel = modelgroup(C.NULLSTRING, DslDiscriminator.NULL)}}
    sealed interface IElementLevel    : IDslRef, ICrosscuttingNameAndWhereto
    sealed interface ISubElementLevel : IDslRef
    sealed interface ICrosscutting    : IDslRef  { companion object { val NULL: ICrosscutting = propertiesOf(C.NULLSTRING, DslRef.NULL)}}
    interface Imodelgroup : IGroupLevel
    class modelgroup(simpleName: String, disc: DslDiscriminator)         : DslRef(1, simpleName, NULL)
        , Imodelgroup, ICrosscuttingPropertiesOf
            { companion object { val funcname: String = modelgroup::class.simpleName!! }         init { this.disc = disc ; refList.add(DslRefAtom(funcname, simpleName)) } }
    interface Iapigroup : IGroupLevel
    class apigroup(  simpleName: String, disc: DslDiscriminator)         : DslRef(1, simpleName, NULL)
        , Iapigroup
            { companion object { val funcname: String = apigroup::class.simpleName!! }           init { this.disc = disc ; refList.add(DslRefAtom(funcname, simpleName)) } }
    interface IModelgroupElement : IElementLevel
    interface IModelOrModelSubElement : ICrosscuttingPropertiesOf
    interface IModel: IModelgroupElement, ICrosscuttingPropertiesOf
    class     model(    simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
            , IModel, IModelgroupElement, IModelOrModelSubElement
                {   companion object {
                        val funcname: String = model::class.simpleName!! // has to be defined before(!) NULL
                        val NULL = model(C.NULLSTRING, DslRef.NULL)
                    }
                    init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) }
                    enum class MODELELEMENT { MODEL, DTO, TABLE } }
    interface Ifiller : IModelgroupElement
    class     filler(   simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
            , Ifiller
                { companion object { val funcname: String = filler::class.simpleName!! }   init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) } }
    interface IallModels: IModelgroupElement
    class     allModels(simpleName: String, parentDslRef: IDslRef)       : DslRef(2, simpleName, parentDslRef)
            , IallModels
                { companion object { val funcname: String = allModels::class.simpleName!! }   init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) } }
    interface IModelSubElement : IModelOrModelSubElement, ICrosscuttingNameAndWhereto, ICrosscuttingPropertiesOf, ISubElementLevel
    interface    Idto: IModelSubElement
    class         dto(  simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                , Idto
                    { companion object { val funcname: String = dto::class.simpleName!! }   init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) } }
    interface    ITable: IModelSubElement
    class         table(simpleName: String, parentDslRef: IDslRef)       : DslRef(3, simpleName, parentDslRef)
                , ITable
                    { companion object { val funcname: String = table::class.simpleName!! }   init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) } }
    interface IApigroupElement : IElementLevel
    interface Iapi: IApigroupElement
    class     api(    simpleName: String, parentDslRef: IDslRef)         : DslRef(2, simpleName, parentDslRef)
            , Iapi
                { companion object { val funcname: String = api::class.simpleName!! }   init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) } }
    interface ApigroupSubElement : ISubElementLevel, ICrosscuttingNameAndWhereto
    interface Iapifun: ApigroupSubElement
    class         apifun(    simpleName: String, parentDslRef: IDslRef)  : DslRef(3, simpleName, parentDslRef)
                , Iapifun
                    { companion object { val funcname: String = apifun::class.simpleName!! }   init { this.disc = parentDslRef.disc ; createRefList(parentDslRef, funcname, simpleName) } }

    interface IDslRun : ICrosscuttingNameAndWhereto
    class DslRun(simpleName: String) : DslRef(1, simpleName, NULL)
        , IDslRun
            { companion object { val funcname: String = DslRun::class.simpleName!! } init { this.disc = DslDiscriminator(DslRun::class.simpleName!!) ; refList.add(DslRefAtom(funcname, simpleName)) } }

    interface ICrosscuttingNameAndWhereto : ICrosscutting
    interface InameAndWhereto : ICrosscuttingNameAndWhereto
    class nameAndWhereto(simpleName: String, parentDslRef: IDslRef)      : DslRef(-1, simpleName, parentDslRef)
        , InameAndWhereto
            { companion object { val funcname: String = nameAndWhereto::class.simpleName!! } }
    interface ICrosscuttingPropertiesOf : ICrosscutting

    interface  IpropertiesOf : ICrosscuttingPropertiesOf
    class propertiesOf(simpleName: String, parentDslRef: IDslRef) :   DslRef(-1, simpleName, parentDslRef)
        , IpropertiesOf
            { companion object { val funcname: String = propertiesOf::class.simpleName!! } }

    class NULL(level: Int = 0, simpleName: String = C.NULLSTRING, dslDiscriminator: DslDiscriminator = DslDiscriminator(C.NULLSTRING)) : DslRef(level, C.NULLSTRING, NULL) { init { this.disc = dslDiscriminator } }
    //endregion class DslRef refs
    //region DslRef companion object ...
    companion object {
        val NULL: DslRef = NULL()
        fun refListJoin(refList: List<DslRefAtom>) = refList.joinToString(";")
        fun refAtomsListFull(refString: String, dslDiscriminator: DslDiscriminator = DslDiscriminator(C.DEFAULT)) = refString.split(";").map { DslRefAtom.from(it) }.toMutableList().also {if (it.isNotEmpty() && it.first().functionName != "disc") it.add(0, DslRefAtom("disc", dslDiscriminator.dslDiscriminator))}
        fun <T : IDslRef> funcname(dslRefClass: KClass<T>): String {
            val companion = dslRefClass.companionObjectInstance ?: return "$dslRefClass has no companion object with val funcname"
            val companionProp = companion::class.memberProperties.first { it.name == "funcname" /* companion object { val funcname = "xxx" } */ }
            //companionProp.isAccessible = true
            return companionProp.getter.call(companion).toString()
        }
        //region introspection helpers on DslRef ...
        inline fun <reified T: IDslRef> funcnamesImplementing(dslRefIfcClass: KClass<T>): List<String> {
            //val funcnamesList = listOf<String>()
            val classesImplementingInterface = classesImplementingInterface(dslRefIfcClass)
            val funcnamesList = classesImplementingInterface.map { DslRef.funcname(it.kotlin) }
            return funcnamesList
        }
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

    override val refList = mutableListOf<DslRefAtom>()
    //region helpers ...
//    inline fun <reified T: DslRef> newChildRef(simpleName: String, childRefClass: KClass<T>) : T {
//        val newRef = DslRef.NULL(this.level+1, simpleName, this.disc) as DslRef
//        newRef.refList.addAll(this.refList)
//        newRef.refList.add(DslRefAtom("<APPEND>", simpleName))
//        when (newRef) { // TODO don't know how to get an exhaustive when on all alid DslRef Classes AND check their VALID selaed subclasses
//            /* dummy cases to have an exhaustive when at compile time */
//            is NULL -> {}
//            is allModels -> {}
//            is api -> {}
//            is apifun -> {}
//            is apigroup -> {}
//            is dto -> {}
//            is filler -> {}
//            is model -> { model(simpleName, this) }
//            is modelgroup -> {}
//            is nameAndWhereto -> {}
//            is propertiesOf -> {}
//            is table -> {}
//            is DslRun -> {}
//        }
//        // TODO this is inherently unsafe as it is possible to instantiate a random childRef to ANY parentRef
//        val result = T::class.primaryConstructor!!.call(simpleName, this.disc, this)
//        return result
//    }
    fun refListJoin() = refListJoin(refList)
    protected fun createRefList(parentDslRef: IDslRef, funcname: String, simpleName: String) {
        if (parentDslRef.refList.size > parentDslRef.level) {
            log.warn("parent refList '{}' is longer than its level '{}' while creating ref for '{}'", refListJoin(), parentDslRef.level, this)
        }
        refList.addAll(parentDslRef.refList.take(parentDslRef.level))
        refList.add(DslRefAtom(funcname, simpleName))
    }
    //endregion

    //region DslRefAtom
    data class DslRefAtom(val functionName: String, val simpleName: String = C.DEFAULTSTRING) {
        override fun toString() = "$functionName${ if (simpleName in listOf(C.DEFAULTSTRING, C.DEFAULT)) "" else ":$simpleName"}"
        fun debugToString() = "$functionName:$simpleName"
        companion object {
            fun from(refString: String) = refString.split(":").let { if(it.size == 2) DslRefAtom(it[0], it[1]) else DslRefAtom(it[0]) }
        }
    }
    //endregion

    //region equals and hashCode ...
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DslRef) return false
        if (disc != other.disc) return false
        if (refList != other.refList) return false
        return true
    }
    override fun hashCode(): Int {
        var result = disc.hashCode()
        result = 31 * result + refList.hashCode()
        return result
    }
    //endregion
}

object DslRefString {
    //region DslRefString { ...
    fun groupRef(refString: String, dslDiscriminator: DslDiscriminator) : DslRef.IGroupLevel = groupRef(DslRef.refAtomsListFull(refString), dslDiscriminator)
    fun groupRef(refAtomsList: List<DslRef.DslRefAtom>, dslDiscriminator: DslDiscriminator) : DslRef.IGroupLevel {
        if(refAtomsList.size < 2) {// first element is DslDiscriminator
            throw DslException("cannot extract groupRef from '${DslRef.refListJoin(refAtomsList)}' as it has less than 2 elements ")
        }
        val (groupLevelRefAtom, _) = DslRef.getDslRefFuncname(DslRef.IGroupLevel::class, refAtomsList, 1)
        val dslGroupRef = when (groupLevelRefAtom.functionName) {
            DslRef.modelgroup.funcname -> { DslRef.modelgroup(groupLevelRefAtom.simpleName, dslDiscriminator) }
            DslRef.apigroup.funcname ->   { DslRef.apigroup(groupLevelRefAtom.simpleName, dslDiscriminator) }
            else -> { throw DslException("This should be impossible") }
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
        val groupRef = groupRef(refAtomsList, dslDiscriminator)
        val (elementLevelRefAtom, _) = DslRef.getDslRefFuncname(DslRef.IElementLevel::class, refAtomsList, 2)
        val elementRef = when (groupRef) {
            //DslRef.IGroupLevel.NULL -> { throw DslException("trying to get elementRef on NULL") }
            is DslRef.Imodelgroup -> {
                when (elementLevelRefAtom.functionName) {
                    DslRef.model.funcname -> { DslRef.model(elementLevelRefAtom.simpleName, groupRef) }
                    DslRef.filler.funcname -> { DslRef.filler(elementLevelRefAtom.simpleName, groupRef) }
                    DslRef.allModels.funcname -> { DslRef.allModels(elementLevelRefAtom.simpleName, groupRef) }
                    else -> { throw DslException("unknown Imodelgroup.funcname '${elementLevelRefAtom}'") }
                }
            }
            is DslRef.Iapigroup -> {
                when (elementLevelRefAtom.functionName) {
                    DslRef.api.funcname -> { DslRef.api(elementLevelRefAtom.simpleName, groupRef)}
                    else -> { throw DslException("unknown Iapigroup.funcname '${elementLevelRefAtom}'") }
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
//    context(IDslClass)
//    fun fromSelf(selfDslRef: DslRef, refString: String): DslRef { // TODO implement me
//        val atomList = refString.split(";").map { DslRefAtom.from(it) }
//        val discriminator = atomList[0].functionName
//        val group = DslRef.DslGroupRefEither.NULL
//        if(atomList.size > 1) {
//            when (atomList[1].functionName) {
//                DslRef.modelgroup.funcname -> DslRef.modelgroupRef(discriminator, atomList[1].simpleName)
//                DslRef.apigroup.funcname   -> DslRef.apigroupRef(  discriminator, atomList[1].simpleName)
//            }
//        }
//        return DslRef.NULL
//    }
//    //endregion
}

//context(IDslClass, DslDiscriminatorWrapper)
//fun String.ref() = DslRefString.fromSelf(selfDslRef, this)

