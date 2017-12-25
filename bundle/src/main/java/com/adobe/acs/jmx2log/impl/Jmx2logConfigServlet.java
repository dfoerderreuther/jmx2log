package com.adobe.acs.jmx2log.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 25.12.17.
 */
@Component
@Service
@Properties({
        @Property(name = "felix.webconsole.label", value = "JMX2Log"),
        @Property(name = "felix.webconsole.title", value = "JMX to Log")
})
public class Jmx2logConfigServlet extends HttpServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        res.getWriter().println("test");

        try {

            final PrintWriter os = new PrintWriter(res.getWriter());
            os.println("<table>");

            final List<MBeanServer> servers = new LinkedList<MBeanServer>();
            servers.add(ManagementFactory.getPlatformMBeanServer());
            servers.addAll(MBeanServerFactory.findMBeanServer(null));
            for (final MBeanServer server : servers) {
                os.println("  <tr><td colspan='4'>&nbsp;</td></tr>");
                os.println("  <tr><td>Server:</td><td colspan='3'>"
                        + server.getClass().getName() + "</td></tr>");

                final Set<ObjectName> mbeans = new HashSet<ObjectName>();
                mbeans.addAll(server.queryNames(null, null));
                for (final ObjectName mbean : mbeans) {
                    os.println("  <tr><td colspan='4'>&nbsp;</td></tr>");
                    os.println("  <tr><td>MBean:</td><td colspan='3'>" + mbean
                            + "</td></tr>");

                    final MBeanAttributeInfo[] attributes;
                    attributes = server.getMBeanInfo(mbean).getAttributes();

                    for (final MBeanAttributeInfo attribute : attributes) {
                        os.print("  <tr><td>&nbsp;</td><td>" + attribute.getName()
                                + "</td><td>" + attribute.getType() + "</td><td>");

                        try {
                            final Object value = server.getAttribute(mbean,
                                    attribute.getName());
                            if (value == null) {
                                os.print("<font color='#660000'>null</font>");
                            } else {
                                os.print(value.toString());
                            }
                        } catch (Exception e) {
                            os.print("<font color='#990000'>" + e.getMessage()
                                    + "</font>");
                        }

                        os.println("</td></tr>");
                    }
                }
            }

            os.println("</table>");
            os.flush();

        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
    }

}
