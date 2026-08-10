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

package com.alibaba.nacos.console.handler.ai;

import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSourceInfo;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateResponse;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.List;

/**
 * 控制台 AI 资源导入处理器接口：列举导入源、搜索、校验与执行外部资源导入。
 * Handler for Console AI resource import operations.
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public interface AiResourceImportHandler {
    
    /**
     * 列出已配置的 AI 资源导入源，可按资源类型过滤。
     * List import sources.
     *
     * @param resourceType 可选的资源类型过滤
     * @return 导入源信息列表
     * @throws NacosException 源配置无效
     */
    List<AiResourceImportSourceInfo> listSources(String resourceType) throws NacosException;
    
    /**
     * 在外部导入源中搜索候选资源。
     * Search external candidates.
     *
     * @param request 搜索请求
     * @return 搜索响应
     * @throws NacosException 源不可搜索
     */
    AiResourceImportSearchResponse search(AiResourceImportSearchRequest request)
        throws NacosException;
    
    /**
     * 校验已选候选资源是否符合导入条件。
     * Validate selected candidates.
     *
     * @param request 校验请求
     * @return 校验结果响应
     * @throws NacosException 无法启动校验
     */
    AiResourceImportValidateResponse validate(AiResourceImportValidateRequest request)
        throws NacosException;
    
    /**
     * 对已通过校验的候选资源执行导入。
     * Execute import for selected candidates.
     *
     * @param request 执行导入请求
     * @return 导入执行响应
     * @throws NacosException 无法启动导入
     */
    AiResourceImportExecuteResponse execute(AiResourceImportExecuteRequest request)
        throws NacosException;
}
