package com.adobe.acs.jmx2log.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 25.12.17.
 */

@Component
@Service(value = Runnable.class)
@Properties({
        @Property(name = "scheduler.expression", value = "0 * * * * ?"),
        @Property(name = "scheduler.concurrent", boolValue = false)

})
public class Jmx2logCronService implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public void run() {
        log.info("Executing a cron job (job#1) through the whiteboard pattern");

    }
}
