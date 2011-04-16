package org.beanlet.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.beanlet.BeanletMetaData;
import org.beanlet.MetaData;
import org.beanlet.common.*;
import org.jargo.ComponentMetaData;


public final class BeanletMetaDataImpl<T> extends AbstractMetaData implements 
        BeanletMetaData<T> {

    private final ComponentMetaData<T> componentMetaData;
    private final AtomicReference<List<MetaData>> ref;
    
    public static BeanletMetaData<?> instance(ComponentMetaData<?> metaData) {
        @SuppressWarnings("unchecked")
        ComponentMetaData<Object> m = (ComponentMetaData<Object>) metaData;
        return new BeanletMetaDataImpl<Object>(m);
    }    

    public BeanletMetaDataImpl(ComponentMetaData<T> componentMetaData) {
        super(componentMetaData.getType(), componentMetaData.getDescription());
        this.componentMetaData = componentMetaData;
        this.ref = new AtomicReference<List<MetaData>>();
    }

    public String getBeanletName() {
        return componentMetaData.getComponentName();
    }
    
    public ClassLoader getClassLoader() {
        return componentMetaData.getComponentUnit().getClassLoader();
    }

    public Class<T> getType() {
        Class<T> type = componentMetaData.getType();
        return type;
    }
    
    public List<Class<?>> getInterfaces() {
        return componentMetaData.getInterfaces();
    }

    public boolean isProxy() {
        return componentMetaData.isProxy();
    }

    public boolean isVanilla() {
        return componentMetaData.isVanilla();
    }

    public boolean isStatic() {
        return componentMetaData.isStatic();
    }

    public List<MetaData> getMetaData() {
        List<MetaData> metaData = ref.get();
        if (metaData == null) {
            metaData = new ArrayList<MetaData>();
            for (org.jargo.MetaData m : componentMetaData.getMetaData()) {
                if (m instanceof MetaData) {
                    metaData.add((MetaData) m);
                }
            }
            metaData = Collections.unmodifiableList(metaData);
            ref.set(metaData);
        }
        return metaData;
    }
    
    public boolean isMetaDataPresent(Class<? extends MetaData> metaDataType) {
        boolean present = false;
        for(MetaData m : getMetaData()) {
            if (metaDataType.isAssignableFrom(m.getClass())) {
                present = true;
                break;
            }
        }
        return present;
    }
    
    public <M extends MetaData> List<M> getMetaData(Class<M> metaDataType) {
        List<M> list = new ArrayList<M>();
        for(MetaData m : getMetaData()) {
            if (metaDataType.isAssignableFrom(m.getClass())) {
                @SuppressWarnings("unchecked")
                M t = (M) m;
                list.add(t);
            }
        }
        return list;
    }
}