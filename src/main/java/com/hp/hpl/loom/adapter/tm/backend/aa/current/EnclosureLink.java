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
package com.hp.hpl.loom.adapter.tm.backend.aa.current;

public class EnclosureLink {
    public ObservedIncomingLanes observed_IncomingLane1;
    public ObservedIncomingLanes observed_IncomingLane2;
    public String observed_LinkHealthState;
    public Number observed_LocalLinkID;
    public ObservedOutgoingLanes observed_OutgoingLane1;
    public ObservedOutgoingLanes observed_OutgoingLane2;
    public Number observed_PortNumber;
    public Number observed_RemoteLinkID;
    public Number observed_RxBandwidth;
    public Number observed_TxBandwidth;
}
