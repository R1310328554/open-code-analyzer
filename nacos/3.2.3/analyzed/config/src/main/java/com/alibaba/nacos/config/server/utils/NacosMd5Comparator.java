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
import com.alibaba.nacos.config.server.service.ConfigCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.VIPSERVER_TAG;

/**
 * Nacos 默认 MD5 比对器：逐条调用 {@link ConfigCacheService#isUptodate} 判断客户端 MD5 是否过期，支持 VIP 标签路由。
 * The type Nacos md5 comparator.
 *
 * @author Sunrisea
 */
public class NacosMd5Comparator implements Md5Comparator {
    
    /** SPI 注册名，对应 nacos.config.cache.type=nacos */
    @Override
    public String getName() {
        return "nacos";
    }
    
    /**
     * 遍历 clientMd5Map，将服务端 MD5 与客户端不一致的 GroupKey 放入变更集合返回。
     */
    @Override
    public Map<String, ConfigListenState> compareMd5(HttpServletRequest request,
        HttpServletResponse response,
        Map<String, ConfigListenState> clientMd5Map) {
        HashMap<String, ConfigListenState> changedGroupKeys = new HashMap<>(clientMd5Map.size());
        String tag = request.getHeader(VIPSERVER_TAG);
        for (Map.Entry<String, ConfigListenState> entry : clientMd5Map.entrySet()) {
            String groupKey = entry.getKey();
            String clientMd5 = entry.getValue().getMd5();
            String ip = RequestUtil.getRemoteIp(request);
            boolean isUptodate = ConfigCacheService.isUptodate(groupKey, clientMd5, ip, tag);
            if (!isUptodate) {
                changedGroupKeys.put(entry.getKey(), entry.getValue());
            }
        }
        return changedGroupKeys;
    }
}
