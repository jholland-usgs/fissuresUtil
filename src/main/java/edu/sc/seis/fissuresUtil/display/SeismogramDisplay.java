package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.mouse.SDMouseForwarder;
import edu.sc.seis.fissuresUtil.display.mouse.SDMouseMotionForwarder;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.DataSetSeismogramReceptacle;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public abstract class SeismogramDisplay extends BorderedDisplay implements
        DataSetSeismogramReceptacle {

    public SeismogramDisplay() {
        this(mouseForwarder, motionForwarder);
    }

    public SeismogramDisplay(SDMouseForwarder mf, SDMouseMotionForwarder mmf) {
        mouseForwarder = mf;
        motionForwarder = mmf;
        if(mouseForwarder == null || motionForwarder == null) {
            mouseForwarder = new SDMouseForwarder();
            motionForwarder = new SDMouseMotionForwarder();
        }
        add(createCenter(), CENTER);
        colors = COLORS;
    }

    public void add(SeismogramDisplayListener listener) {
        listeners.add(listener);
    }

    public void remove(SeismogramDisplayListener listener) {
        listeners.remove(listener);
    }

    public SeismogramDisplayProvider getCenter() {
        return (SeismogramDisplayProvider)get(CENTER);
    }

    public abstract SeismogramDisplayProvider createCenter();

    public void renderToGraphics(Graphics2D g, Dimension size) {
        PRINTING = true;
        boolean allHere = false;
        long totalWait = 0;
        Iterator seisIt = iterator(DrawableSeismogram.class);
        while(seisIt.hasNext()) {
            DrawableSeismogram cur = (DrawableSeismogram)seisIt.next();
            cur.addToTimeAndAmp();
            if(cur.getDataStatus() == SeismogramContainer.GETTING_DATA) {
                cur.getData();
                allHere = false;
            }
        }
        while(!allHere && totalWait < TWO_MIN) {
            seisIt = iterator(DrawableSeismogram.class);
            allHere = true;
            while(seisIt.hasNext()) {
                DrawableSeismogram cur = (DrawableSeismogram)seisIt.next();
                if(cur.getDataStatus() == SeismogramContainer.GETTING_DATA) {
                    try {
                        Thread.sleep(100);
                        totalWait += 100;
                        if(totalWait % 10000 == 0 && totalWait != 0) {
                            logger.debug("Waiting for data to show before rendering.  We've waited "
                                    + totalWait + " millis");
                        }
                    } catch(InterruptedException e) {}
                    allHere = false;
                }
            }
        }
        logger.debug("Rendering to graphics after waiting " + totalWait
                + " millis for data to arrive");
        if(totalWait >= TWO_MIN) {
            logger.debug("GAVE UP WAITING ON DATA TO RENDER TO GRAPHICS!  SOMEONE IS LYING OR REALLY REALLY SLOW! OR BOTH!!");
        }
        super.renderToGraphics(g, size);
        PRINTING = false;
    }

    private static final long TWO_MIN = 2 * 60 * 1000;

    public Color getColor() {
        return null;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    protected boolean hasConfiguredColors(Class class1) {
        return classToColor.containsKey(class1);
    }

    public void setColors(Class colorGroupClass, Color[] colors) {
        classToColor.put(colorGroupClass, colors);
    }

    public Color getNextColor(Class colorGroupClass) {
        Color[] classColors = colors;
        if(classToColor.containsKey(colorGroupClass)) {
            classColors = (Color[])classToColor.get(colorGroupClass);
        }
        int[] usages = new int[classColors.length];
        for(int i = 0; i < classColors.length; i++) {
            Iterator it = iterator(colorGroupClass);
            while(it.hasNext()) {
                Drawable cur = (Drawable)it.next();
                if(cur.getColor().equals(classColors[i]))
                    usages[i]++;
                if(cur instanceof DrawableSeismogram) {
                    DrawableSeismogram curSeis = (DrawableSeismogram)cur;
                    Iterator childIterator = curSeis.iterator(colorGroupClass);
                    while(childIterator.hasNext()) {
                        Drawable curChild = (Drawable)childIterator.next();
                        if(curChild.getColor().equals(classColors[i]))
                            usages[i]++;
                    }
                }
            }
        }
        for(int minUsage = 0; minUsage >= 0; minUsage++) {
            for(int i = 0; i < usages.length; i++) {
                if(usages[i] == minUsage)
                    return classColors[i];
            }
        }
        return classColors[i++ % classColors.length];
    }

    public void setOutlineColor(Color c) {
        setBorder(BorderFactory.createLineBorder(c));
    }

    public DrawableSeismogram getDrawableSeismogram(DataSetSeismogram ds) {
        DrawableIterator it = iterator(DrawableSeismogram.class);
        while(it.hasNext()) {
            DrawableSeismogram cur = (DrawableSeismogram)it.next();
            if(cur.getSeismogram().equals(ds)) {
                return cur;
            }
        }
        throw new IllegalArgumentException("The passed in data set seismgoram must have a drawable seismogram using it in this display");
    }

    public void outputToPDF(String filename) throws IOException {
        outputToPDF(new File(filename));
    }

    public void outputToPDF(File f) throws IOException {
        f.getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        outputToPDF(bos);
        bos.close();
    }

    public void outputToPDF(File f, TitleBorder header)
            throws IOException {
        f.getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        outputToPDF(bos,
                    true,
                    true,
                    header);
        bos.close();
    }

    public void outputToPDF(OutputStream os) {
        outputToPDF(os, true);
    }

    public void outputToPDF(OutputStream os, boolean landscape) {
        outputToPDF(os, landscape, true);
    }

    public void outputToPDF(OutputStream os, boolean landscape, boolean separate) {
        outputToPDF(os, landscape, separate, null);
    }

    public void outputToPDF(OutputStream os,
                            boolean landscape,
                            boolean separate,
                            TitleBorder header) {
        setPDF(true);
        SeismogramPDFBuilder builder = new SeismogramPDFBuilder(landscape,
                                                                pdfSeismogramsPerPage,
                                                                separate);
        builder.setHeader(header);
        builder.createPDF(this, os);
        setPDF(false);
    }

    private int pdfSeismogramsPerPage = 1;

    private int i = 0;

    public abstract void add(Drawable drawable);

    public abstract void remove(Drawable drawable);

    public abstract DrawableIterator getDrawables(MouseEvent e);

    public abstract DrawableIterator iterator(Class drawableClass);

    public abstract void setTimeConfig(TimeConfig timeConfig);

    public abstract TimeConfig getTimeConfig();

    public abstract void setAmpConfig(AmpConfig ampConfig);

    public abstract void setGlobalizedAmpConfig(AmpConfig ampConfig);

    public abstract void setIndividualizedAmpConfig(AmpConfig ampConfig);

    public abstract AmpConfig getAmpConfig();

    public abstract DataSetSeismogram[] getSeismograms();

    public abstract void print();

    public void remove(Selection selection) {}

    public static void setMouseMotionForwarder(SDMouseMotionForwarder mf) {
        motionForwarder = mf;
    }

    public static SDMouseMotionForwarder getMouseMotionForwarder() {
        return motionForwarder;
    }

    public static void setMouseForwarder(SDMouseForwarder mf) {
        mouseForwarder = mf;
    }

    public static SDMouseForwarder getMouseForwarder() {
        return mouseForwarder;
    }

    public static Set getActiveFilters() {
        return activeFilters;
    }

    public static void setCurrentTimeFlag(boolean visible) {
        currentTimeFlag = visible;
    }

    public static boolean getCurrentTimeFlag() {
        return currentTimeFlag;
    }

    public int getPdfSeismogramsPerPage() {
        return pdfSeismogramsPerPage;
    }

    public void setPdfSeismogramsPerPage(int pdfSeismogramsPerPage) {
        this.pdfSeismogramsPerPage = pdfSeismogramsPerPage;
    }

    public boolean isPDF() {
        return isPDF;
    }

    public void setPDF(boolean isPDF) {
        this.isPDF = isPDF;
    }

    private static SDMouseMotionForwarder motionForwarder;

    private static SDMouseForwarder mouseForwarder;

    private List listeners = new ArrayList();

    private static boolean currentTimeFlag = false;

    protected static Set activeFilters = new HashSet();

    private Color[] colors;

    protected boolean drawNamesForNamedDrawables = true;

    public static final Color[] COLORS = {Color.BLUE,
                                          new Color(217, 91, 23),
                                          new Color(179, 182, 46),
                                          new Color(141, 18, 69),
                                          new Color(65, 200, 115),
                                          new Color(27, 36, 138),
                                          new Color(130, 145, 230),
                                          new Color(54, 72, 21),
                                          new Color(119, 17, 136)};

    private Map classToColor = new HashMap();

    private static final Logger logger = LoggerFactory.getLogger(SeismogramDisplay.class);

    public static boolean PRINTING = false;

    protected boolean isPDF = false;

    public void setDrawNamesForNamedDrawables(boolean drawNamesForNamedDrawables) {
        this.drawNamesForNamedDrawables = drawNamesForNamedDrawables;
    }
}
