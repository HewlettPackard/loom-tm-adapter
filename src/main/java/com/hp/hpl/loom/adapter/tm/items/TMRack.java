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

import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
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

@ItemTypeInfo(value = Types.RACK_TYPE_LOCAL_ID, layers = {Types.INFRASTRUCTURE_LAYER})
@ConnectedTo(toClass = TMEnclosure.class, type = RelationshipNames.CONTAINS, typeName = RelationshipNames.CONTAINS_NAME)
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear rack alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear rack alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear rack alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMRack extends DmaBaseItem<TMRack.ItemAttributes> {

    // ------------------------------------------------------------------ //
    // ----------------------- PRIVATE FIELDS --------------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE METRICS -------------------------- //
    // ------------------------------------------------------------------ //

    @LoomAttribute(type = NumericAttribute.class, key = "CPU utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number cpuUtilisation;

    @LoomAttribute(type = NumericAttribute.class, key = "DRAM utilisation", min = "0", max = "100", unit = "%",
            plottable = true, supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY},
            ignoreUpdate = true)
    private Number dramUtilisation;

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

        @LoomAttribute(key = "Cores", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private Number cores;

        @LoomAttribute(key = "DRAM", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String dram;

        @LoomAttribute(key = "FAM", supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
        private String fam;

        @LoomAttribute(key = "Fabric bandwidth", supportedOperations = {DefaultOperations.SORT_BY})
        private Number fabricBandwidth;

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

        private String path;

        // ------------------------------------------------------------------ //
        // -------------------- DESIRED STATE ------------------------------- //
        // ------------------------------------------------------------------ //

        private String desiredDefaultNextOsImageManifest;
        private String desiredDefaultRunningOsImageManifest;
        private String desiredPower;
        private boolean desiredForceAllFamFabricSocOff;
        private boolean desiredForceAllFabricSocOff;
        private boolean desiredForceAllSocOff;

        // ------------------------------------------------------------------ //
        // -------------------- GETTERS AND SETTERS ------------------------- //
        // ------------------------------------------------------------------ //

        public String getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(final String coordinate) {
            this.coordinate = coordinate;
        }

        public Number getCores() {
            return cores;
        }

        public void setCores(final Number cores) {
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

        public Number getFabricBandwidth() {
            return fabricBandwidth;
        }

        public void setFabricBandwidth(final Number fabricBandwidth) {
            this.fabricBandwidth = fabricBandwidth;
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

        public String getDesiredDefaultNextOsImageManifest() {
            return desiredDefaultNextOsImageManifest;
        }

        public void setDesiredDefaultNextOsImageManifest(final String desiredDefaultNextOsImageManifest) {
            this.desiredDefaultNextOsImageManifest = desiredDefaultNextOsImageManifest;
        }

        public String getDesiredDefaultRunningOsImageManifest() {
            return desiredDefaultRunningOsImageManifest;
        }

        public void setDesiredDefaultRunningOsImageManifest(final String desiredDefaultRunningOsImageManifest) {
            this.desiredDefaultRunningOsImageManifest = desiredDefaultRunningOsImageManifest;
        }

        public String getDesiredPower() {
            return desiredPower;
        }

        public void setDesiredPower(final String desiredPower) {
            this.desiredPower = desiredPower;
        }

        public boolean getDesiredForceAllFamFabricSocOff() {
            return desiredForceAllFamFabricSocOff;
        }

        public void setDesiredForceAllFamFabricSocOff(final boolean desiredForceAllFamFabricSocOff) {
            this.desiredForceAllFamFabricSocOff = desiredForceAllFamFabricSocOff;
        }

        public boolean getDesiredForceAllFabricSocOff() {
            return desiredForceAllFabricSocOff;
        }

        public void setDesiredForceAllFabricSocOff(final boolean desiredForceAllFabricSocOff) {
            this.desiredForceAllFabricSocOff = desiredForceAllFabricSocOff;
        }

        public boolean getDesiredForceAllSocOff() {
            return desiredForceAllSocOff;
        }

        public void setDesiredForceAllSocOff(final boolean desiredForceAllSocOff) {
            this.desiredForceAllSocOff = desiredForceAllSocOff;
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

    public TMRack(final String logicalId, final ItemType itemType) {
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

    private HashMap<String, NonCoreMetricAccumulation> initSocMetricsAndAttributesAccumulation() {
        HashMap<String, NonCoreMetricAccumulation> metAtt = new HashMap<String, NonCoreMetricAccumulation>();
        metAtt.put("cpuUtilisation", new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_DRAM_UTILISATION, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_CPU_STOLEN, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_CPU_SYSTEM, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_CPU_USER, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_CPU_WAIT, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_DISK_INODE_USED, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_DISK_SPACE_USED, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_IN_BYTES_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_IN_ERRORS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_IN_PACKETS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_OUT_BYTES_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_OUT_ERRORS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_NET_OUT_PACKETS_SEC, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.ATT_CORES, new NonCoreMetricAccumulation(0.0, 0));
        metAtt.put(Const.ATT_FABRIC_BANDWIDTH, new NonCoreMetricAccumulation(0.0, 0));
        return metAtt;
    }

    private HashMap<String, NonCoreMetricAccumulation> accumulate(
            final HashMap<String, NonCoreMetricAccumulation> metAtt, final String key, final double increase) {
        NonCoreMetricAccumulation ma = metAtt.get(key);
        ma.setCount(ma.getCount() + 1);
        ma.setTotal(ma.getTotal() + increase);
        metAtt.put(key, ma);
        return metAtt;
    }

    private HashMap<String, NonCoreMetricAccumulation> accumulateSocMetricsAndAttributes() {
        HashMap<String, NonCoreMetricAccumulation> metAtt = initSocMetricsAndAttributesAccumulation();

        Collection<Item> enclosures = this.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(this.getProviderType(),
                        Types.ENCLOSURE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        for (Item enclosureItem : enclosures) {
            Collection<Item> nodes = enclosureItem.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(enclosureItem.getProviderType(),
                            Types.NODE_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
            for (Item nodeItem : nodes) {
                Collection<Item> socs = nodeItem.getConnectedItemsWithRelationshipName(
                        RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(nodeItem.getProviderType(),
                                Types.SOC_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
                for (Item socItem : socs) {
                    TMSoc soc = (TMSoc) socItem;
                    if (null != soc.getCore().getCpuUtilisation()) {
                        metAtt = accumulate(metAtt, "cpuUtilisation", soc.getCore().getCpuUtilisation().doubleValue());
                    }
                    if (null != soc.getCore().getDramUtilisation()) {
                        metAtt = accumulate(metAtt, Const.METRIC_DRAM_UTILISATION,
                                soc.getCore().getDramUtilisation().doubleValue());
                    }
                    if (null != soc.getCore().getCpuStolen()) {
                        metAtt = accumulate(metAtt, Const.METRIC_CPU_STOLEN,
                                soc.getCore().getCpuStolen().doubleValue());
                    }
                    if (null != soc.getCore().getCpuSystem()) {
                        metAtt = accumulate(metAtt, Const.METRIC_CPU_SYSTEM,
                                soc.getCore().getCpuSystem().doubleValue());
                    }
                    if (null != soc.getCore().getCpuUser()) {
                        metAtt = accumulate(metAtt, Const.METRIC_CPU_USER, soc.getCore().getCpuUser().doubleValue());
                    }
                    if (null != soc.getCore().getCpuWait()) {
                        metAtt = accumulate(metAtt, Const.METRIC_CPU_WAIT, soc.getCore().getCpuWait().doubleValue());
                    }
                    if (null != soc.getCore().getDiskInodeUsed()) {
                        metAtt = accumulate(metAtt, Const.METRIC_DISK_INODE_USED,
                                soc.getCore().getDiskInodeUsed().doubleValue());
                    }
                    if (null != soc.getCore().getDiskSpaceUsed()) {
                        metAtt = accumulate(metAtt, Const.METRIC_DISK_SPACE_USED,
                                soc.getCore().getDiskSpaceUsed().doubleValue());
                    }
                    if (null != soc.getCore().getNetInBytesSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_IN_BYTES_SEC,
                                soc.getCore().getNetInBytesSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetInErrorsSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_IN_ERRORS_SEC,
                                soc.getCore().getNetInErrorsSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetInPacketsDroppedSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_IN_PACKETS_DROPPED_SEC,
                                soc.getCore().getNetInPacketsDroppedSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetInPacketsSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_IN_PACKETS_SEC,
                                soc.getCore().getNetInPacketsSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetOutBytesSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_OUT_BYTES_SEC,
                                soc.getCore().getNetOutBytesSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetOutErrorsSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_OUT_ERRORS_SEC,
                                soc.getCore().getNetOutErrorsSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetOutPacketsDroppedSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC,
                                soc.getCore().getNetOutPacketsDroppedSec().doubleValue());
                    }
                    if (null != soc.getCore().getNetOutPacketsSec()) {
                        metAtt = accumulate(metAtt, Const.METRIC_NET_OUT_PACKETS_SEC,
                                soc.getCore().getNetOutPacketsSec().doubleValue());
                    }
                    if (null != soc.getCore().getBridgeIciLinkUtilisation()) {
                        metAtt = accumulate(metAtt, Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION,
                                soc.getCore().getBridgeIciLinkUtilisation().doubleValue());
                    }
                    if (null != soc.getCore().getCores()) {
                        metAtt = accumulate(metAtt, Const.ATT_CORES, soc.getCore().getCores().doubleValue());
                    }
                    if (null != soc.getCore().getBridgeFabricLinkBandwidth()) {
                        metAtt = accumulate(metAtt, Const.ATT_FABRIC_BANDWIDTH,
                                soc.getCore().getBridgeFabricLinkBandwidth().doubleValue());
                    }
                }
            }
        }
        return metAtt;
    }

    private boolean updateAccumulatedMetricsAndAttributes(final HashMap<String, NonCoreMetricAccumulation> metAtt) {
        boolean anyUpdate = false;
        if (metAtt.get("cpuUtilisation").getCount() > 0) {
            double avg = metAtt.get("cpuUtilisation").getAverage();
            if (this.getCpuUtilisation() == null || !this.getCpuUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setCpuUtilisation(avg);
            }
        }
        if (metAtt.get(Const.METRIC_DRAM_UTILISATION).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_DRAM_UTILISATION).getAverage();
            if (this.getDramUtilisation() == null || !this.getDramUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setDramUtilisation(avg);
            }
        }
        if (metAtt.get(Const.METRIC_CPU_STOLEN).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_CPU_STOLEN).getAverage();
            if (this.getCpuStolen() == null || !this.getCpuStolen().equals(avg)) {
                anyUpdate = true;
                this.setCpuStolen(avg);
            }
        }
        if (metAtt.get(Const.METRIC_CPU_SYSTEM).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_CPU_SYSTEM).getAverage();
            if (this.getCpuSystem() == null || !this.getCpuSystem().equals(avg)) {
                anyUpdate = true;
                this.setCpuSystem(avg);
            }
        }
        if (metAtt.get(Const.METRIC_CPU_USER).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_CPU_USER).getAverage();
            if (this.getCpuUser() == null || !this.getCpuUser().equals(avg)) {
                anyUpdate = true;
                this.setCpuUser(avg);
            }
        }
        if (metAtt.get(Const.METRIC_CPU_WAIT).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_CPU_WAIT).getAverage();
            if (this.getCpuWait() == null || !this.getCpuWait().equals(avg)) {
                anyUpdate = true;
                this.setCpuWait(avg);
            }
        }
        if (metAtt.get(Const.METRIC_DISK_INODE_USED).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_DISK_INODE_USED).getAverage();
            if (this.getDiskInodeUsed() == null || !this.getDiskInodeUsed().equals(avg)) {
                anyUpdate = true;
                this.setDiskInodeUsed(avg);
            }
        }
        if (metAtt.get(Const.METRIC_DISK_SPACE_USED).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_DISK_SPACE_USED).getAverage();
            if (this.getDiskSpaceUsed() == null || !this.getDiskSpaceUsed().equals(avg)) {
                anyUpdate = true;
                this.setDiskSpaceUsed(avg);
            }
        }
        if (metAtt.get(Const.METRIC_NET_IN_BYTES_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_IN_BYTES_SEC).getTotal();
            if (this.getNetInBytesSec() == null || !this.getNetInBytesSec().equals(total)) {
                anyUpdate = true;
                this.setNetInBytesSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_IN_ERRORS_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_IN_ERRORS_SEC).getTotal();
            if (this.getNetInErrorsSec() == null || !this.getNetInErrorsSec().equals(total)) {
                anyUpdate = true;
                this.setNetInErrorsSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_IN_PACKETS_DROPPED_SEC).getTotal();
            if (this.getNetInPacketsDroppedSec() == null || !this.getNetInPacketsDroppedSec().equals(total)) {
                anyUpdate = true;
                this.setNetInPacketsDroppedSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_IN_PACKETS_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_IN_PACKETS_SEC).getTotal();
            if (this.getNetInPacketsSec() == null || !this.getNetInPacketsSec().equals(total)) {
                anyUpdate = true;
                this.setNetInPacketsSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_OUT_BYTES_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_OUT_BYTES_SEC).getTotal();
            if (this.getNetOutBytesSec() == null || !this.getNetOutBytesSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutBytesSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_OUT_ERRORS_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_OUT_ERRORS_SEC).getTotal();
            if (this.getNetOutErrorsSec() == null || !this.getNetOutErrorsSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutErrorsSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_OUT_PACKETS_DROPPED_SEC).getTotal();
            if (this.getNetOutPacketsDroppedSec() == null || !this.getNetOutPacketsDroppedSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutPacketsDroppedSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_NET_OUT_PACKETS_SEC).getCount() > 0) {
            double total = metAtt.get(Const.METRIC_NET_OUT_PACKETS_SEC).getTotal();
            if (this.getNetOutPacketsSec() == null || !this.getNetOutPacketsSec().equals(total)) {
                anyUpdate = true;
                this.setNetOutPacketsSec(total);
            }
        }
        if (metAtt.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION).getCount() > 0) {
            double avg = metAtt.get(Const.METRIC_BRIDGE_FABRIC_LINK_UTILISATION).getAverage();
            if (this.getFabricUtilisation() == null || !this.getFabricUtilisation().equals(avg)) {
                anyUpdate = true;
                this.setFabricUtilisation(avg);
            }
        }
        if (metAtt.get(Const.ATT_CORES).getCount() > 0) {
            double total = metAtt.get(Const.ATT_CORES).getTotal();
            if (this.getCore().getCores() == null || !this.getCore().getCores().equals(total)) {
                anyUpdate = true;
                this.getCore().setCores(total);
            }
        }
        if (metAtt.get(Const.ATT_FABRIC_BANDWIDTH).getCount() > 0) {
            double total = metAtt.get(Const.ATT_FABRIC_BANDWIDTH).getTotal();
            if (this.getCore().getFabricBandwidth() == null || !this.getCore().getFabricBandwidth().equals(total)) {
                anyUpdate = true;
                this.getCore().setFabricBandwidth(total);
            }
        }
        return anyUpdate;
    }

    /**
     * Collect the metrics from Nodes and FAM modules that this Enclosure contains, aggregate them
     * according to the type of metric, and set the values for visualization.
     */
    private boolean updateNonCoreAttributes() {
        HashMap<String, NonCoreMetricAccumulation> metAtt = accumulateSocMetricsAndAttributes();
        return updateAccumulatedMetricsAndAttributes(metAtt);
    }

    @Override
    public boolean update() {
        return super.update() | updateNonCoreAttributes();
    }
}
