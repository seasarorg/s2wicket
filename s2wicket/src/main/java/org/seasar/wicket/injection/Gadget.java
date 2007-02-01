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

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;

/**
 * 汎用的な処理を持つユーティリティクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
class Gadget {
	
	/**
	 * このオブジェクトが生成されるときに呼び出されます。
	 */
	private Gadget() {
		// N/A
	}
	
	/**
	 * 指定されたメソッドがwriteReplace()メソッドがどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	static boolean isWriteReplace(Method method) {
		return isMethod(method, Object.class, new Class[0], "writeReplace");
	}
	
	/**
	 * 指定されたメソッドがequals()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	static boolean isEquals(Method method) {
		return isMethod(method, boolean.class, new Class[] {Object.class}, "equals");
	}
	
	/**
	 * 指定されたメソッドがhashCode()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	static boolean isHashCode(Method method) {
		return isMethod(method, int.class, new Class[0], "hashCode");
	}
	
	/**
	 * 指定されたメソッドがtoString()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	static boolean isToString(Method method) {
		return isMethod(method, String.class, new Class[0], "toString");
	}
	
	/**
	 * 指定されたメソッドがfinalize()メソッドかどうかを返します。
	 * @param method メソッドオブジェクト
	 * @return 該当メソッドだった場合は true
	 */
	static boolean isFinalize(Method method) {
		return isMethod(method, void.class, new Class[0], "finalize");
	}
	
	static boolean isGetMethodInterceptor(Method method) {
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
	static boolean isMethod(Method method, Class type, Class[] argTypes, String name) {
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
