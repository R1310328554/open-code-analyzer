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

package com.alibaba.nacos.core.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.namespace.injector.NamespaceDetailInjectorHolder;
import com.alibaba.nacos.core.namespace.model.NamespaceTypeEnum;
import com.alibaba.nacos.core.namespace.model.TenantInfo;
import com.alibaba.nacos.core.namespace.repository.NamespacePersistService;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 命名空间运维服务，提供控制台对租户（Namespace）的 CRUD 与存在性校验。
 *
 * <p>默认 public 命名空间不持久化，自定义命名空间通过 {@link NamespacePersistService} 读写。</p>
 *
 * @author dongyafei
 * @date 2022/8/16
 */

@Service
public class NamespaceOperationService {
    
    /** 命名空间持久化服务。 */
    private final NamespacePersistService namespacePersistService;
    
    /** 默认 public 命名空间展示名。 */
    private static final String DEFAULT_NAMESPACE_SHOW_NAME = "public";
    
    /** 默认 public 命名空间描述。 */
    private static final String DEFAULT_NAMESPACE_DESCRIPTION = "Default Namespace";
    
    /** 命名空间默认配额。 */
    private static final int DEFAULT_QUOTA = 200;
    
    /** 创建命名空间时的默认来源标识。 */
    private static final String DEFAULT_CREATE_SOURCE = "nacos";
    
    /** 租户查询/写入使用的默认 kp 键。 */
    private static final String DEFAULT_KP = "1";
    
    /** 注入命名空间持久化服务。 */
    public NamespaceOperationService(NamespacePersistService namespacePersistService) {
        this.namespacePersistService = namespacePersistService;
    }
    
    /** 返回全部命名空间列表（含 public 与自定义租户）。 */
    public List<Namespace> getNamespaceList() {
        // TODO: 后续改为可配置 kp
        List<TenantInfo> tenantInfos = namespacePersistService.findTenantByKp(DEFAULT_KP);
        
        Namespace namespace0 = new Namespace(NamespaceUtil.getNamespaceDefaultId(),
            DEFAULT_NAMESPACE_SHOW_NAME,
            DEFAULT_NAMESPACE_DESCRIPTION, DEFAULT_QUOTA, 0, NamespaceTypeEnum.GLOBAL.getType());
        NamespaceDetailInjectorHolder.getInstance().injectDetail(namespace0);
        List<Namespace> namespaceList = new ArrayList<>();
        namespaceList.add(namespace0);
        
        for (TenantInfo tenantInfo : tenantInfos) {
            Namespace namespaceTmp = new Namespace(tenantInfo.getTenantId(),
                tenantInfo.getTenantName(),
                tenantInfo.getTenantDesc(), DEFAULT_QUOTA, 0, NamespaceTypeEnum.CUSTOM.getType());
            NamespaceDetailInjectorHolder.getInstance().injectDetail(namespaceTmp);
            namespaceList.add(namespaceTmp);
        }
        return namespaceList;
    }
    
    /**
     * 按命名空间 ID 查询（默认自定义类型）。
     *
     * @param namespaceId 命名空间 ID
     * @return 命名空间详情
     * @throws NacosException 不存在时抛出
     */
    public Namespace getNamespace(String namespaceId) throws NacosException {
        return getNamespace(namespaceId, NamespaceTypeEnum.CUSTOM);
    }
    
    /**
     * 按 ID 与类型查询命名空间。
     *
     * @param namespaceId 命名空间 ID
     * @param type        命名空间类型
     * @return 命名空间详情
     * @throws NacosException 自定义命名空间不存在时抛出
     */
    public Namespace getNamespace(String namespaceId, NamespaceTypeEnum type)
        throws NacosException {
        Namespace result;
        if (StringUtils.isBlank(namespaceId)
            || namespaceId.equals(NamespaceUtil.getNamespaceDefaultId())) {
            result = new Namespace(namespaceId, DEFAULT_NAMESPACE_SHOW_NAME,
                DEFAULT_NAMESPACE_DESCRIPTION,
                DEFAULT_QUOTA, 0, NamespaceTypeEnum.GLOBAL.getType());
            
        } else {
            String typeString = String.valueOf(type.getType());
            TenantInfo tenantInfo = namespacePersistService.findTenantByKp(typeString, namespaceId);
            if (null == tenantInfo) {
                throw new NacosApiException(HttpStatus.NOT_FOUND.value(),
                    ErrorCode.NAMESPACE_NOT_EXIST,
                    "namespaceId [ " + namespaceId + " ] not exist");
            }
            result = new Namespace(namespaceId, tenantInfo.getTenantName(),
                tenantInfo.getTenantDesc(), DEFAULT_QUOTA,
                0, NamespaceTypeEnum.CUSTOM.getType());
        }
        NamespaceDetailInjectorHolder.getInstance().injectDetail(result);
        return result;
    }
    
    /**
     * 创建自定义命名空间。
     *
     * @param namespaceId   命名空间 ID
     * @param namespaceName 展示名称
     * @param namespaceDesc 描述
     * @return 是否创建成功
     * @throws NacosException 已存在或参数非法时抛出
     */
    public Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException {
        return createNamespace(namespaceId, namespaceName, namespaceDesc, NamespaceTypeEnum.CUSTOM);
    }
    
    /**
     * 按指定类型创建命名空间。
     *
     * @param namespaceId   命名空间 ID
     * @param namespaceName 展示名称
     * @param namespaceDesc 描述
     * @param type          命名空间类型，见 {@link NamespaceTypeEnum}
     * @return 是否创建成功
     * @throws NacosException 已存在时抛出
     */
    public Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc,
        NamespaceTypeEnum type) throws NacosException {
        validateNamespaceNotExists(namespaceId);
        String typeString = String.valueOf(type.getType());
        namespacePersistService.insertTenantInfoAtomic(typeString, namespaceId, namespaceName,
            namespaceDesc,
            DEFAULT_CREATE_SOURCE, System.currentTimeMillis());
        return true;
    }
    
    /**
     * 编辑命名空间名称与描述。
     *
     * @param namespaceId   命名空间 ID
     * @param namespaceName 新名称
     * @param namespaceDesc 新描述
     * @return 是否更新成功
     */
    public Boolean editNamespace(String namespaceId, String namespaceName, String namespaceDesc) {
        namespacePersistService.updateTenantNameAtomic(DEFAULT_KP, namespaceId, namespaceName,
            namespaceDesc);
        return true;
    }
    
    /**
     * 删除指定命名空间。
     *
     * @param namespaceId 命名空间 ID
     * @return 是否删除成功
     */
    public Boolean removeNamespace(String namespaceId) {
        namespacePersistService.removeTenantInfoAtomic(DEFAULT_KP, namespaceId);
        return true;
    }
    
    /**
     * 检查命名空间是否存在（public 默认视为存在）。
     *
     * @param namespaceId 命名空间 ID
     * @return 存在返回 {@code true}
     */
    public boolean namespaceExists(String namespaceId) {
        try {
            if (NamespaceUtil.isDefaultNamespaceId(namespaceId)) {
                return true;
            }
            return namespacePersistService.tenantInfoCountByTenantId(namespaceId) > 0;
        } catch (Exception e) {
            Loggers.CORE.error(
                "Namespace validation query db error for namespace: {}, exception: {}", namespaceId,
                e);
            return false;
        }
    }
    
    /**
     * 校验命名空间尚未存在，已存在则抛出异常。
     *
     * @param namespaceId 待创建的命名空间 ID
     * @throws NacosApiException 已存在时抛出
     */
    public void validateNamespaceNotExists(String namespaceId) throws NacosApiException {
        if (namespaceExists(namespaceId)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                ErrorCode.NAMESPACE_ALREADY_EXIST,
                "namespaceId [" + namespaceId + "] already exist.");
        }
    }
}
