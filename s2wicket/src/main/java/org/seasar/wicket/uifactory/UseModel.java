package org.seasar.wicket.uifactory;

public enum UseModel {
	
	/**
	 * フィールドに定義されたモデルを使用します。
	 */
	FIELD,
	
	/**
	 * 必要なときに読み込み，不必要になったら取り除くモデルを使用します。
	 */
	LOADABLE_DETACHABLE

}
