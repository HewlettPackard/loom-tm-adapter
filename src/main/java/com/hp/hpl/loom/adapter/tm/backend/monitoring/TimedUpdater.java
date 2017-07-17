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

public class TimedUpdater {

    static final long MAX_DELTA_T = 50 * 60 * 1000;
    static final long MARGIN_T = 30 * 1000;

    private long lastUpdateTime = 0;

    long getStartTime() {
        long now = System.currentTimeMillis();
        long proposedStartTime = getLastUpdateTime() - MARGIN_T;
        if (now - proposedStartTime > MAX_DELTA_T) {
            return now - MAX_DELTA_T;
        }
        return proposedStartTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

}
