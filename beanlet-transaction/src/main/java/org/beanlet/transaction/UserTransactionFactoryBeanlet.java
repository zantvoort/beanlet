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
import org.beanlet.FactoryBeanlet;
import org.beanlet.Inject;

/**
 * <p>Factory beanlet for a {@code UserTransaction}.</p>
 * 
 * <p>The following example demonstrates how to create a factory using a
 * transaction manager from the JNDI tree:
 * 
 * <pre>
 * &lt;beanlet type="org.beanlet.transaction.UserTransactionFactoryBeanlet"&gt;
 *   &lt;wiring value="BY_NAME"/&gt;
 *   &lt;inject constructor="true" name="java:comp/UserTransaction"/&gt;
 * &lt;/beanlet&gt;
 * </pre>
 * </p>
 * 
 * @author Leon van Zantvoort
 */
public final class UserTransactionFactoryBeanlet implements 
        FactoryBeanlet<UserTransaction> {

    private final UserTransaction userTransaction;
    
    /**
     * Use constructor depenency injection to create a new 
     * {@code UserTransactionFactoryBeanlet} instance.
     * 
     * @param userTransaction user transaction instance to be returned 
     * by this factory's {@code getObject} method.
     */
    @Inject
    public UserTransactionFactoryBeanlet(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }

    /**
     * Returns the user transaction instance that was passed to the 
     * constructor of this factory.
     */
    public UserTransaction getObject() {
        return userTransaction;
    }
}
