package org.seasar.wicket.uifactory;

import static org.seasar.wicket.utils.Gadget.isWicketClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import wicket.Component;

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
		Map<Field, Object> modelMap = buildModel(target);
		// コンポーネントの構築
		buildComponent(target, modelMap);
	}
	
//	--- モデルビルド関連メソッド
	
	/**
	 * 指定されたコンポーネントが持つ各モデルフィールドに対して，モデルオブジェクトを構築してセットします。
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return モデル名とモデルオブジェクトが対で格納されたコレクション
	 */
	private Map<Field, Object> buildModel(Component target) {
		// 生成したモデルオブジェクトを格納するコレクションを生成
		Map<Field, Object> result = new HashMap<Field, Object>();
		// モデルフィールドを抽出
		Field[] targetFields = getTargetModelFields(target);
		// フィールド毎に処理
		for (int i = 0; i < targetFields.length; i++) {
			// フィールドの値がnullかチェック
			try {
				// モデルオブジェクトを取得
				Object modelObj = targetFields[i].get(target);
				if (modelObj == null) {
					// フィールド値とするモデルオブジェクトの生成をモデルビルダーに依頼
					modelObj = modelBuilder.build(targetFields[i], target);
					// モデルフィールドにセット
					targetFields[i].set(target, modelObj);
				}
				// コレクションに追加
				result.put(targetFields[i], modelObj);
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
	private void buildComponent(Component target, Map<Field, Object> modelMap) {
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
		Map<String, SortableField> fieldMap = new HashMap<String, SortableField>();
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
					// 既に同名のフィールドが追加されたかチェック
					//（具象クラスのコンポーネントフィールドを優先し，同名の親クラスにあるフィールドはコンポーネントフィールドとしない）
					if (!fieldMap.containsKey(fields[i].getName())) {
						// 結果のコレクションに追加
						fieldMap.put(fields[i].getName(), new SortableField(fields[i]));
					}
				}
			}
			// スーパークラスを取得し同様の検査を行う
			clazz = clazz.getSuperclass();
		}
		// ソートを行う
		Field[] fields = sortFields(fieldMap);
		// 結果を返却
		return fields;
	}
	
	/**
	 * 指定されたフィールドの一覧について，親子関係に従ってソートを行い，その結果を返します。
	 * @param sortableFieldMap フィールドが格納されたコレクション
	 * @return ソート結果のフィールドの配列
	 */
	private Field[] sortFields(Map<String, SortableField> sortableFieldMap) {
		List<SortableField> rootList = new LinkedList<SortableField>();
		for (SortableField field : sortableFieldMap.values()) {
			String parentName = field.getParentName();
			if (StringUtils.isEmpty(parentName) || parentName.equals("this")) {
				rootList.add(field);
			} else {
				SortableField parentField = sortableFieldMap.get(parentName);
				parentField.addKid(field);
			}
		}
		List<Field> fieldList = new LinkedList<Field>();
		for (SortableField field : rootList) {
			field.accept(fieldList);
		}
		return fieldList.toArray(new Field[0]);
	}
	
	/**
	 * ソートのための機能を持つフィールドのラッパークラスです。
	 */
	private static class SortableField {
		
		/** フィールドオブジェクト */
		private Field field;
		
		/** このフィールドの子となるフィールドのコレクション */
		private List<SortableField> kids = new LinkedList<SortableField>();
		
		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param field フィールドオブジェクト
		 */
		private SortableField(Field field) {
			this.field = field;
		}
		
		/**
		 * このフィールドの親となるフィールドの名前を返します。
		 * @return 親のフィールドの名前
		 */
		private String getParentName() {
			WicketComponent annotation = field.getAnnotation(WicketComponent.class);
			return annotation.parent();
		}
		
		/**
		 * このフィールドの子となるフィールドを追加します。
		 * @param kid 子となるフィールド
		 */
		private void addKid(SortableField kid) {
			kids.add(kid);
		}
		
		/**
		 * 指定されたコレクションに，自身が持つフィールドを追加します。
		 * さらに，子のフィールドについても再帰的に呼び出します。
		 * @param fieldList フィールドを格納する対象となるコレクション
		 */
		private void accept(List<Field> fieldList) {
			fieldList.add(field);
			for (SortableField kid : kids) {
				kid.accept(fieldList);
			}
		}
	}
	
}
