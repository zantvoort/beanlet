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
import java.util.Collections;
import java.util.List;
import org.beanlet.BeanletValidationException;
import org.beanlet.common.AbstractProvider;
import org.jargo.InvocationInterceptorFactory;
import org.jargo.deploy.SequentialDeployable;
import org.beanlet.Remove;
import org.beanlet.Stateful;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.InvocationInterceptor;
import org.jargo.spi.InvocationInterceptorFactoryProvider;
import org.jargo.ComponentConfiguration;
import org.jargo.ConstructorInjection;
import org.jargo.ProxyController;

/**
 * @author Leon van Zantvoort
 */
public final class RemoveInvocationInterceptorFactoryProviderImpl extends AbstractProvider
        implements InvocationInterceptorFactoryProvider {
    
    public Sequence sequence(SequentialDeployable deployable) {
        return Sequence.AFTER;
    }
    
    public List<InvocationInterceptorFactory> getInvocationInterceptorFactories(
            ComponentConfiguration configuration, Method method) {
        List<InvocationInterceptorFactory> factories = 
                new ArrayList<InvocationInterceptorFactory>();
        if (configuration instanceof BeanletConfiguration) {
            AnnotationDomain domain = ((BeanletConfiguration) configuration).
                    getAnnotationDomain();
            final Remove remove = domain.getDeclaration(Remove.class).
                    getAnnotation(MethodElement.instance(method));
            if (remove != null) {
                if (!domain.getDeclaration(Stateful.class).
                        isAnnotationPresent(
                        TypeElement.instance(configuration.getType()))) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Remove annotation MAY only be applied to Stateful beanlets.");
                }
                factories.add(new InvocationInterceptorFactory() {
                    public Class<?> getType() {
                        return RemoveInvocationInterceptor.class;
                    }
                    public List<InvocationInterceptor> getInvocationInterceptors(
                            Object instance, ConstructorInjection injection, 
                            ProxyController controller) {
                        return Collections.singletonList((InvocationInterceptor) 
                                new RemoveInvocationInterceptor(remove));                        
                    }
                });
            }
        }
        return Collections.unmodifiableList(factories);
    }
}
