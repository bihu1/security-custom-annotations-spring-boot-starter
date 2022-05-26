package pl.bihuniak.piotr.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import pl.bihuniak.piotr.autoconfigure.api.AuthorizationRuleProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class CustomSecurityAnnotationConfig extends GlobalMethodSecurityConfiguration {

	private final AuthorizationRuleProvider annotationProvider;
	private final AnnotationResolver annotationResolver;
	private final Class<? extends Annotation> actualAnnotationType;

	CustomSecurityAnnotationConfig(AuthorizationRuleProvider annotationProvider) {
		this.annotationProvider = annotationProvider;
		actualAnnotationType =
			(Class<? extends Annotation>) ((ParameterizedType) annotationProvider.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
		annotationResolver = new AnnotationResolver(actualAnnotationType);
	}

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return new CustomMethodSecurityExpressionHandler(annotationProvider, annotationResolver);
	}

	@Override
	public MethodSecurityMetadataSource methodSecurityMetadataSource() {
		DelegatingMethodSecurityMetadataSource methodSecurityMetadataSource =
			(DelegatingMethodSecurityMetadataSource)super.methodSecurityMetadataSource();
		methodSecurityMetadataSource.getMethodSecurityMetadataSources()
			.add(getFurmsAbstractMethodSecurityMetadataSource());
		return methodSecurityMetadataSource;
	}

	private CustomMethodSecurityMetadataSource getFurmsAbstractMethodSecurityMetadataSource(){
		ExpressionBasedAnnotationAttributeFactory attributeFactory =
			new ExpressionBasedAnnotationAttributeFactory(super.getExpressionHandler());
		return new CustomMethodSecurityMetadataSource(attributeFactory, actualAnnotationType);
	}
}
