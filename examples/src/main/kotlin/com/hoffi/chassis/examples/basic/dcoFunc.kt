package com.hoffi.chassis.examples.basic

import com.hoffi.chassis.chassismodel.Initializer
import com.hoffi.chassis.chassismodel.ReplaceAppendOrModify
import com.hoffi.chassis.chassismodel.typ.TYP
import com.hoffi.chassis.chassismodel.typ.mutable
import com.hoffi.chassis.dsl.internal.DslCtxWrapper
import com.hoffi.chassis.dsl.modelgroup
import com.hoffi.chassis.shared.shared.Tag
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.DCO
import com.hoffi.chassis.shared.shared.reffing.MODELREFENUM.DTO
import com.hoffi.generated.universe.Dummy

const val DCOGROUP = "dCogroup"
const val DCO__ENTITY    = "an"


context(DslCtxWrapper)
fun dcosFunc() {
    dslCtx.topLevelDslFunctionName = object {}.javaClass.enclosingMethod.name

    modelgroup(DCOGROUP) {
        nameAndWhereto {
            packageName("dco")
            dcoNameAndWhereto {
                classPostfix("Dco")
                packageName("dco")
            }
            tableNameAndWhereto {
                classPostfix("Table")
                packageName("table")
            }
        }

        model(DCO__ENTITY) {
            property("value", TYP.STRING, mutable, Tag.CONSTRUCTOR, Tag.HASH_MEMBER, Tag.TO_STRING_MEMBER)
            property("prio", TYP.INT, mutable, Tag.TO_STRING_MEMBER)
            property("aInstant", TYP.INSTANT, mutable)
            property("aLocalDateTime", TYP.LOCALDATETIME, mutable)
            property("someObject", Dummy::class, mutable, Initializer.of("%T.%L", Dummy::class, "NULL"), Tag.TRANSIENT)
            property("someModelObject", DTO inModelgroup ENTITYGROUP withModelName ENTITY__SOMEMODEL, mutable, Tag.CONSTRUCTOR)

            addToStringMembers("aLocalDateTime")

            dco {

            }
            tableFor(DCO) {
                initializer("value", ReplaceAppendOrModify.APPEND, ".uniqueIndex()")

                crud {
                    CRUDALL FOR DCO
                }
            }
        }
    }
}
