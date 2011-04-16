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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.beanlet.MetaData;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;

/**
 *
 * @author Leon van Zantvoort
 */
public abstract class AbstractMetaData implements MetaData {

    private final AnnotatedElement annotatedElement;
    private final String description;
    
    public AbstractMetaData(AnnotatedElement annotatedElement, String description) {
        this.annotatedElement = annotatedElement;
        this.description = description;
    }

    public Annotation[] getDeclaredAnnotations() {
        return annotatedElement.getAnnotations();
    }

    public Annotation[] getAnnotations() {
        return annotatedElement.getAnnotations();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annotatedElement.isAnnotationPresent(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annotatedElement.getAnnotation(annotationClass);
    }
    
    public String getDescription() {
        return description;
    }

    public static AnnotatedElement getAnnotatedElement(final AnnotationDomain domain,
            final Element element) {
        return new AnnotatedElement() {
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
                return domain.getDeclaration(annotationClass).isAnnotationPresent(element);
            }

            public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
                return domain.getDeclaration(annotationClass).getAnnotation(element);
            }

            public Annotation[] getAnnotations() {
                return domain.getAnnotations(element).toArray(new Annotation[0]);
            }

            public Annotation[] getDeclaredAnnotations() {
                return domain.getDeclaredAnnotations(element).toArray(new Annotation[0]);
            }
        };
    }
    
    public static AnnotatedElement getAnnotatedElement(
            List<? extends Annotation> annotations) {
        final Map<Class, Annotation> annotationMap = 
                new HashMap<Class, Annotation>();
        for (Annotation a : annotations) {
            annotationMap.put(a.annotationType(), a);
        }
        return new AnnotatedElement() {
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
                if (annotationClass == null) {
                    throw new NullPointerException();
                }
                return getAnnotation(annotationClass) != null;
            }

            public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
                if (annotationClass == null) {
                    throw new NullPointerException();
                }

                @SuppressWarnings("unchecked")
                A annotation = (A) annotationMap.get(annotationClass);

                return annotation;
            }

            public Annotation[] getAnnotations() {
                return annotationMap.values().toArray(
                        new Annotation[annotationMap.size()]);
            }
            
            public Annotation[] getDeclaredAnnotations() {
                return annotationMap.values().toArray(
                        new Annotation[annotationMap.size()]);
            }
        };
    }
}