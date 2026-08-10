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

package com.alibaba.nacos.ai.pipeline;

import com.alibaba.nacos.ai.pipeline.config.PipelineConfigProvider;
import com.alibaba.nacos.ai.pipeline.model.PipelineCallback;
import com.alibaba.nacos.ai.pipeline.model.PipelineConfig;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecution;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecutionResult;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecutionStatus;
import com.alibaba.nacos.ai.pipeline.model.PipelineNodeResult;
import com.alibaba.nacos.ai.pipeline.repository.PipelineExecutionRepository;
import com.alibaba.nacos.plugin.ai.pipeline.model.PublishPipelineContext;
import com.alibaba.nacos.plugin.ai.pipeline.model.PublishPipelineResourceType;
import com.alibaba.nacos.plugin.ai.pipeline.model.PublishPipelineResult;
import com.alibaba.nacos.plugin.ai.pipeline.spi.PublishPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Core pipeline execution engine. Asynchronously executes pipeline nodes in serial order,
 * persists execution state, and notifies the caller via callback.
 * <p>发布流水线核心执行引擎：按序异步执行各节点、持久化执行状态，完成后通过回调通知调用方。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public class PublishPipelineExecutor {
    
    private static final Logger LOG = LoggerFactory.getLogger(PublishPipelineExecutor.class);
    
    private final PublishPipelineManager pipelineManager;
    
    private final PipelineConfigProvider configProvider;
    
    private final PipelineExecutionRepository executionRepository;
    
    private final ExecutorService asyncExecutor;
    
    public PublishPipelineExecutor(PublishPipelineManager pipelineManager,
        PipelineConfigProvider configProvider,
        PipelineExecutionRepository executionRepository, ExecutorService asyncExecutor) {
        this.pipelineManager = pipelineManager;
        this.configProvider = configProvider;
        this.executionRepository = executionRepository;
        this.asyncExecutor = asyncExecutor;
    }
    
    /**
     * Asynchronously execute the pipeline.
     *
     * <ol>
     *   <li>Check config: if not enabled, return null (no record, no callback)</li>
     *   <li>Get matching services: if empty, return null (no record, no callback)</li>
     *   <li>Create PipelineExecution with IN_PROGRESS status and persist</li>
     *   <li>Return executionId immediately</li>
     *   <li>Submit async task to execute nodes serially, update state, and invoke callback</li>
     * </ol>
     * <p>异步执行发布流水线：校验配置与匹配节点后创建 IN_PROGRESS 记录并立即返回 executionId，后台串行执行各节点并回调。</p>
     *
     * @param context  pipeline context containing resource metadata
     * @param callback async callback, invoked exactly once when pipeline execution completes
     * @return executionId, or null if pipeline is not enabled or no matching nodes
     */
    public String execute(PublishPipelineContext context, PipelineCallback callback) {
        return execute(context, callback, UUID.randomUUID().toString());
    }
    
    /**
     * Asynchronously execute the pipeline with a pre-generated executionId.
     *
     * <p>Callers who need to write pipeline state (e.g. IN_PROGRESS) before the async task
     * starts should pre-generate an executionId and use this overload.</p>
     * <p>使用预生成的 executionId 异步执行流水线，便于调用方在异步任务启动前写入状态。</p>
     *
     * @param context     pipeline context containing resource metadata
     * @param callback    async callback, invoked exactly once when pipeline execution completes
     * @param executionId pre-generated execution identifier
     * @return executionId, or null if pipeline is not enabled or no matching nodes
     */
    public String execute(PublishPipelineContext context, PipelineCallback callback,
        String executionId) {
        // 步骤 1：检查流水线全局配置是否启用
        PipelineConfig config = configProvider.getConfig();
        if (!config.isEnabled()) {
            return null;
        }
        
        // 步骤 2：按资源类型与节点配置匹配 SPI 插件
        List<PublishPipelineService> services =
            pipelineManager.getPipelineServices(context.getResourceType(),
                config.getNodes());
        if (services.isEmpty()) {
            return null;
        }
        
        // 步骤 3：创建 IN_PROGRESS 执行记录并持久化
        long now = System.currentTimeMillis();
        
        PipelineExecution execution = new PipelineExecution();
        execution.setExecutionId(executionId);
        execution.setResourceType(context.getResourceType().name());
        execution.setResourceName(context.getResourceName());
        execution.setNamespaceId(context.getNamespaceId());
        execution.setVersion(context.getVersion());
        execution.setStatus(PipelineExecutionStatus.IN_PROGRESS);
        execution.setPipeline(new ArrayList<>());
        execution.setCreateTime(now);
        execution.setUpdateTime(now);
        
        try {
            executionRepository.save(execution);
        } catch (Exception e) {
            LOG.error("Failed to save initial pipeline execution record for executionId={}",
                executionId, e);
        }
        
        // 步骤 4：提交异步任务串行执行各节点
        asyncExecutor.submit(() -> {
            try {
                boolean allPassed = true;
                
                for (PublishPipelineService service : services) {
                    long startTime = System.currentTimeMillis();
                    String executedAt = Instant.now().toString();
                    PipelineNodeResult nodeResult = new PipelineNodeResult();
                    nodeResult.setNodeId(service.pipelineId());
                    nodeResult.setExecutedAt(executedAt);
                    
                    try {
                        PublishPipelineResult pipelineResult = service.execute(context);
                        long endTime = System.currentTimeMillis();
                        nodeResult.setPassed(pipelineResult.isPassed());
                        nodeResult.setMessage(pipelineResult.getMessage());
                        if (pipelineResult.getType() != null) {
                            nodeResult.setMessageType(pipelineResult.getType().getCode());
                        }
                        nodeResult.setCheckpoints(pipelineResult.getCheckpoints());
                        nodeResult.setDurationMs(endTime - startTime);
                        
                        if (!pipelineResult.isPassed()) {
                            allPassed = false;
                        }
                    } catch (Exception e) {
                        long endTime = System.currentTimeMillis();
                        nodeResult.setPassed(false);
                        nodeResult.setMessage(e.getMessage());
                        nodeResult.setDurationMs(endTime - startTime);
                        allPassed = false;
                    }
                    
                    execution.getPipeline().add(nodeResult);
                    execution.setUpdateTime(System.currentTimeMillis());
                    
                    try {
                        executionRepository.update(execution);
                    } catch (Exception e) {
                        LOG.error("Failed to update pipeline execution record for executionId={}",
                            executionId, e);
                    }
                    
                    if (!allPassed) {
                        break;
                    }
                }
                
                // 全部节点通过后设为 APPROVED，否则 REJECTED
                PipelineExecutionStatus finalStatus = allPassed
                    ? PipelineExecutionStatus.APPROVED : PipelineExecutionStatus.REJECTED;
                execution.setStatus(finalStatus);
                execution.setUpdateTime(System.currentTimeMillis());
                
                try {
                    executionRepository.update(execution);
                } catch (Exception e) {
                    LOG.error("Failed to update final pipeline execution status for executionId={}",
                        executionId, e);
                }
                
                // 组装结果并回调调用方（仅调用一次）
                PipelineExecutionResult result = new PipelineExecutionResult();
                result.setExecutionId(executionId);
                result.setStatus(finalStatus);
                result.setPipeline(execution.getPipeline());
                callback.onComplete(result);
            } catch (Exception e) {
                LOG.error("Unexpected error during pipeline execution for executionId={}",
                    executionId, e);
                // 意外异常时仍回调 REJECTED，避免调用方永久阻塞
                PipelineExecutionResult result = new PipelineExecutionResult();
                result.setExecutionId(executionId);
                result.setStatus(PipelineExecutionStatus.REJECTED);
                result.setPipeline(execution.getPipeline());
                callback.onComplete(result);
            }
        });
        
        return executionId;
    }
    
    /**
     * Read-only check: whether the pipeline is available for the given resource type.
     * <p>只读探测：流水线已启用且存在匹配该资源类型的节点时返回 true。</p>
     *
     * @param resourceType the resource type to check
     * @return true if pipeline is enabled and has matching service nodes
     */
    public boolean isPipelineAvailable(PublishPipelineResourceType resourceType) {
        PipelineConfig config = configProvider.getConfig();
        if (!config.isEnabled()) {
            return false;
        }
        List<PublishPipelineService> services =
            pipelineManager.getPipelineServices(resourceType, config.getNodes());
        return !services.isEmpty();
    }
}
