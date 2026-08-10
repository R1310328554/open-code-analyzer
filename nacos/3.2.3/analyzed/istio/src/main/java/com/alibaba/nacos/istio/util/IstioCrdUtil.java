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

package com.alibaba.nacos.istio.util;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.istio.model.IstioEndpoint;
import com.alibaba.nacos.istio.model.IstioService;
import com.alibaba.nacos.istio.model.ServiceEntryWrapper;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.google.protobuf.Timestamp;
import io.envoyproxy.envoy.config.core.v3.TrafficDirection;
import istio.mcp.v1alpha1.MetadataOuterClass;
import istio.networking.v1alpha3.GatewayOuterClass;
import istio.networking.v1alpha3.ServiceEntryOuterClass;
import istio.networking.v1alpha3.WorkloadEntryOuterClass;
import istio.networking.v1alpha3.WorkloadEntryOuterClass.WorkloadEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Istio CRD 与 Nacos 服务模型之间的转换工具。
 *
 * <p>负责构建 ServiceEntry、WorkloadEntry、集群名及服务名等 xDS/MCP 所需标识。</p>
 *
 * @author special.fy
 */
public class IstioCrdUtil {
    
    /** Istio 可识别的默认分组名（Nacos DEFAULT_GROUP 的映射）。 */
    public static final String VALID_DEFAULT_GROUP_NAME = "DEFAULT-GROUP";
    
    /** 实例 metadata 中存放 Istio 主机名的键。 */
    public static final String ISTIO_HOSTNAME = "istio.hostname";
    
    /** Kubernetes/Istio 标签键合法格式正则。 */
    public static final String VALID_LABEL_KEY_FORMAT =
        "^([a-zA-Z0-9](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?)*/)?((?:[A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])$";
    
    /** Kubernetes/Istio 标签值合法格式正则。 */
    public static final String VALID_LABEL_VALUE_FORMAT =
        "^((?:[A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?$";
    
    /**
     * 构建 Envoy 集群名：{@code direction|port|subset|hostname}。
     *
     * @param direction 流量方向（INBOUND/OUTBOUND）
     * @param subset    DestinationRule 子集名
     * @param hostName  服务主机名
     * @param port      端口
     */
    public static String buildClusterName(TrafficDirection direction, String subset,
        String hostName, int port) {
        return direction.toString().toLowerCase() + "|" + port + "|" + subset + "|" + hostName;
    }
    
    /**
     * 将 Nacos {@link Service} 转为 Istio 服务主机名：{@code name.group.namespace}。
     *
     * @param service Nacos v2 服务
     */
    public static String buildServiceName(Service service) {
        String group = !Constants.DEFAULT_GROUP.equals(service.getGroup()) ? service.getGroup()
            : VALID_DEFAULT_GROUP_NAME;
        
        // DEFAULT_GROUP 对 Istio 无效，主机名仅允许 [0-9A-Za-z-*]
        return service.getName() + "." + group + "." + service.getNamespace();
    }
    
    /**
     * 从 ServiceEntry 资源名反推 Nacos 服务名（去掉域名后缀）。
     */
    public static String parseServiceEntryNameToServiceName(String serviceEntryName,
        String domain) {
        return serviceEntryName.substring(0, serviceEntryName.length() - domain.length() - 1);
    }
    
    /** 从集群名第四段解析 Nacos 服务名。 */
    public static String parseClusterNameToServiceName(String clusterName, String domain) {
        String str = clusterName.split("\\|", 4)[3];
        return str.substring(0, str.length() - domain.length() - 1);
    }
    
    /**
     * 由 {@link IstioService} 构建 MCP {@link ServiceEntryWrapper}。
     *
     * @return 无有效端点时返回 null
     */
    public static ServiceEntryWrapper buildServiceEntry(String serviceName, String hostName,
        IstioService istioService) {
        if (istioService.getHosts().isEmpty()) {
            return null;
        }
        
        ServiceEntryOuterClass.ServiceEntry.Builder serviceEntryBuilder =
            ServiceEntryOuterClass.ServiceEntry
                .newBuilder()
                .setResolution(ServiceEntryOuterClass.ServiceEntry.Resolution.STATIC)
                .setLocation(ServiceEntryOuterClass.ServiceEntry.Location.MESH_INTERNAL);
        
        int port = 0;
        String protocol = "http";
        List<WorkloadEntry> endpoints = buildWorkloadEntry(istioService.getHosts());
        
        serviceEntryBuilder.addHosts(hostName)
            .addPorts(GatewayOuterClass.Port.newBuilder().setNumber(port)
                .setName(protocol).setProtocol(protocol.toUpperCase()).build())
            .addAllEndpoints(endpoints);
        ServiceEntryOuterClass.ServiceEntry serviceEntry = serviceEntryBuilder.build();
        
        Date createTimestamp = istioService.getCreateTimeStamp();
        MetadataOuterClass.Metadata metadata = MetadataOuterClass.Metadata.newBuilder()
            .setName(istioService.getNamespace() + "/" + serviceName)
            .putAnnotations("virtual", "1")
            .putLabels("registryType", "nacos")
            .setCreateTime(
                Timestamp.newBuilder().setSeconds(createTimestamp.getTime() / 1000).build())
            .setVersion(String.valueOf(istioService.getRevision())).build();
        
        return new ServiceEntryWrapper(metadata, serviceEntry);
    }
    
    /**
     * 将 {@link IstioEndpoint} 列表转为 Istio WorkloadEntry，过滤非法标签。
     */
    public static List<WorkloadEntryOuterClass.WorkloadEntry> buildWorkloadEntry(
        List<IstioEndpoint> istioEndpointList) {
        List<WorkloadEntryOuterClass.WorkloadEntry> result = new ArrayList<>();
        
        for (IstioEndpoint istioEndpoint : istioEndpointList) {
            if (!istioEndpoint.isHealthy() || !istioEndpoint.isEnabled()) {
                continue;
            }
            
            Map<String, String> metadata = new HashMap<>(1 << 3);
            if (StringUtils.isNotEmpty(istioEndpoint.getClusterName())) {
                metadata.put("cluster", istioEndpoint.getClusterName());
            }
            
            // 仅保留符合 K8s 标签规范的 metadata 键值
            for (Map.Entry<String, String> entry : istioEndpoint.getLabels().entrySet()) {
                if (!Pattern.matches(VALID_LABEL_KEY_FORMAT, entry.getKey())) {
                    continue;
                }
                if (!Pattern.matches(VALID_LABEL_VALUE_FORMAT, entry.getValue())) {
                    continue;
                }
                metadata.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            
            WorkloadEntryOuterClass.WorkloadEntry workloadEntry =
                WorkloadEntryOuterClass.WorkloadEntry.newBuilder()
                    .setAddress(istioEndpoint.getAdder())
                    .setWeight(istioEndpoint.getWeight())
                    .putAllLabels(metadata)
                    .putPorts(istioEndpoint.getProtocol(), istioEndpoint.getPort()).build();
            
            result.add(workloadEntry);
        }
        return result;
    }
}
