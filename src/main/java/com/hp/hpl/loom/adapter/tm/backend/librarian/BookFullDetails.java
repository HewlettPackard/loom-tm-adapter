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

public class BookFullDetails {

    // Book specific
    public Integer seq_num;
    public Integer id;
    public Integer allocated;
    public Integer attributes;
    public Integer node_id;

    // Shelf specific
    public Integer book_count;
    public Integer size_bytes;
    public Integer ctime;
    public Integer mtime;
    public String name;
    public String creator_id;
    public Integer shelf_id;
}
