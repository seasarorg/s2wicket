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
import java.util.LinkedList;
import java.util.List;

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
	
	/** Seasarコンテナロケータ */
	private IS2ContainerLocator containerLocator;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param containerLocator Seasarコンテナロケータ
	 */
	InjectionProcessor(IS2ContainerLocator containerLocator) {
		super();
		if (containerLocator == null)
			throw new IllegalArgumentException("containerLocator is null.");
		this.containerLocator = containerLocator;
	}
	
	/**
	 * 指定されたオブジェクトが持つSeasarComponentアノテーションに対して，インジェクションを行います。
	 * @param target 処理対象のオブジェクト
	 */
	void inject(Object target) {
		// フィールド値供給オブジェクトを生成
		FieldValueProducer fieldValueProducer = new FieldValueProducer(containerLocator);
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
		Field[] targetFields = getTargetFields(clazz, fieldValueProducer);
		// フィールド毎に処理
		for (int i = 0; i < targetFields.length; i++) {
			// フィールドにアクセスできるようにする
			if (!targetFields[i].isAccessible()) {
				targetFields[i].setAccessible(true);
			}
			try {
				// 対象オブジェクトのフィールド値がnullかチェック
				if (targetFields[i].get(target) == null) {
					// フィールド値とするプロキシオブジェクトを取得
					Object fieldValue = fieldValueProducer.getValue(targetFields[i]);
					// フィールドにインジェクト
					targetFields[i].set(target, fieldValue);
				}
			} catch(AnnotationNotPresentsException e) {
				// N/A
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
	private Field[] getTargetFields(Class<? extends Object> clazz, FieldValueProducer fieldValueProducer) {
		// 結果を格納するコレクションを生成
		List<Field> resultList = new LinkedList<Field>();
		// 処理対象がなくなるか，Wicket提供クラスになるまで繰り返す
		while((clazz != null) && (!(isWicketClass(clazz)))) {
			// 定義されているフィールドを取得
			Field[] fields = clazz.getDeclaredFields();
			// フィールド毎に処理
			for (int i = 0; i < fields.length; i++) {
				// サポートされているフィールドかチェック
				if (fieldValueProducer.isSupported(fields[i])) {
					// 結果のコレクションに追加
					resultList.add(fields[i]);
				}
			}
			// スーパークラスを取得し同様の検査を行う
			clazz = clazz.getSuperclass();
		}
		// 結果を返却
		return resultList.toArray(new Field[0]);
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

}
