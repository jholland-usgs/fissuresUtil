
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class CacheNetworkAccess implements NetworkAccess {

    public CacheNetworkAccess(NetworkAccess net) {
        if (net == null) {
            throw new NullPointerException("network is null");
        } // end of if (net == null)
        this.net = net;
    }

    public NetworkAccess getNetworkAccess() { return net; }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/get_attributes:1.0
    //
    /***/

    public NetworkAttr get_attributes() {
        if (attr == null) {
            attr = net.get_attributes();
        }
        return attr;
    }

    /** retreives the stations for the network, but uses the cached copy
     *  if it has been previously retrieved. The stations are also cleaned
     *  of duplicate networkAttr objects to free memory.
     *  @see clean(Station[])
     */
    public Station[] retrieve_stations() {
        if (stations == null) {
            stations = net.retrieve_stations();
           // clean(stations);
        }
        return stations;
    }
    /** retreives the channels for the stations, but uses the cached copy
     *  if it has been previously retrieved. The channels are also cleaned
     *  of duplicate site objects to free memory.
     *  @see clean(Channel[])
     */
    public Channel[] retrieve_for_station(StationId id) {
        String idStr = StationIdUtil.toString(id);
        if ( ! channelMap.containsKey(idStr)) {
            Channel[] chans = net.retrieve_for_station(id);
          //  clean(chans);
            channelMap.put(idStr, chans);
        }
        return (Channel[])channelMap.get(idStr);
    }

    /** Cleans the array of channels so that the sites are shared
     *  if they are identical. This frees memory that is otherwise wasted on
     *  identical copies of the same objects. */
    public static void clean(Channel[] chans) {
        ArrayList knownSites = new ArrayList();
        knownSites.add(chans[0].my_site);
        for (int i = 0; i < chans.length; i++) {
            clean(chans[i].get_id());
            boolean foundSite = false;
            Iterator it = knownSites.iterator();
            while ( ! foundSite && it.hasNext()) {
                Site site = (Site)it.next();
                if (SiteIdUtil.areEqual(chans[i].my_site.get_id(), site.get_id())) {
                    chans[i].my_site = site;
                    chans[i].get_id().site_code = site.get_code();
                    foundSite=true;
                }
            }
            if ( ! foundSite) {
                knownSites.add(0, chans[i].my_site);
            }
        }
        clean((Site[])knownSites.toArray(new Site[0]));
    }

    public static void clean(ChannelId id){
        id.channel_code = getKnownChannelCode(id.channel_code);
        id.network_id = getKnown(id.network_id);
        id.station_code = getKnownStationCode(id.station_code);
        id.site_code = getKnownSiteCode(id.site_code);
        id.begin_time = getKnown(id.begin_time);
    }

    public static void clean(SiteId id){
        id.network_id = getKnown(id.network_id);
        id.station_code = getKnownStationCode(id.station_code);
        id.site_code = getKnownSiteCode(id.site_code);
        id.begin_time = getKnown(id.begin_time);
    }

    public static void clean(StationId id){
        id.network_id = getKnown(id.network_id);
        id.station_code = getKnownStationCode(id.station_code);
        id.begin_time = getKnown(id.begin_time);
    }

    /** Cleans the array of sites so that the stations are shared
     *  if they are identical. This frees memory that is otherwise wasted on
     *  identical copies of the same objects. */
    public static void clean(Site[] sites) {
        ArrayList knownStations = new ArrayList();
        knownStations.add(sites[0].my_station);
        for (int i = 0; i < sites.length; i++) {
            clean(sites[i].get_id());
            boolean foundStation = false;
            Iterator it = knownStations.iterator();
            while ( ! foundStation && it.hasNext()) {
                Station station = (Station)it.next();
                if (StationIdUtil.areEqual(sites[i].my_station.get_id(), station.get_id())) {
                    sites[i].my_station = station;
                    foundStation=true;
                }
            }
            if ( ! foundStation) {
                knownStations.add(0, sites[i].my_station);
            }
        }
        clean((Station[])knownStations.toArray(new Station[0]));
    }

    /** Cleans the array of stations so that the Network Attributes are shared
     *  if they are identical. This frees memory that is otherwise wasted on
     *  identical copies of the same objects. */
    public static void clean(Station[] stations) {
        ArrayList knownNets = new ArrayList();
        knownNets.add(stations[0].my_network);
        for (int i = 0; i < stations.length; i++) {
            clean(stations[i].get_id());
            boolean foundNet = false;
            Iterator it = knownNets.iterator();
            while ( ! foundNet && it.hasNext()) {
                NetworkAttr network = (NetworkAttr)it.next();
                if (NetworkIdUtil.areEqual(stations[i].my_network.get_id(), network.get_id())) {
                    stations[i].my_network = network;
                    foundNet=true;
                }
            }
            if ( ! foundNet) {
                knownNets.add(0, stations[i].my_network);
            }
        }
    }

    private static String getKnownChannelCode(String unkownCode){
        synchronized(knownChannelCodes){
            Iterator it = knownChannelCodes.iterator();
            while(it.hasNext()){
                String cur = (String)it.next();
                if(cur.equals(unkownCode)) return cur;
            }
            knownChannelCodes.add(unkownCode);
        }
        return unkownCode;
    }

    private static List knownChannelCodes = Collections.synchronizedList(new ArrayList());

    private static String getKnownStationCode(String unkownCode){
        synchronized(knownStationCodes){
            Iterator it = knownStationCodes.iterator();
            while(it.hasNext()){
                String cur = (String)it.next();
                if(cur.equals(unkownCode)) return cur;
            }
            knownStationCodes.add(unkownCode);
        }
        return unkownCode;
    }

    private static List knownStationCodes = Collections.synchronizedList(new ArrayList());

    private static String getKnownSiteCode(String unkownCode){
        synchronized(knownSiteCodes){
            Iterator it = knownSiteCodes.iterator();
            while(it.hasNext()){
                String cur = (String)it.next();
                if(cur.equals(unkownCode)) return cur;
            }
            knownSiteCodes.add(unkownCode);
        }
        return unkownCode;
    }

    private static List knownSiteCodes = Collections.synchronizedList(new ArrayList());

    private static NetworkId getKnown(NetworkId unknownId){
        synchronized(knownNetworkIds){
            Iterator it = knownNetworkIds.iterator();
            while(it.hasNext()){
                NetworkId cur = (NetworkId)it.next();
                if(NetworkIdUtil.areEqual(unknownId, cur))return cur;
            }
            knownNetworkIds.add(unknownId);
        }
        return unknownId;
    }

    private static List knownNetworkIds = Collections.synchronizedList(new ArrayList());

    private static Time getKnown(Time unknownTime){
        synchronized(knownTimes){
            Iterator it = knownTimes.iterator();
            while(it.hasNext()){
                Time cur = (Time)it.next();
                if(cur.date_time.equals(unknownTime.date_time) &&
                   cur.leap_seconds_version == unknownTime.leap_seconds_version)
                    return cur;
            }
            knownTimes.add(unknownTime);
        }
        return unknownTime;
    }

    private static List knownTimes = Collections.synchronizedList(new ArrayList());

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_grouping:1.0
//
    /***/

    public ChannelId[]
        retrieve_grouping(ChannelId id)
        throws ChannelNotFound {
        return net.retrieve_grouping(id);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_groupings:1.0
//
    /***/

    public ChannelId[][]
        retrieve_groupings() {
        return net.retrieve_groupings();
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_channel:1.0
//
    /***/

    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        return net.retrieve_channel(id);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_channels_by_code:1.0
//
    /***/

    public Channel[] retrieve_channels_by_code(String station_code,
                                               String site_code,
                                               String channel_code)
        throws ChannelNotFound {
        return net.retrieve_channels_by_code(station_code, site_code, channel_code);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/locate_channels:1.0
//
    /***/

    public Channel[] locate_channels(edu.iris.Fissures.Area the_area,
                                     SamplingRange sampling,
                                     OrientationRange orientation) {
        return net.locate_channels(the_area, sampling, orientation);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_instrumentation:1.0
//
    /***/

    public Instrumentation
        retrieve_instrumentation(ChannelId id,
                                 edu.iris.Fissures.Time the_time)
        throws ChannelNotFound {
        return net.retrieve_instrumentation(id, the_time);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_calibrations:1.0
//
    /***/

    public Calibration[]
        retrieve_calibrations(ChannelId id,
                              edu.iris.Fissures.TimeRange the_time)
        throws ChannelNotFound,
        edu.iris.Fissures.NotImplemented {
        return net.retrieve_calibrations(id, the_time);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_time_corrections:1.0
//
    /***/

    public TimeCorrection[]
        retrieve_time_corrections(ChannelId id,
                                  edu.iris.Fissures.TimeRange time_range)
        throws ChannelNotFound,
        edu.iris.Fissures.NotImplemented {
        return net.retrieve_time_corrections(id, time_range);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_all_channels:1.0
//
    /***/

    public ChannelId[]
        retrieve_all_channels(int seq_max,
                              ChannelIdIterHolder iter) {

        return net.retrieve_all_channels(seq_max, iter);
    }

//
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/get_audit_trail_for_channel:1.0
//
    /***/

    public edu.iris.Fissures.AuditElement[]
        get_audit_trail_for_channel(ChannelId id)
        throws ChannelNotFound,
        edu.iris.Fissures.NotImplemented {
        return net.get_audit_trail_for_channel(id);
    }

//
    // IDL:iris.edu/Fissures/AuditSystemAccess/get_audit_trail:1.0
//
    /***/

    public AuditElement[]
        get_audit_trail()
        throws NotImplemented {
        return net.get_audit_trail();
    }

    NetworkAccess net;
    NetworkAttr attr;
    Station[] stations;
    HashMap channelMap = new HashMap();

    static Logger logger = Logger.getLogger(CacheNetworkAccess.class);
}

