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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.seasar.framework.container.S2Container;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 * {@link FieldValueProducer}のテストケースクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
public class FieldValueProducerTest extends TestCase {
	
	/**
	 * コンストラクタに不正な値を渡したときのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testConstructorContainerLocatorInvalidArg() throws Exception {
		try {
			new FieldValueProducer(null, null);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			assertTrue(expected.getMessage().startsWith("containerLocator"));
		}
		try {
			IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
			new FieldValueProducer(containerLocator, null);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			assertTrue(expected.getMessage().equals("fieldFilterList is null."));
		}
		try {
			IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
			List<FieldFilter> fieldFilters = new ArrayList<FieldFilter>();
			new FieldValueProducer(containerLocator, fieldFilters);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			assertTrue(expected.getMessage().equals("fieldFilterList is empty."));
		}
	}
	
	/**
	 * {@link FieldValueProducer#isSupported(Field)}のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testIsSupported() throws Exception {
		Field field = Component.class.getDeclaredField("fieldTest");
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		// フィルタがtrueを返す場合
		FieldFilter fieldFilter = createMock(FieldFilter.class);
		expect(fieldFilter.isSupported(field)).andReturn(true);
		replay(fieldFilter);
		List<FieldFilter> filters = new ArrayList<FieldFilter>();
		filters.add(fieldFilter);
		FieldValueProducer target = new FieldValueProducer(containerLocator, filters);
		assertTrue(target.isSupported(field));
		verify(fieldFilter);
		reset(fieldFilter);
		// フィルタがfalseを返す場合
		expect(fieldFilter.isSupported(field)).andReturn(false);
		replay(fieldFilter);
		filters = new ArrayList<FieldFilter>();
		filters.add(fieldFilter);
		target = new FieldValueProducer(containerLocator, filters);
		assertFalse(target.isSupported(field));
		verify(fieldFilter);
	}
	
	/**
	 * {@link FieldValueProducer#getValue(Field)}のテストを行います。
	 * 引数にnullを渡したときのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testGetValueNull() throws Exception {
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		FieldFilter fieldFilter = createMock(FieldFilter.class);
		List<FieldFilter> filters = new ArrayList<FieldFilter>();
		filters.add(fieldFilter);
		FieldValueProducer target = new FieldValueProducer(containerLocator, filters);
		try {
			target.getValue(null);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			// N/A
		}
	}
	
	/**
	 * {@link FieldValueProducer#getValue(Field)}のテストを行います。
	 * 型でルックアップするケースをテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testGetValueByType() throws Exception {
		Field field = Component.class.getDeclaredField("fieldTest");
		Service service = createMock(Service.class);
		service.foo();
		S2Container container = createMock(S2Container.class);
		expect(container.getComponent(Service.class)).andReturn(service);
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		FieldFilter fieldFilter = createMock(FieldFilter.class);
		expect(fieldFilter.getLookupComponentName(field)).andReturn(null);
		replay(service);
		replay(container);
		replay(containerLocator);
		replay(fieldFilter);
		List<FieldFilter> filters = new ArrayList<FieldFilter>();
		filters.add(fieldFilter);
		FieldValueProducer target = new FieldValueProducer(containerLocator, filters);
		Object result = target.getValue(field);
		assertNotNull(result);
		((Service)result).foo();
		verify(containerLocator);
		verify(container);
		verify(service);
		verify(fieldFilter);
	}
	
	/**
	 * {@link FieldValueProducer#getValue(Field)}のテストを行います。
	 * 名前でルックアップするケースをテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testGetValueByName() throws Exception {
		Field field = Component.class.getDeclaredField("fieldTest");
		Service service = createMock(Service.class);
		service.foo();
		S2Container container = createMock(S2Container.class);
		expect(container.getComponent("component1")).andReturn(service);
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		FieldFilter fieldFilter = createMock(FieldFilter.class);
		expect(fieldFilter.getLookupComponentName(field)).andReturn("component1");
		replay(service);
		replay(container);
		replay(containerLocator);
		replay(fieldFilter);
		List<FieldFilter> filters = new ArrayList<FieldFilter>();
		filters.add(fieldFilter);
		FieldValueProducer target = new FieldValueProducer(containerLocator, filters);
		Object result = target.getValue(field);
		assertNotNull(result);
		((Service)result).foo();
		verify(containerLocator);
		verify(container);
		verify(service);
		verify(fieldFilter);
	}
	
	/**
	 * テスト用のクラスです。
	 */
	private static class Component {
		
		/** テスト用のフィールド */
		private Service fieldTest;
		
	}
	
	/**
	 * インジェクションされるオブジェクトのインタフェースです。
	 */
	private static interface Service  {
		
		/**
		 * テスト用のメソッドです。
		 */
		public void foo();
		
	}

}
