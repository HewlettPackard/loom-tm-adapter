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
package com.hp.hpl.loom.adapter.tm.updaters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.aa.status.Component;

public final class AaStatusUtil {

    public static final String ERROR = "error";
    public static final String WARNING = "warning";

    private AaStatusUtil() {}

    private static String getErrorForComponent(Component component) {
        if (component != null && component.errorState != null) {
            if (ERROR.equalsIgnoreCase(component.errorState)) {
                String errMsg = "Error state without message";
                if (component.errorMessage != null) {
                    errMsg = component.errorMessage;
                }
                errMsg += " - Code = " + component.errorCode;
                return errMsg;
            }
        }
        return null;
    }

    private static String getWarningForComponent(Component component) {
        if (component != null && component.errorState != null) {
            if (WARNING.equalsIgnoreCase(component.errorState)) {
                String warnMsg = "Warning state without message";
                if (component.errorMessage != null) {
                    warnMsg = component.errorMessage;
                }
                warnMsg += " - Code = " + component.errorCode;
                return warnMsg;
            }
        }
        return null;
    }

    public static List<String> getErrors(GetDmaStatus status) {
        ArrayList<String> errs = new ArrayList<String>();
        if (status != null && status.componentStatus != null) {
            for (Component component : Arrays.asList(status.componentStatus.aaManager,
                    status.componentStatus.actuatorService, status.componentStatus.planManager,
                    status.componentStatus.stateMonitor)) {
                String errMsg = getErrorForComponent(component);
                if (errMsg != null) {
                    errs.add(errMsg);
                }
            }
        }
        return errs;
    }

    public static List<String> getWarnings(GetDmaStatus status) {
        ArrayList<String> errs = new ArrayList<String>();
        if (status != null && status.componentStatus != null) {
            for (Component component : Arrays.asList(status.componentStatus.aaManager,
                    status.componentStatus.actuatorService, status.componentStatus.planManager,
                    status.componentStatus.stateMonitor)) {
                String errMsg = getWarningForComponent(component);
                if (errMsg != null) {
                    errs.add(errMsg);
                }
            }
        }
        return errs;
    }

}
