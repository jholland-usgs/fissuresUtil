package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import java.awt.Dimension;
import edu.iris.Fissures.model.TimeInterval;

/**
 * PlotInfo.java
 *
 *
 * Created: Wed Jun 12 11:49:01 2002
 *
 * @author Charlie Groves
 * @version
 */

public class PlotInfo {
    public PlotInfo(Dimension size, HashMap seisPlotters, HashMap filterPlotters, TimeInterval displayInterval){
	this.size = size;
	this.seisPlotters = seisPlotters;
	this.filterPlotters = filterPlotters;
	this.displayInterval = displayInterval;
    }

    public Dimension getSize(){ return size; }

    public HashMap getFilterPlotters(){ return filterPlotters; }

    public HashMap getSeisPlotters(){ return seisPlotters; }

    public TimeInterval getDisplayInterval(){ return displayInterval; }

    private Dimension size;
    
    private HashMap seisPlotters, filterPlotters;

    private TimeInterval displayInterval;
}// PlotInfo
