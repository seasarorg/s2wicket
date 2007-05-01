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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.LoadableDetachableModel;
import wicket.model.Model;
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
	 * @param field 処理対象のフィールドオブジェクト
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param modelMap モデルオブジェクトが格納されたコレクション
	 * @return 生成されたコンポーネントオブジェクト
	 */
	Object build(Field field, Component target, Map<Field, Object> modelMap) {
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
			result = createComponentByFieldType(field, target, modelMap, parent.getId());
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
	 * @param parent 親のコンテナコンポーネント
	 * @return 生成されたコンポーネントオブジェクト
	 */
	private Component createComponentByFieldType(Field field, Component target, Map<Field, Object> modelMap, String parentId) {
		// 生成したコンポーネントオブジェクトの変数
		Component result;
		// モデルを取得
		Object model;
		IModel parentModel = null;
		try {
			model = createModel(field, target, modelMap);
		} catch(NecessaryToBindException e) {
			// 親のコンポーネントのモデルとバインドする必要あり
			model = null;
			try {
				Component parentComponent = (Component)e.getParentField().get(target);
				parentModel = parentComponent.getModel();
			} catch (IllegalArgumentException e1) {
				throw new WicketUIFactoryException(target, "Get value of parent field failed.", e1);
			} catch (IllegalAccessException e1) {
				throw new WicketUIFactoryException(target, "Get value of parent field failed.", e1);
			}
		}
		// wicket:idを決定
		String wicketId = getWicketId(field);
		// フィールドの型を取得
		Class<?> clazz = field.getType();
		// 動的プロキシを生成
		result = ComponentProxyFactory.create(field.getName(), clazz, target, wicketId, model, parentId);
		// 親のコンポーネントのモデルとバインドする必要性をチェック
		if (parentModel != null) {
			// プロパティ名を取得
			String propertyName = getPropertyName(field);
			// モデルとバインド
			((BoundCompoundPropertyModel)parentModel).bind(result, propertyName);
		}
		// 生成したコンポーネントオブジェクトを返却
		return result;
	}
	
	/**
	 * 指定されたコンポーネントに関連付けるモデルを生成します。
	 * @param field 処理対象のコンポーネントのフィールド
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param modelMap 生成されたモデルオブジェクトが格納されたコレクション
	 * @return 生成されたモデル
	 * @throws NecessaryToBindException 関連付けがBoundCompoundPropertyModelによるプロパティの明示的な指定を必要とするとき
	 */
	private Object createModel(Field field, Component target, Map<Field, Object> modelMap) throws NecessaryToBindException {
		// WicketComponentアノテーションを取得
		WicketComponent targetAnnotation = field.getAnnotation(WicketComponent.class);
		// どのモデルオブジェクトを使用するかの属性を取得
		UseModel useModel = targetAnnotation.useModel();
		// どのモデルオブジェクトを使用するかにより処理分岐
		if (useModel.equals(UseModel.FIELD)) { // フィールドに定義されたモデルを使用
			// モデル名属性を取得
			String modelName = targetAnnotation.modelName();
			// モデル名が指定されていたかチェック
			if (StringUtils.isNotEmpty(modelName)) {
				// モデル名が指定された場合の処理をコール
				return createModelForSpecifiedModelName(field, modelMap, modelName, target);
			} else {
				// モデル名が指定されなかった場合の処理をコール
				return createModelForNotSpecifiedModelName(field, target, modelMap);
			}
		} else if (useModel.equals(UseModel.LOADABLE_DETACHABLE)) { // 必要なときに読み込み，不必要になったら取り除くモデルを使用
			// LoadableDetachableModelの動的プロキシを生成
			LoadableDetachableModel loadableDetachableModel = LoadableDetachableModelProxyFactory.create(field.getName(), target);
			// 結果を返却
			return loadableDetachableModel;
		} else {
			throw new IllegalStateException("Unknown UseModel value.");
		}
	}
	
	/**
	 * 指定されたコンポーネントフィールドのWicketComponentアノテーションにおいて，
	 * モデル名が指定されなかったときのモデル生成処理を行います。
	 * @param field 処理対象のコンポーネントのフィールド
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param modelMap 生成されたモデルオブジェクトが格納されたコレクション
	 * @return 生成されたモデル
	 * @throws NecessaryToBindException 関連付けがBoundCompoundPropertyModelによるプロパティの明示的な指定を必要とするとき
	 */
	private Object createModelForNotSpecifiedModelName(Field field, Component target, Map<Field, Object> modelMap) throws NecessaryToBindException {
		// WicketComponentアノテーションを取得
		WicketComponent targetAnnotation = field.getAnnotation(WicketComponent.class);
		// 親のコンテナのフィールド名を取得
		String parentName = targetAnnotation.parent();
		// 親のコンテナが指定されていたかチェック
		if (StringUtils.isNotEmpty(parentName)) {
			try {
				// 親のコンテナのフィールドを取得
				Class<? extends Component> clazz = target.getClass();
				Field parentField = clazz.getDeclaredField(parentName);
				// 親のコンテナのWicketComponentアノテーションを取得
				WicketComponent parentAnnotation = parentField.getAnnotation(WicketComponent.class);
				// 親のコンテナのWicketComponentアノテーションのモデル名属性を取得
				String parentModelName = parentAnnotation.modelName();
				// モデル名属性が指定されていたかチェック
				if (StringUtils.isNotEmpty(parentModelName)) {
					// モデルオブジェクトのフィールドを取得
					Entry<Field, Object> modelMapEntry = getModelMapEntry(parentModelName, modelMap, target);
					Field modelField = modelMapEntry.getKey();
					// モデルフィールドのWicketModelアノテーションを取得
					WicketModel modelAnnotation = modelField.getAnnotation(WicketModel.class);
					// モデルフィールドのモデル種別属性を取得
					ModelType modelType = modelAnnotation.type();
					// モデル種別毎に処理
					if (modelType.equals(ModelType.RAW)) { // Raw
						// 未対応
						throw new UnsupportedOperationException("ModelType.RAW for parent container not supported.");
					} else if (modelType.equals(ModelType.BASIC)) { // Model
						// 未対応
						throw new UnsupportedOperationException("ModelType.MODEL for parent container not supported.");
					} else if (modelType.equals(ModelType.PROPERTY)) { // PropertyModel
						// 未対応
						throw new UnsupportedOperationException("ModelType.PROPERTY for parent container not supported.");
					} else if (modelType.equals(ModelType.COMPOUND_PROPERTY)) { // CompoundPropertyModel
						// コンポーネントにモデルは必要ない
						return null;
					} else if (modelType.equals(ModelType.BOUND_COMPOUND_PROPERTY)) { // BoundCompoundPropertyModel
						// バインドする必要があることを例外をスローすることで返却
						throw new NecessaryToBindException(parentField);
					} else {
						// モデル種別未指定はあり得ない
						throw new IllegalStateException("Unknown ModelType.");
					}
				} else {
					// デフォルトのモデルを生成し返却
					return createDefaultModel(field, modelMap);
				}
			} catch(NoSuchFieldException e) {
				throw new WicketUIFactoryException(target, "Parent component field[" + parentName + "] not found.", e);
			}
		} else {
			// デフォルトのモデルを生成し返却
			return createDefaultModel(field, modelMap);				
		}
	}
	
	/**
	 * 指定されたコンポーネントフィールドのWicketComponentアノテーションにおいて，
	 * モデル名が指定されたときのモデル生成処理を行います。
	 * @param field 処理対象のコンポーネントのフィールド
	 * @param modelMap 生成されたモデルオブジェクトが格納されたコレクション
	 * @param modelName 指定されたモデルの名前
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return 生成されたモデル
	 */
	private Object createModelForSpecifiedModelName(Field field, Map<Field, Object> modelMap, String modelName, Component target) {
		// モデルオブジェクトのフィールドとオブジェクトを取得
		Entry<Field, Object> modelMapEntry = getModelMapEntry(modelName, modelMap, target);
		Field modelField = modelMapEntry.getKey();
		Object modelObj = modelMapEntry.getValue();
		// モデルフィールドのWicketModelアノテーションを取得
		WicketModel modelAnnotation = modelField.getAnnotation(WicketModel.class);
		// モデルフィールドのモデル種別属性を取得
		ModelType modelType = modelAnnotation.type();
		// モデル種別毎に処理
		if (modelType.equals(ModelType.RAW)) { // モデルオブジェクトそのまま
			// モデルオブジェクトをそのまま返却
			return modelObj;
		} else if (modelType.equals(ModelType.BASIC)) { // Model
			// 基本モデルを生成
			Model model = new Model((Serializable)modelObj);
			// 基本モデルを返却
			return model;
		} else if (modelType.equals(ModelType.PROPERTY)) { // PropertyModel
			// 関連付けるモデルのプロパティ名を取得
			String propertyName = getPropertyName(field);
			// プロパティモデルを生成
			PropertyModel propertyModel = new PropertyModel(modelObj, propertyName);
			// プロパティモデルを返却
			return propertyModel;
		} else if (modelType.equals(ModelType.COMPOUND_PROPERTY)) { // CompoundPropertyModel
			// 複合プロパティモデルを生成
			CompoundPropertyModel compoundPropertyModel = new CompoundPropertyModel(modelObj);
			// 複合プロパティモデルを返却
			return compoundPropertyModel;
		} else if (modelType.equals(ModelType.BOUND_COMPOUND_PROPERTY)) { // BoundCompoundPropertyModel
			// 複合バインドプロパティモデルを生成
			BoundCompoundPropertyModel boundCompoundPropertyModel = new BoundCompoundPropertyModel(modelObj);
			// 複合バインドプロパティモデルを返却
			return boundCompoundPropertyModel;
		} else {
			// モデル種別未指定はあり得ない
			throw new IllegalStateException("Unknown ModelType.");
		}
	}
	
	/**
	 * 明示的に使用するモデルが指定されなかったときに関連付けるモデルを生成します。
	 * @param field 処理対象のコンポーネントフィールド
	 * @param modelMap 生成されたモデルオブジェクトが格納されたコレクション
	 * @return 生成されたモデル
	 */
	private Object createDefaultModel(Field field, Map<Field, Object> modelMap) {
		// モデルの個数をチェック
		if (modelMap.size() == 1) {
			// モデルオブジェクトのフィールドとオブジェクトを取得
			Entry<Field, Object> modelMapEntry = modelMap.entrySet().iterator().next();
			Field modelField = modelMapEntry.getKey();
			Object modelObj = modelMapEntry.getValue();
			// モデルフィールドのWicketModelアノテーションを取得
			WicketModel modelAnnotation = modelField.getAnnotation(WicketModel.class);
			// モデルフィールドのモデル種別属性を取得
			ModelType modelType = modelAnnotation.type();
			// モデル種別毎に処理
			if (modelType.equals(ModelType.RAW)) { // Raw
				// モデルと関連付けを行わない
				return null;
			} else if (modelType.equals(ModelType.BASIC)) { // Model
				// モデルと関連付けを行わない
				return null;
			} else if (modelType.equals(ModelType.PROPERTY)) { // PropertyModel
				// 関連付けるモデルのプロパティ名を取得
				String propertyName = getPropertyName(field);
				// プロパティモデルを生成
				PropertyModel propertyModel = new PropertyModel(modelObj, propertyName);
				// プロパティモデルを返却
				return propertyModel;
			} else if (modelType.equals(ModelType.COMPOUND_PROPERTY)) { // CompoundPropertyModel
				// 未対応
				throw new UnsupportedOperationException("ModelType.COMPOUND_PROPERTY for parent container not supported.");
			} else if (modelType.equals(ModelType.BOUND_COMPOUND_PROPERTY)) { // BoundCompoundPropertyModel
				// 未対応
				throw new UnsupportedOperationException("ModelType.BOUND_COMPOUND_PROPERTY for parent container not supported.");
			} else {
				// モデル種別未指定はあり得ない
				throw new IllegalStateException("Unknown ModelType.");
			} 
		} else {
			// 関連付けるモデルを特定できない
			return null;
		}
	}
	
	/**
	 * 指定されたフィールド名に一致するモデルオブジェクトのマップエントリを返します。
	 * @param fieldName フィールド名
	 * @param modelMap モデルフィールドとモデルオブジェクトが格納されたコレクション
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return モデルオブジェクトのマップエントリ
	 */
	private Map.Entry<Field, Object> getModelMapEntry(String fieldName, Map<Field, Object> modelMap, Component target) {
		for(Map.Entry<Field, Object> entry : modelMap.entrySet()) {
			if (entry.getKey().getName().equals(fieldName)) {
				return entry;
			}
		}
		throw new WicketUIFactoryException(target, "Model object entry not found. fieldName = " + fieldName);
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
		String propertyName = annotation.modelProperty();
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
			throw new WicketUIFactoryException(target, "Create component object for " + field.getName() + " failed.", e);
		} catch (IllegalAccessException e) {
			throw new WicketUIFactoryException(target, "Create component object for " + field.getName() + " failed.", e);
		} catch (InvocationTargetException e) {
			throw new WicketUIFactoryException(target, "Create component object for " + field.getName() + " failed.", e);
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
				throw new WicketUIFactoryException(target, "Get parent container for " + field.getName() + " failed.", e);
			} catch (NoSuchFieldException e) {
				throw new WicketUIFactoryException(target, "Get parent container for " + field.getName() + " failed.", e);
			} catch (IllegalArgumentException e) {
				throw new WicketUIFactoryException(target, "Get parent container for " + field.getName() + " failed.", e);
			} catch (IllegalAccessException e) {
				throw new WicketUIFactoryException(target, "Get parent container for " + field.getName() + " failed.", e);
			}
		}
		// 結果を返却
		return result;
	}

}
