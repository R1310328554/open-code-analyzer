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

import java.io.Serializable;

/**
 * Prompt 优化响应：流式返回优化进度、分片内容与最终结果。
 * Prompt optimization response.
 *
 * @author nacos
 */
public class PromptOptimizationResponse implements Serializable {
    
    /**
     * 响应类型：THINKING、CONTENT、DONE。
     */
    private StreamResponseType type;
    
    /**
     * 内容分片（流式响应时使用）。
     */
    private String chunk;
    
    /**
     * 优化后的 Prompt（前端从累积内容中解析）。
     */
    private String optimizedPrompt;
    
    /**
     * 优化说明（type 为 done 时附带）。
     */
    private String explanation;
    
    /**
     * 响应是否已完成。
     */
    private boolean done;
    
    public PromptOptimizationResponse() {
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
    
    public String getOptimizedPrompt() {
        return optimizedPrompt;
    }
    
    public void setOptimizedPrompt(String optimizedPrompt) {
        this.optimizedPrompt = optimizedPrompt;
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
