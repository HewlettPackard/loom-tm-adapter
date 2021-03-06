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

/**
 * Represents an log entry generated by the system.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "source", "message", "hostname", "level", "coordinate"})
public class Log {
    @JsonProperty
    private long timestamp;

    @JsonProperty
    private String source;

    @JsonProperty
    private String message;

    @JsonProperty
    private String hostname;

    @JsonProperty
    private String level;

    @JsonProperty
    private String coordinate;

    public Log() {
        // Jackson deserialization
    }

    /**
     *
     * @param timestamp The timestamp for the log as Unix epoch time.
     * @param source The source of the log.
     * @param message The message of the log.
     * @param hostname The hostname generating the log.
     * @param level The log level
     * @param coordinate The coordinate of the element that generated the log
     */
    public Log(final long timestamp, final String source, final String message, final String hostname,
            final String level, final String coordinate) {
        this.timestamp = timestamp;
        this.source = source;
        this.message = message;
        this.hostname = hostname;
        this.level = level;
        this.coordinate = coordinate;
    }

    @Override
    public String toString() {
        String str = "Timestamp : " + timestamp + "\nSource    : " + source + "\nMessage   : " + message
                + "\nHostname  : " + hostname + "\nLevel     : " + level + "\nCoordinate: " + coordinate;
        return str;
    }

    /**
     * @return The timestamp for the log as Unix epoch time, the number of seconds since 1970-01-01
     *         00:00:00 UTC.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The source of the log (i.e System)
     */
    public String getSource() {
        return source;
    }

    /**
     * @return The message of the log.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The hostname generating the log.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return The level of the log (i.e INFO)
     */
    public String getLevel() {
        return level;
    }

    /**
     * @return The location of the object generating the log.
     */
    public String getCoordinate() {
        return coordinate;
    }
}
