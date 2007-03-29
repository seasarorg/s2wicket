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

import java.lang.reflect.Field;

import wicket.model.BoundCompoundPropertyModel;

/**
 * コンポーネントを{@link BoundCompoundPropertyModel}とバインドする必要があることを表す例外クラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
class NecessaryToBindException extends Exception {
	
	/** バインドするモデルが関連づけられた親コンポーネントのフィールドオブジェクト */
	private Field parentField;

	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param parentField バインドするモデルが関連づけられた親コンポーネントのフィールドオブジェクト
	 */
	NecessaryToBindException(Field parentField) {
		super();
		this.parentField = parentField;
	}
	
	/**
	 * バインドするモデルが関連づけられた親コンポーネントのフィールドオブジェクトを返します。
	 * @return バインドするモデルが関連づけられた親コンポーネントのフィールドオブジェクト
	 */
	Field getParentField() {
		return parentField;
	}

}
