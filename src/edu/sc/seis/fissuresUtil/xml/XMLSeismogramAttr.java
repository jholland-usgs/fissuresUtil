package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfParameterMgr.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.log4j.*;

/**
 * XMLSeismogramAttr.java
 *
 *
 * Created: Mon Jul  1 15:07:32 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class XMLSeismogramAttr {

    public static void insert(Element element, SeismogramAttr seismogramAttr) {
	Document doc = element.getOwnerDocument();
	element.appendChild(XMLUtil.createTextElement(doc,
						      "id",
						      seismogramAttr.get_id()));
	
	Element property;
	for(int counter = 0; counter < seismogramAttr.properties.length; counter++) {

	    property =  XMLProperty.createElement(doc, 
					   seismogramAttr.properties[counter],
					   "property");
	    element.appendChild(property);
	}

	Element begin_time = doc.createElement("begin_time");
	XMLTime.insert(begin_time, seismogramAttr.begin_time);
	element.appendChild(begin_time);

	element.appendChild(XMLUtil.createTextElement(doc,
						      "num_points",
						      ""+seismogramAttr.num_points));

	Element sampling_info = doc.createElement("sampling_info");
	XMLSampling.insert(sampling_info, seismogramAttr.sampling_info);
	element.appendChild(sampling_info);

	Element y_unit = doc.createElement("y_unit");
	XMLUnit.insert(y_unit, seismogramAttr.y_unit);
	element.appendChild(y_unit);

	Element channel_id = doc.createElement("channel_id");
	XMLChannelId.insert(channel_id, seismogramAttr.channel_id);
	element.appendChild(channel_id);

	Element parameter;
	for(int counter = 0; counter < seismogramAttr.parm_ids.length; counter++) {

	    parameter = doc.createElement("parameter");
	    XMLParameter.insert(parameter, 
				seismogramAttr.parm_ids[counter].a_id,
				seismogramAttr.parm_ids[counter].creator);
	    element.appendChild(parameter);
	}

	Element time_correction;
	for(int counter = 0; counter < seismogramAttr.time_corrections.length; counter++) {

	    time_correction = doc.createElement("time_correction");
	    XMLQuantity.insert(time_correction, seismogramAttr.time_corrections[counter]);
	    element.appendChild(time_correction);
	}

	Element sample_rate_history;
	for(int counter = 0; counter < seismogramAttr.sample_rate_history.length; counter++) {

	    sample_rate_history = doc.createElement("sample_rate_history");
	    XMLSampling.insert(sample_rate_history, seismogramAttr.sample_rate_history[counter]);
	    element.appendChild(sample_rate_history);
	}

    }

    public static SeismogramAttr getSeismogramAttr(Element base) {
	String id = XMLUtil.evalString(base, "id");
	//System.out.println("The id of the SeismogramAttr is "+id);

	//Get the Properties.
	NodeList property = XMLUtil.evalNodeList(base, "property");
	Property[] properties = new Property[0];
	if(property != null && property.getLength() != 0) {
	    properties = new Property[property.getLength()];
	    for(int counter = 0; counter < property.getLength(); counter++) {
		properties[counter] = XMLProperty.getProperty((Element)property.item(counter));
	    }
	}


	//Get the begin Time.
	edu.iris.Fissures.Time begin_time = new edu.iris.Fissures.Time();
	NodeList begin_time_node = XMLUtil.evalNodeList(base, "begin_time");
	if(begin_time_node != null && begin_time_node.getLength() != 0) {
	    begin_time = XMLTime.getFissuresTime((Element)begin_time_node.item(0));
	}
	
	//get num_points

	int num_points = Integer.parseInt(XMLUtil.evalString(base, "num_points"));

	//Get the sampling_info
	Sampling sampling_info = null;
	NodeList sampling_info_node = XMLUtil.evalNodeList(base, "sampling_info");
	if(sampling_info_node != null && sampling_info_node.getLength() != 0) {
	    sampling_info = XMLSampling.getSampling((Element)sampling_info_node.item(0));
	}

	//get the y_unit

	Unit  y_unit = null;
	NodeList y_unit_node = XMLUtil.evalNodeList(base, "y_unit");
	if(y_unit_node != null && y_unit_node.getLength() != 0 ) {				   
	    y_unit = XMLUnit.getUnit((Element)y_unit_node.item(0));
	}

	//get the channel_id
	ChannelId channel_id = null;
	NodeList channel_id_node = XMLUtil.evalNodeList(base, "channel_id");
	if(channel_id_node != null && channel_id_node.getLength() != 0) {

	    channel_id = XMLChannelId.getChannelId((Element)channel_id_node.item(0));
	}

	//get the parameters
	ParameterRef[] parm_ids = new ParameterRef[0];
	NodeList params = XMLUtil.evalNodeList(base, "parameter");
	if(params != null && params.getLength() != 0) {
	    for(int counter = 0; counter < params.getLength(); counter++) {
		parm_ids[counter] = (ParameterRef)XMLParameter.getParameter((Element)params.item(counter));
	    }
	}

	//get the time_corrections
	Quantity[] time_corrections = new Quantity[0];
	NodeList time_corrections_list = XMLUtil.evalNodeList(base, "time_correction");
	if(time_corrections_list != null && time_corrections_list.getLength() != 0) {
	    for(int counter = 0; counter < time_corrections_list.getLength(); counter++) {

		time_corrections[counter] = XMLQuantity.getQuantity((Element)time_corrections_list.item(counter));
	    }
	}

	//get the sample_rate_history
	Sampling[] sample_rate_history = new Sampling[0];
	NodeList sample_rate_history_list = XMLUtil.evalNodeList(base, "sample_rate_history");
	if(sample_rate_history_list != null && sample_rate_history_list.getLength() != 0) {
	    
	    for(int counter = 0; counter < sample_rate_history_list.getLength(); counter++) {

		sample_rate_history[counter] = XMLSampling.getSampling((Element)sample_rate_history_list.item(0));
	    }

	}
	
	return new SeismogramAttrImpl(id, 
				      properties,
				      begin_time,
				      num_points,
				      sampling_info,
				      y_unit,
				      channel_id,
				      parm_ids,
				      time_corrections,
				      sample_rate_history);
				      
    }
}// XMLSeismogramAttr
