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
 * The {@code PreDestroy} annotation is used on methods as a callback 
 * notification to signal that the beanlet instance is in the process of being 
 * removed by the container. Such methods are so-called pre-destroy methods
 * This annotation is also supported for interceptor methods, further 
 * referred to as pre-destroy lifecycle methods.</p>
 *
 * <p>Pre-destroy methods are typically used to release resources that 
 * the beanlet has been holding.</p>
 *
 * <p><h3>Method Constraints</h3>
 * The method on which the 
 * {@code PreDestroy} annotation is applied MUST fulfill all of the following 
 * criteria:
 * <ul>
 * <li>The method MUST NOT have any parameters except in the case of 
 * interceptors in which case it MUST take an {@code InterceptorContext} object.
 * <li>The method MAY return any type.
 * <li>The method MAY throw a checked exception.
 * <li>The method on which {@code PreDestroy} is applied MAY be {@code public}, 
 * {@code protected}, package private or {@code private}.
 * <li>The method MUST NOT be {@code static}.
 * <li>The method MAY be {@code final}.
 * </ul>
 *
 * {@beanlet.annotation}
 *
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PreDestroy {
    
}
