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

import static org.beanlet.RetentionPolicy.*;
import org.beanlet.RetentionPolicy;
import org.jargo.ComponentEventException;
import org.jargo.ComponentExecutionException;
import org.jargo.Event;
import org.jargo.EventExecutor;
import org.jargo.ComponentObject;
import org.jargo.ComponentObjectFactory;
import org.jargo.ComponentObjectPool;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletEventExecutorImpl<T> implements EventExecutor<T> {
    
    private final String componentName;
    private final RetentionPolicy retentionPolicy;
    private final Class<? extends Throwable>[] exceptionType;
    
    public BeanletEventExecutorImpl(String componentName,
            RetentionPolicy retentionPolicy, 
            Class<? extends Throwable>[] exceptionType) {
        this.componentName = componentName;
        this.retentionPolicy = retentionPolicy;
        this.exceptionType = exceptionType.clone();
    }

    @SuppressWarnings("unchecked")  // b/c of bug in the compiler.
    public Object execute(Event event, ComponentObjectFactory<T> factory) 
            throws ComponentEventException {
        ComponentObject<T> componentObject = factory.getComponentObject();
        if (componentObject == null) {
            throw new ComponentEventException(componentName, event, 
                    "No instance available.");
        }
        Throwable t = null;
        try {
            return componentObject.execute(event);
        } catch (ComponentExecutionException e) {
            t = e.getCause();
            assert t != null;
            throw e;
        } finally {
            if (factory instanceof ComponentObjectPool) {
                ComponentObjectPool<T> pool = (ComponentObjectPool<T>) factory;
                boolean discard = false;
                if (retentionPolicy != RETAIN && t != null) {
                    for (Class c : exceptionType) {
                        if (c.isAssignableFrom(t.getClass())) {
                            discard = true;
                            break;
                        }                    
                    }
                }
                if (discard) {
                    pool.discardComponentObject(componentObject);
                } else {
                    pool.freeComponentObject(componentObject);
                }
            }
        }
    }
}
