package edu.sc.seis.fissuresUtil.xml;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Category;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.LocalDataCenterCallBack;

/**
 * DataSetSeismogram.java Created: Tue Feb 11 10:08:37 2003
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public abstract class DataSetSeismogram implements LocalDataCenterCallBack,
        Cloneable, StdAuxillaryDataNames {

    public DataSetSeismogram(DataSet ds, String name) {
        this(ds, name, null);
    }

    public DataSetSeismogram(DataSet ds,
                             String name,
                             RequestFilter requestFilter) {
        this.dssDataListeners = new LinkedList();
        this.rfChangeListeners = new LinkedList();
        this.requestFilter = requestFilter;
        this.dataSet = ds;
        this.name = name;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch(CloneNotSupportedException e) {
            // can't happen
            logger.error("Caught clone not supported, but this cannot happen!");
        } // end of try-catch
        return null;
    }

    public boolean equals(Object other) {
        if(!(getClass().equals(other.getClass()))) {
            return false;
        }
        if(super.equals(other)) {
            return true;
        }
        // objects are not the same, but may be cloned check request filter
        DataSetSeismogram otherDSS = (DataSetSeismogram)other;
        if(!otherDSS.getName().equals(getName())) {
            return false;
        }
        if(!ChannelIdUtil.areEqual(otherDSS.getRequestFilter().channel_id,
                                   getRequestFilter().channel_id)) {
            return false;
        }
        MicroSecondDate otherB = otherDSS.getBeginMicroSecondDate();
        MicroSecondDate thisB = getBeginMicroSecondDate();
        if(!otherB.equals(thisB)) {
            return false;
        } // end of if ()
        MicroSecondDate otherE = otherDSS.getEndMicroSecondDate();
        MicroSecondDate thisE = getEndMicroSecondDate();
        if(!otherE.equals(thisE)) {
            return false;
        } // end of if ()
        if(otherDSS.getDataSet() != getDataSet()) {
            return false;
        }
        return true;
    }

    /**
     * gets the dataset to which this seismogram belongs. May be null if it does
     * not belong to a dataset.
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    public Channel getChannel() {
        return getDataSet().getChannel(getChannelId());
    }

    public EventAccessOperations getEvent() {
        return getDataSet().getEvent();
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public String getName() {
        if(!isNameAutogenerated()) {
            return name;
        }
        return getAutogeneratedName();
    }

    public void setName(String name) {
        if(name.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException("name must be at least "
                    + MIN_NAME_LENGTH + " characters, " + name);
        }
        this.name = name;
    }

    public boolean isNameAutogenerated() {
        if(name == null || name.length() <= MIN_NAME_LENGTH) {
            return true;
        }
        return false;
    }

    protected String getAutogeneratedName() {
        String autoname = "";
        ChannelId channelId = getRequestFilter().channel_id;
        if(channelId != null && getDataSet() != null) {
            Channel chan = getDataSet().getChannel(channelId);
            if(chan != null) {
                autoname = generateName(chan);
            }
        }
        if(autoname == null || autoname.length() <= MIN_NAME_LENGTH) {
            autoname = ChannelIdUtil.toStringNoDates(getRequestFilter().channel_id);
        }
        return autoname;
    }

    public static String generateName(Channel chan) {
        return chan.getSite().getStation().getName() + " " + chan.get_id().channel_code;
    }

    public String toString() {
        return getName();
    }

    public MicroSecondDate getBeginMicroSecondDate() {
        return new MicroSecondDate(getBeginTime());
    }

    public edu.iris.Fissures.Time getBeginTime() {
        return getRequestFilter().start_time;
    }

    public void setBeginTime(edu.iris.Fissures.Time time) {
        getRequestFilter().start_time = time;
        fireBeginTimeChangedEvent();
    }

    public MicroSecondDate getEndMicroSecondDate() {
        return new MicroSecondDate(getEndTime());
    }

    public edu.iris.Fissures.Time getEndTime() {
        return getRequestFilter().end_time;
    }

    public void setEndTime(edu.iris.Fissures.Time time) {
        getRequestFilter().end_time = time;
        fireEndTimeChangedEvent();
    }

    /**
     * subclass may override this if they do not wish to use the internal
     * requestFilter field.
     */
    public RequestFilter getRequestFilter() {
        return requestFilter;
    }

    public ChannelId getChannelId() {
        return getRequestFilter().channel_id;
    }

    public Unit getYUnit() {
        return y_unit;
    }

    public void setYUnit(Unit unit) {
        y_unit = unit;
    }

    public void addRequestFilterChangeListener(RequestFilterChangeListener listener) {
        rfChangeListeners.add(listener);
    }

    public void removeRequestFilterChangeListener(RequestFilterChangeListener listener) {
        rfChangeListeners.remove(listener);
    }

    protected List getRFChangeListenersCopy() {
        LinkedList tmp;
        synchronized(rfChangeListeners) {
            tmp = new LinkedList(rfChangeListeners);
        }
        return tmp;
    }

    protected List getDSSDataListenersCopy() {
        LinkedList tmp;
        synchronized(dssDataListeners) {
            tmp = new LinkedList(dssDataListeners);
        }
        return tmp;
    }

    protected void fireEndTimeChangedEvent() {
        // use temp array to avoid concurrentModificationException
        Collection tmp = getRFChangeListenersCopy();
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            RequestFilterChangeListener listener = (RequestFilterChangeListener)iterator.next();
            listener.endTimeChanged();
        }
    }

    protected void fireBeginTimeChangedEvent() {
        // use temp array to avoid concurrentModificationException
        Collection tmp = getRFChangeListenersCopy();
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            RequestFilterChangeListener listener = (RequestFilterChangeListener)iterator.next();
            listener.beginTimeChanged();
        }
    }

    public void addSeisDataChangeListener(SeisDataChangeListener dataListener) {
        synchronized(dssDataListeners) {
            dssDataListeners.add(dataListener);
        }
    }

    public void removeSeisDataChangeListener(SeisDataChangeListener dataListener) {
        synchronized(dssDataListeners) {
            dssDataListeners.remove(dataListener);
        }
    }

    protected void fireNewDataEvent(SeisDataChangeEvent event) {
        // use temp array to avoid concurrentModificationException
        List tmp = getDSSDataListenersCopy();
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            SeisDataChangeListener dssDataListener = (SeisDataChangeListener)iterator.next();
            dssDataListener.pushData(event);
        }
    }

    protected void fireDataFinishedEvent(SeisDataChangeEvent event) {
        // use temp array to avoid concurrentModificationException
        List tmp = getDSSDataListenersCopy();
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            SeisDataChangeListener dssDataListener = (SeisDataChangeListener)iterator.next();
            dssDataListener.finished(event);
        }
    }

    protected void fireDataErrorEvent(SeisDataErrorEvent event) {
        // use temp array to avoid concurrentModificationException
        List tmp = getDSSDataListenersCopy();
        Iterator iterator = tmp.iterator();
        while(iterator.hasNext()) {
            SeisDataChangeListener dssDataListener = (SeisDataChangeListener)iterator.next();
            dssDataListener.error(event);
        }
    }

    public void pushData(LocalSeismogramImpl[] seismograms,
                         SeisDataChangeListener initiator) {
        SeisDataChangeEvent event = new SeisDataChangeEvent(seismograms,
                                                            this,
                                                            initiator);
        addToCache(seismograms);
        // if the initiator is not already registered send event to it as well
        if(initiator != null && !dssDataListeners.contains(initiator)) {
            initiator.pushData(event);
        }
        fireNewDataEvent(event);
    }

    protected void addToCache(LocalSeismogramImpl[] seismograms) {
        for(int i = 0; i < seismograms.length; i++) {
            addToCache(seismograms[i]);
        }
    }

    protected void addToCache(LocalSeismogramImpl seismogram) {
        synchronized(seisCache) {
            if(!cacheContains(seismogram)) {
                seisCache.add(new SoftReference(seismogram));
            }
        }
    }

    private boolean cacheContains(LocalSeismogramImpl seismogram) {
        synchronized(seisCache) {
            Iterator it = seisCache.iterator();
            while(it.hasNext()) {
                LocalSeismogramImpl current = (LocalSeismogramImpl)((SoftReference)it.next()).get();
                if(current == null) {
                    it.remove();
                } else if(current.get_id().equals(seismogram.get_id())
                        || equalOrContains(current, seismogram)) {
                    return true;
                } else if(equalOrContains(seismogram, current)) {
                    it.remove();
                    return false;
                }
            }
        }
        return false;
    }

    // returns true if one is equal to or contains two in time
    public static boolean equalOrContains(LocalSeismogramImpl one,
                                          LocalSeismogramImpl two) {
        if(one.getBeginTime().equals(two.getBeginTime())
                || one.getBeginTime().before(two.getBeginTime())) {
            if(one.getEndTime().equals(two.getEndTime())
                    || one.getEndTime().after(two.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    public void finished(SeisDataChangeListener initiator) {
        SeisDataChangeEvent event = new SeisDataChangeEvent(this, initiator);
        // if the initiator is not already registered send event to it as well
        if(!dssDataListeners.contains(initiator)) {
            initiator.finished(event);
        }
        fireDataFinishedEvent(event);
    }

    public void error(SeisDataChangeListener initiator, Throwable e) {
        SeisDataErrorEvent event = new SeisDataErrorEvent(e, this, initiator);
        // if the initiator is not already registered send event to it as well
        if(!dssDataListeners.contains(initiator)) {
            initiator.error(event);
        }
        fireDataErrorEvent(event);
    }

    public abstract void retrieveData(SeisDataChangeListener dataListener);

    public void addAuxillaryData(Object key, Object value) {
        auxillaryData.put(key, value);
    }

    public Object getAuxillaryData(Object key) {
        return auxillaryData.get(key);
    }

    public Object removeAuxillaryData(Object key) {
        return auxillaryData.remove(key);
    }

    public Collection getAuxillaryDataKeys() {
        return auxillaryData.keySet();
    }

    private int MIN_NAME_LENGTH = 5;

    private List dssDataListeners;

    private List rfChangeListeners;

    protected List seisCache = Collections.synchronizedList(new ArrayList());

    RequestFilter requestFilter;

    private DataSet dataSet = null;

    private String name = null;

    private HashMap auxillaryData = new HashMap();

    protected Unit y_unit;

    static Category logger = Category.getInstance(DataSetSeismogram.class.getName());
}// DataSetSeismogram
