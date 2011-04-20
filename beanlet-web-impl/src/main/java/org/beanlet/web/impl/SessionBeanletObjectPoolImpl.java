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
package org.beanlet.web.impl;

import org.beanlet.common.ObjectPool;
import org.beanlet.common.NonReentrantObjectPool;
import org.beanlet.common.ReentrantObjectPool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.jargo.ComponentReference;
import org.jargo.ComponentCreationException;
import org.jargo.ComponentObject;
import org.jargo.ComponentObjectBuilder;
import org.jargo.ComponentObjectPool;

/**
 * @author Leon van Zantvoort
 */
public final class SessionBeanletObjectPoolImpl<T> implements 
        ComponentObjectPool<T> {

    private final String componentName;
    private final boolean lazy;
    private final boolean reentrant;
    private final boolean destroyOnDiscard;
    private final Lock lock;
    private final Map<ComponentReference<T>, 
            Map<HttpSession, ObjectPool<ComponentObject<T>>>> pools;
    
    private ComponentObjectBuilder<T> builder;
    
    public SessionBeanletObjectPoolImpl(String componentName, boolean lazy, 
            boolean reentrant, boolean destroyOnDiscard) {
        this.componentName = componentName;
        this.lazy = lazy;
        this.reentrant = reentrant;
        this.destroyOnDiscard = destroyOnDiscard;
        this.lock = new ReentrantLock(true);
        this.pools = new HashMap<ComponentReference<T>, Map<HttpSession, 
                ObjectPool<ComponentObject<T>>>>();
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
        final ComponentReference<T> reference = builder.reference();
        try {
            ObjectPool<ComponentObject<T>> pool;
            lock.lockInterruptibly();
            try {
                if (reference.isRemoved()) {
                    return null;
                }
                final Map<HttpSession, ObjectPool<ComponentObject<T>>> map =
                        pools.get(reference.weakReference());
                assert map != null;
                ServletRequest request = RequestContextListener.get();
                if (request == null || !(request instanceof HttpServletRequest)) {
                    throw new ComponentCreationException(componentName, 
                            "No http servlet request active.");
                }
                final HttpSession session = ((HttpServletRequest) request).getSession();
                pool = map.get(session);
                if (pool == null) {
                    final ObjectPool<ComponentObject<T>> tmp = reentrant ? 
                        new ReentrantObjectPool<ComponentObject<T>>(lazy ? 0 : 1, 1) : 
                        new NonReentrantObjectPool<ComponentObject<T>>(lazy ? 0 : 1, 1);
                    tmp.init(new ObjectPool.Factory<ComponentObject<T>>() {
                        public ComponentObject<T> newInstance() {
                            // Throws ComponentCreationException on failure.
                            return builder.newInstance();
                        }
                    });
                    pool = tmp;
                    map.put(session, pool);
                    RequestContextListener.setSessionDestroyHook(new Runnable() {
                        public void run() {
                            lock.lock();
                            try {
                                map.remove(session);
                            } finally {
                                lock.unlock();
                                builder.attach(reference);
                                try {
                                    for (ComponentObject object : tmp.destroy()) {
                                        object.destroy();
                                    }
                                } finally {
                                    builder.detach();
                                }
                            }
                        }
                    });
                }
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
            Map<HttpSession, ObjectPool<ComponentObject<T>>> map = pools.
                    get(reference);
            assert map != null;
            ServletRequest request = RequestContextListener.get();
            assert request != null;
            if (request instanceof HttpServletRequest) {
                HttpSession session = ((HttpServletRequest) request).getSession();
                ObjectPool<ComponentObject<T>> pool = map.get(session);
                assert pool != null;
                destroy = !pool.freeInstance(object);
                if (destroy) {
                    assert pool.isDestroyed();
                    pools.remove(reference);
                }
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
                Map<HttpSession, ObjectPool<ComponentObject<T>>> map = pools.
                        get(reference);
                assert map != null;
                ServletRequest request = RequestContextListener.get();
                assert request != null;
                if (request instanceof HttpServletRequest) {
                    HttpSession session = ((HttpServletRequest) request).getSession();
                    ObjectPool<ComponentObject<T>> pool = map.get(session);
                    assert pool != null;
                    if (!pool.discardInstance(object)) {
                        assert pool.isDestroyed();
                        pools.remove(reference);
                    }
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
            Object ok = pools.put(reference, new IdentityHashMap
                    <HttpSession, ObjectPool<ComponentObject<T>>>());
            assert ok == null;
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
            Map<HttpSession, ObjectPool<ComponentObject<T>>> map = pools.
                    get(reference);
            if (map != null) {
                for (Iterator<ObjectPool<ComponentObject<T>>> i = map.values().
                        iterator(); i.hasNext(); ) {
                    ObjectPool<ComponentObject<T>> pool = i.next();
                    objects.addAll(pool.destroy());
                    if (pool.isDestroyed()) {
                        i.remove();
                    }
                }
                if (map.isEmpty()) {
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
