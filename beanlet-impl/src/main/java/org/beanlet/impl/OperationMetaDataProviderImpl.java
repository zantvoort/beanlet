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

import org.beanlet.impl.metadata.OperationMetaDataImpl;
import static org.beanlet.common.AbstractMetaData.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.beanlet.common.AbstractProvider;
import org.jargo.ComponentConfiguration;
import org.jargo.MetaData;
import org.beanlet.Operation;
import org.beanlet.annotation.AnnotationDeclaration;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.Element;
import org.beanlet.annotation.MethodElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.jargo.spi.MetaDataProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class OperationMetaDataProviderImpl extends AbstractProvider 
        implements MetaDataProvider {
    
    public List<MetaData> getMetaData(ComponentConfiguration configuration) {
        List<MetaData> metaData = 
                new ArrayList<MetaData>();
        if (configuration instanceof BeanletConfiguration) {
            AnnotationDomain domain = ((BeanletConfiguration) configuration).
                    getAnnotationDomain();
            AnnotationDeclaration<Operation> operationDeclaration = 
                    domain.getDeclaration(Operation.class);
            Operations operations = Operations.getInstance(configuration);
            for (Method method : operations.getMethods()) {
                Element element = MethodElement.instance(method);
                Operation annotation = operationDeclaration.
                        getAnnotation(element);
                final String operationName;
                if (annotation != null) {
                    if (!annotation.name().equals("")) {
                        operationName = annotation.name();
                    } else {
                        operationName = method.getName();
                    }
                } else {
                    operationName = method.getName();
                }
                String description = annotation == null ? "" : 
                    annotation.description();
                metaData.add(new OperationMetaDataImpl(operationName, 
                        getAnnotatedElement(domain.getAnnotations(element)),
                        method.getReturnType(),
                        method.getParameterTypes(),
                        Attributes.getAttributeName(method),
                        Attributes.isGetter(method),
                        Attributes.isSetter(method),
                        Attributes.isIs(method),
                        description));
            }
        }
        return Collections.unmodifiableList(metaData);
    }
}
