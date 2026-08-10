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

package com.alibaba.nacos.client.naming.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.alibaba.nacos.common.lifecycle.Closeable;

import java.util.List;

/**
 * 命名服务远程代理接口。
 *
 * <p>封装实例注册/注销、服务订阅、服务 CRUD 及健康检查等与服务端交互的能力，由 {@link NamingClientProxyDelegate} 委托至 HTTP 或 gRPC 实现。</p>
 *
 * @author xiweng.yy
 */
public interface NamingClientProxy extends Closeable {
    
    /**
     * 向指定服务注册实例。
     *
     * <p>根据实例类型（临时/持久）由具体代理选择 HTTP 或 gRPC 通道。</p>
     *
     * @param serviceName name of service
     * @param groupName   group of service
     * @param instance    instance to register
     * @throws NacosException nacos exception
     */
    void registerService(String serviceName, String groupName, Instance instance)
        throws NacosException;
    
    /**
     * 批量注册实例到指定服务。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instances   instance
     * @throws NacosException nacos exception
     * @since 2.1.1
     */
    void batchRegisterService(String serviceName, String groupName, List<Instance> instances)
        throws NacosException;
    
    /**
     * 批量注销指定服务下的实例。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instances   deRegister instance
     * @throws NacosException nacos exception
     * @since 2.2.0
     */
    void batchDeregisterService(String serviceName, String groupName, List<Instance> instances)
        throws NacosException;
    
    /**
     * 从服务中注销实例。
     *
     * @param serviceName name of service
     * @param groupName   group name
     * @param instance    instance
     * @throws NacosException nacos exception
     */
    void deregisterService(String serviceName, String groupName, Instance instance)
        throws NacosException;
    
    /**
     * 更新服务下的实例信息。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instance    instance
     * @throws NacosException nacos exception
     */
    void updateInstance(String serviceName, String groupName, Instance instance)
        throws NacosException;
    
    /**
     * 查询服务实例列表。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters
     * @param healthyOnly healthy only
     * @return service info
     * @throws NacosException nacos exception
     */
    ServiceInfo queryInstancesOfService(String serviceName, String groupName, String clusters,
        boolean healthyOnly)
        throws NacosException;
    
    /**
     * 查询服务元数据。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @return service
     * @throws NacosException nacos exception
     */
    Service queryService(String serviceName, String groupName) throws NacosException;
    
    /**
     * 创建服务。
     *
     * @param service  service
     * @param selector selector
     * @throws NacosException nacos exception
     */
    void createService(Service service, AbstractSelector selector) throws NacosException;
    
    /**
     * 删除服务。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @return true if delete ok
     * @throws NacosException nacos exception
     */
    boolean deleteService(String serviceName, String groupName) throws NacosException;
    
    /**
     * 更新服务配置。
     *
     * @param service  service
     * @param selector selector
     * @throws NacosException nacos exception
     */
    void updateService(Service service, AbstractSelector selector) throws NacosException;
    
    /**
     * 分页获取服务名称列表。
     *
     * @param pageNo    page number
     * @param pageSize  size per page
     * @param groupName group name of service
     * @param selector  selector
     * @return list of service
     * @throws NacosException nacos exception
     */
    ListView<String> getServiceList(int pageNo, int pageSize, String groupName,
        AbstractSelector selector)
        throws NacosException;
    
    /**
     * 订阅服务实例变更。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters, current only support subscribe all clusters, maybe deprecated
     * @return current service info of subscribe service
     * @throws NacosException nacos exception
     */
    ServiceInfo subscribe(String serviceName, String groupName, String clusters)
        throws NacosException;
    
    /**
     * 取消订阅服务。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters, current only support subscribe all clusters, maybe deprecated
     * @throws NacosException nacos exception
     */
    void unsubscribe(String serviceName, String groupName, String clusters) throws NacosException;
    
    /**
     * 判断服务是否已订阅。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters, current only support subscribe all clusters, maybe deprecated
     * @return {@code true} if subscribed, otherwise {@code false}
     * @throws NacosException nacos exception
     */
    boolean isSubscribed(String serviceName, String groupName, String clusters)
        throws NacosException;
    
    /**
     * 检查命名服务端是否健康可用。
     *
     * @return true if server is healthy
     */
    boolean serverHealthy();
}
