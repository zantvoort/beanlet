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

import javax.transaction.Transaction;
import org.jargo.ComponentReference;

/**
 * @author Leon van Zantvoort
 */
public interface TransactionLocalDelegate<T> {
 
    /**
     * Returns transaction that is associated with the calling thread, ]
     * {@code null} otherwise. The returned transaction is not necessarily
     * active.
     * 
     * @return the transaction associated with the calling thread.
     */
    Transaction getTransaction();
    
    /**
     * Sets the specified {@code value}. This value can be retrieved by 
     * {@code this.get()} only in the context of the transaction that is
     * associated with the calling thread.
     *
     * @param value value to be assigned to scoped transaction, or {@code null}
     * to remove current value.
     * @throws IllegalStateException if not in transactional scope.
     */
    void set(T value) throws IllegalStateException;
     
    /**
     * Returns the value that has been set on this {@code TransactionLocal} 
     * instance for the transaction that is associated with the calling thread.
     *
     * @throws IllegalStateException if not in transactional scope.
     */
    T get() throws IllegalStateException;
    
    /**
     * Removes the value that is bound to the transaction associated with the
     * calling thread. This method is equal to {@code set(null)}.
     *
     * @throws IllegalStateException if not in transactional scope.
     */
    void remove() throws IllegalStateException;

    /**
     * Callback interface is invoked when a user transaction is started by the
     * specified {@code reference}.
     */
    void onUserTransaction(ComponentReference<?> reference,
            Runnable callback);
}
