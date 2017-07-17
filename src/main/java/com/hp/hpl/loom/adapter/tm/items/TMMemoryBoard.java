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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.items.base.DmaBaseItem;
import com.hp.hpl.loom.adapter.tm.items.base.DmaCoreItemAttributes;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@Root
@ItemTypeInfo(value = Types.MEMORY_BOARD_TYPE_LOCAL_ID, layers = {Types.HYBRID_LAYER})
@ConnectedTo(toClass = TMNode.class,
        relationshipDetails = @LoomAttribute(key = "Node", supportedOperations = {DefaultOperations.GROUP_BY}),
        type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
@ConnectedTo(toClass = TMInterleaveGroup.class, type = RelationshipNames.ASSIGN,
        typeName = RelationshipNames.ASSIGN_NAME)
// @ConnectedTo(toClass = TMShelf.class, type = RelationshipNames.ASSIGN, typeName =
// RelationshipNames.ASSIGN_NAME)
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear memory board alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear memory board alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear memory board alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMMemoryBoard extends DmaBaseItem<TMMemoryBoard.ItemAttributes> {

    private static final int LEVEL_EIGHT = 8;

    // ------------------------------------------------------------------ //
    // ----------------------- PRIVATE FIELDS --------------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE ATTRIBUTES ----------------------- //
    // ------------------------------------------------------------------ //

    @LoomAttribute(key = "Fabric link utilisation", min = "0", max = "100", plottable = true, unit = "%",
            supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
    private Number fabricLinkUtilisation;

    @LoomAttribute(key = "Fabric link bandwidth", min = "0", max = "Infinity", plottable = true, unit = "GB/s",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private Number fabricLinkBandwidth;

    public Number getFabricLinkUtilisation() {
        return fabricLinkUtilisation;
    }

    public void setFabricLinkUtilisation(final Number fabricLinkUtilisation) {
        this.fabricLinkUtilisation = fabricLinkUtilisation;
    }

    public Number getFabricLinkBandwidth() {
        return fabricLinkBandwidth;
    }

    public void setFabricLinkBandwidth(final Number fabricLinkBandwidth) {
        this.fabricLinkBandwidth = fabricLinkBandwidth;
    }

    // ------------------------------------------------------------------ //
    // ------------------------ CORE ATTRIBUTES ------------------------- //
    // ------------------------------------------------------------------ //

    public static class ItemAttributes extends DmaCoreItemAttributes {

        @LoomAttribute(key = "Coordinate", supportedOperations = {DefaultOperations.SORT_BY})
        private String coordinate;

        @LoomAttribute(key = "Power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String powerState;

        @LoomAttribute(key = "Desired power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredPowerState;

        @LoomAttribute(key = "Size", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String size;

        @LoomAttribute(key = "FW version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String firmwareVersion;

        @LoomAttribute(key = "FPGA version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String fpgaVersion;

        @JsonIgnore
        @LoomAttribute(key = "Desired FW Version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredFirmwareVersion;

        @JsonIgnore
        @LoomAttribute(key = "Desired FPGA Version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredFpgaVersion;

        private String alertMessage;

        // ------------------------------------------------------------------ //
        // ------------------------ CORE METRICS ---------------------------- //
        // ------------------------------------------------------------------ //

        // LIBRARIAN
        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Total", min = "0", max = "Infinity",
                plottable = true, unit = "GB", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Double memoryTotal;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Allocated", min = "0", max = "Infinity",
                plottable = true, unit = "GB", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Double memoryAllocated;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Available", min = "0", max = "Infinity",
                plottable = true, unit = "GB", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Double memoryAvailable;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Not Ready", min = "0", max = "Infinity",
                plottable = true, unit = "GB", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Double memoryNotReady;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Offline", min = "0", max = "Infinity",
                plottable = true, unit = "GB", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Double memoryOffline;

        // ------------------------------------------------------------------ //
        // ------------------------ GETTERS AND SETTERS --------------------- //
        // ------------------------------------------------------------------ //

        public String getAlertMessage() {
            return alertMessage;
        }

        public void setAlertMessage(final String alertMessage) {
            this.alertMessage = alertMessage;
        }

        public String getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(final String coordinate) {
            this.coordinate = coordinate;
        }

        public String getPowerState() {
            return powerState;
        }

        public void setPowerState(final String powerState) {
            this.powerState = powerState;
        }

        public String getSize() {
            return size;
        }

        public void setSize(final String size) {
            this.size = size;
        }

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        public void setFirmwareVersion(final String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
        }

        public String getFpgaVersion() {
            return fpgaVersion;
        }

        public void setFpgaVersion(final String fpgaVersion) {
            this.fpgaVersion = fpgaVersion;
        }

        public String getDesiredPowerState() {
            return desiredPowerState;
        }

        public void setDesiredPowerState(final String desiredPowerState) {
            this.desiredPowerState = desiredPowerState;
        }

        public String getDesiredFirmwareVersion() {
            return desiredFirmwareVersion;
        }

        public void setDesiredFirmwareVersion(final String desiredFirmwareVersion) {
            this.desiredFirmwareVersion = desiredFirmwareVersion;
        }

        public String getDesiredFpgaVersion() {
            return desiredFpgaVersion;
        }

        public void setDesiredFpgaVersion(final String desiredFpgaVersion) {
            this.desiredFpgaVersion = desiredFpgaVersion;
        }

        public Double getMemoryTotal() {
            return memoryTotal;
        }

        public void setMemoryTotal(final Double memoryTotal) {
            this.memoryTotal = memoryTotal;
        }

        public Double getMemoryAllocated() {
            return memoryAllocated;
        }

        public void setMemoryAllocated(final Double memoryAllocated) {
            this.memoryAllocated = memoryAllocated;
        }

        public Double getMemoryAvailable() {
            return memoryAvailable;
        }

        public void setMemoryAvailable(final Double memoryAvailable) {
            this.memoryAvailable = memoryAvailable;
        }

        public Double getMemoryNotReady() {
            return memoryNotReady;
        }

        public void setMemoryNotReady(final Double memoryNotReady) {
            this.memoryNotReady = memoryNotReady;
        }

        public Double getMemoryOffline() {
            return memoryOffline;
        }

        public void setMemoryOffline(final Double memoryOffline) {
            this.memoryOffline = memoryOffline;
        }
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMMemoryBoard(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType, ItemAttributes.class);
    }

    private boolean updateNonCoreAttributes() {
        boolean updated = false;
        Item nodeItem = this.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
                        Types.MEMORY_BOARD_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        if (nodeItem == null) {
            return false;
        }
        Item socItem = this.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(nodeItem.getProviderType(),
                        Types.SOC_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        if (socItem == null) {
            return false;
        }
        TMSoc soc = (TMSoc) socItem;
        if (null != soc.getCore().getBridgePsylockeUtilisation()) {
            this.setFabricLinkUtilisation(soc.getCore().getBridgePsylockeUtilisation().doubleValue());
            updated = true;
        }
        return updated;
    }

    @Override
    public boolean update() {
        boolean anyUpdate = updateNonCoreAttributes();
        boolean superUpdate = super.update();
        boolean anyChange = false;

        if (this.getCore().alertMessage != null && this.getAlertLevel() < LEVEL_EIGHT) {
            this.setAlertDescription(this.getCore().alertMessage);
            this.setAlertLevel(LEVEL_EIGHT);
            anyChange = true;
        }

        return superUpdate || anyChange || anyUpdate;
    }

    // -- HACK -- //
    /***
     * Block if it starts on shelf.
     */
    // @Override
    // @SuppressWarnings("checkstyle:magicnumber")
    // public void reportStopTraversalRules() {
    // List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopRules =
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.get(this.getClass());
    //
    // if (stopRules == null) {
    // stopRules = new ArrayList<>(); // Add rule to behave like root ifit
    // // was reached by Image
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
    // if (sourceItemType != null && sourceItemType.toLowerCase().contains("soc")
    // && previousItemType.toLowerCase().contains("shelf")) {
    // stopCondition.setReportItem(false);
    // returnResult = true;
    // } else {
    // returnResult = false;
    // }
    // }
    //// LOG.debug("Result: " + returnResult + " from " + fromItemType + " on MEMORY BOARD");
    // return returnResult;
    // };
    // stopRules.add(stopFromShelf);
    // stopRules.add(ConnectedRelationships.STOP_ON_ROOT);
    // stopRules.add(ConnectedRelationships.VISIT_LAYER_ONLY_ONCE);
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.put(this.getClass(), stopRules);
    // }
    // }
}
