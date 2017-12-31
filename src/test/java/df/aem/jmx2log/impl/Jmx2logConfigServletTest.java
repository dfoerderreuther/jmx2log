package df.aem.jmx2log.impl;

import df.aem.jmx2log.ReadJmxService;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.management.ObjectName;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 30.12.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class Jmx2logConfigServletTest {

    @Mock
    ReadJmxService readJmxService;

    @InjectMocks
    Jmx2logConfigServlet underTest;

    @Mock
    ServletRequest servletRequest;

    @Mock
    ServletResponse servletResponse;

    StringWriter stringWriter = new StringWriter();

    @Before
    public void setup() throws Exception {
        when(servletResponse.getWriter()).thenReturn(new PrintWriter(stringWriter));
    }

    @Test
    public void service_shouldWrite() throws Exception {
        // given
        when(readJmxService.mBeans()).thenReturn(Lists.<ObjectName>newArrayList());

        // when
        underTest.service(servletRequest, servletResponse);

        // then
        assertThat(stringWriter.toString(), is(""));
        //assertThat(stringWriter.toString(), containsString("table"));
    }

    @Test
    public void service_shouldWriteTable() throws Exception {
        // given
        ObjectName mBean = mock(ObjectName.class);
        when(mBean.toString()).thenReturn("mBean-name");
        when(readJmxService.mBeans()).thenReturn(Lists.newArrayList(mBean));

        ReadJmxService.MBeanAttribute attribute = mock(ReadJmxService.MBeanAttribute.class);
        when(attribute.name()).thenReturn("attribute-name");
        when(attribute.type()).thenReturn("attribute-type");
        when(attribute.value()).thenReturn("attribute-value");
        when(readJmxService.attributes(mBean)).thenReturn(Lists.newArrayList(attribute));

        // when
        underTest.service(servletRequest, servletResponse);

        // then
        final String result = stringWriter.toString();
        assertThat(result, containsString("table"));
        assertThat(result, containsString("mBean-name"));
        assertThat(result, containsString("attribute-name"));
        assertThat(result, containsString("attribute-type"));
        assertThat(result, containsString("attribute-value"));
    }

}