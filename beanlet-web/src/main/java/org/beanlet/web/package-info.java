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
 * Adds web specific beanlet scopes.
 * 
 * <p><h3>Beanlet Scopes</h3>
 * The Beanlet web package comes with two object scopes:
 * <ul>
 * <li>{@link org.beanlet.web.Request} beanlets do not provide direct access to 
 * the underlying objects. Instead, clients obtain a stub that delegates 
 * invocations to the underlying instance. In case of request beanlets, 
 * the stub creates a new instance per HTTP request. This instance is always 
 * assigned while performing that particular HTTP request.
 * <li>{@link org.beanlet.web.Session} beanlets do not provide direct access to 
 * the underlying objects. Instead, clients obtain a stub that delegates 
 * invocations to the underlying instance. In case of request beanlets, 
 * the stub creates a new instance per HTTP session. This instance is always 
 * assigned while performing a request for that particular HTTP session. 
 * Session beanlet instances only exist while the session, for which they were
 * created, is active. These beanlet instances are destroyed when their HTTP 
 * session is invalidated or expired.<br>
 * Session beanlets are non {@code reentrant} by default, which means that only 
 * one thread can invoke a method of the beanlet instance at the same time. 
 * This feature is provided by the stub, which controls all access to the 
 * underlying instance. Session beanlets can also be configured to be reentrant.
 * </ul>
 * </p>
 * 
 * <p><h3>Static versus Non-Static</h3>
 * Beanlets are created in either a static or a non-static context. Static, in
 * this context, means that beanlets are created independently from the 
 * current state of the container or application. Static beanlets are created
 * at the container's discretion and therefore do not allow applications to 
 * pass any state to this component while it is being created. Non-static 
 * beanlets are only created upon application request. Applications can pass
 * objects - or state if you will - by using wiring {@code BY_INFO} dependency 
 * injection. Use the {@link org.beanlet.BeanletMetaData} interface to find out
 * whether a beanlet is static, or not.</p>
 * 
 * <p>The static property of a beanlet is derived from the selected beanlet scope,
 * i.e., singleton vanilla beanlets are static, where non-singleton vanilla
 * beanlets are non-static. The following table shows the static property for 
 * all web beanlet scopes.</p>
 * <br>
 * <p><table width="400" border="1">
 * <tr><td></td><th>singleton</th><th>reentrant</th><th><i>static</i></th></tr>
 * <tr><th>Request</th><td>N/A</td><td>N/A</td><td>false</td></tr>
 * <tr><th>Session</th><td>N/A</td><td>true/false</td><td>false</td></tr>
 * </table></p>
 */
package org.beanlet.web;

