package org.seasar.wicket.uifactory;

/**
 * モデルの種別を定義した列挙型です。
 * @see WicketModel
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
public enum ModelType {

	/**
	 * このモデルオブジェクトをそのままコンポーネントに関連付けます。
	 */
	RAW,
	
	/**
	 * 基本モデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	BASIC,
	
	/**
	 * プロパティモデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	PROPERTY,
	
	/**
	 * 複合プロパティモデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	COMPOUND_PROPERTY,
	
	/**
	 * 複合バインドプロパティモデルとして，このモデルオブジェクトをコンポーネントに関連付けします。
	 */
	BOUND_COMPOUND_PROPERTY
	
}
