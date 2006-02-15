package edu.sc.seis.fissuresUtil.database.plottable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class JDBCPlottable extends JDBCTable {

    public JDBCPlottable(Connection conn, String dbType) throws SQLException {
        super("plottable", conn);
        chanTable = new JDBCChannel(conn);
        Context ctx = new VelocityContext();
        ctx.put(dbType, "true");
        TableSetup.setup(getTableName(),
                         conn,
                         this,
                         "edu/sc/seis/fissuresUtil/database/props/plottable/default.props",
                         ctx);
    }

    public void put(PlottableChunk[] chunks) throws SQLException, IOException {
        MicroSecondTimeRange stuffInDB = RangeTool.getFullTime(chunks);
        logger.debug("stuffInDB timeRange: " + stuffInDB);
        MicroSecondDate startTime = PlottableChunk.stripToDay(stuffInDB.getBeginTime());
        logger.debug("start time of chunks: " + startTime);
        MicroSecondDate strippedEnd = PlottableChunk.stripToDay(stuffInDB.getEndTime());
        logger.debug("end time of chunks: " + strippedEnd);
        if(!strippedEnd.equals(stuffInDB.getEndTime())) {
            logger.debug("!strippedEnd.equals(stuffInDB.getEndTime())");
            strippedEnd = strippedEnd.add(PlottableChunk.ONE_DAY);
            logger.debug("strippedEnd now: " + strippedEnd);
        }
        stuffInDB = new MicroSecondTimeRange(startTime, strippedEnd);
        PlottableChunk[] dbChunks = get(stuffInDB,
                                        chunks[0].getChannel(),
                                        chunks[0].getPixelsPerDay());
        logger.debug("got " + dbChunks.length
                + " chunks from stuff that was already in the database");
        PlottableChunk[] everything = new PlottableChunk[chunks.length
                + dbChunks.length];
        System.arraycopy(dbChunks, 0, everything, 0, dbChunks.length);
        System.arraycopy(chunks, 0, everything, dbChunks.length, chunks.length);
        logger.debug("Merging " + everything.length + " chunks");
        for(int i = 0; i < everything.length; i++) {
            logger.debug(everything[i]);
        }
        everything = ReduceTool.merge(everything);
        logger.debug("Breaking "
                + everything.length
                + " remaining chunks after merge into seperate chunks based on day");
        for(int i = 0; i < everything.length; i++) {
            logger.debug(everything[i]);
        }
        everything = breakIntoDays(everything);
        logger.debug("Adding " + everything.length + " chunks split on days");
        for(int i = 0; i < everything.length; i++) {
            logger.debug(everything[i]);
        }
        int rowsDropped = drop(stuffInDB,
                               chunks[0].getChannel(),
                               chunks[0].getPixelsPerDay());
        logger.debug("Dropped " + rowsDropped
                + " rows of stuff that new data covered");
        for(int i = 0; i < everything.length; i++) {
            logger.debug("Adding chunk " + i + ": " + everything[i]);
            int stmtIndex = 1;
            PlottableChunk chunk = everything[i];
            synchronized(put) {
                try {
                    put.setInt(stmtIndex++, chanTable.put(chunk.getChannel()));
                    put.setInt(stmtIndex++, chunk.getPixelsPerDay());
                    put.setTimestamp(stmtIndex++, chunk.getBeginTime()
                            .getTimestamp());
                    put.setTimestamp(stmtIndex++, chunk.getEndTime()
                            .getTimestamp());
                    int[] y = chunk.getData().y_coor;
                    put.setInt(stmtIndex++, y.length / 2);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    for(int k = 0; k < y.length; k++) {
                        dos.writeInt(y[k]);
                    }
                    put.setBytes(stmtIndex++, out.toByteArray());
                    put.executeUpdate();
                } catch(SQLException ex) {
                    logger.warn("problem with sql query: " + put);
                    throw ex;
                }
            }
        }
    }

    private PlottableChunk[] breakIntoDays(PlottableChunk[] everything) {
        List results = new ArrayList();
        for(int i = 0; i < everything.length; i++) {
            PlottableChunk[] days = everything[i].breakIntoDays();
            for(int j = 0; j < days.length; j++) {
                results.add(days[j]);
            }
        }
        return (PlottableChunk[])results.toArray(new PlottableChunk[0]);
    }

    private static int getPixels(int pixelsPerDay, MicroSecondTimeRange tr) {
        TimeInterval inter = tr.getInterval();
        inter = (TimeInterval)inter.convertTo(UnitImpl.DAY);
        double samples = pixelsPerDay * inter.getValue();
        return (int)Math.floor(samples);
    }

    public static int[] fill(MicroSecondTimeRange fullRange,
                             int[] y,
                             PlottableChunk chunk) {
        MicroSecondDate rowBeginTime = chunk.getBeginTime();
        int offsetIntoRequestSamples = SimplePlotUtil.getPixel(y.length,
                                                               fullRange,
                                                               rowBeginTime);
        int[] dataY = chunk.getData().y_coor;
        int numSamples = dataY.length;
        int firstSampleForRequest = 0;
        if(offsetIntoRequestSamples < 0) {
            firstSampleForRequest = -1 * offsetIntoRequestSamples;
        }
        int lastSampleForRequest = numSamples;
        if(offsetIntoRequestSamples + numSamples > y.length) {
            lastSampleForRequest = y.length - offsetIntoRequestSamples;
        }
        for(int i = firstSampleForRequest; i < lastSampleForRequest; i++) {
            y[i + offsetIntoRequestSamples] = dataY[i];
        }
        return y;
    }

    public int drop(MicroSecondTimeRange requestRange,
                    ChannelId id,
                    int samplesPerDay) throws SQLException {
        int chanDbId;
        try {
            chanDbId = chanTable.getDBId(id);
        } catch(NotFound e) {
            logger.info("Channel " + ChannelIdUtil.toStringNoDates(id)
                    + " not found");
            return 0;
        }
        synchronized(drop) {
            drop.setTimestamp(1, requestRange.getEndTime().getTimestamp());
            drop.setTimestamp(2, requestRange.getBeginTime().getTimestamp());
            drop.setInt(3, chanDbId);
            drop.setDouble(4, samplesPerDay);
            return drop.executeUpdate();
        }
    }

    public PlottableChunk[] get(MicroSecondTimeRange requestRange,
                                ChannelId id,
                                int pixelsPerDay) throws SQLException,
            IOException {
        int chanDbId;
        try {
            chanDbId = chanTable.getDBId(id);
        } catch(NotFound e) {
            logger.info("Channel " + ChannelIdUtil.toStringNoDates(id)
                    + " not found");
            return new PlottableChunk[0];
        }
        int index = 1;
        ResultSet rs;
        synchronized(get) {
            get.setTimestamp(index++, requestRange.getEndTime().getTimestamp());
            get.setTimestamp(index++, requestRange.getBeginTime()
                    .getTimestamp());
            get.setInt(index++, chanDbId);
            get.setInt(index++, pixelsPerDay);
            rs = get.executeQuery();
        }
        List chunks = new ArrayList();
        int requestPixels = getPixels(pixelsPerDay, requestRange);
        logger.debug("Request made for " + requestPixels + " from "
                + requestRange + " at " + pixelsPerDay + "ppd");
        while(rs.next()) {
            Timestamp ts = rs.getTimestamp("start_time");
            MicroSecondDate rowBeginTime = new MicroSecondDate(ts);
            logger.debug("rowBeginTime: " + rowBeginTime);
            int offsetIntoRequestPixels = SimplePlotUtil.getPixel(requestPixels,
                                                                  requestRange,
                                                                  rowBeginTime);
            logger.debug("offetIntoRequestPixels: " + offsetIntoRequestPixels);
            int numPixels = rs.getInt("pixel_count");
            logger.debug("numPixels: " + numPixels);
            int firstPixelForRequest = 0;
            if(offsetIntoRequestPixels < 0) {
                // This db row has data starting before the request, start at
                // pertinent point
                firstPixelForRequest = -1 * offsetIntoRequestPixels;
            }
            logger.debug("firstPixelForRequest: " + firstPixelForRequest);
            int lastPixelForRequest = numPixels;
            if(offsetIntoRequestPixels + numPixels > requestPixels) {
                // This row has more data than was requested in it, only get
                // enough to fill the request
                lastPixelForRequest = requestPixels - offsetIntoRequestPixels;
            }
            logger.debug("lastPixleForRequest: " + lastPixelForRequest);
            int pixelsUsed = lastPixelForRequest - firstPixelForRequest;
            logger.debug("pixelsUsed: " + pixelsUsed);
            int[] x = new int[pixelsUsed * 2];
            int[] y = new int[pixelsUsed * 2];
            byte[] dataBytes = rs.getBytes("data");
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));
            for(int i = 0; i < firstPixelForRequest; i++) {
                dis.readInt();
                dis.readInt();
            }
            for(int i = 0; i < pixelsUsed * 2; i++) {
                // x[i] = firstPixelForRequest + i / 2;
                x[i] = firstPixelForRequest + offsetIntoRequestPixels + i / 2;
                y[i] = dis.readInt();
            }
            if(x.length > 0) {
                logger.debug("x[0]: " + x[0]);
            } else {
                logger.debug("ZERO LENGTH ARRAY!!!");
            }
            Plottable p = new Plottable(x, y);
            PlottableChunk pc = new PlottableChunk(p,
                                                   PlottableChunk.getPixel(rowBeginTime,
                                                                           pixelsPerDay)
                                                           + firstPixelForRequest,
                                                   PlottableChunk.getJDay(rowBeginTime),
                                                   PlottableChunk.getYear(rowBeginTime),
                                                   pixelsPerDay,
                                                   id);
            chunks.add(pc);
            logger.debug("Returning " + pc + " from chunk starting at "
                    + rowBeginTime);
        }
        return (PlottableChunk[])chunks.toArray(new PlottableChunk[chunks.size()]);
    }

    private PreparedStatement put, get, drop;

    private JDBCChannel chanTable;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCPlottable.class);
}