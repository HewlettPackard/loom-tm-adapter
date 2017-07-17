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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hp.hpl.loom.adapter.tm.ITMItemCollectorListener;
import com.hp.hpl.loom.adapter.tm.TMItemCollector;
import com.hp.hpl.loom.adapter.tm.backend.RestClient.RestTemplateWithJsonAndString;
import com.hp.hpl.loom.adapter.tm.backend.aa.DmaAPI.PrepareSystemBootListener;
import com.hp.hpl.loom.adapter.tm.backend.aa.prototypes.PowerPrototype;
import com.hp.hpl.loom.adapter.tm.backend.aa.status.Component;
import com.hp.hpl.loom.adapter.tm.items.TMInstance;
import com.hp.hpl.loom.adapter.tm.items.TMRack;
import com.hp.hpl.loom.adapter.tm.items.TMSoc;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TMInstance.class)
public class TestDmaAPI {

    // Testing target object
    DmaAPI api;

    // Mocks
    private RestTemplateWithJsonAndString mockRest;
    private MockModel model;

    // Constants
    private static final String dmaUrl = "dmaUrl";
    private static final String rackPath = "/main/rack1";
    private static final String soc11Path = "/main/rack1/enclosure1.node1.soc_board.soc";
    private static final String soc12Path = "/main/rack1/enclosure1.node2.soc_board.soc";
    private static final String soc21Path = "/main/rack1/enclosure2.node1.soc_board.soc";
    private static final String soc22Path = "/main/rack1/enclosure2.node2.soc_board.soc";
    private static final String socPrototypeLocalPowerPath = "/node/socBoard/soc/localPower";

    private static final String mainEnableNonGracefulShutdown = "/main/enableNonGracefulShutdown";
    private static final String force_all_fabric_soc_off = "/forceAllFabricSocOff";
    private static final String force_all_fam_fabric_soc_off = "/forceAllFamFabricSocOff";
    private static final String force_all_soc_off = "/forceAllSocOff";
    private static final String power = "/power";
    private static final String default_running_os_image_manifest = "/defaultRunningOsImageManifest";
    private static final String default_next_os_image_manifest = "/defaultNextOsImageManifest";
    private static final String local_power = ".localPower";
    private static final String runningOsImageManifest = ".runningOsImageManifest";
    private static final String nextOsImageManifest = ".nextOsImage.manifest";
    private static final String enableNonGracefulShutdown = ".enableNonGracefulShutdown";

    public static final String pausedPath = "/service/paused?paused=true";
    public static final String unpausedPath = "/service/paused?paused=false";
    public static final String debugPath = "/service/debug?debug=true";
    public static final String noDebugPath = "/service/debug?debug=false";
    public static final String memtestPath = "/node/memoryBoard/mediaControllers/powerOnOptions";

    public static final String modeNormal = "NORMAL";
    public static final String modePaused = "PAUSED";
    public static final String modeDebug = "DEBUG";
    public static final String modeDebugPaused = "DEBUG & PAUSED";

    public static final String steady = "steady";

    private static final String valFalse = "false";
    private static final String valTrue = "true";
    private static final String valTBD = "{\"value\":\"" + DmaAPI.TBD + "\",\"type\":\"string\"}";
    private static final String memtestYes = "{\"MemTest\":\"Yes\"}";
    private static final String memtestNo = "{\"MemTest\":\"No\"}";

    private static final String testingCsFile = "src/test/resources/cs_v051.json";
    private static final String invalidCsFile = "src/test/resources/cs_v051_invalid.json";
    private static final String testingUdsFile = "src/test/resources/uds_v051.json";
    private static final String invalidUdsFile = "src/test/resources/uds_v051_invalid.json";

    private static JsonNode jValTrue;
    private static JsonNode jValFalse;
    private static JsonNode jValPowerOn;
    private static JsonNode jValPowerOff;
    private static JsonNode jValTbd;

    private static JsonNode jExpectedCs;
    private static String expectedCs;
    private static JsonNode jExpectedUds;
    private static String expectedUds;

    @BeforeClass
    public static void beforeClass() throws NoSuchItemTypeException, JsonProcessingException, IOException {
        expectedUds = readFile(testingUdsFile);
        expectedCs = readFile(testingCsFile);
        ObjectMapper mapper = new ObjectMapper();
        jExpectedUds = mapper.readTree(expectedUds);
        jExpectedCs = mapper.readTree(expectedCs);
    }

    @Before
    public void before() throws NoSuchItemTypeException, JsonProcessingException, IOException {
        initJsonValues();
        api = new DmaAPI(dmaUrl);
        mockRestClient();
        model = new MockModel();
    }

    @After
    public void after() {}

    private static void initJsonValues() throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        jValTrue = mapper.readTree("{\"value\": true}");
        jValFalse = mapper.readTree("{\"value\": false}");
        jValPowerOn = mapper.readTree("{\"value\": \"" + DmaAPI.POWER_ON + "\"}");
        jValPowerOff = mapper.readTree("{\"value\": \"" + DmaAPI.POWER_OFF + "\"}");
        jValTbd = mapper.readTree("{\"value\": {\"value\": \"" + DmaAPI.TBD + "\", \"type\": \"string\"}}");
    }

    private void mockRestClientGetPowerPrototype(String powerPrototypeValue) {
        PowerPrototype body = PowerMockito.mock(PowerPrototype.class);
        body.value = powerPrototypeValue;
        ResponseEntity<?> getResponse = PowerMockito.mock(ResponseEntity.class);
        PowerMockito.doReturn(HttpStatus.OK).when(getResponse).getStatusCode();
        PowerMockito.doReturn(body).when(getResponse).getBody();
        PowerMockito.doReturn(getResponse).when(mockRest)
                .get(eq(dmaUrl + TMItemCollector.DMA_DESIRED_ROUTE + socPrototypeLocalPowerPath), any());
    }

    private void mockPutOk(String url) {
        ResponseEntity<?> postResponse = PowerMockito.mock(ResponseEntity.class);
        PowerMockito.doReturn(HttpStatus.OK).when(postResponse).getStatusCode();
        PowerMockito.doReturn(postResponse).when(mockRest).putForEntity(eq(url), any(), any());
    }

    private void mockPutError(String url) {
        ResponseEntity<?> postResponse = PowerMockito.mock(ResponseEntity.class);
        PowerMockito.doReturn(HttpStatus.INTERNAL_SERVER_ERROR).when(postResponse).getStatusCode();
        PowerMockito.doReturn(postResponse).when(mockRest).putForEntity(eq(url), any(), any());
    }

    private void mockRestClient() {
        mockRest = PowerMockito.mock(RestTemplateWithJsonAndString.class);
        PowerMockito.doReturn(new ResponseEntity<String>(HttpStatus.OK)).when(mockRest)
                .patchForEntity(eq(dmaUrl + TMItemCollector.DMA_DESIRED_ROUTE), any(ArrayNode.class), eq(null));
        PowerMockito.doReturn(new ResponseEntity<String>(HttpStatus.OK)).when(mockRest)
                .putForEntity(eq(dmaUrl + TMItemCollector.DMA_DESIRED_ROUTE + "/"), any(JsonNode.class), eq(null));
        PowerMockito.doReturn(new ResponseEntity<String>(HttpStatus.OK)).when(mockRest)
                .putForEntity(eq(dmaUrl + TMItemCollector.DMA_CURRENT_ROUTE + "/"), any(JsonNode.class), eq(null));
        api.restClient = mockRest;
    }

    private Component getMockStateMonitor(String phase) {
        Component smon = PowerMockito.mock(Component.class);
        smon.phase = phase;
        return smon;
    }

    private TMItemCollector getMockTMItemCollector() {
        List<ITMItemCollectorListener> listeners = new ArrayList<ITMItemCollectorListener>();
        TMItemCollector collector = PowerMockito.mock(TMItemCollector.class);
        collector.setListeners(listeners);
        return collector;
    }

    private static String readFile(String filename) throws IOException {
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

    @Test
    public void testGetValueJsonNodeTbd() throws JsonProcessingException, IOException {
        JsonNode tdbNode = api.getValueJsonNode(DmaAPI.TBD);
        assertEquals(jValTbd, tdbNode);
    }

    @Test
    public void testGetValueJsonNodePowerOn() throws JsonProcessingException, IOException {
        JsonNode powerOnNode = api.getValueJsonNode(DmaAPI.POWER_ON);
        assertEquals(jValPowerOn, powerOnNode);
    }

    @Test
    public void testGetValueJsonNodePowerOff() throws JsonProcessingException, IOException {
        JsonNode powerOffNode = api.getValueJsonNode(DmaAPI.POWER_OFF);
        assertEquals(jValPowerOff, powerOffNode);
    }

    @Test
    public void testGetValueJsonNodeOther() throws JsonProcessingException, IOException {
        JsonNode otherValueNode = api.getValueJsonNode("otherValue");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedNode = mapper.readTree("{\"value\": \"otherValue\"}");
        assertEquals(expectedNode, otherValueNode);
    }

    @Test
    public void testGetValueJsonNodeTrue() throws JsonProcessingException, IOException {
        JsonNode trueNode = api.getValueJsonNode(true);
        assertEquals(jValTrue, trueNode);
    }

    @Test
    public void testGetValueJsonNodeFalse() throws JsonProcessingException, IOException {
        JsonNode trueNode = api.getValueJsonNode(false);
        assertEquals(jValFalse, trueNode);
    }

    @Test
    public void testSetPathAndValueDesiredState() throws JsonProcessingException, IOException {
        boolean ok = api.setPathAndValueDesiredState(jValTrue, rackPath + force_all_fabric_soc_off);
        assertTrue(ok);
    }

    @Test
    public void testSetPathAndValueDesiredStateNullValue() throws JsonProcessingException, IOException {
        boolean ok = api.setPathAndValueDesiredState(null, socPrototypeLocalPowerPath);
        assertFalse(ok);
    }

    @Test
    public void testSetPathAndValueDesiredStateNullPath() throws JsonProcessingException, IOException {
        boolean ok = api.setPathAndValueDesiredState(jValTrue, null);
        assertFalse(ok);
    }

    @Test
    public void testAreAllSocsOnProtoOnAllOverrides() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertTrue(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnProtoOnNoOverrides() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertTrue(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnProtoOffAllOverrides() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_OFF);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertTrue(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnProtoOffNoOverrides() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_OFF);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertFalse(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnRackOff() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("off");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertFalse(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnForceFamTrue() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(true);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertFalse(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnForceFabricTrue() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(true);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertFalse(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnForceSocTrue() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(true);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertFalse(api.areAllSocsOn(rack));
    }

    @Test
    public void testAreAllSocsOnProtoOnOneSocOff() {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("off");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.createModel();
        TMRack rack = model.getAllRacks().get(0);
        assertFalse(api.areAllSocsOn(rack));
    }

    @Test
    public void testSetInstanceAllPoweredOn() throws JsonProcessingException, IOException {
        api.setInstanceAllPoweredOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("remove", soc11Path + local_power), Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc21Path + local_power), Arrays.asList("remove", soc22Path + local_power)));
    }

    @Test
    public void testSetInstanceAllPoweredOnWithManifest() throws JsonProcessingException, IOException {
        model.setDefaultNextOsManifest("defaultNextManifest");
        api.setInstanceAllPoweredOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("remove", soc11Path + local_power), Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc21Path + local_power), Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, "defaultNextManifest")));
    }

    @Test
    public void testSetInstanceAllPoweredOnWithManifestEmpty() throws JsonProcessingException, IOException {
        model.setDefaultNextOsManifest("");
        api.setInstanceAllPoweredOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("remove", soc11Path + local_power), Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc21Path + local_power), Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, "")));
    }

    @Test
    public void testSetInstanceAllPoweredOnWithManifestOverride() throws JsonProcessingException, IOException {
        model.setNextOsImageManifest12("Soc12NextManifest");
        model.setNextOsImageManifest22("");
        api.setInstanceAllPoweredOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("remove", soc11Path + local_power), Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc21Path + local_power), Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("add", soc12Path + runningOsImageManifest, "Soc12NextManifest"),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "")));
    }

    @Test
    public void testSetInstanceAllPoweredOnWithManifestOverrideAndDefault()
            throws JsonProcessingException, IOException {
        model.setNextOsImageManifest12("");
        model.setNextOsImageManifest22("Soc22NextManifest");
        model.setDefaultNextOsManifest("defaultNextManifest");
        api.setInstanceAllPoweredOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("remove", soc11Path + local_power), Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc21Path + local_power), Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, "defaultNextManifest"),
                Arrays.asList("add", soc12Path + runningOsImageManifest, ""),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "Soc22NextManifest")));
    }

    @Test
    public void testEnableSocPowerOn() throws JsonProcessingException, IOException {
        api.setInstanceEnableSocPowerOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD)));
    }

    @Test
    public void testEnableSocPowerOnNodeManifestOverride() throws JsonProcessingException, IOException {
        model.setNextOsImageManifest11("");
        model.setNextOsImageManifest22("Soc22NextManifest");
        api.setInstanceEnableSocPowerOnDesiredState(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc22Path + runningOsImageManifest, valTBD)));
    }

    @Test
    public void testSetInstanceAllPoweredOff() throws JsonProcessingException, IOException {
        api.setInstanceAllPoweredOffDesiredState(model.createModel(), false);
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetInstanceAllPoweredOffSocOverrides() throws JsonProcessingException, IOException {
        model.setLocalPowerSoc12("");
        model.setLocalPowerSoc21("Soc21LocalPower");
        model.setRunningOsImageManifest11("");
        model.setRunningOsImageManifest22("Soc22RunningManifest");
        model.setEnableNonGracefulShutdown11(true);
        model.setEnableNonGracefulShutdown12(false);
        api.setInstanceAllPoweredOffDesiredState(model.createModel(), false);
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetInstanceAllPoweredOffForce() throws JsonProcessingException, IOException {
        api.setInstanceAllPoweredOffDesiredState(model.createModel(), true);
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valFalse),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valTrue),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetInstanceOnlyFamFabricPoweredOn() throws JsonProcessingException, IOException {
        api.setInstanceOnlyFamFabricPoweredOn(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valTrue),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetInstanceOnlyFamFabricPoweredOnSocOverrides() throws JsonProcessingException, IOException {
        model.setLocalPowerSoc12("");
        model.setLocalPowerSoc21("Soc21LocalPower");
        model.setRunningOsImageManifest11("");
        model.setRunningOsImageManifest22("Soc22RunningManifest");
        model.setEnableNonGracefulShutdown11(true);
        model.setEnableNonGracefulShutdown12(false);
        api.setInstanceOnlyFamFabricPoweredOn(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valTrue),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetInstanceOnlyFamPoweredOn() throws JsonProcessingException, IOException {
        api.setInstanceOnlyFamPoweredOn(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valTrue),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valTrue),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetInstanceOnlyFamPoweredOnSocOverride() throws JsonProcessingException, IOException {
        model.setLocalPowerSoc12("");
        model.setLocalPowerSoc21("Soc21LocalPower");
        model.setRunningOsImageManifest11("");
        model.setRunningOsImageManifest22("Soc22RunningManifest");
        model.setEnableNonGracefulShutdown11(true);
        model.setEnableNonGracefulShutdown12(false);
        api.setInstanceOnlyFamPoweredOn(model.createModel());
        validatePatch(Arrays.asList(Arrays.asList("add", socPrototypeLocalPowerPath, DmaAPI.POWER_OFF),
                Arrays.asList("add", rackPath + power, DmaAPI.POWER_ON),
                Arrays.asList("add", rackPath + force_all_fabric_soc_off, valTrue),
                Arrays.asList("add", rackPath + force_all_fam_fabric_soc_off, valFalse),
                Arrays.asList("add", rackPath + force_all_soc_off, valTrue),
                Arrays.asList("add", mainEnableNonGracefulShutdown, valFalse),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, valTBD),
                Arrays.asList("remove", soc11Path + local_power),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc12Path + local_power),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc21Path + local_power),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + enableNonGracefulShutdown),
                Arrays.asList("remove", soc22Path + local_power),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown)));
    }

    @Test
    public void testSetSocOnDesiredState() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + local_power, DmaAPI.POWER_ON),
                Arrays.asList("add", soc11Path + runningOsImageManifest, "myManifest11"),
                Arrays.asList("add", soc22Path + local_power, DmaAPI.POWER_ON),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "myManifest22")));
    }

    @Test
    public void testSetSocOnDesiredStateWrongRackPower() throws JsonProcessingException, IOException {
        model.setRackPower("off");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateWrongForceFamFabricSocOff() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(true);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateWrongForceFabricSocOff() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(true);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateWrongForceSocOff() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(true);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateWrongLocalPowerNode11() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("off");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc22Path + local_power, DmaAPI.POWER_ON),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "myManifest22")));
    }

    @Test
    public void testSetSocOnDesiredStateWrongLocalPowerBothNodes() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("off");
        model.setLocalPowerNode22("off");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateWrongLocalPowerEnclosure2() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("off");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + local_power, DmaAPI.POWER_ON),
                Arrays.asList("add", soc11Path + runningOsImageManifest, "myManifest11")));
    }

    @Test
    public void testSetSocOnDesiredStateWrongLocalPowerBothEnclosures() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("off");
        model.setLocalPowerEnclosure2("off");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateMissingParam() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("off");
        model.setLocalPowerEnclosure2("off");
        model.setNextOsImageManifest11("myManifest11");
        model.setNextOsImageManifest22("myManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOnDesiredStateUseNoManifestOverrideNoDefault() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + local_power, DmaAPI.POWER_ON),
                Arrays.asList("add", soc22Path + local_power, DmaAPI.POWER_ON)));
    }

    @Test
    public void testSetSocOnDesiredStateUseNoManifestOverrideWithDefault() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setDefaultNextOsManifest("defaultNextManifest");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + local_power, DmaAPI.POWER_ON),
                Arrays.asList("add", soc22Path + local_power, DmaAPI.POWER_ON)));
    }

    @Test
    public void testSetSocOnDesiredStateNothing() throws JsonProcessingException, IOException {
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOnDesiredState(socs);
        assertPatchNotCalled();
    }

    @Test
    public void testSetSocOffDesiredState() throws JsonProcessingException, IOException {
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOffDesiredState(socs, false);
        validatePatch(Arrays.asList(Arrays.asList("remove", soc11Path + enableNonGracefulShutdown),
                Arrays.asList("add", soc11Path + local_power, DmaAPI.POWER_OFF),
                Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("remove", soc22Path + enableNonGracefulShutdown),
                Arrays.asList("add", soc22Path + local_power, DmaAPI.POWER_OFF),
                Arrays.asList("add", soc22Path + runningOsImageManifest, valTBD)));
    }

    @Test
    public void testSetSocOffDesiredStateForce() throws JsonProcessingException, IOException {
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setSocOffDesiredState(socs, true);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + enableNonGracefulShutdown, valTrue),
                Arrays.asList("add", soc11Path + local_power, DmaAPI.POWER_OFF),
                Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc22Path + enableNonGracefulShutdown, valTrue),
                Arrays.asList("add", soc22Path + local_power, DmaAPI.POWER_OFF),
                Arrays.asList("add", soc22Path + runningOsImageManifest, valTBD)));
    }

    @Test
    public void testSetOsManifestBinding() throws JsonProcessingException, IOException {
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setOsManifestBinding(socs, "nextOsImage", DmaAPI.TBD);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + nextOsImageManifest, "nextOsImage"),
                Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc22Path + nextOsImageManifest, "nextOsImage"),
                Arrays.asList("add", soc22Path + runningOsImageManifest, valTBD)));
    }

    @Test
    public void testClearOsManifestBinding() throws JsonProcessingException, IOException {
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.setOsManifestBinding(socs, "", DmaAPI.TBD);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + nextOsImageManifest, ""),
                Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc22Path + nextOsImageManifest, ""),
                Arrays.asList("add", soc22Path + runningOsImageManifest, valTBD)));
    }

    @Test
    public void testSetOsManifestBindingAll() throws JsonProcessingException, IOException {
        List<TMInstance> instances = model.createModel();
        api.setOsManifestBindingAll(instances, "nextOsImage", "runningOsImage");
        validatePatch(Arrays.asList(Arrays.asList("remove", soc11Path + nextOsImageManifest),
                Arrays.asList("remove", soc11Path + runningOsImageManifest),
                Arrays.asList("remove", soc12Path + nextOsImageManifest),
                Arrays.asList("remove", soc12Path + runningOsImageManifest),
                Arrays.asList("remove", soc21Path + nextOsImageManifest),
                Arrays.asList("remove", soc21Path + runningOsImageManifest),
                Arrays.asList("remove", soc22Path + nextOsImageManifest),
                Arrays.asList("remove", soc22Path + runningOsImageManifest),
                Arrays.asList("add", rackPath + default_next_os_image_manifest, "nextOsImage"),
                Arrays.asList("add", rackPath + default_running_os_image_manifest, "runningOsImage")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelValueAllSocOn() throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setDefaultNextOsManifest("defNextManifest");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(
                Arrays.asList(Arrays.asList("add", rackPath + default_running_os_image_manifest, "defNextManifest")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelValueAllSocOnManifestOverrides()
            throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setDefaultNextOsManifest("defNextManifest");
        model.setNextOsImageManifest11("nextOsImageManifest11");
        model.setNextOsImageManifest12("nextOsImageManifest12");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(
                Arrays.asList(Arrays.asList("add", rackPath + default_running_os_image_manifest, "defNextManifest"),
                        Arrays.asList("add", soc11Path + runningOsImageManifest, "nextOsImageManifest11")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelEmptyValueAllSocOn() throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setDefaultNextOsManifest("");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", rackPath + default_running_os_image_manifest, "")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelValueSomeSocsOff() throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc22("off");
        model.setDefaultNextOsManifest("defNextManifest");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + runningOsImageManifest, "defNextManifest")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelValueSomeSocsOffNotSelected()
            throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("off");
        model.setLocalPowerSoc22("on");
        model.setDefaultNextOsManifest("defNextManifest");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + runningOsImageManifest, "defNextManifest"),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "defNextManifest")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelValueSomeSocsManifestOverride()
            throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("off");
        model.setLocalPowerSoc22("on");
        model.setDefaultNextOsManifest("defNextManifest");
        model.setNextOsImageManifest11("nextManifest11");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + runningOsImageManifest, "nextManifest11"),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "defNextManifest")));
    }

    @Test
    public void testSyncOsManifestBindingTopLevelTbdAllOn() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.setDefaultNextOsManifest(DmaAPI.TBD);
        model.setNextOsImageManifest22("nextManifest22");
        model.createModel();
        List<TMSoc> socs =
                model.getSocsByCoordinates(Arrays.asList(MockModel.soc11Coordinate, MockModel.soc22Coordinate));
        api.syncOsManifest(socs);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc22Path + runningOsImageManifest, "nextManifest22")));
    }

    @Test
    public void testSyncOsManifestAll() throws JsonProcessingException, IOException {
        mockRestClientGetPowerPrototype(DmaAPI.POWER_ON);
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setDefaultNextOsManifest("defNextManifest");
        model.setNextOsImageManifest12("nextManifest12");
        List<TMInstance> instances = model.createModel();
        api.syncOsManifestAll(instances);
        validatePatch(
                Arrays.asList(Arrays.asList("add", rackPath + default_running_os_image_manifest, "defNextManifest"),
                        Arrays.asList("add", soc12Path + runningOsImageManifest, "nextManifest12")));
    }

    @Test
    public void testSyncOsManifestAllTopLevelTbd() throws JsonProcessingException, IOException {
        model.setRackPower("on");
        model.setRackDesiredForceAllFamFabricSocOff(false);
        model.setRackDesiredForceAllFabricSocOff(false);
        model.setRackDesiredForceAllSocOff(false);
        model.setLocalPowerEnclosure1("on");
        model.setLocalPowerEnclosure2("on");
        model.setLocalPowerNode11("on");
        model.setLocalPowerNode12("on");
        model.setLocalPowerNode21("on");
        model.setLocalPowerNode22("on");
        model.setLocalPowerSoc11("on");
        model.setLocalPowerSoc12("on");
        model.setLocalPowerSoc21("on");
        model.setLocalPowerSoc22("on");
        model.setDefaultNextOsManifest(DmaAPI.TBD);
        model.setNextOsImageManifest21("nextManifest21");
        List<TMInstance> instances = model.createModel();
        api.syncOsManifestAll(instances);
        validatePatch(Arrays.asList(Arrays.asList("add", soc11Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc12Path + runningOsImageManifest, valTBD),
                Arrays.asList("add", soc21Path + runningOsImageManifest, "nextManifest21"),
                Arrays.asList("add", soc22Path + runningOsImageManifest, valTBD)));
    }

    @Test
    public void testPrepareSystemBootDesiredStatePaused() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertTrue(api.setInstancePrepareSystemBootDesiredState(collector, instances, testingCsFile, testingUdsFile));
        validatePutStates(jExpectedUds, jExpectedCs);
    }

    @Test
    public void testPrepareSystemBootDesiredStateDebugPaused() throws IOException {
        model.setAaMode(modeDebugPaused);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertTrue(api.setInstancePrepareSystemBootDesiredState(collector, instances, testingCsFile, testingUdsFile));
        validatePutStates(jExpectedUds, jExpectedCs);
    }

    @Test
    public void testPrepareSystemBootDesiredStateInvalidUds() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertFalse(api.setInstancePrepareSystemBootDesiredState(collector, instances, testingCsFile, invalidUdsFile));
        assertPutNotCalled();
    }

    @Test
    public void testPrepareSystemBootDesiredStateInexistentUds() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertFalse(api.setInstancePrepareSystemBootDesiredState(collector, instances, testingCsFile, "InexistentUds"));
        assertPutNotCalled();
    }

    @Test
    public void testPrepareSystemBootDesiredStateInvalidCs() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertFalse(api.setInstancePrepareSystemBootDesiredState(collector, instances, invalidCsFile, testingUdsFile));
        assertPutNotCalled();
    }

    @Test
    public void testPrepareSystemBootDesiredStateInexistentCs() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertFalse(api.setInstancePrepareSystemBootDesiredState(collector, instances, "InexistentCs", testingUdsFile));
        assertPutNotCalled();
    }

    @Test
    public void testPrepareSystemBootDesiredStateNormal() throws IOException {
        model.setAaMode(modeNormal);
        mockPutError(dmaUrl + pausedPath);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertTrue(api.setInstancePrepareSystemBootDesiredState(collector, instances, testingCsFile, testingUdsFile));
        validatePutUrl(dmaUrl + pausedPath);
        validateAddListenerWithTimeout(collector, DmaAPI.PAUSE_WAIT_TIMEOUT);

    }

    @Test
    public void testPrepareSystemBootDesiredStateDebug() throws IOException {
        model.setAaMode(modeDebug);
        mockPutError(dmaUrl + pausedPath);
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        assertTrue(api.setInstancePrepareSystemBootDesiredState(collector, instances, testingCsFile, testingUdsFile));
        validatePutUrl(dmaUrl + pausedPath);
        validateAddListenerWithTimeout(collector, DmaAPI.PAUSE_WAIT_TIMEOUT);
    }

    @Test
    public void testEnableMemtest() throws JsonProcessingException, IOException {
        assertTrue(api.setMemtest(true));
        validatePatch(Arrays.asList(Arrays.asList("add", memtestPath, memtestYes)));
    }

    @Test
    public void testDisableMemtest() throws JsonProcessingException, IOException {
        assertTrue(api.setMemtest(false));
        validatePatch(Arrays.asList(Arrays.asList("add", memtestPath, memtestNo)));
    }

    @Test
    public void testListenerOnPaused() {
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        PrepareSystemBootListener listener = api.createSystemBootListener(instances, testingCsFile, testingUdsFile);
        listener.onDmaEvent(collector, modePaused);
        validateRemoveListener(collector);
        validatePutStates(jExpectedUds, jExpectedCs);
    }

    @Test
    public void testListenerOnOtherEvent() {
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        PrepareSystemBootListener listener = api.createSystemBootListener(instances, testingCsFile, testingUdsFile);
        listener.onDmaEvent(collector, "any other event");
        assertPutNotCalled();
    }

    @Test
    public void testListenerOnTimeoutExpired() {
        List<TMInstance> instances = model.createModel();
        TMItemCollector collector = getMockTMItemCollector();
        PrepareSystemBootListener listener = api.createSystemBootListener(instances, testingCsFile, testingUdsFile);
        listener.onDmaListenerTimeoutExpired(collector);
        assertPutNotCalled();
        for (TMInstance instance : instances) {
            validateInstanceAlert(instance,
                    "Could not prepare for system boot, because the Assembly agent did not pause."
                            + " Please pause it manually and try again");
        }
    }

    @Test
    public void testPause() {
        mockPutOk(dmaUrl + pausedPath);
        assertTrue(api.setInstancePausedMode());
        validatePutUrl(dmaUrl + pausedPath);
    }

    @Test
    public void testPauseError() {
        mockPutError(dmaUrl + pausedPath);
        assertFalse(api.setInstancePausedMode());
        validatePutUrl(dmaUrl + pausedPath);
    }

    @Test
    public void testUnpause() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        Component smon = getMockStateMonitor(steady);
        mockPutOk(dmaUrl + unpausedPath);
        assertTrue(api.setInstanceUnpausedMode(instances, smon));
        validatePutUrl(dmaUrl + unpausedPath);
    }

    @Test
    public void testUnpauseError() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        Component smon = getMockStateMonitor(steady);
        mockPutError(dmaUrl + unpausedPath);
        assertFalse(api.setInstanceUnpausedMode(instances, smon));
        validatePutUrl(dmaUrl + unpausedPath);
    }

    @Test
    public void testUnpauseStateMonitorNoSteady() throws IOException {
        model.setAaMode(modePaused);
        List<TMInstance> instances = model.createModel();
        Component smon = getMockStateMonitor("any other phase");
        mockPutOk(dmaUrl + unpausedPath);
        assertTrue(api.setInstanceUnpausedMode(instances, smon));
        validatePutUrl(dmaUrl + unpausedPath);
    }

    @Test
    public void testDebug() throws IOException {
        mockPutOk(dmaUrl + debugPath);
        assertTrue(api.setInstanceDebugMode());
        validatePutUrl(dmaUrl + debugPath);
    }

    @Test
    public void testDebugError() throws IOException {
        mockPutError(dmaUrl + debugPath);
        assertFalse(api.setInstanceDebugMode());
        validatePutUrl(dmaUrl + debugPath);
    }

    @Test
    public void testNoDebug() throws IOException {
        mockPutOk(dmaUrl + noDebugPath);
        assertTrue(api.unsetInstanceDebugMode());
        validatePutUrl(dmaUrl + noDebugPath);
    }

    @Test
    public void testNoDebugError() throws IOException {
        mockPutError(dmaUrl + noDebugPath);
        assertFalse(api.unsetInstanceDebugMode());
        validatePutUrl(dmaUrl + noDebugPath);
    }

    private void assertPatchNotCalled() {
        Mockito.verify(mockRest, Mockito.times(0)).patchForEntity(any(String.class), any(ArrayNode.class), eq(null));
    }

    private void validatePatch(List<List<String>> operations) throws JsonProcessingException, IOException {
        ArrayList<JsonNode> expectedOps = new ArrayList<JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        for (List<String> op : operations) {
            if (op.get(0).equals("add")) {
                if (op.get(2).equals(valTrue) || op.get(2).equals(valFalse) || op.get(2).equals(valTBD)
                        || op.get(2).equals(memtestYes) || op.get(2).equals(memtestNo)) {
                    expectedOps.add(mapper.readTree(
                            "{\"value\": " + op.get(2) + ", \"path\": \"" + op.get(1) + "\", \"op\": \"add\"}"));
                } else {
                    expectedOps.add(mapper.readTree(
                            "{\"value\": \"" + op.get(2) + "\", \"path\": \"" + op.get(1) + "\", \"op\": \"add\"}"));
                }
            } else if (op.get(0).equals("remove")) {
                expectedOps.add(mapper.readTree("{\"op\": \"remove\", \"path\": \"" + op.get(1) + "\" }"));
            } else
                fail("invalid asserted operation : " + op.get(0));
        }
        ArrayNode expectedJArray = mapper.createArrayNode();
        for (JsonNode op : expectedOps) {
            expectedJArray.add(op);
        }
        Mockito.verify(mockRest, Mockito.times(1)).patchForEntity(dmaUrl + TMItemCollector.DMA_DESIRED_ROUTE,
                expectedJArray, null);
    }

    private void validatePutStates(JsonNode expectedDesired, JsonNode expectedCurrent) {
        Mockito.verify(mockRest, Mockito.times(1)).putForEntity(dmaUrl + TMItemCollector.DMA_DESIRED_ROUTE + "/",
                expectedDesired, null);
        Mockito.verify(mockRest, Mockito.times(1)).putForEntity(dmaUrl + TMItemCollector.DMA_CURRENT_ROUTE + "/",
                expectedCurrent, null);
    }

    private void validatePutUrl(String url) {
        Mockito.verify(mockRest, Mockito.times(1)).putForEntity(url, null, null);
    }

    private void assertPutNotCalled() {
        Mockito.verify(mockRest, Mockito.times(0)).putForEntity(any(String.class), any(JsonNode.class), eq(null));
    }

    private void validateAddListenerWithTimeout(TMItemCollector collector, long timeout) {
        Mockito.verify(collector, Mockito.times(1)).addListenerWithTimeout(any(), eq(timeout));
    }

    private void validateRemoveListener(TMItemCollector collector) {
        Mockito.verify(collector, Mockito.times(1)).removeListener(any());
    }

    private void validateInstanceAlert(TMInstance instance, String msg) {
        Mockito.verify(instance, Mockito.times(1)).displayAlert(eq(msg));
    }

}
