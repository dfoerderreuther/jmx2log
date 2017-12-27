package com.adobe.acs.jmx2log;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 25.12.17.
 */
@Service(Runnable.class)
@Component(metatype=true,label="JMX to Log", description="Write JMX values continuously to logfile", configurationFactory = true)
@Properties({
    @Property(name = "scheduler.concurrent", boolValue = false),
    @Property(name = "scheduler.expression", value = "0 * * * * ?")
})
public class JmxToLogService implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_CANONICAL_NAME_PATTERN = ".*replication.*type=agent.*id=\"publish\".*";
    @Property(description="Regex Pattern of canonical name", value=DEFAULT_CANONICAL_NAME_PATTERN)
    private static final String CANONICAL_NAME_PATTERN = "jmxtolog.canonical.pattern";
    private String canonicalNamePattern;

    private static final String DEFAULT_ATTRIBUTE_NAME_PATTERN = "QueueNumEntries";
    @Property(description="Regex Pattern of attribute name", value=DEFAULT_ATTRIBUTE_NAME_PATTERN)
    private static final String ATTRIBUTE_NAME_PATTERN = "jmxtolog.attribute.pattern";
    private String attributeNamePattern;

    @Reference
    ReadJmxService readJmxService;

    @Activate
    protected void activate(ComponentContext ctx) {
        canonicalNamePattern = PropertiesUtil.toString(ctx.getProperties().get(CANONICAL_NAME_PATTERN), DEFAULT_CANONICAL_NAME_PATTERN);
        attributeNamePattern = PropertiesUtil.toString(ctx.getProperties().get(ATTRIBUTE_NAME_PATTERN), DEFAULT_ATTRIBUTE_NAME_PATTERN);
    }

    public void run() {
        log.trace("run");
        for (ObjectName mBean : readJmxService.mBeans(canonicalNamePattern)) {
            try {
                for (ReadJmxService.MBeanAttribute mBeanAttribute : readJmxService.value(mBean, attributeNamePattern)) {
                    log.trace(mBeanAttribute.name() + ": " + mBeanAttribute.value());
                }
            } catch (ReadJmxService.CouldNotReadJmxValueException e) {
                log.error(String.format("cant read mBean values for %s", mBean.toString()), e);
            }
        }
    }
}
