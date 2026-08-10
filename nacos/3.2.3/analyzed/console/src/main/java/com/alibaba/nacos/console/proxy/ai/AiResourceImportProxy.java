/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.proxy.ai;

import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSourceInfo;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.console.handler.ai.AiResourceImportHandler;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 资源导入代理：委托 {@link AiResourceImportHandler} 完成外部源列举、搜索、校验与批量导入。
 * Proxy for Console AI resource import operations.
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
@Service
public class AiResourceImportProxy {
    
    /** AI 资源导入 Handler 实现 */
    private final AiResourceImportHandler importHandler;
    
    /** 注入 AI 资源导入 Handler。 */
    public AiResourceImportProxy(AiResourceImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    /**
     * 列出可用的 AI 资源导入外部源。
     * List import sources.
     *
     * @param resourceType 可选的资源类型过滤
     * @return 导入源列表
     * @throws NacosException 源配置无效时抛出
     */
    public List<AiResourceImportSourceInfo> listSources(String resourceType)
        throws NacosException {
        return importHandler.listSources(resourceType);
    }
    
    /**
     * 在外部源中搜索待导入候选资源。
     * Search external candidates.
     *
     * @param request 搜索请求
     * @return 搜索响应
     * @throws NacosException 源不可搜索时抛出
     */
    public AiResourceImportSearchResponse search(AiResourceImportSearchRequest request)
        throws NacosException {
        return importHandler.search(request);
    }
    
    /**
     * 校验已选候选资源是否可导入。
     * Validate selected candidates.
     *
     * @param request 校验请求
     * @return 校验响应
     * @throws NacosException 无法启动校验时抛出
     */
    public AiResourceImportValidateResponse validate(AiResourceImportValidateRequest request)
        throws NacosException {
        return importHandler.validate(request);
    }
    
    /**
     * 执行选中候选资源的导入。
     * Execute import for selected candidates.
     *
     * @param request 执行请求
     * @return 导入执行响应
     * @throws NacosException 无法启动导入时抛出
     */
    public AiResourceImportExecuteResponse execute(AiResourceImportExecuteRequest request)
        throws NacosException {
        return importHandler.execute(request);
    }
}
