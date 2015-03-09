package com.epam.wilma.safeguard.monitor;
/*==========================================================================
Copyright 2013-2015 EPAM Systems

This file is part of Wilma.

Wilma is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Wilma is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wilma.  If not, see <http://www.gnu.org/licenses/>.
===========================================================================*/

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.epam.wilma.core.safeguard.SafeguardController;
import com.epam.wilma.domain.exception.SystemException;
import com.epam.wilma.safeguard.configuration.SafeguardConfigurationAccess;
import com.epam.wilma.safeguard.configuration.domain.PropertyDTO;
import com.epam.wilma.safeguard.configuration.domain.QueueSizeProvider;
import com.epam.wilma.safeguard.configuration.domain.SafeguardLimits;
import com.epam.wilma.safeguard.monitor.helper.JmxConnectionBuilder;
import com.epam.wilma.safeguard.monitor.helper.JmxObjectNameProvider;

/**
 * Provides tests for the class {@link JmsQueueMonitorTask}.
 * @author Marton_Sereg
 *
 */
public class JmsQueueMonitorTaskTest {

    @InjectMocks
    private JmsQueueMonitorTask underTest;
    @Mock
    private JmxConnectionBuilder jmxConnectionBuilder;
    @Mock
    private JmxObjectNameProvider jmxObjectNameProvider;
    @Mock
    private MBeanServerConnection mBeanServerConnection;
    @Mock
    private ObjectName responseQueue;
    @Mock
    private ObjectName loggerQueue;
    @Mock
    private Logger logger;
    @Mock
    private SafeguardController safeguardController;
    @Mock
    private SafeguardConfigurationAccess configurationAccess;
    @Mock
    private PropertyDTO propertyDTO;
    @Mock
    private QueueSizeProvider queueSizeProvider;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(underTest, "logger", logger);
        given(configurationAccess.getProperties()).willReturn(propertyDTO);
        given(propertyDTO.getSafeguardLimits()).willReturn(new SafeguardLimits(new Long(100), new Long(60), new Long(200), new Long(80)));
    }

    @Test
    public final void testRunShouldSwitchOnlyFIOffWhenFIOffLimitExceededButMWOffLimitNotExceeded() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", true);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", true);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(91));
        // WHEN
        underTest.run();
        // THEN
        verify(logger).info("Due to High load, FI decompression is turned OFF.");
        verify(logger, never()).info("Due to High load, Message Logging is turned OFF.");
        verify(logger, never()).info("Due to Normal load, Message Logging is restored.");
        verify(logger, never()).info("Due to Normal load, FI decompression is restored.");
    }

    @Test
    public final void testRunShouldSwitchFIAndMWOffeWhenFIandMwOffLimitExceeded() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", true);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", true);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(191));
        // WHEN
        underTest.run();
        // THEN
        verify(logger).info("Due to High load, FI decompression is turned OFF.");
        verify(logger).info("Due to High load, Message Logging is turned OFF.");
        verify(logger, never()).info("Due to Normal load, Message Logging is restored.");
        verify(logger, never()).info("Due to Normal load, FI decompression is restored.");
    }

    @Test
    public final void testRunShouldntSwitchOffAnythingWhenLimitsAreNotExceeded() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", true);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", true);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(11));
        // WHEN
        underTest.run();
        // THEN
        verify(logger, never()).info("Due to High load, FI decompression is turned OFF.");
        verify(logger, never()).info("Due to High load, Message Logging is turned OFF.");
        verify(logger, never()).info("Due to Normal load, Message Logging is restored.");
        verify(logger, never()).info("Due to Normal load, FI decompression is restored.");
    }

    @Test
    public final void testRunShouldntSwitchOnAnythingWhenQueueSizeDidntDropBelowAnyLimit() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", false);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", false);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(111));
        // WHEN
        underTest.run();
        // THEN
        verify(logger, never()).info("Due to High load, FI decompression is turned OFF.");
        verify(logger, never()).info("Due to High load, Message Logging is turned OFF.");
        verify(logger, never()).info("Due to Normal load, Message Logging is restored.");
        verify(logger, never()).info("Due to Normal load, FI decompression is restored.");
    }

    @Test
    public final void testRunShouldSwitchMWOnWhenQueueSizeDroppedBelowMWLimit() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", false);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", false);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(58));
        // WHEN
        underTest.run();
        // THEN
        verify(logger, never()).info("Due to High load, FI decompression is turned OFF.");
        verify(logger, never()).info("Due to High load, Message Logging is turned OFF.");
        verify(logger).info("Due to Normal load, Message Logging is restored.");
        verify(logger, never()).info("Due to Normal load, FI decompression is restored.");
    }

    @Test
    public final void testRunShouldSwitchMWAndFIOnWhenQueueSizeDroppedBelowFILimit() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", false);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", false);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(2));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(3));
        // WHEN
        underTest.run();
        // THEN
        verify(logger, never()).info("Due to High load, FI decompression is turned OFF.");
        verify(logger, never()).info("Due to High load, Message Logging is turned OFF.");
        verify(logger).info("Due to Normal load, Message Logging is restored.");
        verify(logger).info("Due to Normal load, FI decompression is restored.");
    }

    @Test(expectedExceptions = SystemException.class)
    public final void testRunShouldThrowExceptionWhenQueueSizeCannotBeRetrieved() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", true);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", true);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willThrow(new AttributeNotFoundException());
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(91));
        // WHEN
        underTest.run();
        // THEN exception is thrown
    }

    @Test
    public final void testRunShouldInitConnectionWhenLoggerQueueIsNull() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "loggerQueue", null);
        QueueSizeProvider queueSizeProvider = new QueueSizeProvider();
        Whitebox.setInternalState(underTest, "queueSizeProvider", queueSizeProvider);
        givenJmxConnectionBuilder();
        // WHEN
        underTest.run();
        // THEN
        verifyConnectionBuilder();
    }

    @Test
    public final void testRunShouldInitConnectionWhenResponseQueueIsNull() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "responseQueue", null);
        QueueSizeProvider queueSizeProvider = new QueueSizeProvider();
        Whitebox.setInternalState(underTest, "queueSizeProvider", queueSizeProvider);
        givenJmxConnectionBuilder();
        // WHEN
        underTest.run();
        // THEN
        verifyConnectionBuilder();
    }

    @Test
    public final void testRunShouldInitConnectionWhenMBeanServerConnectionIsNull() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "mBeanServerConnection", null);
        QueueSizeProvider queueSizeProvider = new QueueSizeProvider();
        Whitebox.setInternalState(underTest, "queueSizeProvider", queueSizeProvider);
        givenJmxConnectionBuilder();
        // WHEN
        underTest.run();
        // THEN
        verifyConnectionBuilder();
    }

    @Test
    public final void testRunShouldSetQueuesSizeIntoQueueSizeProvider() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", true);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", true);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(91));
        // WHEN
        underTest.run();
        // THEN
        verify(queueSizeProvider).setResponseQueueSize(new Long(12));
        verify(queueSizeProvider).setLoggerQueueSize(new Long(91));
    }

    @Test
    public final void testRunShouldSaveQueuesSizeIntoQueueSizeProvider() throws Exception {
        // GIVEN
        Whitebox.setInternalState(underTest, "fIDecompressionEnabled", true);
        Whitebox.setInternalState(underTest, "messageWritingEnabled", true);
        QueueSizeProvider queueSizeProvider = new QueueSizeProvider();
        Whitebox.setInternalState(underTest, "queueSizeProvider", queueSizeProvider);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(12));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(91));
        // WHEN
        underTest.run();
        // THEN
        long loggerQueueSize = ((QueueSizeProvider) Whitebox.getInternalState(underTest, "queueSizeProvider")).getLoggerQueueSize();
        Assert.assertEquals(loggerQueueSize, 91);
        long responseQueueSize = ((QueueSizeProvider) Whitebox.getInternalState(underTest, "queueSizeProvider")).getResponseQueueSize();
        Assert.assertEquals(responseQueueSize, 12);
    }

    private void givenJmxConnectionBuilder() throws Exception {
        given(jmxConnectionBuilder.buildMBeanServerConnection(JmsQueueMonitorTask.JMX_SERVICE_URL)).willReturn(mBeanServerConnection);
        given(jmxObjectNameProvider.getObjectName(JmsQueueMonitorTask.RESPONSE_QUEUE_OBJECT_NAME)).willReturn(responseQueue);
        given(jmxObjectNameProvider.getObjectName(JmsQueueMonitorTask.LOGGER_QUEUE_OBJECT_NAME)).willReturn(loggerQueue);
        given(mBeanServerConnection.getAttribute(responseQueue, "QueueSize")).willReturn(new Long(2));
        given(mBeanServerConnection.getAttribute(loggerQueue, "QueueSize")).willReturn(new Long(3));
    }

    private void verifyConnectionBuilder() {
        verify(jmxConnectionBuilder).buildMBeanServerConnection(JmsQueueMonitorTask.JMX_SERVICE_URL);
        verify(jmxObjectNameProvider).getObjectName(JmsQueueMonitorTask.RESPONSE_QUEUE_OBJECT_NAME);
        verify(jmxObjectNameProvider).getObjectName(JmsQueueMonitorTask.LOGGER_QUEUE_OBJECT_NAME);
    }

}