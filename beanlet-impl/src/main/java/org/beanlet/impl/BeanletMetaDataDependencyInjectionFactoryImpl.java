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

import org.beanlet.common.ParameterizedTypeAwareDependencyInjectionFactory;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.Set;
import org.beanlet.BeanletFactory;
import org.beanlet.BeanletMetaData;
import org.beanlet.BeanletTypeMismatchException;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletMetaDataDependencyInjectionFactoryImpl extends
        ParameterizedTypeAwareDependencyInjectionFactory {
    
    public BeanletMetaDataDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        final boolean supported;
        Class<?> type = getType(ea);
        if (BeanletMetaData.class.isAssignableFrom(type)) {
            Type parameterizedType = getParameterizedType(ea.getElement(), 0);
            if (parameterizedType != null) {
                Class<?> beanletType = getTypeClass(parameterizedType);
                if (parameterizedType instanceof WildcardType) {
                    if (!Object.class.equals(beanletType)) {
                        BeanletFactory<?> factory = getBeanletFactory(ea);
                        Class<?> actualType = factory.getBeanletMetaData().getType();
                        if (!beanletType.isAssignableFrom(actualType)) {
                            throw new BeanletTypeMismatchException(
                                    factory.getBeanletMetaData().getBeanletName(),
                                    ea.getElement().getMember(),
                                    beanletType, actualType);
                        }
                    }
                } else {
                    BeanletFactory<?> factory = getBeanletFactory(ea);
                    Class<?> actualType = factory.getBeanletMetaData().getType();
                    if (!beanletType.equals(actualType)) {
                        throw new BeanletTypeMismatchException(
                                factory.getBeanletMetaData().getBeanletName(),
                                ea.getElement().getMember(),
                                beanletType, actualType);
                    }
                }
            }
            supported = true;
        } else {
            supported = false;
        }
        return supported;
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        return Collections.emptySet();
    }

    public Injectant<?> getInjectant(ElementAnnotation<? extends Element, Inject> ea, 
            ComponentContext<?> ctx) {
        return new InjectantImpl<Object>(BeanletMetaDataImpl.instance(
                ctx.getComponentMetaData()), true);
    }
}
