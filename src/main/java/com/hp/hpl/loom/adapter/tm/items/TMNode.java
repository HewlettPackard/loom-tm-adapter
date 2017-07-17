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
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@ItemTypeInfo(value = Types.NODE_TYPE_LOCAL_ID, layers = {Types.INFRASTRUCTURE_LAYER})
@ConnectedTo(toClass = TMSoc.class,
        relationshipDetails = @LoomAttribute(key = "Soc", supportedOperations = {DefaultOperations.GROUP_BY}),
        type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
// @ConnectedTo(toClass = TMBook.class, type = RelationshipNames.HOSTED, typeName =
// RelationshipNames.HOSTED_NAME)
@ConnectedTo(toClass = TMMemoryBoard.class, type = RelationshipNames.CONTAINS,
        typeName = RelationshipNames.CONTAINS_NAME)
@ConnectedTo(toClass = TMEnclosure.class,
        relationshipDetails = @LoomAttribute(key = "Enclosure", supportedOperations = {DefaultOperations.GROUP_BY}),
        type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
@ActionDefinition(name = "Update Firmware", type = ActionTypes.Item, id = "Update Firmware",
        description = "Update Firmware", icon = "icon-update",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, id = "manage",
                ranges = {@ActionRange(id = "doit", name = "Update")})})
@ActionDefinition(name = "Update Firmware", type = ActionTypes.Aggregation, id = "Update Firmware",
        description = "Update Firmware for all nodes of this aggregation", icon = "icon-update",
        parameters = {@ActionParameter(type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED, id = "manage",
                ranges = {@ActionRange(id = "doit", name = "Update")})})
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear node alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear node alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear node alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMNode extends DmaBaseItem<TMNode.ItemAttributes> {

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE ATTRIBUTES ----------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE METRICS -------------------------- //
    // ------------------------------------------------------------------ //

    @LoomAttribute(type = NumericAttribute.class, key = "CPU utilisation", min = "0", max = "100", plottable = true,
            unit = "%", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "Bridge fabric utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number bridgeFabricUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "Bridge fabric bandwidth", min = "0", max = "inf",
            unit = "GB/s", plottable = true,
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
    private Number bridgeFabricBandwidth;

    @LoomAttribute(type = NumericAttribute.class, key = "FAM module fabric utilisation", min = "0", max = "100",
            unit = "%", plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number famModuleFabricUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "FAM module fabric bandwidth", min = "0", max = "inf",
            unit = "GB/s", plottable = true,
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
    private Number famModuleFabricBandwidth;

    @LoomAttribute(type = NumericAttribute.class, key = "DRAM utilisation", min = "0", max = "100", plottable = true,
            unit = "%", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
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
            unit = "B/s", plottable = true,
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

    public static class ItemAttributes extends DmaCoreItemAttributes {

        @LoomAttribute(key = "Coordinate", supportedOperations = {DefaultOperations.SORT_BY})
        private String coordinate;

        @LoomAttribute(key = "Power state",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String powerState;

        @LoomAttribute(key = "Desired Power state", supportedOperations = {DefaultOperations.GROUP_BY})
        private String desiredPowerState;

        @LoomAttribute(key = "Cores", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number cores;

        @LoomAttribute(key = "DRAM", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number dram;

        @LoomAttribute(key = "FAM", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String fam;

        @LoomAttribute(key = "Fabric bandwidth", supportedOperations = {DefaultOperations.SORT_BY})
        private String fabricBandwidth;

        @LoomAttribute(key = "MP firmware version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String mpFirmwareVersion;

        @JsonIgnore
        @LoomAttribute(key = "Desired MP firmware version",
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String desiredMpFirmwareVersion;

        private String desiredLocalPower;

        // ------------------------------------------------------------------ //
        // ------------------------ CORE METRICS ---------------------------- //
        // ------------------------------------------------------------------ //

        // @LoomAttribute(key = "Percentage of allocated book", ignoreUpdate = true, max = "100",
        // min = "0", unit = "%",
        // visible = true, plottable = true, type = NumericAttribute.class,
        // supportedOperations = {DefaultOperations.GROUP_BY})
        // private Double bookAllocatedPct;

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

        @LoomAttribute(type = NumericAttribute.class, key = "FAM Utilisation", min = "0", max = "100", plottable = true,
                unit = "%", supportedOperations = {DefaultOperations.SORT_BY}, ignoreUpdate = true)
        private Double famUtilisation;

        // ------------------------------------------------------------------ //
        // -------------------- GETTERS AND SETTERS ------------------------- //
        // ------------------------------------------------------------------ //

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

        public Number getCores() {
            return cores;
        }

        public void setCores(final Number cores) {
            this.cores = cores;
        }

        public Number getDram() {
            return dram;
        }

        public void setDram(final Number dram) {
            this.dram = dram;
        }

        public String getFam() {
            return fam;
        }

        public void setFam(final String fam) {
            this.fam = fam;
        }

        public String getFabricBandwidth() {
            return fabricBandwidth;
        }

        public void setFabricBandwidth(final String fabricBandwidth) {
            this.fabricBandwidth = fabricBandwidth;
        }

        public String getMpFirmwareVersion() {
            return mpFirmwareVersion;
        }

        public void setMpFirmwareVersion(final String mpFirmwareVersion) {
            this.mpFirmwareVersion = mpFirmwareVersion;
        }

        // public Double getBookAllocatedPct() {
        // return bookAllocatedPct;
        // }
        //
        // public void setBookAllocatedPct(Double bookAllocatedPct) {
        // this.bookAllocatedPct = bookAllocatedPct;
        // }

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

        public String getDesiredLocalPower() {
            return desiredLocalPower;
        }

        public void setDesiredLocalPower(final String desiredLocalPower) {
            this.desiredLocalPower = desiredLocalPower;
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

    public Number getBridgeFabricUtilisation() {
        return bridgeFabricUtilisation;
    }

    public void setBridgeFabricUtilisation(final Number bridgeFabricUtilisation) {
        this.bridgeFabricUtilisation = bridgeFabricUtilisation;
    }

    public Number getBridgeFabricBandwidth() {
        return bridgeFabricBandwidth;
    }

    public void setBridgeFabricBandwidth(final Number bridgeFabricBandwidth) {
        this.bridgeFabricBandwidth = bridgeFabricBandwidth;
    }

    public Number getFamModuleFabricUtilisation() {
        return famModuleFabricUtilisation;
    }

    public void setFamModuleFabricUtilisation(final Number famModuleFabricUtilisation) {
        this.famModuleFabricUtilisation = famModuleFabricUtilisation;
    }

    public Number getFamModuleFabricBandwidth() {
        return famModuleFabricBandwidth;
    }

    public void setFamModuleFabricBandwidth(final Number famModuleFabricBandwidth) {
        this.famModuleFabricBandwidth = famModuleFabricBandwidth;
    }


    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMNode(final String logicalId, final ItemType itemType) {
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

    private HashMap<String, NonCoreMetricAccumulation> initSocMetricsAccumulation() {
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
        metrics.put(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION_FAM, new NonCoreMetricAccumulation(0.0, 0));
        return metrics;
    }

    private HashMap<String, NonCoreMetricAccumulation> accumulate(
            final HashMap<String, NonCoreMetricAccumulation> metrics, final String key, final double increase) {
        NonCoreMetricAccumulation metric = metrics.get(key);
        metric.setCount(metric.getCount() + 1);
        metric.setTotal(metric.getTotal() + increase);
        metrics.put(key, metric);
        return metrics;
    }

    private HashMap<String, NonCoreMetricAccumulation> accumulateMetrics() {
        HashMap<String, NonCoreMetricAccumulation> metrics = initSocMetricsAccumulation();
        Collection<Item> socs = this.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
                        Types.SOC_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        for (Item socItem : socs) {
            TMSoc soc = (TMSoc) socItem;
            if (null != soc.getCore().getCpuUtilisation()) {
                metrics = accumulate(metrics, "cpuUtilisation", soc.getCore().getCpuUtilisation().doubleValue());
            }
            if (null != soc.getCore().getDramUtilisation()) {
                metrics = accumulate(metrics, Const.METRIC_DRAM_UTILISATION,
                        soc.getCore().getDramUtilisation().doubleValue());
            }
            if (null != soc.getCore().getCpuStolen()) {
                metrics = accumulate(metrics, Const.METRIC_CPU_STOLEN, soc.getCore().getCpuStolen().doubleValue());
            }
            if (null != soc.getCore().getCpuSystem()) {
                metrics = accumulate(metrics, Const.METRIC_CPU_SYSTEM, soc.getCore().getCpuSystem().doubleValue());
            }
            if (null != soc.getCore().getCpuUser()) {
                metrics = accumulate(metrics, Const.METRIC_CPU_USER, soc.getCore().getCpuUser().doubleValue());
            }
            if (null != soc.getCore().getCpuWait()) {
                metrics = accumulate(metrics, Const.METRIC_CPU_WAIT, soc.getCore().getCpuWait().doubleValue());
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
            if (null != soc.getCore().getBridgePsylockeUtilisation()) {
                metrics = accumulate(metrics, Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION_FAM,
                        soc.getCore().getBridgePsylockeUtilisation().doubleValue());
            }
        }
        return metrics;
    }

    private boolean updateAccumulatedMetrics(final HashMap<String, NonCoreMetricAccumulation> metrics) {
        boolean anyUpdate = false;
        if (metrics.get("cpuUtilisation").getCount() > 0) {
            double avg = metrics.get("cpuUtilisation").getAverage();
            if (this.getCpuUtilisation() == null || !this.getCpuUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setCpuUtilisation(avg);
            }
        }
        if (metrics.get(Const.METRIC_DRAM_UTILISATION).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_DRAM_UTILISATION).getAverage();
            if (this.getDramUtilisation() == null || !this.getDramUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setDramUtilisation(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_STOLEN).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_STOLEN).getAverage();
            if (this.getCpuStolen() == null || !this.getCpuStolen().equals(avg)) {
                anyUpdate = true;
                this.setCpuStolen(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_SYSTEM).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_SYSTEM).getAverage();
            if (this.getCpuSystem() == null || !this.getCpuSystem().equals(avg)) {
                anyUpdate = true;
                this.setCpuSystem(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_USER).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_USER).getAverage();
            if (this.getCpuUser() == null || !this.getCpuUser().equals(avg)) {
                anyUpdate = true;
                this.setCpuUser(avg);
            }
        }
        if (metrics.get(Const.METRIC_CPU_WAIT).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_CPU_WAIT).getAverage();
            if (this.getCpuWait() == null || !this.getCpuWait().equals(avg)) {
                anyUpdate = true;
                this.setCpuWait(avg);
            }
        }
        if (metrics.get(Const.METRIC_DISK_INODE_USED).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_DISK_INODE_USED).getAverage();
            if (this.getDiskInodeUsed() == null || !this.getDiskInodeUsed().equals(avg)) {
                anyUpdate = true;
                this.setDiskInodeUsed(avg);
            }
        }
        if (metrics.get(Const.METRIC_DISK_SPACE_USED).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_DISK_SPACE_USED).getAverage();
            if (this.getDiskSpaceUsed() == null || !this.getDiskSpaceUsed().equals(avg)) {
                anyUpdate = true;
                this.setDiskSpaceUsed(avg);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_BYTES_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_BYTES_SEC).getTotal();
            if (this.getNetInBytesSec() == null || !this.getNetInBytesSec().equals(total)) {
                anyUpdate = true;
                this.setNetInBytesSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_ERRORS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_ERRORS_SEC).getTotal();
            if (this.getNetInErrorsSec() == null || !this.getNetInErrorsSec().equals(total)) {
                anyUpdate = true;
                this.setNetInErrorsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC).getTotal();
            if (this.getNetInPacketsDroppedSec() == null || !this.getNetInPacketsDroppedSec().equals(total)) {
                anyUpdate = true;
                this.setNetInPacketsDroppedSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_IN_PACKETS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_IN_PACKETS_SEC).getTotal();
            if (this.getNetInPacketsSec() == null || !this.getNetInPacketsSec().equals(total)) {
                anyUpdate = true;
                this.setNetInPacketsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_BYTES_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_BYTES_SEC).getTotal();
            if (this.getNetOutBytesSec() == null || !this.getNetOutBytesSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutBytesSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_ERRORS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_ERRORS_SEC).getTotal();
            if (this.getNetOutErrorsSec() == null || !this.getNetOutErrorsSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutErrorsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC).getTotal();
            if (this.getNetOutPacketsDroppedSec() == null || !this.getNetOutPacketsDroppedSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutPacketsDroppedSec(total);
            }
        }
        if (metrics.get(Const.METRIC_NET_OUT_PACKETS_SEC).getCount() > 0) {
            double total = metrics.get(Const.METRIC_NET_OUT_PACKETS_SEC).getTotal();
            if (this.getNetOutPacketsSec() == null || !this.getNetOutPacketsSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutPacketsSec(total);
            }
        }
        if (metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION).getAverage();
            if (this.getBridgeFabricUtilisation() == null || !this.getBridgeFabricUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setBridgeFabricUtilisation(avg);
            }
        }
        if (metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION_FAM).getCount() > 0) {
            double avg = metrics.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION_FAM).getAverage();
            if (this.getFamModuleFabricUtilisation() == null || !this.getFamModuleFabricUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setFamModuleFabricUtilisation(avg);
            }
        }
        return anyUpdate;
    }

    /**
     * Collect the metrics from SoCs and FAM modules that this Node contains, aggregate them
     * according to the type of metric, and set the values for visualization.
     */
    private boolean updateNonCoreAttributes() {
        HashMap<String, NonCoreMetricAccumulation> metrics = accumulateMetrics();
        return updateAccumulatedMetrics(metrics);
    }

    @Override
    public boolean update() {
        return super.update() | updateNonCoreAttributes();
    }
}
