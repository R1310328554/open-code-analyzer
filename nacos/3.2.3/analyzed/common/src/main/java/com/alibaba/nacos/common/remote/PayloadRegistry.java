/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote;

import com.alibaba.nacos.api.remote.Payload;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * RPC 载荷（Payload）注册表：通过 {@link ServiceLoader} 扫描 classpath 下所有
 * {@link com.alibaba.nacos.api.remote.Payload} 实现，建立 simpleName → Class 映射，
 * 供远程请求/响应反序列化时按类型名查找具体类。
 * payload registry,Define basic scan behavior request and response.
 *
 * @author liuzunfei
 * @author hujun
 * @version $Id: PayloadRegistry.java, v 0.1 2020年09月01日 10:56 AM liuzunfei Exp $
 */

public class PayloadRegistry {
    
    /** 类型名 → Payload 实现类的全局注册表 */
    private static final Map<String, Class<?>> REGISTRY_REQUEST = new HashMap<>();
    
    /** 是否已完成 SPI 扫描，保证 {@link #scan()} 只执行一次 */
    static boolean initialized = false;
    
    /** 触发 SPI 扫描并注册所有 Payload 实现 */
    public static void init() {
        scan();
    }
    
    private static synchronized void scan() {
        if (initialized) {
            return;
        }
        ServiceLoader<Payload> payloads = ServiceLoader.load(Payload.class);
        for (Payload payload : payloads) {
            register(payload.getClass().getSimpleName(), payload.getClass());
        }
        initialized = true;
    }
    
    /** 注册单个 Payload 类；抽象类跳过，重复 type 抛异常 */
    static void register(String type, Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        if (REGISTRY_REQUEST.containsKey(type)) {
            throw new RuntimeException(
                String.format("Fail to register, type: %s, clazz: %s", type, clazz.getName()));
        }
        REGISTRY_REQUEST.put(type, clazz);
    }
    
    /** 按 simpleName 查找已注册的 Payload 类 */
    public static Class<?> getClassByType(String type) {
        return REGISTRY_REQUEST.get(type);
    }
}
