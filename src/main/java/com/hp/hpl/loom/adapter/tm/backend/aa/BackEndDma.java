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
package com.hp.hpl.loom.adapter.tm.backend.aa;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.tm.backend.BackEndUpdater;
import com.hp.hpl.loom.adapter.tm.backend.RestClient;
import com.hp.hpl.loom.adapter.tm.introspect.PathSetter;

public class BackEndDma<C, D, S, R> {

    private static final Log LOG = LogFactory.getLog(BackEndDma.class);
    private static final int NUM_THREADS = 4;
    private BackEndUpdater<C> current;
    private BackEndUpdater<D> desired;
    private BackEndUpdater<S> status;
    private BackEndUpdater<R> discrepancies;
    private DmaDiscrepanciesMapper<R> discrepanciesMapper;

    private final Class<C> currentType = (Class<C>) GetDmaCurrent.class;
    private final Class<D> desiredType = (Class<D>) GetDmaDesired.class;
    private final Class<S> statusType = (Class<S>) GetDmaStatus.class;
    private final Class<R> discrepanciesType = (Class<R>) GetDmaDiscrepancies.class;

    public BackEndDma(final RestClient netInterface, final String currentRoute, final String desiredRoute,
            final String statusRoute, final String discrepanciesRoute) {
        this.current = new BackEndUpdater<>(netInterface, currentRoute, currentType, false);
        this.desired = new BackEndUpdater<>(netInterface, desiredRoute, desiredType, false);
        this.status = new BackEndUpdater<>(netInterface, statusRoute, statusType, false);
        this.discrepancies = new BackEndUpdater<>(netInterface, discrepanciesRoute, discrepanciesType, false);
        this.discrepanciesMapper = new DmaDiscrepanciesMapper<>();
    }

    public void markAsDirty() {
        this.current.markAsDirty();
        this.desired.markAsDirty();
        this.status.markAsDirty();
        this.discrepancies.markAsDirty();
    }

    public void refreshResult() {
        long t0 = System.currentTimeMillis();
        ExecutorService threadsPool = Executors.newFixedThreadPool(NUM_THREADS);
        threadsPool.submit(dmaCurrentRefresher);
        threadsPool.submit(dmaDesiredRefresher);
        threadsPool.submit(dmaStatusRefresher);
        threadsPool.submit(dmaDiscrepanciesRefresher);
        threadsPool.shutdown();
        try {
            LOG.debug("DMA refresh awaiting termination");
            threadsPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            LOG.debug("DMA refresh terminated");
            // We ignore the first nesting as all dma API result are wrapped in a "value" field.
            PathSetter.treeWalkSetAllPaths(this.current.getRawData(), 1);
            PathSetter.treeWalkSetAllPaths(this.desired.getRawData(), 1);
            PathSetter.treeWalkSetAllPaths(this.discrepancies.getRawData(), 1);
            discrepanciesMapper.refreshMapping(discrepancies);
        } catch (InterruptedException e) {
            LOG.error("DMA refresher threads pool awating was interrupted: ", e);
        }
        LOG.debug("DMA refresh total execution time: " + (System.currentTimeMillis() - t0) + " ms.");
    }

    private Callable<Boolean> dmaCurrentRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            current.refreshResult();
            return true;
        }
    };

    private Callable<Boolean> dmaDesiredRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            desired.refreshResult();
            return true;
        }
    };

    private Callable<Boolean> dmaStatusRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            status.refreshResult();
            return true;
        }
    };

    private Callable<Boolean> dmaDiscrepanciesRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            discrepancies.refreshResult();
            return true;
        }
    };

    public DmaDiscrepanciesMapper<R> getDiscrepancies() {
        return this.discrepanciesMapper;
    }

    public <Z> Iterator<Z> filterCurrent(final Class<Z> classFilter) {
        return this.current.filter(classFilter);
    }

    public <Z> Iterator<Z> filterDesired(final Class<Z> classFilter) {
        return this.desired.filter(classFilter);
    }

    public Object getStatusData() {
        return this.status.getRawData();
    }

}
