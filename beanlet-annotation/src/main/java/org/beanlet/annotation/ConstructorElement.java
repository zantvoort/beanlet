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
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

/**
 *
 * @author Leon van Zantvoort
 */
public final class ConstructorElement implements Element {
    
    public static ConstructorElement instance(Constructor<?> constructor) {
        return new ConstructorElement(constructor);
    }
    
    private final Constructor<?> constructor;
    
    /**
     * @throws NullPointerException
     */
    private ConstructorElement(Constructor<?> constructor) {
        if (constructor == null) {
            throw new NullPointerException();
        }
        this.constructor = constructor;
    }

    public boolean isElementOf(Class<?> cls) {
        return constructor.getDeclaringClass().isAssignableFrom(cls);
    }
    
    public boolean isOverridden(Class<?> cls) {
        return isOverridden(getConstructor(), cls);
    }

    public boolean isHidden(Class<?> cls) {
        return isHidden(getConstructor(), cls);
    }
    
    public boolean isElementOfSubclass(Class<?> cls) {
        return cls.isAssignableFrom(constructor.getDeclaringClass());
    }
    
    public <T extends Annotation> ElementAnnotation<ConstructorElement, T> 
            getElementAnnotation(T annotation) {
        return new ElementAnnotationImpl<T>(this, annotation);
    }
    
    public ElementType getElementType() {
        return ElementType.CONSTRUCTOR;
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
        return getConstructor().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstructorElement) {
            return getConstructor().equals(((ConstructorElement) obj).getConstructor());
        }
        return false;
    }

    @Override
    public String toString() {
        return getConstructor().toString();
    }

    /**
     * Return <tt>true</tt> if specified constructor is overridden by a 
     * constructor of the specified class.
     */
    static boolean isOverridden(Constructor<?> constructor, Class<?> cls) {
        if (cls.isAssignableFrom(constructor.getDeclaringClass())) {
            return false;
        }
        return constructor.getDeclaringClass().isAssignableFrom(cls);
    }
    
    static boolean isHidden(Constructor<?> constructor, Class<?> cls) {
        return isOverridden(constructor, cls);
    }
    
    private final class ElementAnnotationImpl<T extends Annotation> 
            implements ElementAnnotation<ConstructorElement, T> {
        
        private final ConstructorElement element;
        private final T annotation;

        private ElementAnnotationImpl(ConstructorElement element, T annotation) {
            if (annotation == null) {
                throw new NullPointerException();
            }
            this.element = element;
            this.annotation = annotation;
        }

        public ConstructorElement getElement() {
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
