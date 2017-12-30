jmx2log
========

OSGi bundle with a service writing defined JMX values continuously to a logfile. Tested with AEM / Apache Felix

Building
--------

This project uses Maven for building. Common commands:

    mvn clean install

Configuration
--------

Logging
--------

Go to /system/console/slinglog and create an info logger for jmx2log.

![Log Config](/doc/logconfig.png)

Service-Configuration
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

![Service Config](/doc/config.png)

JMX-Dump-View
--------

There is a Servlet for an overview of accessible JMX-Beans and attributes.

/system/console/JMX-Dump
