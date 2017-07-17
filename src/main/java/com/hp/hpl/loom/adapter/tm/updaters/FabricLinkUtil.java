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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.EnclosureLink;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.FabricLink;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringMetric;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.Metric;

public final class FabricLinkUtil {

    private static final Log LOG = LogFactory.getLog(FabricLinkUtil.class);
    private static final int MAX_PERCENT = 100;

    private FabricLinkUtil() {}

    private static Map<Integer, List<Double>> putFabricLinkInfo(Map<Integer, List<Double>> portsMaxValsMap,
            FabricLink link) {
        if (link == null || link.observed_PortNumber == null || link.observed_TxBandwidth == null
                || link.observed_RxBandwidth == null) {
            LOG.debug("no data available from FabricLink, skipping this mapping");
        } else {
            int port = link.observed_PortNumber.intValue();
            double maxTx = link.observed_TxBandwidth.doubleValue();
            double maxRx = link.observed_RxBandwidth.doubleValue();
            portsMaxValsMap.put(port, Arrays.asList(maxTx, maxRx));
        }
        return portsMaxValsMap;
    }

    private static Map<Integer, List<Double>> putEnclosureLinkInfo(Map<Integer, List<Double>> portsMaxValsMap,
            EnclosureLink link) {
        if (link == null || link.observed_PortNumber == null || link.observed_TxBandwidth == null
                || link.observed_RxBandwidth == null) {
            LOG.debug("no data available from EnclosureLink, skipping this mapping");
        } else {
            int port = link.observed_PortNumber.intValue();
            double maxTx = link.observed_TxBandwidth.doubleValue();
            double maxRx = link.observed_RxBandwidth.doubleValue();
            portsMaxValsMap.put(port, Arrays.asList(maxTx, maxRx));
        }
        return portsMaxValsMap;
    }

    private static Map<Integer, List<Double>> getPortsToMaxValuesMappingBridge(
            final TMSocUpdater.MetricAndState resource) {
        Map<Integer, List<Double>> portsMaxValsMap = new HashMap<Integer, List<Double>>();
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink1);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink2);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink3);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink4);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink5);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink6);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink7);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink8);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink9);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink10);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink11);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getState().fabric.fabricLink12);
        return portsMaxValsMap;
    }

    private static Map<Integer, List<Double>> getPortsToMaxValuesMappingSwitch(
            final TMFabricSwitchUpdater.MetricAndState resource) {
        Map<Integer, List<Double>> portsMaxValsMap = new HashMap<Integer, List<Double>>();
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink1);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink2);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink3);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink4);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink5);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink6);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink7);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink8);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink9);
        portsMaxValsMap = putFabricLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.fabricLink10);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink1);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink2);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink3);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink4);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink5);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink6);
        portsMaxValsMap = putEnclosureLinkInfo(portsMaxValsMap, resource.getCurrent().fabric.zswitch.enclosureLink7);
        return portsMaxValsMap;
    }

    private static double getFabricUtilisationAvg(Map<String, Metric> metrics,
            Map<Integer, List<Double>> portsMaxValsMap, List<Integer> ports) {
        double totalMaxRxTx = 0;
        double totalRxTx = 0;
        for (int port : ports) {
            if (!portsMaxValsMap.containsKey(port)) {
                LOG.debug("port " + port + " not found in list");
                continue;
            }
            String txName = BackEndMonitoringMetric.getFabricMetricName(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION,
                    String.valueOf(port), BackEndMonitoringMetric.TX);
            String rxName = BackEndMonitoringMetric.getFabricMetricName(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION,
                    String.valueOf(port), BackEndMonitoringMetric.RX);
            double maxTx = portsMaxValsMap.get(port).get(0);
            double maxRx = portsMaxValsMap.get(port).get(1);
            if (metrics.containsKey(txName)) {
                double txVal = metrics.get(txName).getValue();
                if (maxTx > -1) {
                    totalRxTx += txVal;
                    totalMaxRxTx += maxTx;
                }
            }
            if (metrics.containsKey(rxName)) {
                double rxVal = metrics.get(rxName).getValue();
                if (maxRx > -1) {
                    totalRxTx += rxVal;
                    totalMaxRxTx += maxRx;
                }
            }
        }
        if (totalMaxRxTx == 0) {
            return -1;
        }
        return MAX_PERCENT * totalRxTx / totalMaxRxTx;
    }

    protected static double getBridgeFabricLinkUtilisationAvg(final TMSocUpdater.MetricAndState resource,
            List<Integer> zBridgePorts) {
        Map<Integer, List<Double>> portsMaxValsMap = getPortsToMaxValuesMappingBridge(resource);
        return getFabricUtilisationAvg(resource.getMetrics(), portsMaxValsMap, zBridgePorts);
    }

    protected static double getZswitchFabricLinkUtilisationAvg(final TMFabricSwitchUpdater.MetricAndState resource,
            List<Integer> zSwitchPorts) {
        Map<Integer, List<Double>> portsMaxValsMap = getPortsToMaxValuesMappingSwitch(resource);
        return getFabricUtilisationAvg(resource.getMetrics(), portsMaxValsMap, zSwitchPorts);


    }

}
