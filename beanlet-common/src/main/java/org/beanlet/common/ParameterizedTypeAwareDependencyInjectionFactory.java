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
package org.beanlet.common;

import org.beanlet.plugin.BeanletConfiguration;
import java.lang.reflect.WildcardType;
import org.beanlet.BeanletTypeIsDuplicateException;
import org.beanlet.BeanletTypeMismatchException;
import org.beanlet.BeanletTypeNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletFactory;
import org.beanlet.BeanletWiringException;
import org.beanlet.Inject;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class ParameterizedTypeAwareDependencyInjectionFactory extends
        AbstractDependencyInjectionFactory<Inject> {
    
    private final String beanletName;
    
    public ParameterizedTypeAwareDependencyInjectionFactory(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.beanletName = configuration.getComponentName();
    }

    public Class<Inject> annotationType() {
        return Inject.class;
    }
    
    public boolean isOptional(ElementAnnotation<? extends Element, Inject> ea) {
        return ea.getAnnotation().optional();
    }
    
    @Override 
    public String getName(Inject inject) {
        return inject.name();
    }
    
    @Override
    public Class<?> getType(Inject inject) {
        return inject.type();
    }
    
    public Class<?> getTypeClass(Type t) {
        if (t instanceof WildcardType) {
            return getTypeClass(((WildcardType) t).getUpperBounds()[0]);
        } else {
            return (Class) t;
        }
    }
    
    public Type getParameterizedType(Element element) {
        return getParameterizedType(element, 0);
    }
    
    public Type getParameterizedType(Element element, int typeArgument) {
        return getParameterizedType(element, typeArgument, null);
    }
    
    public Type getParameterizedType(Element element, Class<?> requiredType) {
        return getParameterizedType(element, 0, requiredType);
    }
    
    /**
     * @param element element to read parameterized type from.
     * @param typeArgument index of type array.
     * @param requiredType validates whether element specifies this type, or
     * {@code null} to skip validation.
     * @throws BeanletValidationException if element is not of required type.
     * @return parameterized type for specified element, or {@code Object.class}
     * if element is not paremeterized.
     */
    public Type getParameterizedType(Element element, int typeArgument,
            Class<?> requiredType) {
        // PENDING: handle ArrayOutOfBoundsException?
        final Type t;
        switch (element.getElementType()) {
            case FIELD:
                Field f = ((FieldElement) element).getField();
                if (requiredType != null &&
                        !requiredType.isAssignableFrom(f.getType())) {
                    throw new BeanletTypeMismatchException(beanletName, 
                            element.getMember(), requiredType, f.getType());
                }
                if (f.getGenericType() instanceof ParameterizedType) {
                    t = ((ParameterizedType) f.getGenericType()).
                            getActualTypeArguments()[typeArgument];
                } else {
                    t = null;
                }
                break;
            case METHOD:
                Method m = ((MethodElement) element).getMethod();
                if (requiredType != null &&
                        !requiredType.isAssignableFrom(m.getParameterTypes()[0])) {
                    throw new BeanletTypeMismatchException(beanletName, 
                            element.getMember(), requiredType,
                            m.getParameterTypes()[0]);
                }
                if (m.getGenericParameterTypes()[0] instanceof ParameterizedType) {
                    t = ((ParameterizedType) m.getGenericParameterTypes()[0]).
                            getActualTypeArguments()[typeArgument];
                } else {
                    t = null;
                }
                break;
            case CONSTRUCTOR:
                Constructor c = ((ConstructorElement) element).getConstructor();
                if (requiredType != null &&
                        !requiredType.isAssignableFrom(c.getParameterTypes()[0])) {
                    throw new BeanletTypeMismatchException(beanletName, 
                            element.getMember(), requiredType, 
                            c.getParameterTypes()[0]);
                }
                if (c.getGenericParameterTypes()[0] instanceof ParameterizedType) {
                    t = ((ParameterizedType) c.getGenericParameterTypes()[0]).
                            getActualTypeArguments()[typeArgument];
                } else {
                    t = null;
                }
                break;
            case PARAMETER:
                if (element instanceof MethodParameterElement) {
                    MethodParameterElement mpe = (MethodParameterElement) element;
                    Method pm = mpe.getMethod();
                    if (requiredType != null &&
                            !requiredType.isAssignableFrom(
                            pm.getParameterTypes()[mpe.getParameter()])) {
                        throw new BeanletTypeMismatchException(beanletName, 
                                element.getMember(), requiredType, 
                                pm.getParameterTypes()[0]);
                    }
                    if (pm.getGenericParameterTypes()[0] instanceof ParameterizedType) {
                        t = ((ParameterizedType) pm.
                                getGenericParameterTypes()[mpe.getParameter()]).
                                getActualTypeArguments()[typeArgument];
                    } else {
                        t = null;
                    }
                    break;
                } else if (element instanceof ConstructorParameterElement) {
                    ConstructorParameterElement cpe = (ConstructorParameterElement) element;
                    Constructor pc = cpe.getConstructor();
                    if (requiredType != null &&
                            !requiredType.isAssignableFrom(
                            pc.getParameterTypes()[cpe.getParameter()])) {
                        throw new BeanletTypeMismatchException(beanletName, 
                                element.getMember(), requiredType, 
                                pc.getParameterTypes()[cpe.getParameter()]);
                    }
                    if (pc.getGenericParameterTypes()[cpe.getParameter()] instanceof ParameterizedType) {
                        t = ((ParameterizedType) pc.getGenericParameterTypes()[cpe.getParameter()]).
                                getActualTypeArguments()[typeArgument];
                    } else {
                        t = null;
                    }
                } else {
                    throw new AssertionError(element.getElementType());
                }
                break;
            default:
                throw new AssertionError(element.getElementType());
        }
        return t;
    }
    
    public BeanletFactory<?> getBeanletFactory(
            ElementAnnotation<? extends Element, Inject> ea) throws 
            BeanletWiringException {
        final BeanletFactory<?> factory;
        BeanletApplicationContext bctx = BeanletApplicationContext.instance();
        String name = getName(ea);
        if (bctx.exists(name)) {
            factory = bctx.getBeanletFactory(name);
        } else {
            Type type = getParameterizedType(ea.getElement(), 0);
            Class<?> beanletType = getTypeClass(type);
            if (type == null || (type instanceof WildcardType && Object.class.equals(beanletType))) {
                throw new BeanletWiringException(beanletName, 
                        ea.getElement().getMember());
            }
            Set<String> names = bctx.getBeanletNamesForType(beanletType);
            if (names.isEmpty()) {
                throw new BeanletTypeNotFoundException(beanletName,
                        ea.getElement().getMember(), beanletType);
            }
            if (names.size() > 1) {
                throw new BeanletTypeIsDuplicateException(beanletName,
                        ea.getElement().getMember(), beanletType, names);
            }
            factory = bctx.getBeanletFactory(names.iterator().next(), 
                    beanletType);
        }
        return factory;
    }
}
