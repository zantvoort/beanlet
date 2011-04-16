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
package org.beanlet.impl;

import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentUnit;

public final class BeanletConfigurationImpl<T> implements BeanletConfiguration<T> {

    private final ComponentUnit unit;
    private final XMLAnnotationDomain<T> domain;
    
    public BeanletConfigurationImpl(ComponentUnit unit, 
            XMLAnnotationDomain<T> domain) {
        this.unit = unit;
        this.domain = domain;
    }

    public ComponentUnit getComponentUnit() {
        return unit;
    }

    public Class<T> getType() {
        return domain.getBeanletType();
    }

    public String getDescription() {
        String description = domain.getDescription();
        return description == null ? "" : description;
    }
    
    public String getComponentName() {
        return domain.getBeanletName();
    }

    public String getFactory() {
        return domain.getFactory();
    }
    
    public String getFactoryMethod() {
        return domain.getFactoryMethod();
    }
    
    public AnnotationDomain getAnnotationDomain() {
        return domain;
    }
    
    public int hashCode() {
	int hashCode = 1;
        hashCode = 31*hashCode + getComponentName().hashCode();
        hashCode = 31*hashCode + getType().hashCode();
	return hashCode;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ComponentConfiguration) {
            ComponentConfiguration o = (ComponentConfiguration) obj;
            return (getComponentName().equals(o.getComponentName()) && 
                    getType().equals(o.getType()));
        }
        return false;
    }

    public String toString() {
        return "BeanletConfiguration{name=" + getComponentName() + 
                ", type=" + getType() + 
                ", unit=" + getComponentUnit() + ", " +
                "annotationDomain=" + getAnnotationDomain() + "}@" + 
                Integer.toHexString(System.identityHashCode(this));
    }
}