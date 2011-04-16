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

import org.beanlet.common.ObjectPool;
import org.beanlet.common.NonReentrantObjectPool;
import org.beanlet.common.ReentrantObjectPool;
import org.jargo.ComponentCreationException;
import org.jargo.ComponentObject;
import org.jargo.ComponentObjectBuilder;
import org.jargo.ComponentObjectPool;

/**
 * @author Leon van Zantvoort
 */
public final class StatelessBeanletObjectPoolImpl<T> implements 
        ComponentObjectPool<T> {
    
    private final String componentName;
    private final boolean reentrant;
    private final boolean destroyOnDiscard;
    private final ObjectPool<ComponentObject<T>> pool;
    
    public StatelessBeanletObjectPoolImpl(String componentName, boolean lazy, 
            boolean reentrant, boolean singleton, boolean destroyOnDiscard) {
        int minSize = lazy ? 0 : 1;
        int maxSize = singleton ? 1 : 0;
        
        this.componentName = componentName;
        this.reentrant = reentrant;
        this.pool = reentrant ?
                new ReentrantObjectPool<ComponentObject<T>>(minSize, maxSize) :
                new NonReentrantObjectPool<ComponentObject<T>>(minSize, maxSize);
        this.destroyOnDiscard = destroyOnDiscard;
    }

    public boolean isStatic() {
        return true;
    }
    
    public void init(final ComponentObjectBuilder<T> builder) throws 
            ComponentCreationException {
        pool.init(new ObjectPool.Factory<ComponentObject<T>>() {
            public ComponentObject<T> newInstance() {
                return builder.newInstance();
            }
        });
    }
    
    public ComponentObject<T> getComponentObject() throws 
            ComponentCreationException {
        try {
            return pool.getInstance();
        } catch (InterruptedException e) {
            throw new ComponentCreationException(componentName, e);
        }
    }
    
    public void freeComponentObject(ComponentObject<T> object) {
        if (!pool.freeInstance(object)) {
            object.destroy();
        }
    }
    
    public void discardComponentObject(ComponentObject<T> object) {
        pool.discardInstance(object);
        if (destroyOnDiscard) {
            object.destroy();
        }
    }
    
    public ComponentObject<T> create() throws ComponentCreationException {
        assert false;
        return null;
    }
    
    public void remove() {
        assert false;
    }
    
    public void destroy() {
        RuntimeException x = null;
        for (ComponentObject<T> object : pool.destroy()) {
            try {
                object.destroy();
            } catch (RuntimeException e) {
                x = e;
            }
        }
        if (x != null) {
            throw x;
        }
    }
}
