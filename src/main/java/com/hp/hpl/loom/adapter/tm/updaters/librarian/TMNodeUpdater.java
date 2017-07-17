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
package com.hp.hpl.loom.adapter.tm.updaters.librarian;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Node;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredNode;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianMemory.MemoryDetails;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMNode;
import com.hp.hpl.loom.adapter.tm.items.TMNode.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMNodeUpdater extends TMLibrarianUpdater<TMNode, TMNode.ItemAttributes, TMNodeUpdater.Resource> {
    private static final Log LOG = LogFactory.getLog(TMNodeUpdater.class);

    public static class Resource {
        private Node state;
        private DesiredNode desired;
        private com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Node discrepancies;
        private Double famUtilisation;
        private MemoryDetails memory;
        private Long bookSize;
        private GetDmaStatus status;
    }

    private final BackEndDma<?, ?, ?, ?> dmaUpdater;
    private LibrarianRestClient librarianRestClient;
    private static final Double MULTIPLIER = 1000000000.0;
    private static final Double MAX_PERCENT = 100.0;

    @SuppressWarnings("checkstyle:parameternumber")
    public TMNodeUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final BackEndDma<?, ?, ?, ?> dmaUpdater,
            final LibrarianRestClient librarianRestClient) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector, librarianRestClient);
        this.dmaUpdater = dmaUpdater;
        this.librarianRestClient = librarianRestClient;
    }

    @Override
    protected Iterator<Resource> getResourceIterator() {
        Iterator<DesiredNode> dmaDIter = dmaUpdater.filterDesired(DesiredNode.class);
        Iterator<Node> dmaCIter = dmaUpdater.filterCurrent(Node.class);
        Iterator<Resource> resIter = new Iterator<TMNodeUpdater.Resource>() {

            @Override
            public Resource next() {
                Resource res = new Resource();
                res.desired = dmaDIter.next();
                res.state = dmaCIter.next();
                res.discrepancies = dmaUpdater.getDiscrepancies().getNodeDiscrepanciesByPath(res.state.path);
                res.memory = librarianRestClient.getMemoryFromCoordinate(res.state.coordinate);
                res.bookSize = librarianRestClient.getBookSize();
                res.status = (GetDmaStatus) dmaUpdater.getStatusData();
                return res;
            }

            @Override
            public boolean hasNext() {
                return dmaDIter.hasNext() && dmaCIter.hasNext();
            }
        };
        return resIter;
    }

    @Override
    protected String getItemId(final Resource resource) {
        return resource.state.coordinate;
    }

    @Override
    protected TMNode createEmptyItem(final String logicalId) {
        return new TMNode(logicalId, itemType);
    }

    /**
     * Populate the Node core attributes with the values provided by the resource object
     *
     * @param item
     * @param resource
     */
    private void setCurrentAttributes(final ItemAttributes item, final Resource resource) {
        if (resource.state.coordinate != null) {
            item.setCoordinate(resource.state.coordinate);
        }
        if (resource.state.observed_power != null) {
            item.setPowerState(resource.state.observed_power);
        }
        if (resource.state.socBoard != null) {
            if (resource.state.socBoard.soc != null) {
                if (resource.state.socBoard.soc.soc1 != null) {
                    if (resource.state.socBoard.soc.soc1.observed_NumCores != null) {
                        item.setCores(resource.state.socBoard.soc.soc1.observed_NumCores);
                    }
                    if (resource.state.socBoard.soc.soc1.observed_DimmCount != null
                            && resource.state.socBoard.soc.soc1.observed_DimmSize != null) {
                        item.setDram(resource.state.socBoard.soc.soc1.observed_DimmCount.doubleValue()
                                * resource.state.socBoard.soc.soc1.observed_DimmSize.doubleValue());
                    }
                }
            }
            if (resource.state.socBoard.nodeMp != null) {
                if (resource.state.socBoard.nodeMp.observed_MFWVersion != null) {
                    item.setMpFirmwareVersion(resource.state.socBoard.nodeMp.observed_MFWVersion);
                }
            }
        }
        // TODO when supported by DMA: if FAM is not null:
        // item.setFam("MY FAM");
        // TODO when supported by DMA: if fabric bandwidth is not null:
        // item.setFabricBandwidth("MY FABRIC BANDWIDTH");
    }

    /**
     * Populate the Node desired core attributes with the values provided by the resource object
     *
     * @param item
     * @param resource
     */
    private void setDesiredAttributes(final ItemAttributes item, final Resource resource) {
        if (resource.desired.power != null) {
            item.setDesiredPowerState(resource.desired.power.value);
        }
        if (resource.desired.localPower != null) {
            item.setDesiredLocalPower(resource.desired.localPower.value);
        }
    }

    private void setDiscrepanciesAttributes(final Resource resource, final ItemAttributes item) {
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
    protected ItemAttributes createItemAttributes(final Resource resource) {
        ItemAttributes item = new ItemAttributes();
        String name = resource.state.coordinate.substring(0, resource.state.coordinate.lastIndexOf('/'));
        item.setItemName(resource.state.coordinate.substring(name.lastIndexOf('/') + 1));
        setCurrentAttributes(item, resource);
        setDesiredAttributes(item, resource);
        setDiscrepanciesAttributes(resource, item);
        // item.setBookAllocatedPct(resource.bookAllocated);
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
        super.setStatusAttributes(resource.status, item);
        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr, final Resource resource) {
        if (itemAttr.getFamUtilisation() != resource.famUtilisation) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final Resource resource) {
        connectedItem.setRelationshipWithType(adapter.getProvider(), Types.MEMORY_BOARD_TYPE_LOCAL_ID,
                resource.state.memoryBoard.coordinate, RelationshipNames.CONTAINS);

        connectedItem.setRelationshipWithType(adapter.getProvider(), Types.SOC_TYPE_LOCAL_ID,
                resource.state.socBoard.coordinate, RelationshipNames.CONTAINS);
    }

}
