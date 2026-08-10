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

package com.alibaba.nacos.console.proxy.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.console.handler.core.NamespaceHandler;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.core.namespace.model.form.NamespaceForm;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 命名空间代理：将命名空间 CRUD 与存在性检查委托给 {@link NamespaceHandler}。
 * Proxy class for handling namespace operations.
 *
 * @author zhangyukun
 */
@Service
public class NamespaceProxy {
    
    /** 命名空间 Handler 实现 */
    private final NamespaceHandler namespaceHandler;
    
    /** 注入命名空间 Handler。 */
    public NamespaceProxy(NamespaceHandler namespaceHandler) {
        this.namespaceHandler = namespaceHandler;
    }
    
    /** 获取全部命名空间列表。 */
    /**
     * Get namespace list.
      * <p>命名空间代理；详见类级说明。</p>
     */
    public List<Namespace> getNamespaceList() throws NacosException {
        return namespaceHandler.getNamespaceList();
    }
    
    /** 获取指定命名空间详情。 */
    /**
     * Get the specific namespace information.
      * <p>命名空间代理；详见类级说明。</p>
     */
    public Namespace getNamespaceDetail(String namespaceId) throws NacosException {
        return namespaceHandler.getNamespaceDetail(namespaceId);
    }
    
    /** 创建命名空间。 */
    /**
     * Create or update namespace.
      * <p>命名空间代理；详见类级说明。</p>
     */
    public Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException {
        return namespaceHandler.createNamespace(namespaceId, namespaceName, namespaceDesc);
    }
    
    /** 编辑命名空间信息。 */
    /**
     * Edit namespace.
      * <p>命名空间代理；详见类级说明。</p>
     */
    public Boolean updateNamespace(NamespaceForm namespaceForm) throws NacosException {
        return namespaceHandler.updateNamespace(namespaceForm);
    }
    
    /** 删除命名空间。 */
    /**
     * Delete namespace.
      * <p>命名空间代理；详见类级说明。</p>
     */
    public Boolean deleteNamespace(String namespaceId) throws NacosException {
        return namespaceHandler.deleteNamespace(namespaceId);
    }
    
    /** 检查命名空间 ID 是否已存在。 */
    /**
     * Check if namespace exists.
      * <p>命名空间代理；详见类级说明。</p>
     */
    public Boolean checkNamespaceIdExist(String namespaceId) throws NacosException {
        return namespaceHandler.checkNamespaceIdExist(namespaceId);
    }
}
