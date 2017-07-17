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
package com.hp.hpl.loom.adapter.tm.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Authenticate with and check group membership against corporate LDAP.
 */
public final class EnterpriseDirectory {
    private static final Log LOG = LogFactory.getLog(EnterpriseDirectory.class);

    private EnterpriseDirectory() {}

    /**
     * Authenticate account username with LDAP over SSL.
     *
     * @param username uid of user using their email address, e.g. user@company.com
     * @param password user's password
     * @param serverName IP/FQDN of the LDAP server to authenticate against
     * @param serverPort port to use=
     * @return true if authentication completed successfully
     */
    public static boolean authenticate(final String username, final String password, final String serverName,
            final int serverPort, final String ldapOu, final String ldapO) {
        Hashtable<Object, Object> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldaps://" + serverName + ":" + serverPort);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put(Context.SECURITY_PRINCIPAL, "uid=" + username + ", ou=" + ldapOu + ", o=" + ldapO);
        env.put(Context.SECURITY_CREDENTIALS, password);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authenticating " + username + " with " + env.get(Context.PROVIDER_URL));
        }

        boolean authenticated = false;

        try {
            new InitialDirContext(env);

            authenticated = true;
        } catch (NamingException e) {
            LOG.error("Failed to authenticate with " + env.get(Context.PROVIDER_URL), e);
        }

        return authenticated;
    }

    /**
     * Determine whether user is a member of group. Creates an unauthenticated session with the LDAP
     * service.
     *
     * @param user uid of user using their email address, e.g. user@company.com
     * @param group CN of the group to check membership of, e.g. tm_global_instance_admins
     * @return true if the user is a member of the group
     */
    public static boolean isMemberOf(final String user, final String group, final String serverName,
            final int serverPort, final String ldapOu, final String ldapO) {
        Hashtable<Object, Object> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldaps://" + serverName + ":" + serverPort);
        env.put(Context.SECURITY_AUTHENTICATION, "none");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Connecting to " + env.get(Context.PROVIDER_URL));
        }

        boolean member = false;

        try {
            DirContext ctx = new InitialDirContext(env);

            NamingEnumeration<SearchResult> n1 = ctx.search("ou=" + ldapOu + ",o=" + ldapO + ", cn=" + group, null);
            Attribute attrib;

            if (n1 != null) {
                while (n1.hasMore()) {
                    SearchResult searchResults = n1.next();

                    for (NamingEnumeration<?> nameEnum = searchResults.getAttributes().getAll(); nameEnum.hasMore();) {
                        attrib = (Attribute) nameEnum.next();

                        if (attrib.getID().equals("member")) {
                            for (NamingEnumeration<?> e = attrib.getAll(); e.hasMore();) {
                                String response = e.next().toString();
                                String uid = response.substring(response.indexOf('=') + 1, response.indexOf(','));

                                if (uid.equals(user)) {
                                    member = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (NamingException e) {
            LOG.error("Failed to determine membership of " + user + " in group " + group, e);
        }

        return member;
    }
}
