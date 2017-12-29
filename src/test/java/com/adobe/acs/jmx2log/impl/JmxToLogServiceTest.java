package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.ReadJmxService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.component.ComponentContext;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    JmxToLogService underTest;

    @Test
    public void activate_initSearchConfigs() throws Exception {
        Mockito.when(ctx.getProperties().get(JmxToLogService.SEARCH_CONFIG)).thenReturn(new String[]{"a|b"});

        // when
        underTest.activate(ctx);

        // then
        assertThat(underTest.searchConfigs.size(), is(1));
    }

}