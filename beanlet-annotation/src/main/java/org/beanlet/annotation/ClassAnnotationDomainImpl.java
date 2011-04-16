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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Leon van Zantvoort
 */
class ClassAnnotationDomainImpl extends AbstractAnnotationDomain {
    
    /**
     * Constructs an annotation domain for the specified {@code classes}.
     */
    ClassAnnotationDomainImpl(Class<?>... classes) {
        super(getTypedElements(classes));
    }

    static List<ElementAnnotation> getTypedElements(Class<?>... classes) {
        List<ElementAnnotation> ea = 
                new ArrayList<ElementAnnotation>();
        Set<Class> dupes = new HashSet<Class>();
        Set<Package> packages = new HashSet<Package>();
        for (Class<?> cls : classes) {
            Class<?> tmp = cls;
            do {
                if (dupes.add(tmp)) {
                    ea.addAll(getTypeElementAnnotations(tmp));
                    ea.addAll(getFieldElementAnnotations(tmp));
                    ea.addAll(getMethodElementAnnotations(tmp));
                    ea.addAll(getConstructorElementAnnotations(tmp));
                    ea.addAll(getParameterElementAnnotations(tmp));
                    ea.addAll(getAnnotationTypeElementAnnotations(tmp));

                    Package pkg = cls.getPackage();
                    if (packages.add(pkg)) {
                        ea.addAll(getPackageElementAnnotations(pkg));
                    }
                }
            } while ((tmp = tmp.getSuperclass()) != null);
        }
        return ea;
    }
    
    static List<ElementAnnotation<TypeElement, Annotation>> getTypeElementAnnotations(Class<?> cls) {
        List<ElementAnnotation<TypeElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<TypeElement, Annotation>>();
        if (!Annotation.class.isAssignableFrom(cls)) {
            for (Annotation annotation : cls.getDeclaredAnnotations()) {
                ea.add(TypeElement.instance(cls).getElementAnnotation(annotation));
            }
        }
        return ea;
    }

    static List<ElementAnnotation<FieldElement, Annotation>> getFieldElementAnnotations(Class<?> cls) {
        List<ElementAnnotation<FieldElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<FieldElement, Annotation>>();
        for (Field field : cls.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                ea.add(FieldElement.instance(field).getElementAnnotation(annotation));
            }
        }
        return ea;
    }

    static List<ElementAnnotation<MethodElement, Annotation>> getMethodElementAnnotations(Class<?> cls) {
        List<ElementAnnotation<MethodElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<MethodElement, Annotation>>();
        for (Method method: cls.getDeclaredMethods()) {
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                ea.add(MethodElement.instance(method).getElementAnnotation(annotation));
            }
        }
        return ea;
    }

    static List<ElementAnnotation<ConstructorElement, Annotation>> getConstructorElementAnnotations(Class<?> cls) {
        List<ElementAnnotation<ConstructorElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<ConstructorElement, Annotation>>();
        for (Constructor constructor : cls.getDeclaredConstructors()) {
            for (Annotation annotation : constructor.getDeclaredAnnotations()) {
                ea.add(ConstructorElement.instance(constructor).getElementAnnotation(annotation));
            }
        }
        return ea;
    }

    static List<ElementAnnotation<ParameterElement, Annotation>> getParameterElementAnnotations(Class<?> cls) {
        List<ElementAnnotation<ParameterElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<ParameterElement, Annotation>>();
        for (Constructor constructor : cls.getDeclaredConstructors()) {
            Annotation[][] annotations = constructor.getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    ParameterElement pe = ConstructorParameterElement.instance(constructor, i);
                    @SuppressWarnings("unchecked")
                    ElementAnnotation<ParameterElement, Annotation> tmp = 
                            (ElementAnnotation<ParameterElement, Annotation>) 
                            pe.getElementAnnotation(annotation);
                    ea.add(tmp);
                }
            }
        }
        for (Method method: cls.getDeclaredMethods()) {
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    ParameterElement pe = MethodParameterElement.instance(method, i);
                    @SuppressWarnings("unchecked")
                    ElementAnnotation<ParameterElement, Annotation> tmp = 
                            (ElementAnnotation<ParameterElement, Annotation>) 
                            pe.getElementAnnotation(annotation);
                    ea.add(tmp);
                }
            }
        }
        return ea;
    }
    
    static List<ElementAnnotation<AnnotationTypeElement, Annotation>> getAnnotationTypeElementAnnotations(Class<?> cls) {
        List<ElementAnnotation<AnnotationTypeElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<AnnotationTypeElement, Annotation>>();
        if (Annotation.class.isAssignableFrom(cls)) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) cls;
            for (Annotation annotation : cls.getDeclaredAnnotations()) {
                ea.add(AnnotationTypeElement.
                        instance(annotationClass).getElementAnnotation(annotation));
            }
        }
        return ea;
    }

    static List<ElementAnnotation<PackageElement, Annotation>> getPackageElementAnnotations(Package pkg) {
        List<ElementAnnotation<PackageElement, Annotation>> ea = 
                new ArrayList<ElementAnnotation<PackageElement, Annotation>>();
        for (Annotation annotation : pkg.getDeclaredAnnotations()) {
            ea.add(PackageElement.instance(pkg).getElementAnnotation(annotation));
        }
        return ea;
    }
}
