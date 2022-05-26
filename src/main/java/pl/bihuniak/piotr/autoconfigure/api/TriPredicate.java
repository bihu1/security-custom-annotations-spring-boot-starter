package pl.bihuniak.piotr.autoconfigure.api;

import java.util.Objects;

@FunctionalInterface
public interface TriPredicate <T,U,Z> {

	boolean test(T t, U u, Z z);

	default TriPredicate<T, U, Z> and(TriPredicate<? super T, ? super U, ? super Z> other) {
		Objects.requireNonNull(other);
		return (T t, U u, Z z) -> test(t, u, z) && other.test(t, u, z);
	}

	default TriPredicate<T, U, Z> negate() {
		return (T t, U u, Z z) -> !test(t, u, z);
	}

	default TriPredicate<T, U, Z> or(TriPredicate<? super T, ? super U, ? super Z> other) {
		Objects.requireNonNull(other);
		return (T t, U u, Z z) -> test(t, u, z) || other.test(t, u, z);
	}
}
