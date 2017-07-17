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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;
import com.hp.hpl.loom.adapter.tm.backend.RestClient.RestTemplateWithJsonAndString;
import com.hp.hpl.loom.adapter.tm.backend.aa.BackEndDma;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaCurrent;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaDesired;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaDiscrepancies;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Enclosure;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Node;
import com.hp.hpl.loom.adapter.tm.backend.aa.current.Rack;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredEnclosure;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredNode;
import com.hp.hpl.loom.adapter.tm.backend.aa.desired.DesiredRack;

public class DmaIntegration {

    private static final Log LOG = LogFactory.getLog(DmaIntegration.class);
    // private static final String LATEST_MACHINE_MODEL_PATH =
    // "dma-server/src/test/resources/domain-repository/latestMachineSFPmodels";

    private String desired_route;
    private String current_route;
    private String status_route;
    private String discrepancies_route;
    private String dmaURL;
    private boolean ignore_tests;

    @Before
    public void before() throws URISyntaxException, UnsupportedEncodingException, IOException, ConfigurationException,
            InterruptedException {
        // Do not ignore tests initially.
        ignore_tests = false;

        // Obtain sfpc current working directory and listening port
        // String sfpc_cwd = this.findSfpPath();
        // LOG.debug("SFP cwd: '" + sfpc_cwd + "'");
        // Integer port = this.findSfpPort();
        // LOG.debug("SFP port: '" + port + "'");

        // If test are ignored, skip the following:
        if (ignore_tests) {
            return;
        }


        LOG.debug("Working directory: " + System.getProperty("user.dir"));
        String root = "../../../../dma/domain-repository/latestMachineSFPmodels/";

        LOG.debug("Dma root: '" + root + "'");

        RestTemplateWithJsonAndString rt = new RestClient.RestTemplateWithJsonAndString(false);
        rt.setMessageConverters(
                Arrays.asList(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()));

        // Load the relatives import resources.
        String desired = Paths.get(root).resolve("ds_multi_encl_state_latest.json").toString();
        String current = Paths.get(root).resolve("cs_multi_encl_state_latest.json").toString();

        String desired_compiled = new String(Files.readAllBytes(Paths.get(desired)), StandardCharsets.UTF_8);
        String current_compiled = new String(Files.readAllBytes(Paths.get(current)), StandardCharsets.UTF_8);

        PropertiesConfiguration cfg =
                new PropertiesConfiguration("src/test/resources/tmAdapterDma-deployment.properties");
        ObjectMapper mapper = new ObjectMapper();

        dmaURL = cfg.getString("dmaURL");
        desired_route = dmaURL + TMItemCollector.DMA_DESIRED_ROUTE;
        current_route = dmaURL + TMItemCollector.DMA_CURRENT_ROUTE;
        status_route = dmaURL + TMItemCollector.DMA_STATUS_ROUTE;
        discrepancies_route = dmaURL + TMItemCollector.DMA_DISCREPANCIES_ROUTE;

        String put_desired_route = dmaURL + "/dma/desired/";
        LOG.debug("PUT " + current_route);
        rt.putForEntity(put_desired_route, mapper.readTree("{ \"value\": " + desired_compiled + "}"), null);
        LOG.debug("PUT " + put_desired_route);
        rt.putForEntity(current_route, mapper.readTree("{ \"value\": " + current_compiled + "}"), null);
    }

    @After
    public void after() {

    }

    private String findSfpPath() throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            ignore_tests = true;
            return null;
        }
        String pid = IOUtils.toString(Runtime.getRuntime().exec("pgrep sfpc").getInputStream());
        String res = IOUtils.toString(Runtime.getRuntime().exec("pwdx " + pid).getInputStream());
        return "/" + res.substring(pid.length() + 2).trim();
    }

    private String findDmaRoot(final String curr_dir) {
        return curr_dir.substring(0, curr_dir.indexOf("dma-sfp"));
    }

    private Integer findSfpPort() throws IOException, InterruptedException {
        if (SystemUtils.IS_OS_WINDOWS) {
            ignore_tests = true;
            return null;
        }
        // "netstat -tlnp | grep sfpc | awk -F\" \" '{ print $4 }' | awk -F\":\" '{ print $2 }'";
        Process proc = Runtime.getRuntime().exec("netstat -tlnp");
        proc.waitFor();
        String result = IOUtils.toString(proc.getInputStream());
        for (String line : result.split("\n")) {
            if (line.contains("sfpc")) {
                result = line;
            }
        }
        String[] vals = result.split(" ");
        for (String val : vals) {
            if (val.matches("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+")) {
                result = val;
                break;
            }
        }
        result = result.substring(result.lastIndexOf(":") + 1);
        LOG.debug(result);
        return Integer.parseInt(result);
    }

    @Test
    public void checkDmaConformance() {

        // Skip test if required
        if (ignore_tests) {
            return;
        }

        RestClient rc = new RestClient();
        BackEndDma<GetDmaCurrent, GetDmaDesired, GetDmaStatus, GetDmaDiscrepancies> be =
                new BackEndDma<>(rc, current_route, desired_route, status_route, discrepancies_route);
        be.markAsDirty();
        be.refreshResult();

        assertEquals(true, be.filterCurrent(Rack.class).hasNext());
        assertEquals(true, be.filterCurrent(Enclosure.class).hasNext());
        assertEquals(true, be.filterCurrent(Node.class).hasNext());
        assertEquals(true, be.filterDesired(DesiredRack.class).hasNext());
        assertEquals(true, be.filterDesired(DesiredEnclosure.class).hasNext());
        assertEquals(true, be.filterDesired(DesiredNode.class).hasNext());

        assertNotNull(be.filterCurrent(Rack.class).next());
        assertNotNull(be.filterCurrent(Enclosure.class).next());
        assertNotNull(be.filterCurrent(Node.class).next());
        assertNotNull(be.filterDesired(DesiredRack.class).next());
        assertNotNull(be.filterDesired(DesiredEnclosure.class).next());
        assertNotNull(be.filterDesired(DesiredNode.class).next());
    }


    /*
     * @Test public void testActionSocOn(){ DmaAPI dmaAPI = new DmaAPI(dmaURL); String socCoordinate
     * = "/main/rack_u/1/enclosure/1/node/1/soc_board/soc"; assertEquals(true,
     * dmaAPI.setSocOnDesiredState(socCoordinate, null)); }
     * 
     * @Test public void testActionSocOff(){ DmaAPI dmaAPI = new DmaAPI(dmaURL); String
     * socCoordinate = "/main/rack_u/1/enclosure/1/node/1/soc_board/soc"; assertEquals(true,
     * dmaAPI.setSocOffDesiredState(socCoordinate, false)); }
     * 
     * @Test public void testActionSocOffForce() { DmaAPI dmaAPI = new DmaAPI(dmaURL); String
     * socCoordinate = "/main/rack_u/1/enclosure/1/node/1/soc_board/soc"; assertEquals(true,
     * dmaAPI.setSocOffDesiredState(socCoordinate, true)); }
     */
}
