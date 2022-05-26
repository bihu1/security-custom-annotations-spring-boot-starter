package pl.bihuniak.piotr.autoconfigure.api;

import org.springframework.security.core.Authentication;

import java.util.Objects;
import java.util.function.BiPredicate;

public final class MainRule<T> {
	public final BiPredicate<Authentication, T> predicate;

	public MainRule(BiPredicate<Authentication, T> biPredicate) {
		this.predicate = biPredicate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MainRule<?> mainRule = (MainRule<?>) o;
		return Objects.equals(predicate, mainRule.predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(predicate);
	}

	@Override
	public String toString() {
		return "MainRule{" +
			"predicate=" + predicate +
			'}';
	}
}
