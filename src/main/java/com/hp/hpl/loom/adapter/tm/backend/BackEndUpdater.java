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

import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.tm.introspect.FieldPath;

public class BackEndUpdater<T> {

    private static final Log LOG = LogFactory.getLog(BackEndUpdater.class);
    private boolean isDirty;
    private RestClient netInterface;
    private T data;
    private Class<T> elementType;
    private String route;
    private boolean failOnJsonError;

    public BackEndUpdater(final RestClient netInterface, final String route, final Class<T> elementType,
            final boolean failOnJsonError) {
        this(netInterface, route, elementType);
        this.failOnJsonError = failOnJsonError;
    }

    public BackEndUpdater(final RestClient netInterface, final String route, final Class<T> elementType) {
        this.route = route;
        this.netInterface = netInterface;
        this.elementType = elementType;
        this.isDirty = true;
        this.failOnJsonError = true;
    }

    public void markAsDirty() {
        isDirty = true;
    }

    public void refreshResult() {

        if (isDirty) {
            data = netInterface.getAllResources(route, elementType, failOnJsonError);
            isDirty = false;
        }
    }

    public Object getRawData() {
        return this.data;
    }

    @SuppressWarnings("unchecked")
    public <U> Iterator<U> filter(final Class<U> classFilter) {
        FieldPath path = FieldPath.introspect(elementType, classFilter);
        try {
            return (Iterator<U>) path.getValue(data);
        } catch (IllegalArgumentException e) {
            LOG.error("BUG FOUND: Unreachable code path: " + e.getMessage());
        }
        return Collections.emptyIterator();
    }
}
