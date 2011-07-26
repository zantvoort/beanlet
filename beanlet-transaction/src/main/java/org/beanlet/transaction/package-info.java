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

/**
 * <p>Adds support for JTA transaction.</p>
 * 
 * <p>Beanlet's transaction support requires a transaction manager to be
 * available within the beanlet runtime. Being available to the runtime means
 * that a beanlet must be registered for which the beanlet type matches the 
 * {@code javax.transaction.TransactionManager} interface. This allows the 
 * container to lookup the transaction manager by type. This approach of looking 
 * up a transaction manager also means that only one beanlet matching the 
 * {@code javax.transaction.TransactionManager} interface may exist to avoid
 * ambiguity.</p>
 * 
 * <p>The following beanlet xml-file fragment shows how to integrate the JOTM
 * transaction manager:
 * <pre>
 * &lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *           xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *   &lt;beanlet name="org.objectweb.jotm.Jotm" 
 *            type="org.objectweb.jotm.Jotm"&gt;
 *     &lt;inject constructor="true" index="0" value="true"/&gt;
 *     &lt;inject constructor="true" index="1" value="false"/&gt;
 *     &lt;pre-destroy method="stop"/&gt;
 *   &lt;/beanlet&gt;
 *   &lt;beanlet type="javax.transaction.TransactionManager"
 *            factory="org.objectweb.jotm.Jotm" 
 *            factory-method="getTransactionManager"/&gt;
 *   &lt;beanlet type="javax.transaction.UserTransaction" 
 *            factory="org.objectweb.jotm.Jotm" 
 *            factory-method="getUserTransaction"/&gt;
 * &lt;/beanlets&gt;
 * </pre></p>
 * 
 * <p>The JBoss transaction manager can also be installed, as is shown by the
 * following snippet:
 * <pre>
 * &lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *           xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *   &lt;beanlet type="javax.transaction.TransactionManager"&gt;
 *     &lt;annotations type="com.arjuna.ats.jta.TransactionManager"&gt;
 *       &lt;static-factory&gt;
 *         &lt;method name="transactionManager"&gt;
 *           &lt;parameters/&gt;
 *         &lt;/method&gt;
 *       &lt;/static-factory&gt;
 *     &lt;/annotations&gt;
 *   &lt;/beanlet&gt;
 *   &lt;beanlet type="javax.transaction.UserTransaction"&gt;
 *     &lt;annotations type="com.arjuna.ats.jta.UserTransaction"&gt;
 *       &lt;static-factory&gt;
 *         &lt;method name="userTransaction"&gt;
 *           &lt;parameters/&gt;
 *         &lt;/method&gt;
 *       &lt;/static-factory&gt;
 *     &lt;/annotations&gt;
 *   &lt;/beanlet&gt;
 * &lt;/beanlets&gt;
 * </pre></p>
 * 
 * <p>Alternatively, beanlet can also integrate with a transaction manager
 * that is available from JNDI. This is particularly useful if beanlet runs 
 * embedded within an EJB or Servlet container.</p>
 * 
 * <p>The following beanlet xml-file fragment shows how to integrate a 
 * transaction manager using the JNDI tree:
 * <pre>
 * &lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *           xmlns:jndi="http://beanlet.org/schema/naming"
 *           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *           xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd
 *                               http://beanlet.org/schema/naming http://beanlet.org/schema/naming/beanlet_naming_1_0.xsd"&gt;
 *   &lt;annotations&gt;
 *     &lt;jndi:naming-context/&gt;
 *     &lt;wiring value="BY_NAME"/&gt;
 *   &lt;/annotations&gt;
 *   &lt;beanlet type="org.beanlet.transaction.TransactionManagerFactoryBeanlet"&gt;
 *     &lt;inject constructor="true" name="java:comp/TransactionManager"/&gt;
 *   &lt;/beanlet&gt;
 *   &lt;beanlet type="org.beanlet.transaction.UserTransactionFactoryBeanlet"&gt;
 *     &lt;inject constructor="true" name="java:comp/UserTransaction"/&gt;
 *   &lt;/beanlet&gt;
 * &lt;/beanlets&gt;
 * </pre></p>
 */
package org.beanlet.transaction;

