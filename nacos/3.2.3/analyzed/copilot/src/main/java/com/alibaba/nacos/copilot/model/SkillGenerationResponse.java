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

/**
 * Skill 生成响应：流式返回生成进度、分片与最终 Skill 结果。
 * Skill generation response.
 *
 * @author nacos
 */
public class SkillGenerationResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应类型，如 thinking、tool_call、content、done。
     */
    private StreamResponseType type;
    
    /**
     * 内容分片（流式响应时使用）。
     */
    private String chunk;
    
    /**
     * 生成的 Skill（type 为 done 时附带）。
     */
    private Skill skill;
    
    /**
     * 生成说明（type 为 done 时附带）。
     */
    private String explanation;
    
    /**
     * 响应是否已完成。
     */
    private boolean done;
    
    public SkillGenerationResponse() {
    }
    
    public StreamResponseType getType() {
        return type;
    }
    
    public void setType(StreamResponseType type) {
        this.type = type;
    }
    
    public String getChunk() {
        return chunk;
    }
    
    public void setChunk(String chunk) {
        this.chunk = chunk;
    }
    
    public Skill getSkill() {
        return skill;
    }
    
    public void setSkill(Skill skill) {
        this.skill = skill;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public boolean isDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
}
