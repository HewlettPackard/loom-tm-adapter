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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.TMItemCollector;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;
import com.hp.hpl.loom.adapter.tm.backend.RestClient.RestTemplateWithJsonAndString;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.model.Item;


public class MonitoringServiceApi {

    private static final String PREFIX_INSTANCE = "instance.";
    private static final String COORDINATE = "coordinate";
    private static final double MAX_CPU_UTILISATION = 100.0;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    protected RestTemplateWithJsonAndString restClient;
    private String monServiceUrl;
    private static final Log LOG = LogFactory.getLog(MonitoringServiceApi.class);

    public MonitoringServiceApi(final String monServiceUrl) {
        initRestClient();
        this.monServiceUrl = monServiceUrl;
    }

    private void initRestClient() {
        restClient = new RestClient.RestTemplateWithJsonAndString(false);
        restClient.setMessageConverters(
                Arrays.asList(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()));
    }

    public boolean postInstanceMetrics(final List<TMInstance> instances, long period) {
        ArrayList<Metric> metrics = new ArrayList<Metric>();
        String suffix = "_" + period / MILLISECONDS_PER_SECOND;
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            Map<String, String> dimensions = new HashMap<String, String>();
            dimensions.put(COORDINATE, instance.getCore().getCoordinate());
            long time = instance.getLatestMetricUpdateTimestamp();
            if (instance.getCpuUtilisation() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_CPU_IDLE_PERC + suffix, time,
                        MAX_CPU_UTILISATION - instance.getCpuUtilisation().doubleValue(), dimensions));
            }
            if (instance.getCpuStolen() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_CPU_STOLEN + suffix, time,
                        instance.getCpuStolen().doubleValue(), dimensions));
            }
            if (instance.getCpuSystem() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_CPU_SYSTEM + suffix, time,
                        instance.getCpuSystem().doubleValue(), dimensions));
            }
            if (instance.getCpuUser() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_CPU_USER + suffix, time,
                        instance.getCpuUser().doubleValue(), dimensions));
            }
            if (instance.getCpuWait() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_CPU_WAIT + suffix, time,
                        instance.getCpuWait().doubleValue(), dimensions));
            }
            if (instance.getDiskInodeUsed() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_DISK_INODE_USED + suffix, time,
                        instance.getDiskInodeUsed().doubleValue(), dimensions));
            }
            if (instance.getDiskSpaceUsed() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_DISK_SPACE_USED + suffix, time,
                        instance.getDiskSpaceUsed().doubleValue(), dimensions));
            }
            if (instance.getNetInBytesSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_IN_BYTES_SEC + suffix, time,
                        instance.getNetInBytesSec().doubleValue(), dimensions));
            }
            if (instance.getNetInErrorsSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_IN_ERRORS_SEC + suffix, time,
                        instance.getNetInErrorsSec().doubleValue(), dimensions));
            }
            if (instance.getNetInPacketsDroppedSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_IN_PACKETS_DROPPED_SEC + suffix, time,
                        instance.getNetInPacketsDroppedSec().doubleValue(), dimensions));
            }
            if (instance.getNetInPacketsSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_IN_PACKETS_SEC + suffix, time,
                        instance.getNetInPacketsSec().doubleValue(), dimensions));
            }
            if (instance.getNetOutBytesSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_OUT_BYTES_SEC + suffix, time,
                        instance.getNetOutBytesSec().doubleValue(), dimensions));
            }
            if (instance.getNetOutErrorsSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_OUT_ERRORS_SEC + suffix, time,
                        instance.getNetOutErrorsSec().doubleValue(), dimensions));
            }
            if (instance.getNetOutPacketsDroppedSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC + suffix, time,
                        instance.getNetOutPacketsDroppedSec().doubleValue(), dimensions));
            }
            if (instance.getNetOutPacketsSec() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NET_OUT_PACKETS_SEC + suffix, time,
                        instance.getNetOutPacketsSec().doubleValue(), dimensions));
            }
            if (instance.getFabricUtilisation() != null) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_FABRIC_LINK_UTILISATION + suffix, time,
                        instance.getFabricUtilisation().doubleValue(), dimensions));
            }
            if (instance.getCore().getFamUtilisation() != null) {
                LOG.debug("adding metric " + PREFIX_INSTANCE + Const.METRIC_FAM_UTILISATION + suffix + " with value = "
                        + instance.getCore().getFamUtilisation().doubleValue());
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_FAM_UTILISATION + suffix, time,
                        instance.getCore().getFamUtilisation().doubleValue(), dimensions));
            }
            if (instance.getCore().getDmaTotalDiscrepancyCount() >= 0) {
                metrics.add(new Metric(PREFIX_INSTANCE + Const.METRIC_NUM_DISCREPANCIES + suffix, time,
                        instance.getCore().getDmaTotalDiscrepancyCount(), dimensions));
            }

        }
        try {
            return postMetrics(metrics);
        } catch (IOException e) {
            LOG.error("Could not post metrics", e);
        }
        return false;
    }

    /**
     * add the metrics passed as parameter to the monitoring system
     *
     * @param metric ArrayList of Metric objects you want to add
     * @throws IOException
     */
    private boolean postMetrics(final ArrayList<Metric> metrics) throws IOException {
        if (metrics.size() == 0) {
            LOG.debug("Did not post instance metrics because there are none to update");
            return false;
        }
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jArray = mapper.createArrayNode();
        for (Metric metric : metrics) {
            JsonNode jMetric = mapper.valueToTree(metric);
            jArray.add(jMetric);
        }
        LOG.debug("Going to POST to: " + monServiceUrl + TMItemCollector.MON_METRICS);
        LOG.debug("JSON String that will be sent: " + jArray.toString());
        ResponseEntity<String> response =
                restClient.postForEntity(monServiceUrl + TMItemCollector.MON_METRICS, jArray, null);
        LOG.debug("POST Return code : " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        }
        return false;
    }

}
