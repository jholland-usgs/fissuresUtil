package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkId;

public class RT130ToLocalSeismogram {

    public RT130ToLocalSeismogram() {
        this.ncFile = null;
        this.props = null;
        this.stationLocations = null;
    }

    public RT130ToLocalSeismogram(NCFile ncFile, Properties props)
            throws FileNotFoundException, IOException {
        this.ncFile = ncFile;
        this.props = props;
        String xyFileLoc = props.getProperty("XYFileLoc");
        logger.debug("XY file location: " + xyFileLoc);
        stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
    }

    public RT130ToLocalSeismogram(NCFile ncFile,
                                  Properties props,
                                  Map stationLocations) {
        this.ncFile = ncFile;
        this.props = props;
        this.stationLocations = stationLocations;
    }

    public LocalSeismogramImpl[] ConvertRT130ToLocalSeismogram(PacketType[] seismogramDataPacket) {
        LocalSeismogramImpl[] seismogramDataArray = new LocalSeismogramImpl[seismogramDataPacket.length];
        this.channel = new Channel[seismogramDataPacket.length];
        for(int i = 0; i < seismogramDataPacket.length; i++) {
            seismogramDataArray[i] = ConvertRT130ToLocalSeismogram(seismogramDataPacket[i],
                                                                   i);
        }
        return seismogramDataArray;
    }

    public LocalSeismogramImpl ConvertRT130ToLocalSeismogram(PacketType seismogramData,
                                                             int i) {
        Time mockBeginTimeOfChannel = seismogramData.begin_time_from_state_of_health_file.getFissuresTime();
        int numPoints = seismogramData.number_of_samples;
        if(seismogramData.sample_rate == 0) {
            logger.debug("A sample rate of 0 samples per second was detected.");
            if(props.containsKey(DATASTREAM_TO_SAMPLE_RATE
                    + seismogramData.data_stream_number)) {
                seismogramData.sample_rate = Integer.valueOf(props.getProperty(DATASTREAM_TO_SAMPLE_RATE
                        + seismogramData.data_stream_number))
                        .intValue();
                logger.debug("The sample rate of " + seismogramData.sample_rate
                        + " was found in the props file, and will be used.");
            } else {
                logger.error("The props file does not contain a sample rate for this "
                        + "data stream, and can not be used to correct the problem.");
            }
        }
        SamplingImpl sampling = new SamplingImpl(seismogramData.sample_rate,
                                                 new TimeInterval(1,
                                                                  UnitImpl.SECOND));
        ChannelId channelId;
        if(ncFile == null || props == null) {
            channelId = new ChannelId(MockNetworkId.createNetworkID(),
                                      seismogramData.unitIdNumber,
                                      "" + seismogramData.data_stream_number,
                                      "BH"
                                              + seismogramData.channel_name[seismogramData.channel_number],
                                      mockBeginTimeOfChannel);
        } else {
            this.channel[i] = createChannel(seismogramData, sampling);
            channelId = channel[i].get_id();
        }
        String id = channelId.toString();
        TimeSeriesDataSel timeSeriesDataSel = new TimeSeriesDataSel();
        timeSeriesDataSel.encoded_values(seismogramData.encoded_data);
        MicroSecondDate beginTimeOfSeismogram = LeapSecondApplier.applyLeapSecondCorrection(seismogramData.unitIdNumber,
                                                                                            seismogramData.begin_time_of_seismogram);
        return new LocalSeismogramImpl(id,
                                       beginTimeOfSeismogram.getFissuresTime(),
                                       numPoints,
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       timeSeriesDataSel);
    }

    private Channel createChannel(PacketType seismogramData, Sampling sampling) {
        String stationCode = ncFile.getUnitName(seismogramData.begin_time_from_state_of_health_file,
                                                seismogramData.unitIdNumber);
        String networkIdString = props.getProperty(PopulationProperties.NETWORK_REMAP
                + "XX");
        Time networkBeginTime = ncFile.network_begin_time.getFissuresTime();
        Time channelBeginTime = seismogramData.begin_time_from_state_of_health_file.getFissuresTime();
        NetworkId networkId = PopulationProperties.getNetworkAttr(networkIdString,
                                                                  props)
                .get_id();
        networkId.begin_time = networkBeginTime;
        String tempCode = "B";
        if(seismogramData.sample_rate < 10) {
            tempCode = "L";
        }
        ChannelId channelId = new ChannelId(networkId,
                                            stationCode,
                                            "00",
                                            tempCode
                                                    + "H"
                                                    + seismogramData.channel_name[seismogramData.channel_number],
                                            channelBeginTime);
        TimeRange effectiveNetworkTime = new TimeRange(networkBeginTime,
                                                       TimeUtils.timeUnknown);
        TimeRange effectiveChannelTime = new TimeRange(channelBeginTime,
                                                       TimeUtils.timeUnknown);
        SiteId siteId = new SiteId(networkId,
                                   stationCode,
                                   "00",
                                   channelBeginTime);
        StationId stationId = new StationId(networkId,
                                            stationCode,
                                            channelBeginTime);
        Location location = new Location(0,
                                         0,
                                         new QuantityImpl(0, UnitImpl.METER),
                                         new QuantityImpl(0, UnitImpl.METER),
                                         LocationType.GEOGRAPHIC);
        if(stationLocations.containsKey(stationCode)) {
            location = (Location)stationLocations.get(stationCode);
        } else {
            logger.error("XY file did not contain a location for unit "
                    + stationCode
                    + ".\n"
                    + "The location used for the unit will be the Gulf of Guinea (Atlantic Ocean).");
        }
        NetworkAttrImpl networkAttr = new NetworkAttrImpl(networkId,
                                                          props.getProperty(NETWORK_NAME),
                                                          props.getProperty(NETWORK_DESCRIPTION),
                                                          props.getProperty(NETWORK_OWNER),
                                                          effectiveNetworkTime);
        StationImpl station = new StationImpl(stationId,
                                              stationCode,
                                              location,
                                              effectiveChannelTime,
                                              "",
                                              "",
                                              "",
                                              networkAttr);
        SiteImpl site = new SiteImpl(siteId,
                                     location,
                                     effectiveChannelTime,
                                     station,
                                     "");
        Channel newChannel = new ChannelImpl(channelId,
                                             "",
                                             new Orientation(0, 0),
                                             sampling,
                                             effectiveChannelTime,
                                             site);
        if(channelId.channel_code.endsWith("N")) {
            newChannel.an_orientation = new Orientation(0, 0);
        } else if(channelId.channel_code.endsWith("E")) {
            newChannel.an_orientation = new Orientation(90, 0);
        } else if(channelId.channel_code.endsWith("Z")) {
            newChannel.an_orientation = new Orientation(0, -90);
        }
        return newChannel;
    }

    public Channel[] getChannels() {
        return this.channel;
    }

    private Channel[] channel;

    private final String NETWORK_NAME = "network.name";

    private final String NETWORK_DESCRIPTION = "network.name";

    private final String NETWORK_OWNER = "network.name";

    private final String DATASTREAM_TO_SAMPLE_RATE = "datastream.";

    private NCFile ncFile;

    private Properties props;

    private Map stationLocations;

    private static final Logger logger = Logger.getLogger(RT130ToLocalSeismogram.class);
}
