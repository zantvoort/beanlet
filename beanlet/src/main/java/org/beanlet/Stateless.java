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
 * <p>Stateless beanlets do not share any state between subsequent method
 * invocations. Stateless beanlet instances are pooled and methods called on
 * stateless beanlet stubs are delegated to one of the pooled instances. 
 * If multiple invocations are made on the same stub reference, different
 * beanlet instances can be used to perform the call.<br>
 * Beanlet instances are added and removed from the pool at the container's 
 * discretion. Requesting a beanlet stub through the APIs, or dependency 
 * injection does not necessarily result in the creation of a new beanlet 
 * instance. In general, the number of pooled beanlet instances depends on the 
 * number of concurrent requests made to these stubs.</br>
 * Stateless beanlets can be configured to be {@code reentrant} and 
 * {@code singleton}.</p>
 *
 * {@beanlet.annotation}
 *
 * @see Stateful
 * @see ScopeAnnotation
 * @author Leon van Zantvoort
 */
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Stateless {
    
    /**
     * Set to {@code true} to declare this beanlet to be a singleton beanlet.
     * Only one beanlet instance will exist at the same time for singleton 
     * beanlets.
     */
    boolean singleton() default false;
    
    /**
     * Specifies whether the beanlet instances should be reentrant or not. 
     * Instances of reentrant beanlets can be invoked by multiple threads at
     * the same time.
     */
    boolean reentrant() default false;
}
