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
import com.hp.hpl.loom.adapter.tm.TMItemCollector;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.librarian.Shelf;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMShelf;
import com.hp.hpl.loom.adapter.tm.items.TMShelf.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMShelfUpdater extends TMLibrarianUpdater<TMShelf, TMShelf.ItemAttributes, Shelf> {
    private static final Log LOG = LogFactory.getLog(TMShelfUpdater.class);

    private TMItemCollector.InterleaveGroups interleaveGroups;

    public TMShelfUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final LibrarianRestClient librarianRestClient,
            final TMItemCollector.InterleaveGroups interleaveGroups) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector, librarianRestClient);
        this.interleaveGroups = interleaveGroups;
    }

    @Override
    protected Iterator<Shelf> getResourceIterator() {
        return librarianRestClient.getShelves().iterator();
    }

    @Override
    protected String getItemId(final Shelf resource) {
        return resource.name;
    }

    @Override
    protected TMShelf createEmptyItem(final String logicalId) {
        return new TMShelf(logicalId, itemType);
    }

    @Override
    protected ItemAttributes createItemAttributes(final Shelf resource) {
        ItemAttributes item = new ItemAttributes();

        LOG.info("BETTY Enter createItemAttributes for Shelf " + resource.name);
        item.setItemId(resource.name);
        item.setItemName(resource.name);
        item.setFilename(resource.name);
        item.setOwner(resource.owner);
        item.setGroup(resource.group);
        item.setBookSize(resource.booksize);
        item.setSize(resource.size);
        // item.setInterleaveGroupIds(resource.interleaveGroupIds);
        item.setShelfBooks(resource.books);
        item.setShelfSocs(resource.active);
        LOG.info("BETTY Leave createItemAttributes for Shelf " + resource.name);

        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr, final Shelf resource) {
        if (itemAttr.getSize() != resource.size) {
            return ChangeStatus.CHANGED_UPDATE;
        }

        return ChangeStatus.CHANGED_IGNORE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final Shelf resource) {
        Iterator<Long> iter = resource.books.iterator();

        while (iter.hasNext()) {
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.BOOK_TYPE_LOCAL_ID,
                    Long.toString(iter.next()), RelationshipNames.CONTAINS);
        }

        Iterator<String> iter1 = resource.active.iterator();

        if (resource.active.size() == 0) {
            LOG.info("BETTY Shelf: " + resource.name + ", No SOC Rels found");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Shelf: " + resource.name + ", No SOC Rels found");
            }
        }
        while (iter1.hasNext()) {
            LOG.info("BETTY Shelf: " + resource.name + ", Found SOC Rel: " + iter1.next());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Shelf: " + resource.name + ", Found SOC Rel: " + iter1.next());
            }
        }

        iter1 = resource.active.iterator();

        while (iter1.hasNext()) {
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.SOC_TYPE_LOCAL_ID,
                    iter1.next().replace("/Soc/1", ""), RelationshipNames.ASSIGN);
        }

        // TODO this is to test fake single relationship soc-shelf
        // if (connectedItem.getLogicalId().equals("tm/tm/shelfs/shelf1_node1")) {
        // connectedItem.setRelationshipWithType(adapter.getProvider(), Types.SOC_TYPE_LOCAL_ID,
        // "/MachineVersion/1/Datacenter/BUK1/Rack/A1.AboveFloor/Enclosure/U3/EncNum/1/Node/1/SocBoard/1",
        // RelationshipNames.ASSIGN);
        // }

        // Iterator<Integer> iter2 = resource.interleaveGroupIds.iterator();
        //
        // while (iter2.hasNext()) {
        // String igId = Integer.toString(iter2.next());
        // connectedItem.setRelationshipWithType(adapter.getProvider(),
        // Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID,
        // igId, RelationshipNames.ASSIGN);
        // InterleaveGroup ig = interleaveGroups.raw.get(igId);
        // if (ig != null) {
        // Iterator<String> iter3 = ig.mediaControllers.iterator();
        // Set<String> mb = new HashSet<>();
        // String keyword = "MemoryBoard";
        // while (iter3.hasNext()) {
        // String mcCoordinate = iter3.next();
        // String mbCoordinate = mcCoordinate.substring(0, mcCoordinate.indexOf(keyword) +
        // keyword.length());
        // mbCoordinate = mbCoordinate + "/1";
        // mb.add(mbCoordinate);
        // }
        // iter3 = mb.iterator();
        // while (iter3.hasNext()) {
        // connectedItem.setRelationshipWithType(adapter.getProvider(),
        // Types.MEMORY_BOARD_TYPE_LOCAL_ID,
        // iter3.next(), RelationshipNames.ASSIGN);
        // }
        // }
        // }
    }
}
