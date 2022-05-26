package pl.bihuniak.piotr.autoconfigure;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pl.bihuniak.piotr.autoconfigure.api.AuthorizationRuleProvider;
import pl.bihuniak.piotr.autoconfigure.api.Id;
import pl.bihuniak.piotr.autoconfigure.api.Id2;
import pl.bihuniak.piotr.autoconfigure.api.MainRule;
import pl.bihuniak.piotr.autoconfigure.api.Role;
import pl.bihuniak.piotr.autoconfigure.api.Rule;
import pl.bihuniak.piotr.autoconfigure.api.TestAuthorize;
import pl.bihuniak.piotr.autoconfigure.api.TestUser;

import java.util.Collection;
import java.util.Set;

@Component
class AuthorizationPredicatesProviderMockImpl implements AuthorizationRuleProvider<TestAuthorize> {

	@Override
	public MainRule<TestAuthorize> mainRule() {
		return new MainRule<>(
			(Authentication auth, TestAuthorize ann) ->
				((UserProvider) auth.getPrincipal()).getCurrent().roles.contains(ann.role())
		);
	}

	@Override
	public Collection<Rule<TestAuthorize, ?>> preRulesForSpecificArgumentsTypes() {
		return Set.of(
			new Rule<>(Id.class,
					(Authentication auth, TestAuthorize ann, Id id) -> {
						TestUser current = ((UserProvider) auth.getPrincipal()).getCurrent();
						return current.roles.contains(ann.role()) && current.resources.contains(id);
					}
				),
				new Rule<>(Id2.class,
					(Authentication auth, TestAuthorize ann, Id2 id) -> {
						TestUser current = ((UserProvider) auth.getPrincipal()).getCurrent();
						return current.roles.contains(ann.role()) && current.resources.contains(id);
					}
				)
			);
	}

	@Override
	public Collection<Rule<TestAuthorize, ?>> postRulesForSpecificReturnTypes() {
		return Set.of(
			new Rule<>(Role.class,
				(Authentication auth, TestAuthorize ann, Role role) -> {
					TestUser current = ((UserProvider) auth.getPrincipal()).getCurrent();
					return current.roles.contains(ann.role()) && current.roles.contains(role);
				}
			));
	}
}
