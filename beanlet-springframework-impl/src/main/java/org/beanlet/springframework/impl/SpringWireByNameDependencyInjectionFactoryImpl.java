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
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
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
public final class SpringWireByNameDependencyInjectionFactoryImpl extends
        SpringWiringDependencyInjectionFactory {
    
    private static final Logger logger = Logger.getLogger(
            SpringWireByNameDependencyInjectionFactoryImpl.class.getName());
    
    private final BeanletConfiguration<?> configuration;

    public SpringWireByNameDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    public WiringMode getWiringMode() {
        return WiringMode.BY_NAME;
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        if (isWiringModeSupported(ea)) {
            String name = getName(ea);
            if (configuration.getComponentName().equals(name)) {
                // Exclude itself.
                return false;
            } else {
                if (name.length() > 0) {
                    if (getListableBeanFactory(configuration, ea.getElement()).
                            containsBean(name)) {
                        return true;
                    } else {
                        logger.finest("No spring bean found for name: '" + name + "'.");
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        return Collections.emptySet();
    }

    public Injectant<?> getInjectant(ElementAnnotation<? extends Element, 
            Inject> ea, ComponentContext<?> ctx) {
        return new InjectantImpl<Object>(
                getListableBeanFactory(configuration, ea.getElement()).
                getBean(getName(ea)), false);
    }
}
