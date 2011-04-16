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
 * <p>Allows members to be wired with implicitely selected objects.</p>
 *
 * <p>A member can only be wired {@code BY_INFO}, {@code BY_NAME} or 
 * {@code BY_TYPE} if the beanlet's package,
 * class or member is marked with the specified wiring mode. Note that the
 * wiring modes for the member overrides the class wiring modes and logically,
 * the class wiring modes override the package wiring modes.</p>
 *
 * <p>Members marked with this annotation MUST also be annotated with 
 * {@code Inject}, otherwise the beanlet definition fails.</p>
 *
 * {@beanlet.annotation}
 *
 * @see Inject
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, 
        ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Wiring {
    
    /**
     * Specifies the wiring mode to be used for the marked element.
     */
    WiringMode[] value() default {};
}
