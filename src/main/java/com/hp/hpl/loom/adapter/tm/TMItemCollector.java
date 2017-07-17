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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.AggregationUpdaterBasedItemCollector;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.tm.backend.LibrarianRestClient;
import com.hp.hpl.loom.adapter.tm.backend.ManifestingRestClient;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.DmaAPI;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaCurrent;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaDesired;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaDiscrepancies;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.librarian.InterleaveGroup;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringEvent;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.BackEndMonitoringMetric;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.MonitoringServiceApi;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Manifest;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.adapter.tm.items.TMOsProvisioningManifest;
import com.hp.hpl.loom.adapter.tm.items.TMSoc;
import com.hp.hpl.loom.adapter.tm.updaters.TMEnclosureUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.TMFabricSwitchUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.TMInstanceUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.TMMemoryBoardUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.TMRackUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.TMSocUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.librarian.TMBookUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.librarian.TMInterleaveGroupUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.librarian.TMNodeUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.librarian.TMShelfUpdater;
import com.hp.hpl.loom.adapter.tm.updaters.osprovisioning.TMOsProvisioningManifestUpdater;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionParameters;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.ActionResultFile;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Session;

@SuppressWarnings("unchecked")
public class TMItemCollector extends AggregationUpdaterBasedItemCollector {
    private static final Log LOG = LogFactory.getLog(TMItemCollector.class);

    // Assembly agent routes
    public static final String DMA_CURRENT_ROUTE = "/current";
    public static final String DMA_DESIRED_ROUTE = "/desired";
    public static final String DMA_EDS_ROUTE = "/eds";
    public static final String DMA_STATUS_ROUTE = "/status";
    public static final String DMA_DISCREPANCIES_ROUTE = "/status/state?sum=true";
    // Monitoring service routes
    public static final String MON_METRICS = "/metrics";
    public static final String MON_EVENTS = "/events";

    public static final int N_REFRESHER_THREADS = 100;
    public static final List<Long> METRICS_POST_PERIODS = Arrays.asList(15000L, 300000L, 3600000L);
    public static final long RETRY_POST_PERIOD_ON_FAIL = 5000;

    private String username = null;
    private BackEndDma<GetDmaCurrent, GetDmaDesired, GetDmaStatus, GetDmaDiscrepancies> dmaCommonUpdater;
    private BackEndMonitoringMetric metricMonitoringServiceUpdater;
    private BackEndMonitoringEvent eventMonitoringServiceUpdater;
    private Map<String, List<String>> socCoordinateToIdToManifestId;
    private LibrarianRestClient librarianRestClient;
    private ManifestingRestClient manifestingRestClient;
    private DmaAPI dmaAPI;
    private MonitoringServiceApi msAPI;
    private String defaultCsFile;
    private String defaultUdsFile;
    private ObjectMapper mapper;
    private InterleaveGroups interleaveGroups;
    private List<ITMItemCollectorListener> listeners;

    public class InterleaveGroups {
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Map<String, InterleaveGroup> raw = new HashMap<>();
    }

    public TMItemCollector(final Session session, final BaseAdapter adapter, final AdapterManager adapterManager,
            final Credentials creds) {
        super(session, adapter, adapterManager);
        username = creds.getUsername();
        mapper = new ObjectMapper();
        String librarianURL = ((TMAdapter) adapter).getLibrarianURL();
        librarianRestClient = new LibrarianRestClient(librarianURL);
        String manifestingURL = ((TMAdapter) adapter).getManifestingURL();
        manifestingRestClient = new ManifestingRestClient(manifestingURL);
        RestClient netInterface = ((TMProvider) provider).getRestClient();
        String dmaURL = ((TMAdapter) adapter).getDmaURL();
        defaultCsFile = ((TMAdapter) adapter).getDefaultCsFile();
        defaultUdsFile = ((TMAdapter) adapter).getDefaultUdsFile();
        String monitoringURL = ((TMAdapter) adapter).getMonitoringURL();

        // Create Monitoring Service updaters
        metricMonitoringServiceUpdater = new BackEndMonitoringMetric(netInterface, monitoringURL + MON_METRICS);
        eventMonitoringServiceUpdater = new BackEndMonitoringEvent(netInterface, monitoringURL + MON_EVENTS);
        dmaCommonUpdater = new BackEndDma<GetDmaCurrent, GetDmaDesired, GetDmaStatus, GetDmaDiscrepancies>(netInterface,
                dmaURL + DMA_CURRENT_ROUTE, dmaURL + DMA_EDS_ROUTE, dmaURL + DMA_STATUS_ROUTE,
                dmaURL + DMA_DISCREPANCIES_ROUTE);
        socCoordinateToIdToManifestId = new HashMap<>();
        interleaveGroups = new InterleaveGroups();
        dmaAPI = new DmaAPI(dmaURL);
        startMonitoringServiceUpdaters(monitoringURL);
        setListeners(new ArrayList<ITMItemCollectorListener>());
    }

    @SuppressWarnings("serial")
    @JsonAutoDetect
    public static class ExpectedType extends HashMap<String, String> {
    }

    @Override
    protected void preUpdateItemHook() {
        long t0 = System.currentTimeMillis();
        ExecutorService threadsPool = Executors.newFixedThreadPool(N_REFRESHER_THREADS);
        dmaCommonUpdater.markAsDirty();
        interleaveGroups.raw = librarianRestClient.getInterleaveGroups().stream()
                .collect(Collectors.toMap((ig) -> ig.groupId.toString(), (ig) -> ig));
        try {
            threadsPool.submit(dmaRefresher);
            threadsPool.submit(monitoringMetricsRefresher);
            threadsPool.submit(monitoringEventsRefresher);
            threadsPool.submit(librarianRefresher);
        } catch (RestClientException e) {
            LOG.error("Failed to refhresh results. Will try to Re-authenticate.\nException", e);
            session.setReAuthenticate(provider, true);
        }
        threadsPool.shutdown();
        try {
            threadsPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOG.error("preUpdateItemHook: thread pool waiting was interrupted: ", e);
        }
        long t1 = System.currentTimeMillis();
        LOG.info("preUpdateItemHook total execution time: " + (t1 - t0) + " ms.");
    }

    private void startMonitoringServiceUpdaters(String monitoringUrl) {
        msAPI = new MonitoringServiceApi(monitoringUrl);
        ExecutorService threadsPool = Executors.newFixedThreadPool(N_REFRESHER_THREADS);
        for (long period : METRICS_POST_PERIODS) {
            LOG.info("Created a metric updater for period = " + period);
            threadsPool.submit(new MetricUpdater(period));
        }
    }

    private class MetricUpdater implements Callable<Void> {
        private long period;

        public MetricUpdater(Long period) {
            this.period = period;
        }

        @Override
        public Void call() throws Exception {
            while (true) {
                LOG.debug("updating metrics for period = " + period + " milliseconds");
                List<TMInstance> instances =
                        (List<TMInstance>) (List<?>) getAggregation(Types.INSTANCE_TYPE_LOCAL_ID).getElements();
                boolean updated = msAPI.postInstanceMetrics(instances, period);
                if (updated) {
                    Thread.sleep(period);
                } else {
                    Thread.sleep(RETRY_POST_PERIOD_ON_FAIL);
                }
            }
        }
    }

    private Callable<Boolean> dmaRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            dmaCommonUpdater.refreshResult();
            return true;
        }
    };

    private Callable<Boolean> monitoringMetricsRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            metricMonitoringServiceUpdater.refreshResult();
            return true;
        }
    };

    private Callable<Boolean> monitoringEventsRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            eventMonitoringServiceUpdater.refreshResult();
            return true;
        }
    };

    private Callable<Boolean> librarianRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            librarianRestClient.refreshResult();
            return true;
        }
    };

    @Override
    protected void postUpdateRelationshipsHook() {
        long startTime = System.currentTimeMillis();
        notifyIfPaused();
        long deltaT = System.currentTimeMillis() - startTime;
        LOG.debug("postUpdateItemHook execution time: " + deltaT + " ms.");
    }

    private void notifyIfPaused() {
        if (!this.getListeners().isEmpty()) {
            GetDmaStatus status = (GetDmaStatus) dmaCommonUpdater.getStatusData();
            if (null != status.servicePaused && "True".equalsIgnoreCase(status.servicePaused)) {
                this.notifyListeners(Const.MODE_PAUSED);
            }
        }
    }

    @Override
    protected AggregationUpdater<? extends AdapterItem<?>, ? extends CoreItemAttributes, ?> getAggregationUpdater(
            final Aggregation aggregation) throws NoSuchProviderException, NoSuchItemTypeException {
        String typeId = aggregation.getTypeId();
        ItemType itemType;

        itemType = adapter.getItemType(Types.INSTANCE_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMInstanceUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater, librarianRestClient);
        }
        itemType = adapter.getItemType(Types.RACK_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMRackUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater, librarianRestClient);
        }
        itemType = adapter.getItemType(Types.ENCLOSURE_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMEnclosureUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater, librarianRestClient);
        }
        itemType = adapter.getItemType(Types.NODE_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMNodeUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater, librarianRestClient);
        }
        itemType = adapter.getItemType(Types.MANIFEST_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMOsProvisioningManifestUpdater(aggregation, adapter, itemType, this, manifestingRestClient);
        }
        itemType = adapter.getItemType(Types.SOC_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMSocUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater,
                    metricMonitoringServiceUpdater, eventMonitoringServiceUpdater, socCoordinateToIdToManifestId);
        }
        itemType = adapter.getItemType(Types.MEMORY_BOARD_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMMemoryBoardUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater,
                    metricMonitoringServiceUpdater, librarianRestClient);
        }
        itemType = adapter.getItemType(Types.FABRIC_SWITCH_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMFabricSwitchUpdater(aggregation, adapter, itemType, this, dmaCommonUpdater,
                    metricMonitoringServiceUpdater);
        }
        itemType = adapter.getItemType(Types.SHELF_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMShelfUpdater(aggregation, adapter, itemType, this, librarianRestClient, interleaveGroups);
        }
        itemType = adapter.getItemType(Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMInterleaveGroupUpdater(aggregation, adapter, itemType, this, librarianRestClient);
        }
        itemType = adapter.getItemType(Types.BOOK_TYPE_LOCAL_ID);
        if (itemType.getId().equals(typeId)) {
            return new TMBookUpdater(aggregation, adapter, itemType, this, librarianRestClient);
        }

        return null;
    }

    @Override
    protected Collection<String> getUpdateItemTypeIdList() {
        return Arrays.asList(Types.INSTANCE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID,
                Types.NODE_TYPE_LOCAL_ID, Types.SOC_TYPE_LOCAL_ID, Types.MEMORY_BOARD_TYPE_LOCAL_ID,
                Types.SHELF_TYPE_LOCAL_ID, Types.BOOK_TYPE_LOCAL_ID, Types.FABRIC_SWITCH_TYPE_LOCAL_ID,
                Types.MANIFEST_LOCAL_ID, Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID);
    }

    @Override
    protected Collection<String> getCollectionItemTypeIdList() {
        return Arrays.asList(Types.INSTANCE_TYPE_LOCAL_ID, Types.RACK_TYPE_LOCAL_ID, Types.ENCLOSURE_TYPE_LOCAL_ID,
                Types.NODE_TYPE_LOCAL_ID, Types.SOC_TYPE_LOCAL_ID, Types.MEMORY_BOARD_TYPE_LOCAL_ID,
                Types.SHELF_TYPE_LOCAL_ID, Types.BOOK_TYPE_LOCAL_ID, Types.FABRIC_SWITCH_TYPE_LOCAL_ID,
                Types.MANIFEST_LOCAL_ID, Types.INTERLEAVE_GROUP_TYPE_LOCAL_ID);
    }

    @Override
    protected ActionResult doAction(final Action action, final String itemTypeId, final Collection<Item> items) {
        String actionId = action.getId();
        if (actionId.startsWith("Clear") && (actionId.endsWith("alert") || actionId.endsWith("alerts"))) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Clearing alerts for aggreation " + itemTypeId);
            }

            if (items == null) {
                for (Fibre item : this.getAggregation(itemTypeId).getElements()) {
                    ((Item) item).clearAlert();
                }
            } else {
                for (Item item : items) {
                    item.clearAlert();
                }
            }
            return new ActionResult(ActionResult.Status.completed);
        }

        // for now action are only supported on devices
        if (Types.SOC_TYPE_LOCAL_ID.equals(itemTypeId)) {
            return doSocAction(items, action);
        } else if (Types.INSTANCE_TYPE_LOCAL_ID.equals(itemTypeId)) {
            return doInstanceAction(items, action);
        } else if (Types.MANIFEST_LOCAL_ID.equals(itemTypeId)) {
            return doManifestAction(items, action);
        } else {
            LOG.debug("No action supported for typeId " + itemTypeId);
            return new ActionResult(ActionResult.Status.aborted);
        }
    }

    @SuppressWarnings("checkstyle:visibilitymodifier")
    @JsonAutoDetect
    public static class DeployApp {
        public String packageId;

        public DeployApp(final String packageId) {
            this.packageId = packageId;
        }
    }

    @SuppressWarnings("checkstyle:visibilitymodifier")
    @JsonAutoDetect
    public static class PowerAction {
        public final String action;
        public final List<String> params;
        public final String entityType;
        public final String entityCoordinate;

        public PowerAction(final String action, final List<String> params, final String entityType,
                final String entityCoordinate) {
            this.action = action;
            this.params = params;
            this.entityCoordinate = entityCoordinate;
            this.entityType = entityType;
        }
    }

    private ActionResult doManifestAction(final Collection<Item> items, final Action action) {
        String actionId = action.getId();
        boolean anyFailed = false;

        if (actionId.equalsIgnoreCase(Const.ACTION_UPLOAD_OS_MANIFEST)) {
            // prevent null
            String prefix = action.getParams().get(0).getValue();
            String manifestFile = action.getParams().get(1).getValue();
            boolean ok = false;
            try {
                ActionResultFile file = mapper.readValue(manifestFile, ActionResultFile.class);
                Manifest manifest;
                if (file.content instanceof String) {
                    manifest = mapper.readValue((String) file.content, Manifest.class);
                } else {
                    manifest = mapper.readValue(mapper.writeValueAsString(file.content), Manifest.class);
                }
                ok = manifestingRestClient.createManifest(prefix, manifest);
                return ok ? new ActionResult(ActionResult.Status.completed)
                        : new ActionResult(ActionResult.Status.aborted);
            } catch (IOException e) {
                LOG.debug("Failed reading manifest content.");
                return new ActionResult(ActionResult.Status.completed);
            }
        }

        for (Item item : items) {
            if (item instanceof TMOsProvisioningManifest) {
                LOG.debug("Item identified as os manifest");
                TMOsProvisioningManifest manifest = (TMOsProvisioningManifest) item;
                String name = manifest.getName();
                boolean ok = false;

                if (Const.ACTION_DELETE_OS_MANIFEST.equalsIgnoreCase(actionId)) {
                    ok = manifestingRestClient.deleteManifest(name);
                } else if (Const.ACTION_DOWNLOAD_OS_MANIFEST.equalsIgnoreCase(actionId)) {
                    Manifest m = manifestingRestClient.getManifest(name);
                    ActionResult result = new ActionResult(ActionResult.Status.completed);

                    if (m != null) {
                        try {
                            ActionResultFile file = new ActionResultFile();
                            file.filename = name + ".json";
                            file.type = "application/json";
                            file.content = mapper.writeValueAsString(m);
                            result.setFile(file);
                        } catch (JsonProcessingException e) {
                            result.setErrorMessage(e.getMessage());
                        }
                    }

                    return result;
                } else if (Const.ACTION_BIND_OS_MANIFEST_TO_ALL_SOCS.equalsIgnoreCase(actionId)) {
                    List<TMInstance> instances = (List<TMInstance>) (List<?>) this
                            .getAggregation(Types.INSTANCE_TYPE_LOCAL_ID).getElements();
                    ok = dmaAPI.setOsManifestBindingAll(instances, manifest.getName(), DmaAPI.TBD);
                } else {
                    LOG.debug("Invalid OsProvisioningManifest action : " + actionId);
                    return new ActionResult(ActionResult.Status.aborted);
                }

                if (!ok) {
                    anyFailed = true;
                }
            }
        }
        if (anyFailed) {
            return new ActionResult(ActionResult.Status.aborted);
        }
        return new ActionResult(ActionResult.Status.completed);
    }

    /**
     * Perform the action passed as parameter against the SoC elements in the collection.
     *
     * @param items items which will receive the action
     * @param action Action object to be executed
     * @return
     * @throws Exception
     */
    private ActionResult doSocAction(final Collection<Item> items, final Action action) {
        String actionId = action.getId();
        boolean anyFailed = false;
        List<TMSoc> socs = (List<TMSoc>) (List<?>) items;
        if (Const.ACTION_BIND_OS_MANIFEST_TO_INSTANCE.equalsIgnoreCase(actionId)) {
            List<TMInstance> instances =
                    (List<TMInstance>) (List<?>) this.getAggregation(Types.INSTANCE_TYPE_LOCAL_ID).getElements();
            anyFailed = !dmaAPI.setOsManifestBindingAll(instances, action.getParams().get(0).getValue(), DmaAPI.TBD);
        } else if (Const.ACTION_SYNC_OS_MANIFEST_TO_INSTANCE.equalsIgnoreCase(actionId)) {
            List<TMInstance> instances =
                    (List<TMInstance>) (List<?>) this.getAggregation(Types.INSTANCE_TYPE_LOCAL_ID).getElements();
            anyFailed = !dmaAPI.syncOsManifestAll(instances);
        } else if (Const.ACTION_SET_OS_MANIFEST_BINDING.equalsIgnoreCase(actionId)) {
            String manifest = action.getParams().get(0).getValue();
            if (manifest != null) {
                anyFailed = !dmaAPI.setOsManifestBinding(socs, manifest, DmaAPI.TBD);
            }
        } else if (Const.ACTION_CLEAR_OS_MANIFEST_BINDING.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.setOsManifestBinding(socs, "", DmaAPI.TBD);
        } else if (Const.ACTION_SYNC_OS_MANIFEST.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.syncOsManifest(socs);
        } else {
            String actionName = action.getParams().get(0).getValue();
            boolean ok = false;
            if (action.getParams().size() > 0) {
                if (Const.ACTION_POWER_ON.equalsIgnoreCase(actionName)) {
                    ok = dmaAPI.setSocOnDesiredState(socs);
                } else if (Const.ACTION_POWER_OFF.equalsIgnoreCase(actionName)) {
                    ok = dmaAPI.setSocOffDesiredState(socs, false);
                } else if (Const.ACTION_POWER_OFF_FORCE.equalsIgnoreCase(actionName)) {
                    ok = dmaAPI.setSocOffDesiredState(socs, true);
                } else if (Const.ACTION_ASSIGN_TENANT.equalsIgnoreCase(actionName)) {
                    LOG.debug("SoC action not implemented yet : " + actionName);
                    return new ActionResult(ActionResult.Status.aborted);
                } else {
                    LOG.debug("Invalid SoC action name : " + actionName);
                    return new ActionResult(ActionResult.Status.aborted);
                }
                if (!ok) {
                    anyFailed = true;
                }
            }
        }
        if (anyFailed) {
            return new ActionResult(ActionResult.Status.aborted);
        }
        return new ActionResult(ActionResult.Status.completed);
    }

    /**
     * Perform the action passed as parameter against the Instance elements in the collection.
     *
     * @param items items which will receive the action
     * @param action Action object to be executed
     * @return
     * @throws Exception
     */
    private ActionResult doInstanceAction(final Collection<Item> items, final Action action) {
        String actionId = action.getId();
        boolean anyFailed = false;
        String errorMessage = null;

        List<TMInstance> instances = (List<TMInstance>) (List<?>) items;

        if (((TMProvider) provider).userHasLimitedAccess(username)) {
            LOG.warn("User '" + username + "' is a Tenant Admin - denying permission");
            anyFailed = true;
        } else if (Const.ACTION_ALL_POWERED_ON.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.setInstanceAllPoweredOnDesiredState(instances);
        } else if (Const.ACTION_ENABLE_SOC_POWER_ON.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.setInstanceEnableSocPowerOnDesiredState(instances);
        } else if (Const.ACTION_ALL_POWERED_OFF.equalsIgnoreCase(actionId)) {
            ActionParameters actionParams = action.getParams();

            if (actionParams == null || actionParams.isEmpty()) {
                errorMessage = "No power option specified";
                anyFailed = true;
            } else {
                String actionValue = actionParams.get(0).getValue();

                if (actionValue == null) {
                    errorMessage = "No power option specified";
                    anyFailed = true;
                }

                if (!anyFailed) {
                    if (Const.ACTION_ALL_POWERED_OFF.equalsIgnoreCase(actionValue)) {
                        anyFailed = !dmaAPI.setInstanceAllPoweredOffDesiredState(instances, false);
                    } else if (Const.ACTION_ALL_POWERED_OFF_FORCE.equalsIgnoreCase(actionValue)) {
                        anyFailed = !dmaAPI.setInstanceAllPoweredOffDesiredState(instances, true);
                    }
                }
            }
        } else if (Const.ACTION_ONLY_FAM_FABRIC_POWERED_ON.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.setInstanceOnlyFamFabricPoweredOn(instances);
        } else if (Const.ACTION_ONLY_FAM_POWERED_ON.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.setInstanceOnlyFamPoweredOn(instances);
        } else if (Const.ACTION_BIND_OS_MANIFEST_TO_INSTANCE.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.setOsManifestBindingAll(instances, action.getParams().get(0).getValue(), DmaAPI.TBD);
        } else if (Const.ACTION_SYNC_OS_MANIFEST_TO_INSTANCE.equalsIgnoreCase(actionId)) {
            anyFailed = !dmaAPI.syncOsManifestAll(instances);
        } else if (Const.ACTION_PREPARE_FOR_SYSTEM_BOOT.equalsIgnoreCase(actionId)) {
            anyFailed =
                    !dmaAPI.setInstancePrepareSystemBootDesiredState(this, instances, defaultCsFile, defaultUdsFile);
        } else if (Const.ACTION_SET_AA_MODE.equalsIgnoreCase(actionId)) {
            ActionParameters actionParams = action.getParams();

            if (actionParams == null || actionParams.isEmpty()) {
                errorMessage = "No AA mode specified";
                anyFailed = true;
            } else {
                String actionValue = actionParams.get(0).getValue();

                if (actionValue == null) {
                    errorMessage = "No AA mode specified";
                    anyFailed = true;
                }

                if (!anyFailed) {
                    if (Const.ACTION_PAUSE.equalsIgnoreCase(actionValue)) {
                        anyFailed = !dmaAPI.setInstancePausedMode();
                    } else if (Const.ACTION_UNPAUSE.equalsIgnoreCase(actionValue)) {
                        GetDmaStatus status = (GetDmaStatus) dmaCommonUpdater.getStatusData();
                        anyFailed = !dmaAPI.setInstanceUnpausedMode(instances, status.componentStatus.stateMonitor);
                    } else if (Const.ACTION_SET_DEBUG.equalsIgnoreCase(actionValue)) {
                        anyFailed = !dmaAPI.setInstanceDebugMode();
                    } else if (Const.ACTION_UNSET_DEBUG.equalsIgnoreCase(actionValue)) {
                        anyFailed = !dmaAPI.unsetInstanceDebugMode();
                    } else {
                        errorMessage = "Invalid AA Mode action name : " + actionValue;
                        anyFailed = true;
                    }
                }
            }
        } else if (Const.ACTION_SET_MEMORY_TEST.equalsIgnoreCase(actionId)) {
            String actionName = action.getParams().get(0).getValue();
            if (Const.ACTION_ENABLE_MEMTEST.equalsIgnoreCase(actionName)) {
                anyFailed = !dmaAPI.setMemtest(true);
            } else if (Const.ACTION_DISABLE_MEMTEST.equalsIgnoreCase(actionName)) {
                anyFailed = !dmaAPI.setMemtest(false);
            } else {
                anyFailed = true;
            }
        }

        ActionResult result = null;

        if (anyFailed) {
            result = new ActionResult(ActionResult.Status.aborted);

            result.setErrorMessage(errorMessage);
            LOG.warn(errorMessage);
        } else {
            result = new ActionResult(ActionResult.Status.completed);
        }

        return result;
    }

    public boolean addListener(final ITMItemCollectorListener listener) {
        LOG.info("Add listener");
        return getListeners().add(listener);
    }

    public boolean addListenerWithTimeout(final ITMItemCollectorListener listener, final long timeout) {
        LOG.info("Add listener with timout = " + timeout);
        TMItemCollector notifier = this;
        if (getListeners().add(listener)) {
            Thread timeoutThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        LOG.warn("Sleep interrupted prematurely");
                    } finally {
                        if (getListeners().remove(listener)) {
                            listener.onDmaListenerTimeoutExpired(notifier);
                        }
                    }
                }
            };
            timeoutThread.start();
            return true;
        }
        return false;
    }

    public boolean removeListener(final ITMItemCollectorListener listener) {
        LOG.info("Remove listener");
        return getListeners().remove(listener);
    }

    public boolean notifyListeners(final String eventName) {
        boolean ret = false;
        LOG.info("Notifying listeners of event : " + eventName);
        List<ITMItemCollectorListener> originalListeners = new ArrayList<ITMItemCollectorListener>();
        originalListeners.addAll(getListeners());
        for (ITMItemCollectorListener listener : originalListeners) {
            listener.onDmaEvent(this, eventName);
            ret = true;
        }
        return ret;
    }

    public List<ITMItemCollectorListener> getListeners() {
        return listeners;
    }

    public void setListeners(final List<ITMItemCollectorListener> listeners) {
        this.listeners = listeners;
    }
}
