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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.ITMItemCollectorListener;
import com.hp.hpl.loom.adapter.tm.TMItemCollector;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;
import com.hp.hpl.loom.adapter.tm.backend.RestClient.RestTemplateWithJsonAndString;
import com.hp.hpl.loom.adapter.tm.backend.aa.prototypes.PowerPrototype;
import com.hp.hpl.loom.adapter.tm.backend.aa.status.Component;
import com.hp.hpl.loom.adapter.tm.items.RelationshipNames;
import com.hp.hpl.loom.adapter.tm.items.TMEnclosure;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.adapter.tm.items.TMNode;
import com.hp.hpl.loom.adapter.tm.items.TMRack;
import com.hp.hpl.loom.adapter.tm.items.TMSoc;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.relationships.RelationshipUtil;

/**
 * Class to interact with the DMA API to make changes to the desired state. It contains a REST
 * client to make the calls, and the logic to map the high level "actions" to changes in the desired
 * state in DMA.
 */
public class DmaAPI {
    private static final Log LOG = LogFactory.getLog(DmaAPI.class);

    protected RestTemplateWithJsonAndString restClient;
    private String dmaURL;

    public static final String TBD = "$TBD";
    public static final String POWER_ON = "$(power:on)";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String POWER_OFF = "$(power:off)";
    public static final String MEMTEST_YES = "{\"MemTest\":\"Yes\"}";
    public static final String MEMTEST_NO = "{\"MemTest\":\"No\"}";
    public static final String LOCAL_POWER = "localPower";
    public static final String SOC_SFX = ".soc";
    public static final String RUNNING_OS_IMAGE_MANIFEST = "runningOsImageManifest";
    public static final String NEXT_OS_IMAGE_MANIFEST = "nextOsImage.manifest";
    public static final String ENABLE_NON_GRACEFUL_SHUTDOWN = "enableNonGracefulShutdown";
    public static final String DEFAULT_NEXT_OS_IMAGE_MANIFEST = "defaultNextOsImageManifest";
    public static final String DEFAULT_RUNNING_OS_IMAGE_MANIFEST = "defaultRunningOsImageManifest";
    public static final String SOC_PROTOTYPE_LOCAL_POWER_PATH = "/node/socBoard/soc/localPower";
    public static final String MEMBOARD_POWER_ON_OPTIONS_PATH = "/node/memoryBoard/mediaControllers/powerOnOptions";
    public static final String POWER = "power";
    public static final String FORCE_ALL_SOC_OFF = "forceAllSocOff";
    public static final String FORCE_ALL_FABRIC_SOC_OFF = "forceAllFabricSocOff";
    public static final String FORCE_ALL_FAM_FABRIC_SOC_OFF = "forceAllFamFabricSocOff";
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String PAUSED_PATH = "/service/paused?paused=true";
    public static final String UNPAUSED_PATH = "/service/paused?paused=false";
    public static final String DEBUG_PATH = "/service/debug?debug=true";
    public static final String NO_DEBUG_PATH = "/service/debug?debug=false";

    public static final int PAUSE_WAIT_TIMEOUT = 20000;

    private ArrayList<JsonNode> patchOperations;

    public DmaAPI(final String dmaURL) {
        initRestClient();
        this.dmaURL = dmaURL;
        patchOperations = new ArrayList<JsonNode>();
    }

    private void initRestClient() {
        restClient = new RestClient.RestTemplateWithJsonAndString(false);
        restClient.setMessageConverters(
                Arrays.asList(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()));
    }

    /***
     * Creates an 'add' operation that will be executed when commitHttpPatch is called
     *
     * @param path Path that will be appended to the DMA route in the PUT request
     * @param jValue JsonNode containing the value that will be sent in the PUT body
     * @return true if the PUT was successful (2xx), false otherwise
     */
    private boolean setDesiredState(final String path, final JsonNode jValue) {
        JsonNode addOp = ((ObjectNode) jValue).put("path", path).put("op", "add");
        patchOperations.add(addOp);
        return true;
    }

    /***
     * Creates an 'remove' operation that will be executed when commitHttpPatch is called
     *
     * @param path Path that will be appended to the DMA route in the PUT request
     * @return true if the PUT was successful (2xx), false otherwise
     */
    private boolean deleteFromDesiredState(final String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode deleteOp = mapper.readTree("{\"op\": \"remove\", \"path\": \"" + path + "\" }");
            patchOperations.add(deleteOp);
            return true;
        } catch (IOException e) {
            LOG.warn("Failed to remove path '" + path + "'", e);
        }
        return false;
    }

    /**
     * logs a patch operation, displaying all the operations in the array
     *
     * @param mapper ObjectMapper
     * @param jArray Array of operations as ArrayNode
     */
    private void logPatchArray(final ObjectMapper mapper, final ArrayNode jArray) {
        String prettyJson = jArray.toString();
        try {
            prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jArray);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to log patch", e);
        }

        LOG.info("Going to PATCH Desired State: Array of operations = " + prettyJson.toString());
    }

    /**
     * Performs the PATCH operation, sending all the operations stored in the patchOperations object
     * in the body of the patch, in JSON format
     *
     * @return true if the operation was successful, false if it was not
     */
    private boolean commitHttpPatch() {
        if (patchOperations.isEmpty()) {
            LOG.warn("PATCH operation not performed because no operations are defined");
            return true;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jArray = mapper.createArrayNode();

        for (JsonNode op : patchOperations) {
            jArray.add(op);
        }

        logPatchArray(mapper, jArray);
        ResponseEntity<String> response =
                restClient.patchForEntity(dmaURL + TMItemCollector.DMA_DESIRED_ROUTE, jArray, null);

        LOG.info("PATCH Return code : " + response.getStatusCode());

        if (response.getStatusCode().is2xxSuccessful()) {
            patchOperations.clear();
            return true;
        } else {
            LOG.error("PATCH failed with status code: " + response.getStatusCode());
        }

        return false;
    }

    /**
     * For each SoC in the list passed as parameter, we check that we can power on that SoC
     * according to its higher levels in the model hierarchy. Then we set the desired state of the
     * SoC (local_power) to value On, and the runningOsManifest value to the value that was assigned
     * as the desired next OS image manifest for that SoC.
     *
     * If a SoC was not able to power on due to the higher level state, it will display an alert
     *
     * @param socs List of SoC objects against which the action will be performed
     * @return true if the operation was successful, false if it was not
     */
    public boolean setSocOnDesiredState(final List<TMSoc> socs) {
        boolean noIssues = true;

        for (TMSoc soc : socs) {
            if (canPowerSocOn(soc)) {
                String socLocalPowerPath = soc.getCore().getPath() + SOC_SFX + DOT + LOCAL_POWER;
                JsonNode jValueOn = getValueJsonNode(POWER_ON);
                setDesiredState(socLocalPowerPath, jValueOn);
                String osManifest = soc.getCore().getDesiredNextOsImageManifest();
                if (null != osManifest) {
                    String runningManifestPath = soc.getCore().getPath() + SOC_SFX + DOT + RUNNING_OS_IMAGE_MANIFEST;
                    JsonNode jValManifest = getValueJsonNode(osManifest);
                    setDesiredState(runningManifestPath, jValManifest);
                }
            } else {
                noIssues = false;
            }
        }

        if (commitHttpPatch()) {
            return noIssues;
        } else {
            return false;
        }
    }

    /**
     * Sets the desired state of the SoC passed as parameter (local_power) to value off. The value
     * of force will overwrite SoC-level enableForcefulStop in the model, resulting in a forceful
     * 'power off' action for that SoC.
     *
     * @param socs List of SoC objects that we want to power off
     * @param force indicates weather we want to force the power off action or not
     * @return true if the operation was successful, false if it was not
     */
    public boolean setSocOffDesiredState(final List<TMSoc> socs, final boolean force) {
        for (TMSoc soc : socs) {
            String socPath = soc.getCore().getPath() + SOC_SFX;
            String nongracefulPath = socPath + DOT + ENABLE_NON_GRACEFUL_SHUTDOWN;
            if (force) {
                JsonNode jValueTrue = getValueJsonNode(true);
                setDesiredState(nongracefulPath, jValueTrue);
            } else {
                deleteFromDesiredState(nongracefulPath);
            }
            JsonNode jValueOff = getValueJsonNode(POWER_OFF);
            String localPowerPath = socPath + DOT + LOCAL_POWER;
            setDesiredState(localPowerPath, jValueOff);
            String runningManifestPath = socPath + DOT + RUNNING_OS_IMAGE_MANIFEST;
            JsonNode jValManifest = getValueJsonNode(TBD);
            setDesiredState(runningManifestPath, jValManifest);
        }
        return commitHttpPatch();
    }

    /**
     * Set or change the OS Manifest Binding in the OS manifesting Service, either for an individual
     * SoC or a group of SoCs. Note that this operation will NOT cause a reboot of the SoC, because
     * the socBoard.soc.runningOsManifest is set to TBD.
     *
     * @param socs List of SoC objects that we want to modify
     * @param nextOsImage value to be assigned to runningOsImageManifest at SoC level coordinate
     * @param runningOsImage value to be assigned to runningOsImageManifest at SoC level coordinate
     * @return true if the operation was successful, false if it was not
     */
    public boolean setOsManifestBinding(final List<TMSoc> socs, final String nextOsImage, final String runningOsImage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("nextOsImage: " + nextOsImage + ", runningOsImage: " + runningOsImage);
        }

        for (TMSoc soc : socs) {
            String socPath = soc.getCore().getPath() + SOC_SFX;
            String nextOsImagePath = socPath + DOT + NEXT_OS_IMAGE_MANIFEST;
            String runningOsImagePath = socPath + DOT + RUNNING_OS_IMAGE_MANIFEST;
            JsonNode jNextValue = getValueJsonNode(nextOsImage);
            JsonNode jRunningValue = getValueJsonNode(runningOsImage);
            setPathAndValueDesiredState(jNextValue, nextOsImagePath);
            setPathAndValueDesiredState(jRunningValue, runningOsImagePath);
        }
        return commitHttpPatch();
    }

    /**
     * This function sets the default_next_os_image_manivest and the
     * default_running_os_image_manifest at rack level. Doesn't commit the patch ops
     *
     * @param rack
     * @param nextOsImage
     * @param runningOsImage
     */
    private void setOsManifestBindingInstanceDefaults(final TMRack rack, final String nextOsImage,
            final String runningOsImage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("nextOsImage: " + nextOsImage + ", runningOsImage: " + runningOsImage);
        }

        String defaultNextOsImagePath = rack.getCore().getPath() + SLASH + DEFAULT_NEXT_OS_IMAGE_MANIFEST;
        String defaultRunningOsImagePath = rack.getCore().getPath() + SLASH + DEFAULT_RUNNING_OS_IMAGE_MANIFEST;
        JsonNode jNextValue = getValueJsonNode(nextOsImage);
        JsonNode jRunningValue = getValueJsonNode(runningOsImage);
        setPathAndValueDesiredState(jNextValue, defaultNextOsImagePath);
        setPathAndValueDesiredState(jRunningValue, defaultRunningOsImagePath);
    }

    /**
     * Set or change the OS Manifest Binding in the OS manifesting Service, for all SoCs. Note that
     * this operation will NOT cause a reboot of the SoC.
     *
     * @param instances a list of instances that will be affected
     * @param nextOsImage value to be assigned to default_next_os_image_manifest in the desired
     *        state under the rack Coordinates
     * @param runningOsImage value to be assigned to default_running_os_image_manifest in the
     *        desired state under the rack Coordinates
     * @return true if operations were successful, false otherwise
     */
    public boolean setOsManifestBindingAll(final List<TMInstance> instances, final String nextOsImage,
            final String runningOsImage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("nextOsImage: " + nextOsImage + ", runningOsImage: " + runningOsImage);
        }

        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            Collection<Item> racks = instanceItem.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instanceItem.getProviderType(),
                            Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
            deleteAllSocOverrides(instance, new String[] {NEXT_OS_IMAGE_MANIFEST, RUNNING_OS_IMAGE_MANIFEST});
            for (Item rackItem : racks) {
                TMRack rack = (TMRack) rackItem;
                setOsManifestBindingInstanceDefaults(rack, nextOsImage, runningOsImage);
            }
        }
        return commitHttpPatch();
    }

    /**
     * Iterated through a list of SoCs, get the rack corresponding to each SoC, and return a HashMap
     * of mappings between racks and its corresponding SoCs
     *
     * @param socs List of SoC objects that we want to map
     * @return HashMap mapping the racks (keys) with lists of SoCs (from the parameter)
     */
    private HashMap<TMRack, List<TMSoc>> getRacksSocsMapping(final List<TMSoc> socs) {
        HashMap<TMRack, List<TMSoc>> racksSocsMapping = new HashMap<TMRack, List<TMSoc>>();
        for (TMSoc soc : socs) {
            Item nodeItem = soc.getFirstConnectedItemWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(soc.getProviderType(),
                            Types.NODE_TYPE_LOCAL_ID, Types.SOC_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
            Item enclosureItem = nodeItem.getFirstConnectedItemWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(nodeItem.getProviderType(),
                            Types.ENCLOSURE_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
            Item rackItem = enclosureItem.getFirstConnectedItemWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(enclosureItem.getProviderType(),
                            Types.RACK_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
            TMRack rack = (TMRack) rackItem;
            if (!racksSocsMapping.containsKey(rack)) {
                racksSocsMapping.put(rack, new ArrayList<TMSoc>());
            }
            racksSocsMapping.get(rack).add(soc);
        }
        return racksSocsMapping;
    }

    /**
     * Resynchronize the running OS manifest to the SoCs in the list passed as parameter.
     * Resynchronizing the OS Manifest will have the effect of rebooting the SoCs that are powered
     * on and are not currently running the desired OS manifest.
     *
     * @param socs List of SoC objects that whose OS manifest we want to synchronize
     * @return true if operations were successful, false otherwise
     */
    public boolean syncOsManifest(final List<TMSoc> socs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("syncing " + socs.size() + " SoCs");
        }

        HashMap<TMRack, List<TMSoc>> racksSocsMapping = getRacksSocsMapping(socs);
        Iterator<Entry<TMRack, List<TMSoc>>> it = racksSocsMapping.entrySet().iterator();

        while (it.hasNext()) {
            Entry<TMRack, List<TMSoc>> pair = it.next();
            TMRack rack = pair.getKey();
            List<TMSoc> rackSocs = pair.getValue();
            String defaultNext = rack.getCore().getDesiredDefaultNextOsImageManifest();

            if (LOG.isDebugEnabled()) {
                LOG.debug("defaultNext: " + defaultNext);
            }

            if (!TBD.equals(defaultNext) && areAllSocsOn(rack)) {
                syncOsManifestTopLevel(rackSocs, rack);
            } else {
                for (TMSoc soc : rackSocs) {
                    if (ON.equals(soc.getCore().getDesiredLocalPower())) {
                        syncOsManifestSocLevelAssumingOn(soc, defaultNext);
                    }
                }
            }
            it.remove();
        }
        return commitHttpPatch();
    }

    /**
     * This function assumes that the desired power state of the SoC passed as parameter is on. It
     * sets the value of SocBoard.soc.runningOsManifst. If a node-level override for
     * socBoard.soc.nextOsImage.manifest exists, then sets socBoard.soc.runningOsManifest to the
     * value of socBoard.soc.nextOsImage.manifest, otherwise to the value of
     * default_mnext_os_image_manifest (passed as parameter)
     *
     * @param soc
     * @param defaultNext
     */
    private void syncOsManifestSocLevelAssumingOn(final TMSoc soc, final String defaultNext) {
        String socPath = soc.getCore().getPath() + SOC_SFX;
        String runningOsManifestPath = socPath + DOT + RUNNING_OS_IMAGE_MANIFEST;
        String next = soc.getCore().getDesiredNextOsImageManifest();
        if (next != null && next != "") {
            JsonNode jNextValue = getValueJsonNode(next);
            setPathAndValueDesiredState(jNextValue, runningOsManifestPath);
        } else {
            JsonNode jNextValue = getValueJsonNode(defaultNext);
            setPathAndValueDesiredState(jNextValue, runningOsManifestPath);
        }
    }

    /**
     * Resynchronize the running OS manifest of all SoCs that have a desired state of powered on,
     * under all the instances passed as parameter. Resynchronizing the OS Manifest will have the
     * effect of rebooting currently powered on SoCs and that are not currently running the desired
     * OS manifest.
     *
     * @param instances
     * @return true if operations were successful, false otherwise
     */
    public boolean syncOsManifestAll(final List<TMInstance> instances) {
        for (Item instance : instances) {
            Collection<Item> racks = instance.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                            Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
            for (Item rackItem : racks) {
                TMRack rack = (TMRack) rackItem;
                String defaultNext = rack.getCore().getDesiredDefaultNextOsImageManifest();
                if (!TBD.equals(defaultNext) && areAllSocsOn(rack)) {
                    syncOsManifestTopLevel(null, rack);
                } else {
                    Collection<Item> enclosures = rack.getConnectedItemsWithRelationshipName(
                            RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rack.getProviderType(),
                                    Types.ENCLOSURE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID,
                                    RelationshipNames.CONTAINS));
                    for (Item enclosureItem : enclosures) {
                        Collection<Item> nodes = enclosureItem.getConnectedItemsWithRelationshipName(
                                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(
                                        enclosureItem.getProviderType(), Types.NODE_TYPE_LOCAL_ID,
                                        Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
                        for (Item nodeItem : nodes) {
                            Collection<Item> socs = nodeItem.getConnectedItemsWithRelationshipName(
                                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(
                                            nodeItem.getProviderType(), Types.SOC_TYPE_LOCAL_ID,
                                            Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
                            for (Item socItem : socs) {
                                TMSoc soc = (TMSoc) socItem;
                                if (ON.equals(soc.getCore().getDesiredLocalPower())) {
                                    syncOsManifestSocLevelAssumingOn(soc, defaultNext);
                                }
                            }
                        }
                    }
                }
            }
        }
        return commitHttpPatch();
    }

    /**
     * If the top-level default_next_os_image_manifest is set to an actual value (not TBD, and
     * including empty string), then set the top-level default_running_os_image_manifest to same
     * value if ALL SoCs are on; in this case, for any SoC that has a node-level override for
     * socBoard.soc.nextOsImage.manifest, also set the node-level override for
     * socBoard.soc.runningOsManifest to the value of socBoard.soc.nextOsImage.manifest.
     *
     * @param socs List of SoCs that will be affected, in the case that all the SoCs under the rack
     *        are on. If this parameter is null, all SoCs under the rack will be affected.
     * @param rack Rack object containing all the SoCs against which the action will be performed
     * @return true if the operation was successful
     */
    private boolean syncOsManifestTopLevel(List<TMSoc> socs, final TMRack rack) {
        boolean ok = setTopLevelDefaultImageToNextValue(rack);
        if (socs == null) {
            socs = getAllSocsUnderRack(rack);
        }
        for (TMSoc soc : socs) {
            ok = ok & setSocRunningOsImageToDefault(soc);
        }
        return ok;
    }

    /**
     * @param rack TMRack that will be inspected
     * @return list of all SoCs (TMSoc objects) under the rack passed as parameter
     */
    private List<TMSoc> getAllSocsUnderRack(final TMRack rack) {
        ArrayList<TMSoc> allSocs = new ArrayList<TMSoc>();
        Collection<Item> enclosures = rack.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rack.getProviderType(),
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
                    allSocs.add(soc);
                }
            }
        }
        return allSocs;
    }

    /**
     * Sets the rack level default_running_os_image_manifest to the value of the rack level desired
     * next OS image manifest
     *
     * @param rack TMRack object that will be modified
     * @return true if the operation was successful
     */
    private boolean setTopLevelDefaultImageToNextValue(final TMRack rack) {
        String defaultNext = rack.getCore().getDesiredDefaultNextOsImageManifest();
        JsonNode jNextValue = getValueJsonNode(defaultNext);
        String defaultRunningOsManifestPath = rack.getCore().getPath() + SLASH + DEFAULT_RUNNING_OS_IMAGE_MANIFEST;
        return setPathAndValueDesiredState(jNextValue, defaultRunningOsManifestPath);
    }

    /**
     * Sets the SoC level runningOsImageManifest to the value of the SoC level desired next OS image
     * manifest
     *
     * @param soc TMSoC object that will be modified
     * @return true if the operation was successful
     */
    private boolean setSocRunningOsImageToDefault(final TMSoc soc) {
        String next = soc.getCore().getDesiredNextOsImageManifest();
        if (null != next) {
            String path = soc.getCore().getPath() + SOC_SFX + DOT + RUNNING_OS_IMAGE_MANIFEST;
            JsonNode jVal = getValueJsonNode(next);
            return setPathAndValueDesiredState(jVal, path);
        }
        return true;
    }

    /**
     * Execute a GET request to obtain the local_power value of the SoC in the Node prototype
     *
     * @return local_power value
     */
    private String getNodePrototypeLocalPower() {
        ResponseEntity<PowerPrototype> response = restClient
                .get(dmaURL + TMItemCollector.DMA_DESIRED_ROUTE + SOC_PROTOTYPE_LOCAL_POWER_PATH, PowerPrototype.class);
        LOG.info("GET Node Pototype SoC local_power. Return code : " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            LOG.info("Node Pototype SoC local_power = " + response.getBody().value);
            return response.getBody().value;
        }
        return null;
    }

    /**
     * Checks if a SoC is ON. If the high level requirements prevent it to switch on, false will be
     * returned. Otherwise, if soc override for local_power is on, true will be returned. If the
     * node prototype power value is on, and the override for SoC local power is not off, true will
     * be returned. In any other case, false wil lbe returned.
     *
     * @param soc TMSoC object that we want to check
     * @param nodePrototypePower power value of the node prototype
     * @return true if the SoC is on, false otherwise
     */
    private boolean isSocOn(final TMSoc soc, final String nodePrototypePower) {
        if (!canPowerSocOn(soc)) {
            return false;
        }
        if (POWER_ON.equals(nodePrototypePower) && !OFF.equals(soc.getCore().getDesiredLocalPower())) {
            return true;
        }
        if (ON.equals(soc.getCore().getDesiredLocalPower())) {
            return true;
        }
        return false;
    }

    /**
     * @param rack Rack object containing the SoCs that will be checked
     * @return true if all the SoCs under the rack are on (desired power state). False otherwise
     */
    protected boolean areAllSocsOn(final TMRack rack) {
        String nodePrototypePower = getNodePrototypeLocalPower();
        Collection<Item> enclosures = rack.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rack.getProviderType(),
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
                    if (!isSocOn(soc, nodePrototypePower)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets the values at rack level needed in order to set all the FAM, Fabric and SOC's power on
     *
     * @param instances List of TMInstance objects against which the action will be performed
     * @return true only if all the operations were successful
     */
    public boolean setInstanceAllPoweredOnDesiredState(final List<TMInstance> instances) {
        setSocPrototypeLocalPower(POWER_ON);
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            setRackValues(instance, POWER_ON, false, false, false, false, null);
            deleteAllSocOverrides(instance, new String[] {LOCAL_POWER});
            setConsistentRackManifest(instance);
            setConsistentSocManifest(instance);
        }
        return commitHttpPatch();
    }

    /**
     * Allows users to control power state of SoCs, but will not directly power on the SoCs.
     *
     * @param instances List of TMInstance objects against which the action will be performed
     * @return true only if all the operations were successful
     */
    public boolean setInstanceEnableSocPowerOnDesiredState(final List<TMInstance> instances) {
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            setRackValues(instance, POWER_ON, false, false, false, false, TBD);
            setTbdSocManifest(instance);
        }
        return commitHttpPatch();
    }

    /**
     * Will power off the whole machine (all the instances in the list passed as parameter)
     *
     * @param instances List of TMInstance objects against which the action will be performed
     * @param force true to force the power off, false to do it gracefully
     * @return true if the operation worked, false if it did not
     */
    public boolean setInstanceAllPoweredOffDesiredState(final List<TMInstance> instances, final boolean force) {
        setSocPrototypeLocalPower(POWER_OFF);
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            setRackValues(instance, POWER_OFF, false, false, false, force, TBD);
            deleteAllSocOverrides(instance,
                    new String[] {LOCAL_POWER, RUNNING_OS_IMAGE_MANIFEST, ENABLE_NON_GRACEFUL_SHUTDOWN});
        }
        return commitHttpPatch();
    }

    /**
     * Will only power the FAM and Fabric, causing all SoCs to power off if they are already powered
     * on.
     *
     * @param instances List of TMInstance objects against which the action will be performed
     * @return true only if all the operations were successful
     */
    public boolean setInstanceOnlyFamFabricPoweredOn(final List<TMInstance> instances) {
        setSocPrototypeLocalPower(POWER_OFF);
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            setRackValues(instance, POWER_ON, false, false, true, false, TBD);
            deleteAllSocOverrides(instance,
                    new String[] {LOCAL_POWER, RUNNING_OS_IMAGE_MANIFEST, ENABLE_NON_GRACEFUL_SHUTDOWN});
        }
        return commitHttpPatch();
    }

    /**
     * Will only power the FAM, causing all other resources to power off if they are already powered
     * on
     *
     * @param instances List of TMInstance objects against which the action will be performed
     * @return true only if all the operations were successful
     */
    public boolean setInstanceOnlyFamPoweredOn(final List<TMInstance> instances) {
        setSocPrototypeLocalPower(POWER_OFF);
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            setRackValues(instance, POWER_ON, true, false, true, false, TBD);
            deleteAllSocOverrides(instance,
                    new String[] {LOCAL_POWER, RUNNING_OS_IMAGE_MANIFEST, ENABLE_NON_GRACEFUL_SHUTDOWN});
        }
        return commitHttpPatch();
    }

    private boolean isInstancePaused(final TMInstance instance) {
        String aaMode = instance.getCore().getAaMode();
        return Const.MODE_PAUSED.equalsIgnoreCase(aaMode) || Const.MODE_DEBUG_PAUSED.equalsIgnoreCase(aaMode);
    }

    /**
     * Check if any instance is not paused
     *
     * @param instances
     * @return true only if all the instances are paused
     */
    private boolean isAAPaused(final List<TMInstance> instances) {
        for (Item instanceItem : instances) {
            TMInstance instance = (TMInstance) instanceItem;
            if (!isInstancePaused(instance)) {
                return false;
            }
        }
        return true;
    }

    private boolean executePrepareSystemBoot(final String csFile, final String udsFile) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String uds = readFile(udsFile);
            JsonNode jUds = mapper.readTree(uds);
            String current = readFile(csFile);
            JsonNode jCurrent = mapper.readTree(current);
            ResponseEntity<String> response =
                    restClient.putForEntity(dmaURL + TMItemCollector.DMA_DESIRED_ROUTE + SLASH, jUds, null);
            LOG.info("UDS PUT Return code : " + response.getStatusCode());
            if (!response.getStatusCode().is2xxSuccessful()) {
                return false;
            }
            response = restClient.putForEntity(dmaURL + TMItemCollector.DMA_CURRENT_ROUTE + SLASH, jCurrent, null);
            LOG.info("CURRENT PUT Return code : " + response.getStatusCode());
            if (!response.getStatusCode().is2xxSuccessful()) {
                return false;
            }
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to parse JSON", e);
            return false;
        } catch (IOException e) {
            LOG.warn("Failed to update state", e);
            return false;
        }
        return true;
    }

    /**
     * Class that implements callbacks for asynchronous actions that depend on instance states,
     * which hare notified calling onDmaEvent
     *
     */
    class PrepareSystemBootListener implements ITMItemCollectorListener {

        private String csFile;
        private String udsFile;
        private List<TMInstance> instances;

        PrepareSystemBootListener(final List<TMInstance> instances, final String csFile, final String udsFile) {
            this.csFile = csFile;
            this.udsFile = udsFile;
            this.instances = instances;
        }

        @Override
        public void onDmaEvent(final TMItemCollector notifier, final String eventName) {
            if (eventName.equalsIgnoreCase(Const.MODE_PAUSED)) {
                LOG.info("Assembly agent is now paused. Preparing for system boot...");
                notifier.removeListener(this);
                executePrepareSystemBoot(csFile, udsFile);
            }
        }

        @Override
        public void onDmaListenerTimeoutExpired(final TMItemCollector notifier) {
            LOG.info("Pause Timeout expired, aborting prepare for system boot");
            for (TMInstance instance : instances) {
                instance.displayAlert("Could not prepare for system boot,"
                        + " because the Assembly agent did not pause. Please pause it manually and try again");
            }
        }
    }

    PrepareSystemBootListener createSystemBootListener(final List<TMInstance> instances, final String csFile,
            final String udsFile) {
        return new PrepareSystemBootListener(instances, csFile, udsFile);
    }


    /**
     * Submit an all-off current state json file to endpoint …/dma/dma/current/ and Submit a default
     * UDS json file to endpoint …/dma/dma/desired
     *
     * @param instances List of TMInstance objects against which the action will be performed
     * @return true only if all the operations were successful
     * @throws IOException
     * @throws JsonProcessingException
     */
    public boolean setInstancePrepareSystemBootDesiredState(final TMItemCollector notifier,
            final List<TMInstance> instances, final String csFile, final String udsFile) {
        if (!isAAPaused(instances)) {
            LOG.info("Pausing Assembly agent...");
            setInstancePausedMode();
            PrepareSystemBootListener listener = createSystemBootListener(instances, csFile, udsFile);
            notifier.addListenerWithTimeout(listener, PAUSE_WAIT_TIMEOUT);
            return true;
        }
        LOG.info("Preparing for system boot...");
        return executePrepareSystemBoot(csFile, udsFile);
    }

    protected String readFile(final String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public boolean setInstancePausedMode() {
        ResponseEntity<String> response = restClient.putForEntity(dmaURL + PAUSED_PATH, null, null);
        LOG.info("Pause PUT Return code : " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        }
        return false;
    }

    public boolean setInstanceUnpausedMode(final List<TMInstance> instances, final Component stateMonitor) {
        ResponseEntity<String> response = restClient.putForEntity(dmaURL + UNPAUSED_PATH, null, null);
        LOG.info("Unpause PUT Return code : " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        }
        return false;
    }

    public boolean setInstanceDebugMode() {
        ResponseEntity<String> response = restClient.putForEntity(dmaURL + DEBUG_PATH, null, null);
        LOG.info("Set Debug Mode PUT Return code : " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        }
        return false;
    }

    public boolean unsetInstanceDebugMode() {
        ResponseEntity<String> response = restClient.putForEntity(dmaURL + NO_DEBUG_PATH, null, null);
        LOG.info("Unset Debug Mode PUT Return code : " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        }
        return false;
    }

    public boolean setMemtest(final boolean enable) {
        String value = MEMTEST_NO;
        if (enable) {
            value = MEMTEST_YES;
        }
        JsonNode jVal = getValueJsonNode(value);
        setPathAndValueDesiredState(jVal, MEMBOARD_POWER_ON_OPTIONS_PATH);
        return this.commitHttpPatch();
    }

    /**
     * The SoC actions can be performed by Tenant Admins. The SoC actions assume that the top level
     * force and power attributes are set appropriately, and that the SoC power-related attributes
     * will be rejected if not.
     *
     * @param soc SoC that will be checked
     * @return true if the SoC can be powered on
     */
    private boolean canPowerSocOn(final TMSoc soc) {
        Item nodeItem = soc.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(soc.getProviderType(),
                        Types.NODE_TYPE_LOCAL_ID, Types.SOC_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        TMNode node = (TMNode) nodeItem;
        if (!ON.equals(node.getCore().getDesiredLocalPower())) {
            LOG.info("Cannot power on SoC because its Node desired local_power is OFF");
            return false;
        }
        Item enclosureItem = nodeItem.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(nodeItem.getProviderType(),
                        Types.ENCLOSURE_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        TMEnclosure enclosure = (TMEnclosure) enclosureItem;
        if (!ON.equals(enclosure.getCore().getDesiredLocalPower())) {
            LOG.info("Cannot power on SoC because its Enclosure desired local_power is OFF");
            return false;
        }
        Item rackItem = enclosureItem.getFirstConnectedItemWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(enclosureItem.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        TMRack rack = (TMRack) rackItem;
        if (!ON.equals(rack.getCore().getDesiredPower())) {
            LOG.info("Cannot power on SoC because its Rack desired power is " + rack.getCore().getDesiredPower());
            return false;
        }
        if (rack.getCore().getDesiredForceAllFamFabricSocOff()) {
            LOG.info("Cannot power on SoC because desired force_all_fam_fabric_soc_off is set to true");
            return false;
        }
        if (rack.getCore().getDesiredForceAllFabricSocOff()) {
            LOG.info("Cannot power on SoC because desired force_all_fabric_soc_off is set to true");
            return false;
        }
        if (rack.getCore().getDesiredForceAllSocOff()) {
            LOG.info("Cannot power on SoC because desired force_all_soc_off is set to true");
            return false;
        }
        return true;
    }

    /**
     * If default_next_os_image_manifest is set, also set default_running_os_image_manifest to same
     * value.
     *
     * @param instance Instance object containing the rack against which the action will be
     *        performed
     * @return true if the action worked or was not necessary. False otherwise
     */
    private boolean setConsistentRackManifest(final TMInstance instance) {
        boolean ok = true;
        Collection<Item> racks = instance.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        for (Item rackItem : racks) {
            TMRack rack = (TMRack) rackItem;
            String next = rack.getCore().getDesiredDefaultNextOsImageManifest();
            if (null != next) {
                String path = rack.getCore().getPath() + SLASH + DEFAULT_RUNNING_OS_IMAGE_MANIFEST;
                JsonNode jVal = getValueJsonNode(next);
                ok = ok && setPathAndValueDesiredState(jVal, path);
            }
        }
        return ok;
    }

    /**
     * If any node-level override values exist for socBoard.soc.nextOsImage.manifest, set
     * corresponding socBoard.soc.runningOsImageManifest to same value (can do this because
     * local_power overrides have been deleted).
     *
     * @param instance Instance object against which the action will be performed
     * @return true if the action worked or was not necessary. False otherwise
     */
    private boolean setConsistentSocManifest(final TMInstance instance) {
        boolean ok = true;
        Collection<Item> racks = instance.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        for (Item rackItem : racks) {
            Collection<Item> enclosures = rackItem.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rackItem.getProviderType(),
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
                        ok = ok & this.setSocRunningOsImageToDefault(soc);
                    }
                }
            }
        }
        return ok;
    }

    /**
     * If any node override values exist for socBoard.soc.nextOsImage.manifest, set corresponding
     * socBoard.soc.runningOsImageManifest to TBD.
     *
     * @param instance Instance object containing the SoC against which the action will be performed
     * @return true if the action worked or was not necessary. False otherwise
     */
    private boolean setTbdSocManifest(final TMInstance instance) {
        boolean ok = true;
        Collection<Item> racks = instance.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        for (Item rackItem : racks) {
            Collection<Item> enclosures = rackItem.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rackItem.getProviderType(),
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
                        String next = soc.getCore().getDesiredNextOsImageManifest();
                        if (null != next) {
                            String path = soc.getCore().getPath() + SOC_SFX + DOT + RUNNING_OS_IMAGE_MANIFEST;
                            JsonNode jVal = getValueJsonNode(TBD);
                            ok = ok && setPathAndValueDesiredState(jVal, path);
                        }
                    }
                }
            }
        }
        return ok;
    }

    /**
     * Delete all overrides for node.socBoard.soc.<leaf>, for each leaf in leafs parameter
     *
     * @param instance Instance object containing the SoC against which the action will be performed
     * @return true if the action worked or was not necessary. False otherwise
     */
    private boolean deleteAllSocOverrides(final TMInstance instance, final String[] leafs) {
        boolean ok = true;
        Collection<Item> racks = instance.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        for (Item rackItem : racks) {
            Collection<Item> enclosures = rackItem.getConnectedItemsWithRelationshipName(
                    RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(rackItem.getProviderType(),
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
                        for (String leaf : leafs) {
                            String path = soc.getCore().getPath() + SOC_SFX + DOT + leaf;
                            ok = ok && deleteFromDesiredState(path);
                        }
                    }
                }
            }
        }
        return ok;
    }

    /**
     * Set the local_power attribute of the SoC in the Node prototype to default to the value passed
     * as parameter
     *
     * @param power default value that will be set to the SoC local_power prototype
     * @return true only if the operation was successful
     */
    private boolean setSocPrototypeLocalPower(final String power) {
        JsonNode jValPower = getValueJsonNode(power);
        return setPathAndValueDesiredState(jValPower, SOC_PROTOTYPE_LOCAL_POWER_PATH);
    }

    /**
     * Function used to set the relevant rack level values to modify the FAM, Fabric and SOC's power
     * desired value
     *
     * @param instance TMInstance containing the racks that will be modified
     * @param power desired power state (ON/OFF)
     * @param forceAllFabricSocOff true only if we want to force all Fabric and SoCs to be off
     * @param forceAllFamFabricSocOff true only if we want to force all FAM, Fabric and SoCs to be
     *        off
     * @param forceAllSocOff true only if we want to force all SoCs to be off
     * @param enableForcefulStop true only if we want to perform a forceful stop
     * @param defaultRunningOsImageManifest if this value is different to null, it will overwrite
     *        default_running_os_image_manifest at rack level
     * @return true only if all the operations were successful
     */
    private boolean setRackValues(final TMInstance instance, final String power, final boolean forceAllFabricSocOff,
            final boolean forceAllFamFabricSocOff, final boolean forceAllSocOff, final boolean enableForcefulStop,
            final String defaultRunningOsImageManifest) {
        Collection<Item> racks = instance.getConnectedItemsWithRelationshipName(
                RelationshipUtil.getRelationshipNameBetweenTypeIdsWithRelType(instance.getProviderType(),
                        Types.RACK_TYPE_LOCAL_ID, Types.INSTANCE_TYPE_LOCAL_ID, RelationshipNames.CONTAINS));
        boolean ok = true;
        for (Item rackItem : racks) {
            TMRack rack = (TMRack) rackItem;
            String rackPath = rack.getCore().getPath();
            String rackPowerPath = rackPath + SLASH + POWER;
            String forceAllFabricSocOffPath = rackPath + SLASH + FORCE_ALL_FABRIC_SOC_OFF;
            String forceAllFamFabricSocOffPath = rackPath + SLASH + FORCE_ALL_FAM_FABRIC_SOC_OFF;
            String forceAllSocOffPath = rackPath + SLASH + FORCE_ALL_SOC_OFF;
            String enableNonGracefulShutdownPath = instance.getCore().getPath() + SLASH + ENABLE_NON_GRACEFUL_SHUTDOWN;
            JsonNode jValPower = getValueJsonNode(power);
            JsonNode jValForceAllFabricSocOff = getValueJsonNode(forceAllFabricSocOff);
            JsonNode jValForceAllFamFabricSocOff = getValueJsonNode(forceAllFamFabricSocOff);
            JsonNode jValForceAllSocOff = getValueJsonNode(forceAllSocOff);
            JsonNode jValEnableForcefulStop = getValueJsonNode(enableForcefulStop);
            ok = ok & setPathAndValueDesiredState(jValPower, rackPowerPath);
            ok = ok & setPathAndValueDesiredState(jValForceAllFabricSocOff, forceAllFabricSocOffPath);
            ok = ok & setPathAndValueDesiredState(jValForceAllFamFabricSocOff, forceAllFamFabricSocOffPath);
            ok = ok & setPathAndValueDesiredState(jValForceAllSocOff, forceAllSocOffPath);
            ok = ok & setPathAndValueDesiredState(jValEnableForcefulStop, enableNonGracefulShutdownPath);
            if (null != defaultRunningOsImageManifest) {
                String defaultManifestPath = rackPath + SLASH + DEFAULT_RUNNING_OS_IMAGE_MANIFEST;
                JsonNode jValDefaultManifest = getValueJsonNode(defaultRunningOsImageManifest);
                ok = ok & setPathAndValueDesiredState(jValDefaultManifest, defaultManifestPath);
            }
        }
        return ok;
    }


    /**
     * Creates a JsonNode with the key "value" and the value passed as parameter (String). If the
     * parameter is not a valid string representation of a JsonObject, it is simply wrapped as is.
     * Exceptions are logged, and if they happen, null is returned.
     *
     * @param value String representation of the value to be converted to a JsonNode
     * @return JsonNode corresponding to the json representation of the string value passed as
     *         parameter, to be sent to DMA REST API
     */
    protected JsonNode getValueJsonNode(final String value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (TBD.equals(value)) {
                return mapper.readTree("{\"value\": {\"value\": \"" + value + "\", \"type\": \"string\"}}");
            }
            try {
                JsonNode jVal = mapper.readTree(value);
                JsonNode jWrapped = mapper.createObjectNode();
                ((ObjectNode) jWrapped).set("value", jVal);
                return jWrapped;
            } catch (JsonProcessingException e) {
                return mapper.readTree("{\"value\": \"" + value + "\"}");
            }
        } catch (IOException e) {
            LOG.warn("Failed to node with '" + value + "'", e);
        }
        return null;
    }

    /**
     * Creates a JsonNode with the key "value" and the value passed as parameter (boolean).
     * Exceptions are logged, and if they happen, null is returned.
     *
     * @param value Boolean representation of the value to be converted to a JsonNode
     * @return JsonNode corresponding to the json representation of the boolean value passed as
     *         parameter, to be sent to DMA REST API
     */
    protected JsonNode getValueJsonNode(final boolean value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree("{\"value\": " + value + "}");
        } catch (IOException e) {
            LOG.warn("Failed to node with '" + value + "'", e);
        }
        return null;
    }

    /**
     * sets the value and path passed as parameter to the desired state
     *
     * @param jVal JsonNode containing the value to be sent to DMA
     * @param path path in the DMA REST API where the value will be sent
     * @return true jVal was not null, false if it was
     */
    protected boolean setPathAndValueDesiredState(final JsonNode jVal, final String path) {
        if (null != jVal && path != null) {
            LOG.info("Path : " + path);
            LOG.info("value : " + jVal.toString());
            setDesiredState(path, jVal);
            return true;
        }
        return false;
    }

}
