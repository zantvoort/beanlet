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

import java.util.concurrent.Executor;
import org.beanlet.common.AbstractProvider;
import org.beanlet.common.Beanlets;
import org.jargo.Event;
import org.jargo.EventExecutor;
import org.jargo.ComponentConfiguration;
import org.jargo.spi.EventExecutorProvider;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletEventExecutorProviderImpl extends AbstractProvider
        implements EventExecutorProvider {
    
    public <T> EventExecutor<T> getEventExecutor(
            ComponentConfiguration<T> configuration, 
            Class<? extends Event> type, Executor executor) {
        Beanlets beanlets = Beanlets.getInstance(configuration);
        return new BeanletEventExecutorImpl<T>(configuration.getComponentName(),
                beanlets.getRetentionPolicy(),
                beanlets.getRetentionExceptionType());
    }
}
