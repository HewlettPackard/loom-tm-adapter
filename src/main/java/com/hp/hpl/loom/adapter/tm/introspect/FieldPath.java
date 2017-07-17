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
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * When you need to introspect a type to get an iterator over all items that match your type, this
 * is the class you want to use. Everything assumes that the type being introspected is a tree of
 * types with no back-references.
 *
 * This is very handy when during development, the tree structure change a lot but the data doesn't.
 * You have far less refactoring to do.
 */
public final class FieldPath {
    private static final Log LOG = LogFactory.getLog(FieldPath.class);

    /**
     * Access an Iterator of values that are of the appropriate type contained within the object o.
     *
     * @param o is the object we want to get an iterator from.
     * @return Returns the list of values.
     */
    public Iterator<?> getValue(final Object o) {
        if (o == null) {
            return Collections.emptyIterator();
        }
        if (!o.getClass().equals(treeType)) {
            throw new IllegalArgumentException("Given object is of wrong type.");
        }
        try {
            if (treeContainerKind == ContainerKind.ARRAY) {
                Object[] array = (Object[]) o;

                if (isCompType) {
                    return Arrays.asList(array).iterator();
                }

                if (path.length > 0) {

                    IteratorChain<Object> chains = new IteratorChain<Object>();

                    for (Object child : array) {
                        Iterator<?> iter = this.getValueForIter(child, 0);
                        if (iter != null) {
                            chains.addIterator(iter);
                        }
                    }

                    return chains;
                }

            } else {
                if (path.length > 0) {
                    Iterator<?> iter = this.getValueForIter(o, 0);
                    if (iter != null) {
                        return iter;
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOG.error("Couldn't get value", e);
        }
        return Collections.emptyIterator();
    }

    /**
     * This function look for for the given type `against` in the type `tree`. It assumes that the
     * type will be present at most once and thus stop on first match.
     *
     * @param tree is the type tree that will be inspected.
     * @param against is the type we're looking for.
     * @return Returns a FieldPath object that can be used to lookup a value of Type `against`
     *         inside a value of Type `tree`
     */
    public static FieldPath introspect(final Class<?> tree, final Class<?> against) {

        List<FieldPath.AnyFieldEl> path = new ArrayList<FieldPath.AnyFieldEl>();
        ContainerKind kind = ContainerKind.NONE;
        boolean isCompType = false;

        if (tree.isArray()) {
            kind = ContainerKind.ARRAY;
            Class<?> compType = tree.getComponentType();
            if (!compType.equals(against)) {
                introspectAux(compType, against, path, new HashMap<>());
            } else {
                isCompType = true;
            }
        } else {
            introspectAux(tree, against, path, new HashMap<>());
        }

        return new FieldPath(path, tree, kind, isCompType);
    }

    // ------------------------------------------------------------------ //
    // --------------------- PRIVATE INTERFACE -------------------------- //
    // ------------------------------------------------------------------ //

    private static enum ContainerKind {
        LIST, ARRAY, NONE
    }

    @SuppressWarnings("checkstyle:visibilitymodifier")
    private abstract static class AnyFieldEl {
        public ContainerKind containerKind;
    }

    @SuppressWarnings("checkstyle:visibilitymodifier")
    private static class FieldEl extends AnyFieldEl {
        public Field field;

        public FieldEl(final Field field, final ContainerKind containerKind) {
            this.field = field;
            this.containerKind = containerKind;
        }
    }

    @SuppressWarnings("checkstyle:visibilitymodifier")
    private static class MultiFieldEl extends AnyFieldEl {
        public List<Field> fields;

        public MultiFieldEl(final List<Field> fields, final ContainerKind containerKind) {
            this.fields = new ArrayList<>(fields);
            this.containerKind = containerKind;
        }
    }

    private AnyFieldEl[] path;
    private Class<?> treeType;
    private ContainerKind treeContainerKind;
    private boolean isCompType;

    private FieldPath(final List<AnyFieldEl> from, final Class<?> treeType, final ContainerKind kind,
            final boolean isCompType) {
        path = new AnyFieldEl[from.size()];
        ListIterator<AnyFieldEl> iter = from.listIterator(from.size());
        int i = 0;
        while (iter.hasPrevious()) {
            path[i++] = iter.previous();
        }
        this.treeType = treeType;
        treeContainerKind = kind;
        this.isCompType = isCompType;
    }

    private boolean isList(final AnyFieldEl el) {
        return el.containerKind == ContainerKind.LIST;
    }

    private boolean isArray(final AnyFieldEl el) {
        return el.containerKind == ContainerKind.ARRAY;
    }

    private Iterator<?> getValueForIter(final Object o, final int i) throws IllegalAccessException {
        if (o == null) {
            return null;
        }
        // Reached end of path
        if (i == path.length - 1) {

            if (path[i] instanceof FieldEl) {

                // Simple field
                if (isList(path[i])) {
                    List<?> list = (List<?>) ((FieldEl) path[i]).field.get(o);
                    if (list != null) {
                        return list.iterator();
                    }
                } else if (isArray(path[i])) {
                    Object[] values = (Object[]) ((FieldEl) path[i]).field.get(o);
                    return Arrays.asList(values).iterator();
                } else {
                    Object el = ((FieldEl) path[i]).field.get(o);
                    if (el != null) {
                        return Collections.singletonList(el).iterator();
                    }
                }
                return null;

            } else {

                // Multi field
                MultiFieldEl multi = (MultiFieldEl) path[i];
                if (isList(path[i])) {
                    IteratorChain<Object> chains = new IteratorChain<Object>();

                    for (Field field : multi.fields) {
                        List<?> list = (List<?>) field.get(o);
                        if (list != null) {
                            chains.addIterator(list.iterator());
                        }
                    }

                    return chains;
                } else if (isArray(path[i])) {
                    IteratorChain<Object> chains = new IteratorChain<Object>();

                    for (Field field : multi.fields) {
                        Object[] array = (Object[]) field.get(o);
                        if (array != null) {
                            chains.addIterator(Arrays.asList(array).iterator());
                        }
                    }

                    return chains;
                } else {
                    List<Object> res = new ArrayList<>(multi.fields.size());

                    for (Field field : multi.fields) {
                        Object value = field.get(o);
                        if (value != null) {
                            res.add(field.get(o));
                        }
                    }

                    return res.iterator();
                }
            }
        } else {

            if (path[i] instanceof FieldEl) {

                // simple field
                if (isList(path[i])) {
                    IteratorChain<Object> chains = new IteratorChain<Object>();
                    List<?> list = (List<?>) ((FieldEl) path[i]).field.get(o);

                    if (list != null) {
                        for (Object child : list) {
                            Iterator<?> res = this.getValueForIter(child, i + 1);
                            if (res != null) {
                                chains.addIterator(res);
                            }
                        }
                    }

                    return chains;
                } else if (isArray(path[i])) {
                    IteratorChain<Object> chains = new IteratorChain<Object>();
                    Object[] array = (Object[]) ((FieldEl) path[i]).field.get(o);

                    if (array != null) {
                        for (Object child : array) {
                            Iterator<?> res = this.getValueForIter(child, i + 1);
                            if (res != null) {
                                chains.addIterator(res);
                            }
                        }
                    }

                    return chains;
                } else {
                    return this.getValueForIter(((FieldEl) path[i]).field.get(o), i + 1);
                }
            } else {

                // MultiField
                MultiFieldEl multi = (MultiFieldEl) path[i];

                IteratorChain<Object> chains = new IteratorChain<Object>();

                for (Field field : multi.fields) {
                    if (isList(path[i])) {
                        List<?> list = (List<?>) field.get(o);
                        if (list != null) {
                            for (Object child : list) {
                                Iterator<?> res = this.getValueForIter(child, i + 1);
                                if (res != null) {
                                    chains.addIterator(res);
                                }
                            }
                        }
                    } else if (isArray(path[i])) {
                        Object[] array = (Object[]) field.get(o);
                        if (array != null) {
                            for (Object child : array) {
                                Iterator<?> res = this.getValueForIter(child, i + 1);
                                if (res != null) {
                                    chains.addIterator(res);
                                }
                            }
                        }
                    } else {
                        Object child = field.get(o);
                        Iterator<?> res = this.getValueForIter(child, i + 1);
                        if (res != null) {
                            chains.addIterator(res);
                        }
                    }
                }

                return chains;
            }
        }
    }

    private static FieldPath.AnyFieldEl updateWith(final FieldPath.AnyFieldEl fieldEl, final Field f,
            final ContainerKind contk) {
        if (fieldEl == null) {
            return new FieldPath.FieldEl(f, contk);
        } else if (fieldEl instanceof FieldPath.FieldEl) {
            return new FieldPath.MultiFieldEl(Arrays.asList(((FieldPath.FieldEl) fieldEl).field, f), contk);
        } else {
            ((FieldPath.MultiFieldEl) fieldEl).fields.add(f);
            return fieldEl;
        }
    }

    private static boolean introspectAux(final Class<?> tree, final Class<?> against,
            final List<FieldPath.AnyFieldEl> currentPath, final Map<Class<?>, Boolean> visitedTypes) {

        Boolean state = visitedTypes.get(tree);
        if (state != null) {
            return state;
        }

        FieldPath.AnyFieldEl fieldEl = null;

        // This code assumes a POJO
        // which is not necessarily true
        for (Field f : tree.getDeclaredFields()) {
            Class<?> childClazz = f.getType();
            // Did we found the class ? If yes, then stop.
            if (childClazz.equals(against)) {
                fieldEl = updateWith(fieldEl, f, ContainerKind.NONE);
                continue;
            } else if (childClazz.isArray()) {
                // Is the given class an array?
                Class<?> compType = childClazz.getComponentType();

                if (compType.equals(against)) {
                    fieldEl = updateWith(fieldEl, f, ContainerKind.ARRAY);
                } else if (introspectAux(compType, against, currentPath, visitedTypes)) {
                    fieldEl = updateWith(fieldEl, f, ContainerKind.ARRAY);
                }
                // Okay, let's move forward to the next child then.
                continue;
            } else if (List.class.isAssignableFrom(childClazz)) {
                // Does the given type is a list ? If yes, then we switch to the template type.
                Class<?> typeParamater =
                        (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];

                if (typeParamater.equals(against)) {
                    fieldEl = updateWith(fieldEl, f, ContainerKind.LIST);
                } else if (introspectAux(typeParamater, against, currentPath, visitedTypes)) {
                    fieldEl = updateWith(fieldEl, f, ContainerKind.LIST);
                }
                // Okay, let's move forward to the next child then.
                continue;
            } else if (childClazz.equals(String.class) || Number.class.isAssignableFrom(childClazz)
                    || childClazz.equals(Boolean.class)) {
                // Ignore class.
                continue;
            } else if (introspectAux(childClazz, against, currentPath, visitedTypes)) {
                // Is the type in our child branch ? If yes then stop.
                fieldEl = updateWith(fieldEl, f, ContainerKind.NONE);
                continue;
            }
            // Okay, let's move forward to the next child then.
        }

        // Nothing has been found ? Return false then.
        if (fieldEl == null) {
            visitedTypes.put(tree, false);
            return false;
        }

        visitedTypes.put(tree, true);
        currentPath.add(fieldEl);
        return true;
    }
}
