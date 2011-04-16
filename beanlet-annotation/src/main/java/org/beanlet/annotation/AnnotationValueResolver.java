package org.beanlet.annotation;

import java.lang.reflect.Method;


public interface AnnotationValueResolver {
    
    /**
     * The value returned by {@code getValue} will be returned by the 
     * annotation when the specified {@code method} is called. If this 
     * method returns {@code null}, the the {@code AnnotationProxy} will
     * throw an {@code IncompleteAnnotationException}.
     */
    Object getValue(Method method, ClassLoader loader) throws Throwable;
}