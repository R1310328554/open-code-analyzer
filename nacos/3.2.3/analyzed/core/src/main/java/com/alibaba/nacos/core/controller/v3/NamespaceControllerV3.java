/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.controller.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.namespace.model.form.NamespaceForm;
import com.alibaba.nacos.core.namespace.repository.NamespacePersistService;
import com.alibaba.nacos.core.service.NamespaceOperationService;
import com.alibaba.nacos.core.utils.Commons;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.alibaba.nacos.core.utils.Commons.NACOS_ADMIN_CORE_CONTEXT_V3;

/**
 * 命名空间管理 HTTP 接口 v3：列表、详情、创建、更新、删除及 ID 存在性校验。
 * NamespaceControllerV3.
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(NACOS_ADMIN_CORE_CONTEXT_V3 + "/namespace")
public class NamespaceControllerV3 {
    
    /** 命名空间业务操作服务。 */
    private final NamespaceOperationService namespaceOperationService;
    
    /** 命名空间持久化服务，用于存在性统计。 */
    private final NamespacePersistService namespacePersistService;
    
    /**
     * 注入命名空间操作与持久化服务。
     *
     * @param namespaceOperationService 命名空间业务服务
     * @param namespacePersistService 持久化服务
     */
    public NamespaceControllerV3(NamespaceOperationService namespaceOperationService,
        NamespacePersistService namespacePersistService) {
        this.namespaceOperationService = namespaceOperationService;
        this.namespacePersistService = namespacePersistService;
    }
    
    /** 命名空间 ID 合法字符正则（字母数字下划线与连字符）。 */
    private final Pattern namespaceIdCheckPattern = Pattern.compile("^[\\w-]+");
    
    /** 命名空间名称禁止特殊符号的正则。 */
    private final Pattern namespaceNameCheckPattern = Pattern.compile("^[^@#$%^&*]+$");
    
    /** 命名空间 ID 最大长度。 */
    private static final int NAMESPACE_ID_MAX_LENGTH = 128;
    
    /**
     * 获取全部命名空间列表。
     *
     * @return namespace list
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(resource = Commons.NACOS_ADMIN_CORE_CONTEXT_V3
        + "/namespace", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<List<Namespace>> getNamespaceList() {
        return Result.success(namespaceOperationService.getNamespaceList());
    }
    
    /**
     * 按 ID 查询命名空间完整信息。
     *
     * @param namespaceId namespaceId
     * @return namespace all info
     */
    @Since("3.0.0")
    @GetMapping
    @Secured(resource = Commons.NACOS_ADMIN_CORE_CONTEXT_V3
        + "namespaces", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<Namespace> getNamespace(@RequestParam("namespaceId") String namespaceId)
        throws NacosException {
        return Result.success(namespaceOperationService.getNamespace(namespaceId));
    }
    
    /**
     * 创建命名空间，未指定 ID 时自动生成 UUID。
     *
     * @param namespaceForm namespaceForm.
     * @return whether create ok
     */
    @Since("3.0.0")
    @PostMapping
    @Secured(resource = Commons.NACOS_ADMIN_CORE_CONTEXT_V3
        + "namespaces", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<Boolean> createNamespace(NamespaceForm namespaceForm) throws Exception {
        namespaceForm.validate();
        
        String namespaceId = namespaceForm.getNamespaceId();
        String namespaceName = namespaceForm.getNamespaceName();
        String namespaceDesc = namespaceForm.getNamespaceDesc();
        
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = UUID.randomUUID().toString();
        } else {
            // TODO 校验逻辑应迁移至参数校验 Filter
            namespaceId = namespaceId.trim();
            if (!namespaceIdCheckPattern.matcher(namespaceId).matches()) {
                throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                    ErrorCode.ILLEGAL_NAMESPACE,
                    "namespaceId [" + namespaceId + "] mismatch the pattern");
            }
            if (namespaceId.length() > NAMESPACE_ID_MAX_LENGTH) {
                throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                    ErrorCode.ILLEGAL_NAMESPACE,
                    "too long namespaceId, over " + NAMESPACE_ID_MAX_LENGTH);
            }
        }
        // 名称含非法字符
        if (!namespaceNameCheckPattern.matcher(namespaceName).matches()) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.ILLEGAL_NAMESPACE,
                "namespaceName [" + namespaceName + "] contains illegal char");
        }
        return Result.success(
            namespaceOperationService.createNamespace(namespaceId, namespaceName, namespaceDesc));
    }
    
    /**
     * 更新命名空间名称与描述。
     *
     * @param namespaceForm namespace params
     * @return whether edit ok
     */
    @Since("3.0.0")
    @PutMapping
    @Secured(resource = Commons.NACOS_ADMIN_CORE_CONTEXT_V3
        + "namespaces", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<Boolean> updateNamespace(NamespaceForm namespaceForm) throws NacosException {
        namespaceForm.validate();
        // contains illegal chars
        if (!namespaceNameCheckPattern.matcher(namespaceForm.getNamespaceName()).matches()) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.ILLEGAL_NAMESPACE,
                "namespaceName [" + namespaceForm.getNamespaceName() + "] contains illegal char");
        }
        return Result
            .success(namespaceOperationService.editNamespace(namespaceForm.getNamespaceId(),
                namespaceForm.getNamespaceName(), namespaceForm.getNamespaceDesc()));
    }
    
    /**
     * 按 ID 删除命名空间。
     *
     * @param namespaceId namespace ID
     * @return whether delete ok
     */
    @Since("3.0.0")
    @DeleteMapping
    @Secured(resource = Commons.NACOS_ADMIN_CORE_CONTEXT_V3
        + "namespaces", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<Boolean> deleteNamespace(@RequestParam("namespaceId") String namespaceId) {
        return Result.success(namespaceOperationService.removeNamespace(namespaceId));
    }
    
    /**
     * 检查命名空间 ID 是否已存在（返回租户记录数）。
     *
     * @param namespaceId namespaceId
     * @return whether exist
     */
    @Since("3.0.0")
    @GetMapping("/check")
    @Secured(resource = Commons.NACOS_ADMIN_CORE_CONTEXT_V3
        + "namespaces", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<Integer> checkNamespaceIdExist(@RequestParam("namespaceId") String namespaceId) {
        return Result.success(namespacePersistService.tenantInfoCountByTenantId(namespaceId));
    }
}
