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
package org.beanlet.naming.impl;

import static org.beanlet.naming.impl.NamingHelper.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.beanlet.BeanletWiringException;
import org.jargo.ComponentContext;
import org.beanlet.Inject;
import org.beanlet.WiringMode;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;

/**
 *
 * @author Leon van Zantvoort
 */
public final class NamingWireByTypeDependencyInjectionFactoryImpl extends
        NamingWiringDependencyInjectionFactory {
    
    private static final Logger logger = Logger.getLogger(
            NamingWireByTypeDependencyInjectionFactoryImpl.class.getName());
    
    private final BeanletConfiguration<?> configuration;
    
    public NamingWireByTypeDependencyInjectionFactoryImpl(
            final BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }

    public WiringMode getWiringMode() {
        return WiringMode.BY_TYPE;
    }
    
    private boolean isTypeSupported(Class<?> type) {
        final boolean supported;
        if (type.isPrimitive()) {
            supported = false;
        } else if (type.isArray()) {
            supported = false;
        } else if (type.equals(Class.class)) {
            supported = false;
        } else if (type.equals(String.class)) {
            supported = false;
        } else if (Boolean.TYPE.equals(type)) {
            supported = false;
        } else if (Byte.TYPE.equals(type)) {
            supported = false;
        } else if (Short.TYPE.equals(type)) {
            supported = false;
        } else if (Integer.TYPE.equals(type)) {
            supported = false;
        } else if (Long.TYPE.equals(type)) {
            supported = false;
        } else if (Float.TYPE.equals(type)) {
            supported = false;
        } else if (Double.TYPE.equals(type)) {
            supported = false;
        } else {
            supported = true;
        }
        return supported;
    }
    
    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        boolean supported = false;
        if (isWiringModeSupported(ea)) {
            Class<?> type = getType(ea);
            if (isTypeSupported(type)) {
                try {
                    Set<Binding> bindings = new HashSet<Binding>();
                    Context context = getInitialContext(configuration, 
                            ea.getElement());
                    synchronized (context) {
                        addBindings(context, type, bindings);
                    }
                    if (bindings.size() == 1) {
                        supported = true;
                    } else if (bindings.isEmpty()) {
                        logger.finest("No jndi binding found for type: '" + type + "'.");
                    } else {
                        if (logger.isLoggable(Level.FINE)) {
                            List<String> names = new ArrayList<String>();
                            for (Binding binding : bindings) {
                                names.add(binding.getName());
                            }
                            logger.fine("Multiple jndi bindings match type: '" + 
                                    type + "'. Duplicate bindings: " + names + ".");
                        }
                    }
                } catch (NamingException e) {
                    throw new BeanletWiringException(
                            configuration.getComponentName(), 
                            ea.getElement().getMember(), e);
                }
            }
        }
        return supported;
    }
    
    private void addBindings(Context ctx, Class<?> type, Set<Binding> bindings) throws 
            NamingException {
        NamingEnumeration<Binding> ne = ctx.listBindings("");
        while (ne.hasMore()) {
            Binding binding = ne.next();
            Object o = binding.getObject();
            if (o instanceof Context) {
                addBindings((Context) o, type, bindings);
            } else if (o != null && type.isAssignableFrom(o.getClass())) {
                bindings.add(binding);
            }
        }
    }
    
    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        return Collections.emptySet();
    }

    public Injectant<?> getInjectant(ElementAnnotation<? extends Element, 
            Inject> ea, ComponentContext<?> ctx) {
        try {
            Set<Binding> bindings = new HashSet<Binding>();
            Context context = getInitialContext(configuration, 
                    ea.getElement());
            synchronized (context) {
                addBindings(context, getType(ea), bindings);
            }
            if (bindings.size() != 1) {
                throw new BeanletWiringException(configuration.getComponentName(),
                        ea.getElement().getMember(),
                        "Naming tree has been modified during dependency injection phase.");
            }
            return new InjectantImpl<Object>(bindings.iterator().
                    next().getObject(), true);
        } catch (NamingException e) {
            throw new BeanletWiringException(configuration.getComponentName(), 
                    ea.getElement().getMember(), e);
        }
    }
}
