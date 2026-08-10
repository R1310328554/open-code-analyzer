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

package com.alibaba.nacos.naming.cluster.remote.request;

import com.alibaba.nacos.api.remote.Payload;
import com.alibaba.nacos.core.cluster.remote.request.AbstractClusterRequest;

import java.util.HashSet;
import java.util.Set;

/**
 * Naming 集群请求 Payload 类型注册表。
 *
 * <p>集中声明可被远程 RPC 反序列化的 {@link Payload} 实现类。</p>
 *
 * @author shiyiyue
 */
public class RequestRegistry {
    
    /** 已注册的请求 Payload 类型集合。 */
    private static Set<Class<? extends Payload>> payloads = registryPayload();
    
    /** 初始化并返回 Naming 集群请求类型集合。 */
    private static Set<Class<? extends Payload>> registryPayload() {
        HashSet<Class<? extends Payload>> payloads = new HashSet<>();
        payloads.add(AbstractClusterRequest.class);
        payloads.add(DistroDataRequest.class);
        return payloads;
    }
    
    /** 获取全部已注册的请求 Payload 类型。 */
    public static final Set<Class<? extends Payload>> getPayloads() {
        return payloads;
    }
}
