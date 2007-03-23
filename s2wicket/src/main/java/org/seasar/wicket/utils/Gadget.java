package org.seasar.wicket.utils;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.WebPage;
import wicket.markup.html.panel.Panel;

/**
 * ちょっとした処理を提供するユーティリティクラスです。
 * @author Yoichiro Tanaka
 * @since 1.3.0
 */
public class Gadget {

	/**
	 * 指定されたクラスがWicketで提供されたクラスかどうかを返します。
	 * @param clazz クラスオブジェクト
	 * @return WebPage, Page, Panel, MarkupContainer, Component クラスだった場合は true
	 */
	public static boolean isWicketClass(Class clazz) {
		return (clazz.equals(WebPage.class))
			|| (clazz.equals(Page.class))
			|| (clazz.equals(Panel.class))
			|| (clazz.equals(MarkupContainer.class))
			|| (clazz.equals(Component.class));
	}
	
	/**
	 * 指定されたメソッドがwriteReplace()メソッドがどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	public static boolean isWriteReplace(Method method) {
		return isMethod(method, Object.class, new Class[0], "writeReplace");
	}
	
	/**
	 * 指定されたメソッドがequals()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	public static boolean isEquals(Method method) {
		return isMethod(method, boolean.class, new Class[] {Object.class}, "equals");
	}
	
	/**
	 * 指定されたメソッドがhashCode()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	public static boolean isHashCode(Method method) {
		return isMethod(method, int.class, new Class[0], "hashCode");
	}
	
	/**
	 * 指定されたメソッドがtoString()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	public static boolean isToString(Method method) {
		return isMethod(method, String.class, new Class[0], "toString");
	}
	
	/**
	 * 指定されたメソッドがfinalize()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	public static boolean isFinalize(Method method) {
		return isMethod(method, void.class, new Class[0], "finalize");
	}
	
	public static boolean isGetMethodInterceptor(Method method) {
		return isMethod(method, MethodInterceptor.class, new Class[0], "getMethodInterceptor");
	}

	/**
	 * 指定されたメソッドが，指定された条件に一致するかどうか返します。
	 * @param method メソッドオブジェクト
	 * @param type 戻り値の型
	 * @param argTypes 引数の型の配列
	 * @param name メソッド名
	 * @return 一致した場合は true
	 */
	public static boolean isMethod(Method method, Class type, Class[] argTypes, String name) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if ((method.getReturnType() == type)
				&& (parameterTypes.length == argTypes.length)
				&& (method.getName().equals(name))) {
			for (int i = 0; i < argTypes.length; i++) {
				if (parameterTypes[i] != argTypes[i]) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

}
