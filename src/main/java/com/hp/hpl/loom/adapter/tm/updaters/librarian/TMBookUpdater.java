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

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.librarian.Book;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMBook;
import com.hp.hpl.loom.adapter.tm.items.TMBook.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMBookUpdater extends TMLibrarianUpdater<TMBook, TMBook.ItemAttributes, Book> {

    public TMBookUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector, final LibrarianRestClient librarianRestClient)
            throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType, itemCollector, librarianRestClient);
    }

    @Override
    protected Iterator<Book> getResourceIterator() {
        return librarianRestClient.getBooks().iterator();
    }

    @Override
    protected String getItemId(final Book resource) {
        return resource.lza.toString();
    }

    @Override
    protected TMBook createEmptyItem(final String logicalId) {
        return new TMBook(logicalId, itemType);
    }

    @Override
    protected ItemAttributes createItemAttributes(final Book resource) {
        ItemAttributes item = new ItemAttributes();
        item.setItemId(resource.lza.toString());
        item.setItemName(resource.lza.toString());
        item.setState(resource.state);
        item.setLza(resource.lza);
        item.setSize(resource.size);

        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr, final Book resource) {
        if (!itemAttr.getState().equals(resource.state)) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.CHANGED_UPDATE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final Book resource) {
        Integer groupId = resource.interleaveGroupId;
        connectedItem.setRelationshipWithType(adapter.getProvider(), Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID,
                groupId.toString(), RelationshipNames.ASSIGN);
    }

}
