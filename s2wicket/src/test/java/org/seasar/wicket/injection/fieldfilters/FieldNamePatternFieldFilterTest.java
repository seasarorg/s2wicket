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

package org.seasar.wicket.injection.fieldfilters;

import java.lang.reflect.Field;
import java.util.regex.PatternSyntaxException;

import org.seasar.wicket.injection.SeasarComponentInjectionListener;

import junit.framework.TestCase;

/**
 * {@link FieldNamePatternFieldFilter}のテストケースクラスです。
 * @author Yoichiro Tanaka
 * @since 1.2.0
 */
public class FieldNamePatternFieldFilterTest extends TestCase {

	/**
	 * {@link FieldNamePatternFieldFilter}のpackageNamePatternRegexプロパティのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testPackageNamePatternRegexProperty() throws Exception {
		String packageNamePatternRegex = "org\\.seasar\\.wicket\\.injection[a-z\\.]*";
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		target.setPackageNamePatternRegex(packageNamePatternRegex);
		assertEquals(packageNamePatternRegex, target.getPackageNamePatternRegex());
		//
		packageNamePatternRegex = null;
		try {
			target.setPackageNamePatternRegex(packageNamePatternRegex);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			String message = expected.getMessage();
			assertEquals("packageNamePatternRegex is null.", message);
		}
		//
		packageNamePatternRegex = "";
		try {
			target.setPackageNamePatternRegex(packageNamePatternRegex);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			String message = expected.getMessage();
			assertEquals("packageNamePatternRegex is empty.", message);
		}
		//
		packageNamePatternRegex = ".*[}";
		try {
			target.setPackageNamePatternRegex(packageNamePatternRegex);
			fail("PatternSyntaxException not thrown.");
		} catch(PatternSyntaxException expected) {
			// N/A
		}
	}
	
	/**
	 * {@link FieldNamePatternFieldFilter}のclassNamePatternRegexプロパティのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testClassNamePatternRegexProperty() throws Exception {
		String classNamePatternRegex = ".*Test\\$.*1";
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		target.setClassNamePatternRegex(classNamePatternRegex);
		assertEquals(classNamePatternRegex, target.getClassNamePatternRegex());
		//
		classNamePatternRegex = null;
		try {
			target.setClassNamePatternRegex(classNamePatternRegex);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			String message = expected.getMessage();
			assertEquals("classNamePatternRegex is null.", message);
		}
		//
		classNamePatternRegex = "";
		try {
			target.setClassNamePatternRegex(classNamePatternRegex);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			String message = expected.getMessage();
			assertEquals("classNamePatternRegex is empty.", message);
		}
		//
		classNamePatternRegex = ".*[}";
		try {
			target.setClassNamePatternRegex(classNamePatternRegex);
			fail("PatternSyntaxException not thrown.");
		} catch(PatternSyntaxException expected) {
			// N/A
		}
	}

	/**
	 * {@link FieldNamePatternFieldFilter}のfieldNamePatternRegexプロパティのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testFieldNamePatternRegexProperty() throws Exception {
		String fieldNamePatternRegex = "injection.*";
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		target.setFieldNamePatternRegex(fieldNamePatternRegex);
		assertEquals(fieldNamePatternRegex, target.getFieldNamePatternRegex());
		//
		fieldNamePatternRegex = null;
		try {
			target.setFieldNamePatternRegex(fieldNamePatternRegex);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			String message = expected.getMessage();
			assertEquals("fieldNamePatternRegex is null.", message);
		}
		//
		fieldNamePatternRegex = "";
		try {
			target.setFieldNamePatternRegex(fieldNamePatternRegex);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			String message = expected.getMessage();
			assertEquals("fieldNamePatternRegex is empty.", message);
		}
		//
		fieldNamePatternRegex = ".*[}";
		try {
			target.setFieldNamePatternRegex(fieldNamePatternRegex);
			fail("PatternSyntaxException not thrown.");
		} catch(PatternSyntaxException expected) {
			// N/A
		}
	}
	
	/**
	 * {@link FieldNamePatternFieldFilter#getLookupComponentName(java.lang.reflect.Field)}に
	 * 不正な引数を与えた場合のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testGetLookupComponentNameInvalidArg() throws Exception {
		Field field = null;
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		try {
			target.getLookupComponentName(field);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			// N/A
		}
	}
	
	/**
	 * {@link FieldNamePatternFieldFilter#getLookupComponentName(Field)}のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testGetLookupComponentName() throws Exception {
		Field field = TestComponent1.class.getDeclaredField("injectionField");
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		String result = target.getLookupComponentName(field);
		assertEquals("injectionField", result);
	}
	
	/**
	 * {@link FieldNamePatternFieldFilter#isSupported(Field)}に
	 * 不正な引数を与えた場合のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testIsSupportedInvalidArg() throws Exception {
		Field field = null;
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		try {
			target.isSupported(field);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			// N/A
		}
	}
	
	/**
	 * {@link FieldNamePatternFieldFilter#isSupported(Field)}のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testIsSupported() throws Exception {
		String packageNamePatternRegex = "org\\.seasar\\.wicket\\.injection[a-z\\.]+";
		String classNamePatternRegex = ".*1";
		String fieldNamePatternRegex = "injection.*";
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter(
				packageNamePatternRegex, classNamePatternRegex, fieldNamePatternRegex);
		Field field = TestComponent1.class.getDeclaredField("injectionField");
		assertTrue(target.isSupported(field));
		field = TestComponent1.class.getDeclaredField("field");
		assertFalse(target.isSupported(field));
		field = TestComponent2.class.getDeclaredField("injectionField");
		assertFalse(target.isSupported(field));
		field = SeasarComponentInjectionListener.class.getDeclaredField("injectionProcessor");
		assertFalse(target.isSupported(field));
	}
	
	/**
	 * {@link FieldNamePatternFieldFilter#isSupported(Field)}のテストを行います。
	 * ここでは，正規表現パターンがセットされていない状態で{@link FieldNamePatternFieldFilter#isSupported(Field)}
	 * メソッドが呼び出された場合をテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testIsSupportedNotSetPattern() throws Exception {
		String packageNamePatternRegex = "org\\.seasar\\.wicket\\.injection[a-z\\.]*";
		String classNamePatternRegex = ".*1";
		String fieldNamePatternRegex = "injection.*";
		Field field = TestComponent1.class.getDeclaredField("injectionField");
		FieldNamePatternFieldFilter target = new FieldNamePatternFieldFilter();
		target.setClassNamePatternRegex(classNamePatternRegex);
		target.setFieldNamePatternRegex(fieldNamePatternRegex);
		try {
			target.isSupported(field);
			fail("IllegalStateException not thrown.");
		} catch(IllegalStateException expected) {
			String message = expected.getMessage();
			assertEquals("packageNamePatternRegex not set.", message);
		}
		target = new FieldNamePatternFieldFilter();
		target.setPackageNamePatternRegex(packageNamePatternRegex);
		target.setFieldNamePatternRegex(fieldNamePatternRegex);
		try {
			target.isSupported(field);
			fail("IllegalStateException not thrown.");
		} catch(IllegalStateException expected) {
			String message = expected.getMessage();
			assertEquals("classNamePatternRegex not set.", message);
		}
		target = new FieldNamePatternFieldFilter();
		target.setPackageNamePatternRegex(packageNamePatternRegex);
		target.setClassNamePatternRegex(classNamePatternRegex);
		try {
			target.isSupported(field);
			fail("IllegalStateException not thrown.");
		} catch(IllegalStateException expected) {
			String message = expected.getMessage();
			assertEquals("fieldNamePatternRegex not set.", message);
		}
	}
	
	/**
	 * テスト用に使用するコンポーネントクラスです。
	 * このクラスは，インジェクション対象のフィールドを持っています。
	 */
	private static class TestComponent1 {
		
		/** インジェクションされるフィールド */
		private Object injectionField;
		
		/** インジェクションされないフィールド */
		private Object field;
		
	}

	/**
	 * テスト用に使用するコンポーネントクラスです。
	 * このクラスは，インジェクション対象にはなりません。
	 */
	private static class TestComponent2 {
		
		/** インジェクションされないフィールド */
		private Object injectionField;
		
	}

}
