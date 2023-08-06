package com.hoffi.chassis.dsl.modelgroup.copyboundry

import com.hoffi.chassis.chassismodel.typ.COLLECTIONTYP
import com.hoffi.chassis.shared.dsl.IDslRef
import com.hoffi.chassis.shared.shared.AHasCopyBoundrysData
import com.hoffi.chassis.shared.shared.COPYTYPE
import com.squareup.kotlinpoet.ClassName
import org.slf4j.LoggerFactory

interface IDslApiPrefixedCopyBoundry {
    infix   fun <E : AHasCopyBoundrysData>  List<E>.shallowRestrictions(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit)
    infix   fun <E : AHasCopyBoundrysData>  List<E>.deepRestrictions(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit)
            fun <E : AHasCopyBoundrysData>  FOR(vararg aHasCopyBoundryDataList: List<E>): List<E>

    fun <E : AHasCopyBoundrysData> List<E>.prefixed(dslApiCopyBoundryBlock: IDslApiCopyBoundry.() -> Unit) {
        for (hasCopyBoundryData in this) {
            DslImplCopyBoundryOn(hasCopyBoundryData).apply(dslApiCopyBoundryBlock)
        }
    }
}
interface IDslApiCopyBoundry {
    fun copyBoundry(copyType: COPYTYPE, vararg propName: String): AHasCopyBoundrysData

    // TODO copyBoundry for DslRefs and MODELREFENUMs
    infix fun COPYTYPE.propName(propName: String): AHasCopyBoundrysData
    infix fun COPYTYPE.model(modelRef: IDslRef): AHasCopyBoundrysData
    fun IGNORE(vararg propName: String): AHasCopyBoundrysData
    fun IGNORE(vararg dslRef: IDslRef): AHasCopyBoundrysData
    fun IGNORE(vararg className: ClassName): AHasCopyBoundrysData
    fun INSTANCE(vararg propName: String): AHasCopyBoundrysData
    fun INSTANCE(vararg dslRef: IDslRef): AHasCopyBoundrysData
    fun INSTANCE(vararg className: ClassName): AHasCopyBoundrysData
    fun NEW(vararg propName: String): AHasCopyBoundrysData
    fun NEW(vararg dslRef: IDslRef): AHasCopyBoundrysData
    fun NEW(vararg className: ClassName): AHasCopyBoundrysData
    fun DEEP(vararg propName: String): AHasCopyBoundrysData
    fun DEEP(vararg dslRef: IDslRef): AHasCopyBoundrysData
    fun DEEP(vararg className: ClassName): AHasCopyBoundrysData
    fun DEEPNEW(vararg propName: String): AHasCopyBoundrysData
    fun DEEPNEW(vararg dslRef: IDslRef): AHasCopyBoundrysData
    fun DEEPNEW(vararg className: ClassName): AHasCopyBoundrysData
    infix fun AHasCopyBoundrysData.onlyIf(collectiontyp: COLLECTIONTYP): AHasCopyBoundrysData
}

class DslImplCopyBoundryOn(val aHasCopyBoundrysData: AHasCopyBoundrysData)
    : IDslApiCopyBoundry
{
    val log = LoggerFactory.getLogger(javaClass)
    override fun copyBoundry(copyType: COPYTYPE, vararg propName: String) =    aHasCopyBoundrysData.add(copyType, *propName)
    override fun COPYTYPE.propName(propName: String): AHasCopyBoundrysData =   aHasCopyBoundrysData.add(this, propName)
    override fun COPYTYPE.model(modelRef: IDslRef): AHasCopyBoundrysData =     aHasCopyBoundrysData.add(this, modelRef)
    override fun IGNORE(vararg propName: String): AHasCopyBoundrysData =       aHasCopyBoundrysData.add(COPYTYPE.IGNORE, *propName)
    override fun IGNORE(vararg dslRef: IDslRef): AHasCopyBoundrysData =        aHasCopyBoundrysData.add(COPYTYPE.IGNORE, *dslRef)
    override fun IGNORE(vararg className: ClassName): AHasCopyBoundrysData =   aHasCopyBoundrysData.add(COPYTYPE.IGNORE, *className)
    override fun INSTANCE(vararg propName: String): AHasCopyBoundrysData =     aHasCopyBoundrysData.add(COPYTYPE.INSTANCE, *propName)
    override fun INSTANCE(vararg dslRef: IDslRef): AHasCopyBoundrysData =      aHasCopyBoundrysData.add(COPYTYPE.INSTANCE, *dslRef)
    override fun INSTANCE(vararg className: ClassName): AHasCopyBoundrysData = aHasCopyBoundrysData.add(COPYTYPE.INSTANCE, *className)
    override fun NEW(vararg propName: String): AHasCopyBoundrysData =          aHasCopyBoundrysData.add(COPYTYPE.NEW, *propName)
    override fun NEW(vararg dslRef: IDslRef): AHasCopyBoundrysData  =          aHasCopyBoundrysData.add(COPYTYPE.NEW, *dslRef)
    override fun NEW(vararg className: ClassName): AHasCopyBoundrysData  =     aHasCopyBoundrysData.add(COPYTYPE.NEW, *className)
    override fun DEEP(vararg propName: String): AHasCopyBoundrysData =         aHasCopyBoundrysData.add(COPYTYPE.DEEP, *propName)
    override fun DEEP(vararg dslRef: IDslRef): AHasCopyBoundrysData =          aHasCopyBoundrysData.add(COPYTYPE.DEEP, *dslRef)
    override fun DEEP(vararg className: ClassName): AHasCopyBoundrysData =     aHasCopyBoundrysData.add(COPYTYPE.DEEP, *className)
    override fun DEEPNEW(vararg propName: String): AHasCopyBoundrysData =      aHasCopyBoundrysData.add(COPYTYPE.DEEPNEW, *propName)
    override fun DEEPNEW(vararg dslRef: IDslRef): AHasCopyBoundrysData =       aHasCopyBoundrysData.add(COPYTYPE.DEEPNEW, *dslRef)
    override fun DEEPNEW(vararg className: ClassName): AHasCopyBoundrysData =  aHasCopyBoundrysData.add(COPYTYPE.DEEPNEW, *className)

    override fun AHasCopyBoundrysData.onlyIf(collectiontyp: COLLECTIONTYP): AHasCopyBoundrysData {
        log.warn("${this::class.simpleName}.AHasCopyBoundrysData.onlyIf() Not yet implemented")
        return this
    }
}
