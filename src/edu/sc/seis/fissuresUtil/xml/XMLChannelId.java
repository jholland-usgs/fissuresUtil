package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLChannelId.java
 *
 *
 * Created: Mon Jul  1 14:52:45 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLChannelId {
 
    public static void insert(Element element, ChannelId channelId){
	 Document doc = element.getOwnerDocument();
	 Element network_id = doc.createElement("network_id");
	 XMLNetworkId.insert(network_id, channelId.network_id);
	 element.appendChild(network_id);

	 element.appendChild(XMLUtil.createTextElement(doc,
						       "station_code",
						       channelId.station_code));
	 element.appendChild(XMLUtil.createTextElement(doc,
						       "site_code",
						       channelId.site_code));
	 element.appendChild(XMLUtil.createTextElement(doc,
						       "channel_code",
						       channelId.channel_code));
	 Element begin_time = doc.createElement("begin_time");
	 XMLTime.insert(begin_time, channelId.begin_time);
	 element.appendChild(begin_time);
						       
     }

    public static ChannelId getChannelId(Element base) {

	//get the network_id
	NetworkId network_id = null;
	NodeList network_id_node = XMLUtil.evalNodeList(base, "network_id");
	if(network_id_node != null && network_id_node.getLength() != 0) {

	    network_id = XMLNetworkId.getNetworkId((Element)network_id_node.item(0));	    
	}
	//if(network_id == null) System.out.println("Network ID is null");
	//else System.out.println("The Network Id is NOT NULL");

	//get the station_code
	String station_code = XMLUtil.evalString(base, "station_code");
    
	//get the site_code
	String site_code = XMLUtil.evalString(base, "site_code");
	
	//get the channel_code
	String channel_code = XMLUtil.evalString(base, "channel_code");
	
	//get the begin_time
	edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
	NodeList begin_time_node = XMLUtil.evalNodeList(base, "begin_time");
	if(begin_time_node != null && begin_time_node.getLength() != 0) {
	    begin_time = XMLTime.getFissuresTime((Element)begin_time_node.item(0));
	}
	return new ChannelId(network_id,
			     station_code,
			     site_code,
			     channel_code,
			     begin_time);
	
    }
}// XMLChannelId
