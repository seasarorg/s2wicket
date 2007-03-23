package org.seasar.wicket.uifactory;

import static org.seasar.wicket.utils.Gadget.isWicketClass;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.lang.StringUtils;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * 
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
class BuildingProcessor {
	
//	--- ビルダーフィールド
	
	/** モデルビルダー */
	private ModelBuilder modelBuilder;
	
	/** コンポーネントビルダー */
	private ComponentBuilder componentBuilder;
	
//	--- コンストラクタ
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 */
	BuildingProcessor() {
		super();
		// ビルダーを生成
		modelBuilder = new ModelBuilder();
		componentBuilder = new ComponentBuilder();
	}
	
//	--- ビルド関連メソッド
	
	/**
	 * 指定されたコンポーネントが持つモデルフィールドおよびコンポーネントフィールドについて，構築を行います。
	 * @param target 処理対象のコンポーネントオブジェクト
	 */
	void build(Component target) {
		// モデルの構築
		Map<String, Object> modelMap = buildModel(target);
		// コンポーネントの構築
		buildComponent(target, modelMap);
	}
	
//	--- モデルビルド関連メソッド
	
	/**
	 * 指定されたコンポーネントが持つ各モデルフィールドに対して，モデルオブジェクトを構築してセットします。
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return モデル名とモデルオブジェクトが対で格納されたコレクション
	 */
	private Map<String, Object> buildModel(Component target) {
		// 生成したモデルオブジェクトを格納するコレクションを生成
		Map<String, Object> result = new HashMap<String, Object>();
		// モデルフィールドを抽出
		Field[] targetFields = getTargetModelFields(target);
		// フィールド毎に処理
		for (int i = 0; i < targetFields.length; i++) {
			// フィールドの値がnullかチェック
			try {
				if (targetFields[i].get(target) == null) {
					// フィールド値とするモデルオブジェクトの生成をモデルビルダーに依頼
					Object model = modelBuilder.build(targetFields[i], target);
					// モデルフィールドにセット
					targetFields[i].set(target, model);
					// コレクションに追加
					result.put(targetFields[i].getName(), model);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Building model failed.", e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Building model failed.", e);
			}
		}
		// 結果のコレクションを返却
		return result;
	}
	
	/**
	 * 指定されたコンポーネントが持つフィールドの中で，モデルオブジェクトとなるフィールドの一覧を返します。
	 * @param target 処理対象となるコンポーネントオブジェクト
	 * @return モデルオブジェクトとなるフィールドと判断されたフィールドの配列
	 */
	private Field[] getTargetModelFields(Component target) {
		// コンポーネントのクラスオブジェクトを取得
		Class<? extends Object> clazz = target.getClass();
		// 結果を格納するコレクションを生成
		List<Field> resultList = new ArrayList<Field>();
		// モデルフィールド名の重複を避けるためのコレクションを生成
		Set<String> modelFieldNameSet = new HashSet<String>();
		// 処理対象がなくなるか，Wicket提供クラスになるまで繰り返す
		while((clazz != null) && (!(isWicketClass(clazz)))) {
			// 定義されているフィールドを取得
			Field[] fields = clazz.getDeclaredFields();
			// フィールド毎に処理
			for (int i = 0; i < fields.length; i++) {
				// アクセス可能かチェック
				if (!fields[i].isAccessible()) {
					// アクセス可能にする
					fields[i].setAccessible(true);
				}
				// サポートされているフィールドかチェック
				if (modelBuilder.isSupported(fields[i])) {
					// フィールド名を取得
					String fieldName = fields[i].getName();
					// すでに同名のモデルフィールドが存在するかチェック
					// （具象クラスのモデルフィールドを優先し，同名の親クラスにあるフィールドはモデルフィールドとしない）
					if (!modelFieldNameSet.contains(fieldName)) {
						// 結果のコレクションに追加
						resultList.add(fields[i]);
						// 重複チェックのためにコレクションにフィールド名を追加
						modelFieldNameSet.add(fields[i].getName());
					}
				}
			}
			// スーパークラスを取得し同様の検査を行う
			clazz = clazz.getSuperclass();
		}
		// 結果を返却
		return resultList.toArray(new Field[0]);
	}
	
//	--- コンポーネントビルド関連メソッド
	
	/**
	 * 指定されたコンポーネントが持つ各コンポーネントフィールドに対して，コンポーネントオブジェクトを構築してセットします。
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param modelMap モデルオブジェクトのコレクション
	 */
	private void buildComponent(Component target, Map<String, Object> modelMap) {
		// コンポーネントフィールドを抽出
		Field[] targetFields = getTargetComponentFields(target);
		// フィールド毎に処理
		for (int i = 0; i < targetFields.length; i++) {
			// フィールドの値がnullかチェック
			try {
				if (targetFields[i].get(target) == null) {
					// フィールド値とするコンポーネントオブジェクトの生成をモデルビルダーに依頼
					Object model = componentBuilder.build(targetFields[i], target, modelMap);
					// コンポーネントフィールドにセット
					targetFields[i].set(target, model);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Building component failed.", e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Building component failed.", e);
			}
		}
	}
	
	/**
	 * 指定されたコンポーネントが持つフィールドの中で，コンポーネントオブジェクトとなるフィールドの一覧を返します。
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return コンポーネントオブジェクトとなるフィールドと判断されたフィールドの配列
	 */
	private Field[] getTargetComponentFields(Component target) {
		// コンポーネントのクラスオブジェクトを取得
		Class<? extends Object> clazz = target.getClass();
		// 結果を格納するコレクションを生成
		List<Field> resultList = new ArrayList<Field>();
		// 処理対象がなくなるか，Wicket提供クラスになるまで繰り返す
		while((clazz != null) && (!(isWicketClass(clazz)))) {
			// 定義されているフィールドを取得
			Field[] fields = clazz.getDeclaredFields();
			// フィールド毎に処理
			for (int i = 0; i < fields.length; i++) {
				// アクセス可能かチェック
				if (!fields[i].isAccessible()) {
					// アクセス可能にする
					fields[i].setAccessible(true);
				}
				// サポートされているフィールドかチェック
				if (componentBuilder.isSupported(fields[i])) {
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
	
	
//	--- 検証用メソッド
	
	/**
	 * 指定されたコンポーネントが持つフィールドに対して，インジェクションを行います。
	 * @param target 処理対象のコンポーネントオブジェクト
	 */
	void inject(Component target) {
		// 対象オブジェクトのクラスオブジェクトを取得
		Class<? extends Object> clazz = target.getClass();
		// インジェクション処理対象のフィールドを取得
		Field[] targetFields = getTargetFields(clazz);
		try {
			// モデルのコレクション
			Map<String, Object> modelMap = new HashMap<String, Object>();
			// フィールド毎に処理
			for (int i = 0; i < targetFields.length; i++) {
				Field targetField = targetFields[i];
				// フィールドにアクセスできるようにする
				if (!targetField.isAccessible()) {
					targetField.setAccessible(true);
				}
				// WicketModelアノテーションのフィールドかチェック
				if (targetField.isAnnotationPresent(WicketModel.class)) {
					// 対象オブジェクトのフィールド値がnullかチェック
					if (targetField.get(target) == null) {
						// FIXME ここは試験実装
						String methodName = "create" + StringUtils.capitalize(targetField.getName());
						try {
							// create[ModelName]メソッドが存在した場合
							// createメソッドにコンポーネント生成をお願いする
							Method method = clazz.getMethod(methodName, new Class[0]);
							Object model = method.invoke(target, new Object[0]);
							targetField.set(target, model);
							// モデルのコレクションに追加
							modelMap.put(targetField.getName(), model);
						} catch(NoSuchMethodException e) {
							// モデルオブジェクトを生成
							// フィールドの型を取得
							Class<?> targetFieldClazz = targetField.getType();
							// インスタンスを生成
							Object model = targetFieldClazz.newInstance();
							// フィールドにセット
							targetField.set(target, model);
							// モデルのコレクションに追加
							modelMap.put(targetField.getName(), model);
						}
					}
				}
			}
			// フィールド毎に処理
			for (int i = 0; i < targetFields.length; i++) {
				Field targetField = targetFields[i];
				// フィールドにアクセスできるようにする
				if (!targetField.isAccessible()) {
					targetField.setAccessible(true);
				}
				// WicketComponentアノテーションのフィールドかチェック
				if (targetField.isAnnotationPresent(WicketComponent.class)) {
					// 対象オブジェクトのフィールド値がnullかチェック
					if (targetField.get(target) == null) {
						// FIXME ここは試験実装
						// アノテーションを取得
						WicketComponent annotation = targetField.getAnnotation(WicketComponent.class);
						// 親コンポーネントを決定
						String parentAttr = annotation.parent();
						MarkupContainer parent;
						if (StringUtils.isEmpty(parentAttr) || parentAttr.equals("this")) {
							parent = (MarkupContainer)target;
						} else {
							Field parentField = clazz.getDeclaredField(parentAttr);
							if (!parentField.isAccessible()) {
								parentField.setAccessible(true);
							}
							parent = (MarkupContainer)(parentField.get(target));
						}
						// メソッド名を決定
						String methodName = "create" + StringUtils.capitalize(targetField.getName()) + "Component";
						try {
							// create[FieldName]Component()メソッドが存在した場合
							// createメソッドにコンポーネント生成をお願いする
							Method method = clazz.getMethod(methodName, MarkupContainer.class);
							Object component = method.invoke(target, parent);
							targetField.set(target, component);
						} catch(NoSuchMethodException e) {
							// アノテーションの属性を取得
							String modelAttr = annotation.model();
							String propertyAttr = annotation.property();
							String wicketIdAttr = annotation.wicketId();
							String fieldName = targetField.getName();
							if (StringUtils.isEmpty(propertyAttr)) {
								propertyAttr = fieldName;
							}
							if (StringUtils.isEmpty(wicketIdAttr)) {
								wicketIdAttr = fieldName;
							}
							// モデルオブジェクトを取得
							Object model;
							if (StringUtils.isNotEmpty(modelAttr)) {
								Field modelField = clazz.getDeclaredField(modelAttr);
								if (!modelField.isAccessible()) {
									modelField.setAccessible(true);
								}
								model = modelField.get(target);
							} else {
								if (modelMap.size() == 1) {
									model = modelMap.values().iterator().next();
								} else {
									throw new IllegalStateException("Attribute[model] not found. Field name is " + targetField.getName() + ".");
								}
							}
							// プロパティモデルを生成
							PropertyModel propertyModel = new PropertyModel(model, propertyAttr);
							// フィールドの型を取得
							Class<?> targetFieldClazz = targetField.getType();
							// 生成したコンポーネントの変数
							Component component;
							// 抽象クラスかチェック
							if (Modifier.isAbstract(targetFieldClazz.getModifiers())) {
								// 抽象クラスなので，動的プロキシ生成
								Enhancer enhancer = new Enhancer();
								enhancer.setInterfaces(new Class[] {Serializable.class});
								enhancer.setSuperclass(targetFieldClazz);
								enhancer.setCallback(new WicketComponentMethodInterceptor(target, fieldName));
								component = (Component)enhancer.create(new Class[] {String.class, IModel.class}, new Object[] {wicketIdAttr, propertyModel});
							} else {
								// 具象クラスなので，素直にインスタンス生成
								Constructor<?> constructor = targetFieldClazz.getConstructor(String.class, IModel.class);
								component = (Component)constructor.newInstance(wicketIdAttr, propertyModel);
							}
							// 親コンテナに登録
							parent.add(component);
							// フィールドにインスタンスをセット
							targetField.set(target, component);
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Field injection failed.", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Field injection failed.", e);
		} catch (SecurityException e) {
			throw new IllegalStateException("Field injection failed.", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Field injection failed.", e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Field injection failed.", e);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Field injection failed.", e);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Field injection failed.", e);
		}
	}
	
	private static class WicketComponentMethodInterceptor implements MethodInterceptor, Serializable {
		private Object target;
		private String fieldName;
		public WicketComponentMethodInterceptor(Object target, String fieldName) {
			super();
			this.target = target;
			this.fieldName = fieldName;
		}
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if (Modifier.isAbstract(method.getModifiers())) {
				String methodName = method.getName();
				if (methodName.startsWith("on")) {
					Class<? extends Object> clazz = target.getClass();
					Method targetMethod = clazz.getDeclaredMethod(methodName + StringUtils.capitalize(fieldName));
					targetMethod.invoke(target);
					return null;
				} else {
					throw new UnsupportedOperationException("Method[" + methodName + "()] not supported.");
				}
			} else {
				return proxy.invokeSuper(obj, args);
			}
		}
	}

	/**
	 * 処理対象のフィールドを返します。
	 * @param clazz 対象クラスオブジェクト
	 * @param fieldValueProducer フィールド値供給オブジェクト
	 * @return 処理対象のフィールドの配列
	 */
	private Field[] getTargetFields(Class<? extends Object> clazz) {
		// 結果を格納するコレクションを生成
		List<Field> resultList = new ArrayList<Field>();
		// 処理対象がなくなるか，Wicket提供クラスになるまで繰り返す
		while((clazz != null) && (!(isWicketClass(clazz)))) {
			// 定義されているフィールドを取得
			Field[] fields = clazz.getDeclaredFields();
			// フィールド毎に処理
			for (int i = 0; i < fields.length; i++) {
				// サポートされているフィールドかチェック
				if (fields[i].isAnnotationPresent(WicketComponent.class)
						|| fields[i].isAnnotationPresent(WicketModel.class)) {
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

}
