package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.freq.Cmplx;


/**
 * See http://www.mers.byu.edu/docs/reports/MERS9505.pdf for info on the hilbert transform.
 * 
 * @author crotwell
 * Created on Apr 25, 2005
 */
public class Hilbert implements LocalSeismogramFunction {

    public Hilbert() {
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws FissuresException {
        Cmplx[] c = Cmplx.fft(seis.get_as_floats());
        for(int i = 0; i < c.length/2; i++) {
            double tmp = c[i].i;
            c[i].i = c[i].r;
            c[i].r = -tmp;
        }
        for(int i = c.length/2; i < c.length; i++) {
            double tmp = c[i].i;
            c[i].i = -c[i].r;
            c[i].r = tmp;
        }
        return new LocalSeismogramImpl(seis, Cmplx.fftInverse(c, seis.getNumPoints()));
    }
    
    public Cmplx[] analyticSignal(LocalSeismogramImpl seis) throws FissuresException {
        float[] seisData = seis.get_as_floats();
        LocalSeismogramImpl hilbert = apply(seis);
        float[] hilbertData = hilbert.get_as_floats();
        Cmplx[] out = new Cmplx[seis.getNumPoints()];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Cmplx(seisData[i], hilbertData[i]);
        }
        return out;
    }
}
