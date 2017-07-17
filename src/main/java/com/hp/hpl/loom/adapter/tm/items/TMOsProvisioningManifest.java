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
package com.hp.hpl.loom.adapter.tm.items;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.ActionDefinition;
import com.hp.hpl.loom.adapter.annotations.ActionParameter;
import com.hp.hpl.loom.adapter.annotations.ActionRange;
import com.hp.hpl.loom.adapter.annotations.ActionTypes;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ItemTypeInfo;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.adapter.annotations.Sort;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.Types;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.ItemType;

@ItemTypeInfo(value = Types.MANIFEST_LOCAL_ID,
        sorting = {@Sort(operation = DefaultOperations.SORT_BY, fieldOrder = {"Name", "Release", "Description"})},
        layers = {Types.OS_MANIFEST_LAYER})
@ConnectedTo(toClass = TMSoc.class, type = RelationshipNames.ASSIGN, typeName = RelationshipNames.ASSIGN_NAME)
@ActionDefinition(name = "Delete", type = ActionTypes.Item, id = Const.ACTION_DELETE_OS_MANIFEST,
        description = "Delete an existing manifest", icon = "icon-delete",
        parameters = {@ActionParameter(id = "confirm", name = "Confirmation",
                type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED,
                ranges = {@ActionRange(id = "no", name = "No"), @ActionRange(id = "yes", name = "Yes")})})
@ActionDefinition(name = "Download", type = ActionTypes.Item, id = Const.ACTION_DOWNLOAD_OS_MANIFEST,
        description = "Download an existing manifest (file)", icon = "icon-update",
        parameters = {@ActionParameter(id = "confirm", name = "Confirmation",
                type = com.hp.hpl.loom.model.ActionParameter.Type.ENUMERATED,
                ranges = {@ActionRange(id = "no", name = "No"), @ActionRange(id = "yes", name = "Yes")})})
@ActionDefinition(name = "Upload", type = ActionTypes.Thread, id = Const.ACTION_UPLOAD_OS_MANIFEST,
        description = "Upload a new manifest (file)", icon = "icon-readonly",
        parameters = {
                @ActionParameter(id = "prefix", name = "Prefix",
                        type = com.hp.hpl.loom.model.ActionParameter.Type.STRING),
                @ActionParameter(id = "manifest", name = "Manifest",
                        type = com.hp.hpl.loom.model.ActionParameter.Type.FILE)})
@ActionDefinition(name = "Bind to all SoCs", type = ActionTypes.Item, id = Const.ACTION_BIND_OS_MANIFEST_TO_ALL_SOCS,
        description = "Set selected OS Manifest to all SoCs", icon = "icon-readonly")
@ActionDefinition(name = "Clear alerts", type = ActionTypes.Thread, id = "Clear OS provisioning manifest alerts",
        description = "Clear all alerts in the thread", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Item, id = "Clear OS provisioning manifest alert",
        description = "Clear the alert", icon = "icon-delete")
@ActionDefinition(name = "Clear alert", type = ActionTypes.Aggregation, id = "Clear OS provisioning manifest alert",
        description = "Clear the alert", icon = "icon-delete")
public class TMOsProvisioningManifest extends AdapterItem<TMOsProvisioningManifest.ItemAttributes> {

    // ------------------------------------------------------------------ //
    // ----------------------- PRIVATE FIELDS --------------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ---------------------- NON CORE ATTRIBUTES ----------------------- //
    // ------------------------------------------------------------------ //

    // ------------------------------------------------------------------ //
    // ------------------------ CORE ATTRIBUTES ------------------------- //
    // ------------------------------------------------------------------ //

    public static class ItemAttributes extends CoreItemAttributes {

        @LoomAttribute(key = "Name", supportedOperations = {DefaultOperations.SORT_BY})
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        @LoomAttribute(key = "Description", supportedOperations = {DefaultOperations.SORT_BY})
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        @LoomAttribute(key = "Release", supportedOperations = {DefaultOperations.SORT_BY})
        private String release;

        public String getRelease() {
            return release;
        }

        public void setRelease(final String release) {
            this.release = release;
        }

        @LoomAttribute(type = NumericAttribute.class, key = "Tasks", min = "0", max = "Infinity", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number tasks;

        public Number getTasks() {
            return tasks;
        }

        public void setTasks(final Number tasks) {
            this.tasks = tasks;
        }

        @LoomAttribute(type = NumericAttribute.class, key = "Packages", min = "0", max = "Infinity", plottable = true,
                supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY}, ignoreUpdate = true)
        private Number packages;

        public Number getPackages() {
            return packages;
        }

        public void setPackages(final Number packages) {
            this.packages = packages;
        }
    }

    // ------------------------------------------------------------------ //
    // ---------------------- PUBLIC INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    public TMOsProvisioningManifest(final String logicalId, final ItemType itemType) {
        super(logicalId, itemType);
    }
}
