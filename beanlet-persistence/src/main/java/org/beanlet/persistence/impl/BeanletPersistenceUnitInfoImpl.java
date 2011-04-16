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
package org.beanlet.persistence.impl;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import org.beanlet.BeanletApplicationContext;

/**
 *
 * @author Leon van Zantvoort
 */
public final class BeanletPersistenceUnitInfoImpl implements BeanletPersistenceUnitInfo {
    
    private final PersistenceUnitTransactionType transactionType;
    private final boolean excludeUnlistedClasses;
    private final ClassLoader classLoader;
    private final List<URL> jarFileUrls;
    private final String jtaDataSource;
    private final List<String> managedClassNames;
    private final List<String> mappingFileNames;
    private final ClassLoader newTempClassLoader;
    private final String nonJtaDataSource;
    private final String persistenceProviderClassName;
    private final String persistenceUnitName;
    private final URL persistenceUnitRootUrl;
    private final Properties properties;
    private final String persistenceXMLSchemaVersion;
    private final SharedCacheMode sharedCacheMode;
    private final ValidationMode validationMode;

    public BeanletPersistenceUnitInfoImpl(
            PersistenceUnitTransactionType transactionType, 
            boolean excludeUnlistedClasses,
            ClassLoader classLoader,
            List<URL> jarFileUrls,
            String jtaDataSource,
            List<String> managedClassNames,
            List<String> mappingFileNames,
            ClassLoader newTempClassLoader,
            String nonJtaDataSource,
            String persistenceProviderClassName,
            String persistenceUnitName,
            URL persistenceUnitRootUrl,
            Properties properties,
            String persistenceXMLSchemaVersion,
            SharedCacheMode sharedCacheMode,
            ValidationMode validationMode) {
        this.transactionType = transactionType;
        this.excludeUnlistedClasses = excludeUnlistedClasses;
        this.classLoader = classLoader;
        this.jarFileUrls = jarFileUrls;
        this.jtaDataSource = jtaDataSource;
        this.managedClassNames = managedClassNames;
        this.mappingFileNames = mappingFileNames;
        this.newTempClassLoader = newTempClassLoader;
        this.nonJtaDataSource = nonJtaDataSource;
        this.persistenceProviderClassName = persistenceProviderClassName;
        this.persistenceUnitName = persistenceUnitName;
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
        this.properties = properties;
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
        this.sharedCacheMode = sharedCacheMode;
        this.validationMode = validationMode;
    }
    
    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource == null ? null :
                BeanletApplicationContext.instance().getBeanlet(
                jtaDataSource, DataSource.class);
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    public ClassLoader getNewTempClassLoader() {
        return newTempClassLoader;
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource == null ? null : 
                BeanletApplicationContext.instance().getBeanlet(
                nonJtaDataSource, DataSource.class);
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    public Properties getProperties() {
        return properties;
    }

    public void addTransformer(ClassTransformer classTransformer) {
        Thread.dumpStack();
    }

    public Set<String> getDependencies() {
        Set<String> dependencies = new HashSet<String>();
        if (nonJtaDataSource != null) {
            dependencies.add(nonJtaDataSource);
        }
        if (jtaDataSource != null) {
            dependencies.add(jtaDataSource);
        }
        return Collections.unmodifiableSet(dependencies);
    }

    public String getPersistenceXMLSchemaVersion() {
        return persistenceXMLSchemaVersion;
    }

    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }
}
