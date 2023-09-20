---
layout: page
title: Dev Conventions
subtitle: Naming is one of the most important thing<br/>in SW Development
toc: true
show_sidebar: false
hero_image: ../../assets/Chassis.png
---
# Dev Conventions

Any time you have to decide (`when(...) { }`) on something that might get further options if somewhere in the future is extended, e.g.:<br/>
atm a `model { ... }` can only have `dto { ... }`, `dco { ... }` and `tableFor { ... }` as sub nodes, but this might change in the future.

So if at any place in the sourcecode we have to do "something else" depending on the model sub-node,<br/>
there should be a `whens lambda`, e.g. like in [WhensDslRef](https://github.com/hoffipublic/chassis/blob/master/shared/src/main/kotlin/com/hoffi/chassis/shared/whens/WhensDslRef.kt#L42)

```kotlin
    fun <R> whenModelSubelement(dslRef: IDslRef,
        isDtoRef: () -> R,
        isTableRef: () -> R,
        isDcoRef: () -> R,
        catching: (DslException) -> Throwable = { Throwable("when on '$dslRef' not exhaustive") }
    ): R {
        when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} } // sentinel to check if new MODELREFENUM was added
        return when (dslRef) {
            is DslRef.dto -> isDtoRef()
            is DslRef.table -> isTableRef()
            is DslRef.dco -> isDcoRef()
            else -> throw catching(DslException("no (known) modelSubelement"))
        }
    }
```

and its usage example:

```kotlin
        intersectPropsData.sourceVarName = WhensDslRef.whenModelSubelement(sourceGenModelFromDsl.modelSubElRef,
            isDtoRef = { "source${intersectPropsData.sourceVarNamePostfix}" },
            isDcoRef = { "source${intersectPropsData.sourceVarNamePostfix}" },
            isTableRef = { "resultRow${intersectPropsData.sourceVarNamePostfix}" },
        )
```

As you also can see there is a no-op sentinel line of code in `fun whenModelSubelement(...)`:

```kotlin
when (MODELREFENUM.sentinel) { MODELREFENUM.MODEL, MODELREFENUM.DTO, MODELREFENUM.TABLE, MODELREFENUM.DCO -> {} }
```

As there is also an `enum class MODELREFENUM` which is "relevant also for any decision on model-sub-node elements"<br/>
it is also added here, so that if you change it, you immediately get a compile error also at this code location<br/>
(which under other circumstances you might have forgotten to check and realise much more later whilst regression testing)

Using this convention, if a new model-sub-node is introduced and you add it to the `fun <R> whenModelSubelement(...)`<br/>
automagically all code locations show up in intellij with a compile error, that you have to "adjust".
