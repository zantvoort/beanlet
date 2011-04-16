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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.beanlet.common.event.ExecuteEventImpl;
import org.jargo.ComponentEventException;
import org.jargo.ComponentFactory;
import org.jargo.ComponentReference;
import org.jargo.ComponentReferenceLifecycle;
import org.jargo.ComponentLifecycle;
import org.jargo.Event;

public final class ExecuteBeanletLifecycleImpl<T> implements ComponentLifecycle<T>,
        ComponentReferenceLifecycle<T> {

    private static final Event event = new ExecuteEventImpl();
    
    private final Executor executor;
    private final int threads;
    private final boolean loop;
    private final boolean interrupt;
    private final boolean join;
    
    private final List<AtomicReference<Future>> futures;
    
    private final ThreadLocal<Future> threadLocal;
    private final CountDownLatch latch;
    
    public ExecuteBeanletLifecycleImpl(Executor executor, int threads, 
            boolean loop, boolean interrupt, boolean join) {
        assert threads > 0;
        this.executor = executor;
        this.threads = threads;
        this.loop = loop;
        this.interrupt = interrupt;
        this.join = join;
        this.futures = new ArrayList<AtomicReference<Future>>();
        this.threadLocal = new ThreadLocal<Future>();
        
        this.latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            futures.add(i, new AtomicReference<Future>());
        }
    }
    
    public void onCreate(ComponentFactory<T> factory) {
        if (factory.getComponentMetaData().isStatic()) {
            doCreate(factory.create());
        }
    }
    
    public void onDestroy(ComponentFactory<T> factory) {
        if (factory.getComponentMetaData().isStatic()) {
            doDestroy(factory.create());
        }
    }
    
    public void onCreate(ComponentReference<T> reference) {
        if (!reference.getComponentMetaData().isStatic()) {
            doCreate(reference);
        }
    }
    
    public void onDestroy(ComponentReference<T> reference) {
        if (!reference.getComponentMetaData().isStatic()) {
            doDestroy(reference);
        }
    }
    
    private void doCreate(ComponentReference reference) {
        final ComponentReference ref;
        if (!reference.getComponentMetaData().isStatic()) {
            // Maintain no strong reference to non static beanlet references.
            ref = reference.weakReference();
        } else {
            // Always maintain a strong reference to static beanlet references.
            ref = reference;
        }
        
        final ClassLoader loader = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                // PERMISSION: java.lang.RuntimePermission getClassLoader
                return Thread.currentThread().getContextClassLoader();
            }
        });
        for (int i = 0; i < threads; i++) {
            final AtomicReference<Future> future = futures.get(i);
            
            final AtomicReference<Throwable> throwable = 
                    new AtomicReference<Throwable>();

            final Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        ref.execute(event);
                    } catch (ComponentEventException e) {
                        throwable.set(e);
                        throw e;
                    } catch (RuntimeException e) {
                        throwable.set(e);
                        throw e;
                    } catch (Error e) {
                        throwable.set(e);
                        throw e;
                    }
                }
            };

            class Task extends FutureTask<Object> {
                Task(Runnable runnable) {
                    super(runnable, null);
                }
                public void run() {
                    final ClassLoader org = AccessController.doPrivileged(
                            new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            Thread thread = Thread.currentThread();
                            // PERMISSION: java.lang.RuntimePermission getClassLoader
                            ClassLoader tmp = thread.getContextClassLoader();
                            // PERMISSION: java.lang.RuntimePermission getClassLoader
                            thread.setContextClassLoader(loader);
                            return tmp;
                        }
                    });
                    try {
                        try {
                            threadLocal.set(this);
                            do {
                                runAndReset();
                            } while (loop && !isDone() && ref.isValid());
                        } finally {
                            if (loop && ref.isValid()) {
                                Task task = new Task(runnable);
                                future.set(task);
                                if (!isCancelled()) {
                                    executor.execute(task);
                                } else {
                                    future.set(null);
                                    if (threadLocal.get() != null) {
                                        latch.countDown();
                                        threadLocal.remove();
                                    }
                                }
                            } else {
                                if (threadLocal.get() != null) {
                                    latch.countDown();
                                    threadLocal.remove();
                                }
                            }
                            try {
                                Throwable t = throwable.get();
                                if (t != null) {
                                    throw t;
                                }
                            } catch (RuntimeException e) {
                                throw e;
                            } catch (Error e) {
                                throw e;
                            } catch (Throwable t) {
                                assert false : t;
                            }
                        }
                    } finally {
                        // Cleary possible interrupted state.
                        Thread.interrupted();
                        
                        AccessController.doPrivileged(
                                new PrivilegedAction<Object>() {
                            public Object run() {
                                // PERMISSION: java.lang.RuntimePermission setContextClassLoader
                                Thread.currentThread().setContextClassLoader(org);
                                return null;
                            }
                        });
                    }
                }
            };
            
            Task task = new Task(runnable);
            future.set(task);
            executor.execute(task);
        }
    }
    
    private void doDestroy(ComponentReference reference) {
        Future localFuture = threadLocal.get();
        for (AtomicReference<Future> future : futures) {
            Future f = future.getAndSet(null);
            if (f != localFuture) {
                if (f != null) {
                    f.cancel(interrupt);
                }
            }
        }
        if (join) {
            if (localFuture != null) {
                // Prevent a deadlock.
                latch.countDown();
                threadLocal.remove();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
            
        if (localFuture != null) {
            localFuture.cancel(interrupt);
        }
    }
}
