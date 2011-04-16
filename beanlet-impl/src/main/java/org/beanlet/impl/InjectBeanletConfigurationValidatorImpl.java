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
package org.beanlet.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.beanlet.BeanletValidationException;
import org.beanlet.Inject;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.common.AbstractBeanletConfigurationValidator;
import org.beanlet.plugin.BeanletConfiguration;

/**
 *
 * @author Leon van Zantvoort
 */
public final class InjectBeanletConfigurationValidatorImpl extends
        AbstractBeanletConfigurationValidator<Inject> {
    
    public Class<Inject> annotationType() {
        return Inject.class;
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

    public boolean validate(BeanletConfiguration configuration, 
            Element element) throws BeanletValidationException {
        switch (element.getElementType()) {
            case CONSTRUCTOR:
                Constructor c = ((ConstructorElement) element).getConstructor();
                if (c.getParameterTypes().length > 1) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Constructor specifies more than one parameter: '" + c + "'.");
                }
                break;
            case METHOD:
                Method m = ((MethodElement) element).getMethod();
                if (Modifier.isStatic(m.getModifiers())) {
                    if (m.getParameterTypes().length > 1) {
                        throw new BeanletValidationException(configuration.getComponentName(),
                                "Factory method specifies more than one parameter: '" + m + "'.");
                    }
                    if (m.getReturnType().equals(Void.TYPE)) {
                        throw new BeanletValidationException(configuration.getComponentName(),
                                "Factory method MUST specify a return type: '" + m + "'.");
                    }
                } else {
                    if (m.getParameterTypes().length != 1) {
                        throw new BeanletValidationException(configuration.getComponentName(),
                                "Method does not specify exactly one parameter: '" + m + "'.");
                    }
                }
                break;
            case FIELD:
                Field f = ((FieldElement) element).getField();
                if (!Modifier.isStatic(f.getModifiers())) {
                    if (Modifier.isFinal(f.getModifiers())) {
                        // Disallowed, although it is supported by reflection.
                        throw new BeanletValidationException(configuration.getComponentName(),
                                "Non static field MUST not be final: '" + f + "'.");
                    }
                }
                break;
            case PARAMETER:
                if (element instanceof MethodElement) {
                    Method pm = ((MethodParameterElement) element).getMethod();
                    if (Modifier.isStatic(pm.getModifiers())) {
                        if (pm.getReturnType().equals(Void.TYPE)) {
                            throw new BeanletValidationException(configuration.getComponentName(),
                                    "Factory method MUST specify a return type: '" + pm + "'.");
                        }
                    }
                }
                break;
        }
        return true;
    }
}
