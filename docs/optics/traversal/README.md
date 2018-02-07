---
layout: docs
title: Traversal
permalink: /docs/optics/traversal/
---

## Traversal
A `Traversal` is an optic that can see into a structure and get, set or modify 0 to N foci.

It is a generalization of [`Traverse#traverse`](/docs/typeclasses/traverse). Given a `Traverse<F>` we can apply a function `(A) -> HK<G, B>` to `HK<F, A>` and get `HK<G, HK<F, B>>`.
We can think of `HK<F, A>` as a structure `S` that has a focus `A`. So given a `PTraversal<S, T, A, B>` we can apply a function `(A) -> HK<F, B>` to `S` and get `HK<F, T>`.

 - `Traverse.traverse(fa: HK<F, A>, f: (A) -> HK<G, B>, GA: Applicative<G>): HK<G, HK<F, B>>`
 - `PTraversal.modifyF(s: S, f: (A) -> HK<F, B>, GA: Applicative<F>): HK<F, T>`

You can get a `Traversal` for any existing `Traverse`.

```kotlin
import arrow.*
import arrow.optics.*
import arrow.data.*

val listTraversal: Traversal<ListKWKind<Int>, Int> = Traversal.fromTraversable()

listTraversal.modifyF(Try.applicative(), listOf(1, 2, 3).k()) {
    Try { it / 2 }
}
// Success(value=ListKW(list=[0, 1, 1]))
```
```kotlin
listTraversal.modifyF(Try.applicative(), listOf(0, 2, 3).k()) {
    Try { throw TryException.UnsupportedOperationException("Any arbitrary exception") }
}
// Failure(exception=UnsupportedOperationException(message=Any arbitrary exception))
```

Or by using any of the constructors of `Traversal`.

```kotlin
import arrow.core.*

fun <A> traversalTuple2Example(): Traversal<Tuple2<A, A>, A> = Traversal(
        get1 = { it.a },
        get2 = { it.b },
        set = { a, b, _ -> Tuple2(a, b) }
)
```

Arrow optics also provides a number of predefined `Traversal` optics.

```kotlin
import arrow.optics.instances.*

traversalTuple2<String>().combineAll("Hello, " toT "World!")
// Hello, World!
```
```kotlin
traversalTuple10<Int>().getAll(Tuple10(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
// ListKW(list=[1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
```

## Composition

Composing `Traversal` can be used for accessing and modifying foci in nested structures.

```kotlin
val listOfPairTraversal: Traversal<ListKWKind<Tuple2<String, String>>, Tuple2<String, String>> = Traversal.fromTraversable()
val nestedInts = listOfPairTraversal compose traversalTuple2()

nestedInts.fold(listOf("Hello, " toT "World ", "from " toT "nested structures!").k())
// Hello, World from nested structures!
```

`Traversal` can be composed with all optics and results in the following optics.

|   | Iso | Lens | Prism |Optional | Getter | Setter | Fold | Traversal |
| --- | --- | --- | --- |--- | --- | --- | --- | --- |
| Traversal | Traversal | Traversal | Traversal | Traversal | Fold | Setter | Fold | Traversal |

### Polymorphic Traversal

When dealing with polymorphic types we can also have polymorphic `Traversel`s that allow us to morph the type of the foci.
Previously we used a `Traversal<ListKWKind<Int>, Int>`, it was able to morph the `Int` values in the constructed type `ListKW<Int>`.
With a `PTraversal<ListKWKind<Int>, ListKWKind<String>, Int, String>` we can morph an `Int` to a `String` and thus also morph the type from `ListKW<Int>` to `ListKW<String>`.

```kotlin
val pTraversal: PTraversal<ListKWKind<Int>, ListKWKind<String>, Int, String> = PTraversal.fromTraversable()

pTraversal.set(listOf(1, 2, 3, 4).k(), "Constant")
// ListKW(list=[Constant, Constant, Constant, Constant])
```
```kotlin
pTraversal.modify(listOf(1, 2, 3, 4).k()) {
    "At position $it"
}
// ListKW(list=[At position 1, At position 2, At position 3, At position 4])
```

### Laws

Arrow provides [`TraversalLaws`][traversal_laws_source]{:target="_blank"} in the form of test cases for internal verification of lawful instances and third party apps creating their own traversal.

[traversal_laws_source]: https://github.com/arrow-kt/arrow/blob/master/arrow-test/src/main/kotlin/arrow/laws/TraversalLaws.kt
