package org.seasar.wicket.uifactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WicketPopulateItem {
	
	public String wicketId() default "";
	
	public ModelType modelType() default ModelType.BASIC;
	
	public String modelName() default "";
	
	public String modelProperty() default "";
	
	public WicketAction[] actions() default {};

}
