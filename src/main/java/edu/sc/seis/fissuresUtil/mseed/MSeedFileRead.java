package edu.sc.seis.fissuresUtil.mseed;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.seisFile.BuildVersion;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;

public class MSeedFileRead {

    public static void main(String[] args) throws IOException, SeedFormatException {
        String network = null;
        String station = null;
        String location = null;
        String channel = null;
        String filename = null;
        String outFile = null;
        int maxRecords = -1;
        int defaultRecordSize = 4096;
        boolean verbose = false;
        boolean dumpData = false;
        DataOutputStream dos = null;
        PrintWriter out = new PrintWriter(System.out, true);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-n")) {
                network = args[i + 1];
            } else if (args[i].equals("-s")) {
                station = args[i + 1];
            } else if (args[i].equals("-l")) {
                location = args[i + 1];
            } else if (args[i].equals("-c")) {
                channel = args[i + 1];
            } else if (args[i].equals("-d")) {
                dumpData = true;
            } else if (args[i].equals("-o")) {
                outFile = args[i + 1];
                dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
            } else if (args[i].equals("-r")) {
                defaultRecordSize = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("-m")) {
                maxRecords = Integer.parseInt(args[i + 1]);
                if (maxRecords < -1) {
                    maxRecords = -1;
                }
            } else if (args[i].equals("--verbose")) {
                verbose = true;
            } else if (args[i].equals("--version")) {
                out.println(BuildVersion.getDetailedVersion());
                System.exit(0);
            } else if (args[i].equals("--help")) {
                out.println("java "
                        + MSeedFileRead.class.getName()
                        + " [-n net][-s sta][-l loc][-c chan][-o mseedOutfile][-m maxrecords][--verbose][--version][--help] <filename>");
                System.exit(0);
            } else {
                filename = args[i];
            }
        }
        if (filename == null) {
            return;
        }
        File f = new File(filename);
        InputStream inStream;
        if (f.exists() && f.isFile()) {
            inStream = new FileInputStream(filename);
        } else {
            // maybe a url?
            try {
                URL url = new URL(filename);
                inStream = url.openStream();
            } catch(MalformedURLException e) {
                out.println("Cannot load '" + filename + "', as file or URL: exists=" + f.exists() + " isFile="
                        + f.isFile() + " " + e.getMessage());
                return;
            } catch(FileNotFoundException e) {
                out.println("Cannot load '" + filename + "', as file or URL: exists=" + f.exists() + " isFile="
                        + f.isFile() + " " + e.getMessage());
                return;
            }
        }
        // if you wish to customize the blockette creation, for example to add
        // new types of Blockettes,
        // create an object that implements BlocketteFactory and then
        // SeedRecord.setBlocketteFactory(myBlocketteFactory);
        // see DefaultBlocketteFactory for an example
        DataInputStream dataInStream = new DataInputStream(new BufferedInputStream(inStream, 1024));
        int i = 0;
        try {
            while (maxRecords == -1 || i < maxRecords) {
                SeedRecord sr = SeedRecord.read(dataInStream, defaultRecordSize);
                if (sr instanceof DataRecord) {
                    DataRecord dr = (DataRecord)sr;
                    if ((network == null || network.equals(dr.getHeader().getNetworkCode()))
                            && (station == null || station.equals(dr.getHeader().getStationIdentifier()))
                            && (location == null || location.equals(dr.getHeader().getLocationIdentifier()))
                            && (channel == null || channel.equals(dr.getHeader().getChannelIdentifier()))) {
                        if (dos != null) {
                            dr.write(dos);
                        }
                        if (dos == null || verbose) {
                            // print something to the screen if we are not
                            // saving to disk
                            dr.writeASCII(out, "    ");
                            out.flush();
                        }
                        if (dumpData) {
                            dr.writeData(out);
                        }
                        LocalSeismogramImpl seis = FissuresConvert.toFissures(dr);
                        try {
                            int[] data = seis.get_as_longs();
                            System.out.println("Decompress to "+data.length);
                        } catch(FissuresException e) {
                            throw new RuntimeException(dr.toString(), e);
                        }
                    }
                } else {
                    // print non-data records just because...
                    sr.writeASCII(out, "    ");
                    out.flush();
                }
                i++;
            }
        } catch(EOFException e) {
            // done I guess
        }
        if (dos != null) {
            dos.close();
        }
        dataInStream.close();
        out.println("Finished: " + new Date());
    }
}
