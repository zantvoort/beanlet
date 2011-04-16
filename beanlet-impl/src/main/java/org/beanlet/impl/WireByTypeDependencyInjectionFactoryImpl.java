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

import org.beanlet.common.WiringDependencyInjectionFactory;
import org.beanlet.BeanletTypeIsDuplicateException;
import org.beanlet.BeanletTypeNotFoundException;
import org.beanlet.BeanletWiringException;
import java.util.Collections;
import java.util.Set;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.IgnoreDependency;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.WiringMode;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 *
 * @author Leon van Zantvoort
 */
public final class WireByTypeDependencyInjectionFactoryImpl extends
        WiringDependencyInjectionFactory {
    
    private final BeanletConfiguration<?> configuration;
    
    public WireByTypeDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    public WiringMode getWiringMode() {
        return WiringMode.BY_TYPE;
    }
    
    private boolean isTypeSupported(Class<?> type) {
        final boolean supported;
        if (type.isPrimitive()) {
            supported = false;
        } else if (type.isArray()) {
            supported = false;
        } else if (type.equals(Class.class)) {
            supported = false;
        } else if (type.equals(String.class)) {
            supported = false;
        } else if (Boolean.TYPE.equals(type)) {
            supported = false;
        } else if (Byte.TYPE.equals(type)) {
            supported = false;
        } else if (Short.TYPE.equals(type)) {
            supported = false;
        } else if (Integer.TYPE.equals(type)) {
            supported = false;
        } else if (Long.TYPE.equals(type)) {
            supported = false;
        } else if (Float.TYPE.equals(type)) {
            supported = false;
        } else if (Double.TYPE.equals(type)) {
            supported = false;
        } else {
            supported = true;
        }
        return supported;
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        final boolean supported;
        if (isWiringModeSupported(ea)) {
            Class<?> type = getType(ea);
            if (isTypeSupported(type)) {
                supported = true;
            } else {
                supported = false;
            }
        } else {
            supported = false;
        }
        return supported;
    }

    private String getBeanletName(
            ElementAnnotation<? extends Element, Inject> ea) throws 
            BeanletWiringException {
        Class<?> type = getType(ea);
        Set<String> names = BeanletApplicationContext.instance().
                getBeanletNamesForType(type, true, false);
        if (names.isEmpty()) {
            throw new BeanletTypeNotFoundException(
                    configuration.getComponentName(),
                    ea.getElement().getMember(), type);
        }
        if (names.size() > 1) {
            throw new BeanletTypeIsDuplicateException(
                    configuration.getComponentName(),
                    ea.getElement().getMember(), type, names);
        }
        return names.iterator().next();
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        final Set<String> dependencies;
        AnnotationDomain domain = configuration.getAnnotationDomain();
        if (domain.getDeclaration(IgnoreDependency.class).
                isAnnotationPresent(ea.getElement())) {
            dependencies = Collections.emptySet();
        } else {
            dependencies = Collections.singleton(getBeanletName(ea));
        }
        return dependencies;
    }
    
    public Injectant<?> getInjectant(ElementAnnotation<? extends Element, Inject> ea,
            ComponentContext<?> ctx) {
        return new InjectantImpl<Object>(
                BeanletApplicationContext.instance().
                getBeanlet(getBeanletName(ea)), false);
    }
}
