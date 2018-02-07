---
layout: docs
title: SetKW
permalink: /docs/datatypes/setkw/
---

## SetKW

SetKW(Kinded Wrapper) is a higher kinded wrapper around the the Set collection interface. 

It can be created from the Kotlin Set type with a convient `k()` function.

```kotlin
import arrow.*
import arrow.core.*
import arrow.data.*

setOf(1, 2, 5, 3, 2).k()
// SetKW(set=[1, 2, 5, 3])
```

It can also be initialized with the following:

```kotlin
SetKW(setOf(1, 2, 5, 3, 2))
// SetKW(set=[1, 2, 5, 3])
```
or
```kotlin
SetKW.pure(1)
// SetKW(set=[1])
```

given the following:
```kotlin
val oldNumbers = setOf( -11, 1, 3, 5, 7, 9).k()
val evenNumbers = setOf(-2, 4, 6, 8, 10).k()
val integers = setOf(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5).k()
```
SetKW derives the following typeclasses:

[`Semigroup`](/docs/typeclasses/semigroup/) and [`SemigroupK`](/docs/typeclasses/semigroupk/):

```kotlin
val numbers = oldNumbers.combineK(evenNumbers.combineK(integers))
numbers
// SetKW(set=[-11, 1, 3, 5, 7, 9, -2, 4, 6, 8, 10, -5, -4, -3, -1, 0, 2])
```
```kotlin
evenNumbers.combineK(integers).combineK(oldNumbers)
// SetKW(set=[-2, 4, 6, 8, 10, -5, -4, -3, -1, 0, 1, 2, 3, 5, -11, 7, 9])
```

[`Monoid`](/docs/typeclasses/monoid/) and [`MonoidK`](/docs/typeclasses/monoidk/):
```kotlin
SetKW.monoidK().combineK(numbers, SetKW.empty())
// SetKW(set=[-11, 1, 3, 5, 7, 9, -2, 4, 6, 8, 10, -5, -4, -3, -1, 0, 2])
```

[`Foldable`](/docs/typeclasses/foldable/):
```kotlin
numbers.foldLeft(0) {sum, number -> sum + (number * number)}
// 561
```

Available Instances:

```kotlin
import arrow.debug.*

showInstances<SetKWHK, Unit>()
// [Foldable, Monoid, MonoidK, Semigroup, SemigroupK]
```
