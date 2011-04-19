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
package org.beanlet.web.impl;

import static org.beanlet.web.impl.WebConstants.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.beanlet.common.AbstractElementAnnotationFactory;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.spi.ElementAnnotationFactoryProvider;
import org.beanlet.web.Request;
import org.beanlet.web.Session;
import org.beanlet.web.WebFilter;
import org.beanlet.web.WebListener;

/**
 *
 * @author Leon van Zantvoort
 */
public class MappedElementAnnotationFactoryProviderImpl extends AbstractProvider 
        implements ElementAnnotationFactoryProvider {
    
    private final Map<String, Class<? extends Annotation>> map;
    
    public MappedElementAnnotationFactoryProviderImpl() {
        map = new HashMap<String, Class<? extends Annotation>>();
        map.put("request", Request.class);
        map.put("session", Session.class);
        map.put("listener", WebListener.class);
    }
    
    public List<ElementAnnotationFactory> getElementAnnotationFactories() {
        List<ElementAnnotationFactory> factories = 
                new ArrayList<ElementAnnotationFactory>();
        for (final Map.Entry<String, Class<? extends Annotation>> e : map.entrySet()) {
            ElementAnnotationFactory<?> factory = new AbstractElementAnnotationFactory() {
                public String getNamespaceURI() {
                    return WEB_NAMESPACE_URI;
                }
                public String getNodeName() {
                    return e.getKey();
                }
                public Class<? extends Annotation> annotationType() {
                    return e.getValue();
                }
            };
            factories.add(factory);
        }
        return Collections.unmodifiableList(factories);
    }
}
