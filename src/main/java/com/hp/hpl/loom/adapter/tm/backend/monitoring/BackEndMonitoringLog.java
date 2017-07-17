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

import com.hp.hpl.loom.adapter.tm.backend.RestClient;

public class BackEndMonitoringLog {

    private RestClient net;
    private final String route;
    private String fullRoute;
    private LogDisplay logsBlob;

    public BackEndMonitoringLog(final RestClient netInterface, final String currentRoute) {
        net = netInterface;
        route = currentRoute;
    }

    public void refreshResult() {
        fullRoute = route + "?source=origin1&startTime=1";
        logsBlob = net.getAllResources(fullRoute, LogDisplay.class);
        if (logsBlob != null) {
            logsBlob.getLogs();
        } else {
            System.out.println("No logs update from " + fullRoute);
        }
    }


}
