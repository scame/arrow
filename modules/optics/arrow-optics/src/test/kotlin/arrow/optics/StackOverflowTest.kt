package arrow.optics

import arrow.core.Either
import arrow.data.ListK
import arrow.data.ListKOf
import arrow.data.k
import arrow.syntax.either.left
import arrow.syntax.either.right
import arrow.test.UnitSpec
import io.kotlintest.matchers.shouldBe

class StackOverflowTest: UnitSpec() {

    init {

        "Test #620 regression" {

            val o = AOptional() + BLens() + listToTraversal() + CLens()

            val a = A(
                    B(
                            listOf(
                                    C("a"),
                                    C("b"),
                                    C("c")
                            )
                    )
            )

            val aExpected = A(
                    B(
                            listOf(
                                    C("A"),
                                    C("B"),
                                    C("C")
                            )
                    )
            )


            val aActual = o.modify(a, {it.toUpperCase()})

            aActual shouldBe aExpected

        }

    }

    data class A(val b: B?)

    data class B(val c: List<C>)

    data class C(val s: String)

    fun AOptional() = object: Optional<A, B> {
        override fun set(s: A, b: B): A {
            return s.copy(b = b)
        }

        override fun getOrModify(s: A): Either<A, B> {
            return s.b?.right() ?: s.left()
        }
    }

    fun BLens() = object: Lens<B, List<C>> {
        override fun get(s: B): List<C> {
            return s.c
        }

        override fun set(s: B, b: List<C>): B {
            return s.copy(c = b)
        }
    }

    fun CLens() = object: Lens<C, String> {
        override fun get(s: C): String {
            return s.s
        }

        override fun set(s: C, b: String): C {
            return s.copy(s = b)
        }
    }

    fun <A> listToTraversal(): Traversal<List<A>, A> {
        val listTraversal: Traversal<ListKOf<A>, A> = Traversal.fromTraversable()
        return listToListK2<A>() + listTraversal
    }

    fun <A, B> pListToListK2(): PIso<List<A>, List<B>, ListKOf<A>, ListKOf<B>> = PIso(
            get = { it.k() },
            reverseGet = { (it as ListK<B>).list }
    )

    fun <A> listToListK2(): Iso<List<A>, ListKOf<A>> = pListToListK2()


}