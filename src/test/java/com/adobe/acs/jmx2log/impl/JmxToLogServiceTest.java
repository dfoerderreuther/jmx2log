package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.ReadJmxService;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.component.ComponentContext;

import javax.management.ObjectName;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 29.12.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JmxToLogServiceTest {

    @Mock
    ReadJmxService readJmxService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ComponentContext ctx;

    @InjectMocks
    TestableJmxToLogService underTest = new TestableJmxToLogService();

    @Test
    public void activate_initSearchConfigs() throws Exception {
        // given
        when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"a|b", "c|d"});

        // when
        underTest.activate(ctx);

        // then
        assertThat(underTest.searchConfigs.size(), is(2));
        assertThat(underTest.searchConfigs.get(0).getNamePattern(), is("a"));
        assertThat(underTest.searchConfigs.get(0).getAttributePattern(), is("b"));
        assertThat(underTest.searchConfigs.get(1).getNamePattern(), is("c"));
        assertThat(underTest.searchConfigs.get(1).getAttributePattern(), is("d"));
    }

    @Test
    public void activate_ignoreEmptyConfigs() throws Exception {
        // given
        when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{
                "",
                " ",
                null
        });

        // when
        underTest.activate(ctx);

        // then
        assertThat(underTest.searchConfigs.size(), is(0));
    }

    @Test
    public void activate_resetSearchConfigs() throws Exception {
        // given
        when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"a|b"});
        underTest.activate(ctx);

        // when
        when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"c|d"});
        underTest.activate(ctx);

        // then
        assertThat(underTest.searchConfigs.size(), is(1));
        assertThat(underTest.searchConfigs.get(0).getNamePattern(), is("c"));
        assertThat(underTest.searchConfigs.get(0).getAttributePattern(), is("d"));
    }

    @Test
    public void run_shouldSearchForBeansAndAttributes() throws Exception {
        // given
        when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"beanA|attributeA", "beanB|attributeB"});
        underTest.activate(ctx);

        ObjectName mBean = mock(ObjectName.class);
        when(readJmxService.mBeans(Mockito.anyString())).thenReturn(Lists.newArrayList(mBean));

        ReadJmxService.MBeanAttribute attribute = mockMBean("name", String.class.getName(), "value");
        when(readJmxService.attributes(Mockito.any(ObjectName.class), Mockito.anyString())).thenReturn(Lists.newArrayList(attribute));

        // when
        underTest.run();

        // then
        verify(readJmxService).mBeans("beanA");
        verify(readJmxService).attributes(mBean, "attributeA");

        verify(readJmxService).mBeans("beanB");
        verify(readJmxService).attributes(mBean, "attributeB");
    }
    @Test
    public void run_shouldLogResult() throws Exception {
        // given
        when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"beanA|attributeA"});
        underTest.activate(ctx);

        ObjectName mBean = mock(ObjectName.class);
        when(readJmxService.mBeans(Mockito.anyString())).thenReturn(Lists.newArrayList(mBean));

        ReadJmxService.MBeanAttribute attribute = mockMBean("name", String.class.getName(), "value");
        when(readJmxService.attributes(Mockito.any(ObjectName.class), Mockito.anyString())).thenReturn(Lists.newArrayList(attribute));

        // when
        underTest.run();

        // then
        assertThat(underTest.lastLogParam.name(), is("name"));
        assertThat(underTest.lastLogParam.value().toString(), is("value"));
    }

    private ReadJmxService.MBeanAttribute mockMBean(String name, String type, Object value) {
        ReadJmxService.MBeanAttribute attribute = mock(ReadJmxService.MBeanAttribute.class);
        when(attribute.name()).thenReturn(name);
        when(attribute.type()).thenReturn(type);
        when(attribute.value()).thenReturn(value);
        return attribute;
    }

    class TestableJmxToLogService extends JmxToLogService {

        ReadJmxService.MBeanAttribute lastLogParam;

        @Override
        void log(ReadJmxService.MBeanAttribute mBeanAttribute) {
            this.lastLogParam = mBeanAttribute;
        }
    }

}