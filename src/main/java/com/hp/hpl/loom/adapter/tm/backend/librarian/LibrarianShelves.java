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
package com.hp.hpl.loom.adapter.tm.backend.librarian;

import java.util.List;

public class LibrarianShelves {

    public LibrarianShelves() {}

    public LibrarianShelves(List<LibrarianShelfEntry> entries, Integer group, String mode, Integer owner) {
        this.entries = entries;
        this.group = group;
        this.mode = mode;
        this.owner = owner;
    }

    public List<LibrarianShelfEntry> entries;
    public Integer group;
    public String mode;
    public Integer owner;
}
