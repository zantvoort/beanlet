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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.InvocationImpl;
import org.jargo.ComponentConfiguration;
import org.beanlet.AroundInvoke;
import org.beanlet.BeanletException;
import org.beanlet.BeanletValidationException;
import org.beanlet.ExcludeClassInterceptors;
import org.beanlet.ExcludeDefaultInterceptors;
import org.beanlet.Interceptor;
import org.beanlet.Interceptors;
import org.beanlet.InvocationContext;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.PackageElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.ConstructorInjection;
import org.jargo.spi.InvocationInterceptorFactoryProvider;
import org.jargo.InvocationInterceptor;
import org.jargo.InvocationInterceptorAdapter;
import org.jargo.InvocationInterceptorFactory;
import org.jargo.ProxyController;

/**
 * @author Leon van Zantvoort
 */
public final class InvocationInterceptorFactoryProviderImpl extends AbstractProvider
        implements InvocationInterceptorFactoryProvider {

    private static final Method INTERCEPT_METHOD;
    static {
        try {
            INTERCEPT_METHOD = Interceptor.class.getMethod(
                    "intercept", InvocationContext.class);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    // PENDING: apply equality check based on identity on this map?
    private final Map<Object, Map<Class, List<Interceptor>>> interceptorMap;
    
    public InvocationInterceptorFactoryProviderImpl() {
        // PENDING: replace WeakHashMap by a WeakIdentityHashMap.
        interceptorMap = Collections.synchronizedMap(
                new WeakHashMap<Object, Map<Class, List<Interceptor>>>());
    }
    
    public List<InvocationInterceptorFactory> getInvocationInterceptorFactories(
            ComponentConfiguration configuration, Method method) {
        List<InvocationInterceptorFactory> list =
                new ArrayList<InvocationInterceptorFactory>();
        
        List<InvocationInterceptorFactory> factories = 
                getInterceptorFactories(configuration, method);
        list.addAll(factories);
        
        List<InvocationInterceptorFactory> defaultFactories = 
                getDefaultInterceptorFactories(configuration, method);
        for (Iterator<InvocationInterceptorFactory> i = 
                defaultFactories.iterator(); i.hasNext();) {
            InvocationInterceptorFactory factory = i.next();
            for (InvocationInterceptorFactory f : list) {
                if (f.getType().equals(factory.getType())) {
                    i.remove();
                    break;
                }
            }
        }
        list.addAll(0, defaultFactories);
        
        InvocationInterceptorFactory localFactory = 
                getLocalInterceptorFactory(configuration);
        if (localFactory != null) {
            list.add(localFactory);
        }
        
        return Collections.unmodifiableList(list);
    }
 
    private List<InvocationInterceptorFactory> getDefaultInterceptorFactories(
            final ComponentConfiguration configuration, Method method) {
        List<InvocationInterceptorFactory> factories = 
                new ArrayList<InvocationInterceptorFactory>();
        if (configuration instanceof BeanletConfiguration) {
            AnnotationDomain domain = ((BeanletConfiguration) 
                    configuration).getAnnotationDomain();
            Package pkg = configuration.getType().getPackage();
            if (pkg != null && !domain.getDeclaration(
                    ExcludeDefaultInterceptors.class).isAnnotationPresent(
                    MethodElement.instance(method))) {
                Interceptors annotation = domain.
                        getDeclaration(Interceptors.class).
                        getAnnotation(PackageElement.instance(pkg));
                if (annotation != null) {
                    for (final Class<?> cls : annotation.value()) {
                        factories.add(new InvocationInterceptorFactory() {
                            public Class<?> getType() {
                                return cls;
                            }
                            public List<InvocationInterceptor> getInvocationInterceptors(
                                    Object instance, 
                                    ConstructorInjection injection, 
                                    ProxyController controller) {
                                return Collections.unmodifiableList(transformInterceptors(
                                        createInterceptors(instance, injection, cls, 
                                        (BeanletConfiguration) configuration), true));
                            }
                        });
                    }
                }
            }
        }
        return factories;
    }

    private List<InvocationInterceptorFactory> getInterceptorFactories(
            final ComponentConfiguration configuration, Method method) {
        List<InvocationInterceptorFactory> factories = 
                new ArrayList<InvocationInterceptorFactory>();
        if (configuration instanceof BeanletConfiguration) {
            final Set<Class> lifecycleInterceptors = new HashSet<Class>();
            LinkedHashSet<Class> interceptors = new LinkedHashSet<Class>();
            AnnotationDomain domain = ((BeanletConfiguration) 
                    configuration).getAnnotationDomain();
            if (!domain.getDeclaration(ExcludeClassInterceptors.class).
                    isAnnotationPresent(MethodElement.instance(method))) {
                Interceptors annotation = domain.
                        getDeclaration(Interceptors.class).getAnnotation(
                        TypeElement.instance(configuration.getType()));
                if (annotation != null) {
                    lifecycleInterceptors.addAll(Arrays.asList(annotation.value()));
                    interceptors.addAll(Arrays.asList(annotation.value()));
                }
            }
            Interceptors annotation = domain.
                    getDeclaration(Interceptors.class).getAnnotation(
                    MethodElement.instance(method));
            if (annotation != null) {
                for (Class<?> cls : annotation.value()) {
                    if (interceptors.contains(cls)) {
                        interceptors.remove(cls);
                    }
                    interceptors.add(cls);
                }
            }
            for (final Class<?> cls : interceptors) {
                factories.add(new InvocationInterceptorFactory() {
                    public Class<?> getType() {
                        return cls;
                    }
                    public List<InvocationInterceptor> getInvocationInterceptors(
                            Object instance, 
                            ConstructorInjection injection,
                            ProxyController controller) {
                        return Collections.unmodifiableList(transformInterceptors(
                                createInterceptors(instance, injection, cls, 
                                (BeanletConfiguration) configuration),
                                lifecycleInterceptors.contains(cls)));
                    }
                });
            }
        }
        return factories;
    }

   /**
     * Creates local interceptor (list) from the specified 
     * {@code instance}. An empty list is returned if {@code instance} does not
     * implement an internal interceptor.
     */
    private InvocationInterceptorFactory getLocalInterceptorFactory(
            final ComponentConfiguration configuration) {
        InvocationInterceptorFactory factory = null;
        if (configuration instanceof BeanletConfiguration) {
            if (Interceptor.class.isAssignableFrom(configuration.getType())) {
                final ThreadLocal<org.jargo.InvocationContext> local =
                        new ThreadLocal<org.jargo.InvocationContext>();
                final InvocationContext newCtx = new InvocationContextImpl() {
                    protected org.jargo.InvocationContext
                            getInvocationContext() {
                        return local.get();
                    }
                };
                factory = new InvocationInterceptorFactory() {
                    public Class<?> getType() {
                        return configuration.getType();
                    }
                    public List<InvocationInterceptor> getInvocationInterceptors(
                            final Object instance, 
                            ConstructorInjection injection,
                            ProxyController controller) {
                        return Collections.singletonList((InvocationInterceptor) new InvocationInterceptorAdapter() {
                            public Object getInstance() {
                                return instance;
                            }
                            public boolean isLifecycleInterceptor() {
                                // Local interceptors are per definition no callback interceptor.
                                return false;
                            }
                            public Object intercept(final org.jargo.InvocationContext ctx)
                                    throws Exception {
                                local.set(ctx);
                                return ((Interceptor) instance).intercept(newCtx);
                            }
                            public String toString() {
                                return "InvocationInterceptorAdapter[" + getInstance() + "]@" +
                                        Integer.toHexString(System.identityHashCode(this));
                            }
                        });
                    }
                };
            }
            AnnotationDomain domain = ((BeanletConfiguration) configuration).
                    getAnnotationDomain();
            List<ElementAnnotation<MethodElement, AroundInvoke>> list = domain.
                    getDeclaration(AroundInvoke.class).getTypedElements(
                    MethodElement.class, configuration.getType());
            if (list.size() > 1) {
                throw new BeanletValidationException(configuration.getComponentName(),
                        "Class MAY declare one AroundInvoke method: '" + configuration.getType() + "'.");
            }
            if (!list.isEmpty()) {
                final Method method = list.get(0).getElement().getMethod();
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Method MUST NOT be static: '" + method + "'.");
                }
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1 || !types[0].equals(
                        InvocationContext.class)) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Method MUST specify InvocationContext as parameter: '" + method + "'.");
                }
                final ThreadLocal<org.jargo.InvocationContext> local =
                        new ThreadLocal<org.jargo.InvocationContext>();
                final InvocationContext newCtx = new InvocationContextImpl() {
                    protected org.jargo.InvocationContext
                            getInvocationContext() {
                        return local.get();
                    }
                };
                factory = new InvocationInterceptorFactory() {
                    public Class<?> getType() {
                        return configuration.getType();
                    }
                    public List<InvocationInterceptor> getInvocationInterceptors(
                            final Object instance, 
                            ConstructorInjection injection,
                            ProxyController controller) {
                        return Collections.singletonList((InvocationInterceptor) new InvocationInterceptorAdapter() {
                            public Object getInstance() {
                                return instance;
                            }
                            public boolean isLifecycleInterceptor() {
                                // Local interceptors are per definition no callback interceptor.
                                return false;
                            }
                            public Object intercept(final org.jargo.InvocationContext ctx)
                                    throws Exception {
                                local.set(ctx);
                                return InvocationImpl.invoke(instance, method, newCtx);
                            }
                            public String toString() {
                                return "InvocationInterceptorAdapter[" + getInstance() + "]@" +
                                        Integer.toHexString(System.identityHashCode(this));
                            }
                        });
                    }
                };
            }
        }
        return factory;
    }
    
    private List<InvocationInterceptor> transformInterceptors(
            List<Interceptor> interceptors, final boolean lifecycleInterceptor) {
        List<InvocationInterceptor> transformed =
                new ArrayList<InvocationInterceptor>();
        for (final Interceptor interceptor : interceptors) {
            final ThreadLocal<org.jargo.InvocationContext> local =
                    new ThreadLocal<org.jargo.InvocationContext>();
            final InvocationContext newCtx = new InvocationContextImpl() {
                protected org.jargo.InvocationContext
                        getInvocationContext() {
                    return local.get();
                }
            };
            transformed.add(new InvocationInterceptorAdapter() {
                public Object getInstance() {
                    final Object instance;
                    if (interceptor instanceof InterceptorAdapter) {
                        instance = ((InterceptorAdapter) interceptor).getInstance();
                    } else {
                        instance = interceptor;
                    }
                    return instance;
                }
                public boolean isLifecycleInterceptor() {
                    return lifecycleInterceptor;
                }
                public Object intercept(final org.jargo.InvocationContext ctx)
                        throws Exception {
                    local.set(ctx);
                    return interceptor.intercept(newCtx);
                }
                public String toString() {
                    return "InvocationInterceptorAdapter[" + getInstance() + "]@" +
                            Integer.toHexString(System.identityHashCode(this));
                }
            });
        }
        return transformed;
    }
    
    private List<Interceptor> createInterceptors(Object instance, 
            ConstructorInjection injection, Class<?> cls, 
            BeanletConfiguration configuration) {
        final List<Interceptor> interceptors;
        Map<Class, List<Interceptor>> map = interceptorMap.get(instance);
        if (map != null && map.containsKey(cls)) {
            interceptors = map.get(cls);
        } else {
            interceptors = new ArrayList<Interceptor>();
            String beanletName = configuration.getComponentName();

            final Object o;
            if (injection != null) {
                o = injection.inject();
            } else {
                try {
                    o = cls.newInstance();
                } catch (IllegalAccessException e) {
                    throw new BeanletException(beanletName, e);
                } catch (InstantiationException e) {
                    throw new BeanletException(beanletName, e);
                }
            }
            LinkedList<Class> classes = new LinkedList<Class>();
            Class<?> tmp = cls;
            do {
                classes.addFirst(tmp);
            } while ((tmp = tmp.getSuperclass()) != null);

            AnnotationDeclaration<AroundInvoke> declaration = configuration.
                    getAnnotationDomain().getDeclaration(AroundInvoke.class);

            for (Class c : classes) {
                AroundInvoke aroundInvoke = null;
                for (ElementAnnotation<MethodElement, AroundInvoke> ea : 
                        declaration.getTypedElements(MethodElement.class, c)) {
                    final Method method = ea.getElement().getMethod();
                     if (method.getDeclaringClass().equals(c)) {
                        if (aroundInvoke != null) {
                            throw new BeanletValidationException(beanletName, 
                                    method.getName() + 
                                    "Class MAY declare only one AroundInvoke method: '" + c + "'.");
                        }
                        aroundInvoke = ea.getAnnotation();

                        Class<?>[] types = method.getParameterTypes();
                        if (types.length != 1 || !types[0].equals(
                                InvocationContext.class)) {
                            throw new BeanletValidationException(beanletName,
                                    "Method MUST " +
                                    "have InterceptorContext object " +
                                    "as parameter: '" + method + "'.");
                        }
                        if (!Object.class.isAssignableFrom(method.getReturnType())) {
                            throw new BeanletValidationException(beanletName,
                                    "Return type of method " +
                                    "MUST be Object (or any subclass): '" + method + "'.");
                        }
                        if (Modifier.isStatic(method.getModifiers())) {
                            throw new BeanletValidationException(beanletName,
                                    "Method MUST NOT be static: '" + method + "'.");
                        }

                        if (!ea.getElement().isOverridden(cls)) {
                            if (o instanceof Interceptor) {
                                if (!method.getName().equals(INTERCEPT_METHOD.getName())) {
                                    throw new BeanletValidationException(beanletName,
                                            "ArroundInvoke and " + Interceptor.class.getName() + 
                                            " mixture: '" + method + "'.");
                                } else {
                                    // Interceptor is added after loop.
                                    continue;
                                }
                            }
                            interceptors.add(new InterceptorAdapter() {
                                public Object intercept(InvocationContext ctx)
                                        throws Exception {
                                    return InvocationImpl.invoke(o, method, ctx);
                                }
                                public Object getInstance() {
                                    return o;
                                }
                                public String toString() {
                                    return "InvocationInterceptorAdapter[" + getInstance() + "]@" +
                                            Integer.toHexString(System.identityHashCode(this));
                                }
                            });
                        }
                    }
                }
            }

            if (o instanceof Interceptor) {
                assert interceptors.isEmpty();
                interceptors.add((Interceptor) o);
            }

            if (interceptors.isEmpty()) {
                interceptors.add(new InterceptorAdapter() {
                    public Object intercept(InvocationContext ctx)
                            throws Exception {
                        return ctx.proceed();
                    }
                    public Object getInstance() {
                        return o;
                    }
                    public String toString() {
                        return "InvocationInterceptorAdapter[" + getInstance() + "]@" +
                                Integer.toHexString(System.identityHashCode(this));
                    }
                });
            }

            if (map == null) {
                map = new ConcurrentHashMap<Class, List<Interceptor>>();
                interceptorMap.put(instance, map);
            }
            map.put(cls, interceptors);
        }
        return interceptors;
    }
}
