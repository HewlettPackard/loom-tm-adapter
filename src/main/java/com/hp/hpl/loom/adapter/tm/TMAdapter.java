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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.backend.TMConfig;
import com.hp.hpl.loom.adapter.tm.backend.TMConfig.Server;
import com.hp.hpl.loom.adapter.tm.backend.TMConfig.Service;
import com.hp.hpl.loom.adapter.tm.items.TMBook;
import com.hp.hpl.loom.adapter.tm.items.TMEnclosure;
import com.hp.hpl.loom.adapter.tm.items.TMFabricSwitch;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.adapter.tm.items.TMInterleaveGroup;
import com.hp.hpl.loom.adapter.tm.items.TMMemoryBoard;
import com.hp.hpl.loom.adapter.tm.items.TMNode;
import com.hp.hpl.loom.adapter.tm.items.TMOsProvisioningManifest;
import com.hp.hpl.loom.adapter.tm.items.TMRack;
import com.hp.hpl.loom.adapter.tm.items.TMShelf;
import com.hp.hpl.loom.adapter.tm.items.TMSoc;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.Meta;
import com.hp.hpl.loom.tapestry.Operation;
import com.hp.hpl.loom.tapestry.PatternDefinition;
import com.hp.hpl.loom.tapestry.QueryDefinition;
import com.hp.hpl.loom.tapestry.ThreadDefinition;

public class TMAdapter extends BaseAdapter {
    private static final Log LOG = LogFactory.getLog(TMAdapter.class);

    private String dmaURL;
    private String monitoringURL;
    private String librarianURL;
    private String manifestingURL;
    private String defaultCsFile;
    private String defaultUdsFile;
    private Boolean useFakeData;
    private String defaultLdapServerName;
    private int defaultLdapServerPort;
    private String defaultLdapGroupOu;
    private String defaultLdapPeopleOu;
    private String defaultLdapO;

    public String getDmaURL() {
        return dmaURL;
    }

    public String getMonitoringURL() {
        return monitoringURL;
    }

    public String getLibrarianURL() {
        return librarianURL;
    }

    public String getManifestingURL() {
        return manifestingURL;
    }

    public String getDefaultCsFile() {
        return defaultCsFile;
    }

    public String getDefaultUdsFile() {
        return defaultUdsFile;
    }

    public Boolean getUseFakeData() {
        return useFakeData;
    }

    @Override
    @SuppressWarnings("checkstyle:emptyblock")
    public void onLoad() {
        String configFile = adapterConfig.getPropertiesConfiguration().getString("tmcf");
        defaultCsFile = adapterConfig.getPropertiesConfiguration().getString("default_current_state_file");
        defaultUdsFile = adapterConfig.getPropertiesConfiguration().getString("default_unexpanded_desired_state_file");
        defaultLdapServerName = adapterConfig.getPropertiesConfiguration().getString("ldapServerName");
        defaultLdapServerPort = adapterConfig.getPropertiesConfiguration().getInt("ldapServerPort");
        defaultLdapPeopleOu = adapterConfig.getPropertiesConfiguration().getString("ldapPeopleOu");
        defaultLdapGroupOu = adapterConfig.getPropertiesConfiguration().getString("ldapGroupOu");
        defaultLdapO = adapterConfig.getPropertiesConfiguration().getString("ldapO");

        try {
            useFakeData = adapterConfig.getPropertiesConfiguration().getBoolean("useFakeData");

            if (useFakeData) {
                LOG.warn("Machineless demo mode - faking Librarian global & some other data");
            }
        } catch (NoSuchElementException e) {
            // useFakeLibrarianData is optional
        }

        try {
            File file = new File(configFile);
            ObjectMapper mapper2 = new ObjectMapper();
            mapper2.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TMConfig tmconfig = mapper2.readValue(file, TMConfig.class);
            for (Server server : tmconfig.servers) {
                for (Service service : server.services) {
                    String serviceName = service.service;
                    if (serviceName.equalsIgnoreCase("assemblyAgent")) {
                        LOG.info(service.restUri);
                        dmaURL = service.restUri;
                    } else if (serviceName.equalsIgnoreCase("monitoring")) {
                        LOG.info(service.restUri);
                        monitoringURL = service.restUri;
                    } else if (serviceName.equalsIgnoreCase("librarian")) {
                        LOG.info(service.restUri);
                        librarianURL = service.restUri;
                    } else if (serviceName.equalsIgnoreCase("osManifesting")) {
                        LOG.info(service.restUri);
                        manifestingURL = service.restUri;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        if (dmaURL == null || monitoringURL == null || librarianURL == null || manifestingURL == null) {
            dmaURL = "";
            monitoringURL = "";
            librarianURL = "";
            manifestingURL = "";
            LOG.error("One or more REST endpoints are missing, expecting assemblyAgent, monitoring, librarian,"
                    + "osManifesting");
        }
        super.onLoad();
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new TMItemCollector(session, this, adapterManager, creds);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        return Arrays.asList(TMInstance.class, TMRack.class, TMEnclosure.class, TMNode.class, TMSoc.class,
                TMFabricSwitch.class, TMMemoryBoard.class, TMShelf.class, TMBook.class, TMOsProvisioningManifest.class,
                TMInterleaveGroup.class);
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {
        // TODO Auto-generated method stub
        return new HashMap<String, QuadFunctionMeta>();
    }

    @Override
    public Collection<ItemType> getItemTypes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        final int numPatterns = 3;
        List<PatternDefinition> patterns = new ArrayList<PatternDefinition>(numPatterns);

        List<ItemType> itemTypes = this.getItemTypesFromLocalIds(Arrays.asList(Types.INSTANCE_TYPE_LOCAL_ID,
                Types.SOC_TYPE_LOCAL_ID, Types.MEMORY_BOARD_TYPE_LOCAL_ID, Types.MANIFEST_LOCAL_ID));

        List<String> machineDefaultMetrics = Arrays.asList(Types.SOC_TYPE_LOCAL_ID + ":Cores_avg");
        PatternDefinition pattern = this.createPatternDefinitionWithSingleInputPerThread("machines", itemTypes,
                "Overview (default)", null, true, machineDefaultMetrics);
        patterns.add(pattern);

        itemTypes = this.getItemTypesFromLocalIds(Arrays.asList(Types.INSTANCE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID,
                Types.ENCLOSURE_TYPE_LOCAL_ID, Types.FABRIC_SWITCH_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID,
                Types.SOC_TYPE_LOCAL_ID, Types.MEMORY_BOARD_TYPE_LOCAL_ID));
        pattern = this.createPatternDefinitionWithSingleInputPerThread("hardware", itemTypes, "Hardware", null, false);
        patterns.add(pattern);

        itemTypes = this.getItemTypesFromLocalIds(Arrays.asList(Types.INSTANCE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID,
                Types.ENCLOSURE_TYPE_LOCAL_ID, Types.FABRIC_SWITCH_TYPE_LOCAL_ID, Types.NODE_TYPE_LOCAL_ID,
                Types.SOC_TYPE_LOCAL_ID, Types.MEMORY_BOARD_TYPE_LOCAL_ID));
        Map<String, String> groupByAttributes = new HashMap<>();
        groupByAttributes.put(
                this.getItemTypesFromLocalIds(Arrays.asList(Types.ENCLOSURE_TYPE_LOCAL_ID)).get(0).getId(),
                "core.powerState");
        groupByAttributes.put(
                this.getItemTypesFromLocalIds(Arrays.asList(Types.FABRIC_SWITCH_TYPE_LOCAL_ID)).get(0).getId(),
                "core.powerState");
        groupByAttributes.put(this.getItemTypesFromLocalIds(Arrays.asList(Types.NODE_TYPE_LOCAL_ID)).get(0).getId(),
                "core.powerState");
        groupByAttributes.put(this.getItemTypesFromLocalIds(Arrays.asList(Types.SOC_TYPE_LOCAL_ID)).get(0).getId(),
                "core.powerState");
        groupByAttributes.put(
                this.getItemTypesFromLocalIds(Arrays.asList(Types.MEMORY_BOARD_TYPE_LOCAL_ID)).get(0).getId(),
                "core.powerState");
        pattern = this.createPatternDefinitionWithGroupBy("groupByPowerStatus", itemTypes, "Hardware Power Status",
                null, false, machineDefaultMetrics, groupByAttributes);
        patterns.add(pattern);

        itemTypes = this.getItemTypesFromLocalIds(
                Arrays.asList(Types.SHELF_TYPE_LOCAL_ID, Types.BOOK_TYPE_LOCAL_ID, Types.MANIFEST_LOCAL_ID));
        pattern = this.createPatternDefinitionWithSingleInputPerThread("memory", itemTypes, "Memory", null, false);
        patterns.add(pattern);

        return patterns;
    }

    public PatternDefinition createPatternDefinitionWithGroupBy(final String id, final List<ItemType> itemTypes,
            final String description, final List<Integer> maxFibres, final boolean defaultPattern,
            final List<String> metrics, final Map<String, String> groupByAttributes) {
        if (maxFibres != null && itemTypes.size() != maxFibres.size()) {
            throw new IllegalArgumentException(
                    "itemTypes (" + itemTypes.size() + ") & maxFibres (" + maxFibres.size() + " sizes should match");
        }
        List<Integer> fibreList;
        if (maxFibres == null) {
            fibreList = Collections.nCopies(itemTypes.size(), 0);
        } else {
            fibreList = maxFibres;
        }
        List<ThreadDefinition> threadDefs = new ArrayList<>(itemTypes.size());
        Map<String, ItemType> itMap = new HashMap<>(itemTypes.size());
        int threadIdx = 0;
        for (ItemType it : itemTypes) {
            List<String> ins = new ArrayList<>(1);
            ins.add(getAggregationLogicalIdForPattern(it.getLocalId()));
            QueryDefinition query;
            List<Operation> operations = new ArrayList<>(1);
            String groupByAttribute = groupByAttributes.get(it.getId());
            if (groupByAttribute != null) {
                Map<String, Object> groupParams = new HashMap<>(1);
                groupParams.put(QueryOperation.PROPERTY, groupByAttribute);
                Operation groupOperation = new Operation(DefaultOperations.GROUP_BY.toString(), groupParams);
                operations.add(groupOperation);
            }
            if (fibreList.get(threadIdx) != 0) {
                Map<String, Object> braidParams = new HashMap<>(1);
                braidParams.put(QueryOperation.MAX_FIBRES, fibreList.get(threadIdx));
                Operation braidOperation = new Operation(DefaultOperations.BRAID.toString(), braidParams);
                operations.add(braidOperation);
            }
            if (ins.size() > 0 && operations.size() > 0) {
                query = new QueryDefinition(operations, ins);
            } else {
                query = new QueryDefinition(ins);
            }
            ThreadDefinition threadDefinition = new ThreadDefinition(id + "-" + Integer.toString(threadIdx++),
                    it.getId(), query, createHumanReadableThreadName(it));
            threadDefs.add(threadDefinition);
            itMap.put(it.getId(), it);
        }

        PatternDefinition pd = new PatternDefinition(id, threadDefs, provider.getProviderType(), new Meta(itMap),
                description, metrics);
        pd.setDefaultPattern(defaultPattern);
        return pd;
    }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new TMProvider(providerType, providerId, authEndpoint, providerName,
                this.getClass().getPackage().getName().toString(), defaultLdapServerName, defaultLdapServerPort,
                defaultLdapPeopleOu, defaultLdapGroupOu, defaultLdapO);
    }

    @Override
    protected String createHumanReadableThreadName(final ItemType type) {
        if (type.getLocalId().equals(Types.SOC_TYPE_LOCAL_ID)) {
            return "SoCs";
        }
        if (type.getLocalId().equals(Types.SHELF_TYPE_LOCAL_ID)) {
            return "Shelves";
        }
        if (type.getLocalId().equals(Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID)) {
            return "Interleave Groups";
        }
        if (type.getLocalId().equals(Types.MEMORY_BOARD_TYPE_LOCAL_ID)) {
            return "FAM";
        }
        if (type.getLocalId().equals(Types.MANIFEST_LOCAL_ID)) {
            return "OS Manifest";
        }
        if (type.getLocalId().equals(Types.FABRIC_SWITCH_TYPE_LOCAL_ID)) {
            return "Fabric Switches";
        }
        return super.createHumanReadableThreadName(type);
    }

}
