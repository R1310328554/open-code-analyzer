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

package com.alibaba.nacos.naming.remote.rpc.handler;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.remote.request.ServiceListRequest;
import com.alibaba.nacos.api.naming.remote.response.ServiceListResponse;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.namespace.filter.NamespaceValidation;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.paramcheck.impl.ServiceListRequestParamExtractor;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.utils.ServiceUtil;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 服务列表查询 RPC 请求处理器。
 *
 * <p>按命名空间与分组分页返回已注册服务名列表。</p>
 *
 * @author xiweng.yy
 */
@Since("2.0.0")
@Component
public class ServiceListRequestHandler
    extends RequestHandler<ServiceListRequest, ServiceListResponse> {
    
    @Override
    @NamespaceValidation
    @TpsControl(pointName = "RemoteNamingServiceListQuery", name = "RemoteNamingServiceListQuery")
    @Secured(action = ActionTypes.READ)
    @ExtractorManager.Extractor(rpcExtractor = ServiceListRequestParamExtractor.class)
    /** 查询单例服务集合并按分页参数返回服务名。 */
    public ServiceListResponse handle(ServiceListRequest request, RequestMeta meta)
        throws NacosException {
        Collection<Service> serviceSet =
            ServiceManager.getInstance().getSingletons(request.getNamespace());
        ServiceListResponse result =
            ServiceListResponse.buildSuccessResponse(0, new LinkedList<>());
        if (!serviceSet.isEmpty()) {
            Collection<String> serviceNameSet =
                selectServiceWithGroupName(serviceSet, request.getGroupName());
            // TODO 后续支持按 Selector 过滤服务列表
            List<String> serviceNameList = ServiceUtil
                .pageServiceName(request.getPageNo(), request.getPageSize(), serviceNameSet);
            result.setCount(serviceNameSet.size());
            result.setServiceNames(serviceNameList);
        }
        return result;
    }
    
    /** 按分组名过滤并收集 groupedServiceName。 */
    private Collection<String> selectServiceWithGroupName(Collection<Service> serviceSet,
        String groupName) {
        Collection<String> result = new HashSet<>(serviceSet.size());
        for (Service each : serviceSet) {
            if (Objects.equals(groupName, each.getGroup())) {
                result.add(each.getGroupedServiceName());
            }
        }
        return result;
    }
    
}
