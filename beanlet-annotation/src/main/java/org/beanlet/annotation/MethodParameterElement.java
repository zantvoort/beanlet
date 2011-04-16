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
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 *
 * @author Leon van Zantvoort
 */
public final class MethodParameterElement extends ParameterElement {

    public static MethodParameterElement instance(Method method, int parameter) {
        return new MethodParameterElement(method, parameter);
    }
    
    private final Method method;
    
    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    private MethodParameterElement(Method method, int parameter) {
        super(parameter);
        if (method == null) {
            throw new NullPointerException();
        }
        if (parameter >= method.getParameterTypes().length) {
            throw new IllegalArgumentException("Method " + method + 
                    " does not take " + (parameter + 1) + " argument(s).");
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
        return MethodElement.isOverridden(getMethod(), cls);
    }
    
    public boolean isHidden(Class<?> cls) {
        return MethodElement.isHidden(getMethod(), cls);
    }

    public <T extends Annotation> ElementAnnotation<MethodParameterElement, T> 
            getElementAnnotation(T annotation) {
        return new ElementAnnotationImpl<T>(this, annotation);
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
        // PENDING: add parameter to hashcode.
        return getMethod().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodParameterElement) {
            return getMethod().equals(((MethodParameterElement) obj).getMethod()) &&
                    getParameter() == ((MethodParameterElement) obj).getParameter();
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + getMethod() + ", " + getParameter() + "]";
    }

    private static final class ElementAnnotationImpl<T extends Annotation> 
            implements ElementAnnotation<MethodParameterElement, T> {
        
        private final MethodParameterElement element;
        private final T annotation;

        private ElementAnnotationImpl(MethodParameterElement element, T annotation) {
            if (annotation == null) {
                throw new NullPointerException();
            }
            this.element = element;
            this.annotation = annotation;
        }

        public MethodParameterElement getElement() {
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
