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

import static java.lang.annotation.ElementType.*;

import java.lang.reflect.*;
import java.util.Set;
import org.beanlet.BeanletTypeMismatchException;
import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.ParameterElement;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.Injectant;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentContext;

/**
 *
 * @author Leon van Zantvoort
 */
public class ValidatingDependencyInjection implements DependencyInjection {
    
    private final String beanletName;
    private final DependencyInjection injection;
    
    public ValidatingDependencyInjection(ComponentConfiguration<?> configuration,
            DependencyInjection injection) {
        this.beanletName = configuration.getComponentName();
        this.injection = injection;
    }
    
    public Element getTarget() {
        Element element = injection.getTarget();
        
        assert element.getElementType() == FIELD ||
                element.getElementType() == METHOD ||
                element.getElementType() == CONSTRUCTOR ||
                element.getElementType() == PARAMETER : 
                element.getElementType();
        
        return element;
    }
    
    public boolean isOptional() {
        return injection.isOptional();
    }
    
    public Set<String> getDependencies() throws BeanletWiringException {
        return injection.getDependencies();
    }
    
    public Injectant<?> getInjectant(ComponentContext<?> ctx) throws 
            BeanletWiringException {
        Injectant injectant = injection.getInjectant(ctx);
        if (injectant != null) {
            Object o = injectant.getObject();
            Element target = getTarget();
            if (injectant.isStatic()) {
                switch (target.getElementType()) {
                    case CONSTRUCTOR:
                        Constructor c = ((ConstructorElement) target).getConstructor();
                        assert c.getParameterTypes().length == 0;
                        break;
                    case METHOD:
                        Method m = ((MethodElement) target).getMethod();
                        assert m.getParameterTypes().length == 0;
                        assert Modifier.isStatic(m.getModifiers());
                        assert !m.getReturnType().equals(Void.TYPE);
                        break;
                    case FIELD:
                        Field f = ((FieldElement) target).getField();
                        assert Modifier.isStatic(f.getModifiers());
                        break;
                    default:
                        assert false;
                }
            } else {
                Class<?> type = getType(target);
                if (o == null) {
                    if (type.isPrimitive()) {
                        throw new BeanletWiringException(beanletName,
                                getMember(target),
                                "Primitive cannot be injected with null value.");
                    }
                } else {
                    final boolean match;
                    Class<?> injectantType = o.getClass();
                    if (type.isPrimitive()) {
                        if (Boolean.TYPE.equals(type)) {
                            match = injectantType.equals(Boolean.class);
                        } else if (Byte.TYPE.equals(type)) {
                            match = injectantType.equals(Byte.class);
                        } else if (Short.TYPE.equals(type)) {
                            match = injectantType.equals(Short.class);
                        } else if (Integer.TYPE.equals(type)) {
                            match = injectantType.equals(Integer.class);
                        } else if (Long.TYPE.equals(type)) {
                            match = injectantType.equals(Long.class);
                        } else if (Float.TYPE.equals(type)) {
                            match = injectantType.equals(Float.class);
                        } else if (Double.TYPE.equals(type)) {
                            match = injectantType.equals(Double.class);
                        } else {
                            assert false : type;
                            match = false;
                        }
                    } else {
                        match = type.isAssignableFrom(injectantType);
                    }
                    if (!match) {
                        throw new BeanletTypeMismatchException(beanletName,
                                getMember(target), type, injectantType);
                    }
                }
            }
        }
        return injectant;
    }
    
    private Class<?> getType(Element element) {
        final Class<?> type;
        switch (element.getElementType()) {
            case FIELD:
                type = ((FieldElement) element).getField().getType();
                break;
            case METHOD:
                Method method = ((MethodElement) element).getMethod();
                assert method.getParameterTypes().length > 0;
                type = method.getParameterTypes()[0];
                break;
            case CONSTRUCTOR:
                Constructor contructor = ((ConstructorElement) element).
                        getConstructor();
                assert contructor.getParameterTypes().length > 0;
                type = contructor.getParameterTypes()[0];
                break;
            case PARAMETER:
                int parameter = ((ParameterElement) element).getParameter();
                if (element instanceof MethodParameterElement) {
                    Method m = ((MethodParameterElement) element).getMethod();
                    assert parameter >= 0 && parameter < m.getParameterTypes().length;
                    type = m.getParameterTypes()[parameter];
                } else if (element instanceof ConstructorParameterElement) {
                    Constructor c = ((ConstructorParameterElement) element).getConstructor();
                    assert parameter >= 0 && parameter < c.getParameterTypes().length;
                    type = c.getParameterTypes()[parameter];
                } else {
                    assert false : element.getElementType();
                    type = null;
                }
                break;
            default:
                assert false;
                type = null;
        }
        return type;
    }

    /**
     * @return possibly {@code null}.
     */
    public Member getMember(Element element) {
        // PENDING: copied from AbstractDependencyInjectionFactory
        final Member member;
        switch (element.getElementType()) {
            case ANNOTATION_TYPE:
                member = null;
                break;
            case CONSTRUCTOR:
                member = ((ConstructorElement) element).
                        getConstructor();
                break;
            case FIELD:
                member = ((FieldElement) element).getField();
                break;
            case LOCAL_VARIABLE:
                member = null;
                break;
            case METHOD:
                member = ((MethodElement) element).getMethod();
                break;
            case PACKAGE:
                member = null;
                break;
            case PARAMETER:
                if (element instanceof ConstructorParameterElement) {
                    ConstructorParameterElement cpe = (ConstructorParameterElement) element;
                    member = cpe.getConstructor();
                } else if (element instanceof MethodParameterElement) {
                    MethodParameterElement mpe = (MethodParameterElement) element;
                    member = mpe.getMethod();
                } else {
                    member = null;
                }
                break;
            case TYPE:
                member = null;
                break;
            default:
                member = null;
                break;
        }
        return member;
    }
}
