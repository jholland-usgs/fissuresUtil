package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.DBDataCenter;
import edu.sc.seis.fissuresUtil.database.LocalDataCenterCallBack;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
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

    /** Creates a DCDataSetSeismogram without a containing dataset and without
     a name. The dataset should be automagically set when this is added and the
     name will be autogenerated if needed. */
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
        Iterator it = seisCache.iterator();
        List existingSeismos = new ArrayList();
        while(it.hasNext()){
            LocalSeismogramImpl current = (LocalSeismogramImpl)((SoftReference)it.next()).get();
            if(current != null){
                existingSeismos.add(current);
            }
        }
        RequestFilter[] uncovered = {requestFilter};
        if(existingSeismos.size() > 0){
            LocalSeismogramImpl[] cachedSeismos = new LocalSeismogramImpl[existingSeismos.size()];
            cachedSeismos = (LocalSeismogramImpl[])existingSeismos.toArray(cachedSeismos);
            pushData(cachedSeismos, dataListener);
            uncovered = DBDataCenter.notCovered(uncovered, cachedSeismos);
        }
        try{
            if(this.dataCenterOps instanceof DBDataCenter && uncovered.length > 0) {
                ((DBDataCenter)this.dataCenterOps).request_seismograms(uncovered,
                                                                           (LocalDataCenterCallBack)this,
                                                                       dataListener,
                                                                       false,
                                                                       ClockUtil.now().getFissuresTime());

            }
        } catch(FissuresException fe) {}
    }

    private DataCenterOperations dataCenterOps;

    static Category logger =
        Category.getInstance(DCDataSetSeismogram.class.getName());



}// DataSetSeismogram
