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
package org.beanlet.transaction.impl;

import java.util.Collections;
import java.util.List;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.plugin.ClassResolver;
import org.beanlet.transaction.TransactionAttribute;

/**
 *
 * @author Leon van Zantvoort
 */
public final class TransactionAttributeClassResolverImpl implements ClassResolver {
    
    public List<Class> getClasses(AnnotationDomain annotationDomain) {
        final List<Class> list;
        if (!annotationDomain.getDeclaration(TransactionAttribute.class).
                getElements().isEmpty()) {
            Class cls = TransactionInvocationInterceptor.class;
            list = Collections.singletonList(cls);
        } else {
            list = Collections.emptyList();
        }
        return list;
    }
}
