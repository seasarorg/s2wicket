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

import java.util.ArrayList;
import java.util.List;

import org.seasar.framework.container.S2Container;
import org.seasar.wicket.injection.fieldfilters.AnnotationFieldFilter;
import org.seasar.wicket.injection.fieldfilters.SeasarComponent;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 * {@link InjectionProcessor}のテストケースクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
public class InjectionProcessorTest extends TestCase {

	/**
	 * コンストラクタに不正な値を渡したときのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testConstructorInvalidArg() throws Exception {
		try {
			new InjectionProcessor(null);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			// N/A
		}
		try {
			new InjectionProcessor(null, null);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			assertEquals("containerLocator is null.", expected.getMessage());
		}
		try {
			IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
			new InjectionProcessor(containerLocator, null);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			assertEquals("fieldFilters is null.", expected.getMessage());
		}
		try {
			IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
			List<FieldFilter> filters = new ArrayList<FieldFilter>();
			new InjectionProcessor(containerLocator, filters);
			fail("IllegalArgumentException not thrown.");
		} catch(IllegalArgumentException expected) {
			assertEquals("fieldFilters is empty.", expected.getMessage());
		}
	}
	
	/**
	 * {@link InjectionProcessor#InjectionProcessor(IS2ContainerLocator)}のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testConstructor1Arg() throws Exception {
		FieldFilter fieldFilter = createMock(FieldFilter.class);
		S2Container container = createMock(S2Container.class);
		expect(container.findComponents(FieldFilter.class)).andReturn(new FieldFilter[]{fieldFilter});
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		replay(container);
		replay(containerLocator);
		InjectionProcessor target = new InjectionProcessor(containerLocator);
		verify(container);
		verify(containerLocator);
		FieldValueProducer fieldValueProducer = target.getFieldValueProducer();
		assertNotNull(fieldValueProducer);
		List<FieldFilter> fieldFilters = fieldValueProducer.getFieldFilters();
		assertEquals(2, fieldFilters.size());
		assertSame(fieldFilter, fieldFilters.get(0));
		assertTrue(fieldFilters.get(1) instanceof AnnotationFieldFilter);
	}
	
	/**
	 * {@link InjectionProcessor#InjectionProcessor(IS2ContainerLocator, List)}のテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testConstructor2Arg() throws Exception {
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		replay(containerLocator);
		FieldFilter fieldFilter = createMock(FieldFilter.class);
		replay(fieldFilter);
		List<FieldFilter> filters = new ArrayList<FieldFilter>();
		filters.add(fieldFilter);
		InjectionProcessor target = new InjectionProcessor(containerLocator, filters);
		verify(containerLocator);
		verify(fieldFilter);
		FieldValueProducer fieldValueProducer = target.getFieldValueProducer();
		List<FieldFilter> fieldFilters = fieldValueProducer.getFieldFilters();
		assertSame(filters, fieldFilters);
		assertEquals(1, fieldFilters.size());
		assertSame(fieldFilter, fieldFilters.get(0));
	}
	
	/**
	 * {@link InjectionProcessor#inject(Object)}のテストを行います。
	 * フィールドの型によるルックアップをテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testInjectByType() throws Exception {
		Service service = createMock(Service.class);
		service.foo();
		S2Container container = createMock(S2Container.class);
		expect(container.findComponents(FieldFilter.class)).andReturn(null);
		expect(container.getComponent(Service.class)).andReturn(service);
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		expect(containerLocator.get()).andReturn(container);
		replay(service);
		replay(container);
		replay(containerLocator);
		ComponentForType component = new ComponentForType();
		InjectionProcessor target = new InjectionProcessor(containerLocator);
		target.inject(component);
		component.serviceByType.foo();
		verify(service);
		verify(container);
		verify(containerLocator);
		assertNull(component.serviceNotInject);
	}
	
	/**
	 * {@link InjectionProcessor#inject(Object)}のテストを行います。
	 * アノテーションの属性で指定された名前によるルックアップをテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testInjectByName() throws Exception {
		Service service = createMock(Service.class);
		service.foo();
		S2Container container = createMock(S2Container.class);
		expect(container.findComponents(FieldFilter.class)).andReturn(null);
		expect(container.getComponent("service1")).andReturn(service);
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		expect(containerLocator.get()).andReturn(container);
		replay(service);
		replay(container);
		replay(containerLocator);
		ComponentForName component = new ComponentForName();
		InjectionProcessor target = new InjectionProcessor(containerLocator);
		target.inject(component);
		component.serviceByName.foo();
		verify(service);
		verify(container);
		verify(containerLocator);
		assertNull(component.serviceNotInject);
	}
	
	/**
	 * 型によるルックアップをテストするためのクラスです。
	 */
	private static class ComponentForType {
		
		@SeasarComponent
		Service serviceByType;
		
		Service serviceNotInject;
		
	}

	/**
	 * 名前によるルックアップをテストするためのクラスです。
	 */
	private static class ComponentForName {
		
		@SeasarComponent(name="service1")
		Service serviceByName;
		
		Service serviceNotInject;
		
	}

	/**
	 * インジェクションされるオブジェクトのインタフェースです。
	 */
	private static interface Service {
		
		public void foo();
		
	}
	
}
