---
layout: page
title: Chassis DSL crosscutting nodes
subtitle: nodes that might appear under multiple nodes 
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../assets/Chassis.png
---
# Crosscutting nodes that might appear under multiple nodes

For information how crosscutting nodes are implemented with interface delegation,
see [DSL block delegation showcase]({{ site.baseurl }}{% link dev/arch/Arch_2_DslDelegation.md %})

Crosscutting nodes are nodes that can appear under multiple different other nodes.

Crosscutting nodes are implemented via Kotlin interface delegation to be able to stay "DRY"<br/>
(DRY = Don't Repeat Yourself)

- [DSL block delegation showcase]({{ site.baseurl }}{% link dev/arch/Arch_2_DslDelegation.md %})
- [name and whereto]({{ site.baseurl }}{% link dsl/modelNameAndWhereto.md %})
- [showcase (DSL Block Delegation)]({{ site.baseurl }}{% link dev/arch/Arch_2_DslDelegation.md %})
- class mods: noop by now
- [props and properties]({{ site.baseurl }}{% link dsl/modelProps.md %})
- [gather propertiesOf other (sub)model]({{ site.baseurl }}{% link dsl/modelProps.md %})
- [extends super classes and interfaces]({{ site.baseurl }}{% link dsl/modelExtends.md %})


<hr/>

[back to root](..)
