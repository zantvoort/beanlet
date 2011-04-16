/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Jargo - JSE Container Toolkit.
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
 * http://jargo.org
 */
package org.beanlet.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletWiringException;
import org.beanlet.Dependency;
import org.beanlet.IgnoreDependency;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.Beanlets;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.DependencyInjectionFactory;
import org.jargo.DependencyInspector;

/**
 *
 * @author Leon van Zantvoort
 */
public class DependencyInspectorImpl implements DependencyInspector {
    
    private final BeanletConfiguration configuration;
    private final DependencyInjectionFactory factory;
    private final ConcurrentMap<Class, Set<String>> cache;
    
    public DependencyInspectorImpl(BeanletConfiguration configuration,
            DependencyInjectionFactory factory) {
        this.configuration = configuration;
        this.factory = factory;
        this.cache = new ConcurrentHashMap<Class, Set<String>>();
    }

    public Set<String> getDependencies(Class<?> cls) {
        Set<String> dependencies = new HashSet<String>();
        if (configuration.getFactory() != null) {
            dependencies.add(configuration.getFactory());
            
            if (configuration.getFactoryMethod() != null) {
                BeanletApplicationContext bctx = BeanletApplicationContext.instance();
                Class<?> factoryType = bctx.getBeanletFactory(
                        configuration.getFactory()).getBeanletMetaData().getType();
            
                List<DependencyInjection> injections = factory.
                        getFactoryDependencyInjections(factoryType, 
                        configuration.getFactoryMethod());
                dependencies.addAll(getDependencies(injections));
            }
        }

        dependencies.addAll(getDependencyDependencies(cls));
        dependencies.addAll(getDependencies(
                factory.getConstructorDependencyInjections(cls)));
        dependencies.addAll(getDependencies(
                factory.getSetterDependencyInjections(cls)));
        
        return Collections.unmodifiableSet(dependencies);
    }
    
    private Set<String> getDependencies(List<DependencyInjection> injections) {
        Set<String> dependencies = new HashSet<String>();
        for (DependencyInjection injection : injections) {
            boolean optional = injection.isOptional();
            try {
                for (String dependency : injection.getDependencies()) {
                    if (!optional || BeanletApplicationContext.instance().exists(dependency)) {
                        if (dependency.startsWith(Beanlets.FACTORY_BEANLET_PREFIX)) {
                            dependency = dependency.substring(1);
                        }
                        dependencies.add(dependency);
                    }
                }
            } catch (BeanletWiringException e) {
                if (!optional) {
                    throw e;
                }
            }
        }
        return dependencies;
    }
    
    private Set<String> getDependencyDependencies(Class<?> cls) {
        Set<String> dependencies = cache.get(cls);
        if (dependencies == null) {
            dependencies = new HashSet<String>();
            AnnotationDomain domain = configuration.getAnnotationDomain();
            for (ElementAnnotation<Element, Dependency> ea : domain.
                    getDeclaration(Dependency.class).getElements()) {
                if (ea.getElement().isElementOf(cls)) {
                    if (!domain.getDeclaration(IgnoreDependency.class).
                            isAnnotationPresent(ea.getElement())) {
                        dependencies.addAll(Arrays.asList(ea.getAnnotation().value()));
                    }
                }
            }
            cache.put(cls, dependencies);
        }
        return dependencies;
    }
}
