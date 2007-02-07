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

import org.seasar.framework.container.S2Container;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 * {@link ComponentResolver}のテストケースクラスです。
 * @author Yoichiro Tanaka
 * @since 1.0.0
 */
public class ComponentResolverTest extends TestCase {

	/**
	 * コンポーネント名でSeasarコンポーネントをルックアップする処理をテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testLookupComponentName() throws Exception {
		String componentName = "testComponent";
		TestComponent testComponent = new TestComponent();
		S2Container container = createMock(S2Container.class);
		expect(container.getComponent(componentName)).andReturn(testComponent);
		replay(container);
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		replay(containerLocator);
		ComponentResolver target = new ComponentResolver(componentName, TestComponent.class, containerLocator);
		Object result = target.getTargetObject();
		assertSame(testComponent, result);
		verify(container);
		verify(containerLocator);
	}
	
	/**
	 * コンポーネントの型でSeasarコンポーネントをルックアップする処理をテストします。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testLookupComponentType() throws Exception {
		String componentName = null;
		TestComponent testComponent = new TestComponent();
		S2Container container = createMock(S2Container.class);
		expect(container.getComponent(TestComponent.class)).andReturn(testComponent);
		expect(container.getComponent(TestComponent.class)).andReturn(testComponent);
		replay(container);
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		expect(containerLocator.get()).andReturn(container);
		expect(containerLocator.get()).andReturn(container);
		replay(containerLocator);
		ComponentResolver target = new ComponentResolver(componentName, TestComponent.class, containerLocator);
		Object result = target.getTargetObject();
		assertSame(testComponent, result);
		result = target.getTargetObject();
		assertSame(testComponent, result);
		verify(container);
		verify(containerLocator);
	}
	
	/**
	 * コンポーネントの型にnullを渡したときのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testComponentTypeNull() throws Exception {
		IS2ContainerLocator containerLocator = createMock(IS2ContainerLocator.class);
		try {
			new ComponentResolver("", null, containerLocator);
		} catch(IllegalArgumentException expected) {
			// N/A
		}
	}
	
	/**
	 * コンポーネントリゾルバにnullを渡したときのテストを行います。
	 * @throws Exception 何らかの例外が発生したとき
	 */
	public void testContainerLocatorNull() throws Exception {
		try {
			new ComponentResolver("", TestComponent.class, null);
		} catch(IllegalArgumentException expected) {
			// N/A
		}
	}
	
	/**
	 * テスト用のコンポーネントクラスです。
	 */
	private static class TestComponent {
	}

}
