package arrow.mtl.instances

import arrow.core.*
import arrow.instance
import arrow.instances.traverse
import arrow.instances.traverseFilter
import arrow.mtl.MonadFilter
import arrow.mtl.TraverseFilter
import arrow.typeclasses.Applicative

@instance(Option::class)
interface OptionTraverseFilterInstance : TraverseFilter<ForOption> {
    override fun <A> filter(fa: OptionOf<A>, f: kotlin.Function1<A, kotlin.Boolean>): Option<A> =
            fa.reify().filter(f)

    override fun <G, A, B> traverseFilter(fa: OptionOf<A>, f: kotlin.Function1<A, arrow.Kind<G, Option<B>>>, GA: Applicative<G>): arrow.Kind<G, Option<B>> =
            fa.reify().traverseFilter(f, GA)

    override fun <A, B> map(fa: OptionOf<A>, f: kotlin.Function1<A, B>): Option<B> =
            fa.reify().map(f)

    override fun <G, A, B> traverse(fa: OptionOf<A>, f: kotlin.Function1<A, arrow.Kind<G, B>>, GA: Applicative<G>): arrow.Kind<G, Option<B>> =
            fa.reify().traverse(f, GA)

    override fun <A> exists(fa: OptionOf<A>, p: kotlin.Function1<A, kotlin.Boolean>): kotlin.Boolean =
            fa.reify().exists(p)

    override fun <A, B> foldLeft(fa: OptionOf<A>, b: B, f: kotlin.Function2<B, A, B>): B =
            fa.reify().foldLeft(b, f)

    override fun <A, B> foldRight(fa: OptionOf<A>, lb: Eval<B>, f: kotlin.Function2<A, Eval<B>, Eval<B>>): Eval<B> =
            fa.reify().foldRight(lb, f)

    override fun <A> forall(fa: OptionOf<A>, p: kotlin.Function1<A, kotlin.Boolean>): kotlin.Boolean =
            fa.reify().forall(p)

    override fun <A> isEmpty(fa: OptionOf<A>): kotlin.Boolean =
            fa.reify().isEmpty()

    override fun <A> nonEmpty(fa: OptionOf<A>): kotlin.Boolean =
            fa.reify().nonEmpty()
}

@instance(Option::class)
interface OptionMonadFilterInstance : MonadFilter<ForOption> {
    override fun <A> empty(): Option<A> =
            Option.empty()

    override fun <A, B> ap(fa: OptionOf<A>, ff: OptionOf<kotlin.Function1<A, B>>): Option<B> =
            fa.reify().ap(ff)

    override fun <A, B> flatMap(fa: OptionOf<A>, f: kotlin.Function1<A, OptionOf<B>>): Option<B> =
            fa.reify().flatMap(f)

    override fun <A, B> tailRecM(a: A, f: kotlin.Function1<A, OptionOf<Either<A, B>>>): Option<B> =
            Option.tailRecM(a, f)

    override fun <A, B> map(fa: OptionOf<A>, f: kotlin.Function1<A, B>): Option<B> =
            fa.reify().map(f)

    override fun <A> pure(a: A): Option<A> =
            Option.pure(a)

    override fun <A> filter(fa: OptionOf<A>, f: kotlin.Function1<A, kotlin.Boolean>): Option<A> =
            fa.reify().filter(f)
}
