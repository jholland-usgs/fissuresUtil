package edu.sc.seis.fissuresUtil.display;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;

/**
 * @author groves Created on Nov 4, 2004
 */
public class SimplePlotUtilTest extends TestCase {

    //makes an hour long seismogram with a spike every minute
    public void testMakePlottable() throws CodecException {
        LocalSeismogramImpl seis = makeSeis(START_DATE, ONE_HOUR);
        makeFakePlottAndTest(seis, 1, HALF_SECONDS_IN_HOUR);
    }

    //same as testMakePlottable except the seismogram is missing its first
    // sample which also happens to be a spike
    public void testMakePlottableOnRaggedStart() throws CodecException {
        LocalSeismogramImpl seis = SimplePlotUtil.createRaggedSpike(START_DATE,
                                                                    ONE_HOUR,
                                                                    SSPS * 60,
                                                                    1,
                                                                    SimplePlotUtil.makeChanId(START_TIME));
        makeFakePlottAndTest(seis, 31, HALF_SECONDS_IN_HOUR - 1);
    }

    public void testMakePlottableWithLessThanOnePixel() throws CodecException {
        TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);
        LocalSeismogramImpl seis = makeSeis(START_DATE, ONE_SECOND);
        MicroSecondTimeRange tr = new MicroSecondTimeRange(START_DATE,
                                                           ONE_SECOND);
        Plottable plott = SimplePlotUtil.makePlottable(seis,
                                                       tr,
                                                       HALF_SECONDS_SPD);
        ArrayAssert.assertEquals(new int[] {0, 100}, plott.y_coor);
    }

    public void testMakePlottableInMiddleOfDay() throws CodecException {
        MicroSecondDate middleOfDay = START_DATE.add(new TimeInterval(6,
                                                                      UnitImpl.HOUR));
        LocalSeismogramImpl seis = makeSeis(middleOfDay, ONE_HOUR);
        TimeInterval seven = new TimeInterval(7, UnitImpl.HOUR);
        MicroSecondTimeRange tr = new MicroSecondTimeRange(START_DATE, seven);
        Plottable results = makeFakePlottAndTest(seis,
                                                 1,
                                                 HALF_SECONDS_IN_HOUR,
                                                 tr);
        assertEquals(HALF_SECONDS_SPD / 8, results.x_coor[0]);
    }

    private static LocalSeismogramImpl makeSeis(MicroSecondDate startDate,
                                                TimeInterval length) {
        return SimplePlotUtil.createSpike(startDate,
                                          length,
                                          SSPS * 60,
                                          SimplePlotUtil.makeChanId(START_TIME));
    }

    private static Plottable makeFakePlottAndTest(LocalSeismogramImpl seis,
                                                  int firstSpike,
                                                  int lastSpike)
            throws CodecException {
        return makeFakePlottAndTest(seis, firstSpike, lastSpike, START_DATE);
    }

    private static Plottable makeFakePlottAndTest(LocalSeismogramImpl seis,
                                                  int firstSpike,
                                                  int lastSpike,
                                                  MicroSecondDate startDate)
            throws CodecException {
        return makeFakePlottAndTest(seis,
                                    firstSpike,
                                    lastSpike,
                                    new MicroSecondTimeRange(startDate,
                                                             ONE_HOUR));
    }

    private static Plottable makeFakePlottAndTest(LocalSeismogramImpl seis,
                                                  int firstSpike,
                                                  int lastSpike,
                                                  MicroSecondTimeRange tr)
            throws CodecException {
        int[] yValues = new int[HALF_SECONDS_IN_HOUR];
        for(int i = firstSpike; i < lastSpike; i += 30) {
            yValues[i] = 100;//Spike every minute
        }
        Plottable results = SimplePlotUtil.makePlottable(seis,
                                                         tr,
                                                         HALF_SECONDS_SPD);
        ArrayAssert.assertEquals(yValues, results.y_coor);
        return results;
    }

    private static Time START_TIME = new Time("20000101T000000.000Z", 0);

    private static MicroSecondDate START_DATE = new MicroSecondDate(START_TIME);

    private static TimeInterval ONE_HOUR = new TimeInterval(1, UnitImpl.HOUR);

    private static final int HALF_SECONDS_IN_HOUR = 60 * 60 / 2;

    private static final int HALF_SECONDS_SPD = HALF_SECONDS_IN_HOUR * 24;

    private static int SSPS = SimplePlotUtil.SPIKE_SAMPLES_PER_SECOND;
}