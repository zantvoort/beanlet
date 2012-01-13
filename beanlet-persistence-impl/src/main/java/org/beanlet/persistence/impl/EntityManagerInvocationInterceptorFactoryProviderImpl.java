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
package org.beanlet.persistence.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.PersistenceContext;
import org.jargo.InvocationInterceptor;
import org.jargo.ComponentConfiguration;
import org.jargo.deploy.SequentialDeployable;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.ConstructorInjection;
import org.jargo.InvocationInterceptorFactory;
import org.jargo.ProxyController;
import org.jargo.spi.InvocationInterceptorFactoryProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class EntityManagerInvocationInterceptorFactoryProviderImpl implements
        InvocationInterceptorFactoryProvider {
    
    public Sequence sequence(SequentialDeployable deployable) {
        if (deployable.getClass().getName().equals(
                "org.beanlet.transaction.jersey.TransactionInvocationInterceptorFactoryProviderImpl")) {
            return Sequence.AFTER;
        }
        return Sequence.BEFORE;
    }
    
    public List<InvocationInterceptorFactory> getInvocationInterceptorFactories(
            final ComponentConfiguration configuration, Method method) {
        final List<InvocationInterceptorFactory> list;
        if (PersistenceConstants.isSupported() &&
                configuration instanceof BeanletConfiguration) {
            AnnotationDomain domain = ((BeanletConfiguration) configuration).
                    getAnnotationDomain();
            final Set<PersistenceContext> pctxs = 
                    new HashSet<PersistenceContext>();
            for (ElementAnnotation<? extends Element, PersistenceContext> ea : 
                    domain.getDeclaration(PersistenceContext.class).getElements()) {
                pctxs.add(ea.getAnnotation());
            }
            if (pctxs.isEmpty()) {
                list = Collections.emptyList();
            } else {
                list = Collections.<InvocationInterceptorFactory>singletonList(
                        new InvocationInterceptorFactory() {
                    public Class<?> getType() {
                        return EntityManagerInvocationInterceptor.class;
                    }
                    public List<InvocationInterceptor> getInvocationInterceptors(
                            Object instance, 
                            ConstructorInjection injection,
                            ProxyController controller) {
                        return Collections.<InvocationInterceptor>singletonList(
                                new EntityManagerInvocationInterceptor(pctxs, 
                                configuration.getComponentUnit()));
                    }
                });
            }
        } else {
            list = Collections.emptyList();
        }
        return list;
    }    
}
