package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfTimeSeries.*;
import org.apache.log4j.*;

/**
 * Cuts seismograms based on a begin and end time.
 *
 *
 * Created: Tue Oct  1 21:23:44 2002
 *
 * @author Philip Crotwell
 * @version $Id: Cut.java 3992 2003-05-22 00:36:19Z crotwell $
 */

public class Cut implements LocalSeismogramFunction {
    
    public Cut(MicroSecondDate begin,
               MicroSecondDate end) {
        this.begin = begin;
        this.end = end;
        secPerSec = UnitImpl.divide(UnitImpl.SECOND, UnitImpl.SECOND);
    }
    
    /** Applys the cut to the seismogram. Returns null if no data is within
     *  the cut window.
     */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) {
        if ( begin.after(seis.getEndTime()) || end.before(seis.getBeginTime())) {
            return null;
        } // end of if ()
        
        TimeInterval sampPeriod = seis.getSampling().getPeriod();
        QuantityImpl beginShift = begin.subtract(seis.getBeginTime());
        beginShift = beginShift.divideBy(sampPeriod);
        beginShift = beginShift.convertTo(secPerSec); //should be dimensonless
        int beginIndex = (int)Math.ceil(beginShift.value);
        if (beginIndex < 0) {
            beginIndex = 0;
        } // end of if (beginIndex < 0)
        if (beginIndex >=  seis.getNumPoints()) {
            beginIndex = seis.getNumPoints()-1;
        }
        
        QuantityImpl endShift = seis.getEndTime().subtract(end);
        endShift = endShift.divideBy(sampPeriod);
        endShift = endShift.convertTo(secPerSec); //should be dimensonless
        int endIndex = seis.getNumPoints() - (int)Math.floor(endShift.value);
        if (endIndex < 0) {
            endIndex = 0;
        }
        if (endIndex >  seis.getNumPoints()) {
            endIndex = seis.getNumPoints();
        }
        logger.debug("cut is "+beginIndex+" to "+endIndex+" "+seis.getEndTime()+" "+endShift);
        
        LocalSeismogramImpl outSeis;
        if (seis.can_convert_to_short()) {
            short[] outS = new short[endIndex-beginIndex];
            short[] inS = seis.get_as_shorts();
            System.arraycopy(inS, beginIndex, outS, 0, endIndex-beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outS);
        } else if (seis.can_convert_to_long()) {
            int[] outI = new int[endIndex-beginIndex];
            int[] inI = seis.get_as_longs();
            System.arraycopy(inI, beginIndex, outI, 0, endIndex-beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outI);
            logger.debug("out num_points="+seis.num_points+" out data length="+outI.length);
        } else if (seis.can_convert_to_float()) {
            float[] outF = new float[endIndex-beginIndex];
            float[] inF = seis.get_as_floats();
            System.arraycopy(inF, beginIndex, outF, 0, endIndex-beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outF);
        } else {
            double[] outD = new double[endIndex-beginIndex];
            double[] inD = seis.get_as_doubles();
            System.arraycopy(inD, beginIndex, outD, 0, endIndex-beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outD);
        } // end of else
        
        return outSeis;
    }
    
    protected MicroSecondDate begin;
    protected MicroSecondDate end;
    protected UnitImpl secPerSec;
    
    static Logger logger = Logger.getLogger(Cut.class);
}// Cut
