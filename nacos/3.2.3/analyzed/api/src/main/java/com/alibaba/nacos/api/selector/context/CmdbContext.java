/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.api.selector.context;

import com.alibaba.nacos.api.cmdb.pojo.Entity;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.selector.Selector;

import java.util.List;

/**
 * CMDB 驱动的选择器上下文。
 *
 * <p>由 {@link SelectorContextBuilder#build(Object, Object)} 构建，供 {@link Selector#select(Object)} 读取消费者与提供者及其 CMDB 元数据。</p>
 *
 * @author chenglu
 * @date 2021-07-09 21:31
 */
public class CmdbContext<T extends Instance> {
    
    /** 发起选择的消费者实例（含 CMDB 信息）。 */
    private CmdbInstance<T> consumer;
    
    /** 待筛选的提供者实例列表。 */
    private List<CmdbInstance<T>> providers;
    
    /** 返回消费者 CMDB 实例包装。 */
    public CmdbInstance<T> getConsumer() {
        return consumer;
    }
    
    /** 设置消费者 CMDB 实例包装。 */
    public void setConsumer(CmdbInstance<T> consumer) {
        this.consumer = consumer;
    }
    
    /** 返回提供者列表。 */
    public List<CmdbInstance<T>> getProviders() {
        return providers;
    }
    
    /** 设置提供者列表。 */
    public void setProviders(List<CmdbInstance<T>> providers) {
        this.providers = providers;
    }
    
    /** 调试用字符串表示。 */
    @Override
    public String toString() {
        return "CmdbContext{" + "consumer=" + consumer + ", providers=" + providers + '}';
    }
    
    /** 将 {@link Entity} CMDB 元数据与 {@link Instance} 绑定的包装类型。 */
    public static class CmdbInstance<T> {
        
        /** 实例关联的 CMDB 实体信息。 */
        private Entity entity;
        
        /** 底层服务实例对象。 */
        private T instance;
        
        /** 返回 CMDB 实体。 */
        public Entity getEntity() {
            return entity;
        }
        
        /** 设置 CMDB 实体。 */
        public void setEntity(Entity entity) {
            this.entity = entity;
        }
        
        /** 返回服务实例。 */
        public T getInstance() {
            return instance;
        }
        
        /** 设置服务实例。 */
        public void setInstance(T instance) {
            this.instance = instance;
        }
        
        /** 调试用字符串表示。 */
        @Override
        public String toString() {
            return "CmdbInstance{" + "entity=" + entity + ", instance=" + instance + '}';
        }
    }
}
