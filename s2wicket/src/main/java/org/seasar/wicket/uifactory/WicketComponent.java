/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.seasar.wicket.uifactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wicketコンポーネントの生成をS2Wicketに指示するためのアノテーションです。<br />
 * <p>Wicketを使用した開発では，ページを構成する複数のコンポーネントを生成し，それを親コンポーネントに登録するという，
 * コンポーネントツリーの構築処理を記述する必要があります。これは，SwingやSWTに代表されるGUIアプリケーションにおける
 * 画面の構築処理の記述と非常に近いものです。
 * 
 * 
 * @see WicketAction
 * @see WicketModel
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
	
	public WicketAction[] actions() default {};
	
}
