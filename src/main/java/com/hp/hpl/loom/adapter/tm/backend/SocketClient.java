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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.loom.adapter.tm.util.ThrottleLogger;

public class SocketClient {

    private static final Log LOG = new ThrottleLogger(LogFactory.getLog(SocketClient.class), 10000L);
    private Socket socket;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private boolean requireReconnect = false;

    public SocketClient() {}

    /**
     * Connect to the given server. This method must be called every time
     *
     * @param server Server to connect
     * @return Returns true if connection worked, false if it failed
     */
    public boolean connect(final String server) {
        if (!this.shouldTryAReconnect()) {
            return true;
        }
        String[] vals = server.split(":");
        if (vals.length != 2) {
            throw new Error("Server format is not correct.");
        }
        Integer port = Integer.parseInt(vals[1]);
        String host = vals[0];

        try {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requireReconnect = false;
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            socket = null;
        }

        return false;
    }

    /**
     * Returns true if a connect should be re-runned.
     */
    private boolean shouldTryAReconnect() {
        return requireReconnect || socket == null || !socket.isConnected() || socket.isClosed();
    }

    /**
     * @return Returns true if the socket is ready.
     */
    public boolean isReady() {
        return !requireReconnect;
    }

    /**
     * Get all result from the backend assuming result from the socket are JSON like.
     *
     * @param command is a string that will be sent to the backend through the socket followed by a
     *        `\n`
     * @param expectedType Expected result type.
     * @param <T> expected class type
     * @param <U> command class type
     * @return Returns the expected type or null if something failed
     */
    public <T, U> T getAll(final U command, final Class<T> expectedType) {
        if (socket == null || socket.isClosed()) {
            LOG.error("Socket has been closed.");
            try {
                return expectedType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.error("Couldn't create new instance of expected type", e);
                return null;
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.AUTO_CLOSE_SOURCE, false);
        try {
            toServer.println(mapper.writeValueAsString(command));
            JsonNode response = mapper.readTree(fromServer);
            if (response.get("errno") == null) {
                return mapper.convertValue(response, expectedType);
            }
        } catch (IOException e) {
            LOG.error("Librarian is not reachable (check librarian running in vagrant): " + e.getMessage());
            requireReconnect = true;
        }
        try {
            return expectedType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Couldn't create new instance of expected type", e);
            return null;
        }
    }
}
