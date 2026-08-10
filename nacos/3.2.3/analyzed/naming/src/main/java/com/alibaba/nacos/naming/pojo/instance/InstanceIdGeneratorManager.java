/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.pojo.instance;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.spi.generator.InstanceIdGenerator;
import com.alibaba.nacos.api.utils.StringUtils;
import com.alibaba.nacos.common.spi.NacosServiceLoader;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实例 ID 生成器 SPI 管理器（单例）。
 *
 * <p>启动时通过 {@link NacosServiceLoader} 加载全部 {@link InstanceIdGenerator} 实现，按 type 索引；{@link #generateInstanceId(Instance)} 根据实例指定或默认类型委托生成。</p>
 *
 * @author : huangtianhui
 */
public class InstanceIdGeneratorManager {
    
    /** 全局单例，类加载时完成 SPI 初始化。 */
    private static final InstanceIdGeneratorManager INSTANCE = new InstanceIdGeneratorManager();
    
    private final Map<String, InstanceIdGenerator> generatorMap = new ConcurrentHashMap<>();
    
    public InstanceIdGeneratorManager() {
        init();
    }
    
    /** 扫描并注册所有 InstanceIdGenerator SPI 实现。 */
    private void init() {
        Collection<InstanceIdGenerator> instanceIdGenerators =
            NacosServiceLoader.load(InstanceIdGenerator.class);
        for (InstanceIdGenerator instanceIdGenerator : instanceIdGenerators) {
            generatorMap.put(instanceIdGenerator.type(), instanceIdGenerator);
        }
    }
    
    private InstanceIdGenerator getInstanceIdGenerator(String type) {
        if (generatorMap.containsKey(type)) {
            return generatorMap.get(type);
        }
        throw new NoSuchElementException("The InstanceIdGenerator type is not found ");
    }
    
    /**
     * 根据实例的 generator 类型（缺省为 default）生成唯一 instanceId。
     *
     * @param instance instance
     * @return InstanceId
     */
    public static String generateInstanceId(Instance instance) {
        String instanceIdGeneratorType = instance.getInstanceIdGenerator();
        if (StringUtils.isBlank(instanceIdGeneratorType)) {
            instanceIdGeneratorType = Constants.DEFAULT_INSTANCE_ID_GENERATOR;
        }
        return INSTANCE.getInstanceIdGenerator(instanceIdGeneratorType)
            .generateInstanceId(instance);
    }
    
}
