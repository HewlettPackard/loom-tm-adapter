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
package com.hp.hpl.loom.adapter.tm;

public final class Const {

    private Const() {}

    // Metric names
    public static final String METRIC_DRAM_UTILISATION = "DRAM_utilisation";
    public static final String METRIC_BRIDGE_FABRIC_LINK_UTILISATION = "ZBridgeLinkBandwidth";
    public static final String METRIC_ZSWITCH_FABRIC_LINK_UTILISATION = "ZSwitchLinkBandwidth";
    public static final String METRIC_BRIDGE_FABRIC_LINK_UTILISATION_FAM = "FamBridgeUtilisation";
    public static final String METRIC_BRIDGE_FABRIC_LINK_REQUEST_QUEUE_UTILISATION =
            "Bridge_fabric_link_request_queue_utilisation";
    public static final String METRIC_BRIDGE_ICI_TRANSACTION_TYPE_COUNTS = "bridge_ici_transaction_type_counts";
    public static final String METRIC_BRIDGE_HOME_AGENT_AVERAGE_READ_LATENCY = "bridge_home_agent_average_read_latency";
    public static final String METRIC_BRIDGE_HOME_AGENT_REQUEST_QUEUE_UTILIZATION =
            "bridge_home_agent_request_queue_utilization";
    public static final String METRIC_FABRIC_SWITCH_CORE_ARB_BLOCKED = "fabric_switch_core_arb_blocked";
    public static final String METRIC_CPU_IDLE_PERC = "cpu.idle_perc";
    public static final String METRIC_CPU_STOLEN = "cpu.stolen_perc";
    public static final String METRIC_CPU_SYSTEM = "cpu.system_perc";
    public static final String METRIC_CPU_USER = "cpu.user_perc";
    public static final String METRIC_CPU_WAIT = "cpu.wait_perc";
    public static final String METRIC_DISK_INODE_USED = "disk.inode_used_perc";
    public static final String METRIC_DISK_SPACE_USED = "disk.space_used_perc";
    public static final String DEFAULT_NETWORK_INTERFACE = "eth0";
    public static final String METRIC_NET_IN_BYTES_SEC = "net.in_bytes_sec";
    public static final String METRIC_NET_IN_ERRORS_SEC = "net.in_errors_sec";
    public static final String METRIC_NET_IN_PACKETS_DROPPED_SEC = "net.in_packets_dropped_sec";
    public static final String METRIC_NET_IN_PACKETS_SEC = "net.in_packets_sec";
    public static final String METRIC_NET_OUT_BYTES_SEC = "net.out_bytes_sec";
    public static final String METRIC_NET_OUT_ERRORS_SEC = "net.out_errors_sec";
    public static final String METRIC_NET_OUT_PACKETS_DROPPED_SEC = "net.out_packets_dropped_sec";
    public static final String METRIC_NET_OUT_PACKETS_SEC = "net.out_packets_sec";
    public static final String METRIC_MEMORY_FREE_MB = "mem.free_mb";
    public static final String METRIC_MEMORY_TOTAL_MB = "mem.total_mb";
    public static final String METRIC_FABRIC_LINK_UTILISATION = "fabric_link_used_perc";
    public static final String METRIC_FAM_UTILISATION = "fam_used_perc";
    public static final String METRIC_NUM_DISCREPANCIES = "num_discrepancies";

    // Event names
    public static final String EVENT_OS_BOOTED = "OS_booted";
    public static final String BOOT_TIME = "bootTime";
    public static final String UP_TIME = "upTime";

    // Attribute names
    public static final String ATT_CORES = "cores";
    public static final String ATT_FABRIC_BANDWIDTH = "fabricBandwidth";

    // Action names
    public static final String ACTION_ALL_POWERED_ON = "allPoweredOn";
    public static final String ACTION_ENABLE_SOC_POWER_ON = "enableSocPowerOn";
    public static final String ACTION_ALL_POWERED_OFF = "allPoweredOff";
    public static final String ACTION_ALL_POWERED_OFF_FORCE = "allPoweredOffForce";
    public static final String ACTION_ONLY_FAM_FABRIC_POWERED_ON = "onlyFamFabricPoweredOn";
    public static final String ACTION_ONLY_FAM_POWERED_ON = "onlyFamPoweredOn";
    public static final String ACTION_PREPARE_FOR_SYSTEM_BOOT = "prepareForSystemBoot";
    public static final String ACTION_SET_AA_MODE = "setAaMode";
    public static final String ACTION_SET_MEMORY_TEST = "setMemtest";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_UNPAUSE = "unpause";
    public static final String ACTION_SET_DEBUG = "setDebugMode";
    public static final String ACTION_UNSET_DEBUG = "unsetDebugMode";
    public static final String ACTION_POWER_ON = "powerOn";
    public static final String ACTION_POWER_OFF = "powerOff";
    public static final String ACTION_POWER_OFF_FORCE = "powerOffForce";
    public static final String ACTION_WARM_RESET = "actionWarmReset";
    public static final String ACTION_SET_OS_MANIFEST_BINDING = "setOsManifestBinding";
    public static final String ACTION_CLEAR_OS_MANIFEST_BINDING = "clearOsManifestBinding";
    public static final String ACTION_SYNC_OS_MANIFEST = "syncOsManifest";
    public static final String ACTION_ASSIGN_TENANT = "assignTenant";
    public static final String ACTION_DELETE_OS_MANIFEST = "Delete OS Manifest";
    public static final String ACTION_DOWNLOAD_OS_MANIFEST = "Download OS Manifest";
    public static final String ACTION_UPLOAD_OS_MANIFEST = "Upload OS Manifest";
    public static final String ACTION_BIND_OS_MANIFEST_TO_ALL_SOCS = "Bind OS Manifest to all SoCs";
    public static final String ACTION_BIND_OS_MANIFEST_TO_INSTANCE = "Bind OS Manifest to all SoCs in instance";
    public static final String ACTION_SYNC_OS_MANIFEST_TO_INSTANCE = "Sync OS Manifest to all SoCs in instance";
    public static final String ACTION_ENABLE_MEMTEST = "Enable memtest";
    public static final String ACTION_DISABLE_MEMTEST = "Disable memtest";

    // Assembly Agent modes
    public static final String MODE_NORMAL = "NORMAL";
    public static final String MODE_PAUSED = "PAUSED";
    public static final String MODE_DEBUG = "DEBUG";
    public static final String MODE_DEBUG_PAUSED = "DEBUG & PAUSED";
    public static final String AA_ERROR_STATUS = "ERROR(S) DETECTED";
    public static final String AA_WARNING_STATUS = "WARNING(S) DETECTED";
    public static final String AA_OK_STATUS = "OK";
}
