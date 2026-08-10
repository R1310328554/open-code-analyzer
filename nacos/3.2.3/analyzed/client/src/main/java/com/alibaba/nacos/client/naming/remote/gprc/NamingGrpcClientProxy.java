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

package com.alibaba.nacos.client.naming.remote.gprc;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityStatus;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.remote.NamingRemoteConstants;
import com.alibaba.nacos.api.naming.remote.request.AbstractNamingRequest;
import com.alibaba.nacos.api.naming.remote.request.BatchInstanceRequest;
import com.alibaba.nacos.api.naming.remote.request.InstanceRequest;
import com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchRequest;
import com.alibaba.nacos.api.naming.remote.request.PersistentInstanceRequest;
import com.alibaba.nacos.api.naming.remote.request.ServiceListRequest;
import com.alibaba.nacos.api.naming.remote.request.ServiceQueryRequest;
import com.alibaba.nacos.api.naming.remote.request.SubscribeServiceRequest;
import com.alibaba.nacos.api.naming.remote.response.BatchInstanceResponse;
import com.alibaba.nacos.api.naming.remote.response.NamingFuzzyWatchResponse;
import com.alibaba.nacos.api.naming.remote.response.QueryServiceResponse;
import com.alibaba.nacos.api.naming.remote.response.ServiceListResponse;
import com.alibaba.nacos.api.naming.remote.response.SubscribeServiceResponse;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.alibaba.nacos.api.selector.SelectorType;
import com.alibaba.nacos.client.address.ServerListChangeEvent;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.monitor.MetricsMonitor;
import com.alibaba.nacos.client.naming.cache.NamingFuzzyWatchServiceListHolder;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.remote.AbstractNamingClientProxy;
import com.alibaba.nacos.client.naming.remote.gprc.redo.NamingGrpcRedoService;
import com.alibaba.nacos.client.naming.remote.gprc.redo.data.BatchInstanceRedoData;
import com.alibaba.nacos.client.naming.remote.gprc.redo.data.InstanceRedoData;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.AppNameUtils;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.client.RpcClient;
import com.alibaba.nacos.common.remote.client.RpcClientConfigFactory;
import com.alibaba.nacos.common.remote.client.RpcClientFactory;
import com.alibaba.nacos.common.remote.client.ServerListFactory;
import com.alibaba.nacos.common.remote.client.grpc.GrpcClientConfig;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.remote.RemoteConstants.MONITOR_LABEL_NONE;
import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 命名服务 gRPC 远程代理。
 *
 * <p>通过 {@link RpcClient} 与命名服务端通信，负责临时实例注册/注销、批量注册、服务订阅、模糊监听及服务列表查询；集成 {@link NamingGrpcRedoService} 实现断线重连后的 redo 补偿。</p>
 *
 * @author xiweng.yy
 */
public class NamingGrpcClientProxy extends AbstractNamingClientProxy {
    
    /** 当前命名空间 ID。 */
    private final String namespaceId;
    
    /** 本 gRPC 客户端唯一标识，用于 RpcClientFactory 注册与销毁。 */
    private final String uuid;
    
    /** RPC 请求超时毫秒数，-1 表示使用默认值。 */
    private final Long requestTimeout;
    
    /** 底层 gRPC RPC 客户端。 */
    private final RpcClient rpcClient;
    
    /** 断线重连 redo 服务，缓存注册与订阅状态。 */
    private final NamingGrpcRedoService redoService;
    
    /** 是否启用客户端请求失败指标上报。 */
    private boolean enableClientMetrics = true;
    
    public NamingGrpcClientProxy(String namespaceId, SecurityProxy securityProxy,
        ServerListFactory serverListFactory,
        NacosClientProperties properties, ServiceInfoHolder serviceInfoHolder,
        NamingFuzzyWatchServiceListHolder namingFuzzyWatchServiceListHolder)
        throws NacosException {
        super(securityProxy);
        this.namespaceId = namespaceId;
        this.uuid = UUID.randomUUID().toString();
        this.requestTimeout =
            Long.parseLong(properties.getProperty(CommonParams.NAMING_REQUEST_TIMEOUT, "-1"));
        Map<String, String> labels = new HashMap<>();
        labels.put(RemoteConstants.LABEL_SOURCE, RemoteConstants.LABEL_SOURCE_SDK);
        labels.put(RemoteConstants.LABEL_MODULE, RemoteConstants.LABEL_MODULE_NAMING);
        labels.put(Constants.APPNAME, AppNameUtils.getAppName());
        namingFuzzyWatchServiceListHolder.registerNamingGrpcClientProxy(this);
        GrpcClientConfig grpcClientConfig = RpcClientConfigFactory.getInstance()
            .createGrpcClientConfig(properties.asProperties(), labels);
        this.rpcClient = RpcClientFactory.createClient(uuid, ConnectionType.GRPC, grpcClientConfig);
        this.redoService =
            new NamingGrpcRedoService(this, namingFuzzyWatchServiceListHolder, properties);
        this.enableClientMetrics = Boolean.parseBoolean(
            properties.getProperty(PropertyKeyConst.ENABLE_CLIENT_METRICS, "true"));
        NAMING_LOGGER.info("Create naming rpc client for uuid->{}", uuid);
        start(serverListFactory, serviceInfoHolder, namingFuzzyWatchServiceListHolder);
    }
    
    /** 启动 RPC 客户端、注册推送/模糊监听处理器并订阅服务端列表变更。 */
    private void start(ServerListFactory serverListFactory, ServiceInfoHolder serviceInfoHolder,
        NamingFuzzyWatchServiceListHolder namingFuzzyWatchServiceListHolder)
        throws NacosException {
        rpcClient.serverListFactory(serverListFactory);
        rpcClient.registerConnectionListener(redoService);
        rpcClient.registerServerRequestHandler(new NamingPushRequestHandler(serviceInfoHolder));
        rpcClient.registerServerRequestHandler(
            new NamingFuzzyWatchNotifyRequestHandler(namingFuzzyWatchServiceListHolder));
        rpcClient.start();
        namingFuzzyWatchServiceListHolder.start();
        NotifyCenter.registerSubscriber(this);
    }
    
    @Override
    public void onEvent(ServerListChangeEvent event) {
        rpcClient.onServerListChange();
    }
    
    @Override
    public Class<? extends Event> subscribeType() {
        return ServerListChangeEvent.class;
    }
    
    @Override
    public void registerService(String serviceName, String groupName, Instance instance)
        throws NacosException {
        NAMING_LOGGER.info("[REGISTER-SERVICE] {} registering service {} with instance {}",
            namespaceId, serviceName,
            instance);
        if (instance.isEphemeral()) {
            registerServiceForEphemeral(serviceName, groupName, instance);
        } else {
            doRegisterServiceForPersistent(serviceName, groupName, instance);
        }
    }
    
    /** 临时实例注册：先缓存 redo 数据再发起 RPC。 */
    private void registerServiceForEphemeral(String serviceName, String groupName,
        Instance instance)
        throws NacosException {
        redoService.cacheInstanceForRedo(serviceName, groupName, instance);
        doRegisterService(serviceName, groupName, instance);
    }
    
    @Override
    public void batchRegisterService(String serviceName, String groupName, List<Instance> instances)
        throws NacosException {
        redoService.cacheInstanceForRedo(serviceName, groupName, instances);
        doBatchRegisterService(serviceName, groupName, instances);
    }
    
    @Override
    public void batchDeregisterService(String serviceName, String groupName,
        List<Instance> instances)
        throws NacosException {
        synchronized (redoService.getRegisteredInstances()) {
            List<Instance> retainInstance = getRetainInstance(serviceName, groupName, instances);
            batchRegisterService(serviceName, groupName, retainInstance);
        }
    }
    
    /**
     * 计算批量注销后仍需保留的实例列表。
     *
     * @param serviceName         service name
     * @param groupName           group name
     * @param deRegisterInstances deregister instance list
     * @return instance list that need to be retained.
     */
    private List<Instance> getRetainInstance(String serviceName, String groupName,
        List<Instance> deRegisterInstances)
        throws NacosException {
        if (CollectionUtils.isEmpty(deRegisterInstances)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                String.format(
                    "[Batch deRegistration] need deRegister instance is empty, instances: %s,",
                    deRegisterInstances));
        }
        String combinedServiceName = NamingUtils.getGroupedName(serviceName, groupName);
        InstanceRedoData instanceRedoData =
            redoService.getRegisteredInstancesByKey(combinedServiceName);
        if (!(instanceRedoData instanceof BatchInstanceRedoData)) {
            throw new NacosException(NacosException.INVALID_PARAM, String.format(
                "[Batch deRegistration] batch deRegister is not BatchInstanceRedoData type , instances: %s,",
                deRegisterInstances));
        }
        
        BatchInstanceRedoData batchInstanceRedoData = (BatchInstanceRedoData) instanceRedoData;
        List<Instance> allRedoInstances = batchInstanceRedoData.getInstances();
        if (CollectionUtils.isEmpty(allRedoInstances)) {
            throw new NacosException(NacosException.INVALID_PARAM, String.format(
                "[Batch deRegistration] not found all registerInstance , serviceName：%s , groupName: %s",
                serviceName, groupName));
        }
        
        Map<Instance, Instance> deRegisterInstanceMap = deRegisterInstances.stream()
            .collect(Collectors.toMap(Function.identity(), Function.identity()));
        List<Instance> retainInstances = new ArrayList<>();
        for (Instance redoInstance : allRedoInstances) {
            boolean needRetained = true;
            Iterator<Map.Entry<Instance, Instance>> it =
                deRegisterInstanceMap.entrySet().iterator();
            while (it.hasNext()) {
                Instance deRegisterInstance = it.next().getKey();
                // 仅比较 IP 与端口：redo 缓存中 instanceId 等字段可能为空
                if (compareIpAndPort(deRegisterInstance, redoInstance)) {
                    needRetained = false;
                    // 移除已匹配项以加速后续比较
                    it.remove();
                    break;
                }
            }
            if (needRetained) {
                retainInstances.add(redoInstance);
            }
        }
        return retainInstances;
    }
    
    private boolean compareIpAndPort(Instance deRegisterInstance, Instance redoInstance) {
        return ((deRegisterInstance.getIp().equals(redoInstance.getIp()))
            && (deRegisterInstance.getPort() == redoInstance.getPort()));
    }
    
    /**
     * 执行批量注册 RPC 并标记 redo 为已注册。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instances   instances
     * @throws NacosException NacosException
     */
    public void doBatchRegisterService(String serviceName, String groupName,
        List<Instance> instances)
        throws NacosException {
        BatchInstanceRequest request = new BatchInstanceRequest(namespaceId, serviceName, groupName,
            NamingRemoteConstants.BATCH_REGISTER_INSTANCE, instances);
        requestToServer(request, BatchInstanceResponse.class);
        redoService.instanceRegistered(serviceName, groupName);
    }
    
    /**
     * 执行临时实例注册 RPC 并标记 redo 为已注册。
     *
     * @param serviceName name of service
     * @param groupName   group of service
     * @param instance    instance to register
     * @throws NacosException nacos exception
     */
    public void doRegisterService(String serviceName, String groupName, Instance instance)
        throws NacosException {
        InstanceRequest request = new InstanceRequest(namespaceId, serviceName, groupName,
            NamingRemoteConstants.REGISTER_INSTANCE, instance);
        requestToServer(request, Response.class);
        redoService.instanceRegistered(serviceName, groupName);
    }
    
    /**
     * 执行持久实例注册 RPC（不经 redo 缓存）。
     *
     * @param serviceName name of service
     * @param groupName   group of service
     * @param instance    instance to register
     * @throws NacosException nacos exception
     */
    public void doRegisterServiceForPersistent(String serviceName, String groupName,
        Instance instance)
        throws NacosException {
        PersistentInstanceRequest request =
            new PersistentInstanceRequest(namespaceId, serviceName, groupName,
                NamingRemoteConstants.REGISTER_INSTANCE, instance);
        requestToServer(request, Response.class);
    }
    
    @Override
    public void deregisterService(String serviceName, String groupName, Instance instance)
        throws NacosException {
        NAMING_LOGGER.info("[DEREGISTER-SERVICE] {} deregistering service {} with instance: {}",
            namespaceId,
            serviceName, instance);
        if (instance.isEphemeral()) {
            deregisterServiceForEphemeral(serviceName, groupName, instance);
        } else {
            doDeregisterServiceForPersistent(serviceName, groupName, instance);
        }
    }
    
    /** 临时实例注销：批量场景走差量保留，否则标记 redo 并发起 RPC。 */
    private void deregisterServiceForEphemeral(String serviceName, String groupName,
        Instance instance)
        throws NacosException {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        InstanceRedoData instanceRedoData = redoService.getRegisteredInstancesByKey(key);
        if (instanceRedoData instanceof BatchInstanceRedoData) {
            List<Instance> instances = new ArrayList<>();
            if (null != instance) {
                instances.add(instance);
            }
            batchDeregisterService(serviceName, groupName, instances);
        } else {
            redoService.instanceDeregister(serviceName, groupName);
            doDeregisterService(serviceName, groupName, instance);
        }
    }
    
    /**
     * 执行临时实例注销 RPC 并更新 redo 状态。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instance    instance
     * @throws NacosException nacos exception
     */
    public void doDeregisterService(String serviceName, String groupName, Instance instance)
        throws NacosException {
        InstanceRequest request = new InstanceRequest(namespaceId, serviceName, groupName,
            NamingRemoteConstants.DE_REGISTER_INSTANCE, instance);
        requestToServer(request, Response.class);
        redoService.instanceDeregistered(serviceName, groupName);
    }
    
    /**
     * 执行持久实例注销 RPC。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instance    instance
     * @throws NacosException nacos exception
     */
    public void doDeregisterServiceForPersistent(String serviceName, String groupName,
        Instance instance)
        throws NacosException {
        PersistentInstanceRequest request =
            new PersistentInstanceRequest(namespaceId, serviceName, groupName,
                NamingRemoteConstants.DE_REGISTER_INSTANCE, instance);
        requestToServer(request, Response.class);
    }
    
    @Override
    public void updateInstance(String serviceName, String groupName, Instance instance)
        throws NacosException {
    }
    
    @Override
    public ServiceInfo queryInstancesOfService(String serviceName, String groupName,
        String clusters,
        boolean healthyOnly) throws NacosException {
        ServiceQueryRequest request = new ServiceQueryRequest(namespaceId, serviceName, groupName);
        request.setCluster(clusters);
        request.setHealthyOnly(healthyOnly);
        QueryServiceResponse response = requestToServer(request, QueryServiceResponse.class);
        return response.getServiceInfo();
    }
    
    @Override
    public Service queryService(String serviceName, String groupName) throws NacosException {
        return null;
    }
    
    @Override
    public void createService(Service service, AbstractSelector selector) throws NacosException {
    }
    
    @Override
    public boolean deleteService(String serviceName, String groupName) throws NacosException {
        return false;
    }
    
    @Override
    public void updateService(Service service, AbstractSelector selector) throws NacosException {
    }
    
    @Override
    public ListView<String> getServiceList(int pageNo, int pageSize, String groupName,
        AbstractSelector selector)
        throws NacosException {
        ServiceListRequest request =
            new ServiceListRequest(namespaceId, groupName, pageNo, pageSize);
        if (selector != null) {
            if (SelectorType.valueOf(selector.getType()) == SelectorType.label) {
                request.setSelector(JacksonUtils.toJson(selector));
            }
        }
        ServiceListResponse response = requestToServer(request, ServiceListResponse.class);
        ListView<String> result = new ListView<>();
        result.setCount(response.getCount());
        result.setData(response.getServiceNames());
        return result;
    }
    
    @Override
    public ServiceInfo subscribe(String serviceName, String groupName, String clusters)
        throws NacosException {
        NAMING_LOGGER.info("[GRPC-SUBSCRIBE] service:{}, group:{}, cluster:{} ", serviceName,
            groupName, clusters);
        redoService.cacheSubscriberForRedo(serviceName, groupName, clusters);
        return doSubscribe(serviceName, groupName, clusters);
    }
    
    /**
     * 执行订阅 RPC 并标记订阅 redo 为已注册。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters, current only support subscribe all clusters, maybe deprecated
     * @return current service info of subscribe service
     * @throws NacosException nacos exception
     */
    public ServiceInfo doSubscribe(String serviceName, String groupName, String clusters)
        throws NacosException {
        SubscribeServiceRequest request =
            new SubscribeServiceRequest(namespaceId, groupName, serviceName, clusters,
                true);
        SubscribeServiceResponse response =
            requestToServer(request, SubscribeServiceResponse.class);
        redoService.subscriberRegistered(serviceName, groupName, clusters);
        return response.getServiceInfo();
    }
    
    @Override
    public void unsubscribe(String serviceName, String groupName, String clusters)
        throws NacosException {
        NAMING_LOGGER.info("[GRPC-UNSUBSCRIBE] service:{}, group:{}, cluster:{} ", serviceName,
            groupName, clusters);
        redoService.subscriberDeregister(serviceName, groupName, clusters);
        doUnsubscribe(serviceName, groupName, clusters);
    }
    
    @Override
    public boolean isSubscribed(String serviceName, String groupName, String clusters)
        throws NacosException {
        return redoService.isSubscriberRegistered(serviceName, groupName, clusters);
    }
    
    /**
     * 执行取消订阅 RPC 并清理 redo 缓存。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters, current only support subscribe all clusters, maybe deprecated
     * @throws NacosException nacos exception
     */
    public void doUnsubscribe(String serviceName, String groupName, String clusters)
        throws NacosException {
        SubscribeServiceRequest request =
            new SubscribeServiceRequest(namespaceId, groupName, serviceName, clusters,
                false);
        requestToServer(request, SubscribeServiceResponse.class);
        redoService.removeSubscriberForRedo(serviceName, groupName, clusters);
    }
    
    @Override
    public boolean serverHealthy() {
        return rpcClient.isRunning();
    }
    
    /**
     * 判断命名服务端是否支持指定能力键。
     *
     * @param abilityKey ability key
     * @return true if supported, otherwise false
     */
    public boolean isAbilitySupportedByServer(AbilityKey abilityKey) {
        return rpcClient.getConnectionAbility(abilityKey) == AbilityStatus.SUPPORTED;
    }
    
    /**
     * 发送模糊监听 RPC 请求。
     *
     * @param namingFuzzyWatchRequest namingFuzzyWatchRequest
     * @throws NacosException nacos exception
     */
    public NamingFuzzyWatchResponse fuzzyWatchRequest(
        NamingFuzzyWatchRequest namingFuzzyWatchRequest)
        throws NacosException {
        return requestToServer(namingFuzzyWatchRequest, NamingFuzzyWatchResponse.class);
    }
    
    private <T extends Response> T requestToServer(Request request, Class<T> responseClass)
        throws NacosException {
        Response response = null;
        try {
            if (request instanceof AbstractNamingRequest) {
                request.putAllHeader(
                    getSecurityHeaders(((AbstractNamingRequest) request).getNamespace(),
                        ((AbstractNamingRequest) request).getGroupName(),
                        ((AbstractNamingRequest) request).getServiceName()));
            } else if (request instanceof NamingFuzzyWatchRequest) {
                request.putAllHeader(
                    getSecurityHeaders(((NamingFuzzyWatchRequest) request).getNamespace(), null,
                        null));
            } else {
                throw new NacosException(400, "unknown naming request type");
            }
            
            response = requestTimeout < 0 ? rpcClient.request(request)
                : rpcClient.request(request, requestTimeout);
            if (ResponseCode.SUCCESS.getCode() != response.getResultCode()) {
                // 403 无权限时触发重新登录以刷新 accessToken
                if (NacosException.NO_RIGHT == response.getErrorCode()) {
                    reLogin();
                }
                throw new NacosException(response.getErrorCode(), response.getMessage());
            }
            if (responseClass.isAssignableFrom(response.getClass())) {
                return (T) response;
            }
            NAMING_LOGGER.error(
                "Server return unexpected response '{}', expected response should be '{}'",
                response.getClass().getName(), responseClass.getName());
            throw new NacosException(NacosException.SERVER_ERROR, "Server return invalid response");
        } catch (NacosException e) {
            recordRequestFailedMetrics(request, e, response);
            throw e;
        } catch (Exception e) {
            recordRequestFailedMetrics(request, e, response);
            throw new NacosException(NacosException.SERVER_ERROR, "Request nacos server failed: ",
                e);
        }
    }
    
    /**
     * 记录命名 RPC 请求失败指标。
     *
     * @param request   The registration request object.
     * @param exception The Exception encountered during the registration process, or null if registration was
     *                  successful.
     * @param response  The response object containing registration result information, or null if registration failed.
     */
    private void recordRequestFailedMetrics(Request request, Exception exception,
        Response response) {
        if (!enableClientMetrics) {
            return;
        }
        
        try {
            if (Objects.isNull(response)) {
                MetricsMonitor.getNamingRequestFailedMonitor(request.getClass().getSimpleName(),
                    MONITOR_LABEL_NONE,
                    MONITOR_LABEL_NONE, exception.getClass().getSimpleName()).inc();
            } else {
                MetricsMonitor.getNamingRequestFailedMonitor(request.getClass().getSimpleName(),
                    String.valueOf(response.getResultCode()),
                    String.valueOf(response.getErrorCode()),
                    MONITOR_LABEL_NONE).inc();
            }
        } catch (Throwable t) {
            NAMING_LOGGER.warn("Fail to record metrics for request {}",
                request.getClass().getSimpleName(), t);
        }
    }
    
    @Override
    public void shutdown() throws NacosException {
        NAMING_LOGGER.info("Shutdown naming grpc client proxy for  uuid->{}", uuid);
        redoService.shutdown();
        shutDownAndRemove(uuid);
        NotifyCenter.deregisterSubscriber(this);
    }
    
    /** 销毁并移除 RpcClientFactory 中注册的 gRPC 客户端。 */
    private void shutDownAndRemove(String uuid) {
        synchronized (RpcClientFactory.getAllClientEntries()) {
            try {
                RpcClientFactory.destroyClient(uuid);
                NAMING_LOGGER.info("shutdown and remove naming rpc client  for uuid ->{}", uuid);
            } catch (NacosException e) {
                NAMING_LOGGER.warn("Fail to shutdown naming rpc client  for uuid ->{}", uuid);
            }
        }
    }
    
    public boolean isEnable() {
        return rpcClient.isRunning();
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
}
