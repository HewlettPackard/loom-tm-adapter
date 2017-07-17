/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom.adapter.tm.backend.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BackEndMonitoringMetric.class)
public class TestBackEndMonitoringMetric {

    private static final long now = 1445412480000L;
    private static final String testingMetricsFile = "src/test/resources/exampleMetricsResponse.json";

    // Testing target object
    BackEndMonitoringMetric metricUpdater;

    @Before
    public void before() {
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.currentTimeMillis()).thenReturn(now);
        metricUpdater = new BackEndMonitoringMetric(null, "currentRoute");
    }

    @After
    public void after() {}

    @Test
    public void testInitialStartTime() {
        long startTime = metricUpdater.getStartTime();
        long expectedStartTime = now - BackEndMonitoringMetric.MAX_DELTA_T;
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testStartTimeNow() {
        metricUpdater.setLastUpdateTime(now);
        long startTime = metricUpdater.getStartTime();
        long expectedStartTime = now - BackEndMonitoringMetric.MARGIN_T;
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testStartTimeNewerThanMaxDeltaTAndMarginT() {
        long expectedStartTime = now - BackEndMonitoringMetric.MAX_DELTA_T + 1;
        metricUpdater
                .setLastUpdateTime(now - BackEndMonitoringMetric.MAX_DELTA_T + BackEndMonitoringMetric.MARGIN_T + 1);
        long startTime = metricUpdater.getStartTime();
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testStartTimeNewerThanMaxDeltaTNoMarginT() {
        long expectedStartTime = now - BackEndMonitoringMetric.MAX_DELTA_T;
        metricUpdater
                .setLastUpdateTime(now - BackEndMonitoringMetric.MAX_DELTA_T + BackEndMonitoringMetric.MARGIN_T - 1);
        long startTime = metricUpdater.getStartTime();
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testStartTimeOlderThanMaxDeltaT() {
        metricUpdater.setLastUpdateTime(now - BackEndMonitoringMetric.MAX_DELTA_T - 1);
        long startTime = metricUpdater.getStartTime();
        long expectedStartTime = now - BackEndMonitoringMetric.MAX_DELTA_T;
        assertEquals(expectedStartTime, startTime);
    }

    @Test
    public void testPutMetricFirstTime() {
        Metric metric = new Metric("m", now, 100, null);
        metricUpdater.putMetric("metricName", "metricCoord", metric);
        long latestUpdateTime = metricUpdater.getLastUpdateTime();
        assertEquals(now, latestUpdateTime);
    }

    @Test
    public void testPutNewerMetric() {
        Metric metric1 = new Metric("m1", now - 1, 100, null);
        Metric metric2 = new Metric("m2", now, 100, null);
        metricUpdater.putMetric("metric1Name", "metric1Coord", metric1);
        metricUpdater.putMetric("metric2Name", "metric2Coord", metric2);
        long latestUpdateTime = metricUpdater.getLastUpdateTime();
        assertEquals(now, latestUpdateTime);
    }

    @Test
    public void testPutOlderMetric() {
        Metric metric1 = new Metric("m1", now, 100, null);
        Metric metric2 = new Metric("m2", now - 1, 100, null);
        metricUpdater.putMetric("metric1Name", "metric1Coord", metric1);
        metricUpdater.putMetric("metric2Name", "metric2Coord", metric2);
        long latestUpdateTime = metricUpdater.getLastUpdateTime();
        assertEquals(now, latestUpdateTime);
    }

    @Test
    public void testGetMetricsForCoordinate() {
        Metric metric1 = new Metric("m1", now - 2, 100, null);
        Metric metric2 = new Metric("m2", now, 100, null);
        Metric metric3 = new Metric("m3", now - 1, 100, null);
        metricUpdater.putMetric("metric1Name", "coord1", metric1);
        metricUpdater.putMetric("metric2Name", "coord2", metric2);
        metricUpdater.putMetric("metric3Name", "coord1", metric3);
        HashMap<String, Metric> metricsCoord1 = metricUpdater.getMetricsForCoordinate("coord1");
        HashMap<String, Metric> metricsCoord2 = metricUpdater.getMetricsForCoordinate("coord2");
        assertEquals(2, metricsCoord1.size());
        assertEquals(metric1, metricsCoord1.get("metric1Name"));
        assertEquals(metric3, metricsCoord1.get("metric3Name"));
        assertEquals(1, metricsCoord2.size());
        assertEquals(metric2, metricsCoord2.get("metric2Name"));
    }

    @Test
    public void testGetMetricsForCoordinateUpdate() {
        Metric metric1 = new Metric("m1", now - 2, 100, null);
        Metric metric1Newer = new Metric("m1", now, 100, null);
        metricUpdater.putMetric("metricName", "coord1", metric1);
        metricUpdater.putMetric("metricName", "coord1", metric1Newer);
        HashMap<String, Metric> metricsCoord1 = metricUpdater.getMetricsForCoordinate("coord1");
        assertEquals(1, metricsCoord1.size());
        assertEquals(metric1Newer, metricsCoord1.get("metricName"));
    }

    @Test
    public void testGetMetricsForCoordinateDifferentCoordinate() {
        Metric metric1 = new Metric("m1", now - 2, 100, null);
        Metric metric1Newer = new Metric("m1", now, 100, null);
        metricUpdater.putMetric("metricName", "coord1", metric1);
        metricUpdater.putMetric("metricName", "coord2", metric1Newer);
        HashMap<String, Metric> metricsCoord1 = metricUpdater.getMetricsForCoordinate("coord1");
        HashMap<String, Metric> metricsCoord2 = metricUpdater.getMetricsForCoordinate("coord2");
        assertEquals(1, metricsCoord1.size());
        assertEquals(metric1, metricsCoord1.get("metricName"));
        assertEquals(1, metricsCoord2.size());
        assertEquals(metric1Newer, metricsCoord2.get("metricName"));
    }

    @Test
    public void testParseMetrics() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metricsRoot = mapper.readTree(new File(testingMetricsFile));
        ArrayNode metrics = (ArrayNode) metricsRoot.get("metrics");
        metricUpdater.parseMetrics(metrics);
        HashMap<String, Metric> parsedMetrics = metricUpdater.getMetricsForCoordinate(
                "/MachineVersion/1/Datacenter/BUK1/Rack/A1.AboveFloor/Enclosure/U3/EncNum/1/Node/1");
        ArrayList<String> interfaces = metricUpdater.getNetworkInterfaces();
        assertEquals(3, parsedMetrics.size());
        assertEquals(0.0, parsedMetrics.get("cpu.idle_perc").getValue(), 0.00001);
        assertEquals(11238.0, parsedMetrics.get("net.in_bytes_sec.lo").getValue(), 0.00001);
        assertEquals(7698.0, parsedMetrics.get("net.in_bytes_sec.eth0").getValue(), 0.00001);
        assertEquals(2, interfaces.size());
        assertTrue(interfaces.contains("eth0"));
        assertTrue(interfaces.contains("lo"));
    }

}


