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

/**
 * 指定されたフィールドについて，それがインジェクション対象かどうかを判断し，
 * インジェクション対象だった場合は，ルックアップするためのSeasarコンポーネント名を提供する
 * 処理を規定したインタフェースです。
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
	 * このメソッドに渡されるfieldオブジェクトは，{{@link #isSupported(Field)}メソッドの呼び出し結果が
	 * true のオブジェクトのみとなります。
	 * もしSeasarコンポーネント名ではなく，フィールドの型でルックアップを行う場合は，nullを返却してください。
	 * @param field フィールド
	 * @return ルックアップするSeasarコンポーネント名。もしフィールドの型でルックアップする場合は null
	 */
	public String getLookupComponentName(Field field);
	
}
