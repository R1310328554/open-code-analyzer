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

package com.alibaba.nacos.console.controller.v3.core;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.paramcheck.ConsoleDefaultHttpParamExtractor;
import com.alibaba.nacos.console.proxy.core.NamespaceProxy;
import com.alibaba.nacos.core.namespace.model.form.CreateNamespaceForm;
import com.alibaba.nacos.core.namespace.model.form.NamespaceForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制台 v3 命名空间 REST 控制器：提供命名空间的增删改查及存在性校验接口。
 * Controller for handling HTTP requests related to namespace operations.
 *
 * @author zhangyukun on:2024/8/27
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/core/namespace")
@ExtractorManager.Extractor(httpExtractor = ConsoleDefaultHttpParamExtractor.class)
public class ConsoleNamespaceController {
    
    /** 命名空间操作代理，转发至 core 命名空间服务 */
    private final NamespaceProxy namespaceProxy;
    
    /** 注入命名空间代理并构造控制器 */
    public ConsoleNamespaceController(NamespaceProxy namespaceProxy) {
        this.namespaceProxy = namespaceProxy;
    }
    
    /**
     * 获取全部命名空间列表。
     * Get namespace list.
     *
     * @return 命名空间列表
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "namespaces", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API, tags = Constants.Tag.ONLY_IDENTITY)
    public Result<List<Namespace>> getNamespaceList() throws NacosException {
        return Result.success(namespaceProxy.getNamespaceList());
    }
    
    /**
     * 按命名空间 ID 查询完整命名空间详情。
     * get namespace all info by namespace id.
     *
     * @param namespaceId 命名空间 ID
     * @return 命名空间完整信息
     */
    @Since("3.0.0")
    @GetMapping()
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "namespaces", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<Namespace> getNamespaceDetail(@RequestParam("namespaceId") String namespaceId)
        throws NacosException {
        return Result.success(namespaceProxy.getNamespaceDetail(namespaceId));
    }
    
    /**
     * 创建新命名空间（校验表单后调用代理创建）。
     * create namespace.
     *
     * @param namespaceForm 创建命名空间表单
     * @return 是否创建成功
     */
    @Since("3.0.0")
    @PostMapping
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "namespaces", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<Boolean> createNamespace(CreateNamespaceForm namespaceForm)
        throws NacosException {
        namespaceForm.validate();
        String namespaceId = namespaceForm.getCustomNamespaceId();
        String namespaceName = namespaceForm.getNamespaceName();
        String namespaceDesc = namespaceForm.getNamespaceDesc();
        return Result
            .success(namespaceProxy.createNamespace(namespaceId, namespaceName, namespaceDesc));
    }
    
    /**
     * 编辑已有命名空间元数据。
     * edit namespace.
     *
     * @param namespaceForm 命名空间编辑表单
     * @return 是否更新成功
     */
    @Since("3.0.0")
    @PutMapping
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "namespaces", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<Boolean> updateNamespace(NamespaceForm namespaceForm) throws NacosException {
        namespaceForm.validate();
        return Result.success(namespaceProxy.updateNamespace(namespaceForm));
    }
    
    /**
     * 按 ID 删除命名空间。
     * delete namespace by id.
     *
     * @param namespaceId 待删除的命名空间 ID
     * @return 是否删除成功
     */
    @Since("3.0.0")
    @DeleteMapping
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "namespaces", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<Boolean> deleteNamespace(@RequestParam("namespaceId") String namespaceId)
        throws NacosException {
        return Result.success(namespaceProxy.deleteNamespace(namespaceId));
    }
    
    /**
     * 校验自定义命名空间 ID 是否已存在（空 ID 表示将使用 UUID 创建）。
     * check namespaceId exist.
     *
     * @param namespaceId 自定义命名空间 ID
     * @return 已存在返回 true，否则 false
     */
    @Since("3.0.0")
    @GetMapping("/exist")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "namespaces", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API, tags = Constants.Tag.ONLY_IDENTITY)
    public Result<Boolean> checkNamespaceIdExist(
        @RequestParam("customNamespaceId") String namespaceId)
        throws NacosException {
        // customNamespaceId 为空表示将使用 UUID 创建新命名空间
        if (StringUtils.isBlank(namespaceId)) {
            return Result.success(false);
        }
        return Result.success(namespaceProxy.checkNamespaceIdExist(namespaceId));
    }
}
