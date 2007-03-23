package org.seasar.wicket.utils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.seasar.wicket.utils");
		//$JUnit-BEGIN$
		suite.addTestSuite(GadgetTest.class);
		//$JUnit-END$
		return suite;
	}

}
