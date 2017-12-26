package com.adobe.acs.jmx2log;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 26.12.17.
 */
public interface MBeanAttribute {
    String name();

    String type();

    Object value();
}
