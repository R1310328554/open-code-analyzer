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

package com.alibaba.nacos.console.handler.impl.inner.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.console.handler.core.NamespaceHandler;
import com.alibaba.nacos.console.handler.impl.inner.EnabledInnerHandler;
import com.alibaba.nacos.core.namespace.model.form.NamespaceForm;
import com.alibaba.nacos.core.service.NamespaceOperationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 命名空间内嵌 Handler：CRUD 与存在性校验，委托 {@link NamespaceOperationService}。
 * Implementation of NamespaceHandler that handles namespace-related operations.
 *
 * @author zhangyukun
 */
@Service
@EnabledInnerHandler
public class NamespaceInnerHandler implements NamespaceHandler {
    
    /** 命名空间运维服务 */
    private final NamespaceOperationService namespaceOperationService;
    
    /** 注入命名空间运维服务 */
    public NamespaceInnerHandler(NamespaceOperationService namespaceOperationService) {
        this.namespaceOperationService = namespaceOperationService;
    }
    
    /** 获取全部命名空间列表 */
    @Override
    public List<Namespace> getNamespaceList() {
        return namespaceOperationService.getNamespaceList();
    }
    
    /** 按 ID 获取命名空间详情 */
    @Override
    public Namespace getNamespaceDetail(String namespaceId) throws NacosException {
        return namespaceOperationService.getNamespace(namespaceId);
    }
    
    /** 创建新命名空间 */
    @Override
    public Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException {
        return namespaceOperationService.createNamespace(namespaceId, namespaceName, namespaceDesc);
    }
    
    /** 更新命名空间名称与描述 */
    @Override
    public Boolean updateNamespace(NamespaceForm namespaceForm) throws NacosException {
        return namespaceOperationService.editNamespace(namespaceForm.getNamespaceId(),
            namespaceForm.getNamespaceName(),
            namespaceForm.getNamespaceDesc());
    }
    
    /** 删除指定命名空间 */
    @Override
    public Boolean deleteNamespace(String namespaceId) {
        return namespaceOperationService.removeNamespace(namespaceId);
    }
    
    /** 校验命名空间 ID 是否已存在 */
    @Override
    public Boolean checkNamespaceIdExist(String namespaceId) {
        return namespaceOperationService.namespaceExists(namespaceId);
    }
}
