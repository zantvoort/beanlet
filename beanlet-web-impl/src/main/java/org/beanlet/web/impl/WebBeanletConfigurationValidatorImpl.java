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
package org.beanlet.web.impl;

import org.beanlet.BeanletValidationException;
import org.beanlet.annotation.AnnotationDomain;
import org.beanlet.annotation.TypeElement;
import org.beanlet.plugin.BeanletConfiguration;
import org.beanlet.plugin.BeanletConfigurationValidator;
import org.beanlet.web.WebFilter;
import org.beanlet.web.WebServlet;

import javax.servlet.*;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Leon van Zantvoort
 */
public final class WebBeanletConfigurationValidatorImpl implements
        BeanletConfigurationValidator {

    private static final List<Class> WEB_LISTENER_CLASSES = Arrays.<Class>asList(ServletContextListener.class,
            ServletContextAttributeListener.class, ServletRequestListener.class, ServletRequestAttributeListener.class,
            HttpSessionListener.class, HttpSessionAttributeListener.class);

    public void validate(BeanletConfiguration configuration) throws
            BeanletValidationException {
        TypeElement element = TypeElement.instance(configuration.getType());
        AnnotationDomain domain = configuration.getAnnotationDomain();
        WebServlet webServlet = domain.getDeclaration(WebServlet.class).getAnnotation(element);
        if (webServlet != null) {
            if (!Servlet.class.isAssignableFrom(element.getType())) {
                throw new BeanletValidationException(configuration.getComponentName(),
                        webServlet.getClass().getSimpleName() + " annotation MAY only " +
                        "be applied to elements of type: '" + Servlet.class + "'.");
            }

        }
        WebFilter webFilter = domain.getDeclaration(WebFilter.class).getAnnotation(element);
        if (webFilter != null) {
            if (!Filter.class.isAssignableFrom(element.getType())) {
                throw new BeanletValidationException(configuration.getComponentName(),
                        webFilter.getClass().getSimpleName() + " annotation MAY only " +
                        "be applied to elements of type: '" + Filter.class + "'.");
            }

        }
        WebFilter webListener = domain.getDeclaration(WebFilter.class).getAnnotation(element);
        if (webListener != null) {
            boolean match = false;
            for (Class<?> cls : WEB_LISTENER_CLASSES) {
                if (cls.isAssignableFrom(element.getType())) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                throw new BeanletValidationException(configuration.getComponentName(),
                        webListener.getClass().getSimpleName() + " annotation MAY only " +
                        "be applied to elements of types: '" + WEB_LISTENER_CLASSES + "'.");
            }

        }
    }
}
