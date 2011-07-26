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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.beanlet.BeanletApplicationContext;
import org.beanlet.BeanletFactory;
import org.beanlet.BeanletReference;
import org.beanlet.BeanletValidationException;
import org.beanlet.Event;
import org.beanlet.FactoryBeanlet;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.common.ConstructorInjectionImpl;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.DependencyInjectionFactory;
import org.beanlet.common.FactoryFieldInjectionImpl;
import org.beanlet.common.FactoryMethodInjectionImpl;
import org.beanlet.common.FieldInjectionImpl;
import org.beanlet.plugin.Injectant;
import org.beanlet.common.MethodInjectionImpl;
import org.beanlet.common.event.OperationEventImpl;
import org.jargo.ComponentContext;
import org.jargo.ConstructorInjection;
import org.jargo.InjectionFactory;
import org.jargo.ProxyGenerator;
import org.jargo.SetterInjection;

/**
 *
 * @author Leon van Zantvoort
 */
public final class InjectionFactoryImpl<T> implements InjectionFactory<T> {
    
    private final BeanletConfiguration<T> configuration;
    private final String beanletName;
    private final DependencyInjectionFactory factory;
    
    public InjectionFactoryImpl(BeanletConfiguration<T> configuration,
            DependencyInjectionFactory factory) {
        this.configuration = configuration;
        this.beanletName = configuration.getComponentName();
        this.factory = factory;
    }
    
    public ConstructorInjection<T> getConstructorInjection(Class<?> cls, 
            ComponentContext<T> ctx) {
        final ConstructorInjection<T> injection;
        if (configuration.getFactory() != null) {
            List<DependencyInjection> constructorInjections = factory.
                    getConstructorDependencyInjections(cls);
            if (!constructorInjections.isEmpty()) {
                throw new BeanletValidationException(beanletName,
                        "Beanlets configured to be created by another beanlet " +
                        "MUST NOT specify any constructor injection elements: " +
                        toElementString(constructorInjections) + ".");
            }
            BeanletApplicationContext bctx = BeanletApplicationContext.instance();
            Class<?> factoryType = bctx.getBeanletFactory(
                    configuration.getFactory()).getBeanletMetaData().getType();
            
            final List<DependencyInjection> injections;
            if (configuration.getFactoryMethod() == null) {
                injections = Collections.emptyList();
            } else {
                injections = factory.getFactoryDependencyInjections(
                        factoryType, configuration.getFactoryMethod());
            }
            injection = getFactoryInjection(injections, ctx);
        } else {
            List<DependencyInjection> injections = factory.
                    getConstructorDependencyInjections(cls);
            if (injections.isEmpty()) {
                injection = null;
            } else {
                injection = getConstructorInjection(injections, ctx);
            }
        }
        return injection;
    }
    
    public List<SetterInjection> getSetterInjections(
            Class<?> cls, ComponentContext<T> ctx) {
        List<DependencyInjection> injections = factory.
                getSetterDependencyInjections(cls);
        return getSetterInjections(injections, ctx);
    }

    private ConstructorInjection<T> getConstructorInjection(
            List<DependencyInjection> injections, ComponentContext<T> ctx) {
        final ConstructorInjection<T> injection;
        DependencyInjection first = injections.get(0);
        switch (first.getTarget().getElementType()) {
            case CONSTRUCTOR:
                if (injections.size() > 1) {
                    throw new BeanletValidationException(beanletName,
                            "Beanlets configured to be created by construction " +
                            "injection MAY specify at most one element: " +
                            toElementString(injections) + ".");
                }
                Constructor c = ((ConstructorElement) first.getTarget()).
                        getConstructor();
                if (c.getParameterTypes().length == 0) {
                    Injectant<?> injectant = first.getInjectant(ctx);   // Always request the injectant.
                    assert injectant.isStatic();
                    assert injectant.getObject() == null;
                    injection = new ConstructorInjectionImpl<T>(c);
                } else if (c.getParameterTypes().length == 1) {
                    Injectant<?> injectant = first.getInjectant(ctx);
                    if (injectant != null) {
                        injection = new ConstructorInjectionImpl<T>(c, 
                                injectant.getObject());
                    } else {
                        injection = null;
                    }
                } else {
                    throw new BeanletValidationException(beanletName,
                            "Constructor specifies more than one parameter: '" + c + "'.");
                }
                break;
            case METHOD:
                if (injections.size() > 1) {
                    throw new BeanletValidationException(beanletName,
                            "Found multiple constructor injections or factory elements: " +
                            toElementString(injections) + ".");
                }
                Method m = ((MethodElement) first.getTarget()).getMethod();
                if (m.getParameterTypes().length == 0) {
                    Injectant<?> injectant = first.getInjectant(ctx);   // Always request the injectant.
                    assert injectant.isStatic();
                    assert injectant.getObject() == null;
                    injection = new FactoryMethodInjectionImpl<T>(m);
                } else if (m.getParameterTypes().length == 1) {
                    Injectant<?> injectant = first.getInjectant(ctx);
                    if (injectant != null) {
                        injection = new FactoryMethodInjectionImpl<T>(m, 
                                injectant.getObject());
                    } else {
                        injection = null;
                    }
                } else {
                    throw new BeanletValidationException(beanletName,
                            "Factory method specifies more than one parameter: '" + m + "'.");
                }
                break;
            case PARAMETER:
                if (first.getTarget() instanceof ConstructorParameterElement) {
                    Constructor pc = ((ConstructorParameterElement) first.
                            getTarget()).getConstructor();
                    if (injections.size() != pc.getParameterTypes().length) {
                        throw new BeanletValidationException(beanletName,
                                "Incorrect count of constructor injections or factory elements: '" + pc + "'.");
                    }
                    Object[] args = new Object[injections.size()];
                    for (DependencyInjection i : injections) {
                        Element e = i.getTarget();
                        if (!(e instanceof ConstructorParameterElement)) {
                            throw new BeanletValidationException(beanletName,
                                    "Mixture of injectable elements found: '" + 
                                    first.getTarget() + "', '" + e + "'.");
                        }
                        if (!((ConstructorParameterElement) e).getConstructor().equals(pc)) {
                            throw new BeanletValidationException(beanletName,
                                    "Mixture of injectable elements found: '" + 
                                    first.getTarget() + "', '" + e + "'.");
                        }
                        int param = ((ConstructorParameterElement) e).getParameter();
                        Injectant<?> injectant = i.getInjectant(ctx);
                        args[param] = injectant == null ? null : injectant.getObject();
                    }
                    injection = new ConstructorInjectionImpl<T>(pc, args);
                } else {
                    Method mc = ((MethodParameterElement) first.getTarget()).
                            getMethod();
                    if (injections.size() != mc.getParameterTypes().length) {
                        throw new BeanletValidationException(beanletName,
                                "Incorrect count of method injection or factory elements: '" + mc + "'.");
                    }
                    Object[] args = new Object[injections.size()];
                    for (DependencyInjection i : injections) {
                        Element e = i.getTarget();
                        if (!(e instanceof MethodParameterElement)) {
                            throw new BeanletValidationException(beanletName,
                                    "Mixture of injectable elements found: '" + 
                                    first.getTarget() + "', '" + e + "'.");
                        }
                        if (!((MethodParameterElement) e).getMethod().equals(mc)) {
                            throw new BeanletValidationException(beanletName,
                                    "Mixture of injectable elements found: '" + 
                                    first.getTarget() + "', '" + e + "'.");
                        }
                        int param = ((MethodParameterElement) e).getParameter();
                        Injectant<?> injectant = i.getInjectant(ctx);
                        args[param] = injectant == null ? null : injectant.getObject();
                    }
                    injection = new FactoryMethodInjectionImpl<T>(mc, args);
                }
                break;
            case FIELD:
                if (injections.size() > 1) {
                    throw new BeanletValidationException(beanletName,
                            "Found multiple constructor injections or factory elements: " +
                            toElementString(injections) + ".");
                }
                Field f = ((FieldElement) first.getTarget()).getField();
                Injectant<?> injectant = first.getInjectant(ctx);   // Always request the injectant.
                assert injectant.isStatic();
                assert injectant.getObject() == null;
                injection = new FactoryFieldInjectionImpl<T>(f);
                break;
            default:
                assert false : first.getTarget();
                injection = null;
        }
        return injection;
    }
    
    private ConstructorInjection<T> getFactoryInjection(
            final List<DependencyInjection> injections, 
            final ComponentContext<T> ctx) {
        assert configuration.getFactory() != null;
        return new ConstructorInjection<T>() {
            public Object inject() {
                final Object object;
                if (configuration.getFactoryMethod() == null) {
                    assert injections.isEmpty();
                    object = BeanletApplicationContext.instance().
                            getBeanlet(configuration.getFactory());
                } else {
                    final Event event;
                    if (injections.isEmpty()) {
                        event = new OperationEventImpl(configuration.
                                getFactoryMethod(), new Class[0]);
                    } else {
                        DependencyInjection first = injections.get(0);
                        Element element = first.getTarget();
                        if (element instanceof MethodElement) {
                            if (injections.size() > 1) {
                                throw new BeanletValidationException(beanletName,
                                        "Found multiple injection elements: " +
                                        toElementString(injections) + ".");
                            }
                            Method m = ((MethodElement) element).getMethod();
                            if (m.getParameterTypes().length == 0) {
                                event = new OperationEventImpl(configuration.
                                        getFactoryMethod(),
                                        m.getParameterTypes());
                            } else if (m.getParameterTypes().length == 1) {
                                Injectant<?> i = first.getInjectant(ctx);
                                event = new OperationEventImpl(configuration.
                                        getFactoryMethod(),
                                        m.getParameterTypes(), i == null ? null : i.getObject());
                            } else {
                                throw new BeanletValidationException(beanletName,
                                        "Factory method specifies more than one parameter: '" + m + "'.");
                            }
                        } else if (element instanceof MethodParameterElement) {
                            Method m = ((MethodParameterElement) element).getMethod();
                            if (injections.size() != m.getParameterTypes().length) {
                                throw new BeanletValidationException(beanletName,
                                        "Not all factory method parameters support injection: '" + m + "'.");
                            }
                            Object[] args = new Object[injections.size()];
                            for (DependencyInjection i : injections) {
                                Element e = i.getTarget();
                                if (!(e instanceof MethodParameterElement)) {
                                    throw new BeanletValidationException(beanletName,
                                            "Mixture of injectable elements found: '" + 
                                            first.getTarget() + "', '" + e + "'.");
                                }
                                if (!((MethodParameterElement) e).getMethod().equals(m)) {
                                    throw new BeanletValidationException(beanletName,
                                            "Mixture of injectable elements found: '" + 
                                            first.getTarget() + "', '" + e + "'.");
                                }
                                int param = ((MethodParameterElement) e).getParameter();
                                Injectant<?> injectant = i.getInjectant(ctx);
                                args[param] = injectant == null ? null : injectant.getObject();
                            }
                            event = new OperationEventImpl(configuration.
                                    getFactoryMethod(), m.getParameterTypes(),
                                    args);
                        } else {
                            assert false;
                            event = null;
                        }
                    }
                    BeanletFactory factory = BeanletApplicationContext.
                            instance().getBeanletFactory(configuration.getFactory());
                    BeanletReference reference = factory.create();
                    if (!reference.isExecutable(event)) {
                        throw new BeanletValidationException(
                                beanletName,
                                "Factory method not found: '" + event + "'.");
                    } else {
                        Object beanlet = reference.execute(event);
                        while (beanlet instanceof FactoryBeanlet) {
                            beanlet = ((FactoryBeanlet) beanlet).getObject();
                        }
                        object = beanlet;
                    }
                }
                return object;
            }
            public T inject(ProxyGenerator<T> proxyGenerator) throws 
                    UnsupportedOperationException {
                throw new UnsupportedOperationException("Proxy generation not " +
                        "supported for objects retrieved from factory beanlet. " +
                        "Factory beanlet: '" + configuration.getFactory() + "'.");
            }
        };
    }
    
    private List<SetterInjection> getSetterInjections(
            List<DependencyInjection> injections, ComponentContext<T> ctx) {
        List<SetterInjection> list = new ArrayList<SetterInjection>();

        Set<Element> dupes = new HashSet<Element>();
        Map<Member, Map<Element, DependencyInjection>> memberMap = 
                new HashMap<Member, Map<Element, DependencyInjection>>();
        for (DependencyInjection injection : injections) {
            Element element = injection.getTarget();
            if (!dupes.add(element)) {
                throw new BeanletValidationException(beanletName,
                        "Element is target for multiple injections: '" +
                        element + "'.");
            }
            final Member member;
            if (element instanceof FieldElement) {
                member = ((FieldElement) element).getField();
                assert !memberMap.containsKey(member);
            } else if (element instanceof MethodElement) {
                member = ((MethodElement) element).getMethod();
                assert !memberMap.containsKey(member);
            } else if (element instanceof MethodParameterElement) {
                member = ((MethodParameterElement) element).getMethod();
            } else {
                assert false : element;
                continue;
            }
            Map<Element, DependencyInjection> m = memberMap.get(member);
            if (m == null) {
                m = new HashMap<Element, DependencyInjection>();
                memberMap.put(member, m);
            }
            m.put(element, injection);
        }
        for (Map.Entry<Member, Map<Element, DependencyInjection>> entry :
                memberMap.entrySet()) {
            Member member = entry.getKey();
            Map<Element, DependencyInjection> map = entry.getValue();
            assert map != null;
            if (member instanceof Field) {
                Field field = (Field) member;
                assert map.size() == 1;
                DependencyInjection injection = map.values().iterator().next();
                Injectant<?> injectant = injection.getInjectant(ctx);
                if (injectant != null) {
                    list.add(new FieldInjectionImpl(field, injectant.getObject()));
                }
            } else if (member instanceof Method) {
                Method method = (Method) member;
                assert !map.isEmpty();
                if (map.size() == 1) {
                    DependencyInjection injection = map.values().iterator().next();
                    if (method.getParameterTypes().length == 1) {
                        Injectant<?> injectant = injection.getInjectant(ctx);
                        if (injectant != null) {
                            list.add(new MethodInjectionImpl(method, injectant.getObject()));
                        }
                    } else {
                        throw new BeanletValidationException(beanletName,
                                "Method does not specify exactly one parameter: '" + 
                                method + "'.");
                    }
                } else {
                    if (map.size() != method.getParameterTypes().length) {
                        throw new BeanletValidationException(beanletName,
                                "Not all method parameters support injection: '" + 
                                method + "'.");
                    }
                    Object[] args = new Object[map.size()];
                    for (Map.Entry<Element, DependencyInjection> e : map.entrySet()) {
                        int param = ((MethodParameterElement) e.getKey()).getParameter();
                        Injectant<?> injectant = e.getValue().getInjectant(ctx);
                        args[param] = injectant == null ? null : injectant.getObject();
                    }
                    list.add(new MethodInjectionImpl(method, args));
                }
            } else {
                assert false : member;
            }
        }
        return Collections.unmodifiableList(list);
    }

    private String toElementString(List<DependencyInjection> injections) {
        Set<Element> elements = new HashSet<Element>();
        for (DependencyInjection injection : injections) {
            elements.add(injection.getTarget());
        }
        return String.valueOf(elements);
    }
}
