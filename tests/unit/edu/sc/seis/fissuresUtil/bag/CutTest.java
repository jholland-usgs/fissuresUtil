package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class CutTest
// JUnitDoclet begin extends_implements
    extends TestCase
	    // JUnitDoclet end extends_implements
{
    // JUnitDoclet begin class
    edu.sc.seis.fissuresUtil.bag.Cut cut = null;
    LocalSeismogramImpl seis;
    edu.iris.Fissures.Time time = 
	new edu.iris.Fissures.Time("20001231T235959.000Z", 
				   -1);
    int[] data;

    static {
	BasicConfigurator.configure();
    }
    // JUnitDoclet end class
  
    public CutTest(String name) {
	// JUnitDoclet begin method CutTest
	super(name);
	// JUnitDoclet end method CutTest
    }
  
    public edu.sc.seis.fissuresUtil.bag.Cut createInstance() throws Exception {
	// JUnitDoclet begin method testcase.createInstance
	MicroSecondDate begin = new MicroSecondDate(time);
	MicroSecondDate end = new MicroSecondDate(seis.getEndTime());

	SamplingImpl samp = (SamplingImpl)seis.sampling_info;
	begin = begin.add(samp.getPeriod());
	return new edu.sc.seis.fissuresUtil.bag.Cut(begin, end);
	// JUnitDoclet end method testcase.createInstance
    }
  
    protected void setUp() throws Exception {
	// JUnitDoclet begin method testcase.setUp
	super.setUp();
	data = new int[101];
	for ( int i=0; i<data.length; i++) {
	    data[i] = 0;
	    // create test data creates 20sps data
	    if ( i % 20 == 0) {
		data[i] = 1;
	    } // end of if ()
	
	} // end of for ()
    
	seis = SimplePlotUtil.createTestData("est", data, time);
	cut = createInstance();
	// JUnitDoclet end method testcase.setUp
    }
  
    protected void tearDown() throws Exception {
	// JUnitDoclet begin method testcase.tearDown
	cut = null;
	seis = null;
	data = null;
	super.tearDown();
	// JUnitDoclet end method testcase.tearDown
    }
  
    public void testApply() throws Exception {
	// JUnitDoclet begin method apply
	LocalSeismogramImpl out = cut.apply(seis);
	assertTrue( "Num points is one less "+out.num_points+" "+seis.num_points, out.num_points == seis.num_points-1);
	// JUnitDoclet end method apply
    }
  
  
  
    /**
     * JUnitDoclet moves marker to this method, if there is not match
     * for them in the regenerated code and if the marker is not empty.
     * This way, no test gets lost when regenerating after renaming.
     * Method testVault is supposed to be empty.
     */
    public void testVault() throws Exception {
	// JUnitDoclet begin method testcase.testVault
	// JUnitDoclet end method testcase.testVault
    }
  
    public static void main(String[] args) {
	// JUnitDoclet begin method testcase.main
	junit.textui.TestRunner.run(CutTest.class);
	// JUnitDoclet end method testcase.main
    }
}
