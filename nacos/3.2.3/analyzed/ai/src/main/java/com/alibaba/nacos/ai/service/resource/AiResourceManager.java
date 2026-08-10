/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.service.resource;

import com.alibaba.nacos.ai.constant.AiResourceConstants;
import com.alibaba.nacos.ai.model.AiResource;
import com.alibaba.nacos.ai.model.AiResourceVersion;
import com.alibaba.nacos.ai.pipeline.PublishPipelineExecutor;
import com.alibaba.nacos.ai.pipeline.model.PipelineCallback;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecution;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecutionResult;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecutionStatus;
import com.alibaba.nacos.ai.pipeline.repository.PipelineExecutionRepository;
import com.alibaba.nacos.ai.service.VisibilityHelper;
import com.alibaba.nacos.ai.service.repository.AiResourcePersistService;
import com.alibaba.nacos.ai.service.repository.AiResourceVersionPersistService;
import com.alibaba.nacos.ai.service.repository.QueryCondition;
import com.alibaba.nacos.ai.service.trace.AiResourceTraceService;
import com.alibaba.nacos.ai.service.visibility.DefaultVisibilityAdvisorConverter;
import com.alibaba.nacos.ai.service.visibility.VisibilityAdvisorConverter;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.VersionUtils;
import com.alibaba.nacos.plugin.ai.pipeline.model.ResourceFilesPipelineContext;
import com.alibaba.nacos.plugin.visibility.constant.VisibilityConstants;
import com.alibaba.nacos.plugin.visibility.model.BaseVisibilityPredicate;
import com.alibaba.nacos.plugin.visibility.model.VisibilityQueryContext;
import com.alibaba.nacos.plugin.visibility.spi.QueryAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Shared manager for common AI resource operations (Skill, AgentSpec, etc.).
 *
 * <p>Centralises duplicated CAS update, query, validation, version-resolution and
 * pipeline-callback logic that was previously copy-pasted across
 * {@code SkillOperationServiceImpl} and {@code AgentSpecOperationServiceImpl}.</p>
 * <p>AI 资源通用管理器：集中 Skill/AgentSpec 等共用的 CAS 更新、查询校验、版本解析与流水线回调逻辑。</p>
 *
 * @author nacos
 */
@Service
public class AiResourceManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AiResourceManager.class);
    
    private final AiResourcePersistService aiResourcePersistService;
    
    private final AiResourceVersionPersistService aiResourceVersionPersistService;
    
    private final PipelineExecutionRepository pipelineExecutionRepository;
    
    private final VisibilityAdvisorConverter visibilityAdvisorConverter;
    
    public AiResourceManager(AiResourcePersistService aiResourcePersistService,
        AiResourceVersionPersistService aiResourceVersionPersistService,
        PipelineExecutionRepository pipelineExecutionRepository) {
        this.aiResourcePersistService = aiResourcePersistService;
        this.aiResourceVersionPersistService = aiResourceVersionPersistService;
        this.pipelineExecutionRepository = pipelineExecutionRepository;
        this.visibilityAdvisorConverter = new DefaultVisibilityAdvisorConverter();
    }
    
    // ---- 2.1 CAS 更新方法 ----
    
    /** CAS 重试循环的执行结果。 */
    /** Result of a CAS update loop. */
    enum CasResult {
        /** CAS 更新成功。 */
        /** CAS succeeded. */
        SUCCESS,
        /** meta 行在重试中消失或 metaVersion 丢失。 */
        /** Meta row disappeared or lost its metaVersion during retry. */
        META_LOST,
        /** 已达最大重试次数。 */
        /** All retry attempts exhausted. */
        MAX_RETRIES
    }
    
    /**
     * Generic CAS retry loop.  On conflict the {@code onConflictRefresh} callback is invoked to
     * <p>通用 CAS 重试：冲突时通过回调刷新非目标字段，目标字段保持不变。</p>
     * refresh non-target fields from the latest meta row; target fields (the ones being updated)
     * stay unchanged.
     *
     * @param namespaceId       namespace
     * @param name              resource name
     * @param type              resource type
     * @param initialExpected   initial expected metaVersion
     * @param newValue          the mutable value carrier whose fields are written on each attempt
     * @param onConflictRefresh (newValue, latestMeta) → refresh non-target fields
     * @return the outcome of the loop
     */
    CasResult doCasLoop(String namespaceId, String name, String type, long initialExpected,
        AiResource newValue,
        BiConsumer<AiResource, AiResource> onConflictRefresh) {
        long expected = initialExpected;
        for (int i = 0; i < AiResourceConstants.MAX_WORKING_VERSION_RETRY; i++) {
            if (aiResourcePersistService.updateMetaCas(namespaceId, name, type, expected,
                newValue)) {
                return CasResult.SUCCESS;
            }
            AiResource latest = aiResourcePersistService.find(namespaceId, name, type);
            if (latest == null || latest.getMetaVersion() == null) {
                return CasResult.META_LOST;
            }
            expected = latest.getMetaVersion();
            onConflictRefresh.accept(newValue, latest);
        }
        return CasResult.MAX_RETRIES;
    }
    
    /**
     * Translate a non-SUCCESS CasResult into the appropriate exception for strict callers.
     * <p>将非 SUCCESS 的 CAS 结果转为严格调用方所需的异常。</p>
     */
    private void handleStrictCasResult(CasResult result) throws NacosException {
        if (result == CasResult.META_LOST) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR,
                "Meta cas failed");
        }
        if (result == CasResult.MAX_RETRIES) {
            throw new NacosApiException(NacosException.CONFLICT, ErrorCode.RESOURCE_CONFLICT,
                "Meta update conflict, retry");
        }
    }
    
    /**
     * CAS-update the versionInfo field of a resource meta row.
     * <p>CAS 更新 meta 行的 versionInfo 字段。</p>
     */
    public void updateVersionInfoCas(String namespaceId, AiResource meta, ResourceVersionInfo info)
        throws NacosException {
        if (meta == null || meta.getMetaVersion() == null) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR,
                "Meta version missing");
        }
        AiResource newValue = new AiResource();
        newValue.setStatus(meta.getStatus());
        newValue.setDesc(meta.getDesc());
        newValue.setBizTags(meta.getBizTags());
        newValue.setExt(meta.getExt());
        newValue.setVersionInfo(JacksonUtils.toJson(info));
        CasResult result =
            doCasLoop(namespaceId, meta.getName(), meta.getType(), meta.getMetaVersion(), newValue,
                (nv, latest) -> {
                    nv.setStatus(latest.getStatus());
                    nv.setDesc(latest.getDesc());
                    nv.setBizTags(latest.getBizTags());
                    nv.setExt(latest.getExt());
                });
        handleStrictCasResult(result);
    }
    
    /**
     * CAS-update the bizTags field of a resource meta row.
     * <p>CAS 更新 meta 行的 bizTags 字段。</p>
     */
    public void updateBizTagsCas(String namespaceId, AiResource meta, String bizTags)
        throws NacosException {
        if (meta == null || meta.getMetaVersion() == null) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR,
                "Meta version missing");
        }
        AiResource newValue = new AiResource();
        newValue.setStatus(meta.getStatus());
        newValue.setDesc(meta.getDesc());
        newValue.setBizTags(bizTags);
        newValue.setExt(meta.getExt());
        newValue.setVersionInfo(meta.getVersionInfo());
        CasResult result =
            doCasLoop(namespaceId, meta.getName(), meta.getType(), meta.getMetaVersion(), newValue,
                (nv, latest) -> {
                    nv.setStatus(latest.getStatus());
                    nv.setDesc(latest.getDesc());
                    nv.setExt(latest.getExt());
                    nv.setVersionInfo(latest.getVersionInfo());
                });
        handleStrictCasResult(result);
    }
    
    /**
     * CAS-update the meta status to enable or disable.
     * <p>CAS 更新 meta 启停状态并记录审计日志。</p>
     */
    public void metaEnableDisable(String namespaceId, AiResource meta, boolean enable)
        throws NacosException {
        ResourceVersionInfo info = requireVersionInfo(meta);
        AiResource newValue = new AiResource();
        newValue.setStatus(enable ? AiResourceConstants.META_STATUS_ENABLE
            : AiResourceConstants.META_STATUS_DISABLE);
        newValue.setDesc(meta.getDesc());
        newValue.setBizTags(meta.getBizTags());
        newValue.setExt(meta.getExt());
        newValue.setVersionInfo(JacksonUtils.toJson(info));
        long expected = meta.getMetaVersion() == null ? 0 : meta.getMetaVersion();
        CasResult result =
            doCasLoop(namespaceId, meta.getName(), meta.getType(), expected, newValue,
                (nv, latest) -> {
                    nv.setDesc(latest.getDesc());
                    nv.setBizTags(latest.getBizTags());
                    nv.setExt(latest.getExt());
                });
        handleStrictCasResult(result);
        String operation =
            enable ? AiResourceTraceService.OP_ENABLE : AiResourceTraceService.OP_DISABLE;
        AiResourceTraceService.logSuccess(meta.getType(), meta.getName(), null, operation,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    /**
     * Best-effort CAS-update the description field of a resource meta row.
     * <p>尽力更新 meta 描述字段，失败不抛异常。</p>
     */
    public void bumpMetaDescription(String namespaceId, AiResource meta, String description) {
        if (meta == null || meta.getMetaVersion() == null) {
            return;
        }
        AiResource newValue = new AiResource();
        newValue.setStatus(meta.getStatus());
        newValue.setDesc(description);
        newValue.setBizTags(meta.getBizTags());
        newValue.setExt(meta.getExt());
        newValue.setVersionInfo(meta.getVersionInfo());
        doCasLoop(namespaceId, meta.getName(), meta.getType(), meta.getMetaVersion(), newValue,
            (nv, latest) -> {
                nv.setStatus(latest.getStatus());
                nv.setBizTags(latest.getBizTags());
                nv.setExt(latest.getExt());
                nv.setVersionInfo(latest.getVersionInfo());
            });
    }
    
    /**
     * Best-effort CAS-update both description and bizTags for an imported resource meta.
     * <p>导入后同步 meta 描述与 bizTags，空值保留原值。</p>
     */
    public void syncImportedMeta(String namespaceId, AiResource meta, String description,
        String bizTags) {
        if (meta == null || meta.getMetaVersion() == null) {
            return;
        }
        String resolvedDescription =
            StringUtils.isBlank(description) ? meta.getDesc() : description;
        String resolvedBizTags = StringUtils.isBlank(bizTags) ? meta.getBizTags() : bizTags;
        AiResource newValue = new AiResource();
        newValue.setStatus(meta.getStatus());
        newValue.setDesc(resolvedDescription);
        newValue.setBizTags(resolvedBizTags);
        newValue.setExt(meta.getExt());
        newValue.setVersionInfo(meta.getVersionInfo());
        doCasLoop(namespaceId, meta.getName(), meta.getType(), meta.getMetaVersion(), newValue,
            (nv, latest) -> {
                nv.setStatus(latest.getStatus());
                nv.setExt(latest.getExt());
                nv.setVersionInfo(latest.getVersionInfo());
            });
    }
    
    /**
     * Best-effort CAS-update source field for an imported resource meta.
     * <p>导入后尽力 CAS 更新 source 字段。</p>
     */
    public void syncImportedSource(String namespaceId, AiResource meta, String source) {
        if (meta == null || meta.getMetaVersion() == null || StringUtils.isBlank(source)) {
            return;
        }
        long expected = meta.getMetaVersion();
        for (int i = 0; i < AiResourceConstants.MAX_WORKING_VERSION_RETRY; i++) {
            if (aiResourcePersistService.updateSourceCas(namespaceId, meta.getName(),
                meta.getType(), expected, source)) {
                return;
            }
            AiResource latest = aiResourcePersistService.find(namespaceId, meta.getName(),
                meta.getType());
            if (latest == null || latest.getMetaVersion() == null) {
                return;
            }
            expected = latest.getMetaVersion();
        }
    }
    
    // ---- 2.2 查询与校验辅助 ----
    
    /**
     * Load meta row or throw NOT_FOUND.
     * <p>加载 meta 行，不存在时抛 NOT_FOUND。</p>
     */
    public AiResource requireMeta(String namespaceId, String name, String type)
        throws NacosException {
        AiResource meta = aiResourcePersistService.find(namespaceId, name, type);
        if (meta == null) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                type + " not found: " + name);
        }
        return meta;
    }
    
    /**
     * Find meta row by namespace/name/type.
     * <p>按命名空间/名称/类型查询 meta 行。</p>
     */
    public AiResource findMeta(String namespaceId, String name, String type) {
        return aiResourcePersistService.find(namespaceId, name, type);
    }
    
    /**
     * Find version row by namespace/name/type/version.
     * <p>按四元组查询版本行。</p>
     */
    public AiResourceVersion findVersion(String namespaceId, String name, String type,
        String version) {
        return aiResourceVersionPersistService.find(namespaceId, name, type, version);
    }
    
    /**
     * Update version row storage/description.
     * <p>更新版本行 storage 与描述。</p>
     */
    public void updateVersionStorageAndDesc(String namespaceId, String name, String type,
        String version,
        String storageJson, String description) {
        aiResourceVersionPersistService.updateStorageAndDesc(namespaceId, name, type, version,
            storageJson, description);
    }
    
    /**
     * Update version row storage.
     * <p>更新版本行 storage。</p>
     */
    public void updateVersionStorage(String namespaceId, String name, String type, String version,
        String storageJson) {
        aiResourceVersionPersistService.updateStorage(namespaceId, name, type, version,
            storageJson);
    }
    
    /**
     * Update version row status.
     * <p>更新版本行状态。</p>
     */
    public void updateVersionStatus(String namespaceId, String name, String type, String version,
        String status) {
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version, status);
    }
    
    /**
     * Update version row publish pipeline info.
     * <p>更新版本行发布流水线信息。</p>
     */
    public void updateVersionPublishPipelineInfo(String namespaceId, String name, String type,
        String version,
        String publishPipelineInfo) {
        aiResourceVersionPersistService.updatePublishPipelineInfo(namespaceId, name, type, version,
            publishPipelineInfo);
    }
    
    /**
     * Delete one version row.
     * <p>删除单个版本行。</p>
     */
    public void deleteVersion(String namespaceId, String name, String type, String version) {
        aiResourceVersionPersistService.delete(namespaceId, name, type, version);
    }
    
    /**
     * Delete all version rows for a resource.
     * <p>删除资源下全部版本行。</p>
     */
    public void deleteVersionsByNameAndType(String namespaceId, String name, String type) {
        aiResourceVersionPersistService.deleteByNameAndType(namespaceId, name, type);
    }
    
    /**
     * Delete meta row by namespace/name/type.
     * <p>按三元组删除 meta 行。</p>
     */
    public void deleteMeta(String namespaceId, String name, String type) {
        aiResourcePersistService.delete(namespaceId, name, type);
    }
    
    /**
     * Generate LIKE argument.
     * <p>生成 SQL LIKE 参数。</p>
     */
    public String generateLikeArgument(String value) {
        return aiResourcePersistService.generateLikeArgument(value);
    }
    
    /**
     * List meta rows by basic prompt-style filtering.
     * <p>按基础过滤条件分页列出 meta 行。</p>
     */
    public Page<AiResource> listMetaByType(String namespaceId, String type, String nameLike,
        String bizTagsLike,
        int pageNo, int pageSize) {
        return aiResourcePersistService.list(namespaceId, type, nameLike, bizTagsLike, pageNo,
            pageSize);
    }
    
    /**
     * List meta rows by query condition.
     * <p>按 QueryCondition 分页列出 meta 行。</p>
     */
    public Page<AiResource> listMeta(QueryCondition queryCondition, int pageNo, int pageSize) {
        return aiResourcePersistService.list(queryCondition, pageNo, pageSize);
    }
    
    /**
     * List version rows.
     * <p>分页列出版本行。</p>
     */
    public Page<AiResourceVersion> listVersions(String namespaceId, String name, String type,
        String status,
        int pageNo, int pageSize) {
        return aiResourceVersionPersistService.list(namespaceId, name, type, status, pageNo,
            pageSize);
    }
    
    /**
     * Parse and guarantee a non-null {@link ResourceVersionInfo} from the meta row.
     * <p>解析 meta 的 versionInfo JSON，保证返回非空且 labels 已初始化。</p>
     */
    public static ResourceVersionInfo requireVersionInfo(AiResource meta) {
        ResourceVersionInfo info = parseVersionInfo(meta == null ? null : meta.getVersionInfo());
        if (info == null) {
            info = new ResourceVersionInfo();
            info.setLabels(new HashMap<>(4));
        } else if (info.getLabels() == null) {
            info.setLabels(new HashMap<>(4));
        }
        return info;
    }
    
    /**
     * Deserialise version info JSON; returns {@code null} on blank/invalid input.
     * <p>反序列化 versionInfo JSON；空白或非法输入返回 null。</p>
     */
    public static ResourceVersionInfo parseVersionInfo(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JacksonUtils.toObj(json, ResourceVersionInfo.class);
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Deserialise publish pipeline info JSON; returns {@code null} on blank/invalid input.
     * <p>反序列化发布流水线 JSON；executionId 缺失时返回 null。</p>
     */
    public static PublishPipelineInfo parsePublishPipelineInfo(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            PublishPipelineInfo info = JacksonUtils.toObj(json, PublishPipelineInfo.class);
            if (info == null || StringUtils.isBlank(info.getExecutionId())) {
                return null;
            }
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Throw NOT_FOUND if the current user cannot read the given resource.
     * <p>当前用户不可读时以 NOT_FOUND 隐藏资源存在性。</p>
     */
    public void ensureReadableOrNotFound(AiResource resource, String notFoundMessage)
        throws NacosException {
        if (VisibilityHelper.canReadResource(resource)) {
            return;
        }
        throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
            notFoundMessage);
    }
    
    /**
     * Build a {@link QueryCondition} with visibility filtering applied.
     * <p>构建带可见性过滤的 {@link QueryCondition}。</p>
     */
    public QueryCondition buildQueryCondition(String namespaceId, String resourceType,
        String nameLike,
        String bizTagsLike, String action) {
        String identity = VisibilityHelper.resolveCurrentIdentity();
        String apiType = VisibilityHelper.resolveCurrentApiType();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setNamespaceId(namespaceId);
        queryCondition.setType(resourceType);
        queryCondition.setNameLike(nameLike);
        queryCondition.setBizTagsLike(bizTagsLike);
        VisibilityQueryContext context = new VisibilityQueryContext();
        context.setNamespaceId(namespaceId);
        context.setResourceType(resourceType);
        QueryAdvisor advisor = VisibilityHelper.findVisibilityService()
            .map(service -> service.adviseQuery(identity, action, apiType, context))
            .orElseGet(() -> {
                QueryAdvisor queryAdvisor = new QueryAdvisor();
                queryAdvisor.setBasePredicate(BaseVisibilityPredicate.ALL);
                return queryAdvisor;
            });
        return visibilityAdvisorConverter.convert(queryCondition, identity, advisor, context);
    }
    
    /**
     * Create an empty page result.
     * <p>构造空分页结果。</p>
     */
    public static <T> Page<T> buildEmptyPage(int pageNo) {
        Page<T> page = new Page<>();
        page.setPageItems(new ArrayList<>());
        page.setTotalCount(0);
        page.setPagesAvailable(0);
        page.setPageNumber(pageNo);
        return page;
    }
    
    /**
     * Resolve scope from meta, defaulting to PRIVATE when blank.
     * <p>解析 meta scope，空白时默认 PRIVATE。</p>
     */
    public static String resolveScope(AiResource meta) {
        if (meta == null || StringUtils.isBlank(meta.getScope())) {
            return VisibilityConstants.SCOPE_PRIVATE;
        }
        return meta.getScope();
    }
    
    // ---- 2.3 版本解析 ----
    
    /**
     * Resolve which version string to use given explicit version, label, and meta state.
     * <p>解析目标版本：label &gt; explicitVersion &gt; latest 标签。</p>
     */
    public static String resolveVersion(AiResource meta, String explicitVersion, String label) {
        if (StringUtils.isNotBlank(label)) {
            ResourceVersionInfo info = parseVersionInfo(meta.getVersionInfo());
            if (info != null && info.getLabels() != null) {
                String v = info.getLabels().get(label);
                if (StringUtils.isNotBlank(v)) {
                    return v;
                }
            }
        }
        if (StringUtils.isNotBlank(explicitVersion)) {
            return explicitVersion;
        }
        ResourceVersionInfo info = parseVersionInfo(meta.getVersionInfo());
        if (info != null && info.getLabels() != null) {
            String v = info.getLabels().get(AiResourceConstants.LABEL_LATEST);
            if (StringUtils.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }
    
    // ---- 2.4 流水线回调 ----
    
    /**
     * List all existing version strings for a given resource (name + type).
     * <p>列出资源已有全部版本号。</p>
     */
    public List<String> listExistingVersions(String namespaceId, String name, String type) {
        Page<AiResourceVersion> page =
            aiResourceVersionPersistService.list(namespaceId, name, type, null, 1, 500);
        if (page == null || page.getPageItems() == null || page.getPageItems().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> versions = new ArrayList<>(page.getPageItems().size());
        for (AiResourceVersion v : page.getPageItems()) {
            if (v != null && StringUtils.isNotBlank(v.getVersion())) {
                versions.add(v.getVersion().trim());
            }
        }
        return versions;
    }
    
    /**
     * Construct and insert a version row.
     * <p>构造并插入版本行。</p>
     */
    public void insertVersionRow(String namespaceId, String name, String type, String author,
        String status,
        String version, String description, String storageJson) {
        // 版本行已存在（如删除失败残留或并发插入）则改为更新而非重复插入
        AiResourceVersion existing =
            aiResourceVersionPersistService.find(namespaceId, name, type, version);
        if (existing != null) {
            updateExistingVersionRow(namespaceId, name, type, version, status, description,
                storageJson);
            return;
        }
        AiResourceVersion row = new AiResourceVersion();
        row.setNamespaceId(namespaceId);
        row.setName(name);
        row.setType(type);
        row.setAuthor(author);
        row.setStatus(status);
        row.setVersion(version);
        row.setDesc(description);
        row.setStorage(storageJson);
        try {
            aiResourceVersionPersistService.insert(row);
        } catch (DuplicateKeyException e) {
            // 并发竞态：检查后已被插入，回退为更新
            LOGGER.warn("[insertVersionRow] duplicate key for {}/{}/{}/{}, falling back to update",
                namespaceId, name, type, version);
            updateExistingVersionRow(namespaceId, name, type, version, status, description,
                storageJson);
        }
    }
    
    private void updateExistingVersionRow(String namespaceId, String name, String type,
        String version,
        String status, String description, String storageJson) {
        aiResourceVersionPersistService.updateStorageAndDesc(namespaceId, name, type, version,
            storageJson, description);
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version, status);
    }
    
    /**
     * Find a version row and verify it is in draft status.
     * <p>查找版本行并校验其为 draft 状态。</p>
     *
     * @throws NacosApiException if version not found or not in draft status
     */
    public AiResourceVersion requireDraftVersion(String namespaceId, String name, String type,
        String version)
        throws NacosException {
        AiResourceVersion v =
            aiResourceVersionPersistService.find(namespaceId, name, type, version);
        if (v == null
            || !AiResourceConstants.VERSION_STATUS_DRAFT.equalsIgnoreCase(v.getStatus())) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Current editing version is not draft: " + version);
        }
        return v;
    }
    
    /**
     * Publish a version directly (bypass pipeline). Sets version online, clears editing/reviewing pointers,
     * <p>直接发布（跳过流水线）：置 online、清理 editing/reviewing 指针并递增 onlineCnt。</p>
     * increments onlineCnt, and optionally updates the latest label.
     */
    public void directPublishVersion(String namespaceId, AiResource meta, ResourceVersionInfo info,
        String version, boolean updateLatestLabel) throws NacosException {
        String name = meta.getName();
        String type = meta.getType();
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
            AiResourceConstants.VERSION_STATUS_ONLINE);
        if (StringUtils.equals(info.getEditingVersion(), version)) {
            info.setEditingVersion(null);
        }
        if (StringUtils.equals(info.getReviewingVersion(), version)) {
            info.setReviewingVersion(null);
        }
        Integer cnt = info.getOnlineCnt();
        info.setOnlineCnt(cnt == null ? 1 : (cnt + 1));
        if (info.getLabels() == null) {
            info.setLabels(new HashMap<>(4));
        }
        if (updateLatestLabel) {
            info.getLabels().put(AiResourceConstants.LABEL_LATEST, version);
        }
        updateVersionInfoCas(namespaceId, meta, info);
    }
    
    /**
     * Create both an online version row and a meta row for bootstrap (built-in) resources.
     * <p>引导内置资源：同时插入 online 版本行与 meta 行。</p>
     */
    public void insertBootstrapMeta(String namespaceId, String name, String type,
        String description,
        String bizTags, String owner, String from, String version, String storageJson) {
        insertVersionRow(namespaceId, name, type, owner, AiResourceConstants.VERSION_STATUS_ONLINE,
            version, description, storageJson);
        
        ResourceVersionInfo versionInfo = new ResourceVersionInfo();
        versionInfo.setOnlineCnt(1);
        Map<String, String> labels = new HashMap<>(4);
        labels.put(AiResourceConstants.LABEL_LATEST, version);
        versionInfo.setLabels(labels);
        
        AiResource meta = new AiResource();
        meta.setNamespaceId(namespaceId);
        meta.setName(name);
        meta.setType(type);
        meta.setStatus(AiResourceConstants.META_STATUS_ENABLE);
        meta.setDesc(description);
        meta.setBizTags(bizTags);
        meta.setOwner(owner);
        meta.setFrom(from);
        meta.setScope(VisibilityConstants.SCOPE_PUBLIC);
        meta.setVersionInfo(JacksonUtils.toJson(versionInfo));
        meta.setMetaVersion(1L);
        aiResourcePersistService.insert(meta);
    }
    
    /**
     * Resolve the target version for a submit operation (explicit version or current editing).
     * <p>解析提交目标版本：显式 version 或当前 editingVersion。</p>
     *
     * @throws NacosApiException if no target version can be determined
     */
    public String resolveSubmitTarget(ResourceVersionInfo info, String version, String type,
        String name)
        throws NacosException {
        String target = version;
        if (StringUtils.isBlank(target)) {
            target = info.getEditingVersion();
        }
        if (StringUtils.isBlank(target)) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                "No draft version to submit for " + type + ": " + name);
        }
        return target;
    }
    
    /**
     * Transition a version to reviewing status and update meta pointers accordingly.
     * <p>将 draft 版本提交审核，更新 meta 指针并写审计日志。</p>
     *
     * <p>Only versions in {@code draft} status are allowed to enter the review stage.
     * Submitting a version in any other status (reviewing / reviewed / online / offline)
     * is rejected with {@code INVALID_PARAM} to prevent corrupting formal versions.</p>
     */
    public void moveToReviewing(String namespaceId, String name, String type, String version,
        AiResource meta, ResourceVersionInfo info) throws NacosException {
        // 守卫：仅 draft 状态可提交审核
        requireDraftVersion(namespaceId, name, type, version);
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
            AiResourceConstants.VERSION_STATUS_REVIEWING);
        info.setEditingVersion(null);
        info.setReviewingVersion(version);
        updateVersionInfoCas(namespaceId, meta, info);
        AiResourceTraceService.logSuccess(type, name, version,
            AiResourceTraceService.OP_SUBMIT_REVIEW,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    /**
     * Write an IN_PROGRESS pipeline info record for a version.
     * <p>写入 IN_PROGRESS 流水线信息，供客户端查询 executionId。</p>
     */
    public void writePipelineInfoInProgress(String namespaceId, String name, String type,
        String version,
        String executionId) {
        PublishPipelineInfo pipelineInfo = new PublishPipelineInfo();
        pipelineInfo.setExecutionId(executionId);
        pipelineInfo.setStatus(PipelineExecutionStatus.IN_PROGRESS);
        pipelineInfo.setPipeline(new ArrayList<>());
        aiResourceVersionPersistService.updatePublishPipelineInfo(namespaceId, name, type, version,
            JacksonUtils.toJson(pipelineInfo));
    }
    
    /**
     * Clear pipeline info for a version (edge case when pipeline becomes unavailable).
     * <p>清除版本流水线信息（流水线不可用时的边界处理）。</p>
     */
    public void clearPipelineInfo(String namespaceId, String name, String type, String version) {
        aiResourceVersionPersistService.updatePublishPipelineInfo(namespaceId, name, type, version,
            null);
    }
    
    // ---- 2.5 高层领域无关操作 ----
    
    /**
     * Core publish logic: validate pipeline result, set version online, update meta pointers.
     * <p>核心发布逻辑：校验流水线、置 online、更新 meta 指针。</p>
     *
     * @return the version row (caller may need it for post-processing, e.g. manifest sync)
     */
    public AiResourceVersion doPublish(String namespaceId, String name, String type, String version,
        boolean updateLatestLabel) throws NacosException {
        return doPublish(namespaceId, name, type, version, updateLatestLabel, true,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    private AiResourceVersion doPublish(String namespaceId, String name, String type,
        String version,
        boolean updateLatestLabel, boolean checkVisibility, String operator, String clientIp)
        throws NacosException {
        AiResource meta = requireMeta(namespaceId, name, type);
        if (checkVisibility) {
            VisibilityHelper.checkWritableResource(meta);
        }
        ResourceVersionInfo info = requireVersionInfo(meta);
        
        AiResourceVersion v =
            aiResourceVersionPersistService.find(namespaceId, name, type, version);
        if (v == null) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                type + " version not found: " + name + "@" + version);
        }
        if (!AiResourceConstants.VERSION_STATUS_REVIEWING.equalsIgnoreCase(v.getStatus())
            && !AiResourceConstants.VERSION_STATUS_REVIEWED.equalsIgnoreCase(v.getStatus())
            && !AiResourceConstants.VERSION_STATUS_ONLINE.equalsIgnoreCase(v.getStatus())) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Only reviewing version can be published: " + version);
        }
        
        PublishPipelineInfo pipelineInfo = parsePublishPipelineInfo(v.getPublishPipelineInfo());
        if (pipelineInfo != null && StringUtils.isNotBlank(pipelineInfo.getExecutionId())) {
            PipelineExecution execution =
                pipelineExecutionRepository.findById(pipelineInfo.getExecutionId());
            if (execution == null) {
                throw new NacosApiException(NacosException.INVALID_PARAM,
                    ErrorCode.PARAMETER_VALIDATE_ERROR,
                    "Pipeline execution not found, cannot publish: " + version);
            }
            if (execution.getStatus() != PipelineExecutionStatus.APPROVED) {
                throw new NacosApiException(NacosException.INVALID_PARAM,
                    ErrorCode.PARAMETER_VALIDATE_ERROR,
                    "Pipeline not approved, cannot publish: " + version);
            }
        }
        
        boolean alreadyOnline =
            AiResourceConstants.VERSION_STATUS_ONLINE.equalsIgnoreCase(v.getStatus());
        if (!alreadyOnline) {
            aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
                AiResourceConstants.VERSION_STATUS_ONLINE);
        }
        if (StringUtils.equals(info.getReviewingVersion(), version)) {
            info.setReviewingVersion(null);
        }
        if (!alreadyOnline) {
            Integer cnt = info.getOnlineCnt();
            info.setOnlineCnt(cnt == null ? 1 : (cnt + 1));
        }
        if (info.getLabels() == null) {
            info.setLabels(new HashMap<>(4));
        }
        if (updateLatestLabel) {
            info.getLabels().put(AiResourceConstants.LABEL_LATEST, version);
        }
        updateVersionInfoCas(namespaceId, meta, info);
        AiResourceTraceService.logSuccess(type, name, version, AiResourceTraceService.OP_PUBLISH,
            operator, clientIp);
        return v;
    }
    
    /**
     * Core publish logic for system-triggered pipeline callbacks.
     * <p>系统触发的流水线回调发布，跳过可见性校验。</p>
     *
     * @return the version row (caller may need it for post-processing, e.g. manifest sync)
     */
    public AiResourceVersion doSystemPublish(String namespaceId, String name, String type,
        String version,
        boolean updateLatestLabel) throws NacosException {
        return doPublish(namespaceId, name, type, version, updateLatestLabel, false, "system", "");
    }
    
    /**
     * Core force-publish logic: bypass pipeline validation, set version online, update meta pointers.
     * <p>强制发布：跳过流水线校验，直接置 online。</p>
     *
     * @return the version row (caller may need it for post-processing, e.g. manifest sync)
     */
    public AiResourceVersion doForcePublish(String namespaceId, String name, String type,
        String version,
        boolean updateLatestLabel) throws NacosException {
        AiResource meta = requireMeta(namespaceId, name, type);
        VisibilityHelper.checkWritableResource(meta);
        ResourceVersionInfo info = requireVersionInfo(meta);
        
        AiResourceVersion v =
            aiResourceVersionPersistService.find(namespaceId, name, type, version);
        if (v == null) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                type + " version not found: " + name + "@" + version);
        }
        if (AiResourceConstants.VERSION_STATUS_ONLINE.equalsIgnoreCase(v.getStatus())
            || AiResourceConstants.VERSION_STATUS_OFFLINE.equalsIgnoreCase(v.getStatus())) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Force-publish is not allowed for online or offline version: " + version);
        }
        
        LOGGER.warn("[FORCE-PUBLISH] Bypassing pipeline validation for {} {}@{} by user {}",
            type, name, version, VisibilityHelper.resolveCurrentIdentity());
        
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
            AiResourceConstants.VERSION_STATUS_ONLINE);
        if (StringUtils.equals(info.getEditingVersion(), version)) {
            info.setEditingVersion(null);
        }
        if (StringUtils.equals(info.getReviewingVersion(), version)) {
            info.setReviewingVersion(null);
        }
        Integer cnt = info.getOnlineCnt();
        info.setOnlineCnt(cnt == null ? 1 : (cnt + 1));
        if (info.getLabels() == null) {
            info.setLabels(new HashMap<>(4));
        }
        if (updateLatestLabel) {
            info.getLabels().put(AiResourceConstants.LABEL_LATEST, version);
        }
        updateVersionInfoCas(namespaceId, meta, info);
        AiResourceTraceService.logSuccess(type, name, version,
            AiResourceTraceService.OP_FORCE_PUBLISH,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
        return v;
    }
    
    /**
     * Validate that labels don't reference draft/reviewing versions, then CAS-update labels.
     * <p>校验 label 不指向 draft/reviewing 版本后 CAS 更新 labels。</p>
     */
    public void validateAndUpdateLabels(String namespaceId, String name, String type,
        Map<String, String> labels) throws NacosException {
        AiResource meta = requireMeta(namespaceId, name, type);
        VisibilityHelper.checkWritableResource(meta);
        ResourceVersionInfo info = requireVersionInfo(meta);
        if (labels != null) {
            String editing = info.getEditingVersion();
            String reviewing = info.getReviewingVersion();
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                String targetVersion = entry.getValue();
                if (StringUtils.isNotBlank(editing) && editing.equals(targetVersion)) {
                    throw new NacosApiException(NacosException.INVALID_PARAM,
                        ErrorCode.PARAMETER_VALIDATE_ERROR,
                        "Label '" + entry.getKey() + "' cannot point to draft version: "
                            + targetVersion);
                }
                if (StringUtils.isNotBlank(reviewing) && reviewing.equals(targetVersion)) {
                    throw new NacosApiException(NacosException.INVALID_PARAM,
                        ErrorCode.PARAMETER_VALIDATE_ERROR,
                        "Label '" + entry.getKey() + "' cannot point to reviewing version: "
                            + targetVersion);
                }
            }
        }
        info.setLabels(labels == null ? null : new LinkedHashMap<>(labels));
        updateVersionInfoCas(namespaceId, meta, info);
        AiResourceTraceService.logSuccess(type, name, null, AiResourceTraceService.OP_UPDATE_LABELS,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    /**
     * Update the scope of a resource (requireMeta + checkWritable + persist).
     * <p>更新资源可见性 scope（需可读 meta 且可写）。</p>
     */
    public void doUpdateScope(String namespaceId, String name, String type, String scope)
        throws NacosException {
        AiResource meta = requireMeta(namespaceId, name, type);
        VisibilityHelper.checkWritableResource(meta);
        boolean ok =
            aiResourcePersistService.updateScope(namespaceId, name, type, scope.toUpperCase());
        if (!ok) {
            LOGGER.error("Failed to update scope for {} {}, namespace: {}, scope: {}", type, name,
                namespaceId, scope);
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR,
                "Failed to update scope for " + type + ": " + name);
        }
        AiResourceTraceService.logSuccess(type, name, null, AiResourceTraceService.OP_UPDATE_SCOPE,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp(),
            "scope=" + scope);
    }
    
    /**
     * Toggle a single version's online/offline status and adjust meta onlineCnt.
     * <p>切换单版本 online/offline 并调整 meta onlineCnt。</p>
     *
     * @return the version row if a status change occurred, or {@code null} if already in the target status
     */
    public AiResourceVersion toggleVersionOnlineStatus(String namespaceId, AiResource meta,
        ResourceVersionInfo info, String version, boolean online) throws NacosException {
        String name = meta.getName();
        String type = meta.getType();
        AiResourceVersion v =
            aiResourceVersionPersistService.find(namespaceId, name, type, version);
        if (v == null) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                type + " version not found: " + name + "@" + version);
        }
        String targetStatus = online ? AiResourceConstants.VERSION_STATUS_ONLINE
            : AiResourceConstants.VERSION_STATUS_OFFLINE;
        if (targetStatus.equalsIgnoreCase(v.getStatus())) {
            return null;
        }
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
            targetStatus);
        Integer cnt = info.getOnlineCnt() == null ? 0 : info.getOnlineCnt();
        info.setOnlineCnt(online ? cnt + 1 : Math.max(0, cnt - 1));
        updateVersionInfoCas(namespaceId, meta, info);
        String operation = online ? AiResourceTraceService.OP_ONLINE_VERSION
            : AiResourceTraceService.OP_OFFLINE_VERSION;
        AiResourceTraceService.logSuccess(type, name, version, operation,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
        return v;
    }
    
    /**
     * Create a new meta row (when {@code isNew}) or CAS-update the editing pointer on an existing one.
     * <p>新建 meta 或 CAS 更新已有 meta 的 editing 指针。</p>
     */
    public void initOrUpdateMetaForDraft(String namespaceId, String name, String type,
        String description,
        String bizTags, String version, AiResource existedMeta, boolean isNew)
        throws NacosException {
        if (isNew) {
            String currentUser = VisibilityHelper.resolveCurrentIdentity();
            String defaultScope = VisibilityHelper.resolveDefaultScopeForCreate(type);
            AiResource meta = new AiResource();
            meta.setNamespaceId(namespaceId);
            meta.setName(name);
            meta.setType(type);
            meta.setStatus(AiResourceConstants.META_STATUS_ENABLE);
            meta.setDesc(description);
            meta.setBizTags(bizTags);
            meta.setOwner(currentUser);
            meta.setScope(defaultScope);
            ResourceVersionInfo info = new ResourceVersionInfo();
            info.setEditingVersion(version);
            info.setOnlineCnt(0);
            info.setLabels(new HashMap<>(4));
            meta.setVersionInfo(JacksonUtils.toJson(info));
            meta.setMetaVersion(1L);
            aiResourcePersistService.insert(meta);
        } else if (existedMeta != null) {
            if (existedMeta.getMetaVersion() == null) {
                throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR,
                    "Meta version missing");
            }
            ResourceVersionInfo info = requireVersionInfo(existedMeta);
            info.setEditingVersion(version);
            boolean syncDescription = StringUtils.isNotBlank(description);
            AiResource newValue = new AiResource();
            newValue.setStatus(existedMeta.getStatus());
            newValue.setDesc(syncDescription ? description : existedMeta.getDesc());
            newValue.setBizTags(existedMeta.getBizTags());
            newValue.setExt(existedMeta.getExt());
            newValue.setVersionInfo(JacksonUtils.toJson(info));
            CasResult result = doCasLoop(namespaceId, existedMeta.getName(), existedMeta.getType(),
                existedMeta.getMetaVersion(), newValue,
                (nv, latest) -> {
                    nv.setStatus(latest.getStatus());
                    if (!syncDescription) {
                        nv.setDesc(latest.getDesc());
                    }
                    nv.setBizTags(latest.getBizTags());
                    nv.setExt(latest.getExt());
                });
            handleStrictCasResult(result);
        }
    }
    
    /**
     * Resolve the base version to copy from when creating a draft.
     * <p>解析创建草稿时的基线版本：显式 basedOn → latest → 最高 semver/vN。</p>
     *
     * <p>Priority: explicit basedOnVersion → "latest" label → highest semver → highest vN.
     * Returns {@code null} if no version exists yet.</p>
     *
     * @throws NacosApiException if an explicit basedOnVersion was given but cannot be resolved
     */
    public String resolveBaseVersion(String namespaceId, String name, String type, AiResource meta,
        String basedOnVersion) throws NacosException {
        if (StringUtils.isNotBlank(basedOnVersion)) {
            String resolved = resolveVersion(meta, basedOnVersion, null);
            if (StringUtils.isBlank(resolved)) {
                throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                    "Base version not found for " + type + ": " + name + ", basedOnVersion: "
                        + basedOnVersion);
            }
            return resolved;
        }
        String latest = resolveVersion(meta, null, AiResourceConstants.LABEL_LATEST);
        if (StringUtils.isNotBlank(latest)) {
            return latest;
        }
        List<String> existingVersions = listExistingVersions(namespaceId, name, type);
        String maxSemver = VersionUtils.maxSemver(existingVersions);
        return StringUtils.isNotBlank(maxSemver) ? maxSemver
            : VersionUtils.maxVNumberVersion(existingVersions);
    }
    
    /**
     * Ensure no editing or reviewing version exists; throw CONFLICT otherwise.
     * <p>确保无 editing/reviewing 版本，否则抛 CONFLICT。</p>
     *
     * @param info   the parsed version info
     * @param action action description for error message (e.g. "upload", "create draft")
     */
    public static void ensureNoWorkingVersion(ResourceVersionInfo info, String action)
        throws NacosException {
        if (StringUtils.isNotBlank(info.getEditingVersion())
            || StringUtils.isNotBlank(info.getReviewingVersion())) {
            throw new NacosApiException(NacosException.CONFLICT, ErrorCode.RESOURCE_CONFLICT,
                "There is already a working version (editing/reviewing), cannot " + action);
        }
    }
    
    /**
     * Build a page result from items and the source meta page.
     * <p>由条目列表与源分页构造结果页。</p>
     */
    public static <T> Page<T> buildPageResult(List<T> items, Page<?> sourcePage, int pageNo) {
        Page<T> result = new Page<>();
        result.setPageItems(items);
        result.setTotalCount(sourcePage == null ? 0 : sourcePage.getTotalCount());
        result.setPagesAvailable(sourcePage == null ? 0 : sourcePage.getPagesAvailable());
        result.setPageNumber(pageNo);
        return result;
    }
    
    /**
     * Functional interface for deleting storage associated with a specific version.
     * <p>删除指定版本关联存储的函数式接口。</p>
     */
    @FunctionalInterface
    public interface VersionStorageDeleter {
        
        void deleteStorage(AiResourceVersion version) throws NacosException;
    }
    
    /**
     * Delete meta and all version rows for a resource, invoking the given deleter for each version's storage.
     * <p>删除 meta 及全部版本行，并对每版本调用 storageDeleter。</p>
     */
    public void deleteResourceWithVersions(String namespaceId, String name, String type,
        VersionStorageDeleter storageDeleter) throws NacosException {
        aiResourcePersistService.delete(namespaceId, name, type);
        Page<AiResourceVersion> versions =
            aiResourceVersionPersistService.list(namespaceId, name, type, null, 1, 200);
        aiResourceVersionPersistService.deleteByNameAndType(namespaceId, name, type);
        if (versions != null && versions.getPageItems() != null) {
            for (AiResourceVersion v : versions.getPageItems()) {
                if (v == null || StringUtils.isBlank(v.getVersion())) {
                    continue;
                }
                storageDeleter.deleteStorage(v);
            }
        }
        AiResourceTraceService.logSuccess(type, name, null,
            AiResourceTraceService.OP_DELETE_RESOURCE,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    /**
     * Execute the publish pipeline for a resource version. Returns {@code true} if the pipeline is processing
     * <p>执行发布流水线；异步处理中返回 true，同步穿透返回 false。</p>
     * asynchronously. Returns {@code false} if the pipeline fell through synchronously (caller should do direct
     * publish).
     */
    public boolean runPipelineExecution(String namespaceId, String name, String type,
        String version,
        ResourceFilesPipelineContext ctx, PublishPipelineExecutor executor) {
        return runPipelineExecution(namespaceId, name, type, version, ctx, executor,
            r -> onPipelineComplete(namespaceId, name, type, version, r));
    }
    
    /**
     * Execute the publish pipeline for a resource version with a caller-provided completion callback.
     * <p>执行发布流水线并传入完成回调。</p>
     *
     * <p>The initial IN_PROGRESS record is still written here so the version can expose a stable executionId
     * before async execution starts.</p>
     */
    public boolean runPipelineExecution(String namespaceId, String name, String type,
        String version,
        ResourceFilesPipelineContext ctx, PublishPipelineExecutor executor,
        PipelineCallback callback) {
        String executionId = UUID.randomUUID().toString();
        writePipelineInfoInProgress(namespaceId, name, type, version, executionId);
        String result = executor.execute(ctx, callback, executionId);
        if (StringUtils.isBlank(result)) {
            clearPipelineInfo(namespaceId, name, type, version);
            return false;
        }
        return true;
    }
    
    /**
     * Transition a reviewed version back to draft for re-editing.
     * <p>将 reviewed 版本退回 draft 以便重新编辑。</p>
     *
     * <p>Only versions in {@code reviewed} status can be re-edited. The version number and content
     * are preserved; only the status changes to {@code draft} and meta pointers are updated.</p>
     *
     * @param namespaceId namespace
     * @param name        resource name
     * @param type        resource type (skill, prompt, agentspec)
     * @param version     version to re-edit
     */
    public void doRedraft(String namespaceId, String name, String type, String version)
        throws NacosException {
        AiResource meta = requireMeta(namespaceId, name, type);
        VisibilityHelper.checkWritableResource(meta);
        ResourceVersionInfo info = requireVersionInfo(meta);
        
        AiResourceVersion v =
            aiResourceVersionPersistService.find(namespaceId, name, type, version);
        if (v == null) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                type + " version not found: " + name + "@" + version);
        }
        if (!AiResourceConstants.VERSION_STATUS_REVIEWED.equalsIgnoreCase(v.getStatus())) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Only reviewed version can be re-edited: " + version);
        }
        
        aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
            AiResourceConstants.VERSION_STATUS_DRAFT);
        
        // 标记流水线信息为历史，避免 redraft 后仍视为当前审核
        if (StringUtils.isNotBlank(v.getPublishPipelineInfo())) {
            try {
                PublishPipelineInfo pipelineInfo =
                    JacksonUtils.toObj(v.getPublishPipelineInfo(), PublishPipelineInfo.class);
                pipelineInfo.setHistorical(true);
                aiResourceVersionPersistService.updatePublishPipelineInfo(namespaceId, name, type,
                    version, JacksonUtils.toJson(pipelineInfo));
            } catch (Exception ex) {
                LOGGER.warn("Failed to mark pipeline info as historical for {}@{}", name, version,
                    ex);
            }
        }
        
        if (StringUtils.equals(info.getReviewingVersion(), version)) {
            info.setReviewingVersion(null);
            info.setEditingVersion(version);
            updateVersionInfoCas(namespaceId, meta, info);
        }
        
        AiResourceTraceService.logSuccess(type, name, version,
            AiResourceTraceService.OP_REDRAFT,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    /**
     * Unified delete-draft logic for all resource types.
     * <p>统一删除草稿逻辑：清理 meta 指针并删除版本行与存储。</p>
     *
     * <p>Resolves the target version from editingVersion (primary) or reviewingVersion (fallback
     * for reviewed/draft status), clears the corresponding meta pointer, deletes the version row,
     * and invokes the storage deleter callback.</p>
     *
     * @param namespaceId    namespace
     * @param name           resource name
     * @param type           resource type
     * @param storageDeleter callback to delete resource-specific storage files
     */
    public void doDeleteDraft(String namespaceId, String name, String type,
        VersionStorageDeleter storageDeleter) throws NacosException {
        AiResource meta = requireMeta(namespaceId, name, type);
        VisibilityHelper.checkWritableResource(meta);
        ResourceVersionInfo info = requireVersionInfo(meta);
        String editing = info.getEditingVersion();
        
        if (StringUtils.isBlank(editing)) {
            // 回退：尝试 reviewingVersion（reviewed/draft 可删）
            String reviewing = info.getReviewingVersion();
            if (StringUtils.isBlank(reviewing)) {
                return;
            }
            AiResourceVersion rv =
                aiResourceVersionPersistService.find(namespaceId, name, type, reviewing);
            if (rv == null || (!AiResourceConstants.VERSION_STATUS_REVIEWED
                .equalsIgnoreCase(rv.getStatus())
                && !AiResourceConstants.VERSION_STATUS_DRAFT
                    .equalsIgnoreCase(rv.getStatus()))) {
                return;
            }
            info.setReviewingVersion(null);
            updateVersionInfoCas(namespaceId, meta, info);
            storageDeleter.deleteStorage(rv);
            deleteVersion(namespaceId, name, type, reviewing);
            AiResourceTraceService.logSuccess(type, name, reviewing,
                AiResourceTraceService.OP_DELETE_DRAFT,
                VisibilityHelper.resolveCurrentIdentity(),
                VisibilityHelper.resolveClientIp());
            return;
        }
        
        AiResourceVersion v =
            aiResourceVersionPersistService.find(namespaceId, name, type, editing);
        
        // 先清理 meta 指针
        info.setEditingVersion(null);
        updateVersionInfoCas(namespaceId, meta, info);
        
        // 仅 draft 状态才删除版本行与存储
        if (v != null && AiResourceConstants.VERSION_STATUS_DRAFT
            .equalsIgnoreCase(v.getStatus())) {
            storageDeleter.deleteStorage(v);
            deleteVersion(namespaceId, name, type, editing);
        }
        AiResourceTraceService.logSuccess(type, name, editing,
            AiResourceTraceService.OP_DELETE_DRAFT,
            VisibilityHelper.resolveCurrentIdentity(), VisibilityHelper.resolveClientIp());
    }
    
    /**
     * Handle pipeline completion: persist pipeline info and transition version status.
     * <p>流水线完成回调：持久化流水线信息并将版本置为 reviewed。</p>
     *
     * <p>Both approved and rejected results transition to {@code reviewed}. Users must explicitly
     * call redraft to return to draft.</p>
     */
    public void onPipelineComplete(String namespaceId, String name, String type, String version,
        PipelineExecutionResult result) {
        try {
            PublishPipelineInfo info = new PublishPipelineInfo();
            info.setExecutionId(result == null ? null : result.getExecutionId());
            info.setStatus(result == null ? PipelineExecutionStatus.REJECTED : result.getStatus());
            info.setPipeline(result == null ? null : result.getPipeline());
            aiResourceVersionPersistService.updatePublishPipelineInfo(namespaceId, name, type,
                version,
                JacksonUtils.toJson(info));
            
            boolean approved =
                result != null && result.getStatus() == PipelineExecutionStatus.APPROVED;
            
            aiResourceVersionPersistService.updateStatus(namespaceId, name, type, version,
                AiResourceConstants.VERSION_STATUS_REVIEWED);
            AiResourceTraceService.logSuccess(type, name, version,
                approved ? AiResourceTraceService.OP_REVIEW_APPROVED
                    : AiResourceTraceService.OP_REVIEW_REJECTED,
                "system", "", result == null ? null : result.getExecutionId());
        } catch (Throwable ex) {
            LOGGER.error("Pipeline callback failed for {}@{}", name, version, ex);
        }
    }
}
