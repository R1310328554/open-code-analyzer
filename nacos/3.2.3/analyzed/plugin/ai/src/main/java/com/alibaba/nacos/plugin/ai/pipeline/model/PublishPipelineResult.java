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

package com.alibaba.nacos.plugin.ai.pipeline.model;

import java.util.List;

/**
 * 单个发布流水线插件执行后的审核结果。
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public class PublishPipelineResult {
    
    /**
     * 审核是否通过。流水线引擎据此决定是否继续执行下一个插件。
     */
    private boolean passed;
    
    /**
     * 审核消息，可包含审核意见、改进建议、错误描述等。
     * 当 {@link #passed} 为 {@code false} 时应说明拒绝原因。
     */
    private String message;
    
    /**
     * {@link #message} 的语义类型（例如 skill-scanner 输出的 Markdown 报告）。
     */
    private PublishPipelineMessageType type;
    
    /**
     * 本次插件运行各检查项的审计结果列表。
     */
    private List<Checkpoint> checkpoints;
    
    /** 无参构造。 */
    public PublishPipelineResult() {
    }
    
    /**
     * 构造审核结果，消息类型默认为纯文本。
     *
     * @param passed  是否通过
     * @param message 审核消息
     */
    public PublishPipelineResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
        this.type = PublishPipelineMessageType.TEXT;
    }
    
    /**
     * 创建通过结果，消息按纯文本处理。
     *
     * @param message 通过说明
     * @return 审核通过结果
     */
    public static PublishPipelineResult pass(String message) {
        return pass(message, PublishPipelineMessageType.TEXT, null);
    }
    
    /**
     * 创建通过结果，可指定消息类型与检查点列表。
     *
     * @param message      通过说明
     * @param type         消息语义类型
     * @param checkpoints  各检查项审计结果
     * @return 审核通过结果
     */
    public static PublishPipelineResult pass(String message, PublishPipelineMessageType type,
        List<Checkpoint> checkpoints) {
        PublishPipelineResult r = new PublishPipelineResult();
        r.passed = true;
        r.message = message;
        r.type = type != null ? type : PublishPipelineMessageType.TEXT;
        r.checkpoints = checkpoints;
        return r;
    }
    
    /**
     * 创建拒绝结果，消息按纯文本处理。
     *
     * @param message 拒绝原因
     * @return 审核拒绝结果
     */
    public static PublishPipelineResult reject(String message) {
        return reject(message, PublishPipelineMessageType.TEXT, null);
    }
    
    /**
     * 创建拒绝结果，可指定消息类型与检查点列表。
     *
     * @param message      拒绝原因
     * @param type         消息语义类型
     * @param checkpoints  各检查项审计结果
     * @return 审核拒绝结果
     */
    public static PublishPipelineResult reject(String message, PublishPipelineMessageType type,
        List<Checkpoint> checkpoints) {
        PublishPipelineResult r = new PublishPipelineResult();
        r.passed = false;
        r.message = message;
        r.type = type != null ? type : PublishPipelineMessageType.TEXT;
        r.checkpoints = checkpoints;
        return r;
    }
    
    /** @return 是否通过审核 */
    public boolean isPassed() {
        return passed;
    }
    
    /** @param passed 是否通过审核 */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    
    /** @return 审核消息正文 */
    public String getMessage() {
        return message;
    }
    
    /** @param message 审核消息正文 */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /** @return 消息语义类型 */
    public PublishPipelineMessageType getType() {
        return type;
    }
    
    /** @param type 消息语义类型 */
    public void setType(PublishPipelineMessageType type) {
        this.type = type;
    }
    
    /** @return 各检查项审计结果 */
    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }
    
    /** @param checkpoints 各检查项审计结果 */
    public void setCheckpoints(List<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }
    
    @Override
    public String toString() {
        return "PublishPipelineResult{passed=" + passed + ", message='" + message + "', type="
            + type
            + ", checkpoints=" + checkpoints + "}";
    }
}
