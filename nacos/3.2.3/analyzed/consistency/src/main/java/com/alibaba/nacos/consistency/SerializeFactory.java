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

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.consistency.serialize.HessianSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 一致性层序列化器工厂：内置 Hessian，并通过 SPI 加载其他 {@link Serializer} 实现。
 * Serialization factory.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class SerializeFactory {
    
    /** Hessian 序列化器在 Map 中的键（小写） */
    public static final String HESSIAN_INDEX = "Hessian".toLowerCase();
    
    /** 序列化器名称（小写）→ 实例 */
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<>(4);
    
    /** 默认序列化器类型键 */
    public static final String DEFAULT_SERIALIZER = HESSIAN_INDEX;
    
    static {
        Serializer serializer = new HessianSerializer();
        SERIALIZER_MAP.put(HESSIAN_INDEX, serializer);
        for (Serializer item : NacosServiceLoader.load(Serializer.class)) {
            SERIALIZER_MAP.put(item.name().toLowerCase(), item);
        }
    }
    
    /** 返回默认 Hessian 序列化器 */
    public static Serializer getDefault() {
        return SERIALIZER_MAP.get(DEFAULT_SERIALIZER);
    }
    
    /**
     * 按类型名（不区分大小写）获取序列化器。
     *
     * @param type 序列化器名称
     * @return 对应 {@link Serializer}，未注册时返回 null
     */
    public static Serializer getSerializer(String type) {
        return SERIALIZER_MAP.get(type.toLowerCase());
    }
}
