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

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionParameter;
import com.hp.hpl.loom.adapter.annotations.ActionRange;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.items.base.DmaBaseItem;
import com.hp.hpl.loom.adapter.tm.items.base.DmaCoreItemAttributes;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@Root
@ItemTypeInfo(value = Types.SOC_TYPE_LOCAL_ID,
        sorting = {@Sort(operation = DefaultOperations.GROUP_BY, fieldOrder = {"Application"})},
        layers = {Types.HYBRID_LAYER})
@ConnectedTo(toClass = TMNode.class,
        relationshipDetails = @LoomAttribute(key = "Node", supportedOperations = {DefaultOperations.GROUP_BY}),
        type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
@ConnectedTo(toClass = TMOsProvisioningManifest.class,
        relationshipDetails = @LoomAttribute(key = "OS Manifest", supportedOperations = {DefaultOperations.GROUP_BY}),
        type = RelationshipNames.ASSIGN, typeName = RelationshipNames.ASSIGN_NAME)
@ConnectedTo(toClass = TMShelf.class, type = RelationshipNames.ASSIGN, typeName = RelationshipNames.ASSIGN_NAME)
@ActionDefinition(name = "Power management", type = ActionTypes.Item, id = "Power cycle a SoC",
        description = "Manage SoC power", icon = "icon-cycle",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, id = "manage_soc",
                ranges = {@ActionRange(id = Const.ACTION_POWER_ON, name = "Power ON"),
                        @ActionRange(id = Const.ACTION_POWER_OFF, name = "Power OFF"),
                        @ActionRange(id = Const.ACTION_POWER_OFF_FORCE, name = "Power OFF Force")})})
@ActionDefinition(name = "Power management", type = ActionTypes.Aggregation, id = "Power cycle SoCs",
        description = "Manage SoCs power", icon = "icon-cycle",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, id = "manage_soc",
                ranges = {@ActionRange(id = Const.ACTION_POWER_ON, name = "Power ON"),
                        @ActionRange(id = Const.ACTION_POWER_OFF, name = "Power OFF"),
                        @ActionRange(id = Const.ACTION_POWER_OFF_FORCE, name = "Power OFF Force")})})
@ActionDefinition(name = "Set OS Manifest Binding", type = ActionTypes.Item, id = Const.ACTION_SET_OS_MANIFEST_BINDING,
        description = "Assign a specific OS Manifest (requires subsequent power cycle to actually boot)",
        icon = "icon-update", parameters = {@ActionParameter(id = "manifest", name = "OS Manifest",
                type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
@ActionDefinition(name = "Clear OS Manifest Binding", type = ActionTypes.Item,
        id = Const.ACTION_CLEAR_OS_MANIFEST_BINDING,
        description = "Clear assignment of OS Manifest (requires subsequent power management to actually stop"
                + " SoC from running old manifest)",
        icon = "icon-update")
@ActionDefinition(name = "Sync OS Manifest of SoC", type = ActionTypes.Item, id = Const.ACTION_SYNC_OS_MANIFEST,
        description = "Ensures SoC is running the desired OS image - may result in reboot of running SoC;"
                + " will not power up SoCs that are currently OFF)",
        icon = "icon-update")
@ActionDefinition(name = "Set OS Manifest Binding", type = ActionTypes.Aggregation,
        id = Const.ACTION_SET_OS_MANIFEST_BINDING,
        description = "Assign a specific OS Manifest (requires subsequent power cycle to actually boot)",
        icon = "icon-update", parameters = {@ActionParameter(id = "manifest", name = "OS Manifest",
                type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
@ActionDefinition(name = "Clear OS Manifest Binding", type = ActionTypes.Aggregation,
        id = Const.ACTION_CLEAR_OS_MANIFEST_BINDING,
        description = "Clear assignment of OS Manifest (requires subsequent power management to actually stop"
                + " SoC from running old manifest)",
        icon = "icon-update")
@ActionDefinition(name = "Sync OS Manifest of SoCs", type = ActionTypes.Aggregation, id = Const.ACTION_SYNC_OS_MANIFEST,
        description = "Ensures SoCs are running the desired OS image - may result in reboot of running SoCs;"
                + " will not power up SoCs that are currently OFF)",
        icon = "icon-update")
@ActionDefinition(name = "Set OS Manifest Binding for all SoCs", type = ActionTypes.Thread,
        id = Const.ACTION_BIND_OS_MANIFEST_TO_INSTANCE, description = "Set specified OS Manifest to ALL SoCs",
        icon = "icon-readonly", parameters = {@ActionParameter(id = "manifest", name = "OS Manifest",
                type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
@ActionDefinition(name = "Sync OS Manifests for all SoCs", type = ActionTypes.Thread,
        id = Const.ACTION_SYNC_OS_MANIFEST_TO_INSTANCE,
        description = "Ensures all SoCs are running the desired OS image - may result in rebooting SoCs; "
                + "will not power up SoCs that are currently OFF",
        icon = "icon-readonly")
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear soc alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear soc alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear soc alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMSoc extends DmaBaseItem<TMSoc.ItemAttributes> {

    // ------------------------------------------------------------------ //
    // ----------------------- PRIVATE FIELDS --------------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE ATTRIBUTES ----------------------- //
    // ------------------------------------------------------------------ //

    @LoomAttribute(key = "Application", supportedOperations = {DefaultOperations.GROUP_BY})
    private String application;

    @LoomAttribute(type = NumericAttribute.class, key = "Shelves accessing", min = "0", max = "Infinity",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number shelvesAccessing;

    @LoomAttribute(type = NumericAttribute.class, key = "Books accessing", min = "0", max = "Infinity",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number booksAccessing;

    public static class ItemAttributes extends DmaCoreItemAttributes {

        @LoomAttribute(key = "Coordinate", supportedOperations = {DefaultOperations.SORT_BY})
        private String coordinate;

        @LoomAttribute(key = "IP address", supportedOperations = {DefaultOperations.SORT_BY})
        private String ipAddress;

        @LoomAttribute(key = "Hostname", supportedOperations = {DefaultOperations.SORT_BY})
        private String hostName;

        @LoomAttribute(key = "Power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String powerState;

        @LoomAttribute(key = "Desired power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredPowerState;

        @LoomAttribute(key = "System state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String systemState;

        @LoomAttribute(key = "Cores", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number cores;

        @LoomAttribute(type = NumericAttribute.class, key = "DRAM", min = "0", max = "inf", unit = "GB",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number dram;

        @LoomAttribute(key = "OS Boot Time",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String osBootTime;

        @LoomAttribute(key = "OS Up Time",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String osUpTime;

        @LoomAttribute(key = "OS state (from MFW)",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String osState;

        @LoomAttribute(key = "Running OS manifest",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String runningOsManifest;

        @LoomAttribute(key = "Desired running OS manifest",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredOsManifest;

        @LoomAttribute(key = "Next OS manifest",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredNextOsImageManifest;

        @LoomAttribute(key = "Next OS manifest state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String osManifestState;

        @LoomAttribute(key = "SFW version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String sfwVersion;

        @LoomAttribute(key = "MAC address", supportedOperations = {DefaultOperations.SORT_BY})
        private String macAddress;

        @LoomAttribute(key = "Tenant", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String tenant;

        @JsonIgnore
        @LoomAttribute(key = "Desired System state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredSystemState;

        @JsonIgnore
        @LoomAttribute(key = "Desired SFW version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredSfwVersion;

        private String path;
        private String desiredRunningOsImageManifest;
        private String desiredLocalPower;
        private Boolean desiredEnableNonGracefulShutdown;


        // ------------------------------------------------------------------ //
        // ------------------------ CORE METRICS ---------------------------- //
        // ------------------------------------------------------------------ //

        @LoomAttribute(type = NumericAttribute.class, key = "CPU utilisation", min = "0", max = "100", plottable = true,
                unit = "%", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number cpuUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "DRAM utilisation", min = "0", max = "100", unit = "%",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number dramUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge fabric link utilisation", min = "0", max = "100",
                unit = "%", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeFabricLinkUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge fabric link bandwidth", min = "0", max = "inf",
                unit = "GB/s", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeFabricLinkBandwidth;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge ICI link utilisation", min = "0", max = "100",
                unit = "%", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeIciLinkUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge ICI link bandwidth", min = "0", max = "inf",
                unit = "MB/s", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeIciLinkBandwidth;

        private Number bridgePsylockeUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge fabric link request queue utilisation", min = "0",
                max = "100", plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeFabricLinkRequestQueueUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge ICI transaction type counts", min = "0",
                max = "inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeIciTransactionTypeCounts;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge Home Agent average read latency", min = "0",
                max = "inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeHomeAgentAverageReadLatency;

        @LoomAttribute(type = NumericAttribute.class, key = "Bridge Home Agent request queue utilization", min = "0",
                max = "100", plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number bridgeHomeAgentRequestQueueUtilization;

        @LoomAttribute(type = NumericAttribute.class, key = "CPU Stolen Utilisation", min = "0", max = "100",
                plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number cpuStolen;

        @LoomAttribute(type = NumericAttribute.class, key = "CPU System Utilisation", min = "0", max = "100",
                plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number cpuSystem;

        @LoomAttribute(type = NumericAttribute.class, key = "CPU User Utilisation", min = "0", max = "100",
                plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number cpuUser;

        @LoomAttribute(type = NumericAttribute.class, key = "CPU Wait Percentage", min = "0", max = "100",
                plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number cpuWait;

        @LoomAttribute(type = NumericAttribute.class, key = "Disk Inode Utilisation", min = "0", max = "100",
                plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number diskInodeUsed;

        @LoomAttribute(type = NumericAttribute.class, key = "Disk Space Utilisation", min = "0", max = "100",
                plottable = true, unit = "%",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number diskSpaceUsed;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Input Bytes per second", min = "0", max = "inf",
                plottable = true, unit = "B/s",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number netInBytesSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Input Errors per second", min = "0", max = "inf",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number netInErrorsSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Input Packets Dropped per second", min = "0",
                max = "inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number netInPacketsDroppedSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Input Packets per second", min = "0", max = "inf",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number netInPacketsSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Output Bytes per second", min = "0", max = "inf",
                plottable = true, unit = "B/s",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number netOutBytesSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Output Errors per second", min = "0", max = "inf",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number netOutErrorsSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Output Packets Dropped per second", min = "0",
                max = "inf", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number netOutPacketsDroppedSec;

        @LoomAttribute(type = NumericAttribute.class, key = "Network Output Packets per second", min = "0", max = "inf",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number netOutPacketsSec;

        // ------------------------------------------------------------------ //
        // -------------------- GETTERS AND SETTERS ------------------------- //
        // ------------------------------------------------------------------ //


        public String getDesiredPowerState() {
            return desiredPowerState;
        }

        public String getOsManifestState() {
            return osManifestState;
        }

        public void setOsManifestState(final String osManifestState) {
            this.osManifestState = osManifestState;
        }

        public void setDesiredPowerState(final String desiredPowerState) {
            this.desiredPowerState = desiredPowerState;
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

        public String getSystemState() {
            return systemState;
        }

        public void setSystemState(final String systemState) {
            this.systemState = systemState;
        }

        public Number getCores() {
            return cores;
        }

        public void setCores(final Number observedNumCores) {
            cores = observedNumCores;
        }

        public Number getDram() {
            return dram;
        }

        public void setDram(final Number dram) {
            this.dram = dram;
        }

        public String getOsState() {
            return osState;
        }

        public void setOsState(final String osState) {
            this.osState = osState;
        }

        public String getOsBootTime() {
            return osBootTime;
        }

        public void setOsBootTime(final String osBootTime) {
            this.osBootTime = osBootTime;
        }

        public String getOsUpTime() {
            return osUpTime;
        }

        public void setOsUpTime(final String osUpTime) {
            this.osUpTime = osUpTime;
        }

        public String getRunningOsManifest() {
            return runningOsManifest;
        }

        public void setRunningOsManifest(final String runningOsManifest) {
            this.runningOsManifest = runningOsManifest;
        }

        public String getSfwVersion() {
            return sfwVersion;
        }

        public void setSfwVersion(final String sfwVersion) {
            this.sfwVersion = sfwVersion;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(final String macAddress) {
            this.macAddress = macAddress;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public Number getCpuStolen() {
            return cpuStolen;
        }

        public void setCpuStolen(final Number cpuStolen) {
            this.cpuStolen = cpuStolen;
        }

        public Number getCpuSystem() {
            return cpuSystem;
        }

        public void setCpuSystem(final Number cpuSystem) {
            this.cpuSystem = cpuSystem;
        }

        public Number getCpuUser() {
            return cpuUser;
        }

        public void setCpuUser(final Number cpuUser) {
            this.cpuUser = cpuUser;
        }

        public Number getCpuWait() {
            return cpuWait;
        }

        public void setCpuWait(final Number cpuWait) {
            this.cpuWait = cpuWait;
        }

        public Number getDiskInodeUsed() {
            return diskInodeUsed;
        }

        public void setDiskInodeUsed(final Number cpuInodeUsed) {
            diskInodeUsed = cpuInodeUsed;
        }

        public Number getDiskSpaceUsed() {
            return diskSpaceUsed;
        }

        public void setDiskSpaceUsed(final Number diskSpaceUsed) {
            this.diskSpaceUsed = diskSpaceUsed;
        }

        public Number getNetInBytesSec() {
            return netInBytesSec;
        }

        public void setNetInBytesSec(final Number netInBytesSec) {
            this.netInBytesSec = netInBytesSec;
        }

        public Number getNetInErrorsSec() {
            return netInErrorsSec;
        }

        public void setNetInErrorsSec(final Number netInErrorsSec) {
            this.netInErrorsSec = netInErrorsSec;
        }

        public Number getNetInPacketsDroppedSec() {
            return netInPacketsDroppedSec;
        }

        public void setNetInPacketsDroppedSec(final Number netInPacketsDroppedSec) {
            this.netInPacketsDroppedSec = netInPacketsDroppedSec;
        }

        public Number getNetInPacketsSec() {
            return netInPacketsSec;
        }

        public void setNetInPacketsSec(final Number netInPacketsSec) {
            this.netInPacketsSec = netInPacketsSec;
        }

        public Number getNetOutBytesSec() {
            return netOutBytesSec;
        }

        public void setNetOutBytesSec(final Number netOutBytesSec) {
            this.netOutBytesSec = netOutBytesSec;
        }

        public Number getNetOutErrorsSec() {
            return netOutErrorsSec;
        }

        public void setNetOutErrorsSec(final Number netOutErrorsSec) {
            this.netOutErrorsSec = netOutErrorsSec;
        }

        public Number getNetOutPacketsDroppedSec() {
            return netOutPacketsDroppedSec;
        }

        public void setNetOutPacketsDroppedSec(final Number netOutPacketsDroppedSec) {
            this.netOutPacketsDroppedSec = netOutPacketsDroppedSec;
        }

        public Number getNetOutPacketsSec() {
            return netOutPacketsSec;
        }

        public void setNetOutPacketsSec(final Number netOutPacketsSec) {
            this.netOutPacketsSec = netOutPacketsSec;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(final String hostName) {
            this.hostName = hostName;
        }

        public String getTenant() {
            return tenant;
        }

        public void setTenant(final String tenant) {
            this.tenant = tenant;
        }

        public Number getCpuUtilisation() {
            return cpuUtilisation;
        }

        public void setCpuUtilisation(final Number cpuUtilisation) {
            this.cpuUtilisation = cpuUtilisation;
        }

        public Number getDramUtilisation() {
            return dramUtilisation;
        }

        public void setDramUtilisation(final Number dramUtilisation) {
            this.dramUtilisation = dramUtilisation;
        }

        public Number getBridgeFabricLinkUtilisation() {
            return bridgeFabricLinkUtilisation;
        }

        public void setBridgeFabricLinkUtilisation(final Number bridgeFabricLinkUtilisation) {
            this.bridgeFabricLinkUtilisation = bridgeFabricLinkUtilisation;
        }

        public Number getBridgeFabricLinkBandwidth() {
            return bridgeFabricLinkBandwidth;
        }

        public void setBridgeFabricLinkBandwidth(final Number bridgeFabricLinkBandwidth) {
            this.bridgeFabricLinkBandwidth = bridgeFabricLinkBandwidth;
        }

        public Number getBridgeIciLinkUtilisation() {
            return bridgeIciLinkUtilisation;
        }

        public void setBridgeIciLinkUtilisation(final Number bridgeIciLinkUtilisation) {
            this.bridgeIciLinkUtilisation = bridgeIciLinkUtilisation;
        }

        public Number getBridgePsylockeUtilisation() {
            return bridgePsylockeUtilisation;
        }

        public void setBridgePsylockeUtilisation(final Number bridgePsylockeUtilisation) {
            this.bridgePsylockeUtilisation = bridgePsylockeUtilisation;
        }

        public Number getBridgeIciLinkBandwidth() {
            return bridgeIciLinkBandwidth;
        }

        public void setBridgeIciLinkBandwidth(final Number bridgeIciLinkBandwidth) {
            this.bridgeIciLinkBandwidth = bridgeIciLinkBandwidth;
        }

        public Number getBridgeFabricLinkRequestQueueUtilisation() {
            return bridgeFabricLinkRequestQueueUtilisation;
        }

        public void setBridgeFabricLinkRequestQueueUtilisation(final Number bridgeFabricLinkRequestQueueUtilisation) {
            this.bridgeFabricLinkRequestQueueUtilisation = bridgeFabricLinkRequestQueueUtilisation;
        }

        public Number getBridgeIciTransactionTypeCounts() {
            return bridgeIciTransactionTypeCounts;
        }

        public void setBridgeIciTransactionTypeCounts(final Number bridgeIciTransactionTypeCounts) {
            this.bridgeIciTransactionTypeCounts = bridgeIciTransactionTypeCounts;
        }

        public Number getBridgeHomeAgentAverageReadLatency() {
            return bridgeHomeAgentAverageReadLatency;
        }

        public void setBridgeHomeAgentAverageReadLatency(final Number bridgeHomeAgentAverageReadLatency) {
            this.bridgeHomeAgentAverageReadLatency = bridgeHomeAgentAverageReadLatency;
        }

        public Number getBridgeHomeAgentRequestQueueUtilization() {
            return bridgeHomeAgentRequestQueueUtilization;
        }

        public void setBridgeHomeAgentRequestQueueUtilization(final Number bridgeHomeAgentRequestQueueUtilization) {
            this.bridgeHomeAgentRequestQueueUtilization = bridgeHomeAgentRequestQueueUtilization;
        }

        public String getDesiredSystemState() {
            return desiredSystemState;
        }

        public void setDesiredSystemState(final String desiredSystemState) {
            this.desiredSystemState = desiredSystemState;
        }

        public String getDesiredOsManifest() {
            return desiredOsManifest;
        }

        public void setDesiredOsManifest(final String desiredOsManifest) {
            this.desiredOsManifest = desiredOsManifest;
        }

        public String getDesiredSfwVersion() {
            return desiredSfwVersion;
        }

        public void setDesiredSfwVersion(final String desiredSfwVersion) {
            this.desiredSfwVersion = desiredSfwVersion;
        }

        public String getDesiredNextOsImageManifest() {
            return desiredNextOsImageManifest;
        }

        public void setDesiredNextOsImageManifest(final String desiredNextOsImageManifest) {
            this.desiredNextOsImageManifest = desiredNextOsImageManifest;
        }

        public String getDesiredRunningOsImageManifest() {
            return desiredRunningOsImageManifest;
        }

        public void setDesiredRunningOsImageManifest(final String desiredRunningOsImageManifest) {
            this.desiredRunningOsImageManifest = desiredRunningOsImageManifest;
        }

        public String getDesiredLocalPower() {
            return desiredLocalPower;
        }

        public void setDesiredLocalPower(final String desiredLocalPower) {
            this.desiredLocalPower = desiredLocalPower;
        }

        public Boolean getDesiredEnableNonGracefulShutdown() {
            return desiredEnableNonGracefulShutdown;
        }

        public void setDesiredEnableNonGracefulShutdown(final Boolean desiredEnableNonGracefulShutdown) {
            this.desiredEnableNonGracefulShutdown = desiredEnableNonGracefulShutdown;
        }

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }
    }

    public Number getShelvesAccessing() {
        return shelvesAccessing;
    }

    public void setShelvesAccessing(final Number shelvesAccessing) {
        this.shelvesAccessing = shelvesAccessing;
    }

    public Number getBooksAccessing() {
        return booksAccessing;
    }

    public void setBooksAccessing(final Number booksAccessing) {
        this.booksAccessing = booksAccessing;
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMSoc(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType, ItemAttributes.class);
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(final String application) {
        this.application = application;
    }

    @Override
    public boolean update() {
        boolean superUpdate = super.update();
        boolean anyUpdate = false;

        Map<String, Item> itemMaps = getConnectedItemMapWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
                        Types.SHELF_TYPE_LOCAL_ID, Types.SOC_TYPE_LOCAL_ID, RelationshipNames.ASSIGN));

        int noOfShelves = 0;
        int noOfBooks = 0;
        if (itemMaps != null) {
            Iterator<?> shelfs = itemMaps.values().iterator();
            while (shelfs.hasNext()) {
                TMShelf shelf = (TMShelf) shelfs.next();
                if (shelf.getCore().getShelfSocs()
                        .contains(this.getLogicalId().substring(this.getLogicalId().indexOf("/MachineVersion")))) {
                    noOfShelves++;
                    noOfBooks += shelf.getCore().getShelfBooks().size();
                }
            }
        }
        this.setShelvesAccessing(noOfShelves);
        this.setBooksAccessing(noOfBooks);

        return superUpdate | anyUpdate;
    }

    // -- HACK -- //
    /***
     * Block if it starts on interleave group.
     */
    // @Override
    // public void reportStopTraversalRules() {
    //
    // List<BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean>> stopRules =
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.get(this.getClass());
    //
    // if (stopRules == null) {
    // stopRules = new ArrayList<>();
    //
    // BiFunction<ConnectedRelationships, ConditionalStopInformation, Boolean> stopFromIG =
    // (relation, stopCondition) -> {
    //
    // Boolean returnResult;
    //
    // String fromItemType = stopCondition.getTraversalSource().getItemType().getId();
    // String lastItemTypeBeforeHere = null;
    //
    // if (stopCondition.getSequenceOfTraversedItemsUntilHere().size() >= 2) {
    // lastItemTypeBeforeHere = stopCondition.getSequenceOfTraversedItemsUntilHere()
    // .get(stopCondition.getSequenceOfTraversedItemsUntilHere().size() - 2).getItemType()
    // .getId();
    // }
    // if (fromItemType != null && fromItemType.toLowerCase().contains("interleave")
    // && lastItemTypeBeforeHere != null
    // && lastItemTypeBeforeHere.toLowerCase().contains("node")) {
    // returnResult = true;
    //
    // stopCondition.setReportItem(false);
    // } else {
    // returnResult = false;
    // }
    // LOG.debug("Result:" + returnResult + " from " + fromItemType + " via " +
    // lastItemTypeBeforeHere
    // + " on SOC");
    // return returnResult;
    // };
    //
    // stopRules.add(stopFromIG);
    // stopRules.add(ConnectedRelationships.STOP_ON_ROOT);
    // Fibre.STOP_TRAVERSAL_RULES_BY_CLASS.put(this.getClass(), stopRules);
    // }
    // }
}
