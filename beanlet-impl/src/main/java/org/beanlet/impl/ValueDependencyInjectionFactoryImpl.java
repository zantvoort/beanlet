/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Beanlet - JSE Application Container.
 * Copyright (C) 2006  Leon van Zantvoort
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 * 
 * Leon van Zantvoort
 * 243 Acalanes Drive #11
 * Sunnyvale, CA 94086
 * USA
 *
 * zantvoort@users.sourceforge.net
 * http://beanlet.org
 */
package org.beanlet.impl;

import org.beanlet.common.ParameterizedTypeAwareDependencyInjectionFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletTypeMismatchException;
import org.beanlet.BeanletWiringException;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.CollectionValue;
import org.beanlet.Entry;
import org.beanlet.IgnoreDependency;
import org.beanlet.MapValue;
import org.beanlet.Value;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 *
 * @author Leon van Zantvoort
 */
public final class ValueDependencyInjectionFactoryImpl extends
        ParameterizedTypeAwareDependencyInjectionFactory {
    
    private final BeanletConfiguration<?> configuration;
    
    public ValueDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        Inject inject = ea.getAnnotation();
        Value value = inject.value();
        CollectionValue collection = inject.collection();
        MapValue map = inject.map();
        Class<?> target = getType(ea.getElement());
        final boolean supported;
        if (!inject.ref().equals("") || value.nill() || 
                !value.value().equals("") || value.empty() || 
                !value.ref().equals("")) {
            supported = true;
            Class<?> selectedType = getType(inject);
            if (inject.ref().equals("")) {
                if (target.isPrimitive() && value.nill()) {
                    throw new BeanletWiringException(
                            configuration.getComponentName(),
                            ea.getElement().getMember(),
                            "Primitive cannot be injected with null value.");
                }            
                if (!Object.class.equals(value.type())) {
                    selectedType = value.type();
                }
            }
            if (!Object.class.equals(selectedType)) {
                final Class<?> objectType;
                if (target.isPrimitive()) {
                    if (Boolean.TYPE.equals(target)) {
                        objectType = Boolean.class;
                    } else if (Byte.TYPE.equals(target)) {
                        objectType = Byte.class;
                    } else if (Short.TYPE.equals(target)) {
                        objectType = Short.class;
                    } else if (Integer.TYPE.equals(target)) {
                        objectType = Integer.class;
                    } else if (Long.TYPE.equals(target)) {
                        objectType = Long.class;
                    } else if (Float.TYPE.equals(target)) {
                        objectType = Float.class;
                    } else if (Double.TYPE.equals(target)) {
                        objectType = Double.class;
                    } else {
                        objectType = target;
                    }
                } else {
                    objectType = target;
                }
                if (!objectType.isAssignableFrom(selectedType)) {
                    throw new BeanletTypeMismatchException(
                            configuration.getComponentName(),
                            ea.getElement().getMember(),
                            target, selectedType);
                }
            }
        } else if (collection.value().length > 0 || collection.empty()) {
            supported = true;
            if (!collection.type().equals(Collection.class)) {
                if (!target.isAssignableFrom(collection.type())) {
                    throw new BeanletTypeMismatchException(
                            configuration.getComponentName(),
                            ea.getElement().getMember(),
                            target, collection.type());
                }
            }
        } else if (map.value().length > 0 || map.empty()) {
            supported = true;
            if (!map.type().equals(Map.class)) {
                if (!target.isAssignableFrom(map.type())) {
                    throw new BeanletTypeMismatchException(
                            configuration.getComponentName(),
                            ea.getElement().getMember(),
                            target, map.type());
                }
            }
        } else {
            supported = false;
        }
        return supported;
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        final Set<String> dependencies;
        AnnotationDomain domain = configuration.getAnnotationDomain();
        if (domain.getDeclaration(IgnoreDependency.class).
                isAnnotationPresent(ea.getElement())) {
            dependencies = Collections.emptySet();
        } else {
            Set<String> tmp = new HashSet<String>();
            Inject inject = ea.getAnnotation();
            if (!inject.ref().equals("")) {
                tmp.add(inject.ref());
            }
            MapValue map = inject.map();
            for (Entry entry : map.value()) {
                if (!entry.key().ref().equals("")) {
                    tmp.add(entry.key().ref());
                }
                if (!entry.value().ref().equals("")) {
                    tmp.add(entry.value().ref());
                }
            }
            CollectionValue collection = inject.collection();
            for (Value value : collection.value()) {
                if (!value.ref().equals("")) {
                    tmp.add(value.ref());
                }
            }
            Value value = inject.value();
            if (!value.ref().equals("")) {
                tmp.add(value.ref());
            }
            dependencies = Collections.unmodifiableSet(tmp);
        }
        return dependencies;
    }

    private Class<?> getCollectionValueType(Element element) {
        return getTypeClass(getParameterizedType(element, 0, Collection.class));
    }
    
    private Class<?> getMapKeyType(Element element) {
        return getTypeClass(getParameterizedType(element, 0, Map.class));
    }
    
    private Class<?> getMapValueType(Element element) {
        return getTypeClass(getParameterizedType(element, 1, Map.class));
    }
    
    @SuppressWarnings("unchecked")
    public Injectant<?> getInjectant(
            ElementAnnotation<? extends Element, Inject> ea, 
            ComponentContext<?> ctx) {
        String name = configuration.getComponentName();
        Inject inject = ea.getAnnotation();
        Value value = inject.value();
        if (!inject.ref().equals("")) { 
            return new InjectantImpl<Object>(
                    getValue(inject, getType(ea), ea.getElement()),
                    false);
        }
        if (value.nill() || !value.ref().equals("") || 
                !value.value().equals("") || value.empty()) {
            return new InjectantImpl<Object>(
                    getValue(value, getType(ea), ea.getElement()),
                    false);
        }
        try {
            CollectionValue collection = inject.collection();
            if (collection.value().length > 0 || collection.empty()) {
                Class<?> type = getType(ea.getElement());
                final Class<?> componentType;
                if (type.isArray()) {
                    componentType = type.getComponentType();
                } else {
                    componentType = getCollectionValueType(ea.getElement());
                }

                Collection c;
                if (collection.type().equals(Collection.class)) {
                    try {
                        try {
                            Constructor constr = type.getConstructor();
                            c = (Collection) constr.newInstance();
                        } catch (NoSuchMethodException e) {
                            if (type.isAssignableFrom(Collection.class)) {
                                c = new ArrayList();
                            } else if (type.isAssignableFrom(List.class)) {
                                c = new ArrayList();
                            } else if (type.isAssignableFrom(Set.class)) {
                                c = new HashSet();
                            } else if (type.isAssignableFrom(SortedSet.class)) {
                                c = new TreeSet();
                            } else {
                                throw new BeanletWiringException(name, 
                                        ea.getElement().getMember(),
                                        "Unable to select collection type for: '" + 
                                        type.getName() + "'.");
                            }
                        }
                    } catch (InvocationTargetException e) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), e);
                    } catch (InstantiationException e) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), e);
                    } catch (IllegalAccessException e) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), e);
                    }
                } else {
                    if (collection.type().isInterface()) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), "Type must not be " +
                                "an interface: '" + collection.type().getName() + "'.");
                    }
                    c = (Collection) collection.type().newInstance();
                }
                if (!collection.empty()) {
                    Collection<Object> tmp = (Collection<Object>) 
                            Collections.checkedCollection(c, componentType);
                    for (Value v : collection.value()) {
                        Object o = getValue(v, componentType, ea.getElement());
                        tmp.add(o);
                    }
                }
                if (type.isArray()) {
                    Object[] a = (Object[]) Array.newInstance(componentType, 
                            c.size());
                    Collection<Object> tmp = (Collection<Object>) c;
                    return new InjectantImpl<Object>(tmp.toArray(a), false);
                } else {
                    try {
                        if (collection.synced()) {
                            if (type.isAssignableFrom(Collection.class)) {
                                c = Collections.synchronizedCollection(c);
                            } else if (type.isAssignableFrom(List.class)) {
                                c = Collections.synchronizedList((List) c);
                            } else if (type.isAssignableFrom(Set.class)) {
                                c = Collections.synchronizedSet((Set) c);
                            } else if (type.isAssignableFrom(SortedSet.class)) {
                                c = Collections.synchronizedSortedSet((SortedSet) c);
                            } else {
                                throw new BeanletWiringException(name, 
                                        ea.getElement().getMember(), "Synchronized collection " +
                                        "cannot be injected for type: '" + 
                                        type.getName() + "'.");
                            }
                        }
                        if (collection.unmodifiable()) {
                            if (type.isAssignableFrom(Collection.class)) {
                                c = Collections.unmodifiableCollection(c);
                            } else if (type.isAssignableFrom(List.class)) {
                                c = Collections.unmodifiableList((List) c);
                            } else if (type.isAssignableFrom(Set.class)) {
                                c = Collections.unmodifiableSet((Set) c);
                            } else if (type.isAssignableFrom(SortedSet.class)) {
                                c = Collections.unmodifiableSortedSet((SortedSet) c);
                            } else {
                                throw new BeanletWiringException(name, 
                                        ea.getElement().getMember(), "Unmodifiable collection " +
                                        "cannot be injected for type: '" + 
                                        type.getName() + "'.");
                            }
                        }
                    } catch (ClassCastException e) {
                        // No need to process this exception. The 
                        // ValidatingDependencyInjection class will detect that
                        // this is a false injection. It is therefore safe to
                        // return a collection that is not 
                        // synchronized / unmodifiable.
                    }
                    return new InjectantImpl<Object>(c, false);
                }
            }
            MapValue map = inject.map();
            if (map.value().length > 0 || map.empty()) {
                Class<?> type = getType(ea.getElement());
                Map m;
                if (map.type().equals(Map.class)) {
                    try {
                        try {
                            Constructor constr = type.getConstructor();
                            m = (Map) constr.newInstance();
                        } catch (NoSuchMethodException e) {
                            if (type.isAssignableFrom(Map.class)) {
                                m = new HashMap();
                            } else if (type.isAssignableFrom(SortedMap.class)) {
                                m = new TreeMap();
                            } else if (type.isAssignableFrom(ConcurrentMap.class)) {
                                m = new ConcurrentHashMap();
                            } else {
                                throw new BeanletWiringException(name, 
                                        ea.getElement().getMember(),
                                        "Unable to select collection type for: '" + 
                                        type.getName() + "'.");
                            }
                        }
                    } catch (InvocationTargetException e) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), e);
                    } catch (InstantiationException e) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), e);
                    } catch (IllegalAccessException e) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), e);
                    }
                } else {
                    if (map.type().isInterface()) {
                        throw new BeanletWiringException(name, 
                                ea.getElement().getMember(), "Type must not be " +
                                "an interface: '" + collection.type().getName() + "'.");
                    }
                    m = (Map) map.type().newInstance();
                }
                if (!map.empty()) {
                    Class<?> keyType = getMapKeyType(ea.getElement());
                    Class<?> valueType = getMapValueType(ea.getElement());
                    if (m instanceof Properties) {
                        // Special case.
                        if (keyType.equals(Object.class)) {
                            keyType = String.class;
                        }
                        if (valueType.equals(Object.class)) {
                            valueType = String.class;
                        }
                    }
                    Map<Object, Object> tmp = (Map<Object, Object>) 
                            Collections.checkedMap(m, keyType, valueType);
                    for (Entry entry : map.value()) {
                        tmp.put(getValue(entry.key(), keyType, ea.getElement()),
                                getValue(entry.value(), valueType, ea.getElement()));
                    }
                } 
                try {
                    if (map.synced()) {
                        if (type.isAssignableFrom(Map.class)) {
                            m = Collections.synchronizedSortedMap((SortedMap) m);
                        } else if (type.isAssignableFrom(SortedMap.class)) {
                            m = Collections.synchronizedMap(m);
                        } else {
                            throw new BeanletWiringException(name, 
                                    ea.getElement().getMember(), "Synchronized map " +
                                    "cannot be injected for type: '" + 
                                    type.getName() + "'.");
                        }
                    }
                    if (map.unmodifiable()) {
                        if (type.isAssignableFrom(Map.class)) {
                            m = Collections.unmodifiableSortedMap((SortedMap) m);
                        } else if (type.isAssignableFrom(SortedMap.class)) {
                            m = Collections.unmodifiableMap(m);
                        } else {
                            throw new BeanletWiringException(name, 
                                    ea.getElement().getMember(), "Unmodifiable map " +
                                    "cannot be injected for type: '" + 
                                    type.getName() + "'.");
                        }
                    }
                } catch (ClassCastException e) {
                    // No need to process this exception. The 
                    // ValidatingDependencyInjection class will detect that
                    // this is a false injection. It is therefor safe to
                    // return a collection that is not 
                    // synchronized / unmodifiable.
                }
                return new InjectantImpl<Object>(m, false);
            } else {
                assert false;
                return new InjectantImpl<Object>(null, false);
            }
        } catch (ClassCastException e) {
            throw new BeanletWiringException(name, ea.getElement().getMember(), 
                    "Attempt to insert an incompatible value for parameterized " +
                    "member.", e);
        } catch (IllegalAccessException e) {
            throw new BeanletWiringException(name, ea.getElement().getMember(), 
                    e);
        } catch (InstantiationException e) {
            throw new BeanletWiringException(name, ea.getElement().getMember(), 
                    e);
        }
    }

    private Object getValue(Inject inject, Class<?> type, Element element) {
        return getValue("", Object.class, false, inject.ref(), type, element);
    }
    
    private Object getValue(Value value, Class<?> type, Element element) {
        return getValue(value.value(), value.type(), value.nill(), value.ref(), 
                type, element);
    }
    
    private Object getValue(String value, Class<?> valueType, boolean nill, 
            String ref, Class<?> type, Element element) {
        final Object v;
        if (nill) {
            v = null;
        } else if (!ref.equals("")) {
            v = BeanletApplicationContext.instance().getBeanlet(ref);
        } else {
            Class<?> cls = valueType.equals(Object.class) ? type : valueType;
            v = getValue(value, cls, element);
        }
        return v;
    }
    
    private Object getValue(String value, Class<?> type, Element element) {
        assert value != null;
        assert type != null;
        
        final Object o;
        try {
            try {
                if (type.isPrimitive()) {
                    if (Character.TYPE.equals(type)) {
                        if (value.length() != 1) {
                            throw new BeanletWiringException(
                                    configuration.getComponentName(),
                                    element.getMember(),
                                    "Invalid content for attribute type Character: " +
                                    value + ".");
                        } else {
                            o = Character.valueOf(value.charAt(0));
                        }
                    } else if (Void.TYPE.equals(type)) {
                        throw new BeanletWiringException(
                                configuration.getComponentName(),
                                element.getMember(),
                                "Invalid attribute type: void.");
                    } else {
                        final Class<?> objectType;
                        if (Boolean.TYPE.equals(type)) {
                            objectType = Boolean.class;
                        } else if (Byte.TYPE.equals(type)) {
                            objectType = Byte.class;
                        } else if (Short.TYPE.equals(type)) {
                            objectType = Short.class;
                        } else if (Integer.TYPE.equals(type)) {
                            objectType = Integer.class;
                        } else if (Long.TYPE.equals(type)) {
                            objectType = Long.class;
                        } else if (Float.TYPE.equals(type)) {
                            objectType = Float.class;
                        } else if (Double.TYPE.equals(type)) {
                            objectType = Double.class;
                        } else {
                            objectType = null;
                            assert false : type;
                        }
                        o = objectType.getMethod("valueOf", String.class).
                                invoke(null, value);
                    }
                } else if (type.equals(Class.class)) {
                    o = configuration.getComponentUnit().getClassLoader().loadClass(value);
                } else {
                    if (type.isInterface()) {
                        throw new BeanletWiringException(
                                configuration.getComponentName(),
                                element.getMember(),
                                "Failed to create instance for value \"" + 
                                value + "\". Interfaces " +
                                "cannot be instantiated: " + type.getName() + ".");
                    }
                    Object tmp = null;
                    try {
                        tmp = type.getConstructor(String.class).
                                newInstance(value);
                    } catch (NoSuchMethodException e) {
                        if (!value.equals("")) {
                            throw new BeanletWiringException(
                                    configuration.getComponentName(),
                                    element.getMember(),
                                    "Failed to create instance for value \"" + 
                                    value + "\". Class " + type.getName() + 
                                    " does not declare a String constructor.");
                        }
                        tmp = type.newInstance();
                    }
                    o = tmp;
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } catch (BeanletWiringException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            // Errors are excluded.
            throw new BeanletWiringException(configuration.getComponentName(),
                    element.getMember(), t);
        }
        return o;
    }
}
