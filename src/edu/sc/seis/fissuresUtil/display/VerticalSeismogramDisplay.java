package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.ExceptionHandlerGUI;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.swing.BoxLayout;
import org.apache.log4j.Category;

/**
 * VerticalSeismogramDisplay(VSD) is a JComponent that can contain multiple
 * BasicSeismogramDisplays(BSD) and also controls the selection windows and
 * particle motion windows created by its BSDs.
 *
 *
 * Created: Tue Jun  4 10:52:23 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public abstract class VerticalSeismogramDisplay extends SeismogramDisplay{
    /**
     * Creates a <code>VerticalSeismogramDisplay</code> without a parent
     *
     */
    public VerticalSeismogramDisplay(){
        this(null);
    }

    /**
     * Creates a <code>VerticalSeismogramDisplay</code>
     *
     * @param parent the VSD that controls this VSD
     */
    public VerticalSeismogramDisplay(VerticalSeismogramDisplay parent){
        output.setTimeZone(TimeZone.getTimeZone("GMT"));
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if(parent != null){
            this.originalVisible = parent.getOriginalVisibility();
            this.parent = parent;
        }else{
            this.originalVisible = true;
        }
    }

    public void add(DataSetSeismogram[] dss){ addDisplay(dss); }

    public void remove(DataSetSeismogram[] dss){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            ((BasicSeismogramDisplay)it.next()).remove(dss);
        }
    }

    public boolean contains(DataSetSeismogram seismo){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            if(((BasicSeismogramDisplay)it.next()).contains(seismo)){
                return true;
            }
        }
        return false;
    }

    /**
     * adds the given seismograms to the VSD with their seismogram names as suggestions
     *
     *
     * @param dss the seismograms to be added
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss);

    /**
     * adds the seismograms to the VSD with the passed timeConfig
     *
     * @param dss the seismograms to be added
     * @param tc the time config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc);

    /**
     * adds the seismograms to the VSD with the passed amp config
     * @param dss the seismograms to be added
     * @param ac the amp config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac);

    /**
     * adds the seismograms to the VSD with the passed timeConfig and ampConfig
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @return the BSD the seismograms were added to
     *
     */
    public abstract BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac);

    public DataSetSeismogram[] getSeismograms(){
        java.util.List seismogramList = new ArrayList();
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            DataSetSeismogram[] seismos = ((BasicSeismogramDisplay)e.next()).getSeismograms();
            for(int i = 0; i < seismos.length; i++){
                seismogramList.add(seismos[i]);
            }
        }
        return ((DataSetSeismogram[])seismogramList.toArray(new DataSetSeismogram[seismogramList.size()]));
    }

    /**
     * <code>getDisplays</code> returns a list of all the displays directly held by this VSD
     *
     * @return a list of all direcly held displays
     */
    public LinkedList getDisplays(){ return basicDisplays; }

    /**
     * Sets a string to be appended to the names of each seismogram added to the display.
     * @param suffix the suffix for the seismogram names
     */
    public void setSuffix(String suffix){
        this.suffix = suffix;
    }


    public void print(){
        SeismogramPrinter.print(getDisplayArray());
        revalidate();
    }

    /**
     * <code>getDisplayArray</code> returns an array containing all of
     * the displays directly held by this VSD
     *
     * @return all displays directly held by this vsd
     */
    public BasicSeismogramDisplay[] getDisplayArray(){
        return ((BasicSeismogramDisplay[])basicDisplays.toArray(new BasicSeismogramDisplay[basicDisplays.size()]));
    }

    public void remove(Selection selection){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            ((BasicSeismogramDisplay)it.next()).remove(selection);
        }
    }

    public void clearSelections(){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).clearSelections();
        }
    }

    public void clear(){
        removeAll();
    }

    /**
     * <code>removeAll</code> clears this display and all of its children,
     * and if it has a parent, removes it from the parent as well
     *
     */
    public void removeAll(){
        logger.debug("removing all displays");
        super.removeAll();
        basicDisplays.clear();
        tc = new BasicTimeConfig();
        ac = new RMeanAmpConfig();
        time = "   Time: ";
        amp = "   Amplitude: ";
        repaint();
    }

    /**
     * <code>removeDisplay</code> removes a BSD from the VSD
     *
     * @param display the BSD to be removed
     * @return true if the display is removed
     */
    public boolean removeDisplay(BasicSeismogramDisplay display){
        if(basicDisplays.contains(display)){
            if(basicDisplays.size() == 1){
                this.removeAll();
                return true;
            }
            super.remove(display);
            basicDisplays.remove(display);
            remove(display.getSeismograms());
            ((BasicSeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
            ((BasicSeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
            super.revalidate();
            display.destroy();
            repaint();
            return true;
        }
        return false;
    }

    protected void addTimeBorders(){
        if(topDisplay != null){
            topDisplay.removeTopTimeBorder();
        }
        topDisplay = (BasicSeismogramDisplay)super.getComponent(0);
        topDisplay.addTopTimeBorder();
        if(bottomDisplay != null){
            bottomDisplay.removeBottomTimeBorder();
        }
        bottomDisplay = (BasicSeismogramDisplay)super.getComponent(super.getComponentCount() - 1);
        bottomDisplay.addBottomTimeBorder();
    }


    /**
     * <code>setTimeAmp</code> sets the time and amp labels for all VSDs
     *
     * @param time the new label time
     * @param amp the new label amp
     */
    public void setTimeAmp(MicroSecondDate newTime, QuantityImpl newAmp, UnitRangeImpl ampRange){
        if(curAmpRange != ampRange){
            double absMax = Math.abs(ampRange.getMaxValue());
            double absMin = Math.abs(ampRange.getMinValue());
            double maxVal;
            if(absMax/10 > absMin){
                maxVal = absMax;
            }else{
                maxVal = absMin;
            }
            if(maxVal < 1){
                formatter = new DecimalFormat(" 0.000E0;-0.000E0");
            }else{
                StringBuffer formatPattern = new StringBuffer(" 0.00;-0.00");
                while(maxVal > 10){
                    formatPattern.insert(1,"0");
                    formatPattern.insert(formatPattern.length() - 4, "0");
                    maxVal /= 10;
                }
                formatter = new DecimalFormat(formatPattern.toString());
            }
            curAmpRange = ampRange;
        }
        double newAmpVal = newAmp.getValue();
        if(newAmpVal == Double.NaN){
            amp = "";
        }else{
            String ampString = amplitude;
            ampString += formatter.format(newAmpVal);
            amp = ampString+" "+unitDisplayUtil.getNameForUnit(newAmp.getUnit());
        }
        calendar.setTime(newTime);
        StringBuffer timeBuffer = new StringBuffer(amp.length());
        if(output.format(calendar.getTime()).length() == 21)
            timeBuffer.append(output.format(calendar.getTime()) + "00");
        else if(output.format(calendar.getTime()).length() == 22)
            timeBuffer.append(output.format(calendar.getTime()) + "0");
        else
            timeBuffer.append(output.format(calendar.getTime()));
        int numSpaces = amp.length() - timeBuffer.length() - 10;//Amplitude: is 10 chars
        if(numSpaces <= 0){
            timeBuffer.insert(0, "Time:");
            time = timeBuffer.toString();
        }else{
            StringBuffer spaces = new StringBuffer(numSpaces);
            for (int i = 0; i < numSpaces; i++){
                spaces.append(" ");
            }
            timeBuffer.insert(0, spaces);
            timeBuffer.insert(0, "Time:");
            time = timeBuffer.toString();
        }
    }
    /**
     * <code>setOriginalDisplay</code> sets the display of the unfiltered
     * seismogram in all BSDs held by this VSD or its children
     *
     * @param visible the new visibility of the unfiltered seismogram
     */
    public void setOriginalVisibility(boolean visible){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).setOriginalVisibility(visible);
        }
        originalVisible = visible;
    }

    /**
     *
     * @return true if the unfiltered seismogram is visible
     */
    public boolean getOriginalVisibility(){ return originalVisible; }

    public void setCurrentTimeFlag(boolean visible){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).setCurrentTimeFlag(visible);
        }
        currentTimeFlag = visible;
    }

    public boolean getCurrentTimeFlagStatus(){ return currentTimeFlag; }

    /**
     * <code>applyFilter</code> applies a new filter to all the BSDs held
     * by this VSD and its children
     *
     * @param filter a <code>ColoredFilter</code> value
     */
    public void applyFilter(ColoredFilter filter){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).applyFilter(filter);
        }
    }

    public void removeFilter(ColoredFilter filter){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).removeFilter(filter);
        }
    }

    public void setAmpConfig(AmpConfig ac){
        this.ac = ac;
    }

    public void setGlobalizedAmpConfig(AmpConfig ac){
        setAmpConfig(ac);
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            ((BasicSeismogramDisplay)it.next()).setAmpConfig(ac);
        }
        globalizedAmp = true;
        tc.addListener(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        Iterator it = basicDisplays.iterator();
        Class configClass = ac.getClass();
        while(it.hasNext()){
            try{
                ((BasicSeismogramDisplay)it.next()).setAmpConfig((AmpConfig)configClass.newInstance());
            }catch(IllegalAccessException e){
                ExceptionHandlerGUI.handleException("Problem creating ampConfig from class", e);
            }catch(InstantiationException e){
                ExceptionHandlerGUI.handleException("Problem creating ampConfig from class", e);
            }
        }
        tc.removeListener(ac);
        globalizedAmp = false;
    }

    public AmpConfig getAmpConfig(){
        return ac;
    }
    public void setTimeConfig(TimeConfig config){
        Iterator it = basicDisplays.iterator();
        while(it.hasNext()){
            BasicSeismogramDisplay current = (BasicSeismogramDisplay)it.next();
            if(current.getTimeConfig().equals(tc)){
                current.setTimeConfig(config);
            }
        }
        if(globalizedAmp){
            config.addListener(ac);
        }
        tc = config;
    }

    public TimeConfig getTimeConfig(){
        return tc;
    }

    public void reset(){
        tc.reset();
        ac.reset();
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).reset();
        }
    }

    public void reset(DataSetSeismogram[] seismos){
        Iterator e = basicDisplays.iterator();
        while(e.hasNext()){
            ((BasicSeismogramDisplay)e.next()).reset(seismos);
        }
    }

    private BasicSeismogramDisplay topDisplay, bottomDisplay;

    public static String getTime(){ return time; }

    public static String getAmp(){ return amp; }

    protected String suffix = "";

    protected boolean originalVisible, globalizedAmp = false, currentTimeFlag = false;

    protected TimeConfig tc = new BasicTimeConfig();

    protected AmpConfig ac = new RMeanAmpConfig();

    protected LinkedList basicDisplays = new LinkedList();

    public static String time = new String("");

    public static String amp = new String("");

    private UnitRangeImpl curAmpRange;

    private String amplitude = new String("Amplitude:");

    public static DecimalFormat formatter = new DecimalFormat(" 0.000E0;-0.000E0");

    UnitDisplayUtil unitDisplayUtil = new UnitDisplayUtil();

    protected SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss.SSS");

    protected static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private VerticalSeismogramDisplay parent;

    private static Category logger = Category.getInstance(VerticalSeismogramDisplay.class.getName());
}// VerticalSeismogramDisplay
