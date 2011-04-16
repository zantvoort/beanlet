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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Leon van Zantvoort
 */
class AnnotationDeclarationImpl<T extends Annotation> implements 
        AnnotationDeclaration<T> {

    private final Class<T> annotationType;
    private final List<ElementAnnotation<Element, T>> list;
    private final ConcurrentMap<Class, List<ElementAnnotation<Element, T>>> classSpecificList;
    private final Map<Class<? extends Element>, ElementAnnotationHolder<Element, T>> elementAnnotationMap;
    private final ConcurrentMap<Class, ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element, T>>> classSpecificElementAnnotationMap;
    private final Map<Element, ElementAnnotation<Element, T>> annotations;
    
    public AnnotationDeclarationImpl(Class<T> annotationType) {
        this.annotationType = annotationType;
        this.list = Collections.emptyList();
        this.classSpecificList = new ConcurrentHashMap<Class, List<ElementAnnotation<Element, T>>>();
        this.elementAnnotationMap = Collections.emptyMap();
        this.classSpecificElementAnnotationMap = new ConcurrentHashMap<Class, ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element, T>>>();
        this.annotations = Collections.emptyMap();
    }
    
    public AnnotationDeclarationImpl(Class<T> annotationType,
            List<ElementAnnotation<Element, T>> elementAnnotationList) {
        this.annotationType = annotationType;
        // Defensive copy.
        this.list = 
                new ArrayList<ElementAnnotation<Element, T>>(elementAnnotationList);
        this.classSpecificList = new ConcurrentHashMap<Class, List<ElementAnnotation<Element, T>>>();
        this.elementAnnotationMap = new HashMap<Class<? extends Element>, ElementAnnotationHolder<Element, T>>();
        this.classSpecificElementAnnotationMap = new ConcurrentHashMap<Class, ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element, T>>>();
        this.annotations = new HashMap<Element, ElementAnnotation<Element, T>>();
        
        for (ElementAnnotation<Element, T> ea : elementAnnotationList) {
            ElementAnnotationHolder<Element, T> holder = elementAnnotationMap.get(ea.getElement().getClass());
            if (holder == null) {
                holder = new ElementAnnotationHolder<Element, T>();
                elementAnnotationMap.put(ea.getElement().getClass(), holder);
            }
            holder.elementAnnotations.add(ea);
            annotations.put(ea.getElement(), ea);
        }
    }

    public List<ElementAnnotation<Element, T>> getElements() {
       return Collections.unmodifiableList(list);
    }

    public List<ElementAnnotation<Element, T>> getElements(Class<?> cls) {
        List<ElementAnnotation<Element, T>> list = classSpecificList.get(cls);
        if (list == null) {
            list = new ArrayList<ElementAnnotation<Element, T>>();
            for (ElementAnnotation<Element, T> ea : getElements()) {
                Element e = ea.getElement();
                if (e.isElementOf(cls) && !e.isOverridden(cls)) {
                    list.add(ea);
                }
            }
            classSpecificList.putIfAbsent(cls, list);
        }
        return Collections.unmodifiableList(list);
    }

    public <E extends Element> List<ElementAnnotation<E, T>> getTypedElements(Class<E> elementClass) {
        @SuppressWarnings("unchecked")
        ElementAnnotationHolder<E, T> holder = (ElementAnnotationHolder<E, T>) elementAnnotationMap.get(elementClass);
        final List<ElementAnnotation<E, T>> list;
        if (holder == null) {
            list = Collections.emptyList();
        } else {
            list = Collections.unmodifiableList(holder.elementAnnotations);
        }
        return list;
    }
    
    public <E extends Element> List<ElementAnnotation<E, T>> getTypedElements(Class<E> elementClass, Class<?> cls) {
        ConcurrentMap<Class<? extends Element>, ElementAnnotationHolder<Element, T>> map = 
                classSpecificElementAnnotationMap.get(cls);
        if (map == null) {
            map = new ConcurrentHashMap<Class<? extends Element>, ElementAnnotationHolder<Element, T>>();
            classSpecificElementAnnotationMap.putIfAbsent(cls, map);
        }
        
        @SuppressWarnings("unchecked")
        ElementAnnotationHolder<Element, T> holder = map.get(elementClass);
        if (holder == null) {
            holder = new ElementAnnotationHolder<Element, T>();
            for (ElementAnnotation<E, T> ea : getTypedElements(elementClass)) {
                @SuppressWarnings("unchecked")
                ElementAnnotation<Element, T> tmp = (ElementAnnotation<Element, T>) ea;
                Element e = tmp.getElement();
                if (e.isElementOf(cls) && !e.isOverridden(cls)) {
                    holder.elementAnnotations.add(tmp);
                }
            }
            map.put(elementClass, holder);
        }
        
        @SuppressWarnings("unchecked")
        ElementAnnotationHolder<E, T> h = (ElementAnnotationHolder<E, T>) holder;
        return Collections.unmodifiableList(h.elementAnnotations);
    }
    
    public boolean isAnnotationPresent(Element element) {
        return getAnnotation(element) != null;
    }
    
    protected <E extends Element> ElementAnnotation<E, T> getElementAnnotation(E element) {
        @SuppressWarnings("unchecked")
        ElementAnnotation<E, T> ea = (ElementAnnotation<E, T>) annotations.get(element);
        return ea;
    }
    
    public T getAnnotation(Element element) {
        if (element instanceof TypeElement && 
                annotationType.isAnnotationPresent(Inherited.class)) {
            Class<?> cls = ((TypeElement) element).getType();
            do {
                T annotation = getDeclaredAnnotation(TypeElement.instance(cls));
                if (annotation != null) {
                    return annotation;
                }
            } while ((cls = cls.getSuperclass()) != null);
            return null;
        } else {
            return getDeclaredAnnotation(element);
        }
    }

    public T getDeclaredAnnotation(Element element) {
        ElementAnnotation<Element, T> ea = getElementAnnotation(element);
        return ea == null ? null : ea.getAnnotation();
    }
    
    public Class<T> annotationType() {
        return annotationType;
    }

    public String toString() {
        return "AnnotationDeclaration" + list;
    }

    private class ElementAnnotationHolder<E extends Element, V extends Annotation> {
        private final List<ElementAnnotation<E, V>> elementAnnotations = 
                new ArrayList<ElementAnnotation<E, V>>();
    }
}
