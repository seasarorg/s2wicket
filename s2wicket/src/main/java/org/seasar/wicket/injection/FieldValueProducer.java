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
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * SeasarComponentアノテーションが付与されたフィールドにセットする値を供給するクラスです。
 * 実際のSeasarコンポーネントのメソッドの呼び出しを代行するプロキシオブジェクトを提供します。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class FieldValueProducer {
	
	/** Seasarコンテナロケータ */
	private IS2ContainerLocator containerLocator;
	
	/** フィールドフィルタのコレクション */
	private List<FieldFilter> fieldFilterList;
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param containerLocator コンテナロケータ
	 * @param fieldFilterList フィールドフィルタが格納されたコレクション
	 */
	FieldValueProducer(IS2ContainerLocator containerLocator, List<FieldFilter> fieldFilterList) {
		super();
		// 引数チェック
		if (containerLocator == null)
			throw new IllegalArgumentException("containerLocator is null.");
		if (fieldFilterList == null)
			throw new IllegalArgumentException("fieldFilterList is null.");
		if (fieldFilterList.isEmpty())
			throw new IllegalArgumentException("fieldFilterList is empty.");
		this.containerLocator = containerLocator;
		this.fieldFilterList = fieldFilterList;
	}
	
	/**
	 * 指定されたフィールドに対応するプロキシオブジェクトを生成して返します。
	 * このメソッドに渡されるフィールドは，{{@link #isSupported(Field)}メソッド呼び出しの結果がtrueのもののみです。
	 * @param field フィールドオブジェクト
	 * @return プロキシオブジェクト
	 */
	Object getValue(Field field) {
		// 引数チェック
		if (field == null)
			throw new IllegalArgumentException("field is null.");
		// コンポーネント名を取得
		String componentName = null;
		for (FieldFilter filter : fieldFilterList) {
			String filterResult = filter.getLookupComponentName(field);
			if (!StringUtils.isEmpty(filterResult)) {
				componentName = filterResult;
				break;
			}
		}
		// Seasarコンポーネントリゾルバを生成
		ComponentResolver resolver = new ComponentResolver(componentName, field.getType(), containerLocator);
		// プロキシを生成
		Object proxy = ProxyFactory.create(field.getType(), resolver);
		// プロキシを返却
		return proxy;
	}
	
	/**
	 * 指定されたフィールドがインジェクションの対象としてサポートされているかどうかを返します。
	 * @param field フィールド
	 * @return サポートされていれば true
	 */
	boolean isSupported(Field field) {
		for (FieldFilter filter : fieldFilterList) {
			if (filter.isSupported(field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 適用されているフィールドフィルタのコレクションを返します。
	 * @return フィールドフィルタのコレクション
	 */
	List<FieldFilter> getFieldFilters() {
		return fieldFilterList;
	}
	
}
