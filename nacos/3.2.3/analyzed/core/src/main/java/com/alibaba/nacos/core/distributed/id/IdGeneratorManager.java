/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.distributed.id;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.consistency.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 分布式 ID 生成器管理器：按资源名懒加载并缓存 {@link com.alibaba.nacos.consistency.IdGenerator}，优先使用 SPI 实现，否则回退 {@link SnowFlowerIdGenerator}。
 * Id generator manager.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Component
public class IdGeneratorManager {
    
    /** 资源名到 ID 生成器的并发映射。 */
    private final Map<String, IdGenerator> generatorMap = new ConcurrentHashMap<>();
    
    /** 按资源名创建并初始化 ID 生成器的工厂函数。 */
    private final Function<String, IdGenerator> supplier;
    
    /** 构造管理器，supplier 优先加载 SPI {@link IdGenerator}，无实现时使用雪花算法。 */
    public IdGeneratorManager() {
        this.supplier = s -> {
            IdGenerator generator;
            Collection<IdGenerator> idGenerators = NacosServiceLoader.load(IdGenerator.class);
            Iterator<IdGenerator> iterator = idGenerators.iterator();
            if (iterator.hasNext()) {
                generator = iterator.next();
            } else {
                generator = new SnowFlowerIdGenerator();
            }
            generator.init();
            return generator;
        };
    }
    
    /**
     * 注册单个资源的 ID 生成器（懒创建）。
     *
     * @param resource 资源名称
     */
    public void register(String resource) {
        generatorMap.computeIfAbsent(resource, s -> supplier.apply(resource));
    }
    
    /**
     * 批量注册需要使用 ID 生成器的资源。
     *
     * @param resources resource name list
     */
    public void register(String... resources) {
        for (String resource : resources) {
            generatorMap.computeIfAbsent(resource, s -> supplier.apply(resource));
        }
    }
    
    /**
     * 按资源名获取下一个分布式 ID。
     *
     * @param resource resource name
     * @return id
     */
    public long nextId(String resource) {
        if (generatorMap.containsKey(resource)) {
            return generatorMap.get(resource).nextId();
        }
        throw new NoSuchElementException(
            "The resource is not registered with the distributed "
                + "ID resource for the time being.");
    }
    
    /** 返回已注册的资源到生成器映射（只读视图用途）。 */
    public Map<String, IdGenerator> getGeneratorMap() {
        return generatorMap;
    }
}
