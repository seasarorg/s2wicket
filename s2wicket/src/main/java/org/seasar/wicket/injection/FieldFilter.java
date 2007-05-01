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

package org.seasar.wicket.injection;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import org.seasar.wicket.injection.fieldfilters.AnnotationFieldFilter;

/**
 * 指定されたフィールドについて，それがインジェクション対象かどうかを判断し，
 * インジェクション対象だった場合は，ルックアップするためのSeasarコンポーネント名を提供する
 * 処理を規定したインタフェースです。<br />
 * <p>このインタフェースは，S2Wicketがインジェクション対象とするフィールドかどうかを判断する処理が規定されています。
 * 開発者は，このインタフェースの実装クラスを作成することで，独自にインジェクション対象とするフィールドの判断基準をS2Wicketに
 * 登録することができます。また，インジェクション対象と判断したフィールドについて，そのフィールドに
 * インジェクションするSeasarコンテナ管理下のコンポーネントオブジェクトを，どのようなコンポーネント名でルックアップするかについても
 * 決定します。</p>
 * <p>例えば，文字列"Service"で終わっているフィールド名についてインジェクション対象とし，
 * Seasarコンテナからのルックアップ時のコンポーネント名にフィールド名を使用する，というルールをS2Wicketに
 * 登録したい場合は，以下のような実装クラスを作成します。</p>
 * <pre>
 * public class ServiceFieldFilter implements FieldFilter {
 *     public boolean isSupported(Field field) {
 *         String fieldName = field.getName();
 *         return fieldName.endsWith("Service");
 *     }
 *     public String getLookupComponentName(Field field) {
 *         return field.getName();
 *     }
 * }
 * </pre>
 * <p>上記の自作フィールドフィルタをS2Wicketに登録する方法は，以下の２つから選択できます。
 * <ul>
 * <li>{@link SeasarComponentInjectionListener}クラスのコンストラクタに明示的に渡す。</li>
 * <li>Seasarコンテナに登録する。</li>
 * </ul>
 * 最初の方法は，使用するフィールドフィルタをJavaソースコード上で明示的にインスタンス生成および登録を記述する方式です。
 * 特に{@link AnnotationFieldFilter}による{@link SeasarComponent}アノテーションをマーカーとするインジェクション
 * を行いたくない場合は，最初の方法を採用する必要があります。具体的には，以下のように{@link FieldFilter}インタフェースの
 * 実装オブジェクトを{@link List}コレクションに格納し，{@link SeasarComponentInjectionListener}
 * クラスのコンストラクタに渡します。</p>
 * <pre>
 * FieldFilter myFilter = new ServiceFieldFilter();
 * List<FieldFilter> filters = new ArrayList<FieldFilter>(1);
 * filters.add(myFilter);
 * addComponentInstantiationListener(
 *     new SeasarComponentInjectionListener(this, filters));
 * </pre>
 * <p>S2Wicketは{@link FieldFilter}インタフェースの実装オブジェクトをSeasarコンテナに
 * 登録されたコンポーネントオブジェクトから検索して，自動的にそれらを適用します。それが２番目の方法です。</p>
 * <pre>
 * &lt;components&gt;
 *     &lt;component name="myFilter"
 *         class="ServiceFieldFilter" /&gt;
 * &lt;/components&gt;
 * </pre>
 * <p>{@link SeasarComponentInjectionListener}クラスのコンストラクタには，{@link FieldFilter}
 * オブジェクトのコレクションを引数に持たないものがありますので，この方法ではそれを使用します。
 * 明示的に指定する最初の方法に比べて，Seasarコンテナ内からの{@link FieldFilter}インタフェースの実装オブジェクトの検索に加えて，
 * {@link AnnotationFieldFilter}オブジェクトが暗黙的に適用されることに注意する必要があります。</p>
 * 
 * @see AnnotationFieldFilter
 * @author Yoichiro Tanaka
 * @since 1.1.0
 */
public interface FieldFilter extends Serializable {

	/**
	 * 指定されたフィールドがインジェクションの対象としてサポートされているかどうかを返します。
	 * @param field フィールド
	 * @return サポートされていれば true
	 */
	public boolean isSupported(Field field);
	
	/**
	 * 指定されたフィールドについて，ルックアップするSeasarコンポーネントのコンポーネント名を返します。
	 * このメソッドに渡されるfieldオブジェクトは，{@link #isSupported(Field)}メソッドの呼び出し結果が
	 * true のオブジェクトのみとなります。
	 * もしSeasarコンポーネント名ではなく，フィールドの型でルックアップを行う場合は，nullを返却してください。
	 * @param field フィールド
	 * @return ルックアップするSeasarコンポーネント名。もしフィールドの型でルックアップする場合は null
	 */
	public String getLookupComponentName(Field field);
	
}
