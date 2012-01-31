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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.beanlet.*;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.InvocationImpl;
import org.jargo.Invocation;
import org.jargo.Lifecycle;
import org.jargo.spi.LifecycleProvider;
import org.jargo.ComponentConfiguration;

/**
 *
 * @author Leon van Zantvoort
 */
public final class LifecycleProviderImpl extends AbstractProvider implements 
        LifecycleProvider {

    private final Logger logger = Logger.getLogger(getClass().getName());

    public List<Lifecycle> getLifecycles(final ComponentConfiguration 
            configuration, Executor executor, final boolean vanilla) {
        List<Lifecycle> lifecycles = new ArrayList<Lifecycle>();
        if (configuration instanceof BeanletConfiguration) {
            lifecycles.add(new Lifecycle() {
                public List<Invocation> onCreate(Class<?> cls, boolean interceptor) {
                    return getPostConstructInvocations((BeanletConfiguration) 
                            configuration, cls, interceptor);
                }
                public List<Invocation> onDestroy(Class<?> cls, boolean interceptor) {
                    return getPreDestroyInvocations((BeanletConfiguration) 
                            configuration, cls, interceptor, vanilla);
                }
            });
        }
	return Collections.unmodifiableList(lifecycles);
    }
    
    private List<Invocation> getPostConstructInvocations(BeanletConfiguration
            configuration, Class<?> cls, boolean interceptor) {
        final List<Invocation> invocations;
        if (configuration.getType().equals(cls) || interceptor) {
            AnnotationDeclaration<?> beanletDeclaration = configuration.
                    getAnnotationDomain().getDeclaration(PostConstruct.class);
            AnnotationDeclaration<?> javaDeclaration = null;
            try {
                @java.lang.SuppressWarnings("unchecked")
                Class<? extends Annotation> at = (Class<? extends Annotation>)
                        Class.forName("javax.annotation.PostConstruct");
                javaDeclaration = configuration.getAnnotationDomain().
                        getDeclaration(at);
            } catch (ClassNotFoundException e) {
            }
            
            invocations = new ArrayList<Invocation>();
            
            LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
            Class<?> tmp = cls;
            do {
                // PENDING: exclude Object.class as optimization?
                classes.addFirst(tmp);
            } while ((tmp = tmp.getSuperclass()) != null);

            for (final Class<?> c : classes) {
                Method[] methods = AccessController.doPrivileged(
                        new PrivilegedAction<Method[]>() {
                    public Method[] run() {
                        // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                        return c.getDeclaredMethods();
                    }
                });
                Method postConstructMethod = null;
                for (Method method : methods) {
                    Element e = MethodElement.instance(method); 
                    if (!e.isOverridden(cls)) {
                        if (beanletDeclaration.isAnnotationPresent(e)) {
                            if (postConstructMethod != null) {
                                throw new BeanletValidationException(
                                        configuration.getComponentName(), 
                                        "Class MAY declare only one " +
                                        "PostConstruct method: '" + c + "'.");
                            }
                            postConstructMethod = method;
                        }
                        if (javaDeclaration != null && 
                                javaDeclaration.isAnnotationPresent(e)) {
                            if (postConstructMethod != null) {
                                throw new BeanletValidationException(
                                        configuration.getComponentName(), 
                                        "Class MAY declare only one " +
                                        "PostConstruct method: '" + c + "'.");
                            }
                            postConstructMethod = method;
                        }
                    }
                }

                final Invocation i;
                if (postConstructMethod != null) {
                    if (interceptor) {
                        i = getInterceptorInvocation(postConstructMethod);
                    } else {
                        i = new InvocationImpl(postConstructMethod);
                    }
                } else {
                    i = null;
                }
                if (i != null) {
                    invocations.add(i);
                }
            }
        } else {
            invocations = Collections.emptyList();
        }
        return invocations;
    }
    
    private List<Invocation> getPreDestroyInvocations(BeanletConfiguration configuration,
            Class<?> cls, boolean interceptor, boolean vanilla) {
        final List<Invocation> invocations;
        
        if (configuration.getType().equals(cls) || interceptor) {
            AnnotationDeclaration<?> beanletDeclaration = configuration.
                    getAnnotationDomain().getDeclaration(PreDestroy.class);
            AnnotationDeclaration<?> javaDeclaration = null;
            try {
                @java.lang.SuppressWarnings("unchecked")
                Class<? extends Annotation> at = (Class<? extends Annotation>)
                        Class.forName("javax.annotation.PreDestroy");
                javaDeclaration = configuration.getAnnotationDomain().
                        getDeclaration(at);
            } catch (ClassNotFoundException e) {
            }

            invocations = new ArrayList<Invocation>();

            LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
            Class<?> tmp = cls;
            do {
                classes.addFirst(tmp);
            } while ((tmp = tmp.getSuperclass()) != null);

            for (final Class<?> c : classes) {
                Method[] methods = AccessController.doPrivileged(
                        new PrivilegedAction<Method[]>() {
                    public Method[] run() {
                        // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                        return c.getDeclaredMethods();
                    }
                });
                Method preDestroyMethod = null;
                for (Method method : methods) {
                    Element e = MethodElement.instance(method); 
                    if (!e.isOverridden(cls)) {
                        if (beanletDeclaration.isAnnotationPresent(e)) {
                            if (preDestroyMethod != null) {
                                throw new BeanletValidationException(
                                        configuration.getComponentName(), 
                                        "Class MAY declare only one " +
                                        "PreDestroy method: '" + c + "'.");
                            }
                            preDestroyMethod = method;
                        }
                        if (javaDeclaration != null && 
                                javaDeclaration.isAnnotationPresent(e)) {
                            if (preDestroyMethod != null) {
                                throw new BeanletValidationException(
                                        configuration.getComponentName(), 
                                        "Class MAY declare only one " +
                                        "PreDestroy method: '" + c + "'.");
                            }
                            preDestroyMethod = method;
                        }
                    }
                }

                final Invocation i;
                if (preDestroyMethod != null) {
                    if (vanilla) {
                        AnnotationDeclaration<org.beanlet.SuppressWarnings> ad = configuration.getAnnotationDomain().
                                getDeclaration(org.beanlet.SuppressWarnings.class);
                        if (!ContainsValue(ad.getAnnotation(MethodElement.instance(preDestroyMethod)))) {
                            if (!ContainsValue(ad.getAnnotation(TypeElement.instance(preDestroyMethod.getDeclaringClass())))) {
                                logger.warning(configuration.getComponentName() + ": PreDestroy method is only be executed if vanilla beanlet is destroyed explicitly. PreDestroy method is not executed" +
                                        " if objects are claimed by the garbage collector. This warning can be suppressed by marking this method or class with @org.beanlet.SuppressWarnings(\"predestroy\").");
                            }
                        }
                    }
                    if (interceptor) {
                        i = getInterceptorInvocation(preDestroyMethod);
                    } else {
                        i = new InvocationImpl(preDestroyMethod);
                    }
                } else {
                    i = null;
                }
                if (i != null) {
                    invocations.add(i);
                }
            }
        } else {
            invocations = Collections.emptyList();
        }
        return invocations;
    }

    private static boolean ContainsValue(org.beanlet.SuppressWarnings at) {
        boolean found = false;
        if (at != null) {
            for (String v : at.value()) {
                if (v.equalsIgnoreCase("predestroy")) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private Invocation getInterceptorInvocation(Method method) {
        return new InvocationImpl(method) {
            public Object[] getParameters() {
                final org.jargo.InvocationContext ctx  = 
                        (org.jargo.InvocationContext) super.getParameters()[0];
                return new Object[] {
                    new InvocationContextImpl() {
                        protected org.jargo.InvocationContext 
                                getInvocationContext() {
                            return ctx;
                        }
                    }
                };
            }
        };
    }
}
