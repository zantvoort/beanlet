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
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

/**
 *
 * @author Leon van Zantvoort
 */
public final class ConstructorParameterElement extends ParameterElement {

    public static ConstructorParameterElement instance(Constructor<?> constructor,
            int parameter) {
        return new ConstructorParameterElement(constructor, parameter);
    }
    
    private final Constructor<?> constructor;
    
    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    private ConstructorParameterElement(Constructor<?> constructor, 
            int parameter) {
        super(parameter);
        if (constructor == null) {
            throw new NullPointerException();
        }
        if (parameter >= constructor.getParameterTypes().length) {
            throw new IllegalArgumentException("Constructor " + constructor + 
                    " does not take " + (parameter + 1) + " argument(s).");
        }
        this.constructor = constructor;
    }
    
    public boolean isElementOf(Class<?> cls) {
        return constructor.getDeclaringClass().isAssignableFrom(cls);
    }
    
    public boolean isElementOfSubclass(Class<?> cls) {
        return cls.isAssignableFrom(constructor.getDeclaringClass());
    }
    
    public boolean isOverridden(Class<?> cls) {
        return ConstructorElement.isOverridden(getConstructor(), cls);
    }

    public boolean isHidden(Class<?> cls) {
        return ConstructorElement.isHidden(getConstructor(), cls);
    }
    
    public <T extends Annotation> ElementAnnotation<ConstructorParameterElement, T> 
            getElementAnnotation(T annotation) {
        return new ElementAnnotationImpl<T>(this, annotation);
    }
    
    public Constructor<?> getConstructor() {
        return constructor;
    }
    
    public boolean isMember() {
        return true;
    }
    
    public Member getMember() {
        return getConstructor();
    }

    @Override
    public int hashCode() {
        // PENDING: add parameter to hashcode.
        return getConstructor().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstructorParameterElement) {
            return getConstructor().equals(((ConstructorParameterElement) obj).getConstructor()) &&
                    getParameter() == ((ConstructorParameterElement) obj).getParameter();
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + getConstructor() + ", " + getParameter() + "]";
    }
    
    private static class ElementAnnotationImpl<T extends Annotation> 
            implements ElementAnnotation<ConstructorParameterElement, T> {
        
        private final ConstructorParameterElement element;
        private final T annotation;

        private ElementAnnotationImpl(ConstructorParameterElement element, T annotation) {
            if (annotation == null) {
                throw new NullPointerException();
            }
            this.element = element;
            this.annotation = annotation;
        }

        public ConstructorParameterElement getElement() {
            return element;
        }
        
        public T getAnnotation() {
            return annotation;
        }

        public String toString() {
            return "{element=" + getElement() + ", annotation=" + 
                    getAnnotation() + "}";
        }
    }
}
