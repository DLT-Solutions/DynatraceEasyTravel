package com.dynatrace.easytravel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Methods and fields annotated with @TestOnly must NOT be used in production
 * code, but only for testing.
 * I.e. these elements were only introduced in order to reach testability that
 * cannot be reached easily by other means. Note that e.g. adding a missing or
 * required getter method can be a valuable add-on to the class' interface and
 * should be therefore not automatically annotated in general.
 *
 * Please, use with care!
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
public @interface TestOnly {
	// Empty on purpose
}
