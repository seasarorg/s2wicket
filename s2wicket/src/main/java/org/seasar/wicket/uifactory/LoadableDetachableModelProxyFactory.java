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

import static org.seasar.wicket.utils.Gadget.isEquals;
import static org.seasar.wicket.utils.Gadget.isFinalize;
import static org.seasar.wicket.utils.Gadget.isHashCode;
import static org.seasar.wicket.utils.Gadget.isToString;
import static org.seasar.wicket.utils.Gadget.isWriteReplace;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.lang.StringUtils;

import wicket.Component;
import wicket.model.LoadableDetachableModel;

/**
 * {@link LoadableDetachableModel}オブジェクトの動的プロキシを生成する処理を持つファクトリクラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
class LoadableDetachableModelProxyFactory {
	
	/**
	 * プロキシオブジェクトを生成して返します。
	 * @param fieldName 処理対象のフィールドの名前
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @return プロキシオブジェクト
	 */
	static LoadableDetachableModel create(String fieldName, Component target) {
		// インターセプタを生成
		LoadableDetachableModelMethodInterceptor interceptor =
			new LoadableDetachableModelMethodInterceptor(fieldName, target);
		// エンハンサを生成
		Enhancer enhancer = new Enhancer();
		// 実装するインタフェースをセット
		enhancer.setInterfaces(new Class[] {
				Serializable.class, WriteReplaceHolder.class, MethodInterceptorHolder.class});
		// スーパークラスをセット
		enhancer.setSuperclass(LoadableDetachableModel.class);
		// インターセプタをセット
		enhancer.setCallback(interceptor);
		// プロキシオブジェクトを生成して返却
		return (LoadableDetachableModel)enhancer.create();
	}
	
	/**
	 * {@link LoadableDetachableModel}オブジェクトに対するメソッド呼び出しをインターセプトして処理を行うクラスです。
	 */
	private static class LoadableDetachableModelMethodInterceptor
			implements MethodInterceptor, Serializable, WriteReplaceHolder, MethodInterceptorHolder {

		/** 対象のコンポーネントオブジェクト */
		private Component target;
		
		/** フィールド名 */
		private String fieldName;
		
		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param fieldName 処理対象のフィールドの名前
		 * @param target 処理対象のコンポーネントオブジェクト
		 */
		private LoadableDetachableModelMethodInterceptor(String fieldName, Component target) {
			super();
			this.fieldName = fieldName;
			this.target = target;
		}

		/**
		 * 指定されたオブジェクトのメソッド呼び出しをインターセプトします。
		 * @param obj コール対象のオブジェクト
		 * @param method 呼び出されたメソッドの情報
		 * @param args メソッド呼び出しの際に指定された引数
		 * @param proxy メソッドプロキシ
		 * @throws Throwable メソッド呼び出し時に何らかの例外が発生したとき
		 */
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			// toString()呼び出しかチェック
			if (isToString(method)) {
				// このプロキシオブジェクトのメソッドを呼び出す
				return toString();
			}
			// hashCode()呼び出しかチェック
			if (isHashCode(method)) {
				// このプロキシオブジェクトのメソッドを呼び出す
				return hashCode();
			}
			// equals()呼び出しかチェック
			if (isEquals(method)) {
				// このプロキシオブジェクトのメソッドを呼び出す
				return equals(args[0]);
			}
			// finalize()呼び出しかチェック
			if (isFinalize(method)) {
				// 何もしない
				return null;
			}
			// writeReplace()呼び出しかチェック
			if (isWriteReplace(method)) {
				// このプロキシを永続化せずにダミーのオブジェクトを永続化
				return writeReplace();
			}
			// 呼び出されたメソッドがLoadableDetachableModel#load()メソッドかチェック
			if ((method.getName().equals("load"))
					|| (Modifier.isProtected(method.getModifiers()))
					|| (method.getReturnType().equals(Object.class))) {
				// メソッド名を決定
				String methodName = "load" + StringUtils.capitalize(fieldName) + "Model";
				// 対象のメソッドを取得
				Method targetMethod = target.getClass().getMethod(methodName, new Class[0]);
				// メソッド呼び出し
				Object result = targetMethod.invoke(target, new Object[0]);
				// 結果を返却
				return result;
			} else {
				// 普通にメソッドコール
				return proxy.invokeSuper(obj, args);
			}
		}

		/**
		 * このプロキシオブジェクトがシリアライズされる際に呼び出されます。
		 * ここでは，{@link SerializedProxy}オブジェクトをこのオブジェクトの代わりにシリアライズ対象として返却します。
		 * @return {@link SerializedProxy}オブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		public Object writeReplace() throws ObjectStreamException {
			return new SerializedProxy(fieldName, target);
		}
		
		/**
		 * このオブジェクトを返します。
		 * @return このオブジェクト
		 */
		public MethodInterceptor getMethodInterceptor() {
			return this;
		}
		
	}

	/**
	 * 動的プロキシオブジェクトの代わりにシリアライズされるオブジェクトのクラスです。
	 */
	private static class SerializedProxy implements Serializable {
		
		/** 対象のコンポーネントオブジェクト */
		private Component target;
		
		/** フィールド名 */
		private String fieldName;
		
		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param fieldName 処理対象のフィールドの名前
		 * @param fieldTypeName フィールドの型名
		 * @param target 処理対象のコンポーネントオブジェクト
		 * @param wicketId wicket:id
		 * @param model モデルオブジェクト
		 */
		private SerializedProxy(String fieldName, Component target) {
			super();
			this.fieldName = fieldName;
			this.target = target;
		}
		
		/**
		 * このオブジェクトが永続化状態から復元される際に呼び出されます。
		 * ここでは，新規に動的プロキシを生成して返しています。
		 * @return 新規に生成されたプロキシオブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		private Object readResolve() throws ObjectStreamException {
			Object proxy = LoadableDetachableModelProxyFactory.create(fieldName, target);
			return proxy;
		}
		
	}

	/**
	 * writeReplace()メソッドを持つことを規定するインタフェースです。
	 */
	public static interface WriteReplaceHolder {
		
		/**
		 * オブジェクトがシリアライズされる際に呼び出されます。
		 * 対象のオブジェクトではなく，別のオブジェクトをシリアライズしたい場合に使用します。
		 * @return 実際にシリアライズされるオブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		public Object writeReplace() throws ObjectStreamException;
		
	}
	
	/**
	 * メソッドインターセプタを返す処理を規定したインタフェースです。
	 */
	public static interface MethodInterceptorHolder {

		/**
		 * メソッドインターセプタを返します。
		 * @return メソッドインターセプタ
		 */
		public MethodInterceptor getMethodInterceptor();
		
	}

}
