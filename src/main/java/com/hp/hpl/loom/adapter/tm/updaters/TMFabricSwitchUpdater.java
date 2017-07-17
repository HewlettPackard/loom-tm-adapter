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

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AdapterUpdateResult;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.IZoneBoard;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredIZoneBoard;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringMetric;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.Metric;
import com.hp.hpl.loom.adapter.tm.items.TMFabricSwitch;
import com.hp.hpl.loom.adapter.tm.items.TMFabricSwitch.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMFabricSwitchUpdater
        extends TMAbstractUpdater<TMFabricSwitch, TMFabricSwitch.ItemAttributes, TMFabricSwitchUpdater.MetricAndState> {

    public static class MetricAndState {
        private IZoneBoard current;
        private DesiredIZoneBoard desired;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.IZoneBoard discrepancies;
        private HashMap<String, Metric> metrics;
        private GetDmaStatus status;

        public IZoneBoard getCurrent() {
            return current;
        }

        public HashMap<String, Metric> getMetrics() {
            return metrics;
        }
    }

    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private BackEndMonitoringMetric metricUpdater;

    private static final Log LOG = LogFactory.getLog(TMFabricSwitchUpdater.class);

    public TMFabricSwitchUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final BackEndMonitoringMetric metricUpdater) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector);
        this.dmaUpdater = dmaUpdater;
        this.metricUpdater = metricUpdater;
    }

    @Override
    protected Iterator<MetricAndState> getResourceIterator() {
        Iterator<IZoneBoard> dmaCIter = dmaUpdater.filterCurrent(IZoneBoard.class);
        Iterator<DesiredIZoneBoard> dmaDIter = dmaUpdater.filterDesired(DesiredIZoneBoard.class);
        Iterator<MetricAndState> res = new Iterator<MetricAndState>() {
            @Override
            public boolean hasNext() {
                return dmaCIter.hasNext() && dmaDIter.hasNext();
            }

            @Override
            public MetricAndState next() {
                MetricAndState n = new MetricAndState();
                n.current = dmaCIter.next();
                n.desired = dmaDIter.next();
                n.discrepancies = dmaUpdater.getDiscrepancies().getIzoneDiscrepanciesByPath(n.current.path);
                String nodeCoord = n.current.coordinate.split("/IZoneBoard")[0];
                n.metrics = metricUpdater.getMetricsForCoordinate(nodeCoord);
                n.status = (GetDmaStatus) dmaUpdater.getStatusData();
                return n;
            }
        };
        return res;
    }

    @Override
    public AdapterUpdateResult updateItems(final long cEpoch) {
        long startTime = System.currentTimeMillis();
        AdapterUpdateResult ret = super.updateItems(cEpoch);
        long deltaT = System.currentTimeMillis() - startTime;
        LOG.debug("TMFabricSwitchUpdater.updateItems took " + deltaT + " ms.");
        return ret;
    }

    @Override
    protected String getItemId(final MetricAndState resource) {
        return resource.current.coordinate;
    }

    @Override
    protected TMFabricSwitch createEmptyItem(final String logicalId) {
        return new TMFabricSwitch(logicalId, itemType);
    }

    /**
     * Populate the IzoneBoard core attributes with the values provided by the resource object
     *
     * @param item IzoneBoard item whose current state attributes will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.current.coordinate != null) {
            item.setCoordinate(resource.current.coordinate);
            if (resource.current.coordinate.endsWith("1")) {
                item.setPosition("Upper");
            } else if (resource.current.coordinate.endsWith("2")) {
                item.setPosition("Lower");
            }
        }
        if (resource.current.fabric != null && resource.current.fabric.observed_power != null) {
            item.setPowerState(resource.current.fabric.observed_power);
        }
        if (resource.current.iZoneBoardMp != null && resource.current.iZoneBoardMp.observed_MFWVersion != null) {
            item.setMpFirmwareVersion(resource.current.iZoneBoardMp.observed_MFWVersion);
        }
        if (resource.current.switchMhwFpga != null && resource.current.switchMhwFpga.observed_FPGAVersion != null) {
            item.setFpgaVersion(resource.current.switchMhwFpga.observed_FPGAVersion);
        }
    }

    /**
     * Populate the IzoneBoard desired core attributes with the values provided by the resource
     * object
     *
     * @param item IzoneBoard item whose desired state will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setDesiredAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.desired.fabric.power != null) {
            item.setDesiredPowerState(resource.desired.fabric.power.value);
        }
    }

    /**
     * Populate the IzoneBoard metrics with the values provided by the resource object
     *
     * @param item IzoneBoard item whose metric values will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setMetrics(final ItemAttributes item, final MetricAndState resource) {
        if (resource.metrics != null) {
            double zSwitchIntUsageAvg = FabricLinkUtil.getZswitchFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZSWITCH_TO_ZBRIDGE_PORTS);
            if (zSwitchIntUsageAvg >= 0) {
                item.setFabricLinkIntUtilisation(zSwitchIntUsageAvg);
            }
            double zSwitchExtUsageAvg = FabricLinkUtil.getZswitchFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZSWITCH_TO_ZSWITCH_PORTS);
            if (zSwitchExtUsageAvg >= 0) {
                item.setFabricLinkExtUtilisation(zSwitchExtUsageAvg);
            }
            if (resource.metrics.containsKey(Const.METRIC_FABRIC_SWITCH_CORE_ARB_BLOCKED)) {
                item.setFabricSwitchCoreArbBlocked(
                        resource.metrics.get(Const.METRIC_FABRIC_SWITCH_CORE_ARB_BLOCKED).getValue());
            }
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

    @Override
    protected ItemAttributes createItemAttributes(final MetricAndState resource) {
        ItemAttributes item = new ItemAttributes();
        String name = resource.current.coordinate.substring(0, resource.current.coordinate.lastIndexOf('/'));
        item.setItemName(resource.current.coordinate.substring(name.lastIndexOf('/') + 1));
        setAttributes(item, resource);
        setDesiredAttributes(item, resource);
        setMetrics(item, resource);
        setDiscrepanciesAttributes(resource, item);
        super.setStatusAttributes(resource.status, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final MetricAndState resource) {
        if (resource.metrics != null) {
            double zSwitchIntUsageAvg = FabricLinkUtil.getZswitchFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZSWITCH_TO_ZBRIDGE_PORTS);
            if (itemAttr != null && itemAttr.getFabricLinkIntUtilisation() != null
                    && zSwitchIntUsageAvg != itemAttr.getFabricLinkIntUtilisation().doubleValue()) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            double zSwitchExtUsageAvg = FabricLinkUtil.getZswitchFabricLinkUtilisationAvg(resource,
                    BackEndMonitoringMetric.ZSWITCH_TO_ZSWITCH_PORTS);
            if (itemAttr != null && itemAttr.getFabricLinkExtUtilisation() != null
                    && zSwitchExtUsageAvg != itemAttr.getFabricLinkExtUtilisation().doubleValue()) {
                return ChangeStatus.CHANGED_UPDATE;
            }
            if (areDifferent(resource.metrics, Const.METRIC_FABRIC_SWITCH_CORE_ARB_BLOCKED,
                    itemAttr.getFabricSwitchCoreArbBlocked())) {
                return ChangeStatus.CHANGED_UPDATE;
            }
        }
        // TODO at some point the following value will need to be ChangeStatus.CHANGED_IGNORE
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final MetricAndState resource) {}

}
