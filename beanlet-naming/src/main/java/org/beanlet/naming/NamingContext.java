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
package org.beanlet.naming;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used to enable JNDI dependency injection.</p>
 *
 * <p>A member can be injected with an object from the JNDI tree if a 
 * {@code NamingContext} is available for the specified member. Such a 
 * {@code NamingContext} can be defined at package-, class- or at member-level. 
 * There are two more requirements for members to support JNDI injection:
 * <ul>
 * <li>Members must be marked with the {@code Inject} annotation.
 * <li>Members, for which a {@code NamingContext} is made available at package- 
 * or class-level, must enable wiring {@code BY_NAME} or wiring {@code BY_TYPE} 
 * through the {@code Wiring} annotation. Members that are marked with the
 * {@code NamingContext} support wiring {@code BY_NAME} and {@code BY_TYPE} by 
 * default.
 * </ul></p>
 * 
 * <p>Members marked with this annotation MUST also be annoted with 
 * {@code Inject}, otherwise the beanlet definition fails.</p>
 * 
 * <p><h3>XML Representation</h3>The following xml-fragment shows how to express 
 * this annotation in xml.<br><pre><tt>
 * &lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *           xmlns:jndi="http://beanlet.org/schema/naming"
 *           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *           xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd
 *                            http://beanlet.org/schema/naming http://beanlet.org/schema/naming/beanlet_naming_1_0.xsd"&gt;
 *   &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *     <b>&lt;jndi:naming-context&gt;
 *       &lt;jndi:property name="java.naming.factory.initial" value="com.sun.jndi.cosnaming.CNCtxFactory"/&gt;
 *     &lt;/jndi:naming-context&gt;</b>
 *   &lt;/beanlet&gt;
 * &lt;/beanlets&gt;</tt></pre></p>
 *
 * @see org.beanlet.Inject
 * @see org.beanlet.Wiring
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({PACKAGE, TYPE, CONSTRUCTOR, METHOD, FIELD, PARAMETER})
public @interface NamingContext {

    /**
     * Used to specify properties for the naming context.
     */
    NamingProperty[] value() default {};
}
