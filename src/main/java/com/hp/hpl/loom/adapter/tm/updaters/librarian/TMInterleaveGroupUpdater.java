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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.librarian.InterleaveGroup;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMInterleaveGroup;
import com.hp.hpl.loom.adapter.tm.items.TMInterleaveGroup.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMInterleaveGroupUpdater
        extends TMLibrarianUpdater<TMInterleaveGroup, TMInterleaveGroup.ItemAttributes, InterleaveGroup> {

    public TMInterleaveGroupUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final LibrarianRestClient librarianRestClient)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector, librarianRestClient);
    }

    @Override
    protected Iterator<InterleaveGroup> getResourceIterator() {
        return librarianRestClient.getInterleaveGroups().iterator();
    }

    @Override
    protected String getItemId(final InterleaveGroup resource) {
        return resource.groupId.toString();
    }

    @Override
    protected TMInterleaveGroup createEmptyItem(final String logicalId) {
        return new TMInterleaveGroup(logicalId, itemType);
    }

    @Override
    protected ItemAttributes createItemAttributes(final InterleaveGroup resource) {
        ItemAttributes item = new ItemAttributes();

        item.setItemId(resource.groupId.toString());
        item.setItemName(resource.groupId.toString());
        item.setId(resource.groupId);
        item.setBaseAddress(resource.baseAddress);
        item.setSize(resource.size);
        item.setMediaControllers(resource.mediaControllers);

        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr,
            final InterleaveGroup resource) {

        Set<String> mc = new HashSet<>();
        mc.addAll(itemAttr.getMediaControllers());
        Set<String> newMc = new HashSet<>();
        newMc.addAll(resource.mediaControllers);

        if (!newMc.equals(mc)) {
            return ChangeStatus.CHANGED_UPDATE;
        }

        return ChangeStatus.CHANGED_IGNORE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final InterleaveGroup resource) {
        Iterator<?> iter = resource.mediaControllers.iterator();

        Set<String> mb = new HashSet<>();

        String keyword = "MemoryBoard";

        while (iter.hasNext()) {
            String mcCoordinate = (String) iter.next();
            String mbCoordinate = mcCoordinate.substring(0, mcCoordinate.indexOf(keyword) + keyword.length());
            mbCoordinate = mbCoordinate + "/1";
            mb.add(mbCoordinate);
        }

        iter = mb.iterator();

        while (iter.hasNext()) {
            connectedItem.setRelationshipWithType(adapter.getProvider(), Types.MEMORY_BOARD_TYPE_LOCAL_ID,
                    (String) iter.next(), RelationshipNames.ASSIGN);
        }
    }
}
