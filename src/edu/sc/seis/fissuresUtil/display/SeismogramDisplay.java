package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.DataSetSeismogramReceptacle;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

public abstract class SeismogramDisplay extends JComponent implements DataSetSeismogramReceptacle{
    public SeismogramDisplay(){
        this(mouseForwarder, motionForwarder);
    }

    public SeismogramDisplay(MouseForwarder mf, MouseMotionForwarder mmf){
        mouseForwarder = mf;
        motionForwarder = mmf;
        if(mouseForwarder == null || motionForwarder == null){
            throw new IllegalStateException("The mouse forwarders on SeismogramDisplay must be set before any seismogram displays are invoked");
        }
    }

    public void add(SeismogramDisplayListener listener){
        listeners.add(listener);
    }

    public void remove(SeismogramDisplayListener listener){
        listeners.remove(listener);
    }

    public void switchDisplay(SeismogramDisplay to){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((SeismogramDisplayListener)it.next()).switching(this, to);
        }
    }

    public Color getNextColor(Class colorGroupClass){
        int[] usages = new int[colors.length];
        for (int i = 0; i < colors.length; i++){
            Iterator it = iterator(colorGroupClass);
            while(it.hasNext()){
                Drawable cur = (Drawable)it.next();
                if(cur.getColor().equals(colors[i])){
                    usages[i]++;
                }
                if(cur instanceof DrawableSeismogram){
                    DrawableSeismogram curSeis = (DrawableSeismogram)cur;
                    Iterator childIterator = curSeis.iterator(colorGroupClass);
                    while(childIterator.hasNext()){
                        Drawable curChild = (Drawable)childIterator.next();
                        if(curChild.getColor().equals(colors[i])){
                            usages[i]++;
                        }
                    }
                }
            }
        }
        for(int minUsage = 0; minUsage >= 0; minUsage++){
            for (int i = 0; i < usages.length; i++){
                if(usages[i] == minUsage){
                    return colors[i];
                }
            }
        }
        return colors[i++%colors.length];
    }

    private int i = 0;

    public abstract void add(Drawable drawable);

    public abstract void remove(Drawable drawable);

    public abstract DrawableIterator getDrawables(MouseEvent e);

    public abstract DrawableIterator iterator(Class drawableClass);

    public abstract void setTimeConfig(TimeConfig timeConfig);

    public abstract TimeConfig getTimeConfig();

    public abstract void setAmpConfig(AmpConfig ampConfig);

    public abstract void setGlobalizedAmpConfig(AmpConfig ampConfig);

    public abstract void setIndividualizedAmpConfig(AmpConfig ampConfig);

    public abstract AmpConfig getAmpConfig();

    public abstract DataSetSeismogram[] getSeismograms();

    public abstract void print();

    public void remove(Selection selection){}

    public static void setMouseMotionForwarder(MouseMotionForwarder mf){
        motionForwarder = mf;
    }

    public static MouseMotionForwarder getMouseMotionForwarder(){
        return motionForwarder;
    }

    public static void setMouseForwarder(MouseForwarder mf){
        mouseForwarder = mf;
    }

    public static MouseForwarder getMouseForwarder(){ return mouseForwarder; }

    public static Set getActiveFilters(){ return activeFilters; }

    public static void setCurrentTimeFlag(boolean visible){
        currentTimeFlag = visible;
    }

    public static boolean getCurrentTimeFlag(){ return currentTimeFlag; }

    private static MouseMotionForwarder motionForwarder;

    private static MouseForwarder mouseForwarder;

    private List listeners = new ArrayList();

    private static boolean currentTimeFlag = false;

    protected static Set activeFilters = new HashSet();

    private static Color[] colors = {Color.BLUE, new Color(217, 91, 23), new Color(179, 182,46), new Color(141, 18, 69), new Color(103,109,92),new Color(65,200,115),new Color(27,36,138), new Color(130,145,230), new Color(54,72,21), new Color(119,17,136)};
}
