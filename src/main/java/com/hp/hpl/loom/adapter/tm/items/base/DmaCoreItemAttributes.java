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
package com.hp.hpl.loom.adapter.tm.items.base;

import java.util.List;

import com.hp.hpl.loom.adapter.NumericAttribute;
import com.hp.hpl.loom.adapter.annotations.LoomAttribute;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.model.CoreItemAttributes;

/**
 * Basic attributes that should be used when collecting data from the assembly agent.
 */
public class DmaCoreItemAttributes extends CoreItemAttributes {

    /**
     * NOTE: This attribute is used by weaver to test if an itemType is managed by the assembly
     * agent.
     */
    @LoomAttribute(key = "Desired state", supportedOperations = {DefaultOperations.GROUP_BY})
    private String dmaStatus;

    @LoomAttribute(key = "Number of discrepancies", max = "Infinity", min = "0", visible = true, plottable = true,
            type = NumericAttribute.class, supportedOperations = {DefaultOperations.SORT_BY})
    private int dmaTotalDiscrepancyCount;

    @LoomAttribute(key = "Number of (local) discrepancies", max = "Infinity", min = "0", visible = true,
            plottable = true, type = NumericAttribute.class, supportedOperations = {DefaultOperations.SORT_BY})
    private int dmaDiscrepancyCount;

    @LoomAttribute(key = "Current status",
            supportedOperations = {DefaultOperations.SORT_BY, DefaultOperations.GROUP_BY})
    private String currentStatus;

    private List<String> aaErrorMsgs;
    private List<String> aaWarningMsgs;

    public int getDmaDiscrepancyCount() {
        return dmaDiscrepancyCount;
    }

    public void setDmaDiscrepancyCount(final int count) {
        dmaDiscrepancyCount = count;
    }

    public void setDmaTotalDiscrepancyCount(final int count) {
        dmaTotalDiscrepancyCount = count;
    }

    public int getDmaTotalDiscrepancyCount() {
        return dmaTotalDiscrepancyCount;
    }

    public String getDmaStatus() {
        return dmaStatus;
    }

    public void setDmaStatus(final String dmaStatus) {
        this.dmaStatus = dmaStatus;
    }

    public List<String> getAaErrorMsgs() {
        return aaErrorMsgs;
    }

    public void setAaErrorMsgs(List<String> aaErrorMsgs) {
        this.aaErrorMsgs = aaErrorMsgs;
    }

    public List<String> getAaWarningMsgs() {
        return aaWarningMsgs;
    }

    public void setAaWarningMsgs(List<String> aaWarningMsgs) {
        this.aaWarningMsgs = aaWarningMsgs;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(final String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
