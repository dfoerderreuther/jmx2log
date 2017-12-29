package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.ReadJmxService;
import org.osgi.service.component.annotations.Component;

import javax.management.AttributeNotFoundException;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 26.12.17.
 */
@Component(service = ReadJmxService.class)
public class ReadJmxServiceImpl implements ReadJmxService {

    private MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    public Iterable<ObjectName> mBeans() {
        return server.queryNames(null, null);
    }

    public Iterable<ObjectName> mBeans(final String pattern) {
        final Set<ObjectName> objectNames = server.queryNames(null, new QueryExp() {
            @Override
            public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException {
                return name.toString().matches(pattern);
            }

            @Override
            public void setMBeanServer(MBeanServer s) {

            }
        });
        return objectNames;
    }
    public Iterable<MBeanAttribute> value(ObjectName mBean) throws CouldNotReadJmxValueException {
        return value(mBean, null);
    }

    public Iterable<MBeanAttribute> value(ObjectName mBean, String namePattern) throws CouldNotReadJmxValueException {
        try {
            final MBeanAttributeInfo[] attributes = server.getMBeanInfo(mBean).getAttributes();
            final List<MBeanAttribute> resultAttributes  = new ArrayList<>();
            for (final MBeanAttributeInfo attribute : attributes) {

                final String name = attribute.getName();
                final String type = attribute.getType();
                final Object value = server.getAttribute(mBean, name);
                if (namePattern == null || name.matches(namePattern))

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
