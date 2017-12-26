package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.ReadJmxService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;

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
        search(res.getWriter());
        //complete(res.getWriter());
    }

    private void search(PrintWriter writer) {
        try {
            final PrintWriter os = new PrintWriter(writer);
            os.println("<table>");

            // org.apache.jackrabbit.oak:name="/etc/replication//\*[11111b, no local]@com.day.cq.replication.impl.ConfigManagerImpl",type=BackgroundObserverStats,listenerId=9
            final String attributeNamePattern = "QueueNumEntries";
            String search = ".*replication.*type=agent.*id=\"publish\".*";
            os.println("  <tr><td>search: </td><td colspan='3'>" + search + "</td></tr>");
            os.println("  <tr><td>attributeNamePattern: </td><td colspan='3'>" + attributeNamePattern + "</td></tr>");

            for (final ObjectName mbean : readJmxService.mBeans(search)) {
                os.println("  <tr><td colspan='4'>&nbsp;</td></tr>");
                os.println("  <tr><td colspan='4'>" + mbean + "</td></tr>");

                for (final ReadJmxService.MBeanAttribute attribute : readJmxService.value(mbean, attributeNamePattern)) {
                    os.print("  <tr><td>&nbsp;</td><td>" + attribute.name() + "</td><td>" + attribute.type() + "</td><td>");

                    final Object o = attribute.value();
                    if (o == null) {
                        os.print("<font>null</font>");
                    } else {
                        os.print(o.toString());
                    }

                    os.println("</td></tr>");
                }
            }
            os.println("</table>");
            os.flush();

        } catch (ReadJmxService.CouldNotReadJmxValueException e) {
            e.printStackTrace();
        }
    }

    private void complete(PrintWriter writer) throws IOException {
        try {

            final PrintWriter os = new PrintWriter(writer);
            os.println("<table>");

            for (final ObjectName mbean : readJmxService.mBeans()) {
                os.println("  <tr><td colspan='4'>&nbsp;</td></tr>");
                os.println("  <tr><td colspan='4'>" + mbean + "</td></tr>");

                for (final ReadJmxService.MBeanAttribute attribute : readJmxService.value(mbean)) {
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

        } catch (ReadJmxService.CouldNotReadJmxValueException e) {
            e.printStackTrace();
        }
    }

}
