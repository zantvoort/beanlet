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

import static org.beanlet.common.BeanletConstants.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beanlet.*;
import org.beanlet.common.AbstractElementAnnotationFactory;
import org.beanlet.common.AbstractProvider;
import org.beanlet.plugin.ElementAnnotationFactory;
import org.beanlet.plugin.spi.ElementAnnotationFactoryProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class MappedElementAnnotationFactoryProviderImpl extends AbstractProvider 
        implements ElementAnnotationFactoryProvider {
    
    private final Map<String, Class<? extends Annotation>> map;
    
    public MappedElementAnnotationFactoryProviderImpl() {
        // Annotations with default values should be listed separately.
        map = new HashMap<String, Class<? extends Annotation>>();
        map.put("around-invoke", AroundInvoke.class);
        map.put("factory", Factory.class);
        map.put("exclude-class-interceptors", ExcludeClassInterceptors.class);
        map.put("exclude-default-interceptors", ExcludeDefaultInterceptors.class);
        map.put("ignore-dependency", IgnoreDependency.class);
        map.put("lazy", Lazy.class);
        map.put("post-construct", PostConstruct.class);
        map.put("pre-destroy", PreDestroy.class);
        map.put("proxy-method", ProxyMethod.class);
        map.put("operation", Operation.class);
        map.put("remove", Remove.class);
        map.put("execute", Execute.class);
        map.put("schedule", Schedule.class);
        map.put("start", Start.class);
        map.put("stateful", Stateful.class);
        map.put("stateless", Stateless.class);
        map.put("stop", Stop.class);
        map.put("vanilla", Vanilla.class);
        map.put("wiring", Wiring.class);
        map.put("attribute", Attribute.class);
        map.put("static-factory", StaticFactory.class);
    }
    
    public List<ElementAnnotationFactory> getElementAnnotationFactories() {
        List<ElementAnnotationFactory> factories = 
                new ArrayList<ElementAnnotationFactory>();
        for (final Map.Entry<String, Class<? extends Annotation>> e : map.entrySet()) {
            ElementAnnotationFactory<?> factory = new AbstractElementAnnotationFactory() {
                public String getNamespaceURI() {
                    return BEANLET_NAMESPACE_URI;
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
