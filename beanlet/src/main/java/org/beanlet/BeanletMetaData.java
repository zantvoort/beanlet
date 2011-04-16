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
package org.beanlet;

import java.util.List;

/**
 * Provides information on the beanlet definition.
 *
 * @param <T> the beanlet type.
 * @author Leon van Zantvoort
 */
public interface BeanletMetaData<T> extends MetaData {

    /**
     * Returns canonical name of the beanlet.
     *
     * @return canonical name of the beanlet.
     */
    String getBeanletName();
    
    /**
     * Returns the beanlet type. This can either be a class or
     * an interface.
     *
     * @return beanlet type.
     */
    Class<T> getType();
    
    /**
     * Returns the classloader associated with the deployment of this beanlet.
     * This is not necessarily the classloader that loaded the beanlet type.
     *
     * @return classloader associated with this beanlet.
     */
    ClassLoader getClassLoader();
    
    /** 
     * <p>Returns a list of additional interfaces that are exposed by this 
     * beanlet.</p>
     *
     * <p>Note that this list is empty for non-proxy beanlets.</p>
     *
     * @return list of interfaces exposed by beanlet.
     */
    List<Class<?>> getInterfaces();

    /**
     * <p>Returns {@code true} if this is a proxy beanlet, {@code false}
     * otherwise.</p>
     * 
     * <p>Proxy beanlets have the ability to expose interfaces that are not 
     * implemented by the type. These interfaces are returned by
     * {@code getInterfaces}. Furthermore, proxy beanlets allow method 
     * calls to be intercepted by beanlet interceptors.</p>
     * 
     * @return {@code true} if this is a proxy beanlet.
     */
    boolean isProxy();
    
    /**
     * <p>Returns {@code true} if this is a vanilla beanlet, {@code false} 
     * otherwise.</p>
     *
     * <p>Vanilla beanlets directly expose their beanlet instances and thus
     * their type, as specified by {@code getType}. Proxy vanilla
     * beanlets are implemented by dynamically subclassing the type 
     * adding the proxying logic.</p>
     *
     * @return {@code true} if this is a vanilla beanlet.
     */
    boolean isVanilla();
    
    /**
     * Returns {@code true} if this component is created from a static context.
     */
    boolean isStatic();
    
    /**
     * Returns {@code true} if the specified meta data type is available for
     * this beanlet.
     *
     * @return {@code true} if meta data is available for specified type.
     */
    boolean isMetaDataPresent(Class<? extends MetaData> metaDataType);
    
    /**
     * Returns a list of meta data objects of the specified type.
     *
     * @return list of specified meta data objects for beanlet.
     */
    <M extends MetaData> List<M> getMetaData(Class<M> metaDataType);
    
    /**
     * Returns a list of meta data objects for this beanlet.
     *
     * @return list of meta data objects for beanlet.
     */
    List<MetaData> getMetaData();
}
