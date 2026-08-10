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

package com.alibaba.nacos.ai.importer.manager;

import com.alibaba.nacos.ai.importer.operator.AiResourceOperator;
import com.alibaba.nacos.ai.importer.operator.AiResourceOperatorRegistry;
import com.alibaba.nacos.ai.importer.security.AiResourceImportSecurityGuard;
import com.alibaba.nacos.ai.service.VisibilityHelper;
import com.alibaba.nacos.ai.service.trace.AiResourceTraceService;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportCandidateItem;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportItem;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportResultItem;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportResultStatus;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSourceInfo;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateRequest;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidationItem;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidationStatus;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportArtifact;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportCandidate;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportCandidatePage;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportContext;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportSource;
import com.alibaba.nacos.plugin.ai.importer.spi.AiResourceImportService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates AI resource import search, validation, and execution.
 * <p>AI 资源导入编排服务，协调导入源解析、插件调用、安全校验与资源操作器，提供 search / validate / execute 三阶段流程并记录追踪日志。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
@Service
public class AiResourceImportManager {
    
    /** 导入源解析与管理。 */
    private final AiResourceImportSourceManager sourceManager;
    
    /** 导入插件加载与路由。 */
    private final AiResourceImportPluginManager pluginManager;
    
    /** 资源类型操作器注册表（MCP、Skill 等）。 */
    private final AiResourceOperatorRegistry operatorRegistry;
    
    /** 导入安全守卫（端点/制品校验）。 */
    private final AiResourceImportSecurityGuard securityGuard;
    
    public AiResourceImportManager(AiResourceImportSourceManager sourceManager,
        AiResourceImportPluginManager pluginManager, AiResourceOperatorRegistry operatorRegistry,
        AiResourceImportSecurityGuard securityGuard) {
        this.sourceManager = sourceManager;
        this.pluginManager = pluginManager;
        this.operatorRegistry = operatorRegistry;
        this.securityGuard = securityGuard;
    }
    
    /**
     * List enabled import sources.
     * <p>列出已启用的导入源，可按 resourceType 过滤；返回信息不含 endpoint 等运行时敏感字段。</p>
     *
     * @param resourceType optional resource type
     * @return source info list
     * @throws NacosException if source configuration is invalid
     */
    public List<AiResourceImportSourceInfo> listSources(String resourceType)
        throws NacosException {
        return sourceManager.listSourceInfos(resourceType);
    }
    
    /**
     * Search external candidates from an operator-configured source.
     * <p>从运维配置的导入源搜索外部候选资源，支持游标分页。</p>
     *
     * @param request search request
     * @return search response
     * @throws NacosException if source or plugin cannot handle the request
     */
    public AiResourceImportSearchResponse search(AiResourceImportSearchRequest request)
        throws NacosException {
        requireRequest(request);
        AiResourceImportSource source = null;
        try {
            source = sourceManager.resolveSource(request.getSourceId(), request.getResourceType());
            securityGuard.checkSourceEndpoint(source); // 校验端点是否允许访问
            AiResourceImportService importer =
                pluginManager.resolveImporter(source, request.getResourceType());
            AiResourceImportCandidatePage page =
                importer.search(buildSearchContext(source, request));
            AiResourceImportSearchResponse response = new AiResourceImportSearchResponse();
            response.setSourceId(source.getSourceId());
            response.setResourceType(request.getResourceType());
            response.setItems(toCandidateItems(page == null ? null : page.getItems()));
            if (page != null) {
                response.setNextCursor(page.getNextCursor());
                response.setHasMore(page.isHasMore());
            }
            traceSourceOperation(source, request.getResourceType(),
                AiResourceTraceService.OP_IMPORT_SEARCH, AiResourceTraceService.STATUS_SUCCESS,
                searchTraceExt(source, response));
            return response;
        } catch (NacosException e) {
            traceSourceOperation(source, request.getResourceType(),
                AiResourceTraceService.OP_IMPORT_SEARCH, AiResourceTraceService.STATUS_FAILURE,
                failureTraceExt(source, request.getSourceId(), e.getErrMsg()));
            throw e;
        }
    }
    
    /**
     * Validate selected external candidates.
     * <p>校验所选外部候选：拉取制品、安全检查并调用对应操作器验证与 Nacos 现有状态的关系。</p>
     *
     * @param request validate request
     * @return validate response
     * @throws NacosException if source or plugin cannot be resolved
     */
    public AiResourceImportValidateResponse validate(AiResourceImportValidateRequest request)
        throws NacosException {
        requireRequest(request);
        requireSelectedItems(request.getSelectedItems());
        AiResourceImportSource source = null;
        try {
            source = sourceManager.resolveSource(request.getSourceId(), request.getResourceType());
            securityGuard.checkSourceEndpoint(source);
            AiResourceImportService importer =
                pluginManager.resolveImporter(source, request.getResourceType());
            AiResourceImportContext context = buildItemContext(source, request.getNamespaceId(),
                request.getResourceType(), request.getOptions());
            List<AiResourceImportValidationItem> items = new ArrayList<>();
            for (AiResourceImportItem each : request.getSelectedItems()) {
                items.add(validateItem(source, importer, context, each,
                    request.isOverwriteExisting()));
            }
            AiResourceImportValidateResponse response = new AiResourceImportValidateResponse();
            response.setSourceId(source.getSourceId());
            response.setResourceType(request.getResourceType());
            response.setItems(items);
            traceSourceOperation(source, request.getResourceType(),
                AiResourceTraceService.OP_IMPORT_VALIDATE,
                validationTraceStatus(items), validationTraceExt(source, request, items));
            return response;
        } catch (NacosException e) {
            traceSourceOperation(source, request.getResourceType(),
                AiResourceTraceService.OP_IMPORT_VALIDATE, AiResourceTraceService.STATUS_FAILURE,
                failureTraceExt(source, request.getSourceId(), e.getErrMsg()));
            throw e;
        }
    }
    
    /**
     * Execute import for selected external candidates.
     * <p>执行导入：逐条拉取制品并写入 Nacos，支持覆盖已有资源及跳过无效项。</p>
     *
     * @param request execute request
     * @return execute response
     * @throws NacosException if source or plugin cannot be resolved
     */
    public AiResourceImportExecuteResponse execute(AiResourceImportExecuteRequest request)
        throws NacosException {
        requireRequest(request);
        requireSelectedItems(request.getSelectedItems());
        AiResourceImportSource source = null;
        try {
            source = sourceManager.resolveSource(request.getSourceId(), request.getResourceType());
            securityGuard.checkSourceEndpoint(source);
            AiResourceImportService importer =
                pluginManager.resolveImporter(source, request.getResourceType());
            AiResourceImportContext context = buildItemContext(source, request.getNamespaceId(),
                request.getResourceType(), request.getOptions());
            List<AiResourceImportResultItem> results = new ArrayList<>();
            for (AiResourceImportItem each : request.getSelectedItems()) {
                AiResourceImportResultItem result = executeItem(source, importer, context, each,
                    request.isOverwriteExisting(), request.isSkipInvalid());
                traceExecuteResult(source, context.getResourceType(), each, result);
                results.add(result);
            }
            AiResourceImportExecuteResponse response = buildExecuteResponse(results);
            traceSourceOperation(source, request.getResourceType(),
                AiResourceTraceService.OP_IMPORT_EXECUTE, executeTraceStatus(response),
                executeTraceExt(source, request, response));
            return response;
        } catch (NacosException e) {
            traceSourceOperation(source, request.getResourceType(),
                AiResourceTraceService.OP_IMPORT_EXECUTE, AiResourceTraceService.STATUS_FAILURE,
                failureTraceExt(source, request.getSourceId(), e.getErrMsg()));
            throw e;
        }
    }
    
    private void requireRequest(AiResourceImportSearchRequest request) throws NacosException {
        if (request == null) {
            throw invalid("AI resource import request must not be null.");
        }
        if (StringUtils.isBlank(request.getResourceType())) {
            throw invalid("AI resource import resource type must not be empty.");
        }
        if (StringUtils.isBlank(request.getSourceId())) {
            throw invalid("AI resource import source id must not be empty.");
        }
    }
    
    private void requireRequest(AiResourceImportValidateRequest request) throws NacosException {
        if (request == null) {
            throw invalid("AI resource import request must not be null.");
        }
        if (StringUtils.isBlank(request.getResourceType())) {
            throw invalid("AI resource import resource type must not be empty.");
        }
        if (StringUtils.isBlank(request.getSourceId())) {
            throw invalid("AI resource import source id must not be empty.");
        }
    }
    
    private void requireRequest(AiResourceImportExecuteRequest request) throws NacosException {
        if (request == null) {
            throw invalid("AI resource import request must not be null.");
        }
        if (StringUtils.isBlank(request.getResourceType())) {
            throw invalid("AI resource import resource type must not be empty.");
        }
        if (StringUtils.isBlank(request.getSourceId())) {
            throw invalid("AI resource import source id must not be empty.");
        }
    }
    
    private void requireSelectedItems(List<AiResourceImportItem> selectedItems)
        throws NacosException {
        if (CollectionUtils.isEmpty(selectedItems)) {
            throw invalid("AI resource import selected items must not be empty.");
        }
    }
    
    private AiResourceImportContext buildSearchContext(AiResourceImportSource source,
        AiResourceImportSearchRequest request) {
        AiResourceImportContext context = buildItemContext(source, request.getNamespaceId(),
            request.getResourceType(), request.getOptions());
        context.setQuery(request.getQuery());
        context.setCursor(request.getCursor());
        context.setLimit(resolveLimit(source, request.getLimit()));
        return context;
    }
    
    private AiResourceImportContext buildItemContext(AiResourceImportSource source,
        String namespaceId, String resourceType, java.util.Map<String, String> options) {
        AiResourceImportContext context = new AiResourceImportContext();
        context.setNamespaceId(StringUtils.isBlank(namespaceId)
            ? com.alibaba.nacos.api.common.Constants.DEFAULT_NAMESPACE_ID : namespaceId);
        context.setResourceType(resourceType);
        context.setSource(source);
        context.setOptions(options);
        context.setOperator(VisibilityHelper.resolveCurrentIdentity());
        context.setClientIp(VisibilityHelper.resolveClientIp());
        return context;
    }
    
    private int resolveLimit(AiResourceImportSource source, Integer requestedLimit) {
        int defaultLimit = source.getMaxItemCount() > 0 ? source.getMaxItemCount() : 100;
        if (requestedLimit == null || requestedLimit <= 0) {
            return defaultLimit;
        }
        return Math.min(requestedLimit, defaultLimit);
    }
    
    private List<AiResourceImportCandidateItem> toCandidateItems(
        List<AiResourceImportCandidate> candidates) {
        if (CollectionUtils.isEmpty(candidates)) {
            return Collections.emptyList();
        }
        List<AiResourceImportCandidateItem> result = new ArrayList<>(candidates.size());
        for (AiResourceImportCandidate each : candidates) {
            AiResourceImportCandidateItem item = new AiResourceImportCandidateItem();
            item.setExternalId(each.getExternalId());
            item.setName(each.getName());
            item.setVersion(each.getVersion());
            item.setDescription(each.getDescription());
            item.setMetadata(each.getMetadata());
            result.add(item);
        }
        return result;
    }
    
    private AiResourceImportValidationItem validateItem(AiResourceImportSource source,
        AiResourceImportService importer, AiResourceImportContext context,
        AiResourceImportItem item,
        boolean overwriteExisting) {
        try {
            AiResourceImportArtifact artifact = importer.fetch(context, toPluginItem(item));
            securityGuard.checkArtifact(source, context.getResourceType(), artifact); // 校验制品大小与内容
            AiResourceOperator operator = operatorRegistry.getOperator(artifact.getResourceType());
            AiResourceImportValidationItem result =
                operator.validate(context.getNamespaceId(), artifact, overwriteExisting);
            return result == null ? defaultValidationItem(artifact) : result;
        } catch (NacosException e) {
            return invalidValidationItem(item, e.getErrMsg());
        }
    }
    
    private AiResourceImportResultItem executeItem(AiResourceImportSource source,
        AiResourceImportService importer, AiResourceImportContext context,
        AiResourceImportItem item,
        boolean overwriteExisting, boolean skipInvalid) {
        try {
            AiResourceImportArtifact artifact = importer.fetch(context, toPluginItem(item));
            securityGuard.checkArtifact(source, context.getResourceType(), artifact);
            AiResourceOperator operator = operatorRegistry.getOperator(artifact.getResourceType());
            AiResourceImportResultItem result =
                operator.importResource(context.getNamespaceId(), artifact, overwriteExisting);
            return result == null ? defaultResultItem(artifact) : result;
        } catch (NacosException e) {
            return failedResultItem(item, e.getErrMsg(), skipInvalid);
        }
    }
    
    private com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportItem toPluginItem(
        AiResourceImportItem item) {
        com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportItem result =
            new com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportItem();
        result.setExternalId(item.getExternalId());
        result.setName(item.getName());
        result.setVersion(item.getVersion());
        result.setMetadata(item.getMetadata());
        return result;
    }
    
    private AiResourceImportValidationItem defaultValidationItem(
        AiResourceImportArtifact artifact) {
        AiResourceImportValidationItem result = new AiResourceImportValidationItem();
        result.setExternalId(artifact.getExternalId());
        result.setName(artifact.getName());
        result.setVersion(artifact.getVersion());
        result.setStatus(AiResourceImportValidationStatus.VALID);
        return result;
    }
    
    private AiResourceImportValidationItem invalidValidationItem(AiResourceImportItem item,
        String errorMessage) {
        AiResourceImportValidationItem result = new AiResourceImportValidationItem();
        result.setExternalId(item.getExternalId());
        result.setName(item.getName());
        result.setVersion(item.getVersion());
        result.setStatus(AiResourceImportValidationStatus.INVALID);
        result.setErrors(Collections.singletonList(errorMessage));
        return result;
    }
    
    private AiResourceImportResultItem defaultResultItem(AiResourceImportArtifact artifact) {
        AiResourceImportResultItem result = new AiResourceImportResultItem();
        result.setExternalId(artifact.getExternalId());
        result.setResourceName(artifact.getName());
        result.setVersion(artifact.getVersion());
        result.setStatus(AiResourceImportResultStatus.SUCCESS);
        return result;
    }
    
    private AiResourceImportResultItem failedResultItem(AiResourceImportItem item,
        String errorMessage, boolean skipInvalid) {
        AiResourceImportResultItem result = new AiResourceImportResultItem();
        result.setExternalId(item.getExternalId());
        result.setResourceName(item.getName());
        result.setVersion(item.getVersion());
        result.setStatus(skipInvalid ? AiResourceImportResultStatus.SKIPPED
            : AiResourceImportResultStatus.FAILED);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    private AiResourceImportExecuteResponse buildExecuteResponse(
        List<AiResourceImportResultItem> results) {
        AiResourceImportExecuteResponse response = new AiResourceImportExecuteResponse();
        response.setResults(results);
        response.setTotalCount(results.size());
        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        for (AiResourceImportResultItem each : results) {
            if (AiResourceImportResultStatus.SUCCESS == each.getStatus()) {
                successCount++;
            } else if (AiResourceImportResultStatus.SKIPPED == each.getStatus()) {
                skippedCount++;
            } else {
                failedCount++;
            }
        }
        response.setSuccessCount(successCount);
        response.setFailedCount(failedCount);
        response.setSkippedCount(skippedCount);
        response.setSuccess(failedCount == 0);
        return response;
    }
    
    private void traceExecuteResult(AiResourceImportSource source, String resourceType,
        AiResourceImportItem item, AiResourceImportResultItem result) {
        Map<String, Object> ext = baseTraceExt(source);
        ext.put("external_id", item.getExternalId());
        ext.put("result_status", result.getStatus());
        if (StringUtils.isNotBlank(result.getErrorMessage())) {
            ext.put("error", result.getErrorMessage());
        }
        AiResourceTraceService.log(resourceType,
            StringUtils.defaultIfBlank(result.getResourceName(), item.getName()),
            result.getVersion(), AiResourceTraceService.OP_IMPORT_EXECUTE,
            traceStatus(result.getStatus()), VisibilityHelper.resolveCurrentIdentity(),
            VisibilityHelper.resolveClientIp(), JacksonUtils.toJson(ext));
    }
    
    private void traceSourceOperation(AiResourceImportSource source, String resourceType,
        String operation, String status, Map<String, Object> ext) {
        AiResourceTraceService.log(resourceType, resolveTraceSourceId(source, ext), null,
            operation, status, VisibilityHelper.resolveCurrentIdentity(),
            VisibilityHelper.resolveClientIp(), JacksonUtils.toJson(ext));
    }
    
    private Map<String, Object> searchTraceExt(AiResourceImportSource source,
        AiResourceImportSearchResponse response) {
        Map<String, Object> ext = baseTraceExt(source);
        ext.put("candidate_count", response.getItems() == null ? 0 : response.getItems().size());
        ext.put("has_more", response.isHasMore());
        return ext;
    }
    
    private Map<String, Object> validationTraceExt(AiResourceImportSource source,
        AiResourceImportValidateRequest request, List<AiResourceImportValidationItem> items) {
        Map<String, Object> ext = baseTraceExt(source);
        ext.put("selected_count", request.getSelectedItems().size());
        ext.put("valid_count", countValidationStatus(items,
            AiResourceImportValidationStatus.VALID));
        ext.put("warning_count", countValidationStatus(items,
            AiResourceImportValidationStatus.WARNING));
        ext.put("conflict_count", countValidationStatus(items,
            AiResourceImportValidationStatus.CONFLICT));
        ext.put("invalid_count", countValidationStatus(items,
            AiResourceImportValidationStatus.INVALID));
        ext.put("overwrite_existing", request.isOverwriteExisting());
        return ext;
    }
    
    private Map<String, Object> executeTraceExt(AiResourceImportSource source,
        AiResourceImportExecuteRequest request, AiResourceImportExecuteResponse response) {
        Map<String, Object> ext = baseTraceExt(source);
        ext.put("selected_count", request.getSelectedItems().size());
        ext.put("success_count", response.getSuccessCount());
        ext.put("failed_count", response.getFailedCount());
        ext.put("skipped_count", response.getSkippedCount());
        ext.put("overwrite_existing", request.isOverwriteExisting());
        ext.put("skip_invalid", request.isSkipInvalid());
        return ext;
    }
    
    private Map<String, Object> failureTraceExt(AiResourceImportSource source, String sourceId,
        String errorMessage) {
        Map<String, Object> ext = baseTraceExt(source);
        if (source == null && StringUtils.isNotBlank(sourceId)) {
            ext.put("source_id", sourceId);
        }
        ext.put("error", errorMessage);
        return ext;
    }
    
    private Map<String, Object> baseTraceExt(AiResourceImportSource source) {
        Map<String, Object> ext = new LinkedHashMap<>(6);
        if (source == null) {
            return ext;
        }
        ext.put("source_id", source.getSourceId());
        ext.put("importer", source.getPluginName());
        ext.put("endpoint", source.getEndpoint());
        ext.put("resource_types", source.getResourceTypes());
        return ext;
    }
    
    private String validationTraceStatus(List<AiResourceImportValidationItem> items) {
        return countValidationStatus(items, AiResourceImportValidationStatus.INVALID) > 0
            ? AiResourceTraceService.STATUS_FAILURE : AiResourceTraceService.STATUS_SUCCESS;
    }
    
    private int countValidationStatus(List<AiResourceImportValidationItem> items,
        AiResourceImportValidationStatus status) {
        int count = 0;
        for (AiResourceImportValidationItem each : items) {
            if (status == each.getStatus()) {
                count++;
            }
        }
        return count;
    }
    
    private String executeTraceStatus(AiResourceImportExecuteResponse response) {
        if (response.getFailedCount() > 0) {
            return AiResourceTraceService.STATUS_FAILURE;
        }
        if (response.getSuccessCount() == 0 && response.getSkippedCount() > 0) {
            return AiResourceTraceService.STATUS_SKIPPED;
        }
        return AiResourceTraceService.STATUS_SUCCESS;
    }
    
    private String traceStatus(AiResourceImportResultStatus status) {
        if (AiResourceImportResultStatus.FAILED == status) {
            return AiResourceTraceService.STATUS_FAILURE;
        }
        if (AiResourceImportResultStatus.SKIPPED == status) {
            return AiResourceTraceService.STATUS_SKIPPED;
        }
        return AiResourceTraceService.STATUS_SUCCESS;
    }
    
    private String resolveTraceSourceId(AiResourceImportSource source, Map<String, Object> ext) {
        if (source != null && StringUtils.isNotBlank(source.getSourceId())) {
            return source.getSourceId();
        }
        Object sourceId = ext.get("source_id");
        return sourceId == null ? null : sourceId.toString();
    }
    
    private NacosException invalid(String message) {
        return new NacosApiException(NacosException.INVALID_PARAM,
            ErrorCode.PARAMETER_VALIDATE_ERROR, message);
    }
}
