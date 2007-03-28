package org.seasar.wicket.uifactory;

public @interface WicketAction {
	
	public String method();
	
	public String exp() default "";
	
	public String responsePage() default "";
	
}
