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
 * <p>Applied to static methods or static fields to denote a factory.</p>
 *
 * <p><h3>Constructor Constraints</h3>
 * The constructor on which the {@code StaticFactory} annotation is applied MUST fulfill all
 * of the following criteria:
 * <ul>
 * <li>The constructor MUST NOT have any parameters.
 * <li>The constructor MAY throw a checked exception.
 * <li>The constructor on which {@code StaticFactory} is applied MAY be {@code public},
 * {@code protected}, package private or {@code private}.
 * </ul>
 * </p>
 * <p><h3>Method Constraints</h3>
 * The method on which the {@code StaticFactory} annotation is applied MUST fulfill all
 * of the following criteria:
 * <ul>
 * <li>The method MUST NOT have any parameters.
 * <li>The return type of the method MUST NOT be {@code void}.
 * <li>The method MAY throw a checked exception.
 * <li>The method on which {@code StaticFactory} is applied MAY be {@code public},
 * {@code protected}, package private or {@code private}. 
 * <li>The method MUST be {@code static}.
 * <li>The method MAY be {@code final}. 
 * </ul>
 * </p>
 * <p><h3>Field Constraints</h3>
 * The field on which the {@code Factory} annotation is applied MUST fulfill all
 * of the following criteria:
 * <ul>
  * <li>The field on which {@code StaticFactory} is applied MAY be {@code public},
 * {@code protected}, package private or {@code private}.
 * <li>The field MUST be {@code static}.
 * <li>The field MAY be {@code final}.
 * </ul>
 * </p>
 *
 * {@beanlet.annotation}
 *
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
public @interface StaticFactory {

}
