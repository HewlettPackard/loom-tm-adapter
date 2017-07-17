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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Wrapper to the Log object that provides additional data of the user's request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"startTime", "endTime"})
public class LogDisplay {
    @JsonProperty
    private long startTime;

    @JsonProperty
    private long endTime;

    @JsonProperty
    private List<Log> logs;

    public LogDisplay() {
        // Jackson deserialization
    }

    public LogDisplay(final long startTime, final long endTime, final List<Log> logs) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.logs = logs;
    }

    /**
     * @return The startTime for the log as Unix epoch time, the number of seconds since 1970-01-01
     *         00:00:00 UTC.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return The endTime for the log as Unix epoch time, the number of seconds since 1970-01-01
     *         00:00:00 UTC.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return The list of logs for the query executed.
     */
    public List<Log> getLogs() {
        return logs;
    }
}
