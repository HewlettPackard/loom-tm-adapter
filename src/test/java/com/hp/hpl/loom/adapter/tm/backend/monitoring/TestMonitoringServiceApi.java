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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.TMItemCollector;
import com.hp.hpl.loom.adapter.tm.backend.RestClient.RestTemplateWithJsonAndString;
import com.hp.hpl.loom.adapter.tm.backend.aa.MockModel;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TMInstance.class)
public class TestMonitoringServiceApi {

    // Testing target object
    MonitoringServiceApi api;

    // Mocks
    private RestTemplateWithJsonAndString mockRest;
    private MockModel model;

    // Constants
    private static final String monitoringUrl = "dmaUrl";
    private static final String instancePrefix = "instance.";

    @BeforeClass
    public static void beforeClass() throws NoSuchItemTypeException, JsonProcessingException, IOException {}

    @Before
    public void before() throws NoSuchItemTypeException, JsonProcessingException, IOException {
        api = new MonitoringServiceApi(monitoringUrl);
        mockRestClient();
        model = new MockModel();
    }

    @After
    public void after() {}

    private void mockRestClient() {
        mockRest = PowerMockito.mock(RestTemplateWithJsonAndString.class);
        PowerMockito.doReturn(new ResponseEntity<String>(HttpStatus.OK)).when(mockRest)
                .postForEntity(eq(monitoringUrl + TMItemCollector.MON_METRICS), any(ArrayNode.class), eq(null));
        api.restClient = mockRest;
    }

    @Test
    public void testNoUpdates() throws JsonProcessingException, IOException {
        List<TMInstance> instances = model.createModel();
        api.postInstanceMetrics(instances, 1000);
        assertPostNotCalled();
    }

    @Test
    public void testOneUpdate() throws JsonProcessingException, IOException {
        model.setInstanceLastMetricUpdateTimestamp(123456789012L);
        model.setInstanceNetInBytesSec(5000);
        List<TMInstance> instances = model.createModel();
        api.postInstanceMetrics(instances, 1000);
        validatePost(Arrays.asList(Arrays.asList(instancePrefix + Const.METRIC_NET_IN_BYTES_SEC + "_1", "123456789012",
                "5000.0", MockModel.instanceCoordinate)));
    }

    @Test
    public void testAllUpdate() throws JsonProcessingException, IOException {
        model.setInstanceLastMetricUpdateTimestamp(123456789012L);
        model.setInstanceCpuUtilisation(75);
        model.setInstanceCpuStolen(19);
        model.setInstanceCpuSystem(21);
        model.setInstanceCpuUser(20);
        model.setInstanceCpuWait(15);
        model.setInstanceDiskInodeUsed(150);
        model.setInstanceDiskSpaceUsed(200);
        model.setInstanceNetInBytesSec(5000);
        model.setInstanceNetInErrorsSec(1);
        model.setInstanceNetInPacketsDroppedSec(16);
        model.setInstanceNetInPacketsSec(1500);
        model.setInstanceNetOutBytesSec(15000);
        model.setInstanceNetOutErrorsSec(2);
        model.setInstanceNetOutPacketsDroppedSec(14);
        model.setInstanceNetOutPacketsSec(1000);
        model.setInstanceNumDiscrepancies(50);
        model.setInstanceFamUtil(75.3);
        model.setInstanceFabricUtil(41.2);
        List<TMInstance> instances = model.createModel();
        api.postInstanceMetrics(instances, 1000);
        validatePost(Arrays.asList(
                Arrays.asList(instancePrefix + Const.METRIC_CPU_IDLE_PERC + "_1", "123456789012", "25.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_CPU_STOLEN + "_1", "123456789012", "19.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_CPU_SYSTEM + "_1", "123456789012", "21.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_CPU_USER + "_1", "123456789012", "20.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_CPU_WAIT + "_1", "123456789012", "15.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_DISK_INODE_USED + "_1", "123456789012", "150.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_DISK_SPACE_USED + "_1", "123456789012", "200.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_IN_BYTES_SEC + "_1", "123456789012", "5000.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_IN_ERRORS_SEC + "_1", "123456789012", "1.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_IN_PACKETS_DROPPED_SEC + "_1", "123456789012", "16.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_IN_PACKETS_SEC + "_1", "123456789012", "1500.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_OUT_BYTES_SEC + "_1", "123456789012", "15000.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_OUT_ERRORS_SEC + "_1", "123456789012", "2.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC + "_1", "123456789012", "14.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NET_OUT_PACKETS_SEC + "_1", "123456789012", "1000.0",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_FABRIC_LINK_UTILISATION + "_1", "123456789012", "41.2",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_FAM_UTILISATION + "_1", "123456789012", "75.3",
                        MockModel.instanceCoordinate),
                Arrays.asList(instancePrefix + Const.METRIC_NUM_DISCREPANCIES + "_1", "123456789012", "50.0",
                        MockModel.instanceCoordinate)));
    }

    private void validatePost(List<List<String>> updatedMetrics) throws JsonProcessingException, IOException {
        ArrayList<JsonNode> expectedOps = new ArrayList<JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        for (List<String> metric : updatedMetrics) {
            expectedOps.add(mapper
                    .readTree("{\"name\": \"" + metric.get(0) + "\", \"timestamp\": " + metric.get(1) + ", \"value\": "
                            + metric.get(2) + ", \"dimensions\": {\"coordinate\": \"" + metric.get(3) + "\"}}"));
        }
        ArrayNode expectedJArray = mapper.createArrayNode();
        for (JsonNode op : expectedOps) {
            expectedJArray.add(op);
        }
        Mockito.verify(mockRest, Mockito.times(1)).postForEntity(monitoringUrl + TMItemCollector.MON_METRICS,
                expectedJArray, null);
    }

    private void assertPostNotCalled() {
        Mockito.verify(mockRest, Mockito.times(0)).postForEntity(any(String.class), any(JsonNode.class), eq(null));
    }

}
