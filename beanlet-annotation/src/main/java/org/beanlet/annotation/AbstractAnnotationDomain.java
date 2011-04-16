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
package org.beanlet.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class AbstractAnnotationDomain implements AnnotationDomain {
    
    /**
     * Returns an annotation domain for the specified {@code classes}.
     */
    public static AnnotationDomain instance(Class... classes) {
        return new ClassAnnotationDomainImpl(classes);
    }
    
    private final List<ElementAnnotation> list;
    private final ConcurrentMap<Class, List<ElementAnnotation>> classSpecificList;
    private final Map<Class<? extends Element>, ElementAnnotationHolder<Element>> elementAnnotationMap;
    private final ConcurrentMap<Class, ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element>>> classSpecificElementAnnotationMap;
    private final Map<Element, List<Annotation>> declaredAnnotationMap;
    private final ConcurrentMap<TypeElement, List<Annotation>> typeAnnotationMap;
    private final Map<Class<? extends Annotation>, AnnotationDeclarationImpl>
            declarationMap;
    private final List<AnnotationDeclaration> declarationList;
    
    /**
     * Constructs an annotation domain for the specified {@code classes}.
     *
     * @throws IllegalArgumentException
     */
    public AbstractAnnotationDomain(List<ElementAnnotation> elementAnnotations) 
            throws NullPointerException, IllegalArgumentException {
        if (elementAnnotations == null) {
            throw new NullPointerException();
        }
        
        // Defensive copy.
        list = new ArrayList<ElementAnnotation>(elementAnnotations);
        classSpecificList = new ConcurrentHashMap<Class, List<ElementAnnotation>>();
        elementAnnotationMap = new HashMap<Class<? extends Element>, ElementAnnotationHolder<Element>>();
        classSpecificElementAnnotationMap = new ConcurrentHashMap<Class, ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element>>>();
        declaredAnnotationMap = new HashMap<Element, List<Annotation>>();
        typeAnnotationMap = new ConcurrentHashMap<TypeElement, List<Annotation>>();
        declarationMap = new HashMap<Class<? extends Annotation>, AnnotationDeclarationImpl>();
        declarationList = new ArrayList<AnnotationDeclaration>();
        
        Set<Object> dups = new HashSet<Object>();
        Map<Class<? extends Annotation>, List<ElementAnnotation<Element, Annotation>>> map =
                new HashMap<Class<? extends Annotation>, List<ElementAnnotation<Element, Annotation>>>();
        
        for (ElementAnnotation ea : elementAnnotations) {
            Class<? extends Element> e = ea.getElement().getClass();
            Class<? extends Annotation> t = ea.getAnnotation().annotationType();

            if (!dups.add(Arrays.asList(ea.getElement(), t))) {
                throw new IllegalArgumentException("duplicate element annotation: " + ea);
            }
            
            ElementAnnotationHolder<Element> holder = elementAnnotationMap.get(e);
            if (holder == null) {
                holder = new ElementAnnotationHolder<Element>();
                elementAnnotationMap.put(e, holder);
            }
            
            @SuppressWarnings("unchecked")
            ElementAnnotation<Element, Annotation> tmp = 
                    (ElementAnnotation<Element, Annotation>) ea;
            holder.elementAnnotations.add(tmp);
            
            List<Annotation> l1 = declaredAnnotationMap.get(ea.getElement());
            if (l1 == null) {
                l1 = new ArrayList<Annotation>();
                declaredAnnotationMap.put(ea.getElement(), l1);
            }
            l1.add(ea.getAnnotation());
            
            List<ElementAnnotation<Element, Annotation>> l2 = map.get(t);
            if (l2 == null) {
                l2 = new ArrayList<ElementAnnotation<Element, Annotation>>();
                map.put(t, l2);
            }
            l2.add(tmp);
        }
        for (Map.Entry<Class<? extends Annotation>, List<ElementAnnotation<Element, Annotation>>> entry : map.entrySet()) {
            @SuppressWarnings("unchecked")
            AnnotationDeclarationImpl d = 
                    new AnnotationDeclarationImpl(entry.getKey(), 
                    entry.getValue());
            declarationMap.put(entry.getKey(), d);
            declarationList.add(d);
        }
    }

    public List<AnnotationDeclaration> getDeclarations() {
        return Collections.unmodifiableList(declarationList);
    }
    
    public <T extends Annotation> AnnotationDeclaration<T> getDeclaration(
            Class<T> annotationClass) {
        @SuppressWarnings("unchecked")
        AnnotationDeclaration<T> declaration = (AnnotationDeclaration<T>) declarationMap.get(annotationClass);
        if (declaration == null) {
            declaration = new AnnotationDeclarationImpl<T>(annotationClass);
        }
        return declaration;
    }
    
    protected <E extends Element, T extends Annotation> ElementAnnotation<E, T> 
            getElementAnnotation(E element, Class<T> annotationClass) {
        @SuppressWarnings("unchecked")
        AnnotationDeclarationImpl<T> declaration = (AnnotationDeclarationImpl<T>) declarationMap.get(annotationClass);
        return declaration == null ? null : declaration.getElementAnnotation(element);
    }
    
    public List<ElementAnnotation> getElements() {
        return Collections.unmodifiableList(list);
    }
    
    public List<ElementAnnotation> getElements(Class<?> cls) {
        List<ElementAnnotation> list = classSpecificList.get(cls);
        if (list == null) {
            list = new ArrayList<ElementAnnotation>();
            for (ElementAnnotation ea : getElements()) {
                Element e = ea.getElement();
                if (e.isElementOf(cls) && !e.isOverridden(cls)) {
                    list.add(ea);
                }
            }
            classSpecificList.putIfAbsent(cls, list);
        }
        return Collections.unmodifiableList(list);
    }
    
    public <E extends Element> List<ElementAnnotation<E, Annotation>> getTypedElements(Class<E> elementClass) {
        @SuppressWarnings("unchecked")
        ElementAnnotationHolder<E> holder = (ElementAnnotationHolder<E>) elementAnnotationMap.get(elementClass);
        final List<ElementAnnotation<E, Annotation>> list;
        if (holder == null) {
            list = Collections.emptyList();
        } else {
            list = Collections.unmodifiableList(holder.elementAnnotations);
        }
        return list;
    }
    
    public <E extends Element> List<ElementAnnotation<E, Annotation>> getTypedElements(Class<E> elementClass, Class<?> cls) {
        ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element>> map = 
                classSpecificElementAnnotationMap.get(cls);
        if (map == null) {
            map = new ConcurrentHashMap<Class<? extends Element>, ElementAnnotationHolder<Element>>();
            classSpecificElementAnnotationMap.putIfAbsent(cls, map);
        }
        @SuppressWarnings("unchecked")
        ElementAnnotationHolder<Element> holder = map.get(elementClass);
        if (holder == null) {
            holder = new ElementAnnotationHolder<Element>();
            for (ElementAnnotation<E, Annotation> ea : getTypedElements(elementClass)) {
                @SuppressWarnings("unchecked")
                ElementAnnotation<Element, Annotation> tmp = (ElementAnnotation<Element, Annotation>) ea;
                Element e = tmp.getElement();
                if (e.isElementOf(cls) && !e.isOverridden(cls)) {
                    holder.elementAnnotations.add(tmp);
                }
            }
            map.put(elementClass, holder);
        }
        
        @SuppressWarnings("unchecked")
        ElementAnnotationHolder<E> h = (ElementAnnotationHolder<E>) holder;
        return Collections.unmodifiableList(h.elementAnnotations);
    }
    
    public List<Annotation> getAnnotations(Element element) {
        final List<Annotation> list;
        if (element instanceof TypeElement) {
            List<Annotation> tmp = typeAnnotationMap.get(element);
            if (tmp == null) {
                tmp = new ArrayList<Annotation>();
                Set<Class<? extends Annotation>> types = 
                        new HashSet<Class<? extends Annotation>>();
                Class<?> cls = ((TypeElement) element).getType();
                for (Annotation annotation : getDeclaredAnnotations(element)) {
                    types.add(annotation.annotationType());
                    tmp.add(annotation);
                }
                while ((cls = cls.getSuperclass()) != null) {
                    Element e = TypeElement.instance(cls);
                    for (Annotation annotation : getDeclaredAnnotations(e)) {
                        if (annotation.annotationType().isAnnotationPresent(Inherited.class) && 
                                types.add(annotation.annotationType())) {
                            tmp.add(annotation);
                        }
                    }
                }
                typeAnnotationMap.putIfAbsent((TypeElement) element, tmp);
            }
            list = Collections.unmodifiableList(tmp);
        } else {
            list = getDeclaredAnnotations(element);
        }
        return list;
    }
    
    public List<Annotation> getDeclaredAnnotations(Element element) {
        List<Annotation> list = declaredAnnotationMap.get(element);
        if (list == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(list);
        }
    }
    
    public String toString() {
        return String.valueOf(getElements());
    }
    
    private class ElementAnnotationHolder<E extends Element> {
        private final List<ElementAnnotation<E, Annotation>> elementAnnotations = 
                new ArrayList<ElementAnnotation<E, Annotation>>();
    }
}
