package df.aem.jmx2log.impl;

import com.google.common.collect.Lists;
import df.aem.jmx2log.ReadJmxService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.management.ObjectName;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 29.12.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JmxToLogServiceTest {

    @Mock
    ReadJmxService readJmxService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Map<String, Object> props;

    @InjectMocks
    TestableJmxToLogService underTest = new TestableJmxToLogService();

    @Test
    public void activate_initSearchConfigs() {
        // given
        when(props.get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"a|b", "c|d"});

        // when
        underTest.activate(props);

        // then
        assertThat(underTest.searchConfigs.size(), is(2));
        assertThat(underTest.searchConfigs.get(0).getNamePattern(), is("a"));
        assertThat(underTest.searchConfigs.get(0).getAttributePattern(), is("b"));
        assertThat(underTest.searchConfigs.get(1).getNamePattern(), is("c"));
        assertThat(underTest.searchConfigs.get(1).getAttributePattern(), is("d"));
    }

    @Test
    public void activate_ignoreEmptyConfigs() {
        // given
        when(props.get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{
                "",
                " ",
                null
        });

        // when
        underTest.activate(props);

        // then
        assertThat(underTest.searchConfigs.size(), is(0));
    }

    @Test
    public void activate_resetSearchConfigs() {
        // given
        when(props.get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"a|b"});
        underTest.activate(props);

        // when
        when(props.get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"c|d"});
        underTest.activate(props);

        // then
        assertThat(underTest.searchConfigs.size(), is(1));
        assertThat(underTest.searchConfigs.get(0).getNamePattern(), is("c"));
        assertThat(underTest.searchConfigs.get(0).getAttributePattern(), is("d"));
    }

    @Test
    public void run_shouldSearchForBeansAndAttributes() {
        // given
        when(props.get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"beanA|attributeA", "beanB|attributeB"});
        underTest.activate(props);

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
    public void run_shouldLogResult() {
        // given
        when(props.get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"beanA|attributeA"});
        underTest.activate(props);

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

        ReadJmxService.MBeanAttribute lastLogParam = null;
        String beanPattern = null;
        String attributePattern = null;

        @Override
        void log(ReadJmxService.MBeanAttribute mBeanAttribute,
                 SearchConfig searchConfig) {
            this.lastLogParam = mBeanAttribute;
            this.beanPattern = searchConfig.getNamePattern();
            this.attributePattern = searchConfig.getAttributePattern();
        }
    }

}