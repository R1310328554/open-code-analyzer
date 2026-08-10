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

package com.alibaba.nacos.api.ai.model.mcp;

/**
 * MCP 工具注解，向客户端提供描述工具行为的附加提示属性。
 *
 * <p>注意：ToolAnnotations 中所有属性均为<b>提示性</b>信息，
 * 不保证与实际工具行为完全一致。客户端切勿仅依据来自不可信服务端的
 * ToolAnnotations 做出工具调用决策。</p>
 *
 * @author xiweng.yy
 */
public class McpToolAnnotations {
    
    /** 工具的人类可读标题。 */
    private String title;
    
    /** 若为 true，表示工具不修改其运行环境；默认 false。 */
    private Boolean readOnlyHint;
    
    /**
     * 若为 true，工具可能对环境做破坏性更新；为 false 时仅做增量更新。
     * （仅当 readOnlyHint 为 false 时此属性有意义；默认 true。）
     */
    private Boolean destructiveHint;
    
    /**
     * 若为 true，以相同参数重复调用不会产生额外环境副作用。
     * （仅当 readOnlyHint 为 false 时此属性有意义；默认 false。）
     */
    private Boolean idempotentHint;
    
    /**
     * 若为 true，工具可能与外部开放实体交互；为 false 时交互域封闭；默认 true。
     */
    private Boolean openWorldHint;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Boolean getReadOnlyHint() {
        return readOnlyHint;
    }
    
    public void setReadOnlyHint(Boolean readOnlyHint) {
        this.readOnlyHint = readOnlyHint;
    }
    
    public Boolean getDestructiveHint() {
        return destructiveHint;
    }
    
    public void setDestructiveHint(Boolean destructiveHint) {
        this.destructiveHint = destructiveHint;
    }
    
    public Boolean getIdempotentHint() {
        return idempotentHint;
    }
    
    public void setIdempotentHint(Boolean idempotentHint) {
        this.idempotentHint = idempotentHint;
    }
    
    public Boolean getOpenWorldHint() {
        return openWorldHint;
    }
    
    public void setOpenWorldHint(Boolean openWorldHint) {
        this.openWorldHint = openWorldHint;
    }
}
