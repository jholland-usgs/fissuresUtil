package edu.sc.seis.fissuresUtil.database.plottable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Oct 18, 2004
 */
public class PlottableChunk {

    public PlottableChunk(Plottable data, MicroSecondDate startTime,
            PlottableChunk otherStuff) {
        this(data,
             startTime,
             otherStuff.getSamplesPerSecond(),
             otherStuff.getChannel());
    }

    public PlottableChunk(Plottable data, MicroSecondDate startTime,
            double samplesPerSecond, ChannelId channel) {
        this.data = data;
        this.startTime = startTime;
        this.samplesPerSecond = samplesPerSecond;
        this.channel = channel;
        range = new MicroSecondTimeRange(startTime,
                                         new TimeInterval(data.y_coor.length
                                                                  / samplesPerSecond,
                                                          UnitImpl.SECOND));
        endTime = range.getEndTime();
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof PlottableChunk) {
            PlottableChunk oChunk = (PlottableChunk)o;
            if(ChannelIdUtil.areEqual(channel, oChunk.channel)) {
                if(samplesPerSecond == oChunk.samplesPerSecond) {
                    if(startTime.equals(oChunk.startTime)) {
                        if(data.x_coor.length == oChunk.data.x_coor.length) {
                            for(int i = 0; i < data.x_coor.length; i++) {
                                if(data.x_coor[i] != oChunk.data.x_coor[i]) { return false; }
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public ChannelId getChannel() {
        return channel;
    }

    public Plottable getData() {
        return data;
    }

    public double getSamplesPerSecond() {
        return samplesPerSecond;
    }

    public MicroSecondDate getBeginTime() {
        return startTime;
    }

    public MicroSecondDate getEndTime() {
        return endTime;
    }

    public MicroSecondTimeRange getTimeRange() {
        return range;
    }

    public int hashCode() {
        int hashCode = 81 + ChannelIdUtil.hashCode(getChannel());
        long spsBits = Double.doubleToLongBits(samplesPerSecond);
        int longHash = (int)(spsBits ^ (spsBits >>> 32));
        hashCode = 37 * hashCode + longHash;
        hashCode = 37 * hashCode + startTime.hashCode();
        return 37 * hashCode + data.y_coor.length;
    }

    public String toString() {
        return data.y_coor.length + " point chunk from "
                + ChannelIdUtil.toStringNoDates(channel) + " at "
                + nf.format(samplesPerSecond) + " sps at " + getBeginTime();
    }

    private ChannelId channel;

    private Plottable data;

    private NumberFormat nf = new DecimalFormat("0.00");

    private double samplesPerSecond;

    private MicroSecondDate startTime, endTime;

    private MicroSecondTimeRange range;
}