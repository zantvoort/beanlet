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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 *
 * @author Leon van Zantvoort
 */
public final class Elements {

    private Elements() {
    }
    
    public static Element instance(Member member) {
        final Element e;
        if (member instanceof Constructor) {
            e = ConstructorElement.instance((Constructor) member);
        } else if (member instanceof Method) {
            e = MethodElement.instance((Method) member);
        } else if (member instanceof Field) {
            e = FieldElement.instance((Field) member);
        } else {
            assert false : member;
            e = null;
        }
        return e;
    }
    
    public static Element instance(AnnotatedElement element) {
        final Element e;
        if (element instanceof Class) {
            e = TypeElement.instance((Class) element);
        } else if (element instanceof Constructor) {
            e = ConstructorElement.instance((Constructor) element);
        } else if (element instanceof Method) {
            e = MethodElement.instance((Method) element);
        } else if (element instanceof Field) {
            e = FieldElement.instance((Field) element);
        } else if (element instanceof Package) {
            e = PackageElement.instance((Package) element);
        } else {
            assert false : element;
            e = null;
        }
        return e;
    }
    
    public static Element instance(GenericDeclaration declaration) {
        final Element e;
        if (declaration instanceof Class) {
            e = TypeElement.instance((Class) declaration);
        } else if (declaration instanceof Constructor) {
            e = ConstructorElement.instance((Constructor) declaration);
        } else if (declaration instanceof Method) {
            e = MethodElement.instance((Method) declaration);
        } else {
            assert false : declaration;
            e = null;
        }
        return e;
    }
}
