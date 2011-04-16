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

import static org.beanlet.naming.impl.NamingConstants.*;
import static org.beanlet.naming.impl.NamingHelper.*;
import java.util.Collections;
import java.util.Set;
import javax.naming.Context;
import javax.naming.NamingException;
import org.beanlet.BeanletException;
import org.beanlet.BeanletWiringException;
import org.beanlet.Inject;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.common.AbstractDependencyInjectionFactory;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.InjectantImpl;
import org.jargo.ComponentContext;

/**
 *
 * @author Leon van Zantvoort
 */
public final class NamingDependencyInjectionFactoryImpl extends
        AbstractDependencyInjectionFactory<Inject> {
    
    private final BeanletConfiguration<?> configuration;
    
    public NamingDependencyInjectionFactoryImpl(
            BeanletConfiguration<?> configuration) {
        super(configuration);
        this.configuration = configuration;
    }
    
    public Class<Inject> annotationType() {
        return Inject.class;
    }

    public boolean isSupported(ElementAnnotation<? extends Element, Inject> ea) {
        return Context.class.isAssignableFrom(getType(ea.getElement()));
    }
    
    public boolean isOptional(ElementAnnotation<? extends Element, Inject> ea) {
        return ea.getAnnotation().optional();
    }

    public Set<String> getDependencies(
            ElementAnnotation<? extends Element, Inject> ea) {
        return Collections.emptySet();
    }

    public Injectant<?> getInjectant(ElementAnnotation<? extends Element, 
            Inject> ea, ComponentContext<?> ctx) {
        try {
            final Context context = getInitialContext(configuration, 
                    ea.getElement(), false);
            if (CLOSE_CONTEXT) {
                ctx.reference().addDestroyHook(new Runnable() {
                   public void run() {
                       try {
                           context.close();
                       } catch (NamingException e) {
                           throw new BeanletException(
                                   configuration.getComponentName(), e);
                       }
                   } 
                });
            }
            return new InjectantImpl<Object>(context, false);
        } catch (NamingException e) {
            throw new BeanletWiringException(configuration.getComponentName(), 
                    ea.getElement().getMember(), e);
        }
    }
}
