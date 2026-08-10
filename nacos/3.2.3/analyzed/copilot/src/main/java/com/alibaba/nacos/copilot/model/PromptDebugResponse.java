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
 * Prompt 调试响应：流式返回思考过程、内容分片与完成状态。
 * Prompt debug response.
 *
 * @author nacos
 */
public class PromptDebugResponse implements Serializable {
    
    /**
     * 响应类型：THINKING、CONTENT、DONE。
     */
    private StreamResponseType type;
    
    /**
     * 内容分片（流式响应时使用）。
     */
    private String chunk;
    
    /**
     * 响应是否已完成。
     */
    private boolean done;
    
    public PromptDebugResponse() {
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
    
    public boolean isDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
}
