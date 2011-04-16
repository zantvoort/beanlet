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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jargo.ComponentReference;
import org.jargo.ComponentCreationException;
import org.jargo.ComponentObject;
import org.jargo.ComponentObjectBuilder;
import org.jargo.ComponentObjectPool;

/**
 * @author Leon van Zantvoort
 */
public final class StatefulBeanletObjectPoolImpl<T> implements 
        ComponentObjectPool<T> {

    private final String componentName;
    private final boolean lazy;
    private final boolean reentrant;
    private final boolean destroyOnDiscard;
    private final Lock lock;
    private final Map<ComponentReference<T>, ObjectPool<ComponentObject<T>>> pools;
    
    private ComponentObjectBuilder<T> builder;
    
    public StatefulBeanletObjectPoolImpl(String componentName, boolean lazy, 
            boolean reentrant, boolean destroyOnDiscard) {
        this.componentName = componentName;
        this.lazy = lazy;
        this.reentrant = reentrant;
        this.destroyOnDiscard = destroyOnDiscard;
        this.lock = new ReentrantLock(true);
        this.pools = new HashMap<ComponentReference<T>, ObjectPool<ComponentObject<T>>>();
    }

    public void init(ComponentObjectBuilder<T> builder) throws 
            ComponentCreationException {
        lock.lock();
        try {
            this.builder = builder;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isStatic() {
        return false;
    }
    
    public ComponentObject<T> getComponentObject() throws 
            ComponentCreationException {
        ComponentReference<T> reference = builder.reference().weakReference();
        try {
            final ObjectPool<ComponentObject<T>> pool;
            lock.lockInterruptibly();
            try {
                if (reference.isRemoved()) {
                    return null;
                }
                pool = pools.get(reference);
                assert pool != null;
            } finally {
                lock.unlock();
            }
            return pool.getInstance();
        } catch (InterruptedException e) {
            throw new ComponentCreationException(componentName, e);
        }
    }

    public void freeComponentObject(ComponentObject<T> object) {
        ComponentReference<T> reference = builder.reference().weakReference();
        boolean destroy = false;
        lock.lock();
        try {
            ObjectPool<ComponentObject<T>> pool = pools.get(reference);
            assert pool != null;
            destroy = !pool.freeInstance(object);
            if (destroy) {
                assert pool.isDestroyed();
                pools.remove(reference);
            }
        } finally {
            lock.unlock();
            if (destroy) {
                object.destroy();
            }
        }
    }

    public void discardComponentObject(ComponentObject<T> object) {
        ComponentReference<T> reference = builder.reference().weakReference();
        try {
            lock.lock();
            try {
                ObjectPool<ComponentObject<T>> pool = pools.get(reference);
                assert pool != null;
                if (!pool.discardInstance(object)) {
                    assert pool.isDestroyed();
                    pools.remove(reference);
                }
            } finally {
                lock.unlock();
            }
        } finally {
            if (destroyOnDiscard) {
                reference.invalidate();
            } else {
                reference.remove();
            }
        }
    }
    
    public ComponentObject<T> create() throws ComponentCreationException {
        ComponentReference<T> reference = builder.reference().weakReference();
        lock.lock();
        try {
            assert reference.isValid();
            ObjectPool<ComponentObject<T>> pool = pools.get(reference);
            assert pool == null;
            pool = reentrant ? 
                new ReentrantObjectPool<ComponentObject<T>>(lazy ? 0 : 1, 1) : 
                new NonReentrantObjectPool<ComponentObject<T>>(lazy ? 0 : 1, 1);
            pool.init(new ObjectPool.Factory<ComponentObject<T>>() {
                public ComponentObject<T> newInstance() {
                    // Throws ComponentCreationException on failure.
                    return builder.newInstance();
                }
            });
            pools.put(reference, pool);
        } finally {
            lock.unlock();
        }
        return null;
    }
    
    public void remove() {
        ComponentReference<T> reference = builder.reference().weakReference();
        List<ComponentObject<T>> objects = 
                new ArrayList<ComponentObject<T>>();
        lock.lock();
        try {
            ObjectPool<ComponentObject<T>> pool = pools.get(reference);
            if (pool != null) {
                objects.addAll(pool.destroy());
                if (pool.isDestroyed()) {
                    pools.remove(reference);
                }
            }
        } finally {
            lock.unlock();
            if (!reference.isRemoved()) {
                for (ComponentObject<T> object : objects) {
                    object.destroy();
                }
            }
        }
    }

    public void destroy() {
        RuntimeException x = null;
        for (ComponentReference<T> reference : pools.keySet()) {
            try {
                reference.invalidate();
            } catch (RuntimeException e) {
                x = e;
            }
        }
        assert pools.isEmpty();
        if (x != null) {
            throw x;
        }
    }
}
