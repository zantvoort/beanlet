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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Stateful beanlet maintain conversational state between calls on the same
 * object. Stateful beanlets do not provide direct access to the underlying 
 * objects. Instead, clients obtain a stub that delegates invocations to the 
 * underlying instance. In case of stateful beanlets, the stub always delegates 
 * invocation to the same instance.<br>
 * A new beanlet instance is created for each beanlet that is explicitely 
 * requested throuh the APIs, or implicitely through dependency injection.<br>
 * Stateful beanlets are non {@code reentrant} by default, which means that only 
 * one thread can invoke a method of the beanlet instance at the same time. 
 * This feature is provided by the stub, which controls all access to the 
 * underlying instance. Stateful beanlets can also be configured to be reentrant.</p>
 *
 * {@beanlet.annotation}
 *
 * @see Stateless
 * @see ScopeAnnotation
 * @author Leon van Zantvoort
 */
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Stateful {

    /**
     * Specifies whether the beanlet instances should be reentrant or not. 
     * Instances of reentrant beanlets can be invoked by multiple threads at
     * the same time.
     */
    boolean reentrant() default false;
}
