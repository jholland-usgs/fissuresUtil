/**
 * FreshnessEventColorizer.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.colorizer.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import java.awt.Color;
import java.awt.Paint;

public class FreshnessEventColorizer implements EventColorizer{

    public static Color FRESH_EVENT = DisplayUtils.EVENT_RED;
    public static Color OLD_EVENT = DisplayUtils.EVENT_ORANGE;
    public static Color REALLY_OLD_EVENT = DisplayUtils.EVENT_YELLOW;

    public Paint[] colorize(EventAccessOperations[] events) {
        Paint[] paints = new Color[events.length];
        for (int i = 0; i < events.length; i++) {
            if (i <= events.length - 11){
                paints[i] = REALLY_OLD_EVENT;
            }
            else if (i <= events.length - 6){
                paints[i] = OLD_EVENT;
            }
            else if (i <= events.length - 1){
                paints[i] = FRESH_EVENT;
            }
        }
        return paints;
    }

}

