package pl.bihuniak.piotr.autoconfigure.api;

import org.springframework.security.core.Authentication;

import java.util.Objects;

public final class Rule<T, B> {
	public final TriPredicate<Authentication, T, B> predicate;
	public final Class<B> targetClass;

	public Rule(Class<B> targetClass, TriPredicate<Authentication, T, B> triPredicate) {
		this.predicate = triPredicate;
		this.targetClass = targetClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Rule<?, ?> rule = (Rule<?, ?>) o;
		return Objects.equals(predicate, rule.predicate) && Objects.equals(targetClass, rule.targetClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(predicate, targetClass);
	}

	@Override
	public String toString() {
		return "Rule{" +
			"predicate=" + predicate +
			", targetClass=" + targetClass +
			'}';
	}
}
