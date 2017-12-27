package com.adobe.acs.jmx2log;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 27.12.17.
 */
public @interface JmxToLogConfig {


    String canonicalNamePattern() default  ".*replication.*type=agent.*id=\"publish\".*";

    String attributeNamePattern() default "QueueNumEntries";

    String schedulerExpression() default "0 * * * * ?";


}
