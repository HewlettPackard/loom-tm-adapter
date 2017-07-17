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
package com.hp.hpl.loom.adapter.tm.backend.osprovisioning;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonAutoDetect
public class Manifest {

    public Manifest() {}

    public Manifest(String prefixedName, String _comment, String name, String description, String release,
            List<String> packages, List<String> tasks) {
        this.prefixedName = prefixedName;
        this._comment = _comment;
        this.name = name;
        this.description = description;
        this.release = release;
        this.packages = packages;
        this.tasks = tasks;
    }

    @JsonIgnore
    public String prefixedName;
    public String _comment;
    public String name;
    public String description;
    public String release;
    public List<String> packages;
    public List<String> tasks;
}
