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
package com.hp.hpl.loom.adapter.tm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.hp.hpl.loom.adapter.AdapterItem;
import com.hp.hpl.loom.adapter.annotations.ConnectedTo;
import com.hp.hpl.loom.adapter.annotations.ManyToMany;
import com.hp.hpl.loom.adapter.annotations.ManyToOne;
import com.hp.hpl.loom.adapter.annotations.OneToMany;
import com.hp.hpl.loom.adapter.annotations.Root;
import com.hp.hpl.loom.adapter.tm.items.base.DmaBaseItem;

public class DrawDOTRelations {

    private String relations = "";
    private String roots = "";

    private void addRelations(final String from, final String to, final String type) {
        String color = "blue";
        if ("ManyToOne".equals(type)) {
            color = "red";
        }
        if ("OneToMany".equals(type)) {
            color = "green";
        }
        // label =\"" + type + "\"
        relations += "\"" + from + "\"" + " -> " + "\"" + to + "\"" + "[color=\"" + color + "\"] ;\n";
    }

    public void print() {
        System.out.println("digraph g {");
        System.out.println(relations);
        System.out.println(roots);
        System.out.println("}");
    }

    public void addIfRelationshipIsPresent(final AccessibleObject fm, final Class<?> clazz) {
        if (fm.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany el = fm.getAnnotation(ManyToMany.class);
            this.addRelations(clazz.getSimpleName(), el.toClass().getSimpleName(), "ManyToMany");
        } else if (fm.isAnnotationPresent(ManyToOne.class)) {
            Field f = (Field) fm;
            this.addRelations(clazz.getSimpleName(), f.getType().getSimpleName(), "ManyToOne");
        } else if (fm.isAnnotationPresent(OneToMany.class)) {
            OneToMany el = fm.getAnnotation(OneToMany.class);
            this.addRelations(clazz.getSimpleName(), el.toClass().getSimpleName(), "OneToMany");
        }
    }

    public void addRoot(final Class<?> clazz) {
        roots += "\"" + clazz.getSimpleName() + "\"" + " [shape=box style=dotted] ;\n";
    }

    public static void main(final String[] args) {
        String pathSvg = null;
        String pathDot = null;
        File tmpDot = null;
        File tmpSvg = null;

        // /////////////////////////////////////
        // SETTINGS:
        List<Class<?>> baseType = Arrays.asList(AdapterItem.class, DmaBaseItem.class); // LhItem.class
        String packageToLookUp = "com.hp.hpl.loom.adapter.tm";
        // END SETTINGS
        // /////////////////////////////////////

        try {
            tmpDot = File.createTempFile("dotfile", ".dot");
            System.setOut(new PrintStream(tmpDot));
            pathDot = tmpDot.getAbsolutePath();
            tmpSvg = File.createTempFile("svgfile", ".svg");
            pathSvg = tmpSvg.getAbsolutePath();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Class<?>[] classes = DrawDOTRelations.getClasses(packageToLookUp);
            DrawDOTRelations dot = new DrawDOTRelations();
            for (Class<?> clazz : classes) {
                if (baseType.stream().anyMatch((c) -> c.equals(clazz.getSuperclass()))) {
                    if (clazz.isAnnotationPresent(Root.class)) {
                        dot.addRoot(clazz);
                    }
                    for (ConnectedTo el : clazz.getAnnotationsByType(ConnectedTo.class)) {
                        dot.addRelations(clazz.getSimpleName(), el.toClass().getSimpleName(), "ConnectedTo");
                    }
                    for (Field f : clazz.getDeclaredFields()) {
                        dot.addIfRelationshipIsPresent(f, clazz);
                    }
                    for (Method m : clazz.getDeclaredMethods()) {
                        dot.addIfRelationshipIsPresent(m, clazz);
                    }
                }
            }
            dot.print();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (pathDot != null && pathSvg != null) {
            try {
                Runtime run = Runtime.getRuntime();
                run.exec("dot -Tsvg -o " + pathSvg + " " + pathDot).waitFor();
                run.exec("eog " + pathSvg).waitFor();
                tmpDot.deleteOnExit();
                System.out.println("Created file at: " + pathSvg);
            } catch (IOException e) {
                System.err.println("Generated dot: " + pathDot);
                System.err.println("If dot has been found, Generated svg: " + pathSvg);
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package
     * and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class<?>[] getClasses(final String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(DrawDOTRelations.findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(final File directory, final String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(DrawDOTRelations.findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                final int six = 6;
                classes.add(
                        Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - six)));
            }
        }
        return classes;
    }
}
