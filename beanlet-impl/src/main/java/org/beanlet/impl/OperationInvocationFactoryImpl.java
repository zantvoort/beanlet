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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.beanlet.common.InvocationImpl;
import org.beanlet.event.OperationEvent;
import org.beanlet.common.event.OperationEventImpl;
import org.jargo.ComponentConfiguration;
import org.jargo.Event;
import org.jargo.InvocationFactory;
import org.jargo.Invocation;

/**
 * @author Leon van Zantvoort
 */
public final class OperationInvocationFactoryImpl implements 
        InvocationFactory {
    
    private final Operations operations;
    private final ProxyMethods proxyMethods;
    private final Set<Method> methods;
    
    public OperationInvocationFactoryImpl(ComponentConfiguration 
            configuration) {
        this.operations = Operations.getInstance(configuration);
        this.proxyMethods = ProxyMethods.getInstance(configuration);
        
        Set<Method> tmp = new LinkedHashSet<Method>();
        tmp.addAll(operations.getMethods());
        Method proxyMethod = proxyMethods.getProxyMethod();
        if (proxyMethod != null) {
            tmp.add(proxyMethod);
        }
        
        this.methods = Collections.unmodifiableSet(tmp);
    }

    public Set<Method> getMethods() {
        return methods;
    }

    public Set<Class<? extends Event>> getEventTypes() {
        Set<Class<? extends Event>> c = 
                new LinkedHashSet<Class<? extends Event>>();
        c.add(OperationEventImpl.class);
        return Collections.unmodifiableSet(c);
    }
    
    public Invocation getInvocation(Event event) {
        final Invocation invocation;
        if (event instanceof OperationEvent) {
            OperationEvent operationEvent = (OperationEvent) event;
            Method method = operations.getMethod(operationEvent.getOperationName(), 
                    operationEvent.getParameterTypes());
            if (method != null) {
                invocation = new InvocationImpl(method, 
                        operationEvent.getParameters());
            } else {
                method = proxyMethods.getProxyMethod();
                if (method != null) {
                    Object[] args = new Object[] {
                        operationEvent.getOperationName(),
                        operationEvent.getParameterTypes(),
                        operationEvent.getParameters()
                    };
                    invocation = new InvocationImpl(method, args);
                } else {
                    invocation = null;
                }
            }
        } else {
            invocation = null;
        }
        return invocation;
    }
}
