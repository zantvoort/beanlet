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

import org.beanlet.plugin.Injectant;
import org.beanlet.plugin.DependencyInjection;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.DependencyInjectionFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.beanlet.BeanletWiringException;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.AnnotationTypeElement;
import org.beanlet.annotation.ConstructorElement;
import org.beanlet.annotation.ConstructorParameterElement;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.ElementAnnotation;
import org.beanlet.annotation.FieldElement;
import org.beanlet.annotation.MethodElement;
import org.beanlet.annotation.MethodParameterElement;
import org.beanlet.annotation.TypeElement;
import org.jargo.ComponentContext;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class AbstractDependencyInjectionFactory<T extends Annotation>
        implements DependencyInjectionFactory {
    
    private final String beanletName;
    private final BeanletConfiguration<?> configuration;
    private final AnnotationDomain domain;
    
    public AbstractDependencyInjectionFactory(BeanletConfiguration<?> configuration) {
        this.configuration = configuration;
        this.beanletName = configuration.getComponentName();
        this.domain = configuration.getAnnotationDomain();
    }
    
    public abstract Class<T> annotationType();
    
    public abstract boolean isSupported(
            ElementAnnotation<? extends Element, T> ea);
    
    public abstract boolean isOptional(
            ElementAnnotation<? extends Element, T> ea);
    
    public abstract Set<String> getDependencies(
            ElementAnnotation<? extends Element, T> ea) 
            throws BeanletWiringException;

    public abstract Injectant getInjectant(
            ElementAnnotation<? extends Element, T> ea, ComponentContext<?> ctx)
            throws BeanletWiringException;
    
    /**
     * Extracts name from specified {@code annotation}, possible an empty
     * string.
     */
    public String getName(T annotation) {
        return "";
    }
    
    /**
     * Extracts the type from the specified {@code annotation}, returns 
     * {@code Object.class} by default.
     */
    public Class<?> getType(T annotation) {
        return Object.class;
    }
    
    /**
     * @return possibly an empty string.
     */
    public String getName(ElementAnnotation<? extends Element, T> ea) {
        String name = getName(ea.getAnnotation());
        if (name.length() == 0) {
            name = getName(ea.getElement());
        }
        assert name != null;
        return name;
    }
    
    public Class<?> getType(ElementAnnotation<? extends Element, T> ea) {
        Class<?> type = getType(ea.getAnnotation());
        if (Object.class.equals(type)) {
            type = getType(ea.getElement());
        }
        assert type != null;
        return type;
    }
    
    /**
     * @return possibly an empty string.
     */
    public String getName(Element element) {
        final String name;
        switch (element.getElementType()) {
            case ANNOTATION_TYPE:
                name = "";
                break;
            case CONSTRUCTOR:
                name = "";
                break;
            case FIELD:
                name = ((FieldElement) element).getField().getName();
                break;
            case LOCAL_VARIABLE:
                name = "";
                break;
            case METHOD:
                Method method = ((MethodElement) element).getMethod();
                if (method.getName().startsWith("set")) {
                    name = Character.toLowerCase(method.getName().charAt(3)) +
                            method.getName().substring(4);
                } else {
                    name = method.getName();
                }
                break;
            case PACKAGE:
                name = "";
                break;
            case PARAMETER:
                name = "";
                break;
            case TYPE:
                name = "";
                break;
            default:
                name = "";
                break;
        }
        return name;
    }
    
    /**
     * @return possibly {@code Object.class}.
     */
    public Class<?> getType(Element element) {
        final Class<?> type;
        switch (element.getElementType()) {
            case ANNOTATION_TYPE:
                type = ((AnnotationTypeElement) element).getAnnotationType();
                break;
            case CONSTRUCTOR:
                Constructor constructor = ((ConstructorElement) element).
                        getConstructor();
                if (constructor.getParameterTypes().length == 1) {
                    type = constructor.getParameterTypes()[0];
                } else {
                    type = Object.class;
                }
                break;
            case FIELD:
                type = ((FieldElement) element).getField().getType();
                break;
            case LOCAL_VARIABLE:
                type = Object.class;
                break;
            case METHOD:
                Method method = ((MethodElement) element).getMethod();
                if (method.getParameterTypes().length == 1) {
                    type = method.getParameterTypes()[0];
                } else {
                    type = Object.class;
                }
                break;
            case PACKAGE:
                type = Object.class;
                break;
            case PARAMETER:
                if (element instanceof ConstructorParameterElement) {
                    ConstructorParameterElement cpe = (ConstructorParameterElement) element;
                    type = cpe.getConstructor().getParameterTypes()[cpe.getParameter()];
                } else if (element instanceof MethodParameterElement) {
                    MethodParameterElement mpe = (MethodParameterElement) element;
                    type = mpe.getMethod().getParameterTypes()[mpe.getParameter()];
                } else {
                    type = Object.class;
                }
                break;
            case TYPE:
                type = ((TypeElement) element).getType();
                break;
            default:
                type = Object.class;
                break;
        }
        return type;
    }
    
    public List<DependencyInjection> getConstructorDependencyInjections(
            Class<?> cls) {
        List<DependencyInjection> list = new ArrayList<DependencyInjection>();
        AnnotationDeclaration<T> ad = domain.getDeclaration(annotationType());
        for (final ElementAnnotation<ConstructorElement, T> ea :
                ad.getTypedElements(ConstructorElement.class)) {
            if (ea.getElement().isElementOfSubclass(cls)) {
                if (isSupported(ea)) {
                    list.add(new DependencyInjectionAdapter(ea, 
                            new DependencyInjection() {
                        public Element getTarget() {
                            return ea.getElement();
                        }
                        public Set<String> getDependencies() {
                            return AbstractDependencyInjectionFactory.
                                    this.getDependencies(ea);
                        }
                        public boolean isOptional() {
                            return AbstractDependencyInjectionFactory.
                                    this.isOptional(ea);
                        }
                        public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                            return AbstractDependencyInjectionFactory.
                                    this.getInjectant(ea, ctx);
                        }
                    }));
                }
            }
        }
        for (final ElementAnnotation<ConstructorParameterElement, T> ea :
                ad.getTypedElements(ConstructorParameterElement.class)) {
            if (ea.getElement().isElementOfSubclass(cls)) {
                if (isSupported(ea)) {
                    list.add(new DependencyInjectionAdapter(ea,
                            new DependencyInjection() {
                        public Element getTarget() {
                            return ea.getElement();
                        }
                        public Set<String> getDependencies() {
                            return AbstractDependencyInjectionFactory.
                                    this.getDependencies(ea);
                        }
                        public boolean isOptional() {
                            return AbstractDependencyInjectionFactory.
                                    this.isOptional(ea);
                        }
                        public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                            return AbstractDependencyInjectionFactory.
                                    this.getInjectant(ea, ctx);
                        }
                    }));
                }
            }
        }
                
        if (configuration.getType().isAssignableFrom(cls)) {
            // Factory (static) method / (static) field injection only
            // supported for beanlet type (or any sub class), not for
            // interceptors.

            for (final ElementAnnotation<MethodElement, T> ea :
                    ad.getTypedElements(MethodElement.class)) {
                Method method = ea.getElement().getMethod();
                if (Modifier.isStatic(method.getModifiers()) &&
                        !method.getReturnType().equals(Void.TYPE)) {
                    // No isPartOf check, because factory method can return any type.
                    if (isSupported(ea)) {
                        list.add(new DependencyInjectionAdapter(ea,
                                new DependencyInjection() {
                            public Element getTarget() {
                                return ea.getElement();
                            }
                            public Set<String> getDependencies() {
                                return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                            }
                            public boolean isOptional() {
                                return AbstractDependencyInjectionFactory.this.isOptional(ea);
                            }
                            public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                                return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                            }
                        }));
                    }
                }
            }
            for (final ElementAnnotation<MethodParameterElement, T> ea :
                    ad.getTypedElements(MethodParameterElement.class)) {
                Method method = ea.getElement().getMethod();
                if (Modifier.isStatic(method.getModifiers()) &&
                        !method.getReturnType().equals(Void.TYPE)) {
                    // No isPartOf check, because factory method can return any type.
                    if (isSupported(ea)) {
                        list.add(new DependencyInjectionAdapter(ea,
                                new DependencyInjection() {
                            public Element getTarget() {
                                return ea.getElement();
                            }
                            public Set<String> getDependencies() {
                                return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                            }
                            public boolean isOptional() {
                                return AbstractDependencyInjectionFactory.this.isOptional(ea);
                            }
                            public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                                return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                            }
                        }));
                    }
                }
            }
            for (final ElementAnnotation<FieldElement, T> ea :
                    ad.getTypedElements(FieldElement.class)) {
                Field field = ea.getElement().getField();
                if (Modifier.isStatic(field.getModifiers())) {
                    // No isPartOf check, because factory field can return any type.
                    if (isSupported(ea)) {
                        list.add(new DependencyInjection() {
                            public Element getTarget() {
                                return ea.getElement();
                            }
                            public Set<String> getDependencies() {
                                return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                            }
                            public boolean isOptional() {
                                return AbstractDependencyInjectionFactory.this.isOptional(ea);
                            }
                            public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                                return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                            }
                        });
                    }
                }
            }
        }
        return Collections.unmodifiableList(list);
    }
    
    public List<DependencyInjection> getSetterDependencyInjections(
            Class<?> cls) {
        List<DependencyInjection> list = new ArrayList<DependencyInjection>();
        AnnotationDeclaration<T> ad = domain.getDeclaration(annotationType());
        for (final ElementAnnotation<FieldElement, T> ea :
                ad.getTypedElements(FieldElement.class, cls)) {
            if (Modifier.isStatic(ea.getElement().getField().getModifiers())) {
                continue;
            }
            if (isSupported(ea)) {
                list.add(new DependencyInjectionAdapter(ea,
                        new DependencyInjection() {
                    public Element getTarget() {
                        return ea.getElement();
                    }
                    public Set<String> getDependencies() {
                        return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                    }
                    public boolean isOptional() {
                        return AbstractDependencyInjectionFactory.this.isOptional(ea);
                    }
                    public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                        return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                    }
                }));
            }
        }
        for (final ElementAnnotation<MethodElement, T> ea :
                ad.getTypedElements(MethodElement.class, cls)) {
            if (Modifier.isStatic(ea.getElement().getMethod().getModifiers())) {
                continue;
            }
            if (isSupported(ea)) {
                list.add(new DependencyInjectionAdapter(ea,
                        new DependencyInjection() {
                    public Element getTarget() {
                        return ea.getElement();
                    }
                    public Set<String> getDependencies() {
                        return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                    }
                    public boolean isOptional() {
                        return AbstractDependencyInjectionFactory.this.isOptional(ea);
                    }
                    public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                        return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                    }
                }));
            }
        }
        for (final ElementAnnotation<MethodParameterElement, T> ea :
                ad.getTypedElements(MethodParameterElement.class, cls)) {
            if (Modifier.isStatic(ea.getElement().getMethod().getModifiers())) {
                continue;
            }
            if (isSupported(ea)) {
                list.add(new DependencyInjectionAdapter(ea,
                        new DependencyInjection() {
                    public Element getTarget() {
                        return ea.getElement();
                    }
                    public Set<String> getDependencies() {
                        return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                    }
                    public boolean isOptional() {
                        return AbstractDependencyInjectionFactory.this.isOptional(ea);
                    }
                    public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                        return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                    }
                }));
            }
        }
        return Collections.unmodifiableList(list);
    }
    
    public List<DependencyInjection> getFactoryDependencyInjections(
            Class<?> cls, String factoryMethod) {
        List<DependencyInjection> list = new ArrayList<DependencyInjection>();
        AnnotationDeclaration<T> ad = domain.getDeclaration(annotationType());
        for (final ElementAnnotation<MethodElement, T> ea :
                ad.getTypedElements(MethodElement.class, cls)) {
            Method method = ea.getElement().getMethod();
            if (!Modifier.isStatic(method.getModifiers()) &&
                    !method.getReturnType().equals(Void.TYPE) &&
                    method.getName().equals(factoryMethod)) {
                if (isSupported(ea)) {
                    list.add(new DependencyInjectionAdapter(ea,
                            new DependencyInjection() {
                        public Element getTarget() {
                            return ea.getElement();
                        }
                        public Set<String> getDependencies() {
                            return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                        }
                        public boolean isOptional() {
                            return AbstractDependencyInjectionFactory.this.isOptional(ea);
                        }
                        public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                            return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                        }
                    }));
                }
            }
        }
        for (final ElementAnnotation<MethodParameterElement, T> ea :
                ad.getTypedElements(MethodParameterElement.class, cls)) {
            Method method = ea.getElement().getMethod();
            if (!Modifier.isStatic(method.getModifiers()) &&
                    !method.getReturnType().equals(Void.TYPE) &&
                    method.getName().equals(factoryMethod)) {
                if (isSupported(ea)) {
                    list.add(new DependencyInjectionAdapter(ea,
                            new DependencyInjection() {
                        public Element getTarget() {
                            return ea.getElement();
                        }
                        public Set<String> getDependencies() {
                            return AbstractDependencyInjectionFactory.this.getDependencies(ea);
                        }
                        public boolean isOptional() {
                            return AbstractDependencyInjectionFactory.this.isOptional(ea);
                        }
                        public Injectant<?> getInjectant(ComponentContext<?> ctx) {
                            return AbstractDependencyInjectionFactory.this.getInjectant(ea, ctx);
                        }
                    }));
                }
            }
        }
        return Collections.unmodifiableList(list);
    }
    
    private class DependencyInjectionAdapter implements DependencyInjection {
        
        private final DependencyInjection target;
        private final ElementAnnotation<? extends Element, T> ea;

        public DependencyInjectionAdapter(
                ElementAnnotation<? extends Element, T> ea, 
                DependencyInjection target) {
            this.ea = ea;
            this.target = target;
        }
        
        public Element getTarget() {
            return target.getTarget();
        }
        
        public boolean isOptional() {
            return target.isOptional();
        }
        
        public Set<String> getDependencies() throws BeanletWiringException {
            try {
                return target.getDependencies();
            } catch (Exception e) {
                throw wrapException(e);
            }
        }
        
        public Injectant<?> getInjectant(ComponentContext<?> ctx) throws 
                BeanletWiringException {
            try {
                final Injectant injectant = target.getInjectant(ctx);
                if (injectant == null) {
                    return null;
                } else {
                    return new Injectant<Object>() {
                        public boolean isCacheable() {
                            return injectant.isCacheable();
                        }
                        public boolean isStatic() {
                            return injectant.isStatic();
                        }
                        public Object getObject() {
                            try {
                                return injectant.getObject();
                            } catch (Exception e) {
                                // Although getObject should not throw any exceptions, catch them anyway.
                                throw wrapException(e);
                            }
                        }
                    };
                }
            } catch (Exception e) {
                throw wrapException(e);
            }
        }
        
        public BeanletWiringException wrapException(Exception e) {
            try {
                throw e;
            } catch (BeanletWiringException e2) {
                return e2;
            } catch (Exception e2) {
                return new BeanletWiringException(beanletName,
                        ea.getElement().getMember(), e2);
            }
        }
    }
}
