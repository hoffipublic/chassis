# chassis root README

## Project Layout

- root project chassis
  - console client to conquer all subprojects and their actions
  - gather artifacts??
  - call sub-artifacts (executing them)
- dsl subproject
  - chassis dsl language definition for generating stuff
  - main
  - some variant/plugin
  - other variant/plugin
- codegen sub-project
  - generation of code from dsl specifications
  - main
  - some variantA
  - some variantB
- universe subproject
  - static things that are needed to make generated code work
- plugins???
- generated subproject
  - sub-project(s) where code is generated to see if everything works
  - kotlin (don't need this as this is all about kotlin)
- targetprojects
  - projects that actually do something and depend on generated stuff

## Transformers

- from one name to another
  - and vice versa?
- data format transformation
- data type transformation

## Strategies

### copy strategies

- how deep do you want to copy?
- what to do if field missing?
- what to do if validation fails?

### filler Strategies

- validate fromValue, validate toValue<br/>
  define what to do if certain validation errors, null, etc.

### table Strategies

-tbd-

###
