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

package com.alibaba.nacos.api.naming.remote.request;

import com.alibaba.nacos.api.remote.request.Request;

import java.util.Set;

import static com.alibaba.nacos.api.common.Constants.Naming.NAMING_MODULE;

/**
 * 命名服务模糊监听注册/续期请求。
 *
 * <p>客户端按 {@link #groupKeyPattern} 模式订阅一组服务的变更；首次连接时 {@link #isInitializing} 为 {@code true}，{@link #receivedGroupKeys} 记录已同步的服务键集合。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchRequest extends Request {
    
    /** 是否为首次初始化监听（需全量同步）。 */
    private boolean isInitializing;
    
    /** 命名空间 ID。 */
    private String namespace;
    
    /** 服务键匹配模式（支持通配符，如 {@code DEFAULT_GROUP@@*::*}）。 */
    private String groupKeyPattern;
    
    /** 客户端已接收的服务键集合，用于增量同步去重。 */
    private Set<String> receivedGroupKeys;
    
    /** 监听类型（如 SERVICE）。 */
    private String watchType;
    
    /** 无参构造，供序列化使用。 */
    public NamingFuzzyWatchRequest() {
    }
    
    /**
     * 构造模糊监听请求。
     *
     * @param groupKeyPattern 服务键匹配模式
     * @param watchType       监听类型
     */
    public NamingFuzzyWatchRequest(String groupKeyPattern, String watchType) {
        this.watchType = watchType;
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /** 返回服务键匹配模式。 */
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /** 设置服务键匹配模式。 */
    public void setGroupKeyPattern(String groupKeyPattern) {
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /** 返回监听类型。 */
    public String getWatchType() {
        return watchType;
    }
    
    /** 设置监听类型。 */
    public void setWatchType(String watchType) {
        this.watchType = watchType;
    }
    
    /** 返回已接收的服务键集合。 */
    public Set<String> getReceivedGroupKeys() {
        return receivedGroupKeys;
    }
    
    /** 返回命名空间 ID。 */
    public String getNamespace() {
        return namespace;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /** 是否处于首次初始化阶段。 */
    public boolean isInitializing() {
        return isInitializing;
    }
    
    /** 设置是否首次初始化。 */
    public void setInitializing(boolean initializing) {
        isInitializing = initializing;
    }
    
    /** 设置已接收的服务键集合。 */
    public void setReceivedGroupKeys(Set<String> receivedGroupKeys) {
        this.receivedGroupKeys = receivedGroupKeys;
    }
    
    /** 返回命名模块标识 {@link com.alibaba.nacos.api.common.Constants.Naming#NAMING_MODULE}。 */
    @Override
    public String getModule() {
        return NAMING_MODULE;
    }
    
}
