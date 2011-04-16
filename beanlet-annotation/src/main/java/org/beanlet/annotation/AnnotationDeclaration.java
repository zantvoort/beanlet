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
import java.util.List;

/**
 * 
 * @author Leon van Zantvoort
 */
public interface AnnotationDeclaration<T extends Annotation> {
    
    /**
     * Returns the annotation type represented by this annotation map.
     */
    Class<T> annotationType();
    
    /**
     * Returns a list of all element annotations contained by this annotation 
     * map. 
     */
    List<ElementAnnotation<Element, T>> getElements();
    
    /**
     * Returns a list of all element annotations contained by this annotation 
     * map. 
     */
    List<ElementAnnotation<Element, T>> getElements(Class<?> cls);
    
    /**
     * Returns all element annotations that match the specified 
     * {@code elementClass}.
     */
    <E extends Element> List<ElementAnnotation<E, T>> getTypedElements(
            Class<E> elementClass);

    /**
     * Returns all element annotations that match the specified 
     * {@code elementClass}.
     */
    <E extends Element> List<ElementAnnotation<E, T>> getTypedElements(
            Class<E> elementClass, Class<?> cls);
    
    /**
     * Returns the annotation for the specified {@code element}, or {@code null}
     * if annotation is not present.
     */
    T getAnnotation(Element element);
    
    /**
     * Returns the annotation for the specified {@code element}, or {@code null}
     * if annotation is not declared.
     */
    T getDeclaredAnnotation(Element element);
    
    /**
     * Returns {@code true} if an annotation for the specified type is present 
     * on this element, else {@code false}.
     */
    boolean isAnnotationPresent(Element element);
}
