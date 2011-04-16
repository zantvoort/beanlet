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
 * <li>Vanilla beanlets are the most straight forward type
 * of beanlets within this specification. Unlike the other core scopes, 
 * vanilla beanlets do not have a stub that controls access to the beanlet 
 * instance. Clients have access to the actual object. In short, 
 * they can invoke methods directly on the beanlet instance.<br>
 * Vanilla beanlets are singleton objects by default. The container guarantees 
 * that only a single instance of this beanlet exists at the same time. This 
 * particular instance is always returned upon request.<br>
 * Vanilla beanlets can also be configured to be non-singleton. In this case, 
 * a new beanlet instance is created for each beanlet that is explicitly 
 * requested throuh the APIs, or implicitly through dependency injection.</p>
 * 
 * {@beanlet.annotation}
 *
 * @author Leon van Zantvoort
 */
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Vanilla {
    
    /**
     * {@code true} to declare this beanlet to be a singleton beanlet.
     * Only one beanlet instance will exist at the same time for singleton 
     * beanlets.
     */
    boolean singleton() default true;
}
