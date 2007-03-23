package org.seasar.wicket.uifactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

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
	 * @param targetField 処理対象のフィールドオブジェクト
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
			// フィールドの型を使ってインスタンスを生成
			model = createModelByFieldType(field);
		}
		// 結果を返却
		return model;
	}

	/**
	 * 指定されたフィールドの型を使用してそのインスタンスを生成し，それをモデルオブジェクトとして返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @return 生成されたモデルオブジェクト
	 */
	private Object createModelByFieldType(Field field) {
		try {
			// フィールドの型を生成
			Class<?> type = field.getType();
			// 型を元にインスタンスを生成
			Object model = type.newInstance();
			// 結果を返却
			return model;
		} catch (InstantiationException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create model object for " + field.getName() + " failed.");
		} catch (IllegalAccessException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create model object for " + field.getName() + " failed.");
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
			// TODO 例外処理
			throw new IllegalStateException("Invoke " + target.getClass().getName() + "#" + createMethod.getName() + " failed.");
		} catch (IllegalAccessException e) {
			// TODO 例外処理
			throw new IllegalStateException("Invoke " + target.getClass().getName() + "#" + createMethod.getName() + " failed.");
		} catch (InvocationTargetException e) {
			// TODO 例外処理
			throw new IllegalStateException("Invoke " + target.getClass().getName() + "#" + createMethod.getName() + " failed.");
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
