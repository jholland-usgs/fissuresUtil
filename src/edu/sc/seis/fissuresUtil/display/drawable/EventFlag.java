package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * EventFlagPlotter.java
 *
 *
 * Created: Fri Mar 28 14:20:17 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EventFlag{
    public EventFlag (final PlottableDisplay plottableDisplay,
                      EventAccessOperations eventAccess, Arrival[] arrivals){
        this.display = plottableDisplay;
        this.eventAccess = eventAccess;
        this.arrivals = arrivals;
    }
    
    public int getOriginX(){
        return getX(getOriginXPercent());
    }
    
    public int getOriginY(){
        return getY(getOriginRow());
    }
    
    private int getY(int row){
        return row * display.getRowOffset() + display.titleHeight;
    }
    
    public int getX(Arrival arrival){
        return getX(getXPercent(arrival));
    }
    
    private int getX(MicroSecondDate time){
        return getX(getXPercent(time));
    }
    
    private int getX(double xPercentage){
        return (int)(xPercentage * display.getRowWidth()) + PlottableDisplay.LABEL_X_SHIFT;
    }
    
    public int getY(Arrival arrival){
        return getY(getRow(arrival));
    }
    
    public void draw(Graphics g) {
        // get new graphics to avoid messing up original
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setStroke(new BasicStroke(3));
        g2.setPaint(color);
        int halfOffset =  display.getRowOffset()/2;
        int x = getOriginX();
        int y = getOriginY();
        drawFlagAndPole("Origin", x, y, halfOffset, g2, true);
        for (int i = 0; i < arrivals.length; i++) {
            x = getX(arrivals[i]);
            y = getY(arrivals[i]);
            drawFlagAndPole(arrivals[i].getName(), x, y, halfOffset, g2);
        }
    }
    
    private void drawFlagAndPole(String title, int x, int y, int halfOffset,
                                 Graphics2D g2){
        drawFlagAndPole(title, x, y, halfOffset, g2, false);
    }
    
    //if the wind direction is reversed, the flag appears to the right of the
    //pole
    private void drawFlagAndPole(String title, int x, int y, int halfOffset,
                                 Graphics2D g2, boolean reverseWindDirection){
        //draw stick the same no matter what
        g2.setColor(color);
        g2.drawLine(x, y - halfOffset, x, y + halfOffset);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds(title, g2);
        int textY = y - halfOffset;
        if(reverseWindDirection)
            x -= stringBounds.getWidth();
        stringBounds.setRect(x, textY, stringBounds.getWidth() + 3,
                             stringBounds.getHeight());
        g2.fill(stringBounds);
        g2.setColor(Color.WHITE);
        g2.drawString(title, x, (int)(textY + stringBounds.getHeight() - 3));
    }
    
    public boolean isSelected(int[][] rowAndX) {
        int row = getOriginRow();
        int x = getOriginX();
        if(intersects(rowAndX, x, row)){
            return true;
        }
        for (int i = 0; i < arrivals.length; i++) {
            row = getRow(arrivals[i]);
            x = getX(arrivals[i]);
            if(intersects(rowAndX, x, row)){
                return true;
            }
        }
        return false;
    }
    
    private static boolean intersects(int[][] rowAndX, int x, int row){
        for (int i = 0; i < rowAndX.length; i++) {
            int[] cur = rowAndX[i];
            if(row == cur[0]){
                if(x <= cur[2] && x >= cur[1]){
                    return true;
                }
            }
        }
        return false;
    }
    
    private MicroSecondDate getOriginTime(){
        if(eventOrigin == null) {
            try {
                eventOrigin = eventAccess.get_preferred_origin();
            } catch (NoPreferredOrigin e) {
                eventOrigin = eventAccess.get_origins()[0];
            }
        }
        return new MicroSecondDate(eventOrigin.origin_time);
    }
    
    private int getOriginRow() {
        return getRow(getOriginTime());
    }
    
    private int getRow(Arrival arrival){
        return getRow(getTime(arrival));
    }
    
    private MicroSecondDate getTime(Arrival arrival){
        MicroSecondDate eventTime = getOriginTime();
        return new MicroSecondDate((long)arrival.getTime() * 1000000 + eventTime.getMicroSecondTime());
    }
    
    private int getRow(MicroSecondDate time){
        long microSeconds =  ( new MicroSecondDate(time)).getMicroSecondTime();
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(microSeconds/1000);
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        return hours/2;
    }
    
    private double getOriginXPercent(){
        return getXPercent(getOriginTime());
    }
    
    private double getXPercent(Arrival arrival){
        return getXPercent(getTime(arrival));
    }
    
    private double getXPercent(MicroSecondDate time){
        long microSeconds =  time.getMicroSecondTime();
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(microSeconds/1000);
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        double hoursPerRow = 24/display.getRows();
        double leftoverHours = hours%hoursPerRow;
        leftoverHours += minutes/60.0;
        leftoverHours += seconds/60.0/60.0;
        return leftoverHours/hoursPerRow;
    }
    
    public void setTitleLoc(int x, int y, int width, int height){
        titleX = x;
        titleY = y;
        titleWidth = width;
        titleHeight = height;
    }
    
    public Rectangle getTitleLoc(){
        return new Rectangle(titleX, titleY, titleWidth, titleHeight);
    }
    
    public String getTitle(){
        if(eventTitle == null){
            eventTitle = CacheEvent.getEventInfo(eventAccess, CacheEvent.LOC + " | " + CacheEvent.MAG + " | "  + CacheEvent.DEPTH + " | Distance ") + FORMATTER.format(arrivals[0].getDistDeg()) + " Degrees";
        }
        return eventTitle;
    }
    private static DecimalFormat FORMATTER =  new DecimalFormat("0.00");
    
    public int[][] getEventCoverage(){
        MicroSecondDate earliestTime = null;
        MicroSecondDate latestTime = null;
        for (int i = 0; i < arrivals.length; i++) {
            MicroSecondDate arrivalTime = getTime(arrivals[i]);
            if(earliestTime == null || arrivalTime.before(earliestTime)){
                earliestTime = arrivalTime;
            }
            if(latestTime == null || arrivalTime.after(latestTime)){
                latestTime = arrivalTime;
            }
        }
        MicroSecondDate twoEarlier = earliestTime.subtract(TWO_MINUTES);
        MicroSecondDate tenLater = latestTime.add(TEN_MINUTES);
        int[][] coverage ={{ getRow(twoEarlier), getX(twoEarlier)},
            {getRow(tenLater), getX(tenLater)}};
        return coverage;
    }
    
    private static final TimeInterval TWO_MINUTES= new TimeInterval(2, UnitImpl.MINUTE);
    
    private static final TimeInterval TEN_MINUTES = new TimeInterval(10, UnitImpl.MINUTE);
    
    public EventAccessOperations getEvent(){ return eventAccess; }
    
    public Color getColor(){ return color; }
    
    private int titleX, titleY, titleWidth, titleHeight;
    
    private String eventTitle;
    
    private PlottableDisplay display;
    
    private EventAccessOperations eventAccess;
    
    private Origin eventOrigin;
    
    private Color color = SeismogramDisplay.COLORS[++colorCount%SeismogramDisplay.COLORS.length];
    
    private Arrival[] arrivals;
    
    private static int colorCount = 0;
}// EventFlagPlotter
