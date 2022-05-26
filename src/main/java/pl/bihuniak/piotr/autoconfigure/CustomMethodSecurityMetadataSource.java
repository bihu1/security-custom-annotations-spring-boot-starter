/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package pl.bihuniak.piotr.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.ClassUtils;
import pl.bihuniak.piotr.autoconfigure.api.Order;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

class CustomMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final PrePostInvocationAttributeFactory attributeFactory;
	private final Class<? extends Annotation> annotationClass;

	CustomMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory,
	                                   Class<? extends Annotation>  annotationClass) {
		this.attributeFactory = attributeFactory;
		this.annotationClass = annotationClass;
	}

	@Override
	public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
		Method properMethod = ClassUtils.getMostSpecificMethod(method, targetClass);

		Annotation methodAnnotation = properMethod.getAnnotation(annotationClass);
		Annotation classAnnotation = targetClass.getAnnotation(annotationClass);
		Annotation annotation = ofNullable(methodAnnotation).orElse(classAnnotation);
		if(annotation == null)
			return emptyList();

		String [] ids = new String[0];
		try {
			ids = (String []) annotation.getClass().getDeclaredMethod("ids", null)
				.invoke(annotation);

		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			LOG.warn("WARNING - ids method in annotation {} is not created", annotation.getClass().getName());
		}

		String order = null;
		try {
			order = annotation.getClass().getDeclaredMethod("order", null)
				.invoke(annotation).toString();

		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			LOG.warn("WARNING - order method in annotation {} is not created", annotation.getClass().getName());
		}

		String parameters = Arrays.stream(properMethod.getParameters())
			.map(parameter -> parameter.getType().getName())
			.collect(Collectors.joining(";"));

		String idPartSpEl = "";
		if (ids.length != 0) {
			idPartSpEl = "," + Arrays.stream(ids).collect(Collectors.joining(",#", "#", ""));
		}
		String preSpEl = String.format("preValid('%s', '%s', '%s'%s)",
			method.getDeclaringClass().getName(), method.getName(), parameters, idPartSpEl);
		String postSpEl = String.format("postValid('%s', '%s', '%s')",
			method.getDeclaringClass().getName(), method.getName(), parameters);
		if(order == null || order.equalsIgnoreCase("BOTH"))
			return List.of(
				attributeFactory.createPreInvocationAttribute(null, null, preSpEl),
				attributeFactory.createPostInvocationAttribute(null, postSpEl)
			);
		else if(order.equalsIgnoreCase("PRE")) {
			return singletonList(attributeFactory.createPreInvocationAttribute(null, null, preSpEl));
		}
		else if (order.equalsIgnoreCase("POST")) {
			return List.of(
				attributeFactory.createPreInvocationAttribute(null, null, "ok()"),
				attributeFactory.createPostInvocationAttribute(null, postSpEl)
			);
		}
		throw new IllegalArgumentException("Unsupported order type " + order + ", please chose one of the follow: " + Arrays.toString(Order.values()));
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}
}
