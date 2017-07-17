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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.tm.Const;
import com.hp.hpl.loom.model.ItemType;

public abstract class DmaBaseItem<T extends DmaCoreItemAttributes> extends AdapterItem<T> {

    private static final int ALERT_LVL = 1;

    private static final int AA_ERRORS_ALERT_LEVEL = 10;
    private static final int AA_WARNINGS_ALERT_LEVEL = 2;
    private static final String AA_ERRORS_ALERT_DESCRIPTION = "AA error : ";
    private static final String AA_WARNINGS_ALERT_DESCRIPTION = "AA warning : ";

    private static final Log LOG = LogFactory.getLog(DmaBaseItem.class);

    public DmaBaseItem(final String logicalId, final ItemType itemType, final Class<T> itemAttributesClass) {
        super(logicalId, itemType);
    }

    public void displayAlert(final String alert) {
        this.setAlertDescription(alert);
        this.setAlertLevel(ALERT_LVL);
    }

    private boolean updateAaErrorStatus(DmaCoreItemAttributes core) {
        if (core.getCurrentStatus().contains(Const.AA_ERROR_STATUS)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(core.getItemId() + " : AA has errors");
            }
            if (getAlertLevel() < AA_ERRORS_ALERT_LEVEL) {
                setAlertLevel(AA_ERRORS_ALERT_LEVEL);
                String errDesc = AA_ERRORS_ALERT_DESCRIPTION;
                for (String err : getCore().getAaErrorMsgs()) {
                    errDesc += err + ", ";
                }
                setAlertDescription(errDesc.substring(0, errDesc.length() - 2));
                return true;
            }
        } else if (getAlertLevel() == AA_ERRORS_ALERT_LEVEL
                && getAlertDescription().contains(AA_ERRORS_ALERT_DESCRIPTION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Clearing AA errors alert for " + core.getItemId());
            }
            setAlertLevel(0);
            setAlertDescription("");
        }
        return false;
    }

    private boolean updateAaWarningStatus(DmaCoreItemAttributes core) {
        if (core.getCurrentStatus().contains(Const.AA_WARNING_STATUS)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(core.getItemId() + " : AA has warnings");
            }
            if (getAlertLevel() < AA_WARNINGS_ALERT_LEVEL) {
                setAlertLevel(AA_WARNINGS_ALERT_LEVEL);
                String warnDesc = AA_WARNINGS_ALERT_DESCRIPTION;
                for (String warn : getCore().getAaWarningMsgs()) {
                    warnDesc += warn + ", ";
                }
                setAlertDescription(warnDesc.substring(0, warnDesc.length() - 2));
                return true;
            }
        } else if (getAlertLevel() == AA_WARNINGS_ALERT_LEVEL
                && getAlertDescription().contains(AA_WARNINGS_ALERT_DESCRIPTION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Clearing AA warnings alert for " + core.getItemId());
            }
            setAlertLevel(0);
            setAlertDescription("");
        }
        return false;
    }

    @Override
    public boolean update() {
        boolean parentUpdated = super.update();
        DmaCoreItemAttributes core = getCore();
        boolean errorUpdate = updateAaErrorStatus(core);
        boolean warningUpdate = updateAaWarningStatus(core);
        return parentUpdated | errorUpdate | warningUpdate;
    }

}
