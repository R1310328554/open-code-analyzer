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

package com.alibaba.nacos.copilot.model;

import com.alibaba.nacos.api.ai.model.skills.Skill;

import java.io.Serializable;
import java.util.List;

/**
 * Skill 优化流式响应模型：封装分片类型、内容片段及完成时的优化结果摘要。
 * Skill optimization response.
 *
 * @author nacos
 */
public class SkillOptimizationResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 响应分片类型：thinking、tool_call、content、done。 */
    private StreamResponseType type;
    
    /** 流式内容片段，随 {@link StreamResponseType} 分片推送。 */
    private String chunk;
    
    /** 优化后的 {@link Skill} 对象，仅在 type 为 done 时填充。 */
    private Skill optimizedSkill;
    
    /** 优化变更明细列表，仅在完成阶段返回。 */
    private List<OptimizationChange> changes;
    
    /** 优化质量评分（0–1 或模型给出的相对分值），完成时可选返回。 */
    private Double qualityScore;
    
    /** 优化说明文本，解释改动动机与效果。 */
    private String explanation;
    
    /** 标识当前分片是否为流式响应的最后一帧。 */
    private boolean done;
    
    /** 无参构造，供 JSON 反序列化与框架实例化使用。 */
    public SkillOptimizationResponse() {
    }
    
    /** 获取响应分片类型。 */
    public StreamResponseType getType() {
        return type;
    }
    
    /** 设置响应分片类型。 */
    public void setType(StreamResponseType type) {
        this.type = type;
    }
    
    /** 获取当前内容片段。 */
    public String getChunk() {
        return chunk;
    }
    
    /** 设置当前内容片段。 */
    public void setChunk(String chunk) {
        this.chunk = chunk;
    }
    
    /** 获取优化后的 Skill。 */
    public Skill getOptimizedSkill() {
        return optimizedSkill;
    }
    
    /** 设置优化后的 Skill。 */
    public void setOptimizedSkill(Skill optimizedSkill) {
        this.optimizedSkill = optimizedSkill;
    }
    
    /** 获取优化变更列表。 */
    public List<OptimizationChange> getChanges() {
        return changes;
    }
    
    /** 设置优化变更列表。 */
    public void setChanges(List<OptimizationChange> changes) {
        this.changes = changes;
    }
    
    /** 获取质量评分。 */
    public Double getQualityScore() {
        return qualityScore;
    }
    
    /** 设置质量评分。 */
    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }
    
    /** 获取优化说明。 */
    public String getExplanation() {
        return explanation;
    }
    
    /** 设置优化说明。 */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    /** 是否已完成流式传输。 */
    public boolean isDone() {
        return done;
    }
    
    /** 设置流式完成标志。 */
    public void setDone(boolean done) {
        this.done = done;
    }
}
