---
layout: page
title: Chassis DSL
subtitle: Specifying what to generate
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# The Chassis DSL for generating Code

One of Kotlin's nice language features that makes it ideal for creating *pure Kotlin* DSLs (Domain Specific Languages) are
[trailing lambda parameters](https://kotlinlang.org/docs/lambdas.html#passing-trailing-lambdas){:target="_blank"}, which allow to pass the lambda implementation outside the brackets when calling a function
whose last parameter is a lambda. For a better hands-on explanation see e.g. [Baeldung Kotlin DSL](https://www.baeldung.com/kotlin/dsl){:target="_blank"}.

To be crystal clear here: the Chassis DSL is ***100% pure Kotlin source-code***

Chassis DSL elaborates on this and adds a few nice features to make it more usable and flexible.

- using Kotlin's' `context()` feature for dsl related objects and functions (have a look at e.g. [youtube](https://youtu.be/GISPalIVdQY?si=h5we5HT6byi7BZKW&t=594){:target="_blank"})
- especially we use the context to "pass" the `class DslCtx` through all of our DSL parsing. (`DslCtx` is gathering all information the Chassis DSL contains)
- Multiple parsing `PASS`es of our DSL (see [class DslCtx](https://github.com/hoffipublic/chassis/blob/master/dsl/src/main/kotlin/com/hoffi/chassis/dsl/internal/DslCtx.kt){:target="_blank"})
  - multiple parses are necessary as we want to "use" parsed information of other models for the current model, but the other model might not have been parsed yet at all.
- via `class DslRef` (see [class DslRef](https://github.com/hoffipublic/chassis/blob/master/shared/src/main/kotlin/com/hoffi/chassis/shared/dsl/DslRef.kt){:target="_blank"}) we're able to "reference" any other defined (sub)level element
  - for mor in detail on `DslRef` see (TODO TBD)
  - this enables us to do a lot of things
    - specifying other models as to be extended super-classes/interface
    - gather properties of some other model
    - constrain Fillers or CRUD operations on other models or model-properties
    - etc.
- 

One drawback of the DSL being *pure kotlin sourcecode* is security, as the DSL may contain arbitrary harmful code also.<br/>
But as Chassis and its DSL solely is used at development time (and CICD) this disadvantage is no issue for our use-case: generating code. 

At the moment there is implemented only one toplevel DSL method:

continue with [Chassis top-level modelgroup]({{ site.baseurl }}{% link dsl/modelgroup.md %}) ...

### DslCtx PASS'es

Here's an example of a DSL function implementation, defining the DSL `showcase("someName" { ... showcase Sub(DSL) }` sub-DSL (see [class DslShowcase](https://github.com/hoffipublic/chassis/blob/c6ff23d55f537eb6f79dbee34c69c146d45b2b4d/dsl/src/main/kotlin/com/hoffi/chassis/dsl/DslShowcase.kt#L110))[youtube](https://youtu.be/GISPalIVdQY?si=h5we5HT6byi7BZKW&t=594){:target="_blank"})

As you can see any DSL function implementation should decide what to do in which DSL PASS (be careful though to still decent the DSL tree as inner sub-DSL might also want to do something in that PASS)

```kotlin
context(DslCtxWrapper)
class DslShowcaseDelegateImpl(simpleNameOfDelegator: String, delegatorRef: IDslRef)
  : ADslDelegateClass(simpleNameOfDelegator, delegatorRef), IDslImplShowcaseDelegate
{
    ...
  
    override fun showcase(simpleName: String, block: IDslApiShowcaseBlock.() -> Unit) {
        log.info("fun {}(\"{}\") { ... } in PASS {}", object{}.javaClass.enclosingMethod.name, simpleName, dslCtx.currentPASS)
        when (dslCtx.currentPASS) {
            dslCtx.PASS_ERROR -> TODO()
            dslCtx.PASS_FINISH -> { /* TODO implement me! */ }
            dslCtx.PASS_1_BASEMODELS -> {
                val dslImpl = theShowcaseBlocks.getOrPut(simpleName) { DslShowcaseBlockImpl(simpleName, selfDslRef) }
                dslImpl.apply(block)
            }
            else -> {}
        }
    }
```

<hr/>

[back to root](..)
