package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;

/**
 * OffsetMeanAmpConfig sets every seismogram to zero amplitude at its mean, and displays a user defined amount around it.  If no offset is 
 * given, 500 is used. These amplitude ranges can be set to be over the full time of the seismograms, or only a certain interval 
 * specified by a TimeRangeConfig object given to this amp config
 *
 *
 * Created: Tue May 28 14:40:39 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class OffsetMeanAmpConfig extends AbstractAmpRangeConfig{
    public OffsetMeanAmpConfig(){
	ampRange = new UnitRangeImpl(-500, 500, UnitImpl.COUNT);
    }

/** Returns the OffsetMean amplitude for a given seismogram over its full time range
     */
    public UnitRangeImpl getAmpRange(LocalSeismogram aSeis){
	return this.getAmpRange(aSeis,
				new MicroSecondTimeRange(((LocalSeismogramImpl)aSeis).getBeginTime(), ((LocalSeismogramImpl)aSeis).getEndTime()));
    }

    /** Returns the OffsetMean amplitude for a given seismogram over a set time range
     */
    public UnitRangeImpl getAmpRange(LocalSeismogram aSeis, MicroSecondTimeRange calcIntv){
	LocalSeismogramImpl seis = (LocalSeismogramImpl)aSeis;
	int beginIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
					       calcIntv.getBeginTime());
	if (beginIndex < 0) beginIndex = 0;
	if (beginIndex > seis.getNumPoints()) beginIndex = seis.getNumPoints();
	int endIndex = SeisPlotUtil.getPixel(seis.getNumPoints(),
                                               seis.getBeginTime(),
                                               seis.getEndTime(),
					     calcIntv.getEndTime());
        if (endIndex < 0) endIndex = 0;
        if (endIndex > seis.getNumPoints()) endIndex = seis.getNumPoints();

	if (endIndex == beginIndex) {
            // no data points in window, leave config alone
	    return ampRange;
        }
        try {
	    double mean = seis.getMeanValue(beginIndex, endIndex).getValue();
	    double bottom = this.ampRange.getMinValue() + mean;
	    double top = this.ampRange.getMaxValue() + mean;
	    return new UnitRangeImpl(bottom,
				     top,
				     seis.getAmplitudeRange().getUnit());

	    
	} catch (Exception e) {
	    ampRange = null;
        }
	return ampRange;
    }

    /** Sets this amp config to work over the given TimeRangeConfig
     */
    public void visibleAmpCalc(TimeRangeConfig timeConfig){
	UnitRangeImpl tempRange = ampRange;
	ampRange = null;
	intvCalc = true;
	Iterator e = seismos.iterator();
	while(e.hasNext()){
	    LocalSeismogram current = (LocalSeismogram)e.next();
	    this.getAmpRange(current, timeConfig.getTimeRange(current));
	}
	intvCalc = false;
	if(ampRange == null)
	    ampRange = tempRange;
	this.updateAmpSyncListeners();
    }
    
    public void addSeismogram(LocalSeismogram seis){
	this.getAmpRange(seis);
	seismos.add(seis);
	this.updateAmpSyncListeners();
    }

    public int getOffset(){ return offset; }
    
    public void setOffset(int i) { 
	this.offset = i;
	ampRange = new UnitRangeImpl(-offset, offset, UnitImpl.COUNT);
    }

    public void fireAmpRangeEvent(AmpSyncEvent event) {};

    protected int offset;
    
}// OffsetMeanAmpConfig
