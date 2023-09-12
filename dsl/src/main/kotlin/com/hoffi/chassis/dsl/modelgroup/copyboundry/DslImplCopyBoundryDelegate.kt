package com.hoffi.chassis.dsl.modelgroup.copyboundry

import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.shared.dsl.DslRef
import com.hoffi.chassis.shared.shared.AHasCopyBoundrysData
import com.hoffi.chassis.shared.shared.COPYTYPE
import com.squareup.kotlinpoet.ClassName
import org.slf4j.LoggerFactory

interface IDslApiPrefixedCopyBoundry {
    infix   fun <E : AHasCopyBoundrysData>  List<E>.shallowRestrictions(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit)
    infix   fun <E : AHasCopyBoundrysData>  List<E>.deepRestrictions(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit)
            fun <E : AHasCopyBoundrysData>  FOR(vararg aHasCopyBoundryDataList: List<E>): List<E>
}
interface IDslApiCopyBoundry {
    fun copyBoundry(copyType: COPYTYPE, vararg propName: String): AHasCopyBoundrysData

    infix fun COPYTYPE.propName(propName: String): AHasCopyBoundrysData
    infix fun COPYTYPE.propRef(propRef: DslRef.prop): AHasCopyBoundrysData
    infix fun COPYTYPE.model(modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData
    infix fun COPYTYPE.className(className: ClassName): AHasCopyBoundrysData
    fun IGNORE(vararg propName: String): AHasCopyBoundrysData
    fun IGNORE(vararg propRef: DslRef.prop): AHasCopyBoundrysData
    fun IGNORE(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData
    fun IGNORE(vararg className: ClassName): AHasCopyBoundrysData
    fun INSTANCE(vararg propName: String): AHasCopyBoundrysData
    fun INSTANCE(vararg propRef: DslRef.prop): AHasCopyBoundrysData
    fun INSTANCE(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData
    fun INSTANCE(vararg className: ClassName): AHasCopyBoundrysData
    fun NEW(vararg propName: String): AHasCopyBoundrysData
    fun NEW(vararg propRef: DslRef.prop): AHasCopyBoundrysData
    fun NEW(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData
    fun NEW(vararg className: ClassName): AHasCopyBoundrysData
    fun DEEP(vararg propName: String): AHasCopyBoundrysData
    fun DEEP(vararg propRef: DslRef.prop): AHasCopyBoundrysData
    fun DEEP(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData
    fun DEEP(vararg className: ClassName): AHasCopyBoundrysData
    fun DEEPNEW(vararg propName: String): AHasCopyBoundrysData
    fun DEEPNEW(vararg propRef: DslRef.prop): AHasCopyBoundrysData
    fun DEEPNEW(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData
    fun DEEPNEW(vararg className: ClassName): AHasCopyBoundrysData
    infix fun AHasCopyBoundrysData.onlyIf(collectiontyp: COLLECTIONTYP): AHasCopyBoundrysData
}

class DslImplCopyBoundryOn(val shallowFalseDeepTrue: Boolean, val aHasCopyBoundrysData: AHasCopyBoundrysData)
    : IDslApiCopyBoundry
{
    val log = LoggerFactory.getLogger(javaClass)
    override fun copyBoundry(copyType: COPYTYPE, vararg propName: String) =                             aHasCopyBoundrysData.addPropNameCopyBoundry(copyType, shallowFalseDeepTrue, *propName)
    override infix fun COPYTYPE.propName(propName: String): AHasCopyBoundrysData =                      aHasCopyBoundrysData.addPropNameCopyBoundry(this, shallowFalseDeepTrue, propName)
    override infix fun COPYTYPE.propRef(propRef: DslRef.prop): AHasCopyBoundrysData =                   aHasCopyBoundrysData.addPropRefCopyBoundry(this, shallowFalseDeepTrue, propRef)
    override infix fun COPYTYPE.model(modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData = aHasCopyBoundrysData.addModelRefCopyBoundry(this, shallowFalseDeepTrue, modelRef)
    override infix fun COPYTYPE.className(className: ClassName): AHasCopyBoundrysData =                 aHasCopyBoundrysData.addClassNameCopyBoundry(this, shallowFalseDeepTrue, className)
    override fun IGNORE(vararg propName: String): AHasCopyBoundrysData =                                aHasCopyBoundrysData.addPropNameCopyBoundry(COPYTYPE.IGNORE, shallowFalseDeepTrue, *propName)
    override fun IGNORE(vararg propRef: DslRef.prop): AHasCopyBoundrysData =                            aHasCopyBoundrysData.addPropRefCopyBoundry(COPYTYPE.IGNORE, shallowFalseDeepTrue, *propRef)
    override fun IGNORE(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData =        aHasCopyBoundrysData.addModelRefCopyBoundry(COPYTYPE.IGNORE, shallowFalseDeepTrue, *modelRef)
    override fun IGNORE(vararg className: ClassName): AHasCopyBoundrysData =                            aHasCopyBoundrysData.addClassNameCopyBoundry(COPYTYPE.IGNORE, shallowFalseDeepTrue, *className)
    override fun INSTANCE(vararg propName: String): AHasCopyBoundrysData =                              aHasCopyBoundrysData.addPropNameCopyBoundry(COPYTYPE.INSTANCE, shallowFalseDeepTrue, *propName)
    override fun INSTANCE(vararg propRef: DslRef.prop): AHasCopyBoundrysData =                          aHasCopyBoundrysData.addPropRefCopyBoundry(COPYTYPE.INSTANCE, shallowFalseDeepTrue, *propRef)
    override fun INSTANCE(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData =      aHasCopyBoundrysData.addModelRefCopyBoundry(COPYTYPE.INSTANCE, shallowFalseDeepTrue, *modelRef)
    override fun INSTANCE(vararg className: ClassName): AHasCopyBoundrysData =                          aHasCopyBoundrysData.addClassNameCopyBoundry(COPYTYPE.INSTANCE, shallowFalseDeepTrue, *className)
    override fun NEW(vararg propName: String): AHasCopyBoundrysData =                                   aHasCopyBoundrysData.addPropNameCopyBoundry(COPYTYPE.NEW, shallowFalseDeepTrue, *propName)
    override fun NEW(vararg propRef: DslRef.prop): AHasCopyBoundrysData  =                              aHasCopyBoundrysData.addPropRefCopyBoundry(COPYTYPE.NEW, shallowFalseDeepTrue, *propRef)
    override fun NEW(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData  =          aHasCopyBoundrysData.addModelRefCopyBoundry(COPYTYPE.NEW, shallowFalseDeepTrue, *modelRef)
    override fun NEW(vararg className: ClassName): AHasCopyBoundrysData  =                              aHasCopyBoundrysData.addClassNameCopyBoundry(COPYTYPE.NEW, shallowFalseDeepTrue, *className)
    override fun DEEP(vararg propName: String): AHasCopyBoundrysData =                                  aHasCopyBoundrysData.addPropNameCopyBoundry(COPYTYPE.DEEP, shallowFalseDeepTrue, *propName)
    override fun DEEP(vararg propRef: DslRef.prop): AHasCopyBoundrysData =                              aHasCopyBoundrysData.addPropRefCopyBoundry(COPYTYPE.DEEP, shallowFalseDeepTrue, *propRef)
    override fun DEEP(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData =          aHasCopyBoundrysData.addModelRefCopyBoundry(COPYTYPE.DEEP, shallowFalseDeepTrue, *modelRef)
    override fun DEEP(vararg className: ClassName): AHasCopyBoundrysData =                              aHasCopyBoundrysData.addClassNameCopyBoundry(COPYTYPE.DEEP, shallowFalseDeepTrue, *className)
    override fun DEEPNEW(vararg propName: String): AHasCopyBoundrysData =                               aHasCopyBoundrysData.addPropNameCopyBoundry(COPYTYPE.DEEPNEW, shallowFalseDeepTrue, *propName)
    override fun DEEPNEW(vararg propRef: DslRef.prop): AHasCopyBoundrysData =                           aHasCopyBoundrysData.addPropRefCopyBoundry(COPYTYPE.DEEPNEW, shallowFalseDeepTrue, *propRef)
    override fun DEEPNEW(vararg modelRef: DslRef.IModelOrModelSubelement): AHasCopyBoundrysData =       aHasCopyBoundrysData.addModelRefCopyBoundry(COPYTYPE.DEEPNEW, shallowFalseDeepTrue, *modelRef)
    override fun DEEPNEW(vararg className: ClassName): AHasCopyBoundrysData =                           aHasCopyBoundrysData.addClassNameCopyBoundry(COPYTYPE.DEEPNEW, shallowFalseDeepTrue, *className)

    override fun AHasCopyBoundrysData.onlyIf(collectiontyp: COLLECTIONTYP): AHasCopyBoundrysData {
        log.warn("${this::class.simpleName}.AHasCopyBoundrysData.onlyIf() Not yet implemented") // TODO implementMe
        return this
    }
}
