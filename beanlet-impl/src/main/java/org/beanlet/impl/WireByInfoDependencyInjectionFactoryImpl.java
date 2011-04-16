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

import org.beanlet.common.WiringDependencyInjectionFactory;
import java.util.Map;
import java.util.Set;
import org.beanlet.WiringMode;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;
import org.jargo.ComponentReference;

/**
 *
 * @author Leon van Zantvoort
 */
public final class WireByInfoDependencyInjectionFactoryImpl extends
        WiringDependencyInjectionFactory {
    
    public WireByInfoDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
    }
    
    public WiringMode getWiringMode() {
        return WiringMode.BY_INFO;
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        return isWiringModeSupported(ea);
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        return null;
    }
    
    public Injectant<?> getInjectant(
            ElementAnnotation<? extends Element, Inject> ea,
            ComponentContext<?> ctx) {
        ComponentReference reference = ctx.reference();
        assert reference != null;
        if (ctx.getComponentMetaData().isStatic()) {
            return null;
        }
        Object info = reference.getInfo();
        if (!(info instanceof Map)) {
            return null;
        }
        String name = getName(ea);
        if (!((Map) info).containsKey(name)) {
            return null;
        }
        return new InjectantImpl<Object>(((Map) info).get(name), false);
    }
}
