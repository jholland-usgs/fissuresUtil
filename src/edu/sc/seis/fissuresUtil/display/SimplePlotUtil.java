package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfTimeSeries.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfParameterMgr.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.awt.Dimension;
import java.util.Date;
import org.apache.log4j.*;

/**
 * SimplePlotUtil.java
 *
 *
 * Created: Thu Jul  8 11:22:02 1999
 *
 * @author Philip Crotwell, Charlie Groves
 * @version $Id: SimplePlotUtil.java 3051 2003-01-07 18:00:27Z crotwell $
 */

public class SimplePlotUtil  {
    
    /**
     * Compresses the seismogram to fit the given dimension for the
     * given timeRange. The returned arrays represent pixel coordinates
     * and should have at most 2 values (min and max) per x pixel coordinate.
     */
    public static int[][] compressXvalues(LocalSeismogram seismogram, 
					     MicroSecondTimeRange timeRange,
					     Dimension size) 
	throws CodecException {

        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;

	int[][] out = new int[2][];
	int seisIndex = 0;
	int pixelIndex = 0;
	int numAdded = 0;
	  
      
	if ( seis.getEndTime().before(timeRange.getBeginTime()) ||
	     seis.getBeginTime().after(timeRange.getEndTime()) ) {
	    
	    out[0] = new int[0];
	    out[1] = new int[0];
            logger.info("The end time is before the beginTime in simple seismogram");
	    return out;
	}
	   
	    

        MicroSecondDate tMin = timeRange.getBeginTime();
        MicroSecondDate tMax = timeRange.getEndTime();

	int seisStartIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      tMin);
	int seisEndIndex = getPixel(seis.getNumPoints(),
				    seis.getBeginTime(),
				    seis.getEndTime(),
				    tMax);
	if (seisStartIndex < 0) {
	    seisStartIndex = 0;
	}
	if (seisEndIndex >= seis.getNumPoints()) {
	    seisEndIndex = seis.getNumPoints()-1;
	}

	MicroSecondDate tempdate = getValue(seis.getNumPoints(),
					    seis.getBeginTime(),
					    seis.getEndTime(),
					    seisStartIndex);
	int pixelStartIndex = getPixel(size.width, 
				       tMin,
				       tMax,
				       tempdate);
        
	  

	tempdate = getValue(seis.getNumPoints(),
			    seis.getBeginTime(),
			    seis.getEndTime(),
			    seisEndIndex);
      
        int pixelEndIndex = getPixel(size.width,
                                     tMin,
                                     tMax,
                                     tempdate);
                                 
      
	int pixels = seisEndIndex - seisStartIndex + 1;
	out[0] = new int[2*pixels];
	out[1] = new int[out[0].length];
	int tempYvalues[] = new int [out[0].length];
	seisIndex = seisStartIndex;
	numAdded = 0;
	int xvalue = 0;
	int tempValue = 0;
	xvalue =  Math.round((float)(linearInterp(seisStartIndex,
						  pixelStartIndex,
						  seisEndIndex,
						  pixelEndIndex,
						  seisIndex)));
	seisIndex++;
	int j;
	j = 0;
	while (seisIndex <= seisEndIndex) {
	    tempValue = 
		Math.round((float)(linearInterp(seisStartIndex,
						pixelStartIndex,
						seisEndIndex,
						pixelEndIndex,
						seisIndex)));
	    tempYvalues[j++] = (int)seis.getValueAt(seisIndex).getValue();
	    if(tempValue != xvalue) {
		out[0][numAdded] = xvalue;
		out[0][numAdded+1] = xvalue;
		out[1][numAdded] = getMinValue(tempYvalues, 0, j-1);
		out[1][numAdded+1] = (int)getMaxValue(tempYvalues, 0, j-1);
		j = 0;
		xvalue = tempValue;
		numAdded = numAdded+2;

	    }
	    seisIndex++;
	}
	xvalue = tempValue;
	/*if(j == 0){
	    out[1][numAdded] = getMinValue(tempYvalues, 0, 0);
	    out[1][numAdded+1] = (int)getMaxValue(tempYvalues, 0, 0);
	}
	else{
	    out[1][numAdded] = getMinValue(tempYvalues, 0, j-1);
	    out[1][numAdded + 1] = (int)getMaxValue(tempYvalues, 0, j-1);
	    }*/
	int temp[][] = new int[2][numAdded];
	System.arraycopy(out[0], 0, temp[0], 0, numAdded);
	System.arraycopy(out[1], 0, temp[1], 0, numAdded);
	return temp;
    }

    protected static int[][] compressYvalues(LocalSeismogram seismogram, 
					     MicroSecondTimeRange tr,
					     UnitRangeImpl ampRange,
					     Dimension size)throws CodecException {
	LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;
	/*double pointsPerPixel = tr.getInterval().divideBy(seis.getSampling().getPeriod()).getValue() / 
	    size.width;
	if(pointsPerPixel  < 3 ){
	return getPlottableSimpl(seis, ampRange, tr, size);
	}else{*/
	    int[][] uncomp = compressXvalues(seismogram, tr, size);
	    // enough points to take the extra time to compress the line
	    int[][] comp = new int[2][2 * size.width];
	    int j = 0, startIndex = 0, xvalue = 0, endIndex = 0;
	    if(uncomp[0].length != 0) xvalue = uncomp[0][0];
	    for(int i = 0; i < uncomp[0].length; i++) {
		if(uncomp[0][i] != xvalue) {
		    endIndex = i - 1;
		    comp[1][j] = getMinValue(uncomp[1], startIndex, endIndex);
		    comp[1][j+1] = (int)getMaxValue(uncomp[1], startIndex, endIndex);
		    comp[0][j] = uncomp[0][i];
		    comp[0][j+1] = uncomp[0][i];
		    j = j + 2;
		    startIndex = endIndex + 1;
		    xvalue = uncomp[0][i];
		}  
	    }

	    if(xvalue != 0) {
		startIndex = uncomp[0].length - 1;
		endIndex = uncomp[0].length - 1;
		comp[1][j] = getMinValue(uncomp[1], startIndex, endIndex);
		comp[1][j+1] = (int)getMaxValue(uncomp[1], startIndex, endIndex);
		comp[0][j] = uncomp[0][endIndex];
		comp[0][j+1] = uncomp[0][endIndex];
		j = j + 2;
	    }
	    int temp[][] = new int[2][j];
	    System.arraycopy(comp[0], 0, temp[0], 0, j);
	    System.arraycopy(comp[1], 0, temp[1], 0, j);
	
	    return temp;
	
    }
   
    protected static void  scaleYvalues(int[][] comp, LocalSeismogram seismogram, MicroSecondTimeRange tr, 
					UnitRangeImpl ampRange,  Dimension size) {
	LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;
	double yMin = ampRange.getMinValue();
        double yMax = ampRange.getMaxValue();
	for( int i =0 ; i < comp[1].length; i++) {
	    comp[1][i] = Math.round((float)(linearInterp(yMin, 0,
							 yMax, size.height, 
							 comp[1][i])));
	    
	}
    }
	
        public static int[][] getPlottableSimple(LocalSeismogram seismogram, 
					     UnitRangeImpl a, 
					     MicroSecondTimeRange t,
					     Dimension size) 
    throws CodecException {

        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;

        int[][] out = new int[2][];

	if ( seis.getEndTime().before(t.getBeginTime()) ||
	     seis.getBeginTime().after(t.getEndTime())){
	    out[0] = new int[0];
	    out[1] = new int[0];
            //Logger.log(5,"SeisPlotUtil no data in time window");

	    return out;
	}

        MicroSecondDate tMin = t.getBeginTime();
        MicroSecondDate tMax = t.getEndTime();
	double yMin = a.getMinValue();
        double yMax = a.getMaxValue();

	int seisStartIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      tMin);
	int seisEndIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      tMax);
	// get one more
	seisStartIndex--;
	seisEndIndex++;
	if (seisStartIndex < 0) {
	    seisStartIndex = 0;
	}
	if (seisEndIndex >= seis.getNumPoints()) {
	    seisEndIndex = seis.getNumPoints()-1;
	}
        MicroSecondDate temp = getValue(seis.getNumPoints(),
                                        seis.getBeginTime(),
                                        seis.getEndTime(),
                                        seisStartIndex);
        int pixelStartIndex = getPixel(size.width, 
				       t.getBeginTime(),
				       t.getEndTime(),
                                     temp);
        
        temp = getValue(seis.getNumPoints(),
                        seis.getBeginTime(),
                        seis.getEndTime(),
                        seisEndIndex);
        int pixelEndIndex = getPixel(size.width,
                                     t.getBeginTime(),
                                     t.getEndTime(),
                                     temp);
                                     

        return getPlottableSimple(seis, 
                                  seisStartIndex, seisEndIndex,
                                  pixelStartIndex, pixelEndIndex,
                                  yMin, yMax,
                                  size);
    }

    public static int[][] getPlottableSimple(LocalSeismogramImpl seis, 
                                             int seisStartIndex,
                                             int seisEndIndex,
                                             int pixelStartIndex,
                                             int pixelEndIndex,
                                             double yMin,
                                             double yMax,
                                             Dimension size) 
    throws CodecException {
        int[][] out = new int[2][];
	int previousy = 0;
	int seisIndex = 0;
	int pixelIndex = 0;
	int numAdded = 0;

	out[0] = new int[seisEndIndex-seisStartIndex+1];
	out[1] = new int[out[0].length];
	previousy = (int)seis.getValueAt(seisIndex).value;
	seisIndex = seisStartIndex;

	numAdded = 0;
	while (seisIndex < seisEndIndex) {
	 

	   out[0][numAdded] =  
	       Math.round((float)(linearInterp(seisStartIndex,
					       pixelStartIndex,
					       seisEndIndex,
					       pixelEndIndex,
					       seisIndex)));

	   out[1][numAdded] =  
	       Math.round((float)(linearInterp( yMin, 0,
						yMax, size.height,
						seis.getValueAt(seisIndex).getValue())));	
	
	   numAdded++;  
	   seisIndex = seisIndex + 1;
	}

	int temp[][] = new int[2][];
	temp[0] = new int[numAdded];
	temp[1] = new int[numAdded];
	System.arraycopy(out[0], 0, temp[0], 0, numAdded);
	System.arraycopy(out[1], 0, temp[1], 0, numAdded);
	return temp;

    }

    private static int getMinValue(int[] yValues, int startIndex, int endIndex) {

	int minValue = java.lang.Integer.MAX_VALUE;
	for( int i = startIndex; i <= endIndex; i++) {
	    if(yValues[i] < minValue) minValue = yValues[i];

	}
	return minValue;

    }

    private static int getMaxValue(int[] yValues, int startIndex, int endIndex) {

	int maxValue = java.lang.Integer.MIN_VALUE;
	for( int i = startIndex; i <= endIndex; i++) {
	    if(yValues[i] > maxValue) maxValue = yValues[i];
	}
	return maxValue;

    }

    /** flips an array of ints to be inArray[i] = maxValue -
        inArray[i]. This is mainly useful when displaying as
        calculations are generally done in the traditional y positive
        up coordinate system, and then "flipped" at the last minute to
        plot int the graphical y positive down screen coordinate
        system.  
    */
    public static final void flipArray(int[] inArray, int maxValue) {
	for (int i = 0; i < inArray.length; i++) {
	    // this does the equivalent of inArray[i] = maxValue - intArray[i];
	    // but is presumably faster as no temp ints need be created
	    inArray[i] *= -1;
	    inArray[i] += maxValue;
	}
    }

    /** solves the equation <pre>(yb-ya)/(xb-xa) = (y-ya)/(x-xa)</pre>
     *  for y given x. Useful for finding the pixel for a value given the
     *  dimension of the area and the range of values it is supposed to
     *  cover. Note, this does not check for xa == xb, in which case a
     *  divide by zero would occur.
     */
    public static final double linearInterp(double xa, double ya,
                                      double xb, double yb,
                                      double x) {
        if (x == xa) return ya;
        if (x == xb) return yb;
        return (yb - ya)*(x-xa)/(xb-xa) + ya;
    }

    public static final int getPixel(int totalPixels, 
                               MicroSecondDate begin,
                               MicroSecondDate end,
                               MicroSecondDate value) {
        return (int)Math.round(linearInterp(begin.getMicroSecondTime(),
					    0,
                                            end.getMicroSecondTime(),
                                            totalPixels, 
                                            value.getMicroSecondTime()));
    }

    public static final MicroSecondDate getValue(int totalPixels, 
                                           MicroSecondDate begin,
                                           MicroSecondDate end,
                                           int pixel) {
        double value = 
            linearInterp(0,
                         0,
                         totalPixels,
                         end.getMicroSecondTime()-begin.getMicroSecondTime(),
                         pixel);
        return new MicroSecondDate(begin.getMicroSecondTime() +
                                   Math.round(value));
    }

    public static final int getPixel(int totalPixels, 
                               UnitRangeImpl range,
                               QuantityImpl value) {
        QuantityImpl converted = value.convertTo(range.getUnit());
        return getPixel(totalPixels, range, converted.getValue());
    }

    public static final int getPixel(int totalPixels, 
                               UnitRangeImpl range,
                               double value) {
        return (int)Math.round(linearInterp(range.getMinValue(), 0,
                                            range.getMaxValue(), totalPixels, 
                                            value));
    }

    public static final QuantityImpl getValue(int totalPixels, 
                                    UnitRangeImpl range,
                                    int pixel) {
        double value = linearInterp(0, 
                                    range.getMinValue(),
                                    totalPixels, 
                                    range.getMaxValue(),
                                    pixel);
        return new QuantityImpl(value, range.getUnit());
    }        

    public static final MicroSecondDate getTimeForIndex(int index,
				      		MicroSecondDate beginTime,
						SamplingImpl sampling) {
	TimeInterval width = sampling.getPeriod();
	width = (TimeInterval)width.multiplyBy(index);
	return beginTime.add(width);
    }
    public static LocalSeismogramImpl createTestData() {
        return createTestData("Fake Data");
    }

    public static LocalSeismogramImpl createTestData(String name) {
        int[] dataBits = new int[100];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
            tmpDouble = Math.random()*2.0 -1.0;
            //    tmpDouble = .4 + Math.random()*.1;
            // this makes the values a little more likely to be close 
            // to the center, making it slightly more seimogram like
            tmpDouble = tmpDouble * tmpDouble * tmpDouble * tmpDouble * tmpDouble;
            dataBits[i] = (int)Math.round(tmpDouble*2000.0);
        }

        return createTestData(name, dataBits);
    }

    public static LocalSeismogramImpl createTestData(String name, int[] dataBits) {
        String id = "Nowhere: "+name;
        edu.iris.Fissures.Time time = 
                new edu.iris.Fissures.Time("19991231T235959.000Z", 
                                                    -1);
        
        TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = 
            new SamplingImpl(20,
                         timeInterval);
        ChannelId channelID = new ChannelId(new NetworkId("XX",
							  time), 
					    "FAKE",
					    "00", 
					    "BHZ",
                                            time);

        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);

        Property[] props = new Property[1];
        props[0] = new Property("Name", name);
	TimeInterval[] time_corr = new TimeInterval[1];
	time_corr[0] = new TimeInterval(.123, UnitImpl.SECOND);
        LocalSeismogramImpl seis = 
	    new LocalSeismogramImpl(id,
				    props,
				    time,
				    dataBits.length,
				    sampling,
				    UnitImpl.COUNT,
				    channelID,
				    new ParameterRef[0],
				    time_corr,
				    new SamplingImpl[0],
				    bits);
        return seis;
    }

   public static LocalSeismogramImpl createTestData(String name, int[] dataBits, edu.iris.Fissures.Time time) {
        String id = "Nowhere: "+name;
	TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = 
            new SamplingImpl(20,
                         timeInterval);
        ChannelId channelID = new ChannelId(new NetworkId("XX",
							  time), 
					    "FAKE",
					    "00", 
					    "BHZ",
                                            time);

        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);

        Property[] props = new Property[1];
        props[0] = new Property("Name", name);
	TimeInterval[] time_corr = new TimeInterval[1];
	time_corr[0] = new TimeInterval(.123, UnitImpl.SECOND);
        LocalSeismogramImpl seis = 
	    new LocalSeismogramImpl(id,
				    props,
				    time,
				    dataBits.length,
				    sampling,
				    UnitImpl.COUNT,
				    channelID,
				    new ParameterRef[0],
				    time_corr,
				    new SamplingImpl[0],
				    bits);
        return seis;
    }

    public static LocalSeismogramImpl createCustomSineWave(){
	int[] dataBits = new int[1200];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(0 + 
                                          i*Math.PI*1/20.0)*1000);
	}
	return createTestData("Sine Wave", dataBits, new edu.iris.Fissures.Time("19911015T163000.000Z", -1));
    }
    
    public static LocalSeismogramImpl createSineWave() {
        return createSineWave(0);
    }

    public static LocalSeismogramImpl createSineWave(double phase) {
        return createSineWave(phase, 1);
    }

     public static LocalSeismogramImpl createSineWave(double phase, double hertz) {
	 return createSineWave(phase, hertz, 1200);
     }

    public static LocalSeismogramImpl createSineWave(double phase, double hertz, int numPoints) {
	return createSineWave(phase, hertz, numPoints, 1000);
    }

    public static LocalSeismogramImpl createSineWave(double phase, double hertz, int numPoints, double amp) {
        int[] dataBits = new int[numPoints];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(phase + 
                                          i*Math.PI*hertz/20.0)*amp);
	}
	

        return createTestData("Sine Wave, phase "+phase+" hertz "+hertz,
                              dataBits);
    }

   public static LocalSeismogramImpl createHighSineWave(double phase, double hertz) {
        int[] dataBits = new int[120];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(phase + 
                                          i*Math.PI*hertz/20.0)*1000.0+500);
	}
	

        return createTestData("Sine Wave, phase "+phase+" hertz "+hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createLowSineWave(double phase, double hertz) {
        int[] dataBits = new int[120];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(phase + 
                                          i*Math.PI*hertz/20.0)*1000.0-500);
	}
	

        return createTestData("Sine Wave, phase "+phase+" hertz "+hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createSpike(MicroSecondDate spikeTime) {
        String name = "spike at "+spikeTime.toString();
        int[] dataBits = new int[1000];
        for (int i=0; i<dataBits.length; i++) {
            // assume 20 sps
            if (i % 20 == 0) {
                dataBits[i] = 100;                 
            } // end of if (i % 20 = 0)
        } // end of for (int i=0; i<dataBits.length; i++)
        
        //        dataBits[5*20] = 100;
        MicroSecondDate begin = spikeTime;
        //            spikeTime.subtract(new TimeInterval(5, UnitImpl.SECOND));
        return createTestData(name, dataBits, begin.getFissuresTime());
    }

    static Category logger = Category.getInstance(SimplePlotUtil.class.getName());

} // SimplePlotUtil
