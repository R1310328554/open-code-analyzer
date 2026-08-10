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

package com.alibaba.nacos.naming.utils;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.pojo.Cluster;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClusterInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.constants.FieldsConstants;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.alibaba.nacos.naming.selector.SelectorManager;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 命名服务工具类。
 *
 * <p>提供服务详情转换、服务名分页、实例筛选及健康保护阈值逻辑，供订阅推送与 OpenAPI 查询复用。</p>
 *
 * @author xiweng.yy
 */
public final class ServiceUtil {
    
    private static final int DEFAULT_PORT = 80;
    
    /**
     * 将 {@link ServiceDetailInfo} 转为旧版控制台 JSON 结构。
     *
     * <p>TODO：控制台全面迁移后可删除。</p>
     *
     * @param serviceDetailInfo serviceDetailInfo
     * @return old console ui custom result
     */
    public static Object transferToConsoleResult(ServiceDetailInfo serviceDetailInfo) {
        ObjectNode serviceObject = JacksonUtils.createEmptyJsonNode();
        serviceObject.put(FieldsConstants.NAME, serviceDetailInfo.getServiceName());
        serviceObject.put(FieldsConstants.GROUP_NAME, serviceDetailInfo.getGroupName());
        serviceObject.put(FieldsConstants.PROTECT_THRESHOLD,
            serviceDetailInfo.getProtectThreshold());
        serviceObject.replace(FieldsConstants.SELECTOR,
            JacksonUtils.transferToJsonNode(serviceDetailInfo.getSelector()));
        serviceObject.replace(FieldsConstants.METADATA,
            JacksonUtils.transferToJsonNode(serviceDetailInfo.getMetadata()));
        
        ObjectNode detailView = JacksonUtils.createEmptyJsonNode();
        detailView.replace(FieldsConstants.SERVICE, serviceObject);
        
        List<com.alibaba.nacos.api.naming.pojo.Cluster> clusters = new ArrayList<>();
        String groupedServiceName = NamingUtils.getGroupedName(serviceDetailInfo.getServiceName(),
            serviceDetailInfo.getGroupName());
        for (Map.Entry<String, ClusterInfo> entry : serviceDetailInfo.getClusterMap().entrySet()) {
            com.alibaba.nacos.api.naming.pojo.Cluster clusterView = new Cluster();
            clusterView.setName(entry.getKey());
            clusterView.setHealthChecker(entry.getValue().getHealthChecker());
            clusterView.setMetadata(entry.getValue().getMetadata());
            clusterView.setDefaultPort(DEFAULT_PORT);
            clusterView.setUseIpPort4Check(entry.getValue().isUseInstancePortForCheck());
            clusterView.setDefaultCheckPort(entry.getValue().getHealthyCheckPort());
            clusterView.setServiceName(groupedServiceName);
            clusters.add(clusterView);
        }
        
        detailView.replace(FieldsConstants.CLUSTERS, JacksonUtils.transferToJsonNode(clusters));
        return detailView;
    }
    
    /**
     * 对服务名集合分页并去掉 group@@ 前缀。
     *
     * @param pageNo         page number
     * @param pageSize       size per page
     * @param serviceNameSet service name set
     * @return service name list by paged
     */
    public static List<String> pageServiceName(int pageNo, int pageSize,
        Collection<String> serviceNameSet) {
        List<String> result = new ArrayList<>(serviceNameSet);
        int start = (pageNo - 1) * pageSize;
        if (start < 0) {
            start = 0;
        }
        if (start >= result.size()) {
            return Collections.emptyList();
        }
        int end = start + pageSize;
        if (end > result.size()) {
            end = result.size();
        }
        for (int i = start; i < end; i++) {
            String serviceName = result.get(i);
            int indexOfSplitter = serviceName.indexOf(Constants.SERVICE_INFO_SPLITER);
            if (indexOfSplitter > 0) {
                serviceName = serviceName.substring(indexOfSplitter + 2);
            }
            result.set(i, serviceName);
        }
        return result.subList(start, end);
    }
    
    /** 仅保留健康实例，返回新的 {@link ServiceInfo} 副本。 */
    public static ServiceInfo selectHealthyInstances(ServiceInfo serviceInfo) {
        return selectInstances(serviceInfo, true, false);
    }
    
    /**
     * Select healthy instance of service info.
     *
     * @param serviceInfo original service info
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    /** 仅保留 enabled 实例。 */
    public static ServiceInfo selectEnabledInstances(ServiceInfo serviceInfo) {
        return selectInstances(serviceInfo, false, true);
    }
    
    /**
     * Select instance of service info.
     *
     * @param serviceInfo original service info
     * @param cluster     cluster of instances
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    /** 按集群名筛选实例。 */
    public static ServiceInfo selectInstances(ServiceInfo serviceInfo, String cluster) {
        return selectInstances(serviceInfo, cluster, false, false);
    }
    
    /**
     * Select instance of service info.
     *
     * @param serviceInfo original service info
     * @param healthyOnly whether only select instance which healthy
     * @param enableOnly  whether only select instance which enabled
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    public static ServiceInfo selectInstances(ServiceInfo serviceInfo, boolean healthyOnly,
        boolean enableOnly) {
        return selectInstances(serviceInfo, StringUtils.EMPTY, healthyOnly, enableOnly);
    }
    
    /**
     * Select instance of service info.
     *
     * @param serviceInfo original service info
     * @param cluster     cluster of instances
     * @param healthyOnly whether only select instance which healthy
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    public static ServiceInfo selectInstances(ServiceInfo serviceInfo, String cluster,
        boolean healthyOnly) {
        return selectInstances(serviceInfo, cluster, healthyOnly, false);
    }
    
    /**
     * Select instance of service info.
     *
     * @param serviceInfo original service info
     * @param cluster     cluster of instances
     * @param healthyOnly whether only select instance which healthy
     * @param enableOnly  whether only select instance which enabled
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    public static ServiceInfo selectInstances(ServiceInfo serviceInfo, String cluster,
        boolean healthyOnly,
        boolean enableOnly) {
        return doSelectInstances(serviceInfo, cluster, healthyOnly, enableOnly, null);
    }
    
    /**
     * Select instance of service info with healthy protection.
     *
     * @param serviceInfo     original service info
     * @param serviceMetadata service meta info
     * @param subscriber subscriber
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    public static ServiceInfo selectInstancesWithHealthyProtection(ServiceInfo serviceInfo,
        ServiceMetadata serviceMetadata, Subscriber subscriber) {
        return selectInstancesWithHealthyProtection(serviceInfo, serviceMetadata,
            subscriber.getCluster(), false, false, subscriber.getIp());
    }
    
    /**
     * Select instance of service info with healthy protection.
     *
     * @param serviceInfo     original service info
     * @param serviceMetadata service meta info
     * @param healthyOnly     whether only select instance which healthy
     * @param enableOnly      whether only select instance which enabled
     * @param subscriber subscriber
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    public static ServiceInfo selectInstancesWithHealthyProtection(ServiceInfo serviceInfo,
        ServiceMetadata serviceMetadata, boolean healthyOnly, boolean enableOnly,
        Subscriber subscriber) {
        return selectInstancesWithHealthyProtection(serviceInfo, serviceMetadata,
            subscriber.getCluster(), healthyOnly,
            enableOnly, subscriber.getIp());
    }
    
    /**
     * Select instance of service info with healthy protection.
     *
     * @param serviceInfo     original service info
     * @param serviceMetadata service meta info
     * @param cluster         cluster of instances
     * @param healthyOnly     whether only select instance which healthy
     * @param enableOnly      whether only select instance which enabled
     * @param subscriberIp subscriber ip address
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    public static ServiceInfo selectInstancesWithHealthyProtection(ServiceInfo serviceInfo,
        ServiceMetadata serviceMetadata, String cluster,
        boolean healthyOnly, boolean enableOnly, String subscriberIp) {
        InstancesFilter filter = (filteredResult, allInstances, healthyCount) -> {
            if (serviceMetadata == null) {
                return;
            }
            allInstances = filteredResult.getHosts();
            int originalTotal = allInstances.size();
            // 使用 Selector 按订阅者 IP 进一步过滤实例列表
            SelectorManager selectorManager = ApplicationUtils.getBean(SelectorManager.class);
            allInstances =
                selectorManager.select(serviceMetadata.getSelector(), subscriberIp, allInstances);
            filteredResult.setHosts(allInstances);
            
            // 过滤后若实例数变化，需重新统计健康实例数
            long newHealthyCount = healthyCount;
            if (originalTotal != allInstances.size()) {
                newHealthyCount = 0L;
                for (com.alibaba.nacos.api.naming.pojo.Instance allInstance : allInstances) {
                    if (allInstance.isHealthy()) {
                        newHealthyCount++;
                    }
                }
            }
            
            float threshold = serviceMetadata.getProtectThreshold();
            if (threshold < 0) {
                threshold = 0F;
            }
            if ((float) newHealthyCount / allInstances.size() <= threshold) {
                Loggers.SRV_LOG.warn("protect threshold reached, return all ips, service: {}",
                    filteredResult.getName());
                filteredResult.setReachProtectionThreshold(true);
                List<com.alibaba.nacos.api.naming.pojo.Instance> filteredInstances =
                    allInstances.stream()
                        .map(i -> {
                            if (!i.isHealthy()) {
                                i = InstanceUtil.deepCopy(i);
                                // 健康保护：将不健康实例标记为 healthy 避免全部被摘除
                                i.setHealthy(true);
                            } // else deepcopy is unnecessary
                            return i;
                        })
                        .collect(Collectors.toCollection(LinkedList::new));
                filteredResult.setHosts(filteredInstances);
            }
        };
        return doSelectInstances(serviceInfo, cluster, healthyOnly, enableOnly, filter);
    }
    
    /**
     * Select instance of service info.
     *
     * @param serviceInfo original service info
     * @param cluster     cluster of instances
     * @param healthyOnly whether only select instance which healthy
     * @param enableOnly  whether only select instance which enabled
     * @param filter      do some other filter operation
     * @return new service info
      * <p>Nacos 命名 utils/web 与 persistence 配置：请求工具、Distro 过滤器、服务名/流量修订及数据源条件装配；详见上方类说明。</p>
     */
    private static ServiceInfo doSelectInstances(ServiceInfo serviceInfo, String cluster,
        boolean healthyOnly, boolean enableOnly,
        InstancesFilter filter) {
        ServiceInfo result = new ServiceInfo();
        result.setName(serviceInfo.getName());
        result.setGroupName(serviceInfo.getGroupName());
        result.setCacheMillis(serviceInfo.getCacheMillis());
        result.setLastRefTime(System.currentTimeMillis());
        result.setClusters(cluster);
        result.setReachProtectionThreshold(false);
        Set<String> clusterSets =
            com.alibaba.nacos.common.utils.StringUtils.isNotBlank(cluster) ? new HashSet<>(
                Arrays.asList(cluster.split(","))) : new HashSet<>();
        long healthyCount = 0L;
        // 返回副本中的 hosts 列表，通常不修改原始 ServiceInfo
        List<com.alibaba.nacos.api.naming.pojo.Instance> filteredInstances = new LinkedList<>();
        // 记录经 cluster/enabled 过滤后的全部实例，供健康保护计算比例
        List<com.alibaba.nacos.api.naming.pojo.Instance> allInstances = new LinkedList<>();
        for (com.alibaba.nacos.api.naming.pojo.Instance ip : serviceInfo.getHosts()) {
            if (checkCluster(clusterSets, ip) && checkEnabled(enableOnly, ip)) {
                if (!healthyOnly || ip.isHealthy()) {
                    filteredInstances.add(ip);
                }
                if (ip.isHealthy()) {
                    healthyCount += 1;
                }
                allInstances.add(ip);
            }
        }
        result.setHosts(filteredInstances);
        if (filter != null) {
            filter.doFilter(result, allInstances, healthyCount);
        }
        return result;
    }
    
    private static boolean checkCluster(Set<String> clusterSets,
        com.alibaba.nacos.api.naming.pojo.Instance instance) {
        if (clusterSets.isEmpty()) {
            return true;
        }
        return clusterSets.contains(instance.getClusterName());
    }
    
    private static boolean checkEnabled(boolean enableOnly,
        com.alibaba.nacos.api.naming.pojo.Instance instance) {
        return !enableOnly || instance.isEnabled();
    }
    
    private interface InstancesFilter {
        
        /**
         * 自定义实例过滤回调（如 Selector 与健康保护）。
         *
         * @param filteredResult result with instances already been filtered cluster/enabled/healthy
         * @param allInstances   all instances filtered by cluster/enabled
         * @param healthyCount   healthy instances count filtered by cluster/enabled
         */
        void doFilter(ServiceInfo filteredResult,
            List<com.alibaba.nacos.api.naming.pojo.Instance> allInstances,
            long healthyCount);
        
    }
    
}
