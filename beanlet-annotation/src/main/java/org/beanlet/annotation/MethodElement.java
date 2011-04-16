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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *
 * @author Leon van Zantvoort
 */
public final class MethodElement implements Element {
    
    public static MethodElement instance(Method method) {
        return new MethodElement(method);
    }
    
    private final Method method;
    
    /**
     * @throws NullPointerException
     */
    private MethodElement(Method method) {
        if (method == null) {
            throw new NullPointerException();
        }
        this.method = method;
    }
    
    public boolean isElementOf(Class<?> cls) {
        return getMethod().getDeclaringClass().isAssignableFrom(cls);
    }
    
    public boolean isElementOfSubclass(Class<?> cls) {
        return cls.isAssignableFrom(getMethod().getDeclaringClass());
    }

    public boolean isOverridden(Class<?> cls) {
        return isOverridden(getMethod(), cls);
    }

    /**
     * If a subclass defines a class method with the same signature as a class 
     * method in the superclass, the method in the subclass hides the one in the 
     * superclass.
     * (http://java.sun.com/docs/books/tutorial/java/IandI/override.html)
     */
    public boolean isHidden(Class<?> cls) {
        return isHidden(getMethod(), cls);
    }

    public <T extends Annotation> ElementAnnotation<MethodElement, T> 
            getElementAnnotation(T annotation) {
        return new ElementAnnotationImpl<T>(this, annotation);
    }
    
    public ElementType getElementType() {
        return ElementType.METHOD;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public boolean isMember() {
        return true;
    }
    
    public Member getMember() {
        return getMethod();
    }
    
    @Override
    public int hashCode() {
        return getMethod().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodElement) {
            return getMethod().equals(((MethodElement) obj).getMethod());
        }
        return false;
    }
    
    @Override
    public String toString() {
        return getMethod().toString();
    }
    
    /**
     * Return <tt>true</tt> if specified method is overridden by a method of the 
     * specified class.
     */
    static boolean isOverridden(Method method, Class<?> cls) {
        if (cls.isAssignableFrom(method.getDeclaringClass())) {
            return false;
        }
        if (Modifier.isPrivate(method.getModifiers())) {
            return false;
        }
        
        boolean pkg = !Modifier.isProtected(method.getModifiers()) &&
                    !Modifier.isPublic(method.getModifiers());
        Class<?> type = cls;
        do {
            if (type.equals(method.getDeclaringClass())) {
                return false;
            }
            if (!pkg || type.getPackage().equals(method.getDeclaringClass().
                    getPackage())) {
                try {
                    type.getDeclaredMethod(method.getName(), 
                            method.getParameterTypes());
                    return true;
                } catch (NoSuchMethodException e) {
                }
            }
        } while ((type = type.getSuperclass()) != null);
        return false;
    }
    
    static boolean isHidden(Method method, Class<?> cls) {
        Class<?> type = cls;
        do {
            if (method.getDeclaringClass().equals(type)) {
                break;
            }
            try {
                type.getDeclaredMethod(method.getName(), method.getParameterTypes());
                return true;
            } catch (NoSuchMethodException e) {
                continue;
            }
        } while ((type = type.getSuperclass()) != null);
        return false;
    }
    
    private static class ElementAnnotationImpl<T extends Annotation> 
            implements ElementAnnotation<MethodElement, T> {
        
        private final MethodElement element;
        private final T annotation;

        private ElementAnnotationImpl(MethodElement element, T annotation) {
            if (annotation == null) {
                throw new NullPointerException();
            }
            this.element = element;
            this.annotation = annotation;
        }

        public MethodElement getElement() {
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
