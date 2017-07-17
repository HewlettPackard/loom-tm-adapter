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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionParameter;
import com.hp.hpl.loom.adapter.annotations.ActionRange;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.items.base.DmaBaseItem;
import com.hp.hpl.loom.adapter.tm.items.base.DmaCoreItemAttributes;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.relationships.RelationshipUtil;

@ItemTypeInfo(value = Types.INSTANCE_TYPE_LOCAL_ID, layers = {Types.INFRASTRUCTURE_LAYER})
@ConnectedTo(toClass = TMRack.class, type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)

@ActionDefinition(name = "All Powered ON", type = ActionTypes.Item, id = Const.ACTION_ALL_POWERED_ON,
        description = "FAM, Fabric and SoC power on (even if no OS Manifest have been assigned)", icon = "icon-cycle")
@ActionDefinition(name = "All Powered ON", type = ActionTypes.Aggregation, id = Const.ACTION_ALL_POWERED_ON,
        description = "FAM, Fabric and SoC power on (even if no OS Manifest have been assigned)", icon = "icon-cycle")

@ActionDefinition(name = "All Powered OFF", type = ActionTypes.Item, id = Const.ACTION_ALL_POWERED_OFF,
        description = "FAM, Fabric and SoC power off, stopping on error by default or forcing action "
                + "if 'Force' option is selected",
        icon = "icon-cycle",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED,
                id = "manage_instance", ranges = {@ActionRange(id = Const.ACTION_ALL_POWERED_OFF_FORCE, name = "Force"),
                        @ActionRange(id = Const.ACTION_ALL_POWERED_OFF, name = "Graceful")})})
@ActionDefinition(name = "All Powered OFF", type = ActionTypes.Aggregation, id = Const.ACTION_ALL_POWERED_OFF,
        description = "FAM, Fabric and SoC power off, stopping on error by default or forcing action "
                + "if 'Force' option is selected",
        icon = "icon-cycle",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED,
                id = "manage_instance", ranges = {@ActionRange(id = Const.ACTION_ALL_POWERED_OFF_FORCE, name = "Force"),
                        @ActionRange(id = Const.ACTION_ALL_POWERED_OFF, name = "Graceful")})})

@ActionDefinition(name = "Enable SoC Power ON", type = ActionTypes.Item, id = Const.ACTION_ENABLE_SOC_POWER_ON,
        description = "Allow Tenant Administrators and Users to manipulate SoC power state", icon = "icon-cycle")
@ActionDefinition(name = "Enable SoC Power ON", type = ActionTypes.Aggregation, id = Const.ACTION_ENABLE_SOC_POWER_ON,
        description = "Allow Tenant Administrators and Users to manipulate SoC power state", icon = "icon-cycle")

@ActionDefinition(name = "Only FAM + Fabric Powered ON", type = ActionTypes.Item,
        id = Const.ACTION_ONLY_FAM_FABRIC_POWERED_ON, description = "Only FAM and Fabric powered on",
        icon = "icon-cycle")
@ActionDefinition(name = "Only FAM + Fabric Powered ON", type = ActionTypes.Aggregation,
        id = Const.ACTION_ONLY_FAM_FABRIC_POWERED_ON, description = "Only FAM and Fabric powered on",
        icon = "icon-cycle")

@ActionDefinition(name = "Only FAM Powered ON", type = ActionTypes.Item, id = Const.ACTION_ONLY_FAM_POWERED_ON,
        description = "Only FAM powered on", icon = "icon-cycle")
@ActionDefinition(name = "Only FAM Powered ON", type = ActionTypes.Aggregation, id = Const.ACTION_ONLY_FAM_POWERED_ON,
        description = "Only FAM powered on", icon = "icon-cycle")
@ActionDefinition(name = "Bind OS Manifest to all SoCs", type = ActionTypes.Aggregation,
        id = Const.ACTION_BIND_OS_MANIFEST_TO_INSTANCE,
        description = "Set selected OS Manifest to all SoCs in the instance", icon = "icon-readonly",
        parameters = {@ActionParameter(id = "manifest", name = "OS Manifest",
                type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
@ActionDefinition(name = "Bind OS Manifest to all SoCs", type = ActionTypes.Item,
        id = Const.ACTION_BIND_OS_MANIFEST_TO_INSTANCE,
        description = "Set selected OS Manifest to all SoCs in the instance", icon = "icon-readonly",
        parameters = {@ActionParameter(id = "manifest", name = "OS Manifest",
                type = com.hp.hpl.loom.model.ActionParameter.Type.STRING)})
@ActionDefinition(name = "Prepare for System Boot", type = ActionTypes.Item, id = Const.ACTION_PREPARE_FOR_SYSTEM_BOOT,
        description = "Submit an all-off current state json, and a selectable UDS.", icon = "icon-update")
@ActionDefinition(name = "Prepare for System Boot", type = ActionTypes.Thread,
        id = Const.ACTION_PREPARE_FOR_SYSTEM_BOOT, description = "Primes AA with new initial and current states",
        icon = "icon-update")
@ActionDefinition(name = "Set AA Mode", type = ActionTypes.Item, id = Const.ACTION_SET_AA_MODE,
        description = "Modify operating mode of AA - Debug (TRUE/FALSE), Pause (TRUE/FALSE)", icon = "icon-update",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, id = "aa_mode",
                ranges = {@ActionRange(id = Const.ACTION_PAUSE, name = "Pause"),
                        @ActionRange(id = Const.ACTION_UNPAUSE, name = "Unpause"),
                        @ActionRange(id = Const.ACTION_SET_DEBUG, name = "Set Debug Mode"),
                        @ActionRange(id = Const.ACTION_UNSET_DEBUG, name = "Unset Debug Mode")})})
@ActionDefinition(name = "Sync OS Manifest for all SoCs", type = ActionTypes.Aggregation,
        id = Const.ACTION_SYNC_OS_MANIFEST_TO_INSTANCE, description = "Sync OS Manifests for all related SoCs",
        icon = "icon-readonly")
@ActionDefinition(name = "Sync OS Manifest for all SoCs", type = ActionTypes.Item,
        id = Const.ACTION_SYNC_OS_MANIFEST_TO_INSTANCE, description = "Sync OS Manifests for all related SoCs",
        icon = "icon-readonly")
@ActionDefinition(name = "Set Memory Test", type = ActionTypes.Item, id = Const.ACTION_SET_MEMORY_TEST,
        description = "Instance admins can modify weather or not the memory test is performed when an SoC is booted.",
        icon = "icon-update",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, id = "memtest",
                ranges = {@ActionRange(id = Const.ACTION_ENABLE_MEMTEST, name = "Enable"),
                        @ActionRange(id = Const.ACTION_DISABLE_MEMTEST, name = "Disable")})})

@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear instance alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear instance alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear instance alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMInstance extends DmaBaseItem<TMInstance.ItemAttributes> {
    private static final Log LOG = LogFactory.getLog(TMInstance.class);

    private static final String AA_PAUSED = "PAUSED";

    private static final int AA_PAUSED_ALERT_LEVEL = 5;
    private static final String AA_PAUSED_ALERT_DESCRIPTION = "AA is PAUSED";

    private long latestMetricUpdateTimestamp;

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE METRICS -------------------------- //
    // ------------------------------------------------------------------ //

    @LoomAttribute(type = NumericAttribute.class, key = "CPU utilisation", min = "0", max = "100", plottable = true,
            unit = "%", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "DRAM utilisation", min = "0", max = "100", plottable = true,
            unit = "%", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number dramUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "CPU Stolen Utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuStolen;

    @LoomAttribute(type = NumericAttribute.class, key = "CPU System Utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuSystem;

    @LoomAttribute(type = NumericAttribute.class, key = "CPU User Utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuUser;

    @LoomAttribute(type = NumericAttribute.class, key = "CPU Wait Percentage", min = "0", max = "100", plottable = true,
            unit = "%", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuWait;

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

    public void setDiskInodeUsed(final Number diskInodeUsed) {
        this.diskInodeUsed = diskInodeUsed;
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

    @LoomAttribute(type = NumericAttribute.class, key = "Disk Inode Utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number diskInodeUsed;

    @LoomAttribute(type = NumericAttribute.class, key = "Disk Space Utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number diskSpaceUsed;

    @LoomAttribute(type = NumericAttribute.class, key = "Network Input Bytes per second", min = "0", max = "inf",
            unit = "B/s", plottable = true,
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

    @LoomAttribute(type = NumericAttribute.class, key = "Fabric utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private Number fabricUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "Fabric utilisation", min = "0", max = "inf", unit = "GB/s",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private Number fabricBandwidth;


    // ------------------------------------------------------------------ //
    // ------------------------ CORE ATTRIBUTES ------------------------- //
    // ------------------------------------------------------------------ //

    public static class ItemAttributes extends DmaCoreItemAttributes {

        @LoomAttribute(key = "Coordinate", supportedOperations = {DefaultOperations.SORT_BY})
        private String coordinate;

        @LoomAttribute(key = "FQDN", supportedOperations = {DefaultOperations.SORT_BY})
        private String fqdn;

        @LoomAttribute(key = "FAM power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String famPowerState;

        @LoomAttribute(key = "Desired FAM power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredFamPowerState;

        @LoomAttribute(key = "Fabric power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String fabricPowerState;

        @LoomAttribute(key = "Desired fabric power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredFabricPowerState;

        @LoomAttribute(key = "SoCs", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String socs;

        @LoomAttribute(key = "Cores", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String cores;

        @LoomAttribute(key = "DRAM", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String dram;

        @LoomAttribute(key = "FAM", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String fam;

        @LoomAttribute(key = "Rated fabric bandwidth", supportedOperations = {DefaultOperations.SORT_BY})
        private String ratedFabricBandwidth;

        @LoomAttribute(key = "AA mode", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String aaMode;

        @LoomAttribute(key = "AA phase", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String aaPhase;

        private String path;

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

        @LoomAttribute(type = NumericAttribute.class, key = "FAM utilisation", min = "0", max = "100", unit = "%",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Double famUtilisation;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian SoCs Total", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number socTotal;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian SoCs Active", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number socActive;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian SoCs Offline", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number socOffline;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Pools Total", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number poolTotal;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Pools Active", min = "0",
                max = "Infinity", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number poolActive;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Memory Pools Offline", min = "0",
                max = "Infinity", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number poolOffline;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Books Active", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number libBooks;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Shelves Active", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number libShelves;

        @LoomAttribute(type = NumericAttribute.class, key = "Librarian Max Books", min = "0", max = "Infinity",
                plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
                ignoreUpdate = true)
        private Number libMaxBooks;



        // ------------------------------------------------------------------ //
        // -------------------- GETTERS AND SETTERS ------------------------- //
        // ------------------------------------------------------------------ //

        public void setFqdn(final String name) {
            fqdn = name;
        }

        public String getFqdn() {
            return fqdn;
        }

        public String getRatedFabricBandwidth() {
            return ratedFabricBandwidth;
        }

        public void setRatedFabricBandwidth(final String ratedFabricBandwidth) {
            this.ratedFabricBandwidth = ratedFabricBandwidth;
        }

        public String getAaMode() {
            return aaMode;
        }

        public void setAaMode(final String aaMode) {
            this.aaMode = aaMode;
        }

        public String getAaPhase() {
            return aaPhase;
        }

        public void setAaPhase(final String aaPhase) {
            this.aaPhase = aaPhase;
        }

        public String getFabricPowerState() {
            return fabricPowerState;
        }

        public void setFabricPowerState(final String fabricPowerState) {
            this.fabricPowerState = fabricPowerState;
        }

        public String getDesiredFabricPowerState() {
            return desiredFabricPowerState;
        }

        public void setDesiredFabricPowerState(final String desiredFabricPowerState) {
            this.desiredFabricPowerState = desiredFabricPowerState;
        }

        public String getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(final String coordinate) {
            this.coordinate = coordinate;
        }

        public String getCores() {
            return cores;
        }

        public void setCores(final String cores) {
            this.cores = cores;
        }

        public String getDram() {
            return dram;
        }

        public void setDram(final String dram) {
            this.dram = dram;
        }

        public String getFam() {
            return fam;
        }

        public void setFam(final String fam) {
            this.fam = fam;
        }

        public String getSocs() {
            return socs;
        }

        public void setSocs(final String socs) {
            this.socs = socs;
        }

        public String getFamPowerState() {
            return famPowerState;
        }

        public void setFamPowerState(final String famPowerState) {
            this.famPowerState = famPowerState;
        }

        public String getDesiredFamPowerState() {
            return desiredFamPowerState;
        }

        public void setDesiredFamPowerState(final String desiredFamPowerState) {
            this.desiredFamPowerState = desiredFamPowerState;
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

        public Double getFamUtilisation() {
            return famUtilisation;
        }

        public void setFamUtilisation(final Double famUtilisation) {
            this.famUtilisation = famUtilisation;
        }

        public Number getSocTotal() {
            return socTotal;
        }

        public void setSocTotal(final Number socTotal) {
            this.socTotal = socTotal;
        }

        public Number getSocActive() {
            return socActive;
        }

        public void setSocActive(final Number socActive) {
            this.socActive = socActive;
        }

        public Number getSocOffline() {
            return socOffline;
        }

        public void setSocOffline(final Number socOffline) {
            this.socOffline = socOffline;
        }

        public Number getPoolTotal() {
            return poolTotal;
        }

        public void setPoolTotal(final Number poolTotal) {
            this.poolTotal = poolTotal;
        }

        public Number getPoolActive() {
            return poolActive;
        }

        public void setPoolActive(final Number poolActive) {
            this.poolActive = poolActive;
        }

        public Number getPoolOffline() {
            return poolOffline;
        }

        public void setPoolOffline(final Number poolOffline) {
            this.poolOffline = poolOffline;
        }

        public Number getLibBooks() {
            return libBooks;
        }

        public void setLibBooks(final Number libBooks) {
            this.libBooks = libBooks;
        }

        public Number getLibShelves() {
            return libShelves;
        }

        public void setLibShelves(final Number libShelves) {
            this.libShelves = libShelves;
        }

        public Number getLibMaxBooks() {
            return libMaxBooks;
        }

        public void setLibMaxBooks(final Number libMaxBooks) {
            this.libMaxBooks = libMaxBooks;
        }

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }
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

    public Number getFabricUtilisation() {
        return fabricUtilisation;
    }

    public void setFabricUtilisation(final Number fabricUtilisation) {
        this.fabricUtilisation = fabricUtilisation;
    }

    public Number getFabricBandwidth() {
        return fabricBandwidth;
    }

    public void setFabricBandwidth(final Number fabricBandwidth) {
        this.fabricBandwidth = fabricBandwidth;
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMInstance(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType, ItemAttributes.class);
    }

    private class NonCoreMetricAccumulation {
        private double total;
        private int count;

        public NonCoreMetricAccumulation(final double total, final int count) {
            this.total = total;
            this.count = count;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(final double total) {
            this.total = total;
        }

        public int getCount() {
            return count;
        }

        public void setCount(final int count) {
            this.count = count;
        }

        public double getAverage() {
            return total / count;
        }
    }

    private Map<String, NonCoreMetricAccumulation> initSocMetricsAccumulation() {
        HashMap<String, NonCoreMetricAccumulation> metrics = new HashMap<String, NonCoreMetricAccumulation>();
        metrics.put("cpuUtilisation", new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_DRAM_UTILISATION, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_CPU_STOLEN, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_CPU_SYSTEM, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_CPU_USER, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_CPU_WAIT, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_DISK_INODE_USED, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_DISK_SPACE_USED, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_IN_BYTES_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_IN_ERRORS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_IN_PACKETS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_OUT_BYTES_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_OUT_ERRORS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_NET_OUT_PACKETS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metrics.put(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION, new NonCoreMetricAccumulation(0.0, 0));
        return metrics;
    }

    private Map<String, NonCoreMetricAccumulation> accumulate(final Map<String, NonCoreMetricAccumulation> metrics,
            final String key, final double increase) {
        NonCoreMetricAccumulation metric = metrics.get(key);
        metric.setCount(metric.getCount() + 1);
        metric.setTotal(metric.getTotal() + increase);
        metrics.put(key, metric);
        return metrics;
    }

    private Map<String, NonCoreMetricAccumulation> accumulateSocMetrics() {
        Map<String, NonCoreMetricAccumulation> metrics = initSocMetricsAccumulation();

        Collection<Item> racks = this.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));

        if (racks == null) {
            return Collections.emptyMap();
        }

        for (Item rackItem : racks) {
            Collection<Item> enclosures = rackItem.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rackItem.getProviderType(),
                            Types.ENCLOSURE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));

            if (enclosures == null) {
                continue;
            }

            for (Item enclosureItem : enclosures) {
                Collection<Item> nodes = enclosureItem.getConnectedItemsWithRelationshipName(
                        RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(enclosureItem.getProviderType(),
                                Types.NODE_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));

                if (nodes == null) {
                    continue;
                }

                for (Item nodeItem : nodes) {
                    Collection<Item> socs = nodeItem.getConnectedItemsWithRelationshipName(
                            RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(nodeItem.getProviderType(),
                                    Types.SOC_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));

                    if (socs == null) {
                        continue;
                    }

                    for (Item socItem : socs) {
                        TMSoc soc = (TMSoc) socItem;
                        if (null != soc.getCore().getCpuUtilisation()) {
                            metrics = accumulate(metrics, "cpuUtilisation",
                                    soc.getCore().getCpuUtilisation().doubleValue());
                        }
                        if (null != soc.getCore().getDramUtilisation()) {
                            metrics = accumulate(metrics, Const.METRIC_DRAM_UTILISATION,
                                    soc.getCore().getDramUtilisation().doubleValue());
                        }
                        if (null != soc.getCore().getCpuStolen()) {
                            metrics = accumulate(metrics, Const.METRIC_CPU_STOLEN,
                                    soc.getCore().getCpuStolen().doubleValue());
                        }
                        if (null != soc.getCore().getCpuSystem()) {
                            metrics = accumulate(metrics, Const.METRIC_CPU_SYSTEM,
                                    soc.getCore().getCpuSystem().doubleValue());
                        }
                        if (null != soc.getCore().getCpuUser()) {
                            metrics = accumulate(metrics, Const.METRIC_CPU_USER,
                                    soc.getCore().getCpuUser().doubleValue());
                        }
                        if (null != soc.getCore().getCpuWait()) {
                            metrics = accumulate(metrics, Const.METRIC_CPU_WAIT,
                                    soc.getCore().getCpuWait().doubleValue());
                        }
                        if (null != soc.getCore().getDiskInodeUsed()) {
                            metrics = accumulate(metrics, Const.METRIC_DISK_INODE_USED,
                                    soc.getCore().getDiskInodeUsed().doubleValue());
                        }
                        if (null != soc.getCore().getDiskSpaceUsed()) {
                            metrics = accumulate(metrics, Const.METRIC_DISK_SPACE_USED,
                                    soc.getCore().getDiskSpaceUsed().doubleValue());
                        }
                        if (null != soc.getCore().getNetInBytesSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_IN_BYTES_SEC,
                                    soc.getCore().getNetInBytesSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetInErrorsSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_IN_ERRORS_SEC,
                                    soc.getCore().getNetInErrorsSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetInPacketsDroppedSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_IN_PACKETS_DROPPED_SEC,
                                    soc.getCore().getNetInPacketsDroppedSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetInPacketsSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_IN_PACKETS_SEC,
                                    soc.getCore().getNetInPacketsSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetOutBytesSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_OUT_BYTES_SEC,
                                    soc.getCore().getNetOutBytesSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetOutErrorsSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_OUT_ERRORS_SEC,
                                    soc.getCore().getNetOutErrorsSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetOutPacketsDroppedSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC,
                                    soc.getCore().getNetOutPacketsDroppedSec().doubleValue());
                        }
                        if (null != soc.getCore().getNetOutPacketsSec()) {
                            metrics = accumulate(metrics, Const.METRIC_NET_OUT_PACKETS_SEC,
                                    soc.getCore().getNetOutPacketsSec().doubleValue());
                        }
                        if (null != soc.getCore().getBridgeIciLinkUtilisation()) {
                            metrics = accumulate(metrics, Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION,
                                    soc.getCore().getBridgeIciLinkUtilisation().doubleValue());
                        }
                    }
                }
            }
        }
        return metrics;
    }

    private boolean updateAccumulatedMetrics(final Map<String, NonCoreMetricAccumulation> metrics) {
        boolean anyUpdate = false;
        if (metrics == null) {
            return false;
        }
        if (metrics.get("cpuUtilisation") != null && metrics.get("cpuUtilisation").getCount() > 0) {
            double avg = metrics.get("cpuUtilisation").getAverage();
            if (this.getCpuUtilisation() == null || !this.getCpuUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setCpuUtilisation(avg);
            }
        }
        if (metrics.get(Const.METRIC_DRAM_UTILISATION) != null
                && metrics.get(Const.METRIC_DRAM_UTILISATION).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_DRAM_UTILISATION).getAverage();
            if (this.getDramUtilisation() == null || !this.getDramUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setDramUtilisation(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_STOLEN) != null && metrics.get(Const.METRIC_CPU_STOLEN).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_STOLEN).getAverage();
            if (this.getCpuStolen() == null || !this.getCpuStolen().equals(avg)) {
                anyUpdate = true;
                this.setCpuStolen(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_SYSTEM) != null && metrics.get(Const.METRIC_CPU_SYSTEM).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_SYSTEM).getAverage();
            if (this.getCpuSystem() == null || !this.getCpuSystem().equals(avg)) {
                anyUpdate = true;
                this.setCpuSystem(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_USER) != null && metrics.get(Const.METRIC_CPU_USER).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_USER).getAverage();
            if (this.getCpuUser() == null || !this.getCpuUser().equals(avg)) {
                anyUpdate = true;
                this.setCpuUser(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_WAIT) != null && metrics.get(Const.METRIC_CPU_WAIT).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_WAIT).getAverage();
            if (this.getCpuWait() == null || !this.getCpuWait().equals(avg)) {
                anyUpdate = true;
                this.setCpuWait(avg);
            }
        }
        if (metrics.get(Const.METRIC_DISK_INODE_USED) != null
                && metrics.get(Const.METRIC_DISK_INODE_USED).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_DISK_INODE_USED).getAverage();
            if (this.getDiskInodeUsed() == null || !this.getDiskInodeUsed().equals(avg)) {
                anyUpdate = true;
                this.setDiskInodeUsed(avg);
            }
        }
        if (metrics.get(Const.METRIC_DISK_SPACE_USED) != null
                && metrics.get(Const.METRIC_DISK_SPACE_USED).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_DISK_SPACE_USED).getAverage();
            if (this.getDiskSpaceUsed() == null || !this.getDiskSpaceUsed().equals(avg)) {
                anyUpdate = true;
                this.setDiskSpaceUsed(avg);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_BYTES_SEC) != null
                && metrics.get(Const.METRIC_NET_IN_BYTES_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_BYTES_SEC).getTotal();
            if (this.getNetInBytesSec() == null || !this.getNetInBytesSec().equals(total)) {
                anyUpdate = true;
                this.setNetInBytesSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_ERRORS_SEC) != null
                && metrics.get(Const.METRIC_NET_IN_ERRORS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_ERRORS_SEC).getTotal();
            if (this.getNetInErrorsSec() == null || !this.getNetInErrorsSec().equals(total)) {
                anyUpdate = true;
                this.setNetInErrorsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC) != null
                && metrics.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC).getTotal();
            if (this.getNetInPacketsDroppedSec() == null || !this.getNetInPacketsDroppedSec().equals(total)) {
                anyUpdate = true;
                this.setNetInPacketsDroppedSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_PACKETS_SEC) != null
                && metrics.get(Const.METRIC_NET_IN_PACKETS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_PACKETS_SEC).getTotal();
            if (this.getNetInPacketsSec() == null || !this.getNetInPacketsSec().equals(total)) {
                anyUpdate = true;
                this.setNetInPacketsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_BYTES_SEC) != null
                && metrics.get(Const.METRIC_NET_OUT_BYTES_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_BYTES_SEC).getTotal();
            if (this.getNetOutBytesSec() == null || !this.getNetOutBytesSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutBytesSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_ERRORS_SEC) != null
                && metrics.get(Const.METRIC_NET_OUT_ERRORS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_ERRORS_SEC).getTotal();
            if (this.getNetOutErrorsSec() == null || !this.getNetOutErrorsSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutErrorsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC) != null
                && metrics.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC).getTotal();
            if (this.getNetOutPacketsDroppedSec() == null || !this.getNetOutPacketsDroppedSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutPacketsDroppedSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_PACKETS_SEC) != null
                && metrics.get(Const.METRIC_NET_OUT_PACKETS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_PACKETS_SEC).getTotal();
            if (this.getNetOutPacketsSec() == null || !this.getNetOutPacketsSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutPacketsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION) != null
                && metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION).getAverage();
            if (this.getFabricUtilisation() == null || !this.getFabricUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setFabricUtilisation(avg);
            }
        }
        return anyUpdate;
    }

    /**
     * Collect the metrics from Nodes and FAM modules that this Enclosure contains, aggregate them
     * according to the type of metric, and set the values for visualization.
     */
    private boolean updateNonCoreAttributes() {
        Map<String, NonCoreMetricAccumulation> metrics = accumulateSocMetrics();
        boolean anyUpdate = updateAccumulatedMetrics(metrics);
        if (anyUpdate) {
            this.setLatestMetricUpdateTimestamp(System.currentTimeMillis());
        }
        return anyUpdate;
    }

    /*
     * Update alert levels.
     */
    @Override
    public boolean update() {
        boolean updated = super.update();
        boolean anyUpdate = updateNonCoreAttributes();
        ItemAttributes core = getCore();

        // Only record the highest level alert.

        if (core.getAaMode().contains(AA_PAUSED)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Instance " + core.getItemId() + " has PAUSED AA");
            }

            if (getAlertLevel() < AA_PAUSED_ALERT_LEVEL) {
                setAlertLevel(AA_PAUSED_ALERT_LEVEL);
                setAlertDescription(AA_PAUSED_ALERT_DESCRIPTION);

                anyUpdate = true;
            }
        } else if (getAlertLevel() == AA_PAUSED_ALERT_LEVEL
                && getAlertDescription().equals(AA_PAUSED_ALERT_DESCRIPTION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Clearing AA PAUSED alert for Instance " + core.getItemId());
            }

            setAlertLevel(0);
            setAlertDescription("");
        }

        return updated | anyUpdate;
    }

    public long getLatestMetricUpdateTimestamp() {
        return latestMetricUpdateTimestamp;
    }

    public void setLatestMetricUpdateTimestamp(final long latestMetricUpdateTimestamp) {
        this.latestMetricUpdateTimestamp = latestMetricUpdateTimestamp;
    }
}
