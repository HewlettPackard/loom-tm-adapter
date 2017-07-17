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
package com.hp.hpl.loom.adapter.tm.updaters;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.util.Precision;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.AdapterUpdateResult;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.TMAdapter;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.SocBoard;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredSocBoard;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringEvent;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringMetric;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.Event;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.Metric;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMSoc;
import com.hp.hpl.loom.adapter.tm.items.TMSoc.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMSocUpdater extends TMAbstractUpdater<TMSoc, TMSoc.ItemAttributes, TMSocUpdater.MetricAndState> {
    private static final int COORDINATE_ENCNUM_LABEL = 9;
    private static final int COORDINATE_ENCNUM_VALUE = 10;
    private static final int COORDINATE_NODE_LABEL = 11;
    private static final int COORDINATE_NODE_VALUE = 12;

    public static class MetricAndState {
        private SocBoard state;
        private HashMap<String, Metric> metrics;
        private HashMap<String, Event> events;
        private DesiredSocBoard desired;
        private GetDmaStatus status;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.SocBoard discrepancies;

        public SocBoard getState() {
            return state;
        }

        public HashMap<String, Metric> getMetrics() {
            return metrics;
        }

        public HashMap<String, Event> getEvents() {
            return events;
        }
    }

    private BackEndMonitoringMetric metricUpdater;
    private BackEndMonitoringEvent eventUpdater;
    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private Map<String, List<String>> socCoordinateToIdToManifestId;
    private static final double MAX_CPU_UTILISATION = 100.0;

    private static final Log LOG = LogFactory.getLog(TMSocUpdater.class);

    @SuppressWarnings("checkstyle:parameternumber")
    public TMSocUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final BackEndMonitoringMetric metricUpdater, final BackEndMonitoringEvent eventUpdater,
            final Map<String, List<String>> socCoordinateToIdToManifestId) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector);
        this.metricUpdater = metricUpdater;
        this.eventUpdater = eventUpdater;
        this.dmaUpdater = dmaUpdater;
        this.socCoordinateToIdToManifestId = socCoordinateToIdToManifestId;
    }

    @Override
    protected Iterator<MetricAndState> getResourceIterator() {
        Iterator<DesiredSocBoard> dmaDIter = dmaUpdater.filterDesired(DesiredSocBoard.class);
        Iterator<SocBoard> dmaCIter = dmaUpdater.filterCurrent(SocBoard.class);
        Iterator<MetricAndState> resIter = new Iterator<TMSocUpdater.MetricAndState>() {
            @Override
            public MetricAndState next() {
                MetricAndState n = new MetricAndState();
                n.state = dmaCIter.next();
                n.desired = dmaDIter.next();
                n.discrepancies = dmaUpdater.getDiscrepancies().getSocBoardDiscrepanciesByPath(n.state.path);
                String nodeCoord = n.state.coordinate.split("/SocBoard")[0];
                n.metrics = metricUpdater.getMetricsForCoordinate(nodeCoord);
                n.events = eventUpdater.getEventsForCoordinate(nodeCoord);
                n.status = (GetDmaStatus) dmaUpdater.getStatusData();
                return n;
            }

            @Override
            public boolean hasNext() {
                return dmaDIter.hasNext() && dmaCIter.hasNext();
            }
        };
        return resIter;
    }

    @Override
    public AdapterUpdateResult updateItems(final long cEpoch) {
        long startTime = System.currentTimeMillis();
        AdapterUpdateResult ret = super.updateItems(cEpoch);
        long deltaT = System.currentTimeMillis() - startTime;

        if (LOG.isDebugEnabled()) {
            LOG.debug("TMSocUpdater.updateItems took " + deltaT + " ms.");
        }

        return ret;
    }

    @Override
    protected String getItemId(final MetricAndState resource) {
        return resource.state.coordinate;
    }

    @Override
    protected TMSoc createEmptyItem(final String logicalId) {
        return new TMSoc(logicalId, itemType);
    }

    /**
     * Populate the SoC core attributes with the values provided by the resource object
     *
     * @param item SoC item whose current state attributes will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.state.coordinate != null) {
            item.setCoordinate(resource.state.coordinate);
        }
        if (resource.state.path != null) {
            item.setPath(resource.state.path);
        }
        if (resource.state.soc.observed_power != null) {
            item.setPowerState(resource.state.soc.observed_power);
        }
        if (resource.state.soc.hostname != null) {
            item.setHostName(resource.state.soc.hostname);
        }
        if (resource.state.soc.observed_SocOSState != null) {
            item.setOsState(resource.state.soc.observed_SocOSState);
        }
        if (resource.state.soc.soc1 != null) {
            if (resource.state.soc.soc1.observed_NumCores != null) {
                item.setCores(resource.state.soc.soc1.observed_NumCores);
            }
            if (resource.state.soc.soc1.observed_DimmCount != null
                    && resource.state.soc.soc1.observed_DimmSize != null) {
                item.setDram(resource.state.soc.soc1.observed_DimmCount.doubleValue()
                        * resource.state.soc.soc1.observed_DimmSize.doubleValue());
            }
            if (resource.state.soc.soc1.observed_SFWVersion != null) {
                item.setSfwVersion(resource.state.soc.soc1.observed_SFWVersion);
            }
        }
        // TODO when supported by DMA: if system state is not null:
        // item.setSystemState("MY SYSTEM STATE");
        if (resource.state.soc.runningOsImageManifest != null) {
            String value = resource.state.soc.runningOsImageManifest;
            if (value.isEmpty()) {
                value = "None";
            }
            item.setRunningOsManifest(value);
        }
        if (resource.state.soc.nextOsImage.observed_status != null) {
            item.setOsManifestState(resource.state.soc.nextOsImage.observed_status);
        }
        // TODO when supported by DMA: if MAC address is not null:
        // item.setMacAddress("MY MAC ADDRESS");
        // TODO when supported by DMA: if IP Address is not null:
        // item.setIpAddress("MY IP ADDRESS");
    }

    /**
     * Populate the SoC desired core attributes with the values provided by the resource object
     *
     * @param item SoC item whose desired state will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setDesiredAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.desired.soc.power != null) {
            item.setDesiredPowerState(resource.desired.soc.power.value);
        }
        if (resource.desired.soc.localPower != null) {
            item.setDesiredLocalPower(resource.desired.soc.localPower.value);
        }
        if (resource.desired.soc.enableNonGracefulShutdown != null
                && resource.desired.soc.enableNonGracefulShutdown.getValue() != null) {
            item.setDesiredEnableNonGracefulShutdown(resource.desired.soc.enableNonGracefulShutdown.getValue());
        }
        // TODO when supported by DMA: if desired system state is not null:
        // item.setDesiredSystemState("MY DESIRED SYSTEM STATE");
        if (resource.desired.soc.runningOsImageManifest != null) {
            String value = resource.desired.soc.runningOsImageManifest.value;
            if (resource.desired.soc.runningOsImageManifest.value.equals("$TBD")) {
                value = "None";
            }
            item.setDesiredOsManifest(value);
        }
        if (resource.desired.soc.nextOsImage.manifest != null) {
            String value = resource.desired.soc.nextOsImage.manifest.value;
            if (value.isEmpty()) {
                value = "None";
            }
            item.setDesiredNextOsImageManifest(value);
        }
        if (resource.desired.soc.runningOsImageManifest != null) {
            item.setDesiredRunningOsImageManifest(resource.desired.soc.runningOsImageManifest.value);
        }
    }

    private void setDiscrepanciesAttributes(final MetricAndState resource, final ItemAttributes item) {
        int numTotalDiscrepancies = 0;
        int numLocalDiscrepancies = 0;

        if (resource.discrepancies != null) {
            if (resource.discrepancies.total_diff_all != null) {
                numTotalDiscrepancies = resource.discrepancies.total_diff_all.intValue();
            } else {
                LOG.debug("Missing total discrepancy data for " + item.getItemName() + ", assuming 0 discrepancies");
            }
            if (resource.discrepancies.total_diff != null) {
                numLocalDiscrepancies = resource.discrepancies.total_diff.intValue();
            } else {
                LOG.debug("Missing discrepancy data for " + item.getItemName() + ", assuming 0 discrepancies");
            }
        }

        item.setDmaTotalDiscrepancyCount(numTotalDiscrepancies);
        item.setDmaDiscrepancyCount(numLocalDiscrepancies);
        if (numTotalDiscrepancies == 0) {
            item.setDmaStatus("Sync");
        } else {
            item.setDmaStatus("Discrepant");
        }
    }

    private String getNetMetricName(final String baseName, final String netInterface) {
        return baseName + "." + netInterface;
    }

    private void setNetworkMetrics(final ItemAttributes item, final MetricAndState resource) {
        ArrayList<String> interfaces = metricUpdater.getNetworkInterfaces();
        if (interfaces.size() == 0) {
            return;
        }
        String iface = Const.DEFAULT_NETWORK_INTERFACE;
        if (!interfaces.contains(iface)) {
            interfaces.get(0);
        }
        String inBytesSec = getNetMetricName(Const.METRIC_NET_IN_BYTES_SEC, iface);
        String inErrsSec = getNetMetricName(Const.METRIC_NET_IN_ERRORS_SEC, iface);
        String inPacketsDroppedSec = getNetMetricName(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC, iface);
        String inPacketsSec = getNetMetricName(Const.METRIC_NET_IN_PACKETS_SEC, iface);
        String outBytesSec = getNetMetricName(Const.METRIC_NET_OUT_BYTES_SEC, iface);
        String outErrsSec = getNetMetricName(Const.METRIC_NET_OUT_ERRORS_SEC, iface);
        String outPacketsDroppedSec = getNetMetricName(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC, iface);
        String outPacketsSec = getNetMetricName(Const.METRIC_NET_OUT_PACKETS_SEC, iface);

        if (resource.metrics.containsKey(inBytesSec)) {
            item.setNetInBytesSec(resource.metrics.get(inBytesSec).getValue());
        }
        if (resource.metrics.containsKey(inErrsSec)) {
            item.setNetInErrorsSec(resource.metrics.get(inErrsSec).getValue());
        }
        if (resource.metrics.containsKey(inPacketsDroppedSec)) {
            item.setNetInPacketsDroppedSec(resource.metrics.get(inPacketsDroppedSec).getValue());
        }
        if (resource.metrics.containsKey(inPacketsSec)) {
            item.setNetInPacketsSec(resource.metrics.get(inPacketsSec).getValue());
        }
        if (resource.metrics.containsKey(outBytesSec)) {
            item.setNetOutBytesSec(resource.metrics.get(outBytesSec).getValue());
        }
        if (resource.metrics.containsKey(outErrsSec)) {
            item.setNetOutErrorsSec(resource.metrics.get(outErrsSec).getValue());
        }
        if (resource.metrics.containsKey(outPacketsDroppedSec)) {
            item.setNetOutPacketsDroppedSec(resource.metrics.get(outPacketsDroppedSec).getValue());
        }
        if (resource.metrics.containsKey(outPacketsSec)) {
            item.setNetOutPacketsSec(resource.metrics.get(outPacketsSec).getValue());
        }
    }

    /**
     * Populate the SoC metrics with the values provided by the resource object
     *
     * @param item SoC item whose metric values will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void setMetrics(final ItemAttributes item, final MetricAndState resource) {
        if (resource.metrics != null) {
            Boolean useFakeData = ((TMAdapter) adapter).getUseFakeData();

            // If the SoC is off then don't project some of the fake metrics
            if (item.getPowerState().toLowerCase().equals("off") && useFakeData != null && useFakeData) {
                item.setCpuUtilisation(0);
                item.setDramUtilisation(0);
                item.setBridgeIciLinkUtilisation(0);
            } else {
                if (resource.metrics.containsKey(Const.METRIC_CPU_IDLE_PERC)) {
                    item.setCpuUtilisation(
                            MAX_CPU_UTILISATION - resource.metrics.get(Const.METRIC_CPU_IDLE_PERC).getValue());
                }
                if (resource.metrics.containsKey(Const.METRIC_MEMORY_TOTAL_MB)
                        && resource.metrics.containsKey(Const.METRIC_MEMORY_FREE_MB)) {
                    double total = resource.metrics.get(Const.METRIC_MEMORY_TOTAL_MB).getValue();
                    double free = resource.metrics.get(Const.METRIC_MEMORY_FREE_MB).getValue();

                    item.setDramUtilisation(Precision.round(((total - free) / total) * 100.0, 2));
                }
                double avgIciUtil = FabricLinkUtil.getBridgeFabricLinkUtilisationAvg(resource,
                        BackEndMonitoringMetric.ZBRIDGE_ICI_PORTS);
                if (avgIciUtil >= 0) {
                    item.setBridgeIciLinkUtilisation(avgIciUtil);
                }
            }

            double avgFabricUtil = FabricLinkUtil.getBridgeFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZBRIDGE_EXTERNAL_PORTS);
            if (avgFabricUtil >= 0) {
                item.setBridgeFabricLinkUtilisation(avgFabricUtil);
            }
            double avgFabricPsyUtil = FabricLinkUtil.getBridgeFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZBRIDGE_PSYLOCKE_PORTS);
            if (avgFabricPsyUtil >= 0) {
                item.setBridgePsylockeUtilisation(avgFabricPsyUtil);
            }
            if (resource.metrics.containsKey(Const.METRIC_BRIDGE_FABRIC_LINK_REQUEST_QUEUE_UTILISATION)) {
                item.setBridgeIciLinkBandwidth(
                        resource.metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_REQUEST_QUEUE_UTILISATION).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_BRIDGE_ICI_TRANSACTION_TYPE_COUNTS)) {
                item.setBridgeIciTransactionTypeCounts(
                        resource.metrics.get(Const.METRIC_BRIDGE_ICI_TRANSACTION_TYPE_COUNTS).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_BRIDGE_HOME_AGENT_AVERAGE_READ_LATENCY)) {
                item.setBridgeHomeAgentAverageReadLatency(
                        resource.metrics.get(Const.METRIC_BRIDGE_HOME_AGENT_AVERAGE_READ_LATENCY).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_BRIDGE_HOME_AGENT_REQUEST_QUEUE_UTILIZATION)) {
                item.setBridgeHomeAgentRequestQueueUtilization(
                        resource.metrics.get(Const.METRIC_BRIDGE_HOME_AGENT_REQUEST_QUEUE_UTILIZATION).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_CPU_STOLEN)) {
                item.setCpuStolen(resource.metrics.get(Const.METRIC_CPU_STOLEN).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_CPU_SYSTEM)) {
                item.setCpuSystem(resource.metrics.get(Const.METRIC_CPU_SYSTEM).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_CPU_USER)) {
                item.setCpuUser(resource.metrics.get(Const.METRIC_CPU_USER).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_CPU_WAIT)) {
                item.setCpuWait(resource.metrics.get(Const.METRIC_CPU_WAIT).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_DISK_INODE_USED)) {
                item.setDiskInodeUsed(resource.metrics.get(Const.METRIC_DISK_INODE_USED).getValue());
            }
            if (resource.metrics.containsKey(Const.METRIC_DISK_SPACE_USED)) {
                item.setDiskSpaceUsed(resource.metrics.get(Const.METRIC_DISK_SPACE_USED).getValue());
            }
            setNetworkMetrics(item, resource);
        }
    }

    private void setEvents(final ItemAttributes item, final MetricAndState resource) {
        if (resource.events != null) {
            if (resource.events.containsKey(Const.EVENT_OS_BOOTED)) {
                ObjectMapper mapper = new ObjectMapper();
                String osBooted = resource.events.get(Const.EVENT_OS_BOOTED).getEventData();
                try {
                    JsonNode jOsBooted = mapper.readTree(osBooted);
                    String osBootTime = jOsBooted.get(Const.BOOT_TIME).asText();
                    String osUpTime = jOsBooted.get(Const.UP_TIME).asText();

                    if (osBootTime != null) {
                        item.setOsBootTime(formatTime(osBootTime));
                    }

                    if (osUpTime != null) {
                        item.setOsUpTime(formatElapsedTime(Long.parseLong(osUpTime)));
                    }
                } catch (JsonProcessingException e) {
                    LOG.warn("Failed to parse OS Booted Event : " + osBooted, e);
                } catch (IOException e) {
                    LOG.warn("I/O Exception while parsing OS Booted Event : " + osBooted, e);
                    e.printStackTrace();
                }
            }
        }
    }

    private String formatTime(final String osBootTime) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(osBootTime));
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);

        return zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private String formatElapsedTime(final long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        int days = (int) ((milliseconds / (1000 * 60 * 60 * 24)));

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    /**
     * Populate the SoC item with the values provided by the resource object
     *
     * @param resource MetricAndState object containing the information that will be used
     */
    @Override
    protected ItemAttributes createItemAttributes(final MetricAndState resource) {
        ItemAttributes item = new ItemAttributes();
        String name = resource.state.coordinate.substring(0, resource.state.coordinate.lastIndexOf('/'));
        String[] components = name.split(Pattern.quote("/"));

        // item.setItemName(resource.state.coordinate.substring(name.lastIndexOf('/') + 1));
        item.setItemName(components[COORDINATE_ENCNUM_LABEL] + "/" + components[COORDINATE_ENCNUM_VALUE] + "/"
                + components[COORDINATE_NODE_LABEL] + "/" + components[COORDINATE_NODE_VALUE]);
        setAttributes(item, resource);
        setDesiredAttributes(item, resource);
        setMetrics(item, resource);
        setDiscrepanciesAttributes(resource, item);
        setEvents(item, resource);
        super.setStatusAttributes(resource.status, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final MetricAndState resource) {
        if (resource.metrics != null) {
            if (resource.metrics.containsKey(Const.METRIC_CPU_IDLE_PERC) && itemAttr.getCpuUtilisation() != null
                    && (MAX_CPU_UTILISATION - resource.metrics.get(Const.METRIC_CPU_IDLE_PERC).getValue()) != itemAttr
                            .getCpuUtilisation().doubleValue()) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            if (areDifferent(resource.metrics, Const.METRIC_DRAM_UTILISATION, itemAttr.getDramUtilisation())) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            double avgFabricUtil = FabricLinkUtil.getBridgeFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZBRIDGE_EXTERNAL_PORTS);
            if (itemAttr != null && itemAttr.getBridgeFabricLinkUtilisation() != null
                    && avgFabricUtil != itemAttr.getBridgeFabricLinkUtilisation().doubleValue()) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            double avgIciUtil = FabricLinkUtil.getBridgeFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZBRIDGE_ICI_PORTS);
            if (itemAttr != null && itemAttr.getBridgeIciLinkUtilisation() != null
                    && avgIciUtil != itemAttr.getBridgeIciLinkUtilisation().doubleValue()) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            if (areDifferent(resource.metrics, Const.METRIC_BRIDGE_FABRIC_LINK_REQUEST_QUEUE_UTILISATION,
                    itemAttr.getBridgeFabricLinkRequestQueueUtilisation())) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            if (areDifferent(resource.metrics, Const.METRIC_BRIDGE_ICI_TRANSACTION_TYPE_COUNTS,
                    itemAttr.getBridgeIciTransactionTypeCounts())) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            if (areDifferent(resource.metrics, Const.METRIC_BRIDGE_HOME_AGENT_AVERAGE_READ_LATENCY,
                    itemAttr.getBridgeHomeAgentAverageReadLatency())) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            if (areDifferent(resource.metrics, Const.METRIC_BRIDGE_HOME_AGENT_REQUEST_QUEUE_UTILIZATION,
                    itemAttr.getBridgeHomeAgentRequestQueueUtilization())) {
                return ChangeStatus.CHANGED_UPDATE;
            }
        }
        // TODO at some point the following value will need to be ChangeStatus.CHANGED_IGNORE
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final MetricAndState resource) {
        String desiredManifest = resource.desired.soc.nextOsImage.manifest.value;
        String manifest = resource.state.soc.runningOsImageManifest;
        if (desiredManifest != null && !desiredManifest.isEmpty() && !desiredManifest.equals("$TBD")) {
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.MANIFEST_LOCAL_ID, desiredManifest,
                    RelationshipNames.ASSIGN);
        }
        if (manifest != null && !manifest.isEmpty() && !manifest.equals("$TBD")) {
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.MANIFEST_LOCAL_ID, manifest,
                    RelationshipNames.ASSIGN);
        }
        // List<String> manifestIds = socCoordinateToIdToManifestId.get(resource.state.coordinate);
        // if (manifestIds != null) {
        // for (String manifestId : manifestIds) {
        // connectedItem.setRelationshipWithType(adapter.getProvider(), Types.MANIFEST_LOCAL_ID,
        // manifestId,
        // RelationshipNames.ASSIGN);
        // }
        // }
    }

}
