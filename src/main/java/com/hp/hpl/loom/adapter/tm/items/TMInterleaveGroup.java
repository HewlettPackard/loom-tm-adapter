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
package com.hp.hpl.loom.adapter.tm.items;

import java.util.List;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.ItemType;

@ItemTypeInfo(value = Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID,
        sorting = {@Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"ID", "Size", "Base Address"})},
        layers = {Types.MEMORY_LAYER})
@ConnectedTo(toClass = TMMemoryBoard.class, type = RelationshipNames.ASSIGN, typeName = RelationshipNames.ASSIGN_NAME)
@ConnectedTo(toClass = TMBook.class, type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
// @ConnectedTo(toClass = TMShelf.class, type = RelationshipNames.ASSIGN, typeName =
// RelationshipNames.ASSIGN_NAME)
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear interleave group alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear interleave group alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear interleave group alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMInterleaveGroup extends AdapterItem<TMInterleaveGroup.ItemAttributes> {

    // ------------------------------------------------------------------ //
    // ----------------------- PRIVATE FIELDS --------------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE ATTRIBUTES ----------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ------------------------ CORE ATTRIBUTES ------------------------- //
    // ------------------------------------------------------------------ //

    public static class ItemAttributes extends CoreItemAttributes {

        @LoomAttribute(type = NumericAttribute.class, key = "ID", min = "0", max = "Inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Number id;

        public Number getId() {
            return id;
        }

        public void setId(final Number id) {
            this.id = id;
        }

        @LoomAttribute(type = NumericAttribute.class, key = "Base Address", min = "0", max = "Inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Number baseAddress;

        public Number getBaseAddress() {
            return baseAddress;
        }

        public void setBaseAddress(final Number baseAddress) {
            this.baseAddress = baseAddress;
        }

        @LoomAttribute(type = NumericAttribute.class, key = "Size", min = "0", max = "Inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Number size;

        public Number getSize() {
            return size;
        }

        public void setSize(final Number size) {
            this.size = size;
        }

        private List<String> mediaControllers;

        public List<String> getMediaControllers() {
            return mediaControllers;
        }

        public void setMediaControllers(List<String> mediaControllers) {
            this.mediaControllers = mediaControllers;
        }
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMInterleaveGroup(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }
}
