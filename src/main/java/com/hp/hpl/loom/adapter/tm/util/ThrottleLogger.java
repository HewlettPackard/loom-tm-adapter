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
package com.hp.hpl.loom.adapter.tm.util;

import org.apache.commons.logging.Log;

public class ThrottleLogger implements Log {

    private Log wrappedLogger;
    private long currentTime;
    private long msWait;

    public ThrottleLogger(final Log wrappedLogger, final long ms) {
        this.wrappedLogger = wrappedLogger;
        msWait = ms;
    }

    private void throttle(final Runnable call) {
        long newTime = System.currentTimeMillis();
        if (newTime - currentTime > msWait) {
            call.run();
            currentTime = newTime;
        }
    }

    @Override
    public void debug(final Object arg0) {
        this.throttle(() -> wrappedLogger.debug(arg0));
    }

    @Override
    public void debug(final Object arg0, final Throwable arg1) {
        this.throttle(() -> wrappedLogger.debug(arg0, arg1));
    }

    @Override
    public void error(final Object arg0) {
        this.throttle(() -> wrappedLogger.error(arg0));
    }

    @Override
    public void error(final Object arg0, final Throwable arg1) {
        this.throttle(() -> wrappedLogger.error(arg0, arg1));
    }

    @Override
    public void fatal(final Object arg0) {
        this.throttle(() -> wrappedLogger.fatal(arg0));
    }

    @Override
    public void fatal(final Object arg0, final Throwable arg1) {
        this.throttle(() -> wrappedLogger.fatal(arg0, arg1));
    }

    @Override
    public void info(final Object arg0) {
        this.throttle(() -> wrappedLogger.info(arg0));
    }

    @Override
    public void info(final Object arg0, final Throwable arg1) {
        this.throttle(() -> wrappedLogger.info(arg0, arg1));
    }

    @Override
    public boolean isDebugEnabled() {
        return wrappedLogger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return wrappedLogger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return wrappedLogger.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return wrappedLogger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return wrappedLogger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return wrappedLogger.isWarnEnabled();
    }

    @Override
    public void trace(final Object arg0) {
        this.throttle(() -> wrappedLogger.trace(arg0));
    }

    @Override
    public void trace(final Object arg0, final Throwable arg1) {
        this.throttle(() -> wrappedLogger.trace(arg0, arg1));
    }

    @Override
    public void warn(final Object arg0) {
        this.throttle(() -> wrappedLogger.warn(arg0));
    }

    @Override
    public void warn(final Object arg0, final Throwable arg1) {
        this.throttle(() -> wrappedLogger.warn(arg0, arg1));
    }


}
