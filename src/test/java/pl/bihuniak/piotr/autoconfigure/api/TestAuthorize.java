package pl.bihuniak.piotr.autoconfigure.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface TestAuthorize {
	Role role();
	String[] ids() default {};
	String order() default "PRE";
}
