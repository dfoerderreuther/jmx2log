package com.adobe.acs.jmx2log;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;

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

    private static final String DEFAULT_SEARCH_CONFIG = ".*replication.*type=agent.*id=\"publish\".*|QueueNumEntries";
    @Property(unbounded = PropertyUnbounded.ARRAY, cardinality=10, label="Search", description="Regex Pattern for jmx-bean and attribute in format beanpattern|attributpattern", value=DEFAULT_SEARCH_CONFIG)
    private static final String SEARCH_CONFIG = "jmxtolog.search.configs";

    private List<SearchConfig> searchConfigs;

    @Reference
    ReadJmxService readJmxService;

    @Activate
    protected void activate(ComponentContext ctx) {
        searchConfigs = new ArrayList<>();
        String[] strSearchConfigs = PropertiesUtil.toStringArray(ctx.getProperties().get(SEARCH_CONFIG), new String[]{DEFAULT_SEARCH_CONFIG});
        for (String strSearchConfig : strSearchConfigs) {
            if (StringUtils.isEmpty(strSearchConfig)) {
                continue;
            }
            String[] parts = strSearchConfig.split("|");
            String namePattern = parts[0];
            String attributePattern = parts.length > 0 ? parts[1] : "";
            searchConfigs.add(new SearchConfig(namePattern, attributePattern));
        }
    }

    public void run() {
        log.trace("run");
        for (SearchConfig searchConfig : searchConfigs) {
            logJmxValues(searchConfig.getNamePattern(), searchConfig.getAttributePattern());
        }
    }

    private void logJmxValues(String namePattern, String attributeNamePattern) {
        for (ObjectName mBean : readJmxService.mBeans(namePattern)) {
            try {
                for (ReadJmxService.MBeanAttribute mBeanAttribute : readJmxService.value(mBean, attributeNamePattern)) {
                    log.trace(mBeanAttribute.name() + ": " + mBeanAttribute.value());
                }
            } catch (ReadJmxService.CouldNotReadJmxValueException e) {
                log.error(String.format("cant read mBean values for %s", mBean.toString()), e);
            }
        }
    }

    private static class SearchConfig {

        private final String namePattern;

        private final String attributePattern;

        SearchConfig(String canonicalNamePattern, String attributeNamePattern) {
            this.namePattern = canonicalNamePattern;
            this.attributePattern = attributeNamePattern;
        }

        String getNamePattern() {
            return namePattern;
        }

        String getAttributePattern() {
            return attributePattern;
        }
    }
}
