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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.tm.util.ThrottleLogger;
import com.hp.hpl.loom.model.Credentials;

@JsonIgnoreType
public class RestClient {

    private static final Log LOG = new ThrottleLogger(LogFactory.getLog(RestClient.class), 2000);

    @JsonAutoDetect
    private static class Auth {
        @SuppressWarnings({"unused", "checkstyle:visibilitymodifier"})
        public String username;
        @SuppressWarnings({"unused", "checkstyle:visibilitymodifier"})
        public String password;

        public Auth(final String username, final String password) {
            this.username = username;
            this.password = password;
        }
    }

    @JsonAutoDetect
    private static class Empty {
    }

    public RestClient() {}

    // public void prepareDmaServer(final String urlDmaPutDesiredCurrent, final
    // BackEndUpdater<MockRack> currentUpdater) {
    // RestTemplateWithJsonAndString rt = createRestTemplateWithJsonSupportAndCookie(cookie_dma);
    // ObjectNode tdesiredcurrent = client_dma.fillInitialState(currentUpdater);
    // ResponseEntity<String> response = rt.putForEntity(urlDmaPutDesiredCurrent, tdesiredcurrent,
    // String.class);
    // if (response.getStatusCode().equals(HttpStatus.OK)) {
    // List<String> cookie_values = response.getHeaders().get("Set-Cookie");
    // if (LOG.isDebugEnabled()) {
    // if (cookie_values.size() == 0) {
    // LOG.debug("MISSING COOKIE !");
    // }
    // }
    // if (cookie_values.size() >= 1) {
    // cookie_dma = cookie_values.get(0);
    // }
    // } else {
    // if (LOG.isWarnEnabled()) {
    // LOG.warn("Request failed ! " + response.getStatusCode() + "\n" + response.getBody());
    // }
    // }
    // }



    /**
     * Authenticate on the Mock back-end, this step is necessary to obtain a valid cookie from the
     * mock back-end.
     *
     * @param authEndpoint authorization end point
     * @param creds Credentials (user name and password) to authenticate
     * @return true
     */
    public boolean authenticate(final String authEndpoint, final Credentials creds) {
        RestTemplate rt = createRestTemplateWithJsonSupportAndStringConverter(true);

        try {
            ResponseEntity<String> response =
                    rt.postForEntity(authEndpoint, new Auth(creds.getUsername(), creds.getPassword()), String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                List<String> cookieValues = response.getHeaders().get("set-cookie");
                if (LOG.isDebugEnabled() && cookieValues.size() != 1) {
                    LOG.debug("MISSING COOKIE OR WRONG COOKIE FORMAT: " + cookieValues);
                }
                if (cookieValues.size() == 1) {
                    LOG.debug("Found cookie with value: " + cookieValues.get(0));
                    return true;
                }
            }
        } catch (RestClientException e) {
            LOG.warn("ignored RestClientException : " + e.toString());
        }
        return true;
    }

    /**
     * Perform GET operations on the mock back-end on all possible `url/:id`. Tries to convert each
     * request result into the given class type.
     *
     * @param url is the url
     * @param expectedType is the type expected from the GET.
     * @param failOnUnknownProp set this value to true to reject response that do not match exactly
     *        expected_type.
     * @param <T> generic class type corresponding to each class in which the result is converted
     * @return Return a list of the found results.
     */
    public <T> T getAllResources(final String url, final Class<T> expectedType, final boolean failOnUnknownProp) {
        RestTemplate rt = createRestTemplateWithJsonSupportAndStringConverter(failOnUnknownProp);
        URI route = new UriTemplate(url).expand();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json; version=1.0");
            HttpEntity<?> reqEntity = new HttpEntity<>(headers);
            return rt.exchange(route, HttpMethod.GET, reqEntity, expectedType).getBody();
        } catch (Exception e) {
            LOG.error("Failed to perform GET request : " + url + "\nException : ", e);
            return null;
        }
    }

    public <T> T getAllResources(final String url, final Class<T> expectedType) {
        return this.getAllResources(url, expectedType, true);
    }

    /**
     * Perform a POST at the given url providing the given object.
     *
     * @param url is the action url to talk to.
     * @param obj is the object to sent as part of the post.
     * @return Returns true only if the post has succeeded.
     */
    public boolean postAction(final String url, final Object obj) {
        RestTemplate rt = createRestTemplateWithJsonSupportAndStringConverter(true);
        try {
            ResponseEntity<String> response = rt.postForEntity(url, obj, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Action succeeded for: " + url);
                }
                return true;
            }
        } catch (RestClientException e) {
            LOG.error("POST to '" + url + "' failed", e);
        }
        return false;
    }

    /**
     * Equivalent of {@link RestClient#postAction(String, Object)} called with
     * {@link RestClient.Empty}
     *
     * @see RestClient#postAction(String, Object)
     * @param url URL against which the POST action will be executed
     * @return Returns true only if the post has succeeded
     */
    public boolean postAction(final String url) {
        return this.postAction(url, new Empty());
    }

    private RestTemplateWithJsonAndString createRestTemplateWithJsonSupportAndStringConverter(
            final boolean failOnUnkownProp) {
        RestTemplateWithJsonAndString rt = new RestTemplateWithJsonAndString(failOnUnkownProp);
        return rt;
    }

    public static class RestTemplateWithJsonAndString extends RestTemplate {

        public RestTemplateWithJsonAndString(final boolean failOnUnkownProp) {
            super();
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            ObjectMapper mapper =
                    new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnkownProp);
            MappingJackson2HttpMessageConverter m = new MappingJackson2HttpMessageConverter();
            m.setObjectMapper(mapper);
            messageConverters.add(m);
            messageConverters.add(new StringHttpMessageConverter());
            this.setMessageConverters(messageConverters);
        }

        public <T> ResponseEntity<T> putForEntity(final String url, final Object request, final Class<T> responseType) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json; version=1.0");
            HttpEntity<?> reqEntity = new HttpEntity<>(request, headers);
            return exchange(url, HttpMethod.PUT, reqEntity, responseType);
        }

        public <T> ResponseEntity<T> postForEntity(final String url, final Object request,
                final Class<T> responseType) {
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            this.setRequestFactory(requestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json; version=1.0");
            HttpEntity<?> reqEntity = new HttpEntity<>(request, headers);
            return exchange(url, HttpMethod.POST, reqEntity, responseType);
        }

        public <T> ResponseEntity<T> deleteForEntity(final String url, final Object request,
                final Class<T> responseType) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json; version=1.0");
            HttpEntity<?> reqEntity = new HttpEntity<>(request, headers);
            return exchange(url, HttpMethod.DELETE, reqEntity, responseType);
        }

        public <T> ResponseEntity<T> get(final String url, final Class<T> responseType) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json; version=1.0");
            HttpEntity<?> reqEntity = new HttpEntity<>(headers);
            return exchange(url, HttpMethod.GET, reqEntity, responseType);
        }

        public <T> ResponseEntity<T> patchForEntity(final String url, final Object request,
                final Class<T> responseType) {
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            this.setRequestFactory(requestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json; version=1.0");
            HttpEntity<?> reqEntity = new HttpEntity<>(request, headers);
            return exchange(url, HttpMethod.PATCH, reqEntity, responseType);
        }
    }
}
