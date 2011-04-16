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

import org.beanlet.plugin.ElementAnnotationContext;
import org.beanlet.plugin.XMLElementAnnotation;
import org.beanlet.plugin.ElementAnnotationFactory;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.PackageElement;
import org.w3c.dom.Node;

/**
 *
 * @author Leon van Zantvoort
 */
public final class ElementAnnotationFactories implements 
        ElementAnnotationFactory<Annotation> {
        
    private final List<ElementAnnotationFactory> factories;

    public ElementAnnotationFactories() {
        factories = Collections.emptyList();
    }
    
    public ElementAnnotationFactories(List<ElementAnnotationFactory> factories) {
        if (factories == null) {
            this.factories = Collections.emptyList();
        } else {
            this.factories = factories;
        }
    }

    public XMLElementAnnotation<PackageElement, Annotation> 
            getElementAnnotation(Node node, Package pkg, 
            ElementAnnotationContext ctx) {
        for (ElementAnnotationFactory factory : factories) {
            @SuppressWarnings("unchecked")
            XMLElementAnnotation<PackageElement, Annotation> e = factory.
                    getElementAnnotation(node, pkg, ctx);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    public XMLElementAnnotation<Element, Annotation> getElementAnnotation(
            Node node, Class<?> cls, ElementAnnotationContext ctx) {
        for (ElementAnnotationFactory factory : factories) {
            @SuppressWarnings("unchecked")
            XMLElementAnnotation<Element, Annotation> e = factory.
                    getElementAnnotation(node, cls, ctx);
            if (e != null) {
                return e;
            }
        }
        return null;
    }
}
