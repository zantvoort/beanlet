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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.ComponentConfiguration;
import org.jargo.MetaData;
import org.jargo.spi.MetaDataProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class PersistenceMetaDataProviderImpl extends AbstractProvider 
        implements MetaDataProvider {
    
    public List<MetaData> getMetaData(ComponentConfiguration configuration) {
        List<MetaData> metaData = 
                new ArrayList<MetaData>();
        if (PersistenceConstants.isSupported() &&
                configuration instanceof BeanletConfiguration) {
            AnnotationDomain domain = ((BeanletConfiguration) configuration).
                    getAnnotationDomain();
            AnnotationDeclaration<PersistenceContext> pcd = 
                    domain.getDeclaration(PersistenceContext.class);
            for (ElementAnnotation<Element, PersistenceContext> ea : pcd.getElements()) {
                metaData.add(new PersistenceContextMetaData(ea.getAnnotation()));
            }
            AnnotationDeclaration<PersistenceUnit> pud = 
                    domain.getDeclaration(PersistenceUnit.class);
            for (ElementAnnotation<Element, PersistenceUnit> ea : pud.getElements()) {
                metaData.add(new PersistenceUnitMetaData(ea.getAnnotation()));
            }
        }
        return Collections.unmodifiableList(metaData);
    }
}
