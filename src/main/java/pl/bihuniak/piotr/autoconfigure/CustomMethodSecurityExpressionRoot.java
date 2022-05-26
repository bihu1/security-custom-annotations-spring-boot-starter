/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package pl.bihuniak.piotr.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import pl.bihuniak.piotr.autoconfigure.api.AuthorizationRuleProvider;
import pl.bihuniak.piotr.autoconfigure.api.Rule;
import pl.bihuniak.piotr.autoconfigure.api.TriPredicate;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;


class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final BiPredicate<Authentication, Annotation> mainPredicate;
	private final Map<Class<Object>, Set<TriPredicate<Authentication, Annotation, Object>>> prePredicatesForSpecificArgumentsTypes;
	private final Map<Class<Object>, Set<TriPredicate<Authentication, Annotation, Object>>> postPredicatesForSpecificArgumentsTypes;
	private final AnnotationResolver annotationResolver;

	private Object filterObject;
	private Object returnObject;
	private Object target;


	CustomMethodSecurityExpressionRoot(Authentication authentication,
	                                   AuthorizationRuleProvider annotationProvider, AnnotationResolver annotationResolver) {
		super(authentication);
		this.annotationResolver = annotationResolver;
		mainPredicate = (BiPredicate<Authentication, Annotation>)annotationProvider.mainRule().predicate;
		prePredicatesForSpecificArgumentsTypes = (Map<Class<Object>, Set<TriPredicate<Authentication, Annotation, Object>>>)
			annotationProvider.preRulesForSpecificArgumentsTypes().stream().collect(groupingBy((Rule rule) -> rule.targetClass, mapping((Rule rule) -> rule.predicate, toSet())));
		postPredicatesForSpecificArgumentsTypes = (Map<Class<Object>, Set<TriPredicate<Authentication, Annotation, Object>>>)
			annotationProvider.postRulesForSpecificReturnTypes().stream().collect(groupingBy((Rule rule) -> rule.targetClass, mapping((Rule rule) -> rule.predicate, toSet())));
	}

	public boolean preValid(String clazz, String method, String parameters) {
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;

		return mainPredicate.test(authentication, annotationResolver.get(clazz, method, parameters));
	}

	public boolean preValid(String clazz, String method, String parameters, Object... ids) {
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;

		Annotation annotation = annotationResolver.get(clazz, method, parameters);
		Set<String> classesWithoutRules = Arrays.stream(ids)
			.filter(x -> prePredicatesForSpecificArgumentsTypes.getOrDefault(x.getClass(), emptySet()).isEmpty())
			.map(x -> x.getClass().getName())
			.collect(toSet());
		if (!classesWithoutRules.isEmpty()) {
			LOG.warn("Arguments types {} don't have rule - main rule will be used", classesWithoutRules);
			return mainPredicate.test(authentication, annotation);
		}

		return Arrays.stream(ids)
			.allMatch(id ->
				prePredicatesForSpecificArgumentsTypes.get(id.getClass()).stream()
					.allMatch(predicate -> predicate.test(authentication, annotation, id))
			);
	}

	public boolean postValid(String clazz, String method, String parameters) {
		if(!authentication.isAuthenticated() || isAnonymousUser())
			return false;
		if(postPredicatesForSpecificArgumentsTypes.isEmpty())
			return true;

		Annotation annotation = annotationResolver.get(clazz, method, parameters);
		Set<TriPredicate<Authentication, Annotation, Object>> predicates =
			postPredicatesForSpecificArgumentsTypes.get(returnObject.getClass());
		if (predicates.isEmpty()) {
			LOG.warn("Returned type {} doesn't have rule - main rule will be used", returnObject.getClass().getName());
			return mainPredicate.test(authentication, annotation);
		}

		return predicates.stream()
			.allMatch(predicate -> predicate.test(authentication, annotation, returnObject));
	}

	public boolean ok() {
		return true;
	}

	private boolean isAnonymousUser() {
		return authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.anyMatch(role -> role.equals("ROLE_ANONYMOUS"));
	}

	@Override
	public void setFilterObject(Object filterObject) {
		this.filterObject = filterObject;
	}

	@Override
	public Object getFilterObject() {
		return filterObject;
	}

	@Override
	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	@Override
	public Object getReturnObject() {
		return returnObject;
	}

	@Override
	public Object getThis() {
		return target;
	}

	public void setThis(Object target) {
		this.target = target;
	}
}
