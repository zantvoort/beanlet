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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Bootstrap class that is used to obtain a container managed 
 * {@code EntityManagerFactory}.
 *
 * @author Leon van Zantvoort
 */
public final class BeanletPersistence {
    
    private static final Logger logger = Logger.getLogger(
            BeanletPersistence.class.getName());
    
    static final String PERSISTENCE_PROVIDER_PROPERTY = "javax.persistence.provider";
    static final String PERSISTENCE_PROVIDER_SERVICE = "META-INF/services/"
            + PersistenceProvider.class.getName();
    
    /**
     * Create and return an EntityManagerFactory for the specified persistence unit.
     *
     * @param unitInfo persistence unit info object.
     * @return The factory that creates EntityManagers configured according to the
     *         specified persistence unit/
     */
    public static EntityManagerFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo unitInfo) {
        return createContainerEntityManagerFactory(unitInfo, 
                Collections.EMPTY_MAP);
    }
    
    /**
     * Create and return an EntityManagerFactory for the specified persistence unit 
     * using the given properties.
     *
     * @param unitInfo persistence unit info object.
     * @param props Additional properties to use when creating the factory. The values of
     *            these properties override any values that may have been configured
     *            elsewhere.
     * @return The factory that creates EntityManagers configured according to the
     *         specified persistence unit.
     */
    public static EntityManagerFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo unitInfo, Map<?, ?> map) {

        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.putAll(PersistencePropertiesFactory.getInstance().getProperties());
        properties.putAll(map);
        
        // start by loading a provider explicitly specified in properties. The spec
        // doesn't seem to forbid providers that are not deployed as a service
        Object providerName = properties.get(PERSISTENCE_PROVIDER_PROPERTY);
        if (providerName instanceof String && !((String) providerName).equals("")) {
            EntityManagerFactory factory = createContainerFactory(
                    providerName.toString(),
                    unitInfo,
                    properties);
            if (factory != null) {
                return factory;
            }
        }
        
        if (unitInfo.getPersistenceProviderClassName() != null &&
                !unitInfo.getPersistenceProviderClassName().equals("")) {
            EntityManagerFactory factory = createContainerFactory(
                    unitInfo.getPersistenceProviderClassName(),
                    unitInfo,
                    properties);
            if (factory != null) {
                return factory;
            }
        }
        
        // load correctly deployed providers
        ClassLoader loader = unitInfo.getClassLoader();
        try {
            Enumeration<URL> providers = loader
                    .getResources(PERSISTENCE_PROVIDER_SERVICE);
            while (providers.hasMoreElements()) {
                String name = getProviderName(providers.nextElement());
                if (name != null && !name.equals("")) {
                    EntityManagerFactory factory = createContainerFactory(
                            name,
                            unitInfo,
                            properties);
                    
                    if (factory != null) {
                        return factory;
                    }
                }
            }
        } catch (IOException e) {
            // spec doesn't mention any exceptions thrown by this method
        }
        
        return null;
    }
    
    static String getProviderName(URL url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                url.openStream(),
                "UTF-8"));
        String providerName = null;
        try {
            providerName = in.readLine();
        } finally {
            in.close();
        }
        if (providerName != null) {
            providerName = providerName.trim();
        }
        
        return providerName;
    }
    
    static EntityManagerFactory createContainerFactory(
            String providerName, PersistenceUnitInfo unitInfo, Map properties) {
        try {
            Class providerClass = Class.forName(providerName, true, 
                    unitInfo.getClassLoader());
            PersistenceProvider provider = (PersistenceProvider) providerClass
                    .newInstance();
            return provider.createContainerEntityManagerFactory(unitInfo, 
                    properties);
        } catch (Exception e) {
            logger.log(Level.WARNING, "THROW", e);
            return null;
        }
    }
    
}
