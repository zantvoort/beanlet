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
package org.beanlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>Indicates what action is to be taken in case an exception is encountered 
 * on beanlet invocation.</p>
 *
 * <p>Beanlet instances are returned to the pool after invocation automatically, 
 * unless an exception is raised that matches the exception type
 * specified by this annotation. In this case, the retention policy is executed
 * as specified.</p>
 * 
 * <p><h3>XML Representation</h3>The following xml-fragment shows how to express this annotation in xml.<br><pre><tt>&lt;beanlets xmlns="http://beanlet.org/schema/beanlet"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd"&gt;
 *  &lt;beanlet name="foo" type="com.acme.Foo"&gt;
 *    <b>&lt;retention value="INVALIDATE"&gt;
 *      &lt;class type="java.lang.RuntimeException"/&gt;
 *      &lt;class type="java.lang.Error"/&gt;
 *    &lt;/retention&gt;</b>
 *  &lt;/beanlet&gt;
 *&lt;/beanlets&gt;</tt></pre></p>
 *
 * @see RetentionPolicy
 * @author Leon van Zantvoort
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Retention {

    Class<? extends Throwable>[] type() default {RuntimeException.class, 
            Error.class};
    
    RetentionPolicy value() default RetentionPolicy.INVALIDATE;
}
