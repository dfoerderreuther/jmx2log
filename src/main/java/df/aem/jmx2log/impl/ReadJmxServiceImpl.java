package df.aem.jmx2log.impl;

import com.google.common.annotations.VisibleForTesting;
import df.aem.jmx2log.ReadJmxService;
import df.aem.jmx2log.exception.CouldNotReadJmxValueException;
import df.aem.jmx2log.exception.NoSuchAttributeException;
import df.aem.jmx2log.exception.NoSuchMBeanException;
import org.osgi.service.component.annotations.Component;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Dominik Foerderreuther df@adobe.com on 26.12.17.
 */
@Component(service = ReadJmxService.class)
public class ReadJmxServiceImpl implements ReadJmxService {

    @VisibleForTesting
    protected MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @Override
    public Iterable<ObjectName> mBeans() {
        return this.mBeans(".*");
    }

    @Override
    public Iterable<ObjectName> mBeans(final String pattern) throws NoSuchMBeanException {
        final Set<ObjectName> objectNames = server.queryNames(null, new QueryExp() {
            @Override
            public boolean apply(ObjectName name) {
                return name.toString().matches(pattern);
            }

            @Override
            public void setMBeanServer(MBeanServer s) {
                // We already know the server we run the query on - if ever interested in that
                // information.
            }
        });

        if (objectNames.isEmpty()) {
            throw new NoSuchMBeanException(pattern);
        }
        return objectNames;
    }

    @Override
    public Iterable<MBeanAttribute> attributes(ObjectName mBean) throws CouldNotReadJmxValueException {
        return attributes(mBean, ".*");
    }

    @Override
    public Iterable<MBeanAttribute> attributes(ObjectName mBean, String namePattern)
            throws CouldNotReadJmxValueException {
        try {
            final MBeanAttributeInfo[] attributes = server.getMBeanInfo(mBean).getAttributes();
            final List<MBeanAttribute> resultAttributes = new ArrayList<>();
            for (final MBeanAttributeInfo attribute : attributes) {

                final String mbean = mBean.toString();
                final String name = attribute.getName();
                final String type = attribute.getType();
                final Object value = server.getAttribute(mBean, name);
                if (name.matches(namePattern))

                    resultAttributes.add(new MBeanAttribute() {
                        @Override
                        public String mbean() {
                            return mbean;
                        }

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

            if (resultAttributes.isEmpty()) {
                throw new NoSuchAttributeException(mBean, namePattern);
            }
            return resultAttributes;
        } catch (RuntimeMBeanException | InstanceNotFoundException | IntrospectionException | ReflectionException
                | MBeanException | AttributeNotFoundException e) {
            throw new CouldNotReadJmxValueException(e);
        }
    }
}
