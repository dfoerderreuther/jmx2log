package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.CouldNotReadJmxValueException;
import com.adobe.acs.jmx2log.MBeanAttribute;
import com.adobe.acs.jmx2log.ReadJmxService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

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

    @Reference
    ReadJmxService readJmxService;

    public Jmx2logConfigServlet() {

    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {

            final PrintWriter os = new PrintWriter(res.getWriter());
            os.println("<table>");

            MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            os.println("  <tr><td colspan='4'><hr /></td></tr>");
            os.println("  <tr><td>Server:</td><td colspan='3'>"
                    + server.getClass().getName() + "</td></tr>");

            for (final ObjectName mbean : readJmxService.mBeans()) {
                os.println("  <tr><td colspan='4'>&nbsp;</td></tr>");

                for (final MBeanAttribute attribute : readJmxService.value(mbean)) {
                    os.print("  <tr><td>&nbsp;</td><td>" + attribute.name()
                            + "</td><td>" + attribute.type() + "</td><td>");

                    final Object o = attribute.value();
                    if (o == null) {
                        os.print("<font color='#660000'>null</font>");
                    } else {
                        os.print(o.toString());
                    }

                    os.println("</td></tr>");
                }
            }

            os.println("</table>");
            os.flush();

        } catch (CouldNotReadJmxValueException e) {
            e.printStackTrace();
        }
    }

}
