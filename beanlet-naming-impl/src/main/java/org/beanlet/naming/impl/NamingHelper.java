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
package org.beanlet.naming.impl;

import static org.beanlet.naming.impl.NamingConstants.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.beanlet.BeanletException;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.PackageElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.naming.NamingContext;
import org.beanlet.naming.NamingProperty;
import org.beanlet.plugin.BeanletConfiguration;

/**
 *
 * @author Leon van Zantvoort
 */
public final class NamingHelper {

    private NamingHelper() {
    }

    private static ConcurrentMap<Map, Context> cache = 
            new ConcurrentHashMap<Map, Context>();
    
    public static Context getInitialContext(
            BeanletConfiguration<?> configuration, Element element) throws
            NamingException {
        return getInitialContext(configuration, element, true);
    }

    public static Context getInitialContext(
            final BeanletConfiguration<?> configuration, Element element, 
            boolean useCache) throws NamingException {
        NamingContext namingContext = getNamingContext(configuration, element);
        if (namingContext == null) {
            throw new NamingException("No naming context specified.");
        }
        Map<Object, Object> environment = new HashMap<Object, Object>();    // Use HashMap instead of Hashtable to prevent deadlock.
        for (NamingProperty property : namingContext.value()) {
            environment.put(property.name(), property.value());
        }
        Context context = null;
        if (useCache) {
            context = cache.get(environment);
        }
        if (context == null) {
            final Context tmp = new InitialContext(
                    new Hashtable<Object, Object>(environment));
            if (CLOSE_CONTEXT && useCache) {    // Only close on shutdown for cached context objects.
                configuration.getComponentUnit().addDestroyHook(new Runnable() {
                    public void run() {
                        try {
                            tmp.close();
                        } catch (NamingException e) {
                           throw new BeanletException(
                                   configuration.getComponentName(), e);
                        }
                    }
                });
            }
            if (useCache) {
                Context prev = cache.putIfAbsent(environment, tmp);
                if (prev != null) {
                    tmp.close();
                    context = prev;
                } else {
                    context = tmp;
                }
            }
        }
        return context;
    }
    
    public static NamingContext getNamingContext(
            BeanletConfiguration<?> configuration, Element element) {
        return getNamingContext(configuration, element, false);
    }
    
    public static NamingContext getNamingContext(
            BeanletConfiguration<?> configuration, Element element, 
            boolean memberOnly) {
        final NamingContext namingContext;
        AnnotationDeclaration<NamingContext> declaration = configuration.
                getAnnotationDomain().getDeclaration(NamingContext.class);
        NamingContext tmp = declaration.getDeclaredAnnotation(element);
        if (tmp != null) {
            namingContext = tmp;
        } else {
            if (element instanceof MethodParameterElement) {
                tmp = configuration.getAnnotationDomain().
                        getDeclaration(NamingContext.class).getAnnotation(
                        MethodElement.instance(
                        ((MethodParameterElement) element).getMethod()));
            } else if (element instanceof ConstructorParameterElement) {
                tmp = configuration.getAnnotationDomain().
                        getDeclaration(NamingContext.class).getAnnotation(
                        ConstructorElement.instance(
                        ((ConstructorParameterElement) element).getConstructor()));
            }
            if (tmp != null) {
                namingContext = tmp;
            } else if (!memberOnly) {
                Class<?> type = element.getMember().getDeclaringClass();
                tmp = declaration.getDeclaredAnnotation(
                        TypeElement.instance(type));
                if (tmp != null) {
                    namingContext = tmp;
                } else {
                    tmp = declaration.getDeclaredAnnotation(PackageElement.
                            instance(type.getPackage()));
                    if (tmp != null) {
                        namingContext = tmp;
                    } else {
                        namingContext = null;
                    }
                }
            } else {
                namingContext = null;
            }
        }
        return namingContext;
    }
}
