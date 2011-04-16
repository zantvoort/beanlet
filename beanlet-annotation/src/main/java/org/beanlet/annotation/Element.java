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
package org.beanlet.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;

/**
 *
 * @author Leon van Zantvoort
 */
public interface Element {
   
    /**
     * Returns {@code true} if this element is specified by the given 
     * class, or any base classes.
     */
    boolean isElementOf(Class<?> cls);
    
    /**
     * Returns {@code true} if this element is specified by the given 
     * class, or any sub classes.
     */
    boolean isElementOfSubclass(Class<?> cls);
    
    /**
     * Return {@code true} if specified element is overridden by an element of 
     * the specified class.
     */
    boolean isOverridden(Class<?> cls);
    
    /**
     * Returns {@code true} if the specified element is hidden by an element of
     * the specified class.
     */
    boolean isHidden(Class<?> cls);
    
    /**
     * Returns {@code true} if this element is (part of) a member (
     * field, method, constructor).
     */
    boolean isMember();
    
    /**
     * Returns the member of this element, or {@code null} if {@code isMember} 
     * returns {@code false}.
     */
    Member getMember();
    
    /**
     * Constructs a new {@code ElementAnnotation} for the specified 
     * {@code annotation}.
     */
    <T extends Annotation> ElementAnnotation<? extends Element, T> 
            getElementAnnotation(T annotation);

    /**
     * Returns the {@code ElementType} of this element.
     */
    ElementType getElementType();
}
