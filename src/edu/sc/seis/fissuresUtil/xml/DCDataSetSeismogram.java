package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.DBDataCenter;
import edu.sc.seis.fissuresUtil.database.LocalDataCenterCallBack;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Category;

/**
 * DataSetSeismogram.java
 *
 *
 * Created: Tue Feb 11 10:08:37 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DCDataSetSeismogram
    extends DataSetSeismogram
    implements LocalDataCenterCallBack, Cloneable {


    public DCDataSetSeismogram(RequestFilter rf,
                               DataCenterOperations dco) {
        this(rf, dco, null);
    }

    public DCDataSetSeismogram(RequestFilter rf,
                               DataCenterOperations dco,
                               DataSet ds) {
        this(rf, dco, ds, null);
    }

    public DCDataSetSeismogram(RequestFilter rf,
                               DataCenterOperations dco,
                               DataSet ds,
                               String name) {
        super(ds, name);
        this.requestFilter = rf;
        this.dataCenterOps = dco;
    }

    public void retrieveData(SeisDataChangeListener dataListener){
        RequestFilter[] temp = {requestFilter};
        Iterator it = seisCache.iterator();
        List matchingSeismos = new ArrayList();
        while(it.hasNext()){
            LocalSeismogramImpl current = (LocalSeismogramImpl)((SoftReference)it.next()).get();
            if(current != null&&
               requestFilter.channel_id.equals(current.channel_id) &&
               requestFilter.start_time.equals(current.getBeginTime().getFissuresTime()) &&
               requestFilter.end_time.equals(current.getEndTime().getFissuresTime())){
                matchingSeismos.add(current);
            }
        }
        LocalSeismogramImpl[] cachedSeismos = new LocalSeismogramImpl[matchingSeismos.size()];
        cachedSeismos = (LocalSeismogramImpl[])matchingSeismos.toArray(cachedSeismos);
        pushData(cachedSeismos, dataListener);
        try{
            if(this.dataCenterOps instanceof DBDataCenter) {
                ((DBDataCenter)this.dataCenterOps).request_seismograms(temp,
                                                                           (LocalDataCenterCallBack)this,
                                                                       dataListener,
                                                                       false,
                                                                       new MicroSecondDate().getFissuresTime());

            } else {
                /*
                 DBDataCenter.getDataCenter(this.dataCenterOps).request_seismograms(temp,
                 (LocalDataCenterCallBack)this,
                 dataListener,
                 false,
                 new MicroSecondDate().getFissuresTime());
                 */
            }
        } catch(FissuresException fe) {
            //          throw new DataRetrievalException("Exception occurred while using DataCenter to get Data",fe);
            //       } catch(SQLException fe) {
            //    throw new DataRetrievalException("Exception occurred while using DataCenter to get Data",fe);
        }
    }

    private DataCenterOperations dataCenterOps;

    static Category logger =
        Category.getInstance(DCDataSetSeismogram.class.getName());



}// DataSetSeismogram
