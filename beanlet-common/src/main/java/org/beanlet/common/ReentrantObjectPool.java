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
package org.beanlet.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Leon van Zantvoort
 */
public final class ReentrantObjectPool<T> implements ObjectPool<T> {
    
    private final int minSize;
    private final int maxSize;
    
    private final Lock lock;
    private final Queue<T> instances;
    
    private ObjectPool.Factory<T> factory;
    private int instanceCount;
    private int useCount;
    
    private boolean destroyed;
    
    public ReentrantObjectPool(int minSize, int maxSize) {
        if (minSize < 0 || maxSize < 0) {
            throw new IllegalArgumentException("< 0");
        }
        if (maxSize != 0 && minSize > maxSize) {
            throw new IllegalArgumentException("MinSize > maxSize.");
        }
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.lock = new ReentrantLock(true);
        this.instances = new LinkedList<T>();
        this.instanceCount = 0;
        this.useCount = 0;
    }
    
    public void init(ObjectPool.Factory<T> factory) {
        lock.lock();
        try {
            this.factory = factory;
            while(instanceCount < minSize) {
                instances.offer(factory.newInstance());
                instanceCount++;
            }
        } finally {
            lock.unlock();
        }
    }
    
    public T getInstance() throws
            InterruptedException {
        final T instance;
        lock.lockInterruptibly();
        if (factory == null) {
            throw new IllegalStateException("Not initialized.");
        }
        try {
            if (destroyed) {
                instance = null;
            } else {
                if ((maxSize == 0 &&
                        (instanceCount < Math.max(minSize, 1) ||
                        instances.isEmpty())) ||
                        (maxSize > 0 && instanceCount < maxSize)) {
                    instance = factory.newInstance();
                    instanceCount++;
                } else {
                    instance = instances.poll();
                    assert instance != null;
                }
                useCount++;
                instances.offer(instance);
            }
        } finally {
            lock.unlock();
        }
        return instance;
    }
    
    public boolean freeInstance(T instance) {
        useCount--;
        // Nothing to be done.
        return true;
    }
    
    public boolean discardInstance(T instance) {
        lock.lock();
        try {
            if (!destroyed) {
                instanceCount--;
                instances.remove(instance);
            }
            useCount--;
            return !destroyed;
        } finally {
            lock.unlock();
        }
    }
    
    public List<T> destroy() {
        List<T> remaining = new ArrayList<T>();
        lock.lock();
        try {
            if (!destroyed) {
                destroyed = true;
                remaining.addAll(instances);
                instances.clear();
                instanceCount = 0;
            }
        } finally {
            lock.unlock();
        }
        return Collections.unmodifiableList(remaining);
    }
    
    public boolean isDestroyed() {
        lock.lock();
        try {
            return destroyed && useCount == 0;
        } finally {
            lock.unlock();
        }
    }
}