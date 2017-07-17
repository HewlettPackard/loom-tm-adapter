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

import java.util.HashMap;
import java.util.Iterator;

import com.hp.hpl.loom.adapter.tm.backend.BackEndUpdater;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Enclosure;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.IZoneBoard;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Instance;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.MemoryBoard;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Node;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.Rack;
import com.hp.hpl.loom.adapter.tm.backend.aa.discrepancies.SocBoard;

public class DmaDiscrepanciesMapper<R> {

    private HashMap<String, Instance> instanceDiscMap;
    private HashMap<String, Enclosure> enclosureDiscMap;
    private HashMap<String, IZoneBoard> izoneBoardDiscMap;
    private HashMap<String, MemoryBoard> memoryBoardDiscMap;
    private HashMap<String, Rack> rackDiscMap;
    private HashMap<String, SocBoard> socBoardDiscMap;
    private HashMap<String, Node> nodeDiscMap;

    private void resetMaps() {
        instanceDiscMap = new HashMap<String, Instance>();
        enclosureDiscMap = new HashMap<String, Enclosure>();
        izoneBoardDiscMap = new HashMap<String, IZoneBoard>();
        memoryBoardDiscMap = new HashMap<String, MemoryBoard>();
        rackDiscMap = new HashMap<String, Rack>();
        socBoardDiscMap = new HashMap<String, SocBoard>();
        nodeDiscMap = new HashMap<String, Node>();
    }

    protected void refreshMapping(BackEndUpdater<R> discrepancies) {
        resetMaps();
        Iterator<Instance> instanceDiscIter = discrepancies.filter(Instance.class);
        while (instanceDiscIter.hasNext()) {
            Instance instDisc = instanceDiscIter.next();
            instanceDiscMap.put(instDisc.path, instDisc);
        }
        Iterator<Enclosure> enclosureDiscIter = discrepancies.filter(Enclosure.class);
        while (enclosureDiscIter.hasNext()) {
            Enclosure encDisc = enclosureDiscIter.next();
            enclosureDiscMap.put(encDisc.path, encDisc);
        }
        Iterator<IZoneBoard> izoneDiscIter = discrepancies.filter(IZoneBoard.class);
        while (izoneDiscIter.hasNext()) {
            IZoneBoard izoneDisc = izoneDiscIter.next();
            izoneBoardDiscMap.put(izoneDisc.path, izoneDisc);
        }
        Iterator<MemoryBoard> memboardDiscIter = discrepancies.filter(MemoryBoard.class);
        while (memboardDiscIter.hasNext()) {
            MemoryBoard memboardDisc = memboardDiscIter.next();
            memoryBoardDiscMap.put(memboardDisc.path, memboardDisc);
        }
        Iterator<Rack> rackDiscIter = discrepancies.filter(Rack.class);
        while (rackDiscIter.hasNext()) {
            Rack rackDisc = rackDiscIter.next();
            rackDiscMap.put(rackDisc.path, rackDisc);
        }
        Iterator<SocBoard> socBoardDiscIter = discrepancies.filter(SocBoard.class);
        while (socBoardDiscIter.hasNext()) {
            SocBoard socBoardDisc = socBoardDiscIter.next();
            socBoardDiscMap.put(socBoardDisc.path, socBoardDisc);
        }
        Iterator<Node> nodeDiscIter = discrepancies.filter(Node.class);
        while (nodeDiscIter.hasNext()) {
            Node nodeDisc = nodeDiscIter.next();
            nodeDiscMap.put(nodeDisc.path, nodeDisc);
        }
    }

    public Instance getInstanceDiscrepanciesByPath(String path) {
        return instanceDiscMap.get(path);
    }

    public Enclosure getEnclosureDiscrepanciesByPath(String path) {
        return enclosureDiscMap.get(path);
    }

    public IZoneBoard getIzoneDiscrepanciesByPath(String path) {
        return izoneBoardDiscMap.get(path);
    }

    public MemoryBoard getMemoryBoardDiscrepanciesByPath(String path) {
        return memoryBoardDiscMap.get(path);
    }

    public Rack getRackDiscrepanciesByPath(String path) {
        return rackDiscMap.get(path);
    }

    public SocBoard getSocBoardDiscrepanciesByPath(String path) {
        return socBoardDiscMap.get(path);
    }

    public Node getNodeDiscrepanciesByPath(String path) {
        return nodeDiscMap.get(path);
    }

}
