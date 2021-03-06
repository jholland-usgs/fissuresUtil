package edu.sc.seis.fissuresUtil.time;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;

public class ReduceToolTest extends TestCase {

    public void testDoubleRequest() {
        assertEquals(1,
                     ReduceTool.merge(new RequestFilter[] {fullRequest,
                                                           fullRequest}).length);
    }

    public void testDifferentChannelsWithOverlappingTimesAreLeftAloneByMinimizeRequest() {
        RequestFilter[] fullAndOther = new RequestFilter[] {fullRequest,
                                                            fullForOther};
        assertEquals(2, ReduceTool.merge(fullAndOther).length);
    }

    public void testContiguousSeismograms() {
        assertEquals(1, ReduceTool.merge(createContiguous()).length);
    }

    public static LocalSeismogramImpl[] createContiguous() {
        LocalSeismogramImpl first = MockSeismogram.createSpike();
        return new LocalSeismogramImpl[] {first, createContiguous(first)};
    }

    public static LocalSeismogramImpl[] createEqual() {
        return new LocalSeismogramImpl[] {MockSeismogram.createSpike(start),
                                          MockSeismogram.createSpike(start)};
    }

    public static LocalSeismogramImpl[] createOverlapping() {
        LocalSeismogramImpl first = MockSeismogram.createSpike(start);
        return new LocalSeismogramImpl[] {first,
                                          MockSeismogram.createSpike(first.getBeginTime()
                                                  .add((TimeInterval)first.getTimeInterval()
                                                          .divideBy(2)))};
    }

    private static LocalSeismogramImpl createContiguous(LocalSeismogramImpl first) {
        return MockSeismogram.createSpike(first.getEndTime()
                .add(first.getSampling().getPeriod()));
    }

    public void testEqualSeismograms() {
        LocalSeismogramImpl[] input = createEqual();
        LocalSeismogramImpl[] result = ReduceTool.merge(input);
        assertEquals(1, result.length);
        assertEquals(input[0].getBeginTime(), result[0].getBeginTime());
        assertEquals(input[0].getEndTime(), result[0].getEndTime());
        assertEquals(input[0].getNumPoints(), result[0].getNumPoints());
    }

    public void testOverlappingSeismograms() {
        assertEquals(2, ReduceTool.merge(createOverlapping()).length);
    }

    public void testContiguousEqualAndOverlappingSeismograms() {
        LocalSeismogramImpl[] overlapping = createOverlapping();
        assertEquals(2,
                     ReduceTool.merge(new LocalSeismogramImpl[] {overlapping[0],
                                                                 overlapping[1],
                                                                 MockSeismogram.createSpike(start),
                                                                 createContiguous(overlapping[0])}).length);
    }

    public void testOverlappingMSTR() {
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[3]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[2])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[3])}).length);
    }

    public void testContainedMSTR() {
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[3])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[3]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[2])}).length);
    }

    public void testSplitMSTR() {
        assertEquals(2,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[1]),
                                                                  new MicroSecondTimeRange(dates[2],
                                                                                           dates[3])}).length);
        assertEquals(2,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[2],
                                                                                           dates[3]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[1])}).length);
    }

    public void testTouchingEndBeginMSTR() {
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[1]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[2])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[1])}).length);
    }

    public void testZeroLengthMSTR() {
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[1]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[1])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[1])}).length);
    }

    static MicroSecondDate[] dates = new MicroSecondDate[4];
    static {
        BasicConfigurator.configure(new NullAppender());
        for(int i = 0; i < dates.length; i++) {
            dates[i] = new MicroSecondDate(new Date(i));
        }
    }

    static MicroSecondDate start = new MicroSecondDate();

    static MicroSecondDate end = start.add(new TimeInterval(1, UnitImpl.HOUR));

    RequestFilter fullRequest = new RequestFilter(MockChannel.createChannel()
            .get_id(), start.getFissuresTime(), end.getFissuresTime());

    RequestFilter fullForOther = new RequestFilter(MockChannel.createNorthChannel()
                                                           .get_id(),
                                                   start.getFissuresTime(),
                                                   end.getFissuresTime());
}
