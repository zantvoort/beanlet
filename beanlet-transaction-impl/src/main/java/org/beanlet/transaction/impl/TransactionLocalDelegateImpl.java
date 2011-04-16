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
package org.beanlet.transaction.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.transaction.Transaction;
import org.beanlet.BeanletStateException;
import org.beanlet.common.BeanletStack;
import org.beanlet.plugin.TransactionLocalDelegate;
import org.jargo.ComponentReference;

/**
 *
 * @author Leon van Zantvoort
 */
public final class TransactionLocalDelegateImpl<T> implements 
        TransactionLocalDelegate<T> {
    
    private static final Logger logger = Logger.getLogger(TransactionLocalDelegateImpl.class.getName());
    private static final Map<ComponentReference, List<Runnable>> callbackMap = 
            new HashMap<ComponentReference, List<Runnable>>();
    private static final ThreadLocal<BeanletStack<Transaction>> local;
    static {
        local = new ThreadLocal<BeanletStack<Transaction>>() {
            protected BeanletStack<Transaction> initialValue() {
                return new BeanletStack<Transaction>();
            }
        };
    } 
           
    // PENDING: replace WeakHashMap by a WeakIdentityHashMap.
    private final WeakHashMap<Transaction, T> map;
    
    public TransactionLocalDelegateImpl() {
        map = new WeakHashMap<Transaction, T>();
    }

    static void userTransaction(ComponentReference<?> reference) {
        logger.finest("User transaction started for reference: " + reference + 
                ".");
        final List<Runnable> list;
        synchronized (TransactionLocalDelegateImpl.class) {
            list = callbackMap.get(reference);
        }
        if (list != null) {
            for (Runnable r : list) {
                r.run();
            }
        }
    }
    
    // Package private visibility.
    static void begin(Transaction transaction) {
        assert transaction != null;
        local.get().push(transaction);
    }
    
    // Package private visibility.
    static void commit(Transaction transaction) {
        Transaction tx = local.get().poll();
        assert tx == transaction : tx;
    }
    
    private void checkInTransaction() {
        if (getTransaction() == null) {
            throw new IllegalStateException("Not in transactional scope.");
        }
    }
    
    /**
     * Returns transaction that is associated with the calling thread, ]
     * {@code null} otherwise. The returned transaction is not necessarily
     * active.
     * 
     * @return the transaction associated with the calling thread.
     */
    public Transaction getTransaction() {
        return local.get().peek();
    }
    
    /**
     * Sets the specified {@code value}. This value can be retrieved by 
     * {@code this.get()} only in the context of the transaction that is
     * associated with the calling thread.
     *
     * @param value value to be assigned to scoped transaction, or {@code null}
     * to remove current value.
     * @throws IllegalStateException if not in transactional scope.
     */
    public void set(T value) throws IllegalStateException {
        checkInTransaction();
        map.put(local.get().peek(), value);
    }

    /**
     * Returns the value that has been set on this {@code TransactionLocal} 
     * instance for the transaction that is associated with the calling thread.
     *
     * @throws IllegalStateException if not in transactional scope.
     */
    public T get() throws IllegalStateException {
        checkInTransaction();
        return map.get(local.get().peek());
    }
    /**
     * Removes the value that is bound to the transaction associated with the
     * calling thread. This method is equal to {@code set(null)}.
     *
     * @throws IllegalStateException if not in transactional scope.
     */
    public void remove() throws BeanletStateException {
        checkInTransaction();
        map.remove(local.get().peek());
    }

    /**
     * Callback interface is invoked when a user transaction is started by the
     * specified {@code reference}.
     */
    public void onUserTransaction(
            final ComponentReference<?> reference, Runnable callback) {
        synchronized (TransactionLocalDelegateImpl.class) {
            List<Runnable> list = callbackMap.get(reference);
            if (list == null) {
                list = new ArrayList<Runnable>();
                callbackMap.put(reference, list);
                reference.addDestroyHook(new Runnable() {
                    public void run() {
                        synchronized (TransactionLocalDelegateImpl.class) {
                            callbackMap.remove(reference);
                        }
                    }
                });
            }
            list.add(callback);
        }
    }
}
