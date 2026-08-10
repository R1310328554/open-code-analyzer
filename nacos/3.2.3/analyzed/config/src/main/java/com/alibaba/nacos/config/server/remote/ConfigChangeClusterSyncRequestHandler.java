/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.remote;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.config.remote.request.cluster.ConfigChangeClusterSyncRequest;
import com.alibaba.nacos.api.config.remote.response.cluster.ConfigChangeClusterSyncResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.VersionUtils;
import com.alibaba.nacos.config.server.configuration.ConfigCompatibleConfig;
import com.alibaba.nacos.config.server.model.gray.BetaGrayRule;
import com.alibaba.nacos.config.server.model.gray.TagGrayRule;
import com.alibaba.nacos.config.server.service.ConfigMigrateService;
import com.alibaba.nacos.config.server.service.dump.DumpRequest;
import com.alibaba.nacos.config.server.service.dump.DumpService;
import com.alibaba.nacos.config.server.utils.ParamUtils;
import com.alibaba.nacos.config.server.utils.PropertyUtil;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.namespace.filter.NamespaceValidation;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.paramcheck.impl.ConfigRequestParamExtractor;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.core.remote.grpc.InvokeSource;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.stereotype.Component;

/**
 * 集群配置变更同步 RPC 处理器：接收其它 Nacos 节点的变更通知，
 * 经兼容性与灰度迁移处理后调用 {@link DumpService#dump(DumpRequest)} 落盘并刷新本地缓存。
 * handler to handler config change from other servers.
 *
 * @author liuzunfei
 * @version $Id: ConfigChangeClusterSyncRequestHandler.java, v 0.1 2020年08月11日 4:35 PM liuzunfei Exp $
 */
@Since("2.0.0")
@Component
@InvokeSource(source = {RemoteConstants.LABEL_SOURCE_CLUSTER})
public class ConfigChangeClusterSyncRequestHandler
    extends RequestHandler<ConfigChangeClusterSyncRequest, ConfigChangeClusterSyncResponse> {
    
    /** 配置 dump 服务，负责持久化与内存同步 */
    private final DumpService dumpService;
    
    /** 命名空间/灰度模型迁移服务 */
    private ConfigMigrateService configMigrateService;
    
    /**
     * @param dumpService           配置 dump 服务
     * @param configMigrateService  迁移兼容服务
     */
    public ConfigChangeClusterSyncRequestHandler(DumpService dumpService,
        ConfigMigrateService configMigrateService) {
        this.dumpService = dumpService;
        this.configMigrateService = configMigrateService;
    }
    
    @Override
    @NamespaceValidation
    @TpsControl(pointName = "ClusterConfigChangeNotify")
    @ExtractorManager.Extractor(rpcExtractor = ConfigRequestParamExtractor.class)
    @Secured(signType = SignType.CONFIG, apiType = ApiType.INNER_API)
    /**
     * 处理来自集群节点的配置变更同步请求。
     *
     * @param configChangeSyncRequest 变更 dataId/group/tenant 及灰度信息
     * @param meta                    来源节点 RPC 元数据
     * @return 空成功响应
     * @throws NacosException dump 失败时抛出
     */
    public ConfigChangeClusterSyncResponse handle(
        ConfigChangeClusterSyncRequest configChangeSyncRequest,
        RequestMeta meta) throws NacosException {
        
        checkCompatity(configChangeSyncRequest, meta);
        
        ParamUtils.checkParam(configChangeSyncRequest.getTag());
        DumpRequest dumpRequest = DumpRequest.create(configChangeSyncRequest.getDataId(),
            configChangeSyncRequest.getGroup(), configChangeSyncRequest.getTenant(),
            configChangeSyncRequest.getLastModified(), meta.getClientIp());
        
        dumpRequest.setGrayName(configChangeSyncRequest.getGrayName());
        dumpService.dump(dumpRequest);
        return new ConfigChangeClusterSyncResponse();
    }
    
    /**
     * 若通知来自旧版服务端，尝试迁移 Beta/Tag 灰度并转换为新 grayName 模型。
     *
     * @param configChangeSyncRequest request.
     */
    private void checkCompatity(ConfigChangeClusterSyncRequest configChangeSyncRequest,
        RequestMeta meta) {
        if (PropertyUtil.isGrayCompatibleModel()
            && StringUtils.isBlank(configChangeSyncRequest.getGrayName())) {
            if (configChangeSyncRequest.isBeta()
                || StringUtils.isNotBlank(configChangeSyncRequest.getTag())) {
                
                String grayName = null;
                // 旧集群仍使用 beta/tag 字段，需迁移到新 grayName 存储
                if (configChangeSyncRequest.isBeta()) {
                    configMigrateService.checkMigrateBeta(configChangeSyncRequest.getDataId(),
                        configChangeSyncRequest.getGroup(), configChangeSyncRequest.getTenant());
                    grayName = BetaGrayRule.TYPE_BETA;
                } else {
                    configMigrateService.checkMigrateTag(configChangeSyncRequest.getDataId(),
                        configChangeSyncRequest.getGroup(), configChangeSyncRequest.getTenant(),
                        configChangeSyncRequest.getTag());
                    grayName = TagGrayRule.TYPE_TAG + "_" + configChangeSyncRequest.getTag();
                }
                configChangeSyncRequest.setGrayName(grayName);
                
            }
        }
        
        if (!checkNamespaceCompatible(configChangeSyncRequest, meta)) {
            return;
        }
        
        if (StringUtils.isNotBlank(configChangeSyncRequest.getGrayName())) {
            configMigrateService.namespaceMigrateGray(configChangeSyncRequest.getDataId(),
                configChangeSyncRequest.getGroup(), configChangeSyncRequest.getTenant(),
                configChangeSyncRequest.getGrayName());
        } else {
            configMigrateService.namespaceMigrate(configChangeSyncRequest.getDataId(),
                configChangeSyncRequest.getGroup(), configChangeSyncRequest.getTenant());
        }
        
        configChangeSyncRequest.setTenant("public");
    }
    
    /**
     * 检查是否处于命名空间兼容模式且对端版本低于 3.0.0。
     *
     * @param configSyncRequest the config sync request
     * @param meta              the meta
     * @return the boolean
     */
    public boolean checkNamespaceCompatible(ConfigChangeClusterSyncRequest configSyncRequest,
        RequestMeta meta) {
        if (!ConfigCompatibleConfig.getInstance().isNamespaceCompatibleMode()) {
            return false;
        }
        final String ignoreCheckVersion = "3.0.0";
        final String clusterVersionPrefixNew = "Nacos-Server:v";
        final String clusterVersionPrefixOld = "Nacos-Java-Client:v";
        try {
            String version = null;
            if (meta.getClientVersion().split(clusterVersionPrefixNew).length > 1) {
                version = meta.getClientVersion().split(clusterVersionPrefixNew)[1];
            } else {
                version = meta.getClientVersion().split(clusterVersionPrefixOld)[1];
            }
            if (VersionUtils.compareVersion(version, ignoreCheckVersion) >= 0) {
                return false;
            }
        } catch (Exception e) {
            Loggers.REMOTE_DIGEST.error("checkCompatity error", e);
        }
        return StringUtils.equals(configSyncRequest.getTenant(), "public") || StringUtils.isBlank(
            configSyncRequest.getTenant());
    }
    
}
