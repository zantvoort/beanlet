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

import static java.util.logging.Level.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.Beanlets;
import org.jargo.ComponentObject;
import org.jargo.ComponentObjectBuilder;
import org.jargo.ComponentReference;
import org.jargo.spi.ComponentObjectFactoryProvider;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentObjectFactory;
import org.jargo.deploy.SequentialDeployable;

/**
 * @author Leon van Zantvoort
 */
public final class VanillaBeanletObjectFactoryProviderImpl extends 
        AbstractProvider implements ComponentObjectFactoryProvider {
    
    private final Logger logger = Logger.getLogger(
            VanillaBeanletObjectFactoryProviderImpl.class.getName());
    
    public Sequence sequence(SequentialDeployable deployable) {
        return Sequence.AFTER;
    }
    
    public <T> ComponentObjectFactory<T> getComponentObjectFactory(
            ComponentConfiguration<T> configuration) {
        final ComponentObjectFactory<T> factory;
        Beanlets beanlets = Beanlets.getInstance(configuration);
        if (beanlets.isSingleton()) {
            if (beanlets.isLazy()) {
                factory = new ComponentObjectFactory<T>() {
                    private ComponentObjectBuilder<T> builder;
                    private ComponentObject<T> o;
                    public boolean isStatic() {
                        return true;
                    }
                    public void init(ComponentObjectBuilder<T> builder) {
                        this.builder = builder;
                    }
                    public ComponentObject<T> create() {
                        assert false;
                        return null;
                    }
                    public ComponentObject<T> getComponentObject() {
                        synchronized(this) {
                            if (o == null) {
                                o = builder.newInstance();
                            }
                        }
                        return o;
                    }
                    public void remove() {
                        assert false;
                    }
                    public void destroy() {
                        final ComponentObject<T> tmp;
                        synchronized(this) {
                            tmp = o;
                        }
                        if (tmp != null) {
                            tmp.destroy();
                        }
                    }
                };
            } else {
                factory = new ComponentObjectFactory<T>() {
                    private ComponentObject<T> o;
                    public boolean isStatic() {
                        return true;
                    }
                    public void init(ComponentObjectBuilder<T> builder) {
                        o = builder.newInstance();
                    }
                    public ComponentObject<T> create() {
                        assert false;
                        return null;
                    }
                    public void remove() {
                        assert false;
                    }
                    public ComponentObject<T> getComponentObject() {
                        return o;
                    }
                    public void destroy() {
                        if (o != null) {
                            o.destroy();
                        }
                    }
                };
            }
        } else {
            factory = new ComponentObjectFactory<T>() {
                // ComponentObject does not hold a strong reference to the
                // ComponentReference.
                private Map<ComponentReference<T>, ComponentObject<T>> map = 
                        Collections.synchronizedMap(
                        new WeakHashMap<ComponentReference<T>, ComponentObject<T>>());
                private ComponentObjectBuilder<T> builder;
                public boolean isStatic() {
                    return false;
                }
                public void init(ComponentObjectBuilder<T> builder) {
                    this.builder = builder;
                }
                public ComponentObject<T> create() {
                    ComponentReference<T> reference = builder.reference().
                            weakReference();
                    assert !map.containsKey(reference);
                    ComponentObject<T> object = builder.newInstance();
                    map.put(reference, object);
                    return object;
                }
                public ComponentObject<T> getComponentObject() {
                    ComponentReference<T> reference = builder.reference().
                            weakReference();
                    ComponentObject<T> object = map.get(reference);
                    assert object != null;
                    return object;
                }
                public void remove() {
                    ComponentReference<T> reference = builder.reference().
                            weakReference();
                    ComponentObject<T> object = map.get(reference);
                    assert object != null;
                    try {
                        object.destroy();
                    } finally {
                        map.remove(reference);
                    }
                }
                public void destroy() {
                    RuntimeException x = null;
                    Set<ComponentReference<T>> set = 
                            new HashSet<ComponentReference<T>>(map.keySet());
                    for (ComponentReference<T> reference : set) {
                        try {
                            reference.invalidate();
                        } catch (RuntimeException e) {
                            x = e;
                        }
                    }
                    assert map.isEmpty();
                    if (x != null) {
                        throw x;
                    }
                }
            };
        }
        return factory;
    }
}
