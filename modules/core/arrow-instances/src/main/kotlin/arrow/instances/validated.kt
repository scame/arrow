package arrow.instances

import arrow.*
import arrow.core.Eval
import arrow.data.*
import arrow.typeclasses.*

@instance(Validated::class)
interface ValidatedFunctorInstance<E> : Functor<ValidatedPartialOf<E>> {
    override fun <A, B> map(fa: ValidatedOf<E, A>, f: (A) -> B): Validated<E, B> = fa.reify().map(f)
}

@instance(Validated::class)
interface ValidatedApplicativeInstance<E> : ValidatedFunctorInstance<E>, Applicative<ValidatedPartialOf<E>> {

    fun SE(): Semigroup<E>

    override fun <A> pure(a: A): Validated<E, A> = Valid(a)

    override fun <A, B> map(fa: ValidatedOf<E, A>, f: (A) -> B): Validated<E, B> = fa.reify().map(f)

    override fun <A, B> ap(fa: ValidatedOf<E, A>, ff: Kind<ValidatedPartialOf<E>, (A) -> B>): Validated<E, B> = fa.reify().ap(ff.reify(), SE())

}

@instance(Validated::class)
interface ValidatedApplicativeErrorInstance<E> : ValidatedApplicativeInstance<E>, ApplicativeError<ValidatedPartialOf<E>, E> {

    override fun <A> raiseError(e: E): Validated<E, A> = Invalid(e)

    override fun <A> handleErrorWith(fa: ValidatedOf<E, A>, f: (E) -> ValidatedOf<E, A>): Validated<E, A> =
            fa.reify().handleLeftWith(f)

}

@instance(Validated::class)
interface ValidatedFoldableInstance<E> : Foldable<ValidatedPartialOf<E>> {

    override fun <A, B> foldLeft(fa: ValidatedOf<E, A>, b: B, f: (B, A) -> B): B =
            fa.reify().foldLeft(b, f)

    override fun <A, B> foldRight(fa: ValidatedOf<E, A>, lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> =
            fa.reify().foldRight(lb, f)

}

@instance(Validated::class)
interface ValidatedTraverseInstance<E> : ValidatedFoldableInstance<E>, Traverse<ValidatedPartialOf<E>> {

    override fun <G, A, B> traverse(fa: Kind<ValidatedPartialOf<E>, A>, f: (A) -> Kind<G, B>, GA: Applicative<G>): Kind<G, Validated<E, B>> =
            fa.reify().traverse(f, GA)

}

@instance(Validated::class)
interface ValidatedSemigroupKInstance<E> : SemigroupK<ValidatedPartialOf<E>> {

    fun SE(): Semigroup<E>

    override fun <B> combineK(x: ValidatedOf<E, B>, y: ValidatedOf<E, B>): Validated<E, B> =
            x.reify().combineK(y, SE())

}

@instance(Validated::class)
interface ValidatedEqInstance<L, R> : Eq<Validated<L, R>> {

    fun EQL(): Eq<L>

    fun EQR(): Eq<R>

    override fun eqv(a: Validated<L, R>, b: Validated<L, R>): Boolean = when (a) {
        is Valid -> when (b) {
            is Invalid -> false
            is Valid -> EQR().eqv(a.a, b.a)
        }
        is Invalid -> when (b) {
            is Invalid -> EQL().eqv(a.e, b.e)
            is Valid -> false
        }
    }
}
