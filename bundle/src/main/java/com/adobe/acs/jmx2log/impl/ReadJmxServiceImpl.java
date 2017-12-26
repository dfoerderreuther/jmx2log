package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.CouldNotReadJmxValueException;
import com.adobe.acs.jmx2log.MBeanAttribute;
import com.adobe.acs.jmx2log.ReadJmxService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 26.12.17.
 */
@Component
@Service(value = ReadJmxService.class)
public class ReadJmxServiceImpl implements ReadJmxService {

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    public Iterable<ObjectName> mBeans() {
        return server.queryNames(null, null);
    }

    public Iterable<ObjectName> mBeans(String pattern) {
        return server.queryNames(null, null);
    }

    public Iterable<MBeanAttribute> value(ObjectName mBean) throws CouldNotReadJmxValueException {
        try {
            final MBeanAttributeInfo[] attributes = server.getMBeanInfo(mBean).getAttributes();
            final List<MBeanAttribute> resultAttributes  = new ArrayList<>();
            for (final MBeanAttributeInfo attribute : attributes) {

                final String name = attribute.getName();
                final String type = attribute.getType();
                final Object value = server.getAttribute(mBean, name);

                resultAttributes.add(new MBeanAttribute() {
                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    public String type() {
                        return type;
                    }

                    @Override
                    public Object value() {
                        return value;
                    }
                });

            }
            return resultAttributes;
        } catch (InstanceNotFoundException | IntrospectionException | ReflectionException | MBeanException | AttributeNotFoundException e) {
            throw new CouldNotReadJmxValueException(e);
        }
    }
}
