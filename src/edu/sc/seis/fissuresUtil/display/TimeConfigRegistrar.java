package edu.sc.seis.fissuresUtil.display;

import java.util.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import org.apache.log4j.*;

/**
 * TimeConfigRegistrar.java
 *
 *
 * Created: Mon Jul  1 13:56:36 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class TimeConfigRegistrar implements TimeRangeConfig, TimeSyncListener{
    public TimeConfigRegistrar(){ 
	this(new BoundedTimeConfig()); 
    }

    public TimeConfigRegistrar(TimeRangeConfig timeConfig){
	this.timeConfig = timeConfig;
	timeConfig.addTimeSyncListener(this);
	timeFinder = timeConfig.getTimeFinder();
    }
    
    public void setTimeConfig(TimeRangeConfig newTimeConfig){ 
	timeConfig.removeTimeSyncListener(this);
	Iterator e = seismos.keySet().iterator();
	timeFinder = newTimeConfig.getTimeFinder();
	newTimeConfig.addTimeSyncListener(this);
	while(e.hasNext()){
	    System.out.println("switching seismograms");
	    DataSetSeismogram current = (DataSetSeismogram)e.next();
	    timeConfig.removeSeismogram(current);
	    this.addSeismogram(current);
	    newTimeConfig.addSeismogram(current, (MicroSecondDate)seismos.get(current));
	}
	timeConfig = newTimeConfig;
    }

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(DataSetSeismogram seis){
	seismos.put(seis, timeFinder.getBeginTime(seis));
	timeConfig.addSeismogram(seis, timeFinder.getBeginTime(seis));
    }

    public void addSeismogram(DataSetSeismogram seis, MicroSecondDate b){
	seismos.put(seis, b);
	timeConfig.addSeismogram(seis, b);
    }

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(DataSetSeismogram seis){ 
	timeConfig.removeSeismogram(seis);
    }

    public boolean contains(DataSetSeismogram seis){
	return seismos.containsKey(seis);
    }

    public MicroSecondTimeRange getTimeRange(DataSetSeismogram seis){
	MicroSecondTimeRange current =  timeConfig.getTimeRange(seis);
	seismoDisplayTime.put(seis, current);
	return current;
    }

    public MicroSecondTimeRange getTimeRange(){ 
	genericTime = timeConfig.getTimeRange();
	return genericTime; 
    }
    
    /**
     * Adds a time sync listener to the list to be informed when a time sync event occurs
     * 
     * @param t the time sync listener to be added
     */
    public void addTimeSyncListener(TimeSyncListener t){ timeListeners.add(t); }

    /**
     * Removes a TimeSyncListener from the update list
     *
     * @param t the time sync listener to be removed
     */
    public void removeTimeSyncListener(TimeSyncListener t){ timeListeners.remove(t); }
	
    
    /**
     * Fire an event to all of the time sync listeners to update their time ranges
     *
     */
    public void updateTimeSyncListeners(){
	Iterator e = timeListeners.iterator();
	while(e.hasNext()){
	    ((TimeSyncListener)e.next()).updateTimeRange();
	}
    }

    public void updateTimeRange(){
	this.updateTimeSyncListeners(); 
    }

    public synchronized TimeSnapshot takeSnapshot(){
	return new TimeSnapshot((HashMap)seismoDisplayTime.clone(), genericTime);
    }
    
    public void unregister(){ timeConfig.removeTimeSyncListener(this); }

    public synchronized void setDisplayInterval(TimeInterval t){ timeConfig.setDisplayInterval(t); }

    public synchronized void setBeginTime(MicroSecondDate b){ timeConfig.setBeginTime(b); }
    
    public synchronized void setBeginTime(DataSetSeismogram seismo, MicroSecondDate b){
	timeConfig.setBeginTime(seismo, b);
	seismos.put(seismo, b);
    }
    
    public synchronized void setAllBeginTime(MicroSecondDate b){ 
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext()){
	    DataSetSeismogram current = (DataSetSeismogram)e.next();
	    timeConfig.setBeginTime(current, b);
	    seismos.put(current, b);
	}
    }

    public void set(MicroSecondDate begin, TimeInterval displayInterval){ timeConfig.set(begin, displayInterval); }
    

    public void fireTimeRangeEvent(TimeSyncEvent event){ timeConfig.fireTimeRangeEvent(event);  }

    public TimeFinder getTimeFinder(){ return timeFinder; }

    public void setTimeFinder(TimeFinder tf){ timeFinder = tf; }
 
    protected HashMap seismos = new HashMap();

    protected HashMap seismoDisplayTime = new HashMap();
    
    protected TimeRangeConfig timeConfig;

    protected Set timeListeners = new HashSet();

    protected TimeFinder timeFinder;

    protected MicroSecondTimeRange genericTime;

    protected Category logger = Category.getInstance(TimeConfigRegistrar.class.getName());
}// TimeConfigRegistrar
