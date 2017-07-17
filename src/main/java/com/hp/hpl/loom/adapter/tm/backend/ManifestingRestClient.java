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
package com.hp.hpl.loom.adapter.tm.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Manifest;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Manifests;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Package;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Packages;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Task;
import com.hp.hpl.loom.adapter.tm.backend.osprovisioning.Tasks;

public class ManifestingRestClient {

    private static final Log LOG = LogFactory.getLog(ManifestingRestClient.class);
    private RestTemplate client;
    private HttpEntity<String> entity;
    private String host;

    public ManifestingRestClient(String host) {
        client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json; version=1.0");
        headers.add("Content-Type", "application/json; charset=utf-8; version=1.0");
        entity = new HttpEntity<String>("parameters", headers);
        this.host = host;
    }

    public List<String> getBackendManifests() {
        try {
            ResponseEntity<Manifests> response =
                    client.exchange(host + "/manifest/", HttpMethod.GET, entity, Manifests.class);

            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            return response.getBody().manifests;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return new ArrayList<String>();
        }
    }

    public Manifest getManifest(String prefixedName) {
        try {
            ResponseEntity<Manifest> response =
                    client.exchange(host + "/manifest/" + prefixedName, HttpMethod.GET, entity, Manifest.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return new Manifest("", "", "", "", "", Collections.emptyList(), Collections.emptyList());
        }
    }

    public List<Manifest> getManifests() {
        Iterator<String> iter = getBackendManifests().iterator();
        List<Manifest> manifests = new ArrayList<>();

        while (iter.hasNext()) {
            String prefixedName = iter.next();
            Manifest m = getManifest(prefixedName);
            m.prefixedName = prefixedName;
            manifests.add(m);
        }

        return manifests;
    }

    public List<Package> getPackages() {
        try {
            ResponseEntity<Packages> response =
                    client.exchange(host + "/packages/", HttpMethod.GET, entity, Packages.class);
            return response.getBody().packages;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return new ArrayList<Package>();
        }
    }

    // TODO Manifesting server seems to return 404 for these
    public Package getPackage(String name) {
        try {
            ResponseEntity<Package> response =
                    client.exchange(host + "/packages/" + name, HttpMethod.GET, entity, Package.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return new Package("", "", "");
        }
    }

    public List<Task> getTasks() {
        try {
            ResponseEntity<Tasks> response = client.exchange(host + "/tasks/", HttpMethod.GET, entity, Tasks.class);
            return response.getBody().task;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return new ArrayList<Task>();
        }
    }

    // ACTIONS
    public boolean deleteManifest(String prefixedName) {
        try {
            ResponseEntity<Void> response =
                    client.exchange(host + "/manifest/" + prefixedName, HttpMethod.DELETE, entity, Void.class);
            return response.getStatusCode() == HttpStatus.NO_CONTENT ? true : false;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return false;
        }
    }

    public boolean createManifest(String prefixedName, Manifest manifest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json; version=1.0");
        headers.add("Content-Type", "application/json; charset=utf-8; version=1.0");
        HttpEntity<Manifest> manifestEntity = new HttpEntity<Manifest>(manifest, headers);
        String url = host + "/manifest/";
        if (prefixedName != null) {
            if (prefixedName.length() > 0) {
                if (!prefixedName.substring(prefixedName.length() - 1).equals("/")) {
                    prefixedName = prefixedName + "/";
                }
                url += prefixedName;
            }
        }

        try {
            ResponseEntity<Void> response = client.exchange(url, HttpMethod.POST, manifestEntity, Void.class);
            return response.getStatusCode() == HttpStatus.CREATED ? true : false;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Manifesting server: ", e);
            return false;
        }
    }
}
