package com.aptana.ide.editors.junit.formatting;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for com.aptana.ide.editors.junit.formatting");
		//$JUnit-BEGIN$
		suite.addTestSuite(UnifiedBracketInserterTest.class);
		suite.addTestSuite(SelectionFormattingTests.class);
		//$JUnit-END$
		return suite;
	}

}
