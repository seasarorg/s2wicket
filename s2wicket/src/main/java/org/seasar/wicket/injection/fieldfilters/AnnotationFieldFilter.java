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

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.seasar.wicket.injection.FieldFilter;
import org.seasar.wicket.injection.SeasarComponentInjectionListener;

/**
 * {@link SeasarComponent}アノテーションが付与されたフィールドをインジェクション対象とする処理を持つフィルタ実装クラスです。<br />
 * <p>このフィールドフィルタは，{@link SeasarComponent}アノテーションをマーカーとして，
 * それが付与されたフィールドについて，Seasarコンテナ管理下のコンポーネントオブジェクトをインジェクション対象と判断します。</p>
 * <p>{@link SeasarComponentInjectionListener}クラスのインスタンスを生成する際に，コンストラクタに
 * {@link FieldFilter}インタフェースの実装オブジェクトのコレクションを指定しなかった場合は，
 * このクラスの実装オブジェクトが自動的に使用されるようになります。また，独自に{@link FieldFilter}インタフェースの
 * 実装クラスを作成して，このクラスと併用したい場合は，以下のように{@link SeasarComponentInjectionListener}オブジェクト
 * に登録します。</p>
 * <pre>
 * MyFieldFilter myFieldFilter = ...;
 * AnnotationFieldFilter annotFieldFilter = new AnnotationFieldFilter();
 * List<FieldFilter> filters = new ArrayList<FieldFilter>(2);
 * filters.add(myFieldFilter);
 * filters.add(annotFieldFilter);
 * addComponentInstantiationListener(
 *     new SeasarComponentInjectionListener(this, filters));
 * </pre>
 * <p>このフィールドフィルタ実装では，{@link SeasarComponent}アノテーションの{@link SeasarComponent#name()}プロパティで
 * 指定された名前が，Seasarコンテナからのルックアップ時のコンポーネント名として採用します。もし{@link SeasarComponent#name()}
 * プロパティが記述されていなかった場合は，対象フィールドの型がルックアップ時のキーになります。</p>
 * 
 * @author Yoichiro Tanaka
 * @since 1.1.0
 */
public class AnnotationFieldFilter implements FieldFilter {

	/**
	 * 指定されたフィールドがインジェクションの対象としてサポートされているかどうかを返します。
	 * この実装では，指定されたフィールドにSeasarComponentアノテーションが付与されているかどうかをチェックし，
	 * 付与されている場合はインジェクション対象として true を返します。
	 * @param field フィールド
	 * @return SeasarComponentアノテーションが付与されているフィールドだった場合は true
	 * @see org.seasar.wicket.injection.FieldFilter#isSupported(java.lang.reflect.Field)
	 */
	public boolean isSupported(Field field) {
		// 引数チェック
		if (field == null)
			throw new IllegalArgumentException("field is null.");
		// SeasarComponentアノテーションが付与されているかどうかを返却
		return field.isAnnotationPresent(SeasarComponent.class);
	}

	/**
	 * 指定されたフィールドについて，ルックアップするSeasarコンポーネントのコンポーネント名を返します。
	 * この実装では，フィールドに付与されたSeasarComponentアノテーションのname属性値を
	 * コンポーネント名として返します。もしname属性がなかった場合は，nullを返します。
	 * @param field フィールド
	 * @return SeasarComponentアノテーションのname属性値
	 * @throws IllegalArgumentException {{@link #isSupported(Field)}メソッドの呼び出し結果がfalseのフィールドが与えられたとき
	 * @see org.seasar.wicket.injection.FieldFilter#getLookupComponentName(java.lang.reflect.Field)
	 */
	public String getLookupComponentName(Field field) {
		// 引数チェック
		if (!isSupported(field)) {
			throw new IllegalArgumentException("field is not supported.");
		}
		// SeasarComponentアノテーションを取得
		SeasarComponent annotation = field.getAnnotation(SeasarComponent.class);
		// name属性値を取得
		String name = annotation.name();
		// 結果を返却
		return (StringUtils.isEmpty(name)) ? null : name;
	}

}
