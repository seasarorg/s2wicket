package org.seasar.wicket.uifactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wicketモデルフィールドの生成をS2Wicketに指示することを示すアノテーションです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WicketModel {

	/**
	 * モデルの種別を指定するための属性（任意）です。
	 * この属性を省略した場合，{@link ModelType#PROPERTY}が適用されます。
	 * @return モデルの種別
	 */
	public ModelType type() default ModelType.PROPERTY;
	
}
