package edu.sc.seis.fissuresUtil.chooser;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class NameListCellRenderer extends DefaultListCellRenderer {

    public NameListCellRenderer(boolean useNames){
        this(useNames, ! useNames, true);
    }

    /**
     * Creates a new <code>NameListCellRenderer</code> instance.
     *
     * @param useNames true if names should be used
     * @param useCodes true if codes should be used
     * @param codeIsFirst true if the string should be code - name, false if
     *    it should be name - code
     */
    public NameListCellRenderer(boolean useNames,
                                boolean useCodes,
                                boolean codeIsFirst){
        this.useNames = useNames;
        this.useCodes = useCodes;
        this.codeIsFirst = codeIsFirst;
    }

    private String getDisplayString(String name, String code) {
        String out = "";
        if (useNames) {
            out = name;
            if ((name == null || name.length() == 0) && ! useCodes) {
                out = code;
            }
            if ( useCodes && codeIsFirst) {
                out = code +" - "+out;
            }
            if ( useCodes && ! codeIsFirst) {
                out = out + " - "+ code;
            }
            return out;
        }
        return code;
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        String name = "XXXX";
        if (value instanceof NetworkAccess) {
            try {
                String netCode =
                    ((NetworkAccess)value).get_attributes().get_code();
                if (netCode.startsWith("X") ||
                    netCode.startsWith("Y") ||
                    netCode.startsWith("Z")) {
                    edu.iris.Fissures.Time start =
                        ((NetworkAccess)value).get_attributes().get_id().begin_time;
                    netCode += start.date_time.substring(2,4);
                }
                name =
                    getDisplayString(((NetworkAccess)value).get_attributes().name,
                                     netCode);
            } catch (Throwable e) {
                GlobalExceptionHandler.handle("in renderer for network "+name, e);
            }
        }
        if (value instanceof Station) {
            name = getDisplayString(((Station)value).name,
                                        ((Station)value).get_code());
        }
        if (value instanceof Site) {
            // sites do not have names
            name = ((Site)value).get_code();
        }

        if (value instanceof Channel) {
            name = getDisplayString(((Channel)value).name,
                                        ((Channel)value).get_code());
        }

        if (value instanceof String) {
            name = (String)value;
        } // end of if (value instanceof String)

        //logger.debug("render "+name);
        Component c = super.getListCellRendererComponent(list,
                                                         name,
                                                         index,
                                                         isSelected,
                                                         cellHasFocus);
        return c;
    }

    boolean useNames;

    boolean useCodes;

    boolean codeIsFirst;

    private static Logger logger = Logger.getLogger(NameListCellRenderer.class);

}
