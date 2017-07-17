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

import com.hp.hpl.loom.adapter.AdapterItem;
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

@ItemTypeInfo(value = Types.BOOK_TYPE_LOCAL_ID,
        sorting = {@Sort(operation = DefaultOperations.GROUP_BY, fieldOrder = {"Size", "State"}),
                @Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Size", "State", "LZA"})},
        layers = {Types.MEMORY_LAYER})
@ConnectedTo(toClass = TMShelf.class, relationshipDetails = @LoomAttribute(key = "Shelf", supportedOperations = {}),
        type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
@ConnectedTo(toClass = TMInterleaveGroup.class, type = RelationshipNames.CONTAINS,
        typeName = RelationshipNames.CONTAINS_NAME)
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear book alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear book alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear book alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMBook extends AdapterItem<TMBook.ItemAttributes> {
    // ------------------------------------------------------------------ //
    // ----------------------- PRIVATE FIELDS --------------------------- //
    // ------------------------------------------------------------------ //

    // private final static String NO_APP = "None";

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE ATTRIBUTES ----------------------- //
    // ------------------------------------------------------------------ //

    // @LoomAttribute(key = "Application", supportedOperations =
    // {DefaultOperations.GROUP_BY})
    // private String application;

    // ------------------------------------------------------------------ //
    // ------------------------ CORE ATTRIBUTES ------------------------- //
    // ------------------------------------------------------------------ //

    public static class ItemAttributes extends CoreItemAttributes {

        @LoomAttribute(key = "LZA", min = "0", max = "Inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY})
        private Number lza;

        public Number getLza() {
            return lza;
        }

        public void setLza(final Number lza) {
            this.lza = lza;
        }

        @LoomAttribute(key = "State", supportedOperations = {DefaultOperations.GROUP_BY, DefaultOperations.SORT_BY})
        private String state;

        public String getState() {
            return state;
        }

        public void setState(final String state) {
            this.state = state;
        }

        @LoomAttribute(key = "Size", min = "0", max = "Inf", plottable = true, unit = "Bytes",
                supportedOperations = {DefaultOperations.GROUP_BY, DefaultOperations.SORT_BY})
        private Number size;

        public Number getSize() {
            return size;
        }

        public void setSize(final Number size) {
            this.size = size;
        }
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMBook(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }

    // public String getApplication() {
    // return application;
    // }
    //
    // public void setApplication(final String application) {
    // this.application = application;
    // }

    // @Override
    // public boolean update() {
    // boolean superUpdate = super.update();
    // boolean anyUpdate = false;

    // TMNode node = (TMNode) this.getFirstConnectedItemWithRelationshipName(
    // RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
    // Types.NODE_TYPE_LOCAL_ID, Types.BOOK_TYPE_LOCAL_ID,
    // RelationshipNames.HOSTED));
    //
    // TMApplication application = (TMApplication)
    // node.getFirstConnectedItemWithRelationshipName(
    // RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
    // Types.NODE_TYPE_LOCAL_ID, Types.APPLICATION_TYPE_LOCAL_ID,
    // RelationshipNames.CONTAINS));

    // if (application != null &&
    // !application.getCore().getItemName().equals(this.getApplication())) {
    // this.setApplication(application.getCore().getItemName());
    // anyUpdate = true;
    // }
    // if (application == null && !NO_APP.equals(this.getApplication())) {
    // this.setApplication(NO_APP);
    // anyUpdate = true;
    // }

    // return superUpdate | anyUpdate;
    // }
}
