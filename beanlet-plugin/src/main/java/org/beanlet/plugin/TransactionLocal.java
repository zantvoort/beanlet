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
package org.beanlet.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import javax.transaction.Transaction;
import org.beanlet.BeanletApplicationException;
import org.jargo.ComponentReference;

/**
 * Provides a transaction local scope for objects.
 *
 * @author Leon van Zantvoort
 */
public class TransactionLocal<T> {
 
    private static class LazyHolder {
        static final Constructor<TransactionLocalDelegate> local;
        static {
            try {
                try {
                    Constructor constructor = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Constructor>() {
                        public Constructor run() throws Exception {
                            String path = "META-INF/services/" + 
                                    TransactionLocal.class.getName();

                            // PERMISSION: java.lang.RuntimePermission getClassLoader
                            ClassLoader loader = Thread.currentThread().
                                    getContextClassLoader();
                            final Enumeration<URL> urls;
                            if (loader == null) {
                                urls = TransactionLocal.class.
                                        getClassLoader().getResources(path);
                            } else {
                                urls = loader.getResources(path);
                            }
                            while (urls.hasMoreElements()) {
                                URL url = urls.nextElement();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                        url.openStream()));
                                try {
                                    String className = null;
                                    while ((className = reader.readLine()) != null) {
                                        final String name = className.trim();
                                        if (!name.startsWith("#") && !name.startsWith(";") &&
                                                !name.startsWith("//")) {
                                            final Class<?> cls;
                                            if (loader == null) {
                                                cls = Class.forName(name);
                                            } else {
                                                cls = Class.forName(name, true, loader);
                                            }
                                            int m = cls.getModifiers();
                                            if (TransactionLocalDelegate.class.isAssignableFrom(cls) &&
                                                    !Modifier.isAbstract(m) &&
                                                    !Modifier.isInterface(m)) {
                                                // PERMISSION: java.lang.RuntimePermission accessDeclaredMembers
                                                Constructor constructor = cls.getDeclaredConstructor();
                                                // PERMISSION: java.lang.reflect.ReflectPermission suppressAccessChecks
                                                if (!Modifier.isPublic(constructor.getModifiers())) {
                                                    constructor.setAccessible(true);
                                                }
                                                return constructor;
                                            } else {
                                                throw new ClassCastException(cls.getName());
                                            }
                                        }
                                    }
                                } finally {
                                    reader.close();
                                }
                            }
                            throw new BeanletApplicationException("No " +
                                    "TransactionLocalDelegate implementation " +
                                    "found.");
                        }
                    });
                    @SuppressWarnings("unchecked")
                    Constructor<TransactionLocalDelegate> tmp = 
                            (Constructor<TransactionLocalDelegate>) constructor;
                    local = tmp;
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }
            } catch (BeanletApplicationException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new BeanletApplicationException(e);
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                throw new BeanletApplicationException(t);
            }
        }
    }

    private final TransactionLocalDelegate<T> delegate;
    
    public TransactionLocal() {
        try {
            try {
                @SuppressWarnings("unchecked")
                TransactionLocalDelegate<T> tmp = (TransactionLocalDelegate<T>)
                        LazyHolder.local.newInstance();
                delegate = tmp;
            } catch (ExceptionInInitializerError e) {
                try {
                    throw e.getException();
                } catch (Throwable t) {
                    throw new BeanletApplicationException(t);
                }
            }
        } catch (BeanletApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new BeanletApplicationException(e);
        }
    }

    /**
     * Subclasses can override this method to return a value if no value is
     * currently set.
     */
    protected T initialValue() {
        return null;
    }

    /**
     * Returns transaction that is associated with the calling thread, ]
     * {@code null} otherwise. The returned transaction is not necessarily
     * active.
     * 
     * @return the transaction associated with the calling thread.
     */
    public final Transaction getTransaction() {
        return delegate.getTransaction();
    }
    
//    /**
//     * Returns {@code true} if there is a transaction associated with the 
//     * calling thread. This does not automatically mean that this transaction
//     * is also active.
//     */
//    public final boolean isInTransaction() {
//        return getTransaction() != null;
//    }
    
    /**
     * Sets the specified {@code value}. This value can be retrieved by 
     * {@code this.get()} only in the context of the transaction that is
     * associated with the calling thread.
     *
     * @param value value to be assigned to scoped transaction, or {@code null}
     * to remove current value.
     * @throws IllegalStateException if not in transactional scope.
     */
    public final void set(T value) throws IllegalStateException {
        delegate.set(value);
    }
     
    /**
     * Returns the value that has been set on this {@code TransactionLocal} 
     * instance for the transaction that is associated with the calling thread.
     *
     * @throws IllegalStateException if not in transactional scope.
     */
    public final T get() throws IllegalStateException {
        T value = delegate.get();
        return value == null ? initialValue() : value;
    }
    
    /**
     * Removes the value that is bound to the transaction associated with the
     * calling thread. This method is equal to {@code set(null)}.
     *
     * @throws IllegalStateException if not in transactional scope.
     */
    public final void remove() throws IllegalStateException {
        delegate.remove();
    }
    
    /**
     * Callback interface is invoked when a user transaction is started by the
     * specified {@code reference}.
     */
    public final void onUserTransaction(ComponentReference<?> reference,
            Runnable callback) {
        delegate.onUserTransaction(reference.weakReference(), callback);
    }
}
