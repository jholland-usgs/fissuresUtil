package edu.sc.seis.fissuresUtil.database.plottable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

/**
 * @author groves Created on Oct 18, 2004
 */
public class PlottableChunk {

    /** for hibernate */
    protected PlottableChunk() {}

    /**
     * Creates a plottable chunk consisting of the plottable in data, starting
     * start pixels into the jday and year of otherstuff at
     * otherstuff.getPixelsPerDay ppd.
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          PlottableChunk otherStuff) {
        this(data,
             startPixel,
             otherStuff.getJDay(),
             otherStuff.getYear(),
             otherStuff.getPixelsPerDay(),
             otherStuff.getNetworkCode(),
             otherStuff.getStationCode(),
             otherStuff.getSiteCode(),
             otherStuff.getChannelCode());
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year of start data at pixelsPerDay
     * NOTE: The start pixel should be relative to the beginning of the jday of
     * the start date. Otherwise, things get screwy.
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          MicroSecondDate startDate,
                          int pixelsPerDay,
                          String networkCode,
                          String stationCode,
                          String siteCode,
                          String channelCode) {
        this(data,
             startPixel,
             getJDay(startDate),
             getYear(startDate),
             pixelsPerDay,
             networkCode,
             stationCode,
             siteCode,
             channelCode);
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year at pixelsPerDay
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          int jday,
                          int year,
                          int pixelsPerDay,
                          String networkCode,
                          String stationCode,
                          String siteCode,
                          String channelCode) {
        this.data = data;
        // here we shall get rid of days of dead space if they exist
        if(startPixel >= pixelsPerDay) {
            int numDaysToAdd = startPixel / pixelsPerDay;
            MicroSecondDate date = getDate(jday, year);
            date = date.add(new TimeInterval(new TimeInterval(1.0, UnitImpl.DAY).multiplyBy(numDaysToAdd)));
            jday = getJDay(date);
            year = getYear(date);
            startPixel = startPixel % pixelsPerDay;
        }
        this.beginPixel = startPixel;
        this.pixelsPerDay = pixelsPerDay;
        this.jday = jday;
        this.year = year;
        this.networkCode = networkCode;
        this.stationCode = stationCode;
        this.siteCode = siteCode;
        this.channelCode = channelCode;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof PlottableChunk) {
            PlottableChunk oChunk = (PlottableChunk)o;
            if(networkCode.equals(oChunk.networkCode) &&
                    stationCode.equals(oChunk.stationCode) &&
                    siteCode.equals(oChunk.siteCode) &&
                    channelCode.equals(oChunk.channelCode)) {
                if(pixelsPerDay == oChunk.pixelsPerDay) {
                    if(jday == oChunk.jday) {
                        if(year == oChunk.year) {
                            if(data.x_coor.length == oChunk.data.x_coor.length) {
                                for(int i = 0; i < data.x_coor.length; i++) {
                                    if(data.x_coor[i] != oChunk.data.x_coor[i]) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static Calendar makeCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }

    public static Calendar makeCalWithDate(int jday, int year) {
        Calendar cal = makeCal();
        cal.set(Calendar.DAY_OF_YEAR, jday);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static MicroSecondDate getDate(int jday, int year) {
        return new MicroSecondDate(makeCalWithDate(jday, year).getTimeInMillis() * 1000);
    }

    public static MicroSecondDate getTime(int pixel,
                                          int jday,
                                          int year,
                                          int pixelsPerDay) {
        Calendar cal = makeCalWithDate(jday, year);
        double sampleMillis = SimplePlotUtil.linearInterp(0,
                                                          0,
                                                          pixelsPerDay,
                                                          MILLIS_IN_DAY,
                                                          pixel);
        sampleMillis = Math.floor(sampleMillis);
        return new MicroSecondDate((cal.getTimeInMillis() + (long)sampleMillis) * 1000);
    }

    public static int getJDay(MicroSecondDate time) {
        Calendar cal = makeCal();
        cal.setTime(time);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static int getYear(MicroSecondDate time) {
        Calendar cal = makeCal();
        cal.setTime(time);
        return cal.get(Calendar.YEAR);
    }

    public static int getPixel(MicroSecondDate time, int pixelsPerDay) {
        MicroSecondDate day = new MicroSecondDate(stripToDay(time));
        MicroSecondTimeRange tr = new MicroSecondTimeRange(day, ONE_DAY);
        double pixel = SimplePlotUtil.getPixel(pixelsPerDay, tr, time);
        return (int)Math.floor(pixel);
    }

    public static MicroSecondDate stripToDay(Date d) {
        Calendar cal = makeCal();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new MicroSecondDate(cal.getTime());
    }

    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    public Plottable getData() {
        return data;
    }

    public int getPixelsPerDay() {
        return pixelsPerDay;
    }

    public int getBeginPixel() {
        return beginPixel;
    }

    public int getNumPixels() {
        return data.y_coor.length / 2;
    }

    public MicroSecondDate getTime(int pixel) {
        return getTime(pixel, getJDay(), getYear(), getPixelsPerDay());
    }

    public MicroSecondDate getBeginTime() {
        return getTime(beginPixel);
    }

    public MicroSecondDate getEndTime() {
        return getTime(getBeginPixel() + getNumPixels());
    }

    public MicroSecondTimeRange getTimeRange() {
        return new MicroSecondTimeRange(getBeginTime(), getEndTime());
    }

    public int getJDay() {
        return jday;
    }

    public int getYear() {
        return year;
    }

    public int hashCode() {
        int hashCode = 81 + channelCode.hashCode();
        hashCode = 37 * hashCode + stationCode.hashCode();
        hashCode = 37 * hashCode + siteCode.hashCode();
        hashCode = 37 * hashCode + channelCode.hashCode();
        hashCode = 37 * hashCode + pixelsPerDay;
        hashCode = 37 * hashCode + jday;
        hashCode = 37 * hashCode + year;
        return 37 * hashCode + data.y_coor.length;
    }

    public String toString() {
        return getNumPixels() + " pixel chunk from "
                + networkCode + "."
                + stationCode + "." + siteCode + "." + channelCode + " at "
                + pixelsPerDay + " ppd from " + getTimeRange();
    }

    public List<PlottableChunk> breakIntoDays() {
        int numDays = (int)Math.ceil((beginPixel + getNumPixels())
                / (double)getPixelsPerDay());
        List<PlottableChunk> dayChunks = new ArrayList<PlottableChunk>();
        MicroSecondDate time = getBeginTime();
        for(int i = 0; i < numDays; i++) {
            int firstDayPixels = pixelsPerDay - getBeginPixel();
            int startPixel = (i - 1) * pixelsPerDay + firstDayPixels;
            int stopPixel = i * pixelsPerDay + firstDayPixels;
            int pixelIntoNewDay = 0;
            if(i == 0) {
                startPixel = 0;
                stopPixel = firstDayPixels;
                pixelIntoNewDay = getBeginPixel();
            }
            if(i == numDays - 1) {
                stopPixel = getNumPixels();
            }
            int[] y = new int[(stopPixel - startPixel) * 2];
            System.arraycopy(data.y_coor, startPixel * 2, y, 0, y.length);
            Plottable p = new Plottable(null, y);
            dayChunks.add(new PlottableChunk(p,
                                             pixelIntoNewDay,
                                             getJDay(time),
                                             getYear(time),
                                             getPixelsPerDay(),
                                             getNetworkCode(),
                                             getStationCode(),
                                             getSiteCode(),
                                             getChannelCode()));
            time = time.add(ONE_DAY);
        }
        return dayChunks;
    }

    // hibernate

    protected void setData(Plottable data) {
        this.data = data;
    }

    protected void setPixelsPerDay(int pixelsPerDay) {
        this.pixelsPerDay = pixelsPerDay;
    }

    protected void setBeginPixel(int beginPixel) {
        this.beginPixel = beginPixel;
    }

    protected void setJday(int jday) {
        this.jday = jday;
    }

    protected void setYear(int year) {
        this.year = year;
    }

    public long getDbid() {
        return dbid;
    }

    protected void setDbid(long dbid) {
        this.dbid = dbid;
    }
    
    protected Timestamp getBeginTimestamp() {
        return getBeginTime().getTimestamp();
    }
    
    protected void setBeginTimestamp(Timestamp begin) {
        MicroSecondDate msd = new MicroSecondDate(begin);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(msd);
        year = cal.get(Calendar.YEAR);
        jday = cal.get(Calendar.DAY_OF_YEAR);
    }
    
    protected Timestamp getEndTimestamp() {
        return getEndTime().getTimestamp();
    }
    
    protected void setEndTimestamp(Timestamp begin) {
        // this is generated from begin and pixels, so no need to set
        // have this method as a no-op so hibernate doesn't get mad
    }

    private long dbid;

    private String networkCode;
    private String stationCode;
    private String siteCode;
    private String channelCode;

    private Plottable data;

    private int pixelsPerDay, beginPixel;

    private int jday, year;

    private static final Logger logger = Logger.getLogger(PlottableChunk.class);

    
    public String getNetworkCode() {
        return networkCode;
    }

    
    public void setNetworkCode(String networkCode) {
        this.networkCode = networkCode;
    }

    
    public String getStationCode() {
        return stationCode;
    }

    
    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    
    public String getSiteCode() {
        return siteCode;
    }

    
    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    
    public String getChannelCode() {
        return channelCode;
    }

    
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }
}