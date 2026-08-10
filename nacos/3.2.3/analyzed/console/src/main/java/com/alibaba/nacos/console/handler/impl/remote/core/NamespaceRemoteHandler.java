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

package com.alibaba.nacos.console.handler.impl.remote.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.console.handler.core.NamespaceHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.core.namespace.model.form.NamespaceForm;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 命名空间远程 Handler：CRUD 与存在性校验，通过 {@link NacosMaintainerClientHolder} 调用远端 Naming Maintainer API。
 * Remote Implementation of NamespaceHandler that handles namespace-related operations.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
public class NamespaceRemoteHandler implements NamespaceHandler {
    
    /** 运维客户端持有者，提供 Naming Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public NamespaceRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 获取远端全部命名空间列表。 */
    @Override
    public List<Namespace> getNamespaceList() throws NacosException {
        return clientHolder.getNamingMaintainerService().getNamespaceList();
    }
    
    /** 按 ID 获取远端命名空间详情。 */
    @Override
    public Namespace getNamespaceDetail(String namespaceId) throws NacosException {
        return clientHolder.getNamingMaintainerService().getNamespace(namespaceId);
    }
    
    /** 在远端创建新命名空间。 */
    @Override
    public Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException {
        return clientHolder.getNamingMaintainerService().createNamespace(namespaceId, namespaceName,
            namespaceDesc);
    }
    
    /** 更新远端命名空间名称与描述。 */
    @Override
    public Boolean updateNamespace(NamespaceForm namespaceForm) throws NacosException {
        return clientHolder.getNamingMaintainerService()
            .updateNamespace(namespaceForm.getNamespaceId(), namespaceForm.getNamespaceName(),
                namespaceForm.getNamespaceDesc());
    }
    
    /** 删除远端指定命名空间。 */
    @Override
    public Boolean deleteNamespace(String namespaceId) throws NacosException {
        return clientHolder.getNamingMaintainerService().deleteNamespace(namespaceId);
    }
    
    /** 校验远端命名空间 ID 是否已存在。 */
    @Override
    public Boolean checkNamespaceIdExist(String namespaceId) throws NacosException {
        return clientHolder.getNamingMaintainerService().checkNamespaceIdExist(namespaceId);
    }
}
