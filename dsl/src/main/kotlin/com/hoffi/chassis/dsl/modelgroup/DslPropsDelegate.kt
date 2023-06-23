package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.*
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.Tags
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import org.slf4j.LoggerFactory

class DslModelProp(
    val name: String,
    val propRef: DslRef.prop,
    var eitherTypModelOrClass: EitherTypOrModelOrPoetType,
    var mutable: Mutable = Mutable(false),
    val tags: Tags = Tags.NONE,
    var length: Int = C.DEFAULT_INT,
    val collectionType: COLLECTIONTYPE = COLLECTIONTYPE.NONE
) {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun toString(): String =
        "${DslModelProp::class.simpleName}[${if (tags.contains(Tag.CONSTRUCTOR)) "C " else "  "}${if (mutable.bool) "var " else "fix "}${if (collectionType != COLLECTIONTYPE.NONE) "$collectionType " else ""}${name}/${eitherTypModelOrClass}})${if (tags.isNotEmpty()) ", tags:$tags" else ""}] OF ${propRef}"
}
context(DslCtxWrapper)
class DslPropsDelegate(
    simpleNameOfParentDslBlock: String,
    parentRef: IDslRef
) : ADslDelegateClass(simpleNameOfParentDslBlock, parentRef), IDslApiPropFuns {
    val log = LoggerFactory.getLogger(javaClass)

    override val selfDslRef = DslRef.showcase(simpleNameOfParentDslBlock, parentRef)

    val theProps = mutableMapOf<String, DslModelProp>()
    fun getPropOrNull(name: String) = theProps[name]
    fun addProp(name: String, prop: DslModelProp) = if (!theProps.containsKey(name)) prop.also { theProps[name] = prop } else { throw DslException("$parentRef already has a property $name") }

    // ===================================================================================================================================
    // ====================   "primitive" TYP properties   ===============================================================================
    // ===================================================================================================================================
    override fun property(
        name: String,
        typ: TYP, // in interface = TYP.STRING,
        mutable: Mutable, // in interface = immutable(),
        collectionType: COLLECTIONTYPE, // in interface = COLLECTIONTYPE.NONE,
        initializer: Initializer, // in interface = Initializer.EMPTY,
        modifiers: MutableSet<KModifier>, // in interface = mutableSetOf(),
        length: Int, // in interface = -1,
        tags: Tags, // in interface = Tags.NONE
    ) {
        log.info("fun {}(\"{}, TYP\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }

        if (Tag.NULLABLE in tags) throw DslException("Tag.NULLABLE not allowed for primitive TYP properties in property $name of $parentRef")
        val typProp = EitherTypOrModelOrPoetType.EitherTyp(typ)
        val prop = DslModelProp(name, DslRef.prop(name, parentRef), typProp, mutable, tags, length, collectionType)
        addProp(name, prop)
    }

    // ===================================================================================================================================
    // ====================   "CLASS" propertys   ====================================================================================
    // ===================================================================================================================================
    override fun property(name: String, poetType: TypeName, mutable: Mutable, collectionType: COLLECTIONTYPE, initializer: Initializer, modifiers: MutableSet<KModifier>, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, poetType\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        val prop = DslModelProp(name, DslRef.prop(name, parentRef), EitherTypOrModelOrPoetType.EitherPoetType(poetType), mutable, tags, length, collectionType)
        addProp(name, prop)
    }

    override fun property(name: String, modelRefString: String, modelSubelement: DslRef.model.MODELELEMENT, mutable: Mutable, collectionType: COLLECTIONTYPE, initializer: Initializer, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, modelRefString\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        val modelOrModelSubElementRef = DslRefString.MODELREF(modelRefString)
        if (modelOrModelSubElementRef !is DslRef.model) {
            throw DslException("prop $name of ref: $parentRef does not reference a model  (model() { ... }")
        }
        when (modelSubelement) {
            DslRef.model.MODELELEMENT.MODEL -> throw DslException("should have been catched above")
            DslRef.model.MODELELEMENT.DTO ->   property(name, DslRef.dto(  C.DEFAULT, modelOrModelSubElementRef), mutable, collectionType, initializer, length, tags)
            DslRef.model.MODELELEMENT.TABLE -> property(name, DslRef.table(C.DEFAULT, modelOrModelSubElementRef), mutable, collectionType, initializer, length, tags)
        }
    }

    // ===================================================================================================================================
    // ====================   Model properties   =========================================================================================
    // ===================================================================================================================================

    override fun property(name: String, modelSubElementRefString: String, mutable: Mutable, collectionType: COLLECTIONTYPE, initializer: Initializer, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, modelSubElementRefString\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        val modelOrModelSubElementRef = DslRefString.MODELREF(modelSubElementRefString)
        if (modelOrModelSubElementRef !is DslRef.IModelSubelement) {
            throw DslException("prop $name of ref: $parentRef does not reference a modelSubElement (dto/table/...)")
        }
        property(name, modelOrModelSubElementRef, mutable, collectionType, initializer, length, tags)
    }


    override fun property(name: String, modelSubElementRef: DslRef.IModelSubelement, mutable: Mutable, collectionType: COLLECTIONTYPE, initializer: Initializer, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, DslRef\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }

        if (Tag.NULLABLE in tags) log.warn("Tag.NULLABLE for Class property $name of $parentRef")
        val prop = DslModelProp(name, DslRef.prop(name, parentRef), EitherTypOrModelOrPoetType.EitherModel(modelSubElementRef), mutable, tags, length, collectionType)
        addProp(name, prop)
    }
}
