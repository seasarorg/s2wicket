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
