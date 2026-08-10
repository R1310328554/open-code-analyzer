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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.enums.OperationType;
import com.alibaba.nacos.config.server.model.ConfigHistoryInfo;
import com.alibaba.nacos.config.server.model.ConfigHistoryInfoDetail;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfoWrapper;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoGrayPersistService;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoPersistService;
import com.alibaba.nacos.config.server.service.repository.HistoryConfigInfoPersistService;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.encryption.handler.EncryptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 配置历史查询服务：分页/详情/前后版本对比，含权限校验与加密内容解密。
 * HistoryService.
 *
 * @author dongyafei
 * @date 2022/8/11
 */
@Service
public class HistoryService {
    
    private final HistoryConfigInfoPersistService historyConfigInfoPersistService;
    
    private final ConfigInfoPersistService configInfoPersistService;
    
    private final ConfigInfoGrayPersistService configInfoGrayPersistService;
    
    public HistoryService(HistoryConfigInfoPersistService historyConfigInfoPersistService,
        ConfigInfoPersistService configInfoPersistService,
        ConfigInfoGrayPersistService configInfoGrayPersistService) {
        this.historyConfigInfoPersistService = historyConfigInfoPersistService;
        this.configInfoPersistService = configInfoPersistService;
        this.configInfoGrayPersistService = configInfoGrayPersistService;
    }
    
    /**
     * 分页查询指定 dataId/group/namespace 的配置变更历史列表。
     */
    public Page<ConfigHistoryInfo> listConfigHistory(String dataId, String group,
        String namespaceId, Integer pageNo,
        Integer pageSize) {
        return historyConfigInfoPersistService.findConfigHistory(dataId, group, namespaceId, pageNo,
            pageSize);
    }
    
    /**
     * 按 nid 查询单条历史详情，解密后返回明文 content。
     */
    public ConfigHistoryInfo getConfigHistoryInfo(String dataId, String group, String namespaceId,
        Long nid)
        throws AccessException {
        ConfigHistoryInfo configHistoryInfo =
            historyConfigInfoPersistService.detailConfigHistory(nid);
        if (Objects.isNull(configHistoryInfo)) {
            return null;
        }
        // 校验历史记录与请求参数三元组一致，防止越权访问
        checkHistoryInfoPermission(configHistoryInfo, dataId, group, namespaceId);
        
        String encryptedDataKey = configHistoryInfo.getEncryptedDataKey();
        Pair<String, String> pair = EncryptionHandler.decryptHandler(dataId, encryptedDataKey,
            configHistoryInfo.getContent());
        configHistoryInfo.setContent(pair.getSecond());
        
        return configHistoryInfo;
    }
    
    /**
     * 查询指定 id 的上一条历史记录（时间序前一版本）。
     */
    public ConfigHistoryInfo getPreviousConfigHistoryInfo(String dataId, String group,
        String namespaceId, Long id)
        throws AccessException {
        ConfigHistoryInfo configHistoryInfo =
            historyConfigInfoPersistService.detailPreviousConfigHistory(id);
        if (Objects.isNull(configHistoryInfo)) {
            return null;
        }
        // check if history config match the input
        checkHistoryInfoPermission(configHistoryInfo, dataId, group, namespaceId);
        
        String encryptedDataKey = configHistoryInfo.getEncryptedDataKey();
        Pair<String, String> pair = EncryptionHandler.decryptHandler(dataId, encryptedDataKey,
            configHistoryInfo.getContent());
        configHistoryInfo.setContent(pair.getSecond());
        
        return configHistoryInfo;
    }
    
    /**
     * 按命名空间列出当前全部配置（非历史表）。
     */
    public List<ConfigInfoWrapper> getConfigListByNamespace(String namespaceId) {
        return configInfoPersistService.queryConfigInfoByNamespace(namespaceId);
    }
    
    /**
     * 校验 dataId、group、namespaceId 与历史记录一致，否则抛出 {@link AccessException}。
     */
    private void checkHistoryInfoPermission(ConfigHistoryInfo configHistoryInfo, String dataId,
        String group,
        String namespaceId) throws AccessException {
        if (!Objects.equals(configHistoryInfo.getDataId(), dataId)
            || !Objects.equals(configHistoryInfo.getGroup(),
                group)
            || !Objects.equals(configHistoryInfo.getTenant(), namespaceId)) {
            throw new AccessException("Please check dataId, group or namespaceId.");
        }
    }
    
    /**
     * Query the detailed config history info pair, including the original version and the updated version.
      * <p>配置历史查询；详见类级说明。</p>
     */
    public ConfigHistoryInfoDetail getConfigHistoryInfoDetail(String dataId, String group,
        String namespaceId, Long nid)
        throws AccessException {
        ConfigHistoryInfo configHistoryInfo =
            historyConfigInfoPersistService.detailConfigHistory(nid);
        if (Objects.isNull(configHistoryInfo)) {
            return null;
        }
        
        // check if history config match the input
        checkHistoryInfoPermission(configHistoryInfo, dataId, group, namespaceId);
        
        // transform
        ConfigHistoryInfoDetail configHistoryInfoDetail = new ConfigHistoryInfoDetail();
        BeanUtils.copyProperties(configHistoryInfo, configHistoryInfoDetail);
        configHistoryInfoDetail.setOpType(configHistoryInfoDetail.getOpType().trim());
        
        // 新增操作：仅填充 updated 侧字段
        if (OperationType.INSERT.getValue().equals(configHistoryInfoDetail.getOpType())) {
            configHistoryInfoDetail.setUpdatedContent(configHistoryInfo.getContent());
            configHistoryInfoDetail.setUpdatedMd5(configHistoryInfo.getMd5());
            configHistoryInfoDetail
                .setUpdatedEncryptedDataKey(configHistoryInfo.getEncryptedDataKey());
            configHistoryInfoDetail.setUpdateExtInfo(configHistoryInfo.getExtInfo());
            configHistoryInfoDetail.setOriginalExtInfo(StringUtils.EMPTY);
            configHistoryInfoDetail.setOriginalContent(StringUtils.EMPTY);
            configHistoryInfoDetail.setOriginalMd5(StringUtils.EMPTY);
            configHistoryInfoDetail.setOriginalEncryptedDataKey(StringUtils.EMPTY);
        }
        
        // 更新操作：当前记录为 original，下一条或现网为 updated
        if (OperationType.UPDATE.getValue().equals(configHistoryInfoDetail.getOpType())) {
            
            configHistoryInfoDetail.setOriginalExtInfo(configHistoryInfo.getExtInfo());
            configHistoryInfoDetail.setOriginalContent(configHistoryInfo.getContent());
            configHistoryInfoDetail.setOriginalMd5(configHistoryInfo.getMd5());
            configHistoryInfoDetail
                .setOriginalEncryptedDataKey(configHistoryInfo.getEncryptedDataKey());
            
            ConfigHistoryInfo nextHistoryInfo =
                historyConfigInfoPersistService.getNextHistoryInfo(dataId, group,
                    namespaceId, configHistoryInfoDetail.getPublishType(),
                    configHistoryInfoDetail.getGrayName(), nid);
            
            ConfigInfo currentConfigInfo = null;
            if (Objects.isNull(nextHistoryInfo)) {
                // 并发场景下二次拉取下一条历史或现网配置
                currentConfigInfo = StringUtils.isEmpty(configHistoryInfoDetail.getGrayName())
                    ? configInfoPersistService.findConfigInfo(dataId, group, namespaceId)
                    : configInfoGrayPersistService.findConfigInfo4Gray(dataId, group, namespaceId,
                        configHistoryInfoDetail.getGrayName());
                nextHistoryInfo =
                    historyConfigInfoPersistService.getNextHistoryInfo(dataId, group, namespaceId,
                        configHistoryInfoDetail.getPublishType(),
                        configHistoryInfoDetail.getGrayName(), nid);
                
            }
            
            if (nextHistoryInfo != null) {
                configHistoryInfoDetail.setUpdateExtInfo(nextHistoryInfo.getExtInfo());
                configHistoryInfoDetail.setUpdatedContent(nextHistoryInfo.getContent());
                configHistoryInfoDetail.setUpdatedMd5(nextHistoryInfo.getMd5());
                configHistoryInfoDetail
                    .setUpdatedEncryptedDataKey(nextHistoryInfo.getEncryptedDataKey());
            } else {
                configHistoryInfoDetail.setUpdatedContent(currentConfigInfo.getContent());
                configHistoryInfoDetail.setUpdatedMd5(currentConfigInfo.getMd5());
                configHistoryInfoDetail
                    .setUpdatedEncryptedDataKey(currentConfigInfo.getEncryptedDataKey());
                
            }
        }
        
        // 删除操作：保留被删版本的 original 快照
        if (OperationType.DELETE.getValue().equals(configHistoryInfoDetail.getOpType())) {
            configHistoryInfoDetail.setOriginalMd5(configHistoryInfo.getMd5());
            configHistoryInfoDetail.setOriginalContent(configHistoryInfo.getContent());
            configHistoryInfoDetail
                .setOriginalEncryptedDataKey(configHistoryInfo.getEncryptedDataKey());
            configHistoryInfoDetail.setOriginalExtInfo(configHistoryInfo.getExtInfo());
        }
        
        // 对 original/updated 密文内容统一解密
        if (StringUtils.isNotBlank(configHistoryInfoDetail.getOriginalContent())) {
            String originalContent = EncryptionHandler.decryptHandler(dataId,
                configHistoryInfoDetail.getOriginalEncryptedDataKey(),
                configHistoryInfoDetail.getOriginalContent())
                .getSecond();
            configHistoryInfoDetail.setOriginalContent(originalContent);
        }
        if (StringUtils.isNotBlank(configHistoryInfoDetail.getUpdatedContent())) {
            String updatedContent = EncryptionHandler.decryptHandler(dataId,
                configHistoryInfoDetail.getUpdatedEncryptedDataKey(),
                configHistoryInfoDetail.getUpdatedContent())
                .getSecond();
            configHistoryInfoDetail.setUpdatedContent(updatedContent);
        }
        
        return configHistoryInfoDetail;
    }
    
}
