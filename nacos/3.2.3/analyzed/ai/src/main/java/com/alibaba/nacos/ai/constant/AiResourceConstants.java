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

package com.alibaba.nacos.ai.constant;

/**
 * AI 资源操作（Skill、AgentSpec 等）的共享常量。
 *
 * <p>抽取原先在 {@code SkillOperationServiceImpl} 与
 * {@code AgentSpecOperationServiceImpl} 中重复定义的常量。</p>
 *
 * @author nacos
 */
public final class AiResourceConstants {
    
    private AiResourceConstants() {
    }
    
    /**
     * 元数据状态：资源已启用。
     */
    public static final String META_STATUS_ENABLE = "enable";
    
    /**
     * 元数据状态：资源已禁用。
     */
    public static final String META_STATUS_DISABLE = "disable";
    
    /**
     * 版本状态：已上线（已发布且生效）。
     */
    public static final String VERSION_STATUS_ONLINE = "online";
    
    /**
     * 版本状态：草稿（尚未提交）。
     */
    public static final String VERSION_STATUS_DRAFT = "draft";
    
    /**
     * 版本状态：审核中（流水线运行中）。
     */
    public static final String VERSION_STATUS_REVIEWING = "reviewing";
    
    /**
     * 版本状态：已审核（流水线通过，待发布）。
     */
    public static final String VERSION_STATUS_REVIEWED = "reviewed";
    
    /**
     * 版本状态：已下线。
     */
    public static final String VERSION_STATUS_OFFLINE = "offline";
    
    /**
     * 基于 CAS 的元数据更新操作最大重试次数。
     */
    public static final int MAX_WORKING_VERSION_RETRY = 3;
    
    /**
     * 指向最新已发布版本的标签键。
     */
    public static final String LABEL_LATEST = "latest";
}
