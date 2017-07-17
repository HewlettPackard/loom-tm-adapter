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

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@ItemTypeInfo(value = Types.SHELF_TYPE_LOCAL_ID,
        sorting = {@Sort(operation = DefaultOperations.GROUP_BY, fieldOrder = {"Book Size", "Owner", "Group"}),
                @Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Filename", "Book Size", "Owner", "Group"})},
        layers = {Types.MEMORY_LAYER})
@ConnectedTo(toClass = TMSoc.class, type = RelationshipNames.ASSIGN, typeName = RelationshipNames.ASSIGN_NAME)
@ConnectedTo(toClass = TMBook.class, type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
// @ConnectedTo(toClass = TMMemoryBoard.class, type = RelationshipNames.ASSIGN, typeName =
// RelationshipNames.ASSIGN_NAME)
// @ConnectedTo(toClass = TMInterleaveGroup.class, type = RelationshipNames.CONTAINS,
// typeName = RelationshipNames.CONTAINS_NAME)
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear shelf alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear shelf alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear shelf alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMShelf extends AdapterItem<TMShelf.ItemAttributes> {

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
        @LoomAttribute(key = "Filename", supportedOperations = {DefaultOperations.SORT_BY})
        private String filename;

        @LoomAttribute(type = NumericAttribute.class, key = "Owner", min = "0", max = "Inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number owner;

        @LoomAttribute(type = NumericAttribute.class, key = "Group", min = "0", max = "Inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number group;

        @LoomAttribute(type = NumericAttribute.class, key = "Book Size", min = "0", max = "Inf", plottable = true,
                unit = "Bytes", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number bookSize;

        @JsonIgnore
        private List<Integer> interleaveGroupIds;
        @JsonIgnore
        private List<String> shelfSocs;
        @JsonIgnore
        private List<Long> shelfBooks;

        public Number getOwner() {
            return owner;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(final String filename) {
            this.filename = filename;
        }

        public void setOwner(final Number owner) {
            this.owner = owner;
        }

        public Number getGroup() {
            return group;
        }

        public void setGroup(final Number group) {
            this.group = group;
        }

        public Number getBookSize() {
            return bookSize;
        }

        public void setBookSize(final Number bookSize) {
            this.bookSize = bookSize;
        }

        private Long size;

        public Long getSize() {
            return size;
        }

        public void setSize(final Long size) {
            this.size = size;
        }

        public List<Integer> getInterleaveGroupIds() {
            return interleaveGroupIds;
        }

        public void setInterleaveGroupIds(final List<Integer> interleaveGroupIds) {
            this.interleaveGroupIds = interleaveGroupIds;
        }

        public List<String> getShelfSocs() {
            return shelfSocs;
        }

        public void setShelfSocs(final List<String> shelfSocs) {
            this.shelfSocs = shelfSocs;
        }

        public List<Long> getShelfBooks() {
            return shelfBooks;
        }

        public void setShelfBooks(final List<Long> shelfBooks) {
            this.shelfBooks = shelfBooks;
        }
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMShelf(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }

    // @Override
    // @SuppressWarnings("checkstyle:magicnumber")
    // public void reportStopTraversalRules() {
    // List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopRules =
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.get(this.getClass());
    //
    // if (stopRules == null) {
    // stopRules = new ArrayList<>();
    // BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean> stopFromShelf =
    // (relation,
    // stopCondition) -> {
    // Boolean returnResult = false;
    //
    // int sourceItemIndex = stopCondition.getSequenceOfTraversedItemsUntilHere().size() - 3;
    // int previousItemIndex = stopCondition.getSequenceOfTraversedItemsUntilHere().size() - 2;
    // if (stopCondition.getSequenceOfTraversedItemsUntilHere().size() >= 3) {
    // String sourceItemType = stopCondition.getSequenceOfTraversedItemsUntilHere()
    // .get(sourceItemIndex).getItemType().getId();
    // String previousItemType = stopCondition.getSequenceOfTraversedItemsUntilHere()
    // .get(previousItemIndex).getItemType().getId();
    // if (sourceItemType != null && sourceItemType.toLowerCase().contains("node")
    // && previousItemType.toLowerCase().contains("memory")) {
    // stopCondition.setReportItem(false);
    // returnResult = true;
    // } else {
    // returnResult = false;
    // }
    // }
    // return returnResult;
    // };
    // stopRules.add(stopFromShelf);
    // stopRules.add(ConnectedRelationships.STOP_ON_ROOT);
    // stopRules.add(ConnectedRelationships.VISIT_LAYER_ONLY_ONCE);
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.put(this.getClass(), stopRules);
    // }
    // }
    // // -- HACK -- //
    // /***
    // * Block if it starts on interleave group.
    // */
    // @Override
    // public void reportStopTraversalRules() {
    //
    // List<BiFunction<ConnectedRelationships, ConditionalStopInformation,
    // Boolean>> stopRules =
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.get(this.getClass());
    //
    // if (stopRules == null) {
    // stopRules = new ArrayList<>();
    //
    // BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>
    // stopFromIG =
    // (relation, stopCondition) -> {
    //
    // Boolean returnResult;
    //
    // String fromItemType =
    // stopCondition.getTraversalSource().getItemType().getId();
    // String lastItemTypeBeforeHere = null;
    //
    // if (stopCondition.getSequenceOfTraversedItemsUntilHere().size() >= 2)
    // lastItemTypeBeforeHere =
    // stopCondition.getSequenceOfTraversedItemsUntilHere()
    // .get(stopCondition.getSequenceOfTraversedItemsUntilHere().size() -
    // 2).getItemType()
    // .getId();
    // if ((fromItemType != null)
    // && (fromItemType.toLowerCase().contains("node")
    // || fromItemType.toLowerCase().contains("enclosure")
    // || fromItemType.toLowerCase().contains("rack"))
    // && (lastItemTypeBeforeHere != null
    // && lastItemTypeBeforeHere.toLowerCase().contains("interleave"))) {
    // returnResult = true;
    //
    // stopCondition.setReportItem(false);
    // } else {
    // returnResult = false;
    // }
    // LOG.debug("Result: " + returnResult + " from " + fromItemType + " via " +
    // lastItemTypeBeforeHere
    // + " on Shelf");
    // return returnResult;
    // };
    //
    // stopRules.add(stopFromIG);
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.put(this.getClass(), stopRules);
    // }
    // }
}
