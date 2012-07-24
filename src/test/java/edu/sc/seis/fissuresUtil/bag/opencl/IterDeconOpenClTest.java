package edu.sc.seis.fissuresUtil.bag.opencl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import edu.sc.seis.fissuresUtil.bag.IterDecon;
import edu.sc.seis.fissuresUtil.bag.IterDeconResult;
import edu.sc.seis.fissuresUtil.bag.opencl.IterDeconOpenCl;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

import org.junit.*;

import static org.junit.Assert.*;


public class IterDeconOpenClTest {
    
    static {
        System.setProperty("javacl.debug", "true");
    }
    
    @Test
    public void testFFT() throws Exception {
        int n = 1024;
        float dt = 0.1f;
        int indexA = 4;
        int indexB = 6;
        int indexC = 154;
        float[] inData = new float[n];
        inData[indexA] = 23;
        inData[indexB] = -10;
        inData[indexC] = 14;
        float[][] outData = computeFFT(inData);
        assertArrayEquals(outData[0], outData[1], 0.001f);
    }
    
    @Test
    public void testForwardFFT() throws Exception {
        int n = 1024;
        float delta = 0.05f;
        float[] inData = new float[n];
        inData[100] = 1 / delta; // mimic input to sac for testing gaussian filter
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        CLBuffer<Float> gaussVals = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        System.out.println("before gaussian");
        FloatArrayResult out = openCL.forwardFFT(inCLBuffer, new CLEvent[0]);
        float[] outData = out.getAfterWait(openCL.queue);
        
        float[] iterDeconFFT = IterDecon.forwardFFT(inData);
        
        assertArrayEquals("forward fft", iterDeconFFT, outData, 0.001f);
    }
    

    @Test
    public void testInverseFFT() throws Exception {
        int n = 1024;
        float delta = 0.05f;
        float[] inData = new float[n];
        inData[100] = 1 / delta; // mimic input to sac for testing gaussian filter
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        CLBuffer<Float> gaussVals = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        System.out.println("before gaussian");
        FloatArrayResult out = openCL.forwardFFT(inCLBuffer, new CLEvent[0]);
        float[] outData = out.getAfterWait(openCL.queue);
        FloatArrayResult openClInverseFFT = openCL.inverseFFT(out.getResult(), out.getEventsToWaitFor());
        float[] openclInverseFFTData = openClInverseFFT.getAfterWait(openCL.queue);
        
        float[] iterDeconFFT = IterDecon.inverseFFT(outData);
        
        assertArrayEquals("inverse fft", iterDeconFFT, openclInverseFFTData, 0.001f);
    }
    
    public float[][] computeFFT(float[] inData) throws Exception {
    int n = inData.length;
        float[] inDataCmplx = new float[2*n];
        for (int i = 0; i < inData.length; i++) {
            inDataCmplx[2*i] = inData[i];
            inDataCmplx[2*i+1] = 0;
        }
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inDataCmplx);
        CLBuffer<Float> outClBuffer = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, inDataCmplx.length);

        CLEvent fftEvent = openCL.fft.transform(openCL.queue, inCLBuffer, outClBuffer, false);
        
        // shorten to oregonDSP order in opencl
        CLBuffer<Float> shortenFFTVals = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        openCL.shortenFFT.setArgs(outClBuffer, shortenFFTVals, n);
        CLEvent shortenFFTEvent = openCL.shortenFFT.enqueueNDRange(openCL.queue, new int[] {n}, fftEvent);
        FloatArrayResult shortenFFTResult = new FloatArrayResult(shortenFFTVals, shortenFFTEvent);
        float[] oregonDSPOrder = shortenFFTResult.getAfterWait(openCL.queue);
        
        // shorten to oregonDSP order in java
        //Pointer<Float> outPtr = outClBuffer.read(openCL.queue, fftEvent); // blocks until  finished
        //float[] oregonDSPOrder = new float[n];
        // real
        //for (int i = 0; i < oregonDSPOrder.length/2; i++) {
        //    oregonDSPOrder[i] = outPtr.get(2*i);
        //}
        //nyquist
        //oregonDSPOrder[oregonDSPOrder.length/2] = outPtr.get(inDataCmplx.length/2);
        // imaginary
        //for (int i = 1; i < oregonDSPOrder.length/2; i++) {
        //    oregonDSPOrder[oregonDSPOrder.length-i] = -1*outPtr.get(2*i+1);
        //}
        
        IterDecon iterDecon  = new IterDecon(1, true, 1, 1);
        IterDecon.useOregonDSPFFT = true;
        float[] forward = IterDecon.forwardFFT(inData);
        
        // Print the first 10 output values :
       // for (int i = 0; i < 10 && i < n && i < outClBuffer.getElementCount(); i++)
       //     System.out.println("fft out[" + i + "] = " + oregonDSPOrder[i]+"   "+forward[i]+"        "+outPtr.get(i));
        return new float[][] { forward, oregonDSPOrder };
    }
    

    @Test
    public void testRoundTripFFT() throws IOException {
    
        int n = 1024;
        float gwidth = 2.5f;
        float dt = 0.1f;
        float[] inData = new float[n];
        inData[100] = 1;
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        System.out.println("After init opencl");
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        System.out.println("before fft ");
        FloatArrayResult forwardFFT = openCL.forwardFFT(inCLBuffer);
        
        FloatArrayResult inverse = openCL.inverseFFT(forwardFFT.getResult(), forwardFFT.getEventsToWaitFor());
        System.out.println("before read "+forwardFFT.getEventsToWaitFor()[0].getCommandExecutionStatus()+"  "+inverse.getEventsToWaitFor()[0].getCommandExecutionStatus());
        System.out.print("Inverse events: ");
        for (int i = 0; i < inverse.getEventsToWaitFor().length; i++) {
            System.out.print("  "+inverse.getEventsToWaitFor()[i].getCommandExecutionStatus());
        }
        System.out.println();
        float[] inverseFlts = inverse.getAfterWait(openCL.queue);
        assertArrayEquals(inData, inverseFlts, 0.001f);
    }


    @Test
    public void testGaussianMulFactor() {
        int n = 1024;
        float gwidthFactor = 2.5f;
        float dt = .1f;
        for(int i=1; i<n; i++) {
        assertEquals(" "+i,
                     calcGaussianNative(i, gwidthFactor, dt, n),
                     calcGaussianOpenCL(i, gwidthFactor, dt, n),
                     0.001);
        }
    }
    
    float calcGaussianOpenCL(int i,
                             float gwidthFactor,
                             float dt,
                             int n) {
        float df = 1/(n * dt);
        //float gwidth = 4*gwidthFactor*gwidthFactor;
        float omega;
        float gauss;
        if (i == 0) {
            gauss = 1;
        } else {
            omega = (float)(i*2*Math.PI*df);
            gauss = (float)Math.exp(-omega*omega / (4*gwidthFactor*gwidthFactor));
        }
        return gauss;
    }
    
    float calcGaussianNative(int i,
                             float gwidthFactor,
                             float dt,
                             int n) {
        float df = 1/(n * dt);
        float d_omega = (float)(2*Math.PI*df);
        float gwidth = 4*gwidthFactor*gwidthFactor;
        float gauss;
        float omega;

        if (i==0) {
        // Handle the nyquist frequency
        omega = (float)(Math.PI/dt); // eliminate 2 / 2
        gauss = (float)(Math.exp(-omega*omega / gwidth));
        return gauss;
        }

        int j;
            j  = i*2;
            omega = i*d_omega;
            gauss = (float)(Math.exp(-omega*omega / gwidth));
        return gauss;
    }
    

    public static float[] calcGaussianFilter(float[] x,
                                         float gwidthFactor,
                                         float dt) {
        // gwidthFactor of zero means no filter
        if (gwidthFactor == 0) {
            return x;
        }
        float[] forward = new float[2*x.length];
        for (int i = 0; i < forward.length; i++) {
            forward[i] = 1;
        }
        float df = 1/(forward.length * dt);
        float d_omega = 2*(float)Math.PI*df;
        float gwidth = 4*gwidthFactor*gwidthFactor;
        float gauss;
        float omega;

        forward[0] = 1;
        forward[1] = 1;

        int j;
        for (int i=1; i<forward.length/2; i++) {
            j  = i*2;
            omega = i*d_omega;
            gauss = (float)Math.exp(-omega*omega / gwidth);
                forward[j] *= gauss;
                forward[j+1] *= gauss;
        }
        return forward;
    }
    
    @Test
    public void testGaussianDataFFT() throws Exception {
        int n = 16;
        float gwidth = 2.5f;
        float dt = 0.1f;
        float[] inData = new float[n];
        inData[0] = 1;
        float[][] outData = computeFFT(inData);
        assertArrayEquals(outData[0], outData[1], 0.001f);
    }
    
    @Test
    public void testGaussian() throws IOException {
        int n = 16;
        float gwidth = 2.5f;
        float dt = 0.1f;
        float[] inData = new float[n];
        inData[0] = 1;
        
        System.out.println("testGaussian length="+n);
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        System.out.println("After init opencl");
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        CLBuffer<Float> gaussVals = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);

        System.out.println("before gaussian filter");
        FloatArrayResult gauss = openCL.gaussianFilter(inCLBuffer, gwidth, dt, gaussVals);
        System.out.println("before read");
        float[] gaussFlts = gauss.getAfterWait(openCL.queue);
        float[] gaussValsFlts = gaussVals.read(openCL.queue, gauss.getEventsToWaitFor()).getFloats();
        
        float[] nativeGaussVals = new float[n];
        IterDecon iterDecon  = new IterDecon(1, true, 1, 1);
        IterDecon.useOregonDSPFFT = true;
        float[] gaussCPU = IterDecon.gaussianFilter(inData, gwidth, dt, nativeGaussVals);
        assertEquals("gauss val length", nativeGaussVals.length, gaussValsFlts.length);
     // Print the first 10 output values :
        for (int i = 0; i < n && i < gaussValsFlts.length && i < gaussValsFlts.length; i++)
            System.out.println("gaussValsFlts[" + i + "] = " + gaussValsFlts[i]+"   "+nativeGaussVals[i]);
     // Print the first 10 output values :
        for (int i = 0; i < 10 && i < gaussCPU.length && i < gaussFlts.length; i++)
            System.out.println("out[" + i + "] = " + gaussFlts[i]+"   "+gaussCPU[i]+"  ratio " + gaussFlts[i]/gaussCPU[i]);
        assertArrayEquals("gauss mul factor", nativeGaussVals, gaussValsFlts, 0.001f);
        assertArrayEquals("gaussian filter result", gaussCPU, gaussFlts, 0.001f);
    }
    
    float[] createFakeFFTData(int n) {
        float[] inData = new float[2*n];
        inData[0] = 1;
        inData[1] = 0;
        inData[inData.length/2] = n/2;
        inData[inData.length/2+1] = 0;
        for (int i = 1; i < inData.length/4; i++) {
            inData[2*i] = i;
            inData[2*i+1] = -1*i-0.1f;
            inData[inData.length-2*i] = i;
            inData[inData.length-2*i+1] = i+0.1f;
        }
        return inData;
    }
    
    @Test
    public void testShortenFFT() throws IOException {
        int n = 16;
        float[] inData = createFakeFFTData(n);
        assertEquals("fake data len", n*2, inData.length);
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        System.out.println("After init opencl");
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        CLBuffer<Float> shortenFFTVals = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        openCL.shortenFFT.setArgs(inCLBuffer, shortenFFTVals, n);
        CLEvent shortenFFTEvent = openCL.shortenFFT.enqueueNDRange(openCL.queue, new int[] {n});
        FloatArrayResult shortenFFTResult = new FloatArrayResult(shortenFFTVals, shortenFFTEvent);
        float[] outData = shortenFFTResult.getAfterWait(openCL.queue);
        System.out.println("test shorten fft: "+inData.length+" to "+n);
        for (int i = 0; i < inData.length; i++) {
            System.out.print(inData[i]+" ");
        }
        System.out.println();
        for (int i = 0; i < outData.length; i++) {
            System.out.print(outData[i]+"     ");
        }
        System.out.println();
        assertEquals("array length", n, outData.length);
        assertEquals("real "+0, inData[0], outData[0], 0.0000001f);
        for (int i = 1; i < n/2; i++) {
            assertEquals("real "+i, inData[2*i], outData[i], 0.0000001f);
            assertEquals("imag "+i, inData[2*i+1], -1*outData[n-i], 0.000001f);
        }
        assertEquals("last real "+n/2, inData[n], outData[n/2], 0.000001f);
    }
    
    @Test
    public void testLengthenFFT() throws Exception {
        int n = 16;
        float[] inData = createFakeFFTData(n);
        assertEquals("fake data len", n*2, inData.length);
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        System.out.println("After init opencl");
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        
        FloatArrayResult shortData = openCL.shortenFFT(inCLBuffer);
        assertEquals("shorten len", n, shortData.getAfterWait(openCL.queue).length);
        System.out.println("test lengthen n="+n+"  short="+shortData.getAfterWait(openCL.queue).length);
        CLBuffer<Float> shortCLBuffer = shortData.getResult();
        long testn = shortCLBuffer.getElementCount();
        System.out.println("IterDeconOpenCL before lengthenFFT length: "+testn+" ");
        FloatArrayResult lengthenData = openCL.lengthenFFT(shortData.getResult(), shortData.getEventsToWaitFor());
        assertEquals("lengthen len", 2*n, lengthenData.getAfterWait(openCL.queue).length);
        
        float[] outData = lengthenData.getAfterWait(openCL.queue);
        

        for (int i = 0; i < inData.length; i++) {
            System.out.print(inData[i]+" ");
        }
        System.out.println();
        for (int i = 0; i < outData.length; i++) {
            System.out.print(outData[i]+" ");
        }
        System.out.println();
        
        assertArrayEquals(inData, outData, 0.00001f );
    }

    @Test
    public void testGaussianFilter() throws Exception {
         
        SacTimeSeries sac = new SacTimeSeries();
        DataInputStream in = new DataInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/gauss1024.sac"));
        sac.read(in);
        System.out.println("sac npts "+sac.getHeader().getNpts()+" "+sac.getHeader().getDelta());
        float[] sacData = sac.getY();
        int npts = sac.getHeader().getNpts();
        float delta = sac.getHeader().getDelta();
        System.out.println("sac file loaded, before opencl");
         
        //int npts = 1024;
        //float delta = 0.05f;
        float[] data = new float[npts];
        data[100] = 1 / delta;
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(data);
        CLBuffer<Float> gaussVals = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, npts);
        System.out.println("before gaussian");
        FloatArrayResult out = openCL.gaussianFilter(inCLBuffer, 2.5f, delta, gaussVals);
        float[] outData = out.getAfterWait(openCL.queue);
        assertArrayEquals("gaussian filter", sacData, outData, 0.001f);
    }
    
    @Test
    public void testPower() throws Exception {
        int n = 64;
        float[] inData = new float[n];
        for (int i = 0; i < inData.length; i++) {
            inData[i] = i+1;// because of base zero
        }
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        FloatResult result = openCL.power(inCLBuffer, new CLEvent[0]);
        assertEquals("sum 1*1 to "+n+"*"+n, n*(n+1)*(2*n+1)/6, result.getAfterWait(), 0.001f);
    }
    
    @Test
    public void testCalcMaxSpike() throws Exception {
        int n = 1024;
        int bump = 3;
        float[] inData = new float[n];
        for (int i = 0; i < inData.length; i++) {
            inData[i] = i+1;
        }
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);
        CLBuffer<Float> inCLBuffer = openCL.makeCLBuffer(inData);
        
        CLBuffer<Integer> indexCLBuffer = openCL.context.createBuffer(CLMem.Usage.InputOutput, Integer.class, n);
        CLBuffer<Float> ampsCLBuffer = openCL.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        CLEvent resultEvent = openCL.calcMaxSpike(inCLBuffer, ampsCLBuffer, indexCLBuffer, bump, new CLEvent[0]);
        IntArrayResult indexResult = new IntArrayResult(indexCLBuffer, resultEvent);
        FloatArrayResult ampsResult = new FloatArrayResult(ampsCLBuffer, resultEvent);
        float[] amps = ampsResult.getAfterWait(openCL.queue);
        int[] index = indexResult.getAfterWait(openCL.queue);
        for (int i = 0; i < amps.length; i++) {
            System.out.println("amps at "+index[i]+" "+amps[i]);
        }
        assertEquals("max bump of "+bump, n/2, amps[bump], 0.001f);
        assertEquals("max bump index of "+bump, n/2-1, index[bump]);
    }

    @Test
    public void testCorrelationNorm() throws Exception {
        int n = 1024;
        float[] fData = new float[n];
        fData[2] = 2;
        float[] gData = new float[n];
        gData[1] = 2;
        IterDeconOpenCl openCL = new IterDeconOpenCl(1, true, 1, 1);

        CLBuffer<Float> fCLBuffer = openCL.makeCLBuffer(fData);
        CLBuffer<Float> gCLBuffer = openCL.makeCLBuffer(gData);
        
        FloatArrayResult corrResult = openCL.correlateNorm(fCLBuffer, gCLBuffer);
        float[] corr = corrResult.getAfterWait(openCL.queue);
        assertEquals("lag 0", 0f, corr[0], 0.00001f);
        assertEquals("lag 1", 1f, corr[1], 0.00001f);
        assertEquals("lag 2", 0f, corr[2], 0.00001f);
        assertEquals("lag 3", 0f, corr[3], 0.00001f);
    }
    /**
     * Test copied from IterDecon.java. 
     * @throws Exception
     */
    //@Test
    public void testESK1999_312_16_45_41_6() throws Exception {
        SacTimeSeries sac = new SacTimeSeries();
        DataInputStream in = new DataInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/ESK1999_312_16.predicted.sac"));
        sac.read(in);
        in.close();
        float[] fortranData = sac.getY();
        in = new DataInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/ESK_num.sac"));
        sac.read(in);
        in.close();
        float[] num = sac.getY();
        in = new DataInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/ESK_denom.sac"));
        sac.read(in);
        in.close();
        float[] denom = sac.getY();
        IterDeconOpenCl iterdecon = new IterDeconOpenCl(100, true, .0001f, 3);
        IterDeconResult result = iterdecon.process(num, denom, sac.getHeader().getDelta());
        float[] pred = result.getPredicted();
        FloatArrayResult predResult = iterdecon.phaseShift(iterdecon.makeCLBuffer(pred), 5, sac.getHeader().getDelta());
        int[] s = result.getShifts();
        float[] a = result.getAmps();
        int i = 0;
        // spikes from fortran are in time, div delta to get index
        // output from fortran iterdecon_tjo is:
        // The maximum spike delay is 102.40012
        //
        // File Spike amplitude Spike delay Misfit Improvement
        // r001 0.384009242E+00 0.100 48.98% 51.0211%
        // r002 -0.132486761E+00 16.250 42.91% 6.0732%
        // r003 0.116493061E+00 2.250 38.21% 4.6952%
        // r004 -0.988256037E-01 10.800 34.83% 3.3792%
        // r005 -0.606716201E-01 15.450 33.56% 1.2736%
        // r006 -0.635700300E-01 20.650 32.16% 1.3983%
        // r007 -0.568093359E-01 41.350 31.04% 1.1166%
        // r008 0.520336218E-01 3.950 30.11% 0.9368%
        // r009 0.494165495E-01 1.000 29.26% 0.8449%
        // r010 -0.416982807E-01 79.850 28.66% 0.6015%
        // ... snip ...
        // r100 0.105094928E-01 3.500 14.55% 0.0382%
        //
        // Last Error Change = 0.0382%
        //
        // Number of bumps in final result: 100
        // The final deconvolution reproduces 85.4% of the signal.
        assertEquals("spike " + i, 0.100 / sac.getHeader().getDelta(), s[i], 0.1f);
        assertEquals("amp   " + i, 0.384009242 / sac.getHeader().getDelta(), a[i], 0.001f);
        i++;
        assertEquals("spike " + i, 16.250 / sac.getHeader().getDelta(), s[i], 0.1f);
        assertEquals("amp   " + i, -0.132486761 / sac.getHeader().getDelta(), a[i], 0.001f);
        i++;
        assertEquals("spike " + i, 2.250 / sac.getHeader().getDelta(), s[i], 0.1f);
        assertEquals("amp   " + i, 0.116493061 / sac.getHeader().getDelta(), a[i], 0.001f);
        i++;
        assertEquals("spike " + i, 10.800 / sac.getHeader().getDelta(), s[i], 0.1f);
        assertEquals("amp   " + i, -0.0988256037 / sac.getHeader().getDelta(), a[i], 0.001f);
        i++;
        assertEquals("spike " + i, 15.450 / sac.getHeader().getDelta(), s[i], 0.1f);
        assertEquals("amp   " + i, -0.0606716201 / sac.getHeader().getDelta(), a[i], 0.001f);
        i++;
        assertArrayEquals("fortran predicted", fortranData, pred, 0.000001f);
        assertEquals("percent match", 85.4, result.getPercentMatch(), 0.1f);
    }
}
