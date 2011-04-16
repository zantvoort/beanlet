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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.beanlet.BeanletValidationException;
import org.beanlet.Operation;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.MethodElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.Beanlets;
import org.beanlet.common.event.OperationEventImpl;
import org.jargo.ComponentConfiguration;
import org.jargo.Event;

/**
 * @author Leon van Zantvoort
 */
public final class Operations {
    
    private static final Map<ComponentConfiguration<?>, Operations> cache =
            new HashMap<ComponentConfiguration<?>, Operations>();
    
    public static final Operations getInstance() {
        return getInstance(null);
    }
    
    public static final synchronized Operations getInstance(
            final ComponentConfiguration<?> configuration) {
        Operations operations = cache.get(configuration);
        if (operations == null) {
            if (configuration instanceof BeanletConfiguration) {
                operations = new Operations((BeanletConfiguration<?>) configuration);
            } else {
                operations = new Operations();
            }
            configuration.getComponentUnit().addDestroyHook(new Runnable() {
                public void run() {
                    synchronized (Operations.class) {
                        cache.remove(configuration);
                    }
                }
            });
            cache.put(configuration, operations);
        }
        return operations;
    }

    private final BeanletConfiguration<?> configuration;
    private final ProxyMethods proxyMethods;
    private final Map<Object, Method> namedMethods;
    private final Set<Method> methods;
    private final List<Class<?>> interfaces;
    private final boolean proxy;
    
    private Operations() {
        this.configuration = null;
        this.proxyMethods = ProxyMethods.getInstance();
        this.namedMethods = new HashMap<Object, Method>();
        this.methods = new HashSet<Method>();
        this.interfaces = new ArrayList<Class<?>>();
        this.proxy = false;
    }
    
    private Operations(BeanletConfiguration configuration) {
        this.configuration = configuration;
        this.proxyMethods = ProxyMethods.getInstance(configuration);
        this.namedMethods = new HashMap<Object, Method>();
        
        Beanlets beanlets = Beanlets.getInstance(configuration);
        AnnotationDomain domain = configuration.getAnnotationDomain();
        
        Class<?> type = configuration.getType();
        
        this.proxy = beanlets.isPropxy();
        if (beanlets.isVanilla()) {
            // Automatically includes all implemented interfaces.
            namedMethods.putAll(getNamedMethods(configuration.getType()));
        } else {
            for (Class<?> inf : beanlets.getInterfaces()) {
                // Add all methods of the interface as operations if it is 
                // implemented by type.
                if (inf.isAssignableFrom(type)) {
                    namedMethods.putAll(getNamedMethods(inf));
                }
            }
        }
        
        for (ElementAnnotation<MethodElement, Operation> e : 
                domain.getDeclaration(Operation.class).
                getTypedElements(MethodElement.class, type)) {
            Operation operation = e.getAnnotation();
            Method method = e.getElement().getMethod();
                
            final String operationName;
            if (!operation.name().equals("")) {
                operationName = operation.name();
            } else {
                operationName = method.getName();
            }
            Object key = Arrays.asList(operationName,
                    Arrays.asList(method.getParameterTypes()));

            Method org = namedMethods.put(key, method);
            if (org != null) {
                if (!org.equals(method)) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Operation is defined by multiple methods: '" + operationName + "'.");
                }
            }
        }
        
        this.interfaces = new ArrayList<Class<?>>(beanlets.getInterfaces());

        if (proxyMethods.getProxyMethod() == null) {
            // Check if all methods are exposed as operations.
            List<Class> classes = new ArrayList<Class>();
            if (beanlets.isVanilla()) {
                classes.add(configuration.getType());
            }
            classes.addAll(interfaces);
            for (Class<?> cls : classes) {
                for (Method method : cls.getMethods()) {
                    if (!cls.isInterface()) {
                        // Object's methods are excluded from check.
                        try {
                            Object.class.getMethod(
                                    method.getName(),
                                    method.getParameterTypes());
                            continue;
                        } catch (NoSuchMethodException e) {
                        }
                    }
                    Object key = Arrays.asList(method.getName(),
                            Arrays.asList(method.getParameterTypes()));
                    Method m = namedMethods.get(key);
                    
                    final String t;
                    if (cls.isInterface()) {
                        t = "Interface";
                    } else {
                        t = "Superclass";
                    }
                    if (m == null) {
                        throw new BeanletValidationException(configuration.getComponentName(),
                                t + " method not exposed as operation: '" + method + "'.");
                    }
                    // Only check if method does not return void.
                    if (!method.getReturnType().equals(Void.TYPE)) {
                        if (!method.getReturnType().isAssignableFrom(m.getReturnType())) {
                            throw new BeanletValidationException(configuration.getComponentName(),
                                    t + " method specifies invalid return type: '"  + method + "'.");
                        }
                    }
                }
            }
        }

        Set<Method> tmp = new HashSet<Method>(namedMethods.values());
        // It has all been checked, now add the remaining methods.
        for (Class<?> inf : beanlets.getInterfaces()) {
            if (!inf.isAssignableFrom(type)) {
                tmp.addAll(Arrays.asList(inf.getMethods()));
            }
        }
        this.methods = Collections.unmodifiableSet(tmp);
    }
 
    public Event getEvent(Method method, Object[] args) {
        final Event event;
        if (proxyMethods.getProxyMethod() != null || 
                getMethod(method.getName(), method.getParameterTypes()) != null) {
            event = new OperationEventImpl(method.getName(), method.getParameterTypes(), args);
        } else {
            event = null;
        }
        return event;
    }
    
    public List<Class<?>> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }
    
    public boolean isProxy() {
        return proxy;
    }
    
    public Set<Method> getMethods() {
        return methods;
    }
    
    /**
     * Returns a method for the specified operation {@code name} and
     * {@code parameterTypes}, or {@code null} if no method can be found. This
     * method will not return the proxy method.
     */
    public Method getMethod(String name, Class<?>[] parameterTypes) {
        return namedMethods.get(Arrays.asList(name, 
                Arrays.asList(parameterTypes)));
    }
    
    private Map<Object, Method> getNamedMethods(Class<?> cls) {
        Map<Object, Method> namedMethods = new HashMap<Object, Method>();
        for (Method method : cls.getMethods()) {
            try {
                Method classMethod = configuration.getType().getMethod(
                        method.getName(),
                        method.getParameterTypes());
                if (!cls.isInterface()) {
                    try {
                        Object.class.getMethod(
                                method.getName(),
                                method.getParameterTypes());
                        continue;
                    } catch (NoSuchMethodException e) {
                    }
                }
                namedMethods.put(Arrays.asList(classMethod.getName(),
                        Arrays.asList(classMethod.getParameterTypes())),
                        classMethod);
            } catch (NoSuchMethodException e) {
                assert false : method;
            }
        }
        return namedMethods;
    }
}
