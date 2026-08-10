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

package com.alibaba.nacos.core.paramcheck.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.remote.request.InstanceRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.core.paramcheck.AbstractRpcParamExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * 单实例注册 gRPC 请求参数提取器：合并服务级与实例级 IP、端口、集群、元数据字段。
 * Param extractor for {@link InstanceRequest}.
 *
 * @author zhuoguang
 */
public class InstanceRequestParamExtractor extends AbstractRpcParamExtractor {
    
    /** 从 {@link InstanceRequest} 提取命名空间、服务名及实例详情。 */
    @Override
    public List<ParamInfo> extractParam(Request request) {
        InstanceRequest req = (InstanceRequest) request;
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setNamespaceId(req.getNamespace());
        paramInfo.setServiceName(req.getServiceName());
        paramInfo.setGroup(req.getGroupName());
        Instance instance = req.getInstance();
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        if (instance == null) {
            paramInfos.add(paramInfo);
            return paramInfos;
        }
        paramInfo.setIp(instance.getIp());
        paramInfo.setPort(String.valueOf(instance.getPort()));
        paramInfo.setCluster(instance.getClusterName());
        paramInfo.setMetadata(instance.getMetadata());
        paramInfos.add(paramInfo);
        return paramInfos;
    }
}
