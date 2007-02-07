/*
 * $Id$
 * 
 * ==============================================================================
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

package org.seasar.wicket.injection;

import static org.seasar.wicket.injection.Gadget.isEquals;
import static org.seasar.wicket.injection.Gadget.isFinalize;
import static org.seasar.wicket.injection.Gadget.isGetMethodInterceptor;
import static org.seasar.wicket.injection.Gadget.isHashCode;
import static org.seasar.wicket.injection.Gadget.isToString;
import static org.seasar.wicket.injection.Gadget.isWriteReplace;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Seasarコンポーネントのメソッド呼び出しを代理するプロキシオブジェクトを生成する処理を持つクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class ProxyFactory {
	
	/**
	 * プロキシオブジェクトを生成して返します。
	 * @param componentType コンポーネントの型
	 * @param resolver Seasarコンポーネントリゾルバ
	 * @return プロキシオブジェクト
	 */
	static Object create(Class componentType, ComponentResolver resolver) {
		// インターセプタを生成
		ComponentMethodInterceptor interceptor = new ComponentMethodInterceptor(componentType, resolver);
		// エンハンサを生成
		Enhancer enhancer = new Enhancer();
		// コンポーネントの型がインタフェースかどうかチェック
		if (componentType.isInterface()) {
			// 実装するインタフェースをセット
			enhancer.setInterfaces(new Class[] {
					componentType, Serializable.class, WriteReplaceHolder.class, MethodInterceptorHolder.class});
		} else {
			// 実装するインタフェースをセット
			enhancer.setInterfaces(new Class[] {
					Serializable.class, WriteReplaceHolder.class, MethodInterceptorHolder.class});
			// スーパークラスを指定
			enhancer.setSuperclass(componentType);
		}
		// インターセプタをセット
		enhancer.setCallback(interceptor);
		// プロキシオブジェクトを生成して返却
		return enhancer.create();
	}
	
	/**
	 * Seasarコンポーネントのメソッド呼び出しをインターセプトした際の処理を行うクラスです。
	 */
	private static class ComponentMethodInterceptor
			implements MethodInterceptor, Serializable, WriteReplaceHolder, MethodInterceptorHolder {
		
		/** コンポーネントの型名 */
		private String componentTypeName;
		
		/** コンポーネントリゾルバ */
		private ComponentResolver componentResolver;

		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param componentType コンポーネントの型
		 * @param componentResolver コンポーネントリゾルバ
		 */
		private ComponentMethodInterceptor(Class componentType, ComponentResolver componentResolver) {
			super();
			// 引数をフィールドに保持
			componentTypeName = componentType.getName();
			this.componentResolver = componentResolver;
		}

		/**
		 * 指定されたオブジェクトのメソッド呼び出しをインターセプトします。
		 * ここでは，指定されたオブジェクトの代わりに，コンポーネントリゾルバによって得られたSeasarコンポーネントのメソッドを呼び出します。
		 * @param object コール対象のオブジェクト
		 * @param method 呼び出されたメソッドの情報
		 * @param args メソッド呼び出しの際に指定された引数
		 * @param methodProxy メソッドプロキシ
		 * @throws Throwable メソッド呼び出し時に何らかの例外が発生したとき
		 */
		public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
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
			// getMethodInterceptor()呼び出しかチェック
			if (isGetMethodInterceptor(method)) {
				// このプロキシのメソッドを呼び出す
				return getMethodInterceptor();
			}
			// Seasarコンポーネントを取得
			Object target = componentResolver.getTargetObject();
			// Seasarコンポーネントのメソッドを呼び出し，その結果を返却
			return methodProxy.invoke(target, args);
		}

		/**
		 * このプロキシオブジェクトがシリアライズされる際に呼び出されます。
		 * ここでは，{@link SerializedProxy}オブジェクトをこのオブジェクトの代わりにシリアライズ対象として返却します。
		 * @return {@link SerializedProxy}オブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		public Object writeReplace() throws ObjectStreamException {
			return new SerializedProxy(componentTypeName, componentResolver);
		}
		
		/**
		 * 指定されたオブジェクトとこのオブジェクトの内容の一致性を返します。
		 * @param obj 比較対象のオブジェクト
		 * @return 内容が一致する場合は true
		 */
		public boolean equals(Object obj) {
			if (!(obj instanceof ComponentMethodInterceptor)) {
				return false;
			} else {
				ComponentMethodInterceptor target = (ComponentMethodInterceptor)obj;
				return new EqualsBuilder()
					.append(componentTypeName, target.componentTypeName)
					.append(componentResolver, target.componentResolver)
					.isEquals();
			}
		}
		
		/**
		 * このオブジェクトのハッシュ値を返します。
		 * @return ハッシュ値
		 */
		public int hashCode() {
			int hashCode = componentTypeName.hashCode();
			hashCode += componentResolver.hashCode() * 127;
			return hashCode;
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
		
		/** コンポーネントの型の名前 */
		private String componentTypeName;
		
		/** コンポーネントリゾルバ */
		private ComponentResolver componentResolver;

		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param componentTypeName コンポーネントの型の名前
		 * @param componentResolver コンポーネントリゾルバ
		 */
		private SerializedProxy(String componentTypeName, ComponentResolver componentResolver) {
			super();
			this.componentTypeName = componentTypeName;
			this.componentResolver = componentResolver;
		}
		
		/**
		 * このオブジェクトが永続化状態から復元される際に呼び出されます。
		 * ここでは，新規に動的プロキシを生成して返しています。
		 * @return 新規に生成されたプロキシオブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		private Object readResolve() throws ObjectStreamException {
			try {
				Class componentType = Class.forName(componentTypeName);
				Object proxy = ProxyFactory.create(componentType, componentResolver);
				return proxy;
			} catch(ClassNotFoundException e) {
				throw new IllegalStateException("ComponentType[" + componentTypeName + "] class not found.", e);
			}
		}
		
	}
	
	/**
	 * writeReplace()メソッドを持つことを規定するインタフェースです。
	 */
	private static interface WriteReplaceHolder {
		
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
	static interface MethodInterceptorHolder {

		/**
		 * メソッドインターセプタを返します。
		 * @return メソッドインターセプタ
		 */
		public MethodInterceptor getMethodInterceptor();
		
	}

}
