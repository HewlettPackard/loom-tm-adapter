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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;
import com.hp.hpl.loom.adapter.tm.util.EnterpriseDirectory;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.ProviderImpl;

public class TMProvider extends ProviderImpl {
    private static final String TM_GLOBAL_INFRA_ADMINS = "tm_global_infra_admins";
    private static final String TM_GLOBAL_INSTANCE_ADMINS = "tm_global_instance_admins";
    private static final String TM_GLOBAL_TENANT_ADMINS = "tm_global_tenant_admins";

    @JsonIgnoreProperties
    private List<String> limitedUsers = null;

    @JsonIgnoreProperties
    private RestClient netInterface;

    @JsonIgnoreProperties
    private String ldapServerName;

    @JsonIgnoreProperties
    private int ldapServerPort;

    @JsonIgnoreProperties
    private String ldapOu;

    @JsonIgnoreProperties
    private String ldapO;

    @JsonIgnoreProperties
    private String ldapPeopleOu;

    @JsonIgnoreProperties
    private String ldapGroupOu;

    @SuppressWarnings("checkstyle:parameternumber")
    public TMProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName, final String adapterPackage, final String ldapServerName,
            final int ldapServerPort, final String ldapPeopleOu, final String ldapGroupOu, final String ldapO) {
        super(providerType, providerId, authEndpoint, providerName, adapterPackage);
        netInterface = new RestClient();
        limitedUsers = new ArrayList<String>();
        this.ldapServerName = ldapServerName;
        this.ldapServerPort = ldapServerPort;
        this.ldapPeopleOu = ldapPeopleOu;
        this.ldapGroupOu = ldapGroupOu;
        this.ldapO = ldapO;
    }

    @Override
    public boolean authenticate(final Credentials creds) {
        // return net_interface.authenticate(authEndpoint, creds);
        boolean authenticated = false;

        if (creds.getUsername().indexOf('@') == -1) {
            // No email address supplied, so use built-in account

            authenticated = "loom".equals(creds.getPassword()) && "loom".equals(creds.getUsername());
        } else {
            // Check that not only do the credentials reflect a valid account but that the user is
            // also a member of one of the required groups.

            if (EnterpriseDirectory.authenticate(creds.getUsername(), creds.getPassword(), ldapServerName,
                    ldapServerPort, ldapPeopleOu, ldapO)) {
                boolean tenantAdmin = EnterpriseDirectory.isMemberOf(creds.getUsername(), TM_GLOBAL_TENANT_ADMINS,
                        ldapServerName, ldapServerPort, ldapGroupOu, ldapO);

                authenticated = EnterpriseDirectory.isMemberOf(creds.getUsername(), TM_GLOBAL_INFRA_ADMINS,
                        ldapServerName, ldapServerPort, ldapGroupOu, ldapO)
                        || EnterpriseDirectory.isMemberOf(creds.getUsername(), TM_GLOBAL_INSTANCE_ADMINS,
                                ldapServerName, ldapServerPort, ldapGroupOu, ldapO)
                        || tenantAdmin;

                if (tenantAdmin) {
                    limitedUsers.add(creds.getUsername());
                }
            }
        }

        return authenticated;
    }

    public boolean userHasLimitedAccess(final String username) {
        return limitedUsers.contains(username);
    }

    public RestClient getRestClient() {
        return netInterface;
    }
}
