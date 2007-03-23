package org.seasar.wicket.uifactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wicketコンポーネントを生成することを示すアノテーションです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WicketComponent {
	
//	/**
//	 * Wicketコンポーネントを生成する処理を行うメソッドの名前です。
//	 * この属性を省略した場合は，"create[フィールド名]Component()"という名前のメソッドを検索し，
//	 * もしあればそのメソッドをコール，もしなければWicketコンポーネントの生成をS2Wicket自身が行います。
//	 * @return Wicketコンポーネントを生成する処理を行うメソッドの名前
//	 */
//	public String factoryMethodName() default "";
	
	public String wicketId() default "";
	
	public String parent() default "";
	
	public String model() default "";
	
	public String property() default "";
	
}
