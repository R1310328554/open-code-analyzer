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

package com.alibaba.nacos.console.handler.impl.remote.config;

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigCloneInfo;
import com.alibaba.nacos.api.config.model.ConfigDetailInfo;
import com.alibaba.nacos.api.config.model.ConfigGrayInfo;
import com.alibaba.nacos.api.config.model.ConfigListenerInfo;
import com.alibaba.nacos.api.config.model.SameConfigPolicy;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.constant.ParametersField;
import com.alibaba.nacos.config.server.controller.parameters.SameNamespaceCloneConfigBean;
import com.alibaba.nacos.config.server.model.ConfigRequestInfo;
import com.alibaba.nacos.config.server.model.form.ConfigForm;
import com.alibaba.nacos.console.handler.config.ConfigHandler;
import com.alibaba.nacos.console.handler.impl.ConditionFunctionEnabled;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerService;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.ALL_PATTERN;

/**
 * 配置管理远程 Handler：分页查询、发布/删除、导入导出、Beta 灰度与监听器查询，通过 {@link NacosMaintainerClientHolder} 调用远端 Config Maintainer API。
 * Remote Implementation of ConfigHandler for handling internal configuration operations.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
@Conditional(ConditionFunctionEnabled.ConditionConfigEnabled.class)
public class ConfigRemoteHandler implements ConfigHandler {
    
    /** 运维客户端持有者，提供 Config Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 配置导入导出远程服务（HTTP 直连远端节点） */
    private final ConfigImportAndExportService importAndExportService;
    
    /** 注入运维客户端持有者与导入导出服务 */
    public ConfigRemoteHandler(NacosMaintainerClientHolder clientHolder,
        ConfigImportAndExportService importAndExportService) {
        this.clientHolder = clientHolder;
        this.importAndExportService = importAndExportService;
    }
    
    /** 分页查询远端配置列表，dataId 含通配符时自动切换模糊搜索。 */
    @Override
    public Page<ConfigBasicInfo> getConfigList(int pageNo, int pageSize, String dataId,
        String group,
        String namespaceId, Map<String, Object> configAdvanceInfo) throws NacosException {
        String search = dataId.contains(ALL_PATTERN) ? Constants.CONFIG_SEARCH_BLUR
            : Constants.CONFIG_SEARCH_ACCURATE;
        return listConfigInfo(search, pageNo, pageSize, dataId, group, namespaceId,
            configAdvanceInfo);
    }
    
    /** 获取远端单条配置完整详情，不存在时返回 null。 */
    @Override
    public ConfigDetailInfo getConfigDetail(String dataId, String group, String namespaceId)
        throws NacosException {
        try {
            return clientHolder.getConfigMaintainerService().getConfig(dataId, group, namespaceId);
        } catch (NacosException e) {
            if (NacosException.NOT_FOUND == e.getErrCode()) {
                return null;
            }
            throw e;
        }
    }
    
    /** 发布或更新远端配置，含 Beta 灰度分支。 */
    @Override
    public Boolean publishConfig(ConfigForm configForm, ConfigRequestInfo configRequestInfo)
        throws NacosException {
        ConfigMaintainerService configMaintainerService = clientHolder.getConfigMaintainerService();
        if (StringUtils.isBlank(configRequestInfo.getBetaIps())) {
            return configMaintainerService.publishConfig(configForm.getDataId(),
                configForm.getGroup(),
                configForm.getNamespaceId(), configForm.getContent(), configForm.getAppName(),
                configForm.getSrcUser(), configForm.getConfigTags(), configForm.getDesc(),
                configForm.getType());
        } else {
            return configMaintainerService.publishBetaConfig(configForm.getDataId(),
                configForm.getGroup(),
                configForm.getNamespaceId(), configForm.getContent(), configForm.getAppName(),
                configForm.getSrcUser(), configForm.getConfigTags(), configForm.getDesc(),
                configForm.getType(),
                configRequestInfo.getBetaIps());
        }
    }
    
    /** 删除远端指定配置。 */
    @Override
    public Boolean deleteConfig(String dataId, String group, String namespaceId, String tag,
        String clientIp,
        String srcUser) throws NacosException {
        return clientHolder.getConfigMaintainerService().deleteConfig(dataId, group, namespaceId);
    }
    
    /** 按 ID 批量删除远端配置。 */
    @Override
    public Boolean batchDeleteConfigs(List<Long> ids, String clientIp, String srcUser)
        throws NacosException {
        return clientHolder.getConfigMaintainerService().deleteConfigs(ids);
    }
    
    /** 按配置内容关键字分页搜索远端配置。 */
    @Override
    public Page<ConfigBasicInfo> getConfigListByContent(String search, int pageNo, int pageSize,
        String dataId,
        String group, String namespaceId, Map<String, Object> configAdvanceInfo)
        throws NacosException {
        return listConfigInfo(search, pageNo, pageSize, dataId, group, namespaceId,
            configAdvanceInfo);
    }
    
    /** 查询远端指定配置的客户端监听器状态，可选聚合模式。 */
    @Override
    public ConfigListenerInfo getListeners(String dataId, String group, String namespaceId,
        boolean aggregation)
        throws Exception {
        return clientHolder.getConfigMaintainerService().getListeners(dataId, group, namespaceId,
            aggregation);
    }
    
    /** 按客户端 IP 查询远端其订阅的全部配置及 MD5 状态。 */
    @Override
    public ConfigListenerInfo getAllSubClientConfigByIp(String ip, boolean all, String namespaceId,
        boolean aggregation)
        throws NacosException {
        return clientHolder.getConfigMaintainerService().getAllSubClientConfigByIp(ip, all,
            namespaceId, aggregation);
    }
    
    /** 委托导入导出服务从远端导出配置 ZIP。 */
    @Override
    public ResponseEntity<byte[]> exportConfig(String dataId, String group, String namespaceId,
        String appName,
        List<Long> ids) throws Exception {
        return importAndExportService.exportConfig(dataId, group, namespaceId, appName, ids);
    }
    
    /** 委托导入导出服务向远端导入配置 ZIP 并批量发布。 */
    @Override
    public Result<Map<String, Object>> importAndPublishConfig(String srcUser, String namespaceId,
        SameConfigPolicy policy, MultipartFile file, String srcIp, String requestIpApp)
        throws NacosException {
        return importAndExportService.importConfig(srcUser, namespaceId, policy, file, srcIp,
            requestIpApp);
    }
    
    /** 将选中配置克隆到远端目标命名空间，支持重命名 group/dataId。 */
    @Override
    public Result<Map<String, Object>> cloneConfig(String srcUser, String namespaceId,
        List<SameNamespaceCloneConfigBean> configBeansList, SameConfigPolicy policy, String srcIp,
        String requestIpApp) throws NacosException {
        List<ConfigCloneInfo> configInfos = new ArrayList<>(configBeansList.size());
        configBeansList.forEach(sameNamespaceCloneConfigBean -> {
            ConfigCloneInfo configCloneInfo = new ConfigCloneInfo();
            configCloneInfo.setConfigId(sameNamespaceCloneConfigBean.getCfgId());
            configCloneInfo.setTargetDataId(sameNamespaceCloneConfigBean.getDataId());
            configCloneInfo.setTargetGroupName(sameNamespaceCloneConfigBean.getGroup());
            configInfos.add(configCloneInfo);
        });
        return Result.success(
            clientHolder.getConfigMaintainerService().cloneConfig(namespaceId, configInfos, srcUser,
                policy));
    }
    
    /** 移除远端 Beta 灰度配置。 */
    @Override
    public boolean removeBetaConfig(String dataId, String group, String namespaceId,
        String remoteIp,
        String requestIpApp, String srcUser) throws NacosException {
        return clientHolder.getConfigMaintainerService().stopBeta(dataId, group, namespaceId);
    }
    
    /** 查询远端 Beta 灰度配置详情，404 时返回 null。 */
    @Override
    public ConfigGrayInfo queryBetaConfig(String dataId, String group, String namespaceId)
        throws NacosException {
        try {
            return clientHolder.getConfigMaintainerService().queryBeta(dataId, group, namespaceId);
        } catch (NacosException e) {
            if (NacosException.NOT_FOUND == e.getErrCode()) {
                // 管理 API 返回 404 表示该配置未处于 Beta 灰度。
                return null;
            }
            // 其他异常继续向上抛出。
            throw e;
        }
    }
    
    /** 按高级过滤条件分页搜索远端配置详情。 */
    private Page<ConfigBasicInfo> listConfigInfo(String search, int pageNo, int pageSize,
        String dataId,
        String groupName, String namespaceId, Map<String, Object> configAdvanceInfo)
        throws NacosException {
        String type = getInfoFromAdvanceInfo(configAdvanceInfo, ParametersField.TYPES);
        String appName = getInfoFromAdvanceInfo(configAdvanceInfo, "appName");
        String configTags = getInfoFromAdvanceInfo(configAdvanceInfo, "config_tags");
        String configDetail = getInfoFromAdvanceInfo(configAdvanceInfo, "content");
        return clientHolder.getConfigMaintainerService()
            .searchConfigByDetails(dataId, groupName, namespaceId, search, configDetail, type,
                configTags, appName,
                pageNo, pageSize);
    }
    
    /** 从高级搜索参数映射中安全提取字符串值。 */
    private String getInfoFromAdvanceInfo(Map<String, Object> configAdvanceInfo, String key) {
        return configAdvanceInfo.containsKey(key) ? (String) configAdvanceInfo.get(key)
            : StringUtils.EMPTY;
    }
    
}
