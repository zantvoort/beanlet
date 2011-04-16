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
import javax.persistence.PersistenceUnit;
import org.jargo.ComponentContext;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.common.AbstractDependencyInjectionFactory;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 * The {@code EntityManagerFactory} API used to obtain an application-managed
 * entity manager is the same independent of whether this API is used in Java EE 
 * or Java SE environments (5.2.2 p115).
 *
 * @author Leon van Zantvoort
 */
public final class EntityManagerFactoryDependencyInjectionFactoryImpl extends
        AbstractDependencyInjectionFactory<PersistenceUnit> {
    
    private final BeanletConfiguration<?> configuration;
    
    public EntityManagerFactoryDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    public Class<PersistenceUnit> annotationType() {
        return PersistenceUnit.class;
    }
    
    public boolean isSupported(
            ElementAnnotation<? extends Element, PersistenceUnit> ea) {
        return true;
    }
    
    public boolean isOptional(
            ElementAnnotation<? extends Element, PersistenceUnit> ea) {
        return false;
    }

    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, PersistenceUnit> ea) {
        PersistenceUnit unit = ea.getAnnotation();
        BeanletPersistenceUnitInfo unitInfo = BeanletPersistenceUnitInfoFactory.
                getInstance(configuration.getComponentUnit()).
                getPersistenceUnitInfo(unit.unitName());
        return Collections.unmodifiableSet(unitInfo.getDependencies());
    }
    
    public Injectant<?> getInjectant(
            ElementAnnotation<? extends Element, PersistenceUnit> ea, 
            ComponentContext<?> ctx) {
        PersistenceUnit unit = ea.getAnnotation();
        return new InjectantImpl<Object>(
                new ApplicationManagedEntityManagerFactory(unit, 
                configuration.getComponentUnit()), true);
    }
}
