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

package org.seasar.wicket.utils;

import ognl.Ognl;
import ognl.OgnlException;
import wicket.Component;

/**
 * OGNL式に関する汎用的な処理を提供するユーティリティクラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
public class OgnlUtils {

	/**
	 * 指定されたOGNL式を評価し，結果を返します。
	 * @param expression OGNL式
	 * @param target ルートオブジェクトとなるコンポーネントオブジェクト
	 * @return 評価結果
	 * @throws OgnlException 式の評価中に何らかの例外が発生したとき
	 */
	public static Object evaluate(String expression, Component target) throws OgnlException {
		// OGNL式をパース
		Object parsedExp = Ognl.parseExpression(expression);
		// OGNL式を評価
		Object result = Ognl.getValue(parsedExp, target);
		// 結果を返却
		return result;
	}
	
}
