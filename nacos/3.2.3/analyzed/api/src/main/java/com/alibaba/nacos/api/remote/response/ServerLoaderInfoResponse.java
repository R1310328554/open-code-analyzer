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

package com.alibaba.nacos.api.remote.response;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务端负载/加载信息响应。
 *
 * <p>携带服务端地址及 {@link #loaderMetrics} 键值对形式的负载指标，供运维或客户端了解节点运行状态。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerLoaderInfoResponse.java, v 0.1 2020年09月03日 2:46 PM liuzunfei Exp $
 */
public class ServerLoaderInfoResponse extends Response {
    
    /** 服务端节点地址。 */
    String address;
    
    /** 负载指标键值对（如连接数、线程池使用率等）。 */
    Map<String, String> loaderMetrics = new HashMap<>();
    
    /**
     * 按 key 读取单条负载指标。
     *
     * @param key 指标名称
     * @return 指标值，不存在时返回 {@code null}
     */
        return loaderMetrics.get(key);
    }
    
    /**
     * 写入或更新一条负载指标。
     *
     * @param key   指标名称
     * @param value 指标值
     */
        this.loaderMetrics.put(key, value);
    }
    
    /** 返回全部负载指标映射。 */
    public Map<String, String> getLoaderMetrics() {
        return loaderMetrics;
    }
    
    /** 替换全部负载指标映射。 */
    public void setLoaderMetrics(Map<String, String> loaderMetrics) {
        this.loaderMetrics = loaderMetrics;
    }
}
