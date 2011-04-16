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
 * Defines all possible transaction types.
 *
 * @author Leon van Zantvoort
 */
public enum TransactionAttributeType {
    
    /**
     * Business methods annotated with this attribute must not be called within
     * the scope of an active transaction. An unchecked exception is thrown if
     * the method is called within the scope of an active transaction.
     * The method will be invoked in an unspecified transaction context, similar 
     * to {@code NEVER_SUPPORTED}.
     */
    NEVER,
    
    /**
     * This attribute indicates that a business method should never be executed
     * in the context of a transaction by the container. If a transaction is
     * already active for the running thread, the transaction is suspended
     * during the lifetime of the underlying method. The business method runs
     * in an unspecified transaction context.
     */
    NOT_SUPPORTED,
    
    /**
     * The mandatory attribute indicates that the business method must be called
     * in the context of an active transaction. If not, an unchecked exception
     * is thrown.
     */
    MANDATORY,
    
    /**
     * This attribute indicates that the container should defer the creation of
     * a transaction to the caller. The container may allow the invocation of
     * the business method within the transaction context of a caller. The 
     * method may also be called with no transaction context associated with the 
     * caller.
     */
    SUPPORTS,
    
    /**
     * This attribute indicates that the business method must be invoked in the
     * context of a transaction. If there is a transaction associated with the
     * caller, that transaction is associated with the method invocation. If the
     * caller does not have an associated transaction, the container starts a
     * new transaction prior to invoking the business method, and subsequently
     * terminates the transaction when the business method has returned.
     */
    REQUIRED,
            
    /**
     * This attribute indicates that the business method will always be invoked
     * in the context of a new transaction. If the caller invokes the method 
     * within a transaction, the container will suspend that transaction and 
     * start a new transaction before calling the actual business method. When
     * the business method completes, the container will terminate the newly
     * created transaction and resume the caller's transaction.
     */
    REQUIRES_NEW;
}
