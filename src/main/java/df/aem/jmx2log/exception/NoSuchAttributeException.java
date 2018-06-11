package df.aem.jmx2log.exception;

import javax.management.ObjectName;
import java.text.MessageFormat;

public class NoSuchAttributeException extends JmxToLogException {

    public NoSuchAttributeException(ObjectName mBean, String attrPattern) {
        super(MessageFormat.format(
                "The MBean \"{0}\" does not contain any attribute matching \"{1}\"",
                mBean.getCanonicalName(), attrPattern));
    }
}
