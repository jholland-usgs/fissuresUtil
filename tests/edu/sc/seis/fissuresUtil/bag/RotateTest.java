package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.mockFissures.Defaults;
import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import edu.sc.seis.fissuresUtil.bag.DistAz;
// JUnitDoclet end import

/**
 * Generated by JUnitDoclet, a tool provided by
 * ObjectFab GmbH under LGPL.
 * Please see www.junitdoclet.org, www.gnu.org
 * and www.objectfab.de for informations about
 * the tool, the licence and the authors.
 */


public class RotateTest
    // JUnitDoclet begin extends_implements
    extends TestCase
    // JUnitDoclet end extends_implements
{
    // JUnitDoclet begin class
    edu.sc.seis.fissuresUtil.bag.Rotate rotate = null;
    float[] origx;
    float[] origy;
    // JUnitDoclet end class

    public RotateTest(String name) {
        // JUnitDoclet begin method RotateTest
        super(name);
        // JUnitDoclet end method RotateTest
    }

    public edu.sc.seis.fissuresUtil.bag.Rotate createInstance() throws Exception {
        // JUnitDoclet begin method testcase.createInstance
        return new edu.sc.seis.fissuresUtil.bag.Rotate();
        // JUnitDoclet end method testcase.createInstance
    }

    protected void setUp() throws Exception {
        // JUnitDoclet begin method testcase.setUp
        super.setUp();
        rotate = createInstance();
        origx = new float[3];
        origy = new float[3];
        origx[0] = 0;
        origy[0] = 0;
        origx[1] = 1;
        origy[1] = 1;
        origx[2] = .5f;
        origy[2] = -.5f;

        // JUnitDoclet end method testcase.setUp
    }

    protected void tearDown() throws Exception {
        // JUnitDoclet begin method testcase.tearDown
        rotate = null;
        origx = null;
        origy = null;
        super.tearDown();
        // JUnitDoclet end method testcase.tearDown
    }

    public void testApply() throws Exception {
        // JUnitDoclet begin method apply
        // JUnitDoclet end method apply
    }

    public void testRotate() throws Exception {
        // JUnitDoclet begin method rotate
        float[] x = new float[origx.length];
        System.arraycopy(origx, 0, x, 0, x.length);
        float[] y = new float[origy.length];
        System.arraycopy(origy, 0, y, 0, y.length);
        rotate.rotate(x, y, Math.PI/2); // 90 degrees
        assertEquals(0, x[0], 0.0001);
        assertEquals(0, y[0], 0.0001);
        assertEquals(1, x[1], 0.0001);
        assertEquals(-1, y[1], 0.0001);
        assertEquals(" x from (.5, -.5)", -.5, x[2], 0.0001);
        assertEquals(" y from (.5, -.5)", -.5, y[2], 0.0001);
        rotate.rotate(x, y, -Math.PI/2); // inverse transform
        ArrayAssert.assertEquals(origx, x, 0.0001f);
        // JUnitDoclet end method rotate
    }

    public void testRotateGCP() throws Exception {
        // JUnitDoclet begin method rotate
        MicroSecondDate now = new MicroSecondDate();
        // both spikes are same, so 45 degree part motion e and n
        LocalSeismogramImpl xSeis = SimplePlotUtil.createSpike(now);
        LocalSeismogramImpl ySeis = SimplePlotUtil.createSpike(now);
        Location staLoc = new Location(55.3f, -3.2f, Defaults.ZERO_K, Defaults.ZERO_K, null);
        Location evtLoc = new Location(36.52f, 71.23f, Defaults.ZERO_K, Defaults.ZERO_K, null);
        float[][] ans = rotate.rotateGCP(xSeis, ySeis, staLoc, evtLoc);
        DistAz distAz = new DistAz(staLoc.latitude, staLoc.longitude,
                                   evtLoc.latitude, evtLoc.longitude);
        System.out.println("x y "+
                          100*Math.sqrt(2)*Math.sin((distAz.baz-45)*Math.PI/180.0)+" "+
                          -100*Math.sqrt(2)*Math.cos((distAz.baz-45)*Math.PI/180.0));
        assertEquals( 100*Math.sqrt(2)*Math.sin(rotate.dtor(distAz.baz-45)), ans[0][0],0.001f);
        assertEquals( -100*Math.sqrt(2)*Math.cos(rotate.dtor(distAz.baz-45)), ans[1][0],0.001f);

        // JUnitDoclet end method rotate
    }

    public void testRotateGCPXAxis() throws Exception {
        // JUnitDoclet begin method rotate
        MicroSecondDate now = new MicroSecondDate();
        // both spikes are same, so 45 degree part motion e and n
        LocalSeismogramImpl xSeis = SimplePlotUtil.createSpike(now);
        LocalSeismogramImpl ySeis = SimplePlotUtil.createSpike(now);
        Location staLoc = new Location(0f, 0f, Defaults.ZERO_K, Defaults.ZERO_K, null);
        Location evtLoc = new Location(0f, 10f, Defaults.ZERO_K, Defaults.ZERO_K, null);
        float[][] ans = rotate.rotateGCP(xSeis, ySeis, staLoc, evtLoc);
        DistAz distAz = new DistAz(staLoc.latitude, staLoc.longitude,
                                   evtLoc.latitude, evtLoc.longitude);
        System.out.println("x y "+
                          100*Math.sqrt(2)*Math.sin((distAz.baz-45)*Math.PI/180.0)+" "+
                          -100*Math.sqrt(2)*Math.cos((distAz.baz-45)*Math.PI/180.0));
        assertEquals( 100, ans[0][0],0.001f);
        assertEquals( -100, ans[1][0],0.001f);

        // JUnitDoclet end method rotate
    }

    public void testDtor() throws Exception {
        // JUnitDoclet begin method dtor
        // JUnitDoclet end method dtor
    }

    public void testRtod() throws Exception {
        // JUnitDoclet begin method rtod
        // JUnitDoclet end method rtod
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
        junit.textui.TestRunner.run(RotateTest.class);
        // JUnitDoclet end method testcase.main
    }
}
