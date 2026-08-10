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

package com.alibaba.nacos.console.proxy.config;

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
import com.alibaba.nacos.console.handler.config.ConfigHandler;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 配置管理代理：将配置 CRUD、监听查询、导入导出与 Beta 灰度操作委托给 {@link ConfigHandler}。
 * Proxy class for handling configuration operations.
 *
 * @author zhangyukun
 */
@Service
public class ConfigProxy {
    
    /** 配置 Handler 实现 */
    private final ConfigHandler configHandler;
    
    /** 注入配置 Handler。 */
    @Autowired
    public ConfigProxy(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }
    
    /** 分页查询配置列表，支持高级过滤条件。 */
    /**
     * Get configure information list.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Page<ConfigBasicInfo> getConfigList(int pageNo, int pageSize, String dataId,
        String group, String namespaceId,
        Map<String, Object> configAdvanceInfo)
        throws IOException, ServletException, NacosException {
        return configHandler.getConfigList(pageNo, pageSize, dataId, group, namespaceId,
            configAdvanceInfo);
    }
    
    /** 获取指定 dataId/group/命名空间下的配置详情。 */
    /**
     * Get the specific configuration information.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public ConfigDetailInfo getConfigDetail(String dataId, String group, String namespaceId)
        throws NacosException {
        return configHandler.getConfigDetail(dataId, group, namespaceId);
    }
    
    /** 发布或更新配置。 */
    /**
     * Add or update configuration.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Boolean publishConfig(ConfigForm configForm, ConfigRequestInfo configRequestInfo)
        throws NacosException {
        return configHandler.publishConfig(configForm, configRequestInfo);
    }
    
    /** 删除单条配置。 */
    /**
     * Delete configuration.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Boolean deleteConfig(String dataId, String group, String namespaceId, String tag,
        String clientIp,
        String srcUser) throws NacosException {
        return configHandler.deleteConfig(dataId, group, namespaceId, tag, clientIp, srcUser);
    }
    
    /** 按 ID 批量删除配置。 */
    /**
     * Batch delete configurations.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Boolean batchDeleteConfigs(List<Long> ids, String clientIp, String srcUser)
        throws NacosException {
        return configHandler.batchDeleteConfigs(ids, clientIp, srcUser);
    }
    
    /** 按配置内容全文搜索并分页返回。 */
    /**
     * Search config list by config detail.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Page<ConfigBasicInfo> getConfigListByContent(String search, int pageNo, int pageSize,
        String dataId, String group,
        String namespaceId, Map<String, Object> configAdvanceInfo) throws NacosException {
        return configHandler.getConfigListByContent(search, pageNo, pageSize, dataId, group,
            namespaceId,
            configAdvanceInfo);
    }
    
    /** 查询指定配置的订阅客户端列表。 */
    /**
     * Subscribe to configured client information.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public ConfigListenerInfo getListeners(String dataId, String group, String namespaceId,
        boolean aggregation)
        throws Exception {
        return configHandler.getListeners(dataId, group, namespaceId, aggregation);
    }
    
    /** 按客户端 IP 与命名空间查询订阅的配置列表。 */
    /**
     * Get subscription information based on IP, tenant, and other parameters.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public ConfigListenerInfo getAllSubClientConfigByIp(String ip, boolean all, String namespaceId,
        boolean aggregation)
        throws NacosException {
        return configHandler.getAllSubClientConfigByIp(ip, all, namespaceId, aggregation);
    }
    
    /** 导出版本 v2 配置包（含 metadata.yml 元数据文件）。 */
    /**
     * New version export config adds metadata.yml file to record config metadata.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public ResponseEntity<byte[]> exportConfigV2(String dataId, String group, String namespaceId,
        String appName,
        List<Long> ids) throws Exception {
        return configHandler.exportConfig(dataId, group, namespaceId, appName, ids);
    }
    
    /** 从文件导入并发布配置。 */
    /**
     * Imports and publishes a configuration from a file.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Result<Map<String, Object>> importAndPublishConfig(String srcUser, String namespaceId,
        SameConfigPolicy policy, MultipartFile file, String srcIp, String requestIpApp)
        throws NacosException {
        return configHandler.importAndPublishConfig(srcUser, namespaceId, policy, file, srcIp,
            requestIpApp);
    }
    
    /** 在同命名空间内克隆配置。 */
    /**
     * Clone configuration.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public Result<Map<String, Object>> cloneConfig(String srcUser, String namespaceId,
        List<SameNamespaceCloneConfigBean> configBeansList, SameConfigPolicy policy, String srcIp,
        String requestIpApp) throws NacosException {
        return configHandler.cloneConfig(srcUser, namespaceId, configBeansList, policy, srcIp,
            requestIpApp);
    }
    
    /** 移除 Beta 灰度配置。 */
    /**
     * Remove beta configuration based on dataId, group, and namespaceId.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public boolean removeBetaConfig(String dataId, String group, String namespaceId,
        String remoteIp,
        String requestIpApp, String srcUser) throws NacosException {
        return configHandler.removeBetaConfig(dataId, group, namespaceId, remoteIp, requestIpApp,
            srcUser);
    }
    
    /** 查询 Beta 灰度配置内容。 */
    /**
     * Query beta configuration based on dataId, group, and namespaceId.
      * <p>配置管理代理；详见类级说明。</p>
     */
    public ConfigGrayInfo queryBetaConfig(String dataId, String group, String namespaceId)
        throws NacosException {
        return configHandler.queryBetaConfig(dataId, group, namespaceId);
    }
}
