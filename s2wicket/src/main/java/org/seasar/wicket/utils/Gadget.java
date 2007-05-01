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

package org.seasar.wicket.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.WebPage;
import wicket.markup.html.list.ListItem;
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
	
	public static boolean isPopulateItem(Method method) {
		return isMethod(method, void.class, new Class[] {ListItem.class}, "populateItem");
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
	
	/**
	 * 指定されたクラスが持つコンストラクタの中で，指定された個数の引数を持ち，最後の引数の型が指定された型のオブジェクトを受け入れることができるコンストラクタを返します。
	 * @param clazz コンストラクタを持つクラスオブジェクト
	 * @param argCount 引数の数
	 * @param targetClazz 最後の引数の検査対象の型 
	 * @return 条件に一致するコンストラクタ
	 * @throws IllegalArgumentException 条件に一致するコンストラクタがなかったとき
	 */
	public static Constructor getConstructorMatchLastArgType(Class clazz, int argCount, Class targetClazz) {
		// 全てのコンストラクタを取得
		Constructor[] constructors = clazz.getConstructors();
		// コンストラクタごとに処理
		for (int i = 0; i < constructors.length; i++) {
			// 引数の型の配列を取得
			Class[] parameterTypes = constructors[i].getParameterTypes();
			// 引数の個数をチェック
			if (argCount == parameterTypes.length) {
				// 引数の型をチェック
				if (((Class<? extends Object>)parameterTypes[argCount - 1]).isAssignableFrom(targetClazz)) {
					// 結果を返却
					return constructors[i];
				}
			}
		}
		// コンストラクタが存在しなかった
		// TODO 例外処理
		throw new IllegalArgumentException(
				"Constructor to match [" + clazz.getName() + ":" + targetClazz.getName() + "] not found ");
	}

}
