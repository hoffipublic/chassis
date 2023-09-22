---
layout: page
title: DSL Context
subtitle: DslCtx
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../../assets/Chassis.png
---
# DslContext DslCtx passed through all of Chassis DSL parsing

DslContext holds all `ADslClass`'es (and more)

Beside taking care of the parsing `PASS`'es, the most central part is the inline function to create and manage all(!) ADslImpl classes via introspection:

```kotlin
    context(DslCtxWrapper) // for calling constructor of ADslClass 1st parameter
    inline fun <reified T : ADslClass> ctxObjOrCreate(dslRef: IDslRef): T {
        ...
    }
    context(DslCtxWrapper)
    inline fun <reified T : ADslClass> ctxObjCreate(dslRef: IDslRef): T {
        ...
    }
    inline fun <reified T : ADslClass> ctxObj(key: IDslRef): T {
        ...
    }
    inline fun <reified T : ADslClass> addToCtx(aDslClass: T): T {
        ...
    }
    inline fun <reified T : ADslClass> ctxObjCreateNonDelegate(aDslClassCreateBlock: () -> T): T {
        ...
    }
```

Using inline reified functions ensures that we can get a typesafe ***actual*** `ADslClass` class instance by its `IDslRef` from the `DslCtx` and not just a generic `ADslClass`

```kotlin
    // getting a specific `DslImplModel` and not just a base `ADslClass`
    val dslModel: DslImplModel = dslCtx.ctxObj(parentDslRef)
```


[link to DSL docs]({{ site.baseurl }}{% link dsl/dsl.md %})

