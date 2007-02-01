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

/**
 * SeasarComponentアノテーションが付与されたフィールドにセットする値を供給するクラスです。
 * 実際のSeasarコンポーネントのメソッドの呼び出しを代行するプロキシオブジェクトを提供します。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class FieldValueProducer {
	
	/** Seasarコンテナロケータ */
	private IS2ContainerLocator containerLocator;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param containerLocator コンテナロケータ
	 */
	FieldValueProducer(IS2ContainerLocator containerLocator) {
		super();
		if (containerLocator == null)
			throw new IllegalArgumentException("containerLocator is null.");
		this.containerLocator = containerLocator;
	}
	
	/**
	 * 指定されたフィールドに対応するプロキシオブジェクトを生成して返します。
	 * @param field フィールドオブジェクト
	 * @return プロキシオブジェクト
	 */
	Object getValue(Field field) throws AnnotationNotPresentsException {
		// 引数チェック
		if (field == null)
			throw new IllegalArgumentException("field is null.");
		// フィールドにアノテーションが付与されているかチェック
		if (field.isAnnotationPresent(SeasarComponent.class)) {
			// アノテーションオブジェクトを取得
			SeasarComponent annotation = field.getAnnotation(SeasarComponent.class);
			// コンポーネント名を取得
			String componentName = annotation.name();
			// Seasarコンポーネントリゾルバを生成
			ComponentResolver resolver = new ComponentResolver(componentName, field.getType(), containerLocator);
			// プロキシを生成
			Object proxy = ProxyFactory.create(field.getType(), resolver);
			// プロキシを返却
			return proxy;
		} else {
			throw new AnnotationNotPresentsException();
		}
	}
	
	/**
	 * 指定されたフィールドがインジェクションの対象としてサポートされているかどうかを返します。
	 * @param field フィールド
	 * @return サポートされていれば true
	 */
	boolean isSupported(Field field) {
		return field.isAnnotationPresent(SeasarComponent.class);
	}

}
