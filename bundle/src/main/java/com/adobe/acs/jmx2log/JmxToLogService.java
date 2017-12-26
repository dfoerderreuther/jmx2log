package com.adobe.acs.jmx2log;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
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
@Property(name = "scheduler.concurrent", boolValue = false)
public class JmxToLogService implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_CANONICAL_NAME_PATTERN = "org\\.apache\\.jackrabbit\\.oak.*";
    @Property(description="Regex Pattern of canonical name", value=DEFAULT_CANONICAL_NAME_PATTERN)
    private static final String CANONICAL_NAME_PATTERN = "jmxtolog.canonical.pattern";
    private String canonicalNamePattern;

    private static final String DEFAULT_SCHEDULER_EXPRESSION = "0 * * * * ?";
    @Property(description="Cron Scheduler Expression", value=DEFAULT_SCHEDULER_EXPRESSION)
    private static final String SCHEDULER_EXPRESSION = "scheduler.expression";
    private String schedulerExpression;

    ObjectName mbean;

    @Activate
    protected void activate(ComponentContext ctx) {
        canonicalNamePattern = PropertiesUtil.toString(ctx.getProperties().get(CANONICAL_NAME_PATTERN), DEFAULT_CANONICAL_NAME_PATTERN);
        schedulerExpression = PropertiesUtil.toString(ctx.getProperties().get(SCHEDULER_EXPRESSION), DEFAULT_SCHEDULER_EXPRESSION);

        /*

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        QueryExp queryExp = new QueryExp() {
            public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException {
                return false;
            }

            public void setMBeanServer(MBeanServer s) {

            }
        };

        final Set<ObjectName> mbeans = server.queryNames(null, null);

        for (final ObjectName mbean : mbeans) {
            this.mbean = mbean;
        }*/
    }

    public void run() {

        log.error("run");
    }
}
