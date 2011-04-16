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
package org.beanlet.persistence.impl;

import java.util.Collections;
import java.util.Set;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import org.beanlet.BeanletValidationException;
import org.jargo.ComponentContext;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.common.AbstractDependencyInjectionFactory;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 *
 * @author Leon van Zantvoort
 */
public class EntityManagerDependencyInjectionFactoryImpl extends
        AbstractDependencyInjectionFactory<PersistenceContext> {
    
    private final BeanletConfiguration<?> configuration;
    
    public EntityManagerDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    public Class<PersistenceContext> annotationType() {
        return PersistenceContext.class;
    }

    public boolean isSupported(
            ElementAnnotation<? extends Element, PersistenceContext> ea) {
        return true;
    }
    
    public boolean isOptional(
            ElementAnnotation<? extends Element, PersistenceContext> ea) {
        return false;
    }

    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, PersistenceContext> ea) {
        PersistenceContext pctx = ea.getAnnotation();
        BeanletPersistenceUnitInfo unitInfo = BeanletPersistenceUnitInfoFactory.
                getInstance(configuration.getComponentUnit()).
                getPersistenceUnitInfo(pctx.unitName());
        return Collections.unmodifiableSet(unitInfo.getDependencies());
    }

    public Injectant<?> getInjectant(
            ElementAnnotation<? extends Element, PersistenceContext> ea, 
            ComponentContext<?> ctx) {
        PersistenceContext pctx = ea.getAnnotation();
        if (pctx.type() == PersistenceContextType.EXTENDED) {
            if (ctx.getComponentMetaData().isStatic()) {
                throw new BeanletValidationException(
                        configuration.getComponentName(),
                        "Extended persistence context MUST NOT be applied to " +
                        "static beanlets.");
            }
        }
        ContainerManagedEntityManagerFactory factory = 
                new ContainerManagedEntityManagerFactory(pctx, 
                configuration.getComponentUnit());
        return new InjectantImpl<Object>(
                factory.createEntityManager(), false);
    }
}
