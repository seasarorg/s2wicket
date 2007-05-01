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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ognl.OgnlException;

import org.apache.commons.lang.StringUtils;
import org.seasar.wicket.utils.OgnlUtils;

import wicket.Component;

/**
 * モデルオブジェクトを構築する処理を持つビルダークラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
class ModelBuilder {

	/**
	 * 指定されたフィールドがモデルフィールドかどうかを返します。
	 * @param target チェック対象のフィールドオブジェクト
	 * @return モデルフィールドと判断された場合はtrue，そうでなかった場合はfalse
	 */
	boolean isSupported(Field target) {
		// @WicketModelアノテーションが付与されているかどうかを確認
		return target.isAnnotationPresent(WicketModel.class);
	}

	/**
	 * 指定されたフィールドに対応するモデルオブジェクトを生成し，その結果を返します。
	 * このメソッドに渡されるフィールドは，{@link #isSupported(Field)}メソッドの呼び出し結果がtrueのフィールドのみです。
	 * @param field 処理対象のフィールドオブジェクト
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return 生成されたモデルオブジェクト
	 */
	Object build(Field field, Component target) {
		// 生成結果のモデルを保持する変数
		Object model;
		// create[フィールド名]()メソッドを取得
		Method createMethod = getCreateMethod(field, target);
		// createメソッドが存在したかチェック
		if (createMethod != null) {
			// createメソッドを呼び出してモデルオブジェクトを生成
			model = createModelByCreateMethod(createMethod, target);
		} else {
			// WicketModelアノテーションのexp属性値を取得
			WicketModel annotation = field.getAnnotation(WicketModel.class);
			String exp = annotation.exp();
			// exp属性が指定されたかチェック
			if (StringUtils.isNotEmpty(exp)) {
				// OGNL式を使ってインスタンスを生成
				model = createModelByOgnl(field, target, exp);
			} else {
				// フィールドの型を使ってインスタンスを生成
				model = createModelByFieldType(field, target);
			}
		}
		// 結果を返却
		return model;
	}

	/**
	 * 指定されたモデルフィールドについて，指定されたOGNL式の評価結果をモデルオブジェクトとして取得し，その結果を返します。
	 * @param field 処理対象のモデルフィールド
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param exp モデルを生成するためのOGNL式
	 * @return 生成されたモデルオブジェクト
	 */
	private Object createModelByOgnl(Field field, Component target, String exp) {
		try {
			// 式を評価し，評価結果を取得
			Object result = OgnlUtils.evaluate(exp, target);
			// 結果を返却
			return result;
		} catch (OgnlException e) {
			throw new WicketUIFactoryException(target, "Creating model object by OGNL expression failed. exp=[" + exp + "]", e);
		}
	}

	/**
	 * 指定されたフィールドの型を使用してそのインスタンスを生成し，それをモデルオブジェクトとして返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return 生成されたモデルオブジェクト
	 */
	private Object createModelByFieldType(Field field, Component target) {
		try {
			// フィールドの型を生成
			Class<?> type = field.getType();
			// 型を元にインスタンスを生成
			Object model = type.newInstance();
			// 結果を返却
			return model;
		} catch (InstantiationException e) {
			throw new WicketUIFactoryException(target, "Create model object for " + field.getName() + " failed.");
		} catch (IllegalAccessException e) {
			throw new WicketUIFactoryException(target, "Create model object for " + field.getName() + " failed.");
		}
	}

	/**
	 * 指定されたcreate[フィールド名]()メソッドを呼び出し，その結果のオブジェクトをモデルオブジェクトとして返します。
	 * @param createMethod create[フィールド名]()メソッドオブジェクト
	 * @param target 呼び出し対象のcreateメソッドを持つコンポーネントオブジェクト
	 * @return 生成されたモデルオブジェクト
	 */
	private Object createModelByCreateMethod(Method createMethod, Component target) {
		try {
			// createメソッド呼び出し
			Object model = createMethod.invoke(target, new Object[0]);
			// 結果を返却
			return model;
		} catch (IllegalArgumentException e) {
			throw new WicketUIFactoryException(target, "Invoke " + createMethod.getName() + " failed.");
		} catch (IllegalAccessException e) {
			throw new WicketUIFactoryException(target, "Invoke " + createMethod.getName() + " failed.");
		} catch (InvocationTargetException e) {
			throw new WicketUIFactoryException(target, "Invoke " + createMethod.getName() + " failed.");
		}
	}

	/**
	 * 指定されたフィールドに対応したcreate[フィールド名]()メソッドを返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @param target メソッドを持つかどうかを検査するコンポーネントオブジェクト
	 * @return create[フィールド名]()メソッドオブジェクト もし存在しない場合はnull
	 */
	private Method getCreateMethod(Field field, Component target) {
		// メソッド名を決定
		String methodName = "create" + StringUtils.capitalize(field.getName());
		try {
			// メソッドを取得
			Class<? extends Component> clazz = target.getClass();
			Method method = clazz.getDeclaredMethod(methodName, new Class[0]);
			// 結果を返却
			return method;
		} catch(NoSuchMethodException e) {
			// メソッドが存在しないのでnullを返却
			return null;
		}
	}

}
