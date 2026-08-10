/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.k8s.sync;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.naming.core.InstanceOperatorClientImpl;
import com.alibaba.nacos.naming.core.ServiceOperatorV2Impl;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1EndpointAddress;
import io.kubernetes.client.openapi.models.V1EndpointSubset;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1EndpointsList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * K8s 同步服务：监听集群 Service/Endpoints 变更并同步到 Nacos 命名服务。
 *
 * <p>使用 SharedIndexInformer  watch 全命名空间资源，在应用就绪后启动，关闭时停止 Informer。</p>
 *
 * @author EmanuelGi
 */
@Component
public class K8sSyncServer {
    
    /** K8s 同步配置。 */
    @Autowired
    private K8sSyncConfig k8sSyncConfig;
    
    @Autowired
    private ServiceOperatorV2Impl serviceOperatorV2;
    
    @Autowired
    private InstanceOperatorClientImpl instanceOperatorClient;
    
    /** Kubernetes 共享 Informer 工厂。 */
    private SharedInformerFactory factory;
    
    /**
     * 应用就绪后启动 K8s Informer 并注册关闭钩子。
     *
     * @throws IOException 集群外 kubeconfig 加载失败时抛出
     */
    @EventListener(ApplicationReadyEvent.class)
    public void start() throws IOException {
        if (!k8sSyncConfig.isEnabled()) {
            Loggers.MAIN.info("The Nacos k8s-sync is disabled.");
            return;
        }
        Loggers.MAIN.info("Starting Nacos k8s-sync ...");
        startInformer();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            
            @Override
            public void run() {
                Loggers.MAIN.info("Stopping Nacos k8s-sync ...");
                K8sSyncServer.this.stop();
                Loggers.MAIN.info("Nacos k8s-sync stopped...");
            }
        });
    }
    
    /**
     * 创建 ApiClient、注册 Service/Endpoints 事件处理器并等待 Informer 缓存同步。
     *
     * @throws IOException API 客户端初始化失败时抛出
     */
    public void startInformer() throws IOException {
        ApiClient apiClient;
        CoreV1Api coreV1Api;
        
        if (k8sSyncConfig.isOutsideCluster()) {
            apiClient = getOutsideApiClient();
        } else {
            apiClient = ClientBuilder.cluster().build();
        }
        // 设置全局默认 Kubernetes API 客户端
        Configuration.setDefaultApiClient(apiClient);
        coreV1Api = new CoreV1Api();
        
        OkHttpClient httpClient = apiClient.getHttpClient().newBuilder().build();
        apiClient.setHttpClient(httpClient);
        
        factory = new SharedInformerFactory(apiClient);
        SharedIndexInformer<V1Service> serviceInformer =
            factory.sharedIndexInformerFor(
                (CallGeneratorParams params) -> {
                    CoreV1Api.APIlistServiceForAllNamespacesRequest request =
                        coreV1Api.listServiceForAllNamespaces();
                    request.resourceVersion(params.resourceVersion);
                    request.timeoutSeconds(params.timeoutSeconds);
                    request.watch(params.watch);
                    return request.buildCall(null);
                },
                V1Service.class,
                V1ServiceList.class);
        
        SharedIndexInformer<V1Endpoints> endpointInformer =
            factory.sharedIndexInformerFor(
                (CallGeneratorParams params) -> {
                    CoreV1Api.APIlistEndpointsForAllNamespacesRequest request =
                        coreV1Api.listEndpointsForAllNamespaces();
                    request.resourceVersion(params.resourceVersion);
                    request.timeoutSeconds(params.timeoutSeconds);
                    request.watch(params.watch);
                    return request.buildCall(null);
                },
                V1Endpoints.class,
                V1EndpointsList.class);
        
        serviceInformer.addEventHandler(
            new ResourceEventHandler<V1Service>() {
                
                @Override
                public void onAdd(V1Service service) {
                    if (service.getMetadata() == null || service.getSpec() == null) {
                        return;
                    }
                    String serviceName = service.getMetadata().getName();
                    String namespace = service.getMetadata().getNamespace();
                    List<V1ServicePort> servicePorts = service.getSpec().getPorts();
                    try {
                        registerService(namespace, serviceName, servicePorts, false,
                            endpointInformer);
                        Loggers.MAIN.info("add service, namespace:" + namespace
                            + " serviceName: " + serviceName);
                    } catch (Exception e) {
                        Loggers.MAIN.warn(
                            "add service fail, message:" + e.getMessage() + " namespace:"
                                + namespace + " serviceName: " + serviceName);
                    }
                }
                
                @Override
                public void onUpdate(V1Service oldService, V1Service newService) {
                    if (oldService.getMetadata() == null || oldService.getSpec() == null
                        || newService.getMetadata() == null
                        || newService.getSpec() == null) {
                        return;
                    }
                    List<V1ServicePort> oldServicePorts = oldService.getSpec().getPorts();
                    String serviceName = newService.getMetadata().getName();
                    String namespace = newService.getMetadata().getNamespace();
                    List<V1ServicePort> newServicePorts = newService.getSpec().getPorts();
                    boolean portChanged = compareServicePorts(oldServicePorts, newServicePorts);
                    try {
                        registerService(namespace, serviceName, newServicePorts, portChanged,
                            endpointInformer);
                        Loggers.MAIN.info("update service, namespace: " + namespace
                            + " serviceName: " + serviceName);
                    } catch (Exception e) {
                        Loggers.MAIN.warn("update service fail, message: " + e.getMessage()
                            + " namespace: "
                            + namespace + " serviceName: " + serviceName);
                    }
                }
                
                @Override
                public void onDelete(V1Service service, boolean deletedFinalStateUnknown) {
                    if (service.getMetadata() == null) {
                        return;
                    }
                    String serviceName = service.getMetadata().getName();
                    String namespace = service.getMetadata().getNamespace();
                    try {
                        unregisterService(namespace, serviceName);
                        Loggers.MAIN.info("delete service, namespace:" + namespace
                            + " serviceName:" + serviceName);
                    } catch (Exception e) {
                        Loggers.MAIN.warn("delete service fail, message: " + e.getMessage()
                            + " namespace:" + namespace + " serviceName:" + serviceName);
                    }
                }
            });
        
        endpointInformer.addEventHandler(new ResourceEventHandler<V1Endpoints>() {
            
            @Override
            public void onAdd(V1Endpoints obj) {
                if (obj.getMetadata() == null) {
                    return;
                }
                String serviceName = obj.getMetadata().getName();
                String namespace = obj.getMetadata().getNamespace();
                Set<String> addIpSet = getIpFromEndpoints(obj);
                
                //TODO 因为需要指定namespace，这里servicelister需要重新new，是否可以优化,比如说作为单例的放到map中
                Lister<V1Service> serviceLister =
                    new Lister<>(serviceInformer.getIndexer(), namespace);
                V1Service service = serviceLister.get(serviceName);
                List<V1ServicePort> servicePorts = service.getSpec().getPorts();
                try {
                    registerInstances(addIpSet, namespace, serviceName, servicePorts);
                    Loggers.MAIN.info("add instances, namespace:" + namespace + " serviceName: "
                        + serviceName);
                } catch (NacosException e) {
                    Loggers.MAIN.warn("add instances fail, message:" + e.getMessage()
                        + " namespace:" + namespace + ", serviceName: " + serviceName);
                }
            }
            
            @Override
            public void onUpdate(V1Endpoints oldObj, V1Endpoints newObj) {
                if (newObj.getMetadata() == null) {
                    return;
                }
                String serviceName = newObj.getMetadata().getName();
                String namespace = newObj.getMetadata().getNamespace();
                Lister<V1Service> serviceLister =
                    new Lister<>(serviceInformer.getIndexer(), namespace);
                V1Service service = serviceLister.get(serviceName);
                List<V1ServicePort> servicePorts = service.getSpec().getPorts();
                try {
                    registerService(namespace, serviceName, servicePorts, false, endpointInformer);
                    Loggers.MAIN.info("update instances, namespace:" + namespace + " serviceName: "
                        + serviceName);
                } catch (NacosException e) {
                    Loggers.MAIN
                        .warn("update instances fail, message:" + e.getMessage() + " namespace:"
                            + namespace + ", serviceName: " + serviceName);
                }
            }
            
            @Override
            public void onDelete(V1Endpoints obj, boolean deletedFinalStateUnknown) {
                if (obj.getMetadata() == null) {
                    return;
                }
                String serviceName = obj.getMetadata().getName();
                String namespace = obj.getMetadata().getNamespace();
                Set<String> deleteIpSet = getIpFromEndpoints(obj);
                try {
                    List<? extends Instance> oldInstanceList =
                        instanceOperatorClient.listAllInstances(namespace, serviceName);
                    unregisterInstances(deleteIpSet, namespace, serviceName, oldInstanceList);
                    Loggers.MAIN.info("delete instances, namespace:" + namespace + ", serviceName: "
                        + serviceName);
                } catch (NacosException e) {
                    Loggers.MAIN.info("delete instances fail, namespace:" + namespace
                        + ", serviceName: " + serviceName);
                }
            }
        });
        
        // 等待各 Informer 本地缓存同步完成后再继续
        // 确保本地缓存已包含最新完整的 K8s 资源数据
        long timeout = 30000L;
        long startTime = System.currentTimeMillis();
        serviceInformer.run();
        while (!serviceInformer.hasSynced()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new RuntimeException("Informer serviceInformer sync timed out");
            }
            ThreadUtils.sleep(100L);
        }
        startTime = System.currentTimeMillis();
        endpointInformer.run();
        while (!endpointInformer.hasSynced()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new RuntimeException("Informer endpointInformer sync timed out");
            }
            ThreadUtils.sleep(100L);
        }
    }
    
    /**
     * 根据 K8s Endpoints 信息构造 Nacos {@link Instance}（持久实例）。
     *
     * @param ip 实例 IP
     * @param targetPort Pod 目标端口
     * @param serviceName 服务名（作 clusterName）
     * @param port Service 端口
     * @return 待注册的 Nacos 实例
     */
    public Instance createInstance(String ip, int targetPort, String serviceName, int port) {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(targetPort);
        instance.setClusterName(serviceName);
        instance.setEphemeral(false);
        instance.setHealthy(true);
        instance.addMetadata("servicePort", String.valueOf(port));
        return instance;
    }
    
    /**
     * 注册或更新 Nacos 服务，并 reconcile Endpoints 与现有实例差异。
     *
     * @param namespace K8s 命名空间（对应 Nacos namespace）
     * @param serviceName 服务名
     * @param servicePorts Service 端口列表
     * @param portChanged 端口是否变更（变更时全量重注册实例）
     * @throws NacosException 注册失败时抛出
     */
    public void registerService(String namespace, String serviceName,
        List<V1ServicePort> servicePorts, boolean portChanged,
        SharedIndexInformer<V1Endpoints> endpointInformer) throws NacosException {
        // TODO 提取 default namespace 常量
        
        Service service =
            Service.newService(namespace, Constants.DEFAULT_GROUP, serviceName, false);
        ServiceManager.getInstance().getSingleton(service);
        
        //NotifyCenter.publishEvent(new NamingTraceEvent.RegisterServiceTraceEvent(System.currentTimeMillis(),
        //        namespace, Constants.DEFAULT_GROUP, serviceName));
        
        Set<String> oldIpSet = new HashSet<>();
        List<? extends Instance> oldInstanceList =
            instanceOperatorClient.listAllInstances(namespace, serviceName);
        for (Instance instance : oldInstanceList) {
            oldIpSet.add(instance.getIp());
        }
        Lister<V1Endpoints> endpointLister = new Lister<>(endpointInformer.getIndexer(), namespace);
        V1Endpoints endpoints = endpointLister.get(serviceName);
        Set<String> newIpSet = getIpFromEndpoints(endpoints);
        
        // 注销已从 Endpoints 消失的实例
        Set<String> deleteIpSet = new HashSet<>();
        deleteIpSet.addAll(oldIpSet);
        deleteIpSet.removeAll(newIpSet);
        unregisterInstances(deleteIpSet, namespace, serviceName, oldInstanceList);
        // 注册 Endpoints 中新增的实例
        Set<String> addIpSet = new HashSet<>();
        addIpSet.addAll(newIpSet);
        if (!portChanged) {
            addIpSet.removeAll(oldIpSet);
        }
        registerInstances(addIpSet, namespace, serviceName, servicePorts);
    }
    
    /**
     * 删除 Nacos 服务及其全部实例。
     *
     * @param namespace K8s 命名空间
     * @param serviceName 服务名
     * @throws NacosException 注销失败时抛出
     */
    public void unregisterService(String namespace, String serviceName) throws NacosException {
        List<? extends Instance> instancelist =
            instanceOperatorClient.listAllInstances(namespace, serviceName);
        for (Instance instance : instancelist) {
            instanceOperatorClient.removeInstance(namespace, serviceName, instance);
        }
        serviceOperatorV2.delete(namespace, serviceName);
    }
    
    /**
     * 批量注册新增 IP 对应的 Nacos 实例（按 Service 端口展开）。
     *
     * @param addIpSet 待新增 IP 集合
     * @param namespace K8s 命名空间
     * @param serviceName 服务名
     * @param servicePorts Service 端口列表
     * @throws NacosException 注册失败时抛出
     */
    public void registerInstances(Set<String> addIpSet, String namespace, String serviceName,
        List<V1ServicePort> servicePorts) throws NacosException {
        for (V1ServicePort servicePort : servicePorts) {
            int port = servicePort.getPort();
            if (!servicePort.getTargetPort().isInteger()) {
                continue;
            }
            int targetPort = servicePort.getTargetPort().getIntValue();
            for (String ip : addIpSet) {
                Instance instance = createInstance(ip, targetPort, serviceName, port);
                instanceOperatorClient.registerInstance(namespace, serviceName, instance);
            }
        }
        // TODO：注册实例后是否需发布 naming 事件
    }
    
    /**
     * 按 IP 集合从 Nacos 服务中移除实例。
     *
     * @param deleteIpSet 待删除 IP 集合
     * @param namespace K8s 命名空间
     * @param serviceName 服务名
     * @param oldInstanceList 当前 Nacos 上的实例列表
     */
    public void unregisterInstances(Set<String> deleteIpSet, String namespace, String serviceName,
        List<? extends Instance> oldInstanceList) throws NacosException {
        for (Instance instance : oldInstanceList) {
            if (deleteIpSet.contains(instance.getIp())) {
                instanceOperatorClient.removeInstance(namespace, serviceName, instance);
            }
        }
    }
    
    /** 从 V1Endpoints 提取全部就绪 IP 地址。 */
    public Set<String> getIpFromEndpoints(V1Endpoints endpoints) {
        Set<String> ipSet = new HashSet<>();
        List<V1EndpointSubset> endpointSubsetList = endpoints.getSubsets();
        for (V1EndpointSubset endpointSubset : endpointSubsetList) {
            for (V1EndpointAddress endpointAddress : endpointSubset.getAddresses()) {
                ipSet.add(endpointAddress.getIp());
            }
        }
        return ipSet;
    }
    
    /**
     * 比较 Service 端口列表是否发生变化。
     *
     * @param oldServicePorts 变更前端口列表
     * @param newServicePorts 变更后端口列表
     * @return 端口集合是否相同
     */
    public boolean compareServicePorts(List<V1ServicePort> oldServicePorts,
        List<V1ServicePort> newServicePorts) {
        if (oldServicePorts.size() != newServicePorts.size()) {
            return false;
        }
        return oldServicePorts.containsAll(newServicePorts)
            && newServicePorts.containsAll(oldServicePorts);
    }
    
    /**
     * 集群外模式：从 kubeconfig 文件加载配置并构建 {@link ApiClient}。
     */
    public ApiClient getOutsideApiClient() throws IOException {
        String kubeConfigPath = k8sSyncConfig.getKubeConfig();
        
        // 从文件系统加载集群外 kubeconfig
        ApiClient apiClient = ClientBuilder
            .kubeconfig(KubeConfig.loadKubeConfig(
                Files.newBufferedReader(Paths.get(kubeConfigPath), StandardCharsets.UTF_8)))
            .build();
        
        // 将上述客户端设为全局默认 ApiClient
        Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }
    
    /**
     * 停止所有已注册的 Informer。
     */
    public void stop() {
        if (factory != null) {
            factory.stopAllRegisteredInformers();
        }
    }
}
