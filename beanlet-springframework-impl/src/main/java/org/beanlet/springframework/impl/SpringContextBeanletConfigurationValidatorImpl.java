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
package org.beanlet.springframework.impl;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.beanlet.BeanletValidationException;
import org.beanlet.CollectionValue;
import org.beanlet.Inject;
import org.beanlet.MapValue;
import org.beanlet.Value;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.ParameterElement;
import static org.beanlet.springframework.impl.SpringHelper.*;
import org.beanlet.common.AbstractBeanletConfigurationValidator;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.springframework.SpringContext;

/**
 *
 * @author Leon van Zantvoort
 */
public final class SpringContextBeanletConfigurationValidatorImpl extends
        AbstractBeanletConfigurationValidator<SpringContext> {
    
    public Class<SpringContext> annotationType() {
        return SpringContext.class;
    }

    public Class<?>[] getParameterTypes() {
        return null;
    }

    public Class<?> getReturnType() {
        return null;
    }

    public Class<?>[] getExceptionTypes() {
        return null;
    }

    public int getRequiredModifiers() {
        return 0;
    }

    public int getInvalidModifiers() {
        return 0;
    }

    public boolean isBeanletTypeRequired() {
        return false;
    }

    private void checkInjection(BeanletConfiguration<?> configuration, 
            Element element, Inject inject) {
        Value value = inject.value();
        CollectionValue collection = inject.collection();
        MapValue map = inject.map();
        final boolean result;
        // Rules copied from ValueDependencyInjectionFactoryImpl.
        if (value.nill() || !value.value().equals("") || value.empty() || 
                !value.ref().equals("")) {
            result = true;
        } else if (collection.value().length > 0 || collection.empty()) {
            result = true;
        } else if (map.value().length > 0 || map.empty()) {
            result = true;
        } else {
            result = false;
        }
        if (result) {
            throw new BeanletValidationException(
                    configuration.getComponentName(),
                    "Element target for value/ref injection is also " +
                    "marked for Spring wiring: '" + element + "'.");
        }
    }
    
    public boolean validate(BeanletConfiguration configuration, 
            Element element) throws BeanletValidationException {
        if (!element.getElementType().equals(ElementType.PACKAGE) &&
                !element.getElementType().equals(ElementType.TYPE)) {
            AnnotationDeclaration<Inject> declaration = configuration.
                    getAnnotationDomain().getDeclaration(Inject.class);
            Inject inject = declaration.getAnnotation(element);
            if (inject == null) {
                if (element instanceof ParameterElement || 
                        element instanceof FieldElement) {
                    throw new BeanletValidationException(
                            configuration.getComponentName(),
                            "Member marked with SpringContext annotation MUST also " +
                            "specify Inject annotation: '" + 
                            element.getMember() + "'.");
                } else if (element instanceof MethodElement) {
                    boolean hasInject = false;
                    Method method = ((MethodElement) element).getMethod();
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        MethodParameterElement mpe = MethodParameterElement.
                                instance(method, i);
                        Inject tmp = declaration.getAnnotation(mpe);
                        if (tmp != null) {
                            checkInjection(configuration, element, tmp);
                            hasInject = true;
                        }
                    }
                    if (!hasInject) {
                        throw new BeanletValidationException(
                                configuration.getComponentName(),
                                "Member marked with SpringContext annotation " +
                                "MUST also specify Inject annotation: '" + 
                                element.getMember() + "'.");
                    }
                } else if (element instanceof ConstructorElement) {
                    boolean hasInject = false;
                    Constructor constructor = ((ConstructorElement) element).
                            getConstructor();
                    for (int i = 0; i < constructor.getParameterTypes().length; i++) {
                        ConstructorParameterElement cpe = ConstructorParameterElement.
                                instance(constructor, i);
                        Inject tmp = declaration.getAnnotation(cpe);
                        if (tmp != null) {
                            checkInjection(configuration, element, tmp);
                            hasInject = true;
                        }
                    }
                    if (!hasInject) {
                        throw new BeanletValidationException(
                                configuration.getComponentName(),
                                "Member marked with SpringContext annotation " +
                                "MUST also specify Inject annotation: '" + 
                                element.getMember() + "'.");
                    }
                }
            } else {
                checkInjection(configuration, element, inject);
            }
        }
        return true;
    }
}
