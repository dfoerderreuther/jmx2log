jmx2log
========

This bundle which contains a service for writing jmx values from current vm to a logfile.

Building
--------

This project uses Maven for building. Common commands:

    mvn clean install

Configuration
--------

Go to /system/console/configMgr and create a new service instance with a configuration. Possible configuration settings are:

**Property scheduler.concurrent**

Enables concurrent running. Default is false. 

**Property scheduler.expression**

Time settings...

**Search**

1..n Strings with JMX search pattern in format BEAN|ATTRIBUTE

The service is searching with BEAN for a JMX bean with a matching canonical name. BEAN is used a regular expression.

If a matching bean was found, the service is searching for an attribute with a matching name. The serach pattern ATTRIBUTE is used as a regular expression, again.

