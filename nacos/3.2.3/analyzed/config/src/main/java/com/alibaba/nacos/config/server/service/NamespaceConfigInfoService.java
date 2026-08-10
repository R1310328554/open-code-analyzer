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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.config.server.constant.PropertiesConstant;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoPersistService;
import com.alibaba.nacos.core.namespace.injector.AbstractNamespaceDetailInjector;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Service;

/**
 * 命名空间详情注入器：为 {@link Namespace} 填充配置配额与当前配置条数，
 * 供控制台展示租户容量概览。
 * Namespace detail for config info.
 *
 * @author xiweng.yy
 */
@Service
public class NamespaceConfigInfoService extends AbstractNamespaceDetailInjector {
    
    private final ConfigInfoPersistService configInfoPersistService;
    
    public NamespaceConfigInfoService(ConfigInfoPersistService configInfoPersistService) {
        this.configInfoPersistService = configInfoPersistService;
    }
    
    /**
     * 注入命名空间配额（若配置）与 configCount。
     *
     * @param namespace 待 enrich 的命名空间对象
     */
    @Override
    public void injectDetail(Namespace namespace) {
        
        if (EnvUtil.getProperty(PropertiesConstant.DEFAULT_TENANT_QUOTA, Integer.class) != null) {
            namespace.setQuota(
                EnvUtil.getProperty(PropertiesConstant.DEFAULT_TENANT_QUOTA, Integer.class));
        }
        
        // 统计该命名空间下配置总数并写入 namespace
        int configCount = configInfoPersistService.configInfoCount(namespace.getNamespace());
        namespace.setConfigCount(configCount);
    }
}
