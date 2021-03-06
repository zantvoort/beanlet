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

import javax.transaction.UserTransaction;
import org.beanlet.BeanletStateException;

/**
 * Provides access to the transaction runtime context of the beanlet instance.
 *
 * @author Leon van Zantvoort
 */
public interface TransactionContext {
    
    /**
     * Mark the current transaction for rollback.
     *
     * @throws BeanletStateException if the current thread is not associated 
     * with a transaction.
     */
    void setRollbackOnly() throws BeanletStateException;
    
    /**
     * Test if the transaction has been marked for rollback only.
     * @return <code>true</code> if the transaction has been marked for rollback only.
     * @throws BeanletStateException if the current thread is not associated
     * with a transaction.
     */
    boolean getRollbackOnly() throws BeanletStateException;
    
    /**
     * Obtain the transaction demarcation interface.
     * @throws BeanletStateException if no user transaction has been installed.
     */
    UserTransaction getUserTransaction() throws BeanletStateException;
}
