# TypeQuux

[![Build Status](https://travis-ci.org/SimianQuant/typequux.svg?branch=master)](https://travis-ci.org/SimianQuant/typequux)
[![Build status](https://ci.appveyor.com/api/projects/status/7aspsw5t4f24xbbp?svg=true)](https://ci.appveyor.com/project/harshad-deo/typequux-8tp9g)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.simianquant/typequux_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.simianquant/typequux_2.13)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](https://www.scala-js.org)
[![Coverage Status](https://coveralls.io/repos/github/SimianQuant/typequux/badge.svg?branch=master)](https://coveralls.io/github/SimianQuant/typequux?branch=master)
[![Scaladoc](http://javadoc-badge.appspot.com/com.simianquant/typequux_2.13.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.simianquant/typequux_2.13) 
[![Gitter](https://badges.gitter.im/SimianQuant/typequux.svg)](https://gitter.im/SimianQuant/typequux?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Typelevel programming allows developers to encode several flavours of runtime invariants into the type system. Unfortunately, 
libraries that support typelevel programming tend to be poorly documented, difficult to understand and difficult to hack. This makes
them hard to customize to the needs of a specific project/problem. 

TypeQuux provides concise, efficient and easy-to-modify  implementations of several typelevel programming primitives. As 
such, it represents collected wisdom on type-hackery in scala. 

To see what is possible, head on over to the [project site](https://simianquant.github.io/typequux/TypeQuux.html) or peruse through the [API](https://simianquant.github.io/typequux/api/typequux/index.html). You can see an indexed view of supported primitives and their operations [here](https://simianquant.github.io/typequux/Contents+in+Depth.html). 

To use, add the following line to your `build.sbt` file:

```scala
libraryDependencies += "com.simianquant" %% "typequux" % "0.9.0" // scala-jvm
libraryDependencies += "com.simianquant" %%% "typequux" % "0.9.0" // scala-js/cross
```

Binaries for 2.11 and 2.12 are available upto version 0.8.1. After version 0.9.0, only 2.13 binaries are available.

Currently supported primitives are:

1. [Church encodings of booleans](https://simianquant.github.io/typequux/Church+Encoding+of+Booleans.html)
2. [Peano numbers](https://simianquant.github.io/typequux/Peano+Numbers.html)
3. [Dense numbers](https://simianquant.github.io/typequux/Dense+Numbers.html) (like peano numbers but **much** faster)
4. [Type-Sets](https://simianquant.github.io/typequux/Type+Sets.html)
5. [Type-Maps](https://simianquant.github.io/typequux/Type+Maps.html)
6. [Natural Transformations](https://simianquant.github.io/typequux/Natural+Transformations.html)
7. [Type Unions and Exclusions](https://simianquant.github.io/typequux/Type-Unions+and+Exclusions.html)
8. [Singleton types for literals](https://simianquant.github.io/typequux/Singleton+Types+for+Literals.html)
9. [Covariant heterogenous lists](https://simianquant.github.io/typequux/Covariant+Heterogenous+Lists.html)
10. [HList style operations on tuples](https://simianquant.github.io/typequux/Tuple+Ops.html)
11. [Collections with statically known sizes](https://simianquant.github.io/typequux/Sized+Vectors.html)
12. [Collections indexed by a string](https://simianquant.github.io/typequux/String+Indexed+Collections.html), which are like associative maps with static guarantees
13. [Records](https://simianquant.github.io/typequux/Records.html), which are like adhoc classes
14. [Constraints](https://simianquant.github.io/typequux/Understanding+Constraints.html), that allow you to abstract over arity and structure

## Changelog

### 0.8.0
1. Drop support for Scala Native

### 0.8.1
1. Add FalseConstraint

### 0.9.0
1. Add support for Scala 2.13

## License

Copyright 2020 Harshad Deo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
