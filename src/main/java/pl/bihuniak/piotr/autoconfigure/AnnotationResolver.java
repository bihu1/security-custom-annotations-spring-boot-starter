package pl.bihuniak.piotr.autoconfigure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.ofNullable;

class AnnotationResolver {
	private final Class<? extends Annotation> annotationClass;

	AnnotationResolver(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	Annotation get(String clazz, String method, String parameters) {
		try {
			Class<?>[] params;
			if(parameters.isBlank())
				params = new Class[0];
			else
				params = Arrays.stream(parameters.split(";"))
					.map(this::getaClass)
					.toArray(Class[]::new);

			Class<?> aClass = getaClass(clazz);
			Annotation annotation = aClass.getDeclaredMethod(method, params).getAnnotation(annotationClass);
			if(annotation == null)
				return aClass.getAnnotation(annotationClass);
			else
				return annotation;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> getaClass(String clazz) {
		try {
			return Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
