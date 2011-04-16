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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.PackageElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.springframework.SpringContext;
import org.beanlet.springframework.SpringResource;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 *
 * @author Leon van Zantvoort
 */
public final class SpringHelper {

    private static final WeakHashMap<ClassLoader, Map<SpringContext, 
            ListableBeanFactory>> factories =            
            new WeakHashMap<ClassLoader, Map<SpringContext, ListableBeanFactory>>();
    
    private SpringHelper() {
    }
    
    public static synchronized ListableBeanFactory getListableBeanFactory(
            BeanletConfiguration<?> configuration, Element element) {
        SpringContext springContext = getSpringContext(configuration, element);
        if (springContext == null) {
            throw new ApplicationContextException("No spring context specified.");
        }
        final ClassLoader loader = configuration.getComponentUnit().
                getClassLoader();
        Map<SpringContext, ListableBeanFactory> map = factories.get(loader);
        if (map == null) {
            map = new HashMap<SpringContext, ListableBeanFactory>();
            factories.put(loader, map);
        }
        ListableBeanFactory factory = map.get(springContext);
        if (factory == null) {
            ClassLoader org = null;
            try {
                org = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        // PERMISSION: java.lang.RuntimePermission getClassLoader
                        ClassLoader org = Thread.currentThread().getContextClassLoader();
                        // PERMISSION: java.lang.RuntimePermission setContextClassLoader
                        Thread.currentThread().setContextClassLoader(loader);
                        return org;
                    }
                });
                if (springContext.applicationContext()) {
                    factory = new GenericApplicationContext();
                } else {
                    factory = new DefaultListableBeanFactory();
                }
                // Do not create spring context in priviliged scope!
                for (SpringResource r : springContext.value()) {
                    String path = r.value();
                    Resource resource = null;
                    BeanDefinitionReader reader = null;
                    switch (r.type()) {
                        case CLASSPATH:
                            resource = new ClassPathResource(path);
                            break;
                        case FILESYSTEM:
                            resource = new FileSystemResource(path);
                            break;
                        case URL:
                            resource = new UrlResource(path);
                            break;
                        default:
                            assert false : r.type();
                    }
                    switch (r.format()) {
                        case XML:
                            reader = new XmlBeanDefinitionReader(
                                    (BeanDefinitionRegistry) factory);
                            break;
                        case PROPERTIES:
                            reader = new PropertiesBeanDefinitionReader(
                                    (BeanDefinitionRegistry) factory);
                            break;
                        default:
                            assert false : r.format();
                    }
                    if (resource != null && resource.exists()) {
                        reader.loadBeanDefinitions(resource);
                    }
                }
                if (factory instanceof ConfigurableApplicationContext) {
                    ((ConfigurableApplicationContext) factory).refresh();
                }
                map.put(springContext, factory);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ApplicationContextException("Failed to construct spring " + 
                        (springContext.applicationContext() ? 
                        "application context" : "bean factory") + ".", e);
            } finally {
                final ClassLoader tmp = org;
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        // PERMISSION: java.lang.RuntimePermission setContextClassLoader
                        Thread.currentThread().setContextClassLoader(tmp);
                        return null;
                    }
                });
            }
        }
        return factory;
    }
    
    public static SpringContext getSpringContext(
            BeanletConfiguration<?> configuration, Element element) {
        return getSpringContext(configuration, element, false);
    }
    
    public static SpringContext getSpringContext(
            BeanletConfiguration<?> configuration, Element element, 
            boolean memberOnly) {
        final SpringContext springContext;
        AnnotationDeclaration<SpringContext> declaration = configuration.
                getAnnotationDomain().getDeclaration(SpringContext.class);
        SpringContext tmp = declaration.getDeclaredAnnotation(element);
        if (tmp != null) {
            springContext = tmp;
        } else {
            if (element instanceof MethodParameterElement) {
                tmp = configuration.getAnnotationDomain().
                        getDeclaration(SpringContext.class).getAnnotation(
                        MethodElement.instance(
                        ((MethodParameterElement) element).getMethod()));
            } else if (element instanceof ConstructorParameterElement) {
                tmp = configuration.getAnnotationDomain().
                        getDeclaration(SpringContext.class).getAnnotation(
                        ConstructorElement.instance(
                        ((ConstructorParameterElement) element).getConstructor()));
            }
            if (tmp != null) {
                springContext = tmp;
            } else if (!memberOnly) {
                Class<?> type = element.getMember().getDeclaringClass();
                tmp = declaration.getDeclaredAnnotation(
                        TypeElement.instance(type));
                if (tmp != null) {
                    springContext = tmp;
                } else {
                    tmp = declaration.getDeclaredAnnotation(PackageElement.
                            instance(type.getPackage()));
                    if (tmp != null) {
                        springContext = tmp;
                    } else {
                        springContext = null;
                    }
                }
            } else {
                springContext = null;
            }
        }
        return springContext;
    }
}
