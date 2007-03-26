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
	static Component create(String fieldName, Class fieldType, Component target, String wicketId, IModel model) {
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
			return (Component)enhancer.create(new Class[] {String.class, IModel.class}, new Object[] {wicketId, model});
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
		private IModel model;
		
		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param fieldName 処理対象のフィールドの名前
		 * @param fieldType フィールドの型
		 * @param target 処理対象のコンポーネントオブジェクト
		 * @param wicketId wicket:id
		 * @param model モデルオブジェクト
		 */
		private WicketComponentMethodInterceptor(String fieldName, Class fieldType, Component target, String wicketId, IModel model) {
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
				Method targetMethod = clazz.getMethod(methodName, method.getParameterTypes());
				// メソッド呼び出し
				Object result = targetMethod.invoke(target, args);
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
		private IModel model;

		/**
		 * このオブジェクトが生成されるときに呼び出されます。
		 * @param fieldName 処理対象のフィールドの名前
		 * @param fieldTypeName フィールドの型名
		 * @param target 処理対象のコンポーネントオブジェクト
		 * @param wicketId wicket:id
		 * @param model モデルオブジェクト
		 */
		private SerializedProxy(String fieldName, String fieldTypeName, Component target, String wicketId, IModel model) {
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
