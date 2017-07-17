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

public final class Types {

    public static final String INSTANCE_TYPE_LOCAL_ID = "instance";
    public static final String RACK_TYPE_LOCAL_ID = "rack";
    public static final String NODE_TYPE_LOCAL_ID = "node";
    public static final String ENCLOSURE_TYPE_LOCAL_ID = "enclosure";
    public static final String FABRIC_SWITCH_TYPE_LOCAL_ID = "fabric_switch";
    public static final String SOC_TYPE_LOCAL_ID = "soc";
    public static final String MEMORY_BOARD_TYPE_LOCAL_ID = "memoryboard";
    public static final String BOOK_TYPE_LOCAL_ID = "book";
    public static final String SHELF_TYPE_LOCAL_ID = "shelf";
    public static final String INTERLEAVE_GROUP_TYPE_LOCAL_ID = "interleave_group";
    public static final String MANIFEST_LOCAL_ID = "manifest";

    public static final String INFRASTRUCTURE_LAYER = "infrastructure_layer";
    public static final String MEMORY_LAYER = "memory_layer";
    public static final String HYBRID_LAYER = "hybrid_layer";
    public static final String APPLICATION_LAYER = "application_layer";
    public static final String OS_MANIFEST_LAYER = "os_manifest_layer";

    /**
     * Private constructor as this is a utility class
     */
    private Types() {}
}
