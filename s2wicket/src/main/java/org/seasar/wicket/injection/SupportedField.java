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
 * フィールドフィルタによりインジェクション対象と判断されたフィールドを表すクラスです。
 * このクラスは，サポートするフィールドと，サポートの許可を判断したフィールドフィルタを関連付けるために作成されました。
 * @see FieldFilter
 * @author yoichiro
 * @since 1.1.0
 */
class SupportedField {
	
	/** インジェクション対象としてサポートされるフィールド */
	private Field field;
	
	/** サポートを判断したフィールドフィルタ */
	private FieldFilter fieldFilter;

	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param field インジェクション対象としてサポートされるフィールド
	 * @param fieldFilter サポートを判断したフィールドフィルタ
	 */
	SupportedField(Field field, FieldFilter fieldFilter) {
		super();
		this.field = field;
		this.fieldFilter = fieldFilter;
	}

	/**
	 * インジェクション対象としてサポートされるフィールドを返します。
	 * @return インジェクション対象としてサポートされるフィールド
	 */
	Field getField() {
		return field;
	}

	/**
	 * サポートを判断したフィールドフィルタを返します。
	 * @return サポートを判断したフィールドフィルタ
	 */
	FieldFilter getFieldFilter() {
		return fieldFilter;
	}
	
	/**
	 * 指定されたフィールドのためにSeasarコンテナからコンポーネントオブジェクトルックアップ際に使用するコンポーネント名を返します。
	 * @return コンポーネント名
	 */
	String getLookupComponentName() {
		return fieldFilter.getLookupComponentName(field);
	}

}
