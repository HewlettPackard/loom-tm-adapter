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

public class LibrarianMemory {

    public static class MemoryDetails {
        public Integer allocated;
        public Integer available;
        public Integer notready;
        public Integer offline;
        public Integer total;

        public MemoryDetails() {}

        public MemoryDetails(Integer allocated, Integer available, Integer notready, Integer offline, Integer total) {
            this.allocated = allocated;
            this.available = available;
            this.notready = notready;
            this.offline = offline;
            this.total = total;
        }
    }

    public MemoryDetails memory;
}
