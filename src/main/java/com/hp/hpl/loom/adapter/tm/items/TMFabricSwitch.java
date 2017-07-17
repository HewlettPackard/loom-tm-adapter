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
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.items.base.DmaBaseItem;
import com.hp.hpl.loom.adapter.tm.items.base.DmaCoreItemAttributes;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.ItemType;

@ItemTypeInfo(value = Types.FABRIC_SWITCH_TYPE_LOCAL_ID, layers = {Types.INFRASTRUCTURE_LAYER})
@ConnectedTo(toClass = TMEnclosure.class,
        relationshipDetails = @LoomAttribute(key = "Enclosure", supportedOperations = {DefaultOperations.GROUP_BY}),
        type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear izone board alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear izone board alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear izone board alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMFabricSwitch extends DmaBaseItem<TMFabricSwitch.ItemAttributes> {

    public static class ItemAttributes extends DmaCoreItemAttributes {

        @LoomAttribute(key = "Coordinate", supportedOperations = {DefaultOperations.SORT_BY})
        private String coordinate;

        @LoomAttribute(key = "Power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String powerState;

        @LoomAttribute(key = "Desired power state", supportedOperations = {DefaultOperations.GROUP_BY})
        private String desiredPowerState;

        @LoomAttribute(key = "MP firmware version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String mpFirmwareVersion;

        @JsonIgnore
        @LoomAttribute(key = "Desired MP firmware version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredMpFirmwareVersion;

        @LoomAttribute(key = "FPGA version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String fpgaVersion;

        @JsonIgnore
        @LoomAttribute(key = "Desired FPGA version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredFpgaVersion;

        @LoomAttribute(key = "Position", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String position;

        @JsonIgnore
        @LoomAttribute(key = "Switch ID", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String switchId;

        // ------------------------------------------------------------------ //
        // ------------------------ CORE METRICS ---------------------------- //
        // ------------------------------------------------------------------ //

        @LoomAttribute(key = "Fabric link utilisation", min = "0", max = "100", unit = "%", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number fabricLinkIntUtilisation;

        @LoomAttribute(key = "Fabric link external utilisation", min = "0", max = "100", unit = "%", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number fabricLinkExtUtilisation;

        @LoomAttribute(key = "Fabric link bandwidth", unit = "GB/s",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number fabricLinkBandwidth;

        @LoomAttribute(key = "Fabric switch core Arb blocked",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number fabricSwitchCoreArbBlocked;

        // ------------------------------------------------------------------ //
        // -------------------- GETTERS AND SETTERS ------------------------- //
        // ------------------------------------------------------------------ //

        public String getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(final String coordinate) {
            this.coordinate = coordinate;
        }

        public String getSwitchId() {
            return switchId;
        }

        public void setSwitchId(final String switchId) {
            this.switchId = switchId;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(final String position) {
            this.position = position;
        }

        public String getPowerState() {
            return powerState;
        }

        public void setPowerState(final String powerState) {
            this.powerState = powerState;
        }

        public String getMpFirmwareVersion() {
            return mpFirmwareVersion;
        }

        public void setMpFirmwareVersion(final String mpFirmwareVersion) {
            this.mpFirmwareVersion = mpFirmwareVersion;
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

        public String getDesiredMpFirmwareVersion() {
            return desiredMpFirmwareVersion;
        }

        public void setDesiredMpFirmwareVersion(final String desiredMpFirmwareVersion) {
            this.desiredMpFirmwareVersion = desiredMpFirmwareVersion;
        }

        public String getDesiredFpgaVersion() {
            return desiredFpgaVersion;
        }

        public void setDesiredFpgaVersion(final String desiredFpgaVersion) {
            this.desiredFpgaVersion = desiredFpgaVersion;
        }

        public Number getFabricLinkIntUtilisation() {
            return fabricLinkIntUtilisation;
        }

        public void setFabricLinkIntUtilisation(final Number fabricLinkUtilisation) {
            fabricLinkIntUtilisation = fabricLinkUtilisation;
        }

        public Number getFabricLinkExtUtilisation() {
            return fabricLinkExtUtilisation;
        }

        public void setFabricLinkExtUtilisation(final Number fabricLinkUtilisation) {
            fabricLinkExtUtilisation = fabricLinkUtilisation;
        }

        public Number getFabricLinkBandwidth() {
            return fabricLinkBandwidth;
        }

        public void setFabricLinkBandwidth(final Number fabricLinkBandwidth) {
            this.fabricLinkBandwidth = fabricLinkBandwidth;
        }

        public Number getFabricSwitchCoreArbBlocked() {
            return fabricSwitchCoreArbBlocked;
        }

        public void setFabricSwitchCoreArbBlocked(final Number fabricSwitchCoreArbBlocked) {
            this.fabricSwitchCoreArbBlocked = fabricSwitchCoreArbBlocked;
        }
    }

    public TMFabricSwitch(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType, ItemAttributes.class);
    }
}
