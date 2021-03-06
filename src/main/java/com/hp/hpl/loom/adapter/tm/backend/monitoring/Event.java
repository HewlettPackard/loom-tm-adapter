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
 * Represents an event generated by hardware components of the system (i.e. Zbridge)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "source", "eventId", "eventName", "eventType", "eventData", "coordinate"})
public class Event {
    @JsonProperty
    private long timestamp;

    @JsonProperty
    private String source;

    @JsonProperty
    private String eventId;

    @JsonProperty
    private String eventName;

    @JsonProperty
    private String eventType;

    @JsonProperty
    private String eventData;

    @JsonProperty
    private String coordinate;

    public Event() {
        // Jackson deserialization
    }

    /**
     * @param timestamp The timestamp for the event as milliseconds since the Unix epoch.
     * @param source The source of the event.
     * @param eventId Event id given by the generating source.
     * @param eventName Event name.
     * @param eventType The type of the event.
     * @param eventData Event details.
     * @param coordinate Location of the object generating the event.
     */
    public Event(final long timestamp, final String source, final String eventId, final String eventName,
            final String eventType, final String eventData, final String coordinate) {
        this.timestamp = timestamp;
        this.source = source;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventData = eventData;
        this.coordinate = coordinate;
    }

    @Override
    public String toString() {
        String str = "EventId   : " + eventId + "\nTimestamp : " + timestamp + "\nSource    : " + source
                + "\nEventName : " + eventName + "\nEventType : " + eventType + "\nEventData : " + eventData
                + "\nCoordinate: " + coordinate;
        return str;
    }

    /**
     * @return The timestamp for the event as Unix epoch time, the number of milliseconds since
     *         1970-01-01 00:00:00 UTC.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return The source of the event.
     */
    public String getSource() {
        return source;
    }

    /**
     * @return the eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @return the eventName
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @return The type of the event
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @return the event details.
     */
    public String getEventData() {
        return eventData;
    }

    /**
     * @return the coordinate of the object generating the event.
     */
    public String getCoordinate() {
        return coordinate;
    }
}
