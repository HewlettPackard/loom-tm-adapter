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
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Rack;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredRack;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianMemory.MemoryDetails;
import com.hp.hpl.loom.adapter.tm.introspect.FieldPath;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMRack;
import com.hp.hpl.loom.adapter.tm.items.TMRack.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMRackUpdater extends TMAbstractUpdater<TMRack, TMRack.ItemAttributes, TMRackUpdater.MetricAndState> {

    public static class MetricAndState {
        private Rack state;
        private DesiredRack desired;
        private Double famUtilisation;
        private GetDmaStatus status;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Rack discrepancies;
        private MemoryDetails memory;
        private Long bookSize;
    }

    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private LibrarianRestClient librarianRestClient;
    private static final Double MULTIPLIER = 1000000000.0;
    private static final Double MAX_PERCENT = 100.0;

    private static final Log LOG = LogFactory.getLog(TMRackUpdater.class);

    public TMRackUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final LibrarianRestClient librarianRestClient) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector);
        this.dmaUpdater = dmaUpdater;
        this.librarianRestClient = librarianRestClient;
    }

    @Override
    protected Iterator<MetricAndState> getResourceIterator() {
        Iterator<Rack> dmaCIter = dmaUpdater.filterCurrent(Rack.class);
        Iterator<DesiredRack> dmaDIter = dmaUpdater.filterDesired(DesiredRack.class);
        return new Iterator<MetricAndState>() {
            @Override
            public boolean hasNext() {
                return dmaCIter.hasNext() && dmaDIter.hasNext();
            }

            @Override
            public MetricAndState next() {
                MetricAndState n = new MetricAndState();
                n.state = dmaCIter.next();
                n.desired = dmaDIter.next();
                n.discrepancies = dmaUpdater.getDiscrepancies().getRackDiscrepanciesByPath(n.state.path);
                n.status = (GetDmaStatus) dmaUpdater.getStatusData();
                n.memory = librarianRestClient.getMemoryFromCoordinate(n.state.coordinate);
                n.bookSize = librarianRestClient.getBookSize();
                return n;
            }
        };
    }

    @Override
    public AdapterUpdateResult updateItems(final long cEpoch) {
        long startTime = System.currentTimeMillis();
        AdapterUpdateResult ret = super.updateItems(cEpoch);
        long deltaT = System.currentTimeMillis() - startTime;
        LOG.debug("TMRackUpdater.updateItems took " + deltaT + " ms.");
        return ret;
    }

    @Override
    protected String getItemId(final MetricAndState resource) {
        return resource.state.coordinate;
    }

    @Override
    protected TMRack createEmptyItem(final String logicalId) {
        return new TMRack(logicalId, itemType);
    }

    /**
     * Populate the Rack core attributes with the values provided by the resource object
     *
     * @param item Rack item whose current state attributes will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.state.coordinate != null) {
            item.setCoordinate(resource.state.coordinate);
        }
        if (resource.state.path != null) {
            item.setPath(resource.state.path);
        }
        // TODO when supported by DMA: if cores is not null:
        // item.setCores("MY CORES");
        // TODO when supported by DMA: if DRAM is not null:
        // item.setDram("MY DRAM");
        // TODO when supported by DMA: if FAM is not null:
        // item.setFam("MY FAM");
        // TODO when supported by DMA: if Fabric Bandwidth is not null:
        // item.setFam("MY FABRIC BANDWIDTH");
    }

    private void setLibrarianAttributes(final ItemAttributes item, final MetricAndState resource) {
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

    /**
     * Populate the Node desired core attributes with the values provided by the resource object
     *
     * @param item
     * @param resource
     */
    private void setDesiredAttributes(final ItemAttributes item, final MetricAndState resource) {
        if (resource.desired.defaultNextOsImageManifest != null) {
            item.setDesiredDefaultNextOsImageManifest(resource.desired.defaultNextOsImageManifest.value);
        }
        if (resource.desired.defaultRunningOsImageManifest != null) {
            item.setDesiredDefaultRunningOsImageManifest(resource.desired.defaultRunningOsImageManifest.value);
        }
        if (resource.desired.power != null) {
            item.setDesiredPower(resource.desired.power.value);
        }
        if (resource.desired.forceAllFabricSocOff != null) {
            item.setDesiredForceAllFabricSocOff(resource.desired.forceAllFabricSocOff.value);
        }
        if (resource.desired.forceAllFamFabricSocOff != null) {
            item.setDesiredForceAllFamFabricSocOff(resource.desired.forceAllFamFabricSocOff.value);
        }
        if (resource.desired.forceAllSocOff != null) {
            item.setDesiredForceAllSocOff(resource.desired.forceAllSocOff.value);
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
        String name = resource.state.coordinate.substring(0, resource.state.coordinate.lastIndexOf('/'));
        item.setItemName(resource.state.coordinate.substring(name.lastIndexOf('/') + 1));
        setAttributes(item, resource);
        setDesiredAttributes(item, resource);
        setLibrarianAttributes(item, resource);
        super.setStatusAttributes(resource.status, item);
        setDiscrepanciesAttributes(resource, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final MetricAndState resource) {
        if (itemAttr.getFamUtilisation() != resource.famUtilisation) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final MetricAndState resource) {

        FieldPath nodePath = FieldPath.introspect(Rack.class, Enclosure.class);

        Iterator<?> iter = nodePath.getValue(resource.state);

        while (iter.hasNext()) {
            Enclosure enclosure = (Enclosure) iter.next();
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.ENCLOSURE_TYPE_LOCAL_ID,
                    enclosure.coordinate, RelationshipNames.CONTAINS);
        }
    }

}
