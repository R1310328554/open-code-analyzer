/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.model;

import java.util.Map;

/**
 * Nacos 配置监听器查询结果。
 *
 * <p>可按配置维度或 IP 维度查询当前监听该配置的客户端列表及状态。</p>
 *
 * @author xiweng.yy
 */
public class ConfigListenerInfo {
    
    /** 按配置（dataId/group）查询监听器。 */
    public static final String QUERY_TYPE_CONFIG = "config";
    
    /** 按客户端 IP 查询监听器。 */
    public static final String QUERY_TYPE_IP = "ip";
    
    /** 查询类型，见 {@link #QUERY_TYPE_CONFIG} 与 {@link #QUERY_TYPE_IP}。 */
    private String queryType;
    
    /** 监听器标识到状态的映射。 */
    private Map<String, String> listenersStatus;
    
    /** 获取查询类型。 */
    public String getQueryType() {
        return queryType;
    }
    
    /** 设置查询类型。 */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    
    /** 获取监听器状态映射。 */
    public Map<String, String> getListenersStatus() {
        return listenersStatus;
    }
    
    /** 设置监听器状态映射。 */
    public void setListenersStatus(Map<String, String> listenersStatus) {
        this.listenersStatus = listenersStatus;
    }
}
