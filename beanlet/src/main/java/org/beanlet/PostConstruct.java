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
 * <p>The {@code PostConstruct} annotation is used on a method that needs to be 
 * executed after dependency injection is done to perform any initialization. 
 * Such methods are so-called post construct methods. This annotation is
 * also supported for interceptor methods, further referred to as
 * post-construct lifecycle methods.</p>
 *
 * <p>{@code PostConstruct} methods are invoked by the container before
 * beanlet instances are put into service. The post-construct method is 
 * called even if the class does not request any resources to be injected. 
 * The beanlet instance is not put into service if an exception is thrown by 
 * this method.</p>
 *
 * <p><h3>Method Constraints</h3>
 * Only one method can be marked with this annotation. The method on which the 
 * {@code PostConstruct} annotation is applied MUST fulfill all of the following 
 * criteria:
 * <ul>
 * <li>The method MUST NOT have any parameters except in the case of 
 * interceptors in which case it MUST take an {@code InterceptorContext} object.
 * <li>The method MAY return any type.
 * <li>The method MAY throw a checked exception.
 * <li>The method on which {@code PostConstruct} is applied MAY be 
 * {@code public}, {@code protected}, package private or {@code private}. 
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
public @interface PostConstruct {
    
}
