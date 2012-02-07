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
package org.beanlet.common;

import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.PackageElement;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.RetentionPolicy;
import org.beanlet.Retention;
import org.beanlet.Stateful;
import org.beanlet.Stateless;
import org.beanlet.Lazy;
import org.beanlet.Singleton;
import org.beanlet.Proxy;
import org.beanlet.ScopeAnnotation;
import org.beanlet.BeanletValidationException;
import org.jargo.ComponentConfiguration;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 *
 * @author Leon van Zantvoort
 */
public final class Beanlets {
    
    public static final String FACTORY_BEANLET_PREFIX = "&";
    
    private static final RetentionPolicy DEFAULT_POLICY;
    private static final Class<? extends Throwable>[] DEFAULT_TYPE;
    private static final Map<ComponentConfiguration, Beanlets> cache =
            new HashMap<ComponentConfiguration, Beanlets>();
    
    
    public static final boolean CHAIN_JARGO_EXCEPTIONS;
    
    static {
        try {
            DEFAULT_POLICY = (RetentionPolicy) Retention.class.
                    getMethod("value").getDefaultValue();
            @SuppressWarnings("unchecked")
            Class<? extends Throwable>[] tmp = (Class<? extends Throwable>[])
            Retention.class.getMethod("type").getDefaultValue();
            DEFAULT_TYPE = tmp;
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        // PERMISSION: java.util.PropertyPermission "org.beanlet.chainJargoExceptions" "read"
        CHAIN_JARGO_EXCEPTIONS = Boolean.getBoolean("org.beanlet.chainJargoExceptions");
    }
    
    public static final synchronized Beanlets getInstance(
            final ComponentConfiguration<?> configuration) {
        Beanlets beanlets = cache.get(configuration);
        if (beanlets == null) {
            if (configuration instanceof BeanletConfiguration) {
                beanlets = new Beanlets((BeanletConfiguration<?>) configuration);
            } else {
                beanlets = new Beanlets();
            }
            configuration.getComponentUnit().addDestroyHook(new Runnable() {
                public void run() {
                    synchronized (Beanlets.class) {
                        cache.remove(configuration);
                    }
                }
            });
            cache.put(configuration, beanlets);
        }
        return beanlets;
    }
    
    private final Stateful statefulAnnotation;
    private final Stateless statelessAnnotation;
    private final Singleton singletonAnnotation;
    private final Lazy lazyAnnotation;
    private final Lazy lazyPackageAnnotation;
    private final Retention retentionAnnotation;
    private final Proxy proxyAnnotation;
    private final List<Class<?>> interfaces;
    private final boolean hasScopeAnnotation;
    
    private Beanlets() {
        statefulAnnotation = null;
        statelessAnnotation = null;
        singletonAnnotation = null;
        lazyAnnotation = null;
        lazyPackageAnnotation = null;
        retentionAnnotation = null;
        proxyAnnotation = null;
        interfaces = Collections.emptyList();
        hasScopeAnnotation = false;
    }
    
    private Beanlets(BeanletConfiguration configuration) {
        // Too bad that we cannot get this info from the meta data object.
        statefulAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Stateful.class).getAnnotation(
                TypeElement.instance(configuration.getType()));
        statelessAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Stateless.class).getAnnotation(
                TypeElement.instance(configuration.getType()));
        singletonAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Singleton.class).getAnnotation(
                TypeElement.instance(configuration.getType()));
        lazyAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Lazy.class).getAnnotation(
                TypeElement.instance(configuration.getType()));
        lazyPackageAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Lazy.class).getAnnotation(
                PackageElement.instance(configuration.getType().getPackage()));
        retentionAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Retention.class).getAnnotation(
                TypeElement.instance(configuration.getType()));
        proxyAnnotation = configuration.getAnnotationDomain().getDeclaration(
                Proxy.class).getAnnotation(
                TypeElement.instance(configuration.getType()));
        
        interfaces = new ArrayList<Class<?>>();
        if (proxyAnnotation != null) {
            for (Class<?> inf : proxyAnnotation.value()) {
                if (inf.isInterface()) {
                    interfaces.add(inf);
                } else {
                    throw new BeanletValidationException(configuration.getComponentName(),
                            "Not an interface: '" + inf + "'.");
                }
            }
        }
        
        boolean tmpHasScopeAnnotation = false;
        for (ElementAnnotation<TypeElement, ? extends Annotation> ea :
                configuration.getAnnotationDomain().
                getTypedElements(TypeElement.class, configuration.getType())) {
            if (ea.getAnnotation().annotationType().
                    isAnnotationPresent(ScopeAnnotation.class)) {
                tmpHasScopeAnnotation = true;
            }
        }
        hasScopeAnnotation = tmpHasScopeAnnotation;
    }

    public boolean isStateful() {
        return getStateful() != null;
    }
    
    public Stateful getStateful() {
        return statefulAnnotation;
    }
    
    public boolean isStateless() {
        return getStateless() != null;
    }
    
    public Stateless getStateless() {
        return statelessAnnotation;
    }
    
    public boolean isVanilla() {
        return getSingleton() != null || !hasScopeAnnotation;
    }

    public boolean isSingleton() {
        return getSingleton() != null;
    }

    public Singleton getSingleton() {
        return singletonAnnotation;
    }
    
    public boolean isLazy() {
        return getLazy() != null && getLazy().value();
    }
    
    public Lazy getLazy() {
        return lazyAnnotation == null ? lazyPackageAnnotation : lazyAnnotation;
    }
    
    public RetentionPolicy getRetentionPolicy() {
        return retentionAnnotation == null ? DEFAULT_POLICY : 
            retentionAnnotation.value();
    }
    
    public Class<? extends Throwable>[] getRetentionExceptionType() {
        return (retentionAnnotation == null ? 
                DEFAULT_TYPE : retentionAnnotation.type()).clone();
    }
    
    public boolean isPropxy() {
        return proxyAnnotation != null;
    }
    
    public List<Class<?>> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }
}
