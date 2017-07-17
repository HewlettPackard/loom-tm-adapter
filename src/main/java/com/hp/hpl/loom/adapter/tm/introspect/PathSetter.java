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
package com.hp.hpl.loom.adapter.tm.introspect;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class PathSetter {
    private static final Log LOG = LogFactory.getLog(PathSetter.class);

    private PathSetter() {}

    private static final String PATH_FIELD = "path";
    private static final String DOT_SEP = ".";
    private static final String SLASH_SEP = "/";
    private static final String PATH_ROOT = "/main";

    public static void treeWalkSetAllPaths(final Object o, final int ignorePrefixUntilDepthLevel) {
        treeWalkSetAllPaths(o, ignorePrefixUntilDepthLevel, PATH_ROOT, 2);
    }

    public static void treeWalkSetAllPaths(final Object o, final int ignorePrefixUntilDepthLevel, final String prefix,
            final int slashDepth) {
        // Early return if null object: Nothing needs to be done.
        if (o == null) {
            return;
        }

        Class<?> oClass = o.getClass();
        Stream<Class<?>> ignoredClasses = Arrays.asList(String.class, List.class, Number.class, Boolean.class).stream();

        // If the class is a commonly ignored class, let's not waste resources introspecting it.
        if (ignoredClasses.anyMatch((c) -> c.isAssignableFrom(oClass))) {
            return;
        }

        // This try should never fail because, the field
        // access is set to true by passing the permissions checks.
        try {
            for (Field f : oClass.getFields()) {
                f.setAccessible(true);
                String fName = f.getName();
                if (PATH_FIELD.equals(fName)) {
                    if (f.get(o) == null && String.class.equals(f.getType())) {
                        f.set(o, prefix.substring(0, prefix.length()));
                    }
                } else {
                    // Introspect that field then
                    if (ignorePrefixUntilDepthLevel == 0) {
                        if (slashDepth == 0) {
                            PathSetter.treeWalkSetAllPaths(f.get(o), 0, prefix + DOT_SEP + fName, 0);
                        } else {
                            PathSetter.treeWalkSetAllPaths(f.get(o), 0, prefix + SLASH_SEP + fName, slashDepth - 1);
                        }
                    } else {
                        PathSetter.treeWalkSetAllPaths(f.get(o), ignorePrefixUntilDepthLevel - 1, prefix, slashDepth);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.error("Couldn't process field", e);
        } catch (IllegalAccessException e) {
            LOG.error("Couldn't process field", e);
        }
    }
}
