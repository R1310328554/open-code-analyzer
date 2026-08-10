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

package com.alibaba.nacos.console.handler.impl.noop.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.console.handler.naming.ServiceHandler;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.model.form.ServiceForm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.List;

/**
 * 服务管理空实现 Handler：functionMode 为 config 时 naming 模块未启用，全部接口返回 {@link ErrorCode#API_FUNCTION_DISABLED}。
 * Noop Implementation of ServiceHandler that handles service-related operations.
 * Used when `naming` module is disabled(functionMode is `config`)
 *
 * @author xiweng.yy
 */
@org.springframework.stereotype.Service
@ConditionalOnMissingBean(value = ServiceHandler.class, ignored = ServiceNoopHandler.class)
public class ServiceNoopHandler implements ServiceHandler {
    
    /** naming 模块未启用时的统一错误提示文案 */
    private static final String MCP_NOT_ENABLED_MESSAGE =
        "Current functionMode is `config`, naming module is disabled.";
    
    /** 创建服务 — 功能未启用时抛出异常 */
    @Override
    public void createService(ServiceForm serviceForm, ServiceMetadata serviceMetadata)
        throws Exception {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 删除服务 — 功能未启用时抛出异常 */
    @Override
    public void deleteService(String namespaceId, String serviceName, String groupName)
        throws Exception {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新服务 — 功能未启用时抛出异常 */
    @Override
    public void updateService(ServiceForm serviceForm, ServiceMetadata serviceMetadata)
        throws Exception {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 获取负载均衡选择器类型列表 — 功能未启用时抛出异常 */
    @Override
    public List<String> getSelectorTypeList() throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 分页查询服务订阅者 — 功能未启用时抛出异常 */
    @Override
    public Page<SubscriberInfo> getSubscribers(int pageNo, int pageSize, String namespaceId,
        String serviceName,
        String groupName, boolean aggregation) throws Exception {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 分页查询服务列表 — 功能未启用时抛出异常 */
    @Override
    public Object getServiceList(boolean withInstances, String namespaceId, int pageNo,
        int pageSize,
        String serviceName, String groupName, boolean ignoreEmptyService) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 查询服务详情 — 功能未启用时抛出异常 */
    @Override
    public ServiceDetailInfo getServiceDetail(String namespaceId, String serviceName,
        String groupName)
        throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新集群元数据 — 功能未启用时抛出异常 */
    @Override
    public void updateClusterMetadata(String namespaceId, String groupName, String serviceName,
        String clusterName,
        ClusterMetadata clusterMetadata) throws Exception {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            MCP_NOT_ENABLED_MESSAGE);
    }
}
