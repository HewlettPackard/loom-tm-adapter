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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"startTime", "endTime"})
public class MetricDisplay {
    @JsonProperty
    private long startTime;

    @JsonProperty
    private long endTime;

    @JsonProperty
    private JsonNode metrics;


    public MetricDisplay() {
        // Jackson deserialization
    }

    public MetricDisplay(final long startTime, final long endTime, final JsonNode metrics) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.metrics = metrics;
    }

    /**
     * @return The startTime for the metric as Unix epoch time, the number of seconds since
     *         1970-01-01 00:00:00 UTC.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return The endTime for the metric as Unix epoch time, the number of seconds since 1970-01-01
     *         00:00:00 UTC.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return The Metric list of the result query executed.
     */
    public JsonNode getMetrics() {
        return metrics;
    }
}
