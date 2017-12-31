package df.aem.jmx2log;

import javax.management.ObjectName;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 26.12.17.
 */
public interface ReadJmxService {

    Iterable<ObjectName> mBeans();

    Iterable<ObjectName> mBeans(String pattern);

    Iterable<MBeanAttribute> attributes(ObjectName mBean) throws CouldNotReadJmxValueException;

    Iterable<MBeanAttribute> attributes(ObjectName mBean, String namePattern) throws CouldNotReadJmxValueException;

    interface MBeanAttribute {
        String name();

        String type();

        Object value();
    }

    class CouldNotReadJmxValueException extends Exception {

        public CouldNotReadJmxValueException(Throwable e) {
            super(e);
        }

    }
}
