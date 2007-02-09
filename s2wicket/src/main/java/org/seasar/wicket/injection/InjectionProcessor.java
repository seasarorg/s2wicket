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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.seasar.framework.container.S2Container;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.WebPage;
import wicket.markup.html.panel.Panel;

/**
 * SeasarComponentアノテーションを持つフィールドにインジェクションを行う処理を持つクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class InjectionProcessor {
	
	/** フィールド値提供オブジェクト */
	private FieldValueProducer fieldValueProducer;
	
	/**
	 * フィールド値提供オブジェクトを返します。
	 * @return フィールド値提供オブジェクト
	 */
	FieldValueProducer getFieldValueProducer() {
		return fieldValueProducer;
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * このコンストラクタでは，フィールドフィルタとして{@link AnnotationFieldFilter}オブジェクトが適用されます。
	 * さらに，Seasarコンテナに{@link FieldFilter}インタフェースの実装オブジェクトが登録されている場合は，
	 * そのオブジェクトも適用されます。
	 * @param containerLocator Seasarコンテナロケータ
	 */
	InjectionProcessor(IS2ContainerLocator containerLocator) {
		super();
		// 引数チェック
		if (containerLocator == null)
			throw new IllegalArgumentException("containerLocator is null.");
		// デフォルトのフィールドフィルタ実装を生成
		List<FieldFilter> fieldFilters = createDefaultFieldFilters(containerLocator);
		// フィールド値供給オブジェクトを生成
		fieldValueProducer = new FieldValueProducer(containerLocator, fieldFilters);
	}
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param containerLocator Seasarコンテナロケータ
	 * @param fieldFilters フィールドフィルタが格納されたコレクション
	 */
	InjectionProcessor(IS2ContainerLocator containerLocator, List<FieldFilter> fieldFilters) {
		super();
		// 引数チェック
		if (containerLocator == null)
			throw new IllegalArgumentException("containerLocator is null.");
		if (fieldFilters == null)
			throw new IllegalArgumentException("fieldFilters is null.");
		if (fieldFilters.isEmpty())
			throw new IllegalArgumentException("fieldFilters is empty.");
		// フィールド値供給オブジェクトを生成
		fieldValueProducer = new FieldValueProducer(containerLocator, fieldFilters);
	}
	
	/**
	 * 指定されたオブジェクトが持つSeasarComponentアノテーションに対して，インジェクションを行います。
	 * @param target 処理対象のオブジェクト
	 */
	void inject(Object target) {
		// インジェクションを実行
		inject(target, fieldValueProducer);
	}
	
	/**
	 * 指定されたオブジェクトが持つSeasarComponentアノテーションに対して，指定された
	 * フィールド値供給オブジェクトからSeasarコンポーネントを取得して，それを呼び出す動的プロキシを
	 * インジェクションします。
	 * @param target 対象オブジェクト
	 * @param fieldValueProducer フィールド値供給オブジェクト
	 */
	private void inject(Object target, FieldValueProducer fieldValueProducer) {
		// 対象オブジェクトのクラスオブジェクトを取得
		Class<? extends Object> clazz = target.getClass();
		// インジェクション処理対象のフィールドを取得
		SupportedField[] targetFields = getTargetFields(clazz, fieldValueProducer);
		// フィールド毎に処理
		for (int i = 0; i < targetFields.length; i++) {
			Field targetField = targetFields[i].getField();
			// フィールドにアクセスできるようにする
			if (!targetField.isAccessible()) {
				targetField.setAccessible(true);
			}
			try {
				// 対象オブジェクトのフィールド値がnullかチェック
				if (targetField.get(target) == null) {
					// フィールド値とするプロキシオブジェクトを取得
					Object fieldValue = fieldValueProducer.getValue(targetFields[i]);
					// フィールドにインジェクト
					targetField.set(target, fieldValue);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Field injection failed.", e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Field injection failed.", e);
			}
		}
	}

	/**
	 * 処理対象のフィールドを返します。
	 * @param clazz 対象クラスオブジェクト
	 * @param fieldValueProducer フィールド値供給オブジェクト
	 * @return 処理対象のフィールドの配列
	 */
	private SupportedField[] getTargetFields(Class<? extends Object> clazz, FieldValueProducer fieldValueProducer) {
		// 結果を格納するコレクションを生成
		List<SupportedField> resultList = new ArrayList<SupportedField>();
		// 処理対象がなくなるか，Wicket提供クラスになるまで繰り返す
		while((clazz != null) && (!(isWicketClass(clazz)))) {
			// 定義されているフィールドを取得
			Field[] fields = clazz.getDeclaredFields();
			// フィールド毎に処理
			for (int i = 0; i < fields.length; i++) {
				// サポートされているフィールドかチェック
				FieldFilter fieldFilter = fieldValueProducer.isSupported(fields[i]);
				if (fieldFilter != null) {
					// 結果のコレクションに追加
					resultList.add(new SupportedField(fields[i], fieldFilter));
				}
			}
			// スーパークラスを取得し同様の検査を行う
			clazz = clazz.getSuperclass();
		}
		// 結果を返却
		return resultList.toArray(new SupportedField[0]);
	}
	
	/**
	 * 指定されたクラスがWicketで提供されたクラスかどうかを返します。
	 * @param clazz クラスオブジェクト
	 * @return WebPage, Page, Panel, MarkupContainer, Component クラスだった場合は true
	 */
	private boolean isWicketClass(Class clazz) {
		return (clazz.equals(WebPage.class))
			|| (clazz.equals(Page.class))
			|| (clazz.equals(Panel.class))
			|| (clazz.equals(MarkupContainer.class))
			|| (clazz.equals(Component.class));
	}
	
	/**
	 * デフォルトのフィールドフィルタが格納されたコレクションを返します。
	 * このメソッドでは，{@link AnnotationFieldFilter}オブジェクトと，
	 * Seasarコンテナに登録された{@link FieldFilter}インタフェースを実装したSeasarコンポーネントを
	 * コレクションに格納して返します。
	 * @param containerLocator Seasarコンテナロケータ
	 * @return {@link AnnotationFieldFilter}オブジェクトおよび{@link FieldFilter}インタフェースを実装し且つSeasarコンテナに登録されたオブジェクトを持つコレクション
	 */
	private List<FieldFilter> createDefaultFieldFilters(IS2ContainerLocator containerLocator) {
		List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();
		S2Container container = containerLocator.get();
		FieldFilter[] filters = (FieldFilter[])container.findComponents(FieldFilter.class);
		if (filters != null) {
			fieldFilters.addAll(Arrays.asList(filters));
		}
		fieldFilters.add(new AnnotationFieldFilter());
		return fieldFilters;
	}

}
