package pl.bihuniak.piotr.autoconfigure.api;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

public interface AuthorizationRuleProvider<T extends Annotation> {
	MainRule<T> mainRule();

	default Collection<Rule<T, ?>> preRulesForSpecificArgumentsTypes() {
		return Set.of();
	}

	default Collection<Rule<T, ?>> postRulesForSpecificReturnTypes() {
		return Set.of();
	}
}
