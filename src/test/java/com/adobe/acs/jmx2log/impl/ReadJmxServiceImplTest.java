package com.adobe.acs.jmx2log.impl;

import com.adobe.acs.jmx2log.ReadJmxService;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import javax.management.ObjectName;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Dominik Foerderreuther <df@adobe.com> on 29.12.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadJmxServiceImplTest {

    @InjectMocks
    ReadJmxServiceImpl underTest;

    @Test
    public void mBeans_shouldGetCompleteList() throws Exception {
        // when
        final Iterable<ObjectName> mBeans = underTest.mBeans();

        // then
        assertThat(Iterables.size(mBeans), greaterThan(0));
    }

    @Test
    public void mBeans_shouldFindByRegex() throws Exception {
        // when
        final String pattern = ".*MemoryPool.*";
        final Iterable<ObjectName> mBeans = underTest.mBeans(pattern);

        // then
        assertThat(Iterables.size(mBeans), greaterThan(0));

        for (ObjectName mBean : mBeans) {
            assertTrue(mBean.toString().matches(pattern));
        }
    }

    @Test
    public void value_shouldGetAttributes() throws Exception {
        // given
        final String pattern = ".*GarbageCollector.*";
        ObjectName mBean = Iterables.getFirst(underTest.mBeans(pattern), null);

        // when
        final Iterable<ReadJmxService.MBeanAttribute> attributes = underTest.attributes(mBean);

        // then
        assertThat(Iterables.size(attributes), greaterThan(0));
    }

    @Test
    public void value_shouldFindAttribute() throws Exception {
        // given
        final String pattern = ".*GarbageCollector.*";
        ObjectName mBean = Iterables.getFirst(underTest.mBeans(pattern), null);

        // when
        final String attributePattern = "CollectionCou.*";
        final Iterable<ReadJmxService.MBeanAttribute> attributes = underTest.attributes(mBean, attributePattern);

        // then
        assertThat(Iterables.size(attributes), greaterThan(0));
        assertThat(Iterables.getFirst(attributes, null).value(), notNullValue());
    }



}