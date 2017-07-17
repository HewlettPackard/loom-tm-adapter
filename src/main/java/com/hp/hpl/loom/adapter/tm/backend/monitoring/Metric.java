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
package com.hp.hpl.loom.adapter.tm.backend.monitoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a measurement of a specific metric at a specific timestamp.
 */
@SuppressWarnings("checkstyle:membername")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "timestamp", "value", "dimensions", "value_meta"})
public class Metric {
    @JsonProperty
    private String name;

    @JsonProperty
    private long timestamp;

    @JsonProperty
    private double value;

    @JsonProperty
    @JsonIgnore
    private ObjectNode value_meta;

    @JsonProperty
    private Map<String, String> dimensions = new HashMap<String, String>();

    public Metric() {
        // Jackson deserialization
    }

    /**
     * @param name Name of the metric
     * @param timestamp Timestamp when the metric was collected
     * @param value Value of the metric
     * @param dimensions HashMap containing keys and values of other metadata of the metric
     */
    public Metric(final String name, final long timestamp, final double value, final Map<String, String> dimensions) {
        this.name = name;
        this.timestamp = timestamp;
        this.value = value;
        this.dimensions = dimensions;
    }

    @Override
    public String toString() {
        String str = "Name   : " + name + "\nTimestamp : " + timestamp + "\nValue    : " + value;
        Iterator<Entry<String, String>> it = dimensions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            str += "\n" + pair.getKey() + " : " + pair.getValue();
        }
        return str;
    }

    /**
     * @return The name of the metric
     */
    public String getName() {
        return name;
    }

    /**
     * @return The timestamp of the measurement for the metric.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The value of the measurement for the metric.
     */
    public double getValue() {
        return value;
    }

    /**
     * @return The dimensions or tags for the metric.
     */
    public ObjectNode getValueMeta() {
        return value_meta;
    }

    public Map<String, String> getDimensions() {
        return dimensions;
    }
}
