package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;


public class BulletproofVestFactory{

    /**
     * Bulletproofing the NetworkAccess combines our four delicious ProxyNetworkAccess
     * classes in a hard candy shell.  The order is SynchronizedDCNetworkAccess
     * inside a NSNetworkAccess inside a
     * RetryNetworkAccess inside a CacheNetworkAccess.
     * This means that all of the results you get from
     * this network access will be synched around the DC, cached, if there are some
     * transient network issues we'll retry 3 times, and if the network access
     * reference goes stale we'll reget it from the naming service.  It'd is required
     * to use a NSNetworkDC as the type of NetworkDCOperations as it allows the
     * NetworkDC to be refreshed from the naming service if the reference goes stale
     *
     * @throws IllegalArguemtnException if the netDC does not wrap a NSNetworkDC at some level.
     */
    public static ProxyNetworkAccess vestNetworkAccess(NetworkAccess na, ProxyNetworkDC netDC) {
        //avoid vesting if it is already vested.
        if(na instanceof CacheNetworkAccess) {
            return (ProxyNetworkAccess) na;
        }else {
            // side effect, make sure we have an NSNetworkDC inside the ProxyNetworkDC
            // throws an IllegalArgumentException if there is not a NSNetworkDC inside somewhere
            NSNetworkDC nsNetDC = (NSNetworkDC)netDC.getWrappedDC(NSNetworkDC.class);
            // side effect  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            SynchronizedNetworkAccess synch = new SynchronizedNetworkAccess(na);
            RetryNetworkAccess retry = new RetryNetworkAccess(synch, 3);
            CacheNetworkAccess cache = new CacheNetworkAccess(retry);
            NetworkId id = cache.get_attributes().get_id();
            NSNetworkAccess nsNetworkAccess = new NSNetworkAccess(synch, id, netDC);
            retry.setNetworkAccess(nsNetworkAccess);
            cache.setNetworkAccess(retry);

            return cache;
        }

    }

    public static ProxyNetworkDC vestNetworkDC(String serverDNS, String serverName, FissuresNamingService fisName) {
        return new RetryNetworkDC(new NSNetworkDC(serverDNS, serverName, fisName), 2);
    }

    public static ProxySeismogramDC vestSeismogramDC(String serverDNS, String serverName, FissuresNamingService fisName) {
        NSSeismogramDC ns = new NSSeismogramDC(serverDNS, serverName, fisName);
        RetrySeismogramDC retry = new RetrySeismogramDC(ns, 3);
        return retry;
    }

    public static ProxyPlottableDC vestPlottableDC(String serverDNS, String serverName, FissuresNamingService fisName) {
        NSPlottableDC ns = new NSPlottableDC(serverDNS, serverName, fisName);
        RetryPlottableDC retry = new RetryPlottableDC(ns, 3);
        CachePlottableDC cache = new CachePlottableDC(retry);
        return cache;
    }
}


