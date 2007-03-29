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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import ognl.MethodFailedException;
import ognl.Ognl;
import ognl.OgnlException;

import org.apache.commons.lang.StringUtils;
import org.seasar.wicket.utils.Gadget;

import wicket.Component;
import wicket.Page;
import wicket.model.IModel;

/**
 * コンポーネントに対する動的プロキシを生成する処理を持つファクトリクラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
class ComponentProxyFactory {
	
	/**
	 * プロキシオブジェクトを生成して返します。
	 * @param fieldName 処理対象のフィールドの名前
	 * @param fieldType フィールドの型
	 * @param target 処理対象のコンポーネントオブジェクト
	 * @param wicketId wicket:id
	 * @param model モデルオブジェクト
	 * @return プロキシオブジェクト
	 */
	static Component create(String fieldName, Class fieldType, Component target, String wicketId, Object model) {
		// インターセプタを生成
		WicketComponentMethodInterceptor interceptor =
			new WicketComponentMethodInterceptor(fieldName, fieldType, target, wicketId, model);
		// エンハンサを生成
		Enhancer enhancer = new Enhancer();
		// 実装するインタフェースをセット
		enhancer.setInterfaces(new Class[] {
				Serializable.class, WriteReplaceHolder.class, MethodInterceptorHolder.class});
		// スーパークラスをセット
		enhancer.setSuperclass(fieldType);
		// インターセプタをセット
		enhancer.setCallback(interceptor);
		// プロキシオブジェクトを生成して返却
		if (model != null) {
			if (model instanceof IModel) {
				return (Component)enhancer.create(new Class[] {String.class, IModel.class}, new Object[] {wicketId, model});
			} else {
				Constructor constructor = Gadget.getConstructorMatchLastArgType(fieldType, 2, model.getClass());
				return (Component)enhancer.create(constructor.getParameterTypes(), new Object[] {wicketId, model});
			}
		} else {
			return (Component)enhancer.create(new Class[] {String.class}, new Object[] {wicketId});
		}
	}
	
	/**
	 * コンポーネントに対するメソッド呼び出しをインターセプトして処理を行うクラスです。
	 */
	private static class WicketComponentMethodInterceptor
			implements MethodInterceptor, Serializable, WriteReplaceHolder, MethodInterceptorHolder {
		
		/** 対象のコンポーネントオブジェクト */
		private Component target;
		
		/** フィールド名 */
		private String fieldName;
		
		/** 対象のフィールドの型名 */
		private String fieldTypeName;
		
		/** wicket:id */
		private String wicketId;
		
		/** モデルオブジェクト */
		private Object model;
		
		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param fieldName 処理対象のフィールドの名前
		 * @param fieldType フィールドの型
		 * @param target 処理対象のコンポーネントオブジェクト
		 * @param wicketId wicket:id
		 * @param model モデルオブジェクト
		 */
		private WicketComponentMethodInterceptor(String fieldName, Class fieldType, Component target, String wicketId, Object model) {
			super();
			this.fieldName = fieldName;
			this.fieldTypeName = fieldType.getName();
			this.target = target;
			this.wicketId = wicketId;
			this.model = model;
		}

		/**
		 * 指定されたオブジェクトのメソッド呼び出しをインターセプトします。
		 * @param object コール対象のオブジェクト
		 * @param method 呼び出されたメソッドの情報
		 * @param args メソッド呼び出しの際に指定された引数
		 * @param methodProxy メソッドプロキシ
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
			// 呼び出し対象のメソッドが抽象メソッドかチェック
			if (Modifier.isAbstract(method.getModifiers())) {
				// 実際に呼び出すメソッドの名前を決定
				String methodName = method.getName();
				methodName += StringUtils.capitalize(fieldName);
				// コンポーネントオブジェクトからメソッドを取得
				Class<? extends Component> clazz = target.getClass();
				Method targetMethod = getMethod(clazz, methodName, method.getParameterTypes());
				// メソッドが存在したかチェック
				if (targetMethod != null) {
					// メソッド呼び出し
					Object result = targetMethod.invoke(target, args);
					// 結果を返却
					return result;
				} else {
					// フィールドオブジェクトを取得
					Field field = target.getClass().getDeclaredField(fieldName);
					// WicketComponentアノテーションを取得
					WicketComponent wicketComponentAnnotation = field.getAnnotation(WicketComponent.class);
					// 呼び出されたメソッド名に一致するWicketActionアノテーションを取得
					WicketAction wicketAction = getWicketActionAnnotation(wicketComponentAnnotation, method.getName());
					// 該当するWicketActionアノテーションが存在したかチェック
					if (wicketAction != null) {
						// 実行する式を取得
						String exp = wicketAction.exp();
						// 式が指定されたかチェック
						if (StringUtils.isNotEmpty(exp)) {
							try {
								// 式をパース
								Object parsedExp = Ognl.parseExpression(exp);
								// 式を評価し，評価結果を取得
								Ognl.getValue(parsedExp, target);
							} catch(MethodFailedException e) {
								// handleExceptionメソッドを取得
								Method handleExceptionMethod = getMethod(target.getClass(), "handleException", new Class[] {Object.class, String.class, Exception.class});
								// 取得できたかチェック
								if (handleExceptionMethod != null) {
									// handleExceptionメソッド呼び出し
									handleExceptionMethod.invoke(target, new Object[] {target, method.getName(), e.getReason()});
									// 結果を返却
									return null;
								} else {
									// 原因となった例外を取得してスロー
									throw e.getReason();
								}
							} catch(OgnlException e) {
								throw new WicketUIFactoryException(target, "Evaluation of OGNL expression failed. exp=[" + exp + "]");
							}
						}
						// responsePage属性値を取得
						String responsePage = wicketAction.responsePage();
						// responsePage属性が指定されたかチェック
						if (StringUtils.isNotEmpty(responsePage)) {
							// ページクラスを取得
							Class<?> pageClazz;
							try {
								pageClazz = Class.forName(responsePage);
							} catch(ClassNotFoundException e) {
								// 処理対象のコンポーネントが所属するページオブジェクトのクラスのパッケージからページクラスを取得
								Page page = target.getPage();
								Package targetPackage = page.getClass().getPackage();
								pageClazz = Class.forName(targetPackage.getName() + "." + responsePage);
							}
							// レスポンスページをセット
							target.setResponsePage(pageClazz);
						}
					}
				}
				// 結果を返却
				return null;
			} else {
				// 普通にメソッドコール
				return proxy.invokeSuper(obj, args);
			}
		}
		
		/**
		 * 指定されたWicketComponentアノテーションが持つactions属性から，指定されたメソッド名に対応するWicketActionアノテーションを取得して返します。
		 * @param wicketComponentAnnotation WicketComponentアノテーション
		 * @param methodName メソッド名
		 * @return WicketActionアノテーション もし存在しなかった場合はnull
		 */
		private WicketAction getWicketActionAnnotation(WicketComponent wicketComponentAnnotation, String methodName) {
			WicketAction[] wicketActions = wicketComponentAnnotation.actions();
			for (int i = 0; i < wicketActions.length; i++) {
				if (wicketActions[i].method().equals(methodName)) {
					return wicketActions[i];
				}
			}
			return null;
		}
		
		/**
		 * 指定されたクラスに定義されたメソッドの中で，指定されたメソッド名と引数の型を持つメソッドを返します。
		 * @param clazz 処理対象のクラスオブジェクト
		 * @param methodName メソッド名
		 * @param parameterTypes 引数の型の配列
		 * @return 条件に一致したメソッドの情報を持つオブジェクト もし一致するメソッドがなければnull
		 */
		private Method getMethod(Class clazz, String methodName, Class[] parameterTypes) {
			try {
				return clazz.getMethod(methodName, parameterTypes);
			} catch(NoSuchMethodException e) {
				return null;
			}
		}

		/**
		 * このプロキシオブジェクトがシリアライズされる際に呼び出されます。
		 * ここでは，{@link SerializedProxy}オブジェクトをこのオブジェクトの代わりにシリアライズ対象として返却します。
		 * @return {@link SerializedProxy}オブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		public Object writeReplace() throws ObjectStreamException {
			return new SerializedProxy(fieldName, fieldTypeName, target, wicketId, model);
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
		
		/** 対象のフィールドの型名 */
		private String fieldTypeName;
		
		/** wicket:id */
		private String wicketId;
		
		/** モデルオブジェクト */
		private Object model;

		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param fieldName 処理対象のフィールドの名前
		 * @param fieldTypeName フィールドの型名
		 * @param target 処理対象のコンポーネントオブジェクト
		 * @param wicketId wicket:id
		 * @param model モデルオブジェクト
		 */
		private SerializedProxy(String fieldName, String fieldTypeName, Component target, String wicketId, Object model) {
			super();
			this.fieldName = fieldName;
			this.fieldTypeName = fieldTypeName;
			this.target = target;
			this.wicketId = wicketId;
			this.model = model;
		}
		
		/**
		 * このオブジェクトが永続化状態から復元される際に呼び出されます。
		 * ここでは，新規に動的プロキシを生成して返しています。
		 * @return 新規に生成されたプロキシオブジェクト
		 * @throws ObjectStreamException 何らかの例外が発生したとき
		 */
		private Object readResolve() throws ObjectStreamException {
			try {
				Class fieldType = Class.forName(fieldTypeName);
				Object proxy = ComponentProxyFactory.create(fieldName, fieldType, target, wicketId, model);
				return proxy;
			} catch(ClassNotFoundException e) {
				throw new IllegalStateException("Field type [" + fieldTypeName + "] class not found.", e);
			}
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
