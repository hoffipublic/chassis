package com.hoffi.chassis.dsl.modelgroup

import com.hoffi.chassis.chassismodel.C
import com.hoffi.chassis.chassismodel.dsl.DslException
import com.hoffi.chassis.dsl.internal.ADslDelegateClass
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.shared.COLLECTIONTYP
import com.hoffi.chassis.shared.EitherTypOrModelOrPoetType
import com.hoffi.chassis.shared.Mutable
import com.hoffi.chassis.shared.TYP
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.dsl.DslRefString
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.Initializer
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.Tags
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM
import com.hoffi.chassis.shared.whens.WhensDslRef
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import org.slf4j.LoggerFactory

class DslModelProp constructor(
    val name: String,
    val propRef: DslRef.prop,
    var eitherTypModelOrClass: EitherTypOrModelOrPoetType,
    var mutable: Mutable = Mutable(false),
    val modifiers: MutableSet<KModifier> = mutableSetOf(),
    val tags: Tags = Tags.NONE,
    var length: Int = C.DEFAULT_INT,
    val collectionType: COLLECTIONTYP = COLLECTIONTYP.NONE
) {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun toString(): String =
        "${DslModelProp::class.simpleName}[${if (tags.contains(Tag.CONSTRUCTOR)) "C " else "  "}${if (mutable.bool) "var " else "fix "}${if (collectionType != COLLECTIONTYP.NONE) "$collectionType " else ""}${name}/${eitherTypModelOrClass}})${if (tags.isNotEmpty()) ", tags:$tags" else ""}] OF ${propRef}"
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
    fun addProp(name: String, prop: DslModelProp): DslModelProp = if (!theProps.containsKey(name)) prop.also { theProps[name] = prop } else { throw DslException("$parentRef already has a property $name") }

    // ===================================================================================================================================
    // ====================   "primitive" TYP properties   ===============================================================================
    // ===================================================================================================================================
    override fun property(
        name: String,
        typ: TYP, // in interface = TYP.STRING,
        mutable: Mutable, // in interface = immutable(),
        collectionType: COLLECTIONTYP, // in interface = COLLECTIONTYP.NONE,
        initializer: Initializer, // in interface = Initializer.EMPTY,
        modifiers: MutableSet<KModifier>, // in interface = mutableSetOf(),
        length: Int, // in interface = -1,
        tags: Tags, // in interface = Tags.NONE
    ) {
        log.info("fun {}(\"{}, TYP\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }

        if (Tag.NULLABLE in tags) throw DslException("Tag.NULLABLE not allowed for primitive TYP properties in property $name of $parentRef")
        val typProp = EitherTypOrModelOrPoetType.EitherTyp(typ, initializer)
        val prop = DslModelProp(name, DslRef.prop(name, parentRef), typProp, mutable, modifiers, tags, length, collectionType)
        addProp(name, prop)
    }

    // ===================================================================================================================================
    // ====================   "CLASS" propertys   ====================================================================================
    // ===================================================================================================================================
    override fun property(name: String, poetType: TypeName, isInterface: Boolean, mutable: Mutable, collectionType: COLLECTIONTYP, initializer: Initializer, modifiers: MutableSet<KModifier>, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, poetType\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        val prop = DslModelProp(name, DslRef.prop(name, parentRef), EitherTypOrModelOrPoetType.EitherPoetType(poetType as ClassName, isInterface, initializer), mutable, modifiers, tags, length, collectionType)
        addProp(name, prop)
    }

    override fun property(name: String, modelRefString: String, modelSubelement: MODELREFENUM, mutable: Mutable, collectionType: COLLECTIONTYP, initializer: Initializer, modifiers: MutableSet<KModifier>, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, modelRefString\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        val modelOrModelSubElementRef = DslRefString.MODELREF(modelRefString)
        if (modelOrModelSubElementRef !is DslRef.model) {
            throw DslException("prop $name of ref: $parentRef does not reference a model  (model() { ... }")
        }
        when (modelSubelement) {
            MODELREFENUM.MODEL -> throw DslException("should have been catched above")
            MODELREFENUM.DTO ->   property(name, DslRef.dto(  C.DEFAULT, modelOrModelSubElementRef), mutable, collectionType, initializer, modifiers, length, tags)
            MODELREFENUM.TABLE -> property(name, DslRef.table(C.DEFAULT, modelOrModelSubElementRef), mutable, collectionType, initializer, modifiers, length, tags)
        }
    }

    // ===================================================================================================================================
    // ====================   Model properties   =========================================================================================
    // ===================================================================================================================================

    override fun property(name: String, modelSubElementRefString: String, mutable: Mutable, collectionType: COLLECTIONTYP, initializer: Initializer, modifiers: MutableSet<KModifier>, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, modelSubElementRefString\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return // do something only in PASS.ONE_BASEMODELS
        ////if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        val modelOrModelSubElementRef = DslRefString.MODELREF(modelSubElementRefString)
        if (modelOrModelSubElementRef !is DslRef.IModelSubelement) {
            throw DslException("prop $name of ref: $parentRef does not reference a modelSubElement (dto/table/...)")
        }
        property(name, modelOrModelSubElementRef, mutable, collectionType, initializer, modifiers, length, tags)
    }


    override fun property(name: String, modelSubElementRef: IDslRef, mutable: Mutable, collectionType: COLLECTIONTYP, initializer: Initializer, modifiers: MutableSet<KModifier>, length: Int, tags: Tags) {
        log.info("fun {}(\"{}, DslRef\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name.replace("-.*$".toRegex(), ""), name, dslCtx.currentPASS)
        if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        //if (dslCtx.currentPASS != dslCtx.PASS_4_REFERENCING) return // do something only in PASS.ONE_BASEMODELS
        ////if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }
        //if (dslCtx.currentPASS != dslCtx.PASS_1_BASEMODELS) return // do something only in PASS.ONE_BASEMODELS
        ////if (tags.contains(Tag.TO_STRING_MEMBER)) { toStringMembersClassProps.add(ModelGenPropRef(modelGenRef, name)) }

        WhensDslRef.whenModelSubelement(modelSubElementRef, {}, {}, catching = { throw DslException("must be a modelsubelement (dto, table, ...): property '${name}' of $parentRef") })

        if (Tag.NULLABLE in tags) log.warn("Tag.NULLABLE for Class property $name of $parentRef")
        // isInterface of GenModel will be set to correct value in setModelClassNameOfReffedModelProperties()
        val prop = DslModelProp(name, DslRef.prop(name, parentRef), EitherTypOrModelOrPoetType.EitherModel(modelSubElementRef as DslRef.IModelSubelement, true, initializer), mutable, modifiers, tags, length, collectionType)
        addProp(name, prop)
    }
}
