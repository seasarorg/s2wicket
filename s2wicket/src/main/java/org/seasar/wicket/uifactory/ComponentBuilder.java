package org.seasar.wicket.uifactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * コンポーネントオブジェクトを構築する処理を持つビルダークラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
class ComponentBuilder {

	/**
	 * 指定されたフィールドがコンポーネントフィールドかどうかを返します。
	 * @param target チェック対象のフィールドオブジェクト
	 * @return コンポーネントフィールドと判断された場合はtrue，そうでなかった場合はfalse
	 */
	boolean isSupported(Field target) {
		// @WicketComponentアノテーションが付与されているかどうかを確認
		return target.isAnnotationPresent(WicketComponent.class);
	}

	/**
	 * 指定されたフィールドに対応するコンポーネントオブジェクトを生成し，その結果を返します。
	 * このメソッドに渡されるフィールドは，{@link #isSupported(Field)}メソッドの呼び出し結果がtrueのフィールドのみです。
	 * @param targetField 処理対象のフィールドオブジェクト
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param modelMap モデルオブジェクトが格納されたコレクション
	 * @return 生成されたコンポーネントオブジェクト
	 */
	Object build(Field field, Component target, Map<String, Object> modelMap) {
		// 生成したコンポーネントの変数
		Component result;
		// 親コンポーネントを取得
		MarkupContainer parent = getParentContainer(field, target);
		// create[フィールド名]Component()メソッドを取得
		Method createMethod = getCreateMethod(field, target);
		// createメソッドが存在したかチェック
		if (createMethod != null) {
			// createメソッドを呼び出してコンポーネントオブジェクトを生成
			result = createComponentByCreateMethod(createMethod, target, field);
		} else {
			// フィールドの型を使ってインスタンスを生成
			result = createComponentByFieldType(field, target, modelMap);
		}
		// 親コンテナに追加
		parent.add(result);
		// 結果を返却
		return result;
	}
	
	/**
	 * 指定されたフィールドの型を使用してそのインスタンスを生成し，それをコンポーネントオブジェクトとして返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @param target 処理対象のフィールドを持つコンポーネントオブジェクト
	 * @param modelMap モデルオブジェクトが格納されたコレクション
	 * @return 生成されたコンポーネントオブジェクト
	 */
	private Component createComponentByFieldType(Field field, Component target, Map<String, Object> modelMap) {
		// 生成したコンポーネントオブジェクトの変数
		Component result;
		// モデルオブジェクトを取得
		Object model = getModel(field, modelMap);
		// モデルのプロパティ名を決定
		String propertyName = getPropertyName(field);
		// プロパティモデルを生成
		PropertyModel propertyModel = new PropertyModel(model, propertyName);
		// wicket:idを決定
		String wicketId = getWicketId(field);
		// フィールドの型を取得
		Class<?> clazz = field.getType();
		// フィールドの型が抽象クラスかチェック
		if (Modifier.isAbstract(clazz.getModifiers())) {
			// 動的プロキシを生成
			result = ComponentProxyFactory.create(field.getName(), clazz, target, wicketId, propertyModel);
		} else {
			// 素直にインスタンス生成
			result = createNewComponentInstance(field, wicketId, propertyModel);
		}
		// 生成したコンポーネントオブジェクトを返却
		return result;
	}
	
	/**
	 * 指定されたフィールドに対応するコンポーネントオブジェクトを生成して返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @param wicketId wicket:id
	 * @param model モデルオブジェクト
	 * @return 生成されたコンポーネントオブジェクト
	 */
	private Component createNewComponentInstance(Field field, String wicketId, IModel model) {
		try {
			// コンストラクタを取得
			Class<?> clazz = field.getType();
			Constructor<?> constructor = clazz.getConstructor(String.class, IModel.class);
			// インスタンスを生成
			Component component = (Component)constructor.newInstance(wicketId, model);
			// 結果を返却
			return component;
		} catch(NoSuchMethodException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create new component instance for " + field.getName() + " failed.", e);
		} catch (IllegalArgumentException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create new component instance for " + field.getName() + " failed.", e);
		} catch (InstantiationException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create new component instance for " + field.getName() + " failed.", e);
		} catch (IllegalAccessException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create new component instance for " + field.getName() + " failed.", e);
		} catch (InvocationTargetException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create new component instance for " + field.getName() + " failed.", e);
		}
	}
	
	/**
	 * 指定されたフィールドのwicket:idを返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @return wicket:id
	 */
	private String getWicketId(Field field) {
		// アノテーションを取得
		WicketComponent annotation = field.getAnnotation(WicketComponent.class);
		// wicketId属性値を取得
		String wicketId = annotation.wicketId();
		// wicketId属性が指定されたかチェック
		if (StringUtils.isNotEmpty(wicketId)) {
			// wicketId属性値をそのまま返却
			return wicketId;
		} else {
			// フィールド名をwicket:idとして返却
			return field.getName();
		}
	}
	
	/**
	 * 指定されたフィールドとモデルを関連付ける際のプロパティ名を返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @return モデルを関連付ける際のプロパティ名
	 */
	private String getPropertyName(Field field) {
		// アノテーションを取得
		WicketComponent annotation = field.getAnnotation(WicketComponent.class);
		// property属性値を取得
		String propertyName = annotation.property();
		// property属性値が指定されたかチェック
		if (StringUtils.isNotEmpty(propertyName)) {
			// property属性値をそのまま返却
			return propertyName;
		} else {
			// フィールド名をプロパティ名として返却
			return field.getName();
		}
	}
	
	/**
	 * 指定されたフィールドに適用するモデルオブジェクトを返します。
	 * @param field 処理対象のフィールド
	 * @param modelMap モデルオブジェクトが格納されたコレクション
	 * @return モデルオブジェクト
	 */
	private Object getModel(Field field, Map<String, Object> modelMap) {
		// アノテーションを取得
		WicketComponent annotation = field.getAnnotation(WicketComponent.class);
		// model属性値を取得
		String modelName = annotation.model();
		// model属性値が指定されたかチェック
		if (StringUtils.isNotEmpty(modelName)) {
			// モデルオブジェクトのコレクションから取得
			Object result = modelMap.get(modelName);
			// 結果を返却
			return result;
		} else {
			// モデルの個数が1つかどうかチェック
			if (modelMap.size() == 1) {
				// コレクションから取得して返却
				return modelMap.values().iterator().next();
			} else {
				// モデルを特定できない
				throw new IllegalStateException("Attribute[model] not found. Field name is " + field.getName());
			}
		}
	}

	/**
	 * 指定されたcreate[フィールド名]Component()メソッドを呼び出し，その結果のオブジェクトをコンポーネントオブジェクトとして返します。
	 * @param createMethod create[フィールド名]Component()メソッドオブジェクト
	 * @param target 呼び出し対象のcreateメソッドを持つコンポーネントオブジェクト
	 * @param field 処理対象のフィールドオブジェクト
	 * @return 生成されたコンポーネントオブジェクト
	 */
	private Component createComponentByCreateMethod(Method createMethod, Component target, Field field) {
		try {
			// createメソッドを呼び出してコンポーネントを生成
			Component result = (Component)createMethod.invoke(target, new Object[0]);
			// 結果を返却
			return result;
		} catch (IllegalArgumentException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create component object for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
		} catch (IllegalAccessException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create component object for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
		} catch (InvocationTargetException e) {
			// TODO 例外処理
			throw new IllegalStateException("Create component object for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
		}
	}
	
	/**
	 * 指定されたフィールドに対応したcreate[フィールド名]Component()メソッドを返します。
	 * @param field 処理対象のフィールドオブジェクト
	 * @param target メソッドを持つかどうかを検査するコンポーネントオブジェクト
	 * @return create[フィールド名]Component()メソッドオブジェクト もし存在しない場合はnull
	 */
	private Method getCreateMethod(Field field, Component target) {
		// メソッド名を決定
		String methodName = "create" + StringUtils.capitalize(field.getName()) + "Component";
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

	/**
	 * 指定されたフィールドに付与されたアノテーションに従って，親コンテナコンポーネントを決定し，その結果を返します。
	 * @param field 処理対象のフィールド
	 * @param target 親コンテナの取得対象となるコンポーネントオブジェクト
	 * @return 親コンテナコンポーネントオブジェクト
	 */
	private MarkupContainer getParentContainer(Field field, Component target) {
		// 結果の親コンテナの変数
		MarkupContainer result;
		// アノテーションを取得
		WicketComponent annotation = field.getAnnotation(WicketComponent.class);
		// parent属性値を取得
		String parentAttr = annotation.parent();
		// parent属性値をチェック
		if ((StringUtils.isEmpty(parentAttr)) || (parentAttr.equals("this"))) {
			// 処理対象のコンポーネント自身を親コンテナとする
			result = (MarkupContainer)target;
		} else {
			try {
				// parent属性値で示されたフィールドを取得
				Class<? extends Component> clazz = target.getClass();
				Field parentField = clazz.getDeclaredField(parentAttr);
				// フィールドのアクセス権を変更
				if (!(parentField.isAccessible())) {
					parentField.setAccessible(true);
				}
				// フィールドの値を親コンテナコンポーネントとする
				result = (MarkupContainer)(parentField.get(target));
			} catch (SecurityException e) {
				// TODO 例外処理
				throw new IllegalStateException("Get parent container for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
			} catch (NoSuchFieldException e) {
				// TODO 例外処理
				throw new IllegalStateException("Get parent container for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
			} catch (IllegalArgumentException e) {
				// TODO 例外処理
				throw new IllegalStateException("Get parent container for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
			} catch (IllegalAccessException e) {
				// TODO 例外処理
				throw new IllegalStateException("Get parent container for " + target.getClass().getName() + "#" + field.getName() + " failed.", e);
			}
		}
		// 結果を返却
		return result;
	}

}
