/*
 * $Id$
 * 
 * ==============================================================================
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

package org.seasar.wicket.injection.fieldfilters;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Seasarコンポーネントのためのプレースホルダとなるフィールドにタグ付けするためのアノテーションです。
 * <p>このアノテーションが付与されたフィールドには，Seasarコンテナにより管理されているコンポーネントがインジェクションされます。
 * ただし，このアノテーションを使用可能にするには，{@link AnnotationFieldFilter}フィールドフィルタがS2Wicketに登録されている
 * ことが必要です。</p>
 * <p>このアノテーションは，Wicketが管理するコンポーネントクラスのフィールドに対して付与します。
 * 例えば，WebPageクラスのサブクラス内で以下のように使用します。</p>
 * <pre>
 * public class InputPage extends WebPage {
 * 
 *     &#064;SeasarComponent(name="orderService")
 *     private OrderService orderService;
 * 
 *     public InputPage() {
 *         ...
 *         Form form = new Form() {
 *             public void onSubmit() {
 *                 orderService.order(...);
 *             }
 *         }
 *         ...
 *     }
 * }
 * </pre>
 * <p>上記のコードでは，Seasarコンテナに"orderService"という名前で登録されているコンポーネントが呼び出し対象となります。</p>
 * <p>Wicketでは，管理下にあるコンポーネントはHTTPセッションに格納されるため，永続される可能性を持っています。
 * そのため，直接Seasarコンポーネントオブジェクトをフィールドに代入してしまうと，永続化対象となってしまいます。
 * これを解決するために，Seasarコンポーネントを呼び出す処理を持つ動的プロキシオブジェクトがフィールドにインジェクションされます。</p>
 * 
 * @see AnnotationFieldFilter
 * @see FieldFilter
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface SeasarComponent {
	
	/**
	 * コンポーネントの名前を明示的に指定するための属性（任意）です。
	 * もしこれが指定されなかった場合は，このフィールドの型によるルックアップが行われます。
	 * @return 名前
	 */
	public String name() default "";

}
