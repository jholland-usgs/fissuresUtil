package edu.sc.seis.fissuresUtil.display;

import java.awt.*;

/**
 * Plotters are objects to be put in the main display of a SeismogramDisplay.  
 * 
 * Created: Fri Jun  7 10:27:49 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version 0.1
 * @see SeismogramPlotter
 */

public interface Plotter {

    public void draw(Graphics2D canvas, Dimension size, TimeSnapshot timeState, AmpSnapshot ampState);

    public void toggleVisibility();

    public void setVisibility(boolean b);
    
}// Plotter
