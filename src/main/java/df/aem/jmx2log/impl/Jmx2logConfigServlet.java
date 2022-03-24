package df.aem.jmx2log.impl;

import df.aem.jmx2log.ReadJmxService;
import df.aem.jmx2log.exception.CouldNotReadJmxValueException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Dominik Foerderreuther df@adobe.com on 25.12.17.
 */
@Component(service = Servlet.class, property = {"felix.webconsole.label=JMX-Dump", "felix.webconsole.title=JMX mBean dump"})
public class Jmx2logConfigServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(Jmx2logConfigServlet.class);

    @Reference
    ReadJmxService readJmxService;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException {
        complete(res.getWriter());
    }

    private void complete(PrintWriter writer) {

        final PrintWriter os = new PrintWriter(writer);


        for (final ObjectName mbean : readJmxService.mBeans()) {
            os.println("<table class=\"nicetable ui-widget\">\n" +
                    "<thead class=\"ui-widget-header\">\n" +
                    "<tr>\n" +
                    "<th colspan=\"3\" class=\"ui-widget-header\">" + mbean + "</th>\n" +
                    "</tr>\n" +
                    "</thead>\n");

            os.println("<tbody class=\"ui-widget-content\">\n");

            try {
                for (final ReadJmxService.MBeanAttribute attribute : readJmxService.attributes(mbean)) {

                    os.println("<tr>\n" +
                            "<td style=\"width:20%\">" + attribute.name() + "</td>\n" +
                            "<td style=\"width:20%\">" + attribute.type() + "</td>\n" +
                            "<td>" + attribute.value() + "</td>\n" +
                            "</tr>\n");

                }

            } catch (CouldNotReadJmxValueException e) {
                log.error("cant read attributes of " + mbean.toString(), e);
            }

            os.println("</tbody>\n");
            os.println("</table>\n");
        }

        os.flush();

    }

}
