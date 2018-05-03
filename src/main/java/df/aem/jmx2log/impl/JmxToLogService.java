package df.aem.jmx2log.impl;

import com.google.common.annotations.VisibleForTesting;
import df.aem.jmx2log.ReadJmxService;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 25.12.17.
 */
@Service(Runnable.class)
@Component(metatype=true,
        label="JMX to Log", description="Write JMX values continuously to logfile",
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Properties({
    @Property(name = "scheduler.concurrent", boolValue = false),
    @Property(name = "scheduler.expression", value = "0 * * * * ?")
})
public class JmxToLogService implements Runnable {

    private static final String LOGGER_NAME_PROP = "logger.name";
    private static final String LOGGER_NAME_DEFAULT = "jmx2log";

    private static final String MESSAGE_TEMPLATE_PROP = "messageTemplate";
    private static final String MESSAGE_TEMPLATE_DEFAULT = "{1}: {2}";

    @Property(label = "Logger", name=LOGGER_NAME_PROP, value=LOGGER_NAME_DEFAULT,
              description = "Name of the logger to log to.")
    private Logger log = null;

    @Property(label = "Message Template", name=MESSAGE_TEMPLATE_PROP, value = MESSAGE_TEMPLATE_DEFAULT,
              description = "{0}: Type {1}: Name of property, {2}: Value of property, {3}: Bean pattern, {4}: Property pattern")
    private String messageTemplate = MESSAGE_TEMPLATE_DEFAULT;

    @VisibleForTesting
    static final String DEFAULT_SEARCH_CONFIG = ".*replication.*type=agent.*id=\"publish\".*|QueueNumEntries";
    @Property(unbounded = PropertyUnbounded.ARRAY, cardinality=10, label="Search", description="Regex Pattern for jmx-bean and attribute in format beanpattern|attributpattern", value=DEFAULT_SEARCH_CONFIG)
    static final String SEARCH_CONFIG = "jmxtolog.search.configs";

    @VisibleForTesting
    List<SearchConfig> searchConfigs;

    @VisibleForTesting
    @Reference
    ReadJmxService readJmxService;

    @Activate
    protected void activate(final Map<String, Object> props) {
        searchConfigs = new ArrayList<>();
        String[] strSearchConfigs = PropertiesUtil.toStringArray(props.get(SEARCH_CONFIG), new String[]{DEFAULT_SEARCH_CONFIG});
        for (String strSearchConfig : strSearchConfigs) {
            if (StringUtils.isBlank(strSearchConfig)) {
                continue;
            }
            String[] parts = strSearchConfig.split("\\|");
            String namePattern = parts[0];
            String attributePattern = parts.length > 1 ? parts[1] : "";
            searchConfigs.add(new SearchConfig(namePattern, attributePattern));
        }

        messageTemplate = PropertiesUtil.toString(props.get(MESSAGE_TEMPLATE_PROP), MESSAGE_TEMPLATE_DEFAULT);
        log = LoggerFactory.getLogger(PropertiesUtil.toString(props.get(LOGGER_NAME_PROP), LOGGER_NAME_DEFAULT));
    }

    @Deactivate
    private void deactivate() {
        this.log = null;
    }

    public void run() {
        for (SearchConfig searchConfig : searchConfigs) {
            logJmxValues(searchConfig.getNamePattern(), searchConfig.getAttributePattern());
        }
    }

    private void logJmxValues(String namePattern, String attributeNamePattern) {
        for (ObjectName mBean : readJmxService.mBeans(namePattern)) {
            try {
                for (ReadJmxService.MBeanAttribute mBeanAttribute : readJmxService.attributes(mBean, attributeNamePattern)) {
                    log(mBeanAttribute, namePattern, attributeNamePattern);
                }
            } catch (ReadJmxService.CouldNotReadJmxValueException e) {
                log.error(String.format("cant read mBean values for %s", mBean.toString()), e);
            }
        }
    }

    @VisibleForTesting
    void log(final ReadJmxService.MBeanAttribute mBeanAttribute,
             final String beanPattern, final String attributePattern) {
        if(log!=null) {
            final String message = MessageFormat.format(messageTemplate,
                    mBeanAttribute.type(), mBeanAttribute.name(), mBeanAttribute.value(),
                    beanPattern, attributePattern);
            log.info(message);
        }
    }

    @VisibleForTesting
    static class SearchConfig {

        private final String namePattern;

        private final String attributePattern;

        SearchConfig(String canonicalNamePattern, String attributePattern) {
            this.namePattern = canonicalNamePattern;
            this.attributePattern = attributePattern;
        }

        String getNamePattern() {
            return namePattern;
        }

        String getAttributePattern() {
            return attributePattern;
        }
    }
}
