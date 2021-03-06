package edu.sc.seis.fissuresUtil.rt130;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.RT130Report;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class RT130FileHandler {

    public RT130FileHandler(Properties props,
                            List rt130FileHandlerFlags,
                            RT130Report report) throws FileNotFoundException,
            IOException, ParseException {
        this.report = report;
        pp = new PropParser(props);
        flags = rt130FileHandlerFlags;
        checkFlagsForIncompatibleSettings();
        LeapSecondApplier.addLeapSeconds(pp.getPath(LeapSecondApplier.LEAP_SECOND_FILE));
        LeapSecondApplier.addCorrections(pp.getPath(LeapSecondApplier.POWER_UP_TIMES));
        Map dataStreamToSampleRate = RT130ToLocalSeismogram.makeDataStreamToSampleRate(props,
                                                                                       pp);
        chanCreator = new DASChannelCreator(props);
        toSeismogram = new RT130ToLocalSeismogram(chanCreator,
                                                  dataStreamToSampleRate);
        double nominalLengthOfData = Double.parseDouble(pp.getString("nominalLengthOfData"));
        acceptableLengthOfData = new TimeInterval((nominalLengthOfData + (nominalLengthOfData * 0.05)),
                                                  UnitImpl.MILLISECOND);
    }

    public boolean handle(File f) throws IOException {
        if(flags.contains(RT130FileHandlerFlag.SCAN)) {
            return scan(f);
        } else {
            return read(f);
        }
    }

    public boolean scan(File file) throws IOException {
        if(file.getName().endsWith("00000000")) {
            return read(file);
        }
        String unitIdNumber = getUnitId(file);
        TimeInterval lengthOfData;
        try {
            lengthOfData = FileNameParser.getLengthOfData(file.getName());
        } catch(RT130FormatException e) {
            reportFormatException(file, e);
            return false;
        }
        if(lengthOfData.greaterThan(acceptableLengthOfData)) {
            reportBadName(file,
                          file.getName()
                                  + " indicates more data than in a regular rt130 file. The file will be read to determine its true length.");
            return read(file);
        }
        MicroSecondDate beginTime = getBeginTime(file, unitIdNumber);
        MicroSecondDate nominalEndTime = beginTime.add(acceptableLengthOfData);
        MicroSecondTimeRange fileTimeWindow = new MicroSecondTimeRange(beginTime,
                                                                       nominalEndTime);
        Channel[] channel;
        try {
            channel = chanCreator.create(unitIdNumber,
                                         file.getCanonicalPath(),
                                         fileTimeWindow);
        } catch(RT130FormatError err) {
            reportFormatException(file, err);
            return false;
        }
        MicroSecondDate endTime = beginTime.add(lengthOfData);
        for(int i = 0; i < channel.length; i++) {
            addToReport(channel[i], beginTime, endTime);
        }
        return true;
    }

    private MicroSecondDate getBeginTime(File file, String unitId) {
        MicroSecondDate begin = FileNameParser.getBeginTime(file.getParentFile()
                                                                    .getParentFile()
                                                                    .getParentFile()
                                                                    .getName(),
                                                            file.getName());
        return LeapSecondApplier.applyLeapSecondCorrection(unitId, begin);
    }

    public boolean read(File file) throws IOException {
        String unitIdNumber = getUnitId(file);
        TimeInterval seismogramTime = processAllChannels(file, unitIdNumber);
        if(flags.contains(RT130FileHandlerFlag.FULL)) {
            TimeInterval lengthOfDataFromFileName;
            try {
                lengthOfDataFromFileName = FileNameParser.getLengthOfData(file.getName());
            } catch(RT130FormatException e) {
                reportFormatException(file, e);
                return false;
            }
            if(lengthOfDataFromFileName.value != seismogramTime.value) {
                reportBadName(file,
                              file.getName()
                                      + " seems to be an invalid rt130 file name."
                                      + " The length of data described in the file"
                                      + " name does not match the length of data in the file.");
            }
        }
        return true;
    }

    private void reportFormatException(File file, Exception e)
            throws IOException {
        report.addFileFormatException(file.getCanonicalPath(), e.getMessage());
        logger.error(e.getMessage());
    }

    private void reportBadName(File file, String msg) throws IOException {
        report.addMalformedFileNameException(file.getCanonicalPath(), msg);
        logger.error(msg);
    }

    private String getUnitId(File file) {
        return file.getParentFile().getParentFile().getName();
    }

    private TimeInterval processAllChannels(File file, String unitIdNumber)
            throws IOException {
        String fileName = file.getName();
        String yearAndDay = file.getParentFile()
                .getParentFile()
                .getParentFile()
                .getName();
        MicroSecondDate beginTime = FileNameParser.getBeginTime(yearAndDay,
                                                                fileName);
        beginTime = LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                beginTime);
        MicroSecondDate endTime = beginTime.add(acceptableLengthOfData);
        MicroSecondTimeRange fileTimeWindow = new MicroSecondTimeRange(beginTime,
                                                                       endTime);
        TimeInterval seismogramTime = new TimeInterval(0, UnitImpl.MILLISECOND);
        PacketType[] seismogramDataPacketArray;
        try {
            seismogramDataPacketArray = rtFileReader.processRT130Data(file.getCanonicalPath(),
                                                                      true,
                                                                      fileTimeWindow);
        } catch(RT130FormatException e) {
            reportFormatException(file, e);
            return seismogramTime;
        }
        LocalSeismogramImpl[] seis;
        try {
            seis = toSeismogram.convert(seismogramDataPacketArray);
        } catch(RT130FormatError e) {
            reportFormatException(file, e);
            return seismogramTime;
        }
        Channel[] chans = toSeismogram.getChannels();
        for(int i = 0; i < seis.length; i++) {
            // Add one sample period to end time to pad to the start of the next
            // packet
            MicroSecondDate end = seis[i].getEndTime()
                    .add(seis[i].getSampling().getPeriod());
            for(int j = 0; j < chans.length; j++) {
                if(chans[j].get_id() == seis[i].channel_id) {
                    addToReport(chans[j], seis[i].getBeginTime(), end);
                    break;
                }
            }
            // Get the time from the first seismogram
            if(flags.contains(RT130FileHandlerFlag.FULL)
                    && seismogramTime.value == 0) {
                seismogramTime = new TimeInterval(seis[i].getBeginTime(), end);
            }
        }
        return (TimeInterval)seismogramTime.convertTo(UnitImpl.MILLISECOND);
    }

    private void addToReport(Channel channel,
                             MicroSecondDate beginTime,
                             MicroSecondDate endTime) {
        report.addRefTekSeismogram(channel, beginTime, endTime);
    }

    private void checkFlagsForIncompatibleSettings() {
        if(flags.contains(RT130FileHandlerFlag.SCAN)
                && flags.contains(RT130FileHandlerFlag.FULL)) {
            while(flags.contains(RT130FileHandlerFlag.FULL)) {
                flags.remove(RT130FileHandlerFlag.FULL);
            }
            logger.warn("Both -scan and -full flags were set.");
            logger.warn("Scan processing of RT130 data: ON");
        }
    }

    private DASChannelCreator chanCreator;

    private RT130FileReader rtFileReader = new RT130FileReader();

    private RT130ToLocalSeismogram toSeismogram;

    private List flags;

    private PropParser pp;

    private RT130Report report;

    private static final Logger logger = LoggerFactory.getLogger(RT130FileHandler.class);

    private TimeInterval acceptableLengthOfData;
}
