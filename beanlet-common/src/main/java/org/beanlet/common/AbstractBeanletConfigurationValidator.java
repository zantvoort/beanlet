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
import org.beanlet.plugin.BeanletConfigurationValidator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.beanlet.BeanletValidationException;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
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
public abstract class AbstractBeanletConfigurationValidator<T extends Annotation>
        implements BeanletConfigurationValidator {
    
    public abstract Class<T> annotationType();
    
    /**
     * Returns the valid parameter types for the underlying members.
     * First parameter is used in case of field validation.
     */
    public abstract Class<?>[] getParameterTypes();
    
    public abstract Class<?> getReturnType();
    
    public abstract Class<?>[] getExceptionTypes();
    
    /**
     * Returns the modifiers that are required for a method, or
     * {@code 0} if no restrictions apply.
     */
    public abstract int getRequiredModifiers();
    
    /**
     * Returns the modifiers that are not allowed for a method, or
     * {@code 0} if no restrictions apply.
     */
    public abstract int getInvalidModifiers();

    public abstract boolean isBeanletTypeRequired();
    
    /**
     * This method provides a hook for validating individual elements.
     * 
     * @return {@code true} to proceed validation, {@code false} to skip
     * validation for the specified {@code element}.
     */
    public boolean validate(BeanletConfiguration configuration, 
            Element element) throws BeanletValidationException {
        if (isBeanletTypeRequired() && 
                !element.isElementOf(configuration.getType())) {
            throw new BeanletValidationException(configuration.getComponentName(),
                    annotationType().getSimpleName() + " annotation MAY only " +
                    "be applied to elements of beanlet type: '" + element + "'.");
        }
        return true;
    }
    
    public void validate(BeanletConfiguration configuration) throws
            BeanletValidationException {
        AnnotationDomain domain = configuration.getAnnotationDomain();
        AnnotationDeclaration<T> declaration = domain.getDeclaration(
                annotationType());
        for (ElementAnnotation<ConstructorElement, T> ea :
            declaration.getTypedElements(ConstructorElement.class)) {
                if (validate(configuration, ea.getElement())) {
                    validateConstructor(configuration.getComponentName(), ea);
                }
        }
        for (ElementAnnotation<ConstructorParameterElement, T> ea :
            declaration.getTypedElements(ConstructorParameterElement.class)) {
                if (validate(configuration, ea.getElement())) {
                    validateConstructorParameter(configuration.getComponentName(), ea);
                }
        }
        for (ElementAnnotation<MethodElement, T> ea :
            declaration.getTypedElements(MethodElement.class)) {
                if (validate(configuration, ea.getElement())) {
                    validateMethod(configuration.getComponentName(), ea);
                }
        }
        for (ElementAnnotation<MethodParameterElement, T> ea :
            declaration.getTypedElements(MethodParameterElement.class)) {
                if (validate(configuration, ea.getElement())) {
                    validateMethodParameter(configuration.getComponentName(), ea);
                }
        }
        for (ElementAnnotation<FieldElement, T> ea :
            declaration.getTypedElements(FieldElement.class)) {
                if (validate(configuration, ea.getElement())) {
                    validateField(configuration.getComponentName(), ea);
                }
        }
    }
    
    public void validateConstructor(String beanletName,
            ElementAnnotation<ConstructorElement, T> ea) {
        validateMember(beanletName, ea.getElement().getConstructor());
    }
    
    public void validateConstructorParameter(String beanletName,
            ElementAnnotation<ConstructorParameterElement, T> ea) {
        validateMember(beanletName, ea.getElement().getConstructor());
    }
    
    public void validateMethod(String beanletName,
            ElementAnnotation<MethodElement, T> ea) {
        validateMember(beanletName, ea.getElement().getMethod());
    }
    
    public void validateMethodParameter(String beanletName,
            ElementAnnotation<MethodParameterElement, T> ea) {
        validateMember(beanletName, ea.getElement().getMethod());
    }
    
    public void validateField(String beanletName,
            ElementAnnotation<FieldElement, T> ea) {
        validateMember(beanletName, ea.getElement().getField());
    }
    
    public final void validateMember(String beanletName, Member member) {
        int missing = getRequiredModifiers() & (~ member.getModifiers());
        if (missing != 0) {
            throw new BeanletValidationException(beanletName,
                    "Element MUST specify the " +
                    Modifier.toString(missing) + " modifier(s): '" + member + "'.");
        }
        int invalid = member.getModifiers() & getInvalidModifiers();
        if (invalid != 0) {
            throw new BeanletValidationException(beanletName,
                    "Element MUST NOT specify the " +
                    Modifier.toString(invalid) + " modifier(s): '" + member + "'.");
        }
        
        if (getParameterTypes() != null) {
            final Class<?>[] types;
            if (member instanceof Method) {
                types = ((Method) member).getParameterTypes();
            } else if (member instanceof Constructor) {
                types = ((Constructor) member).getParameterTypes();
            } else if (member instanceof Field) {
                types = new Class[]{((Field) member).getType()};
            } else {
                assert false : member;
                types = null;
            }
            boolean allowParameters = true;
            if (types.length == getParameterTypes().length) {
                for (int i = 0; i < types.length; i++) {
                    if (!types[i].isAssignableFrom(getParameterTypes()[i])) {
                        allowParameters = false;
                        break;
                    }
                }
            } else {
                allowParameters = false;
            }
            if (!allowParameters) {
                if (getParameterTypes().length == 0) {
                    if (!(member instanceof Field)) {
                        throw new BeanletValidationException(beanletName,
                                "Element MUST NOT specify any parameters: '" + member + "'.");
                    }
                } else {
                    throw new BeanletValidationException(beanletName,
                            "Element MUST specify the following parameters: " +
                            Arrays.asList(getParameterTypes()) +
                            " (or any superclasses): '" + member + "'.");
                }
            }
        }
        
        if (getReturnType() != null) {
            final Class<?> type;
            if (member instanceof Method) {
                type = ((Method) member).getReturnType();
            } else {
                type = null;
            }
            if (type != null && !getReturnType().isAssignableFrom(type)) {
                throw new BeanletValidationException(beanletName,
                        "Element MUST specify return type '" + 
                        getReturnType() + "' (or any subclass): '" + 
                        member + "'.");
            }
        }
        
        if (getExceptionTypes() != null) {
            final Class<?>[] types;
            if (member instanceof Method) {
                types = ((Method) member).getExceptionTypes();
            } else if (member instanceof Constructor) {
                types = ((Constructor) member).getExceptionTypes();
            } else {
                types = null;
            }
            if (types != null) {
                for (Class<?> exceptionCls : types) {
                    if (!RuntimeException.class.isAssignableFrom(exceptionCls) &&
                            !Error.class.isAssignableFrom(exceptionCls)) {
                        boolean allowException = false;
                        for (Class<?> allowedExceptionCls : getExceptionTypes()) {
                            if (allowedExceptionCls.isAssignableFrom(exceptionCls)) {
                                allowException = true;
                                break;
                            }
                        }
                        if (!allowException) {
                            if (getExceptionTypes().length == 0) {
                                throw new BeanletValidationException(beanletName,
                                        "Element MUST NOT throw a checked " +
                                        "exception: '" + member + "'.");
                            } else {
                                throw new BeanletValidationException(beanletName,
                                        "Element MAY only throw one of the " +
                                        "following exceptions: " +
                                        Arrays.asList(getExceptionTypes()) +
                                        "(or any subclasses): '" + member + "'.");
                            }
                        }
                    }
                }
            }
        }
    }
}
