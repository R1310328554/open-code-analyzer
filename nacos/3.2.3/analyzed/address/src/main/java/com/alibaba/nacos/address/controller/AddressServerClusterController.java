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

package com.alibaba.nacos.address.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.address.component.AddressServerGeneratorManager;
import com.alibaba.nacos.address.component.AddressServerManager;
import com.alibaba.nacos.address.constant.AddressServerConstants;
import com.alibaba.nacos.address.misc.Loggers;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.core.ClusterOperator;
import com.alibaba.nacos.naming.core.InstanceOperator;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Address server cluster controller.
 * <p>地址服务器集群节点 REST 控制器：映射 {@code /nacos/v1/as/nodes}，支持 POST 注册与 DELETE 移除指定 IP 列表；底层通过 {@link InstanceOperator} 写入 Nacos 命名服务。</p>
 *
 * @author pbting
 * @since 1.1.0
 */
@RestController
@RequestMapping({AddressServerConstants.ADDRESS_SERVER_REQUEST_URL + "/nodes"})
public class AddressServerClusterController {
    
    private final InstanceOperator instanceOperator;
    
    private final NamingMetadataManager metadataManager;
    
    private final ClusterOperator clusterOperator;
    
    private final AddressServerManager addressServerManager;
    
    private final AddressServerGeneratorManager addressServerGeneratorManager;
    
    public AddressServerClusterController(InstanceOperator instanceOperator,
        NamingMetadataManager metadataManager,
        ClusterOperator clusterOperator, AddressServerManager addressServerManager,
        AddressServerGeneratorManager addressServerGeneratorManager) {
        this.instanceOperator = instanceOperator;
        this.metadataManager = metadataManager;
        this.clusterOperator = clusterOperator;
        this.addressServerManager = addressServerManager;
        this.addressServerGeneratorManager = addressServerGeneratorManager;
    }
    
    /**
     * Create new cluster.
     * <p>向指定 product/cluster 注册 IP 列表；自动创建集群元数据并校验 IP 格式。</p>
     *
     * @param product Ip list of products to be associated
     * @param cluster Ip list of product cluster to be associated
     * @param ips     will post ip list.
     * @return result of create new cluster
     */
    @Since("1.1.0")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<String> postCluster(@RequestParam(required = false) String product,
        @RequestParam(required = false) String cluster, @RequestParam(name = "ips") String ips) {
        
        // 1. 解析存储侧 product/cluster 名称
        String productName = addressServerGeneratorManager.generateProductName(product);
        String clusterName = addressServerManager.getDefaultClusterNameIfEmpty(cluster);
        
        // 2. 解析返回给客户端的原始 product/cluster 名称
        String rawProductName = addressServerManager.getRawProductName(product);
        String rawClusterName = addressServerManager.getRawClusterName(cluster);
        Loggers.ADDRESS_LOGGER.info(
            "put cluster node,the cluster name is " + cluster + "; the product name=" + product
                + "; the ip list=" + ips);
        ResponseEntity<String> responseEntity;
        try {
            String serviceName =
                addressServerGeneratorManager.generateNacosServiceName(productName);
            
            Result result = registerCluster(serviceName, rawProductName, clusterName, ips);
            if (InternetAddressUtil.checkOk(result.getCheckResult())) {
                responseEntity = ResponseEntity
                    .ok("product=" + rawProductName + ",cluster=" + rawClusterName
                        + "; put success with size="
                        + result.getSize());
            } else {
                responseEntity =
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getCheckResult());
            }
        } catch (Exception e) {
            responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        
        return responseEntity;
    }
    
    /**
     * 注册集群节点：校验服务非 ephemeral、确保集群元数据存在、校验并注册各 Instance。
     * @return 含 IP 校验结果与数量的 Result
     */
    private Result registerCluster(String serviceName, String productName, String clusterName,
        String ips)
        throws NacosException {
        String serviceWithoutGroup = NamingUtils.getServiceName(serviceName);
        String groupName = NamingUtils.getGroupName(serviceName);
        Service service = Service.newService(Constants.DEFAULT_NAMESPACE_ID, groupName,
            serviceWithoutGroup, false);
        service = ServiceManager.getInstance().getSingleton(service);
        if (service.isEphemeral()) {
            return new Result(
                String.format("Service %s is ephemeral service, can't use as address server",
                    serviceName),
                0);
        }
        ServiceMetadata serviceMetadata =
            metadataManager.getServiceMetadata(service).orElse(new ServiceMetadata());
        if (!serviceMetadata.getClusters().containsKey(clusterName)) {
            ClusterMetadata metadata = new ClusterMetadata();
            metadata.setHealthyCheckType(AbstractHealthChecker.None.TYPE);
            metadata.setHealthChecker(new AbstractHealthChecker.None());
            clusterOperator.updateClusterMetadata(Constants.DEFAULT_NAMESPACE_ID, serviceName,
                clusterName, metadata);
        }
        String[] ipArray = addressServerManager.splitIps(ips);
        String checkResult = InternetAddressUtil.checkIps(ipArray);
        if (InternetAddressUtil.checkOk(checkResult)) {
            List<Instance> instanceList = addressServerGeneratorManager
                .generateInstancesByIps(serviceName, productName, clusterName, ipArray);
            for (Instance instance : instanceList) {
                instanceOperator.registerInstance(Constants.DEFAULT_NAMESPACE_ID, serviceName,
                    instance);
            }
        }
        return new Result(checkResult, ipArray.length);
    }
    
    /**
     * Delete cluster.
     * <p>从指定 product/cluster 删除 IP 列表；ips 为空返回 400；服务不存在返回 404。</p>
     *
     * @param product Ip list of products to be associated
     * @param cluster Ip list of product cluster to be associated
     * @param ips     will delete ips.
     * @return delete result (the cluster information is return if success, exception information is return if  fail)
     */
    @Since("1.1.0")
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteCluster(@RequestParam(required = false) String product,
        @RequestParam(required = false) String cluster, @RequestParam String ips) {
        //1. prepare the storage name for product and cluster
        String productName = addressServerGeneratorManager.generateProductName(product);
        String clusterName = addressServerManager.getDefaultClusterNameIfEmpty(cluster);
        
        //2. prepare the response name for product and cluster to client
        String rawProductName = addressServerManager.getRawProductName(product);
        String rawClusterName = addressServerManager.getRawClusterName(cluster);
        ResponseEntity responseEntity = ResponseEntity.status(HttpStatus.OK)
            .body("product=" + rawProductName + ", cluster=" + rawClusterName + " delete success.");
        try {
            
            String serviceName =
                addressServerGeneratorManager.generateNacosServiceName(productName);
            String serviceWithoutGroup = NamingUtils.getServiceName(serviceName);
            String groupName = NamingUtils.getGroupName(serviceName);
            Optional<com.alibaba.nacos.naming.core.v2.pojo.Service> service =
                com.alibaba.nacos.naming.core.v2.ServiceManager
                    .getInstance().getSingletonIfExist(Constants.DEFAULT_NAMESPACE_ID, groupName,
                        serviceWithoutGroup);
            
            if (!service.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("product=" + rawProductName + " not found.");
            }
            if (StringUtils.isBlank(ips)) {
                // ips 为空时不允许删除
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ips must not be empty.");
            }
            // 删除指定 IP 列表对应的 Instance
            String[] ipArray = addressServerManager.splitIps(ips);
            String checkResult = InternetAddressUtil.checkIps(ipArray);
            if (InternetAddressUtil.checkOk(checkResult)) {
                List<Instance> instanceList = addressServerGeneratorManager
                    .generateInstancesByIps(serviceName, rawProductName, clusterName, ipArray);
                for (Instance each : instanceList) {
                    instanceOperator.removeInstance(Constants.DEFAULT_NAMESPACE_ID, serviceName,
                        each);
                }
            } else {
                responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(checkResult);
            }
        } catch (Exception e) {
            
            responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getCause());
        }
        
        return responseEntity;
    }
    
    /** 内部结果：IP 校验消息与处理的 IP 数量 */
    private class Result {
        
        private final String checkResult;
        
        private final int size;
        
        public Result(String checkResult, int size) {
            this.checkResult = checkResult;
            this.size = size;
        }
        
        public String getCheckResult() {
            return checkResult;
        }
        
        public int getSize() {
            return size;
        }
    }
}
