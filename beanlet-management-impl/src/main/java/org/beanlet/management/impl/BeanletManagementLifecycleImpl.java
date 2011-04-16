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
package org.beanlet.management.impl;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.beanlet.BeanletException;
import org.beanlet.management.Manageable;
import org.jargo.ComponentConfiguration;
import org.jargo.ComponentFactory;
import org.jargo.ComponentReference;
import org.jargo.ComponentReferenceLifecycle;
import org.jargo.ComponentLifecycle;

public final class BeanletManagementLifecycleImpl<T> implements 
        ComponentLifecycle<T>, ComponentReferenceLifecycle<T> {

    private static final SelfNamingEvent event = new SelfNamingEventImpl();
    
    private final ComponentConfiguration<T> configuration;
    private final Manageable manageable;
    private final boolean selfNaming;
    
    private final ConcurrentMap<Object, ObjectName> map;
            
    public BeanletManagementLifecycleImpl(ComponentConfiguration<T> configuration,
            Manageable manageable, boolean selfNaming) {
        this.configuration = configuration;
        this.manageable = manageable;
        this.selfNaming = selfNaming;
        this.map = new ConcurrentHashMap<Object, ObjectName>();
    }
    
    public void onCreate(ComponentFactory<T> factory) {
        if (factory.getComponentMetaData().isStatic()) {
            ComponentReference<T> reference = factory.create();
            ObjectName name = getObjectName(reference);
            DynamicMBean mbean = new BeanletDynamicMBean(reference, 
                    configuration.getType(), configuration.getComponentUnit().getClassLoader(), 
                    manageable);
            register(mbean, name);
            map.put(factory.getComponentMetaData().getComponentName(), name);
        }
    }

    public void onDestroy(ComponentFactory<T> factory) {
        if (factory.getComponentMetaData().isStatic()) {
            ObjectName name = map.remove(factory.getComponentMetaData().getComponentName());
            assert name != null;
            unregister(name);
        }
    }

    public void onCreate(ComponentReference<T> reference) {
        if (!reference.getComponentMetaData().isStatic()) {
            ObjectName name = getObjectName(reference);
            DynamicMBean mbean = new BeanletDynamicMBean(reference.weakReference(), 
                    configuration.getType(), configuration.getComponentUnit().getClassLoader(), 
                    manageable);
            register(mbean, name);
            map.put(reference.weakReference(), name);
        }
    }
    
    public void onDestroy(ComponentReference<T> reference) {
        if (!reference.getComponentMetaData().isStatic()) {
            ObjectName name = map.remove(reference);
            assert name != null;
            unregister(name);
        }
    }
    
    private void register(Object object, ObjectName name) {
        switch (manageable.registrationPolicy()) {
            case FAIL_ON_EXISTING:
                registerFailOnExisting(object, name);
                break;
            case IGNORE_EXISTING:
                registerIgnoreExisting(object, name);
                break;
            case REPLACE_EXISTING:
                registerReplaceExisting(object, name);
                break;
        }
    }

    private void registerFailOnExisting(Object object, ObjectName name) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mbeanServer.registerMBean(object, name);
        } catch (MBeanRegistrationException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        } catch (NotCompliantMBeanException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        } catch (InstanceAlreadyExistsException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        }
    }
    
    private void registerIgnoreExisting(Object object, ObjectName name) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mbeanServer.registerMBean(object, name);
        } catch (MBeanRegistrationException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        } catch (NotCompliantMBeanException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        } catch (InstanceAlreadyExistsException e) {
            // Ignore.
        }
    }
    
    private void registerReplaceExisting(Object object, ObjectName name) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            while (true) {
                if (mbeanServer.isRegistered(name)) {
                    try {
                        mbeanServer.unregisterMBean(name);
                    } catch (InstanceNotFoundException e) {
                    }
                }
                try {
                    mbeanServer.registerMBean(object, name);
                    break;
                } catch (InstanceAlreadyExistsException ex) {
                }
            }
        } catch (MBeanRegistrationException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        } catch (NotCompliantMBeanException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        }
    }
    
    private void unregister(ObjectName name) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mbeanServer.unregisterMBean(name);
        } catch (MBeanRegistrationException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        } catch (InstanceNotFoundException e) {
            throw new BeanletException(configuration.getComponentName(), e);
        }
    }
    
    private ObjectName getObjectName(ComponentReference reference) {
        try {
            final ObjectName name;
            if (selfNaming) {
                assert reference.isExecutable(event);
                name = (ObjectName) reference.execute(event);
            } else {
                name = manageable.namingStrategy().newInstance().getObjectName(
                        reference.getComponent(),
                        reference.getComponentMetaData().getComponentName(), 
                        reference.getComponentMetaData().getType());
            }
            return name;
        } catch (BeanletException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanletException(configuration.getComponentName(), e);
        }
    }
}
