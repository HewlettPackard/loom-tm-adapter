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
package com.hp.hpl.loom.adapter.tm.backend.aa.desired;

public class DesiredRack {
    public Desired<String> coordinate;
    public Desired<String> allFabricPow;
    public Desired<String> allNodeFamPow;
    public Desired<String> allNodeSocPow;
    public Desired<String> defaultNextOsImageManifest;
    public Desired<String> defaultRunningOsImageManifest;
    public BooleanWithTbd enableNonGracefulShutdown;
    public DesiredEnclosure enclosure1;
    public DesiredEnclosure enclosure2;
    public DesiredEnclosure enclosure3;
    public DesiredEnclosure enclosure4;
    public DesiredEnclosure enclosure5;
    public DesiredEnclosure enclosure6;
    public DesiredEnclosure enclosure7;
    public DesiredEnclosure enclosure8;
    public Desired<Boolean> forceAllFabricSocOff;
    public Desired<Boolean> forceAllFamFabricSocOff;
    public Desired<Boolean> forceAllSocOff;
    public Desired<String> power;
}
