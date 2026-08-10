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

package com.alibaba.nacos.auth.parser.grpc;

import com.alibaba.nacos.api.config.remote.request.AbstractConfigRequest;
import com.alibaba.nacos.api.config.remote.request.ConfigBatchListenRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.common.utils.ReflectUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.List;

/**
 * 配置中心 gRPC 资源解析器。
 *
 * <p>从配置相关远程请求（含批量监听）中提取 tenant（命名空间）、group 与 dataId，
 * 对非标准请求类型通过反射读取字段作为兜底。</p>
 *
 * @author xiweng.yy
 */
public class ConfigGrpcResourceParser extends AbstractGrpcResourceParser {
    
    /** {@inheritDoc} — 批量监听取首个上下文 tenant，否则读 AbstractConfigRequest 或反射字段。 */
    @Override
    protected String getNamespaceId(Request request) {
        String namespaceId = StringUtils.EMPTY;
        if (request instanceof ConfigBatchListenRequest) {
            List<ConfigBatchListenRequest.ConfigListenContext> configListenContexts =
                ((ConfigBatchListenRequest) request)
                    .getConfigListenContexts();
            if (!configListenContexts.isEmpty()) {
                namespaceId = ((ConfigBatchListenRequest) request).getConfigListenContexts().get(0)
                    .getTenant();
            }
        } else if (request instanceof AbstractConfigRequest) {
            namespaceId = ((AbstractConfigRequest) request).getTenant();
        } else {
            // 非标准配置请求，反射读取 tenant 字段
            namespaceId = (String) ReflectUtils.getFieldValue(request, "tenant", StringUtils.EMPTY);
        }
        return StringUtils.isBlank(namespaceId) ? StringUtils.EMPTY : namespaceId;
    }
    
    /** {@inheritDoc} — 优先从 AbstractConfigRequest 读取 group，否则反射读取。 */
    @Override
    protected String getGroup(Request request) {
        String groupName;
        if (request instanceof AbstractConfigRequest) {
            groupName = ((AbstractConfigRequest) request).getGroup();
        } else {
            groupName = (String) ReflectUtils
                .getFieldValue(request, com.alibaba.nacos.api.common.Constants.GROUP,
                    StringUtils.EMPTY);
        }
        return StringUtils.isBlank(groupName) ? StringUtils.EMPTY : groupName;
    }
    
    /** {@inheritDoc} — 优先从 AbstractConfigRequest 读取 dataId，否则反射读取。 */
    @Override
    protected String getResourceName(Request request) {
        String dataId;
        if (request instanceof AbstractConfigRequest) {
            dataId = ((AbstractConfigRequest) request).getDataId();
        } else {
            dataId = (String) ReflectUtils
                .getFieldValue(request, com.alibaba.nacos.api.common.Constants.DATA_ID,
                    StringUtils.EMPTY);
        }
        return StringUtils.isBlank(dataId) ? StringUtils.EMPTY : dataId;
    }
}
