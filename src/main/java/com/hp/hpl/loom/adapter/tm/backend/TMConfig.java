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
package com.hp.hpl.loom.adapter.tm.backend;

import java.util.List;
import java.util.Map;

public class TMConfig {

    public static class Rack {
        public String coordinate;
        public List<Enclosure> enclosures;
    }

    public static class Enclosure {
        public String coordinate;
        public List<Node> nodes;
        public List<IZoneBoard> iZoneBoards;
    }

    public static class Node {
        public String coordinate;
        public String serialNumber;
        public NodeMp nodeMp;
        public Soc soc;
        public List<MediaController> mediaControllers;
    }

    public static class NodeMp {
        public String coordinate;
        public String ipv4Address;
        public String mfwApiUri;
        public String msCollector;
    }

    public static class Soc {
        public String coordinate;
        public String macAddress;
        public String tlsprivateCertificate;

        public String getCoordinate() {
            return coordinate;
        }
    }

    public static class MediaController {
        public String coordinate;
        public String memorySize;
    }

    public static class IZoneBoard {
        public String coordinate;
        public IzBoardMp izBoardMp;
    }

    public static class IzBoardMp {
        public String msCollector;
        public String coordinate;
        public String ipv4Address;
        public String mfwApiUri;
    }

    public static class InterleaveGroup {
        public Integer groupId;
        public List<String> mediaControllers;
    }

    public static class Service {
        public String service;
        public String restUri;
    }

    public static class Server {
        public List<Service> services;
    }

    public String coordinate;
    public List<Server> servers;
    public Map<Object, Object> advancedPowerManager;
    public List<Rack> racks;
    public List<InterleaveGroup> interleaveGroups;
}
