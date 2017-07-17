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
package com.hp.hpl.loom.adapter.tm.updaters.osprovisioning;

import java.util.Iterator;

import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ConnectedItem;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.backend.ManifestingRestClient;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Manifest;
import com.hp.hpl.loom.adapter.tm.items.TMOsProvisioningManifest;
import com.hp.hpl.loom.adapter.tm.items.TMOsProvisioningManifest.ItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes.ChangeStatus;
import com.hp.hpl.loom.model.ItemType;

public class TMOsProvisioningManifestUpdater
        extends AggregationUpdater<TMOsProvisioningManifest, TMOsProvisioningManifest.ItemAttributes, Manifest> {

    private ManifestingRestClient manifestingRestClient;

    public TMOsProvisioningManifestUpdater(final Aggregation aggregation, final BaseAdapter adapter,
            final ItemType itemType, final ItemCollector itemCollector,
            final ManifestingRestClient manifestingRestClient) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType.getLocalId(), itemCollector);
        this.manifestingRestClient = manifestingRestClient;
    }

    @Override
    protected Iterator<Manifest> getResourceIterator() {
        return manifestingRestClient.getManifests().iterator();
    }

    @Override
    protected String getItemId(final Manifest resource) {
        return resource.prefixedName;
    }

    @Override
    protected TMOsProvisioningManifest createEmptyItem(final String logicalId) {
        return new TMOsProvisioningManifest(logicalId, itemType);
    }

    @Override
    protected ItemAttributes createItemAttributes(final Manifest resource) {
        ItemAttributes item = new ItemAttributes();

        item.setItemId(resource.prefixedName);
        item.setItemName(resource.prefixedName);
        item.setName(resource.name);
        item.setDescription(resource.description);
        item.setRelease(resource.release);
        item.setPackages(resource.packages.size());
        item.setTasks(resource.tasks.size());

        return item;
    }

    @Override
    protected ChangeStatus compareItemAttributesToResource(final ItemAttributes itemAttr, final Manifest resource) {
        if (!itemAttr.getDescription().equals(resource.description)
                || !itemAttr.getRelease().equals(resource.release)) {
            return ChangeStatus.CHANGED_UPDATE;
        }
        return ChangeStatus.CHANGED_IGNORE;
    }

    @Override
    protected void setRelationships(final ConnectedItem connectedItem, final Manifest resource) {}
}
