package edu.sc.seis.fissuresUtil.map;


import com.bbn.openmap.*;

import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import edu.sc.seis.fissuresUtil.chooser.ChannelChooser;
import edu.sc.seis.fissuresUtil.display.EventTableModel;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.layers.ChannelChooserLayer;
import edu.sc.seis.fissuresUtil.map.layers.DistanceLayer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.EventTableLayer;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.fissuresUtil.map.tools.ZoomTool;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.ListSelectionModel;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;

public class OpenMap extends OpenMapComponent {

    public static final Color WATER = new Color(54, 179, 221);
    public static final float DEFAULT_SCALE = 200000000f;

    private MapHandler mapHandler;
    private LayerHandler lh;
    private EventLayer el;
    private StationLayer stl;
    private DistanceLayer dl;
    private MapBean mapBean;
    private MouseDelegator mouseDelegator;
    private List tools = new ArrayList();
    private static Logger logger = Logger.getLogger(OpenMap.class);


    /**Creates a new openmap.  Both the channel chooser and the event table
     * model can be null.  If so, channels and events just won't get drawn
     */
    public OpenMap(String shapefile) {
        try {
            mapHandler = new MapHandler();
            mapHandler.add(this);
            // Create a MapBean
            mapBean = getMapBean();

            //get the projection and set its background color and center point
            //            Proj proj = new Orthographic(new LatLonPoint(mapBean.DEFAULT_CENTER_LAT, mapBean.DEFAULT_CENTER_LON),
            //                                         DEFAULT_SCALE,
            //                                         mapBean.DEFAULT_WIDTH,
            //                                         mapBean.DEFAULT_HEIGHT);
            Proj proj = (Proj)mapBean.getProjection();

            proj.setBackgroundColor(WATER);
            //mapBean.setProjection(proj);

            mapHandler.add(mapBean);

            // Create and add a LayerHandler to the MapHandler.  The
            // LayerHandler manages Layers, whether they are part of the
            // map or not.  layer.setVisible(true) will add it to the map.
            // The LayerHandler has methods to do this, too.  The
            // LayerHandler will find the MapBean in the MapHandler.
            lh = new LayerHandler();
            mapHandler.add(lh);

            GraticuleLayer gl = new GraticuleLayer();
            Properties graticuleLayerProps = gl.getProperties(new Properties());
            graticuleLayerProps.setProperty("10DegreeColor", "FF888888");
            gl.setProperties(graticuleLayerProps);

            gl.setShowRuler(true);

            mapHandler.add(gl);
            lh.addLayer(gl, 0);

            // Create a ShapeLayer to show world political boundaries.
            //ShapeLayer shapeLayer = new ShapeLayer();
            ShapeLayer shapeLayer = new InformativeShapeLayer();

            //Create shape layer properties
            Properties shapeLayerProps = new Properties();
            shapeLayerProps.put("prettyName", "Political Solid");
            shapeLayerProps.put("lineColor", "000000");
            shapeLayerProps.put("fillColor", "39DA87");
            shapeLayerProps.put("shapeFile", shapefile + ".shp");
            shapeLayerProps.put("spatialIndex", shapefile + ".ssx");
            shapeLayer.setProperties(shapeLayerProps);
            shapeLayer.setVisible(true);
            mapHandler.add(shapeLayer);
            lh.addLayer(shapeLayer);

            // Create the directional and zoom control tool
            //OMToolSet omts = new OMToolSet();
            // Create an OpenMap toolbar
            //ToolPanel toolBar = new ToolPanel();
            //Create an OpenMap Info Line
            InformationDelegator infoDel = new InformationDelegator();
            infoDel.setShowLights(false);
            //Create an OpenMap Mouse Delegator
            mouseDelegator = new MouseDelegator();

            // Add the ToolPanel and the OMToolSet to the MapHandler.  The
            // OpenMapFrame will find the ToolPanel and attach it to the
            // top part of its content pane, and the ToolPanel will find
            // the OMToolSet and add it to itself.
            //mapHandler.add(omts);
            //mapHandler.add(toolBar);

            mapHandler.add(mouseDelegator);
            mapHandler.add(infoDel);
        }
        catch (MultipleSoloMapComponentException msmce) {
            // The MapHandler is only allowed to have one of certain
            // items.  These items implement the SoloMapComponent
            // interface.  The MapHandler can have a policy that
            // determines what to do when duplicate instances of the
            // same type of object are added - replace or ignore.

            // In this class, this will never happen, since we are
            // controlling that one MapBean, LayerHandler,
            // MouseDelegator, etc is being added to the MapHandler.
            GlobalExceptionHandler.handle(msmce);
        }
    }

    /**
     * Creates an OpenMap with an EventTableLayer.  Consider this deprecated.
     */
    public OpenMap(EventTableModel etm, ListSelectionModel lsm, String shapefile) {
        this(shapefile);
        setEventLayer(new EventTableLayer(etm, lsm, mapBean));
    }

    /**
     * Deprecated
     */
    public void addStationsFromChannelChooser(ChannelChooser chooser) {
        if(chooser != null) {
            setStationLayer(new ChannelChooserLayer(chooser));
        }
    }

    public void setStationLayer(StationLayer staLayer) {
        stl = staLayer;
        mapHandler.add(stl);
        lh.addLayer(stl,0);
        el.addEQSelectionListener(stl);
        if (el instanceof EventTableLayer) {
            ((EventTableLayer)el).getTableModel().addEventDataListener(stl);
        }
    }

    public StationLayer getStationLayer(){ return stl; }

    public void setEventLayer(EventLayer evl) {
        el = evl;
        mapHandler.add(el);
        lh.addLayer(el,0);

        dl = new DistanceLayer(mapBean);
        el.addEQSelectionListener(dl);
        mapHandler.add(dl);
        lh.addLayer(dl, 1);
        if (el instanceof EventTableLayer) {
            ((EventTableLayer)el).getTableModel().addEventDataListener(dl);
        }
    }

    public EventLayer getEventLayer(){ return el; }

    public Layer[] getLayers() {
        return lh.getLayers();
    }

    public MapBean getMapBean() {
        if (mapBean == null) {
            mapBean = new BufferedMapBean();
        }
        return mapBean;
    }

    public void setZoom(float zoomFactor) {
        mapBean.zoom(new ZoomEvent(this, ZoomEvent.ABSOLUTE, zoomFactor));
    }

    public void addMouseMode(MapMouseMode mode) {
        mouseDelegator.addMouseMode(mode);
        if (mode instanceof ZoomTool) {
            ((ZoomTool)mode).addZoomListener(getMapBean());
        }
    }

    public void setActiveMouseMode(MapMouseMode mode) {
        mouseDelegator.setActiveMouseMode(mode);
    }


    public void writeMapToPNG(String filename) throws IOException {
        synchronized(OpenMap.class) {
            Projection proj = mapBean.getProjection();
            int w = proj.getWidth(), h = proj.getHeight();
            logger.debug(ExceptionReporterUtils.getMemoryUsage()+" before make Buf Image");
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.getGraphics();
            g.setColor(WATER);
            g.fillRect(0,0,w,h);
            Layer[] layers = getLayers();
            for (int i = layers.length - 1; i >= 0; i--){
                layers[i].renderDataForProjection(proj, g);
            }


            File loc = new File(filename);
            File temp = File.createTempFile(loc.getName(), null);
            ImageIO.write(bi, "png", temp);
            loc.delete();
            temp.renameTo(loc);
        }
    }

    private BufferedImage getImage(){
        Object img = imgRef.get();
        if(img == null){
            Projection proj = mapBean.getProjection();
            img = new BufferedImage(proj.getWidth(), proj.getHeight(), BufferedImage.TYPE_INT_RGB);
            imgRef = new SoftReference(img);
        }
        return (BufferedImage)img;
    }

    SoftReference imgRef = new SoftReference(null);

    private class InformativeShapeLayer extends ShapeLayer{
        public void renderDataForProjection(Projection p, Graphics g){
            logger.debug(ExceptionReporterUtils.getMemoryUsage()+" InformativeShapeLayer: rendering shape layer");
            super.renderDataForProjection(p,g);
        }
    }

    public static void main(String[] args) {
        OpenMap om = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");

        try {
            om.writeMapToPNG("map.png");
        } catch (IOException e) {
            logger.error("problem saving image to map.png", e);
        }
        System.out.println("done");
        System.exit(0);
    }
}
