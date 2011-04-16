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
package org.beanlet.transaction.impl;

import static org.beanlet.transaction.impl.TransactionHelper.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.beanlet.BeanletValidationException;
import org.jargo.InvocationInterceptor;
import org.jargo.ComponentConfiguration;
import org.jargo.ProxyController;
import org.jargo.deploy.SequentialDeployable;
import org.beanlet.Stateless;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.transaction.AfterBegin;
import org.beanlet.transaction.AfterCompletion;
import org.beanlet.transaction.BeforeCompletion;
import org.beanlet.transaction.TransactionAttribute;
import org.beanlet.transaction.TransactionSynchronization;
import org.jargo.ConstructorInjection;
import org.jargo.InvocationInterceptorFactory;
import org.jargo.spi.InvocationInterceptorFactoryProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class TransactionInvocationInterceptorFactoryProviderImpl implements
        InvocationInterceptorFactoryProvider {
    
    private final Map<Object, Map<TransactionAttribute, TransactionInvocationInterceptor>> interceptorMap;
    private static final Logger logger = Logger.getLogger(TransactionInvocationInterceptorFactoryProviderImpl.class.getName());
    
    public TransactionInvocationInterceptorFactoryProviderImpl() {
        // PENDING: replace WeakHashMap by a WeakIdentityHashMap.
        interceptorMap = Collections.synchronizedMap(
                new WeakHashMap<Object, Map<TransactionAttribute, TransactionInvocationInterceptor>>());
    }
    
    public Sequence sequence(SequentialDeployable deployable) {
        return Sequence.BEFORE;
    }
    
    public List<InvocationInterceptorFactory> getInvocationInterceptorFactories(
            final ComponentConfiguration<?> configuration, final Method method) {
        final List<InvocationInterceptorFactory> factories;
        final TransactionAttribute attribute;
        if (configuration instanceof BeanletConfiguration) {
            AnnotationDeclaration<TransactionAttribute> declaration =
                    ((BeanletConfiguration<?>) configuration).getAnnotationDomain().
                    getDeclaration(TransactionAttribute.class);
            if (declaration.isAnnotationPresent(MethodElement.instance(method))) {
                attribute = declaration.getAnnotation(MethodElement.instance(method));
            } else {
                attribute = declaration.getAnnotation(TypeElement.instance(
                        configuration.getType()));
            }
            if (attribute == null) {
                factories = Collections.emptyList();
            } else {
                factories = Collections.singletonList((InvocationInterceptorFactory)
                new InvocationInterceptorFactory() {
                    public Class<?> getType() {
                        return TransactionInvocationInterceptor.class;
                    }
                    public List<InvocationInterceptor> getInvocationInterceptors(
                            Object instance, ConstructorInjection<?> injection,
                            ProxyController controller) {
                        Map<TransactionAttribute, TransactionInvocationInterceptor> map =
                                interceptorMap.get(instance);
                        if (map == null) {
                            map = Collections.synchronizedMap(
                                    new HashMap<TransactionAttribute, TransactionInvocationInterceptor>());
                            interceptorMap.put(instance, map);
                        }
                        TransactionInvocationInterceptor interceptor =
                                map.get(attribute);
                        if (interceptor == null) {
                            interceptor =
                                    new TransactionInvocationInterceptor(
                                    configuration,
                                    getTransactionManager(configuration.getComponentName(), method),
                                    attribute.value(),
                                    attribute.timeout(),
                                    getTransactionSynchronization((BeanletConfiguration<?>) configuration, 
                                    new WeakReference<Object>(instance), controller));
                            map.put(attribute, interceptor);
                        }
                        return Collections.singletonList(
                                (InvocationInterceptor) interceptor);
                    }
                });
            }
        } else {
            factories = Collections.emptyList();
        }
        return factories;
    }
    
    private static TransactionSynchronization getTransactionSynchronization(
            BeanletConfiguration<?> configuration, 
            final WeakReference<Object> ref, final ProxyController controller) {
        Class<?> type = configuration.getType();
        
        Method tmpAfterBegin = null;
        Method tmpBeforeCompletion = null;
        Method tmpAfterCompletion = null;
        
        if (TransactionSynchronization.class.isAssignableFrom(type)) {
            try {
                tmpAfterBegin = type.getMethod("afterBegin");
                tmpBeforeCompletion = type.getMethod("beforeCompletion");
                tmpAfterCompletion = type.getMethod("afterCompletion", Boolean.TYPE);
            } catch (NoSuchMethodException e) {
                assert false : e;
            }
        }
        
        AnnotationDomain domain = configuration.getAnnotationDomain();
        AnnotationDeclaration<AfterBegin> afterBegin = domain.
                getDeclaration(AfterBegin.class);
        for (ElementAnnotation<MethodElement, AfterBegin> ea :
                afterBegin.getTypedElements(MethodElement.class)) {
            Method method = ea.getElement().getMethod();
            if (method.getParameterTypes().length == 0 &&
                    method.getReturnType().equals(Void.TYPE)) {
                if (!ea.getElement().isOverridden(configuration.getType())) {
                    if (tmpAfterBegin != null) {
                        if (!method.getName().equals("afterBegin")) {
                            throw new BeanletValidationException(configuration.getComponentName(),
                                    "Multiple AfterBegin methods found.");
                        }
                    }
                    tmpAfterBegin = method;
                }
            } else {
                throw new BeanletValidationException(configuration.getComponentName(),
                        "Invalid signature for AfterBegin method: '" + method + "'.");
            }
        }
        AnnotationDeclaration<BeforeCompletion> beforeCompletion = domain.
                getDeclaration(BeforeCompletion.class);
        for (ElementAnnotation<MethodElement, BeforeCompletion> ea :
                beforeCompletion.getTypedElements(MethodElement.class)) {
            Method method = ea.getElement().getMethod();
            if (method.getParameterTypes().length == 0 &&
                    method.getReturnType().equals(Void.TYPE)) {
                if (!ea.getElement().isOverridden(configuration.getType())) {
                    if (tmpBeforeCompletion != null) {
                        if (!method.getName().equals("beforeCompletion")) {
                            throw new BeanletValidationException(configuration.getComponentName(),
                                    "Multiple BeforeCompletion methods found.");
                        }
                    }
                    tmpBeforeCompletion = method;
                }
            } else {
                throw new BeanletValidationException(configuration.getComponentName(),
                        "Invalid signature for BeforeCompletion method: '" + method + "'.");
            }
        }
        AnnotationDeclaration<AfterCompletion> afterCompletion = domain.
                getDeclaration(AfterCompletion.class);
        for (ElementAnnotation<MethodElement, AfterCompletion> ea :
                afterCompletion.getTypedElements(MethodElement.class)) {
            Method method = ea.getElement().getMethod();
            if (method.getParameterTypes().length == 1 &&
                    method.getParameterTypes()[0].equals(Boolean.TYPE) &&
                    method.getReturnType().equals(Void.TYPE)) {
                if (!ea.getElement().isOverridden(configuration.getType())) {
                    if (tmpAfterCompletion != null) {
                        if (!method.getName().equals("afterCompletion")) {
                            throw new BeanletValidationException(configuration.getComponentName(),
                                    "Multiple AfterCompletion methods found.");
                        }
                    }
                    tmpAfterCompletion = method;
                }
            } else {
                throw new BeanletValidationException(configuration.getComponentName(),
                        "Invalid signature for AfterCompletion method: '" + method + "'.");
            }
        }

        final Method afterBeginMethod = tmpAfterBegin;
        final Method beforeCompletionMethod = tmpBeforeCompletion;
        final Method afterCompletionMethod = tmpAfterCompletion;
        final TransactionSynchronization synchronization;
        if (afterBeginMethod != null || tmpBeforeCompletion != null ||
                tmpAfterCompletion != null) {
            if (domain.getDeclaration(Stateless.class).isAnnotationPresent(
                    TypeElement.instance(configuration.getType()))) {
                throw new BeanletValidationException(configuration.getComponentName(),
                        "TransactionSynchronization callback prohibited for Stateless beanlets.");
            }

            synchronization = new TransactionSynchronization() {
                public void afterBegin() {
                    if (afterBeginMethod != null) {
                        if (!afterBeginMethod.isAccessible()) {
                            afterBeginMethod.setAccessible(true);
                        }
                        try {
                            try {
                                Object instance = ref.get();
                                if (instance == null) {
                                    logger.severe("Assertion failed: referent is null on afterBegin.");
                                } else {
                                    try {
                                        if (controller != null) {
                                            controller.attach(true);
                                        }
                                        afterBeginMethod.invoke(instance);
                                    } finally {
                                        if (controller != null) {
                                            controller.detach();
                                        }
                                    }
                                }
                            } catch (InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Error e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    }
                }
                public void beforeCompletion() {
                    if (beforeCompletionMethod != null) {
                        if (!beforeCompletionMethod.isAccessible()) {
                            beforeCompletionMethod.setAccessible(true);
                        }
                        try {
                            try {
                                Object instance = ref.get();
                                if (instance == null) {
                                    logger.severe("Assertion failed: referent is null on beforeCompletion.");
                                } else {
                                    try {
                                        if (controller != null) {
                                            controller.attach(true);
                                        }
                                        beforeCompletionMethod.invoke(instance);
                                    } finally {
                                        if (controller != null) {
                                            controller.detach();
                                        }
                                    }
                                }
                            } catch (InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Error e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    }
                }
                public void afterCompletion(boolean committed) {
                    if (afterCompletionMethod != null) {
                        if (!afterCompletionMethod.isAccessible()) {
                            afterCompletionMethod.setAccessible(true);
                        }
                        try {
                            try {
                                Object instance = ref.get();
                                if (instance == null) {
                                    logger.severe("Assertion failed: referent is null on afterCompletion.");
                                } else {
                                    try {
                                        if (controller != null) {
                                            controller.attach(true);
                                        }
                                        afterCompletionMethod.invoke(instance, committed);
                                    } finally {
                                        if (controller != null) {
                                            controller.detach();
                                        }
                                    }
                                }
                            } catch (InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Error e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    }
                }
            };
        } else {
            synchronization = null;
        }
        return synchronization;
    }
}
