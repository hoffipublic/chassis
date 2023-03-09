package com.hoffi.chassis.dsl

import com.hoffi.chassis.chassismodel.dsl.DslInstance
import com.hoffi.chassis.chassismodel.dsl.DslTopLevel
import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.modelgroup.DslModelgroup
import com.hoffi.chassis.shared.dsl.DslDiscriminatorWrapper
import com.hoffi.chassis.shared.dsl.DslRef

context(DslRun, DslDiscriminatorWrapper)
@DslTopLevel
fun modelgroup(simpleName: String, modelgroupBlock: DslModelgroup.() -> Unit) {
//    when (DSLPASS)
//
//    when (dslCtx.DSLPASS) {
//        DslCtx.DSLPASS.ONE_BASEMODELS -> dslCtx.createModelgroup(name).apply(modelgroupBlock)
//        DslCtx.DSLPASS.TWO_TABLEMODELS -> dslCtx[ModelgroupName(name)].apply(modelgroupBlock)
//        DslCtx.DSLPASS.THREE_ALLMODELS -> {
//            // only execute allModels { } clause, do not apply(modelgroupBlock)
//            val theModelGroup = dslCtx[ModelgroupName(name)]
//            if (theModelGroup.allModelsBlock != null) {
//                theModelGroup.allModelsObj.apply(theModelGroup.allModelsBlock!!)
//            }
//        }
//        DslCtx.DSLPASS.FOUR_REFERENCING -> dslCtx[ModelgroupName(name)].apply(modelgroupBlock)
//        DslCtx.DSLPASS.FINISH -> {
//            // finishing all models after allModels { } clause has run
//            val theModelGroup = dslCtx[ModelgroupName(name)]
//            for (modelgroupModel in dslCtx.allModels(theModelGroup.modelgroupName)) {
//                GENS.values().forEach {
//                    when (it) {
//                        GENS.DTO ->    modelgroupModel.finish(GENS.DTO)
//                        GENS.TABLE ->  modelgroupModel.finish(GENS.TABLE)
//                        // FILLER has To be last, as it references Models above
//                        GENS.FILLER -> modelgroupModel.finish(GENS.FILLER)
//                        GENS.UNDEF -> {}
//                        GENS.COMMON -> {}
//                    }
//                }
//            }
//        }
//    }

    val modelgroupRef = DslRef.modelgroup(simpleName, dslDiscriminator)
    @DslInstance val dslModelgroup: DslModelgroup = dslCtx.createModelgroup(modelgroupRef)
    dslModelgroup.apply(modelgroupBlock)
}

//abstract class DslClass(val modelGenRef: ModelGenRef,
//               private val dslClassProps: DslClassProps = DslClassProps(modelGenRef)
//) :   IDslClassPropFuns by dslClassProps
//{
//    lateinit var dslModel: DslModel
//    override fun toString(): String = "${DslClass::class.simpleName}[${modelGenRef}]"
//    private val propertys = mutableMapOf<String, Property>()
//    val toStringMembersClassProps: MutableSet<ModelGenPropRef> = mutableSetOf()
//    init {
//        dslClassProps.propertys = propertys
//        dslClassProps.toStringMembersClassProps = toStringMembersClassProps
//    }
//    val MODELREF = modelGenRef.modelRef
//    val GENTYPE = modelGenRef.gentype
//    var clausePresent = false
//    val noClause: Boolean
//        get() = !clausePresent
////    var kind: ClassObjectOrInterface = ClassObjectOrInterface.UNDEFINED
//    @NonDslBlock var gatherPropertysOfSuperclasses: GENS = Defaults.getGENS(GENTYPE, DefaultValueGatherPropertysOfSuperclasses) // if GENS.COMMON then Do IGNORE IGather!
//    @NonDslBlock fun gatherPropertysOfSuperclasses(gentype: GENS = GENTYPE) { gatherPropertysOfSuperclasses = gentype }
//
//    @NonDslBlock var subPackage = if (modelGenRef.gentype == GENS.FILLER) "filler" else ""
//        private set(value) { if (dslCtx.DSLPASS == DslCtx.DSLPASS.ONE_BASEMODELS) field = value.replace("/", ".").modifyPackagename() }
//    @NonDslBlock fun subPackage(aSubPackage: String) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return
//        this.subPackage = aSubPackage
//    }
//    fun prependSubpackageFromAll(subpackage: String) {
//        if (subpackage == this.subPackage) return
//        this.subPackage = "${this.subPackage}${if (this.subPackage.isNotBlank()) "." else ""}$subpackage"
//    }
//
//    @NonDslBlock var prefix = ""
//        private set(value) { field = value.modifyClassname()  }
//    @NonDslBlock var postfix = ""
//        private set(value) { field = value.modifyClassname()  }
//    @NonDslBlock fun prefix(prefix: String) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return
//        this.prefix = prefix
//    }
//    @NonDslBlock fun postfix(postfix: String) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return
//        this.postfix = postfix
//    }
//
//    @NonDslBlock fun addPrefix(prefix: String) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return
//        this.prefix = this.prefix + prefix.modifyClassname()
//    }
//    fun addPrefixFromAll(prefix: String) {
//        this.prefix = this.prefix + prefix.modifyClassname()
//    }
//    @NonDslBlock fun addPostfix(postfix: String) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return
//        this.postfix = this.postfix + postfix.modifyClassname()
//    }
//    fun addPostfixFromAll(postfix: String) {
//        this.postfix = this.postfix + postfix.modifyClassname()
//    }
//
//    val classModifiers = mutableSetOf<KModifier>()
//    @NonDslBlock fun classModifiers(vararg modifiers: KModifier) { classModifiers.addAll(modifiers) }
//    @NonDslBlock infix fun plus(modifier: KModifier) { classModifiers.add(modifier) }
//    val extendsObj = Extends(modelGenRef)
//    @NonDslBlock fun extends(replaceSuperclass: Boolean = false, replaceSuperInterfaces: Boolean = false, function: Extends.() -> Unit) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.FOUR_REFERENCING) return
//        if (replaceSuperclass) { extendsObj.replaceSuperclass = true }
//        if (replaceSuperInterfaces) { extendsObj.replaceSuperInterfaces = true }
//        extendsObj.apply(function)
//    }
//
//    val modelClassPropsFunSpecs = mutableMapOf<String, FunSpec.Builder>()
//
//    @NonDslBlock var constructorVisibility = true
//        set(value) {
//            when (modelGenRef.gentype) {
//                GENS.COMMON -> throw DslException("$modelGenRef setting constructorVisibility in common dslModel not allowed, please use allModels { xxx { constructorVisibility = false } }")
//                GENS.FILLER -> throw DslException("$modelGenRef setting constructorVisibility for filler { } not allowed. it is a singleton object anyways.")
//                else -> field = value
//            }
//        }
//
//    @NonDslBlock fun annotateProperty(name: String, annotationBuilder: AnnotationSpec.Builder) {
//        if (dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return // do something only in DSLPASS.ONE_BASEMODELS
//        if (propertys.containsKey(name)) {
//            propertys[name]!!.annotations.add(annotationBuilder)
//        } else {
//            val dslModelProp = dslModel.itsPropertys { it.nameInDsl == name }
//            if (dslModelProp.isNotEmpty()) {
//                println("warning: ${modelGenRef} adding ${annotationBuilder.build()} to common-modelElement property '$name'")
//                dslModelProp.first().annotations.add(annotationBuilder)
//            } else {
//                throw DslException("$modelGenRef annotateProperty named '$name' not found")
//            }
//        }
//    }
//
//
//    protected fun getProp(name: String) = propertys[name]!!
//    fun getPropOrNull(name: String) = propertys[name]
//    private fun addProp(name: String, prop: Property) = if (!propertys.containsKey(name)) prop.also { propertys[name] = prop } else { throw CodegenException("$modelGenRef already has a property $name") }
//    fun containsProperty(name: String) = propertys.contains(name)
//    fun itsPropertys(propFilter: (Property) -> Boolean = {true}) = propertys.values.filter(propFilter)
//    fun addAllPropertys(props: Collection<Property>) { propertys.putAll(props.map { it.nameInDsl to it })}
//
//    /* does pre-translate the typeFor!!! */
//    fun alterPropertyForDB(name: String,
//                           formatAddendum: String = Defaults.DEFAULT_STRING,
//                           vararg argsAddendum: Any,
//                           length: Int = Defaults.DEFAULT_INT,
//                           tags: Tags = Tags.NONE
//    ) {
//        if(dslCtx.DSLPASS != DslCtx.DSLPASS.ONE_BASEMODELS) return // do something only in DSLPASS.ONE_BASEMODELS
//        if (modelGenRef.gentype != GENS.TABLE) throw DslException("alterPropertyForDB only allowed for DslModel(s) 'tableType'")
//        if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
//        val modelProperty = dslCtx[modelGenRef].getProp(name, GENTYPE)
//        if (formatAddendum.contains("nullable()") || argsAddendum.any { it.toString().contains(("nullable()")) }) {
//            throw DslException("$modelGenRef prop '${modelProperty.nameInDsl}' \".nullable()\" not allowed")
//        }
//        val newProperty = modelProperty.createDbProperty(GENS.TABLE, modelGenRef.modelRef, length)
//        newProperty.tags.addAll(tags)
//        val initializer = newProperty.eitherTypModelOrClass.initializer
//        if (formatAddendum != Defaults.DEFAULT_STRING) {
//            val newFormat = if (formatAddendum.startsWith('.')) initializer.format + formatAddendum else "${initializer.format}.$formatAddendum"
//            val newArgs = mutableListOf<Any>()
//            newArgs.addAll(initializer.args)
//            newArgs.addAll(argsAddendum)
//            newProperty.eitherTypModelOrClass.initializer = Initializer.of(newFormat, newArgs)
//        }
//        addProp(name, newProperty)
//    }
//
//    @NonDslBlock fun manyToMany(modelRefString: String, mutable: Mutable = immutable, collectionType: COLLECTIONTYPE = COLLECTIONTYPE.LIST, oneToManyBlock: ManyToMany.() -> Unit) =
//        manyToMany(modelRefString.modelRef(), mutable, collectionType, oneToManyBlock)
//    @NonDslBlock fun manyToMany(modelRef: ModelRef, mutable: Mutable = immutable, collectionType: COLLECTIONTYPE = COLLECTIONTYPE.LIST, oneToManyBlock: ManyToMany.() -> Unit) {
//        if(dslCtx.DSLPASS != DslCtx.DSLPASS.FOUR_REFERENCING) return // do something only in DSLPASS.FOUR_REFERENCING
//        val oneToMany = ManyToMany(dslCtx[modelGenRef], mutable, collectionType)
//        oneToMany.apply(oneToManyBlock)
//    }
//
//    @NonDslBlock fun function(name: String, function: FunSpec.Builder.() -> Unit) {
//        if(dslCtx.DSLPASS != DslCtx.DSLPASS.FOUR_REFERENCING) return // do something only in DSLPASS.FOUR_REFERENCING
//        val funSpecBuilder: FunSpec.Builder = FunSpec.builder(name)
//        modelClassPropsFunSpecs[name] = funSpecBuilder
//        funSpecBuilder.function()
//    }
//
//    @NonDslBlock fun addToStringMembers(toStringMembersList: List<ModelGenPropRef>) {
//        toStringMembersClassProps.addAll(toStringMembersList)
//    }
//    @NonDslBlock fun addToStringMembers(vararg propName: String) {
//        toStringMembersClassProps.addAll(propName.map { ModelGenPropRef(modelGenRef, it) })
//    }
//
//    sealed class EitherInitializerOrBusinessInit {
//        data class EitherInitializer(val initializer: Initializer): EitherInitializerOrBusinessInit()
//        data class EitherBusinessInit(val businessinit: BUSINESSINIT): EitherInitializerOrBusinessInit()
//    }
//    @DslModel class DefaultBusinessValues(val modelGenRef: ModelGenRef) {
//        lateinit var dslModel: DslModel
//        val gentype = modelGenRef.gentype
//        val defaultBusinessValuesMap = mutableMapOf<String, EitherInitializerOrBusinessInit>()
//        @NonDslBlock infix fun String.with(initializer: Initializer) {
//            defaultBusinessValuesMap[this] = EitherInitializerOrBusinessInit.EitherInitializer(initializer)
//        }
//        @NonDslBlock infix fun String.with(businessinit: BUSINESSINIT) {
//            defaultBusinessValuesMap[this] = EitherInitializerOrBusinessInit.EitherBusinessInit(businessinit)
//        }
//        private fun propFromPropString(modelGenRefPropRefString: String): Property {
//            val parts = modelGenRefPropRefString.splitRef()
//            val prop = when (parts) {
//                is EitherModelRefModelGenRefOrModelGenPropRef.EitherSimple -> dslModel.getProp(parts.simpleString, gentype)
//                is EitherModelRefModelGenRefOrModelGenPropRef.EitherModelGenPropRef -> dslModel.getProp(parts.modelGenPropRef.propName)
//                is EitherModelRefModelGenRefOrModelGenPropRef.EitherModelGenRef -> throw DslException("\"$modelGenRefPropRefString\" is ModelGenRef instead of PropRef")
//                is EitherModelRefModelGenRefOrModelGenPropRef.EitherModelRef -> throw DslException("\"$modelGenRefPropRefString\" is ModelRef instead of PropRef")
//            }
//            return prop
//        }
//        @NonDslBlock fun init(modelGenRefPropRefString: String, initializer: Initializer) {
//            val prop = propFromPropString(modelGenRefPropRefString)
//            when (prop.eitherTypModelOrClass) {
//                is ClassProperty -> defaultBusinessValuesMap[prop.nameInDsl] = EitherInitializerOrBusinessInit.EitherInitializer(initializer)
//                is ModelProperty -> defaultBusinessValuesMap[prop.nameInDsl] = EitherInitializerOrBusinessInit.EitherInitializer(initializer)
//                is TypProperty -> defaultBusinessValuesMap[prop.nameInDsl] = EitherInitializerOrBusinessInit.EitherInitializer(initializer)
//            }
//        }
//        @NonDslBlock fun init(modelGenRefPropRefString: String, businessinit: BUSINESSINIT = BUSINESSINIT.INIT) {
//            val prop = propFromPropString(modelGenRefPropRefString)
//            when (prop.eitherTypModelOrClass) {
//                is ClassProperty -> defaultBusinessValuesMap[prop.nameInDsl] = EitherInitializerOrBusinessInit.EitherBusinessInit(businessinit)
//                is ModelProperty -> defaultBusinessValuesMap[prop.nameInDsl] = EitherInitializerOrBusinessInit.EitherBusinessInit(businessinit)
//                is TypProperty -> defaultBusinessValuesMap[prop.nameInDsl] = EitherInitializerOrBusinessInit.EitherBusinessInit(businessinit)
//            }
//        }
//    }
//    val defaultBusinessValues = DefaultBusinessValues(modelGenRef)
//    @NonDslBlock fun initBusinessValues(function: DslClass.DefaultBusinessValues.() -> Unit) {
//        defaultBusinessValues.dslModel = dslModel
//        when (dslCtx.DSLPASS) {
//            DslCtx.DSLPASS.ONE_BASEMODELS -> return
//            DslCtx.DSLPASS.TWO_TABLEMODELS -> defaultBusinessValues.function()
//            DslCtx.DSLPASS.THREE_ALLMODELS -> return
//            DslCtx.DSLPASS.FOUR_REFERENCING -> return
//            DslCtx.DSLPASS.FINISH -> return
//        }
//    }
////    fun addToStringMembers(vararg toStringMembers: String, toStringMembersList: List<String> = emptyList()) {
////        val expandSet = mutableSetOf<String>()
////        val iterSet = mutableSetOf<String>(*toStringMembers)
////        iterSet.addAll(toStringMembersList)
////        for (tsm in iterSet) {
////            val els = tsm.split('.')
////            when (els.size) {
////                3 -> expandSet.add(tsm)
////                2 -> expandSet.add("${modelgroup.modelgroupName.string}.$tsm")
////                1 -> expandSet.add("${modelgroup.modelgroupName.string}.${modelGenRef.modelRef.modelName.string}.$tsm")
////                else -> throw CodegenException("syntax error in toStringMember '${tsm}")
////            }
////        }
////        toStringMembersClassProps.addAll(expandSet)
////    }
//}
//sealed class EitherTypeOrModelGenRef {
//    val extendsNothing: Boolean
//        get() = this is ExtendsNothing
//    val extendsSomething: Boolean
//        get() = this !is ExtendsNothing
//    abstract val isInterface: Boolean
//    data class EitherKClass(val typeName: TypeName, override val isInterface: Boolean): EitherTypeOrModelGenRef()
//    data class EitherDslModelGenRef(val modelGenRef: ModelGenRef): EitherTypeOrModelGenRef() {
//        constructor(modelGenRefString: String, gentype: GENS) : this(modelGenRefString.toSpecificModelGenRef(gentype))
//        val gentype: GENS
//            get() = modelGenRef.gentype
//        override val isInterface: Boolean
//            get() {
//                val theModel = dslCtx[modelGenRef]
//                return if (gentype == GENS.COMMON) {
//                    println("warning: determining class|interface to $modelGenRef from modelElement/common kind")
//                    theModel.kind == ClassObjectOrInterface.INTERFACE
//                } else theModel.theKind(modelGenRef.gentype) == ClassObjectOrInterface.INTERFACE
//            }
//    }
//    data class ExtendsNothing(val dummy: String = "ExtendsNothing"): EitherTypeOrModelGenRef() {
//        override val isInterface = false
//        companion object { val INSTANCE = ExtendsNothing() }
//    }
//    companion object {
//        val NOTHING = ExtendsNothing.INSTANCE
//    }

//@DslModel class Extends(val modelGenRef: ModelGenRef) {
//    @NonDslBlock var replaceSuperclass = false
//    @NonDslBlock var replaceSuperInterfaces = false
//    var superClass: EitherTypeOrModelGenRef = EitherTypeOrModelGenRef.NOTHING
//    @NonDslBlock var superClassConstructorFormat = Format.EMPTY
//    val superInterfaces = mutableListOf<EitherTypeOrModelGenRef>()
////    var eitherExtendsModelOrClass: EitherExtendsModelOrClass = ExtendsNothing.INSTANCE
////    val superInterfaces = mutableListOf<EitherExtendsModelOrClass>()
//    override fun toString(): String = "${Extends::class.simpleName}[${modelGenRef}](${superInterfaces.joinToString()})"
//
//    @NonDslBlock operator fun KClass<*>.unaryPlus() {
//        if (this.java.isInterface) {
//            if (replaceSuperInterfaces) superInterfaces.clear()
//            superInterfaces.add(EitherTypeOrModelGenRef.EitherKClass(this.asTypeName(), true))
//        } else {
//            if ((superClass === EitherTypeOrModelGenRef.NOTHING) || replaceSuperclass) {
//                superClass = EitherTypeOrModelGenRef.EitherKClass(this.asTypeName(), false)
//            } else {
//                throw DslException("$modelGenRef already has superClass $superClass -> cannot set superClass $this")
//            }
//        }
//    }
//    @NonDslBlock fun constructorParams(format: String, vararg args: Any) {
//        superClassConstructorFormat = Format.of(format, *args)
//    }
//
//    @NonDslBlock fun from(modelRefString: String) {
//        modelRefString.unaryPlus()
//    }
//    @NonDslBlock operator fun String.unaryPlus() {
//        // at this time we cannot say for sure if the referenced DslModel is a class or an interface
//        val modelRefString = if (this.contains(':')) this else "${modelGenRef.modelgroupNameString}:$this"
//        var modelRefGentype = modelRefString.gentype()
//        if (modelRefGentype == GENS.COMMON) modelRefGentype = modelGenRef.gentype
//        val modelEither = EitherTypeOrModelGenRef.EitherDslModelGenRef(modelRefString, modelRefGentype)
//        if (modelEither.isInterface) {
//            if (replaceSuperInterfaces) superInterfaces.clear()
//            superInterfaces.add(modelEither)
//        } else {
//            if ((superClass === EitherTypeOrModelGenRef.NOTHING) || replaceSuperclass) {
//                superClass = modelEither
//            } else {
//                throw DslException("$modelGenRef already has superClass $superClass -> cannot set superClass $this")
//            }
//        }
//    }
//}


//    private val log = LoggerFactory.getLogger(this::class.java)
//    override fun toString(): String = "${DslModel::class.simpleName}[${modelGenRef.modelRef}]"
//
//    val dslDtoObj = DslDto(ModelGenRef(modelRef, GENS.DTO)).also { it.dslModel = this }
//    val dslTableObj = DslTable(ModelGenRef(modelRef, GENS.TABLE)).also { it.dslModel = this }
//    val dslFillerObj = DslFiller(ModelGenRef(modelRef, GENS.FILLER)).also { it.dslModel = this }
//
//    init {
//        super.clausePresent = true // "COMMON" clause is modelElement { } itself, and therefore always present
//        super.dslModel = this
//        dslClassName.dslModel = this
//    }
//
//    companion object {
//        fun create(name: String, modelgroup: DslModelgroup) = DslModel(ModelRef(modelgroup.modelgroupName, ModelName(name)), modelgroup)
//        fun universeCreate(modelGenRef: ModelGenRef) = DslModel(modelGenRef.modelRef, DslModelgroup(modelGenRef.modelgroupName))
//    }
//
//    /** central point to get dslModel for specific gentype: GENS */
//    operator fun get(gentype: GENS = GENS.COMMON): DslClass = when (gentype) {
//        GENS.COMMON -> this
//        GENS.DTO -> dslDtoObj
//        GENS.TABLE -> dslTableObj
//        else -> throw DslException("DslModel operator get: unknown GENS: $gentype in $this")
//    }
//
//    fun theKind(gentype: GENS): ClassObjectOrInterface {
//        if (gentype == GENS.FILLER) return ClassObjectOrInterface.OBJECT
//        var theKind = ClassObjectOrInterface.UNDEFINED
//        if (this[gentype].clausePresent) {
//            theKind = this[gentype].kind // of dtoType, tableType, ...
//            if (theKind == ClassObjectOrInterface.UNDEFINED) theKind = this.kind // of COMMON dslModel { }
//            if (theKind == ClassObjectOrInterface.UNDEFINED) theKind = ClassObjectOrInterface.CLASS
//            if (gentype == GENS.TABLE && theKind != ClassObjectOrInterface.INTERFACE) theKind = ClassObjectOrInterface.OBJECT
//        }
//        return theKind
//    }
//
//    fun getProp(name: String, gentype: GENS) = allPropertys(gentype).firstOrNull { it.nameInDsl == name } ?: throw CodegenException("$this $gentype (or its gathered superclasses) does not have a prop named '$name'") // this[gentype].getPropOrNull(name) ?: getProp(name)
//
//    /** GENS.COMMON means do IGNORE gather properties of superclasses */
//    private fun gatherPropertysOfSuperclassesFor(gentype: GENS): GENS {
//        val specificModel = get(gentype)
//        return if (specificModel.gatherPropertysOfSuperclasses != GENS.COMMON) {
//            specificModel.gatherPropertysOfSuperclasses
//        } else {
//            this.gatherPropertysOfSuperclasses
//        }
//    }
//
//    fun propertyExists(name: String, gentype: GENS) = get(gentype).containsProperty(name) || get(GENS.COMMON).containsProperty(name)
//    fun specificGentypeContainsProperty(name: String, gentype: GENS) = get(gentype).containsProperty(name)
//
//    fun classModifiers(gentype: GENS): Set<KModifier> {
//        return this[gentype].classModifiers + classModifiers
//    }
//
////    private fun extendsClassOrModelThatIsNoInterface(gentype: GENS): EitherKClassOrModelGenRefString {
////        val specialExtendsNonInterfaces = get(gentype).extendsObj.eitherKClassOrModelGenRefStrings.filter { it.isNotInterface }
////        val commonExtendsNonInterfaces = extendsObj.eitherKClassOrModelGenRefStrings.filter { it.isNotInterface }
////        if (specialExtendsNonInterfaces.isNotEmpty()) {
////            if (specialExtendsNonInterfaces.size > 1) {
////                throw DslException("$modelGenRef special extends more than one non interface ${specialExtendsNonInterfaces.joinToString()}")
////            } else {
////                return specialExtendsNonInterfaces[0]
////            }
////        }
////        if (commonExtendsNonInterfaces.isNotEmpty()) {
////            if(commonExtendsNonInterfaces.size > 1) {
////                throw DslException("$modelGenRef common extends more than one non interface ${commonExtendsNonInterfaces.joinToString()}")
////            } else {
////                return commonExtendsNonInterfaces[0]
////            }
////        }
////        return EitherKClassOrModelGenRefString.NOTHING
////    }
////    fun doesExtendAModelOrClass(gentype: GENS): Boolean = get(gentype).extendsObj.eitherExtendsModelOrClass.extendsClassOrModel || get(GENS.COMMON).extendsObj.eitherExtendsModelOrClass.extendsClassOrModel
//
//    /** Propertys with same name in specific overwrite propertys in common dslModel (independent of data type)<br/>
//     * if gathering props of superclasses also get props of all these superclasses */
//    fun allPropertys(gentype: GENS, forceGatherForGentype: GENS = GENS.UNDEF, propFilter: (Property) -> Boolean = { true }): MutableSet<Property> {
//        val gentypeProps = get(gentype).itsPropertys(propFilter)
//        val modelProps = this.itsPropertys(propFilter)
//        val allProps = mutableSetOf<Property>()
//        allProps.addAll(gentypeProps)
//        allProps.addAll(modelProps)
//
//        val gentypeToGatherPropsOf = if (forceGatherForGentype != GENS.UNDEF)  forceGatherForGentype
//        else gatherPropertysOfSuperclassesFor(gentype)
//
//        if (gentypeToGatherPropsOf != GENS.COMMON) { // != GENS.COMMON means yes, doGatherPropertysOfSuperclasses
//            var superClass = get(gentypeToGatherPropsOf).extendsObj.superClass
//            if (superClass === EitherTypeOrModelGenRef.NOTHING) superClass = extendsObj.superClass
//
//            while (superClass is EitherTypeOrModelGenRef.EitherDslModelGenRef) {
//                val superModel = dslCtx[superClass.modelGenRef]
//                // ==========  RECURSION!!! ============
//                val superModelProps = superModel.allPropertys(gentypeToGatherPropsOf, forceGatherForGentype, propFilter)
//                allProps.addAll(superModelProps)
//                superClass = superModel.get(gentype).extendsObj.superClass
//                if (superClass === EitherTypeOrModelGenRef.NOTHING) superClass = superModel.extendsObj.superClass
//            }
//        }
//        return allProps
//    }
//
//    fun intersectPropertyNames(fromGentype: GENS, toGentype: GENS): Set<Property> {
//        val fromPropertys: MutableSet<Property> = allPropertys(fromGentype).toMutableSet() // copy
//        val toPropertys: MutableSet<Property> =   allPropertys(toGentype)
//
//        return fromPropertys.intersect(toPropertys)
//    }
//
//
//    fun toStringMembers(gentype: GENS): MutableSet<ModelGenPropRef> {
//        return get(gentype).toStringMembersClassProps.toMutableSet().apply { addAll(toStringMembersClassProps) }
//    }
//
//    @NonDslBlock fun dtoType(function: DslDto.() -> Unit) {
//        when (dslCtx.DSLPASS) {
//            DslCtx.DSLPASS.ONE_BASEMODELS -> {
//                dslDtoObj.clausePresent = true
//                dslDtoObj.function()
//            }
//            DslCtx.DSLPASS.TWO_TABLEMODELS -> {
//                // push information up into special models ( dtoType { } )
//                if (dslDtoObj.kind == ClassObjectOrInterface.UNDEFINED) dslDtoObj.kind = kind
//                if (dslDtoObj.kind == ClassObjectOrInterface.UNDEFINED) dslDtoObj.kind = ClassObjectOrInterface.CLASS
//                dslDtoObj.function()
//            }
//            DslCtx.DSLPASS.THREE_ALLMODELS -> {}
//            DslCtx.DSLPASS.FOUR_REFERENCING -> dslDtoObj.function()
//            DslCtx.DSLPASS.FINISH -> dslDtoObj.finish()
//        }
//    }
//    @NonDslBlock fun tableType(of: GENS = GENS.DTO, function: DslTable.() -> Unit) {
//        when (dslCtx.DSLPASS) {
//            DslCtx.DSLPASS.ONE_BASEMODELS -> dslTableObj.clausePresent = true
//            DslCtx.DSLPASS.TWO_TABLEMODELS -> return
//            DslCtx.DSLPASS.THREE_ALLMODELS -> {}
//            DslCtx.DSLPASS.FOUR_REFERENCING -> {
//                dslTableObj.extendsObj.replaceSuperclass = true
//                dslTableObj.function()
//                if (dslTableObj.kind == ClassObjectOrInterface.CLASS || dslTableObj.kind == ClassObjectOrInterface.UNDEFINED) dslTableObj.kind = kind
//                if (dslTableObj.kind == ClassObjectOrInterface.CLASS || dslTableObj.kind == ClassObjectOrInterface.UNDEFINED) ClassObjectOrInterface.OBJECT
//                // if tableType has a property "uuid" with Tag.PRIMARY, tableType superclass will be replaced with UuidTable in codeGen
//                dslTableObj.extendsObj.superClass = EitherTypeOrModelGenRef.EitherKClass(ClassName("org.jetbrains.exposed.sql", "TableType"), false)
//                dslTableObj.extendsObj.constructorParams("%S", dslCtx[modelGenRef].tableName(GENS.TABLE))
//                translateToDbProperties()
//            }
//            DslCtx.DSLPASS.FINISH -> return
//        }
//    }
//    private fun translateToDbProperties() {
//        val allGatheredPropertys = allPropertys(GENS.TABLE)
//        val alreadyTableObjects = dslTableObj.itsPropertys().toSet()
//        allGatheredPropertys.removeAll(alreadyTableObjects) // these already have been created in tableType { } clause
//        dslTableObj.addAllPropertys(allGatheredPropertys.map {
//            it.createDbProperty(GENS.TABLE, modelGenRef.modelRef)
//        })
//    }
//
//    @NonDslBlock fun filler(dslFillerBlock: DslFiller.() -> Unit) {
//        when (dslCtx.DSLPASS) {
//            DslCtx.DSLPASS.ONE_BASEMODELS -> dslFillerObj.dslFillerBlock()
//            DslCtx.DSLPASS.TWO_TABLEMODELS -> return
//            DslCtx.DSLPASS.THREE_ALLMODELS -> {}
//            DslCtx.DSLPASS.FOUR_REFERENCING ->  return
//            DslCtx.DSLPASS.FINISH -> dslFillerObj.finish()
//        }
//    }
//
//    /** *******************************************************************************
//     **********************************************************************************
//     * ********       Big final copying of DslModel to EitherModelNew      ************
//     **********************************************************************************
//     ******************************************************************************* */
//    fun finish(gentype: GENS) {
//        if ( gentype != GENS.FILLER && this[gentype].noClause) return
//        log.info("${this::class.simpleName}.finish($gentype) of $this")
//        val targetModelGenRef = modelGenRef.copy(gentype = gentype)
//        val modelElement: EitherModelNew = when (gentype) {
//            GENS.UNDEF -> TODO()
//            GENS.COMMON -> TODO()
//            GENS.DTO -> EitherModelNew.DtoModel(targetModelGenRef, this).also { Models.dtoModels[targetModelGenRef] = it }
//            GENS.TABLE -> EitherModelNew.TableModel(targetModelGenRef, this).also { Models.tableModels[targetModelGenRef] = it }
//            GENS.FILLER -> {
//                val fillers = dslFillerObj.fillerDatas.values.map {
//                    Filler(Models[it.toModelGenRef], Models[it.fromModelGenRef], it.copyBoundrys)
//                }.toMutableSet()
//                val fillerModel = FillerModel(targetModelGenRef, this, fillers)
//                Models.fillers[dslFillerObj.modelGenRef] = fillerModel
//                return
//            }
//        }
//
//        modelElement.kind = when (this.theKind(gentype)) {
//            ClassObjectOrInterface.UNDEFINED -> throw DslException("${this[gentype]} kind UNDEFINED")
//            ClassObjectOrInterface.CLASS -> TypeSpec.Kind.CLASS
//            ClassObjectOrInterface.OBJECT -> TypeSpec.Kind.OBJECT
//            ClassObjectOrInterface.INTERFACE -> TypeSpec.Kind.INTERFACE
//        }
//
//        modelElement.modelNameString = this.modelGenRef.modelNameString
//
////        val theSpecialSubPackage = this[gentype].subPackage
////        var thePackageName = dslClassName.fullStringPackage(dslCtx.basePackage, dslCtx.argsSubpackages[gentype], this.modelgroup.subPackage, this.subPackage, theSpecialSubPackage)
////        if (thePackageName.isBlank()) thePackageName = "generated"
//
//        modelElement.classModifiers.addAll(this[gentype].classModifiers)
//        modelElement.classModifiers.addAll(this.classModifiers)
//
//        modelElement.typeWrapper = dslClassName.typeWrapper(gentype)
//
//        modelElement.constructorVisibility = this[gentype].constructorVisibility
//
//        var gatherPropertiesFromGENS = get(gentype).gatherPropertysOfSuperclasses
//        if (gatherPropertiesFromGENS == GENS.COMMON) gatherPropertiesFromGENS = gatherPropertysOfSuperclasses
//
//        val propertysToCopyOver = if (gentype == GENS.TABLE) {
//            this.allPropertys(gentype) { Tag.TRANSIENT !in it.tags }
//        } else {
//            this.allPropertys(gentype)
//        }
//        modelElement.propertys.putAll(propertysToCopyOver.map {
//            it.nameInDsl to it.apply {
//                it.eitherTypModelOrClass.gentype = gentype
//                it.modelGenRefPropIsIn = it.modelGenRefPropIsIn.copy(gentype = gentype)
//            }
//        })
//        val propertysGatheredToCopyOver = if (gentype == GENS.TABLE) {
//            this.allPropertys(gentype, gatherPropertiesFromGENS) { Tag.TRANSIENT !in it.tags }
//        } else {
//            this.allPropertys(gentype, if (gatherPropertiesFromGENS == GENS.COMMON) gentype else gatherPropertiesFromGENS )
//        }
//        modelElement.gatheredPropertys.putAll(propertysGatheredToCopyOver.map { it.nameInDsl to it })
//        // each prop that is a ModelProperty its EitherModelNew reference will be updated to the correct and filled out dslModel in App
//
//        modelElement.businessInitializers.putAll(this.defaultBusinessValues.defaultBusinessValuesMap)
//        modelElement.businessInitializers.putAll(this[gentype].defaultBusinessValues.defaultBusinessValuesMap)
//        modelElement.didGatherPropsFromSuperclasses =
//            if (this[gentype].gatherPropertysOfSuperclasses != GENS.COMMON) this[gentype].gatherPropertysOfSuperclasses else this.gatherPropertysOfSuperclasses
//
//        modelElement.modelFunSpecs.putAll(this.modelClassPropsFunSpecs)
//        modelElement.modelFunSpecs.putAll(this[gentype].modelClassPropsFunSpecs)
//
//        var superClass = get(gentype).extendsObj.superClass
//        var superClassInitializer = get(gentype).extendsObj.superClassConstructorFormat
//        if (superClass === EitherTypeOrModelGenRef.NOTHING) {
//            // so we take the superClass of the common modelElement
//            superClass = extendsObj.superClass
//            superClassInitializer = extendsObj.superClassConstructorFormat
//        }
//        modelElement.eitherExtendsModelOrClass = when (superClass) {
//            is EitherTypeOrModelGenRef.EitherDslModelGenRef -> {
//                if (superClass.modelGenRef.gentype == GENS.COMMON) {
//                    EitherExtendsModelOrClass.createExtendsModel(superClass.modelGenRef.copy(gentype = gentype), superClassInitializer)
//                } else {
//                    EitherExtendsModelOrClass.createExtendsModel(superClass.modelGenRef, superClassInitializer)
//                }
//            }
//            is EitherTypeOrModelGenRef.EitherKClass -> EitherExtendsModelOrClass.createExtendsClass(superClass.typeName, superClassInitializer)
//            is EitherTypeOrModelGenRef.ExtendsNothing -> ExtendsNothing.INSTANCE
//        }
//        // interfaces
//        modelElement.superInterfaces.addAll(
//            get(gentype).extendsObj.superInterfaces.map {
//                when (it) {
//                    is EitherTypeOrModelGenRef.EitherDslModelGenRef -> {
//                        if (it.gentype == GENS.COMMON) {
//                            EitherExtendsModelOrClass.createExtendsModel(it.modelGenRef.copy(gentype = gentype))
//                        } else {
//                            EitherExtendsModelOrClass.createExtendsModel(it.modelGenRef)
//                        }
//                    }
//                    is EitherTypeOrModelGenRef.EitherKClass -> EitherExtendsModelOrClass.createExtendsClass(it.typeName)
//                    is EitherTypeOrModelGenRef.ExtendsNothing -> throw DslException("finish() userInterfaces Contains ExtendsNothing for $modelGenRef")
//                }
//            }
//        )
//        if ( ! get(gentype).extendsObj.replaceSuperInterfaces) {
//            modelElement.superInterfaces.addAll(
//                extendsObj.superInterfaces.map {
//                    when (it) {
//                        is EitherTypeOrModelGenRef.EitherDslModelGenRef -> {
//                            if (it.gentype == GENS.COMMON) {
//                                EitherExtendsModelOrClass.createExtendsModel(it.modelGenRef.copy(gentype = gentype))
//                            } else {
//                                EitherExtendsModelOrClass.createExtendsModel(it.modelGenRef)
//                            }
//                        }
//                        is EitherTypeOrModelGenRef.EitherKClass -> EitherExtendsModelOrClass.createExtendsClass(it.typeName)
//                        is EitherTypeOrModelGenRef.ExtendsNothing -> throw DslException("finish() userInterfaces Contains ExtendsNothing for $modelGenRef")
//                    }
//                }
//            )
//        }
//
//        if (dslCtx.dslErrors.isNotEmpty()) {
//            dslCtx.dslErrors.forEachIndexed { i, errorMsg ->
//                log.error(String.format("%3d. %s", i+1, "\"$errorMsg\""))
//            }
//            throw DslException("${DslModel::class.simpleName}.finish() ${dslCtx.dslErrors.size} Error(s) (see above)")
//        }
//    }
//
//
//
//
//// TODO put Format and Initializer somewhere else
//class Format(val format: String, val args: Array<out Any> = arrayOf()) {
//    override fun toString() = "\"$format\"->${args.joinToString()}"
//    fun isEmpty() = format.isBlank()
//    companion object {
//        val EMPTY = Format("")
//        fun of(format: String, vararg args: Any) = Format(format, args)
//    }
//
//    fun copy(format: String = this.format, args: Array<out Any> = this.args) = Format(format, args)
//}
//class Initializer(val format: String, var args: Array<out Any> = arrayOf()) {
//    override fun toString() = "\"$format\"->${args.joinToString()}"
//    fun isEmpty() = format.isBlank()
//    fun isNotEmpty() = !format.isBlank()
//    companion object {
//        val EMPTY = Initializer("")
//        fun of(format: String, vararg args: Any) = Initializer(format, args)
//        fun of(format: String, args: List<Any>) = Initializer(format, args.toTypedArray())
//    }
//    fun copy(format: String = this.format, args: Array<out Any> = this.args): Initializer = Initializer(format, args)
//}
//
////@DslModel class Common(modelGenRef: ModelGenRef, modelgroup: DslModelgroup) : DslClass(modelGenRef) {
////    override fun toString(): String = "${Common::class.simpleName}[${modelGenRef}]"
////}
//
//interface IModelForGentype {
//    fun finish()
//}
//
//@DslModel class DslDto(modelGenRef: ModelGenRef) : DslClass(modelGenRef), IModelForGentype {
//    override fun toString(): String = "${DslDto::class.simpleName}[${modelGenRef}]"
//    override fun finish() {
//        TODO("Not yet implemented")
//    }
//}
//
//@DslModel class DslTable(modelGenRef: ModelGenRef) : DslClass(modelGenRef), IModelForGentype {
//    override fun toString(): String = "${DslTable::class.simpleName}[${modelGenRef}]"
//    override fun finish() {
//        TODO("Not yet implemented")
//    }
//}
//data class CopyBoundry(val modelGenPropRef: ModelGenPropRef, val copyType: COPYTYPE) {
//    infix fun propRef(propName: String): CopyBoundry {
//        return this.copy(modelGenPropRef = this.modelGenPropRef.copy(propName = propName))
//    }
//
//    infix fun forPropRef(eitherRefString: String): CopyBoundry {
//        val either = eitherRefString.splitRef()
//        return when (either) {
//            is EitherModelRefModelGenRefOrModelGenPropRef.EitherModelGenPropRef -> this.copy(modelGenPropRef = either.modelGenPropRef)
//            is EitherModelRefModelGenRefOrModelGenPropRef.EitherModelGenRef -> this.copy(modelGenPropRef = ModelGenPropRef(either.modelGenRef.modelRef, either.modelGenRef.gentype, "DoesNotExist"))
//            is EitherModelRefModelGenRefOrModelGenPropRef.EitherModelRef -> this.copy(modelGenPropRef = ModelGenPropRef(either.modelRef, "DoesNotExist"))
//            is EitherModelRefModelGenRefOrModelGenPropRef.EitherSimple -> throw DslException("\"$eitherRefString\" is no dslModel(Gen)Ref")
//        }
//    }
//
//    companion object {
//        val NONE = CopyBoundry(ModelGenPropRef.NOTHING, COPYTYPE.DEEP)
//        infix fun of(copytype: COPYTYPE): CopyBoundry = NONE.copy(copyType = copytype)
//    }
//}
//
//enum class COPYTYPE {IGNORE, INSTANCE, NEW, DEEP, DEEPNEW }
//enum class BUSINESSINIT { NULL, NEW, INIT }
//
//@DslModel class DslFiller(val modelGenRef: ModelGenRef) {
//    lateinit var dslModel: DslModel
//    override fun toString(): String = "${DslFiller::class.simpleName}[${modelGenRef}]"
//    class FillerData(val toModelGenRef: ModelGenRef, val fromModelGenRef: ModelGenRef, val copyBoundrys: MutableSet<CopyBoundry> = mutableSetOf()) {
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is FillerData) return false
//            if (toModelGenRef != other.toModelGenRef) return false
//            if (fromModelGenRef != other.fromModelGenRef) return false
//            return true
//        }
//        override fun hashCode(): Int {
//            var result = toModelGenRef.hashCode()
//            result = 31 * result + fromModelGenRef.hashCode()
//            return result
//        }
//    }
//    data class FillerToFrom(val toModelGenRef: ModelGenRef, val fromModelGenRef: ModelGenRef)
//    val fillerDatas = mutableMapOf<FillerToFrom, FillerData>()
//
//    private fun addFillerModel(refs: ModelGenRefPair, mutual: Boolean = false, copyBoundrys: Array<out CopyBoundry> = arrayOf()) {
//        if (refs.fromModelGenRef == modelGenRef && refs.toModelGenRef == modelGenRef) throw DslException("mutual self filler is default and shouldn't be set explicityly")
//        if (refs.fromModelGenRef.modelRef != modelGenRef.modelRef && refs.toModelGenRef.modelRef != modelGenRef.modelRef) throw DslException("filler in $modelGenRef mutual neither from nor to is of ${modelGenRef.modelRef}")
//        if (refs.fromModelGenRef.gentype == GENS.FILLER || refs.toModelGenRef.gentype == GENS.FILLER) throw DslException("fillers from/to fillers are not allowed")
//        when (refs.fromModelGenRef.gentype) {
//            GENS.UNDEF -> throw DslException("fillers UNDEF not allowed")
//            GENS.COMMON -> throw DslException("fillers COMMON not allowed")
//            GENS.FILLER -> throw DslException("fillers FILLER not allowed")
//            GENS.DTO -> {}
//            GENS.TABLE -> {}
//        }
//        when (refs.toModelGenRef.gentype) {
//            GENS.UNDEF -> throw DslException("fillers UNDEF not allowed")
//            GENS.COMMON -> throw DslException("fillers COMMON not allowed")
//            GENS.FILLER -> throw DslException("fillers FILLER not allowed")
//            GENS.DTO -> {}
//            GENS.TABLE -> {}
//        }
//        fillerDatas[FillerToFrom(refs.toModelGenRef, refs.fromModelGenRef)] = FillerData(refs.toModelGenRef, refs.fromModelGenRef, copyBoundrys.toHashSet())
//        if (mutual) {
//            fillerDatas[FillerToFrom(refs.fromModelGenRef, refs.toModelGenRef)] = FillerData(refs.fromModelGenRef, refs.toModelGenRef, copyBoundrys.toHashSet())
//        }
//    }
//    @NonDslBlock operator fun GENS.unaryPlus(): CopyBoundry {
//        val copyBoundrys = arrayOf<CopyBoundry>()
//        addFillerModel(ModelGenRefPair(modelGenRef.copy(gentype = this), modelGenRef.copy(gentype = this)), false, copyBoundrys)
//        return CopyBoundry.NONE
//    }
//
//    @NonDslBlock fun fill(gentype: GENS, vararg copyBoundrys: CopyBoundry) =
//        addFillerModel(ModelGenRefPair(modelGenRef.copy(gentype = gentype), modelGenRef.copy(gentype = gentype)), false, copyBoundrys)
//    @NonDslBlock fun fill(refs: GensGensPair, vararg copyBoundrys: CopyBoundry) =
//        addFillerModel(ModelGenRefPair(modelGenRef.copy(gentype = refs.toGentype), modelGenRef.copy(gentype = refs.fromGentype)), refs.mutual, copyBoundrys)
//    @NonDslBlock fun fill(refs: GensStringPair, vararg copyBoundrys: CopyBoundry) =
//        addFillerModel(ModelGenRefPair(modelGenRef.copy(gentype = refs.toGentype), refs.fromModelGenRefString.modelGenRef()), refs.mutual, copyBoundrys)
//
//    fun finish() { }
//}
//
//@DslModel class ManyToMany(val dslModel: DslModel, val isMutable: Mutable, val collectionType: COLLECTIONTYPE = COLLECTIONTYPE.LIST) {
//
//}
//
//// TODO put somewhere else from here
//// ========================================================================================================================================
//
//data class ModelgroupName(val string: String) { override fun toString() = string
//
//    companion object {
//        val NOTHING = ModelgroupName("NOTHING")
//    }
//}
///** nameDiscriminator if two models have the same name but the eventual class names might differ
// * (e.g. "BaseClass", "IBaseClass", "ABaseClass" as normal class, Interface, Abstract class)
// * Code generation should only use the string property and IGNORE the toString() method
// */
//data class ModelName(val string: String, val nameDiscriminator: String = "") {
//    override fun toString() = if (nameDiscriminator.isBlank()) string else "${string}|${nameDiscriminator}"
//    companion object {
//        val NOTHING = ModelName("NOTHING")
//    }
//}
//@JvmInline value class Mutable(val bool: Boolean)
//val immutable = Mutable(false)
//val mutable = Mutable(true)
//
//data class ModelRef(val modelgroupName: ModelgroupName, val modelName: ModelName) {
//    val modelNameString = modelName.string
//    val modelgroupNameString = modelgroupName.string
//    val modelgroupDotModelString = "${modelgroupName.string}.${modelName.string}"
//
//    constructor(modelgroupNameString: String, modelNameString: String) : this(ModelgroupName(modelgroupNameString), ModelName(modelNameString))
//    constructor(modelgroupName: ModelgroupName, modelNameString: String) : this(modelgroupName, ModelName(modelNameString))
//    override fun toString(): String = "${modelgroupName.string}:${modelName.string.ifBlank { "\"\"" }}"
//
//    companion object {
//        val NOTHING = ModelRef(ModelgroupName.NOTHING, ModelName.NOTHING)
//        fun from(modelgroupNameString: String, modelNameString: String) = ModelRef(ModelgroupName(modelgroupNameString), ModelName(modelNameString))
//        fun from(modelgroupName: ModelgroupName, modelNameString: String) = ModelRef(modelgroupName, ModelName(modelNameString))
//        fun from(modelgroupName: ModelgroupName, modelName: ModelName) = ModelRef(modelgroupName, modelName)
//        fun from(compoundString: String): ModelRef {
//            val splitted = compoundString.split('.')
//            assert(splitted.size >= 2)
//            val modelgroupNameString = ModelgroupName(splitted[0])
//            val modelNameString = ModelName(splitted[1])
//            return ModelRef(modelgroupNameString, modelNameString)
//        }
//    }
//}
//data class ModelGenRef(val modelRef: ModelRef, val gentype: GENS) {
//    val modelgroupName = modelRef.modelgroupName
//    val modelNameString = modelRef.modelNameString
//    val modelgroupNameString = modelRef.modelgroupNameString
//    val modelgroupDotModelString = modelRef.modelgroupDotModelString
//    constructor(modelgroupNameString: String, modelNameString: String, gentype: GENS) : this(ModelRef(modelgroupNameString, modelNameString), gentype)
//    constructor(modelgroupName: ModelgroupName, modelNameString: String, gentype: GENS) : this(ModelRef(modelgroupName, modelNameString), gentype)
//    override fun toString(): String = "${gentype}|${modelRef}"
//    companion object {
//        val NOTHING = ModelGenRef(ModelRef.NOTHING, GENS.UNDEF)
//        fun from(modelgroupNameString: String, modelNameString: String, gentype: GENS = GENS.COMMON) = ModelGenRef(ModelRef(modelgroupNameString, modelNameString), gentype)
//        fun from(modelgroupName: ModelgroupName, modelNameString: String, gentype: GENS = GENS.COMMON) = ModelGenRef(ModelRef(modelgroupName, ModelName(modelNameString)), gentype)
//        fun from(modelgroupName: ModelgroupName, modelName: ModelName, gentype: GENS = GENS.COMMON) = ModelGenRef(ModelRef(modelgroupName, modelName), gentype)
//        fun from(compoundString: String): ModelGenRef {
//            val parts = compoundString.split('.')
//            assert(parts.size >= 3)
//            val modelgroupNameString = parts[0]
//            val modelNameString = parts[1]
//            val gentype = GENS.valueOf(parts[2])
//            return ModelGenRef(modelgroupNameString, modelNameString, gentype)
//        }
//    }
//}
//data class ModelGenFunRef(val modelRef: ModelRef, val gentype: GENS = GENS.COMMON, val funName: String) {
//    constructor(modelRef: ModelRef, funName: String): this(modelRef, GENS.COMMON, funName)
//    constructor(modelGenRef: ModelGenRef, funName: String): this(modelGenRef.modelRef, modelGenRef.gentype, funName)
//}
//data class ModelGenPropRef(val modelRef: ModelRef, val gentype: GENS = GENS.COMMON, val propName: String) {
//    constructor(modelRef: ModelRef, propName: String): this(modelRef, GENS.COMMON, propName)
//    constructor(modelGenRef: ModelGenRef, propName: String): this(modelGenRef.modelRef, modelGenRef.gentype, propName)
//    companion object {
//        val NOTHING = ModelGenPropRef(ModelRef.NOTHING, "NOTHING")
//        fun from(modelgroupNameString: String, modelNameString: String, propName: String): ModelGenPropRef = ModelGenPropRef(ModelGenRef.from(modelgroupNameString, modelNameString), propName)
//        fun from(modelgroupNameString: String, modelNameString: String, gentype: GENS, propName: String): ModelGenPropRef = ModelGenPropRef(ModelGenRef.from(modelgroupNameString, modelNameString, gentype), propName)
//    }
//}
