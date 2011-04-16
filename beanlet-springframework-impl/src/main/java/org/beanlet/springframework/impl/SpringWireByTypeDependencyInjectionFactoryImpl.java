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

import static org.beanlet.springframework.impl.SpringHelper.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.beanlet.BeanletWiringException;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.WiringMode;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 *
 * @author Leon van Zantvoort
 */
public final class SpringWireByTypeDependencyInjectionFactoryImpl extends
        SpringWiringDependencyInjectionFactory {
    
    private static final Logger logger = Logger.getLogger(
            SpringWireByTypeDependencyInjectionFactoryImpl.class.getName());
            
    private final BeanletConfiguration<?> configuration;
    
    public SpringWireByTypeDependencyInjectionFactoryImpl(
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
                supported = getBeanName(ea) != null;
            } else {
                supported = false;
            }
        } else {
            supported = false;
        }
        return supported;
    }

    /**
     * This method does not throw any exceptions if no injectant is found. This
     * differs from the original core WireByType implementation b/c this factory
     * is put in front of all the beanlet injection factories.
     */
    private String getBeanName(ElementAnnotation<? extends Element, Inject> ea) {
        Class<?> type = getType(ea);
        String[] names = getListableBeanFactory(configuration, ea.getElement()).
                getBeanNamesForType(type);
        if (names.length == 1) {
            return names[0];
        } else if (names.length == 0) {
            logger.finest("No spring bean found for type: '" + type + "'.");
            return null;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Multiple spring beans match type: '" + 
                        type + "'. Duplicate beans: " +
                        Arrays.toString(names) + ".");
            }
            return null;
        }
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        return Collections.emptySet();
    }
    
    public Injectant<?> getInjectant(ElementAnnotation<? extends Element, Inject> ea,
            ComponentContext<?> ctx) {
        String name = getBeanName(ea);
        if (name == null) {
            throw new BeanletWiringException(configuration.getComponentName(),
                    ea.getElement().getMember(),
                    "Spring beans have been modified during dependency injection phase.");
        }
        return new InjectantImpl<Object>(
                getListableBeanFactory(configuration, ea.getElement()).
                getBean(name), false);
    }
}
