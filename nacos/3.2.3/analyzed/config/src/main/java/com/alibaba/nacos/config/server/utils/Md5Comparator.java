/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.config.server.model.ConfigListenState;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * MD5 比对 SPI 接口：长轮询场景下比较客户端上报 MD5 与服务端缓存是否一致，返回发生变更的配置键集合。
 * The interface Md5 comparator.
 *
 * @author Sunrisea
 */
public interface Md5Comparator {
    
    /**
     * 返回比对器实现名称，与 {@code nacos.config.cache.type} 配置项匹配。
     * Gets md 5 comparator name.
     *
     * @return the md 5 comparator name
     */
    public String getName();
    
    /**
     * 遍历客户端 MD5 映射，筛出服务端已变更的 GroupKey 及其监听状态。
     * Compare md 5 list.
     *
     * @param request      the request
     * @param response     the response
     * @param clientMd5Map the client md 5 map
     * @return the list
     */
    public Map<String, ConfigListenState> compareMd5(HttpServletRequest request,
        HttpServletResponse response,
        Map<String, ConfigListenState> clientMd5Map);
}
