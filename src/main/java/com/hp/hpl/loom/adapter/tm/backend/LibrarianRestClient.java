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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hp.hpl.loom.adapter.tm.backend.librarian.Book;
import com.hp.hpl.loom.adapter.tm.backend.librarian.InterleaveGroup;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianBooks;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobal;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalActive;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalMemory;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalPools;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianGlobalSocs;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianInterleaveGroups;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianMemory;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianMemory.MemoryDetails;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianShelf;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianShelfEntry;
import com.hp.hpl.loom.adapter.tm.backend.librarian.LibrarianShelves;
import com.hp.hpl.loom.adapter.tm.backend.librarian.Shelf;

public class LibrarianRestClient {

    private static final int FAKE_BOOKS_PER_NODE = 512;
    private static final int FAKE_NUM_NODES = 40;
    private static final long FAKE_BOOK_SIZE = 8L * 1024 * 1024 * 1024;
    @SuppressWarnings("checkstyle:magicnumber")
    private static final int FAKE_POOLS_TOTAL = (FAKE_NUM_NODES * FAKE_BOOKS_PER_NODE * 8) / 1024;
    private static final int FAKE_POOLS_ACTIVE = FAKE_POOLS_TOTAL;
    private static final int FAKE_POOLS_OFFLINE = 0;

    private static final Log LOG = LogFactory.getLog(LibrarianRestClient.class);
    private static final int NUM_THREADS = 6;
    private RestTemplate client;
    private HttpEntity<String> entity;
    private String host;
    private Long bookSize;
    private LibrarianGlobal global;
    private Map<String, MemoryDetails> memoryByCoordinate;
    private List<InterleaveGroup> interleaveGroups;
    private List<Book> books;
    private List<Shelf> shelves;

    public LibrarianRestClient(final String host) {
        client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json; version=1.0");
        headers.add("Content-Type", "application/json; charset=utf-8; version=1.0");
        entity = new HttpEntity<String>("parameters", headers);
        this.host = host;
        memoryByCoordinate = new HashMap<String, MemoryDetails>();
        interleaveGroups = Collections.emptyList();
        books = Collections.emptyList();
        shelves = new ArrayList<>();
    }

    /**
     * Calls the Librarian REST interface to refresh all the data
     */
    public void refreshResult() {
        long t0 = System.currentTimeMillis();
        ExecutorService threadsPool = Executors.newFixedThreadPool(NUM_THREADS);
        threadsPool.submit(globalRefresher);
        threadsPool.submit(bookSizeRefresher);
        threadsPool.submit(interleaveGroupsRefresher);
        threadsPool.submit(booksRefresher);
        threadsPool.submit(shelvesRefresher);
        threadsPool.submit(memoryRefresher);
        threadsPool.shutdown();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian refresh awaiting termination");
            }

            threadsPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian refresh terminated");
            }
        } catch (InterruptedException e) {
            LOG.error("Librarian refresher threads pool awating was interrupted: ", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Librarian refresh total execution time: " + (System.currentTimeMillis() - t0) + " ms");
        }
    }

    private Callable<Boolean> memoryRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            refreshMemory();
            return true;
        }
    };

    private Callable<Boolean> globalRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            long t0 = System.currentTimeMillis();

            global = getGlobalRestCall();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian global execution time: " + (System.currentTimeMillis() - t0) + " ms");
            }

            return true;
        }
    };

    private Callable<Boolean> bookSizeRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            long t0 = System.currentTimeMillis();

            bookSize = getBookSize();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian book size execution time: " + (System.currentTimeMillis() - t0) + " ms");
            }

            return true;
        }
    };

    private Callable<Boolean> interleaveGroupsRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            long t0 = System.currentTimeMillis();

            interleaveGroups = getInterleaveGroupsRestCall();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian interleave groups execution time: " + (System.currentTimeMillis() - t0) + " ms");
            }

            return true;
        }
    };

    private Callable<Boolean> booksRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            long t0 = System.currentTimeMillis();

            books = getBooksRestCall();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian books execution time: " + (System.currentTimeMillis() - t0) + " ms");
            }

            return true;
        }
    };

    private Callable<Boolean> shelvesRefresher = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            long t0 = System.currentTimeMillis();

            shelves = getShelvesRestCall();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian shelves execution time: " + (System.currentTimeMillis() - t0) + " ms");
            }

            return true;
        }
    };

    private boolean refreshMemory() {
        long t0 = System.currentTimeMillis();

        for (String coord : memoryByCoordinate.keySet()) {
            MemoryDetails mem = getMemoryRestCall(coord);
            if (mem == null) {
                LOG.info("Refresh memory aborted, because couldn't get memory for coordinate " + coord);
                return false;
            }
            memoryByCoordinate.put(coord, mem);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Librarian memory execution time: " + (System.currentTimeMillis() - t0) + " ms");
        }

        return true;
    }

    public MemoryDetails getMemoryFromCoordinate(final String coordinate) {
        if (memoryByCoordinate.containsKey(coordinate)) {
            return memoryByCoordinate.get(coordinate);
        }
        MemoryDetails mem = new MemoryDetails(0, 0, 0, 0, 0);
        memoryByCoordinate.put(coordinate, mem);
        return mem;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Shelf> getShelves() {
        return shelves;
    }

    public List<InterleaveGroup> getInterleaveGroups() {
        return interleaveGroups;
    }

    public Long getBookSize() {
        if (bookSize == null) {
            bookSize = getBookSizeRestCall();
        }
        return bookSize;
    }

    public LibrarianGlobal getGlobal() {
        return global;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public LibrarianGlobal getFakeGlobal() {
        LibrarianGlobal fakeGlobal = new LibrarianGlobal();

        fakeGlobal.active = new LibrarianGlobalActive(FAKE_NUM_NODES, FAKE_BOOKS_PER_NODE * FAKE_NUM_NODES);

        long fakeTotalMemory = FAKE_BOOKS_PER_NODE * FAKE_NUM_NODES * FAKE_BOOK_SIZE;
        long fakeAllocatedMemory = Math.round(fakeTotalMemory * 0.7321);
        long fakeAvailableMemory = Math.round((fakeTotalMemory - fakeAllocatedMemory) * 0.82);
        long fakeNotReadyMemory = fakeTotalMemory - fakeAllocatedMemory - fakeAvailableMemory;
        long fakeOfflineMemory = 0;

        fakeGlobal.memory = new LibrarianGlobalMemory(fakeTotalMemory, fakeAllocatedMemory, fakeAvailableMemory,
                fakeNotReadyMemory, fakeOfflineMemory);

        fakeGlobal.pools = new LibrarianGlobalPools(FAKE_POOLS_TOTAL, FAKE_POOLS_ACTIVE, FAKE_POOLS_OFFLINE);
        fakeGlobal.socs = new LibrarianGlobalSocs(FAKE_NUM_NODES, FAKE_NUM_NODES, 0);

        return fakeGlobal;
    }

    private List<Book> getBooksRestCall() {
        try {
            Iterator<InterleaveGroup> iter = getInterleaveGroupsRestCall().iterator();
            LibrarianBooks libBooks = new LibrarianBooks();
            libBooks.books = new ArrayList<>();
            while (iter.hasNext()) {
                InterleaveGroup interleaveGroup = iter.next();
                Integer groupId = interleaveGroup.groupId;
                ResponseEntity<LibrarianBooks> response =
                        client.exchange(host + "/books/" + groupId, HttpMethod.GET, entity, LibrarianBooks.class);
                LibrarianBooks lbs = response.getBody();
                if (libBooks.book_size == null) {
                    libBooks.book_size = lbs.book_size;
                }
                for (int i = 0; i < lbs.books.size(); i++) {
                    lbs.books.get(i).interleaveGroupId = groupId;
                }
                libBooks.books.addAll(lbs.books);
            }
            return libBooks.books;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return Collections.emptyList();
        }
    }

    private List<Shelf> getShelvesRestCall() {
        LibrarianShelves libShelves = getLibrarianShelvesRestCall();
        List<Shelf> shlvs = new ArrayList<>();
        LOG.info("BETTY ENTER getShelvesRestCall");
        if (libShelves == null) {
            return shlvs;
        }
        for (int i = 0; i < libShelves.entries.size(); i++) {
            LibrarianShelfEntry libShelfEntry = libShelves.entries.get(i);
            long t0 = System.currentTimeMillis();

            LibrarianShelf libShelf = getLibrarianShelfRestCall(libShelfEntry.name);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Librarian shelf details execution time: " + (System.currentTimeMillis() - t0) + " ms");
            }

            Shelf shelf = new Shelf();
            shelf.name = libShelfEntry.name;
            shelf.active = libShelf.active;
            shelf.books = libShelf.books;
            shelf.booksize = libShelf.booksize;
            shelf.group = libShelf.group;
            shelf.mode = libShelf.mode;
            shelf.owner = libShelf.owner;
            shelf.policy = libShelf.policy;
            shelf.size = libShelf.size;
            shelf.type = libShelfEntry.type;
            // shelf.interleaveGroupIds = getShelfInterleaveGroupsRestCall(libShelfEntry.name);
            shlvs.add(shelf);
        }
        LOG.info("BETTY LEAVING getShelvesRestCall");
        return shlvs;
    }

    // private List<Integer> getShelfInterleaveGroupsRestCall(String name) {
    // Iterator<InterleaveGroup> iter = getInterleaveGroupsRestCall().iterator();
    // List<Integer> interleaveGroupIds = new ArrayList<>();
    // while (iter.hasNext()) {
    // InterleaveGroup interleaveGroup = iter.next();
    // ResponseEntity<LibrarianBooks> response = client.exchange(host + "/books/" +
    // interleaveGroup.groupId,
    // HttpMethod.GET, entity, LibrarianBooks.class);
    // LibrarianBooks libBooks = response.getBody();
    // Iterator<Book> iterBook = libBooks.books.iterator();
    //
    // while (iterBook.hasNext()) {
    // Book book = iterBook.next();
    // if (book.shelf != null && book.shelf.equals(name)) {
    // interleaveGroupIds.add(interleaveGroup.groupId);
    // break;
    // }
    // }
    // }
    // return interleaveGroupIds;
    // }

    private LibrarianShelves getLibrarianShelvesRestCall() {
        try {
            ResponseEntity<LibrarianShelves> response =
                    client.exchange(host + "/shelf/", HttpMethod.GET, entity, LibrarianShelves.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return new LibrarianShelves(Collections.emptyList(), 0, "", 0);
        }
    }

    private LibrarianShelf getLibrarianShelfRestCall(final String name) {
        try {
            ResponseEntity<LibrarianShelf> response =
                    client.exchange(host + "/shelf/" + name, HttpMethod.GET, entity, LibrarianShelf.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return new LibrarianShelf(Collections.emptyList(), Collections.emptyList(), 0L, 0, 0, 0, "", 0L);
        }
    }

    private List<InterleaveGroup> getInterleaveGroupsRestCall() {
        try {
            ResponseEntity<LibrarianInterleaveGroups> response = client.exchange(host + "/interleaveGroups/",
                    HttpMethod.GET, entity, LibrarianInterleaveGroups.class);
            return response.getBody().interleaveGroups;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return Collections.emptyList();
        }
    }

    private MemoryDetails getMemoryRestCall(final String coordinate) {
        try {
            ResponseEntity<LibrarianMemory> response =
                    client.exchange(host + "/allocated/" + coordinate, HttpMethod.GET, entity, LibrarianMemory.class);
            return response.getBody().memory;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return null;
        }
    }

    private Long getBookSizeRestCall() {
        try {
            ResponseEntity<LibrarianBooks> response =
                    client.exchange(host + "/books/", HttpMethod.GET, entity, LibrarianBooks.class);
            LibrarianBooks libBooks = response.getBody();
            return libBooks.book_size;
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return 0L;
        }
    }

    private LibrarianGlobal getGlobalRestCall() {
        try {
            ResponseEntity<LibrarianGlobal> response =
                    client.exchange(host + "/global/", HttpMethod.GET, entity, LibrarianGlobal.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("Fail to communication with Librarian LMP server: ", e);
            return new LibrarianGlobal(new LibrarianGlobalMemory(0L, 0L, 0L, 0L, 0L), new LibrarianGlobalSocs(0, 0, 0),
                    new LibrarianGlobalPools(0, 0, 0), new LibrarianGlobalActive(0, 0));
        }
    }
}
