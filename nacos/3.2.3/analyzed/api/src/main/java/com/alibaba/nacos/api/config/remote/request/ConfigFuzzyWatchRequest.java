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

package com.alibaba.nacos.api.config.remote.request;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.request.Request;

import java.util.Set;

/**
 * 批量模糊监听配置的客户端请求。
 *
 * <p>携带 groupKey 模式、已接收键集合及监听类型，向服务端注册或同步模糊订阅。</p>
 *
 * @author stone-98
 * @date 2024/3/4
 */
public class ConfigFuzzyWatchRequest extends Request {
    
    /** 模糊匹配的 groupKey 模式。 */
    private String groupKeyPattern;
    
    /** 客户端已知的 groupKey 集合，用于差异同步。 */
    private Set<String> receivedGroupKeys;
    
    /** 监听类型（注册、取消等）。 */
    private String watchType;
    
    /** 客户端是否处于模糊监听初始化阶段。 */
    private boolean isInitializing;
    
    /** 无参构造。 */
    public ConfigFuzzyWatchRequest() {
    }
    
    /** 获取 groupKey 匹配模式。 */
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /** 设置 groupKey 匹配模式。 */
    public void setGroupKeyPattern(String groupKeyPattern) {
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /** 获取已接收的 groupKey 集合。 */
    public Set<String> getReceivedGroupKeys() {
        return receivedGroupKeys;
    }
    
    /** 设置已接收的 groupKey 集合。 */
    public void setReceivedGroupKeys(Set<String> receivedGroupKeys) {
        this.receivedGroupKeys = receivedGroupKeys;
    }
    
    /** 获取监听类型。 */
    public String getWatchType() {
        return watchType;
    }
    
    /** 设置监听类型。 */
    public void setWatchType(String watchType) {
        this.watchType = watchType;
    }
    
    /** 是否处于初始化阶段。 */
    public boolean isInitializing() {
        return isInitializing;
    }
    
    /** 设置初始化标志。 */
    public void setInitializing(boolean initializing) {
        isInitializing = initializing;
    }
    
    /**
     * 返回所属模块名。
     *
     * @return 配置模块标识
     */
    @Override
    public String getModule() {
        return Constants.Config.CONFIG_MODULE;
    }
    
}
