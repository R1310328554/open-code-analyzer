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

package com.alibaba.nacos.core.remote.grpc.filter;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link NacosGrpcServerTransportFilter} SPI 加载器：按类型筛选已注册的传输过滤器。
 * Service Loader for nacos grpc server transport filter.
 *
 * @author xiweng.yy
 */
public class NacosGrpcServerTransportFilterServiceLoader {
    
    /**
     * 按通道类型加载传输过滤器列表。
     * Load Server Interceptors by type.
     *
     * @param type should be `CLUSTER` or `SDK`
     * @return Server Interceptors for type
     */
    public static List<NacosGrpcServerTransportFilter> loadServerTransportFilters(String type) {
        List<NacosGrpcServerTransportFilter> result = new LinkedList<>();
        // 遍历 SPI 注册的传输过滤器并按 type() 匹配
        for (NacosGrpcServerTransportFilter each : NacosServiceLoader
            .load(NacosGrpcServerTransportFilter.class)) {
            if (StringUtils.equals(type, each.type())) {
                result.add(each);
            }
        }
        return result;
    }
}
