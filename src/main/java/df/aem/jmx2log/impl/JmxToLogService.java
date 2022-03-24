package df.aem.jmx2log.impl;

import com.google.common.annotations.VisibleForTesting;
import df.aem.jmx2log.ReadJmxService;
import df.aem.jmx2log.exception.CouldNotReadJmxValueException;
import df.aem.jmx2log.exception.NoSuchAttributeException;
import df.aem.jmx2log.exception.NoSuchMBeanException;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Dominik Foerderreuther df@adobe.com on 25.12.17.
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

    private static final Logger CLASS_LOG = LoggerFactory.getLogger(JmxToLogService.class);

    private static final String LOGGER_NAME_PROP = "logger.name";
    private static final String LOGGER_NAME_DEFAULT = "jmx2log";

    private static final String MESSAGE_TEMPLATE_PROP = "messageTemplate";
    private static final String MESSAGE_TEMPLATE_DEFAULT = "{1}: {2}";

    @Property(label = "Logger", name=LOGGER_NAME_PROP, value=LOGGER_NAME_DEFAULT,
              description = "Name of the logger to log to.")
    private Logger jmxLog = null;

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
        searchConfigs = new CopyOnWriteArrayList<>(
            parseSearchConfigs(PropertiesUtil.toStringArray(props.get(SEARCH_CONFIG), new String[]{DEFAULT_SEARCH_CONFIG})));

        messageTemplate = PropertiesUtil.toString(props.get(MESSAGE_TEMPLATE_PROP), MESSAGE_TEMPLATE_DEFAULT);
        jmxLog = LoggerFactory.getLogger(PropertiesUtil.toString(props.get(LOGGER_NAME_PROP), LOGGER_NAME_DEFAULT));
    }

    private List<SearchConfig> parseSearchConfigs(String[] patternLines) {
        final List<SearchConfig> configs = new LinkedList<>();
        for (String patternLine : patternLines) {
            if (StringUtils.isBlank(patternLine)) {
                CLASS_LOG.warn("Ignoring empty line in search configs.");
                continue;
            }

            final int split = StringUtils.indexOf(patternLine, '|');
            final String beanPattern;
            final String attrPattern;
            if(split<0) {
                beanPattern = patternLine;
                attrPattern = ".*";
                CLASS_LOG.info("Assuming attribute pattern \".*\" to output all attributes since configuration line \"{}\" does not contain separator '|'.", patternLine);
            } else {
                beanPattern = StringUtils.substring(patternLine, 0, split);
                attrPattern = StringUtils.substring(patternLine, split+1);
            }

            CLASS_LOG.info("Adding bean \"{}\", attribute \"{}\" to search configuration", beanPattern, attrPattern);
            configs.add(
                    new SearchConfig(
                            StringUtils.trim(beanPattern),
                            StringUtils.trim(attrPattern)));
        }

        return configs;
    }

    @Deactivate
    private void deactivate() {
        this.jmxLog = null;
    }

    public void run() {
        for (SearchConfig searchConfig : searchConfigs) {
            try {
                logJmxValues(searchConfig);
            } catch (NoSuchMBeanException nsmbe) {
                CLASS_LOG.warn("\"{}\" currently does not denote an existing MBean.", searchConfig);
            }
        }
    }

    private void logJmxValues(final SearchConfig searchConfig) {
        for (ObjectName mBean : readJmxService.mBeans(searchConfig.getNamePattern())) {
            try {
                for (ReadJmxService.MBeanAttribute mBeanAttribute : readJmxService.attributes(mBean, searchConfig.getAttributePattern())) {
                    log(mBeanAttribute, searchConfig);
                }
            } catch (NoSuchAttributeException nsae) {
                CLASS_LOG.warn("Currently unable to read mbean attribute for {}.", nsae.getMessage());
            } catch (CouldNotReadJmxValueException e) {
                jmxLog.error("Failed to read values of \"{}\" for \"{}\".", mBean.toString(), e.getMessage());
                CLASS_LOG.error("Failed to read MBean values", e);
            }
        }
    }

    @VisibleForTesting
    void log(final ReadJmxService.MBeanAttribute mBeanAttribute,
             final SearchConfig searchConfig) {
        if(jmxLog !=null) {
            final String message = MessageFormat.format(messageTemplate,
                    mBeanAttribute.type(), mBeanAttribute.name(), mBeanAttribute.value(),
                    searchConfig.getNamePattern(), searchConfig.getAttributePattern());
            jmxLog.info(message);
        }
    }

    @VisibleForTesting
    protected static class SearchConfig {

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

        @Override
        public String toString() {
            return "SearchConfig{" +
                    "namePattern='" + namePattern + '\'' +
                    ", attributePattern='" + attributePattern + '\'' +
                    '}';
        }
    }
}
