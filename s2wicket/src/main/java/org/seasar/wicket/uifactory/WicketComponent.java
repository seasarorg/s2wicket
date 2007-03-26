package org.seasar.wicket.uifactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wicketコンポーネントの生成をS2Wicketに指示することを示すアノテーションです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WicketComponent {
	
	/**
	 * 生成するコンポーネントのwicket:idを指定するための属性（任意）です。
	 * この属性を省略した場合，このアノテーションが付与されたフィールドの名前がwicket:idとして採用されます。
	 * @return wicket:idとする文字列
	 */
	public String wicketId() default "";
	
	/**
	 * 生成するコンポーネントを登録する親のコンテナコンポーネントのフィールド名を指定するための属性（任意）です。
	 * この属性を省略した場合，または"this"を指定した場合は，
	 * このアノテーションが付与されたフィールドが所属するコンポーネントが親コンテナコンポーネントとして採用されます。
	 * @return 親のコンテナコンポーネントのフィールド名
	 */
	public String parent() default "";
	
	/**
	 * 生成するコンポーネントと関連付けを行うモデルオブジェクトのフィールド名を指定するための属性（任意）です。
	 * 
	 * @return
	 */
	public String modelName() default "";
	
	public String modelProperty() default "";
	
}
