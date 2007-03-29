package org.seasar.wicket.uifactory;

import wicket.Component;

/**
 * Wicketコンポーネントの構築中に何らかのエラーが発生したことを示す実行時例外クラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
public class WicketUIFactoryException extends RuntimeException {
	
	/** 処理対象だったコンポーネント */
	private Component target;

	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param target 処理対象だったコンポーネントオブジェクト
	 * @param message 詳細なメッセージ
	 * @param cause 原因となった例外
	 */
	public WicketUIFactoryException(Component target, String message, Throwable cause) {
		super(message, cause);
		this.target = target;
	}

	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 * @param target 処理対象だったコンポーネントオブジェクト
	 * @param message 詳細なメッセージ
	 */
	public WicketUIFactoryException(Component target, String message) {
		super(message);
		this.target = target;
	}

	/**
	 * 詳細なメッセージを返します。
	 * @return 詳細なメッセージ
	 */
	@Override
	public String getMessage() {
		String message = super.getMessage();
		message += "[target=" + target.toString(true) + "]";
		return message;
	}
	
}
