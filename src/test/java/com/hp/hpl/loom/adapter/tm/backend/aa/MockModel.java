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
package com.hp.hpl.loom.adapter.tm.backend.aa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.powermock.api.mockito.PowerMockito;

import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMEnclosure;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.adapter.tm.items.TMNode;
import com.hp.hpl.loom.adapter.tm.items.TMRack;
import com.hp.hpl.loom.adapter.tm.items.TMSoc;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.relationships.RelationshipUtil;

public class MockModel {

    private Collection<Item> allInstances;
    private Collection<Item> allRacks;
    private Collection<Item> allSocs;

    @SuppressWarnings("unchecked")
    public List<TMInstance> getAllInstances() {
        return (List<TMInstance>) (List<?>) allInstances;
    }

    @SuppressWarnings("unchecked")
    public List<TMRack> getAllRacks() {
        return (List<TMRack>) (List<?>) allRacks;
    }

    @SuppressWarnings("unchecked")
    public List<TMSoc> getAllSocs() {
        return (List<TMSoc>) (List<?>) allSocs;
    }

    public List<TMSoc> getSocsByCoordinates(List<String> coords) {
        List<TMSoc> socsSelection = new ArrayList<TMSoc>();
        for (Item socItem : allSocs) {
            TMSoc soc = (TMSoc) socItem;
            if (coords.contains(soc.getCore().getCoordinate())) {
                socsSelection.add(soc);
            }
        }
        return socsSelection;
    }

    protected static final String rackCoordinate = "/rack/1";
    protected static final String soc11Coordinate = "/rack/1/enclosure/1/node/1/soc_board";
    protected static final String soc12Coordinate = "/rack/1/enclosure/1/node/2/soc_board";
    protected static final String soc21Coordinate = "/rack/1/enclosure/2/node/1/soc_board";
    protected static final String soc22Coordinate = "/rack/1/enclosure/2/node/2/soc_board";

    public static final String instanceCoordinate = "/instance/1";
    protected static final String instancePath = "/main";
    protected static final String rackPath = "/main/rack1";
    protected static final String soc11Path = "/main/rack1/enclosure1.node1.soc_board";
    protected static final String soc12Path = "/main/rack1/enclosure1.node2.soc_board";
    protected static final String soc21Path = "/main/rack1/enclosure2.node1.soc_board";
    protected static final String soc22Path = "/main/rack1/enclosure2.node2.soc_board";

    private String aaMode = "NORMAL";

    private Long instanceLastMetricUpdateTimestamp = null;
    private Number instanceCpuUtilisation = null;
    private Number instanceCpuStolen = null;
    private Number instanceCpuSystem = null;
    private Number instanceCpuUser = null;
    private Number instanceCpuWait = null;
    private Number instanceDiskInodeUsed = null;
    private Number instanceDiskSpaceUsed = null;
    private Number instanceNetInBytesSec = null;
    private Number instanceNetInErrorsSec = null;
    private Number instanceNetInPacketsDroppedSec = null;
    private Number instanceNetInPacketsSec = null;
    private Number instanceNetOutBytesSec = null;
    private Number instanceNetOutErrorsSec = null;
    private Number instanceNetOutPacketsDroppedSec = null;
    private Number instanceNetOutPacketsSec = null;
    private int instanceNumDiscrepancies = -1;
    private Double instanceFamUtil = null;
    private Number instanceFabricUtil = null;

    private String defaultNextOsManifest = null;
    private String rackPower = null;
    private Boolean rackDesiredForceAllFamFabricSocOff;
    private Boolean rackDesiredForceAllFabricSocOff;
    private Boolean rackDesiredForceAllSocOff;

    private String nextOsImageManifest11 = null;
    private String nextOsImageManifest12 = null;
    private String nextOsImageManifest21 = null;
    private String nextOsImageManifest22 = null;

    private String runningOsImageManifest11 = null;
    private String runningOsImageManifest12 = null;
    private String runningOsImageManifest21 = null;
    private String runningOsImageManifest22 = null;

    private String local_powerSoc11 = null;
    private String local_powerSoc12 = null;
    private String local_powerSoc21 = null;
    private String local_powerSoc22 = null;

    private Boolean enableNonGracefulShutdown11 = null;
    private Boolean enableNonGracefulShutdown12 = null;
    private Boolean enableNonGracefulShutdown21 = null;
    private Boolean enableNonGracefulShutdown22 = null;

    private String local_powerNode11 = null;
    private String local_powerNode12 = null;
    private String local_powerNode21 = null;
    private String local_powerNode22 = null;

    private String local_powerEnclosure1 = null;
    private String local_powerEnclosure2 = null;

    public String getAaMode() {
        return aaMode;
    }

    public void setAaMode(String aaMode) {
        this.aaMode = aaMode;
    }

    public Long getInstanceLastMetricUpdateTimestamp() {
        return instanceLastMetricUpdateTimestamp;
    }

    public void setInstanceLastMetricUpdateTimestamp(Long instanceLastMetricUpdateTimestamp) {
        this.instanceLastMetricUpdateTimestamp = instanceLastMetricUpdateTimestamp;
    }

    public Number getInstanceCpuUtilisation() {
        return instanceCpuUtilisation;
    }

    public void setInstanceCpuUtilisation(Number instanceCpuUtilisation) {
        this.instanceCpuUtilisation = instanceCpuUtilisation;
    }

    public Number getInstanceCpuStolen() {
        return instanceCpuStolen;
    }

    public void setInstanceCpuStolen(Number instanceCpuStolen) {
        this.instanceCpuStolen = instanceCpuStolen;
    }

    public Number getInstanceCpuSystem() {
        return instanceCpuSystem;
    }

    public void setInstanceCpuSystem(Number instanceCpuSystem) {
        this.instanceCpuSystem = instanceCpuSystem;
    }

    public Number getInstanceCpuUser() {
        return instanceCpuUser;
    }

    public void setInstanceCpuUser(Number instanceCpuUser) {
        this.instanceCpuUser = instanceCpuUser;
    }

    public Number getInstanceCpuWait() {
        return instanceCpuWait;
    }

    public void setInstanceCpuWait(Number instanceCpuWait) {
        this.instanceCpuWait = instanceCpuWait;
    }

    public Number getInstanceDiskInodeUsed() {
        return instanceDiskInodeUsed;
    }

    public void setInstanceDiskInodeUsed(Number instanceDiskInodeUsed) {
        this.instanceDiskInodeUsed = instanceDiskInodeUsed;
    }

    public Number getInstanceDiskSpaceUsed() {
        return instanceDiskSpaceUsed;
    }

    public void setInstanceDiskSpaceUsed(Number instanceDiskSpaceUsed) {
        this.instanceDiskSpaceUsed = instanceDiskSpaceUsed;
    }

    public Number getInstanceNetInBytesSec() {
        return instanceNetInBytesSec;
    }

    public void setInstanceNetInBytesSec(Number instanceNetInBytesSec) {
        this.instanceNetInBytesSec = instanceNetInBytesSec;
    }

    public Number getInstanceNetInErrorsSec() {
        return instanceNetInErrorsSec;
    }

    public void setInstanceNetInErrorsSec(Number instanceNetInErrorsSec) {
        this.instanceNetInErrorsSec = instanceNetInErrorsSec;
    }

    public Number getInstanceNetInPacketsDroppedSec() {
        return instanceNetInPacketsDroppedSec;
    }

    public void setInstanceNetInPacketsDroppedSec(Number instanceNetInPacketsDroppedSec) {
        this.instanceNetInPacketsDroppedSec = instanceNetInPacketsDroppedSec;
    }

    public Number getInstanceNetInPacketsSec() {
        return instanceNetInPacketsSec;
    }

    public void setInstanceNetInPacketsSec(Number instanceNetInPacketsSec) {
        this.instanceNetInPacketsSec = instanceNetInPacketsSec;
    }

    public Number getInstanceNetOutBytesSec() {
        return instanceNetOutBytesSec;
    }

    public void setInstanceNetOutBytesSec(Number instanceNetOutBytesSec) {
        this.instanceNetOutBytesSec = instanceNetOutBytesSec;
    }

    public Number getInstanceNetOutErrorsSec() {
        return instanceNetOutErrorsSec;
    }

    public void setInstanceNetOutErrorsSec(Number instanceNetOutErrorsSec) {
        this.instanceNetOutErrorsSec = instanceNetOutErrorsSec;
    }

    public Number getInstanceNetOutPacketsDroppedSec() {
        return instanceNetOutPacketsDroppedSec;
    }

    public void setInstanceNetOutPacketsDroppedSec(Number instanceNetOutPacketsDroppedSec) {
        this.instanceNetOutPacketsDroppedSec = instanceNetOutPacketsDroppedSec;
    }

    public Number getInstanceNetOutPacketsSec() {
        return instanceNetOutPacketsSec;
    }

    public void setInstanceNetOutPacketsSec(Number instanceNetOutPacketsSec) {
        this.instanceNetOutPacketsSec = instanceNetOutPacketsSec;
    }

    public int getInstanceNumDiscrepancies() {
        return instanceNumDiscrepancies;
    }

    public void setInstanceNumDiscrepancies(int instanceNumDiscrepancies) {
        this.instanceNumDiscrepancies = instanceNumDiscrepancies;
    }

    public Double getInstanceFamUtil() {
        return instanceFamUtil;
    }

    public void setInstanceFamUtil(Double instanceFamUtil) {
        this.instanceFamUtil = instanceFamUtil;
    }

    public Number getInstanceFabricUtil() {
        return instanceFabricUtil;
    }

    public void setInstanceFabricUtil(Number instanceFabricUtil) {
        this.instanceFabricUtil = instanceFabricUtil;
    }

    public void setDefaultNextOsManifest(String defaultNextOsManifest) {
        this.defaultNextOsManifest = defaultNextOsManifest;
    }

    public void setRackPower(String rackPower) {
        this.rackPower = rackPower;
    }

    public void setRackDesiredForceAllFamFabricSocOff(Boolean forceAllFamFabricSocOff) {
        this.rackDesiredForceAllFamFabricSocOff = forceAllFamFabricSocOff;
    }

    public void setRackDesiredForceAllFabricSocOff(Boolean forceAllFabricSocOff) {
        this.rackDesiredForceAllFabricSocOff = forceAllFabricSocOff;
    }

    public void setRackDesiredForceAllSocOff(Boolean forceAllSocOff) {
        this.rackDesiredForceAllSocOff = forceAllSocOff;
    }

    public void setNextOsImageManifest11(String nextOsImageManifest11) {
        this.nextOsImageManifest11 = nextOsImageManifest11;
    }

    public void setNextOsImageManifest12(String nextOsImageManifest12) {
        this.nextOsImageManifest12 = nextOsImageManifest12;
    }

    public void setNextOsImageManifest21(String nextOsImageManifest21) {
        this.nextOsImageManifest21 = nextOsImageManifest21;
    }

    public void setNextOsImageManifest22(String nextOsImageManifest22) {
        this.nextOsImageManifest22 = nextOsImageManifest22;
    }

    public void setRunningOsImageManifest11(String runningOsImageManifest11) {
        this.runningOsImageManifest11 = runningOsImageManifest11;
    }

    public void setRunningOsImageManifest12(String runningOsImageManifest12) {
        this.runningOsImageManifest12 = runningOsImageManifest12;
    }

    public void setRunningOsImageManifest21(String runningOsImageManifest21) {
        this.runningOsImageManifest21 = runningOsImageManifest21;
    }

    public void setRunningOsImageManifest22(String runningOsImageManifest22) {
        this.runningOsImageManifest22 = runningOsImageManifest22;
    }

    public void setLocalPowerSoc11(String localPower11) {
        this.local_powerSoc11 = localPower11;
    }

    public void setLocalPowerSoc12(String localPower12) {
        this.local_powerSoc12 = localPower12;
    }

    public void setLocalPowerSoc21(String localPower21) {
        this.local_powerSoc21 = localPower21;
    }

    public void setLocalPowerSoc22(String localPower22) {
        this.local_powerSoc22 = localPower22;
    }

    public void setEnableNonGracefulShutdown11(Boolean enableNonGracefulShutdown11) {
        this.enableNonGracefulShutdown11 = enableNonGracefulShutdown11;
    }

    public void setEnableNonGracefulShutdown12(Boolean enableNonGracefulShutdown12) {
        this.enableNonGracefulShutdown12 = enableNonGracefulShutdown12;
    }

    public void setEnableNonGracefulShutdown21(Boolean enableNonGracefulShutdown21) {
        this.enableNonGracefulShutdown21 = enableNonGracefulShutdown21;
    }

    public void setEnableNonGracefulShutdown22(Boolean enableNonGracefulShutdown22) {
        this.enableNonGracefulShutdown22 = enableNonGracefulShutdown22;
    }

    public void setLocalPowerNode11(String localPower11) {
        this.local_powerNode11 = localPower11;
    }

    public void setLocalPowerNode12(String localPower12) {
        this.local_powerNode12 = localPower12;
    }

    public void setLocalPowerNode21(String localPower21) {
        this.local_powerNode21 = localPower21;
    }

    public void setLocalPowerNode22(String localPower22) {
        this.local_powerNode22 = localPower22;
    }

    public void setLocalPowerEnclosure1(String localPower1) {
        this.local_powerEnclosure1 = localPower1;
    }

    public void setLocalPowerEnclosure2(String localPower2) {
        this.local_powerEnclosure2 = localPower2;
    }

    private TMSoc.ItemAttributes populateSocCoreAttributes(TMSoc.ItemAttributes socCore, String path, String coordinate,
            String nextOsImageManifest, String runningOsImageManifest, String local_power,
            Boolean enableNonGracefulShutdown) {
        if (null != path) {
            PowerMockito.doReturn(path).when(socCore).getPath();
        }
        if (null != coordinate) {
            PowerMockito.doReturn(coordinate).when(socCore).getCoordinate();
        }
        if (null != nextOsImageManifest) {
            PowerMockito.doReturn(nextOsImageManifest).when(socCore).getDesiredNextOsImageManifest();
        }
        if (null != runningOsImageManifest) {
            PowerMockito.doReturn(runningOsImageManifest).when(socCore).getDesiredRunningOsImageManifest();
        }
        if (null != local_power) {
            PowerMockito.doReturn(local_power).when(socCore).getDesiredLocalPower();
        }
        if (null != enableNonGracefulShutdown) {
            PowerMockito.doReturn(enableNonGracefulShutdown).when(socCore).getDesiredEnableNonGracefulShutdown();
        }
        return socCore;
    }

    private TMSoc createSoc(int enclosureId, int nodeId, TMNode parentNode) {
        TMSoc soc = PowerMockito.mock(TMSoc.class);
        TMSoc.ItemAttributes socCore = PowerMockito.mock(TMSoc.ItemAttributes.class);
        if (enclosureId == 1 && nodeId == 1) {
            socCore = populateSocCoreAttributes(socCore, soc11Path, soc11Coordinate, nextOsImageManifest11,
                    runningOsImageManifest11, local_powerSoc11, enableNonGracefulShutdown11);
        } else if (enclosureId == 1 && nodeId == 2) {
            socCore = populateSocCoreAttributes(socCore, soc12Path, soc12Coordinate, nextOsImageManifest12,
                    runningOsImageManifest12, local_powerSoc12, enableNonGracefulShutdown12);
        } else if (enclosureId == 2 && nodeId == 1) {
            socCore = populateSocCoreAttributes(socCore, soc21Path, soc21Coordinate, nextOsImageManifest21,
                    runningOsImageManifest21, local_powerSoc21, enableNonGracefulShutdown21);
        } else if (enclosureId == 2 && nodeId == 2) {
            socCore = populateSocCoreAttributes(socCore, soc22Path, soc22Coordinate, nextOsImageManifest22,
                    runningOsImageManifest22, local_powerSoc22, enableNonGracefulShutdown22);
        }
        PowerMockito.doReturn(socCore).when(soc).getCore();
        final String relationshipName =
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(parentNode.getProviderType(),
                        Types.NODE_TYPE_LOCAL_ID, Types.SOC_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(parentNode).when(soc).getFirstConnectedItemWithRelationshipName(relationshipName);
        return soc;
    }

    private Collection<Item> createMockSocs(int enclosureId, int nodeId, TMNode parentNode) {
        TMSoc soc1 = createSoc(enclosureId, nodeId, parentNode);
        Collection<Item> socs = new ArrayList<Item>();
        socs.add(soc1);
        allSocs.add(soc1);
        return socs;
    }

    private TMNode.ItemAttributes populateNodeCoreAttributes(TMNode.ItemAttributes nodeCore, String localPower) {
        if (null != localPower) {
            PowerMockito.doReturn(localPower).when(nodeCore).getDesiredLocalPower();
        }
        return nodeCore;
    }

    private TMNode createNode(int enclosureId, int nodeId, TMEnclosure parentEnclosure) {
        TMNode node = PowerMockito.mock(TMNode.class);
        Collection<Item> socs = createMockSocs(enclosureId, nodeId, node);
        TMNode.ItemAttributes nodeCore = PowerMockito.mock(TMNode.ItemAttributes.class);
        if (enclosureId == 1 && nodeId == 1) {
            nodeCore = populateNodeCoreAttributes(nodeCore, local_powerNode11);
        } else if (enclosureId == 1 && nodeId == 2) {
            nodeCore = populateNodeCoreAttributes(nodeCore, local_powerNode12);
        } else if (enclosureId == 2 && nodeId == 1) {
            nodeCore = populateNodeCoreAttributes(nodeCore, local_powerNode21);
        } else if (enclosureId == 2 && nodeId == 2) {
            nodeCore = populateNodeCoreAttributes(nodeCore, local_powerNode22);
        }
        PowerMockito.doReturn(nodeCore).when(node).getCore();
        final String relationshipName = RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(
                node.getProviderType(), Types.SOC_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(socs).when(node).getConnectedItemsWithRelationshipName(relationshipName);
        final String relationshipNameUp =
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(parentEnclosure.getProviderType(),
                        Types.ENCLOSURE_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(parentEnclosure).when(node).getFirstConnectedItemWithRelationshipName(relationshipNameUp);
        return node;
    }

    private Collection<Item> createMockNodes(int enclosureId, TMEnclosure parentEnclosure) {
        TMNode node1 = createNode(enclosureId, 1, parentEnclosure);
        TMNode node2 = createNode(enclosureId, 2, parentEnclosure);
        Collection<Item> nodes = new ArrayList<Item>();
        nodes.add(node1);
        nodes.add(node2);
        return nodes;
    }

    private TMEnclosure.ItemAttributes populateEnclosureCoreAttributes(TMEnclosure.ItemAttributes EnclosureCore,
            String localPower) {
        if (null != localPower) {
            PowerMockito.doReturn(localPower).when(EnclosureCore).getDesiredLocalPower();
        }
        return EnclosureCore;
    }

    private TMEnclosure createMockEnclosure(int enclosureId, TMRack parentRack) {
        TMEnclosure enclosure = PowerMockito.mock(TMEnclosure.class);
        Collection<Item> nodes = createMockNodes(enclosureId, enclosure);
        TMEnclosure.ItemAttributes enclosureCore = PowerMockito.mock(TMEnclosure.ItemAttributes.class);
        if (enclosureId == 1) {
            enclosureCore = populateEnclosureCoreAttributes(enclosureCore, local_powerEnclosure1);
        } else if (enclosureId == 2) {
            enclosureCore = populateEnclosureCoreAttributes(enclosureCore, local_powerEnclosure2);
        }
        PowerMockito.doReturn(enclosureCore).when(enclosure).getCore();
        final String relationshipName =
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(enclosure.getProviderType(),
                        Types.NODE_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(nodes).when(enclosure).getConnectedItemsWithRelationshipName(relationshipName);
        final String relationshipNameUp =
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(parentRack.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(parentRack).when(enclosure).getFirstConnectedItemWithRelationshipName(relationshipNameUp);
        return enclosure;
    }

    private Collection<Item> createMockEnclosures(TMRack parentRack) {
        TMEnclosure enclosure1 = createMockEnclosure(1, parentRack);
        TMEnclosure enclosure2 = createMockEnclosure(2, parentRack);
        Collection<Item> enclosures = new ArrayList<Item>();
        enclosures.add(enclosure1);
        enclosures.add(enclosure2);
        return enclosures;
    }

    private TMRack.ItemAttributes populateRackCoreAttributes(TMRack.ItemAttributes rackCore, String path,
            String coordinate, String rackDefaultNextOsManifest, String power, Boolean desiredForceAllFamFabricSocOff,
            Boolean desiredForceAllFabricSocOff, Boolean desiredForceAllSocOff) {
        if (null != path) {
            PowerMockito.doReturn(path).when(rackCore).getPath();
        }
        if (null != coordinate) {
            PowerMockito.doReturn(coordinate).when(rackCore).getCoordinate();
        }
        if (null != rackDefaultNextOsManifest) {
            PowerMockito.doReturn(rackDefaultNextOsManifest).when(rackCore).getDesiredDefaultNextOsImageManifest();
        }
        if (null != power) {
            PowerMockito.doReturn(power).when(rackCore).getDesiredPower();
        }
        if (null != desiredForceAllFamFabricSocOff) {
            PowerMockito.doReturn(desiredForceAllFamFabricSocOff).when(rackCore).getDesiredForceAllFamFabricSocOff();
        }
        if (null != desiredForceAllFabricSocOff) {
            PowerMockito.doReturn(desiredForceAllFabricSocOff).when(rackCore).getDesiredForceAllFabricSocOff();
        }
        if (null != desiredForceAllSocOff) {
            PowerMockito.doReturn(desiredForceAllSocOff).when(rackCore).getDesiredForceAllSocOff();
        }
        return rackCore;
    }

    private Collection<Item> createMockRack() {
        TMRack rack = PowerMockito.mock(TMRack.class);
        TMRack.ItemAttributes rackCore = PowerMockito.mock(TMRack.ItemAttributes.class);
        rackCore = populateRackCoreAttributes(rackCore, rackPath, rackCoordinate, defaultNextOsManifest, rackPower,
                rackDesiredForceAllFamFabricSocOff, rackDesiredForceAllFabricSocOff, rackDesiredForceAllSocOff);
        PowerMockito.doReturn(rackCore).when(rack).getCore();
        Collection<Item> racks = new ArrayList<Item>();
        Collection<Item> enclosures = createMockEnclosures(rack);
        final String relationshipName =
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rack.getProviderType(),
                        Types.ENCLOSURE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(enclosures).when(rack).getConnectedItemsWithRelationshipName(relationshipName);
        racks.add(rack);
        return racks;
    }

    private TMInstance.ItemAttributes populateInstanceCoreAttributes(TMInstance.ItemAttributes instanceCore,
            String path, String coordinate, String aaMode, int numDiscrepancies, Double famUtil) {
        if (null != path) {
            PowerMockito.doReturn(path).when(instanceCore).getPath();
        }
        if (null != coordinate) {
            PowerMockito.doReturn(coordinate).when(instanceCore).getCoordinate();
        }
        if (null != aaMode) {
            PowerMockito.doReturn(aaMode).when(instanceCore).getAaMode();
        }
        PowerMockito.doReturn(numDiscrepancies).when(instanceCore).getDmaTotalDiscrepancyCount();
        PowerMockito.doReturn(famUtil).when(instanceCore).getFamUtilisation();
        return instanceCore;
    }

    private void populateInstanceMetrics(TMInstance instance) {
        if (instanceLastMetricUpdateTimestamp != null) {
            PowerMockito.doReturn(instanceLastMetricUpdateTimestamp).when(instance).getLatestMetricUpdateTimestamp();
        }
        if (instanceCpuUtilisation != null) {
            PowerMockito.doReturn(instanceCpuUtilisation).when(instance).getCpuUtilisation();
        }
        if (instanceCpuStolen != null) {
            PowerMockito.doReturn(instanceCpuStolen).when(instance).getCpuStolen();
        }
        if (instanceCpuSystem != null) {
            PowerMockito.doReturn(instanceCpuSystem).when(instance).getCpuSystem();
        }
        if (instanceCpuUser != null) {
            PowerMockito.doReturn(instanceCpuUser).when(instance).getCpuUser();
        }
        if (instanceCpuWait != null) {
            PowerMockito.doReturn(instanceCpuWait).when(instance).getCpuWait();
        }
        if (instanceDiskInodeUsed != null) {
            PowerMockito.doReturn(instanceDiskInodeUsed).when(instance).getDiskInodeUsed();
        }
        if (instanceDiskSpaceUsed != null) {
            PowerMockito.doReturn(instanceDiskSpaceUsed).when(instance).getDiskSpaceUsed();
        }
        if (instanceNetInBytesSec != null) {
            PowerMockito.doReturn(instanceNetInBytesSec).when(instance).getNetInBytesSec();
        }
        if (instanceNetInErrorsSec != null) {
            PowerMockito.doReturn(instanceNetInErrorsSec).when(instance).getNetInErrorsSec();
        }
        if (instanceNetInPacketsDroppedSec != null) {
            PowerMockito.doReturn(instanceNetInPacketsDroppedSec).when(instance).getNetInPacketsDroppedSec();
        }
        if (instanceNetInPacketsSec != null) {
            PowerMockito.doReturn(instanceNetInPacketsSec).when(instance).getNetInPacketsSec();
        }
        if (instanceNetOutBytesSec != null) {
            PowerMockito.doReturn(instanceNetOutBytesSec).when(instance).getNetOutBytesSec();
        }
        if (instanceNetOutErrorsSec != null) {
            PowerMockito.doReturn(instanceNetOutErrorsSec).when(instance).getNetOutErrorsSec();
        }
        if (instanceNetOutPacketsDroppedSec != null) {
            PowerMockito.doReturn(instanceNetOutPacketsDroppedSec).when(instance).getNetOutPacketsDroppedSec();
        }
        if (instanceNetOutPacketsSec != null) {
            PowerMockito.doReturn(instanceNetOutPacketsSec).when(instance).getNetOutPacketsSec();
        }
        if (instanceFabricUtil != null) {
            PowerMockito.doReturn(instanceFabricUtil).when(instance).getFabricUtilisation();
        }
    }

    private Collection<Item> createMockInstance() {
        TMInstance instance = PowerMockito.mock(TMInstance.class);
        TMInstance.ItemAttributes instanceCore = PowerMockito.mock(TMInstance.ItemAttributes.class);
        instanceCore = populateInstanceCoreAttributes(instanceCore, instancePath, instanceCoordinate, aaMode,
                instanceNumDiscrepancies, instanceFamUtil);
        PowerMockito.doReturn(instanceCore).when(instance).getCore();
        populateInstanceMetrics(instance);
        Collection<Item> instances = new ArrayList<Item>();
        allRacks = createMockRack();
        final String relationshipName =
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS);
        PowerMockito.doReturn(allRacks).when(instance).getConnectedItemsWithRelationshipName(relationshipName);
        instances.add(instance);
        return instances;
    }

    public List<TMInstance> createModel() {
        allSocs = new ArrayList<Item>();
        allRacks = new ArrayList<Item>();
        allInstances = createMockInstance();
        return getAllInstances();
    }

}
