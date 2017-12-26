package com.adobe.acs.jmx2log;

import javax.management.ObjectName;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 26.12.17.
 */
public interface ReadJmxService {

    Iterable<ObjectName> mBeans();

    Iterable<ObjectName> mBeans(String pattern);

    Iterable<MBeanAttribute> value(ObjectName mBean) throws CouldNotReadJmxValueException;

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
