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

package com.alibaba.nacos.naming.selector.context;

import com.alibaba.nacos.api.cmdb.pojo.Entity;
import com.alibaba.nacos.api.cmdb.pojo.PreservedEntityTypes;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.selector.context.CmdbContext;
import com.alibaba.nacos.api.selector.context.SelectorContextBuilder;
import com.alibaba.nacos.cmdb.service.CmdbReader;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CMDB 选择器上下文构建器。
 *
 * <p>通过 {@link CmdbReader} 查询消费者与各提供者 IP 对应的 CMDB {@link Entity}，组装 {@link CmdbContext} 供标签类选择器使用。</p>
 *
 * @author chenglu
 * @date 2021-07-16 11:58
 */
public class CmdbSelectorContextBuilder<T extends Instance>
    implements SelectorContextBuilder<CmdbContext<Instance>, String, List<T>> {
    
    /** 上下文类型：CMDB。 */
    private static final String CONTEXT_TYPE = "CMDB";
    
    /**
     * 从 Spring 容器获取 {@link CmdbReader}。
     *
     * @return CMDB 读取器
     */
    /** 延迟从 Spring 获取 CmdbReader Bean。 */
    public CmdbReader getCmdbReader() {
        return ApplicationUtils.getBean(CmdbReader.class);
    }
    
    @Override
    /** 查询消费者与提供者 CMDB 实体并构建 CmdbContext。 */
    public CmdbContext<Instance> build(String consumer, List<T> provider) {
        // 构建消费者 CMDB 上下文
        Entity consumerEntity =
            getCmdbReader().queryEntity(consumer, PreservedEntityTypes.ip.name());
        Instance consumerInstance = new Instance();
        consumerInstance.setIp(consumer);
        CmdbContext.CmdbInstance<Instance> consumerCmdbInstance = new CmdbContext.CmdbInstance<>();
        consumerCmdbInstance.setEntity(consumerEntity);
        consumerCmdbInstance.setInstance(consumerInstance);
        CmdbContext<Instance> cmdbContext = new CmdbContext<>();
        cmdbContext.setConsumer(consumerCmdbInstance);
        
        // 逐实例查询提供者 CMDB 实体
        List<CmdbContext.CmdbInstance<Instance>> providerCmdbInstances =
            Optional.ofNullable(provider)
                .orElse(Collections.emptyList())
                .stream()
                .map(is -> {
                    CmdbContext.CmdbInstance<Instance> providerCmdbInstance =
                        new CmdbContext.CmdbInstance<>();
                    providerCmdbInstance.setInstance(is);
                    Entity providerEntity =
                        getCmdbReader().queryEntity(is.getIp(), PreservedEntityTypes.ip.name());
                    providerCmdbInstance.setEntity(providerEntity);
                    return providerCmdbInstance;
                })
                .collect(Collectors.toList());
        cmdbContext.setProviders(providerCmdbInstances);
        
        return cmdbContext;
    }
    
    @Override
    /** 返回上下文类型 CMDB。 */
    public String getContextType() {
        return CONTEXT_TYPE;
    }
}
