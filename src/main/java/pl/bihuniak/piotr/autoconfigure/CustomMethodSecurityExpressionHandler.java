/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package pl.bihuniak.piotr.autoconfigure;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import pl.bihuniak.piotr.autoconfigure.api.AuthorizationRuleProvider;

class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
	private final AuthorizationRuleProvider annotationProvider;
	private final AnnotationResolver annotationResolver;

	public CustomMethodSecurityExpressionHandler(AuthorizationRuleProvider authorizationPredicatesProvider,
	                                             AnnotationResolver annotationResolver) {
		this.annotationProvider = authorizationPredicatesProvider;
		this.annotationResolver = annotationResolver;
	}

	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
		Authentication authentication, MethodInvocation invocation) {
		CustomMethodSecurityExpressionRoot root =
			new CustomMethodSecurityExpressionRoot(authentication, annotationProvider, annotationResolver);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(this.trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		return root;
	}
}
