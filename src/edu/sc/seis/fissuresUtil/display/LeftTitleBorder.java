package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.*;

/**
 * LeftTitleBorder.java
 *
 *
 * Created: Fri Oct 22 11:21:22 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class LeftTitleBorder extends javax.swing.border.AbstractBorder {
    
    public LeftTitleBorder(String title) {
        top = 0;
        left = 15;
        right = 0;
        bottom = 0;
        this.title = title;
        isChannelTitle = false;
    }
        
    public LeftTitleBorder(ChannelId channelId) {
        top = 0;
        left = 50;
        right = 0;
        bottom = 0;
        this.chanId = channelId;
        isChannelTitle = true;
    }
    
    public Insets getBorderInsets(Component c) {
        if (title == null || title.equals("")) {
            return new Insets(0,0,0,0);
        } else {
            return new Insets(top, left, bottom, right);
        }
    }

    public Insets getBorderInsets(Component c, Insets i) {
        if (title == null || title.equals("")) {
            i.top = 0;
            i.left = 0;
            i.right = 0;
            i.bottom = 0;
            return new Insets(0,0,0,0);
        } else {
            i.top = top;
            i.left = left;
            i.right = right;
            i.bottom = bottom;
            return new Insets(top, left, bottom, right);
        }
    }

    public void paintBorder(Component c, 
                            Graphics g, 
                            int x, 
                            int y, 
                            int width, 
                            int height) {

        Graphics copy = g.create();
        if (copy != null) {
            try {
                copy.translate(x, y);
                FontMetrics fm = copy.getFontMetrics();

                int vOffset = top+fm.getAscent();
                if (isChannelTitle) {
		    if (4 * fm.getHeight() < height) {
                        copy.drawString(chanId.network_id.network_code,
                                        0,
                                        vOffset);
                        vOffset += fm.getHeight();
                    }
                    if (2 * fm.getHeight() < height) {
                        copy.drawString(chanId.station_code,
                                        0,
                                        vOffset);
                        vOffset += fm.getHeight();
                    }
                    if (3 * fm.getHeight() < height) {
                        copy.drawString(chanId.site_code,
                                        0,
                                        vOffset);
                        vOffset += fm.getHeight();
                    }
                    copy.drawString(chanId.channel_code,
                                    0,
                                    vOffset);
                    vOffset += fm.getHeight();
                } else {
                    int prevNewline = -1;
                    int currNewline = title.indexOf("\n");
                    if (currNewline == -1) currNewline = title.length();
                    while (prevNewline < title.length() - 1) {
                        copy.drawString(title.substring(prevNewline+1,
                                                        prevNewline+2),
                                        5,
                                        vOffset);
                        
                        prevNewline += 1;
                        currNewline = title.indexOf("\n", prevNewline);
                        if (currNewline == -1) currNewline = title.length();
                        vOffset += fm.getHeight();
                    }
                }

            } finally {
                copy.dispose();
            }
        }
    }

    /**
       * Get the value of Title.
       * @return Value of Title.
       */
    public String getTitle() {
        if (isChannelTitle) {
            return title;
        } else {
            return ChannelIdUtil.toStringNoDates(chanId);
        }
    }
    
    /**
       * Set the value of Title.
       * @param v  Value to assign to Title.
       */
    public void setTitle(String  v) {
        this.title = v;
        isChannelTitle = false;
    }

    public void setTitle(ChannelId channelId) {
        chanId = channelId;
        isChannelTitle = true;
    }
    
    protected String title;

    protected int top, left, bottom, right;

    protected ChannelId chanId = null;

    protected boolean isChannelTitle = false;

} // LeftTitleBorder
