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

package com.alibaba.nacos.plugin.config.model;

import com.alibaba.nacos.plugin.config.constants.ConfigChangePointCutTypes;

import java.util.HashMap;

/**
 * 配置变更插件请求模型。
 *
 * <p>封装切点类型与键值对形式的请求参数，供 {@link com.alibaba.nacos.plugin.config.spi.ConfigChangePluginService}
 * 在 execute 阶段读取上下文信息。</p>
 *
 * @author liyunfei
 */
public class ConfigChangeRequest {
    
    /** 本次配置变更对应的切点类型。 */
    private ConfigChangePointCutTypes requestType;
    
    /** 请求附加参数，初始容量 8。 */
    private HashMap<String, Object> requestArgs = new HashMap<>(8);
    
    /**
     * 构造指定切点类型的配置变更请求。
     *
     * @param requestType 切点类型
     */
    public ConfigChangeRequest(ConfigChangePointCutTypes requestType) {
        this.requestType = requestType;
    }
    
    /**
     * 获取请求切点类型。
     *
     * @return 切点类型
     */
    public ConfigChangePointCutTypes getRequestType() {
        return requestType;
    }
    
    /**
     * 设置请求参数。
     *
     * @param key 参数键
     * @param value 参数值
     */
    public void setArg(String key, Object value) {
        requestArgs.put(key, value);
    }
    
    /**
     * 获取指定键的请求参数。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 {@code null}
     */
    public Object getArg(String key) {
        return requestArgs.getOrDefault(key, null);
    }
    
    /**
     * 获取全部请求参数。
     *
     * @return 参数 Map
     */
    public HashMap<String, Object> getRequestArgs() {
        return requestArgs;
    }
}
