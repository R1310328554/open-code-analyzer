/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.handler.config;

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigDetailInfo;
import com.alibaba.nacos.api.config.model.ConfigGrayInfo;
import com.alibaba.nacos.api.config.model.ConfigListenerInfo;
import com.alibaba.nacos.api.config.model.SameConfigPolicy;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.config.server.controller.parameters.SameNamespaceCloneConfigBean;
import com.alibaba.nacos.config.server.model.ConfigRequestInfo;
import com.alibaba.nacos.config.server.model.form.ConfigForm;
import jakarta.servlet.ServletException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 配置中心控制台处理器接口：配置 CRUD、导入导出、监听查询及 Beta 灰度管理。
 * Interface for handling configuration operations.
 *
 * @author zhangyukun
 */
public interface ConfigHandler {
    
    /**
      * 按条件分页查询配置列表。
     * Retrieves the configuration based on the specified parameters.
     *
     * @param pageNo            分页页码
     * @param pageSize          每页条数
     * @param dataId            配置 dataId
     * @param group             配置所属 group
     * @param namespaceId       命名空间 ID
     * @param configAdvanceInfo Additional advanced search criteria.
     * @return ConfigInfo  containing all details of the specified configuration.
     * @throws IOException      If an input or output exception occurs.
     * @throws ServletException If a servlet-specific exception occurs.
     * @throws NacosException   If an error related to Nacos configuration occurs.
     */
    Page<ConfigBasicInfo> getConfigList(int pageNo, int pageSize, String dataId, String group,
        String namespaceId,
        Map<String, Object> configAdvanceInfo) throws IOException, ServletException, NacosException;
    
    /**
      * 获取指定配置的详细信息。
     * Retrieves detailed information about a specific configuration.
     *
     * @param dataId      配置 dataId
     * @param group       配置所属 group
     * @param namespaceId 命名空间 ID
     * @return A ConfigAllInfo object containing all details of the specified configuration.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    ConfigDetailInfo getConfigDetail(String dataId, String group, String namespaceId)
        throws NacosException;
    
    /**
      * 发布新配置或更新已有配置。
     * Publishes a new configuration or updates an existing configuration.
     *
     * @param configForm        The form object containing configuration details.
     * @param configRequestInfo Additional request information related to the configuration.
     * @return A Boolean indicating whether the publish operation was successful.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    Boolean publishConfig(ConfigForm configForm, ConfigRequestInfo configRequestInfo)
        throws NacosException;
    
    /**
      * 删除指定配置。
     * Deletes a specific configuration.
     *
     * @param dataId      The identifier of the configuration data to delete.
     * @param group       配置所属 group
     * @param namespaceId 命名空间 ID
     * @param tag         The tag associated with the configuration.
     * @param clientIp    The IP address of the client requesting the deletion.
     * @param srcUser     The source user requesting the deletion.
     * @return A Boolean indicating whether the deletion was successful.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    Boolean deleteConfig(String dataId, String group, String namespaceId, String tag,
        String clientIp, String srcUser)
        throws NacosException;
    
    /**
      * 按 ID 批量删除配置。
     * Deletes multiple configurations based on their IDs.
     *
     * @param ids      A list of IDs of the configurations to delete.
     * @param clientIp The IP address of the client requesting the deletion.
     * @param srcUser  The source user requesting the deletion.
     * @return A Boolean indicating whether the deletion was successful.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    Boolean batchDeleteConfigs(List<Long> ids, String clientIp, String srcUser)
        throws NacosException;
    
    /**
      * 导出配置（含元数据）。
     * Exports the configuration with metadata based on the specified parameters.
     *
     * @param dataId      配置 dataId
     * @param group       配置所属 group
     * @param namespaceId 命名空间 ID
     * @param appName     The application name associated with the configuration.
     * @param ids         A list of IDs of the configurations to export.
     * @return A ResponseEntity containing the exported configuration as a byte array.
     * @throws Exception If an unexpected error occurs during the export process.
     */
    ResponseEntity<byte[]> exportConfig(String dataId, String group, String namespaceId,
        String appName,
        List<Long> ids) throws Exception;
    
    /**
      * 按配置内容搜索配置列表。
     * Searches for configurations based on detailed criteria.
     *
     * @param search            The search keyword.
     * @param pageNo            分页页码
     * @param pageSize          每页条数
     * @param dataId            配置 dataId
     * @param group             配置所属 group
     * @param namespaceId       命名空间 ID
     * @param configAdvanceInfo Additional advanced search criteria.
     * @return A Page object containing a list of ConfigInfo that matches the search criteria.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    Page<ConfigBasicInfo> getConfigListByContent(String search, int pageNo, int pageSize,
        String dataId, String group,
        String namespaceId, Map<String, Object> configAdvanceInfo) throws NacosException;
    
    /**
      * 查询指定配置的监听者（订阅客户端）状态。
     * Retrieves the status of listeners for a specific configuration.
     *
     * @param dataId      配置 dataId
     * @param group       配置所属 group
     * @param namespaceId 命名空间 ID
     * @param aggregation whether aggregation from other servers
     * @return A ConfigListenerInfo object containing the status of the listeners.
     * @throws Exception If an unexpected error occurs.
     */
    ConfigListenerInfo getListeners(String dataId, String group, String namespaceId,
        boolean aggregation)
        throws Exception;
    
    /**
      * 按客户端 IP 查询其订阅的配置列表。
     * Get subscription information based on IP, tenant, and other parameters.
     *
     * @param ip IP address of the client
     * @param all Whether to retrieve all configurations
     * @param namespaceId Tenant information
     * @param aggregation whether aggregation from other servers
     * @return ConfigListenerInfo object containing subscription information
     * @throws NacosException If an error occurs while retrieving the subscription information.
     */
    ConfigListenerInfo getAllSubClientConfigByIp(String ip, boolean all, String namespaceId,
        boolean aggregation)
        throws NacosException;
    
    /**
      * 从文件导入并发布配置。
     * Imports and publishes a configuration from a file.
     *
     * @param srcUser      The source user performing the import.
     * @param namespaceId  命名空间 ID
     * @param policy       The policy for handling existing configurations.
     * @param file         The file containing the configuration to import.
     * @param srcIp        The IP address of the source.
     * @param requestIpApp The IP address of the requester.
     * @return A Result object containing the status and additional information about the operation.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    Result<Map<String, Object>> importAndPublishConfig(String srcUser, String namespaceId,
        SameConfigPolicy policy,
        MultipartFile file, String srcIp, String requestIpApp) throws NacosException;
    
    /**
      * 克隆配置至目标命名空间。
     * Clones an existing configuration to a different namespace.
     *
     * @param srcUser         The source user performing the clone operation.
     * @param namespaceId     The namespace identifier where the configuration will be cloned to.
     * @param configBeansList A list of configurations to be cloned.
     * @param policy          The policy for handling existing configurations in the target namespace.
     * @param srcIp           The IP address of the source.
     * @param requestIpApp    The IP address of the requester.
     * @return A Result object containing the status and additional information about the operation.
     * @throws NacosException If an error related to Nacos configuration occurs.
     */
    Result<Map<String, Object>> cloneConfig(String srcUser, String namespaceId,
        List<SameNamespaceCloneConfigBean> configBeansList, SameConfigPolicy policy, String srcIp,
        String requestIpApp) throws NacosException;
    
    /**
      * 停止 Beta 灰度发布。
     * Remove beta configuration based on dataId, group, and namespaceId.
     *
     * @param dataId       the dataId
     * @param group        the group
     * @param namespaceId  the namespaceId
     * @param remoteIp     the IP address of the client making the request
     * @param requestIpApp the name of the application making the request
     * @param srcUser      the src user performing the operation
     * @return true if the beta configuration is successfully removed
     * @throws NacosException if an error occurs while removing the beta configuration
     */
    boolean removeBetaConfig(String dataId, String group, String namespaceId, String remoteIp,
        String requestIpApp,
        String srcUser) throws NacosException;
    
    /**
      * 查询 Beta 灰度配置详情。
     * Query beta configuration based on dataId, group, and namespaceId.
     *
     * @param dataId      the dataId
     * @param group       the group
     * @param namespaceId the namespaceId
     * @return ConfigInfo4Beta containing the beta configuration details
     * @throws NacosException if an error occurs while querying the beta configuration
     */
    ConfigGrayInfo queryBetaConfig(String dataId, String group, String namespaceId)
        throws NacosException;
}
