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

import org.beanlet.BeanletApplicationContext;
import org.beanlet.web.FilterDefinition;
import org.beanlet.web.ServletDefinition;

import javax.servlet.*;
import java.util.EnumSet;

/**
 *
 * @author Leon van Zantvoort
 */
public class ServletDeployerImpl implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        BeanletApplicationContext ctx = BeanletApplicationContext.instance();
        for (String name : ctx.getBeanletNamesForType(ServletDefinition.class)) {
            ServletDefinition definition = ctx.getBeanlet(name, ServletDefinition.class);
            ServletRegistration.Dynamic registration =
                    servletContextEvent.getServletContext().addServlet(definition.getName(), definition.getServlet());
            registration.addMapping(definition.getMapping().toArray(new String[0]));
            registration.setInitParameters(definition.getInitParameters());
            registration.setAsyncSupported(definition.isAsyncSupported());
        }
        for (String name : ctx.getBeanletNamesForType(FilterDefinition.class)) {
            FilterDefinition definition = ctx.getBeanlet(name, FilterDefinition.class);
            FilterRegistration.Dynamic registration =
                    servletContextEvent.getServletContext().addFilter(definition.getName(), definition.getFilter());
            registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),
                    definition.isUrlMatchAfter(), definition.getUrlMapping().toArray(new String[0]));
            registration.addMappingForServletNames(EnumSet.allOf(DispatcherType.class),
                    definition.isServletMatchAfter(), definition.getServletMapping().toArray(new String[0]));
            registration.setInitParameters(definition.getInitParameters());
            registration.setAsyncSupported(definition.isAsyncSupported());
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
