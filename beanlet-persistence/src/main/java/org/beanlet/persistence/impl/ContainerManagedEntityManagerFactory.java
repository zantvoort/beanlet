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

import java.util.Map;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.jargo.ComponentUnit;

/**
 * @author Leon van Zantvoort
 */
public final class ContainerManagedEntityManagerFactory implements 
        EntityManagerFactory {
    
    private final BeanletPersistenceContext bpctx;
    
    public ContainerManagedEntityManagerFactory(PersistenceContext pctx, 
            ComponentUnit componentUnit) {
        bpctx = BeanletPersistenceContext.getInstance(pctx, componentUnit);
    }

    public EntityManager createEntityManager() {
        return bpctx.createEntityManager();
    }

    public EntityManager createEntityManager(Map map) {
        return bpctx.createEntityManager();
    }
    
    public boolean isOpen() {
        assert false;
        return true;
    }

    public void close() {
        assert false;
    }

    public Cache getCache() {
        throw new AssertionError("Must not be invoked.");
    }

    public CriteriaBuilder getCriteriaBuilder() {
        throw new AssertionError("Must not be invoked.");
    }

    public Metamodel getMetamodel() {
        throw new AssertionError("Must not be invoked.");
    }

    public Map<String, Object> getProperties() {
        throw new AssertionError("Must not be invoked.");
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        throw new AssertionError("Must not be invoked.");
    }
}
