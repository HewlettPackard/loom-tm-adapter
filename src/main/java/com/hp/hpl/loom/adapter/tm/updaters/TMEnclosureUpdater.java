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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AdapterUpdateResult;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Enclosure;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Node;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredEnclosure;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianMemory.MemoryDetails;
import com.hp.hpl.loom.adapter.tm.introspect.FieldPath;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMEnclosure;
import com.hp.hpl.loom.adapter.tm.items.TMEnclosure.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMEnclosureUpdater
        extends TMAbstractUpdater<TMEnclosure, TMEnclosure.ItemAttributes, TMEnclosureUpdater.MetricAndState> {

    public static class MetricAndState {
        private Enclosure state;
        private DesiredEnclosure desired;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Enclosure discrepancies;
        private GetDmaStatus status;
        private MemoryDetails memory;
        private Long bookSize;
    }

    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private LibrarianRestClient librarianRestClient;
    private static final Double MULTIPLIER = 1000000000.0;
    private static final Double MAX_PERCENT = 100.0;

    private static final Log LOG = LogFactory.getLog(TMEnclosureUpdater.class);

    public TMEnclosureUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final LibrarianRestClient librarianRestClient) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector);
        this.dmaUpdater = dmaUpdater;
        this.librarianRestClient = librarianRestClient;
    }

    @Override
    protected Iterator<MetricAndState> getResourceIterator() {
        Iterator<Enclosure> dmaCIter = dmaUpdater.filterCurrent(Enclosure.class);
        Iterator<DesiredEnclosure> dmaDIter = dmaUpdater.filterDesired(DesiredEnclosure.class);
        return new Iterator<TMEnclosureUpdater.MetricAndState>() {
            @Override
            public MetricAndState next() {
                MetricAndState n = new MetricAndState();
                n.desired = dmaDIter.next();
                n.state = dmaCIter.next();
                n.discrepancies = dmaUpdater.getDiscrepancies().getEnclosureDiscrepanciesByPath(n.state.path);
                n.status = (GetDmaStatus) dmaUpdater.getStatusData();
                n.memory = librarianRestClient.getMemoryFromCoordinate(n.state.coordinate);
                n.bookSize = librarianRestClient.getBookSize();
                return n;
            }

            @Override
            public boolean hasNext() {
                return dmaCIter.hasNext() && dmaDIter.hasNext();
            }
        };
    }

    @Override
    public AdapterUpdateResult updateItems(final long cEpoch) {
        long startTime = System.currentTimeMillis();
        AdapterUpdateResult ret = super.updateItems(cEpoch);
        long deltaT = System.currentTimeMillis() - startTime;
        LOG.debug("TMEnclosureUpdater.updateItems took " + deltaT + " ms.");
        return ret;
    }

    @Override
    protected String getItemId(final MetricAndState resource) {
        return resource.state.coordinate;
    }

    @Override
    protected TMEnclosure createEmptyItem(final String logicalId) {
        return new TMEnclosure(logicalId, itemType);
    }

    /**
     * Populate the Enclosure core attributes with the values provided by the resource object
     *
     * @param item Enclosure item whose current state attributes will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.state.coordinate != null) {
            item.setCoordinate(resource.state.coordinate);
        }
        if (resource.state.observed_power != null) {
            item.setPowerState(resource.state.observed_power);
        }
        // TODO when supported by DMA: if cores is not null:
        // item.setCores("MY CORES");
        // TODO when supported by DMA: if DRAM is not null:
        // item.setDram("MY DRAM");
        // TODO when supported by DMA: if FAM is not null:
        // item.setFam("MY FAM");
        // TODO when supported by DMA: if Fabric bandwidth is not null:
        // item.setFabricBandwidth("MY FABRIC BANDWIDTH");
    }

    /**
     * Populate the Enclosure desired core attributes with the values provided by the resource
     * object
     *
     * @param item Enclosure item whose desired state will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setDesiredAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.desired.observed_power != null) {
            item.setDesiredPowerState(resource.desired.observed_power.value);
        }
        if (resource.desired.localPower != null) {
            item.setDesiredLocalPower(resource.desired.localPower.value);
        }
    }

    /**
     * Populate the Enclosure librarian metrics with the values provided by the resource object
     *
     * @param item Enclosure item whose values will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setLibrarianMetrics(final MetricAndState resource, final ItemAttributes item) {
        Double allocatedMemory = resource.memory.allocated * resource.bookSize / MULTIPLIER;
        Double notReadyMemory = resource.memory.notready * resource.bookSize / MULTIPLIER;
        Double offlineMemory = resource.memory.offline * resource.bookSize / MULTIPLIER;
        Double totalMemory = resource.memory.total * resource.bookSize / MULTIPLIER;
        Double availableMemory = resource.memory.available * resource.bookSize / MULTIPLIER;
        Double famUtilisation = (totalMemory - availableMemory) / totalMemory * MAX_PERCENT;
        item.setMemoryAllocated(allocatedMemory);
        item.setMemoryNotReady(notReadyMemory);
        item.setMemoryOffline(offlineMemory);
        item.setMemoryTotal(totalMemory);
        item.setMemoryAvailable(availableMemory);
        item.setFamUtilisation(famUtilisation);
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
        String name = resource.state.coordinate.substring(0, resource.state.coordinate.lastIndexOf('/'));
        item.setItemName(resource.state.coordinate.substring(name.lastIndexOf('/') + 1));
        setAttributes(item, resource);
        setDesiredAttributes(item, resource);
        setLibrarianMetrics(resource, item);
        super.setStatusAttributes(resource.status, item);
        setDiscrepanciesAttributes(resource, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final MetricAndState resource) {
        if (itemAttr != null && !itemAttr.getPowerState().equals(resource.state.observed_power)) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final MetricAndState resource) {
        connectedItem.setRelationshipWithType(adapter.getProvider(), Types.FABRIC_SWITCH_TYPE_LOCAL_ID,
                resource.state.iZone1.iZoneBoard1.coordinate, RelationshipNames.CONTAINS);
        connectedItem.setRelationshipWithType(adapter.getProvider(), Types.FABRIC_SWITCH_TYPE_LOCAL_ID,
                resource.state.iZone1.iZoneBoard2.coordinate, RelationshipNames.CONTAINS);

        FieldPath nodePath = FieldPath.introspect(Enclosure.class, Node.class);
        Iterator<?> iter = nodePath.getValue(resource.state);
        while (iter.hasNext()) {
            Node node = (Node) iter.next();
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.NODE_TYPE_LOCAL_ID, node.coordinate,
                    RelationshipNames.CONTAINS);
        }
    }

}
