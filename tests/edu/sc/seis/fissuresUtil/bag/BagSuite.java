package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestSuite;


// JUnitDoclet begin import
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class BagSuite
// JUnitDoclet begin extends_implements
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  // JUnitDoclet end class

  public static TestSuite suite() {

    TestSuite suite;

    suite = new TestSuite("edu.sc.seis.fissuresUtil.bag");

    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.RTrendTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.TaperTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.StatisticsTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.RMeanTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.MotionVectorUtilTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.DistAzTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.CutTest.class);
    suite.addTestSuite(edu.sc.seis.fissuresUtil.bag.CalculusTest.class);



    // JUnitDoclet begin method suite
    // JUnitDoclet end method suite

    return suite;
  }

  public static void main(String[] args) {
    // JUnitDoclet begin method testsuite.main
    junit.textui.TestRunner.run(suite());
    // JUnitDoclet end method testsuite.main
  }
}
