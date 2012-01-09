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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.beanlet.Attribute;
import org.beanlet.AttributeAccessType;
import org.beanlet.BeanletValidationException;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.common.Beanlets;
import org.beanlet.common.event.AttributeReadEventImpl;
import org.beanlet.common.event.AttributeWriteEventImpl;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.ComponentConfiguration;
import org.jargo.Event;

/**
 * @author Leon van Zantvoort
 */
public final class Attributes {

    private static final Map<ComponentConfiguration, Attributes> cache = new HashMap<ComponentConfiguration, Attributes>();

    public static Attributes getInstance() {
        return getInstance(null);
    }

    public static synchronized Attributes getInstance(
            final ComponentConfiguration<?> configuration) {
        Attributes attributes = cache.get(configuration);
        if (attributes == null) {
            if (configuration instanceof BeanletConfiguration) {
                attributes = new Attributes((BeanletConfiguration<?>) configuration);
            } else {
                attributes = new Attributes();
            }
            configuration.getComponentUnit().addDestroyHook(new Runnable() {
                public void run() {
                    synchronized (Attributes.class) {
                        cache.remove(configuration);
                    }
                }
            });
            cache.put(configuration, attributes);
        }
        return attributes;
    }

    public static boolean isGetter(Method method) {
        if (method.getName().startsWith("get") && method.getName().length() > 3 && 
                method.getParameterTypes().length == 0 && 
                !method.getReturnType().equals(Void.TYPE)) {
            return true;
        }
        if (isIs(method)) {
            return true;
        }
        return false;
    }

    public static boolean isIs(Method method) {
        if (method.getName().startsWith("is") && 
                method.getName().length() > 2 && 
                method.getParameterTypes().length == 0 && 
                (method.getReturnType().equals(Boolean.TYPE) || 
                method.getReturnType().equals(Boolean.class))) {
            return true;
        }
        return false;
    }

    public static boolean isSetter(Method method) {
        if (method.getName().startsWith("set") && 
                method.getName().length() > 3 && 
                method.getParameterTypes().length == 1 && 
                method.getReturnType().equals(Void.TYPE)) {
            return true;
        }
        return false;
    }

    public static String getAttributeName(Method method) {
        return getAttributeName(method, true);
    }

    private static String getAttributeName(Method method, boolean nillable) {
        final String attributeName;
        if ((method.getName().startsWith("get") || method.getName().
                startsWith("set")) && method.getName().length() > 3) {
            attributeName = Character.toLowerCase(method.getName().charAt(3)) + 
                    method.getName().substring(4);
        } else if (method.getName().startsWith("is") && 
                method.getName().length() > 2) {
            attributeName = Character.toLowerCase(method.getName().charAt(2)) + 
                    method.getName().substring(3);
        } else if (!nillable) {
            attributeName = method.getName();
        } else {
            attributeName = null;
        }
        return attributeName;
    }

    private final AnnotationDomain domain;
    private final ProxyMethods proxyMethods;

    private final Map<String, Getter> getters;
    private final Map<String, Setter> setters;
    private final Map<Member, Getter> memberGetters;
    private final Map<Member, Setter> memberSetters;

    private Attributes() {
        this.domain = null;
        this.proxyMethods = ProxyMethods.getInstance();
        this.getters = new HashMap<String, Getter>();
        this.setters = new HashMap<String, Setter>();
        this.memberGetters = new HashMap<Member, Getter>();
        this.memberSetters = new HashMap<Member, Setter>();
    }

    private Attributes(BeanletConfiguration configuration) {
        this.domain = configuration.getAnnotationDomain();
        this.proxyMethods = ProxyMethods.getInstance(configuration);
        this.getters = new HashMap<String, Getter>();
        this.setters = new HashMap<String, Setter>();
        this.memberGetters = new HashMap<Member, Getter>();
        this.memberSetters = new HashMap<Member, Setter>();

        Class<?> type = configuration.getType();
        for (ElementAnnotation<FieldElement, Attribute> e : 
                domain.getDeclaration(Attribute.class).
                getTypedElements(FieldElement.class, type)) {
            Attribute attribute = e.getAnnotation();
            Field field = e.getElement().getField();
            String attributeName = attribute.name().equals("") ? 
                field.getName() : attribute.name();
            List accessTypes = Arrays.asList(attribute.accessType());
            if (accessTypes.contains(AttributeAccessType.READ) || 
                    accessTypes.isEmpty()) {
                if (!getters.containsKey(attributeName)) {
                    Getter getter = createFieldGetter(attributeName, field);
                    getters.put(attributeName, getter);
                    memberGetters.put(getter.getMember(), getter);
                } else {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Duplicate readable attributes found: \'" + 
                            attributeName + "\'.");
                }
            }
            if (accessTypes.contains(AttributeAccessType.WRITE) ||
                    (accessTypes.isEmpty() && 
                    Modifier.isPublic(field.getModifiers()) &&
                    !Modifier.isFinal(field.getModifiers()))) {
                if (accessTypes.contains(AttributeAccessType.WRITE) &&
                        Modifier.isFinal(field.getModifiers())) {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Writable attribute MUST NOT be a final field: '" +
                            field + "\'.");
                }
                if (!setters.containsKey(attributeName)) {
                    Setter setter = createFieldSetter(attributeName, field);
                    setters.put(attributeName, setter);
                    memberSetters.put(setter.getMember(), setter);
                } else {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Duplicate writeable attributes found: \'" + 
                            attributeName + "\'.");
                }
            }
        }
        for (ElementAnnotation<MethodElement, Attribute> e : 
                domain.getDeclaration(Attribute.class).
                getTypedElements(MethodElement.class, type)) {
            Attribute attribute = e.getAnnotation();
            Method method = e.getElement().getMethod();
            final String attributeName;
            if (attribute.name().equals("")) {
                attributeName = getAttributeName(method, false);
            } else {
                attributeName = attribute.name();
            }
            if (isGetter(method)) {
                List accessTypes = Arrays.asList(attribute.accessType());
                if (accessTypes.contains(AttributeAccessType.WRITE)) {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Getter method MUST NOT specify WRITE access type: '" + 
                            method + "'.");
                }
                Getter getter = createMethodGetter(attributeName, method);
                if (!getters.containsKey(attributeName)) {
                    Setter setter = setters.get(attributeName);
                    if (setter == null || getter.getType().
                            equals(setter.getType())) {
                        getters.put(attributeName, getter);
                        memberGetters.put(getter.getMember(), getter);
                    } else {
                        throw new BeanletValidationException(
                                configuration.getComponentName(), 
                                "Duplicate readable attributes found: '" + 
                                attributeName + "'.");
                    }
                } else {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Duplicate readable attributes: \'" + 
                            attributeName + "\'.");
                }
            } else if (isSetter(method)) {
                List accessTypes = Arrays.asList(attribute.accessType());
                if (accessTypes.contains(AttributeAccessType.READ)) {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Setter method MUST NOT specify READ access type: '" + 
                            method + "'.");
                }
                Setter setter = createMethodSetter(attributeName, method);
                if (!setters.containsKey(attributeName)) {
                    Getter getter = getters.get(attributeName);
                    if (getter == null || setter.getType().
                            equals(getter.getType())) {
                        setters.put(attributeName, setter);
                        memberSetters.put(setter.getMember(), setter);
                    } else {
                        throw new BeanletValidationException(
                                configuration.getComponentName(), 
                                "Duplicate writeable attributes found: '" + 
                                attributeName + "'.");
                    }
                } else {
                    throw new BeanletValidationException(
                            configuration.getComponentName(), 
                            "Duplicate writable attributes found: \'" + 
                            attributeName + "\'.");
                }
            } else {
                throw new BeanletValidationException(
                        configuration.getComponentName(), 
                        "Invalid attribute method signature: \'" + 
                        attributeName + "\'.");
            }
        }

        Beanlets beanlets = Beanlets.getInstance(configuration);
        final List<Class<?>> classes;
        if (beanlets.isVanilla()) {
            // Automatically includes all implemented interfaces.
            classes = Collections.<Class<?>>singletonList(type);
        } else {
            classes = beanlets.getInterfaces();
        }
        for (Class<?> cls : classes) {
            for (Method method : cls.getMethods()) {
                if (!cls.isInterface()) {
                    // Object's methods are excluded as well.
                    try {
                        Object.class.getMethod(method.getName(), 
                                method.getParameterTypes());
                        continue;
                    } catch (NoSuchMethodException e) {
                    }
                }
                final String t;
                if (cls.isInterface()) {
                    t = "Interface";
                } else {
                    t = "Superclass";
                }
                if (isGetter(method)) {
                    // Method is recognized as a getter.
                    String attributeName = getAttributeName(method, false);
                    if (!getters.containsKey(attributeName)) {
                        // Getter has not yet been constructed.
                        final Getter getter;
                        if (cls.isAssignableFrom(type)) {
                            // Getter is directly supported (implemented) by underlying class.
                            getter = createMethodGetter(attributeName, method);
                        } else {
                            // Method is not supported by underlying class.
                            if (proxyMethods.getProxyMethod() != null) {
                                // Proxy method will be used to support getter.
                                getter = createProxyMethodGetter(attributeName, 
                                        method, method.getReturnType());
                            } else {
                                // Not explicitely marked as attribute so skip method instead of exception.
                                //
                                // Leon van Zantvoort
                                // 2011/01/09
                                continue;
//                                throw new BeanletValidationException(
//                                        configuration.getComponentName(), t +
//                                        "Getter and setter type do not match: '" +
//                                        method + "'.");
                            }
                        }
                        Setter setter = setters.get(attributeName);
                        if (setter == null || getter.getType().equals(
                                setter.getType())) {
                            // Setter (counterpart) does not exist, or is of same type.
                            getters.put(attributeName, getter);
                            memberGetters.put(getter.getMember(), getter);
                        } else {
                            // Not explicitely marked as attribute so skip method instead of exception.
                            //
                            // Leon van Zantvoort
                            // 2011/01/09
                            continue;
//                            throw new BeanletValidationException(
//                                    configuration.getComponentName(),
//                                    "Duplicate writable attributes found: '" +
//                                    attributeName + "'.");
                        }
                    }
                } else if (isSetter(method)) {
                    String attributeName = getAttributeName(method, false);
                    if (!setters.containsKey(attributeName)) {
                        final Setter setter;
                        if (cls.isAssignableFrom(type)) {
                            setter = createMethodSetter(attributeName, method);
                        } else {
                            if (proxyMethods.getProxyMethod() != null) {
                                setter = createProxyMethodSetter(attributeName, 
                                        method, method.getParameterTypes()[0]);
                            } else {
                                // Not explicitely marked as attribute so skip method instead of exception.
                                //
                                // Leon van Zantvoort
                                // 2011/01/09
                                continue;
//                                throw new BeanletValidationException(
//                                        configuration.getComponentName(), t +
//                                        " method not exposed as attribute: \'" +
//                                        method + "\'.");
                            }
                        }
                        Getter getter = getters.get(attributeName);
                        if (getter == null || setter.getType().equals(
                                getter.getType())) {
                            setters.put(attributeName, setter);
                            memberSetters.put(setter.getMember(), setter);
                        } else {
                            // Not explicitely marked as attribute so skip method instead of exception.
                            //
                            // Leon van Zantvoort
                            // 2011/01/09
                            continue;
//                            throw new BeanletValidationException(
//                                    configuration.getComponentName(),
//                                    "Superclass" + attributeName + "\'.");
                        }
                    }
                }
            }
        }
        if (beanlets.isVanilla()) {
            for (Field field : type.getFields()) {
                String attributeName = field.getName();
                if (!getters.containsKey(field)) {
                    Getter getter = createFieldGetter(attributeName, field);
                    getters.put(attributeName, getter);
                    memberGetters.put(getter.getMember(), getter);
                }
                if (!Modifier.isFinal(field.getModifiers()) &&
                        !setters.containsKey(field)) {
                    Setter setter = createFieldSetter(attributeName, field);
                    setters.put(attributeName, setter);
                    memberSetters.put(setter.getMember(), setter);
                }
            }
        }
    }

    public Event getEvent(Method method, Object[] args) {
        final Event event;
        if (isGetter(method)) {
            String attributeName = getAttributeName(method, false);
            if (getters.containsKey(attributeName)) {
                event = new AttributeReadEventImpl(attributeName);
            } else {
                event = null;
            }
        } else if (isSetter(method)) {
            String attributeName = getAttributeName(method, false);
            if (setters.containsKey(attributeName)) {
                event = new AttributeWriteEventImpl(attributeName, args[0]);
            } else {
                event = null;
            }
        } else {
            event = null;
        }
        return event;
    }

    public List<Class<?>> getInterfaces() {
        return Collections.emptyList();
    }

    public Collection<Getter> getGetters() {
        return Collections.unmodifiableCollection(getters.values());
    }

    public Getter getGetter(String attributeName) {
        return getters.get(attributeName);
    }

    public Getter getGetter(Member member) {
        return memberGetters.get(member);
    }

    public Collection<Setter> getSetters() {
        return Collections.unmodifiableCollection(setters.values());
    }

    public Setter getSetter(String attributeName) {
        return setters.get(attributeName);
    }

    public Setter getSetter(Member member) {
        return memberSetters.get(member);
    }

    private Getter createProxyMethodGetter(final String name, 
            final Method method, final Class<?> type) {
        final Method proxyMethod = proxyMethods.getProxyMethod();
        assert proxyMethod != null;
        if (!Modifier.isPublic(proxyMethod.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    proxyMethod.setAccessible(true);
                    return null;
                }
            });
        }

        final Attribute annotation = domain.getDeclaration(Attribute.class).
                getAnnotation(MethodElement.instance(method));
        return new Getter() {

            public String getName() {
                return name;
            }

            public Member getMember() {
                return method;
            }

            public Class<?> getType() {
                return method.getReturnType();
            }

            public String getDescription() {
                return annotation == null ? "" : annotation.description();
            }

            public Object get(Object instance) throws Exception {
                try {
                    return proxyMethod.invoke(instance, method.getName(), 
                            method.getParameterTypes(), new Object[0]);
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getTargetException();
                    } catch (RuntimeException e2) {
                        throw e2;
                    } catch (Error e2) {
                        throw e2;
                    } catch (Exception e2) {
                        throw e2;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        };
    }

    private Getter createMethodGetter(final String name, final Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    method.setAccessible(true);
                    return null;
                }
            });
        }

        final Attribute annotation = domain.getDeclaration(Attribute.class).
                getAnnotation(MethodElement.instance(method));
        return new Getter() {

            public String getName() {
                return name;
            }

            public Member getMember() {
                return method;
            }

            public Class<?> getType() {
                return method.getReturnType();
            }

            public String getDescription() {
                return annotation == null ? "" : annotation.description();
            }

            public Object get(Object instance) throws Exception {
                try {
                    return method.invoke(instance);
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getTargetException();
                    } catch (RuntimeException e2) {
                        throw e2;
                    } catch (Error e2) {
                        throw e2;
                    } catch (Exception e2) {
                        throw e2;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        };
    }

    private Getter createFieldGetter(final String name, final Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    field.setAccessible(true);
                    return null;
                }
            });
        }

        final Attribute annotation = domain.getDeclaration(Attribute.class).
                getAnnotation(FieldElement.instance(field));
        return new Getter() {

            public String getName() {
                return name;
            }

            public Member getMember() {
                return field;
            }

            public Class<?> getType() {
                return field.getType();
            }

            public String getDescription() {
                return annotation == null ? "" : annotation.description();
            }

            public Object get(Object instance) throws Exception {
                return field.get(instance);
            }
        };
    }

    private Setter createProxyMethodSetter(final String name, 
            final Method method, final Class<?> type) {
        final Method proxyMethod = proxyMethods.getProxyMethod();
        assert proxyMethod != null;
        if (!Modifier.isPublic(proxyMethod.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    proxyMethod.setAccessible(true);
                    return null;
                }
            });
        }

        final Attribute annotation = domain.getDeclaration(Attribute.class).
                getAnnotation(MethodElement.instance(method));
        return new Setter() {

            public String getName() {
                return name;
            }

            public Member getMember() {
                return method;
            }

            public Class<?> getType() {
                return method.getParameterTypes()[0];
            }

            public String getDescription() {
                return annotation == null ? "" : annotation.description();
            }

            public void set(Object instance, Object injection) throws Exception {
                try {
                    proxyMethod.invoke(instance, method.getName(), 
                            method.getParameterTypes(), new Object[]{injection});
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getTargetException();
                    } catch (RuntimeException e2) {
                        throw e2;
                    } catch (Error e2) {
                        throw e2;
                    } catch (Exception e2) {
                        throw e2;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        };
    }

    private Setter createMethodSetter(final String name, final Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    method.setAccessible(true);
                    return null;
                }
            });
        }

        final Attribute annotation = domain.getDeclaration(Attribute.class).
                getAnnotation(MethodElement.instance(method));
        return new Setter() {

            public String getName() {
                return name;
            }

            public Member getMember() {
                return method;
            }

            public Class<?> getType() {
                return method.getParameterTypes()[0];
            }

            public String getDescription() {
                return annotation == null ? "" : annotation.description();
            }

            public void set(Object instance, Object injection) throws Exception {
                try {
                    method.invoke(instance, injection);
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getTargetException();
                    } catch (RuntimeException e2) {
                        throw e2;
                    } catch (Error e2) {
                        throw e2;
                    } catch (Exception e2) {
                        throw e2;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        };
    }

    private Setter createFieldSetter(final String name, final Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                    field.setAccessible(true);
                    return null;
                }
            });
        }

        final Attribute annotation = domain.getDeclaration(Attribute.class).
                getAnnotation(FieldElement.instance(field));

        return new Setter() {

            public String getName() {
                return name;
            }

            public Member getMember() {
                return field;
            }

            public Class<?> getType() {
                return field.getType();
            }

            public String getDescription() {
                return annotation == null ? "" : annotation.description();
            }

            public void set(Object instance, Object injection) throws Exception {
                field.set(instance, injection);
            }
        };
    }

    public static interface Getter {

        String getName();

        Member getMember();

        Class<?> getType();

        String getDescription();

        Object get(Object instance) throws Exception;
    }

    public static interface Setter {

        String getName();

        Member getMember();

        Class<?> getType();

        String getDescription();

        void set(Object instance, Object injection) throws Exception;
    }
}
