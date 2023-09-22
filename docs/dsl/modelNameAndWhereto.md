---
layout: page
title: nameAndWhereto
subtitle: deciding how things are named<br/>and where generated files go to 
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# nameAndWhereto: deciding how things are named<br/>and where generated files go to

One of the most complex "things" dealing with the nitty gritties of code generation is:

- what is the name of it?
- which package it goes to?
- which subpackage it goes to?
- which location it goes to in the filesystem (basepath)?
- which sublocation in that basepath it goes to?
- does it have prefix and/or postfix?
- do I take a "common for all" prefix for any model in a modelgroup?
- does a specific prefix/postfix overwrite a defined prefix/postfix defined in the modelgroup? or concat it?
- does concat do concat before? or after? a prefix/postfix defined on a higher level?
- can I have an exception for a special single model subelement concerning all of this? (e.g. a common superclass/interface)?

To be best of all able to achieve what we have in mind, Chassis DSL tries to give you the most "adjustable" sub node `nameAndWhereto`.

In combination with (TODO link) `naming strategy resolution` (which is a bit tricky) you can adjust any naming knob for the generated classes, objects and interfaces that can wish for.

a complete `nameAndWhereto { ... }` looks like the following<br/>
and can be place inside:

- `dslRun("runName").configure { ... }`
- `modelgroup("mgName") { ... }`
- `model("modelName") { ... }`
- `dto|dco|tableFor|... { ... }` (at this place without the ability to specify for other model subelements)

```kotlin
        nameAndWhereto {
            baseDirAbsolute(absolute: String)
            baseDirAbsolute(absolute: Path)
            baseDir(concat: String)
            baseDir(concat: Path)
            pathAbsolute(absolute: String)
            pathAbsolute(absolute: Path)
            path(concat: String)
            path(concat: Path)

            classPrefixAbsolute(absolute: String)
            classPrefix(concat: String)
            classPrefixBefore(concat: String)
            classPrefixAfter(concat: String)
            classPostfixAbsolute(absolute: String)
            classPostfix(concat: String)
            classPostfixBefore(concat: String)
            classPostfixAfter(concat: String)

            basePackageAbsolute(absolute: String)
            basePackage(concat: String)
            packageNameAbsolute(absolute: String)
            packageName(concat: String)

            dtoNameAndWhereto {
                // all the same of above
            }
            tableNameAndWhereto {
                // all the same of above
            }
        }
```

again: to find out what interfaces the DSL node "enables" I recommend using intellij's `actions -> Navigate -> Goto by Reference Action -> Type Hierarchy`

## mode of operation

`nameAndWhereto { ... }` deals with three things:

1. the filesystem dir/path to where the class/object/interface is written to (without the package folder structure)
2. the pre/postfixes of the class/object/interface (=model) name
3. the `package` of the class/object/interface

For *each of these* three you set/alter two different variable values:

1. global
2. addendum

Also for each of the three things you have two variants of funcs:

1. absolute ones<br/>
   the underlying variable value is *replaced* by the given value
2. concat ones<br/>
   the underlying variable value is *appended/prepended* with the given value<br/>
   so. e.g. modelgroup can concat a prefix, model can concat another prefix and eventually the submodel another one

The global value might e.g. set in `dslRun("runName").configure { ... }`) and the parsed modelgroups just operate on the addendum

This way you have fine granular power on specifying where things should go,<br/>
but still can deal with *the eventual* target *on a higher level* (e.g. modelgroup oder dslRun).

The *real eventual* values for dir, classname and package name depend on the implementations of the corresponding
(TODO link Strategies) Strategies, which ultimately decide which values will win or overwritten or concatenated.

Naming strategies are evaluated in `DslCtx.PASS_FINISH` in the respective `finish()` functions of submodel DslImpl's (dto, dco, tableFor, ...)
(but also depend on preparation in finish() methods of modelgroup and model()

<hr/>

[back to root](..)
