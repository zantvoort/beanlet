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
import java.util.Map;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.jargo.ComponentUnit;

/**
 * @author Leon van Zantvoort
 */
public final class ApplicationManagedEntityManagerFactory implements 
        EntityManagerFactory {
    
    private final BeanletEntityManagerFactory emf;
    
    public ApplicationManagedEntityManagerFactory(PersistenceUnit persistenceUnit, 
            ComponentUnit componentUnit) {
        this.emf = BeanletEntityManagerFactoryRegistry.
                getInstance(persistenceUnit, componentUnit);
    }

    public EntityManager createEntityManager() {
        return createEntityManager(Collections.emptyMap());
    }

    public EntityManager createEntityManager(Map map) {
        final EntityManager em;
        if (emf.getPersistenceUnitInfo().getTransactionType() == 
                PersistenceUnitTransactionType.JTA) {
            em = new JTAEntityManager(emf.createEntityManager(map));
        } else if (emf.getPersistenceUnitInfo().getTransactionType() == 
                PersistenceUnitTransactionType.RESOURCE_LOCAL) {
            em = new ResourceLocalEntityManager(emf.createEntityManager(map));
        } else {
            throw new PersistenceException(
                    "PersistenceUnitTransactionType not supported: " +
                    emf.getPersistenceUnitInfo().getTransactionType() + ".");
        }
        return em;
    }
    
    public boolean isOpen() {
        return emf.isOpen();
    }

    public void close() {
        emf.close();
    }

    public Cache getCache() {
        return emf.getCache();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return emf.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return emf.getMetamodel();
    }

    public Map<String, Object> getProperties() {
        return emf.getProperties();
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return emf.getPersistenceUnitUtil();
    }
}
