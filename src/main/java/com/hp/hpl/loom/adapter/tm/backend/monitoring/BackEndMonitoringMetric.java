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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;

public class BackEndMonitoringMetric extends TimedUpdater {

    private static final Log LOG = LogFactory.getLog(BackEndMonitoringMetric.class);

    private RestClient net;
    private final String route;
    private String fullRoute;
    private HashMap<String, HashMap<String, Metric>> metricsByCoordinateAndName;
    private ArrayList<String> networkInterfaces;
    private MetricDisplay metricsBlob;

    private static List<String> metricsToCollect = Arrays.asList(Const.METRIC_CPU_IDLE_PERC, Const.METRIC_CPU_STOLEN,
            Const.METRIC_CPU_SYSTEM, Const.METRIC_CPU_USER, Const.METRIC_CPU_WAIT, Const.METRIC_DISK_INODE_USED,
            Const.METRIC_DISK_SPACE_USED, Const.METRIC_NET_IN_BYTES_SEC, Const.METRIC_NET_IN_ERRORS_SEC,
            Const.METRIC_NET_IN_PACKETS_DROPPED_SEC, Const.METRIC_NET_IN_PACKETS_SEC, Const.METRIC_NET_OUT_BYTES_SEC,
            Const.METRIC_NET_OUT_ERRORS_SEC, Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC, Const.METRIC_NET_OUT_PACKETS_SEC,
            Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION, Const.METRIC_ZSWITCH_FABRIC_LINK_UTILISATION,
            Const.METRIC_MEMORY_FREE_MB, Const.METRIC_MEMORY_TOTAL_MB);

    public static final List<Integer> ZBRIDGE_ICI_PORTS = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
    public static final List<Integer> ZBRIDGE_PSYLOCKE_PORTS = Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15);
    public static final List<Integer> ZBRIDGE_EXTERNAL_PORTS = Arrays.asList(16, 17, 18, 19);
    public static final List<Integer> ZSWITCH_TO_ZBRIDGE_PORTS = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    public static final List<Integer> ZSWITCH_TO_ZSWITCH_PORTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17);

    public static final String PORT = "port";
    public static final String DIRECTION = "direction";
    public static final String TX = "tx";
    public static final String RX = "rx";

    public BackEndMonitoringMetric(final RestClient netInterface, final String currentRoute) {
        net = netInterface;
        route = currentRoute;
        metricsByCoordinateAndName = new HashMap<String, HashMap<String, Metric>>();
        networkInterfaces = new ArrayList<String>();
    }

    private static boolean isNetworkMetric(final String metricName) {
        return metricName.startsWith("net.");
    }

    /**
     * Create the Full route according to the metrics we want to collect. We will only ask for the
     * last value of each metric (samples=1)
     *
     * @return String representing the full route
     */
    private String getFullRoute() {
        String fRoute = route + "?metric=";
        for (String metricName : metricsToCollect) {
            fRoute += metricName + ",";
        }
        long startTime = getStartTime();
        fRoute = fRoute.substring(0, fRoute.length() - 1) + "&startTime=" + startTime + "&samples=1";

        if (LOG.isDebugEnabled()) {
            LOG.debug("Full route to monitoring service: " + fRoute);
        }

        return fRoute;
    }

    /**
     * Calls the Monitoring Service REST interface, requesting the latest value of each metric, then
     * parses the result and stores the update in metricsByNameAndCoordinate
     */
    public void refreshResult() {
        long t0 = System.currentTimeMillis();
        fullRoute = getFullRoute();
        metricsBlob = net.getAllResources(fullRoute, MetricDisplay.class);
        if (metricsBlob != null) {
            JsonNode jMetrics = metricsBlob.getMetrics();
            try {
                parseMetrics(jMetrics);
            } catch (JsonProcessingException e) {
                LOG.error("Couldn't parse metrics", e);
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No metrics update from " + fullRoute);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Monitoring refresh total execution time: " + (System.currentTimeMillis() - t0) + " ms.");
        }
    }

    private void parseAndStoreValues(final JsonNode jSerie, final ObjectMapper mapper, final String metricName,
            final String coordinate, final HashMap<String, Integer> columns) throws JsonProcessingException {
        JsonNode jValues = jSerie.get("values");
        JsonNode jTags = jSerie.get("tags");
        if (jValues.isArray()) {
            for (final JsonNode jValue : jValues) {
                parseAndStoreValue(jValue, mapper, metricName, coordinate, columns, jTags);
            }
        }
    }

    private long getTimestamp(final ArrayList<JsonNode> jVals, final HashMap<String, Integer> columns,
            final ObjectMapper mapper) throws JsonProcessingException {
        int timePos = columns.get("time");
        JsonNode jTimestamp = jVals.get(timePos);
        return mapper.treeToValue(jTimestamp, Long.class);
    }

    private double getValue(final ArrayList<JsonNode> jVals, final HashMap<String, Integer> columns,
            final ObjectMapper mapper) throws JsonProcessingException {
        JsonNode jMetricValue = jVals.get(columns.get("value"));
        return mapper.treeToValue(jMetricValue, Double.class);
    }

    private Map<String, String> getDimensions(final ArrayList<JsonNode> jVals, final JsonNode jTags,
            final ObjectMapper mapper) throws JsonProcessingException {
        Map<String, String> dimensions = new HashMap<String, String>();
        Iterator<String> tagIter = jTags.fieldNames();
        while (tagIter.hasNext()) {
            String tagName = tagIter.next();
            JsonNode jTagVal = jTags.get(tagName);
            String tagVal = mapper.treeToValue(jTagVal, String.class);

            if (LOG.isDebugEnabled()) {
                LOG.debug("tag : " + tagName + " : " + tagVal);
            }

            dimensions.put(tagName, tagVal);
        }
        return dimensions;
    }

    private void parseAndStoreValue(final JsonNode jValue, final ObjectMapper mapper, final String metricName,
            final String coordinate, final HashMap<String, Integer> columns, final JsonNode jTags)
            throws JsonProcessingException {
        ArrayList<JsonNode> jVals = new ArrayList<JsonNode>();
        if (jValue.isArray()) {
            for (final JsonNode jVal : jValue) {
                jVals.add(jVal);
            }
        }
        long timestamp = getTimestamp(jVals, columns, mapper);
        double value = getValue(jVals, columns, mapper);
        Map<String, String> dimensions = getDimensions(jVals, jTags, mapper);
        Metric metric = new Metric(metricName, timestamp, value, dimensions);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Putting metric: " + metricName + " for coordinate: " + coordinate);
        }

        putMetric(metricName, coordinate, metric);
    }

    private String getDevice(final JsonNode jSerie, final ObjectMapper mapper) throws JsonProcessingException {
        JsonNode jDevice = jSerie.get("tags").get("device");
        if (null != jDevice) {
            return mapper.treeToValue(jDevice, String.class);
        }
        return "";
    }

    private void rememberNetworkInterface(final String device) {
        if (!networkInterfaces.contains(device)) {
            networkInterfaces.add(device);
        }
    }

    private String getMetricName(final JsonNode jSerie, final ObjectMapper mapper, final String device)
            throws JsonProcessingException {
        JsonNode jName = jSerie.get("name");
        String metricName = mapper.treeToValue(jName, String.class);
        if (isNetworkMetric(metricName)) {
            metricName = metricName + "." + device;
            rememberNetworkInterface(device);
        }
        return metricName;
    }

    private String getCoordinate(final JsonNode jSerie, final ObjectMapper mapper) throws JsonProcessingException {
        JsonNode jCoordinate = jSerie.get("tags").get("coordinate");
        return mapper.treeToValue(jCoordinate, String.class);
    }

    private HashMap<String, Integer> getColumns(final JsonNode jSerie, final ObjectMapper mapper)
            throws JsonProcessingException {
        HashMap<String, Integer> columns = new HashMap<String, Integer>();
        JsonNode jColumns = jSerie.get("columns");
        int i = 0;
        if (jColumns.isArray()) {
            for (final JsonNode jColumn : jColumns) {
                String columnName = mapper.treeToValue(jColumn, String.class);
                columns.put(columnName, i);
                i++;
            }
        }
        return columns;
    }

    private void parseSeries(final JsonNode jSeries) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        for (final JsonNode jSerie : jSeries) {
            String device = getDevice(jSerie, mapper);
            String metricName = getMetricName(jSerie, mapper, device);
            String coordinate = getCoordinate(jSerie, mapper);
            HashMap<String, Integer> columns = getColumns(jSerie, mapper);
            parseAndStoreValues(jSerie, mapper, metricName, coordinate, columns);
        }
    }

    /**
     * Parses the JsonNode to an ArrayList of metrics, assuming the JsonNode has the "series" format
     * returned by the Monitoring Service Rest API, under the "metrics" node. The parsed metrics are
     * saved into the metricsByNameAndCoordinate HashMap
     *
     * @param jMetrics JsonNode object representing the "metrics" node returned by Monitoring
     *        Service
     * @throws JsonProcessingException
     */
    protected void parseMetrics(final JsonNode jMetrics) throws JsonProcessingException {
        if (jMetrics.isArray()) {
            for (final JsonNode jMetric : jMetrics) {
                JsonNode jSeries = jMetric.get("series");
                if (jSeries.isArray()) {
                    parseSeries(jSeries);
                }
            }
        }
    }

    /**
     * Private function to store a Metric in metricsByNameAndCoordinate, it will update
     * latestUpdateTime if necessary too
     *
     * @param name metric name (inner hash)
     * @param coordinate metric coordinate (outer hash)
     * @param metric Metric Object to store
     */
    protected void putMetric(String name, final String coordinate, final Metric metric) {
        if (metricsByCoordinateAndName.get(coordinate) == null) {
            metricsByCoordinateAndName.put(coordinate, new HashMap<String, Metric>());
        }
        if (metric.getTimestamp() > getLastUpdateTime()) {
            setLastUpdateTime(metric.getTimestamp());
        }
        if (name.equalsIgnoreCase(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION)
                || name.equalsIgnoreCase(Const.METRIC_ZSWITCH_FABRIC_LINK_UTILISATION)) {
            name = getFabricMetricName(name, metric.getDimensions().get(PORT), metric.getDimensions().get(DIRECTION));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("put metric with name = " + name + " and coordinate = " + coordinate);
        }

        metricsByCoordinateAndName.get(coordinate).put(name, metric);
    }

    public static String getFabricMetricName(final String name, final String port, final String direction) {
        return name + "_" + port + "_" + direction;
    }

    /**
     * Returns a HashMap of Metrics (key = metric name) corresponding to the coordinate value passed
     * as parameter. If the Metric was not provided in the latest refresh (i.e. not available in
     * this.data, the previous Metric value will be returned.
     *
     * @param rackLevelCoordinate coordinate (starting at rack level) for which we want the metrics
     * @return HashMap of Metrics associated to the coordinate
     */
    public HashMap<String, Metric> getMetricsForCoordinate(final String coordinate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get metrics for coordinate: " + coordinate);
        }

        return metricsByCoordinateAndName.get(coordinate);
    }

    public ArrayList<String> getNetworkInterfaces() {
        return networkInterfaces;
    }
}
