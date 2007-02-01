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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for jp.eisbahn.wicket.seasar");
		//$JUnit-BEGIN$
		suite.addTestSuite(ProxyFactoryTest.class);
		suite.addTestSuite(S2ContainerHolderTest.class);
		suite.addTestSuite(GadgetTest.class);
		suite.addTestSuite(FieldValueProducerTest.class);
		suite.addTestSuite(S2ContainerLocatorTest.class);
		suite.addTestSuite(InjectionProcessorTest.class);
		suite.addTestSuite(ComponentResolverTest.class);
		suite.addTestSuite(SeasarComponentInjectionListenerTest.class);
		//$JUnit-END$
		return suite;
	}

}
