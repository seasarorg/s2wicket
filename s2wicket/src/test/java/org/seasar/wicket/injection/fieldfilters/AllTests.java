package org.seasar.wicket.injection.fieldfilters;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.seasar.wicket.injection.fieldfilters");
		//$JUnit-BEGIN$
		suite.addTestSuite(AnnotationFieldFilterTest.class);
		//$JUnit-END$
		return suite;
	}

}
