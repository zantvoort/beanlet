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
import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 *
 * @author Leon van Zantvoort
 */
public final class FieldElement implements Element {
    
    public static FieldElement instance(Field field) {
        return new FieldElement(field);
    }
    
    private final Field field;
    
    /**
     * @throws NullPointerException
     */
    private FieldElement(Field field) {
        if (field == null) {
            throw new NullPointerException();
        }
        this.field = field;
    }
    
    public boolean isElementOf(Class<?> cls) {
        return getField().getDeclaringClass().isAssignableFrom(cls);
    }
    
    public boolean isElementOfSubclass(Class<?> cls) {
        return cls.isAssignableFrom(getField().getDeclaringClass());
    }
    
    public boolean isOverridden(Class<?> cls) {
        return false;
    }
    
    /**
     * Within a class, a field that has the same name as a field in the
     * superclass hides the superclass's field, even if their types are
     * different.
     * (http://java.sun.com/docs/books/tutorial/java/IandI/hidevariables.html)
     */
    public boolean isHidden(Class<?> cls) {
        Class<?> type = cls;
        do {
            if (getField().getDeclaringClass().equals(type)) {
                break;
            }
            try {
                type.getDeclaredField(getField().getName());
                return true;
            } catch (NoSuchFieldException e) {
                continue;
            }
        } while ((type = type.getSuperclass()) != null);
        return false;
    }
    
    public <T extends Annotation> ElementAnnotation<FieldElement, T>
            getElementAnnotation(T annotation) {
        return new ElementAnnotationImpl<T>(this, annotation);
    }
    
    public ElementType getElementType() {
        return ElementType.FIELD;
    }
    
    public Field getField() {
        return field;
    }

    public boolean isMember() {
        return true;
    }
    
    public Member getMember() {
        return getField();
    }
    
    @Override
    public int hashCode() {
        return getField().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldElement) {
            return getField().equals(((FieldElement) obj).getField());
        }
        return false;
    }
    
    @Override
    public String toString() {
        return getField().toString();
    }
    
    private static class ElementAnnotationImpl<T extends Annotation>
            implements ElementAnnotation<FieldElement, T> {
        
        private final FieldElement element;
        private final T annotation;
        
        private ElementAnnotationImpl(FieldElement element, T annotation) {
            if (annotation == null) {
                throw new NullPointerException();
            }
            this.element = element;
            this.annotation = annotation;
        }
        
        public FieldElement getElement() {
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
