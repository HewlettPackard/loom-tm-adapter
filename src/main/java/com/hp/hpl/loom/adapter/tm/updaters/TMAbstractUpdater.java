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

import java.util.HashMap;
import java.util.List;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.AggregationUpdater;
import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.adapter.tm.backend.aa.GetDmaStatus;
import com.hp.hpl.loom.adapter.tm.backend.monitoring.Metric;
import com.hp.hpl.loom.adapter.tm.items.base.DmaCoreItemAttributes;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.CoreItemAttributes;
import com.hp.hpl.loom.model.ItemType;

public abstract class TMAbstractUpdater<T extends AdapterItem<A>, A extends CoreItemAttributes, R>
        extends AggregationUpdater<T, A, R> {

    public TMAbstractUpdater(final Aggregation aggregation, final BaseAdapter adapter, final ItemType itemType,
            final ItemCollector itemCollector) throws NoSuchItemTypeException {
        super(aggregation, adapter, itemType.getLocalId(), itemCollector);
    }

    /**
     * Check if the metric with name metricName is stored in the resource and its value is different
     * from vizValue
     *
     * @param metrics Metrics HashMap containing the updated metrics keyed by name
     * @param metricName name of the metric to be checked
     * @param vizValue current value of the metric being displayed
     * @return true if the metric is in resource, and it's different from the vizValue
     */
    protected boolean areDifferent(HashMap<String, Metric> metrics, String metricName, Number vizValue) {
        return metrics != null && metrics.containsKey(metricName) && vizValue != null
                && metrics.get(metricName).getValue() != vizValue.doubleValue();
    }

    /**
     * Populate the AA-related status attributes with the values provided by the status object
     *
     * @param status GetDmaStatus object containing the information that will be used
     * @param item DmaCoreItemAttributes item whose status attributes will be updated
     */
    protected void setStatusAttributes(final GetDmaStatus status, final DmaCoreItemAttributes item) {
        if (status == null) {
            return;
        }
        List<String> errs = AaStatusUtil.getErrors(status);
        List<String> warnings = AaStatusUtil.getWarnings(status);
        item.setAaErrorMsgs(errs);
        item.setAaWarningMsgs(warnings);
        if (errs.size() == 0) {
            if (warnings.size() == 0) {
                item.setCurrentStatus(Const.AA_OK_STATUS);
            } else {
                item.setCurrentStatus(warnings.size() + " " + Const.AA_WARNING_STATUS);
            }
        } else {
            item.setCurrentStatus(errs.size() + " " + Const.AA_ERROR_STATUS);
        }
    }

}
