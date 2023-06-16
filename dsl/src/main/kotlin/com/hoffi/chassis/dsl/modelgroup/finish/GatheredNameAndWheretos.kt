package com.hoffi.chassis.dsl.modelgroup.finish

import com.hoffi.chassis.dsl.internal.DslRun
import com.hoffi.chassis.dsl.modelgroup.AModelSubElement
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoOnSubElementsDelegateImpl
import com.hoffi.chassis.dsl.whereto.DslNameAndWheretoWithSubElementsDelegateImpl
import com.hoffi.chassis.shared.dsl.DslRef

class GatheredNameAndWheretos {
    //lateinit var
    lateinit var dslRun: DslRun
    lateinit var fromDslRunConfigure: DslNameAndWheretoWithSubElementsDelegateImpl
    lateinit var fromModelgroup: DslNameAndWheretoOnSubElementsDelegateImpl
    lateinit var fromModel: DslNameAndWheretoOnSubElementsDelegateImpl
    val fromSubElements = mutableMapOf<DslRef.IModelSubElement, AModelSubElement>()
    val dsRunNameAndWhereto = mutableMapOf<String, String>()
}
