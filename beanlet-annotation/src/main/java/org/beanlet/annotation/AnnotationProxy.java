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
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Leon van Zantvoort
 */
public class AnnotationProxy<T extends Annotation> {

    private static final Method HASH_CODE;
    private static final Method EQUALS;
    private static final Method TO_STRING;
    private static final Method ANNOTATION_TYPE;

    static {
        try {
            Class<?> type = Annotation.class;
            HASH_CODE = Object.class.getMethod("hashCode");
            EQUALS = Object.class.getMethod("equals", Object.class);
            TO_STRING = Object.class.getMethod("toString");
            ANNOTATION_TYPE = type.getMethod("annotationType");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private final Class<T> annotationType;
    private final ClassLoader loader;
    private final AnnotationValueResolver resolver;
    private final ConcurrentMap<Method, Object> data;
    
    private boolean initialized;
    
    public static <T extends Annotation> T newProxyInstance(Class<T> annotationType) {
        return newProxyInstance(annotationType, 
                annotationType.getClassLoader(), null);
    }
    
    public static <T extends Annotation> T newProxyInstance(
            Class<T> annotationType, ClassLoader loader, 
            AnnotationValueResolver resolver) {
        return newProxyInstance(annotationType, loader, resolver, true);
    }
    
    public static <T extends Annotation> T newProxyInstance(
            Class<T> annotationType, ClassLoader loader, 
            AnnotationValueResolver resolver, boolean init) {
        final AnnotationProxy<T> p = new AnnotationProxy<T>(annotationType, loader,
                resolver);
        InvocationHandler h = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return p.invoke(proxy, method, args);
            }
        };
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(loader, new Class[]{annotationType}, h);
        if (init) {
            // Forces the annotation to fully initialize:
            proxy.toString();
        }
        return proxy;
    }
    
    protected AnnotationProxy(Class<T> annotationType, ClassLoader loader,
            AnnotationValueResolver resolver) {
        this.annotationType = annotationType;
        this.loader = loader;
        this.resolver = resolver;
        this.data = new ConcurrentHashMap<Method, Object>();
    }

    private synchronized void init() {
        if (!initialized) {
            Method[] methods = annotationType.getDeclaredMethods();
            if (data.size() < methods.length) {
                for (Method method : methods) {
                    init(method);
                }
            }
            initialized = true;
        }
    }
    
    public Class<T> annotationType() {
        return annotationType;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }
    
    public Object getValue(Method method) throws Throwable {
        final Object o;
        if (resolver != null) {
            Object tmp = resolver.getValue(method, getClassLoader());
            if (tmp == null) {
                IncompleteAnnotationException e = new 
                        IncompleteAnnotationException(annotationType(), 
                        method.getName());
                e.initCause(new NullPointerException(
                        "Failed to resolve annotation " +
                        "value: " + resolver.getClass().getName()));
                throw e;
            }
            o = tmp;
        } else {
            o = method.getDefaultValue();
        }
        return o;
    }
    
    private void init(Method method) {
        if (!data.containsKey(method)) {
            try {
                Object o = getValue(method);
                if (o == null) {
                    throw new IncompleteAnnotationException(annotationType,
                            method.getName());
                }
                final Class<?> returnType;
                Class<?> tmp = method.getReturnType();
                if (method.getReturnType().isPrimitive()) {
                    if (Boolean.TYPE.equals(tmp)) {
                        returnType = Boolean.class;
                    } else if (Byte.TYPE.equals(tmp)) {
                        returnType = Byte.class;
                    } else if (Short.TYPE.equals(tmp)) {
                        returnType = Short.class;
                    } else if (Integer.TYPE.equals(tmp)) {
                        returnType = Integer.class;
                    } else if (Long.TYPE.equals(tmp)) {
                        returnType = Long.class;
                    } else if (Float.TYPE.equals(tmp)) {
                        returnType = Float.class;
                    } else if (Double.TYPE.equals(tmp)) {
                        returnType = Double.class;
                    } else {
                        throw new AssertionError(tmp);
                    }
                } else {
                    returnType = tmp;
                }
                // Pending: check generic type also.
                if (!returnType.isAssignableFrom(o.getClass())) {
                    throw new AnnotationTypeMismatchException(method,
                            o.getClass().getName());
                }
                data.putIfAbsent(method, o);
            } catch (RuntimeException e) {
                throw e;
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                assert false : t;
                throw new RuntimeException(t);
            }
        }
    }
    
    public int hashCode() {
        init();
        int hashCode = 0;
        for (Map.Entry<Method, Object> entry : data.entrySet()) {
            int tmp = 127 * entry.getKey().getName().hashCode();
            Object v = entry.getValue();
            assert v != null;
            if (v.getClass().isArray()) {
                Class<?> type = v.getClass().getComponentType();
                if (type.isPrimitive()) {
                    if (type.equals(Boolean.TYPE)) {
                        tmp ^= Arrays.hashCode((boolean[]) v);
                    } else if (type.equals(Byte.TYPE)) {
                        tmp ^= Arrays.hashCode((byte[]) v);
                    } else if (type.equals(Character.TYPE)) {
                        tmp ^= Arrays.hashCode((char[]) v);
                    } else if (type.equals(Double.TYPE)) {
                        tmp ^= Arrays.hashCode((double[]) v);
                    } else if (type.equals(Float.TYPE)) {
                        tmp ^= Arrays.hashCode((float[]) v);
                    } else if (type.equals(Integer.TYPE)) {
                        tmp ^= Arrays.hashCode((int[]) v);
                    } else if (type.equals(Long.TYPE)) {
                        tmp ^= Arrays.hashCode((long[]) v);
                    } else if (type.equals(Short.TYPE)) {
                        tmp ^= Arrays.hashCode((short[]) v);
                    } else {
                        assert false : type;
                    }
                } else {
                    tmp ^= Arrays.hashCode((Object[]) v);
                }
            } else {
                tmp ^= v.hashCode();
            }
            hashCode += tmp;
        }
        return hashCode;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Annotation) {
            if (!annotationType().equals(((Annotation) obj).annotationType())) {
                return false;
            }
            init();
            for (Map.Entry<Method, Object> entry : data.entrySet()) {
                try {
                    try {
                        Object v1 = entry.getValue();
                        assert v1 != null;
                        Object v2 = entry.getKey().invoke(obj);
                        assert v2 != null;
                        if (v1.getClass().isArray()) {
                            Class<?> type = v1.getClass().getComponentType();
                            final boolean equals;
                            if (type.isPrimitive()) {
                                if (type.equals(Boolean.TYPE)) {
                                    equals = Arrays.equals((boolean[]) v1, (boolean[]) v2);
                                } else if (type.equals(Byte.TYPE)) {
                                    equals = Arrays.equals((byte[]) v1, (byte[]) v2);
                                } else if (type.equals(Character.TYPE)) {
                                    equals = Arrays.equals((char[]) v1, (char[]) v2);
                                } else if (type.equals(Double.TYPE)) {
                                    equals = Arrays.equals((double[]) v1, (double[]) v2);
                                } else if (type.equals(Float.TYPE)) {
                                    equals = Arrays.equals((float[]) v1, (float[]) v2);
                                } else if (type.equals(Integer.TYPE)) {
                                    equals = Arrays.equals((int[]) v1, (int[]) v2);
                                } else if (type.equals(Long.TYPE)) {
                                    equals = Arrays.equals((long[]) v1, (long[]) v2);
                                } else if (type.equals(Short.TYPE)) {
                                    equals = Arrays.equals((short[]) v1, (short[]) v2);
                                } else {
                                    assert false : type;
                                    equals = false;
                                }
                            } else {
                                equals = Arrays.equals((Object[]) v1, (Object[]) v2);
                            }
                            if (!equals) {
                                return false;
                            }
                        }
                        if (!v1.equals(v2)) {
                            return false;
                        }
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Error e) {
                    throw e;
                } catch (Throwable t) {
                    assert false : t;
                }
            }
        }
        return false;
    }
    
    public String toString() {
        init();
        StringBuilder builder = new StringBuilder("@");
        builder.append(annotationType.getName());
        builder.append("(");
        for (Iterator<Map.Entry<Method, Object>> i = data.entrySet().iterator();
        i.hasNext();) {
            Map.Entry<Method, Object> e = i.next();
            builder.append(e.getKey().getName());
            builder.append("=");
            Object o = data.get(e.getKey());
            assert o != null;
            if (o.getClass().isArray()) {
                Class<?> type = o.getClass().getComponentType();
                final String str;
                if (type.isPrimitive()) {
                    if (type.equals(Boolean.TYPE)) {
                        str = Arrays.toString((boolean[]) o);
                    } else if (type.equals(Byte.TYPE)) {
                        str = Arrays.toString((byte[]) o);
                    } else if (type.equals(Character.TYPE)) {
                        str = Arrays.toString((char[]) o);
                    } else if (type.equals(Double.TYPE)) {
                        str = Arrays.toString((double[]) o);
                    } else if (type.equals(Float.TYPE)) {
                        str = Arrays.toString((float[]) o);
                    } else if (type.equals(Integer.TYPE)) {
                        str = Arrays.toString((int[]) o);
                    } else if (type.equals(Long.TYPE)) {
                        str = Arrays.toString((long[]) o);
                    } else if (type.equals(Short.TYPE)) {
                        str = Arrays.toString((short[]) o);
                    } else {
                        assert false : type;
                        str = "";
                    }
                } else {
                    str = Arrays.toString((Object[]) o);
                }
                builder.append(str);
            } else {
                builder.append(o);
            }
            if (i.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }
    
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.equals(HASH_CODE)) {
            return hashCode();
        } else if (method.equals(EQUALS)) {
            return equals(args[0]);
        } else if (method.equals(TO_STRING)) {
            return toString();
        } else if (method.equals(ANNOTATION_TYPE)) {
            return annotationType;
        }
        init(method);
        Object o = data.get(method);
        assert o != null : method;
        if (o == null) {
            throw new IncompleteAnnotationException(annotationType,
                    method.getName());
        }
        return o;
    }
    
    /**
     * The default implementation of this method is restricted to primitives,
     * {@code String} and {@code Class}.
     */
    public static Object valueOf(String str, Class type,
            ClassLoader loader) throws Throwable {
        final Object o;
        if (type.isPrimitive()) {
            final Class<?> objectType;
            if (Boolean.TYPE.equals(type)) {
                objectType = Boolean.class;
            } else if (Byte.TYPE.equals(type)) {
                objectType = Byte.class;
            } else if (Short.TYPE.equals(type)) {
                objectType = Short.class;
            } else if (Integer.TYPE.equals(type)) {
                objectType = Integer.class;
            } else if (Long.TYPE.equals(type)) {
                objectType = Long.class;
            } else if (Float.TYPE.equals(type)) {
                objectType = Float.class;
            } else if (Double.TYPE.equals(type)) {
                objectType = Double.class;
            } else {
                throw new AssertionError(type);
            }
            try {
                o = str.length() == 0 ? null : objectType.getMethod(
                        "valueOf", String.class).invoke(null, str);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else if (type.isEnum()) {
            if (str.length() != 0) {
                Object tmp = null;
                try {
                    tmp = Enum.class.getMethod("valueOf", 
                            Class.class, String.class).invoke(null, type, str);
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getTargetException();
                    } catch (RuntimeException e2) {
                        throw e2;
                    } catch (Exception e2) {
                        assert false : e;
                    }
                }
                o = tmp;
            } else {
                o = null;
            }
        } else if (type.isAnnotation()) {
            o = null;
        } else if (type.isArray()) {
            o = null;
        } else if (String.class.equals(type)) {
            o = str.length() == 0 ? null : str;
        } else if (Class.class.equals(type)) {
            o = str.length() == 0 ? null : loader.loadClass(str);
        } else {
            o = null;
        }
        return o;
    }

}
