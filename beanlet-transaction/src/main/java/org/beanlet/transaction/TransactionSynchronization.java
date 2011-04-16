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
package org.beanlet.transaction;

/**
 * Allows a beanlet instance to synchronize its state with the transactions
 * performed on it. Beanlets are not required to implement this interface.
 *
 * @see AfterBegin
 * @see BeforeCompletion
 * @see AfterCompletion
 * @author Leon van Zantvoort
 */
public interface TransactionSynchronization {

    /**
     * The {@code afterBegin} method notifies a beanlet instance that a new 
     * transaction has started, and that the subsequent business methods on the 
     * instance will be invoked in the context of the transaction.
     */
    void afterBegin();
    
    /**
     * The {@code beforeCompletion} method notifies a beanlet instance that a 
     * transaction is about to be committed.
     */
    void beforeCompletion();
    
    /**
     * The {@code afterCompletion} method notifies a beanlet instance that 
     * the transaction commit protocol has completed, and tells the instance 
     * whether the transaction has been committed or rolled back.
     * Param {@code committed} is {@code true} if the transaction has been 
     * committed, {@code false} if it has been rolled back.
     */
    void afterCompletion(boolean committed);
}
