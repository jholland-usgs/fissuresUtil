
package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.FissuresException;

/**
 * Statistics.java
 *
 *
 * Created: Wed Apr  4 22:27:52 2001
 *
 * @author Philip Crotwell
 * @version $Id: Statistics.java 7451 2004-03-05 21:32:18Z crotwell $
 */

public class Statistics  {

    /**
     * Creates a new <code>Statistics</code> instance.
     *
     * @param iSeries an <code>int[]</code> value
     */
    public Statistics(int[] iSeries) {
    this.iSeries = iSeries;
    beginIndex = 0;
    endIndex = iSeries.length;
    }

    /**
     * Creates a new <code>Statistics</code> instance.
     *
     * @param sSeries a <code>short[]</code> value
     */
    public Statistics(short[] sSeries) {
    this.sSeries = sSeries;
    beginIndex = 0;
    endIndex = sSeries.length;
    }

    /**
     * Creates a new <code>Statistics</code> instance.
     *
     * @param fSeries a <code>float[]</code> value
     */
    public Statistics(float[] fSeries) {
    this.fSeries = fSeries;
    beginIndex = 0;
    endIndex = fSeries.length;
    }

    /**
     * Creates a new <code>Statistics</code> instance.
     *
     * @param dSeries a <code>double[]</code> value
     */
    public Statistics(double[] dSeries) {
    this.dSeries = dSeries;
    beginIndex = 0;
    endIndex = dSeries.length;
    }

    /**
     * Creates a new <code>Statistics</code> instance.
     *
     * @param seismo a <code>LocalSeismogramImpl</code> value
     */
    public Statistics(LocalSeismogramImpl seismo) throws FissuresException {
    if(seismo.can_convert_to_short()){
        sSeries = seismo.get_as_shorts();
        endIndex = sSeries.length;
    }else if(seismo.can_convert_to_long()){
        iSeries = seismo.get_as_longs();
        endIndex = iSeries.length;
    }else if(seismo.can_convert_to_float()){
        fSeries = seismo.get_as_floats();
        endIndex = fSeries.length;
    }else{
        dSeries = seismo.get_as_doubles();
        endIndex = dSeries.length;
    }
    beginIndex = 0;
    }

    /**
     * Finds the <code>min</code> value.
     *
     * @return the minimum
     */
    public double min() {
    return minMaxMean()[0];
    }

    /**
     * Finds the <code>min</code> value between index beginIndex and endIndex-1.
     *
     * @param beginIndex first index to search
     * @param endIndex end index, last + 1
     * @return a <code>double</code> value
     */
    public double min(int beginIndex, int endIndex){
    return minMaxMean(beginIndex, endIndex)[0];
    }

    /**
     * Finds the <code>max</code> value.
     *
     * @return a <code>double</code> value
     */
    public double max(){
    return minMaxMean()[1];
    }

    /**
     * Finds the <code>max</code> value between index beginIndex and endIndex-1.
     *
     * @param beginIndex an <code>int</code> value
     * @param endIndex an <code>int</code> value
     * @return a <code>double</code> value
     */
    public double max(int beginIndex, int endIndex){
    return minMaxMean(beginIndex, endIndex)[1];
    }

    /**
     * Calulates <code>mean</code> of the data.
     *
     * @return a <code>double</code> value
     */
    public double mean(){
    return minMaxMean()[2];
    }

    /**
     * Finds the <code>mean</code> value between index beginIndex and endIndex-1.
     *
     * @param beginIndex an <code>int</code> value
     * @param endIndex an <code>int</code> value
     * @return a <code>double</code> value
     */
    public double mean(int beginIndex, int endIndex){
    return minMaxMean(beginIndex, endIndex)[2];
    }

    /**
     * Calculates the min, max  and mean. The value at index 0 is the min,
     * at index 1 is the max and at index 2 is the mean
     *
     * @return min, max, mean in a double[3]
     */
    public double[] minMaxMean(){
    return minMaxMean(0, getLength());
    }

    /**
     * Calculates the min, max  and mean from beginIndex to endIndex-1.
     * The value at index 0 is the min,
     * at index 1 is the max and at index 2 is the mean
     *
     *
     * @param beginIndex first index
     * @param endIndex last index +1
     * @return min, max, mean in a double[3]
     */
    public double[] minMaxMean(int beginIndex, int endIndex){
    if (beginIndex < 0 ) {
        throw new IllegalArgumentException("begin Index < 0 "+beginIndex);
    } // end of if (beginIndex < 0 )
    if (endIndex > getLength() ) {
        throw new IllegalArgumentException("end Index > data length "+endIndex);
    } // end of if (beginIndex < 0 )

    if(minMaxMeanCalculated){
        if(beginIndex == this.beginIndex && endIndex == this.endIndex){
        return minMaxMean;
        }

        // begin or end has changed, destory any cached values
        flushCache();

        if(this.beginIndex > beginIndex && this.endIndex < endIndex ||
           this.beginIndex < beginIndex && this.endIndex > endIndex){
        minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
        return minMaxMean;
        }
        int removalStart, removalEnd, newDataStart, newDataEnd;
        if(this.beginIndex < beginIndex || this.endIndex < endIndex){
        removalStart = this.beginIndex;
        removalEnd = beginIndex - 1;
        newDataStart = this.endIndex;
        newDataEnd = endIndex - 1;
        }else{
        removalStart = endIndex;
        removalEnd = this.endIndex - 1;
        newDataStart = beginIndex;
        newDataEnd = this.beginIndex;
        }
        minMaxMean[2] *= this.endIndex - this.beginIndex;
        if(iSeries != null){
        for(int j = removalStart; j <= removalEnd; j++) {
            if(iSeries[j] <= minMaxMean[0]){
            // if min is found in remave section reaclulate
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            if(iSeries[j] >= minMaxMean[1]){
            // if max is found in remove section reaclulate
            minMaxMean= calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            minMaxMean[2] -= iSeries[j];
        }
        for(int j = newDataStart; j <= newDataEnd; j++) {
            if(iSeries[j] < minMaxMean[0]){
            minMaxMean[0] = iSeries[j];
            }
            if(iSeries[j] > minMaxMean[1]){
            minMaxMean[1] = iSeries[j];
            }
            minMaxMean[2] += iSeries[j];
        }
        }else if(sSeries != null){
        for(int j = removalStart; j <= removalEnd; j++) {
            if(sSeries[j] <= minMaxMean[0]){
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            if(sSeries[j] >= minMaxMean[1]){
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            minMaxMean[2] -= sSeries[j];
        }
        for(int j = newDataStart; j <= newDataEnd; j++) {
            if(sSeries[j] < minMaxMean[0]){
            minMaxMean[0] = sSeries[j];
            }
            if(sSeries[j] > minMaxMean[1]){
            minMaxMean[1] = sSeries[j];
            }
            minMaxMean[2] += sSeries[j];
        }
        }else if(fSeries != null){
        for(int j = removalStart; j <= removalEnd; j++) {
            if(fSeries[j] <= minMaxMean[0]){
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            if(fSeries[j] >= minMaxMean[1]){
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            minMaxMean[2] -= fSeries[j];
        }
        for(int j = newDataStart; j <= newDataEnd; j++) {
            if(fSeries[j] < minMaxMean[0]){
            minMaxMean[0] = fSeries[j];
            }
            if(fSeries[j] > minMaxMean[1]){
            minMaxMean[1] = fSeries[j];
            }
            minMaxMean[2] += fSeries[j];
        }
        }else if(dSeries != null){
        for(int j = removalStart; j <= removalEnd; j++) {
            if(dSeries[j] <= minMaxMean[0]){
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            if(dSeries[j] >= minMaxMean[1]){
            minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
            return minMaxMean;
            }
            minMaxMean[2] -= dSeries[j];
        }
        for(int j = newDataStart; j <= newDataEnd; j++) {
            if(dSeries[j] < minMaxMean[0]){
            minMaxMean[0] = dSeries[j];
            }
            if(dSeries[j] > minMaxMean[1]){
            minMaxMean[1] = dSeries[j];
            }
            minMaxMean[2] += dSeries[j];
        }
        }
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        minMaxMeanCalculated = true;
        return minMaxMean;
    }
    minMaxMean = calculateMinMaxMean(beginIndex, endIndex);
    return minMaxMean;
    }

    private double[] calculateMinMaxMean(int beginIndex, int endIndex){
    double[] outMinMaxMean = new double[3];
    outMinMaxMean[0] = Double.POSITIVE_INFINITY;
    outMinMaxMean[1] = Double.NEGATIVE_INFINITY;
    outMinMaxMean[2] = 0;
    if (iSeries != null) {
        for (int i = beginIndex; i < endIndex; i++) {
        outMinMaxMean[0] = Math.min(outMinMaxMean[0], iSeries[i]);
        outMinMaxMean[1] = Math.max(outMinMaxMean[1], iSeries[i]);
        outMinMaxMean[2] += iSeries[i];
        } // end of for (int i=0; i<iSeries.length; i++)
    } else if (sSeries != null) {
        for (int i = beginIndex; i < endIndex; i++) {
        outMinMaxMean[0] = Math.min(outMinMaxMean[0], sSeries[i]);
        outMinMaxMean[1] = Math.max(outMinMaxMean[1], sSeries[i]);
        outMinMaxMean[2] += sSeries[i];
        } // end of for (int i=0; i<sSeries.length; i++)
    } else if (fSeries != null) {
        for (int i = beginIndex; i < endIndex; i++) {
        outMinMaxMean[0] = Math.min(outMinMaxMean[0], fSeries[i]);
        outMinMaxMean[1] = Math.max(outMinMaxMean[1], fSeries[i]);
        outMinMaxMean[2] += fSeries[i];
        } // end of for (int i=0; i<fSeries.length; i++)
    } else if (dSeries != null) {
        for (int i = beginIndex; i < endIndex; i++) {
        outMinMaxMean[0] = Math.min(outMinMaxMean[0], dSeries[i]);
        outMinMaxMean[1] = Math.max(outMinMaxMean[1], dSeries[i]);
        outMinMaxMean[2] += dSeries[i];
        } // end of for (int i=0; i<dSeries.length; i++)
    }
    outMinMaxMean[2] /= (endIndex - beginIndex);
    return outMinMaxMean;
    }

    /**
     * Calualates the variance.
     *
     * @return variance
     */
    public double var() {
    if ( ! varianceCalculated) {
        variance = binarySumDevSqr(0, getLength(), mean()) /
        (getLength()-1);
    }
    return variance;
    }

    /**
     * Calulates the standard deviation. This is a shortcut for Math.sqrt(var());
     *
     * @return the Standard deviation
     */
    public double stddev() {
    return Math.sqrt(var());
    }


    /**
     * Calculates the linear Least Squares slope and intercept for this series.
     * Note that this is calculated relative to the index of the array, with
     * zero index as the first data point.
     *
     * @return the intercept in index 0 and the slope in index 1
     */
    public double[] linearLeastSquares() {
    int n = getLength()-1; // use zero based, so n => n-1
    int sumToN = n*(n+1)/2;
    int sumSqrToN = n*(n+1)*(2*n+1)/6;

    double sumValues = binarySum(0, getLength());
    double indexSumValues = binaryIndexSum(0, getLength());
    double d = n*sumSqrToN - sumToN*sumToN;
    double[] out = new double[2];
    out[0] = (sumSqrToN * sumValues - sumToN * indexSumValues)/d;
    out[1] = (n * indexSumValues - sumValues * sumValues)/d;
    return out;
    }

    /**
     * Calculates the autocovariance function out to the given lag.
     *
     * @param maxlag the maximum lag to calculate the acf
     * @return the acf for the series out to maxlag
     */
    public double[] acf(int maxlag) {
    if (autocorrelation.length < maxlag+1) {
        double[] tmp = new double[maxlag+1];
        System.arraycopy(autocorrelation, 0,
                 tmp, 0,
                 autocorrelation.length);
        double normalizer = binarySumDevSqr(0, getLength(), mean());
        for (int i=autocorrelation.length; i< maxlag+1; i++) {
        tmp[i] = binarySumDevLag(0, getLength(), mean(), i) /
            normalizer;
        }
        autocorrelation = tmp;
    }
    return autocorrelation;
    }

    /**
     * Describe <code>acf95conf</code> method here.
     *
     * @param maxlag an <code>int</code> value
     * @return a <code>double[]</code> value
     */
    public double[] acf95conf(int maxlag) {
    double[] acfVals = acf(maxlag);
    double[] out = new double[acfVals.length];
    double sumsqrs=0;
    for (int i=0; i<acfVals.length; i++) {
        sumsqrs += acfVals[i]*acfVals[i];
        out[i] = 1.96*Math.sqrt(1+2*sumsqrs) /
        Math.sqrt(getLength());
    }
    return out;
    }

    /**
     * Calculates the TRatio for the acf out to the given max lag.
     *
     * @param maxlag the maximu lag
     * @return the T ratio
     */
    public double[] acfTRatio(int maxlag) {
    double[] acfVals = acf(maxlag);
    double[] conf = acf95conf(maxlag);
    double[] out = new double[acfVals.length];
    for (int i=0; i<acfVals.length; i++) {
        out[i] = 1.96* Math.abs(acfVals[i]) /
        conf[i];
    }
    return out;
    }

    /**
     * Computes the partial autocorrelation function, after Wei, William S.
     *  Time Series Analysis, pp 22-23.
     * @param maxlag the maximum lag
     * @return the pacf
     */
    public double[] pacf(int maxlag) {
    if (partialautocorr.length < maxlag) {
        double[] tmp = new double[maxlag];
        System.arraycopy(partialautocorr, 0,
                 tmp, 0,
                 partialautocorr.length);

        double[] myacf = acf(maxlag);
        double[][] pacfMatrix = new double[maxlag+1][maxlag+1];
        pacfMatrix[1][1] = myacf[1];
        for (int k=2; k<=maxlag; k++) {
        double topSum = 0;
        double botSum = 0;
        for (int j=1; j<k; j++) {
            topSum += pacfMatrix[k-1][j] * myacf[k-j];
            botSum += pacfMatrix[k-1][j] * myacf[j];
        }
        pacfMatrix[k][k] = ( myacf[k] - topSum ) /
                           ( 1 - botSum );
        for (int j=1; j< k; j++) {
            pacfMatrix[k][j] = pacfMatrix[k-1][j] -
            pacfMatrix[k][k] * pacfMatrix[k-1][k-j];
        }
        }
        partialautocorr = new double[maxlag+1];
        partialautocorr[0] = 1;
        for (int k=1; k<=maxlag; k++) {
        partialautocorr[k] = pacfMatrix[k][k];
        }
    }
    return partialautocorr;
    }

    /**
     * Calculates the 95 percent confidence for the pacf. This is just
     * 1.96*sqrt(length)
     *
     * @param maxlag the maximum lag
     * @return the 95 percent conf for the pacf
     */
    public double pacf95conf(int maxlag) {
    double out = 1.96 /
        Math.sqrt(getLength());
    return out;
    }

    /**
     * Calculates the TRatio for the pacf.
     *
     * @param maxlag the maximum lag
     * @return T ratio for the pacf
     */
    public double[] pacfTRatio(int maxlag) {
    double[] pacfVals = pacf(maxlag);
    double conf = pacf95conf(maxlag);
    double[] out = new double[pacfVals.length];
    for (int i=0; i<pacfVals.length; i++) {
        out[i] = 1.96* Math.abs(pacfVals[i]) /
        conf;
    }
    return out;
    }

    /**
     * The length of the series.
     */
    public int getLength() {
    if (iSeries != null) {
        return iSeries.length;
    }
    if (sSeries != null) {
        return sSeries.length;
    }
    if (fSeries != null) {
        return fSeries.length;
    }
    if (dSeries != null) {
        return dSeries.length;
    }
    return 0;
    }

    /**
     * Creates a histogram of the values. Each value is added to the bin
     Math.floor((value-start)/width) and the returned int array has
     length number
     * @param start binning start value
     * @param width bin width
     * @param number number of bins
     * @return an histogram
     */
    public int[] histogram(double start, double width, int number) {
    int[] histo = new int[number];
    int bin;
    if (iSeries != null) {
        for (int i=0; i< iSeries.length; i++) {
        bin = (int)Math.floor((iSeries[i]-start)/width);
        if (bin >= 0 && bin < number) {
            histo[bin]++;
        } // end of if (bin >= 0 && bin < number)
        } // end of for (int i=0; i< iSeries.length; i++)
        return histo;
    }
    if (sSeries != null) {
        for (int i=0; i< sSeries.length; i++) {
        bin = (int)Math.floor((sSeries[i]-start)/width);
        if (bin >= 0 && bin < number) {
            histo[bin]++;
        } // end of if (bin >= 0 && bin < number)
        } // end of for (int i=0; i< iSeries.length; i++)
        return histo;
    }
    if (fSeries != null) {
        for (int i=0; i< fSeries.length; i++) {
        bin = (int)Math.floor((fSeries[i]-start)/width);
        if (bin >= 0 && bin < number) {
            histo[bin]++;
        } // end of if (bin >= 0 && bin < number)
        } // end of for (int i=0; i< iSeries.length; i++)
        return histo;
    }
    if (dSeries != null) {
        for (int i=0; i< dSeries.length; i++) {
        bin = (int)Math.floor((dSeries[i]-start)/width);
        if (bin >= 0 && bin < number) {
            histo[bin]++;
        } // end of if (bin >= 0 && bin < number)
        } // end of for (int i=0; i< iSeries.length; i++)
        return histo;
    }
    return new int[0];
    }

    /**
     * Calulates the sum of the series from beginIndex to endIndex-1.
     * This is done recursively in halves to avoid rounding errors.
     *
     * @param start starting index
     * @param finish last index +1
     * @return the sum
     */
    public double binarySum(int start, int finish) {
    if (iSeries != null) {
        return iBinarySum(start, finish);
    } // end of if (iSeries != null)
    if (sSeries != null) {
        return sBinarySum(start, finish);
    } // end of if (sSeries != null)
    if (fSeries != null) {
        return fBinarySum(start, finish);
    } // end of if (fSeries != null)
    if (dSeries != null) {
        return dBinarySum(start, finish);
    } // end of if (dSeries != null)
    return 0;
    }

    private double iBinarySum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += iSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return iBinarySum(start, middle) +
        iBinarySum(middle, finish);
    }
    }

    private double sBinarySum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += sSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return sBinarySum(start, middle) +
        sBinarySum(middle, finish);
    }
    }

    private double fBinarySum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += fSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return fBinarySum(start, middle) +
        fBinarySum(middle, finish);
    }
    }

    private double dBinarySum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += dSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return dBinarySum(start, middle) +
        dBinarySum(middle, finish);
    }
    }

    /**
     * Calulates the sum of the square of the difference from the mean.
     * Sum ((xi-mean)^2)
     *
     * @param start first index
     * @param finish last index+1
     * @param mean the mean
     * @return the result
     */
    public double binarySumDevSqr(int start, int finish, double mean) {
    if (iSeries != null) {
        return iBinarySumDevSqr(start, finish, mean);
    } // end of if (iSeries != null)
    if (sSeries != null) {
        return sBinarySumDevSqr(start, finish, mean);
    } // end of if (sSeries != null)
    if (fSeries != null) {
        return fBinarySumDevSqr(start, finish, mean);
    } // end of if (iSeries != null)
    if (dSeries != null) {
        return dBinarySumDevSqr(start, finish, mean);
    } // end of if (dSeries != null)
    return 0;
    }

    private double iBinarySumDevSqr(int start, int finish, double mean) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += (iSeries[i]-mean)*(iSeries[i]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return iBinarySumDevSqr(start, middle, mean) +
        iBinarySumDevSqr(middle, finish, mean);
    }
    }

    private double sBinarySumDevSqr(int start, int finish, double mean) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += (sSeries[i]-mean)*(sSeries[i]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return sBinarySumDevSqr(start, middle, mean) +
        sBinarySumDevSqr(middle, finish, mean);
    }
    }

    private double fBinarySumDevSqr(int start, int finish, double mean) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += (fSeries[i]-mean)*(fSeries[i]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return fBinarySumDevSqr(start, middle, mean) +
        fBinarySumDevSqr(middle, finish, mean);
    }
    }

    private double dBinarySumDevSqr(int start, int finish, double mean) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += (dSeries[i]-mean)*(dSeries[i]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return dBinarySumDevSqr(start, middle, mean) +
        dBinarySumDevSqr(middle, finish, mean);
    }
    }

    /**
     * Describe <code>binarySumDevLag</code> method here.
     *
     * @param start an <code>int</code> value
     * @param finish an <code>int</code> value
     * @param mean a <code>double</code> value
     * @param lag an <code>int</code> value
     * @return a <code>double</code> value
     */
    public double binarySumDevLag(int start, int finish,
                  double mean, int lag) {
    if (iSeries != null) {
        return iBinarySumDevLag(start, finish, mean, lag);
    } // end of if (iSeries != null)
    if (sSeries != null) {
        return sBinarySumDevLag(start, finish, mean, lag);
    } // end of if (sSeries != null)
    if (fSeries != null) {
        return fBinarySumDevLag(start, finish, mean, lag);
    } // end of if (iSeries != null)
    if (dSeries != null) {
        return dBinarySumDevLag(start, finish, mean, lag);
    } // end of if (dSeries != null)
    return 0;
    }

    private double iBinarySumDevLag(int start, int finish,
                    double mean, int lag) {
    if (finish-start < lag+8) {
        double val = 0;
        for (int i=start; i< finish && i<getLength()-lag; i++) {
        val += (iSeries[i]-mean)*(iSeries[i+lag]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return iBinarySumDevLag(start, middle, mean, lag) +
        iBinarySumDevLag(middle, finish, mean, lag);
    }
    }

    private double sBinarySumDevLag(int start, int finish,
                    double mean, int lag) {
    if (finish-start < lag+8) {
        double val = 0;
        for (int i=start; i< finish && i<getLength()-lag; i++) {
        val += (sSeries[i]-mean)*(sSeries[i+lag]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return sBinarySumDevLag(start, middle, mean, lag) +
        sBinarySumDevLag(middle, finish, mean, lag);
    }
    }

    private double fBinarySumDevLag(int start, int finish,
                    double mean, int lag) {
    if (finish-start < lag+8) {
        double val = 0;
        for (int i=start; i< finish && i<getLength()-lag; i++) {
        val += (fSeries[i]-mean)*(fSeries[i+lag]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return fBinarySumDevLag(start, middle, mean, lag) +
        fBinarySumDevLag(middle, finish, mean, lag);
    }
    }

    private double dBinarySumDevLag(int start, int finish,
                    double mean, int lag) {
    if (finish-start < lag+8) {
        double val = 0;
        for (int i=start; i< finish && i<getLength()-lag; i++) {
        val += (dSeries[i]-mean)*(dSeries[i+lag]-mean);
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return dBinarySumDevLag(start, middle, mean, lag) +
        dBinarySumDevLag(middle, finish, mean, lag);
    }
    }

    /**
     * Calulates the sum(i*yi) from beginIndex to endIndex-1.
     * This is done recursively in halves to avoid rounding errors.
     * This is mainly used as one of the terms in a linear least squares.
     *
     * @param start starting index
     * @param finish last index +1
     * @return the sum
     */
    public double binaryIndexSum(int start, int finish) {
    if (iSeries != null) {
        return iBinaryIndexSum(start, finish);
    } // end of if (iSeries != null)
    if (sSeries != null) {
        return sBinaryIndexSum(start, finish);
    } // end of if (sSeries != null)
    if (fSeries != null) {
        return fBinaryIndexSum(start, finish);
    } // end of if (fSeries != null)
    if (dSeries != null) {
        return dBinaryIndexSum(start, finish);
    } // end of if (dSeries != null)
    return 0;
    }

    private double iBinaryIndexSum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += i * iSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return iBinarySum(start, middle) +
        iBinarySum(middle, finish);
    }
    }

    private double sBinaryIndexSum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += i * sSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return sBinarySum(start, middle) +
        sBinarySum(middle, finish);
    }
    }

    private double fBinaryIndexSum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += i * fSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return fBinarySum(start, middle) +
        fBinarySum(middle, finish);
    }
    }

    private double dBinaryIndexSum(int start, int finish) {
    if (finish-start < 8) {
        double val = 0;
        for (int i=start; i< finish; i++) {
        val += i * dSeries[i];
        }
        return val;
    } else {
        int middle = (start + finish) / 2;
        return dBinarySum(start, middle) +
        dBinarySum(middle, finish);
    }
    }

    private void flushCache() {
    minMaxMeanCalculated = false;
    varianceCalculated = false;
    }

    /**
     * Describe <code>main</code> method here.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args) {
    int[] testSeries = new int[10];
    testSeries[0] = 13;
    testSeries[1] = 8;
    testSeries[2] = 15;
    testSeries[3] = 4;
    testSeries[4] = 4;
    testSeries[5] = 12;
    testSeries[6] = 11;
    testSeries[7] = 7;
    testSeries[8] = 14;
    testSeries[9] = 12;
    Statistics s = new Statistics(testSeries);
    System.out.println("Mean = "+s.mean());
    System.out.println("Variance = "+s.var());
    double[] testACF = s.acf(5);
    for (int i=0; i<testACF.length; i++) {
        System.out.println("acf "+i+" = "+testACF[i]);
    }
    double[] testPACF = s.pacf(5);
    for (int i=0; i<testPACF.length; i++) {
        System.out.println("pacf "+i+" = "+testPACF[i]);
    }
    }

    /**
     * Describe variable <code>iSeries</code> here.
     *
     */
    protected int[] iSeries;

    /**
     * Describe variable <code>sSeries</code> here.
     *
     */
    protected short[] sSeries;

    /**
     * Describe variable <code>fSeries</code> here.
     *
     */
    protected float[] fSeries;

    /**
     * Describe variable <code>dSeries</code> here.
     *
     */
    protected double[] dSeries;

    /**
     * Describe variable <code>minMaxMeanCalculated</code> here.
     *
     */
    protected boolean minMaxMeanCalculated = false;

    /**
     * Describe variable <code>variance</code> here.
     *
     */
    protected double variance;
    /**
     * Describe variable <code>varianceCalculated</code> here.
     *
     */
    protected boolean varianceCalculated = false;

    /**
     * Describe variable <code>autocorrelation</code> here.
     *
     */
    protected double[] autocorrelation = new double[0];
    /**
     * Describe variable <code>partialautocorr</code> here.
     *
     */
    protected double[] partialautocorr = new double[0];
    /**
     * Describe variable <code>minMaxMean</code> here.
     *
     */
    protected double[] minMaxMean = new double[3];

    /**
     * Describe variable <code>beginIndex</code> here.
     *
     */
    protected int beginIndex;

    /**
     * Describe variable <code>endIndex</code> here.
     *
     */
    protected int endIndex;

} // Statistics
