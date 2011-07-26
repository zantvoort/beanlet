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

import org.beanlet.BeanletValidationException;
import org.beanlet.Inject;
import org.beanlet.StaticFactory;
import org.beanlet.annotation.*;
import org.beanlet.common.AbstractBeanletConfigurationValidator;
import org.beanlet.plugin.BeanletConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *
 * @author Leon van Zantvoort
 */
public final class StaticFactoryBeanletConfigurationValidatorImpl extends
        AbstractBeanletConfigurationValidator<StaticFactory> {
    
    public Class<StaticFactory> annotationType() {
        return StaticFactory.class;
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
                if (c.getParameterTypes().length > 0) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Constructor specifies parameters. Use @Inject instead: '" + c + "'.");
                }
                break;
            case METHOD:
                Method m = ((MethodElement) element).getMethod();
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Static factory method MUST be static: '" + m + "'.");
                }
                if (m.getParameterTypes().length > 0) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Static factory method specifies parameters. Use @Inject instead: '" + m + "'.");
                }
                if (m.getReturnType().equals(Void.TYPE)) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Static factory method MUST specify a return type: '" + m + "'.");
                }
                break;
            case FIELD:
                Field f = ((FieldElement) element).getField();
                if (!Modifier.isStatic(f.getModifiers())) {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Static factory field MUST specify be static: '" + f + "'.");
                }
                break;
        }
        return true;
    }
}
