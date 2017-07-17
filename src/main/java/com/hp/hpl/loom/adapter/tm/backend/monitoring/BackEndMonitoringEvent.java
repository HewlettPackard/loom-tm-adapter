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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;

public class BackEndMonitoringEvent extends TimedUpdater {

    private static final Log LOG = LogFactory.getLog(BackEndMonitoringEvent.class);

    private RestClient net;
    private final String route;
    private String fullRoute;
    private EventDisplay eventsBlob;
    private HashMap<String, HashMap<String, Event>> eventsByCoordinateAndName;

    private static List<String> eventsToCollect = Arrays.asList(Const.EVENT_OS_BOOTED);

    static final long MAX_DELTA_T = 50 * 60 * 1000;
    static final long MARGIN_T = 30 * 1000;

    public BackEndMonitoringEvent(final RestClient netInterface, final String currentRoute) {
        net = netInterface;
        route = currentRoute;
        eventsByCoordinateAndName = new HashMap<String, HashMap<String, Event>>();
    }

    private String getFullRoute() {
        long startTime = getStartTime();
        String fRoute = route + "?startTime=" + startTime + "&samples=1&filter=eventName=";
        for (String eventName : eventsToCollect) {
            fRoute = fRoute + eventName + ",";
        }
        fRoute = fRoute.substring(0, fRoute.length() - 1);
        LOG.debug("Full route to monitoring service: " + fRoute);
        return fRoute;
    }

    public void refreshResult() {
        fullRoute = getFullRoute();
        eventsBlob = net.getAllResources(fullRoute, EventDisplay.class);
        if (eventsBlob != null) {
            List<Event> events = eventsBlob.getEvents();
            for (Event event : events) {
                putEvent(event.getEventName(), event.getCoordinate(), event);
            }
        } else {
            LOG.debug("No events update from " + fullRoute);
        }
    }

    protected void putEvent(String name, final String coordinate, final Event event) {
        if (eventsByCoordinateAndName.get(coordinate) == null) {
            eventsByCoordinateAndName.put(coordinate, new HashMap<String, Event>());
        }
        if (event.getTimestamp() > getLastUpdateTime()) {
            setLastUpdateTime(event.getTimestamp());
        }
        LOG.debug("put event with name = " + name + " and coordinate = " + coordinate);
        eventsByCoordinateAndName.get(coordinate).put(name, event);
    }

    public HashMap<String, Event> getEventsForCoordinate(final String coordinate) {
        LOG.debug("Get events for coordinate: " + coordinate);
        return eventsByCoordinateAndName.get(coordinate);
    }

}
