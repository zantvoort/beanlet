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
package org.beanlet.rest.jersey;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.beanlet.*;
import org.beanlet.rest.Restlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;


public class JerseyRestContainer extends ServletContainer {

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {

        class BeanletManagedComponentProvider implements IoCFullyManagedComponentProvider {

            private final String beanletName;

            public BeanletManagedComponentProvider(String beanletName) {
                this.beanletName = beanletName;
            }

            // Never invoked.
            public ComponentScope getScope() {
                assert false;
                return ComponentScope.Undefined;
            }

            // Is invoked every time server needs the class.
            public Object getInstance() {
                BeanletFactory<?> factory = BeanletApplicationContext.instance().getBeanletFactory(beanletName);
                BeanletReference<?> reference = factory.create();
                JerseyHelper.pushBeanletReference(reference);
                return reference.getBeanlet();
            }
        };

        class JersyManagedComponentProvider implements IoCProxiedComponentProvider {
            
            private final String beanletName;
            
            public JersyManagedComponentProvider(String beanletName) {
                this.beanletName = beanletName;
            }
            
            // Never invoked.
            public Object getInstance() {
                assert false;
                return null;
            }

            public Object proxy(Object o) {
                assert o != null;
                try {
                    JerseyHelper.setJerseyObject(beanletName, o);
                    return BeanletApplicationContext.instance().getBeanlet(beanletName);
                } finally {
                    JerseyHelper.setJerseyObject(null, null);
                }
            }
        }

        wa.initiate(rc, new IoCComponentProviderFactory() {
            public IoCComponentProvider getComponentProvider(Class<?> c) {
                final IoCComponentProvider provider;
                Set<String> beanletNames = BeanletApplicationContext.instance().getBeanletNamesForType(c);
                if (beanletNames.isEmpty()) {
                    provider = null;
                } else if (beanletNames.size() == 1) {
                    String beanletName = beanletNames.iterator().next();
                    BeanletFactory<?> factory = BeanletApplicationContext.instance().getBeanletFactory(beanletName);
                    Restlet restlet = factory.getBeanletMetaData().getAnnotation(Restlet.class);
                    if (restlet != null && restlet.createRestlet()) {
                        if (!factory.getBeanletMetaData().isVanilla() || factory.getBeanletMetaData().isStatic()) {
                            throw new BeanletValidationException(beanletName, "Restlet beanlets created by Rest runtime must be not non-static vanilla beanlets.");
                        }
                        provider = new JersyManagedComponentProvider(beanletName);
                    } else {
                        if (!factory.getBeanletMetaData().isVanilla()) {
                            throw new BeanletValidationException(beanletName, "Restlet beanlets created by Beanlet runtime must be vanilla beanlets.");
                        }
                        provider = new BeanletManagedComponentProvider(beanletName);
                    }
                } else {
                    throw new BeanletApplicationException("Multiple beanlets exist for restlet type: " + c.getName());
                }
                return provider;
            };

            public IoCComponentProvider getComponentProvider(ComponentContext ctx, Class<?> c) {
                return getComponentProvider(c);
            }
        });
    }

    @Override
    public int service(URI baseUri, URI requestUri, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            return super.service(baseUri, requestUri, request, response);
        } finally {
            if (JerseyHelper.getJerseyObject() == null) {   // Should not be invoked in this mode.
                BeanletReference<?> reference = JerseyHelper.popBeanletReference();
                assert reference != null;
                if (!reference.getBeanletMetaData().isStatic()) {
                    reference.invalidate();
                }
            }
        }
    }
}
