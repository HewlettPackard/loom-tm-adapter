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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AdapterUpdateResult;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.TMAdapter;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Instance;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Rack;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredInstance;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobal;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalActive;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalMemory;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalPools;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalSocs;
import com.hp.hpl.loom.adapter.tm.introspect.FieldPath;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.adapter.tm.items.TMInstance.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMInstanceUpdater
        extends TMAbstractUpdater<TMInstance, TMInstance.ItemAttributes, TMInstanceUpdater.MetricAndState> {

    private static final String FQDN_UNKNOWN = "unknown";
    private static final Log LOG = LogFactory.getLog(TMInstanceUpdater.class);

    public static class MetricAndState {
        private Instance state;
        private DesiredInstance desired;
        private GetDmaStatus status;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Instance discrepancies;
        private Long bookSize;
        private LibrarianGlobal libGlobal;
    }

    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private LibrarianRestClient librarianRestClient;
    private static final Double MULTIPLIER = 1024 * 1024 * 1024.0;
    private static final Double MAX_PERCENT = 100.0;

    public TMInstanceUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final LibrarianRestClient librarianRestClient) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector);
        this.dmaUpdater = dmaUpdater;
        this.librarianRestClient = librarianRestClient;
    }

    @Override
    protected Iterator<MetricAndState> getResourceIterator() {
        Iterator<Instance> dmaCIter = dmaUpdater.filterCurrent(Instance.class);
        Iterator<DesiredInstance> dmaDIter = dmaUpdater.filterDesired(DesiredInstance.class);
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
                n.discrepancies = dmaUpdater.getDiscrepancies().getInstanceDiscrepanciesByPath(n.state.path);
                n.status = (GetDmaStatus) dmaUpdater.getStatusData();
                n.bookSize = librarianRestClient.getBookSize();

                Boolean useFakeData = ((TMAdapter) adapter).getUseFakeData();

                if (useFakeData != null && useFakeData) {
                    n.libGlobal = librarianRestClient.getFakeGlobal();
                } else {
                    n.libGlobal = librarianRestClient.getGlobal();
                }

                return n;
            }
        };
    }

    @Override
    public AdapterUpdateResult updateItems(final long cEpoch) {
        long startTime = System.currentTimeMillis();
        AdapterUpdateResult ret = super.updateItems(cEpoch);
        long deltaT = System.currentTimeMillis() - startTime;
        LOG.debug("TMInstanceUpdater.updateItems took " + deltaT + " ms.");
        return ret;
    }

    @Override
    protected String getItemId(final MetricAndState resource) {
        return "/";
    }

    @Override
    protected TMInstance createEmptyItem(final String logicalId) {
        return new TMInstance(logicalId, itemType);
    }

    private String getCoordinate(final MetricAndState resource) {
        return resource.state.rack1.coordinate.split("/Rack")[0];
    }

    /**
     * Populate the Instance core attributes with the values provided by the resource object
     *
     * @param resource MetricAndState object containing the information that will be used
     * @param item Instance item whose current state attributes will be updated
     */
    private void setAttributes(final MetricAndState resource, final ItemAttributes item) {
        if (null == resource || null == resource.state) {
            LOG.warn("resource was null, nothing to update");
            return;
        }
        if (null != resource.state.path) {
            item.setPath(resource.state.path);
        }
        if (null != resource.state.rack1) {
            if (null != resource.state.rack1.coordinate) {
                String coord = getCoordinate(resource);
                item.setCoordinate(coord);
                String name = coord.substring(0, coord.lastIndexOf('/'));
                item.setItemName(coord.substring(name.lastIndexOf('/') + 1));
            }
        } else {
            item.setItemName("Empty DMA Model");

            if (LOG.isDebugEnabled()) {
                LOG.debug("rack1 not found in resource.state, will not update some current attributes");
            }
        }

        try {
            item.setFqdn(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            item.setFqdn(FQDN_UNKNOWN);
            LOG.warn("Unable to resolve FQDN");
        }

        // TODO when supported by DMA: if SoCs is not null:
        // item.setSocs("MY SOCS");
        // TODO when supported by DMA: if cores is not null:
        // item.setCores("MY CORES");
        // TODO when supported by DMA: if DRAM is not null:
        // item.setDram("MY DRAM");
        // TODO when supported by DMA: if FAM is not null:
        // item.setFam("MY FAM");
        // TODO when supported by DMA: if Rated Fabric Bandwidth is not null:
        // item.setRatedFabricBandwidth("MY RATED FABRIC BANDWIDTH");
    }

    /**
     * Populate the Instance desired core attributes with the values provided by the resource object
     *
     * @param item Instance item whose desired state will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setDesiredAttributes(final MetricAndState resource, final ItemAttributes item) {
        if (null == resource || null == resource.desired) {
            LOG.warn("resource was null, nothing to update");
            return;
        }
        if (null != resource.desired.rack1) {
            if (resource.desired.rack1.allNodeFamPow != null) {
                item.setDesiredFamPowerState(resource.desired.rack1.allNodeFamPow.value);
            }
            if (resource.desired.rack1.allFabricPow != null) {
                item.setDesiredFabricPowerState(resource.desired.rack1.allFabricPow.value);
            }
        } else {
            LOG.debug("rack1 not found in resource.desired, will not update some desired attributes");
        }
    }

    /**
     * Populate the Instance status attributes with the values provided by the resource object
     *
     * @param item Instance item whose status attributes will be updated
     * @param resource MetricAndState object containing the information that will be used
     */
    private void setStatusAttributes(final MetricAndState resource, final ItemAttributes item) {
        if (resource.status == null) {
            return;
        }
        super.setStatusAttributes(resource.status, item);
        boolean debug = false;
        boolean paused = false;
        if (resource.status.serviceDebug != null && "True".equalsIgnoreCase(resource.status.serviceDebug)) {
            debug = true;
        }
        if (resource.status.servicePaused != null && "True".equalsIgnoreCase(resource.status.servicePaused)) {
            paused = true;
        }
        if (!debug && !paused) {
            item.setAaMode(Const.MODE_NORMAL);
        } else if (debug && !paused) {
            item.setAaMode(Const.MODE_DEBUG);
        } else if (!debug && paused) {
            item.setAaMode(Const.MODE_PAUSED);
        } else {
            item.setAaMode(Const.MODE_DEBUG_PAUSED);
        }
        if (resource.status.componentStatus != null && resource.status.componentStatus.stateMonitor != null
                && resource.status.componentStatus.stateMonitor.phase != null) {
            item.setAaPhase(resource.status.componentStatus.stateMonitor.phase);
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

    /**
     * Populate the Instance librarian metrics with the values provided by the resource object
     *
     * @param item Instance item whose desired state will be updated
     */
    private void setLibrarianMetrics(final MetricAndState resource, final ItemAttributes item) {
        LibrarianGlobalMemory memory = resource.libGlobal.memory;
        LibrarianGlobalSocs libSocs = resource.libGlobal.socs;
        LibrarianGlobalPools libPools = resource.libGlobal.pools;
        LibrarianGlobalActive libActive = resource.libGlobal.active;

        if (LOG.isDebugEnabled()) {
            LOG.debug(resource.state.path + " (in books) == Memory total: " + memory.total + ", Memory allocated: "
                    + memory.allocated + ", Memory not ready: " + memory.notready + ", Memory offline: "
                    + memory.offline);
        }

        item.setMemoryAllocated(memory.allocated / MULTIPLIER);
        item.setMemoryNotReady(memory.notready / MULTIPLIER);
        item.setMemoryOffline(memory.offline / MULTIPLIER);
        item.setMemoryTotal(memory.total / MULTIPLIER);
        item.setMemoryAvailable(memory.available / MULTIPLIER);

        if (LOG.isDebugEnabled()) {
            LOG.debug(resource.state.path + " (in GB) == Memory total: " + item.getMemoryTotal()
                    + ", Memory available: " + item.getMemoryAvailable() + ", Memory allocated: "
                    + item.getMemoryAllocated() + ", Memory not ready: " + item.getMemoryNotReady()
                    + ", Memory offline: " + item.getMemoryOffline());
        }

        item.setSocTotal(libSocs.total);
        item.setSocActive(libSocs.active);
        item.setSocOffline(libSocs.offline);
        item.setPoolTotal(libPools.total);
        item.setPoolActive(libPools.active);
        item.setPoolOffline(libPools.offline);
        item.setLibBooks(libActive.books);
        item.setLibShelves(libActive.shelves);
        item.setLibMaxBooks(memory.total / librarianRestClient.getBookSize());
        if (memory.total == 0) {
            item.setFamUtilisation(0.0);
        } else {
            item.setFamUtilisation(MAX_PERCENT * ((memory.total - memory.available) / (double) (memory.total)));
        }
        LOG.debug("set instance FAM utilisation value to " + item.getFamUtilisation());
    }

    @Override
    protected ItemAttributes createItemAttributes(final MetricAndState resource) {
        ItemAttributes item = new ItemAttributes();
        if (null == resource || null == resource.status) {
            LOG.warn("resource was null, nothing to update");
            return item;
        }
        setAttributes(resource, item);
        setDesiredAttributes(resource, item);
        setLibrarianMetrics(resource, item);
        setStatusAttributes(resource, item);
        setDiscrepanciesAttributes(resource, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final MetricAndState resource) {
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final MetricAndState resource) {

        FieldPath nodePath = FieldPath.introspect(Instance.class, Rack.class);

        Iterator<?> iter = nodePath.getValue(resource.state);

        while (iter.hasNext()) {
            Rack rack = (Rack) iter.next();
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.RACK_TYPE_LOCAL_ID, rack.coordinate,
                    RelationshipNames.CONTAINS);
        }
    }

}
