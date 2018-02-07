---
layout: docs
title: Glossary
permalink: /docs/patterns/glossary/
---

## Functional Programming Glossary

Note: This section keeps on growing! Keep an eye on it from time to time.

### Datatypes

A datatype is a class that encapsulates one reusable coding pattern.
These solutions have a canonical implementation that is generalised for all possible uses.

Some common patterns expressed as datatypes are absence handling with [`Option`]({{ '/docs/datatypes/option' | relative_url }}),
branching in code with [`Either`]({{ '/docs/datatypes/either' | relative_url }}),
catching exceptions with [`Try`]({{ '/docs/datatypes/try' | relative_url }}),
or interacting with the platform the program runs in using [`IO`]({{ '/docs/effects/io' | relative_url }}).

You can read more about all the [datatypes]({{ '/docs/datatypes/intro' | relative_url }}) that Arrow provides in its [section of the docs]({{ '/docs/datatypes/intro' | relative_url }}).

### Typeclasses

A typeclass is a specification for one behavior associated with a single type.
This behavior is checked by a test suite called the "laws" for that typeclass.
These test suites are available in the package arrow-tests.

What differentiates typeclasses from regular OOP inheritance is that typeclasses are meant to be implemented outside of their types.
The association is done using generic parametrization rather than the usual subclassing by implementing the interface.
This means that they can be implemented for any class, even those not in the current project,
and allows us to make available at the global scope any one implementation of a typeclasses for the single unique type they're associated with.

Examples of these behaviors are comparability ([`Eq`]({{ '/docs/typeclasses/eq' | relative_url }})),
composability ([`Monoid`]({{ '/docs/typeclasses/monoid' | relative_url }})),
its contents can be mapped from one type to another ([`Functor`]({{ '/docs/typeclasses/functor' | relative_url }})),
or error recovery ([`MonadError`]({{ '/docs/typeclasses/monaderror' | relative_url }})).

You can read more about all the [typeclasses]({{ '/docs/typeclasses/intro' | relative_url }}) that Arrow provides in its [section of the docs]({{ '/docs/typeclasses/intro' | relative_url }}).

To define a fully featured typeclass in Λrrow you use an interface that extends from `TC`, and use the annotation `@typeclass` to generate all boilerplate related to global lookup, which is explained below. The annotation and interface to extend have different names to avoid collision.

```kotlin
@typeclass
interface Eq<F>: TC {
  fun eqv(a: F, b: F): Boolean
}
```

### Instances

A single implementation of a typeclass for a specific datatype or class.
Because typeclasses require generic parameters each implementation is meant to be unique for that parameter.

```kotlin
@instance
interface IntEqInstance: Eq<Int> {
  override fun eqv(a: Int, b: Int): Boolean = a == b
}
```

In Λrrow all typeclass instances can be looked up in a global scope using an inlined reified method with the same name as the typeclass.
Its generic parameter will be used for the lookup, which reinforces the concept that most typeclasses should have a single implementation per type.

All the instances in the library are already registered and available in the global scope.
If you're defining your own instances and would like for them to be discoverable in the global scope 
you can add them by annotating them as `@instance`, and Λrrow's [annotation processor](https://github.com/arrow-kt/arrow#additional-setup) will register them for you. The interface has to have all required methods defined by default implementations, and be named after the datatype + typeclass + the word `Instance`.

```kotlin
import arrow.*
import arrow.typeclasses.*

eq<Int>()
// arrow.instances.IntEqInstance@5856df3a
```

### Type constructors

> NOTE: This approach to type constructors will be simplified if [KEEP-87](https://github.com/Kotlin/KEEP/pull/87) is approved. Go vote!

A type constructor is any class or interface that has at least one generic parameter. For example, 
[`ListKW<A>`]({{ '/docs/datatypes/listkw' | relative_url }}) or [`Option<A>`]({{ '/docs/datatypes/option' | relative_url }}).
They're called constructors because they're similar to a factory function where the parameter is `A`, except type constructors work only for types.
So, we could say that after applying the parameter `Int` to the type constructor `ListKW<A>` it returns a `ListKW<Int>`.
As `ListKW<Int>` isn't parametrized in any generic value it is not considered a type constructor anymore, just a regular type.

Like functions, a type constructor with several parameters like [`Either<L, R>`]({{ '/docs/datatypes/either' | relative_url }}) can be partially applied for one of them to return another type constructor with one fewer parameter,
for example applying `Throwable` to the left side yields `Either<Throwable, A>`, or applying `String` to the right side resuls in `Either<E, String>`.

Type constructors are useful when matched with typeclasses because they help us represent instances of parametrized classes -the containers- that work for all generic parameters -the content-.
As type constructors is not a first class feature in Kotlin, Λrrow uses an interface `HK<F, A>` to represent them.
HK stands for Higher Kind, which is the name of the language feature that allows working directly with type constructors.

#### Higher Kinds

In a Higher Kind with the shape `HK<F, A>`, if `A` is the type of the content then `F` has to be the type of the container.

A malformed Higher Kind would use the whole type constructor to define the container, duplicating the type of the content ~~`HK<Option<A>, A>`~~. This incorrect representation has large a number of issues when working with partially applied types and nested types.

What Λrrow does instead is define a surrogate type that's not parametrized to represent `F`.
These types are named same as the container and suffixed by HK, as in `OptionHK` or `ListKWHK`.

```kotlin
class OptionHK private constructor()

sealed class Option<A>: HK<OptionHK, A>
```

```kotlin
class ListKWHK private constructor()

data class ListKW<A>(val list: List<A>): HK<ListKWHK, A>
```

As `ListKW<A>` is the only existing implementation of `HK<ListKWHK, A>`, we can define an extension function on `HK<ListKWHK, A>` to do the downcasting safely for us. This function by convention is called `ev()` (evidence or evaluate).

```kotlin
fun HK<ListKWHK, A>.ev() = this as ListKW<A>
```

This way we have can to convert from `ListKW<A>` to `HK<ListKWHK, A>` via simple subclassing and from `HK<ListKWHK, A>` to `ListKW<A>` using the function `ev()`. Being able to define extension functions that work for partially applied generics is a feature from Kotlin that's not available in Java. You can define `fun HK<OptionHK, A>.ev()` and `fun HK<ListKWHK, A>.ev()` and the compiler can smartly decide which one you're trying to use. If it can't it means there's an ambiguity you should fix!

The function `ev()` is already defined for all datatypes in Λrrow. If you're creating your own datatype that's also a type constructor and would like to create all these helper types and functions,
you can do so simply by annotating it as `@higerkind` and the Λrrow's [annotation processor](https://github.com/arrow-kt/arrow#additional-setup) will create them for you.

```kotlin
@higherkind
data class ListKW<A>(val list: List<A>): ListKWKind<A>

// Generates the following code:
//
// class ListKWHK private constructor()
// typealias ListKWKind<A> = HK<ListKWHK, A>
// fun ListKWKind<A>.ev() = this as ListKW<A>
```

Note that the annotation `@higerkind` will also generate the integration typealiases required by [KindedJ]({{ '/docs/integrations/kindedj' | relative_url }}) as long as the datatype is invariant. You can read more about sharing Higher Kinds and type constructors across JVM libraries in [KindedJ's README](https://github.com/KindedJ/KindedJ#rationale).

#### Using Higher Kinds with typeclasses

Now that we have a way of representing generic constructors for any type, we can write typeclasses that are parametrised for containers.

Let's take as an example a typeclass that specifies how to map the contents of any container `F`. This typeclass that comes from computer science is called a [`Functor`]({{ '/docs/typeclasses/functor' | relative_url }}).

```kotlin
@typeclass
interface Functor<F>: TC {
  fun <A, B> map(fa: HK<F, A>, f: (A) -> B): HK<F, B>
}
```

See how the class is parametrized on the container `F`, and the function is parametrized to the content `A`. This way we can have a single representation that works for all mappings from `A` to `B`.

Let's define an instance of `Functor` for the datatype `ListKW`, our own wrapper for lists.

```kotlin
@instance
interface ListKWFunctorInstance : Functor<ListKWHK> {
  override fun <A, B> map(fa: HK<ListKWHK, A>, f: (A) -> B): ListKW<B> {
    val list: ListKW<A> = fa.ev()
    return list.map(f)
  }
}
```

This interface extends `Functor` for the value `F` of `ListKW`. We use an annotation processor `@instance` to generate an object out of an interface with all the default methods already defined, and to add that method to the global typeclass instance lookup. See that we respect the naming convention of datatype + typeclass + the word `Instance`.

```kotlin
@instance
interface ListKWFunctorInstance : Functor<ListKWHK>
```

The signature of `map` once the types have been replaced takes a parameter `HK<ListKWHK, A>`, which is the receiver, and a mapping function from `A` to `B`. This means that map will work for all instances of `ListKW<A>` for whatever the value of `A` can be.

```kotlin
override fun <A, B> map(fa: HK<ListKWHK, A>, f: (A) -> B): ListKW<B>
```

The implementation is short. On the first line we downcast `HK<ListKWHK, A>` to `ListKW<A>` using `ev()`. Once the value has been downcasted, the implementation of map inside the `ListKW<A>` we have obtained already implements the expected behavior of map.

```kotlin
val list: ListKW<A> = fa.ev()
return list.map(f)
```

#### Using Higher Kinds and typeclasses with functions

Higher kinds are also used to model functions that require for a datatype to implement a typeclass. This way you can create functions that abstract behavior (defined by a typeclass) and allow callers to define which datatype they'd like to apply it to.

Let's use the typeclass [`Applicative`]({{ '/docs/typeclasses/applicative' | relative_url }}), that contains the constructor function `pure()`.

```kotlin
@typeclass
interface Applicative<F>: Functor<F>, TC {

  // Constructs the current datatype with a value of type A inside
  fun <A> pure(a: A): HK<F, A>
  
  /* ... */
}
```

Once we have this typeclass behavior define we can now write a function that's parametrized for any `F` that has one instance of `Applicative`. The function uses the constructor `pure` to create a value of type `HK<F, User>`, effectively generifying the return on any container `F`.

```kotlin
inline fun <reified F> randomUserStructure(f: (Int) -> User, AP: Applicative<F> = applicative<F>()): HK<F, User> =
  AP.pure(f(Math.random()))
```

Now lets create a simple example instance of `Applicative` where our `F` is `ListKW`. This implementation of a `pure` constructor is trivial for lists, as it just requires wrapping the value.

```kotlin
@instance
interface ListKWApplicativeInstance : Applicative<ListKWHK> {
  override fun <A> pure(a: A): HK<ListKWHK, A> = ListKW(listOf(a))
  
  /* ... */
}
```

And now we can show how this function `randomUserStructure()` can be used for any datatype that implements [`Applicative`]({{ '/docs/typeclasses/applicative' | relative_url }}). As the function returns a value `HK<F, User>` the caller is responsible of calling `ev()` to downcast it to the expected value.

```kotlin
val list = randomUserStructure(::User, ListKW.applicative()).ev()
//[User(342)]
```

```kotlin
val option = randomUserStructure(::User, Option.applicative()).ev()
//Some(User(765))
```

```kotlin
val either = randomUserStructure(::User, Either.applicative<Unit>()).ev()
//Right(User(221))
```

Passing the instance in every function call seems like a burden. But, remember that all instances already defined in Λrrow can be looked up globally!! That means that whenever asking for any instance you can ask for a default value that'll be looked up globally based on its type:

```kotlin
import arrow.data.*

applicative<ListKWHK>()
// arrow.data.ListKWApplicativeInstanceImplicits$instance$1@2706cf34
```

```kotlin
import arrow.core.*

applicative<OptionHK>()
// arrow.core.OptionApplicativeInstanceImplicits$instance$1@6e5e6f49
```

So, because `randomUserStructure` provides a default value for [`Applicative`]({{ '/docs/typeclasses/applicative' | relative_url }}) that's looked up globally, we can call it without passing the second parameter as long as we tell the compiler what type we're expecting the function to return.

```kotlin
val list: ListKW<User> = randomUserStructure(::User).ev()
//[User(342)]
```

```kotlin
val option: Option<User> = randomUserStructure(::User).ev()
//Some(User(765))
```

This system of looking up global instances and allowing manual overrides has several pitfalls and can and will be simplified when the feature request [KEEP-87](https://github.com/Kotlin/KEEP/pull/87) is implemented into the language.
