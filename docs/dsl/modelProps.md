---
layout: page
title: class properties of models and model elements
subtitle: class props
menubar: data_menu_chassis
toc: true
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# model and model element's class properties

There are four types of properties:

1. `TYP` properties, of a Chassis predefined type (see below)
2. `KClass<*>` properties
3. `poetType: TypeName` properties
4. model properties (a reference to another model or modelsubelement of the Chassis DSL)

currently predefined `TYP`s are:

|-----|---------------------|
| INT | TYP(Integer::class) |
| LONG | TYP(Long::class) |
| STRING | TYP(String::class) |
| BOOL | TYP(Boolean::class) |
| UUID | TYP(java.util) |
| INSTANT | TYP(kotlinx.datetime.Instant::class) |
| LOCALDATETIME | TYP(kotlinx.datetime.LocalDateTime::class) |

There are a plethora of function overloads defined to enable you with fewest possible typing to specify what you want the property characteristics to be.

- mutable: Mutable
  - 
- collectionType: COLLECTIONTYP
  - NONE
  - LIST
  - SET
  - COLLECTION
  - ITERABLE
- initializer: Initializer
  - `Initializer.of(format: String, vararg args: Any)` wrapper around kotlinPoet initializer, see [KotlinPoet](https://square.github.io/kotlinpoet/){:target="_blank"}
- modifiers: MutableSet&lt;com.squareup.kotlinpoet.KModifier&gt; 
- length: Int
  - used for Database mapping if the persistent type has to have fixed length (e.g varchar(2048))
- tags: Tags (see code for a complete list)
  - DEFAULT_INITIALIZER
  - NO_DEFAULT_INITIALIZER
  - CONSTRUCTOR
  - CONSTRUCTOR_INSUPER
  - COLLECTION_IMMUTABLE
  - HASH_MEMBER
  - PRIMARY
  - TO_STRING_MEMBER
  - TRANSIENT
  - NULLABLE
  - NULLABLE_GENERICTYPE

## propertiesOf

the fun `propertiesOf(...)` allows you to specify that the current (sub)model should (directly) have the properties of the referenced Chassis DSL (sub)model.

second parameter is one of (specifying which of the rerenced props to gather):

```kotlin
enum class GatherPropertiesEnum {
    NONE,
    PROPERTIES_ONLY_DIRECT_ONES,
    PROPERTIES_AND_SUPERCLASS_PROPERTIES,
    SUPERCLASS_PROPERTIES_ONLY
}
```

<hr/>

[back to root](..)
