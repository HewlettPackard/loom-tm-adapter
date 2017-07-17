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
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.MemoryBoard;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredMemoryBoard;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianMemory.MemoryDetails;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringMetric;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.Metric;
import com.hp.hpl.loom.adapter.tm.items.TMMemoryBoard;
import com.hp.hpl.loom.adapter.tm.items.TMMemoryBoard.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMMemoryBoardUpdater
        extends TMAbstractUpdater<TMMemoryBoard, TMMemoryBoard.ItemAttributes, TMMemoryBoardUpdater.MetricAndState> {

    public static class MetricAndState {
        private MemoryBoard state;
        private HashMap<String, Metric> metrics;
        private DesiredMemoryBoard desired;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.MemoryBoard discrepancies;
        private String alert;
        private MemoryDetails memory;
        private Long bookSize;
        private GetDmaStatus status;
    }

    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private BackEndMonitoringMetric metricUpdater;
    private LibrarianRestClient librarianRestClient;
    private static final Double MULTIPLIER = 1000000000.0;

    private static final Log LOG = LogFactory.getLog(TMMemoryBoardUpdater.class);

    @SuppressWarnings("checkstyle:parameternumber")
    public TMMemoryBoardUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final BackEndMonitoringMetric metricUpdater, final LibrarianRestClient librarianRestClient)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector);
        this.metricUpdater = metricUpdater;
        this.dmaUpdater = dmaUpdater;
        this.librarianRestClient = librarianRestClient;
    }

    @Override
    protected Iterator<MetricAndState> getResourceIterator() {
        Iterator<MemoryBoard> dmaCIter = dmaUpdater.filterCurrent(MemoryBoard.class);
        Iterator<DesiredMemoryBoard> dmaDIter = dmaUpdater.filterDesired(DesiredMemoryBoard.class);
        Iterator<MetricAndState> resIter = new Iterator<TMMemoryBoardUpdater.MetricAndState>() {
            @Override
            public MetricAndState next() {
                MetricAndState n = new MetricAndState();
                n.desired = dmaDIter.next();
                n.state = dmaCIter.next();
                n.discrepancies = dmaUpdater.getDiscrepancies().getMemoryBoardDiscrepanciesByPath(n.state.path);
                String nodeCoord = n.state.coordinate.split("/MemoryBoard")[0];
                n.metrics = metricUpdater.getMetricsForCoordinate(nodeCoord);
                n.memory = librarianRestClient.getMemoryFromCoordinate(n.state.coordinate);
                n.bookSize = librarianRestClient.getBookSize();
                n.status = (GetDmaStatus) dmaUpdater.getStatusData();
                return n;
            }

            @Override
            public boolean hasNext() {
                return dmaCIter.hasNext() && dmaDIter.hasNext();
            }
        };
        return resIter;
    }

    @Override
    public AdapterUpdateResult updateItems(final long cEpoch) {
        long startTime = System.currentTimeMillis();
        AdapterUpdateResult ret = super.updateItems(cEpoch);
        long deltaT = System.currentTimeMillis() - startTime;
        LOG.debug("TMMemoryBoardUpdater.updateItems took " + deltaT + " ms.");
        return ret;
    }

    @Override
    protected String getItemId(final MetricAndState resource) {
        return resource.state.coordinate;
    }

    @Override
    protected TMMemoryBoard createEmptyItem(final String logicalId) {
        return new TMMemoryBoard(logicalId, itemType);
    }

    /**
     * Populate the Memory Board core attributes with the values provided by the resource object
     *
     * @param item MemoryBoard item whose current state attributes will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.state.coordinate != null) {
            item.setCoordinate(resource.state.coordinate);
        }
        if (resource.state.mediaControllers.observed_power != null) {
            item.setPowerState(resource.state.mediaControllers.observed_power);
        }
        // TODO when supported by DMA: if size state is not null:
        // item.setSize("MY FAM SIZE");
    }

    /**
     * Populate the Memory Board desired core attributes with the values provided by the resource
     * object
     *
     * @param item MemoryBoard item whose desired state will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setDesiredAttributes(final ItemAttributes item, final MetricAndState resource) {}

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

    /**
     * Populate the Memory Board metrics with the values provided by the resource object
     *
     * @param item MemoryBoard item whose metric values will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setMetrics(final ItemAttributes item, final MetricAndState resource) {}

    @Override
    protected ItemAttributes createItemAttributes(final MetricAndState resource) {
        ItemAttributes item = new ItemAttributes();
        String name = resource.state.coordinate.substring(0, resource.state.coordinate.lastIndexOf('/'));
        item.setItemName(resource.state.coordinate.substring(name.lastIndexOf('/') + 1));
        setAttributes(item, resource);
        setDesiredAttributes(item, resource);
        setDiscrepanciesAttributes(resource, item);
        setMetrics(item, resource);
        item.setAlertMessage(resource.alert);
        item.setMemoryAllocated(resource.memory.allocated * resource.bookSize / MULTIPLIER);
        item.setMemoryNotReady(resource.memory.notready * resource.bookSize / MULTIPLIER);
        item.setMemoryOffline(resource.memory.offline * resource.bookSize / MULTIPLIER);
        item.setMemoryTotal(resource.memory.total * resource.bookSize / MULTIPLIER);
        item.setMemoryAvailable(resource.memory.available * resource.bookSize / MULTIPLIER);
        super.setStatusAttributes(resource.status, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final MetricAndState resource) {
        // TODO at some point the following value will need to be
        // ChangeStatus.CHANGED_IGNORE
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final MetricAndState resource) {}

}
